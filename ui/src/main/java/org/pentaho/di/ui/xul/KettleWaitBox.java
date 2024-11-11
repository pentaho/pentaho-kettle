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


package org.pentaho.di.ui.xul;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.tags.SwtWaitBox;

/**
 * Created by bmorrise on 2/26/16.
 */
public class KettleWaitBox extends SwtWaitBox {

  private XulDomContainer container;
  private String icon;

  public KettleWaitBox( Element self, XulComponent parent, XulDomContainer container, String tagName ) {
    super( self, parent, container, tagName );
    this.container = container;
  }

  @Override protected Shell createDialog() {

    Shell dialog = super.createDialog();

    if ( icon == null ) {
      return dialog;
    }

    Image[] images = KettleImageUtil.loadImages( container, dialog.getShell(), icon );

    if ( images != null ) {
      dialog.getShell().setImages( images );
    }

    return dialog;
  }

  public void setIcon( String icon ) {
    this.icon = icon;
  }
}
