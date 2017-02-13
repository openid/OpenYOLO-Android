# OpenYOLO overview

The OpenYOLO protocol defines three core operations to assist with
authentication in an Android app:

- Retrieving an existing credential: An app can attempt to retrieve a
  credential to authenticate a user. In most cases, a user can be
  automatically signed-in using a credential retrieved using OpenYOLO.

- Assisting with sign-up: In the case where no credential can be retrieved
  by OpenYOLO, a request can be made to a provider to retrieve information
  that can be used to create a new account, including generating a unique
  password if required. The entire sign-up process of the app can often be
  replaced with a single tap when using this API, producing a basic user profile
  and unique password suitable for use with the application.

- Saving a credential: An app can attempt to save a credential that
  has been used to successfully authenticate. With user permission, this
  credential will be saved to the provider of their choice. Saving the
  credential facilitates future authentication attempts through the retrieve
  operation, and is particularly useful when the credential provider supports
  cross-device synchronization.

## High level concepts

Before providing a high level overview of the OpenYOLO operations, some
terms that will be used throughout the discussion must be defined.

### Credential

A _credential_ is defined in OpenYOLO to be composed of the following
properties:

- A required _authentication domain_, where the credential can be used.

- A required _authentication method_, which describes how to verify the
  credential.

- A required _identifier_ which designates a specific account in the context of
  both the authentication domain and method. Typically, identifiers are
  email addresses, phone numbers, or some other unique account identifier
  associated with a federated identity provider.

- An optional _password_, which can be used for password-based authentication
  methods.

- An optional _display name_, used to assist the user in identifying
  credentials. Typically, the display name for a credential is the user's
  real name or chosen alias.

- An optional _display picture_, used to assist the user in visually identifying
  credentials. Typically, the display picture is a picture of the user, or
  an avatar that they have chosen or been assigned.

Additional properties can also be optionally specified for a credential,
however a credential provider is not _required_ to store or return any optional
values. As such, the existence of these values should not be relied upon by
apps which consume credentials from OpenYOLO.

A credential is represented in OpenYOLO using a
[protocol buffer (v2) message][protobuf], defined as follows:

```protobuf
message Credential {
    optional string       id                    = 1; // required
    optional string       authDomain            = 2; // required
    optional string       authMethod            = 3; // required
    optional string       displayName           = 4;
    optional string       displayPictureUri     = 5;
    optional string       password              = 6;
    repeated KeyValuePair additionalProps       = 7;
}
```

### Credential providers

A _credential provider_ is defined in OpenYOLO to be any application which
provides the required broadcast receivers and activity intent filters to
handle all OpenYOLO operations. Credential providers are typically one of
the following:

- Dedicated apps whose sole purpose is to store and protect credentials a
  user has chosen to store.

- Browsers which save and form-fill credentials. Many sites have dedicated
  Android applications which can benefit from retrieving credentials that are
  already stored in the browser.

- System services such as Smart Lock for Passwords which can store credentials
  for the user in the absence of other installed alternatives.

While any app can become a credential provider by supporting the
required OpenYOLO endpoints, only apps for which credential management is a
clearly declared and visible feature to the user should become credential
providers. [Protecting users](protecting-users.md) describes these criteria
in more detail, and the technical counter-measures that are taken to make it
difficult for inappropriate or malicious apps to be used as credential
providers.

### Authentication domains

An _authentication domain_ is defined in OpenYOLO to be a scope within
which a credential is considered to be usable. Authentication domains are
represented as absolute, hierarchical URIs of form `scheme://authority` -
no path, query or fragment is permitted.

Two forms of authentication domain are defined for OpenYOLO:

- Android authentication domains, of form `android://FINGERPRINT@PACKAGE` where
  `PACKAGE` is the package name of an app and `FINGERPRINT` is a
  Base64, URL-safe encoding of the app's public key (provided by
  the [Signature][signature-class] type in Android). The fingerprint string
  includes both the hash used, and the hash data, e.g.
  `sha512-7fmduHKTdHHrlMvldlEqAIlSfii1tl35bxj1OXN5Ve8c4lU6URVu4xtSHc3BVZxS6WWJnxMDhIfQN0N0K2NDJg==`.
  The two fingerprint hash algorithms officially supported by all OpenYOLO
  providers are `sha256` and `sha512`.

