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
