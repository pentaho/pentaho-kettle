/*******************************************************************************
 * Copyright (c) 2011, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.widgets.fileuploadkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.widgets.IFileUploadAdapter;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.widgets.FileUpload;


public final class FileUploadLCA extends WidgetLCA<FileUpload> {

  public static final FileUploadLCA INSTANCE = new FileUploadLCA();

  private static final String TYPE = "rwt.widgets.FileUpload";
  private static final String[] ALLOWED_STYLES = { "BORDER", "MULTI" };

  private static final String PROP_TEXT = "text";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_FILTER_EXTENSIONS = "filterExtensions";

  private static final String[] DEFAUT_FILTER_EXTENSIONS = new String[ 0 ];

  @Override
  public void preserveValues( FileUpload fileUpload ) {
    preserveProperty( fileUpload, PROP_TEXT, fileUpload.getText() );
    preserveProperty( fileUpload, PROP_IMAGE, fileUpload.getImage() );
    preserveProperty( fileUpload, PROP_FILTER_EXTENSIONS, fileUpload.getFilterExtensions() );
  }

  @Override
  public void renderInitialization( FileUpload fileUpload ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( fileUpload, TYPE );
    remoteObject.setHandler( new FileUploadOperationHandler( fileUpload ) );
    remoteObject.set( "parent", getId( fileUpload.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( fileUpload, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( FileUpload fileUpload ) throws IOException {
    ControlLCAUtil.renderChanges( fileUpload );
    WidgetLCAUtil.renderCustomVariant( fileUpload );
    renderProperty( fileUpload, PROP_TEXT, fileUpload.getText(), "" );
    renderProperty( fileUpload, PROP_IMAGE, fileUpload.getImage(), null );
    renderProperty( fileUpload,
                    PROP_FILTER_EXTENSIONS,
                    fileUpload.getFilterExtensions(),
                    DEFAUT_FILTER_EXTENSIONS );
    renderListenSelection( fileUpload );
    renderSubmit( fileUpload );
  }

  private static void renderSubmit( FileUpload fileUpload ) {
    String url = fileUpload.getAdapter( IFileUploadAdapter.class ).getAndResetUrl();
    if( url != null ) {
      getRemoteObject( fileUpload ).call( "submit", new JsonObject().add( "url", url ) );
    }
  }

  private FileUploadLCA() {
    // prevent instantiation
  }

}
