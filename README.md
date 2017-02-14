# OpenYOLO for Android

[![Build Status](https://www.bitrise.io/app/e7b1a2bcce9d787c.svg?token=kvggLFAJ-Tx48g6I671NSg)](https://www.bitrise.io/app/e7b1a2bcce9d787c)
[![codecov](https://codecov.io/gh/openid/OpenYOLO-Android/branch/master/graph/badge.svg)](https://codecov.io/gh/openid/OpenYOLO-Android)

OpenYOLO for Android is a protocol for storing, updating and assisting in the 
creation of user credentials apps. It can be used to assist in the sign-in 
and sign-up process for Android apps, and queries can be responded to by 
any installed credential provider on the device.

This repository contains the
[OpenYOLO Specification](https://github.com/openid/OpenYOLO-Android/tree/master/spec/en) and
reference implementation.

OpenYOLO is inspired by, and ultimately intended to replace, Google's
[Smart Lock for Passwords][yolo] API, which is
internally known as YOLO (You Only Login Once).

## OpenYOLO is *not* production ready

OpenYOLO is currently in the *experimental* stage; both the specification and
reference implementation are *unstable*. We do not recommend that it be used
in production Android apps. At such a time that OpenYOLO is considered stable
and ready for production use, an official announcement shall be made and both
the specification and reference implementation will be promoted to v1.0.0.

[yolo]: https://developers.google.com/identity/smartlock-passwords/android/
