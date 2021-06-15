/*******************************************************************************
 * Copyright (c) 2011, 2012 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import java.math.BigDecimal;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.SerializableCompatibility;


class ProbeResult implements SerializableCompatibility {

  private final Point size;
  private final Probe probe;
  private transient float avgCharWidth;

  ProbeResult( Probe probe, Point size ) {
    this.probe = probe;
    this.size = size;
  }

  Probe getProbe() {
    return probe;
  }

  Point getSize() {
    return size;
  }

  float getAvgCharWidth() {
    if( avgCharWidth == 0 ) {
      BigDecimal width = new BigDecimal( getSize().x );
      BigDecimal charCount = new BigDecimal( probe.getText().length() );
      avgCharWidth = width.divide( charCount, 2, BigDecimal.ROUND_HALF_UP ).floatValue();
    }
    return avgCharWidth;
  }
}
