package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ClientFileLoader;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.rap.rwt.widgets.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

/**
 * Clipboard implemented as a custom widget. This class is meant to be instantiated only once per UI session.
 */
public class Clipboard extends Widget {

  private RemoteObject remoteObject;
  private List<ClipboardListener> listeners = new ArrayList<ClipboardListener>();

  public Clipboard( Composite parent ) {
    super( parent, SWT.NONE );
    ResourceManager resourceManager = RWT.getResourceManager();
    ClientFileLoader clientFileLoader = RWT.getClient().getService( ClientFileLoader.class );
    clientFileLoader.requireJs( resourceManager.getLocation( "js/clipboard.js" ) );

    Connection connection = RWT.getUISession().getConnection();
    remoteObject = connection.createRemoteObject( "webSpoon.Clipboard" );
    remoteObject.set( "parent", WidgetUtil.getId( this ) );
    remoteObject.set( "self", remoteObject.getId() );
    remoteObject.setHandler( new AbstractOperationHandler() {
      @Override
      public void handleNotify(String event, JsonObject properties) {
        String widgetId = properties.get( "widgetId" ).asString();
        if ( event.equals( "paste" ) ) {
          listeners.stream()
            .filter( l -> l.getWidgetId().equals( widgetId ) )
            .forEach( l -> l.pasteListener( properties.get( "text" ).asString() ) );
        } else if ( event.equals( "cut" ) ) {
          listeners.stream()
            .filter( l -> l.getWidgetId().equals( widgetId ) )
            .forEach( l -> l.cutListener() );
        }
      }
    } );
    remoteObject.listen( "paste", true );
    remoteObject.listen( "copy", true );
    remoteObject.listen( "cut", true );
  }

  public void dispose() {
    remoteObject.destroy();
  }

  /**
   * Set data to the clipboard. This should be called before cut/copy.
   * @param text
   */
  public void setContents( String text ) {
    remoteObject.set( "text", text );
  }

  /**
   * Attach a widget to the clipboard. This should be called before any clipboard event.
   * @param widget
   */
  public void attachToClipboard( Widget widget ) {
    String widgetId = WidgetUtil.getId( widget );
    JavaScriptExecutor executor = RWT.getClient().getService( JavaScriptExecutor.class );
    executor.execute(
      "var x = document.getElementById( 'input-clipboard' );\n"
      + "x.value='" + widgetId + "';\n"
      + "x.focus();"
    );
  }

  public void addClipboardListener( ClipboardListener listener ) {
    this.listeners.add( listener );
  }

  public void removeClipboardListener( ClipboardListener listener ) {
    this.listeners.remove( listener );
  }

  public void downloadCanvasImage( String rwtId, String name ) {
    JsonObject obj = new JsonObject();
    obj.add( "rwtId", rwtId );
    obj.add( "name", name );
    remoteObject.call( "downloadCanvasImage", obj );
  }
}
