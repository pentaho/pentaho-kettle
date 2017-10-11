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

package org.pentaho.di.resource;

import java.util.Hashtable;
import java.util.Map;

/**
 * With this resource naming scheme we try to keep the original filename. However, if there are multiple files with the
 * same name, we add a sequence nr starting at 2.
 *
 * For example :
 *
 * Load orders.ktr Load orders 2.ktr Load orders 3.ktr etc.
 *
 * @author matt
 *
 */
public class SequenceResourceNaming extends SimpleResourceNaming {

  private Map<String, Integer> sequenceMap;

  public SequenceResourceNaming() {
    sequenceMap = new Hashtable<String, Integer>();
  }

  //
  // End result could look like any of the following:
  //
  // Inputs:
  // Prefix : Marc Sample Transformation
  // Original Path: D:\japps\pentaho\kettle\samples
  // Extension : .ktr
  //
  // Output Example 1 (no file system prefix, no path used)
  // Marc_Sample_Transformation_001.ktr
  // Output Example 2 (file system prefix: ${KETTLE_FILE_BASE}!, no path used)
  // ${KETTLE_FILE_BASE}!Marc_Sample_Transformation_003.ktr
  // Output Example 3 (file system prefix: ${KETTLE_FILE_BASE}!, path is used)
  // ${KETTLE_FILE_BASE}!japps/pentaho/kettle/samples/Marc_Sample_Transformation_014.ktr

  protected String getFileNameUniqueIdentifier( String filename, String extension ) {

    String key = filename + extension;
    Integer seq = sequenceMap.get( key );
    if ( seq == null ) {
      seq = new Integer( 2 );
      sequenceMap.put( key, seq );
      return null;
    }

    sequenceMap.put( key, new Integer( seq.intValue() + 1 ) );

    return seq.toString();
  }

}
