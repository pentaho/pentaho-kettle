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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;

public class SortRowsMetaInjectionTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void sortRowsMetaInjectTest() throws Exception {
    boolean[] origAscending = new boolean[] { false, true };
    boolean[] origCaseSensitive = new boolean[] { false, true };
    boolean[] origPresorted = new boolean[] { false, true };
    String[] origNames = new String[] {"field1", "field2"};

    SortRowsMeta meta = new SortRowsMeta();
    meta.setAscending( origAscending );
    meta.setCaseSensitive( origCaseSensitive );
    meta.setPreSortedField( origPresorted );
    meta.setFieldName( origNames );
    meta.setFreeMemoryLimit( "50" );

    SortRowsMetaInjection inj = meta.getStepMetaInjectionInterface();

    List<StepInjectionMetaEntry> injected = new ArrayList<StepInjectionMetaEntry>();
    inj.injectStepMetadataEntries( injected );

    assertTrue( "Empty Injection - the same values: acscending", Arrays.equals( origAscending, meta.getAscending() ) );
    assertTrue( "Empty Injection - the same values: case-sensitive", Arrays.equals( origCaseSensitive, meta.getCaseSensitive() ) );
    assertTrue( "Empty Injection - the same values: presorted", Arrays.equals( origPresorted, meta.getPreSortedField() ) );
    assertArrayEquals( "Empty Injection - the same values: field-names", origNames, meta.getFieldName() );

    String memStr = "90";
    injected.add( new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.FREE_MEMORY_TRESHOLD.toString(), memStr, ValueMetaInterface.TYPE_INTEGER, "descrition" ) );
    inj.injectStepMetadataEntries( injected );

    assertTrue( "Scalar Injection - the same values: acscending", Arrays.equals( origAscending, meta.getAscending() ) );
    assertTrue( "Scalar Injection - the same values: case-sensitive", Arrays.equals( origCaseSensitive, meta.getCaseSensitive() ) );
    assertTrue( "Scalar Injection - the same values: presorted", Arrays.equals( origPresorted, meta.getPreSortedField() ) );
    assertArrayEquals( "Scalar Injection - the same values: field-names", origNames, meta.getFieldName() );
    assertEquals( "Memory Treshold has been injected", memStr, meta.getFreeMemoryLimit() );

    injected.clear();
    String name = "injectedFieldName";
    StepInjectionMetaEntry fieldsEntry = new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.FIELDS.toString(), memStr, ValueMetaInterface.TYPE_NONE, "descrition" );
    StepInjectionMetaEntry fieldEntry = new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.FIELD.toString(), memStr, ValueMetaInterface.TYPE_NONE, "descrition" );
    StepInjectionMetaEntry nameEntry = new StepInjectionMetaEntry( SortRowsMetaInjection.Entry.NAME.toString(), name, ValueMetaInterface.TYPE_STRING, "descrition" );

    fieldsEntry.getDetails().add( fieldEntry );
    fieldEntry.getDetails().add( nameEntry );
    injected.add( fieldsEntry );
    inj.injectStepMetadataEntries( injected );

    boolean[] defBooleanArray = new boolean[] { false };
    assertTrue( "Grid Injection - new values: acscending", Arrays.equals( defBooleanArray, meta.getAscending() ) );
    assertTrue( "Grid Injection - new values: case-sensitive", Arrays.equals( defBooleanArray, meta.getCaseSensitive() ) );
    assertTrue( "Grid Injection - new values: presorted", Arrays.equals( defBooleanArray, meta.getPreSortedField() ) );
    assertArrayEquals( "Grid Injection - new values: field-names", new String[] {name}, meta.getFieldName() );
  }

}
