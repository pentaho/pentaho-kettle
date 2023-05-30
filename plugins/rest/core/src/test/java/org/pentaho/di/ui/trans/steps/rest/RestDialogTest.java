/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.rest;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.reflect.Whitebox.setInternalState;

public class RestDialogTest {

  private Label bodyl = mock( Label.class );
  private ComboVar body = mock( ComboVar.class );
  private ComboVar type = mock( ComboVar.class );

  private Label paramsl = mock( Label.class );
  private TableView params = mock( TableView.class );
  private Button paramsb = mock( Button.class );

  private Label matrixl = mock( Label.class );
  private TableView matrix = mock( TableView.class );
  private Button matrixb = mock( Button.class );

  private ComboVar method = mock( ComboVar.class );

  private RestDialog dialog = mock( RestDialog.class );

  @Before
  public void setup() {
    doCallRealMethod().when( dialog ).setMethod();

    setInternalState( dialog, "wlBody", bodyl );
    setInternalState( dialog, "wBody", body );
    setInternalState( dialog, "wApplicationType", type );

    setInternalState( dialog, "wlParameters", paramsl );
    setInternalState( dialog, "wParameters", params );
    setInternalState( dialog, "wGet", paramsb );

    setInternalState( dialog, "wlMatrixParameters", matrixl );
    setInternalState( dialog, "wMatrixParameters", matrix );
    setInternalState( dialog, "wMatrixGet", matrixb );

    setInternalState( dialog, "wMethod", method );
  }

  @Test
  public void testSetMethod_GET() {
    doReturn( RestMeta.HTTP_METHOD_GET ).when( method ).getText();

    dialog.setMethod();

    verify( bodyl, times( 1 ) ).setEnabled( false );
    verify( body, times( 1 ) ).setEnabled( false );
    verify( type, times( 1 ) ).setEnabled( false );

    verify( paramsl, times( 1 ) ).setEnabled( false );
    verify( params, times( 1 ) ).setEnabled( false );
    verify( paramsb, times( 1 ) ).setEnabled( false );

    verify( matrixl, times( 1 ) ).setEnabled( false );
    verify( matrix, times( 1 ) ).setEnabled( false );
    verify( matrixb, times( 1 ) ).setEnabled( false );
  }

  @Test
  public void testSetMethod_POST() {
    doReturn( RestMeta.HTTP_METHOD_POST ).when( method ).getText();

    dialog.setMethod();

    verify( bodyl, times( 1 ) ).setEnabled( true );
    verify( body, times( 1 ) ).setEnabled( true );
    verify( type, times( 1 ) ).setEnabled( true );

    verify( paramsl, times( 1 ) ).setEnabled( true );
    verify( params, times( 1 ) ).setEnabled( true );
    verify( paramsb, times( 1 ) ).setEnabled( true );

    verify( matrixl, times( 1 ) ).setEnabled( true );
    verify( matrix, times( 1 ) ).setEnabled( true );
    verify( matrixb, times( 1 ) ).setEnabled( true );
  }

  @Test
  public void testSetMethod_PUT() {
    doReturn( RestMeta.HTTP_METHOD_PUT ).when( method ).getText();

    dialog.setMethod();

    verify( bodyl, times( 1 ) ).setEnabled( true );
    verify( body, times( 1 ) ).setEnabled( true );
    verify( type, times( 1 ) ).setEnabled( true );

    verify( paramsl, times( 1 ) ).setEnabled( true );
    verify( params, times( 1 ) ).setEnabled( true );
    verify( paramsb, times( 1 ) ).setEnabled( true );

    verify( matrixl, times( 1 ) ).setEnabled( true );
    verify( matrix, times( 1 ) ).setEnabled( true );
    verify( matrixb, times( 1 ) ).setEnabled( true );
  }

  @Test
  public void testSetMethod_PATCH() {
    doReturn( RestMeta.HTTP_METHOD_PATCH ).when( method ).getText();

    dialog.setMethod();

    verify( bodyl, times( 1 ) ).setEnabled( true );
    verify( body, times( 1 ) ).setEnabled( true );
    verify( type, times( 1 ) ).setEnabled( true );

    verify( paramsl, times( 1 ) ).setEnabled( true );
    verify( params, times( 1 ) ).setEnabled( true );
    verify( paramsb, times( 1 ) ).setEnabled( true );

    verify( matrixl, times( 1 ) ).setEnabled( true );
    verify( matrix, times( 1 ) ).setEnabled( true );
    verify( matrixb, times( 1 ) ).setEnabled( true );
  }

  @Test
  public void testSetMethod_DELETE() {
    doReturn( RestMeta.HTTP_METHOD_DELETE ).when( method ).getText();

    dialog.setMethod();

    verify( bodyl, times( 1 ) ).setEnabled( false );
    verify( body, times( 1 ) ).setEnabled( false );
    verify( type, times( 1 ) ).setEnabled( false );

    verify( paramsl, times( 1 ) ).setEnabled( true );
    verify( params, times( 1 ) ).setEnabled( true );
    verify( paramsb, times( 1 ) ).setEnabled( true );

    verify( matrixl, times( 1 ) ).setEnabled( true );
    verify( matrix, times( 1 ) ).setEnabled( true );
    verify( matrixb, times( 1 ) ).setEnabled( true );
  }

  @Test
  public void testSetMethod_OPTIONS() {
    doReturn( RestMeta.HTTP_METHOD_OPTIONS ).when( method ).getText();

    dialog.setMethod();

    verify( bodyl, times( 1 ) ).setEnabled( false );
    verify( body, times( 1 ) ).setEnabled( false );
    verify( type, times( 1 ) ).setEnabled( false );

    verify( paramsl, times( 1 ) ).setEnabled( false );
    verify( params, times( 1 ) ).setEnabled( false );
    verify( paramsb, times( 1 ) ).setEnabled( false );

    verify( matrixl, times( 1 ) ).setEnabled( false );
    verify( matrix, times( 1 ) ).setEnabled( false );
    verify( matrixb, times( 1 ) ).setEnabled( false );
  }

  @Test
  public void testSetMethod_HEAD() {
    doReturn( RestMeta.HTTP_METHOD_HEAD ).when( method ).getText();

    dialog.setMethod();

    verify( bodyl, times( 1 ) ).setEnabled( false );
    verify( body, times( 1 ) ).setEnabled( false );
    verify( type, times( 1 ) ).setEnabled( false );

    verify( paramsl, times( 1 ) ).setEnabled( false );
    verify( params, times( 1 ) ).setEnabled( false );
    verify( paramsb, times( 1 ) ).setEnabled( false );

    verify( matrixl, times( 1 ) ).setEnabled( false );
    verify( matrix, times( 1 ) ).setEnabled( false );
    verify( matrixb, times( 1 ) ).setEnabled( false );
  }
}
