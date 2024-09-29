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
 * Wrong declaration - setter should have only one parameter.
 */
@InjectionSupported( localizationPrefix = "" )
public class MetaBeanWrong5 {

  @Injection( name = "TEST" )
  public void setter( int index, int value ) {
  }
}
