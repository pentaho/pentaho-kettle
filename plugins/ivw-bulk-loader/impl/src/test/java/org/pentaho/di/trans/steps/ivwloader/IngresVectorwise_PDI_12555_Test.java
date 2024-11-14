/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.ivwloader;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
