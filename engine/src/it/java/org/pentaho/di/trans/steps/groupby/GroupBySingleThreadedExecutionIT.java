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



package org.pentaho.di.trans.steps.groupby;

import org.pentaho.test.util.SingleThreadedExecutionGuarder;

/**
 * @author Andrey Khayrutdinov
 */
public class GroupBySingleThreadedExecutionIT extends SingleThreadedExecutionGuarder<GroupByMeta> {

  @Override protected GroupByMeta createMeta() {
    return new GroupByMeta();
  }
}
