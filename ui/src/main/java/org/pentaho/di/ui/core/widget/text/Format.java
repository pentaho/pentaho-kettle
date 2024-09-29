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
