/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.Spoon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 2/18/16.
 */
public class ThinDialog extends Dialog {

  private final boolean doAuthenticate;
  protected Shell parent;
  protected int width;
  protected int height;
  protected Browser browser;
  protected Shell dialog;
  protected Display display;

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;
  private LogChannelInterface log = spoonSupplier.get().getLog();
  @SuppressWarnings( "squid:S1075" )
  private static final String SECURITY_CHECK_PATH = "/j_spring_security_check";

  /**
   * @param doAuthenticate if true, will attempt to authenticate against the repository server prior to opening the SWT
   *                       browser.
   */
  public ThinDialog( Shell shell, int width, int height, boolean doAuthenticate ) {
    super( shell );
    this.width = width;
    this.height = height;
    this.doAuthenticate = doAuthenticate;
  }

  public ThinDialog( Shell shell, int width, int height ) {
    this( shell, width, height, false );
  }

  public void createDialog( String title, String url, int options, Image logo ) {
    display = getParent().getDisplay();

    dialog = new Shell( getParent(), options );
    dialog.setText( title );
    dialog.setImage( logo );
    dialog.setSize( width, height );
    dialog.setLayout( new FillLayout() );

    dialog.addListener( SWT.Traverse, e -> {
      if ( e.detail == SWT.TRAVERSE_ESCAPE ) {
        e.doit = false;
      }
    } );

    try {
      browser = new Browser( dialog, SWT.NONE );
      if ( doAuthenticate ) {
        authenticate();
      }
      browser.setUrl( url );
      browser.addCloseWindowListener( event -> {
        Browser browse = (Browser) event.widget;
        Shell shell = browse.getShell();
        shell.close();
      } );
      if ( Const.isRunningOnWebspoonMode() ) {
        new BrowserFunction( browser, "getConnectionId" ) {
          @Override public Object function( Object[] arguments ) {
            try {
              Class webSpoonUtils = Class.forName( "org.pentaho.di.webspoon.WebSpoonUtils" );
              Method getConnectionId = webSpoonUtils.getDeclaredMethod( "getConnectionId" );
              return getConnectionId.invoke( null );
            } catch ( ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
              e.printStackTrace();
              return null;
            }
          }
        };
      }
    } catch ( Exception e ) {
      MessageBox messageBox = new MessageBox( dialog, SWT.ICON_ERROR | SWT.OK );
      messageBox.setMessage( "Browser cannot be initialized." );
      messageBox.setText( "Exit" );
      messageBox.open();
    }
    setPosition();
    dialog.open();
  }

  protected void setPosition() {
    Rectangle shellBounds = getParent().getBounds();
    Point dialogSize = dialog.getSize();
    dialog.setLocation( shellBounds.x + ( shellBounds.width - dialogSize.x ) / 2, shellBounds.y
      + ( shellBounds.height - dialogSize.y ) / 2 );
  }

  /**
   * If connected to a repository with a defined Uri, this method will attempt to authenticate to the server, retrieve
   * the auth cookie, and add it to the SWT browser's cookie store. Used in cases where the thin dialog may be loaded
   * from pentaho server, not just local service in spoon.
   */
  private void authenticate() {
    Repository repo = spoonSupplier.get().getRepository();
    if ( repo != null ) {
      repo.getUri().ifPresent( uri -> setCookies( repo.getUserInfo(), uri ) );
    }
  }

  /**
   * POSTs username/pass to the security check page, retrieving the JSESSIONID cookie and setting it on the browser.
   */
  private void setCookies( IUser user, URI uri ) {
    Objects.requireNonNull( browser );
    HttpClient client = HttpClientBuilder.create().build();
    try {
      URIBuilder builder = new URIBuilder( uri.toString() + SECURITY_CHECK_PATH );
      builder.addParameter( "j_username", user.getName() )
        .addParameter( "j_password", user.getPassword() );
      log.logDebug( "Authenticating with " + user.getName() );
      HttpResponse resp = client.execute( new HttpPost( builder.build() ) );

      Arrays.stream( resp.getHeaders( "Set-Cookie" ) )
        .map( Header::getValue )
        .flatMap( s -> HttpCookie.parse( s ).stream() )
        .forEach( cookie -> Browser.setCookie( cookie.toString(), uri.toString() ) );
    } catch ( IOException | URISyntaxException e ) {
      log.logError( e.getMessage(), e );
    }
  }
}
