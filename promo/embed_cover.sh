#!/bin/sh
# Embeds the thumbnail as MP4 cover art (shown by Finder, Photos, VLC, WhatsApp, etc.)
set -e
cd "$(dirname "$0")/out"
ffmpeg -y -loglevel error -i globber-reel.mp4 -i globber-reel-thumbnail.png \
  -map 0 -map 1 -c copy -c:v:1 mjpeg -disposition:v:1 attached_pic globber-reel.cover.mp4
mv globber-reel.cover.mp4 globber-reel.mp4
echo "cover embedded"
