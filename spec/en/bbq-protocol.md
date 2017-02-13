# Background broadcast query protocol (BBQ)

BBQ is a protocol designed to allow an Android app to simultaneously request
data from multiple providers on the device. Requests and responses are sent
as targeted broadcast messages, using
[protocol buffer (v2) messages][protobuf] to encode the request and response
data. The use of broadcast messages allows implementations to be fully
asynchronous, and protocol buffers allow messages to be compact and efficient,
while avoiding common issues with custom Parcelable types.

## Structure of a request

A broadcast query has the following mandatory properties:

- The data type being requested, described with
  [reverse domain name notation][reverse-domain], as is
  typically used for package names and intent actions in Android.
  For example, `org.openyolo.credential`.
- The package name of the requesting app, e.g. `com.example.app`.
- A randomly generated, 64-bit request ID. This is used to distinguish the
  request from other requests with the same data type that may not have been
  fully resolved.
- A randomly generated, 64-bit response ID. A separate response ID is generated
  for each expected responder, allowing responders to be distinguished and their
  identity to be recovered from the mapping of response ID to package name that
  is created prior to sending the request.
- A description of the cryptographic scheme used for the exchange:
    - A string that uniquely identifies the scheme.
    - A set of key-value pairs carrying scheme-specific parameters.

An additional data-type specific message can be carried in the request if
necessary, in the form of a byte-array (typically an encoded protocol buffer).
Additional parameters can also be encoded into the message as
key-value pairs, allowing for extension of the protocol itself.

The request is encoded using a [protocol buffer (v2) message][protobuf].
Following the current guidance for protocol buffer message definitions, all
fields are marked `optional` or `repeated` to provide flexibility in changing
this specification in future. Fields currently considered to be required are
marked as such with a comment.

```protobuf
message BroadcastQuery {
  optional string       dataType         = 1; // required
  optional string       requestingApp    = 2; // required
  optional sfixed64     requestId        = 3; // required
  optional sfixed64     responseId       = 4; // required
  optional bytes        queryMessage     = 5;
  repeated Parameter    additionalParams = 6;
}

message Parameter {
	optional string name  = 1; // required
	optional bytes  value = 2;
}
```

## Dispatching a request

A request is dispatched as one or more targeted broadcast intents. First, the
requester uses the Android [PackageManager][pm-api] API to determine the set
of apps which can provide data of the required type:

```java
Intent intent = new Intent(dataType);
intent.addCategory(BBQ_CATEGORY);
List<ResolveInfo> responderInfos =
    packageManager.queryBroadcastReceivers(intent, 0);
```

A separate request is created for each potential responder, with a unique
response ID, and sent as a targeted broadcast:

```java
BroadcastQuery query = new BroadcastQuery.Builder()
    /* ... */
    .setResponseId(idForResponder.get(responder))
    .build();
Intent bbqIntent = new Intent(dataType);
bbqIntent.setPackage(responder);
bbqIntent.setExtra(EXTRA_QUERY_MESSAGE, query.encode());
context.sendBroadcast(bbqIntent);
```

## Structure of a response

A broadcast query response has the following mandatory properties:

- The 64-bit request ID that the response is associated with, copied from the
  request.
- The 64-bit response ID unique to this response, copied from the request.

Query responses are also represented as a V2 protocol buffer messages.
The response copies the request and response IDs from the request message,
and may include a data-type specific response message, if necessary.
The absence of a data-type specific response message is generally interpreted
to mean that the provider is unable to service the request.

The structure of the query response message is therefore as follows:

```protobuf
message BroadcastQueryResponse {
  optional sfixed64  requestId             = 1; // required
  optional sfixed64  responseId            = 2; // required
  optional bytes     responseMessage       = 3;
  repeated Parameter additionalParams      = 4;
}
```

## Receiving a response

Responses are sent back to the requester in the form of targeted broadcasts.
The requester dynamically registers a broadcast receiver to capture
responses. The _ACTION_ for the response is the requested data type with the
request ID concatenated in zero-padded hex form, e.g.
"org.openyolo.credential:000000000000CAFE" where "org.openyolo.credential" is
the requested data type and "000000000000CAFE" is the request ID (51966 in
decimal).

```java
IntentFilter filter = new IntentFilter();
filter.addAction(encodeAction(dataType, requestId));
filter.addCategory(BBQ_CATEGORY);
context.registerReceiver(new BroadcastReciever() { ... }, filter);
```

In order to avoid waiting indefinitely for responses from faulty receivers,
a timeout should be used, after which absent responses should be treated as
though the provider was unable to service the request (equivalent to
responding with no data-type specific message payload).

## Rooted devices

The BBQ protocol relies on the integrity of the Android broadcast system
(and the `setPackage` mechanism in particular) to guarantee the privacy of the
messages sent between a requester and a provider. On a rooted device, it is
potentially possible for a malicious app or system service with root access to
read these messages, and expose plain-text passwords.

Cryptography provides no additional protection. If an attacker can read the
private messages sent via the broadcast system, this will typically imply they
have access to the memory location of the buffers. If ephemeral public-private
key pairs are used, which don't authenticate either party, a man-in-the-middle
attack is possible.

There is no trusted third party which can sign keys to
prove they are associated to a particular app:

- Key pairs cannot be distributed with the app, as they could be easily
  extracted from the application in advance, or on-demand with
  root access on the device.

- Keys cannot be dynamically signed by a trusted entity on the device (such as
  the platform itself, or Google Play Services) as these exchanges
  would also be susceptible to attack by anything with root access.

As such, we strongly recommend that password manager apps disable the OpenYOLO
protocol on rooted devices, if they are able to detect this. The option could
be given to the user to re-enable the feature, with a warning as to the security
risks of doing this. Generally, rooted devices are very risky to a user's
security, so warning users of this fact prior to even allowing a password
manager to be configured is also advisable, as the following attacks are
also possible:

- Directly reading keys and passwords from the memory space of the password
  manager or app
- Scraping the contents of EditText instances for passwords
- Key-logging the user
- Injecting code into the process space of the password manager or app

The authors of this specification have no evidence that the kernel modifications
required to break this protocol exist on real devices, but they are certainly
feasible. As such, all rooted devices should be treated with suspicion when
using the BBQ protocol to transport security- or privacy-sensitive data.

[pm-api]: https://developer.android.com/reference/android/content/pm/PackageManager.html "android.content.pm.PackageManager"
[protobuf]: https://developers.google.com/protocol-buffers
[reverse-domain]: https://en.wikipedia.org/wiki/Reverse_domain_name_notation
