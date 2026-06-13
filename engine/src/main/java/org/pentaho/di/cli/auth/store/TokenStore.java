/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.cli.auth.store;

import org.pentaho.di.cli.auth.BrowserAuthSessionHolder;

import java.util.Optional;

/**
 * Persistence contract for storing and loading Pan authentication credentials
 * across process invocations.
 *
 * <p>
 * Implementations must treat all credential data as sensitive. The calling
 * code ({@link BrowserAuthSessionHolder}) does NOT
 * check
 * {@link #isAvailable()} before calling the other methods — an unavailable
 * backend
 * must silently no-op on {@link #save} / {@link #delete} and return
 * {@link Optional#empty()} from {@link #load}.
 *
 * <p>
 * <b>Threading:</b> All methods may be called from the CLI main thread only;
 * no concurrency guarantee is required beyond the memory-visibility of
 * a single sequential caller.
 *
 * <p>
 * <b>Scope:</b> Each store holds exactly one credential (the most-recently
 * saved). Pan connects to one server per invocation, so per-server multiplexing
 * is not needed for v1.
 *
 * <h2>Implementations</h2>
 * <ul>
 * <li>{@link EncryptedFileTokenStore} — AES-256-GCM encrypted
 * Properties file with a key derived from {@code user.name + hostname} via
 * PBKDF2. Works in all environments (Windows, macOS, Linux, Docker, CI) with
 * zero external dependencies. Fallback when native store is unavailable.</li>
 * <li>{@link FileTokenStore} — plaintext Properties file with POSIX 600
 * permissions. Only used when explicitly configured via
 * {@code token.store.backend=file} (e.g. for debugging).</li>
 * </ul>
 *
 * @see TokenStoreFactory
 */
public interface TokenStore {

  /**
   * Persist {@code credential}, overwriting any previously saved value.
   *
   * <p>
   * Implementations must not throw on storage failure — log at error level
   * and return silently so that authentication still succeeds within the current
   * process even when persistence is unavailable.
   *
   * @param credential the credential to store; never {@code null}
   */
  void save( StoredCredential credential );

  /**
   * Load the most-recently saved credential, if any.
   *
   * <p>
   * Returns {@link Optional#empty()} when:
   * <ul>
   * <li>no credential has been saved yet</li>
   * <li>the stored credential cannot be read (corrupted, permission error)</li>
   * <li>the backend is unavailable</li>
   * </ul>
   *
   * @return stored credential, or empty
   */
  Optional<StoredCredential> load();

  /**
   * Remove any saved credential from the backing store.
   *
   * <p>
   * No-ops silently if no credential is stored or deletion fails.
   */
  void delete();

  /**
   * Returns {@code true} when this backend can be used in the current
   * environment.
   *
   * <p>
   * Called once by {@link TokenStoreFactory} at selection time; not called
   * again per operation. Implementations may perform a lightweight capability
   * check (e.g. confirm an OS tool exists) but must not block or throw.
   *
   * @return {@code true} if the store backend is operational
   */
  boolean isAvailable();
}
