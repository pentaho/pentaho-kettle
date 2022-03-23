FROM alpine:3.12.4

WORKDIR /root

RUN apk add --no-cache \
    ca-certificates=20191127-r4 \
    curl=7.76.1-r0 \
    git=2.26.3-r0 \
    nano=4.9.3-r0 \
    python3 py3-pip \
    gcc musl-dev python3-dev libffi-dev openssl-dev cargo \
    rsync=3.1.3-r3 \
    bash=5.0.17-r0

RUN pip3 install wheel \
    boxsdk[jwt]==2.12.0  