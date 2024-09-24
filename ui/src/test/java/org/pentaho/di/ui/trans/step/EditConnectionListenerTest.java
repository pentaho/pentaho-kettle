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
