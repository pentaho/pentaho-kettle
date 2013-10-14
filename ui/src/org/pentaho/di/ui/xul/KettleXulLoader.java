/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.xul;

import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class KettleXulLoader extends SwtXulLoader {

  public KettleXulLoader() throws XulException {
    super();

    parser.handlers.remove( "DIALOG" );
    parser.registerHandler( "DIALOG", org.pentaho.di.ui.xul.KettleDialog.class.getName() );
  }
}
