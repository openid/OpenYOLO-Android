# Migrating from the Google Smart Lock for Passwords API to OpenYOLO

Google will continue to offer the Smart Lock for Passwords standalone API for
the foreseeable future. In 2018, the Smart Lock for Passwords API will start
automatically using the OpenYOLO protocol if other OpenYOLO providers are
available on the device, however we encourage developers with the available
resources to explicitly move to the OpenYOLO API, as this is where all future
work and innovation will occur. The OpenYOLO API also offers other
non-functional benefits, such as simpler code that is easier to test.

This document describes how to migrate your current integration with the
Google Smart Lock for Passwords to the OpenYOLO API. If your application does
not currently utilize Smart Lock for Passwords, we recommend instead reading
the main [README](../README.md), which contains a getting started guide and
links to other resources.

OpenYOLO for Android is intended to be the open-standards based successor to
the Google Smart Lock for Passwords API. OpenYOLO provides the same
functionality and UX flows as Google Smart Lock for Passwords, while also
allowing the user to choose from any compatible credential manager.

Migration is a straightforward process, as the APIs are structurally very
similar. This document will demonstrate how to migrate to the OpenYOLO API for
each API method that is offered in the Smart Lock for Passwords API.

## Importing the OpenYOLO API

To use the OpenYOLO API, first add the API as a dependency on your project. If
you do not use any of the other Google authentication features (like Google
Sign-in), you can also remove the dependency on Play Services Authentication:

```diff
dependencies {
-  // optionally, remove play services auth if you no longer require it
-  implementation 'com.google.android.gms:play-services-auth:x.y.z'
+  // add the OpenYOLO API
+  implementation 'org.openyolo:openyolo-api:x.y.z'
}
```

Unlike Smart Lock for Passwords, the OpenYOLO client library will work on
devices without Google Play Services.

## Account hints

Smart Lock for Passwords defined the concept of a hint request, which allows
an app to solicit a credential to simplify new account creation or existing
account discovery. Hints provide a user identifier and authentication method,
and may optionally include a display name, profile picture and generated
password.

### Sending a hint request

In both the Smart Lock for Passwords API and the OpenYOLO API, Intents are used
to communicate with the user’s credential provider to retrieve an account hint.

With Smart Lock for Passwords, a hint request for an email, phone number or
Google Sign-in based account would be constructed and dispatched as follows:

```java
// deprecated: this is the old way of doing things
void sendSmartLockHintRequest() {
  PendingIntent hintPickerIntent =
      Auth.CredentialsApi.getHintPickerIntent(
          mGoogleApiClient,
          new HintRequest.Builder()
              .setEmailAddressIdentifierSupported(true)
              .setPhoneNumberIdentifierSupported(true)
              .setAccountTypes(IdentityProviders.GOOGLE)
              .setIdTokenRequested(true)
              .setServerClientId(MY_GOOGLE_CLIENT_ID)
              .setIdTokenNonce(“nonceValue”)
              .build());

  startIntentSenderForResult(
      hintPickerIntent.getIntentSender(),
      RC_HINT,
      null, // fillInIntent
      0, // flagsMask
      0, // flagsValues
      0); // extraFlags
}
```

This code assumes that a `GoogleApiClient` instance has already been created and
connected. Such pre-flight initialization is not necessary for OpenYOLO,
where the initialization can be fully synchronous, and the code changes to the
following:

```java
private mCredentialClient =
    CredentialClient.getApplicationBoundInstance(this);

void sendOpenYoloHintRequest() {
  Map<String, TokenRequestInfo> tokenProviders = new HashMap();
  tokenProviders.put(
      "https://accounts.google.com",
      new TokenRequestInfo.Builder()
          .setClientId(MY_GOOGLE_CLIENT_ID)
          .setNonce("randomlyGeneratedNonce")
          .build());

  Intent hintIntent = mCredentialClient.getHintRetrieveIntent(
      new HintRetrieveRequest.Builder(
          AuthenticationMethods.EMAIL,
          AuthenticationMethods.PHONE,
          AuthenticationMethods.GOOGLE)
          .setTokenProviders(tokenProviders)
          .build());

  startActivityForResult(hintIntent, RC_HINT);
}
```

The key differences to note are:

- The `CredentialClient` is easier to create than a `GoogleApiClient` instance:
  a `Context` reference is all that is required, with no asynchronous
  initialization.

