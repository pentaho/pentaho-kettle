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

package org.pentaho.di.job.entry.loadSave;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.base.LoadSaveBase;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.di.trans.steps.loadsave.initializer.JobEntryInitializer;
import org.pentaho.di.trans.steps.loadsave.validator.DatabaseMetaLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class LoadSaveTester<T extends JobEntryInterface> extends LoadSaveBase<T> {

  public LoadSaveTester( Class<T> clazz, List<String> commonAttributes,
                         List<String> xmlAttributes, List<String> repoAttributes, Map<String, String> getterMap,
                         Map<String, String> setterMap,
                         Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap,
                         Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap,
                         JobEntryInitializer<T> jobEntryInitializer ) {
    super( clazz, commonAttributes, xmlAttributes, repoAttributes, getterMap, setterMap,
      fieldLoadSaveValidatorAttributeMap, fieldLoadSaveValidatorTypeMap );
  }

  public LoadSaveTester( Class<T> clazz, List<String> commonAttributes,
                         List<String> xmlAttributes, List<String> repoAttributes, Map<String, String> getterMap,
                         Map<String, String> setterMap,
                         Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap,
                         Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap ) {
    this( clazz, commonAttributes, xmlAttributes, repoAttributes, getterMap, setterMap,
      fieldLoadSaveValidatorAttributeMap, fieldLoadSaveValidatorTypeMap, null );
  }

  public LoadSaveTester( Class<T> clazz, List<String> commonAttributes,
                         Map<String, String> getterMap, Map<String, String> setterMap,
                         Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap,
                         Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap ) {
    this( clazz, commonAttributes, Arrays.<String>asList(), Arrays.<String>asList(), getterMap, setterMap,
      fieldLoadSaveValidatorAttributeMap, fieldLoadSaveValidatorTypeMap );
  }

  public LoadSaveTester( Class<T> clazz, List<String> commonAttributes,
                         List<String> xmlAttributes, List<String> repoAttributes, Map<String, String> getterMap,
                         Map<String, String> setterMap ) {
    this( clazz, commonAttributes, xmlAttributes, repoAttributes, getterMap, setterMap,
      new HashMap<String, FieldLoadSaveValidator<?>>(), new HashMap<String, FieldLoadSaveValidator<?>>() );
  }

  public LoadSaveTester( Class<T> clazz, List<String> commonAttributes,
                         Map<String, String> getterMap, Map<String, String> setterMap ) {
    this( clazz, commonAttributes, Arrays.<String>asList(), Arrays.<String>asList(), getterMap, setterMap,
      new HashMap<String, FieldLoadSaveValidator<?>>(), new HashMap<String, FieldLoadSaveValidator<?>>() );
  }

  protected void validateLoadedMeta( List<String> attributes, Map<String, FieldLoadSaveValidator<?>> validatorMap,
      T metaSaved, T metaLoaded ) {
    super.validateLoadedMeta( attributes, validatorMap, metaSaved, metaLoaded );
    boolean checkDatabases = false;
    for ( FieldLoadSaveValidator<?> validator : validatorMap.values() ) {
      if ( validator instanceof DatabaseMetaLoadSaveValidator ) {
        checkDatabases = true;
      }
    }
    if ( checkDatabases ) {
      try {
        validateJobEntryUsesDatabaseMeta( metaSaved );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }
  }

  private void validateJobEntryUsesDatabaseMeta( T metaSaved ) throws KettleException {
    DatabaseMeta[] declaredConnections = metaSaved.getUsedDatabaseConnections();
    if ( declaredConnections == null || declaredConnections.length <= 0 ) {
      throw new KettleException( "The job entry did not report any used database connections." );
    }
    List<DatabaseMeta> declaredConnectionsList = Arrays.asList( declaredConnections );
    for ( DatabaseMeta usedDatabase : databases ) {
      if ( !declaredConnectionsList.contains( usedDatabase ) ) {
        throw new KettleException( "The job entry did not declare that a connection was used." );
      }
    }
  }

  public void testSerialization() throws KettleException {
    testXmlRoundTrip();
    testRepoRoundTrip();
    testClone();
  }

  @SuppressWarnings( "deprecation" )
  public void testXmlRoundTrip() throws KettleException {
    T metaToSave = createMeta();
    if ( initializer != null ) {
      initializer.modify( metaToSave );
    }
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
        createValidatorMapAndInvokeSetters( xmlAttributes, metaToSave );
    T metaLoaded = createMeta();
    String xml = "<step>" + metaToSave.getXML() + "</step>";
    InputStream is = new ByteArrayInputStream( xml.getBytes() );
    metaLoaded.loadXML( XMLHandler.getSubNode( XMLHandler.loadXMLFile( is, null, false, false ), "step" ), databases, null, null, null );
    validateLoadedMeta( xmlAttributes, validatorMap, metaToSave, metaLoaded );
  }

  @SuppressWarnings( "deprecation" )
  public void testRepoRoundTrip() throws KettleException {
    T metaToSave = createMeta();
    if ( initializer != null ) {
      initializer.modify( metaToSave );
    }
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
        createValidatorMapAndInvokeSetters( repoAttributes, metaToSave );
    T metaLoaded = createMeta();
    Repository rep = new MemoryRepository();
    metaToSave.saveRep( rep, null, null );
    metaLoaded.loadRep( rep, null, null, databases, null );
    validateLoadedMeta( repoAttributes, validatorMap, metaToSave, metaLoaded );
  }

  @SuppressWarnings( "deprecation" )
  protected void testClone() {
    T metaToSave = createMeta();
    if ( initializer != null ) {
      initializer.modify( metaToSave );
    }
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
        createValidatorMapAndInvokeSetters( xmlAttributes, metaToSave );

    @SuppressWarnings( "unchecked" )
    T metaLoaded = (T) metaToSave.clone();
    validateLoadedMeta( xmlAttributes, validatorMap, metaToSave, metaLoaded );
  }
}
