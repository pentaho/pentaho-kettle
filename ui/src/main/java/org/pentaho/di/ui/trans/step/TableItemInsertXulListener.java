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



package org.pentaho.di.ui.trans.step;

import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Created by IntelliJ IDEA. User: nbaker Date: Jun 8, 2010 Time: 11:05:32 AM To change this template use File |
 * Settings | File Templates.
 */
public interface TableItemInsertXulListener {
  public boolean tableItemInsertedFor( ValueMetaInterface v );
}
