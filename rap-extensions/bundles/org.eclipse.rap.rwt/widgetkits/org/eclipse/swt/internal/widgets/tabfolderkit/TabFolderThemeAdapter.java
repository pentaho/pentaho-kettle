/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.tabfolderkit;

import org.eclipse.rap.rwt.internal.theme.WidgetMatcher;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher.Constraint;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;


public class TabFolderThemeAdapter extends ControlThemeAdapterImpl {

  @Override
  protected void configureMatcher( WidgetMatcher matcher ) {
    super.configureMatcher( matcher );
    matcher.addState( "bottom", createItemOnBottomMatcher() );
    matcher.addState( "selected", createSelectedItemMatcher() );
    matcher.addState( "first", createFirstItemMatcher() );
    matcher.addState( "last", createLastItemMatcher() );
  }

  public BoxDimensions getContentContainerBorder( TabFolder folder ) {
    return getCssBorder( "TabFolder-ContentContainer", folder );
  }

  public BoxDimensions getItemBorder( TabItem item ) {
    return getCssBorder( "TabItem", item );
  }

  public BoxDimensions getItemPadding( TabItem item ) {
    return getCssBoxDimensions( "TabItem", "padding", item ).dimensions;
  }

  public BoxDimensions getItemMargin( TabItem item ) {
    return getCssBoxDimensions( "TabItem", "margin", item ).dimensions;
  }

  private static Constraint createItemOnBottomMatcher() {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        if( widget instanceof TabItem ) {
          TabItem item = ( TabItem )widget;
          return ( item.getParent().getStyle() & SWT.BOTTOM ) != 0;
        }
        return false;
      }
    };
  }

  private static Constraint createSelectedItemMatcher() {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        if( widget instanceof TabItem ) {
          TabItem item = ( TabItem )widget;
          TabItem[] selection = item.getParent().getSelection();
          return selection.length > 0 && selection[ 0 ] == item;
        }
        return false;
      }
    };
  }

  private static Constraint createFirstItemMatcher() {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        if( widget instanceof TabItem ) {
          TabItem item = ( TabItem )widget;
          TabFolder folder = item.getParent();
          return folder.getItem( 0 ) == item;
        }
        return false;
      }
    };
  }

  private static Constraint createLastItemMatcher() {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        if( widget instanceof TabItem ) {
          TabItem item = ( TabItem )widget;
          TabFolder folder = item.getParent();
          return folder.getItem( folder.getItemCount() - 1 ) == item;
        }
        return false;
      }
    };
  }

}
