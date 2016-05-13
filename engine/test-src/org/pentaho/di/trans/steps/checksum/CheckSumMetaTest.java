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
package org.pentaho.di.trans.steps.checksum;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class CheckSumMetaTest {

  @Test
  public void cloneTest() throws Exception {
    CheckSumMeta meta = new CheckSumMeta();
    meta.allocate( 2 );
    meta.setFieldName( new String[] { "field1", "field2" } );
    meta.setCheckSumType( 2 ); // md5
    CheckSumMeta aClone = (CheckSumMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getFieldName(), aClone.getFieldName() ) );
    assertEquals( meta.getCheckSumType(), aClone.getCheckSumType() );
    assertEquals( meta.getXML(), aClone.getXML() );
  }

}
