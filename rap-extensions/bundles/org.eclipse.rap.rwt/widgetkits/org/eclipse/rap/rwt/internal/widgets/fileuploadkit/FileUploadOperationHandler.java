/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.widgets.fileuploadkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.rap.rwt.internal.widgets.IFileUploadAdapter;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;


public class FileUploadOperationHandler extends ControlOperationHandler<FileUpload> {

  private static final String PROP_FILE_NAMES = "fileNames";

  public FileUploadOperationHandler( FileUpload fileUpload ) {
    super( fileUpload );
  }

  @Override
  public void handleSet( FileUpload fileUpload, JsonObject properties ) {
    super.handleSet( fileUpload, properties );
    handleSetFileNames( fileUpload, properties );
  }

  @Override
  public void handleNotify( FileUpload fileUpload, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( fileUpload, properties );
    } else {
      super.handleNotify( fileUpload, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET fileNames
   *
   * @param fileNames ([String]) array with selected file names
   */
  public void handleSetFileNames( FileUpload fileUpload, JsonObject properties ) {
    JsonValue value = properties.get( PROP_FILE_NAMES );
    if( value != null ) {
      JsonArray arrayValue = value.asArray();
      String[] fileNames = new String[ arrayValue.size() ];
      for( int i = 0; i < fileNames.length; i++ ) {
        fileNames[ i ] = arrayValue.get( i ).asString();
      }
      fileUpload.getAdapter( IFileUploadAdapter.class ).setFileNames( fileNames );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( FileUpload fileUpload, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    fileUpload.notifyListeners( SWT.Selection, event );
  }

}
