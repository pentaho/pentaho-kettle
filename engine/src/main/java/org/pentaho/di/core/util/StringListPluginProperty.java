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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl</a>
 *
 */
public class StringListPluginProperty extends KeyValue<List<String>> implements PluginProperty, Iterable<String> {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 2003662016166396542L;

  /**
   * Value XML tag name.
   */
  public static final String VALUE_XML_TAG_NAME = "value";

  /**
   * The separator character.
   */
  public static final char SEPARATOR_CHAR = ',';

  /**
   * @param key
   *          key to use.
   */
  public StringListPluginProperty( final String key ) {
    super( key, new ArrayList<String>() );
  }

  /**
   * @param list
   *          list to transform, maybe null.
   * @return string, never null.
   */
  public static String asString( final List<String> list ) {
    if ( list == null ) {
      return "";
    }
    return StringUtils.join( list, SEPARATOR_CHAR );
  }

  /**
   * @param input
   *          the input.
   * @return new list, never null.
   */
  public static List<String> fromString( final String input ) {
    final List<String> result = new ArrayList<String>();
    if ( StringUtils.isBlank( input ) ) {
      return result;
    }
    for ( String value : StringUtils.split( input, SEPARATOR_CHAR ) ) {
      result.add( value );
    }
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#appendXml(java.lang.StringBuilder)
   */
  public void appendXml( final StringBuilder builder ) {
    if ( !this.evaluate() ) {
      return;
    }
    final String value = asString( this.getValue() );
    builder.append( XMLHandler.addTagValue( this.getKey(), value ) );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#evaluate()
   */
  public boolean evaluate() {
    return CollectionPredicates.NOT_NULL_OR_EMPTY_COLLECTION.evaluate( this.getValue() );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#loadXml(org.w3c.dom.Node)
   */
  public void loadXml( final Node node ) {
    final String stringValue = XMLHandler.getTagValue( node, this.getKey() );
    final List<String> values = fromString( stringValue );
    this.setValue( values );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#readFromPreferences(java.util.prefs.Preferences)
   */
  public void readFromPreferences( final Preferences node ) {
    final String stringValue = node.get( this.getKey(), asString( this.getValue() ) );
    this.setValue( fromString( stringValue ) );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#readFromRepositoryStep(org.pentaho.di.repository.Repository,
   *      long)
   */
  public void readFromRepositoryStep( final Repository repository, final IMetaStore metaStore,
    final ObjectId stepId ) throws KettleException {
    final String stringValue = repository.getStepAttributeString( stepId, this.getKey() );
    this.setValue( fromString( stringValue ) );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#saveToPreferences(java.util.prefs.Preferences)
   */
  public void saveToPreferences( final Preferences node ) {
    node.put( this.getKey(), asString( this.getValue() ) );
  }

  /**
   * {@inheritDoc}
   *
   * @see at.aschauer.commons.pentaho.plugin.PluginProperty#saveToRepositoryStep(org.pentaho.di.repository.Repository,
   *      long, long)
   */
  public void saveToRepositoryStep( final Repository repository, final IMetaStore metaStore,
    final ObjectId transformationId, final ObjectId stepId ) throws KettleException {
    final String stringValue = asString( this.getValue() );
    repository.saveStepAttribute( transformationId, stepId, this.getKey(), stringValue );
  }

  /**
   * @param values
   *          values to set, no validation.
   */
  public void setValues( final String... values ) {
    if ( this.getValue() == null ) {
      this.setValue( new ArrayList<String>() );
    }
    for ( String value : values ) {
      this.getValue().add( value );
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Iterable#iterator()
   */
  public Iterator<String> iterator() throws IllegalStateException {
    this.assertValueNotNull();
    return this.getValue().iterator();
  }

  /**
   * @return true if list is empty .
   */
  public boolean isEmpty() {
    this.assertValueNotNull();
    return this.getValue().isEmpty();
  }

  /**
   * @return size
   * @throws IllegalStateException
   *           if value is null.
   */
  public int size() throws IllegalStateException {
    this.assertValueNotNull();
    return this.getValue().size();
  }

  /**
   * Assert state, value not null.
   *
   * @throws IllegalStateException
   *           if this.value is null.
   */
  public void assertValueNotNull() throws IllegalStateException {
    if ( this.getValue() == null ) {
      throw new IllegalStateException( "Value is null" );
    }
  }

}
