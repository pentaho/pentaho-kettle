/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
