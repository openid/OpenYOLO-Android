# Saving credentials

After successfully authenticating a user, it is recommended that an app always
attempt to save this known-good credential using OpenYOLO. First, the
system must be queried to determine which providers are able to save the
credential. This is a simple [Package Manager][pm-api] API call to find
activities with action `org.openyolo.save` and category `org.openyolo`.

It is anticipated that some credential providers will only be able to store
id-and-password based credentials, while others may support federated
credentials. Such providers can be distinguished by requiring that providers
declare their supported set of authentication methods via an intent filter,
with a data URI filter based on the authentication method URI.

For a credential provider which only supports id-and-password based credentials,
this would look as follows:

```xml
<activity
    android:name="com.example.provider.SaveCredentialActivity"
    android:theme="@style/AppTheme.Dialog"
    android:exported="true"
    android:excludeFromRecents="true">
  <intent-filter>
    <action android:name="org.openyolo.save"/>
    <category android:name="org.openyolo" />
    <!--
    as this app only supports saving ID and password based credentials, this is
    declared using a data filter.
    -->
    <data
        android:scheme="openyolo"
        android:host="id-and-password" />
  </intent-filter>
</activity>
```

If a provider supports saving credentials for multiple authentication methods,
then multiple data filters can be specified. If the data filter is omitted,
this indicates the provider supports saving all credentials, regardless of
authentication type.

Given a credential object to be saved, the system can be queried to
find all password managers which support saving this credential:

```java
Intent saveIntent = new Intent("org.openyolo.save");
saveIntent.addCategory("org.openyolo");

// set the authentication type as a data parameter
saveIntent.setData(Uri.parse("openyolo://id-and-password"));

List<ResolveInfo> supportingProviders =
    getPackageManager().queryIntentActivities(saveIntent, 0);
```

This list should be presented to the user, in order to allow them to select
which provider they wish to save the credential to.

## Filtering the provider list

The list of credential providers returned by querying the package manager
may include unsafe options - it is important to further filter this list based
on the following criteria:

1. If the user has a preferred credential provider defined in the Google Play
   Services managed settings, and this credential provider
   is in the list, it should be used directly.

1. If the user has a whitelist of credential providers defined in the
   Google Play Services managed settings, the dialog presented to the user
   for save should be restricted to these options.

1. If Google Play Services is unavailable, all options should be displayed
   such that known providers are clearly distinguished from unknown providers.
   Selecting an unknown provider should require a second confirmation, to avoid
   the user accidentally interacting with an unknown provider by tapping on the
   wrong area of the screen.

[Protecting the user from malicious providers](protecting-users.md) provides
more information on how the Google Play Services settings and the known
providers list are defined.

## Behavior of the save intent

The behavior of the activity or activities that implement the save flow
is beyond the scope of this specification. However,. if a saved credential
matches an existing credential by identifier and authentication method, and
authentication domain, the credential provider should allow this to be
automatically saved where possible. This will allow apps to easily update
credentials in response to password change events.

To mitigate potential attempts to spoof a credential provider's UI, it is
also recommended that a method of pushing the request to a full screen version
of the provider is made available. This will allow security-conscious users
to determine that it is really the credential provider they are interacting
with, and not some attempt to phish their master password.

## Save response

The save response, returned to the app via `onActivityResult`, can be one of
two values:

1. `RESULT_OK`, if the credential is successfully saved.
2. `RESULT_CANCELLED`, if the credential was not saved for any reason. No
   further details need to be provided to the calling application, as the
   application is unlikely to be able to take any remedial action.

[pm-api]: https://developer.android.com/reference/android/content/pm/PackageManager.html "android.content.pm.PackageManager"
