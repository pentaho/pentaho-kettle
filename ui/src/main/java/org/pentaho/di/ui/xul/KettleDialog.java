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

package org.pentaho.di.ui.xul;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.containers.XulRoot;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

import java.util.HashMap;
import java.util.Map;

public class KettleDialog extends SwtDialog {
  private final Map<String, Image[]> imagesCache = new HashMap<>();

  public KettleDialog( Element self, XulComponent parent, XulDomContainer container, String tagName ) {
    super( self, parent, container, tagName );
  }

  @Override protected Shell getParentShell( XulComponent parent ) {
    if ( parent == null && Spoon.getInstance() != null ) {
      return Spoon.getInstance().getShell();
    }
    return super.getParentShell( parent );
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
      images = KettleImageUtil.loadImages( domContainer, dialog.getShell(), icon );
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
}
