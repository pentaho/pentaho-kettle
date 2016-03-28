/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.multimerge;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class MultiMergeJoinMetaTest {

  private MultiMergeJoinMeta multiMergeMeta;

  @Before
  public void setup() throws Exception {
    multiMergeMeta = new MultiMergeJoinMeta();
  }

  @Test
  public void testSetGetInputSteps() {
    assertNull( multiMergeMeta.getInputSteps() );
    String[] inputSteps = new String[] { "Step1", "Step2" };
    multiMergeMeta.setInputSteps( inputSteps );
    assertArrayEquals( inputSteps, multiMergeMeta.getInputSteps() );
  }

}
