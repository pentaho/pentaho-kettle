/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.step;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog.EditConnectionListener;

import org.eclipse.swt.custom.CCombo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class EditConnectionListenerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static String TEST_NAME = "TEST_NAME";

  private static String TEST_HOST = "TEST_HOST";

  private BaseStepDialog dialog;

  private EditConnectionListener editConnectionListener;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void init() {
    dialog = mock( BaseStepDialog.class );
    when( dialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) ).thenAnswer(
      new PropsSettingAnswer(
        TEST_NAME, TEST_HOST ) );
    dialog.transMeta = spy( new TransMeta() );
    CCombo combo = mock( CCombo.class );
    when( combo.getText() ).thenReturn( TEST_NAME );

    editConnectionListener = spy( dialog.new EditConnectionListener( combo ) );
    doNothing().when( editConnectionListener ).showErrorDialog( any( Exception.class ) );
  }

  private static class PropsSettingAnswer implements Answer<String> {

    private final String name;

    private final String host;

    public PropsSettingAnswer( String name, String host ) {
      this.name = name;
      this.host = host;
    }

    @Override
    public String answer( InvocationOnMock invocation ) throws Throwable {
      DatabaseMeta meta = (DatabaseMeta) invocation.getArguments()[ 0 ];
      meta.setName( name );
      meta.setHostname( host );
      return name;
    }
  }

  private static DatabaseMeta anyDbMeta() {
    return any( DatabaseMeta.class );
  }

  private static DatabaseManagementInterface anyDbMgr() {
    return any( DatabaseManagementInterface.class );
  }

  private static DatabaseMeta createDefaultDatabase( boolean sharedDb ) {
    DatabaseMeta existing = new DatabaseMeta();
    existing.setName( TEST_NAME );
    existing.setHostname( TEST_HOST );
    existing.setShared( sharedDb );
    return existing;
  }

}
