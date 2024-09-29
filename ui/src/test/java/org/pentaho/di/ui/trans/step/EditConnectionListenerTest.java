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

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.swt.custom.CCombo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog.EditConnectionListener;

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
    when( dialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta() ) ).thenAnswer( new PropsSettingAnswer(
        TEST_NAME, TEST_HOST ) );
    dialog.transMeta = spy( new TransMeta() );
    CCombo combo = mock( CCombo.class );
    when( combo.getText() ).thenReturn( TEST_NAME );

    editConnectionListener = spy( dialog.new EditConnectionListener( combo ) );
    doNothing().when( editConnectionListener ).showErrorDialog( any( Exception.class ) );
  }

  @Test
  public void replaceSharedConnection() throws IOException, KettleException {
    dialog.transMeta.addDatabase( createDefaultDatabase( true ) );
    SharedObjects sharedObjects = mock( SharedObjects.class );
    doReturn( sharedObjects ).when( dialog.transMeta ).getSharedObjects();

    editConnectionListener.widgetSelected( null );

    verify( editConnectionListener ).replaceSharedConnection( any( DatabaseMeta.class ), any( DatabaseMeta.class ) );
    verify( sharedObjects ).removeObject( any( SharedObjectInterface.class ) );
    verify( sharedObjects ).storeObject( any( SharedObjectInterface.class ) );
    verify( sharedObjects ).saveToFile();
  }

  @Test
  public void replaceSharedConnectionDoesNotExecuted_for_nonshared_connection() {
    dialog.transMeta.addDatabase( createDefaultDatabase( false ) );
    editConnectionListener.widgetSelected( null );

    verify( editConnectionListener, never() ).replaceSharedConnection( any( DatabaseMeta.class ), any(
        DatabaseMeta.class ) );
  }

  @Test
  public void replaceSharedConnectionReturnsFalse_on_error() throws IOException, KettleException {
    dialog.transMeta.addDatabase( createDefaultDatabase( false ) );
    SharedObjects sharedObjects = mock( SharedObjects.class );
    doThrow( KettleException.class ).when( sharedObjects ).saveToFile();

    boolean actualResult = editConnectionListener.replaceSharedConnection( anyDbMeta(), anyDbMeta() );

    assertFalse( actualResult );
    verify( editConnectionListener ).showErrorDialog( any( Exception.class ) );
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
      DatabaseMeta meta = (DatabaseMeta) invocation.getArguments()[0];
      meta.setName( name );
      meta.setHostname( host );
      return name;
    }
  }

  private static DatabaseMeta anyDbMeta() {
    return any( DatabaseMeta.class );
  }

  private static DatabaseMeta createDefaultDatabase( boolean sharedDb ) {
    DatabaseMeta existing = new DatabaseMeta();
    existing.setName( TEST_NAME );
    existing.setHostname( TEST_HOST );
    existing.setShared( sharedDb );
    return existing;
  }

}