- Web authentication domains, which match the domain of the site and can have
  either a http or https scheme (e.g. `https://example.com` and
  `http://www.example.com` are valid web authentication domains).

A single _authentication system_, which maintains and validates credentials,
may be represented by multiple distinct authentication domains. For example,
a credential for `android://...@com.example.app` may be usable on
`https://example.com` or `https://www.example.com`, when these three entities
all use the same authentication system.
However, it is important that `android://HASH-A@com.example.app` and
`android://HASH-B@com.example.app` should not be treated as equivalent
_automatically_ - either could represent a compromised, side-loaded variant of
an app that is attempting to steal user credentials.

An authentication domain _equivalence class_ defines the set of authentication
domains across which a given credential can be freely shared. Such equivalence
classes improve the usability of OpenYOLO, but must be carefully defined to
avoid compromising the security of a user's credentials.

Determining which apps and sites form an equivalence class is the
responsibility of a credential provider implementation, and need not be
consistent between credential provider implementations. Google's
[Digital Asset Links API][asset-links] provides a data source of declared,
verifiable relationships between apps and sites that can be used to construct
an authentication domain equivalence class for a given credential.
The [OpenYOLO SPI](spi-overview.md) will provide utilities to help in using
the digital asset links API, though credential providers are free to use any
other data source.

### Authentication method

An _authentication method_ is a mechanism by which a user credential can be
verified, and is given a unique URI identifier. Any URI of form
`scheme://authority` can be used to describe an authentication method; OpenYOLO
defines some standard URIs for the two most common types of authentication
method:

- Identifier and password based authentication under the direct control of the
  app, where the identifier and password are non-empty strings composed of
  printable ASCII characters. The URI for this authentication method is
  standardized as `openyolo://id-and-password`.

- Federated credentials (e.g. OpenID Connect), where a user identifier is
  passed to an identity provider (that is typically not controlled by the app).
  with some supporting information that identifies the app. The URI
  used to denote authentication with an identity provider is based upon the
  origin to which federated requests are typically sent for that provider.
  For example, the URI that should be used for Google Sign-in accounts is
  `https://accounts.google.com`, while the URI that should be used for
  Facebook Sign-in accounts is `https://www.facebook.com`. Other common
  federated identity provider URIs are defined in the OpenYOLO specification.
  Any authentication mechanism with protocol http or https is assumed to
  represent an federated identity provider.

## Retrieving credentials

Credential retrieval requests are dispatched using the
[BBQ protocol](bbq-protocol.md) to all credential providers on the device
simultaneously. This is particularly useful for users who have more than one
credential provider, such as Smart Lock for Passwords (present on all devices
with Google apps), and Dashlane, which the user has chosen to install. Such
users are often in a state where disjoint sets of credentials are stored in
each, so querying both increases the chance that a credential can be found.

The BBQ protocol uses [Android broadcast intents][intent-overview] with
recipients specified by package name in order to asynchronously deliver
requests and responses. A retrieval request carries the following information:

- A set of authentication domains from which a credential is required.

- The set of authentication mechanisms that are supported by the requester.

It is recommended that an app send a credential request whenever a user would
typically be required to sign in manually, and _before_ any login UI is shown.
Some intermediate UI (such as a loading screen) can be displayed while waiting
for a response. If a credential is available, the entire manual sign-in flow
can be skipped, resulting in an improved user experience.

Retrieval responses may carry an intent that can be used to retrieve a
credential from a provider. Providers respond with no intent if they know that
they do not have a credential for the provider, or if they refuse to service
the request. Providers _may_ respond with an intent even if they do not know
that they have a credential available: providers which use a master password
to encrypt their stores which is not stored to disk may require the user to
take an action to unlock the store before an accurate answer can be determined.

The OpenYOLO API will return a single intent that can be dispatched with
[startActivityForResult][intent-results], and returns the selected credential
data (if any) via `onActivityResult`.

For the specifics of this flow, see
[Retrieving Credentials](retrieving-credentials.md).

## Assisting sign-up

If no existing credentials can be retrieved from a credential provider,
then OpenYOLO provides a fall-back mechanism that can be used to help in
creating a new account. This mechanism will typically allow a new user
account to be created without the need to manually enter any information.

First, the app must provide a descriptor of the types of credentials that
it can support. This is done by providing a list of one or more
supported authentication methods. If password authentication is supported,
then a _password specification_ can optionally be provided that describes
the set of passwords that the app supports.

