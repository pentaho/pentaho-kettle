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

package org.pentaho.di.trans.streaming.common;

import com.google.common.base.Throwables;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;

@InjectionSupported ( localizationPrefix = "StreamingFileInput.Injection." )
public abstract class BaseStreamStepMeta extends StepWithMappingMeta implements StepMetaInterface {


  @Injection ( name = "TRANSFORMATION_PATH" )  // pull this stuff up to common
  protected String transformationPath;

  @Injection ( name = "NUM_MESSAGES" )
  protected String batchSize;

  @Injection ( name = "DURATION" )
  protected String batchDuration;

  @Override public String getXML() {
    StringBuilder builder = new StringBuilder();
    getFieldToNameStream()
      // create an xml fragment for each field, using the injection annotation name as the element name
      .forEach( entry -> builder.append( "    " )
        .append( XMLHandler.addTagValue(
          entry.getValue(), fieldVal( entry.getKey() ) ) ) );
    return builder.toString();
  }

  @Override public void loadXML(
    Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) {

    getFieldToNameStream()
      .forEach( entry -> setTagValue( stepnode, entry.getKey(), entry.getValue() ) );
  }

  private void setTagValue( Node stepnode, Field field, String tagname ) {
    try {
      field.set( this, XMLHandler.getTagValue( stepnode, tagname ) );
    } catch ( IllegalAccessException e ) {
      Throwables.propagate( e );
    }
  }

  private Stream<Map.Entry<Field, String>> getFieldToNameStream() {
    return Stream.concat( Arrays.stream( getClass().getDeclaredFields() ), Arrays.stream( getClass().getSuperclass().getDeclaredFields() ) )
      // get this class' fields, map them to injection annotations.
      .collect( Collectors.toMap( Function.identity(), field -> field.getAnnotationsByType( Injection.class ) ) )
      .entrySet().stream()
      // filter out fields that don't have an injection annotation
      .filter( entry -> entry.getValue().length > 0 )
      // extract out the name as specified in the injection
      .map( entry -> immutableEntry( entry.getKey(), entry.getValue()[ 0 ].name() ) );
  }


  private String fieldVal( Field field ) {
    try {
      return field.get( this ).toString();
    } catch ( IllegalAccessException e ) {
      Throwables.propagate( e );
    }
    return "";
  }


  public void setTransformationPath( String transformationPath ) {
    this.transformationPath = transformationPath;
  }

  public void setBatchSize( String batchSize ) {
    this.batchSize = batchSize;
  }

  public void setBatchDuration( String batchDuration ) {
    this.batchDuration = batchDuration;
  }

  @Override public void setDefault() {
    batchSize = "1000";
    batchDuration = "1000";
  }

  public String getTransformationPath() {
    return transformationPath;
  }

  public String getBatchSize() {
    return batchSize;
  }

  public String getBatchDuration() {
    return batchDuration;
  }
}
