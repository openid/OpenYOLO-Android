![OpenYOLO for Android](https://rawgit.com/openid/OpenYOLO-Android/master/openyolo_android_lockup.svg)

[![Build Status](https://travis-ci.org/openid/OpenYOLO-Android.svg?branch=master)](https://travis-ci.org/openid/OpenYOLO-Android)
[![codecov](https://codecov.io/gh/openid/OpenYOLO-Android/branch/master/graph/badge.svg)](https://codecov.io/gh/openid/OpenYOLO-Android)

OpenYOLO for Android is a protocol for storing, updating and assisting in the 
creation of user credentials apps. It can be used to assist in the sign-in 
and sign-up process for Android apps, and queries can be responded to by 
any installed credential provider on the device.

This repository contains the reference implementation of the OpenYOLO protocol,
and the standard API and Service Provider Interface (SPI).

<div align="center">
  <img src="https://user-images.githubusercontent.com/445775/30930082-60d21ace-a375-11e7-90cf-cc75fe5747fd.gif"/>
  <p><em>Example of auto sign-in</em></p>
</div>

## Project Status

OpenYOLO has reached beta production ready stage and can be integrated into production applications.
The following credential providers include production implementations of OpenYOLO:

| <img src="https://user-images.githubusercontent.com/445775/31100963-59c51db2-a780-11e7-87f4-e7ab2750b92a.png" width="200" height="200" /> | <img src="https://user-images.githubusercontent.com/445775/31101083-0cc4fd92-a781-11e7-95fe-9f9e9fadae70.png" width="200" height="200" /> | <img src="https://user-images.githubusercontent.com/445775/31101050-eeb9966e-a780-11e7-96ae-614f765f3f12.png" width="200" height="200" /> | <img src="https://user-images.githubusercontent.com/445775/32184911-078a0814-bd5b-11e7-8957-9a9b4d3cc415.png" width="200" height="200" /> |
|:---:|:---:|:---:|:---:|
| 1Password | Dashlane | Google Smart Lock | LastPass |

The OpenYOLO for Android specification can be found [here](http://openid.net/specs/openyolo-android-03.html).
This is currently under review as an implementor's draft by the
[Account Chooser and OpenYOLO Working Group](http://openid.net/wg/ac/), with the goal for it to
reach final specification status in the near future.

## Getting Started as a Client

[![Download](https://api.bintray.com/packages/openid/net.openid/openyolo-api/images/download.svg) ](https://bintray.com/openid/net.openid/openyolo-api/_latestVersion)

The client library for the OpenYOLO for Android protocol lives in the ```api``` directory. It allows
you to easily manipulate credentials for your application by interacting with the credential
provider of the user's choice. This allows you to:

1. Automatically sign users into your app using saved credentials.
1. Rapidly on-board new users by bootstrapping off of existing identities.
1. Prompt users after sign-in/sign up to save their credentials for future automatic sign in.

<div align="center">
  <b><a href="api">Getting Started Guide</a></b> | <b><a href="demoapps">Sample Implementation</a></b>
</div>


## Getting Started as a Credential Provider

[ ![Download](https://api.bintray.com/packages/openid/net.openid/openyolo-spi/images/download.svg) ](https://bintray.com/openid/net.openid/openyolo-spi/_latestVersion)

A getting started guide has not been written for credential providers. In its absence the protocol's
specification as well as several sample implementations are good alternatives.

<div align="center">
  <b><a href="demoproviders">Sample Implementations</a></b> | <b><a href="testapp">Test App</a></b> | <b><a href="http://openid.net/specs/openyolo-android-03.html">Specification</a></b>
</div>

