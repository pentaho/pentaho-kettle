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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bmorrise on 9/15/17.
 */
public abstract class FormatRule {

  protected String pattern;
  protected Pattern compiledPattern;

  abstract Format execute( String value );

  public FormatRule( String pattern ) {
    this.pattern = pattern;
    this.compiledPattern = Pattern.compile( pattern );
  }

  protected Matcher parse( String value ) {
    return compiledPattern.matcher( value );
  }
}
