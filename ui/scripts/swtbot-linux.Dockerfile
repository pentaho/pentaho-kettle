# Headless Linux environment for ui/scripts/run-swtbot-tests.sh.
# The PDI distribution ships Linux SWT for x86/x86_64 only, so build and run this image as linux/amd64
# (emulated on Apple silicon). SWT 3.108 with SWT_GTK3=0 (as in spoon.sh) needs GTK2.
#
#   docker build --platform linux/amd64 -t pdi-swtbot-linux -f ui/scripts/swtbot-linux.Dockerfile ui/scripts
#   docker run --rm --platform linux/amd64 \
#     -v "$PWD":"$PWD" -v "$HOME/.m2/repository":/root/.m2/repository -w "$PWD" \
#     -e SWTBOT_WORK_DIR="$PWD/ui/target/swtbot-linux" \
#     pdi-swtbot-linux bash ui/scripts/run-swtbot-tests.sh
#
# Mounting the repository at the same path keeps the extracted-zip stamp valid on both host and container.
# SWTBOT_WORK_DIR keeps Linux output separate from a macOS run. On a Linux host, add --user "$(id -u):$(id -g)"
# (and -e HOME=/tmp) so files written to the mount are not owned by root.
# WebKit and lsb_release are deliberately not installed: without a browser Spoon skips the Welcome tab and logs
# "no underlying browser available", which is harmless; with lsb_release it may show a blocking browser warning.
FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    xvfb xauth libgtk2.0-0 libxtst6 fonts-dejavu-core unzip maven \
  && rm -rf /var/lib/apt/lists/*
