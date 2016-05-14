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
package org.pentaho.di.trans.steps.dbproc;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class DBProcMetaTest {

  @Test
  public void cloneTest() throws Exception {
    DBProcMeta meta = new DBProcMeta();
    meta.allocate( 2 );
    meta.setArgument( new String[] { "aa", "bb" } );
    meta.setArgumentDirection( new String[] { "cc", "dd" } );
    meta.setArgumentType( new int[] { 10, 50 } );
    meta.setProcedure( "aprocedure" );
    meta.setResultName( "aResultName" );
    DBProcMeta aClone = (DBProcMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getArgument(), aClone.getArgument() ) );
    assertTrue( Arrays.equals( meta.getArgumentDirection(), aClone.getArgumentDirection() ) );
    assertTrue( Arrays.equals( meta.getArgumentType(), aClone.getArgumentType() ) );
    assertEquals( meta.getProcedure(), aClone.getProcedure() );
    assertEquals( meta.getResultName(), aClone.getResultName() );
    assertEquals( meta.getXML(), aClone.getXML() );
  }
}
