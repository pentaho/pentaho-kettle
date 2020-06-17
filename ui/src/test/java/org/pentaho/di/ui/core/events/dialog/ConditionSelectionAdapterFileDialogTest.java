/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.core.events.dialog;

import org.eclipse.swt.events.SelectionEvent;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.events.dialog.extension.ExtensionPointWrapper;
import org.pentaho.di.ui.core.widget.TextVar;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;

public class ConditionSelectionAdapterFileDialogTest {

  @Test
  public void testWidgetSelectedHelper() {
    //SETUP
    LogChannelInterface log = mock( LogChannelInterface.class );
    TextVar textVar = mock( TextVar.class );

    AbstractMeta meta = mock( AbstractMeta.class );
    RepositoryUtility repositoryUtility = mock( RepositoryUtility.class );
    ExtensionPointWrapper extensionPointWrapper = mock( ExtensionPointWrapper.class );
    SelectionAdapterOptions options = new SelectionAdapterOptions( SelectionOperation.FILE );
    SelectionEvent event = mock( SelectionEvent.class );

    String testPath = "/home/devuser/some/path";
    when( meta.environmentSubstitute( testPath ) ).thenReturn( testPath );
    when( textVar.getText() ).thenReturn( testPath );
    ArgumentCaptor textCapture = ArgumentCaptor.forClass( String.class );
    doNothing().when( textVar ).setText( (String) textCapture.capture() );

    ConditionSelectionAdapterFileDialogTextVar testInstance =
      new ConditionSelectionAdapterFileDialogTextVar( log, textVar, meta, options, repositoryUtility, extensionPointWrapper,
        () -> SelectionOperation.FOLDER );

    testInstance.widgetSelected( event );
    assertTrue( testInstance.getSelectionOptions().getSelectionOperation() == SelectionOperation.FOLDER  );

    options = new SelectionAdapterOptions( SelectionOperation.FILE );
    ConditionSelectionAdapterFileDialogTextVar testInstance2 =
      new ConditionSelectionAdapterFileDialogTextVar( log, textVar, meta, options, repositoryUtility, extensionPointWrapper,
        () -> SelectionOperation.FILE );

    testInstance.widgetSelected( event );
    assertTrue( testInstance2.getSelectionOptions().getSelectionOperation() == SelectionOperation.FILE  );

  }
}
