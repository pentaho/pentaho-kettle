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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 9/15/17.
 */
public class TextFormatter {

  private List<FormatRule> rules = new ArrayList<>();
  public static TextFormatter instance;

  public TextFormatter() {
    registerRule( new UrlFormatRule() );
  }

  public static TextFormatter getInstance() {
    if ( instance == null ) {
      instance = new TextFormatter();
    }
    return instance;
  }

  public void registerRule( FormatRule rule ) {
    rules.add( rule );
  }

  public Format execute( String value ) {
    Format format = new Format();
    for ( FormatRule rule : rules ) {
      format.add( rule.execute( value ) );
    }
    return format;
  }
}
