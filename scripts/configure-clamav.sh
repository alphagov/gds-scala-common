#!/bin/sh

clamtar=/tmp/clam-av.tar.gz

if [ ! -d /opt/clam-av ]; then
	echo "Downloading clamav install"
	curl -o $clamtar http://alphagov.github.com/development-utils/macosx/clam-av.tar.gz
	sudo tar -C /opt -xzf $clamtar
fi

Echo "Updating virus definitions"
sudo /opt/clam-av/bin/freshclam

Echo "Starting antivirus"
sudo /opt/clam-av/sbin/clamd 2>/dev/null