- Account types from the Smart Lock for Passwords API are now “authentication
  methods”, with email auth, phone auth and federated authentication methods
  all treated in a consistent manner.

- A plain `Intent` is used instead of a `PendingIntent`.

One may also notice the new `setTokenProviders` method - in an effort to support
acquiring ID tokens from additional providers, clients are now encouraged to
explicitly declare the ID token providers they support (e.g. Google,
Microsoft), along with any provider-specific parameters that may be required.
Currently, OpenYOLO directly supports specifying your application’s OpenID
Connect client ID and an ID token nonce value.

### Receiving a hint response

Hint responses are delivered to the application via onActivityResult in both
the Smart Lock and OpenYOLO APIs. Both indicate the success or failure of the
request via a result code and Intent extras, though the data is structurally
different.

With Smart Lock for Passwords:

```java
// deprecated: this is the old way of doing things
@Override protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {

  if (requestCode == RC_HINT && resultCode == RESULT_OK) {
    Credential credential =
        data.getParcelableExtra(Credential.EXTRA_KEY);
    if (credential == null) {
      authenticateManually();
    } else {
      processSmartLockCredentialAsHint(credential);
    }
  }
}
```

With OpenYOLO:

```java
@Override
protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {

  if (requestCode == RC_HINT) {
    HintRetrieveResult result =
        mCredentialClient.getHintRetrieveResult(data);

    if (result.isSuccessful()) {
      // condition is equivalent to:
      // result.getResultCode() == HintRetrieveResult.CODE_HINT_SELECTED
      Hint hint = result.getHint();
      processOpenYoloHint(hint);
    } else {
      authenticateManually();
      return;
    }
  }
}
```

The standard result codes for hint retrieval are all defined as constants on
the `HintRetrieveResult` class. The result codes in OpenYOLO are more detailed
than those returned by Smart Lock for Passwords, providing more information
particularly in the cases where a hint is not returned:

- `CODE_HINT_SELECTED`: The user selected a hint, which can be retrieved by
  calling the `getHint()` method.

- `CODE_BAD_REQUEST`: The request was malformed in some way. The `getHint()`
  method will return `null`.

- `CODE_NO_HINTS_AVAILABLE`: There are no hints available in the user’s
  credential manager that match the request type. The `getHint()` method will
  return `null`.

- `CODE_USER_REQUESTS_MANUAL_AUTH`: The user has indicated via their credential
  manager that they wish to manually authenticate. The `getHint()` method will
  return `null`.

- `CODE_USER_CANCELED`: The user canceled the hint retrieval flow, such as by
  pressing the back button or tapping outside of a selection dialog. The
  `getHint()` method will return `null`.

- `CODE_UNKNOWN`: The credential provider failed to handle the hint request.
  The `getHint()` method will return `null`.

### Processing a returned hint

In the Smart Lock for Passwords API, the same `Credential` type is returned for
both hint and existing credential requests. In OpenYOLO, a separate `Hint` type
is introduced - this is intended to help clarify that hints and credentials are
related but not identical, and will allow for future evolution of hints and
credentials that may diverge.

OpenYOLO also provides generated passwords with hints. The constraints for
these passwords can be specified using a `PasswordSpecification` instance,
optionally provided as a parameter on the HintRequest.

In Smart Lock for Passwords, a hint would be processed as follows:

```java
// deprecated: this is the old way of doing things
void processSmartLockCredentialAsHint(Credential credential) {
  if (credential.getAccountType() == null) {
    // this is not a federated account. It is not easy to distinguish
    // between email and phone number accounts with this API,
    // other than by heuristically evaluating the contents of the
    // identifier.
    if (isPhoneNumber(credential.getId()) {
      createPhoneNumberAccount(
        credential.getId(),
        credential.getDisplayName(),
        credential.getProfilePictureUri());
    } else {
      createEmailAccount(credential.getId());
    }

  } else if (IdentityProviders.GOOGLE.equals(
      credential.getAccountType()) {
    // this is a Google Sign-in account. First, see if we have an ID
    // token that can be used to directly authenticate.
    if (credential.getIdTokens().size() > 0) {
      authWithIdToken(credential.getIdTokens().get(0));
    } else {
      // if there is no ID token, use the Google Sign-in API to
      // authenticate.
      signInWithGoogle(credential.getId());
    }
  }
}
```

