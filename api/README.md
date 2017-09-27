# OpenYOLO for Android - Programmatic Credential API

<div align="center">
  <img src="https://user-images.githubusercontent.com/445775/30930082-60d21ace-a375-11e7-90cf-cc75fe5747fd.gif"/>
  <p><em>Example of auto sign-in</em></p>
</div>

This is an open-source library for the OpenYOLO for Android protocol. It allows you to easily
manipulate credentials for your application by interacting with the credential provider of the
user's choice. This allows you to:

1. Automatically sign users into your app using saved credentials.
1. Rapidly on-board new users by bootstrapping off of existing identities.
1. Prompt users after sign-in/sign up to save their credentials for future automatic sign in.

## Installation
As a pre-requisite, you will need to add the OpenYOLO Api as a dependency. For example, if your
project uses Gradle:

```groovy
dependencies {
    // ...
    compile 'com.openyolo:api:xxx'
}
```

## Terminology

 - [Authentication Method](https://spec.openyolo.org/openyolo-android-spec.html#authentication-methods):
An authentication method is a mechanism by which a credential can be verified. It is represented as
a URI of the form ```scheme://authority```. For example OpenYOLO defines several common types such
as ``openyolo://email`` and ```openyolo://phone```.  When requesting a credential or hint your
application will declare which authentication methods it supports.
 - [Authentication Domain](https://spec.openyolo.org/openyolo-android-spec.html#authentication-domains):
An authentication domain is the scope in which a credential is considered to be usable. A domain
spans a single Android application or web address. If your authentication system does not span
multiple domains you can safely ignore this concept as a client. However, if your application offers
both an app and web app you can prove this relationship to credential providers to allow credentials
to be shared between the two. ```TOOD(dxslly): Add guide for linking authentication domains.```

## Usage

The entry point is the ```CredentialClient``` class. It is a light weight client that can be created
as needed and offers methods to perform all standard flows. The majority of flows follow the pattern
of sending requests via launching Activity based intents and optionally handling their results via
the ```onActivityResult()``` call back.

A typical sequence of flows for sign-in might looks as follows:
1. Auto Sign-In: Attempt to auto sign in the user, by retrieving a saved credential.
1. Account Creation: If no credential was found and the user is interested in signing in, assist in
account creation by retrieving a hint.
1. Save valid credential: If the user was successfully signed in, save the credential for future
retrieve calls.
1. Delete stale credential: If a retrieved credential is no longer valid, delete the credential so
it will not be returned in future retrieve calls.

### Auto Sign-In

It is recommended to always try to automatically Sign-In the user first.  This is done by crafting a
```CredentialRetrieveRequest``` listing the Authentication Methods your application supports (e.g.
```AuthenticationMethods.EMAIL``` if your app uses email based identifier and optionally a
password).

```java
CredentialClient client = CredentialClient.getInstance(getContext());

CredentialRetrieveRequest request  =
    CredentialRetrieveRequest.fromAuthMethods(
        AuthenticationMethods.EMAIL,
        AuthenticationMethods.GOOGLE);
Intent retrieveCredentialIntent = client.getCredentialRetrieveIntent(request);
startActivityForResult(retrieveCredentialIntent, RC_RETRIEVE_CREDENTIAL);
```

Receive the results and check if a credential was returned:
1. If a credential was returned, attempt to sign-in using your own authentication system. If the
credential was not valid (ie. it failed to authenticate the user), delete the credential.
1. If no credential was returned it is recommended to try to assist the user with account creation
unless the result code specifies the user does not wish to. Notable result codes that indicate the
user wants to branch away from assisted account creation are:
    - USER_CANCELED: Indicates the user does not want to authenticate at this time. It is
      recommended to eject from the sign-in process at this point.
    - USER_REQUESTS_MANUAL_AUTH: Indicates the user wants to authenticate manually. It is
      recommended to drop out of these assisted/automatic flows at this point.

```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (RC_RETRIEVE_CREDENTIAL == requestCode) {
       CredentialRetrieveResult result = client.getCredentialRetrieveResult(data);
       if (result.isSuccessful()) {
           // A credential was retrieved, you may automatically sign the user in.
           result.getCredential();
       } else {
           // A credential was not retrieved, you may look at the result code to determine why
           // and decide what step to take next. For example, result code may inform you of the
           // user's intent such as CredentialRetrieveResult.CODE_USER_CANCELED.
           result.getResultCode();
       }
    }
}
```

### Account Creation

If the user could not be automatically signed-in, assisting with account creation is the next
recommended step. This request is very similar to the credential retrieval request, requiring you to
list the Authentication Methods your application supports.

```java
CredentialClient client = CredentialClient.getInstance(getContext());

HintRetrieveRequest request  =
    HintRetrieveRequest.fromAuthMethods(
        AuthenticationMethods.EMAIL,
        AuthenticationMethods.GOOGLE);
Intent retrieveHintIntent = client.getHintRetrieveIntent(request);
startActivityForResult(retrieveCredentialIntent, RC_RETRIEVE_HINT);
```

Receive the results and check if a hint was returned:
1. If a hint was returned, it is recommended to check if an account with a matching identifier
   already exists and intelligently switch to a log-in flow. If no existing account matches there
   are several branches of actions your app may want to take:
    1. If enough information is present, you may automatically create a new account for the user.
       The amount of information required will depend on the authentication method and personal
       requirements.
    1. If more information is needed to create an account, it is recommended to use the given
       information to skip manual entry and have the user finish account creation manually.
1. If a hint was not returned, it is recommended to fallback to manual account creation unless the
   result code specifies the user does not wish to. A result code of ```USER_CANCELED``` indicates
   the user does not wish to authenticate at this time.
   
Once the account has been created you should save the credential for future retrieve calls.

```java
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (RC_RETRIEVE_HINT == requestCode) {
       HintRetrieveResult result = client.getHintRetrieveResult(data);
       if (result.isSuccessful()) {
         // A hint was retrieved, you may be able to automatically create an account for the
         // user, or offer the user to sign in if an existing account matches the hint.
         result.getHint();
       } else {
         // A credential was not retrieved, you may look at the result code to determine why
         // and decide what step to take next. For example, result code may inform you of the
         // user's intent such as HintRetrieveResult.CODE_USER_CANCELED.
         result.getResultCode();
       }
    }
}
```

### Saving Valid Credentials

When ever a valid credential is changed or created (e.g. a manual password change, or a new account
is created) it is recommended that you save the new or updated credential to enable a future
automatic sign-in experience.

```java
// Craft the valid credential.
String identifer = "joe@gmail.com";
AuthenticationMethod authMethod = AuthenticationMethods.EMAIL;
AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(this);
Credential credential = new Credential.Builder(identifier, authMethod, authDomain).build();

// Or if you have created an account using a hint there is a utility method that will convert the
// hint into a new Credential.Builder.
Credential credential = hint.toCredentialBuilder(this).build();

// Send the request. It is safe to ignore the result.
Intent saveCredentialIntent = client.getSaveIntent(credential);
startActivityForResult(saveCredentialIntent, RC_SAVE_CREDENTIAL);
```

### Deleting Invalid Credentials

If a retrieved credential turns out to be invalid it is recommended that you delete the credential.

```java
// Send the request. It is safe to ignore the result.
Intent deleteCredentialIntent = client.getDeleteIntent(credential);
startActivityForResult(deleteCredentialIntent, RC_DELETE_CREDENTIAL);
```


### Signing Out

There is a common pitfall when implementing account sign-in/sign-out where an application will
automatically sign-in a user after they have just signed out when the user's intention was likely to
either remain signed out or switch to a new account. To resolve this, some state needs to be added
for tracking the user's previous intent and prevent the user from being automatically signed in
again.

The ```CredentialClient.disableAutoSignIn()``` offers a simple way to handle this case. Once called
it will require the user to mediate auto sign-in requests until a successful request is fulfilled.
It is recommended you call this method whenever a user is signed out of your application. 

```java
public void signOutUser() {
    // Your application's custom sign out logic.
    // ...
    CredentialClient client = CredentialClient.getInstance(getContext());
    client.disableAutoSignIn();
}
```

## Testing against providers

// TODO(dxslly): Describe how to test in the wild, enumerate production implementations?
