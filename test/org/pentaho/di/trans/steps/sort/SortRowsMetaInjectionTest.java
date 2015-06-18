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

package org.pentaho.di.trans.steps.sort;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;

public class SortRowsMetaInjectionTest extends TestCase {

  public static String SORT_SIZE_ROWS = "5000000";
  public static String SORT_DIRECTORY = "${java.io.tmpdir}";
  public static String SORT_FILE_PREFIX = "sort";
  public static String FREE_MEMORY_TRESHOLD = "";
  public static boolean ONLY_PASS_UNIQUE_ROWS = true;
  public static boolean COMPRESS_TEMP_FILES = true;

  public static int NR_FIELDS = 10;
  public static String NAME = "field";
  public static boolean SORT_ASCENDING = true;
  public static boolean IGNORE_CASE = true;
  public static boolean PRESORTED = true;

  public void testInjectionExtraction() throws Exception {

    // Test Strategy :
    //
    // Populate a new SortRowsMeta object, extract the metadata,
    // then inject into another set of metadata, compare the results.
    //
    SortRowsMeta meta = populateSortRowsMeta();

    List<StepInjectionMetaEntry> entries = meta.extractStepMetadataEntries();

    assertEquals( 7, entries.size() );

    SortRowsMeta newMeta = new SortRowsMeta();
    newMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

    // Manual compare of the metadata
    //
    assertEquals( meta.getSortSize(), newMeta.getSortSize() );
    assertEquals( meta.getDirectory(), newMeta.getDirectory() );
    assertEquals( meta.getPrefix(), newMeta.getPrefix() );
    assertEquals( meta.getFreeMemoryLimit(), newMeta.getFreeMemoryLimit() );
    assertEquals( meta.isOnlyPassingUniqueRows(), newMeta.isOnlyPassingUniqueRows() );
    assertEquals( meta.getCompressFiles(), newMeta.getCompressFiles() );
    assertEquals( meta.getFieldName().length, newMeta.getFieldName().length );
    for ( int i = 0; i < NR_FIELDS; i++ ) {
      assertEquals( meta.getFieldName()[i], newMeta.getFieldName()[i] );
      assertEquals( meta.getAscending()[i], newMeta.getAscending()[i] );
      assertEquals( meta.getCaseSensitive()[i], newMeta.getCaseSensitive()[i] );
      assertEquals( meta.getPreSortedField()[i], newMeta.getPreSortedField()[i] );
    }

    // Automatic compare
    //
    List<StepInjectionMetaEntry> cmpEntries = newMeta.extractStepMetadataEntries();
    StepInjectionUtil.compareEntryValues( entries, cmpEntries );
  }

  public void testInjectionEntries() throws Exception {
    SortRowsMeta meta = populateSortRowsMeta();
    List<StepInjectionMetaEntry> entries = meta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();
    assertEquals( 7, entries.size() );
    assertNotNull( StepInjectionUtil.findEntry( entries, SortRowsMetaInjection.Entry.SORT_SIZE_ROWS ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, SortRowsMetaInjection.Entry.SORT_DIRECTORY ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, SortRowsMetaInjection.Entry.SORT_FILE_PREFIX ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, SortRowsMetaInjection.Entry.FREE_MEMORY_TRESHOLD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, SortRowsMetaInjection.Entry.ONLY_PASS_UNIQUE_ROWS ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, SortRowsMetaInjection.Entry.COMPRESS_TEMP_FILES ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.findEntry( entries, SortRowsMetaInjection.Entry.FIELDS );
    assertNotNull( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = StepInjectionUtil.findEntry( fieldsEntry.getDetails(),
      SortRowsMetaInjection.Entry.FIELD );
    assertNotNull( fieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(), SortRowsMetaInjection.Entry.NAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(), SortRowsMetaInjection.Entry.SORT_ASCENDING ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(), SortRowsMetaInjection.Entry.IGNORE_CASE ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(), SortRowsMetaInjection.Entry.PRESORTED ) );

  }

  private SortRowsMeta populateSortRowsMeta() {
    SortRowsMeta meta = new SortRowsMeta();
    meta.allocate( NR_FIELDS );
    meta.setSortSize( SORT_SIZE_ROWS );
    meta.setDirectory( SORT_DIRECTORY );
    meta.setPrefix( SORT_FILE_PREFIX );
    meta.setFreeMemoryLimit( FREE_MEMORY_TRESHOLD );
    meta.setOnlyPassingUniqueRows( ONLY_PASS_UNIQUE_ROWS );
    meta.setCompressFiles( COMPRESS_TEMP_FILES );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getFieldName()[i] = NAME + i;
      meta.getAscending()[i] = SORT_ASCENDING;
      meta.getCaseSensitive()[i] = IGNORE_CASE;
      meta.getPreSortedField()[i] = PRESORTED;
    }

    return meta;
  }

}
