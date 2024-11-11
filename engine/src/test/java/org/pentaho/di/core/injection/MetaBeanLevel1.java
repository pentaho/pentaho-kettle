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

@InjectionSupported( localizationPrefix = "", groups = { "FILENAME_LINES", "FILENAME_LINES2" }, hide = {
  "FLONG_HIDDEN" } )
public class MetaBeanLevel1 {

  @InjectionDeep
  private MetaBeanLevel2 sub;

  @Injection( name = "FBOOLEAN" )
  public boolean fboolean;
  @Injection( name = "FINT" )
  public int fint;
  @Injection( name = "FLONG" )
  public long flong;
  @Injection( name = "FLONG_HIDDEN" )
  public long flong_hidden;

  public MetaBeanLevel2 getSub() {
    return sub;
  }

  public void setSub( MetaBeanLevel2 sub ) {
    this.sub = sub;
  }
}
