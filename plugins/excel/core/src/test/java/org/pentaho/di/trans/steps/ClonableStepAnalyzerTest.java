/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;

public abstract class ClonableStepAnalyzerTest {

  protected abstract IClonableStepAnalyzer newInstance();

  @Test
  public void testCloneAnalyzer() {
    final IClonableStepAnalyzer analyzer = newInstance();
    // verify that cloneAnalyzer returns an instance that is different from the original
    Assert.assertNotEquals( analyzer, analyzer.cloneAnalyzer() );

  }
}
