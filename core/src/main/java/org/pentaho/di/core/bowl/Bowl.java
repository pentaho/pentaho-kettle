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

package org.pentaho.di.core.bowl;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Set;


/**
 * A Bowl is a generic container/context/workspace concept. Different plugin implementations may implement this for
 * additional features.
 * <p>
 * Bowls provide access to various "Manager" classes that in turn provide access to specific types of stored objects
 * that may be specificially grouped in the Bowl. As much as possible, Managers should generally not depend on
 * Bowl-specific features *except* common underlying storage mechanisms that are defined as APIs on Bowl itself (this
 * interface). For example, ConnectionManager does not directly depend on Bowl-specific attributes, except for the
 * MetaStore.
 * <p>
 * Specific subclasses for contexts should implement their custom logic to get a MetaStore or other underlying storage,
 * and the managers should just build on that and not have any other Bowl-specific code.
 * <p>
 * All implementations of Bowl should implement equals() and hashcode().
 * <p/>
 * If available, most code that needs a Bowl should get Bowls for Management from Spoon, and for execution from
 * TransMeta or JobMeta.
 *
 */
public interface Bowl {

  /**
   * Gets a Read-Only Metastore that handles any defaulting required for execution-time handling of metastores, for the
   * Bowl.
   *
   * @return IMetaStore A metastore for execution with the Bowl. Never null.
   */
  IMetaStore getMetastore() throws MetaStoreException;

  /**
   * Gets a Manager for some type of object specifically in the context of this Bowl.
   * <p>
   * Since constructing and initializing Managers can be expensive, and instances may share state or have other
   * limitations, callers with a Bowl should use this method in favor of directly using the manager type.
   * <p>
   * @see BowlManagerFactoryRegistry for how Managers should be registered.
   *
   * @return a manager instance, never null.
   * @throws NotFoundException if the manager type is unknown
   * @throws KettleException for other errors.
   */
  <T> T getManager( Class<T> managerClazz ) throws KettleException;

  /**
   * Parent Bowls are any Bowls that a particular Bowl inherits from.
   *
   *
   * @return Set&lt;Bowl&gt; A set of Parent Bowls. Should not be null.
   */
  Set<Bowl> getParentBowls();

  /**
   * Get the SharedObjectsIO instance that is responsible for reading/writing of
   *  Shared Objects
   * @return
   *
   */
  SharedObjectsIO getSharedObjectsIO() ;

  /**
   * Get a default variable space using this Bowl's context. Everytime you will get a new instance.
   * <p>
   * Implementations may include different sets of Variables depending on what is in context for that Bowl.
   *
   * @return a default variable space.
   */
  VariableSpace getADefaultVariableSpace();

  /**
   * Return the display name of the level. This string should be read from resource file
   * @return level name
   */
  String getLevelDisplayName();
}
