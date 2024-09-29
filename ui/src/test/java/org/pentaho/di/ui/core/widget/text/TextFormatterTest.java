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


package org.pentaho.di.ui.core.widget.text;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by bmorrise on 9/15/17.
 */
public class TextFormatterTest {

  @Test
  public void testUrlFormatter() {
    String value =
      "This has one [this is the first value](http://www.example.com/page/index.html?query=test1) [this is the second"
        + " value](http://www.example.com/page/index.html?query=test2)";

    Format format = TextFormatter.getInstance().execute( value );

    Assert.assertEquals( "This has one this is the first value this is the second value", format.getText() );
    Assert.assertEquals( 2, format.getStyleRanges().size() );
    Assert.assertEquals( format.getStyleRanges().get( 0 ).data, "http://www.example.com/page/index.html?query=test1" );
    Assert.assertEquals( format.getStyleRanges().get( 1 ).data, "http://www.example.com/page/index.html?query=test2" );
    Assert.assertEquals( 13, format.getStyleRanges().get( 0 ).start );
    Assert.assertEquals( 37, format.getStyleRanges().get( 1 ).start );
  }

}
