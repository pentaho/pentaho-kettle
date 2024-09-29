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

package org.pentaho.di.job.entry.loadSave;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
@Ignore
public abstract class JobEntryLoadSaveTestSupport<T extends JobEntryInterface> {

  private LoadSaveTester<T> tester;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Before
  public void setUp() throws Exception {
    List<String> commonAttributes = listCommonAttributes();
    List<String> xmlAttributes = listXmlAttributes();
    List<String> repoAttributes = listRepositoryAttributes();
    Map<String, String> getters = createGettersMap();
    Map<String, String> setters = createSettersMap();
    Map<String, FieldLoadSaveValidator<?>> attributeValidators = createAttributeValidatorsMap();
    Map<String, FieldLoadSaveValidator<?>> typeValidators = createTypeValidatorsMap();

    assertTrue( !commonAttributes.isEmpty() || !( xmlAttributes.isEmpty() || repoAttributes.isEmpty() ) );

    tester = new LoadSaveTester<T>( getJobEntryClass(), commonAttributes, xmlAttributes, repoAttributes, getters, setters,
      attributeValidators, typeValidators );
  }

  @Test
  public void testSerialization() throws KettleException {
    tester.testSerialization();
  }

  protected abstract Class<T> getJobEntryClass();

  protected abstract List<String> listCommonAttributes();


  protected List<String> listXmlAttributes() {
    return Collections.emptyList();
  }

  protected List<String> listRepositoryAttributes() {
    return Collections.emptyList();
  }

  protected Map<String, String> createGettersMap() {
    return Collections.emptyMap();
  }

  protected Map<String, String> createSettersMap() {
    return Collections.emptyMap();
  }

  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    return Collections.emptyMap();
  }

  protected Map<String, FieldLoadSaveValidator<?>> createTypeValidatorsMap() {
    return Collections.emptyMap();
  }


  @SuppressWarnings( "unchecked" )
  protected static <T1, T2> Map<T1, T2> toMap( Object... pairs ) {
    Map<T1, T2> result = new HashMap<T1, T2>( pairs.length );
    for ( int i = 0; i < pairs.length; i += 2 ) {
      T1 key = (T1) pairs[ i ];
      T2 value = (T2) pairs[ i + 1 ];
      result.put( key, value );
    }
    return result;
  }
}
