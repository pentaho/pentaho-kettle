#!/bin/sh


OUTPUT="dockerized-phantomjs"

# This builds an image containing the binaries from the phantomjs.
# curl is included in the build so that internet connections work fine
rm -rf ${OUTPUT}
dockerize -n -o ${OUTPUT} \
          --verbose \
          $(which phantomjs) \
          $(which curl)

rm ${OUTPUT}/Dockerfile
rm ${OUTPUT}/$(which phantomjs)

# taring archive
cd ${OUTPUT} && \
  tar -zcf ../dockerized-phantomjs.tgz ./lib ./lib64 ./usr/lib && \
  cd ..