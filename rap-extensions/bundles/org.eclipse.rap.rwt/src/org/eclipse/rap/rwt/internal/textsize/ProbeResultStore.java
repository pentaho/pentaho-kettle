/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.SerializableCompatibility;


final class ProbeResultStore implements SerializableCompatibility  {

  private final Map<FontData,ProbeResult> probeResults;

  static ProbeResultStore getInstance() {
    return SingletonUtil.getSessionInstance( ProbeResultStore.class );
  }

  ProbeResultStore() {
    probeResults = new HashMap<>();
  }

  ProbeResult createProbeResult( Probe probe, Point size ) {
    ProbeResult result = new ProbeResult( probe, size );
    probeResults.put( probe.getFontData(), result );
    return result;
  }

  ProbeResult getProbeResult( FontData fontData ) {
    return probeResults.get( fontData );
  }

  boolean containsProbeResult( FontData fontData ) {
    return getProbeResult( fontData ) != null;
  }

}
