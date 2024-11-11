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

package org.pentaho.di.core.injection.inheritance;

import java.util.List;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;

public class MetaBeanParent<T extends MetaBeanParentItem, A> {

  @InjectionDeep
  public List<T> items;

  @Injection( name = "A" )
  A obj;

  @InjectionDeep( prefix = "ITEM" )
  public T test1() {
    return null;
  }

  @InjectionDeep( prefix = "SUB" )
  public List<T> test2() {
    return null;
  }
}
