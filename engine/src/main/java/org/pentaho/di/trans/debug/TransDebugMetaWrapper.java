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

package org.pentaho.di.trans.debug;

import org.pentaho.di.trans.Trans;

public class TransDebugMetaWrapper {
  private final Trans trans;
  private final TransDebugMeta transDebugMeta;

  public TransDebugMetaWrapper( Trans trans, TransDebugMeta transDebugMeta ) {
    super();
    this.trans = trans;
    this.transDebugMeta = transDebugMeta;
  }

  public Trans getTrans() {
    return trans;
  }

  public TransDebugMeta getTransDebugMeta() {
    return transDebugMeta;
  }
}
