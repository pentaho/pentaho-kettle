#! /bin/bash
set -x

##sudo mv /usr/local/maven-3.5.2/conf/settings.xml /usr/local/maven-3.5.2/conf/ORIG.settings.xml
##sudo cp ./.travis.maven.settings.xml /usr/local/maven-3.5.2/conf/settings.xml

mkdir ~/.m2
cp .travis.maven.settings.xml ~/.m2/settings.xml

mvn clean install -DskipTests -B -V | grep -E -v "\[INFO\] Download(ed|ing) from "
