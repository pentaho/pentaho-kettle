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

package org.pentaho.di.trans.steps.salesforcedelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SalesforceDeleteDataTest {

  @Test
  public void testConstructor() {
    SalesforceDeleteData data = new SalesforceDeleteData();
    assertNull( data.inputRowMeta );
    assertNull( data.outputRowMeta );
    assertNull( data.deleteResult );
    assertNull( data.deleteId );
    assertNull( data.outputBuffer );
    assertEquals( 0, data.iBufferPos );
    assertEquals( -1, data.indexOfKeyField );
  }
}
