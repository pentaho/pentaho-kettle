/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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
