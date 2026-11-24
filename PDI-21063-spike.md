# PDI-21063 - VFS caching spike

Updated: 2026-11-24. Spike [PDI-21063](https://pentaho-eng.atlassian.net/browse/PDI-21063),
epic [PDI-21042](https://pentaho-eng.atlassian.net/browse/PDI-21042).

**Verdict:** the epic is not one bug. Its 10 tickets come from 6 independent
causes (A-F). Five are reproduced with tests and have a proof-of-concept (PoC)
fix that turns the tests green. C is not reproduced on any line.

PoC = proof that a fix direction works, not a production change. Each still
needs design review, hardening, product QA and its own ticket/PR.

## Status per Jira ticket

Results are at the upstream tips on 2026-11-24 (kettle master `e7ec0605c72`,
11.0 `cd4e024b599`, 10.2 `a1e577acf11`). RED = a test asserting correct
behaviour fails; GREEN = it passes; n/a = code not present on that line.

| Jira | Symptom | Cause | master | 11.0 | 10.2 | PoC fix |
| --- | --- | --- | --- | --- | --- | --- |
| [PDI-21035](https://pentaho-eng.atlassian.net/browse/PDI-21035) | Parallel output to two S3 connections -> `403` | A | RED | RED | RED | Proven, all lines |
| [PDI-20723](https://pentaho-eng.atlassian.net/browse/PDI-20723) | Parallel PVFS reads across providers fail | A | RED | RED | RED | Proven, all lines |
| [PDI-21092](https://pentaho-eng.atlassian.net/browse/PDI-21092) | Raw `s3://` + named PVFS mix switches config | A | RED | RED | RED | Proven, all lines |
| [PDI-21074](https://pentaho-eng.atlassian.net/browse/PDI-21074) | Cancel leaks edits; rename corrupts connection | B | GREEN | GREEN | RED | Proven on 10.2 (pentaho/pentaho-kettle#10782) |
| [PDI-20299](https://pentaho-eng.atlassian.net/browse/PDI-20299) | Cancel keeps edited/old name in memory | B | GREEN | GREEN | RED | Proven on 10.2 (pentaho/pentaho-kettle#10782) |
| [PDI-20586](https://pentaho-eng.atlassian.net/browse/PDI-20586) | Edits/variable changes leave stale config (Carte) | C | GREEN | GREEN | GREEN | None - not reproduced |
| [PDI-20647](https://pentaho-eng.atlassian.net/browse/PDI-20647) | Execution works after connection deleted | C | GREEN | GREEN | GREEN | None - not reproduced |
| [PDI-21015](https://pentaho-eng.atlassian.net/browse/PDI-21015) | Repository changes invisible after refresh | D | GREEN (fixed) | RED | n/a | Proven on 11.0 (folder listing only) |
| [PDI-19753](https://pentaho-eng.atlassian.net/browse/PDI-19753) | Parquet S3A to second bucket -> `Wrong FS` | E | RED | RED | RED | Proven, all lines |
| [PDI-20874](https://pentaho-eng.atlassian.net/browse/PDI-20874) | GCS key-path variable never re-resolved | F | RED 2/3 | RED 3/3 | RED 3/3 | Proven, all lines |

## A - PDI-21035, PDI-20723, PDI-21092: shared PVFS resolution context

- **Cause:** `ConnectionFileSystem.initializeVariableSpace()` writes
  `connection=<name>` into the caller's shared `VariableSpace`;
  `KettleVFSImpl.buildFsOptions()` later reads it to build provider options.
  Parallel steps overwrite each other's value, and a raw `s3://` URI inherits
  the last PVFS connection. Provider-independent; the S3 `403` is a symptom.
- **Tests:** `PvfsResolutionIsolationTest` (3 deterministic core tests) and
  `PvfsConnectionIsolationMinioIT` (real S3 via MinIO, two users restricted
  to their own buckets). Before the fix, 38-40 of 80 parallel writes got
  `403` on every line.
- **PoC:** resolve into a child `Variables` initialized from the caller's
  space, so the caller's space is never written. The method is identical on
  all lines; one commit cherry-picks cleanly.
- **PoC result:** core tests 3 RED -> GREEN and MinIO IT 5/5 on master, 11.0
  and 10.2 (10.2 IT lives in big-data-plugin). Neighbouring suites
  (`org.pentaho.di.connections.**`, `org.pentaho.di.core.vfs.**`) have no new
  failures: master 264/264, 11.0 203/203, 10.2 204/204. Full master s3-vfs
  module passes.
- **Open:** pick the final design (PoC child space vs passing the connection
  explicitly); check the cost of copying variables per lookup; search plugins
  for code reading `connection` after a lookup; nested `zip:pvfs://` untested.

## B - PDI-21074, PDI-20299: live connection details (10.2 only)

- **Cause:** 10.2 `ConnectionManager.getConnectionDetails()` returns the cached
  instance, so dialogs mutate it before Save and Cancel cannot undo it.
  Master and 11.0 already return a clone.
- **Tests:** `ConnectionManagerEditIsolationTest` (3 tests), RED on 10.2 only.
- **PoC:** cherry-pick of the open PR pentaho/pentaho-kettle#10782. 3 RED ->
  GREEN, no core regressions.
- **Open:** merge pentaho/pentaho-kettle#10782, optionally with these tests.

## C - PDI-20586, PDI-20647: stale config / deleted connection (not reproduced)

- **Tests:** 3 core tests and 3 MinIO tests: a changed credential variable,
  an edited+saved connection and a deleted connection are all honoured on the
  next lookup. GREEN on every line. The S3 file-system cache is keyed by the
  resolved options, so new credentials produce a new file system.
- **Not yet probed:** Carte remote variable flow and the Spoon browse-tree
  cache (PDI-20586); already-open handles after delete (PDI-20647). A can
  produce the same symptom, so re-test after A is fixed.

## D - PDI-21015: repository VFS refresh (11.0)

- **Cause:** master fixed this under BACKLOG-50458 in two commits, neither on
  11.0. D1 `a7e490b133f`: `refresh()` calls `super.refresh()` to clear the
  Commons VFS child-name cache. D2 `af236de294b`: the connection list
  refreshes the metastore. 10.2 has no repo-vfs module.
- **Tests:** 2 new `LocalPurTest` tests (another client adds/deletes a file,
  then refresh). RED on 11.0, GREEN on master. Needs VPN.
- **PoC:** D1 backported to `LocalPurFileObject.refresh()` (one line).
  2 RED -> GREEN, `LocalPurTest` 8/8.
- **Open:** D2 not reproduced and not ported: 11.0 pentaho-metastore lacks
  `IMetaStore.refresh()`. Re-enable the 11 QA scenarios disabled by
  QUALITY-12059 after the backport.

## E - PDI-19753: Hadoop bridge file-system cache key

- **Cause:** pentaho-hadoop-shims `PvfsHadoopBridge` caches one Hadoop
  `FileSystem` per `PvfsConf`, without the bucket. The S3A file system for
  bucket A is reused for bucket B and `checkPath()` throws `Wrong FS`.
- **Test:** `ParquetPvfsS3AMultipleBucketsIT` (MinIO), plus a one-bucket control.
- **PoC:** key the cache by connection and bucket (authority). Same change in
  `common-base` (master, 11.0) and `shims/apache/driver` (10.2).
  1 error -> 0 on all lines; shim module tests have no failures.
- **Open:** the "last used" `fs` field is still order-dependent; relies on
  `fs.s3a.impl.disable.cache=true`; authority for HCP/GCS/ADLS unchecked;
  cached file systems are not closed on `close()`; the cache key holds
  credentials (pre-existing).

## F - PDI-20874: GCS credential materialization (not a cache bug)

- **Cause:** `GoogleCloudStorageProvider.prepare()` stores the key JSON at
  save time and `getOpts()` always uses it, never re-reading `keyPath`. On
  11.0 and 10.2 `prepare()` also skips variable substitution, so a variable
  key path is never read (master fixed that part in `3a1b83448`).
- **Tests:** `GoogleCloudStorageCredentialPersistenceTest` (3 tests).
- **PoC:** `getOpts()` resolves and reads the current `keyPath`, falling back
  to the stored key only when there is no path; `prepare()` substitutes
  variables (backport on 11.0/10.2). RED -> 3/3 on all lines; module 7/7 on
  master and 11.0. 10.2 has 3 pre-existing `GoogleCloudStorageFileObjectTest`
  errors (Mockito on JDK 21), unchanged by the fix.
- **Open:** a missing key file now fails instead of silently using the stale
  key (product decision); `getLocations()` and `test()` still use the stored key.

## Branches and commits

All on the `befc` forks. Each branch: reproduction tests first (RED), then
the PoC fix (GREEN). Format: test commit -> fix commit.

| Cause (Jira) | Repo | master | 11.0 | 10.2 |
| --- | --- | --- | --- | --- |
| A (PDI-21035, PDI-20723, PDI-21092) | [pentaho-kettle](https://github.com/befc/pentaho-kettle/branches/all?query=spike%2FPDI-21063) | `8548d5cc84e` -> `99b2ae9bfbf` | `e77e8cb86f0` -> `cd54cca1fac` | `1c2c118331d` -> `b7aa087c35a` |
| A (10.2 S3 IT only) | [big-data-plugin](https://github.com/befc/big-data-plugin/tree/spike/PDI-21063-10.2) | - | - | `d6688ef35` (test) |
| B (PDI-21074, PDI-20299) | pentaho-kettle | tests only | tests only | `1c2c118331d` -> `915ba742858` |
| C (PDI-20586, PDI-20647) | pentaho-kettle | tests only | tests only | tests only |
| D (PDI-21015) | pentaho-kettle | tests only | `e77e8cb86f0` -> `6059096c908` | - |
| E (PDI-19753) | [pentaho-hadoop-shims](https://github.com/befc/pentaho-hadoop-shims/branches/all?query=spike%2FPDI-21063) | `9571093c14` -> `6ca62b11a9` | `e6246f847b` -> `68ecfa479a` | `8bb756972a` -> `96214c8110` |
| F (PDI-20874) | [pdi-plugins-ee](https://github.com/befc/pdi-plugins-ee/branches/all?query=spike%2FPDI-21063) | `f124deb06` -> `b55ddce9b` | `aeb270115` -> `ab3676dd1` | `10298cc96` -> `bce12e620` |

Branch names: `spike/PDI-21063-{master,11.0,10.2}`. This document is on
pentaho-kettle `spike/PDI-21063-master`. Build changes are test-scoped only
(MinIO Testcontainers; logging deps in shims 10.2).

## Remaining effort and next steps

Engineering person-days with Copilot assistance, after the PoCs.

| Cause (Jira) | Remaining work | Days |
| --- | --- | --- |
| A (PDI-21035, PDI-20723, PDI-21092) | Decide design, harden on master, backport to 11.0/10.2, QA | 3-4.5 |
| B (PDI-21074, PDI-20299) | Merge pentaho/pentaho-kettle#10782 + tests | 0.5 |
| C (PDI-20586, PDI-20647) | Carte / browse-tree / open-handle probes; fix not estimated | 2-4 |
| D (PDI-21015) | D1 backport PR; D2 metastore backport if wanted | 0.5 + 1-2 |
| E (PDI-19753) | Harden PoC on master, 11.0, 10.2 | 1.5-2 |
| F (PDI-20874) | Product decision on missing key file, harden, backports | 1-2 |

To close the spike:

- [x] Every epic ticket mapped to a cause.
- [x] Reproducing test per cause on master, 11.0, 10.2.
- [x] PoC per reproduced cause on remote branches.
- [ ] Team decision on the A design (PDI-21035, PDI-20723, PDI-21092).
- [ ] One implementation ticket per cause linked to PDI-21042; C stays an
      investigation ticket (PDI-20586, PDI-20647).

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

# A/C MinIO IT - kettle master plugins/s3-vfs/core
mvn verify -Dit.test=PvfsConnectionIsolationMinioIT -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dcheckstyle.skip -Djacoco.skip=true
# A/C MinIO IT - kettle 11.0 plugins/s3-vfs/core, big-data-plugin 10.2 s3-vfs (JUnit 4, surefire)
mvn test -Dtest=PvfsConnectionIsolationMinioIT -Dcheckstyle.skip -Djacoco.skip=true

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
