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


package org.pentaho.di.core.injection;

/**
 * Wrong declaration - two annotations on the one method.
 */
@InjectionSupported( localizationPrefix = "" )
public class MetaBeanWrong2 {

  @Injection( name = "TEST" )
  @InjectionDeep
  public String getter() {
    return null;
  }
}
