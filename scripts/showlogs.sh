#!/bin/sh
cp -f converter.pl /tmp/converter.pl
cd /tmp
scp -P 2222 sam@wicaweb5.intec.ugent.be:usertest.csv ./
perl converter.pl > out.png
eog out.png
