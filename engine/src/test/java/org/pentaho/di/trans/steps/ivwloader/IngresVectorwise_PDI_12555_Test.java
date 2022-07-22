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

package org.pentaho.di.trans.steps.ivwloader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IngresVectorwise_PDI_12555_Test {

  @Mock
  IngresVectorwiseLoader ingresVectorwiseLoaderMock;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks( this );
  }

  @Test
  public void testReplace() {
    String input = "\\\"Name\"";
    String[] from = new String[] { "\"" };
    String[] to = new String[] { "\\\"" };

    doCallRealMethod().when( ingresVectorwiseLoaderMock ).replace( anyString(), any( String[].class ),
        any( String[].class ) );

    String actual = ingresVectorwiseLoaderMock.replace( input, from, to );
    String expected = "\\\\\"Name\\\"";
    assertEquals( actual, expected );
  }

  @Test
  public void testMasqueradPassword() {
    doCallRealMethod().when( ingresVectorwiseLoaderMock ).masqueradPassword( anyString() );
    doCallRealMethod().when( ingresVectorwiseLoaderMock ).substitute( anyString(), anyString(), anyString() );

    String cmdUsingVwload = "this is the string without brackets";
    String actual = ingresVectorwiseLoaderMock.masqueradPassword( cmdUsingVwload );
    // to make sure that there is no exceptions
    assertEquals( "", actual );

    String cmdUsingSql = "/path_to_sql/sql @00.000.000.000,VW[db_user,db_pass]::db_name";
    actual = ingresVectorwiseLoaderMock.masqueradPassword( cmdUsingSql );
    String expected = "/path_to_sql/sql @00.000.000.000,VW[username,password]::db_name";
    assertEquals( expected, actual );
  }
}
