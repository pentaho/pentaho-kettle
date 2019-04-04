/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
