# PDI-21058: PUR shared-object lock ordering

This document records the PUR repository lock inversion tracked by PDI-21058, the lock-scope change, and the evidence required to validate it. It is written for someone investigating why repository reads release the repository lock before decoding and assembling metadata.

## Relationship to BISERVER-15667 and BISERVER-15820

PDI-21058 is a Kettle/PUR deadlock and is separate from the platform JCR issues. [BISERVER-15667](https://jira.pentaho.com/browse/BISERVER-15667) tracks stale cached-session version-history state, addressed by PR #6395. [BISERVER-15820](https://jira.pentaho.com/browse/BISERVER-15820) coordinates JCR version reads with content writes. Neither platform change fixes the PUR lock-order inversion described here.

## 1. The problem

PUR shared-object import and load paths use two lock domains: the repository-wide `readWriteLock` and the shared-object node lock used by `RepositorySharedObjectsIO`. A deadlock is possible when a reader holds the repository read lock while decoding or assembling an object and waits for the node lock, while an importer holds the node lock and waits for the repository write lock.

The inversion was captured on stock build 319 as well as later builds, so it is independent of the BISERVER-15667 cached-session fix.

```mermaid
sequenceDiagram
  participant Reader as PUR load/read
  participant Repo as Repository read/write lock
  participant Node as Shared-object node lock
  participant Import as PUR import

  Reader->>Repo: acquire read lock
  Import->>Node: acquire node lock
  Reader->>Node: wait while decoding/assembling
  Import->>Repo: wait for write lock
  Note over Reader,Import: Lock-order inversion; neither request can continue
```

## 2. Solution

The repository read lock now protects repository enumeration and the batched retrieval of file data and version summaries. It is released in `finally` before metadata decoding or shared-object assembly begins. The same scope rule is applied to individual and batch job/transformation loads and database-list reads, so code that can resolve shared objects does not perform that work while holding the repository read lock.

`readSharedObjects` retains the read lock for `getFilesByType`, `getDataForReadInBatch`, and `getVersionSummaryInBatch`; it assembles the returned values after the lock is released. Per-object assembly errors retain the existing log-and-skip behavior. Other failure paths still release locks through `finally` blocks.

### Second inversion: shared-object cache lock vs repository lock

A review of this change found a second inversion between the two static locks in `PurRepository`: the shared-object cache lock (`sharedObjectsLock`) and the repository lock (`readWriteLock`). It is present in `master` with the same lock order.

- Loader: `loadAndCacheSharedObjects` held the cache write lock while `readSharedObjects` waited for the repository read lock.
- Saver: `save`/`deleteDatabaseMeta` held the repository write lock and then waited for the cache write lock in `updateSharedObjectCache`.

With a cold cache, a loader and a saver block each other permanently. Two further effects of the old code: `updateSharedObjectCache` itself loaded the cache, so every saver on a cold cache could trigger the load, and a failed load cleared the cache while other threads might have been using it.

Fix:

- The cache is read from the repository without the cache lock and then published under the lock only if no invalidation happened meanwhile. A generation counter, bumped by `clearSharedObjectCache` and by cache updates on a cold cache, detects this; the loader then retries.
- `updateSharedObjectCache` never reads the repository. On a cold cache it only invalidates, because the next load reads the saved change from the repository.
- A failed load leaves the cache untouched and still throws `KettleException("Unable to read shared objects from repository")`.

Reproduction against a live server (stock 11.1 #454 with the PDI-21058 jars): a client harness ran concurrent cold-cache loads and `saveDatabaseMeta` calls. Before the fix it ended with `DEADLOCK DETECTED loads=257 saves=0`, with the loader blocked in `loadAndCacheSharedObjects` and the saver in `updateSharedObjectCache`. After the fix, a 60 s run ended with `NO DEADLOCK loads=2 saves=1434`. The low load count is expected: every save on a cold cache invalidates it, so loads are retried.

## 3. Validation

The focused PUR test suite passed on the `master` worktree:

```text
mvn -o -B -q -pl plugins/pur/core \
  -Dtest=PurRepositoryLoadLockTest test
```

The suite includes checks that the repository write lock is available during job/transformation decoding, database decoding, individual object assembly, and shared-object assembly, plus lock release after a failed shared-object update.

The nine tests in `PurRepositoryLoadLockTest` are `repositoryLoadsReleaseReadLockBeforeDeserialization`, `databaseListReadReleasesReadLockBeforeDeserialization`, `batchJobAndTransformationLoadsReleaseReadLockBeforeDeserialization`, `sharedObjectCacheLoadReleasesReadLockBeforeAssembly`, `sharedObjectLoadFailureReleasesRepositoryLocks`, and, for the cache-lock inversion, `coldCacheLoadDoesNotHoldSharedObjectsLockWhileReadingRepository`, `cacheUpdateOnColdCacheDoesNotReadRepository`, `cacheClearedDuringColdLoadIsNotPublishedStale` and `cacheUpdatedDuringColdLoadIsNotPublishedStale`. The four cache-lock tests failed before the fix and pass after it; the full `plugins/pur/core` suite passes (456 tests).

### Evidence of the original inversion

A thread dump taken on stock suite-release #319 during concurrent KTR imports (2026-10-27, JDK 17) shows both sides of the inversion in the import request threads:

- One thread is in `StreamToTransNodeConverter.convert` -> `SharedObjectUtil.moveAllSharedObjects` -> `DatabaseConnectionManager` (monitor held) -> `RepositorySharedObjectsIO.saveSharedObject` -> `PurRepository.save`.
- Another is in `convertPostRepoSave` -> `PurRepository.loadTransformation` -> `TransDelegate.dataNodeToElement` -> `AbstractMeta.getDatabases` -> `RepositorySharedObjectsIO.lock`, waiting on the shared-object lock.
- A third is in `convert` -> `AbstractMeta.setRepository` -> `getDatabases` -> `RepositorySharedObjectsIO.getSharedObjects` -> `PurRepository.getConnections` -> `getRepositoryObjects`, reading under the repository lock.

The dump contains no JVM-reported "Java-level deadlock" because the repository lock is a `ReadWriteLock`; the threads are blocked on lock acquisition instead.

### Live check with the platform harness

The patched Pentaho Server used for the BISERVER-15820 runs was stock #454 with a `pdi-pur-plugin-core` jar built from the PDI-21058 branch state at that time, plus the patched platform repository jar. It ran the 300 s, 48-thread mixed workload from `tools/repository-concurrency` with the harness stall watchdog enabled. The patched run showed no hangs or timeouts and no JCR exception lines in the server log (stock: 20). See `BISERVER-15820-JCR-VERSION-READ-WRITE-COORDINATION.md` for the full tables.

That workload is file and JCR oriented (REST and SOAP file and version operations). It does not import or load KTR/KJB files, so it does not substitute for the dedicated concurrent KTR import test below.

### Still required

A bounded live test that runs KTR imports with shared connections and repository transformation loads concurrently, checks request timeouts, and verifies imported content by read-back. Until it runs, the PUR fix is validated by unit tests and the thread-dump analysis only.

## 4. Scope and limits

This change addresses lock order within PUR loading and shared-object assembly. It does not disable repository locking or change the shared-object persistence contract. The initial PR targets `master`; release-line backports should follow only after the master change is reviewed, passes CI, and receives the live concurrency validation above.