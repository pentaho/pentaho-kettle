/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer.InstanceCreator;
import org.eclipse.swt.graphics.FontData;


public class FontDataFactory {

  private final SharedInstanceBuffer<FontData, FontData> cache;
  private final InstanceCreator<FontData, FontData> instanceCreator;

  public FontDataFactory() {
    cache = new SharedInstanceBuffer<FontData, FontData>();
    instanceCreator = new InstanceCreator<FontData, FontData>() {
      public FontData createInstance( FontData fontData ) {
        return cloneFontData( fontData );
      }
    };
  }

  public FontData findFontData( FontData fontData ) {
    return cache.get( fontData, instanceCreator );
  }

  private static FontData cloneFontData( FontData fontData ) {
    String name = fontData.getName();
    int height = fontData.getHeight();
    int style = fontData.getStyle();
    return new FontData( name, height, style );
  }

}
