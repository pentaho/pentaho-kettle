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

## 3. Validation

The focused PUR test suite passed on the `master` worktree:

```text
mvn -o -B -q -pl plugins/pur/core \
  -Dtest=PurRepositoryLoadLockTest test
```

The suite includes checks that the repository write lock is available during job/transformation decoding, database decoding, individual object assembly, and shared-object assembly, plus lock release after a failed shared-object update.

The five tests in `PurRepositoryLoadLockTest` are `repositoryLoadsReleaseReadLockBeforeDeserialization`, `databaseListReadReleasesReadLockBeforeDeserialization`, `batchJobAndTransformationLoadsReleaseReadLockBeforeDeserialization`, `sharedObjectCacheLoadReleasesReadLockBeforeAssembly` and `sharedObjectLoadFailureReleasesRepositoryLocks`.

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