With OpenYOLO, this changes to:

```java
void processOpenYoloHint(Credential credential) {
  if (AuthenticationMethods.PHONE.equals(
      hint.getAuthenticationMethod())) {
    createPhoneNumberAccount(
        hint.getId(),
        hint.getDisplayName(),
        hint.getDisplayPictureUri());

  } else if (AuthenticationMethods.EMAIL.equals(
      hint.getAuthenticationMethod())) {
    // note that we can now use the generated password as part of the
    // account creation process.
    createEmailAccount(
        hint.getId(),
        hint.getDisplayName(),
        hint.getDisplayPictureUri(),
        hint.getGeneratedPassword());

  } else if (AuthenticationMethods.GOOGLE.equals(
      hint.getAuthenticationMethod())) {
    // this is a Google Sign-in account. First, see if we have an ID
    // token that can be used to directly authenticate.
    if (hint.getIdToken() != null) {
      authWithIdtoken(hint.getIdToken());
    } else {
      // if there is no ID token, use the Google Sign-in API to
      // authenticate.
      signInWithGoogle(hint.getId());
    }
  }
}
```

The notable changes here are that:

- OpenYOLO's authentication methods are more explicit and easier to work with
  than the mix of identifier inference and account types that Smart Lock for
  Passwords provided (e.g. the unspecified `isPhoneNumber` method above).

- OpenYOLO guarantees that at most one ID token will be returned; the
  use case for multiple ID tokens in the Smart Lock for Passwords API never
  materialized, so the API in OpenYOLO has been simplified to reflect this.

The properties available on Smart Lock's `Credential` type align to the
properties on OpenYOLO's `Hint` type as follows:

| *Credential method*    | *Hint method*             | *Notes*                 |
|------------------------|---------------------------|-------------------------|
| getId()                | getIdentifier()           |                         |
| getAccountType()       | getAuthenticationMethod() | Both are URI-based, but OpenYOLO defines URIs for email, phone number and username based auth in addition to the same semantics for federated credentials. |
| getName()              | getDisplayName()          |                         |
| getProfilePictureUri() | getDisplayPicture()       |                         |
| getIdTokens()          | getIdToken()              |                         |
| getGeneratedPassword() | getGeneratedPassword()    | This was not a fully implemented feature in Smart Lock for Passwords, but it is in OpenYOLO; a password will be randomly generated by the credential provider that conforms to the password specification provided in the request. |
| getFamilyName()        | N/A  | Not defined in the OpenYOLO specification, as this assumes too much culture-specific structure to names. |
| getGivenName()         | N/A  | Not defined in the OpenYOLO specification, for the same reasons as for `getFamilyName`. |
| getPassword()          | N/A  | Not applicable to OpenYOLO hints, which do not conflate existing and generated credentials in this way. See `getGeneratedPassword`. |
| N/A | getAdditionalProperties() | OpenYOLO offers extensibility for Hints via a general map of strings to byte arrays. |

## Existing accounts

Smart Lock for Passwords offers access to existing credentials stored in a
user's Google account, either automatically or via single tap consent from the
user. OpenYOLO offers the same functionality, with a restructured API that
focuses on convenience for the developer.

### Sending a retrieve request

In Smart Lock for Passwords, requesting existing credentials is broken into
two stages:

1. A credential request is constructed and dispatched, with the initial
   response delivered via a callback. The callback will either directly contain
   the credential (automatic sign-in), provide an `Intent` to be invoked
   (user consent required), or return nothing (no credentials available).
2. If an `Intent` is provided, this must be invoked and the result captured in
   `onActivityResult`.

APIs which rely on multiple asynchronous steps, where the responses are
delivered through different mechanisms, are inherently hard to understand. For
OpenYOLO, this process has been simplified such that an `Intent` is directly
created and used for the request, and all processing can be handled via
`onActivityResult`.

In Smart Lock for Passwords, an existing credential retrieval for an
app that supports password login, ID token auth for Google accounts, and
Facebook Sign-in would look like:

