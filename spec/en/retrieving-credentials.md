# Retrieving credentials

NOTE: writing this chapter is *in progress*.

## Credential

## Retrieve request messages

```protobuf
message FindRequest {
    repeated string       authDomains        = 1; // at least one required
    repeated string       authMethods        = 2; // at least one required
    repeated KeyValuePair additionalParams   = 3;
}
```

The set of authentication domains specified in the request _may_ be disjoint
from the set of domains known to be related to the requester (via its package
name): such requests would allow trusted intermediaries (e.g. keyboard apps,
mail clients, etc.) to request credentials that they do not directly own.
It is the responsibility of the credential provider to determine whether to
permit such requests. Typically, requests should only be permitted for
an authentication domain that is provably associated with to the requester,
or if the app has been explicitly whitelisted (by the user or provider) to
request credentials outside its equivalence class.

## Retrieve response messages

```protobuf
message FindResponse {
    optional bytes        retrieveIntent   = 1; // required
    repeated KeyValuePair additionalParams = 2;
}
```

Providers indicate whether they may be able to provide a credential to the
requester by responding with a message that optionally contains an activity
[Intent][intent-class]
to retrieve the credential. The absence of an intent in the response indicates
that the provider knows that it does not have an available credential, or is
refusing to serve the request.

An activity intent is used for the final stage of retrieving the request to
allow the credential provider to interact with the user in some way before
releasing the credential. Many credential providers will require an unlock code
(a PIN number, password or recognized fingerprint) in order to decrypt and
release the credential, or may simply wish to notify the user that the
credential is being released to avoid surprising the user.

If the requester receives more than one Intent-carrying response, the user
should be prompted to choose between the available options. If no
intent-carrying responses are received, then the requester should proceed to
a manual sign-in.

## Retrieve intent responses

The intent should be dispatched using [startActivityForResult][intent-results],
allowing the response to be delivered to via `onActivityResult`. The provider
can describe two outcomes to this process:

- The operation was canceled (indicated by
  [ACTIVITY_CANCELED][result-canceled]). This can occur as a result of the user
  failing to enter their unlock code correctly, or explicitly canceling the
  flow.
- The operation succeeded (indicated by [ACTIVITY_OK][result-ok]), and a
  credential is carried in the response. The credential is encrypted using the
  shared secret established by the BBQ protocol.

When provided with a credential, the application should immediately attempt to
use this credential, and should do so without requiring any additional user
input (e.g. pressing a "sign in" button).

[intent-class]: https://developer.android.com/reference/android/content/Intent.html
[intent-results]: https://developer.android.com/training/basics/intents/result.html
[result-canceled]: https://developer.android.com/reference/android/app/Activity.html#RESULT_CANCELED
[result-ok]: https://developer.android.com/reference/android/app/Activity.html#RESULT_OK
