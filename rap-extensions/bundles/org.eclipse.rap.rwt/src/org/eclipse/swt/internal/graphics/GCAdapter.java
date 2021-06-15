/*******************************************************************************
 * Copyright (c) 2010, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.graphics.GCOperation.SetProperty;

public final class GCAdapter {

  private final List<GCOperation> gcOperations;
  private boolean forceRedraw;
  private Rectangle paintRect;

  public GCAdapter() {
    gcOperations = new LinkedList<>();
  }

  public void addGCOperation( GCOperation operation ) {
    gcOperations.add( operation );
  }

  public GCOperation[] getGCOperations() {
    GCOperation[] result = new GCOperation[ gcOperations.size() ];
    gcOperations.toArray( result );
    return result;
  }

  public void clearGCOperations() {
    gcOperations.clear();
  }

  public GCOperation[] getTrimmedGCOperations() {
    int counter = 0;
    boolean stop = false;
    GCOperation[] operations = getGCOperations();
    for( int i = operations.length - 1; i >= 0 && !stop; i-- ) {
      if( isDrawOperation( operations[ i ] ) ) {
        stop = true;
      } else {
        counter++;
      }
    }
    GCOperation[] result = new GCOperation[ operations.length - counter ];
    System.arraycopy( operations, 0, result, 0, result.length );
    return result;
  }

  public void setForceRedraw( boolean forceRedraw ) {
    this.forceRedraw = forceRedraw;
  }

  public boolean getForceRedraw() {
    return forceRedraw;
  }

  public void setPaintRect( Rectangle paintRect ) {
    this.paintRect = paintRect;
  }

  public Rectangle getPaintRect() {
    return paintRect;
  }

  private static boolean isDrawOperation( GCOperation operation ) {
    return !( operation instanceof SetProperty );
  }

}