```java
// deprecated: this is the old way of doing things
void sendSmartLockRetrieveRequest() {
  CredentialRequest cr = new CredentialRequest.Builder()
      .setPasswordLoginSupported(true)
      .setAccountTypes(
          IdentityProviders.GOOGLE,
          IdentityProviders.FACEBOOK)
      .setIdTokenRequested(true)
      .setServerClientId(GOOGLE_CLIENT_ID)
      .setIdTokenNonce("randomlyGeneratedNonce")
      .build();

  Auth.CredentialsApi.request(mGoogleApiClient, cr)
      .setResultCallback(this::handleSmartLockRequestInitialResult);
}

void handleSmartLockRequestInitialResult(CredentialRequestResult result) {
  if (result.getStatus().isSuccess()) {
    signInWithSmartLockCredential(result.getCredential());
  } else if (result.getStatus().getStatusCode()
      == CommonStatusCodes.RESOLUTION_REQUIRED) {
    try {
      result.getStatus().startResolutionForResult(this, RC_RETRIEVE);
      return;
    } catch (IntentSender.SendIntentException e) {
      Log.e(TAG, "Oh no :(");
    }
  }

  startManualAuthentication();
}

@Override protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);

  if (requestCode == RC_RETRIEVE) {
    if (resultCode == RESULT_OK) {
      Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
      signInWithSmartLockCredential(credential);
    } else {
      // user canceled or the request failed
      startManualAuthentication();
    }
  }
}
```

The multiple paths to either `signInWithSmartLockCredential` or
`startManualAuthentication` can be hard to follow. In OpenYOLO, this is
simplified to:

```java
void sendOpenYoloRetrieveRequest() {
  CredentialRetrieveRequest request = new CredentialRetrieveRequest.Builder(
      AuthenticationMethods.EMAIL,
      AuthenticationMethods.GOOGLE,
      AuthenticationMethods.FACEBOOK)
      .addTokenProvider(
          "https://accounts.google.com",
          new TokenRequestInfo.Builder()
              .setClientId(GOOGLE_CLIENT_ID),
              .setNonce("randomlyGeneratedNonce")
              .build())
      .build();

  startActivityForResult(
      mOpenYoloClient.getCredentialRetrieveIntent(request),
      RC_RETRIEVE);
}

@Override protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);

  if (requestCode == RC_RETRIEVE) {
    CredentialRetrieveResult result =
        mCredentialClient.getCredentialRetrieveResult(data);
    if (result.isSuccessful()) {
      // condition is equivalent to:
      // result.getResultCode() ==
      //     CredentialRetrieveResult.CODE_CREDENTIAL_SELECTED
      signInWithOpenYoloCredential(result.getCredential());
    } else {
      // no credential available; further information can be found if necessary
      // by checking the other status codes defined in CredentialRetrieveResult,
      // but in most cases manual authentication is the only recourse.
      startManualAuthentication();
    }
  }
}
```

Automatic sign-in and consent based sign-in are not distinguished in OpenYOLO;
both are still possible, but the differences are not exposed via the API to
the client application.

## Processing the credential response

Smart Lock for Passwords and OpenYOLO both have `Credential` types, which are
very similar, though as previously noted OpenYOLO distinguishes hints and
existing credentials through different types.

A credential from Smart Lock for Passwords would be consumed as follows:

```java
// deprecated: this is the old way of doing things
void signInWithSmartLockCredential(Credential credential) {
  if (credential.getIdTokens().size() > 0) {
    // an ID token was returned, which should be used as the secure alternative
    // to password auth whenever possible.
    authWithIdToken(credential.getIdTokens().get(0));
    return;
  }

  String at = credential.getAccountType();
  if (at == null) {
    // this is not a federated account, so it must be a password based
    // account.
    authWithPassword(credential.getId(), credential.getPassword());
  } else if (at.equals(IdentityProviders.GOOGLE)) {
    // Use the Google Sign-in API to authenticate.
    authWithGoogle(credential.getId());
  } else if (at.equals(IdentityProviders.FACEBOOK)) {
    // Use the Facebook SDK to authenticate
    authWithFacebook(credential.getId());
  } else {
    Log.w(TAG, "Smart Lock returned unknown credential type, ignoring");
    startManualAuthentication();
  }
}
```

With OpenYOLO:

