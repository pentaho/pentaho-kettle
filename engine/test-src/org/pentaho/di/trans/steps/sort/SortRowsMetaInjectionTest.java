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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

public class SortRowsMetaInjectionTest extends BaseMetadataInjectionTest<SortRowsMeta> {
  @Before
  public void setup() {
    setup( new SortRowsMeta() );
  }

  @Test

  public void test() throws Exception {
    check( "SORT_DIRECTORY", new StringGetter() {
      public String get() {
        return meta.getDirectory();
      }
    } );
    check( "SORT_FILE_PREFIX", new StringGetter() {
      public String get() {
        return meta.getPrefix();
      }
    } );
    check( "SORT_SIZE_ROWS", new StringGetter() {
      public String get() {
        return meta.getSortSize();
      }
    } );
    check( "FREE_MEMORY_TRESHOLD", new StringGetter() {
      public String get() {
        return meta.getFreeMemoryLimit();
      }
    } );
    check( "ONLY_PASS_UNIQUE_ROWS", new BooleanGetter() {
      public boolean get() {
        return meta.isOnlyPassingUniqueRows();
      }
    } );
    check( "COMPRESS_TEMP_FILES", new BooleanGetter() {
      public boolean get() {
        return meta.getCompressFiles();
      }
    } );
    check( "NAME", new StringGetter() {
      public String get() {
        return meta.getFieldName()[0];
      }
    } );
    check( "SORT_ASCENDING", new BooleanGetter() {
      public boolean get() {
        return meta.getAscending()[0];
      }
    } );
    check( "IGNORE_CASE", new BooleanGetter() {
      public boolean get() {
        return meta.getCaseSensitive()[0];
      }
    } );
    check( "PRESORTED", new BooleanGetter() {
      public boolean get() {
        return meta.getPreSortedField()[0];
      }
    } );

  public void sortRowsMetaInjectTest() throws Exception {
    boolean[] origAscending = new boolean[] { false, true };
    boolean[] origCaseSensitive = new boolean[] { false, true };
	boolean[] origCollator = new boolean[] { false, true };
	int[] origStrength = new int[] { 3, 0 };
    boolean[] origPresorted = new boolean[] { false, true };
    String[] origNames = new String[] {"field1", "field2"};

    SortRowsMeta meta = new SortRowsMeta();
    meta.setAscending( origAscending );
    meta.setCaseSensitive( origCaseSensitive );
	meta.setCollatorEnabled( origCollator );
	meta.setCollatorStrength( origStrength );
    meta.setPreSortedField( origPresorted );
    meta.setFieldName( origNames );
    meta.setFreeMemoryLimit( "50" );

    SortRowsMetaInjection inj = meta.getStepMetaInjectionInterface();

    List<StepInjectionMetaEntry> injected = new ArrayList<StepInjectionMetaEntry>();
    inj.injectStepMetadataEntries( injected );

    assertTrue( "Empty Injection - the same values: acscending", Arrays.equals( origAscending, meta.getAscending() ) );
    assertTrue( "Empty Injection - the same values: case-sensitive", Arrays.equals( origCaseSensitive, meta.getCaseSensitive() ) );
	assertTrue( "Empty Injection - the same values: collator-enabled", Arrays.equals( origCollator, meta.getCollatorEnabled() ) );
	assertTrue( "Empty Injection - the same values: collator-strength", Arrays.equals( origStrength, meta.getCollatorStrength() ) );
    assertTrue( "Empty Injection - the same values: presorted", Arrays.equals( origPresorted, meta.getPreSortedField() ) );
    assertArrayEquals( "Empty Injection - the same values: field-names", origNames, meta.getFieldName() );

    String memStr = "90";
    injected.add( new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.FREE_MEMORY_TRESHOLD.toString(), memStr, ValueMetaInterface.TYPE_INTEGER, "descrition" ) );
    inj.injectStepMetadataEntries( injected );

    assertTrue( "Scalar Injection - the same values: acscending", Arrays.equals( origAscending, meta.getAscending() ) );
    assertTrue( "Scalar Injection - the same values: case-sensitive", Arrays.equals( origCaseSensitive, meta.getCaseSensitive() ) );
	assertTrue( "Scalar Injection - the same values: collator-enabled", Arrays.equals( origCollator, meta.getCollatorEnabled() ) );
	assertTrue( "Scalar Injection - the same values: collator-strength", Arrays.equals( origStrength, meta.getCollatorStrength() ) );
    assertTrue( "Scalar Injection - the same values: presorted", Arrays.equals( origPresorted, meta.getPreSortedField() ) );
    assertArrayEquals( "Scalar Injection - the same values: field-names", origNames, meta.getFieldName() );
    assertEquals( "Memory Treshold has been injected", memStr, meta.getFreeMemoryLimit() );

    injected.clear();
    String name = "injectedFieldName";
	String strength = "0";
    StepInjectionMetaEntry fieldsEntry = new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.FIELDS.toString(), memStr, ValueMetaInterface.TYPE_NONE, "descrition" );
    StepInjectionMetaEntry fieldEntry = new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.FIELD.toString(), memStr, ValueMetaInterface.TYPE_NONE, "descrition" );
    StepInjectionMetaEntry nameEntry = new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.NAME.toString(), name, ValueMetaInterface.TYPE_STRING, "descrition" );
	StepInjectionMetaEntry strengthEntry = new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.COLLATOR_STRENGTH.toString(), strength, ValueMetaInterface.TYPE_STRING, "descrition" );

    fieldsEntry.getDetails().add( fieldEntry );
    fieldEntry.getDetails().add( nameEntry );
	fieldEntry.getDetails().add( strengthEntry );
    injected.add( fieldsEntry );
    inj.injectStepMetadataEntries( injected );

    boolean[] defBooleanArray = new boolean[] { false };
	int[] defIntArray = new int[] { 0 };
    assertTrue( "Grid Injection - new values: acscending", Arrays.equals( defBooleanArray, meta.getAscending() ) );
    assertTrue( "Grid Injection - new values: case-sensitive", Arrays.equals( defBooleanArray, meta.getCaseSensitive() ) );
	assertTrue( "Grid Injection - new values: collator-enabled", Arrays.equals( defBooleanArray, meta.getCollatorEnabled() ) );
	assertArrayEquals( "Grid Injection - new values: collator-strength", defIntArray, meta.getCollatorStrength() );
    assertTrue( "Grid Injection - new values: presorted", Arrays.equals( defBooleanArray, meta.getPreSortedField() ) );
    assertArrayEquals( "Grid Injection - new values: field-names", new String[] {name}, meta.getFieldName() );

  }
}
