# PDI Docker Integration Tests

Runs KTR files in the PDI build of the commit under test, in three modes:

| Mode | How the KTR runs |
| --- | --- |
| `PAN` | `pan.sh` runs with `docker exec` in the PDI container. |
| `CARTE` | Carte runs in the PDI container. The test calls `addTrans`, `startTrans`, and `transStatus`. |
| `SERVER` | The KTR runs through the Carte endpoint of the Pentaho Server container (`/pentaho/kettle/`). |

The containers are started by the fabric8 `docker-maven-plugin` (version `0.48.1`) in `pre-integration-test`, and stopped in `post-integration-test`.
The module runs only with `-DrunDockerITs`.

## Containers

| Container | Image | Purpose |
| --- | --- | --- |
| `pdi` | base `automation/pdi-client:11.1`, with the `pdi-ce` zip of the commit | PDI under test |
| `pentaho-server` | base `automation/pentaho-server:11.1-pgsql`, with the kettle jars and plugins of the commit | Server under test |
| `postgresDatabase` | `postgres:16.3` | Databases of the Server (`hibernate`, `quartz`, `jackrabbit`, `emailgroups`) |
| `pdi-ssh-password`, `pdi-ssh-publickey` | `alpine:latest` | SSH servers of `ssh-test-environment/docker-compose.yml` |
| `pdi-http-proxy`, `pdi-http-proxy-auth` | `wenbushi/ngx_http_proxy_connect`, `ubuntu/squid` | HTTP proxies |

The Maven build unpacks the `pdi-ce` zip (dependency `org.pentaho.di:pdi-ce:<version>:zip`) into `target/pdi-ce`.
The PDI image replaces `data-integration` in the base image with it. The Server image replaces the kettle jars and plugin folders that it already has.
`docker/pentaho-server/prepare-server.sh` also points the Server datasources from `localhost:5432` to `pdi-postgres:5432`.

When the Server starts, the test applies Pentaho's schema scripts from the image (`data/postgresql/`) to the Postgres databases.
It runs the scripts with `docker exec`, and then runs `start-pentaho.sh`.

## Test groups

| Class | Tag | KTR files |
| --- | --- | --- |
| `KtrExecutionIT` | `basic` | `src/test/resources/ktr/**`. A file named `<name>.fail.txt` next to a KTR means the KTR must fail. |
| `SshKtrExecutionIT` | `ssh` | `src/test/resources/ssh/Test_SSH_Command-*.ktr`. |

`SshKtrRewriter` changes the SSH endpoints of these KTRs for the test network before each run.
`localhost:2222` becomes `pdi-ssh-password:22`, and `localhost:3129` becomes `pdi-http-proxy-auth:3128`.
The key path becomes `/tmp/docker-it/ssh/test_rsa`. The files in `src/test/resources` do not change.

The SSH KTRs, server configs, and test keys are copies of `ssh-test-environment/`. If you change one copy, change the other.

## Run

Build the PDI zip first:

```bash
mvn -B install -DskipTests -pl assemblies/client -am
```

Run all modes and all tags:

```bash
mvn -B -f integration/docker-it/pom.xml verify -DrunDockerITs
```

One mode and one tag:

```bash
mvn -B -f integration/docker-it/pom.xml verify -DrunDockerITs \
  -Dpdi-plugin-tester.execution-types=PAN -Dgroups=basic
```

## Properties

| Property | Default | Meaning |
| --- | --- | --- |
| `pentaho.docker.pull.host` | `repo.pentaho.com/pnt-docker/` | Registry prefix of the base images. |
| `pdi-plugin-tester.pdi-version` | `11.1` | Tag of the base images. |
| `pdi-plugin-tester.execution-types` | `PAN,CARTE,SERVER` | Modes to run. |
| `pdi-plugin-tester.carte-host-port` | `9081` | Host port of Carte in the PDI container. |
| `pdi-plugin-tester.server-host-port` | `8080` | Host port of the Pentaho Server. |
| `pdi-plugin-tester.startup-timeout-ms` | `300000` | Wait for Carte, Postgres, and the SSH services. |
| `pdi-plugin-tester.server-startup-timeout-ms` | `900000` | Wait for the Pentaho Server. |
| `pdi-plugin-tester.skip-pentaho-server-container` | `false` | Skip the Server container. |
| `pdi-plugin-tester.skip-postgres-container` | `false` | Skip the Postgres container. |
| `pdi-plugin-tester.skip-ssh-containers` | `false` | Skip the SSH and proxy containers. |
| `groups` / `excludedGroups` | empty | JUnit tags to include or exclude. |
| `rerunFailingTestsCount` | `2` | Reruns of a failing test. |

## Add a KTR case

Put a `.ktr` file in `src/test/resources/ktr/<area>/`. The test runs it in each selected mode.
For a KTR that must fail, add `<name>.fail.txt` next to it.

## Limits

- The base images need access to the Pentaho registry (`repo.pentaho.com`).
- The commit's `pdi-ce` zip needs the Pentaho Maven repository. Without valid credentials, the build fails before this module runs.
