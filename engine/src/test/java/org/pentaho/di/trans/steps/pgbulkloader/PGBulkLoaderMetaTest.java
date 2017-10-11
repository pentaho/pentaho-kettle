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

package org.pentaho.di.trans.steps.pgbulkloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
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
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.DatabaseMetaLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

/**
 * Created by gmoran on 2/25/14.
 */
public class PGBulkLoaderMetaTest {

  private StepMeta stepMeta;
  private PGBulkLoader loader;
  private PGBulkLoaderData ld;
  private PGBulkLoaderMeta lm;

  LoadSaveTester loadSaveTester;
  Class<PGBulkLoaderMeta> testMetaClass = PGBulkLoaderMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes =
        Arrays.asList( "schemaName", "tableName", "loadAction", "dbNameOverride", "delimiter",
            "enclosure", "stopOnError", "fieldTable", "fieldStream", "dateMask", "databaseMeta" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "schemaName", "getSchemaName" );
        put( "tableName", "getTableName" );
        put( "loadAction", "getLoadAction" );
        put( "dbNameOverride", "getDbNameOverride" );
        put( "delimiter", "getDelimiter" );
        put( "enclosure", "getEnclosure" );
        put( "stopOnError", "isStopOnError" );
        put( "fieldTable", "getFieldTable" );
        put( "fieldStream", "getFieldStream" );
        put( "dateMask", "getDateMask" );
        put( "databaseMeta", "getDatabaseMeta" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "schemaName", "setSchemaName" );
        put( "tableName", "setTableName" );
        put( "loadAction", "setLoadAction" );
        put( "dbNameOverride", "setDbNameOverride" );
        put( "delimiter", "setDelimiter" );
        put( "enclosure", "setEnclosure" );
        put( "stopOnError", "setStopOnError" );
        put( "fieldTable", "setFieldTable" );
        put( "fieldStream", "setFieldStream" );
        put( "dateMask", "setDateMask" );
        put( "databaseMeta", "setDatabaseMeta" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );
    FieldLoadSaveValidator<String[]> datemaskArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new DateMaskLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldTable", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldStream", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "dateMask", datemaskArrayLoadSaveValidator );
    attrValidatorMap.put( "databaseMeta", new DatabaseMetaLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    // typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator(), 1 ) );

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @Before
  public void setUp() {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "loader" );

    lm = new PGBulkLoaderMeta();
    ld = new PGBulkLoaderData();

    PluginRegistry plugReg = PluginRegistry.getInstance();

    String loaderPid = plugReg.getPluginId( StepPluginType.class, lm );

    stepMeta = new StepMeta( loaderPid, "loader", lm );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );

    loader = new PGBulkLoader( stepMeta, ld, 1, transMeta, trans );
  }

  @Test
  public void testTopLevelMetadataEntries() {

    try {
      List<StepInjectionMetaEntry> entries =
        loader.getStepMeta().getStepMetaInterface().getStepMetaInjectionInterface().getStepInjectionMetadataEntries();

      String masterKeys = "SCHEMA TABLE LOADACTION STOPONERROR DELIMITER ENCLOSURE DBNAMEOVERRIDE MAPPINGS ";

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

      String childKeys = "STREAMNAME FIELDNAME DATEMASK ";

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
            childEntry.setValue( "new_".concat( childEntry.getKey() ) );
          }

        }

      }

      loader.getStepMeta().getStepMetaInterface().getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

      assertEquals( "Schema name not properly injected... ", "new_SCHEMA", lm.getSchemaName() );
      assertEquals( "Table name not properly injected... ", "new_TABLE", lm.getTableName() );
      assertEquals( "DB Name Override not properly injected... ", "new_DBNAMEOVERRIDE", lm.getDbNameOverride() );
      assertEquals( "Delimiter not properly injected... ", "new_DELIMITER", lm.getDelimiter() );
      assertEquals( "Enclosure not properly injected... ", "new_ENCLOSURE", lm.getEnclosure() );
      assertEquals( "Load action not properly injected... ", "new_LOADACTION", lm.getLoadAction() );
      assertEquals( "Stop on error not properly injected... ", Boolean.TRUE, lm.isStopOnError() );

      assertEquals( "Field name not properly injected... ", "new_FIELDNAME", lm.getFieldTable()[0] );
      assertEquals( "Stream name not properly injected... ", "new_STREAMNAME", lm.getFieldStream()[0] );
      assertEquals( "Date Mask not properly injected... ", "new_DATEMASK", lm.getDateMask()[0] );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

  }

  public class DateMaskLoadSaveValidator implements FieldLoadSaveValidator<String> {
    Random r = new Random();
    private final String[] masks = new String[] { PGBulkLoaderMeta.DATE_MASK_PASS_THROUGH, PGBulkLoaderMeta.DATE_MASK_DATE, PGBulkLoaderMeta.DATE_MASK_DATETIME };

    @Override
    public String getTestObject() {
      int idx = r.nextInt( 3 );
      return masks[ idx ];
    }

    @Override
    public boolean validateTestObject( String test, Object actual ) {
      return test.equals( actual );
    }
  }
}
