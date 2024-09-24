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

package org.pentaho.di.trans.steps.splitfieldtorows;

import org.junit.Test;
import org.pentaho.di.trans.steps.StepMockUtil;

import static org.junit.Assert.assertEquals;

/**
 * @author Andrey Khayrutdinov
 */
public class SplitFieldToRowsTest {

  @Test
  public void interpretsNullDelimiterAsEmpty() throws Exception {
    SplitFieldToRows step =
      StepMockUtil.getStep( SplitFieldToRows.class, SplitFieldToRowsMeta.class, "handlesNullDelimiter" );

    SplitFieldToRowsMeta meta = new SplitFieldToRowsMeta();
    meta.setDelimiter( null );
    meta.setDelimiterRegex( false );

    SplitFieldToRowsData data = new SplitFieldToRowsData();

    step.init( meta, data );
    // empty string should be quoted --> \Q\E
    assertEquals( "\\Q\\E", data.delimiterPattern.pattern() );
  }
}
