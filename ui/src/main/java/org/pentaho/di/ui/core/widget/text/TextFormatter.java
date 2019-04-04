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
