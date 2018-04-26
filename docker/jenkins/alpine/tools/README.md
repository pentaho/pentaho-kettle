# Phantomjs docker build

Because alpine uses musl, some apps that are dynamicaly linked to glibc will not run.
Java and Phantomjs are such tools we use that are linked to glibc.

In this docker image, we'll build and extract the minimal required libs to be able to run such tools in alpine. 
For that, we'll use [dockerize](https://github.com/larsks/dockerize) to pack the needed ELF binaries that we add to alpine.


#### Usage
```
./build.sh
```

This will create a **dockerized-phantomjs.tgz** file with the necessary ELF binaries in it.  
This file is then used when building the alpine version.