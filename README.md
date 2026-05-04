# ShushDJX - Automatically skip `Up next` spoken segments from Spotify's DJ.

## Purpose

This app has a single purpose: avoid having to listen to Spotify's "DJ
X" speak its introductions/summaries/commentary between sets of songs.

## Operation

On initial launch it will request permission to read notifications
(which is how it detects that DJ X is speaking), and subsequently will
be triggered by Android for each notification. When triggered by
anything other than Spotify it will ignore the notification. Spotify
notifications that are not DJ X's `Up next`, are also ignored. Finally
if the notification _is_ for `Up next`, the app will send a "next track"
event to skip the spoken segment.

## Development notes to self
- Once per VM/container, run `.headless/install-android-sdk.sh`
- To build a debug APK: `.headless/build-and-deploy.sh` (with optional `--build-only` to skip install/run)
- To build a release APK: `.headless/build-and-deploy.sh --release` (with optional `--build-only` to skip install/run)
- Create GitHub release with: `command gh release create v0.<N> --notes "<NOTES>" ./app/build/outputs/apk/release/app-release.apk`
