# PDI-21063 - VFS caching investigation

Updated: 2026-11-24.

## Purpose and baseline

[PDI-21063](https://pentaho-eng.atlassian.net/browse/PDI-21063) is an open P1
spike under [PDI-21042](https://pentaho-eng.atlassian.net/browse/PDI-21042).
The aim is to reproduce the epic's reported symptoms, separate their causes,
and choose fixes or workarounds based on evidence.

- Spike branches (on the `befc` forks, never upstream): `spike/PDI-21063-{master,11.0,10.2}`
  in each touched repository. See
  [Proof-of-concept fixes and branches](#proof-of-concept-fixes-and-branches).
- Starting revision: `e7ec0605c722ba5574951a21bc1ac1e464b7ef87` (upstream master,
  re-verified as the upstream tip on 2026-11-24).
- Release branches checked on 2026-11-24 at their upstream tips: see
  [Cross-branch results](#cross-branch-results-master-110-102).
- Stage 1 (reproduction) changed tests only. Stage 2 adds a timeboxed
  proof-of-concept (PoC) fix per reproduced cause (A, B, D, E, F). A PoC proves
  that a fix direction turns the red tests green without regressing nearby
  suites. It is not a production-ready change: each still needs design review,
  product QA and a normal ticket/PR.
- A failing test must assert the expected product contract, not assert that a
  known bug exists. A passing test on master may mean a release-specific fix
  already exists; it does not disprove the release report.
- The initial assessment below is a hypothesis map, not a declaration that all
  symptoms have been reproduced or share a root cause.

## Ticket-to-issue matrix

Status key: **Reproduced** = a test asserting the correct contract fails for
the reported reason; **Not reproduced** = the probe passes at that layer
(not a disproof of the field report); **Blocked** = no runnable evidence yet.

| Epic ticket | Reported symptom | Cause | Status | Evidence |
| --- | --- | --- | --- | --- |
| [PDI-21035](https://pentaho-eng.atlassian.net/browse/PDI-21035) | Parallel output to two S3 connections fails with 403 | **A** shared resolution context | **Reproduced** on master, 11.0 and 10.2 (unit + MinIO IT) | Unit: deterministic wrong options. MinIO: 38-40 of 80 parallel writes get a real `403` on every branch. |
| [PDI-20723](https://pentaho-eng.atlassian.net/browse/PDI-20723) | Parallel PVFS reads across providers/endpoints fail | **A** | **Reproduced** via the same mechanism on master, 11.0 and 10.2 (unit, provider-independent) | Same unit test as PDI-21035; resolution, not read vs write, is what races. |
| [PDI-21092](https://pentaho-eng.atlassian.net/browse/PDI-21092) | Mixed raw `s3://` and named PVFS selection changes configuration | **A** | **Reproduced** on master, 11.0 and 10.2 (unit + MinIO IT) | PVFS lookup overwrites caller's `connection` variable; the following raw `s3://` write uses the wrong credentials -> `403`. |
| [PDI-21074](https://pentaho-eng.atlassian.net/browse/PDI-21074) | Cancel leaks edits; rename corrupts live connection | **B** live cached details (10.2) | **Reproduced on 10.2**; green on master and 11.0 | 2 tests red on 10.2 (`a1e577acf11`, upstream tip). Fix exists in master/11.0; 10.2 PR [pentaho/pentaho-kettle#10782](https://github.com/pentaho/pentaho-kettle/pull/10782) open. |
| [PDI-20299](https://pentaho-eng.atlassian.net/browse/PDI-20299) | Cancel leaves an edited/old connection name in memory | **B** | **Reproduced on 10.2**; green on master and 11.0 | Scheme lookup returns the cancelled rename. |
| [PDI-20586](https://pentaho-eng.atlassian.net/browse/PDI-20586) | Edits/variable changes leave stale config (esp. Carte) | **C** (unconfirmed); A can mimic it | **Not reproduced** on master, 11.0 or 10.2 (unit + MinIO) | Changed credential variable, edited+saved connection are both honoured on next lookup, at core and real-S3 level. Remaining suspects: Carte variable flow, browse-tree cache, A. |
| [PDI-20874](https://pentaho-eng.atlassian.net/browse/PDI-20874) | GCS key-path variable not re-resolved, even after restart | **F** GCS credential materialization (new, not VFS caching) | **Reproduced** on master (2/3), 11.0 and 10.2 (3/3) | `prepare()` persists the key JSON; `getOpts()` uses it and never re-reads the resolved `keyPath`. On 10.2/11.0 `prepare()` does not even substitute variables in `keyPath`, so a variable key path is never read. |
| [PDI-20647](https://pentaho-eng.atlassian.net/browse/PDI-20647) | Execution still succeeds after deleting its connection | **C** (unconfirmed) | **Not reproduced** on master, 11.0 or 10.2 (unit + MinIO) | Fresh lookup after delete fails as expected. Ticket has no reproducer; open-handle / already-loaded-step path untested. |
| [PDI-21015](https://pentaho-eng.atlassian.net/browse/PDI-21015) | Repository project/config changes invisible after refresh | **D** repo-VFS refresh | **Reproduced on 11.0** (folder listing); green on master; n/a on 10.2 (no repo-vfs plugin) | After another client adds/deletes a file, `refresh()` still returns the old child list. Applying master's `a7e490b133f` on 11.0 turns it green. Connection-list part (`af236de294b`) not reproduced; needs a metastore backport on 11.0. QA scenarios disabled by QUALITY-12059. |
| [PDI-19753](https://pentaho-eng.atlassian.net/browse/PDI-19753) | Parquet S3A output to a second bucket raises `Wrong FS` | **E** PVFS->Hadoop bridge FS cache | **Reproduced** on master, 11.0 and 10.2 (pentaho-hadoop-shims, MinIO) | Second bucket fails `Wrong FS s3a://bucket-b... expected s3a://bucket-a...`. |

PDI-21063 is the coordinating spike, not an additional runtime failure.

Summary: the epic is **not one bug**. Six distinct causes, each owning a
fixed set of tickets. Every later section, test, finding and workstream is
labelled with the cause letter **and** its Jira IDs.

| Cause | Jira tickets | Lines affected (tested) | Upstream fix | Spike PoC (red -> green) |
| --- | --- | --- | --- | --- |
| **A** Shared PVFS resolution context | [PDI-21035](https://pentaho-eng.atlassian.net/browse/PDI-21035), [PDI-20723](https://pentaho-eng.atlassian.net/browse/PDI-20723), [PDI-21092](https://pentaho-eng.atlassian.net/browse/PDI-21092) | master, 11.0, 10.2 | None | **Proven** on master, 11.0, 10.2 (core 3 -> 0 red; MinIO IT 5/5 green) |
| **B** Live connection details (10.2) | [PDI-21074](https://pentaho-eng.atlassian.net/browse/PDI-21074), [PDI-20299](https://pentaho-eng.atlassian.net/browse/PDI-20299) | 10.2 only | On master/11.0; 10.2 PR pentaho/pentaho-kettle#10782 open | **Proven** on 10.2 by cherry-picking pentaho/pentaho-kettle#10782 (3 -> 0 red) |
| **C** Stale config / deleted connection | [PDI-20586](https://pentaho-eng.atlassian.net/browse/PDI-20586), [PDI-20647](https://pentaho-eng.atlassian.net/browse/PDI-20647) | Not reproduced on any line | n/a | None (nothing red to fix) |
| **D** Repository VFS refresh | [PDI-21015](https://pentaho-eng.atlassian.net/browse/PDI-21015) | 11.0 (master fixed; 10.2 has no repo-vfs) | On master only (`a7e490b133f`, `af236de294b`) | **Proven** on 11.0 for D1 (`a7e490b133f` backport, 2 -> 0 red); D2 not attempted |
| **E** Hadoop bridge FS cache key | [PDI-19753](https://pentaho-eng.atlassian.net/browse/PDI-19753) | master, 11.0, 10.2 | None | **Proven** on master, 11.0, 10.2 (IT 1 -> 0 errors) |
| **F** GCS credential materialization | [PDI-20874](https://pentaho-eng.atlassian.net/browse/PDI-20874) | master, 11.0, 10.2 | None | **Proven** on master, 11.0, 10.2 (3/3 green) |

## A. Shared PVFS resolution context (PDI-21035, PDI-20723, PDI-21092)

Tickets: PDI-21035 (parallel S3 output -> 403), PDI-20723 (parallel PVFS
reads across providers), PDI-21092 (mixed raw `s3://` + named PVFS).

`ConnectionFileSystem.initializeVariableSpace()` takes the variable space from
the filesystem options and writes `connection=<name>` into it. Later
`KettleVFSImpl.buildFsOptions()` reads that variable and calls
`VFSHelper.getOpts(...)`. When callers share a transformation variable space,
another resolution can change the name between those operations.

Expected symptoms include selecting the other connection's credentials and
leaking a named connection into a later raw-provider URI request. This is
provider-independent; an S3 403 is an observable consequence, not proof that
the S3 client cache itself is wrong.

Candidate fixes:

1. Isolate the per-resolution variable space, preserving caller variables but
   never writing internal connection selection to the caller's space.
2. Prefer explicit connection/options propagation over a magic variable.
   Audit nested/archive URIs and raw-provider behavior before removing the
   compatibility path.
3. Do not solve this by serializing all VFS calls or merely disabling caches:
   neither addresses the ownership of mutable resolution state cleanly.

Cloning connection details alone does not clone the variable space passed to
them, so the existing master/11.0 clone behavior does not eliminate A.

**Confirmed on master** (`e7ec0605c72`). Write site:
`core/.../connections/vfs/provider/ConnectionFileSystem.java`
`initializeVariableSpace()` (`varSpace.setVariable( "connection", name )`).
Read site: `core/.../core/vfs/KettleVFSImpl.java` `buildFsOptions()` (loop over
`listVariables()` -> `VFSHelper.getOpts(...)`). In the MinIO IT almost every
write of the "losing" connection in a round fails, i.e. under real parallel
steps this is close to systematic rather than a rare race. The S3 provider
already works around the same variable (`S3Provider` "use an empty Variables
to prevent other 'connection' values from causing StackOverflowErrors").

## B. Live connection details on older releases (PDI-21074, PDI-20299)

Tickets: PDI-21074 (cancel leaks edits; rename corrupts live connection),
PDI-20299 (cancel leaves edited/old name in memory).

The 10.2 getters return objects from `detailsByName`; callers/dialogs can mutate
the cached object before Save. Cancel cannot roll those changes back. Save,
rename and in-place encryption can then expose mutated state in the session.

Candidate fix: return defensive copies, as master/11.0 do, and verify that
provider `cloneDetails()` implementations also isolate mutable fields.
Use 10.2 for red evidence and master for a comparison; do not make master fail
artificially to reproduce an older-release defect.

**Confirmed on 10.2** (`a1e577acf11`): `ConnectionManager.getConnectionDetails(..)`
returns the live `detailsByName` instance; master/11.0 return `clone(..)`.

## C. Refresh, substitution and lifecycle (PDI-20586, PDI-20647)

Tickets: PDI-20586 (edits/variable changes leave stale config, esp. Carte),
PDI-20647 (execution succeeds after its connection is deleted).

Separate the following assertions:

- PDI-20586: same connection, changed variable values between
  resolutions/executions.
- PDI-20586: explicit connection edit/save invalidates the appropriate
  cached state.
- PDI-20647: deleted connection is not usable through a fresh lookup.
- PDI-20647: reusing an already-open file/stream after deletion may have a
  different contract; define it before treating that as a defect.

**Findings so far.** On master none of the C cases reproduce at core level or
against real S3 (MinIO): a changed credential variable, an edited+saved
connection and a deleted connection are all honoured on the next lookup. The
S3 file-system cache is keyed by the resolved options, so new credentials
produce a new file system. This matches the repeated "cannot reproduce" in
PDI-20586. The same probes are also green on 11.0 and 10.2 (see
cross-branch results). Still untested: Carte/remote execution variable flow
(PDI-20586), Spoon browse-tree caching (PDI-20586), already-open handles
(PDI-20647).

PDI-20874 was originally grouped under C but is a separate GCS defect; see F.

Candidate designs for C (PDI-20586, PDI-20647), only after reproduction: cache immutable resolved options
per execution/configuration identity; invalidate on appropriate lifecycle
events; or provide a no-cache policy. A UI switch requires metastore/UI/provider
work. Do not put credential text into logs or diagnostic cache keys.

## D. Repository VFS refresh (PDI-21015)

Ticket: PDI-21015 (repository project/config changes invisible after refresh).

Exercise an actual repository-backed file/config update observed from a
different client and then an explicit refresh. An ordinary local-file test
cannot establish this bug. First inspect the existing BACKLOG-50458 fix and
the tests suppressed by QUALITY-12059 before adding duplicate coverage.

**Status: reproduced on 11.0 (2026-11-24, on VPN).** The master fix for
BACKLOG-50458 has two parts, and neither is on 11.0:

- **D1, folder listing (`a7e490b133f`).** `PurFileObject.refresh()` now calls
  `super.refresh()`, which clears the child-name cache that Commons VFS fills
  in `getChildren()`. On 11.0 the class is `LocalPurFileObject` in
  `plugins/repo-vfs/repo-vfs-server-local`, and its `refresh()` only reloads
  the file itself. So after another client adds or deletes a file, refresh
  still lists the old children.
  - Reproduced with two new `LocalPurTest` tests (red on 11.0, green on
    master).
  - Proven fix: adding the one-line `super.refresh()` on 11.0 makes the
    module green (8/8). This was applied temporarily and reverted.
- **D2, connection list (`af236de294b`).** `ConnectionManager` now calls
  `factory.getMetaStore().refresh()` before loading connections. Not
  reproduced: it needs a caching repository metastore and a second client.
  It also can't be cherry-picked to 11.0, because the latest 11.0
  `pentaho:metastore` snapshot has no `IMetaStore.refresh()`. That would
  need a pentaho-metastore backport first.
- **10.2 is not affected:** it has no `plugins/repo-vfs` module.

QUALITY-12059 disabled 11 related QA scenarios (qa-automation PR #7382,
`pdi/src/test/resources/tk421/suites/server/project_profile_smoke_tests.xml`);
re-enabling them on 11.0 after the backport is the product-level check.

Fixture note: `LocalPurTest` needs `pentaho:pentaho-platform-repository:tests`
from `repo.eng.pentaho.com` (VPN). Run with `-U` the first time: stale local
snapshot metadata pointed at a build without the `tests` jar.

## E. Multi-bucket Hadoop/Parquet output (PDI-19753)

Ticket: PDI-19753 (Parquet S3A output to a second bucket raises `Wrong FS`).

Reproduce the production Parquet/S3A call path with one PVFS connection and two
different buckets. A Commons VFS text-output test does not cover Hadoop
`FileSystem.checkPath()` or the Parquet shim. Use a test-owned S3 service if
the owning module's harness supports it.

**Confirmed on pentaho-hadoop-shims master** (`df750a007a3`):
`common-base/.../shim/common/pvfs/PvfsHadoopBridge.java` keeps its own
`Cache<PvfsConf, FileSystem> fsCache` (it disables the Hadoop FS cache because
that only keys on scheme+authority). The `PvfsConf` key does not include the
bucket, so the S3A FileSystem created for bucket A is reused for bucket B and
`FileSystem.checkPath()` throws `Wrong FS`. Independent of A; fix is to include
the bucket/authority in the cache key (or one FS per bucket).

## F. GCS credential materialization (PDI-20874)

Ticket: PDI-20874 (GCS key-path variable not re-resolved, even after restart).
Not a VFS cache defect.

`GoogleCloudStorageProvider.prepare()` reads the resolved key path and stores
the key JSON in `serviceAccountKey`; `getOpts()` uses the stored JSON and never
re-resolves `keyPath`, which also explains persistence across restart.

On 10.2 and 11.0 `prepare()` additionally does not substitute variables in
`keyPath` (master gained that in `3a1b83448`, BACKLOG-42701), so a variable key
path is never read at all. Candidate fix: resolve `keyPath` in the execution
space inside `getOpts()` (persisted JSON only as fallback), and backport the
`prepare()` substitution to 10.2/11.0.

## Reproduction strategy and evidence ledger

Prefer deterministic unit tests for the state ownership race and connection
object mutation. Use integration tests for real filesystem/provider lifecycle,
repository refresh and Hadoop/Parquet boundaries.

For S3 integration coverage, use a test-owned container, random loopback ports,
two independently restricted credentials/buckets, and distinct sentinel data.
Assert the actual data/target as well as success: shared credentials with broad
permissions can mask cross-routing. Do not depend on cloud credentials or an
already running workstation service.

Revisions: pentaho-kettle master `e7ec0605c72` (worktree
`pentaho-kettle.worktrees/PDI-21063`), pentaho-kettle 10.2 `a1e577acf11`
(`pentaho-kettle.worktrees/PDI-21063-10.2`), pdi-plugins-ee master
`ab9e0cf8169`, pentaho-hadoop-shims master `df750a007a3`. JDK 21.0.12, Maven
3.9.16, Docker Desktop, `quay.io/minio/minio` RELEASE.2025-09-07.

| Cause | Jira | Test (class#method) | Repo / rev | Result |
| --- | --- | --- | --- | --- |
| A | PDI-21035, PDI-20723 | `PvfsResolutionIsolationTest#pdi21035AndPdi20723ParallelResolutionsKeepTheirOwnConnectionOptions` | kettle master `core` | **RED** (deterministic): expected `first-*`, got `second-*` options |
| A | PDI-21092 | `PvfsResolutionIsolationTest#pdi21092PvfsDoesNotReplaceCallerConnectionParameter` | kettle master `core` | **RED**: caller `connection` = `first-*` instead of `application-parameter` |
| A | PDI-21092 | `PvfsResolutionIsolationTest#pdi21092RawUriDoesNotInheritPreviouslySelectedPvfsConnection` | kettle master `core` | **RED**: raw `test4://` URI gets `first-*` options |
| A | PDI-21035, PDI-20723 | `PvfsConnectionIsolationMinioIT#pdi21035ParallelPvfsWritesUseTheirOwnConnection` | kettle master `plugins/s3-vfs/core` | **RED** (3/3 runs): 38-39 of 80 writes `S3Exception: Forbidden (403)` |
| A | PDI-21092 | `PvfsConnectionIsolationMinioIT#pdi21092PvfsLookupDoesNotHijackCallerConnectionForRawS3` | kettle master `plugins/s3-vfs/core` | **RED**: `connection` overwritten to `conn-a-*`; raw `s3://bucket-b` write -> `403` |
| B | PDI-21074 | `ConnectionManagerEditIsolationTest#pdi21074CancelDiscardsUnsavedCredentialEdit` | kettle 10.2 `core` | **RED**: `expected original-token but was wrong-token` (GREEN on master) |
| B | PDI-21074 | `ConnectionManagerEditIsolationTest#pdi21074SavingRenamedConnectionDoesNotCorruptOriginal` | kettle 10.2 `core` | **RED**: `expected original but was renamed` (GREEN on master) |
| B | PDI-20299 | `ConnectionManagerEditIsolationTest#pdi20299CancelDiscardsUnsavedRenameFromSchemeLookup` | kettle 10.2 `core` | **RED**: `expected original but was cancelled-rename` (GREEN on master) |
| C | PDI-20586 | `PvfsResolutionIsolationTest#pdi20586ChangedVariableIsResolvedOnNextLookup` | kettle master `core` | GREEN (not reproduced) |
| C | PDI-20586 | `PvfsResolutionIsolationTest#pdi20586SavedEditIsUsedOnNextLookupOfSamePvfsUri` | kettle master `core` | GREEN (not reproduced) |
| C | PDI-20647 | `PvfsResolutionIsolationTest#pdi20647DeletedConnectionIsNotUsableThroughFreshLookup` | kettle master `core` | GREEN (not reproduced) |
| C | PDI-20586 | `PvfsConnectionIsolationMinioIT#pdi20586ChangedCredentialVariableIsUsedOnNextLookup` | kettle master `plugins/s3-vfs/core` | GREEN (not reproduced against real S3) |
| C | PDI-20586 | `PvfsConnectionIsolationMinioIT#pdi20586EditedConnectionIsUsedOnNextLookup` | kettle master `plugins/s3-vfs/core` | GREEN (not reproduced against real S3) |
| C | PDI-20647 | `PvfsConnectionIsolationMinioIT#pdi20647DeletedConnectionIsNotUsable` | kettle master `plugins/s3-vfs/core` | GREEN (not reproduced against real S3) |
| D | PDI-21015 | `LocalPurTest#pdi21015RefreshShowsChildAddedByAnotherClient` | kettle 11.0 `plugins/repo-vfs/repo-vfs-server-local` | **RED**: `expected: <2> but was: <1>` (GREEN on master `repo-vfs-pur`) |
| D | PDI-21015 | `LocalPurTest#pdi21015RefreshHidesChildDeletedByAnotherClient` | kettle 11.0 `plugins/repo-vfs/repo-vfs-server-local` | **RED**: `expected: <1> but was: <2>` (GREEN on master `repo-vfs-pur`) |
| E | PDI-19753 | `ParquetPvfsS3AMultipleBucketsIT#onePvfsConnectionWritesParquetToTwoBuckets` | pentaho-hadoop-shims `common-base` (MinIO) | **RED** (2/2 runs): `Wrong FS s3a://bucket-b-... expected s3a://bucket-a-...` |
| E | PDI-19753 (control) | `ParquetPvfsS3AMultipleBucketsIT#parquetOutputToSingleBucketWorks` | pentaho-hadoop-shims `common-base` (MinIO) | GREEN (control: one bucket works) |
| F | PDI-20874 (control) | `GoogleCloudStorageCredentialPersistenceTest#savedKeyPathExpressionSurvivesFreshMetastoreLoad` | pdi-plugins-ee master `google-cloud-storage-vfs/core` | GREEN on master (RED on 10.2/11.0) |
| F | PDI-20874 | `GoogleCloudStorageCredentialPersistenceTest#reopenedConnectionUsesCurrentKeyPathVariable` | pdi-plugins-ee master `google-cloud-storage-vfs/core` | **RED**: after a restart-equivalent reload, the key saved earlier (`first@`) is used instead of the currently resolved `keyPath` (`second@`) |
| F | PDI-20874 | `GoogleCloudStorageCredentialPersistenceTest#reopenedConnectionRejectsMissingCurrentKeyFile` | pdi-plugins-ee master `google-cloud-storage-vfs/core` | **RED**: the current `keyPath` points to a missing file, but the stale saved key is still used (no `NoSuchFileException`) |

Commands (from each module directory):

```sh
# A (PDI-21035/20723/21092) + C (PDI-20586/20647) unit, B (PDI-21074/20299) unit
mvn -o -q test -Dtest=PvfsResolutionIsolationTest -Dcheckstyle.skip -Djacoco.skip=true
mvn -o -q test -Dtest=ConnectionManagerEditIsolationTest -Dcheckstyle.skip -Djacoco.skip=true

# A + C MinIO IT (kettle master plugins/s3-vfs/core). Off VPN, plugins are fetched
# from Central via a mirror-only settings file; local Pentaho snapshots are reused.
mvn -s central-settings.xml -nsu verify -Daether.enhancedLocalRepository.trackingFilename=__none__ \
  -Dit.test=PvfsConnectionIsolationMinioIT -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false \
  -Dcheckstyle.skip -Djacoco.skip=true

# F, PDI-20874 (pdi-plugins-ee root)
mvn -o -pl google-cloud-storage-vfs/core -Dtest=GoogleCloudStorageCredentialPersistenceTest test

# D, PDI-21015 (VPN; kettle 11.0 plugins/repo-vfs/repo-vfs-server-local,
# master plugins/repo-vfs/repo-vfs-pur). Add -U on the first run.
mvn -q test -Dtest=LocalPurTest -Dcheckstyle.skip -Djacoco.skip=true

# E, PDI-19753 (pentaho-hadoop-shims root)
mvn -o -pl common-base -DrunITs test-compile \
  build-helper:add-test-source@test-integration_add-source \
  resources:copy-resources@test-integration_add-resources \
  compiler:testCompile@test-integration_compile \
  failsafe:integration-test failsafe:verify -Dit.test=ParquetPvfsS3AMultipleBucketsIT

# A + C MinIO IT on 11.0 (kettle plugins/s3-vfs/core) and 10.2 (big-data-plugin s3-vfs),
# after installing the branch's kettle-core into ~/.m2 (mvn -o install -DskipTests)
mvn -s central-settings.xml -nsu -Daether.enhancedLocalRepository.trackingFilename=__none__ \
  test -Dtest=PvfsConnectionIsolationMinioIT -Dcheckstyle.skip -Djacoco.skip=true

# A + B + C core on 10.2/11.0 (from core/ on 10.2)
mvn -o test -Dtest='PvfsResolutionIsolationTest,ConnectionManagerEditIsolationTest' \
  -Dcheckstyle.skip -Djacoco.skip=true
```

Fixture notes (for whoever extends these tests):

- `ConnectionManager.getInstance(bowl)` shares providers and lookup filters
  JVM-wide (`putIfAbsent`); register test providers once (`@BeforeClass`).
  `putIfAbsent` is not enough when other suites in the same JVM register the
  same key first: `ConnectionFileProviderTest` registers a `test4` provider,
  so in a combined run `PvfsResolutionIsolationTest` (A: PDI-21035, PDI-20723,
  PDI-21092; C: PDI-20586, PDI-20647) silently used the wrong provider. The
  committed test now puts its provider into `connectionProviders` by
  reflection and restores the previous one in `@AfterClass`.
- `VFSHelper.getOpts` needs a `VFSLookupFilter`; without it options are null and
  raw-URI tests pass vacuously.
- `MemoryMetaStore` cannot re-save an existing element; use `XmlMetaStore` on a
  temp folder for edit scenarios.
- Commons VFS caches are process-wide; use unique connection names per test.
- `minio/minio` is no longer pullable from Docker Hub; the IT uses
  `quay.io/minio/minio` with `asCompatibleSubstituteFor("minio/minio")`
  (Testcontainers 2.0.5, test scope only). Bucket isolation uses two MinIO
  users with per-bucket policies so cross-routing surfaces as a real 403.

Update this ledger with exact class/method, command, revision, counts and
observed mismatch. Distinguish product assertion failures from fixture errors,
dependency-resolution failures and unavailable infrastructure.

## Cross-branch results (master, 11.0, 10.2)

Run on 2026-11-24 after `git fetch upstream`. Every repository was checked
at its upstream branch tip. The master worktrees were already at the tip
(0 commits behind), so the master results above stand as recorded.

| Repo | master | 11.0 | 10.2 |
| --- | --- | --- | --- |
| pentaho-kettle | `e7ec0605c72` | `cd4e024b599` | `a1e577acf11` |
| big-data-plugin (10.2 S3 provider) | n/a | n/a | `f7274e20a` |
| pentaho-hadoop-shims | `df750a007a` | `a383bb8408` | `381a33596a` |
| pdi-plugins-ee | `ab9e0cf81` | `9aaad602b` | `051f68059` |

| Cause | Jira | Test | master | 11.0 | 10.2 |
| --- | --- | --- | --- | --- | --- |
| A | PDI-21035, PDI-20723 | `PvfsResolutionIsolationTest#pdi21035AndPdi20723ParallelResolutionsKeepTheirOwnConnectionOptions` | **RED** | **RED** | **RED** |
| A | PDI-21092 | `PvfsResolutionIsolationTest#pdi21092PvfsDoesNotReplaceCallerConnectionParameter` | **RED** | **RED** | **RED** |
| A | PDI-21092 | `PvfsResolutionIsolationTest#pdi21092RawUriDoesNotInheritPreviouslySelectedPvfsConnection` | **RED** | **RED** | **RED** |
| A | PDI-21035, PDI-20723 | `PvfsConnectionIsolationMinioIT#pdi21035ParallelPvfsWritesUseTheirOwnConnection` | **RED** 38-39/80 `403` | **RED** 40/80 `403` | **RED** 39/80 `403` |
| A | PDI-21092 | `PvfsConnectionIsolationMinioIT#pdi21092PvfsLookupDoesNotHijackCallerConnectionForRawS3` | **RED** | **RED** (`conn-b` -> `conn-a`, raw write `403`) | **RED** (same) |
| B | PDI-21074 | `ConnectionManagerEditIsolationTest#pdi21074CancelDiscardsUnsavedCredentialEdit` | GREEN | GREEN | **RED** |
| B | PDI-21074 | `ConnectionManagerEditIsolationTest#pdi21074SavingRenamedConnectionDoesNotCorruptOriginal` | GREEN | GREEN | **RED** |
| B | PDI-20299 | `ConnectionManagerEditIsolationTest#pdi20299CancelDiscardsUnsavedRenameFromSchemeLookup` | GREEN | GREEN | **RED** |
| C | PDI-20586 | `PvfsResolutionIsolationTest` `#pdi20586ChangedVariable...`, `#pdi20586SavedEdit...` | GREEN | GREEN | GREEN |
| C | PDI-20647 | `PvfsResolutionIsolationTest#pdi20647DeletedConnectionIsNotUsableThroughFreshLookup` | GREEN | GREEN | GREEN |
| C | PDI-20586 | `PvfsConnectionIsolationMinioIT` `#pdi20586ChangedCredentialVariable...`, `#pdi20586EditedConnection...` | GREEN | GREEN | GREEN |
| C | PDI-20647 | `PvfsConnectionIsolationMinioIT#pdi20647DeletedConnectionIsNotUsable` | GREEN | GREEN | GREEN |
| D | PDI-21015 | `LocalPurTest` (2 `pdi21015*` tests) | GREEN | **RED** 2/2 | n/a (no repo-vfs) |
| E | PDI-19753 | `ParquetPvfsS3AMultipleBucketsIT#onePvfsConnectionWritesParquetToTwoBuckets` | **RED** | **RED** (`Wrong FS`) | **RED** (`Wrong FS`) |
| F | PDI-20874 | `GoogleCloudStorageCredentialPersistenceTest` (3 tests) | **RED** 2/3 | **RED** 3/3 | **RED** 3/3 |

Findings:

- **A (PDI-21035, PDI-20723, PDI-21092) affects every supported line.** `ConnectionFileSystem.initializeVariableSpace()`
  writes `connection` into the shared space on 10.2, 11.0 and master. The
  failure rates on 10.2 and 11.0 match master, so A needs a fix and backports
  on all three lines. It is not a master-only regression.
- **B (PDI-21074, PDI-20299) is confirmed 10.2-only.** 11.0 already returns cloned details (green), so
  only [pentaho/pentaho-kettle#10782](https://github.com/pentaho/pentaho-kettle/pull/10782)
  is needed.
- **C (PDI-20586, PDI-20647) remains unreproduced on every line.** On 10.2, S3 is served by
  big-data-plugin, whose tip includes two recent S3 changes:
  - `f7274e20a`, "add cache for VFS [PDI-20801]", a large `S3CommonFileSystem`
    caching change;
  - `8573f9fff`, "s3 credentials [PDI-20852]", an optional-decrypt change for
    default-connection keys.

  The C probes stay green with both changes, and A still reproduces. Neither
  commit addresses A.
- **F (PDI-20874) is worse on 10.2 and 11.0 (3/3 red, versus 2/3 on master).** `getOpts()`
  is the same on all three lines. On 10.2 and 11.0, however, `prepare()` opens
  the raw `keyPath` without variable substitution. Master gained
  `getVar(keyPath, space)` in `3a1b83448` (BACKLOG-42701, "Project variable
  support"), which is on master only. So on 10.2 and 11.0:
  - a variable `keyPath` (for example `${GCPAccountKey}`) is never read, at
    save or at execution;
  - `ConnectionManager.save` logs the `NoSuchFileException` and saves anyway.

  From code reading (untested): a connection first saved with a literal path
  keeps that key JSON forever. This matches PDI-20874. The F backport
  therefore needs both changes: the `getVar` substitution in `prepare()`, and
  re-resolution in `getOpts()`.
- **E (PDI-19753) affects every line.** On each branch, the second bucket fails in
  `S3AFileSystem.checkPath`, reached from `PvfsHadoopBridge.getFileStatus`.
  The bridge is `common-base/.../common/pvfs` on 11.0 and
  `shims/apache/driver/.../shim/pvfs` on 10.2, with the same bucket-less
  `fsCache` key. The fix must be applied in both module layouts.
- On 10.2 and 11.0, the S3 IT runs as a JUnit 4 test under surefire. Those
  modules have no `src/it` failsafe or JUnit 5 setup, and surefire is 2.21.

Porting notes for the reproduction tests (now committed as the first commit of
each spike branch; see
[Proof-of-concept fixes and branches](#proof-of-concept-fixes-and-branches)):

- **kettle 11.0**, A + B + C tests (`pentaho-kettle.worktrees/PDI-21063-11.0`):
  - The core tests are master's, unchanged.
  - The IT is a self-contained JUnit 4 port at
    `plugins/s3-vfs/core/src/test/java/org/pentaho/amazon/s3/PvfsConnectionIsolationMinioIT.java`,
    with no `KettleS3Environment`/`TestBowl`. It uses
    `AmazonS3FileSystemBootstrap`, and `"0"` for the private
    `ACCESS_KEY_SECRET_KEY`.
  - Added test-scoped `testcontainers-minio` 2.0.5.
  - `core` and `engine` were installed from the worktree first, because the
    local 11.0 snapshot dated from May.
- **kettle 10.2**, A + B + C core tests (`pentaho-kettle.worktrees/PDI-21063-10.2`):
  - `PvfsResolutionIsolationTest` uses a 10.2 `BaseBowl`
    (`getMetastore`/`getExplicitMetastore`, `getConnectionManager()`; no
    `SharedObjectsIO`/`getManager`).
  - Run from `core/`: the root reactor fails to read `engine/pom.xml` off VPN
    (missing `cron-utils` version in the stale parent POM).
  - `kettle-core` was installed from the worktree for the plugin IT.
- **pdi-plugins-ee 10.2 / 11.0**, F / PDI-20874 (`pdi-plugins-ee.worktrees/PDI-21063-10.2`, `-11.0`):
  - Uses master's `GoogleCloudStorageCredentialPersistenceTest`, with one
    adaptation: the save helper tolerates a `KettleException` from
    `prepare()`, as these branches' `ConnectionManager.save` does.
  - Adds an explicit `assertNotNull` on the key, giving an assertion instead
    of an NPE.
  - The adapted test gives the same 1 pass / 2 fail on master.
  - Command: `mvn -o -pl google-cloud-storage-vfs/core -Dtest=GoogleCloudStorageCredentialPersistenceTest test`.
  - Logs: `PDI-21063-F-{10.2,11.0}.log`.
- **pentaho-hadoop-shims 11.0**, E / PDI-19753 (`pentaho-hadoop-shims.worktrees/PDI-21063-11.0`):
  - Master's IT, unchanged except `COMPRESSION.UNCOMPRESSED`: Pentaho's enum
    replaces `CompressionCodecName` on this branch.
  - Same command as master.
- **pentaho-hadoop-shims 10.2**, E / PDI-19753 (`pentaho-hadoop-shims.worktrees/PDI-21063-10.2`):
  - The IT is in `shims/apache/driver/src/it` (there is no `common-base` on
    10.2), with a `mock-maker-inline` resource.
  - Added test-scoped `log4j-api`, `log4j-core` and `commons-logging` to the
    driver POM: the Kettle logging init and the AWS SDK verification helper
    need them.
  - Command: `mvn -o -pl shims/apache/driver -am -DrunITs ... -Dit.test=ParquetPvfsS3AMultipleBucketsIT`.
  - Logs: `PDI-21063-E-{10.2,11.0}.log`.
- **big-data-plugin 10.2**, A + C MinIO IT (`big-data-plugin.worktrees/PDI-21063-10.2`):
  - Same JUnit 4 IT, with the 10.2 bowl, at
    `s3-vfs/src/test/java/org/pentaho/amazon/s3/PvfsConnectionIsolationMinioIT.java`.
  - Uses test-scoped `org.testcontainers:minio` 1.21.4, because the module
    compiles for Java 11.

## Proof-of-concept fixes and branches

Each spike branch has the reproduction tests as its first commit (red) and the
PoC fix as later commits (green), so `git checkout <test commit>` reproduces
and the branch tip proves the fix. All branches are on the `befc` forks.

| Repository | Line | Branch | Causes / Jira on the branch |
| --- | --- | --- | --- |
| pentaho-kettle | master | [befc/pentaho-kettle `spike/PDI-21063-master`](https://github.com/befc/pentaho-kettle/tree/spike/PDI-21063-master) | A (PDI-21035, PDI-20723, PDI-21092) test + fix; B (PDI-21074, PDI-20299), C (PDI-20586, PDI-20647), D (PDI-21015) tests; this document |
| pentaho-kettle | 11.0 | [befc/pentaho-kettle `spike/PDI-21063-11.0`](https://github.com/befc/pentaho-kettle/tree/spike/PDI-21063-11.0) | A test + fix; D (PDI-21015) test + fix; B, C tests |
| pentaho-kettle | 10.2 | [befc/pentaho-kettle `spike/PDI-21063-10.2`](https://github.com/befc/pentaho-kettle/tree/spike/PDI-21063-10.2) | A test + fix; B (PDI-21074, PDI-20299) test + fix; C tests |
| big-data-plugin | 10.2 | [befc/big-data-plugin `spike/PDI-21063-10.2`](https://github.com/befc/big-data-plugin/tree/spike/PDI-21063-10.2) | A (PDI-21035, PDI-20723, PDI-21092) and C (PDI-20586, PDI-20647) MinIO IT only; the A fix is in kettle-core 10.2 |
| pentaho-hadoop-shims | master, 11.0, 10.2 | [befc/pentaho-hadoop-shims](https://github.com/befc/pentaho-hadoop-shims/branches/all?query=spike%2FPDI-21063) `spike/PDI-21063-{master,11.0,10.2}` | E (PDI-19753) test + fix |
| pdi-plugins-ee | master, 11.0, 10.2 | [befc/pdi-plugins-ee](https://github.com/befc/pdi-plugins-ee/branches/all?query=spike%2FPDI-21063) `spike/PDI-21063-{master,11.0,10.2}` | F (PDI-20874) test + fix |

Commits (test commit -> fix commit):

| Cause | Jira | Repo | master | 11.0 | 10.2 |
| --- | --- | --- | --- | --- | --- |
| A | PDI-21035, PDI-20723, PDI-21092 | pentaho-kettle | `8548d5cc84e` -> `99b2ae9bfbf` | `e77e8cb86f0` -> `cd54cca1fac` | `1c2c118331d` -> `b7aa087c35a` |
| A | PDI-21035, PDI-20723, PDI-21092 | big-data-plugin (10.2 S3 IT) | n/a | n/a | `d6688ef35` (test only) |
| B | PDI-21074, PDI-20299 | pentaho-kettle | green, no fix needed | green, no fix needed | `1c2c118331d` -> `915ba742858` (cherry-pick of pentaho/pentaho-kettle#10782, original author kept) |
| D | PDI-21015 | pentaho-kettle | green, already fixed | `e77e8cb86f0` -> `6059096c908` | n/a (no repo-vfs) |
| E | PDI-19753 | pentaho-hadoop-shims | `9571093c14` -> `6ca62b11a9` | `e6246f847b` -> `68ecfa479a` | `8bb756972a` -> `96214c8110` |
| F | PDI-20874 | pdi-plugins-ee | `f124deb06` -> `b55ddce9b` | `aeb270115` -> `ab3676dd1` | `10298cc96` -> `bce12e620` |

Results (all with Docker/MinIO where applicable, 2026-11-24):

| Cause | Jira | Check | master | 11.0 | 10.2 |
| --- | --- | --- | --- | --- | --- |
| A | PDI-21035, PDI-20723, PDI-21092 | 3 `PvfsResolutionIsolationTest` A tests | RED 3 -> GREEN | RED 3 -> GREEN | RED 3 -> GREEN |
| A | PDI-21035, PDI-20723, PDI-21092 | `PvfsConnectionIsolationMinioIT` (5 tests, 2 A) | 5/5 GREEN (was 38-39/80 `403`) | 5/5 GREEN (was 40/80 `403`) | 5/5 GREEN in big-data-plugin, against fixed kettle-core 10.2 (was 39/80 `403`) |
| A | PDI-21035, PDI-20723, PDI-21092 | Regression: core `org.pentaho.di.connections.**` + `org.pentaho.di.core.vfs.**` | 264 run, 3 fail -> 264/264 | 203 run, 3 fail -> 203/203 | 203 run, 6 fail (3 A + 3 B) -> 204/204 (A + B fixes; #10782 adds 1 test) |
| A | PDI-21035, PDI-20723, PDI-21092 | Regression: full `plugins/s3-vfs/core` `mvn verify` | BUILD SUCCESS, 0 failures | not run | not run |
| B | PDI-21074, PDI-20299 | 3 `ConnectionManagerEditIsolationTest` tests | GREEN (already) | GREEN (already) | RED 3 -> GREEN |
| D | PDI-21015 | `LocalPurTest` (6 existing + 2 `pdi21015*`) | 8/8 (already) | RED 2 -> 8/8 | n/a |
| E | PDI-19753 | `ParquetPvfsS3AMultipleBucketsIT` | 1 error -> 0 | 1 error -> 0 | 1 error -> 0 |
| E | PDI-19753 | Regression: shim module tests | 101 run, 0 fail, 2 skip | 61 run, 0 fail, 2 skip | 39 pvfs + 22 other, 0 fail |
| F | PDI-20874 | `GoogleCloudStorageCredentialPersistenceTest` | RED 2/3 -> 3/3 | RED 3/3 -> 3/3 | RED 3/3 -> 3/3 |
| F | PDI-20874 | Regression: `google-cloud-storage-vfs/core` | 7/7 | 7/7 | 3 `GoogleCloudStorageFileObjectTest` errors, pre-existing (same without the fix; Mockito on JDK 21) |

### A PoC (PDI-21035, PDI-20723, PDI-21092)

`ConnectionFileSystem.initializeVariableSpace()` no longer writes
`connection` into the caller's (shared) `VariableSpace`. It creates a child
`Variables`, initializes it from the caller space, sets `connection` on the
child and hands the child to `details.setSpace(...)`. The method is identical
on master, 11.0 and 10.2, so the same commit cherry-picks cleanly.

Caveats / open design points:

- `initializeVariablesFrom` copies every variable (including System
  properties) per resolution. This is acceptable for proof but needs a
  performance check on hot paths (many small files, `getChildren`).
- The alternative design, passing the connection explicitly to
  `KettleVFSImpl.buildFsOptions` / `VFSHelper.getOpts` instead of the magic
  `connection` variable, is still open. It is cleaner but touches more API.
- Code that relied on the side effect (reading `connection` from the caller
  space after a PVFS lookup) would change behaviour. None found in the
  regression suites; a usage search across plugins is still needed.
- Nested/archive URIs (`zip:pvfs://...`) are not covered by the tests.

### B PoC (PDI-21074, PDI-20299), 10.2 only

Cherry-pick of open PR pentaho/pentaho-kettle#10782 ("fix: clone connection
details [PDI-21074]"): `ConnectionManager.getConnectionDetails` returns a
clone, as master and 11.0 already do. It applies cleanly on top of the A fix
and turns all 3 B tests green with no core regressions. Recommendation: merge
pentaho/pentaho-kettle#10782; the spike's B tests can be added to it as
regression tests.

### D PoC (PDI-21015), 11.0 only

Backport of master `a7e490b133f` (BACKLOG-50458): `LocalPurFileObject.refresh()`
calls `super.refresh()` so Commons VFS drops its cached child names. One line,
2 -> 0 red, existing `LocalPurTest` still green. D2 (connection list refresh,
master `af236de294b`) is not included: it needs `IMetaStore.refresh()`, which
pentaho-metastore 11.0 does not have, and it was not reproduced.

### E PoC (PDI-19753)

The `PvfsHadoopBridge` `fsCache` is keyed by `(PvfsConf, authority)`, where the
authority (bucket) comes from `pvfsConf.mapPath(path).toUri().getAuthority()`,
instead of `PvfsConf` alone. Same change in `common-base` (master/11.0) and
`shims/apache/driver` (10.2).

Caveats:

- The single "last used" `fs` field (used by `getUri()`/`getWorkingDirectory()`)
  is still order-dependent.
- Relies on `fs.s3a.impl.disable.cache=true`, otherwise Hadoop's own cache
  would still be keyed by the first bucket.
- The authority mapping for HCP, GCS and ADLS connection types is unchecked.
- Up to one cached `FileSystem` per connection and bucket for 1 hour;
  `close()` does not close the cached instances.
- The cache key already contained credentials before this change (not new,
  but worth a security review).

### F PoC (PDI-20874)

`getOpts()` resolves `getVar(keyPath, space)` and reads the current key file
(an `IOException` becomes `UncheckedIOException`); it falls back to the stored
`serviceAccountKey` only when there is no `keyPath`. `prepare()` uses `getVar`
too, which on 10.2 and 11.0 is a backport of master `3a1b83448`.

Caveats:

- A missing key file now fails the step instead of silently using the stale
  key. This is a behaviour change that product must accept.
- An unresolved variable is treated as a literal path.
- `prepare()` still persists the key JSON in the metastore.
- `getLocations()` and `test()` still use the stored `serviceAccountKey`.

### Not attempted

- C (PDI-20586, PDI-20647): no red test on any line, so there is nothing to
  prove a fix against. It stays an investigation item.
- D2 (PDI-21015 connection-list part): needs a pentaho-metastore 11.0 change
  and a reproducer first.

## Preliminary effort with Copilot assistance

These are engineering person-days, not guaranteed elapsed delivery dates.
They assume access to Maven artifacts, runnable affected builds, review and QA.
They are refined after the PoCs: the fix direction is proven for A, B, D1, E
and F, so most remaining effort is design review, hardening, backports and QA.

| Cause | Jira | Workstream | Estimate | Confidence / main uncertainty |
| --- | --- | --- | --- | --- |
| A | PDI-21035, PDI-20723, PDI-21092 | Choose design (PoC child `Variables` vs explicit options), harden on master, usage search for the `connection` variable, perf check | 1.5-2.5 days | High for the PoC design (proven on all lines); medium if explicit options are chosen |
| A | PDI-21035, PDI-20723, PDI-21092 | Backports to 11.0 and 10.2 (clean cherry-pick proven) + product QA, incl. 10.2 big-data-plugin S3 | 1.5-2 days | High; QA matrix is the main cost |
| B | PDI-21074, PDI-20299 | Merge pentaho/pentaho-kettle#10782 on 10.2 (proven) + add the 3 B tests | 0.5 day | High |
| C | PDI-20586, PDI-20647 | Carte / browse-tree / open-handle probes before any fix | 2-4 days investigation, fix not estimated | Low; not reproduced on any line, may be explained by A |
| D | PDI-21015 | D1: backport `a7e490b133f` to 11.0 (proven). D2: `IMetaStore.refresh()` in pentaho-metastore 11.0 + `af236de294b`. Re-enable QUALITY-12059 scenarios | 0.5 day D1; 1-2 days D2 | High for D1; medium for D2 (not reproduced) |
| E | PDI-19753 | Harden PoC (close cached FS, `fs` field, non-S3 authority) on master, 11.0 (`common-base`), 10.2 (`shims/apache/driver`) | 1.5-2 days | Medium-high |
| F | PDI-20874 | Product decision on missing key file; harden PoC; cover `getLocations()`/`test()`; 10.2/11.0 include the `getVar` backport | 1-2 days | Medium; compatibility with connections saved with only key JSON |

Copilot can help generate fixtures, deterministic tests and backports; it does
not replace reproducing the product paths or human review of cache semantics.
The estimate for C (PDI-20586, PDI-20647) must not be interpreted as a committed scope
or evidence that all its tickets share a fix.

## Next steps

Spike closure criteria and status:

| Criterion | Status |
| --- | --- |
| Every epic ticket mapped to a cause, with Jira IDs on every point | Done |
| Reproducing test per cause on master, 11.0, 10.2 | Done (A, B, D, E, F reproduced; C not reproduced) |
| PoC fix per reproduced cause, red -> green, on remote branches | Done (A, B, D1, E, F) |
| A (PDI-21035, PDI-20723, PDI-21092) fix design decided | **Open** - needs a team decision |
| One follow-up implementation ticket per cause, linked to PDI-21042 | **Open** |

1. A (PDI-21035, PDI-20723, PDI-21092): review the PoC with the team and decide
   child `Variables` (PoC) vs explicit options; then open the master fix PR
   and the 11.0 / 10.2 backports.
2. B (PDI-21074, PDI-20299): merge pentaho/pentaho-kettle#10782; optionally add
   the spike's `ConnectionManagerEditIsolationTest`.
3. D (PDI-21015): open the 11.0 backport of `a7e490b133f`; scope the
   pentaho-metastore `refresh()` backport for D2.
4. E (PDI-19753) and F (PDI-20874): open implementation tickets from the PoC
   branches with the caveats above; F needs a product decision on a missing
   key file.
5. C (PDI-20586, PDI-20647): probe Carte remote execution, the Spoon browse
   tree and already-open handles; re-test after the A fix lands, since A can
   mimic C.
6. All: decide which reproduction tests become permanent regression tests (the
   A MinIO IT needs Docker; the E IT uses MinIO via Testcontainers).

Working state: everything is committed and pushed to the `befc` fork
branches listed in [Proof-of-concept fixes and branches](#proof-of-concept-fixes-and-branches).
Build changes are test-scoped only: `testcontainers-minio` 2.0.5 in kettle
master and 11.0 `plugins/s3-vfs/core/pom.xml`, `minio` 1.21.4 in
big-data-plugin 10.2 `s3-vfs/pom.xml`, and logging dependencies in shims 10.2
`shims/apache/driver/pom.xml`. No upstream PRs or Jira comments have been
created by this spike.
