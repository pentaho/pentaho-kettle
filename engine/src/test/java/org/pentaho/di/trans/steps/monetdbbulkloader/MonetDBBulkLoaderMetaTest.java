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
package org.pentaho.di.trans.steps.monetdbbulkloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;


/**
 * Created by gmoran on 2/25/14.
 */
public class MonetDBBulkLoaderMetaTest implements InitializerInterface<StepMetaInterface> {

  private StepMeta stepMeta;
  private MonetDBBulkLoader loader;
  private MonetDBBulkLoaderData ld;
  private MonetDBBulkLoaderMeta lm;
  LoadSaveTester loadSaveTester;
  Class<MonetDBBulkLoaderMeta> testMetaClass = MonetDBBulkLoaderMeta.class;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "loader" );

    lm = new MonetDBBulkLoaderMeta();
    ld = new MonetDBBulkLoaderData();

    PluginRegistry plugReg = PluginRegistry.getInstance();

    String loaderPid = plugReg.getPluginId( StepPluginType.class, lm );

    stepMeta = new StepMeta( loaderPid, "loader", lm );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );

    loader = new MonetDBBulkLoader( stepMeta, ld, 1, transMeta, trans );

    List<String> attributes =
        Arrays.asList( "dbConnectionName", "schemaName", "tableName", "logFile", "fieldSeparator", "fieldEnclosure",
            "NULLrepresentation", "encoding", "truncate", "fullyQuoteSQL", "fieldTable", "fieldStream",
            "fieldFormatOk" );

    // Important note - the "databaseMeta" is not tested here as it's tied to the dbConnectionName. Since the loader
    // has assymetry here, we have to not test the databaseMeta, or we have to do surgery on the MonetDBBulkLoaderMeta
    // so that it's symmetric (and has no dependent variables like this).
    // MB -5/2016

    // Note - autoSchema is not persisted and neither is autoStringWidths. (Old functionality).

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldTable", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldStream", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldFormatOk",
        new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 5 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            new HashMap<String, String>(), new HashMap<String, String>(), attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof MonetDBBulkLoaderMeta ) {
      ( (MonetDBBulkLoaderMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  @Ignore( "Fails intermittently.  Test introduced without new code so unclear what this is testing." )
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testTopLevelMetadataEntries() {

    try {
      List<StepInjectionMetaEntry> entries =
          loader.getStepMeta().getStepMetaInterface().getStepMetaInjectionInterface().getStepInjectionMetadataEntries();

      String masterKeys = "SCHEMA TABLE LOGFILE FIELD_SEPARATOR FIELD_ENCLOSURE NULL_REPRESENTATION ENCODING TRUNCATE "
          + "FULLY_QUOTE_SQL BUFFER_SIZE MAPPINGS ";

      for ( StepInjectionMetaEntry entry : entries ) {
        String key = entry.getKey();
        assertTrue( masterKeys.contains( key ) );
        masterKeys = masterKeys.replace( key, "" );

      }

      assertTrue( masterKeys.trim().length() == 0 );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

  }

  @Test
  public void testChildLevelMetadataEntries() {

    try {
      List<StepInjectionMetaEntry> entries =
          loader.getStepMeta().getStepMetaInterface().getStepMetaInjectionInterface().getStepInjectionMetadataEntries();

      String childKeys = "STREAMNAME FIELDNAME FIELD_FORMAT_OK ";

      StepInjectionMetaEntry mappingEntry = null;

      for ( StepInjectionMetaEntry entry : entries ) {
        String key = entry.getKey();
        if ( key.equals( "MAPPINGS" ) ) {
          mappingEntry = entry;
          break;
        }
      }

      assertNotNull( mappingEntry );

      List<StepInjectionMetaEntry> fieldAttributes = mappingEntry.getDetails().get( 0 ).getDetails();

      for ( StepInjectionMetaEntry attribute : fieldAttributes ) {
        String key = attribute.getKey();
        assertTrue( childKeys.contains( key ) );
        childKeys = childKeys.replace( key, "" );

      }

      assertTrue( childKeys.trim().length() == 0 );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

  }

  // Note - the cloneTest() was removed since it's being covered by the load/save tester now.

  @Test
  public void testInjection() {

    try {
      List<StepInjectionMetaEntry> entries =
          loader.getStepMeta().getStepMetaInterface().getStepMetaInjectionInterface().getStepInjectionMetadataEntries();

      for ( StepInjectionMetaEntry entry : entries ) {
        entry.setValueType( lm.findAttribute( entry.getKey() ).getType() );
        switch ( entry.getValueType() ) {
          case ValueMetaInterface.TYPE_STRING:
            entry.setValue( "new_".concat( entry.getKey() ) );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            entry.setValue( Boolean.TRUE );
            break;
          default:
            break;
        }

        if ( !entry.getDetails().isEmpty() ) {

          List<StepInjectionMetaEntry> childEntries = entry.getDetails().get( 0 ).getDetails();
          for ( StepInjectionMetaEntry childEntry : childEntries ) {
            switch ( childEntry.getValueType() ) {
              case ValueMetaInterface.TYPE_STRING:
                childEntry.setValue( "new_".concat( childEntry.getKey() ) );
                break;
              case ValueMetaInterface.TYPE_BOOLEAN:
                childEntry.setValue( Boolean.TRUE );
                break;
              default:
                break;
            }
          }

        }

      }

      loader.getStepMeta().getStepMetaInterface().getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

      assertEquals( "Schema name not properly injected... ", "new_SCHEMA", lm.getSchemaName() );
      assertEquals( "Table name not properly injected... ", "new_TABLE", lm.getTableName() );
      assertEquals( "Logfile not properly injected... ", "new_LOGFILE", lm.getLogFile() );
      assertEquals( "Field separator not properly injected... ", "new_FIELD_SEPARATOR", lm.getFieldSeparator() );
      assertEquals( "Field enclosure not properly injected... ", "new_FIELD_ENCLOSURE", lm.getFieldEnclosure() );
      assertEquals( "Null representation not properly injected... ", "new_NULL_REPRESENTATION", lm.getNULLrepresentation() );
      assertEquals( "Encoding path not properly injected... ", "new_ENCODING", lm.getEncoding() );
      assertEquals( "Buffer size not properly injected... ", "new_BUFFER_SIZE", lm.getBufferSize() );
      assertEquals( "Truncate not properly injected... ", Boolean.TRUE, lm.isTruncate() );
      assertEquals( "Fully Quote SQL not properly injected... ", Boolean.TRUE, lm.isFullyQuoteSQL() );

      assertEquals( "Field name not properly injected... ", "new_FIELDNAME", lm.getFieldTable()[0] );
      assertEquals( "Stream name not properly injected... ", "new_STREAMNAME", lm.getFieldStream()[0] );
      assertEquals( "Field Format not properly injected... ", Boolean.TRUE, lm.getFieldFormatOk()[0] );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

  }

}
