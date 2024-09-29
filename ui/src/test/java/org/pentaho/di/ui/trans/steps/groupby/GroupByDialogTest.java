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


package org.pentaho.di.ui.trans.steps.groupby;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.di.ui.core.widget.TableView;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GroupByDialogTest {

  @Mock
  private TableView tableView = mock( TableView.class );

  @Mock
  private Button allRowsCheckBox = mock( Button.class );

  @Mock
  private GroupByDialog dialog = mock( GroupByDialog.class );

  @Mock
  private TableItem tableItem = mock( TableItem.class );

  @Before
  public void setup() {

    doCallRealMethod().when( dialog ).updateAllRowsCheckbox( tableView, allRowsCheckBox, true );
    doReturn( 2 ).when( tableView ).nrNonEmpty();
    doReturn( tableItem ).when( tableView ).getNonEmpty( anyInt() );
  }

  @Test
  public void updateAllRowsCheckbox_trueTest() {

    doReturn( "CUM_SUM" ).when( tableItem ).getText( anyInt() );
    dialog.updateAllRowsCheckbox( tableView, allRowsCheckBox, true );

    verify( allRowsCheckBox, times( 1 ) ).setSelection( true );
    verify( allRowsCheckBox, times( 1 ) ).setEnabled( false );
  }

  @Test
  public void updateAllRowsCheckbox_falseTest() {

    doReturn( "ANOTHER_VALUE" ).when( tableItem ).getText( anyInt() );
    dialog.updateAllRowsCheckbox( tableView, allRowsCheckBox, true );

    verify( allRowsCheckBox, times( 1 ) ).setEnabled( true );
  }
}
