#!/usr/bin/env bash
# SWTBot is not published to Maven Central. This downloads the SWTBot 4.2.0 release and installs the jar the
# harness needs into the local Maven repository as org.eclipse.swtbot:org.eclipse.swtbot.swt.finder:4.2.0, so the
# run script and the Maven swtbot profile resolve it like any other dependency. Once the jar is published to the
# team's Maven repository under the same coordinates, this script is no longer needed.
set -euo pipefail

VERSION="4.2.0"
BUNDLE="org.eclipse.swtbot.swt.finder_4.2.0.202405291350.jar"
URL="https://download.eclipse.org/technology/swtbot/releases/${VERSION}/repository.zip"
CACHE_DIR="${SWTBOT_DOWNLOAD_DIR:-${HOME}/.cache/pentaho-swtbot/${VERSION}}"
ZIP_PATH="${CACHE_DIR}/repository.zip"

mkdir -p "${CACHE_DIR}"
if [[ ! -f "${CACHE_DIR}/plugins/${BUNDLE}" ]]; then
  curl -fL --retry 3 --retry-delay 2 -o "${ZIP_PATH}" "${URL}"
  unzip -oq -d "${CACHE_DIR}" "${ZIP_PATH}"
fi

mvn -q install:install-file \
  -Dfile="${CACHE_DIR}/plugins/${BUNDLE}" \
  -DgroupId=org.eclipse.swtbot \
  -DartifactId=org.eclipse.swtbot.swt.finder \
  -Dversion="${VERSION}" \
  -Dpackaging=jar \
  -DgeneratePom=true

echo "Installed org.eclipse.swtbot:org.eclipse.swtbot.swt.finder:${VERSION} into the local Maven repository"
