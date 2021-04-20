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

import com.jayway.jsonpath.Option;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.steps.jsoninput.JsonInput;
import org.pentaho.di.trans.steps.jsoninput.JsonInputField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class FastJsonReaderTest {
  private static final Option[] OPTIONS_WITH_DEFAULT_PATH_LEAF_TO_NULL =
    { Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL };
  private static final Option[] OPTIONS_WITHOUT_DEFAULT_PATH_LEAF_TO_NULL =
    { Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST };
  private final LogChannelInterface logMock = mock( LogChannelInterface.class );

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testFastJsonReaderConstructor_DefaultPathLeafToNull_IgnoreMissingPath() throws KettleException {
    testFastJsonReaderConstructor_Base( mock( JsonInput.class ), new JsonInputField[ 0 ], true, true );
  }

  @Test
  public void testFastJsonReaderConstructor_DefaultPathLeafToNull_NoIgnoreMissingPath() throws KettleException {
    testFastJsonReaderConstructor_Base( mock( JsonInput.class ), new JsonInputField[ 0 ], true, false );
  }

  @Test
  public void testFastJsonReaderConstructor_NoDefaultPathLeafToNull_IgnoreMissingPath() throws KettleException {
    testFastJsonReaderConstructor_Base( mock( JsonInput.class ), new JsonInputField[ 0 ], false, true );
  }

  @Test
  public void testFastJsonReaderConstructor_NoDefaultPathLeafToNull_NoIgnoreMissingPath() throws KettleException {
    testFastJsonReaderConstructor_Base( mock( JsonInput.class ), new JsonInputField[ 0 ], false, false );
  }

  @Test
  public void testFastJsonReaderConstructor_Null() throws KettleException {
    testFastJsonReaderConstructor_Base( mock( JsonInput.class ), new JsonInputField[ 0 ], false, false );
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

  /**
   * <p>Base method for the 'testFastJsonReaderConstructor_*' tests.</p>
   * <p>Note that this method immediately asserts the value of the following fields:</p>
   * <p>
   *   <ul>
   *     <li>ignoreMissingPath ({@link FastJsonReader#isIgnoreMissingPath()})</li>
   *     <li>defaultPathLeafToNull ({@link FastJsonReader#isDefaultPathLeafToNull()})</li>
   *     <li>Initial Json configuration ({@link FastJsonReader#getJsonConfiguration()})</li>
   *   </ul>
   * </p>
   *
   * @return the newly created instance
   * @throws KettleException if something went wrong
   */
  private FastJsonReader testFastJsonReaderConstructor_Base( JsonInput step, JsonInputField[] inputFields,
                                                             boolean defaultPathLeafToNull,
                                                             boolean ignoreMissingPath ) throws KettleException {

    FastJsonReader reader = new FastJsonReader( step, inputFields, defaultPathLeafToNull, ignoreMissingPath, logMock );

    assertNotNull( reader );
    assertEquals( defaultPathLeafToNull, reader.isDefaultPathLeafToNull() );
    assertEquals( ignoreMissingPath, reader.isIgnoreMissingPath() );

    Option[] options = OPTIONS_WITH_DEFAULT_PATH_LEAF_TO_NULL;
    if ( !defaultPathLeafToNull ) {
      options = OPTIONS_WITHOUT_DEFAULT_PATH_LEAF_TO_NULL;
    }
    EnumSet<Option> expectedOptions = EnumSet.noneOf( Option.class );
    expectedOptions.addAll( Arrays.asList( options ) );
    assertEquals( expectedOptions, reader.getJsonConfiguration().getOptions() );

    return reader;
  }
}
