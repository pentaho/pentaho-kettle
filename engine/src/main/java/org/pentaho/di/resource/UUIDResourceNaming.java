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

import org.pentaho.di.core.util.UUIDUtil;

public class UUIDResourceNaming extends SimpleResourceNaming {

  //
  // End result could look like any of the following:
  //
  // Inputs:
  // Prefix : Marc Sample Transformation
  // Original Path: D:\japps\pentaho\kettle\samples
  // Extension : .ktr
  //
  // Output Example 1 (no file system prefix, no path used)
  // Marc_Sample_Transformation_03a32f25-1538-11dc-ae07-5dbf1395f3fd.ktr
  // Output Example 2 (file system prefix: ${KETTLE_FILE_BASE}!, no path used)
  // ${KETTLE_FILE_BASE}!Marc_Sample_Transformation_03a32f25-1538-11dc-ae07-5dbf1395f3fd.ktr
  // Output Example 3 (file system prefix: ${KETTLE_FILE_BASE}!, path is used)
  // ${KETTLE_FILE_BASE}!japps/pentaho/kettle/samples/
  //   Marc_Sample_Transformation_03a32f25-1538-11dc-ae07-5dbf1395f3fd.ktr

  protected String getFileNameUniqueIdentifier() {
    // This implementation assumes that the name alone
    // will be insufficient to uniquely identify the
    // file. So, return a UUID which will be used
    // to guarentee uniqueness.
    //
    // The UUID will look something like this:
    // 03a32f25-1538-11dc-ae07-5dbf1395f3fd
    //
    return UUIDUtil.getUUIDAsString();
  }

}
