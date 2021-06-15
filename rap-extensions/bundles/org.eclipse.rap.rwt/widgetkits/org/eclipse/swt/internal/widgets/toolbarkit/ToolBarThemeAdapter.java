/*******************************************************************************
 * Copyright (c) 2009, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.toolbarkit;

import org.eclipse.rap.rwt.internal.theme.Size;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher.Constraint;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;


public class ToolBarThemeAdapter extends ControlThemeAdapterImpl {

  @Override
  protected void configureMatcher( WidgetMatcher matcher ) {
    super.configureMatcher( matcher );
    matcher.addStyle( "FLAT", SWT.FLAT );
    matcher.addState( "first", createFirstItemMatcher() );
    matcher.addState( "last", createLastItemMatcher() );
    matcher.addState( "disabled", createDisabledItemMatcher() );
  }

  public BoxDimensions getItemBorder( ToolItem item ) {
    return getCssBorder( "ToolItem", item );
  }

  public BoxDimensions getItemPadding( ToolItem item ) {
    return getCssBoxDimensions( "ToolItem", "padding", item ).dimensions;
  }

  public BoxDimensions getToolBarPadding( Control control ) {
    return getCssBoxDimensions( "ToolBar", "padding", control ).dimensions;
  }

  public int getToolBarSpacing( Control control ) {
    return getCssDimension( "ToolBar", "spacing", control );
  }

  public int getItemSpacing( ToolItem item ) {
    return getCssDimension( "ToolItem", "spacing", item );
  }

  public int getSeparatorWidth( Control control ) {
    return getCssDimension( "ToolItem-Separator", "width", control );
  }

  public Size getDropDownImageSize( Control control ) {
    return getCssImageSize( "ToolItem-DropDownIcon", "background-image", control );
  }

  private static Constraint createFirstItemMatcher() {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        if( widget instanceof ToolItem ) {
          ToolItem item = ( ToolItem )widget;
          ToolBar toolBar = item.getParent();
          return toolBar.getItem( 0 ) == item;
        }
        return false;
      }
    };
  }

  private static Constraint createLastItemMatcher() {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        if( widget instanceof ToolItem ) {
          ToolItem item = ( ToolItem )widget;
          ToolBar toolBar = item.getParent();
          return toolBar.getItem( toolBar.getItemCount() - 1 ) == item;
        }
        return false;
      }
    };
  }

  private static Constraint createDisabledItemMatcher() {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        if( widget instanceof ToolItem ) {
          return !( ( ToolItem )widget ).getEnabled();
        }
        return false;
      }
    };
  }

}
