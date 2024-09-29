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


package org.pentaho.di.core.injection;

/**
 * Wrong declaration - getter shouldn't have parameters.
 */
@InjectionSupported( localizationPrefix = "" )
public class MetaBeanWrong4 {

  @InjectionDeep
  public void getter( int index ) {
  }
}
