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
package org.eclipse.swt.internal.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.internal.lifecycle.DisposedWidgets;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Widget;


public class WidgetRemoteAdapter implements RemoteAdapter, SerializableCompatibility {

  private final static Runnable[] EMPTY = new Runnable[ 0 ];
  private static final int DATA = 1;
  private static final int LISTENERS = 2;
  private static final int VARIANT = 3;

  private final String id;
  private Widget parent;
  private boolean initialized;
  private transient int preserved;
  private transient Map<String, Object> preservedValues;
  private transient long preservedListeners;
  private transient Runnable[] renderRunnables;
  private transient Object[] data;
  private transient String variant;

  public WidgetRemoteAdapter( String id ) {
    this.id = id;
    initialize();
  }

  void initialize() {
    preservedValues = new HashMap<>();
  }

  @Override
  public String getId() {
    return id;
  }

  public void setParent( Widget parent ) {
    this.parent = parent;
  }

  @Override
  public Widget getParent() {
    return parent;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized( boolean initialized ) {
    this.initialized = initialized;
  }

  @Override
  public void preserve( String propertyName, Object value ) {
    preservedValues.put( propertyName, value );
  }

  @Override
  public Object getPreserved( String propertyName ) {
    return preservedValues.get( propertyName );
  }

  public void preserveListeners( long eventList ) {
    markPreserved( LISTENERS );
    preservedListeners = eventList;
  }

  public boolean hasPreservedListeners() {
    return hasPreserved( LISTENERS );
  }

  public long getPreservedListeners() {
    return preservedListeners;
  }

  public void preserveData( Object[] data ) {
    markPreserved( DATA );
    this.data = data;
  }

  public boolean hasPreservedData() {
    return hasPreserved( DATA );
  }

  public Object[] getPreservedData() {
    return data;
  }

  public void preserveVariant( String variant ) {
    markPreserved( VARIANT );
    this.variant = variant;
  }

  public boolean hasPreservedVariant() {
    return hasPreserved( VARIANT );
  }

  public String getPreservedVariant() {
    return variant;
  }

  public void clearPreserved() {
    preserved = 0;
    preservedValues.clear();
    preservedListeners = 0;
    data = null;
    variant = null;
  }

  protected void markPreserved( int index ) {
    preserved |= ( 1 << index );
  }

  protected boolean hasPreserved( int index ) {
    return ( preserved & ( 1 << index ) ) != 0;
  }

  protected RemoteObject getRemoteObject() {
    return RemoteObjectFactory.getRemoteObject( getId() );
  }

  public void addRenderRunnable( Runnable renderRunnable ) {
    if( renderRunnables == null ) {
      renderRunnables = new Runnable[] { renderRunnable };
    } else {
      Runnable[] newRunnables = new Runnable[ renderRunnables.length + 1 ];
      System.arraycopy( renderRunnables, 0, newRunnables, 0, renderRunnables.length );
      newRunnables[ newRunnables.length - 1 ] = renderRunnable;
      renderRunnables = newRunnables;
    }
  }

  public Runnable[] getRenderRunnables() {
    return renderRunnables == null ? EMPTY : renderRunnables;
  }

  public void clearRenderRunnables() {
    renderRunnables = null;
  }

  @Override
  public void markDisposed( Widget widget ) {
    if( initialized ) {
      DisposedWidgets.add( widget );
    }
  }

  private Object readResolve() {
    initialize();
    return this;
  }

}
