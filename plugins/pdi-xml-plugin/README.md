# kettle-xml-plugin 

### Pre-requisites for building the project:

* Maven, version 3+
* Java JDK 1.8
* This settings.xml in your /.m2 directory

### Building it

### Build for nightly/release

All required profiles are activated by the presence of a property named "release".
```
$ mvn clean install -Drelease
```

This will build, unit test, and package the whole project (all of the sub-modules). The artifact will be generated in:` core/target `

Build for CI/dev

The release builds will compile the source for production (meaning potential obfuscation and/or uglification). To build without that happening, just eliminate the release property.

```
$ mvn clean install
```
