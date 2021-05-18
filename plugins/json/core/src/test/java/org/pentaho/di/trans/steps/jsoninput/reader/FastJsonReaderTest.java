/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput.reader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.steps.jsoninput.JsonInputField;

import com.jayway.jsonpath.Option;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class FastJsonReaderTest {
  private static final Option[] DEFAULT_OPTIONS = { Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL };
  private static final Option[] OPTIONS_WO_DEFAULT_PATH_LEAF_TO_NULL = { Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST };
  private EnumSet<Option> expectedOptions = EnumSet.noneOf( Option.class );
  private JsonInputField[] fields;

  private FastJsonReader fJsonReader;
  private LogChannelInterface logMock = mock( LogChannelInterface.class );

  @Before
  public void setUp() throws Exception {
    fields = new JsonInputField[] {};
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testFastJsonReaderCreated_Default() throws KettleException {
    fJsonReader = new FastJsonReader( logMock );
    expectedOptions.addAll( Arrays.asList( DEFAULT_OPTIONS ) );
    assertNotNull( fJsonReader );
    assertEquals( false, fJsonReader.isIgnoreMissingPath() );
    assertEquals( true, fJsonReader.isDefaultPathLeafToNull() );
    assertEquals( expectedOptions, fJsonReader.getJsonConfiguration().getOptions() );
  }

  @Test
  public void testFastJsonReaderCreated_WithInputFields() throws KettleException {
    expectedOptions.addAll( Arrays.asList( DEFAULT_OPTIONS ) );
    fJsonReader = new FastJsonReader( fields, logMock );
    assertNotNull( fJsonReader );
    assertEquals( false, fJsonReader.isIgnoreMissingPath() );
    assertEquals( true, fJsonReader.isDefaultPathLeafToNull() );
    assertEquals( expectedOptions, fJsonReader.getJsonConfiguration().getOptions() );
  }

  @Test
  public void testFastJsonReaderCreated_WithDefaultPathLeafToNullFalse() throws KettleException {
    expectedOptions.addAll( Arrays.asList( OPTIONS_WO_DEFAULT_PATH_LEAF_TO_NULL ) );
    fJsonReader = new FastJsonReader( fields, false, logMock );
    assertNotNull( fJsonReader );
    assertEquals( false, fJsonReader.isIgnoreMissingPath() );
    assertEquals( false, fJsonReader.isDefaultPathLeafToNull() );
    assertEquals( expectedOptions, fJsonReader.getJsonConfiguration().getOptions() );
  }

  @Test
  public void testFastJsonReaderCreated_WithDefaultPathLeafToNullTrue() throws KettleException {
    expectedOptions.addAll( Arrays.asList( DEFAULT_OPTIONS ) );
    fJsonReader = new FastJsonReader( fields, true, logMock );
    assertNotNull( fJsonReader );
    assertEquals( false, fJsonReader.isIgnoreMissingPath() );
    assertEquals( true, fJsonReader.isDefaultPathLeafToNull() );
    assertEquals( expectedOptions, fJsonReader.getJsonConfiguration().getOptions() );
  }

  @Test
  public void testFastJsonReaderGetMaxRowSize() throws KettleException {
    List<List<Integer>> mainList = new ArrayList<>();
    List<Integer> l1 = new ArrayList<>();
    List<Integer> l2 = new ArrayList<>();
    List<Integer> l3 = new ArrayList<>();
    l1.add( 1 );
    l2.add( 1 );
    l2.add( 2 );
    l3.add( 1 );
    l3.add( 2 );
    l3.add( 3 );
    mainList.add( l1 );
    mainList.add( l2 );
    mainList.add( l3 );
    assertEquals( 3, FastJsonReader.getMaxRowSize( Collections.singletonList( mainList ) ) );
  }
}
