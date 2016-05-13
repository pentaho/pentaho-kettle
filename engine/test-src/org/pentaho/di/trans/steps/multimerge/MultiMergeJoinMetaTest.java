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
package org.pentaho.di.trans.steps.multimerge;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

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

  @Test
  public void testGetXml() {
    String[] inputSteps = new String[] { "Step1", "Step2" };
    multiMergeMeta.setInputSteps( inputSteps );
    multiMergeMeta.setKeyFields( new String[] {"Key1", "Key2"} );
    String xml = multiMergeMeta.getXML();
    Assert.assertTrue( xml.contains( "step0" ) );
    Assert.assertTrue( xml.contains( "step1" ) );

  }

  @Test
  public void cloneTest() throws Exception {
    MultiMergeJoinMeta meta = new MultiMergeJoinMeta();
    meta.allocateKeys( 2 );
    meta.allocateInputSteps( 3 );
    meta.setKeyFields( new String[] { "key1", "key2" } );
    meta.setInputSteps( new String[] { "step1", "step2", "step3" } );
    // scalars should be cloned using super.clone() - makes sure they're calling super.clone()
    meta.setJoinType( "INNER" );
    MultiMergeJoinMeta aClone = (MultiMergeJoinMeta) meta.clone();
    Assert.assertFalse( aClone == meta );
    Assert.assertTrue( Arrays.equals( meta.getKeyFields(), aClone.getKeyFields() ) );
    Assert.assertTrue( Arrays.equals( meta.getInputSteps(), aClone.getInputSteps() ) );
    Assert.assertEquals( meta.getJoinType(), aClone.getJoinType() );
  }
}
