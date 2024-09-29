/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.spoon.trans.TransGraph;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ExpandedContentManager {

  static Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  /**
   * The value of the most recent URL navigated to.
   * Storing this value is useful because in some Operating Systems the internal browser implementations used via the
   * SWT browser widget seem to always return the same URL when a new value is set that only contains changes in its
   * hash section.
   */
  static String lastNavigateURL;

  /**
   * isBrowserVisible
   * 
   * @return a boolean that represents that the web browser is the topmost control of the active TransGraph. If browser
   *         hasn't been created it will return false.
   */
  public static boolean isVisible() {
    return isVisible( spoonInstance().getActiveTransGraph() );
  }

  /**
   * isBrowserVisible( TransGraph graph )
   * 
   * @param graph
   *          a TransGraph object that is being interrogated to see if the web browser is the topmost control
   * @return true if the web browser is the topmost control of the graph
   */
  public static boolean isVisible( TransGraph graph ) {
    if ( graph != null ) {
      if ( graph.getChildren().length > 0 ) {
        return graph.getChildren()[0] instanceof Browser;
      }
    }
    return false;
  }

  /**
   * createExpandedContent
   * 
   * creates a web browser for the current TransGraph
   */
  public static void createExpandedContent( String url ) {
    createExpandedContent( spoonInstance().getActiveTransGraph(), url );
  }

  /**
   * createExpandedContent( TransGraph parent )
   * 
   * Create a web browser for the TransGraph argument.
   * 
   * @param parent
   *          a TransGraph that will be the parent of the web browser.
   * @param url
   *          The content to open and expand
   */
  public static void createExpandedContent( TransGraph parent, String url ) {
    if ( parent == null ) {
      return;
    }
    Browser browser = getExpandedContentForTransGraph( parent );
    if ( browser == null ) {
      browser = new Browser( parent, SWT.NONE );
      browser.addKeyListener( new KeyListener() {
        @Override public void keyPressed( KeyEvent keyEvent ) {
          int state = keyEvent.stateMask, key = keyEvent.keyCode;

          boolean copyContent = state == SWT.CTRL && key == SWT.F6,
              arrowNavigation = ( state == SWT.COMMAND || state == SWT.ALT )
                  && ( key == SWT.ARROW_LEFT || key == SWT.ARROW_RIGHT ),
              backslashNavigation = ( state == SWT.SHIFT && key == SWT.BS ),
              reloadContent = state == SWT.CTRL && ( key == SWT.F5 || key == 114 /* r key */ ) || key == SWT.F5,
              zoomContent = state == SWT.CTRL && ( key == SWT.KEYPAD_ADD || key == SWT.KEYPAD_SUBTRACT
                  || key == 61 /* + key */ || key == 45 /* - key */ );

          if ( copyContent ) {
            Browser thisBrowser = (Browser) keyEvent.getSource();
            Clipboard clipboard = new Clipboard( thisBrowser.getDisplay() );
            clipboard.setContents( new String[] { lastNavigateURL }, new Transfer[] { TextTransfer.getInstance() } );
            clipboard.dispose();
          } else if ( arrowNavigation || backslashNavigation || reloadContent || zoomContent ) {
            keyEvent.doit = false;
          }
        }

        @Override public void keyReleased( KeyEvent keyEvent ) {
        }
      } );
    }

    browser.setUrl( url );
    lastNavigateURL = url;
  }

  /**
   * showTransformationBrowser
   * 
   * Creates and shows the web browser for the active TransGraph
   */
  public static void showExpandedContent() {
    showExpandedContent( spoonInstance().getActiveTransGraph() );
  }

  /**
   * showExpandedContent( TransGraph graph )
   * 
   * @param graph
   *          TransGraph to create the web browser for. If the wev browser hasn't been created this will create one.
   *          Else it will just bring the web browser associated to this TransGraph to the top.
   */
  public static void showExpandedContent( TransGraph graph ) {
    if ( graph == null ) {
      return;
    }
    Browser browser = getExpandedContentForTransGraph( graph );
    if ( browser == null ) {
      return;
    }
    if ( !isVisible( graph ) ) {
      maximizeExpandedContent( browser );
    }
    if ( Const.isOSX() && graph.isExecutionResultsPaneVisible() ) {
      graph.extraViewComposite.setVisible( false );
    }
    browser.moveAbove( null );
    browser.getParent().layout( true );
    browser.getParent().redraw();
  }

  /**
   * getExpandedContentForTransGraph
   * 
   * @param graph
   *          a TransGraph object that will be interrogated for a web browser
   * @return a web browser that is associated with the TransGraph or null if it has yet to be created.
   */
  public static Browser getExpandedContentForTransGraph( TransGraph graph ) {
    for ( Control control : graph.getChildren() ) {
      if ( control instanceof Browser ) {
        return (Browser) control;
      }
    }
    return null;
  }

  /**
   * hideExpandedContent
   * 
   * hides the web browser associated with the active TransGraph
   */
  public static void hideExpandedContent() {
    hideExpandedContent( spoonInstance().getActiveTransGraph() );
  }

  /**
   * closeExpandedContent
   *
   * closes the web browser associated with the active TransGraph
   */
  public static void closeExpandedContent() {
    closeExpandedContent( spoonInstance().getActiveTransGraph() );
  }

  /**
   * hideExpandedContent( TransGraph graph )
   * 
   * @param graph
   *          the TransGraph whose web browser will be hidden
   */
  public static void hideExpandedContent( TransGraph graph ) {
    doToExpandedContent( graph, browser -> {
      if ( Const.isOSX() && graph.isExecutionResultsPaneVisible() ) {
        graph.extraViewComposite.setVisible( true );
      }
      browser.moveBelow( null );
      browser.getParent().layout( true, true );
      browser.getParent().redraw();
    } );
  }

  /**
   * closeExpandedContent( TransGraph graph )
   *
   * @param graph
   *          the TransGraph whose web browser will be closed
   */
  public static void closeExpandedContent( TransGraph graph ) {
    doToExpandedContent( graph, Browser::close );
  }

  /**
   * doToExpandedContent( TransGraph graph )
   *
   * @param graph
   *          the TransGraph whose web browser will be hidden
   * @param browserAction Consumer for acting on the browser
   */
  private static void doToExpandedContent( TransGraph graph, Consumer<Browser> browserAction ) {
    Browser browser = getExpandedContentForTransGraph( graph );
    if ( browser == null ) {
      return;
    }
    SashForm sash = (SashForm) spoonInstance().getDesignParent();
    sash.setWeights( spoonInstance().getTabSet().getSelected().getSashWeights() );

    browserAction.accept( browser );
  }

  private static Spoon spoonInstance() {
    return spoonSupplier.get();
  }

  /**
   * maximizeBrowser
   * 
   * @param browser
   *          the browser object to maximize. We try to take up as much of the Spoon window as possible.
   */
  private static void maximizeExpandedContent( Browser browser ) {
    SashForm sash = (SashForm) spoonInstance().getDesignParent();
    int[] weights = sash.getWeights();
    int[] savedSashWeights = new int[weights.length];
    System.arraycopy( weights, 0, savedSashWeights, 0, weights.length );
    spoonInstance().getTabSet().getSelected().setSashWeights( savedSashWeights );
    weights[0] = 0;
    weights[1] = 1000;
    sash.setWeights( weights );
    FormData formData = new FormData();
    formData.top = new FormAttachment( 0, 0 );
    formData.left = new FormAttachment( 0, 0 );
    formData.bottom = new FormAttachment( 100, 0 );
    formData.right = new FormAttachment( 100, 0 );
    browser.setLayoutData( formData );
    browser.getParent().layout( true );
    browser.getParent().redraw();
  }
}
