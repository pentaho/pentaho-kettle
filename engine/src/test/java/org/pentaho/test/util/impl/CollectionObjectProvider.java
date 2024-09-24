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

package org.pentaho.test.util.impl;

import java.util.Collection;

import org.pentaho.test.util.ObjectProvider;

public class CollectionObjectProvider<T> implements ObjectProvider<T> {
  private final Collection<T> objects;

  public CollectionObjectProvider( Collection<T> objects ) {
    this.objects = objects;
  }

  @Override
  public Collection<T> getTestObjects() {
    return objects;
  }
}
