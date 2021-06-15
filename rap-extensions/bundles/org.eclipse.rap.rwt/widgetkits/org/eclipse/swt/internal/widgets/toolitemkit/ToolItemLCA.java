/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.toolitemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.IToolItemAdapter;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


public final class ToolItemLCA extends WidgetLCA<ToolItem> {

  public static final ToolItemLCA INSTANCE = new ToolItemLCA();

  private static final String TYPE = "rwt.widgets.ToolItem";
  private static final String[] ALLOWED_STYLES = {
    "PUSH", "CHECK", "RADIO", "SEPARATOR", "DROP_DOWN"
  };

  private static final String PROP_VISIBLE = "visible";
  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_HOT_IMAGE = "hotImage";
  private static final String PROP_CONTROL = "control";
  private static final String PROP_SELECTION = "selection";
  private static final String PROP_BADGE = "badge";

  @Override
  public void preserveValues( ToolItem item ) {
    WidgetLCAUtil.preserveBounds( item, item.getBounds() );
    WidgetLCAUtil.preserveEnabled( item, item.getEnabled() );
    WidgetLCAUtil.preserveToolTipText( item, item.getToolTipText() );
    preserveProperty( item, PROP_VISIBLE, isVisible( item ) );
    preserveProperty( item, PROP_TEXT, item.getText() );
    preserveProperty( item, PROP_IMAGE, getImage( item ) );
    preserveProperty( item, PROP_HOT_IMAGE, item.getHotImage() );
    preserveProperty( item, PROP_CONTROL, item.getControl() );
    preserveProperty( item, PROP_SELECTION, item.getSelection() );
    preserveProperty( item, PROP_BADGE, getBadge( item ) );
  }

  @Override
  public void renderInitialization( ToolItem item ) throws IOException {
    ToolBar toolBar = item.getParent();
    // TODO [tb] For the index, it is currently ignored that controls
    //           attached to a ToolItem use an index-slot of their own on
    //           the client, while they don't on the server. In theory,
    //           this could lead to an incorrect order of the items on the
    //           client, which is problematic with the keyboard-control
    //           and radio-groups.
    RemoteObject remoteObject = createRemoteObject( item, TYPE );
    remoteObject.setHandler( new ToolItemOperationHandler( item ) );
    remoteObject.set( "parent", getId( toolBar ) );
    remoteObject.set( "style", createJsonArray( getStyles( item, ALLOWED_STYLES ) ) );
    remoteObject.set( "index", toolBar.indexOf( item ) );
  }

  @Override
  public void renderChanges( ToolItem item ) throws IOException {
    WidgetLCAUtil.renderBounds( item, item.getBounds() );
    WidgetLCAUtil.renderEnabled( item, item.getEnabled() );
    WidgetLCAUtil.renderToolTip( item, item.getToolTipText() );
    WidgetLCAUtil.renderCustomVariant( item );
    WidgetLCAUtil.renderData( item );
    renderText( item );
    renderMnemonicIndex( item );
    renderProperty( item, PROP_VISIBLE, isVisible( item ), true );
    renderProperty( item, PROP_IMAGE, getImage( item ), null );
    renderProperty( item, PROP_HOT_IMAGE, item.getHotImage(), null );
    renderProperty( item, PROP_CONTROL, item.getControl(), null );
    renderProperty( item, PROP_SELECTION, item.getSelection(), false );
    renderProperty( item, PROP_BADGE, getBadge( item ), null );
    if( !isSeparator( item ) ) {
      renderListenSelection( item );
    }
  }

  private static void renderText( ToolItem item ) {
    String newValue = item.getText();
    if( hasChanged( item, PROP_TEXT, newValue, "" ) ) {
      String text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      getRemoteObject( item ).set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( ToolItem item ) {
    String text = item.getText();
    if( hasChanged( item, PROP_TEXT, text, "" ) ) {
      int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
      if( mnemonicIndex != -1 ) {
        getRemoteObject( item ).set( PROP_MNEMONIC_INDEX, mnemonicIndex );
      }
    }
  }

  private static boolean isVisible( ToolItem item ) {
    return item.getAdapter( IToolItemAdapter.class ).getVisible();
  }

  static Image getImage( ToolItem item ) {
    Image result;
    if( item.getEnabled() && item.getParent().getEnabled() ) {
      result = item.getImage();
    } else {
      result = item.getDisabledImage();
      if( result == null ) {
        result = item.getImage();
      }
    }
    return result;
  }

  private static boolean isSeparator( ToolItem item ) {
    return ( item.getStyle() & SWT.SEPARATOR ) != 0;
  }

  private static String getBadge( ToolItem item ) {
    return ( String )item.getData( RWT.BADGE );
  }

  private ToolItemLCA() {
    // prevent instantiation
  }

}
