/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2018 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.steps.jsoninput.JsonInputField;

import com.jayway.jsonpath.Option;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.EnumSet;

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
}
