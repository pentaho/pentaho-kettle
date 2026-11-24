# PDI-21063 - VFS caching spike

Updated: 2026-11-24. Spike [PDI-21063](https://pentaho-eng.atlassian.net/browse/PDI-21063), epic [PDI-21042](https://pentaho-eng.atlassian.net/browse/PDI-21042).

**Verdict:** the epic is not one bug. Its 10 tickets are tracked in 6
workstreams (A-F), each with a reproduced mechanism and proof-of-concept
(PoC). This does not cover every reported workflow: PDI-20647's Spoon
reproduction remains unverified.

A PoC shows that a fix direction works; it is not a production change. Each
still needs design review, hardening, product QA and its own ticket/PR.

## Jira index

Test results at the upstream tips on 2026-11-24 (kettle master `e7ec0605c72`,
11.0 `cd4e024b599`, 10.2 `a1e577acf11`). **RED -> PoC** = a test of the
correct behaviour fails and the spike PoC makes it pass; GREEN = the covered
probe passes, not proof the whole Jira is resolved; UNVERIFIED = the reported
workflow was not tested; n/a = code not on that line. Baseline qualifications
and cause details are in the linked section.

| Jira | Reported symptom | Section | master | 11.0 | 10.2 |
| --- | --- | --- | --- | --- | --- |
| [PDI-21035](https://pentaho-eng.atlassian.net/browse/PDI-21035) | Parallel output to two S3 connections -> `403` | [A](#a) | RED -> PoC | RED -> PoC | RED -> PoC |
| [PDI-20723](https://pentaho-eng.atlassian.net/browse/PDI-20723) | Parallel PVFS reads across providers fail | [A](#a) | RED -> PoC | RED -> PoC | RED -> PoC |
| [PDI-21092](https://pentaho-eng.atlassian.net/browse/PDI-21092) | Raw `s3://` + named PVFS mix switches config | [A](#a) | RED -> PoC | RED -> PoC | RED -> PoC |
| [PDI-21074](https://pentaho-eng.atlassian.net/browse/PDI-21074) | Cancel leaks edits; rename corrupts connection | [B](#b) | GREEN | GREEN | RED -> PoC |
| [PDI-20299](https://pentaho-eng.atlassian.net/browse/PDI-20299) | Cancel keeps edited/old name in memory | [B](#b) | GREEN | GREEN | RED -> PoC |
| [PDI-20586](https://pentaho-eng.atlassian.net/browse/PDI-20586) | Edits/variable changes leave stale config (Carte) | [C](#c) | RED -> PoC | RED -> PoC | RED -> PoC |
| [PDI-20647](https://pentaho-eng.atlassian.net/browse/PDI-20647) | Spoon rerun succeeds after VFS connection deleted | [C](#c) | UNVERIFIED | UNVERIFIED | UNVERIFIED |
| [PDI-21015](https://pentaho-eng.atlassian.net/browse/PDI-21015) | Repository changes invisible after refresh | [D](#d) | GREEN | RED -> PoC | n/a |
| [PDI-19753](https://pentaho-eng.atlassian.net/browse/PDI-19753) | Parquet S3A to second bucket -> `Wrong FS` | [E](#e) | RED -> PoC | RED -> PoC | RED -> PoC |
| [PDI-20874](https://pentaho-eng.atlassian.net/browse/PDI-20874) | GCS key-path variable never re-resolved | [F](#f) | RED -> PoC | RED -> PoC | RED -> PoC |

Branches: `spike/PDI-21063-{master,11.0,10.2}` on the `befc` forks of
[pentaho-kettle](https://github.com/befc/pentaho-kettle/branches/all?query=spike%2FPDI-21063),
[big-data-plugin](https://github.com/befc/big-data-plugin/tree/spike/PDI-21063-10.2),
[pentaho-hadoop-shims](https://github.com/befc/pentaho-hadoop-shims/branches/all?query=spike%2FPDI-21063) and
[pdi-plugins-ee](https://github.com/befc/pdi-plugins-ee/branches/all?query=spike%2FPDI-21063).
Each has the tests first (RED), then the fix (GREEN); commits below are
`test -> fix`. Build changes are test-scoped only. This document is on
pentaho-kettle `spike/PDI-21063-master`.

<a id="a"></a>
## A - PDI-21035, PDI-20723, PDI-21092: shared PVFS resolution context

- **Cause:** `ConnectionFileSystem.initializeVariableSpace()` writes
  `connection=<name>` into the caller's shared `VariableSpace`, and
  `KettleVFSImpl.buildFsOptions()` reads it back to build provider options.
  Parallel steps overwrite each other's value, and a raw `s3://` URI inherits
  the last PVFS connection. Not S3-specific; the `403` is only the symptom.
- **Tests:** `PvfsResolutionIsolationTest` (3 core tests) and
  `PvfsConnectionIsolationMinioIT` (two MinIO users, each limited to its own
  bucket; 38-40 of 80 parallel writes got `403`).
- **PoC:** resolve into a child `Variables` copied from the caller's space,
  so the shared space is never written. The code is identical on all lines.
  MinIO IT 5/5. No new failures in `org.pentaho.di.connections.**` +
  `org.pentaho.di.core.vfs.**` (master 264, 11.0 203, 10.2 204 tests) or the
  master s3-vfs module.
- **Commits (pentaho-kettle):** master `8548d5cc84e -> 99b2ae9bfbf`, 11.0
  `e77e8cb86f0 -> cd54cca1fac`, 10.2 `1c2c118331d -> b7aa087c35a`; the 10.2 IT
  is in big-data-plugin `d6688ef35`.
- **Open:** final design (PoC child space vs passing the connection
  explicitly); cost of copying variables per lookup; plugins that read
  `connection` after a lookup; nested `zip:pvfs://` untested.
- **Remaining effort:** 3-4.5 days (design, master, 11.0/10.2 backports, QA).

<a id="b"></a>
## B - PDI-21074, PDI-20299: live connection details (10.2 only)

- **Cause:** 10.2 `ConnectionManager.getConnectionDetails()` returns the
  cached instance, so dialogs change it before Save and Cancel cannot undo
  it. Master and 11.0 already return a clone.
- **Tests:** `ConnectionManagerEditIsolationTest` (3 tests).
- **PoC:** cherry-pick of the open PR pentaho/pentaho-kettle#10782; no core
  regressions.
- **Commits (pentaho-kettle 10.2):** `1c2c118331d -> 915ba742858`.
- **Open / effort:** merge pentaho/pentaho-kettle#10782, optionally with these
  tests: 0.5 day.

<a id="c"></a>
## C - PDI-20586, PDI-20647: stale connections in a long-running Carte

PDI-20586's variable-credential issue reproduces across requests in one Carte
JVM, unlike the earlier in-process probes.

**PDI-20647 coverage gap:** the reporter's Steps to Reproduce field specifies
QAT **11.1.0.0-59**, **Spoon**: create a MinIO VFS connection, save a
transformation that reads a text file, run it successfully, delete the
connection, then rerun the same transformation. Our deletion probe instead
uses a separate connection manager and a new Carte request, which resets
connection caches. It also lists/writes files instead of executing the
reported input step, so it is not a reproduction of the Spoon workflow.
The ticket names `TableInput`, which is a JDBC/SQL step; the actual step and
configuration need confirmation (no transformation or attachments supplied).

- **Test:** `CarteConnectionCachingMinioIT` starts a real `Carte` and MinIO
  (two users, one bucket each) and runs a saved job over `GET
  /kettle/executeJob/`: Create file (job entry) -> transformation with Get
  File Names + Text file output, all on `pvfs://<conn>/${BUCKET}`.

| Scenario | Jira | master | 11.0 | 10.2 |
| --- | --- | --- | --- | --- |
| Connection uses `${S3_ACCESS_KEY}`/`${S3_SECRET_KEY}`; each request passes other values | PDI-20586 | RED -> PoC 1 | RED -> PoC 1 | RED -> PoC 1 |
| Another process edits the saved connection | PDI-20586 | GREEN | GREEN | RED -> PoC 2 |
| Related Carte probe: separate manager deletes the connection, then a new request runs (not the reported Spoon workflow) | PDI-20647 | GREEN | GREEN | RED -> PoC 2 |

- **Cause 1 - variables (all lines):** `KettleVFSImpl.buildFsOptions()`
  puts the caller's live `VariableSpace` into the pvfs `FileSystemOptions`.
  Commons VFS caches file systems by options and compares them by
  `hashCode()` only. `JobEntryBase` and `AbstractMeta` (`TransMeta`) hash by
  name/filename, so every later run of the same job reuses the first run's
  `ConnectionFileSystem` and its variables. Seen: request 2 (user B) got `403`
  in the job entry; request 3 (user B -> bucket A) wrote with user A's keys in
  the job entry and Text file output, while Get File Names (a step, identity
  hash) was correctly denied. This is the "job vs transformation level"
  mix-up in PDI-20586. It also leaks one file system per first-seen object.
- **Related Carte cause 2 - edit/delete (10.2 only):** 10.2 `ConnectionManager` loads all
  connections once and Carte never resets it. Master/11.0 call
  `clearBowlCache()` -> `ConnectionManager.reset()` on every Carte execution
  (BISERVER-15647, `472aac7c5dc`).
- **PoC 1:** new `VariableSpaceSnapshot` (immutable copy, equality on the
  variable values) stored in the options instead of the live space.
- **PoC 2 (10.2):** `BaseHttpServlet.resetConnections()` called at the same
  Carte entry points as master's `clearBowlCache()`.
- **Probe results:** Carte IT 3/3 and `PvfsConnectionIsolationMinioIT` 5/5 on all
  lines; core regression master 264, 11.0 203, 10.2 204; 10.2 engine
  `org.pentaho.di.www` 177 (JDK 11; on JDK 21, 119 pre-existing Mockito errors).
  The pre-C Carte runs on master/11.0 already included the A PoC; they are
  not verification of completely unmodified upstream.
- **PDI-20647 possible later upstream change:** the December 2025 report
  predates BISERVER-15647 (`472aac7c5dc`, committed May 2026), which added
  `spoon.clearBowlCaches()` before local transformation execution in
  `TransGraph`. This may explain different behavior on current master, but
  needs validation against the reporter's workflow, not inference from Carte.
- **Commits:** pentaho-kettle master `0876db9d896 -> 54dc61598ab`, 11.0
  `fa9e2a53e30 -> b2291ce6f8c`, 10.2 `e18b9f9a83d` (PoC 2) + `4c65c83ef20`
  (PoC 1); the 10.2 test is big-data-plugin `e5723654f`.
- **Open:** a snapshot copies all variables on each lookup (cost); the cache
  is still compared by hash only, so collisions are possible; `reset()` is not
  safe while another job is reading connections (10.2); the Spoon browse-tree
  cache (PDI-20586), the reported Spoon rerun, and already-open handles after
  delete (PDI-20647) are not covered.
- **Remaining effort:** 3-4 days (PoC 1 design + perf check on all lines,
  PoC 2 for 10.2, Carte QA with variable credentials). Exact PDI-20647
  reproduction and any fix are not yet estimated.

<a id="d"></a>
## D - PDI-21015: repository VFS refresh (11.0)

- **Cause:** master fixed this under BACKLOG-50458 in two commits that are not
  on 11.0. D1 `a7e490b133f`: `refresh()` calls `super.refresh()` to clear the
  Commons VFS child-name cache. D2 `af236de294b`: the connection list
  refreshes the metastore. 10.2 has no repo-vfs module.
- **Tests:** 2 new `LocalPurTest` tests (another client adds or deletes a
  file, then refresh). Needs VPN.
- **PoC:** D1 backported to `LocalPurFileObject.refresh()` (one line);
  `LocalPurTest` 8/8.
- **Commits (pentaho-kettle 11.0):** `e77e8cb86f0 -> 6059096c908`.
- **Open:** D2 not reproduced or ported; 11.0 pentaho-metastore lacks
  `IMetaStore.refresh()`. Re-enable the QA scenarios disabled by QUALITY-12059.
- **Remaining effort:** 0.5 day (D1) + 1-2 days (D2, if wanted).

<a id="e"></a>
## E - PDI-19753: Hadoop bridge file-system cache key

- **Cause:** pentaho-hadoop-shims `PvfsHadoopBridge` caches one Hadoop
  `FileSystem` per `PvfsConf`, ignoring the bucket. Bucket A's S3A file
  system is reused for bucket B, and `checkPath()` throws `Wrong FS`.
- **Test:** `ParquetPvfsS3AMultipleBucketsIT` (MinIO), with a one-bucket control.
- **PoC:** key the cache by connection and bucket (authority), in
  `common-base` (master, 11.0) and `shims/apache/driver` (10.2); shim module
  tests have no failures.
- **Commits (pentaho-hadoop-shims):** master `9571093c14 -> 6ca62b11a9`, 11.0
  `e6246f847b -> 68ecfa479a`, 10.2 `8bb756972a -> 96214c8110`.
- **Open:** the "last used" `fs` field is still order-dependent; relies on
  `fs.s3a.impl.disable.cache=true`; authority for HCP/GCS/ADLS unchecked;
  cached file systems not closed on `close()`; the cache key holds
  credentials (pre-existing).
- **Remaining effort:** 1.5-2 days.

<a id="f"></a>
## F - PDI-20874: GCS credential materialization (not a cache bug)

- **Cause:** `GoogleCloudStorageProvider.prepare()` stores the key JSON at
  save time and `getOpts()` always uses it, never re-reading `keyPath`. On
  11.0 and 10.2 `prepare()` also skips variable substitution, so a variable
  key path is never read (fixed on master in `3a1b83448`). This is why master
  fails 2 of 3 tests and 11.0/10.2 fail all 3.
- **Tests:** `GoogleCloudStorageCredentialPersistenceTest` (3 tests).
- **PoC:** `getOpts()` reads the currently resolved `keyPath`, falling back
  to the stored key only when there is no path; `prepare()` substitutes
  variables (backport on 11.0/10.2). Module 7/7 on master and 11.0; 10.2 has
  3 pre-existing `GoogleCloudStorageFileObjectTest` errors (Mockito on JDK
  21), with or without the fix.
- **Commits (pdi-plugins-ee):** master `f124deb06 -> b55ddce9b`, 11.0
  `aeb270115 -> ab3676dd1`, 10.2 `10298cc96 -> bce12e620`.
- **Open:** a missing key file now fails instead of silently using the stale
  key (product decision); `getLocations()` and `test()` still use the stored key.
- **Remaining effort:** 1-2 days.

## Closing the spike

- [x] Every epic ticket mapped to a cause.
- [x] Reproducing test per cause on master, 11.0, 10.2.
- [x] PoC per reproduced cause on remote branches.
- [ ] PDI-20647: confirm the input-step configuration and reproduce the Spoon
      run/delete/rerun workflow on unmodified master; compare the reported
      11.1.0.0-59 build if available.
- [ ] Team decision on the A design (PDI-21035, PDI-20723, PDI-21092).
- [ ] One implementation ticket per cause linked to PDI-21042.

## Appendix: how to run

JDK 21, Maven 3.9, Docker (`quay.io/minio/minio`). D needs VPN (`-U` on the
first run). Off VPN, add `-s central-settings.xml -nsu
-Daether.enhancedLocalRepository.trackingFilename=__none__` (Central mirror).
On kettle 10.2 run Maven from `core/`. For the plugin ITs, first
`mvn -o install -DskipTests` the branch's kettle `core`.

```sh
# A/C (PDI-21035, PDI-20723, PDI-21092 / PDI-20586, PDI-20647) and
# B (PDI-21074, PDI-20299) - kettle core
mvn -o test -Dtest='PvfsResolutionIsolationTest,ConnectionManagerEditIsolationTest' -Dcheckstyle.skip -Djacoco.skip=true

# A/C MinIO ITs - kettle master plugins/s3-vfs/core
mvn verify -Dit.test='PvfsConnectionIsolationMinioIT,CarteConnectionCachingMinioIT' -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dcheckstyle.skip -Djacoco.skip=true
# A/C MinIO ITs - kettle 11.0 plugins/s3-vfs/core, big-data-plugin 10.2 s3-vfs (JUnit 4, surefire)
# 10.2 Carte IT: first install kettle 10.2 core AND engine
mvn test -Dtest='PvfsConnectionIsolationMinioIT,CarteConnectionCachingMinioIT' -Dcheckstyle.skip -Djacoco.skip=true

# D (PDI-21015) - kettle 11.0 plugins/repo-vfs/repo-vfs-server-local (master: repo-vfs-pur)
mvn test -Dtest=LocalPurTest -Dcheckstyle.skip -Djacoco.skip=true

# E (PDI-19753) - pentaho-hadoop-shims root (10.2: -pl shims/apache/driver -am, -DfailIfNoTests=false)
mvn -o -pl common-base -DrunITs test-compile \
  build-helper:add-test-source@test-integration_add-source \
  resources:copy-resources@test-integration_add-resources \
  compiler:testCompile@test-integration_compile \
  failsafe:integration-test failsafe:verify -Dit.test=ParquetPvfsS3AMultipleBucketsIT

# F (PDI-20874) - pdi-plugins-ee root
mvn -o -pl google-cloud-storage-vfs/core -Dtest=GoogleCloudStorageCredentialPersistenceTest test
```

Fixture notes:

- `ConnectionManager` providers are JVM-wide and registered with
  `putIfAbsent`; another suite's `test4` provider silently replaced ours in
  combined runs. `PvfsResolutionIsolationTest` now sets its provider by
  reflection and restores it in `@AfterClass`.
- `VFSHelper.getOpts` needs a `VFSLookupFilter`, otherwise raw-URI tests pass
  vacuously. Use `XmlMetaStore` (not `MemoryMetaStore`) for edit scenarios.
  Use unique connection names per test (Commons VFS caches are process-wide).
- S3 isolation needs two MinIO users with per-bucket policies so
  cross-routing shows up as a real `403`.
