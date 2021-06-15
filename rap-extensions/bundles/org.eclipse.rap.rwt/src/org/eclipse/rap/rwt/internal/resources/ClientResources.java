/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 *    Rüdiger Herrmann - bug 335112
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.resources;

import static org.eclipse.rap.rwt.internal.resources.ClientFilesReader.getInputFiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.rap.rwt.internal.RWTProperties;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.theme.AppearanceWriter;
import org.eclipse.rap.rwt.internal.theme.Theme;
import org.eclipse.rap.rwt.internal.theme.ThemeManager;
import org.eclipse.rap.rwt.internal.util.HTTP;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.SWT;


public final class ClientResources {

  private static final String CLIENT_FILES = "client.files";
  private static final String CLIENT_JS = "client.js";

  private static final List<String> JAVASCRIPT_FILES = getInputFiles( CLIENT_FILES );

  private static final String[] WIDGET_IMAGES = {
    "resource/static/image/blank.gif",
    "resource/widget/rap/arrows/chevron-left.png",
    "resource/widget/rap/arrows/chevron-right.png",
    "resource/widget/rap/arrows/chevron-left-hover.png",
    "resource/widget/rap/arrows/chevron-right-hover.png",
    "resource/widget/rap/tree/loading.gif",
    "resource/widget/rap/ctabfolder/maximize.gif",
    "resource/widget/rap/ctabfolder/minimize.gif",
    "resource/widget/rap/ctabfolder/restore.gif",
    "resource/widget/rap/ctabfolder/close.gif",
    "resource/widget/rap/ctabfolder/close_hover.gif",
    "resource/widget/rap/ctabfolder/ctabfolder-dropdown.png",
    "resource/widget/rap/cursors/alias.gif",
    "resource/widget/rap/cursors/copy.gif",
    "resource/widget/rap/cursors/move.gif",
    "resource/widget/rap/cursors/nodrop.gif",
    "resource/widget/rap/cursors/up_arrow.cur",
    "resource/widget/rap/scale/h_line.gif",
    "resource/widget/rap/scale/v_line.gif"
  };

  private final ApplicationContextImpl applicationContext;
  private final ResourceManager resourceManager;
  private final ThemeManager themeManager;

  public ClientResources( ApplicationContextImpl applicationContext ) {
    this.applicationContext = applicationContext;
    resourceManager = applicationContext.getResourceManager();
    themeManager = applicationContext.getThemeManager();
  }

  public void registerResources() {
    try {
      registerTextResource( "resource/static/html/blank.html" );
      registerJavascriptFiles();
      registerThemeResources();
      registerWidgetImages();
    } catch( IOException ioe ) {
      throw new RuntimeException( "Failed to register resources", ioe );
    }
  }

  private void registerJavascriptFiles()
    throws IOException
  {
    ContentBuffer contentBuffer = new ContentBuffer();
    String appearanceCode = createAppearanceCode();
    if( RWTProperties.isDevelopmentMode() ) {
      append( contentBuffer, "debug-settings.js" );
      for( String javascriptFile : JAVASCRIPT_FILES ) {
        append( contentBuffer, javascriptFile );
      }
    } else {
      append( contentBuffer, CLIENT_JS );
    }
    contentBuffer.append( appearanceCode.getBytes( HTTP.CHARSET_UTF_8 ) );
    registerJavascriptResource( contentBuffer, SWT.getVersion() + "/rap-client.js" );
  }

  private String createAppearanceCode() {
    List<String> customAppearances = themeManager.getAppearances();
    return AppearanceWriter.createAppearanceTheme( customAppearances );
  }

  private void append( ContentBuffer contentBuffer, String location ) throws IOException {
    InputStream inputStream = openResourceStream( location );
    if( inputStream == null ) {
      throw new IOException( "Failed to load resource: " + location );
    }
    try {
      contentBuffer.append( inputStream );
    } finally {
      inputStream.close();
    }
  }

  private void registerThemeResources() {
    String[] themeIds = themeManager.getRegisteredThemeIds();
    for( String themeId : themeIds ) {
      Theme theme = themeManager.getTheme( themeId );
      theme.registerResources( applicationContext );
    }
  }

  private void registerWidgetImages() throws IOException {
    for( String resourcePath : WIDGET_IMAGES ) {
      InputStream inputStream = openResourceStream( resourcePath );
      resourceManager.register( resourcePath, inputStream );
      inputStream.close();
    }
  }

  private void registerTextResource( String name ) throws IOException {
    InputStream inputStream = openResourceStream( name );
    try {
      resourceManager.register( name, inputStream );
    } finally {
      inputStream.close();
    }
  }

  private void registerJavascriptResource( ContentBuffer buffer, String name ) throws IOException {
    InputStream inputStream = buffer.getContentAsStream();
    try {
      resourceManager.register( name, inputStream );
    } finally {
      inputStream.close();
    }
    String location = resourceManager.getLocation( name );
    applicationContext.getStartupPage().setClientJsLibrary( location );
  }

  private InputStream openResourceStream( String name ) {
    return getClass().getClassLoader().getResourceAsStream( name );
  }

}
