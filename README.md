# ShushDJX - Automatically skip `Up next` spoken segments from Spotify's DJ.

## Purpose

This app has a single purpose: avoid having to listen to Spotify's "DJ
X" speak its introductions/summaries/commentary between sets of songs.

## Installation

Users who don't want to build the .apk from source have two choices:
- side-load (using `adb`) an `.apk` downloaded from the [latest release](https://github.com/fischman/ShushDJX/releases/latest) (doesn't get auto-updated with new releases)
- join the [testers group](https://groups.google.com/g/dwiwph-testers/) and install from the [Google Play Store](https://play.google.com/store/apps/details?id=org.fischman.shushdjx) (link will 404 until group is joined). The group is used for access control only, not email. The group is necessary until the app can pass out of Google Play Store "closed testing" and into ["production access"](https://support.google.com/googleplay/android-developer/answer/14151465#overview), unfortunately.

## Operation

On initial launch it will request permission to read notifications in
order to register for MediaSession metadata updates (which is how it
detects that DJ X is speaking). Subsequently it will drop the
individual checkmarks in the Read Permissions system settings and not
read any notification contents.

When MediaSession metadata change is detected, if it shows for `Up
next`, the app will send a "next track" event to skip the spoken
segment (and retry a few times until it detects success, since Spotify
sometimes ignores the request).

## Development notes to self
- Once per VM/container, run `.headless/install-android-sdk.sh`
- To build a debug APK: `.headless/build-and-deploy.sh` (with optional `--build-only` to skip install/run)
- To build a release APK: `.headless/build-and-deploy.sh --release` (with optional `--build-only` to skip install/run)
- To build an .aab for uploading to [Play Console](https://play.google.com/console/u/0/developers/4908727064133047922/app/4974223821336177603/): `.headless/build-and-deploy.sh --release-aab` (builds only, no install/run)
- Create GitHub release with: `command gh release create v0.<N> --notes "<NOTES>" ./app/build/outputs/apk/release/app-release.apk`
