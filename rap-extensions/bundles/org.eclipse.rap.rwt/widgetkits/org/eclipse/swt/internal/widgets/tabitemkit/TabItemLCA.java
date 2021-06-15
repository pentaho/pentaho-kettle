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
package org.eclipse.swt.internal.widgets.tabitemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


public class TabItemLCA extends WidgetLCA<TabItem> {

  public static final TabItemLCA INSTANCE = new TabItemLCA();

  private static final String TYPE = "rwt.widgets.TabItem";

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_CONTROL = "control";
  private static final String PROP_BADGE = "badge";

  @Override
  public void preserveValues( TabItem item ) {
    WidgetLCAUtil.preserveToolTipText( item, item.getToolTipText() );
    preserveProperty( item, PROP_TEXT, item.getText() );
    preserveProperty( item, PROP_IMAGE, item.getImage() );
    preserveProperty( item, PROP_CONTROL, item.getControl() );
    preserveProperty( item, PROP_BADGE, getBadge( item ) );
  }

  @Override
  public void readData( TabItem item ) {
  }

  @Override
  public void renderInitialization( TabItem item ) throws IOException {
    TabFolder parent = item.getParent();
    RemoteObject remoteObject = createRemoteObject( item, TYPE );
    // TODO [tb] : Do not render id!
    remoteObject.set( "id", WidgetUtil.getId( item ) );
    remoteObject.set( "parent", WidgetUtil.getId( parent ) );
    remoteObject.set( "index", parent.indexOf( item ) ) ;
  }

  @Override
  public void renderChanges( TabItem item ) throws IOException {
    WidgetLCAUtil.renderCustomVariant( item );
    WidgetLCAUtil.renderData( item );
    WidgetLCAUtil.renderToolTip( item, item.getToolTipText() );
    renderText( item );
    renderMnemonicIndex( item );
    renderProperty( item, PROP_IMAGE, item.getImage(), null );
    renderProperty( item, PROP_CONTROL, item.getControl(), null );
    renderProperty( item, PROP_BADGE, getBadge( item ), null );
  }

  private static void renderText( TabItem item ) {
    String newValue = item.getText();
    if( WidgetLCAUtil.hasChanged( item, PROP_TEXT, newValue, "" ) ) {
      String text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      getRemoteObject( item ).set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( TabItem item ) {
    String text = item.getText();
    if( WidgetLCAUtil.hasChanged( item, PROP_TEXT, text, "" ) ) {
      int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
      if( mnemonicIndex != -1 ) {
        getRemoteObject( item ).set( PROP_MNEMONIC_INDEX, mnemonicIndex );
      }
    }
  }

  private static String getBadge( TabItem item ) {
    return ( String )item.getData( RWT.BADGE );
  }

  private TabItemLCA() {
    // prevent instantiation
  }

}
