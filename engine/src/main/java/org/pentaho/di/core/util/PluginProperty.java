/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.util;

import java.util.prefs.Preferences;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 *
 */
public interface PluginProperty {

  /**
   * The default string value.
   */
  String DEFAULT_STRING_VALUE = "";

  /**
   * The default value.
   */
  Boolean DEFAULT_BOOLEAN_VALUE = Boolean.FALSE;

  /**
   * The default integer value.
   */
  Integer DEFAULT_INTEGER_VALUE = 0;

  /**
   * The default double value.
   */
  Double DEFAULT_DOUBLE_VALUE = 0.0;

  /**
   * The true value.
   */
  String BOOLEAN_STRING_TRUE = "Y";

  /**
   * @return true if value not null or 'false'.
   */
  boolean evaluate();

  /**
   * @param node
   *          preferences node
   */
  void saveToPreferences( final Preferences node );

  /**
   * @param node
   *          preferences node.
   */
  void readFromPreferences( final Preferences node );

  /**
   * @param builder
   *          builder to append to.
   */
  void appendXml( final StringBuilder builder );

  /**
   * @param node
   *          the node.
   */
  void loadXml( final Node node );

  /**
   * @param repository
   *          the repository.
   * @param metaStore
   *          the MetaStore
   * @param transformationId
   *          the transformationId.
   * @param stepId
   *          the stepId.
   * @throws KettleException
   *           ...
   */
  void saveToRepositoryStep( final Repository repository, final IMetaStore metaStore,
    final ObjectId transformationId, final ObjectId stepId ) throws KettleException;

  /**
   *
   * @param repository
   *          the repository.
   * @param stepId
   *          the stepId.
   * @throws KettleException
   *           ...
   */
  void readFromRepositoryStep( final Repository repository, final IMetaStore metaStore, final ObjectId stepId ) throws KettleException;
}
