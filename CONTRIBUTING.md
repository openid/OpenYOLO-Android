# Contributing to OpenYOLO for Android

All contributions to OpenYOLO for Android are welcome!

Note that as this library is planned to be used in high-profile production code,
we insist on a very high standards for the code and design, but don't feel shy:
discuss your plans over
[GitHub Issues](https://github.com/openid/OpenYOLO-Android/issues) and the
[mailing list](http://groups.google.com/group/oidf-account-chooser-list), and
send in pull requests!

# Required legal agreements

In order to contribute to this project, you need to execute two legal agreements
that cover your contributions.  Pull requests from users who have not signed
these agreements will not be merged.

## Execute the contributor license agreement (CLA)

1. Visit http://openid.net/contribution-license-agreement/
2. Tap *Execute OpenID Foundation Contribution License Agreement* for the
   version relevant to you (Individual or Corporate).
3. Follow the instructions to sign the agreement.

## Execute the working group contribution agreement

In addition to the Code License Agreement, the OpenID Foundation also requires
a working group contribution agreement to cover any contributions you may make
towards the OpenID Connect spec itself (e.g. in comments, bug reports, feature
requests).

1. Visit http://openid.net/intellectual-property/
2. Tap *Execute Contributor Agreement By Electronic Signature* in the box
   marked *Resources*.
3. Follow the instructions to sign the document, state 'Account Chooser &
   OpenYOLO' as the Initial Working Group.

# Making a pull request

## Before you start

Before you work on a big new feature, get in touch to make sure that your work
is inline with the direction of the project and get input on your architecture.
You can file an [Issue](https://github.com/openid/OpenYOLO-Android/issues)
discussing your proposal, or email the
[list][oidf-ac-list]. Smaller bug
fixes and tests are fine to submit without prior discussion.

## Code standards & conditions for PR acceptance

The OpenYOLO Android library follows the
[AOSP Java Code Style](https://source.android.com/source/code-style). This code
style is automatically enforced as part of the build, when you run the `check`
task:

```
./gradlew check
```
Pull requests are expected to at least meet the following criteria:

- The build of the `check` task must succeed. If it does not, reviewers will
  simply reply to the PR with "Please fix the build errors", and review no
  further until this criteria is met.

- The initial submission should consist of a single commit. Review comments
  should be addressed through additional commits on the same branch. Avoid
  rebasing or history rewriting in your PR where possible, to preserve the
  review history. Intermediary commits will all be discarded when the PR 
  is accepted, as the reviewer will "squash and merge" your PR once ready.
  
- It is the submitters responsibility to ensure that a PR can be cleanly
  merged into the master branch.

- All code submitted must have at least 60% coverage for the affected lines.
  Reviewers may request higher coverage, or additional tests generally, if 
  the changes made warrant this. Generally, aim for a net increase in code
  coverage for the project with each PR.
  
- Code submitted must not drop the overall coverage of the project below 80%.
  If the code coverage is already below 80%, then contributions must increase
  the net coverage to be accepted.
  
- Code changes that have implications for the specification must have an 
  associated thread of discussion here on [list][oidf-ac-list], and consensus 
  must have been reached that the change is worthwhile. Preferably, a separate
  pull request should already have been merged into to the 
  [OpenYOLO-Spec repository][spec-repo] before a code change is submitted.
  
- Code changes that affect the API must also be demonstrated as part of the 
  test and sample applications.

- Code changes that affect the SPI must also be demonstrated as part of the 
  test and demo providers.

- Complex changes _should_ be broken into multiple smaller pull requests for 
  review, and these should not be directly submitted to the master branch. 
  Instead, they must be submitted to a feature branch until the complete 
  set of changes is made, and then merged into master upon agreement that
  the work is complete.

- As a general guideline, pull requests should not contain more than a 1000
  line delta. Reviewers may still agree to review PRs that are larger than
  this at their discretion, or they can request that the PR be split into
  smaller changes for more effective review.

## Responsibilities of reviewers

Reviewers (members of the 
[@openid/openyolo-android-maintainers][maintainers-team] team) are expected
to adhere to the following guidelines:

- New pull requests should be allocated a reviewer within one business day of
  initial submission.

- Initial review comments on a PR should be supplied within two business days
  of a reviewer being assigned.

- Reviewers should not review code that is submitted by members of their own
  organization, unless there is no reasonable alternative.

- Reviewers must squash all PRs into a single commit on submission (use the
  "squash and merge" option when accepting the pull request).

- All comments and discussion should occur via issues, in reviews, or on the
  [mailing list][oidf-ac-list]. Avoid direct communication outside of these, 
  or at least summarize such direct communication in one of the official
  avenues, in order to ensure everyone has a chance to understand the decision
  making process.

# Nonclamenture

The following acronyms and terms will be common in reviews:

- CL: Change List. This term is commonly used by Googlers, and is 
  interchangeable for PR (pull request). The reviewers from Google apologize in
  advance if they use confusing, Google-specific terminology at any point in
  their reviews or conversations :)

- Fix in follow-up: a requested change from a reviewer (or a request from a
  submitter) that can be fixed in a follow-up pull request. If such a change
  will not be fixed in the current PR, an issue should be opened to track its
  completion. Attempts to use such requests to back out of writing tests or
  finishing features is generally frowned upon.

- LGTM: Looks Good To Me. This is used as an indication that a reviewer is
  overall satisfied with the change; this may come with some caveats, such as
  comments that must be addressed before the change can be merged.

- Nit: a minor issue with a pull request that _should_ be resolved as part of
  the current review, though the reviewer is likely to accept rebuttals to
  suggestions presented in this form, or suggestions to "fix in follow-up".

- PTAL: Please Take Another Look. If you address review comments with a new
  change, it is helpful to include this as a stand-alone comment on the PR to
  indicate to the reviewer that you have finished addressing their comments
  and the PR is ready for re-review.


[maintainers-team]: https://github.com/orgs/openid/teams/openyolo-android-maintainers
[oidf-ac-list]: http://groups.google.com/group/oidf-account-chooser-list
[spec-repo]: https://github.com/openid/OpenYOLO-Spec
