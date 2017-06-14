# kettle-palo-plugin

This plugin is for pentaho Data integration (ETL) a.k.a kettle

Loads as: `PaloCellInput, PaloCellOnput, PaloCubeCreate, PaloCubeDelete, PaloDimInput, PaloDimOutput`
### Pre-requisites for building the project:

* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://github.com/pentaho/maven-parent-poms/blob/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

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