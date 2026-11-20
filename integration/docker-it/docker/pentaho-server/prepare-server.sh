#!/bin/sh
# Runs at image build time in the Pentaho Server image. The image is built from the base image and the kettle jars and plugins of the commit.
set -e

SERVER=/home/devuser/pentaho/pentaho-server
OVERLAY=/tmp/docker-it/overlay
WEBLIB="$SERVER/tomcat/webapps/pentaho/WEB-INF/lib"
PLUGINS="$SERVER/pentaho-solutions/system/kettle/plugins"

# The image points its datasources at localhost. In the test network the database is pdi-postgres.
grep -rl --include='*.xml' --include='*.properties' "localhost:5432" \
  "$SERVER/tomcat/webapps" "$SERVER/tomcat/conf" "$SERVER/pentaho-solutions" \
  | xargs sed -i "s#localhost:5432#pdi-postgres:5432#g"

# Replace each kettle jar that the image already has. Compare by artifact name, without the version.
for jar in "$OVERLAY"/lib/kettle-*.jar; do
  artifact=$(basename "$jar" | sed 's/-[0-9].*\.jar$//')
  if ls "$WEBLIB/$artifact"-[0-9]*.jar >/dev/null 2>&1; then
    rm -f "$WEBLIB/$artifact"-[0-9]*.jar
    cp "$jar" "$WEBLIB/"
  fi
done

# Replace each plugin folder that the image already has.
for dir in "$OVERLAY"/plugins/*/; do
  name=$(basename "$dir")
  if [ -d "$PLUGINS/$name" ]; then
    rm -rf "$PLUGINS/$name"
    cp -r "$dir" "$PLUGINS/$name"
  fi
done

rm -rf /tmp/docker-it
