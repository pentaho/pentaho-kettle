/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMeta;

/**
 * Created by gmoran on 2/25/14.
 */
public class PGBulkLoaderMetaTest {

  private StepMeta stepMeta;
  private PGBulkLoader loader;
  private PGBulkLoaderData ld;
  private PGBulkLoaderMeta lm;

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

      String masterKeys = "SCHEMA TABLE LOADACTION STOPONERROR DELIMITER ENCLOSURE PSQLPATH DBNAMEOVERRIDE MAPPINGS ";

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
      assertEquals( "PSQL path not properly injected... ", "new_PSQLPATH", lm.getPsqlpath() );
      assertEquals( "Stop on error not properly injected... ", Boolean.TRUE, lm.isStopOnError() );

      assertEquals( "Field name not properly injected... ", "new_FIELDNAME", lm.getFieldTable()[0] );
      assertEquals( "Stream name not properly injected... ", "new_STREAMNAME", lm.getFieldStream()[0] );
      assertEquals( "Date Mask not properly injected... ", "new_DATEMASK", lm.getDateMask()[0] );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

  }

}
