/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import org.eclipse.swt.custom.CCombo;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class BaseStepDialog_ConnectionLine_Test {

  private static String INITIAL_NAME = "qwerty";
  private static String INPUT_NAME = "asdfg";

  private static String INITIAL_HOST = "1.2.3.4";
  private static String INPUT_HOST = "5.6.7.8";

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  @Test
  public void adds_WhenConnectionNameIsUnique() throws Exception {
    TransMeta transMeta = new TransMeta();

    invokeAddConnectionListener( transMeta, INPUT_NAME );

    assertOnlyDbExists( transMeta, INPUT_NAME, INPUT_HOST );
  }

  @Test
  public void ignores_WhenConnectionNameIsUsed() throws Exception {
    TransMeta transMeta = new TransMeta();
    transMeta.addDatabase( createDefaultDatabase() );

    invokeAddConnectionListener( transMeta, null );

    assertOnlyDbExists( transMeta, INITIAL_NAME, INITIAL_HOST );
  }

  private void invokeAddConnectionListener( TransMeta transMeta, String answeredName ) throws Exception {
    BaseStepDialog dialog = mock( BaseStepDialog.class );
    when( dialog.showDbDialogUnlessCancelledOrValid( any( DatabaseMeta.class ) ) )
      .thenAnswer( new PropsSettingAnswer( answeredName, INPUT_HOST ) );

    dialog.transMeta = transMeta;
    dialog.new AddConnectionListener( mock( CCombo.class ) ).widgetSelected( null );
  }


  @Test
  public void edits_WhenNotRenamed() throws Exception {
    TransMeta transMeta = new TransMeta();
    transMeta.addDatabase( createDefaultDatabase() );

    invokeEditConnectionListener( transMeta, INITIAL_NAME );

    assertOnlyDbExists( transMeta, INITIAL_NAME, INPUT_HOST );
  }

  @Test
  public void edits_WhenNewNameIsUnique() throws Exception {
    TransMeta transMeta = new TransMeta();
    transMeta.addDatabase( createDefaultDatabase() );

    invokeEditConnectionListener( transMeta, INPUT_NAME );

    assertOnlyDbExists( transMeta, INPUT_NAME, INPUT_HOST );
  }

  @Test
  public void ignores_WhenNewNameIsUsed() throws Exception {
    TransMeta transMeta = new TransMeta();
    transMeta.addDatabase( createDefaultDatabase() );

    invokeEditConnectionListener( transMeta, null );

    assertOnlyDbExists( transMeta, INITIAL_NAME, INITIAL_HOST );
  }

  private void invokeEditConnectionListener( TransMeta transMeta, String answeredName ) throws Exception {
    BaseStepDialog dialog = mock( BaseStepDialog.class );
    when( dialog.showDbDialogUnlessCancelledOrValid( any( DatabaseMeta.class ) ) )
      .thenAnswer( new PropsSettingAnswer( answeredName, INPUT_HOST ) );

    CCombo combo = mock( CCombo.class );
    when( combo.getText() ).thenReturn( INITIAL_NAME );

    dialog.transMeta = transMeta;
    dialog.new EditConnectionListener( combo ).widgetSelected( null );
  }


  private DatabaseMeta createDefaultDatabase() {
    DatabaseMeta existing = new DatabaseMeta();
    existing.setName( INITIAL_NAME );
    existing.setHostname( INITIAL_HOST );
    return existing;
  }

  private void assertOnlyDbExists( TransMeta transMeta, String name, String host ) {
    assertEquals( 1, transMeta.getDatabases().size() );
    assertEquals( name, transMeta.getDatabase( 0 ).getName() );
    assertEquals( host, transMeta.getDatabase( 0 ).getHostname() );
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
}
