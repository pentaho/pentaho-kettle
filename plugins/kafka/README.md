#pentaho-big-data-kettle-plugins-kafka

### How to build this project
*This is a maven-based project so you must have it available on your system.*

**Typical build.** 
```
mvn install
```

**Compile only.**
```
mvn compile
```

**Run the unit tests.**
```
mvn test
```

**Run the integration tests.**

```
mvn verify -DrunITs
```
*Note that this project's integration tests rely on integration test artifacts from the pentaho-metaverse-core project. Install those before running these tests. This can be accomplished with the following example commands:
```
cd <parentDir>/pentaho-metaverse/core
mvn install -DrunITs
```