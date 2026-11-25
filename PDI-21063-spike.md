# PDI-21063 - VFS caching spike

Updated: 2026-11-25. Spike [PDI-21063](https://pentaho-eng.atlassian.net/browse/PDI-21063), epic [PDI-21042](https://pentaho-eng.atlassian.net/browse/PDI-21042).

**Verdict:** the epic is not one bug. Its 10 tickets span 7 workstreams
(A-G); G is input validation, not caching.
PDI-20647 now reproduces on unmodified master through Spoon, interpreting
the ticket's ambiguous `TableInput` wording as **Text File Input**.
Other uncovered workflows remain explicitly listed below.

A PoC shows that a fix direction works; it is not a production change. Each
still needs design review, hardening, product QA and its own ticket/PR.

## Planning summary (PO/PM)

### Split the work per cause, not per symptom

The 10 epic tickets are symptoms. Several share one cause (A fixes 3 tickets,
B fixes 2), and one is not caching at all (G). Ticketing per symptom would
mean the same code changed and reviewed several times, with partial fixes in
between. Recommended split:

- **One Jira bug per root cause** (A-G, plus the new `zip:pvfs` gap), owning
  the code change, tests and backports.
- **Main Jira** = the existing ticket closest to the root cause with the
  easiest reproduction. Create a new bug only when none exists.
- Related reports can remain linked as **duplicates** where appropriate.
  Leave them open until their own reported workflow is retested on the fixed
  build.
- Add a root-cause block to the main Jira; keep the reporter's text.

Jira today (2026-11-25): all 10 tickets Open (PDI-20586 Blocked), no fix
versions, no implementation tickets.

### Candidates for the next 4 weeks

Effort = developer days with Copilot agents, including hardening, unit/IT
tests and backports; product QA is extra. Spike branches already hold the
tests and a PoC for each.

| Cause | Main Jira | Related reports | User impact | Lines | Effort | Depends on |
|---|---|---|---|---|---|---|
| [A](#a) shared PVFS context | PDI-21035 | PDI-20723, PDI-21092 | Parallel S3/PVFS steps fail or **use another connection's credentials** | all | 3-4 d | B first on 10.2 |
| [B](#b) live connection details | PDI-21074 | PDI-20299 | Cancel doesn't undo edits; rename corrupts connection | 10.2 | 0.5 d | PR pentaho/pentaho-kettle#10782 |
| [C](#c) stale Carte connections | PDI-20586 | - | Carte reruns use the **first run's credentials/variables** | all | 3-4 d | A; cheaper cache key |
| [D](#d) repository refresh | PDI-21015 | - | Repository changes invisible until restart | 11.0 | 0.5 d (+1-2 d D2) | VPN |
| [E](#e) Hadoop bridge cache key | PDI-19753 | - | Parquet to a second bucket fails (`Wrong FS`) | all | 1.5-2 d | - |
| [F](#f) GCS key materialization | PDI-20874 | - | Changed GCS key file ignored | all | 1-2 d | - |
| [G](#g) deleted connection, optional input | PDI-20647 | - | Run **succeeds with 0 rows** after connection deleted | master (+0.5-1 d per backport) | 1-2 d | Reporter's step confirmed |
| `zip:pvfs` `${VAR}` credentials | **new bug** | - (found by spike) | `403` reading zips over a connection with variable credentials | to check | not estimated | - |

Total: about 11-19 developer days for A-G (upper end includes D2 and G backports), which fits
4 weeks for one developer with QA running in parallel. Highest user risk:
A and C (a run can use another connection's or an earlier run's
credentials); cheapest: B and D (half a day each).

### Jira edit tracker

Root-cause block = cause, reproducing test, PoC direction, affected lines and
compare links from [Branches](#branches), headed "Root cause (PDI-21063 spike)".

| # | Jira | Role | Edits | Done |
|---|---|---|---|---|
| 1 | [PDI-21035](https://pentaho-eng.atlassian.net/browse/PDI-21035) | Main A | Summary and spike findings added; links to the spike comment. | [x] |
| 2 | [PDI-20723](https://pentaho-eng.atlassian.net/browse/PDI-20723) | Linked A | Kept "duplicates PDI-21035"; added comment to retest the original scenario. | [x] |
| 3 | [PDI-21092](https://pentaho-eng.atlassian.net/browse/PDI-21092) | Samples | No edit: retain existing "action item from PDI-21035". | [x] |
| 4 | [PDI-21074](https://pentaho-eng.atlassian.net/browse/PDI-21074) | Main B | Spike evidence, comparison links and PR pentaho/pentaho-kettle#10782 added. | [x] |
| 5 | [PDI-20299](https://pentaho-eng.atlassian.net/browse/PDI-20299) | Linked B | Linked as duplicate of PDI-21074; added comment to retest the original UI scenario. | [x] |
| 6 | [PDI-20586](https://pentaho-eng.atlassian.net/browse/PDI-20586) | Main C | Summary and Carte spike findings added; status remains Blocked for the owner to review. | [x] |
| 7 | [PDI-21015](https://pentaho-eng.atlassian.net/browse/PDI-21015) | Main D | Spike findings added: file refresh PoC tested; reported connection-list refresh not reproduced or ported. | [x] |
| 8 | [PDI-19753](https://pentaho-eng.atlassian.net/browse/PDI-19753) | Main E | Spike findings added; link to PDI-21035 changed to "relates to". Affects versions: 10.2, 11.0, 11.1. | [x] |
| 9 | [PDI-20874](https://pentaho-eng.atlassian.net/browse/PDI-20874) | Main F | Spike findings and branch comparisons added. | [x] |
| 9a | [PDI-20874](https://pentaho-eng.atlassian.net/browse/PDI-20874) | Main F | Added 11.0 to affects versions (tested on the 11.0 branch). | [x] |
| 10 | [PDI-20647](https://pentaho-eng.atlassian.net/browse/PDI-20647) | Main G | Added Text File Input spike findings and comparison links. | [x] |
| 10a | [PDI-20647](https://pentaho-eng.atlassian.net/browse/PDI-20647) | Main G | No additional chain test needed for the spike; Text File Input reproduces the symptom. The exact reported transformation remains unverified. | [x] |
| 11 | new bug | Main zip gap | Create in PDI-21042: "`zip:pvfs://` does not resolve connection credential variables (e.g. Unzip from S3)". Repro test in [Branches](#branches). Link "relates to PDI-21035". | [ ] |
| 12 | [PDI-21063](https://pentaho-eng.atlassian.net/browse/PDI-21063) | Spike | Posted [spike findings comment](https://pentaho-eng.atlassian.net/browse/PDI-21063?focusedCommentId=469015), including ticket mapping and comparisons. | [x] |
| 12a | [PDI-21063](https://pentaho-eng.atlassian.net/browse/PDI-21063) | Spike | No separate "relates to" links needed; tickets share the epic. | [x] |
| 13 | all | Check | Re-read links, affects versions and summaries on the existing epic tickets. | [x] |
| 13a | PDI-21074, PDI-20586, PDI-21015 | Follow-up | Added direct branch, test and PoC links. | [x] |
| 13b | [PDI-19753](https://pentaho-eng.atlassian.net/browse/PDI-19753) | Follow-up | Affects versions 10.2, 11.0 and 11.1 added. | [x] |

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
| [PDI-21015](https://pentaho-eng.atlassian.net/browse/PDI-21015) | Repository changes invisible after refresh | [D](#d) | GREEN | RED -> PoC | n/a |
| [PDI-19753](https://pentaho-eng.atlassian.net/browse/PDI-19753) | Parquet S3A to second bucket -> `Wrong FS` | [E](#e) | RED -> PoC | RED -> PoC | RED -> PoC |
| [PDI-20874](https://pentaho-eng.atlassian.net/browse/PDI-20874) | GCS key-path variable never re-resolved | [F](#f) | RED -> PoC | RED -> PoC | RED -> PoC |
| [PDI-20647](https://pentaho-eng.atlassian.net/browse/PDI-20647) | Spoon rerun succeeds after VFS connection deleted | [G](#g) | RED -> PoC | UNVERIFIED | UNVERIFIED |

Branches and compare links: [Branches](#branches). This document is on
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
- **Design options (master):**
  - **A1 child copy:** the PoC above; `connection` is written into a per-resolution copy.
  - **A2 explicit connection:** `ConnectionFileSystem` passes the name to `VFSHelper.getOpts` and resolves the
    provider URI with those options. Nothing is written, nothing is copied.

  Both pass every A test: core `PvfsResolutionIsolationTest` 3/3 and `PvfsConnectionIsolationMinioIT` 6/7 (the
  7th is the pre-existing nested gap below; stock fails 3/7). Core `connections.**` + `core.vfs.**`: 264/264
  for both.
- **Cost per PVFS lookup** (`PvfsLookupCostIT`, MinIO, file-object resolution only, no S3 request timed; JDK 21;
  median of 7x1 s after 3 s warm-up; stock and A1/A2 measured twice, stock varied by about 10%):

  | Variables in space | Stock | A1 | A2 | A1 + C PoC 1 | A2 + C PoC 1 |
  |---|---|---|---|---|---|
  | 190 (System only) | 69-76 µs | 76-78 µs | 65-66 µs | 93 µs | 81 µs |
  | 1,190 | 198-220 µs | 229-230 µs | 180-181 µs | 376 µs | 292 µs |
  | 5,190 | 683-782 µs | 813-819 µs | 623-643 µs | 1,824 µs | 1,414 µs |

  A1 adds one variable copy per resolution (8 µs at 190 variables, 139 µs at 5,190): +1-20% time, +7-11%
  allocation. A2 skips one variable scan: 5-20% faster and 1-6% less allocation than stock. Both are small next to
  an S3 request (milliseconds); A1 only matters for per-row lookups at volume (about +1 s per 100k files). The real
  cost is C's snapshot (+22-127%, see [C](#c)).
- **Who reads `connection`:** upstream master of pentaho-kettle, pdi-plugins-ee, big-data-plugin,
  pentaho-hadoop-shims, pentaho-big-data-ee, pdi-ee-plugin and pentaho-metaverse has one reader,
  `KettleVFSImpl.buildFsOptions`. That is the BACKLOG-28010 (2019) feature where a user-set `connection` variable
  selects a named connection for raw URIs; it keeps working in A1 and A2. `details.getSpace()` is read after lookup
  by ADLS Gen1/Gen2, Catalog and shims `PvfsConf`, but only for `${}` substitution: same values in A1 and A2, and no
  one resolves URIs with it. Third-party plugins were not checked.
  Compatibility: a raw `s3://` step that only worked because an earlier PVFS step leaked `connection` stops
  working in both designs. That is the PDI-21092 fix and needs a release note.
- **Nested `zip:pvfs://`:** parallel reads through two connections on one shared space are already isolated on
  stock and in A1/A2, and the caller's space stays clean. The PVFS layer under `zip:` receives the zip options,
  not the caller's space. **New gap, not an A ticket:** a connection with `${VAR}` credentials fails with `403`
  through `zip:pvfs://` on stock and in A1/A2, while plain `pvfs://` works. It needs its own ticket.
- **10.2:** `getConnectionDetails()` returns the shared cached instance ([B](#b)), so `details.setSpace()` still
  races between steps in A1 and A2. Merge B (pentaho/pentaho-kettle#10782) before A on 10.2. This is from code
  reading; not tested.
- **Recommendation:** A2. It has no hidden state, no copy, is faster, and passes the same tests. A2 risk: provider
  URIs bypass `buildFsOptions`. That matches stock whenever a connection was selected, but needs review of the
  fallback when `getOpts` returns `null`.
- **Branches:** [compare links](#branches); the `+ C` benchmark columns cherry-pick `54dc61598ab`.
- **Remaining effort:** 3-4 days (A2 hardening, master, 11.0/10.2 backports after B, QA, nested-variable ticket
  separately).

<a id="b"></a>
## B - PDI-21074, PDI-20299: live connection details (10.2 only)

- **Cause:** 10.2 `ConnectionManager.getConnectionDetails()` returns the
  cached instance, so dialogs change it before Save and Cancel cannot undo
  it. Master and 11.0 already return a clone.
- **Tests:** `ConnectionManagerEditIsolationTest` (3 tests).
- **PoC:** cherry-pick of the open PR pentaho/pentaho-kettle#10782; no core
  regressions.
- **Branches:** [compare links](#branches).
- **Open / effort:** merge pentaho/pentaho-kettle#10782, optionally with these
  tests: 0.5 day.

<a id="c"></a>
## C - PDI-20586: stale connections in a long-running Carte

- **Test:** `CarteConnectionCachingMinioIT` starts a real Carte and MinIO
  (two users, one bucket each) and runs a saved job over `GET
  /kettle/executeJob/`: Create file (job entry) -> transformation with Get
  File Names + Text file output, all on `pvfs://<conn>/${BUCKET}`.
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
  (BISERVER-15647, `472aac7c5dc`). External edit/delete probes pass on
  master/11.0 and fail before PoC 2 on 10.2. The deletion probe is related
  evidence, **not PDI-20647's reported Spoon workflow** (see [G](#g)).
- **PoC 1:** new `VariableSpaceSnapshot` (immutable copy, equality on the
  variable values) stored in the options instead of the live space.
- **PoC 2 (10.2):** `BaseHttpServlet.resetConnections()` called at the same
  Carte entry points as master's `clearBowlCache()`.
- **Probe results:** Carte IT 3/3 and `PvfsConnectionIsolationMinioIT` 5/5 on all
  lines; core regression master 264, 11.0 203, 10.2 204; 10.2 engine
  `org.pentaho.di.www` 177 (JDK 11; on JDK 21, 119 pre-existing Mockito errors).
  The pre-C Carte runs on master/11.0 already included the A PoC; they are
  not verification of completely unmodified upstream.
- **Branches:** [compare links](#branches).
- **Open:** a snapshot copies, sorts and hashes all variables on each lookup:
  +22-127% per PVFS lookup over A alone (measured in [A](#a)), worse than
  linear. Needs a cheaper key, e.g. only the variables the connection
  references, or a cached hash. The cache is still compared by hash only, so
  collisions are possible; `reset()` is not
  safe while another job is reading connections (10.2); PDI-20586's Spoon
  browse-tree cache remains untested.
- **Remaining effort:** 3-4 days (PoC 1 design + perf check on all lines,
  PoC 2 for 10.2, Carte QA with variable credentials).

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
- **Branches:** [compare links](#branches).
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
- **Branches:** [compare links](#branches).
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
- **Branches:** [compare links](#branches).
- **Open:** a missing key file now fails instead of silently using the stale
  key (product decision); `getLocations()` and `test()` still use the stored key.
- **Remaining effort:** 1-2 days.

<a id="g"></a>
## G - PDI-20647: optional input masks a deleted connection (not a cache bug)

- **Why separate from C:** first suspected to be a stale cached connection, like
  PDI-20586. The Spoon reproduction showed a different cause (input
  validation), different code (`FileInputList`) and a different fix. The
  Carte deletion probe in [C](#c) is unrelated to this ticket's workflow.

- **Reporter workflow:** QAT **11.1.0.0-59**, Spoon, save a text-reading
  transformation, run it, delete its MinIO VFS connection, rerun. No `.ktr`
  was supplied. `TableInput` is a JDBC/SQL step; **Text File Input is our
  explicit interpretation**, using the user-authorized constructed fixture.
- **Reproduction:** `SpoonConnectionDeletionMinioIT` loads
  [this reusable transformation](plugins/s3-vfs/core/src/test/resources/PDI-20647-spoon-read-delete-rerun.ktr).
  It executes real `TransGraph.start`, Spoon cache refresh, local runtime
  factory, XML metastore/connection manager, Text File Input and MinIO.
  The same open graph/metadata/manager survive run/delete/rerun. Only SWT
  hosting, save-dialog/autosave interaction and UI scheduling are mocked;
  runtime preparation and execution run directly. First run reads exactly
  `alpha`, `beta`; after verified deletion, the remote file still exists,
  but the default optional input returns **0 rows, 0 errors, no preparation
  exception**. Required-input and genuinely missing optional-file controls
  already behave correctly.
- **Cause:** `ConnectionFileSystem` returns `UndefinedConnectionFileObject`
  for a missing connection, with type `IMAGINARY` and no resolved provider
  object. `FileInputList` treats it like an absent optional file. An empty
  file list then completes successfully. **Not cached data/credentials.**
  Current master's pre-execution cache refresh does not fix this symptom.
- **PoC:** classify an imaginary connection alias with no resolved provider
  as non-accessible, independently of Required, using the existing file-input
  error path. Preserve legitimate missing optional files, connection/bucket
  roots and existing generic-exception handling. Rejected direction:
  recording all resolution exceptions does **not** fix this Spoon case,
  because undefined-connection resolution itself does not throw.
- **Evidence:** isolated upstream master `e7ec0605c722ba5574951a21bc1ac1e464b7ef87`,
  no A/C PoCs: core 6 tests / 1 expected failure and Spoon 3 / 1
  expected failure before this PoC; then core/provider 147/147, Text File
  Input 22/22 and Spoon 3/3. Repeated Spoon with `S3FileTypeIT` in one reused
  JVM: 9/9, none skipped. Combined on the spike branch with the existing
  A/Carte PoCs: core controls 12/12 and MinIO ITs 11/11.
  JDK 21.0.12, Maven 3.9.16; reactor `verify`, no shared Maven snapshot
  installation.
- **Historical build:** exact tag `11.1.0.0-59` (`03415f2eef6`) contains
  the same undefined-object/optional-file mechanism. Runtime validation is
  **blocked** by unavailable parent
  `org.pentaho:pentaho-ce-jar-parent-pom:11.1.0.0-59`; the alternative
  `repo.pentaho.com` endpoint returns HTTP 401. Source evidence is not runtime
  proof. The exact Spoon test has not run on 11.0/10.2.
- **Branches:** [compare links](#branches).
- **Manual replay:** create the named MinIO connection, upload
  `input.txt` containing `alpha\nbeta\n`, set the sample's three parameters,
  save and run, delete that connection in Spoon, rerun with Required=`N`.
  The PoC must report an initialization error, not successful zero-row input.
- **Open / effort:** accept the step interpretation; review the alias
  classification and regression-test other file-list consumers, error-ignore
  modes and live GUI execution. Already-open handles are outside this test.
  Allow 1-2 days for master hardening/product QA, plus 0.5-1 day per requested
  backport; historical artifact access can extend elapsed time.

<a id="branches"></a>
## Branches (tests and PoC fixes)

All on the `pentaho` repositories (not personal forks), branches `spike/PDI-21063-{master,11.0,10.2}`; A
designs on `spike/PDI-21063-A-design`, `-A1-child-copy`, `-A2-explicit-connection`.
**Tests** compares the upstream base with the test commit (RED); **PoC fix**
compares the test commit with the fix (GREEN). Build changes are test-scoped only.
\* Shared test commit for the line (10.2: A and B tests; 11.0: A, B and D tests).

| Cause | Main Jira | Repo | Line | Tests | PoC fix |
|---|---|---|---|---|---|
| [A](#a) | PDI-21035 | pentaho-kettle | master | [8548d5cc84e](https://github.com/pentaho/pentaho-kettle/compare/e7ec0605c72...8548d5cc84e) | [99b2ae9bfbf](https://github.com/pentaho/pentaho-kettle/compare/8548d5cc84e...99b2ae9bfbf) (A1) |
| [A design](#a) | PDI-21035 | pentaho-kettle | master | [b0e925b2170](https://github.com/pentaho/pentaho-kettle/compare/8548d5cc84e...b0e925b2170) (benchmark, nested zip) | [3987a9d3914](https://github.com/pentaho/pentaho-kettle/compare/b0e925b2170...3987a9d3914) (A1), [3b356132de2](https://github.com/pentaho/pentaho-kettle/compare/b0e925b2170...3b356132de2) (A2) |
| [A](#a) | PDI-21035 | pentaho-kettle | 11.0 | [e77e8cb86f0](https://github.com/pentaho/pentaho-kettle/compare/cd4e024b599...e77e8cb86f0) * | [cd54cca1fac](https://github.com/pentaho/pentaho-kettle/compare/e77e8cb86f0...cd54cca1fac) |
| [A](#a) | PDI-21035 | pentaho-kettle + big-data-plugin | 10.2 | [1c2c118331d](https://github.com/pentaho/pentaho-kettle/compare/a1e577acf11...1c2c118331d) *, [d6688ef35](https://github.com/pentaho/big-data-plugin/compare/f7274e20a...d6688ef35) (IT) | [b7aa087c35a](https://github.com/pentaho/pentaho-kettle/compare/1c2c118331d...b7aa087c35a) |
| [B](#b) | PDI-21074 | pentaho-kettle | 10.2 | [1c2c118331d](https://github.com/pentaho/pentaho-kettle/compare/a1e577acf11...1c2c118331d) * | [915ba742858](https://github.com/pentaho/pentaho-kettle/compare/b7aa087c35a...915ba742858) |
| [C](#c) | PDI-20586 | pentaho-kettle | master | [0876db9d896](https://github.com/pentaho/pentaho-kettle/compare/3094b39f136...0876db9d896) | [54dc61598ab](https://github.com/pentaho/pentaho-kettle/compare/0876db9d896...54dc61598ab) |
| [C](#c) | PDI-20586 | pentaho-kettle | 11.0 | [fa9e2a53e30](https://github.com/pentaho/pentaho-kettle/compare/6059096c908...fa9e2a53e30) | [b2291ce6f8c](https://github.com/pentaho/pentaho-kettle/compare/fa9e2a53e30...b2291ce6f8c) |
| [C](#c) | PDI-20586 | big-data-plugin + pentaho-kettle | 10.2 | [e5723654f](https://github.com/pentaho/big-data-plugin/compare/d6688ef35...e5723654f) | [4c65c83ef20](https://github.com/pentaho/pentaho-kettle/compare/915ba742858...4c65c83ef20) (PoC 2 + PoC 1) |
| [D](#d) | PDI-21015 | pentaho-kettle | 11.0 | [e77e8cb86f0](https://github.com/pentaho/pentaho-kettle/compare/cd4e024b599...e77e8cb86f0) * | [6059096c908](https://github.com/pentaho/pentaho-kettle/compare/cd54cca1fac...6059096c908) |
| [E](#e) | PDI-19753 | pentaho-hadoop-shims | master | [9571093c14](https://github.com/pentaho/pentaho-hadoop-shims/compare/df750a007a...9571093c14) | [6ca62b11a9](https://github.com/pentaho/pentaho-hadoop-shims/compare/9571093c14...6ca62b11a9) |
| [E](#e) | PDI-19753 | pentaho-hadoop-shims | 11.0 | [e6246f847b](https://github.com/pentaho/pentaho-hadoop-shims/compare/a383bb8408...e6246f847b) | [68ecfa479a](https://github.com/pentaho/pentaho-hadoop-shims/compare/e6246f847b...68ecfa479a) |
| [E](#e) | PDI-19753 | pentaho-hadoop-shims | 10.2 | [8bb756972a](https://github.com/pentaho/pentaho-hadoop-shims/compare/381a33596a...8bb756972a) | [96214c8110](https://github.com/pentaho/pentaho-hadoop-shims/compare/8bb756972a...96214c8110) |
| [F](#f) | PDI-20874 | pdi-plugins-ee | master | [f124deb06](https://github.com/pentaho/pdi-plugins-ee/compare/ab9e0cf81...f124deb06) | [b55ddce9b](https://github.com/pentaho/pdi-plugins-ee/compare/f124deb06...b55ddce9b) |
| [F](#f) | PDI-20874 | pdi-plugins-ee | 11.0 | [aeb270115](https://github.com/pentaho/pdi-plugins-ee/compare/9aaad602b...aeb270115) | [ab3676dd1](https://github.com/pentaho/pdi-plugins-ee/compare/aeb270115...ab3676dd1) |
| [F](#f) | PDI-20874 | pdi-plugins-ee | 10.2 | [10298cc96](https://github.com/pentaho/pdi-plugins-ee/compare/051f68059...10298cc96) | [bce12e620](https://github.com/pentaho/pdi-plugins-ee/compare/10298cc96...bce12e620) |
| [G](#g) | PDI-20647 | pentaho-kettle | master | [cdae9e3dee7](https://github.com/pentaho/pentaho-kettle/compare/0cc9ea04073...cdae9e3dee7) | [252a62638ef](https://github.com/pentaho/pentaho-kettle/compare/cdae9e3dee7...252a62638ef) |
| zip gap | new bug | pentaho-kettle | master | [b0e925b2170](https://github.com/pentaho/pentaho-kettle/compare/8548d5cc84e...b0e925b2170) | none |

## Closing the spike

- [x] Every epic ticket mapped to a workstream and covered/uncovered scenarios.
- [x] Cross-line tests for the original A-F probes; G validated on master.
- [x] PoC per reproduced cause on remote branches.
- [x] PDI-20647: reproduce Spoon run/delete/rerun on unmodified master and
      validate a PoC, under the documented Text File Input interpretation.
- [x] A design inputs (PDI-21035, PDI-20723, PDI-21092): cost, `connection`
      readers and nested `zip:pvfs://` measured; options reported.
- [x] Ticket split and 4-week candidates proposed ([Planning summary](#planning-summary-popm)).

- [ ] Jira edits ([tracker](#jira-edit-tracker)).

Design choices and fix versions belong to the implementation tickets.

## Appendix: how to run

JDK 21, Maven 3.9, Docker (`quay.io/minio/minio`). D needs VPN (`-U` on the
first run). Off VPN, add `-s central-settings.xml -nsu
-Daether.enhancedLocalRepository.trackingFilename=__none__` (Central mirror).
On kettle 10.2 run Maven from `core/`. For the plugin ITs, first
`mvn -o install -DskipTests` the branch's kettle `core`.

```sh
# A/C (PDI-21035, PDI-20723, PDI-21092 / PDI-20586) and
# B (PDI-21074, PDI-20299) - kettle core
mvn -o test -Dtest='PvfsResolutionIsolationTest,ConnectionManagerEditIsolationTest' -Dcheckstyle.skip -Djacoco.skip=true

# A/C MinIO ITs - kettle master plugins/s3-vfs/core
mvn verify -Dit.test='PvfsConnectionIsolationMinioIT,CarteConnectionCachingMinioIT' -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dcheckstyle.skip -Djacoco.skip=true
# A design (PDI-21035, PDI-20723, PDI-21092) - kettle master ROOT, per branch; label each run
mvn -B verify -nsu -DskipDefault -Pbase,plugins,highdeps -pl plugins/s3-vfs/core -am \
  -Dit.test=PvfsConnectionIsolationMinioIT,PvfsLookupCostIT -Dpdi21063.bench=true -Dpdi21063.variant=A2 \
  -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dcheckstyle.skip -Djacoco.skip=true -Dmaven.javadoc.skip=true
# results: plugins/s3-vfs/core/target/pdi21063-a-bench.tsv
# A/C MinIO ITs - kettle 11.0 plugins/s3-vfs/core, big-data-plugin 10.2 s3-vfs (JUnit 4, surefire)
# 10.2 Carte IT: first install kettle 10.2 core AND engine
mvn test -Dtest='PvfsConnectionIsolationMinioIT,CarteConnectionCachingMinioIT' -Dcheckstyle.skip -Djacoco.skip=true

# G (PDI-20647) - kettle master ROOT, reactor artifacts, no install
mvn -B verify -nsu -DskipDefault -Pbase,plugins,highdeps \
  -pl plugins/s3-vfs/core -am -Dit.test=SpoonConnectionDeletionMinioIT \
  -Dtest=FileInputListTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dcheckstyle.skip -Djacoco.skip=true -Dmaven.javadoc.skip=true
# Repetition/isolation: -Dit.test=SpoonConnectionDeletionMinioIT,S3FileTypeIT
#                     -DforkCount=1 -DreuseForks=true

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
