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
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 *
 */
public class IntegerPluginProperty extends KeyValue<Integer> implements PluginProperty {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -2990345692552430357L;

  /**
   * Constructor. Value is null.
   *
   * @param key
   *          key to set.
   * @throws IllegalArgumentException
   *           if key is invalid.
   */
  public IntegerPluginProperty( final String key ) throws IllegalArgumentException {
    super( key, DEFAULT_INTEGER_VALUE );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#evaluate()
   */
  public boolean evaluate() {
    final Integer value = this.getValue();
    return value != null && value != 0;
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#appendXml(java.lang.StringBuilder)
   */
  public void appendXml( final StringBuilder builder ) {
    builder.append( XMLHandler.addTagValue( this.getKey(), this.getValue() ) );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#loadXml(org.w3c.dom.Node)
   */
  public void loadXml( final Node node ) {
    final Integer value = Integer.parseInt( XMLHandler.getTagValue( node, this.getKey() ) );
    this.setValue( value );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#readFromRepositoryStep(org.pentaho.di.repository.Repository,
   *      long)
   */
  public void readFromRepositoryStep( final Repository repository, final IMetaStore metaStore,
    final ObjectId stepId ) throws KettleException {
    final Long longValue = repository.getStepAttributeInteger( stepId, this.getKey() );
    this.setValue( longValue.intValue() );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#saveToPreferences(java.util.prefs.Preferences)
   */
  public void saveToPreferences( final Preferences node ) {
    node.putInt( this.getKey(), this.getValue() );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#readFromPreferences(java.util.prefs.Preferences)
   */
  public void readFromPreferences( final Preferences node ) {
    this.setValue( node.getInt( this.getKey(), this.getValue() ) );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#saveToRepositoryStep(org.pentaho.di.repository.Repository,
   *      long, long)
   */
  public void saveToRepositoryStep( final Repository repository, final IMetaStore metaStore,
    final ObjectId transformationId, final ObjectId stepId ) throws KettleException {
    repository.saveStepAttribute( transformationId, stepId, this.getKey(), this.getValue() );
  }

}
