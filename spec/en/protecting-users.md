# Protecting users from malicious providers

OpenYOLO offers a novel mechanism for direct credential exchange between apps
and credential providers, which can dramatically simplify the sign-in and
sign-up experience. User understanding of the feature, and in particular its
security implications, will also be low. It is therefore important that
technical measures are taken to prevent the user from inadvertently compromising
the security of their credentials.

Two mechanisms are proposed in order to achieve this:

- A known provider list, which can be utilized by the client library in order
  to warn a user before it interacts with an unknown provider. This will work
  on any Android device.
- A supporting Google Play Services API, which will store an explicit
  whitelist of providers that the user has selected, and their "default"
  provider for the save and hint flows. This will only work on devices with
  Google Play Services - the majority, but not all, Android devices. The API
  is therefore designed to provide additional security when available, but not
  to be essential to the functioning of OpenYOLO.

## The known provider list

The "known provider" list will be retrievable from
(*TODO: URL TO BE DETERMINED*), and served from infrastructure
controlled by the OpenID Foundation. This resource has mime-type
"application/json", is UTF-8 encoded, and contains a list of JSON objects that
describe each provider via the following properties:

- "name": The canonical, human-readable name of the provider (e.g.
  "Smart Lock for Passwords")
- "package-identifiers": a list of one or more strings in the format used for
  authentication domains, that identifies the legitimate app package name(s)
  and signature(s) for this provider. For instance, a provider may have multiple
  signatures due to key rotation, and multiple package names if they release
  "beta" / "alpha" channels of their app under different package names, to
  allow concurrent installation.
- "store-link": a canonical link for the app. It must be possible for the
  maintainers of the known app list to verify the app's signature using this
  link. Google Play Store links are preferred, but other stores may be
  referenced for apps that are targeted at specific demographics
  (e.g. the Chinese market).

For example, such a list may look like:

```json
[
  {
    "name": "Firefox",
    "package-identifiers": [
      "android://sha256-2gCe6pR_AO_Q2Vu8Iep-4AsiKNnUHQxu0FaDHO_qa178GByKybdT_BuE8_dYk99G5Uvx_gdONXAOO2EaXidpVQ==@org.mozilla.firefox",
      "android://sha256-2gCe6pR_AO_Q2Vu8Iep-4AsiKNnUHQxu0FaDHO_qa178GByKybdT_BuE8_dYk99G5Uvx_gdONXAOO2EaXidpVQ==@org.mozilla.firefox_beta"
    ],
    "store-link": "https://play.google.com/store/apps/details?id=org.mozilla.firefox"
  },
  //
  // ...
  //
  {
    "name": "LastPass",
    "package-identifiers": [
      "android://sha256-d5XXKGMGcVvMZ7bw3-Aotgq035ClbqO7RwDQG7x6P7ofwLxW42VRYL8jScbFfyW7hLyXYZEmrPrPsYqkJfDeNQ==@com.lastpass.lpandroid"
    ],
    "store-link": "https://www.amazon.com/dp/B005V2S7FW",
  },
  {
    "name": "Smart Lock for Passwords",
    "package-identifiers": [
      "android://sha256-7fmduHKTdHHrlMvldlEqAIlSfii1tl35bxj1OXN5Ve8c4lU6URVu4xtSHc3BVZxS6WWJnxMDhIfQN0N0K2NDJg==@com.google.android.gms"
    ],
    "store-link": "https://play.google.com/store/apps/details?id=com.google.android.gms",
  },
]
```

### Inclusion and exclusion criteria

The criteria for an provider to be included in the known list are as follows:

- The app must be published on at least one major app store, as determined by
  the OpenYOLO repository owners.
- The app must clearly be a credential provider of some form: either a
  dedicated app, or an app that clearly states its ability to store passwords
  (browsers typically fall into this category).
- A point of contact (email) must be designated for the app for notification
  of security issues, and acknowledgment of security issues via this address
  must occur in a timely manner (e.g. 1 business day for critical issues).

Only "production" package identifiers will be permitted in the list: apps
compiled with debug keys or those that are not published in a manner accessible
to end users will not be included.

An app may be removed from the list for the following reasons:

- An actively exploited security issue with high user impact is discovered.
- If an adequate plan to fix a lower impact security issue is not sent in
  response to a query via the point of contact in a timely manner (e.g.
  10 business days).
- If a plan to fix a security issue is not adhered to, leaving an app
  exploitable with no steps towards resolution.

Examples of security issues with high user impact are:

- The provider does not correctly construct an authentication domain equivalence
  class, resulting in one or more apps being able to retrieve credentials to
  which they should not have access.
