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

package org.pentaho.di.trans.steps.tableinput;

import junit.framework.TestCase;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;

import java.util.List;

public class TableInputMetaInjectionTest extends TestCase {

  public static String SQL = "select * from ${TABLE_NAME}";
  public static boolean LAZY_CONVERSION = true;
  public static boolean REPLACE_VARIABLES = true;
  public static boolean EXECUTE_FOR_EACH_ROW = true;
  public static String LIMIT = "10";

  public void testInjectionExtraction() throws Exception {

    // Test Strategy :
    //
    // Populate a new object, extract the metadata,
    // then inject into another set of metadata, compare the results.
    //
    TableInputMeta meta = populateTableInputMeta();

    List<StepInjectionMetaEntry> entries = meta.extractStepMetadataEntries();

    assertEquals( 5, entries.size() );

    TableInputMeta newMeta = new TableInputMeta();
    newMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

    // Automatic compare
    //
    List<StepInjectionMetaEntry> cmpEntries = newMeta.extractStepMetadataEntries();
    StepInjectionUtil.compareEntryValues( entries, cmpEntries );
  }

  public void testInjectionEntries() throws Exception {
    TableInputMeta meta = populateTableInputMeta();
    List<StepInjectionMetaEntry> entries = meta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();
    assertEquals( 5, entries.size() );

    assertNotNull( StepInjectionUtil.findEntry( entries, TableInputMetaInjection.Entry.SQL ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableInputMetaInjection.Entry.LAZY_CONVERSION ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableInputMetaInjection.Entry.REPLACE_VARIABLES ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableInputMetaInjection.Entry.EXECUTE_FOR_EACH_ROW ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TableInputMetaInjection.Entry.LIMIT ) );
  }

  private TableInputMeta populateTableInputMeta() {
    TableInputMeta meta = new TableInputMeta();

    meta.setSQL( SQL );
    meta.setLazyConversionActive( LAZY_CONVERSION );
    meta.setVariableReplacementActive( REPLACE_VARIABLES );
    meta.setExecuteEachInputRow( EXECUTE_FOR_EACH_ROW );
    meta.setRowLimit( LIMIT );

    return meta;
  }

}