This descriptor can then be sent to a credential provider using the OpenYOLO
API in order to derive a credential hint. If a default credential provider can
be determined by the OpenYOLO API, then it will construct an intent to send the
descriptor to this provider and return it for the app to dispatch when ready.
Similarly, if only one credential provider is available on the device and it is
a "known" provider, then an intent to directly interact with that provider will
be constructed and returned. See [Protecting users](protecting-users.md) for
details on default and known providers.

If no default provider is found or multiple providers exist, an intent
is constructed for a dialog that will allow the user to choose a provider,
after which an intent will be dispatched to that provider containing the
descriptor.

The flow for creating a credential hint based on the descriptor is under the
control of the provider, and not part of this specification. A hint constructed
by the provider is returned to the app via the intent data carried by
`onActivityResult`.

For more detail on this flow, see [Assisting sign-up](assisting-sign-up.md).

## Saving credentials

When a user successfully authenticates with an app, either manually or via
the OpenYOLO retrieve or assisted sign-up flows, this credential should be
saved for future use. Manually saving credentials to a provider is frustrating
and error-prone - the user must manually switch to the credential provider,
and follow the provider-specific flow to manually enter their credential again
for storage, with the possibility of typographical errors.

A significantly better user experience can be provided if the credential can
be saved in-context, just after it has been verified by an app. OpenYOLO
provides the mechanism to achieve this.

After a credential has been verified by an app, it should construct a
representation of the credential to be saved by a credential provider. This
can then be sent to be stored using the OpenYOLO aPI.

If a preferred credential provider can be determined by the OpenYOLO API, then
it will construct an intent to send the save request to the provider,
carrying the plain-text credential data in an intent extra. Similarly, if
only one credential provider is available on the device and it is a "known"
provider, the intent to directly interact with that provider will be
constructed. See [Protecting users](protecting-users.md) for details on
default and known providers.

If no preferred provider is found, an intent is constructed for a dialog that
will allow the user to choose a provider, after which an intent will be
dispatched to that provider containing the credential.

The flow for saving the credential past this point is under the control of
the provider, and not part of this specification. The outcome of the save
operation (success or failure) is communicated back to the app via a result
code to `onActivityResult`.

For more detail on this flow, see [Saving credentials](saving-credentials.md).

## Protecting users from malicious providers

Given the sensitive nature of the data being exchanged by OpenYOLO, the protocol
will become an obvious target for attackers. A probable attack will be for
malicious apps to register themselves as credential providers, and attempt to
trick users into providing credentials. Distinguishing legitimate credential
provider apps from malicious apps is therefore an important aspect of building
trust in the protocol, for both app developers and users.

In order to achieve this, a "known provider" list will be hosted by the
OpenID Foundation. A static version of this list is included in the OpenYOLO
API, and is automatically updated by client library when necessary.

An "unknown" provider will still be usable via the protocol. Prior to any
interaction with an unknown provider, a dialog will be displayed
warning the user that this provider is unknown, and that they should only
proceed if they trust the provider. "Known" providers will not have this
restriction.

Additionally, on devices with Google Play Services, further protections can
be offered. Play Services will provide a secure host for user preferences
on the majority of Android devices. A new API for Google Play Services is
proposed that will:

- Specify which providers are enabled. This will require that providers
  explicitly "opt-in" by directing the user to a settings page to enable
  the provider, typically after installing the provider. Many credential
  providers already include such a flow in order to enable themselves as an
  "accessibility service" in order to perform form-fill.

  If a provider is not on this explicit whitelist, the OpenYOLO API
  will not even attempt to communicate with it. This protects the user in a much
  more direct way, avoiding the possibility of a user accidentally interacting
  with a provider.

- Specify a "default" provider. This is the provider that will be automatically
  used for save requests and hint requests, removing the need to show a
  "provider picker" dialog prior to these operations.

Technical information on the known provider list and the Play Services API
can be found in the chapter on [protecting users](protecting-users.md).


[asset-links]: https://developers.google.com/digital-asset-links/
[cancel-result]: https://developer.android.com/reference/android/app/Activity.html#RESULT_CANCELED
[intent-results]: https://developer.android.com/training/basics/intents/result.html
[intent-overview]: https://developer.android.com/guide/components/intents-filters.html
[protobuf]: https://developers.google.com/protocol-buffers
[signature-class]: https://developer.android.com/reference/android/content/pm/Signature.html
