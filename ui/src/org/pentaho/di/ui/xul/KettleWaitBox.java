/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
