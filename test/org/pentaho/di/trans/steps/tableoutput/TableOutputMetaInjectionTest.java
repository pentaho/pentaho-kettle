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

package org.pentaho.di.trans.steps.tableoutput;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;

public class TableOutputMetaInjectionTest extends TestCase {

  public static String SCHEMA_NAME = "schema";
  public static String TABLE_NAME = "${TABLE_NAME}";
  public static String COMMIT_SIZE = "1000";
  public static boolean TRUNCATE_TABLE = true;
  public static boolean SPECIFY_DATABASE_FIELDS = true;
  public static boolean IGNORE_INSERT_ERRORS = true;
  public static boolean USE_BATCH_UPDATE = true;

  public static boolean PARTITION_OVER_TABLES = true;
  public static String PARTITIONING_FIELD = "partitioningField";
  public static boolean PARTITION_PER_MONTH = true;
  public static boolean PARTITION_PER_DAY = false;

  public static boolean TABLE_NAME_DEFINED_IN_FIELD = true;
  public static String TABLE_NAME_FIELD = "tableField";
  public static boolean STORE_TABLE_NAME = true;

  public static boolean RETURN_AUTO_GENERATED_KEY = true;
  public static String AUTO_GENERATED_KEY_FIELD = "keyField";

  public static int NR_FIELDS = 10;

  public static String DATABASE_FIELDNAME = "dbField";
  public static String STREAM_FIELDNAME = "inField";

  public void testInjectionExtraction() throws Exception {

    // Test Strategy :
    //
    // Populate a new object, extract the metadata,
    // then inject into another set of metadata, compare the results.
    //
    TableOutputMeta meta = populateTableOutputMeta();

    List<StepInjectionMetaEntry> entries = meta.extractStepMetadataEntries();

    assertEquals( 16, entries.size() );

    TableOutputMeta newMeta = new TableOutputMeta();
    newMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

    // Automatic compare
    //
    List<StepInjectionMetaEntry> cmpEntries = newMeta.extractStepMetadataEntries();
    StepInjectionUtil.compareEntryValues( entries, cmpEntries );
  }

  public void testInjectionEntries() throws Exception {
    TableOutputMeta meta = populateTableOutputMeta();
    List<StepInjectionMetaEntry> entries = meta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();
    assertEquals( 16, entries.size() );

    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.TARGET_SCHEMA ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.TARGET_TABLE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.COMMIT_SIZE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.TRUNCATE_TABLE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.SPECIFY_DATABASE_FIELDS ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.IGNORE_INSERT_ERRORS ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.USE_BATCH_UPDATE ) );

    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.PARTITION_OVER_TABLES ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.PARTITIONING_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.PARTITION_DATA_PER ) );

    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.TABLE_NAME_DEFINED_IN_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.TABLE_NAME_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.STORE_TABLE_NAME ) );

    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.RETURN_AUTO_GENERATED_KEY ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableOutputMetaInjection.Entry.AUTO_GENERATED_KEY_FIELD ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.findEntry( entries,
      TableOutputMetaInjection.Entry.DATABASE_FIELDS );
    assertNotNull( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = StepInjectionUtil.findEntry( fieldsEntry.getDetails(),
      TableOutputMetaInjection.Entry.DATABASE_FIELD );
    assertNotNull( fieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TableOutputMetaInjection.Entry.DATABASE_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TableOutputMetaInjection.Entry.STREAM_FIELDNAME ) );
  }

  private TableOutputMeta populateTableOutputMeta() {
    TableOutputMeta meta = new TableOutputMeta();
    meta.allocate( NR_FIELDS );

    meta.setSchemaName( SCHEMA_NAME );
    meta.setTableName( TABLE_NAME );
    meta.setCommitSize( COMMIT_SIZE );
    meta.setTruncateTable( TRUNCATE_TABLE );
    meta.setIgnoreErrors( IGNORE_INSERT_ERRORS );
    meta.setUseBatchUpdate( USE_BATCH_UPDATE );
    meta.setSpecifyFields( SPECIFY_DATABASE_FIELDS );

    meta.setPartitioningEnabled( PARTITION_OVER_TABLES );
    meta.setPartitioningField( PARTITIONING_FIELD );
    meta.setPartitioningDaily( PARTITION_PER_DAY );
    meta.setPartitioningMonthly( PARTITION_PER_MONTH );

    meta.setTableNameInField( TABLE_NAME_DEFINED_IN_FIELD );
    meta.setTableNameField( TABLE_NAME_FIELD );
    meta.setTableNameInTable( STORE_TABLE_NAME );

    meta.setReturningGeneratedKeys( RETURN_AUTO_GENERATED_KEY );
    meta.setGeneratedKeyField( AUTO_GENERATED_KEY_FIELD );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getFieldDatabase()[i] = DATABASE_FIELDNAME + i;
      meta.getFieldStream()[i] = STREAM_FIELDNAME + i;
    }

    return meta;
  }

}
