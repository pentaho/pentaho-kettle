/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.xul;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.SwtUniversalImageSvg;
import org.pentaho.di.core.svg.SvgImage;
import org.pentaho.di.core.svg.SvgSupport;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.containers.XulRoot;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class KettleDialog extends SwtDialog {
  private Map<String, Image[]> imagesCache = new HashMap<>();

  public KettleDialog( Element self, XulComponent parent, XulDomContainer container, String tagName ) {
    super( self, parent, container, tagName );
  }

  @Override
  public void show() {
    show( true );
  }

  @SuppressWarnings( "deprecation" )
  @Override
  public void show( boolean force ) {
    if ( ( force ) || ( !buttonsCreated ) ) {
      setButtons();
    }

    isDialogHidden = false;

    dialog.getShell().setText( title );

    // Remember the size from a last time or do proper layouting of the window.
    //
    if ( getWidth() > 0 && getHeight() > 0 ) {
      BaseStepDialog.setSize( getShell(), getWidth(), getHeight(), true );
    } else {
      BaseStepDialog.setSize( getShell() );
    }

    width = getShell().getSize().x;
    height = getShell().getSize().y;

    dialog.getShell().layout( true, true );

    // Timing is everything - fire the onLoad events so that anyone who is trying to listens gets notified
    //
    notifyListeners( XulRoot.EVENT_ON_LOAD );

    setAppicon( appIcon );

    returnCode = dialog.open();
  }

  @Override
  public void hide() {

    if ( closing || dialog.getMainArea().isDisposed() || getParentShell( getParent() ).isDisposed()
        || ( getParent() instanceof SwtDialog && ( (SwtDialog) getParent() ).isDisposing() ) ) {
      return;
    }

    // Save the window location & size in the Kettle world...
    //
    WindowProperty windowProperty = new WindowProperty( getShell() );
    PropsUI.getInstance().setScreen( windowProperty );

    super.hide();
  }

  @Override
  public void setAppicon( String icon ) {
    this.appIcon = icon;

    if ( appIcon == null || dialog == null ) {
      return;
    }

    Image[] images;
    synchronized ( imagesCache ) {
      images = imagesCache.get( icon );
    }
    if ( images == null ) {
      images = loadImages( icon );
      synchronized ( imagesCache ) {
        imagesCache.put( icon, images );
      }
    }
    if ( images == null ) {
      super.setAppicon( icon );
    } else {
      if ( images != null && dialog != null ) {
        dialog.getShell().setImages( images );
      }
    }
  }

  /**
   * Icon sizes for rendering dialog icon from svg.
   */
  static final int[] IMAGE_SIZES = new int[] { 256, 128, 64, 48, 32, 16 };

  /**
   * Load multiple images from svg, or just png file.
   */
  private Image[] loadImages( String resource ) {
    Display d = dialog.getShell().getDisplay();
    if ( d == null ) {
      d = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
    }

    if ( SvgSupport.isSvgEnabled() && ( SvgSupport.isSvgName( resource ) || SvgSupport.isPngName( resource ) ) ) {
      InputStream in = null;
      try {
        in = getResourceInputStream( resource );
        // getResourceInputStream( SvgSupport.toSvgName( resource ) );
        // load SVG
        SvgImage svg = SvgSupport.loadSvgImage( in );
        SwtUniversalImage image = new SwtUniversalImageSvg( svg );

        Image[] result = new Image[IMAGE_SIZES.length];
        for ( int i = 0; i < IMAGE_SIZES.length; i++ ) {
          result[i] = image.getAsBitmapForSize( d, IMAGE_SIZES[i], IMAGE_SIZES[i] );
        }
        return result;
      } catch ( Throwable ignored ) {
        // any exception will result in falling back to PNG
        ignored.printStackTrace();
      } finally {
        IOUtils.closeQuietly( in );
      }
      resource = SvgSupport.toPngName( resource );
    }

    InputStream in = null;
    try {
      in = getResourceInputStream( resource );
      return new Image[] { new Image( d, in ) };
    } catch ( Throwable ignored ) {
      // any exception will result in falling back to PNG
    } finally {
      IOUtils.closeQuietly( in );
    }
    return null;
  }

  /**
   * Retrieve file from original path.
   */
  private InputStream getResourceInputStream( String resource ) throws IOException {
    InputStream in = ( (KettleXulLoader) domContainer.getXulLoader() ).getOriginalResourceAsStream( resource );
    return in;
  }
}
