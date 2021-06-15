/*******************************************************************************
 * Copyright (c) 2009, 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;


public class Decorator extends Widget {
  private static final String KEY_DECORATORS = Widget.class.getName() + "#decorators";
  private static final Decorator[] EMPTY_DECORATORS = new Decorator[ 0 ];

  public static Decorator[] getDecorators( Widget widget ) {
    Decorator[] result = EMPTY_DECORATORS;
    List<Decorator> decorationsList = getDecoratorsList( widget );
    if( decorationsList != null ) {
      result = decorationsList.toArray( new Decorator[ decorationsList.size() ] );
    }
    return result;
  }

  private Widget decoratedWidget;
  private DisposeListener disposeListener;
  
  public Decorator( Widget widget, int style ) {
    super( widget, style );
    this.decoratedWidget = widget;
    registerDisposeListeners();
    bindDecoration();
  }

  protected final Widget getDecoratedWidget() {
    return decoratedWidget;
  }
  
  //////////////////
  // Helping methods

  private void registerDisposeListeners() {
    disposeListener = new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        release();
      }
    };
    addDisposeListener( disposeListener );
    this.decoratedWidget.addDisposeListener( disposeListener );
  }
  
  private void release() {
    if( decoratedWidget != null && !decoratedWidget.isDisposed() ) {
      removeDisposeListener( disposeListener );
      decoratedWidget.removeDisposeListener( disposeListener );
      disposeListener = null;
      unbindDecoration();
      decoratedWidget = null;
      dispose();
    }
  }
  
  private void bindDecoration() {
    List<Decorator> decorations = getDecoratorsList( decoratedWidget );
    if( decorations == null ) {
      decorations = new ArrayList<Decorator>();
    }
    decorations.add( this );
    setDecoratorsList( decorations );
  }

  private void unbindDecoration() {
    List<Decorator> decorations = getDecoratorsList( decoratedWidget );
    if( decorations != null ) {
      decorations.remove( this );
      if( decorations.size() == 0 ) {
        decorations = null;
      }
      setDecoratorsList( decorations );
    }
  }

  private void setDecoratorsList( List<Decorator> decorations ) {
    decoratedWidget.setData( KEY_DECORATORS, decorations );
  }

  @SuppressWarnings("unchecked")
  private static List<Decorator> getDecoratorsList( Widget widget ) {
    return ( List<Decorator> )widget.getData( KEY_DECORATORS );
  }
}
