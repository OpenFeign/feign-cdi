# Feign CDI Build & Release Process

This repo uses [semantic versions](http://semver.org/). Please keep this in mind when choosing version numbers.

1. **Alert others you are releasing**

   There should be no commits made to master while the release is in progress (about 10 minutes). Before you start
   a release, alert others on [gitter](https://gitter.im/OpenFeign/feign) so that they don't accidentally merge
   anything. If they do, and the build fails because of that, you'll have to recreate the release tag described below.

1. **Push a git tag**

   The tag should be of the format `release-N.M.L`, for example `release-8.18.0`.

1. **Wait for Travis CI**

   This part is controlled by [`travis/publish.sh`](travis/publish.sh). It creates a couple commits, bumps the version,
   publishes artifacts, syncs to Maven Central.

## Credentials

Credentials of various kind are needed for the release process to work. If you notice something
failing due to unauthorized, check the repo settings in [Travis CI Settings[(https://travis-ci.org/OpenFeign/feign-cdi/settings).

Delete any settings that seem like they're failing and re-add them.  Do not use `travis encrypt` to create these environment variables.

## Builds

Commits to master just run a regular set of tests.  Since all releases are pushed to bintray and sync'd to maven central, we cannot push snapshots.

Releases include the extra `deploy` step that pushes the artifacts to bintray, creating the release.  Once the release is created, the zipkin plugin syncs them to maven central.