#!/bin/sh
# LinkedIn-safe export: one video + one audio track, yuv420p, faststart. No embedded cover.
set -e
cd "$(dirname "$0")/out"
ffmpeg -y -loglevel error -i globber-reel.mp4 -map 0:v:0 -map 0:a:0 -c:v libx264 -profile:v high -level 4.1 \
  -pix_fmt yuv420p -color_range tv -crf 18 -preset slow -r 30 -c:a aac -b:a 192k -ar 48000 -movflags +faststart globber-reel-linkedin.mp4
echo "out/globber-reel-linkedin.mp4 ready"
