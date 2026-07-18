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



package org.pentaho.amazon.s3;

import org.junit.Before;

public class S3FileOutputMetaProcessFilenameTest {

  private S3FileOutputMeta meta = null;

  @Before
  public void beforeTest() {
    meta = new S3FileOutputMeta();
    meta.setFileName( "" );
  }

}
