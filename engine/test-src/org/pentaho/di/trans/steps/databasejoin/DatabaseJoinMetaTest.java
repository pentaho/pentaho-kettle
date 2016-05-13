/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.databasejoin;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class DatabaseJoinMetaTest {

  @Test
  public void cloneTest() throws Exception {
    DatabaseJoinMeta meta = new DatabaseJoinMeta();
    meta.allocate( 2 );
    meta.setParameterType( new int[] { 10, 50 } );
    meta.setParameterField( new String[] { "ee", "ff" } );
    meta.setSql( "SELECT * FROM FOO AS BAR" );
    DatabaseJoinMeta aClone = (DatabaseJoinMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getParameterField(), aClone.getParameterField() ) );
    assertTrue( Arrays.equals( meta.getParameterType(), aClone.getParameterType() ) );
    assertEquals( meta.getSql(), aClone.getSql() );
    assertEquals( meta.getXML(), aClone.getXML() );
  }
}