```java
void signInWithOpenYoloCredential(Credential credential) {

  if (credential.getIdToken() != null) {
    authWithIdToken(credential.getIdToken());
    return;
  }

  AuthenticationMethod am = credential.getAuthenticationMethod();
  if (am.equals(AuthenticationMethods.EMAIL)) {
    authWithPassword(credential.getIdentifier(), credential.getPassword());
  } else if (am.equals(AuthenticationMethods.GOOGLE)) {
    authWithGoogle(credential.getIdentifier());
  } else if (am.equals(AuthenticationMethods.FACEBOOK)) {
    authWithFacebook(credential.getIdentifier());
  } else {
    Log.w(TAG, "Smart Lock returned unknown credential type, ignoring");
    startManualAuthencation();
  }
}
```

As can be seen, the code is structurally very similar. The properties on the
Smart Lock credential type map to the OpenYOLO credential type as follows:

| *Smart Lock Credential method* | *OpenYOLO Credential method* | *Notes*      |
|------------------------|---------------------------|-------------------------|
| getId()                        | getIdentifier()              |              |
| getAccountType()               | getAuthenticationMethod()    | Both are URI-based, but OpenYOLO does not leave password auth as an implicit "other option"; all OpenYOLO credentials must have a defined authentication method, which makes processing the credentials easier. |
| getPassword()          | getPassword()                        |              |
| getName()              | getDisplayName()                     |              |
| getProfilePictureUri() | getDisplayPicture()                  |              |
| getIdTokens()          | getIdToken()                         |              |
| getGeneratedPassword() | N/A                                  | As OpenYOLO credentials are specific to the existing account case, generated passwords are not applicable. |
| getFamilyName()        | N/A                                  | Not defined in the OpenYOLO specification, as this assumes too much culture-specific structure to names. |
| getGivenName()         | N/A                                  | Not defined in the OpenYOLO specification, for the same reasons as for `getFamilyName`. |
| N/A                    | getAdditionalProperties()            | OpenYOLO offers extensibility for Credentials via a general map of strings to byte arrays. |

## Saving Credentials

When a credential is determined to be valid, it can be saved to the user's
credential provider. In the Smart Lock for Passwords API, this was a two-step
process similar to calling `retrieve()`:

1. The credential is constructed and sent to Smart Lock for Passwords using the
   `save()` method, with a callback provided. This callback is then invoked,
   indicating whether the save succeeded, user consent is required to save
   the credential (a `PendingIntent` is returned), or the save was rejected.

2. If an `Intent` is returned, the developer must invoke this `PendingIntent` in
   order to prompt the user to save the credential. The result of this is
   indicated via `onActivityResult()`.

To simplify the API in OpenYOLO, an `Intent` is directly created and used to
save the credential.

In Smart Lock for Passwords, a credential would be saved as follows:

```java
// deprecated: this is the old way of doing things
void saveSmartLockCredential(Credential credential) {
  Auth.CredentialsApi.save(mGoogleApiClient, credential)
      .setResultCallback(this::handleSmartLockSaveResult);
}

void handleSmartLockSaveResult(Status status) {
  if (status.isSuccess()) {
      // credential automatically saved
  } else if (status.hasResolution()) {
      try {
          status.startResolutionForResult(this, RC_SAVE);
      } catch (IntentSender.SendIntentException e) {
          Log.e(TAG, "Oh no :(");
      }
  } else {
      // save refused by provider or failed.
      // ...
  }
}

@Override protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {

  if (requestCode == RC_SAVE) {
    if (resultCode = RESULT_OK) {
      // credential saved
      // ...
    } else {
      // credential not saved
      // ...
    }
  }
}
```

With OpenYOLO, this is simplified to:

```java
void saveOpenYoloCredential(Credential credential) {
  Intent saveIntent = mOpenYoloClient.getSaveIntent(credential);
  startActivityForResult(saveIntent, RC_SAVE);
}

@Override protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {
  if (requestCode == RC_SAVE) {
    CredentialSaveResult result =
           mCredentialClient.getCredentialSaveResult(data);
    if (result.isSuccessful()) {
      // credential was saved. condition is equivalent to:
      // resultCode == CredentialSaveResult.CODE_SAVED
    } else {
      // credential not saved. OpenYOLO distinguishes the failure cases via
      // additional result codes defined in CredentialSaveResult. Two
      // particularly useful additional cases are CODE_USER_CANCELED and
      // CODE_USER_REFUSED, as in some cases an application may wish to handle
      // these differently.
      // ...
    }
}
```

