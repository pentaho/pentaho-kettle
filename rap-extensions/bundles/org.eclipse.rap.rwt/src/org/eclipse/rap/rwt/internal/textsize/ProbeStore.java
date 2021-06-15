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
package org.eclipse.rap.rwt.internal.textsize;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.FontData;


public class ProbeStore {
  private final Map<FontData,Probe> probes;
  private final TextSizeStorage textSizeStorage;

  public ProbeStore( TextSizeStorage textSizeStorage ) {
    this.textSizeStorage = textSizeStorage;
    probes = new HashMap<>();
  }

  Probe[] getProbes() {
    Probe[] result;
    synchronized( probes ) {
      if( probes.isEmpty() ) {
        FontData[] fontList = textSizeStorage.getFontList();
        for( int i = 0; i < fontList.length; i++ ) {
          createProbe( fontList[ i ] );
        }
      }
      result = new Probe[ probes.size() ];
      probes.values().toArray( result );
    }
    return result;
  }

  Probe createProbe( FontData fontData ) {
    Probe result = new Probe( fontData );
    synchronized( probes ) {
      probes.put( fontData, result );
    }
    textSizeStorage.storeFont( fontData );
    return result;
  }

  Probe getProbe( FontData font ) {
    synchronized( probes ) {
      return probes.get( font );
    }
  }

  int getSize() {
    synchronized( probes ) {
      return probes.size();
    }
  }

}
