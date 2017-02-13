# The OpenYOLO Specification

OpenYOLO is a protocol for retrieving, updating and assisting in the creation of
user credentials for Android apps. The protocol is intended to work on any
Android 15+ device, and has no mandatory dependencies on Google-specific
software or infrastructure, such as Google Play Services or the
Google Play Store.

This document describes the OpenYOLO protocol, and the underlying
Background Broadcast Query protocol (BBQ) upon which it is built, and the
APIs that simplify their usage:

- The [OpenYOLO overview](openyolo-overview.md) chapter describes the high
  level concepts and operations that OpenYOLO provides.

- The [BBQ protocol](bbq-protocol.md) chapter describes the broadcast intent based
  mechanism that is used to communicate with multiple credential providers
  simultaneously. This is primarily used by the credential request flow.

- [Retrieving credentials](retrieving-credentials.md) describes in detail the
  flow for retrieving a credential from one of the providers installed on a
  user's device, and allows for automatic sign-in to applications in most
  cases.

- [Assisting sign-up](assisting-sign-up.md) describes in detail the mechanism
  by which a credential provider can provide information to "bootstrap" a new
  account for an app. This typically allows the app to bypass the majority of
  the manual text entry that is required for new account creation.

- [Saving credentials](saving-credentials.md) describes in detail the flow for
  saving and updating credentials, allowing an app to increase the chances that
  a credential can be automatically retrieved upon future installations.

- [Protecting users from malicious providers](protecting-users.md) describes
  the technical counter-measures employed by the OpenYOLO API to minimize
  the risk of credential theft and spoofing by malicious apps.

- The [OpenYOLO API](api-overview.md) chapter describes the high-level API that
  allows apps to retrieve and save credentials, and request assisted sign-up.

- The [OpenYOLO SPI](spi-overview.md) chapter describes the Service Provider
  Interface which helps credential providers to support the OpenYOLO protocol.

App developers who wish to use OpenYOLO should read the
[OpenYOLO overview](openyolo-overview.md) and the
[API overview](api-overview.md) chapters. The other chapters may be of interest
but are not pertinent to simply using the API. Those who are implementing
OpenYOLO in a credential provider should read all chapters.

## Authors

* Iain McGinniss ([@iainmcgin](https://github.com/iainmcgin)), Google Inc.

## Errata

Queries and errata related to the specification should
be filed as [GitHub issues](https://github.com/google/OpenYOLO/issues)
on the [OpenYOLO repository](https://github.com/google/OpenYOLO)
under the label "specification". The raw markdown source for the specification
can be found at
[https://github.com/google/OpenYOLO/spec](https://github.com/google/OpenYOLO/spec).
We accept and encourage contributions, large and small, from any interested
parties who are able to sign the Contributor License Agreement - see the
[CONTRIBUTORS](https://github.com/google/OpenYOLO/CONTRIBUTORS) file for more
information.

## What does the name mean?

YOLO, You Only Login Once, is the internal code name for Google's
[Smart Lock for Passwords][yolo] API. Lessons learned from the design and use
of this API have heavily influenced the design of OpenYOLO.

[yolo]: https://developers.google.com/identity/smartlock-passwords/android
