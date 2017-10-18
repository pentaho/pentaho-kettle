/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.pkg;

import org.pentaho.di.pan.Pan;

/**
 * Executes a transformation calls transformation.xml from within a jar file.
 *
 * @author Matt
 * @since
 */
public class JarPan {
  public static void main( String[] a ) {
    String[] args = new String[a.length + 1];
    args[0] = "-jarfile:/" + JarfileGenerator.TRANSFORMATION_FILENAME;
    for ( int i = 0; i < a.length; i++ ) {
      args[i + 1] = a[i];
    }

    try {
      Pan.main( args );
    } catch ( Exception ke ) {
      System.out.println( "ERROR occurred: " + ke.getMessage() );
      System.exit( 2 );
    }

  }
}
