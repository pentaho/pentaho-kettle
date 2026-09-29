# Pentaho S3 VFS


#### Connecting to an S3-compatible service

The **S3 Connection Type** of an S3 connection selects how the client is built. `Amazon` uses the
Amazon S3 endpoint of the selected region. `Minio/HCP` builds the client from an endpoint that you
provide, so it also covers other services that implement the S3 API, such as Backblaze B2,
Cloudflare R2, Hitachi Content Platform or MinIO. The fields of that connection type are:

* **Region:** free text, as those services do not always use the AWS region names. When left empty,
the default region of the AWS SDK is used.
* **Access Key** / **Secret Key:** the credentials issued by the service.
* **Endpoint:** the service endpoint, for example `https://s3.example-region.example.com`. It is
passed to the client together with the region.
* **Signature Version:** the AWS SDK signer type used to sign the requests.
* **Trust Store** / **Key Store:** for endpoints presenting a certificate that the JVM does not
trust by default, or requiring a client certificate.
* **PathStyle Access:** addresses the bucket in the request path instead of in the host name, for
services that do not support virtual host style addressing.

#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://github.com/pentaho/maven-parent-poms/blob/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

__Build for nightly/release__

All required profiles are activated by the presence of a property named "release".

```
$ mvn clean install -Drelease
```

This will build, unit test, and package the whole project (all of the sub-modules). The artifact will be generated in: ```target```

__Build for CI/dev__

The `release` builds will compile the source for production (meaning potential obfuscation and/or uglification). To build without that happening, just eliminate the `release` property.

```
$ mvn clean install
```

#### Running the tests

__Unit tests__

This will run all tests in the project (and sub-modules).
```
$ mvn test
```

If you want to remote debug a single java unit test (default port is 5005):
```
$ mvn test -Dtest=<<YourTest>> -Dmaven.surefire.debug
```
