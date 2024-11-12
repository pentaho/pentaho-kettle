/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
