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


package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.pentaho.di.core.injection.Injection;

public class UsageParameter implements Cloneable {
  @Injection( name = "TAG", group = "PARAMETERS" )
  public String tag;
  @Injection( name = "VALUE", group = "PARAMETERS" )
  public String value;
  @Injection( name = "DESCRIPTION", group = "PARAMETERS" )
  public String description;

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

}
