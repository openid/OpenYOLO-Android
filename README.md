![OpenYOLO for Android](https://rawgit.com/openid/OpenYOLO-Android/master/openyolo_android_lockup.svg)

[![Build Status](https://travis-ci.org/openid/OpenYOLO-Android.svg?branch=master)](https://travis-ci.org/openid/OpenYOLO-Android)
[![codecov](https://codecov.io/gh/openid/OpenYOLO-Android/branch/master/graph/badge.svg)](https://codecov.io/gh/openid/OpenYOLO-Android)

OpenYOLO for Android is a protocol for storing, updating and assisting in the 
creation of user credentials apps. It can be used to assist in the sign-in 
and sign-up process for Android apps, and queries can be responded to by 
any installed credential provider on the device.

This repository contains the reference implementation of the OpenYOLO protocol,
and the standard API and Service Provider Interface (SPI).

OpenYOLO is inspired by, and ultimately intended to replace, Google's
[Smart Lock for Passwords][yolo] API, which is
internally known as YOLO (You Only Login Once).

## OpenYOLO is *not* production ready

OpenYOLO is currently in the *experimental* stage; both the specification and
reference implementation are *unstable*. We do not recommend that it be used
in production Android apps. At such a time that OpenYOLO is considered stable
and ready for production use, an official announcement shall be made and both
the specification and reference implementation will be promoted to v1.0.0.

## OpenYOLO Specification

The OpenYOLO for Android specification can be found 
[here](https://spec.openyolo.org/openyolo-android-spec.html). This is
currently under review by the 
[Account Chooser and OpenYOLO Working Group](http://openid.net/wg/ac/),
with the intention for it to become an implementor's draft by Q3 2017,
and reach full standardization by Q4 2017.

[yolo]: https://developers.google.com/identity/smartlock-passwords/android/