- An exploit exists allowing an app to corrupt the provider data store
  with a save credential request, resulting in loss of user credential data.
- A code injection attack on a provider from data sent via the OpenYOLO
  protocol.

A provider will be reinstated to the known provider list after critical issues
are resolved and the updated app has been available in app stores long enough
for most user devices to have updated.

To submit your application for inclusion in the known provider list, an issue
must be opened on the OpenYOLO repository.

## The Google Play Services "credentialproviders" API

*NOTE*: The API described in this section is a proposal, that may or may not
be implemented in this form or at all.

The BBQ and OpenYOLO protocols are designed to work without the need for a
"trusted" intermediary, allowing it to work on any Android device.
A trusted intermediary would, however, be very useful in providing additional
protection against attackers. Similar to root certificate authorities for
transport layer security, a mutually trusted party allows otherwise
independent entities to establish mutual trust.

Most Android devices at API level 9 and above have the suite of Google apps
installed, including Google Play Services, which provides developer APIs
independently of the base platform. Google Play Services is a viable candidate
for a trusted intermediary for OpenYOLO, providing security benefits
for the OpenYOLO API when available.

A "credentialproviders" support API is proposed that will provide the following
functionality:

```java
package com.google.android.gms.auth.credentialproviders;

import com.google.android.gms.tasks.Task;

public interface CredentialProvidersApi {

  /**
   * Provides the (potentially empty) list of credential providers that the
   * user has selected.
   * Each provider is identified by a URI of form
   * {@code android://SIGNATURE@PACKAGE}, where:
   *
   * <ul>
   * <li>
   * {@code SIGNATURE} is a url-safe Base64 encoding of the SHA-512 hash of the
   * {@link Signature} object associated with the app.
   * </li>
   * <li>
   * {@code PACKAGE} is the package name of the app.
   * </li>
   */
  Task<List<String>> getProviders();


  /**
   * Provides the user's default credential provider, if defined. The provider
   * is identified by a URI of form {@code android://SIGNATURE@PACKAGE}, where:
   *
   * <ul>
   * <li>
   * {@code SIGNATURE} is a url-safe Base64 encoding of the SHA-512 hash of the
   * {@link Signature} object associated with the app.
   * </li>
   * <li>
   * {@code PACKAGE} is the package name of the app.
   * </li>
   */
  Task<String> getDefaultProvider();

  /**
   * Provides an {@link Intent} for launching the credential provider settings
   * activity, that allows the user to whitelist credential providers for
   * use on the device.
   */
  Intent getSettingsIntent();
}

public final class CredentialProviders implements CredentialProvidersApi {
  public static CredentialProviders getInstance();
  // ... implementation of CredentialProvidersApi methods ...
}
```

This API will be used by the OpenYOLO client library to:

- Ensure that the library does not interact with credential providers they have
  not explicitly selected (as returned by `getProviders()`).

- Interact with only the user's default credential provider for save and
  hint requests.

It is recommended that providers direct the user to the settings activity at
some point during the setup flow, immediately after installation.
This is a common occurrence in providers already to enable the provider as an
`accessibility provider` in order to perform form-fill in apps. Our hope is that
over time, and with additional platform support, launching the credential
provider settings activity will be the system configuration step that a
credential provider will need to do in order to fully integrate their
functionality into the platform.

The selection of providers available to the user in the settings activity will
be populated by all apps found on the device that provide broadcast receivers
and activity intent filters for the OpenYOLO operations. The known provider list
will be used to annotate these entries with "known" / "unknown" as appropriate
and to warn the user if they enable an unknown provider.

One may wonder why the API does not provide methods allowing a provider to
request that they be added to the whitelist, or be set as the
default credential provider, rather than launching a general settings activity.
Such an API would likely be implemented by showing a dialog to the user stating
"Would you like to add X as a credential provider?" with some
explanatory text and "YES"/"NO" options. The risk with such a presentation is
that users often don't read or understand the implications of such a prompt,
and will press the button that most obviously looks like the default action
of the dialog. This is particularly risky for a credential provider list, as
an app can trick a user into agreeing to this prompt by manipulating the
context in which it is shown. For example, a malicious app masquerading as a
game could present a screen prior to launching the dialog stating "agree to
the following prompt to disable ads" and users will likely click through any
sequence of actions for this. Warnings about unknown providers would likely
be similarly ignored.

It is therefore preferable to launch a full-screen dialog which does not have
an obvious "default" action to continue: the user must at least process the
contents of the screen, increasing the chance that they will realize they are
being put at risk by a malicious app.
