# ShushDJX - Automatically skip `Up next` spoken segments from Spotify's DJ.

## Purpose

This app has a single purpose: avoid having to listen to Spotify's "DJ
X" speak its introductions/summaries/commentary between sets of songs.

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
- Create GitHub release with: `command gh release create v0.<N> --notes "<NOTES>" ./app/build/outputs/apk/release/app-release.apk`
