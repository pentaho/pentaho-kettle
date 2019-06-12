#! /bin/bash
set -x

mv /usr/local/maven-3.5.2/conf/settings.xml /usr/local/maven-3.5.2/conf/ORIG.settings.xml

cp ./.travis.maven.settings.xml /usr/local/maven-3.5.2/conf/settings.xml

find /usr/local/maven-3.5.2 -type d -name conf -exec ls -ltr {} \;

exit 1
