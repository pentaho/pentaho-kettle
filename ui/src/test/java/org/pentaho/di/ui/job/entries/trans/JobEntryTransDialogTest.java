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



package org.pentaho.di.ui.job.entries.trans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

/**
 * @author Vadim_Polynkov
 */
public class JobEntryTransDialogTest {

  private static final String FILE_NAME =  "TestTrans.ktr";

  JobEntryTransDialog dialog;

  @Test
  public void testEntryName() {
    dialog = mock( JobEntryTransDialog.class );
    doCallRealMethod().when( dialog ).getEntryName( any() );
    assertEquals( dialog.getEntryName( FILE_NAME ), "${Internal.Entry.Current.Directory}/" + FILE_NAME );
  }
}