There is one additional case to consider for OpenYOLO: as hints are represented
through a different type, if a new account created through the hint flow is
to be saved, it is necessary to convert that `Hint` to a `Credential`. This
can be easily achieved as follows:

```java
Credential.Builder crBuilder = hint.toCredentialBuilder(this);
// ... make any necessary changes or additions ...
saveOpenYoloCredential(crBuilder.build());
```

This carries across all of the equivalent fields from the hint to the
credential, including copying any generated password (from
`getGeneratedPassword()`) into the regular password field (via
`setPassword()` on `Credential.Builder`). In most cases, further modification
of the credential is not required, but a builder is returned rather than
a fully constructed `Credential` to make this convenient when necessary.

## Handling sign out

As Smart Lock for Passwords and OpenYOLO retrieve requests can both result in
automatic sign-in, it is important to avoid a "sign-in loop" if a user chooses
to sign out. This can occur where the first activity executed after signing
out is the login activity; if this invokes the Smart Lock / OpenYOLO retrieve
flows, this can result in automatically signing in the user again, without
providing them any opportunity to intervene.

This is prevented in Smart Lock for Passwords by invoking the
`disableAutoSignIn` method:

```java
// deprecated: this is the old way of doing things
void signOut() {
  Auth.CredentialsApi.disableAutoSignIn(mGoogleApiClient);
  discardAuthToken();
  goToLogin();
}
```

The same method is provided in the OpenYOLO API:

```java
void signOut() {
  mOpenYoloClient.disableAutoSignIn();
  discardAuthToken();
  goToLogin();
}
```

OpenYOLO stores this state, by default, using a shared preferences file in the
context of the client application. This behavior can be customized, to save
this preference wherever you wish, by providing an alternative implementation
of `AppSettings` via the `CredentialClientOptions` type, which can be provided
to a `CredentialClient` during construction.

Automatic sign-in can also be explicitly disabled in OpenYOLO at the start of
the retrieve flow, by setting the `requireUserMediation` property to `true`:

```java
void sendOpenYoloRetrieveRequest() {
  CredentialRetrieveRequest request = new CredentialRetrieveRequest.Builder(
      AuthenticationMethods.EMAIL)
      // ... other settings
      setRequireUserMediation(true)
      .build();

  startActivityForResult(
      mOpenYoloClient.getCredentialRetrieveIntent(request),
      RC_RETRIEVE);
}
```

## Deleting invalid credentials

Occasionally, a credential held by the user's credential manager is found to
be invalid during authentication. A client can request that this credential
be deleted, in order to avoid the frustrating experience of a stale credential
being offered to the user repeatedly.

Smart Lock for Passwords follows the same two-stage pattern as retrieve and
save for this functionality, and looks like the following:

```java
// deprecated: this is the old way of doing things
void deleteSmartLockCredential(Credential credential) {
  Auth.CredentialsApi.delete(mGoogleApiClient, credential)
      .setResultCallback(this::handleSmartLockDeleteResult);
}

void handleSmartLockDeleteResult(Status status) {
  if (status.isSuccess()) {
    // credential automatically deleted
  } else if (status.hasResolution()) {
    try {
      status.startResolutionForResult(this, RC_DELETE);
    } catch (IntentSender.SendIntentException e) {
      Log.e(TAG, "Oh no :(");
    }
  } else {
    // delete refused by provider or failed
  }
}

@Override protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if (requestCode == RC_DELETE) {
    if (resultCode == RESULT_OK) {
      // credential deleted
    } else {
      // credential not deleted
    }
  }
}
```

In OpenYOLO, an `Intent` is used, simplifying the code to the following:

```java
void deleteOpenYoloCredential(Credential credential) {
  Intent deleteIntent = mCredentialClient.getDeleteIntent(credential);
  startActivityForResult(deleteIntent, RC_DELETE);
}

@Override protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if (requestCode == RC_DELETE) {
    CredentialDeleteResult result =
        mCredentialClient.getCredentialDeleteResult();
    if (result.isSuccessful()) {
      // credential deleted. Condition is equivalent to:
      // resultCode == CredentialDeleteResult.CODE_DELETED
    } else {
      // credential not deleted. OpenYOLO distinguishes the reasons that a
      // credential has not been deleted via the additional status codes defined
      // in CredentialDeleteResult.
    }
  }
```
