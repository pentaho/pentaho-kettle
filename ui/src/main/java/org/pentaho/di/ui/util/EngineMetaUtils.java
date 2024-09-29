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

package org.pentaho.di.ui.util;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;

public class EngineMetaUtils {

  /**
   * Validates if {@code engineMetaInterface} is Job or Transformation.
   * 
   * @param engineMetaInterface
   * @return true if engineMetaInterface instance is Job or Transformation, otherwise false.
   */
  public static boolean isJobOrTransformation( EngineMetaInterface engineMetaInterface ) {
    if ( engineMetaInterface == null || engineMetaInterface.getRepositoryElementType() == null ) {
      return false;
    }
    RepositoryObjectType objectType = engineMetaInterface.getRepositoryElementType();
    return RepositoryObjectType.TRANSFORMATION.equals( objectType ) || RepositoryObjectType.JOB.equals( objectType );
  }

}
