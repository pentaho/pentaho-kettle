/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.injection;

/**
 * Wrong declaration - two annotations on the one field.
 */
@InjectionSupported( localizationPrefix = "" )
public class MetaBeanWrong1 {

  @Injection( name = "TEST" )
  @InjectionDeep
  private String s;
}
