/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
