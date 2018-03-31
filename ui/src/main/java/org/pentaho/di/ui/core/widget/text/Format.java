/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.ui.core.widget.text;

import org.eclipse.swt.custom.StyleRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 9/15/17.
 */
public class Format {
  private String text;
  private List<StyleRange> styleRanges = new ArrayList<>();

  public String getText() {
    return text;
  }

  public void setText( String text ) {
    this.text = text;
  }

  public List<StyleRange> getStyleRanges() {
    return styleRanges;
  }

  public void setStyleRanges( List<StyleRange> styleRanges ) {
    this.styleRanges = styleRanges;
  }

  public void add( Format format ) {
    text = format.getText();
    styleRanges.addAll( format.getStyleRanges() );
  }
}
