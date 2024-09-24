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

package org.pentaho.di.ui.spoon;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;

/**
 * The beginnings of a common graph object, used by JobGraph and TransGraph to share common behaviors.
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */
public abstract class AbstractGraph extends Composite {

  protected Point offset, iconoffset, noteoffset;

  protected ScrollBar vert, hori;

  protected Canvas canvas;

  protected float magnification = 1.0f;

  protected Combo zoomLabel;

  protected XulDomContainer xulDomContainer;

  protected static final float MIN_ZOOM = 0.05f;

  protected static final float MAX_ZOOM = 10.0f;

  public AbstractGraph( Composite parent, int style ) {
    super( parent, style );
  }

  protected abstract Point getOffset();

  protected Point getOffset( Point thumb, Point area ) {
    Point p = new Point( 0, 0 );
    Point sel = new Point( hori.getSelection(), vert.getSelection() );

    if ( thumb.x == 0 || thumb.y == 0 ) {
      return p;
    }

    p.x = Math.round( -sel.x * area.x / thumb.x / magnification );
    p.y = Math.round( -sel.y * area.y / thumb.y / magnification );

    return p;
  }

  protected Point magnifyPoint( Point p ) {
    return new Point( Math.round( p.x * magnification ), Math.round( p.y * magnification ) );
  }

  protected Point getThumb( Point area, Point transMax ) {
    Point resizedMax = magnifyPoint( transMax );

    Point thumb = new Point( 0, 0 );
    if ( resizedMax.x <= area.x ) {
      thumb.x = 100;
    } else {
      thumb.x = 100 * area.x / resizedMax.x;
    }

    if ( resizedMax.y <= area.y ) {
      thumb.y = 100;
    } else {
      thumb.y = 100 * area.y / resizedMax.y;
    }

    return thumb;
  }

  public int sign( int n ) {
    return n < 0 ? -1 : ( n > 0 ? 1 : 1 );
  }

  protected Point getArea() {
    org.eclipse.swt.graphics.Rectangle rect = canvas.getClientArea();
    Point area = new Point( rect.width, rect.height );

    return area;
  }

  protected void setZoomLabel() {
    zoomLabel.setText( Integer.toString( Math.round( magnification * 100 ) ) + "%" );
  }

  protected <T extends GUIPositionInterface> void doRightClickSelection( T clicked, List<T> selection ) {
    if ( selection.contains( clicked ) ) {
      return;
    }
    if ( !selection.isEmpty() ) {
      for ( GUIPositionInterface selected : selection ) {
        selected.setSelected( false );
      }
      selection.clear();
    }
    clicked.setSelected( true );
    selection.add( clicked );
    redraw();
  }

  public void redraw() {
    if ( isDisposed() || canvas.isDisposed() ) {
      return;
    }

    canvas.redraw();
    setZoomLabel();
  }

  public void zoomIn() {
    magnification += .1f;
    redraw();
  }

  public void zoomOut() {
    magnification -= .1f;
    redraw();
  }

  public void zoom100Percent() {
    magnification = 1.0f;
    redraw();
  }

  public Point screen2real( int x, int y ) {
    offset = getOffset();
    Point real;
    if ( offset != null ) {
      real =
        new Point( Math.round( ( x / magnification - offset.x ) ), Math.round( ( y / magnification - offset.y ) ) );
    } else {
      real = new Point( x, y );
    }

    return real;
  }

  public Point real2screen( int x, int y ) {
    offset = getOffset();
    Point screen = new Point( x + offset.x, y + offset.y );

    return screen;
  }

  public boolean setFocus() {
    return ( canvas != null && !canvas.isDisposed() ) ? canvas.setFocus() : false;
  }

  public boolean forceFocus() {
    return canvas.forceFocus();
  }

  /**
   * Gets the ChangedWarning for the given TabItemInterface class. This should be overridden by a given TabItemInterface
   * class to support the changed warning dialog.
   *
   * @return ChangedWarningInterface The class that provides the dialog and return value
   */
  public ChangedWarningInterface getChangedWarning() {
    return ChangedWarningDialog.getInstance();
  }

  /**
   * Show the ChangedWarning and return the users selection
   *
   * @return int Value of SWT.YES, SWT.NO, SWT.CANCEL
   */
  public int showChangedWarning( String fileName ) throws KettleException {
    ChangedWarningInterface changedWarning = getChangedWarning();

    if ( changedWarning != null ) {
      try {
        return changedWarning.show( fileName );
      } catch ( Exception e ) {
        throw new KettleException( e );
      }
    }

    return 0;
  }

  public int showChangedWarning() throws KettleException {
    return showChangedWarning( null );
  }

  public void dispose() {
    super.dispose();
    List<XulComponent> pops = xulDomContainer.getDocumentRoot().getElementsByTagName( "menupopup" );
    for ( XulComponent pop : pops ) {
      ( (MenuManager) pop.getManagedObject() ).dispose();
    }
  }
}
