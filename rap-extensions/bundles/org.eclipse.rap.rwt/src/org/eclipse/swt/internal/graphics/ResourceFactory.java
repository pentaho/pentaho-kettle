/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer.InstanceCreator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;


public class ResourceFactory {

  private final SharedInstanceBuffer<Integer, Color> colors;
  private final SharedInstanceBuffer<FontData, Font> fonts;
  private final SharedInstanceBuffer<Integer, Cursor> cursors;
  private InstanceCreator<Integer, Color> colorCreator;
  private InstanceCreator<Integer, Cursor> cursorCreator;
  private InstanceCreator<FontData, Font> fontCreator;

  public ResourceFactory() {
    colors = new SharedInstanceBuffer<>();
    fonts = new SharedInstanceBuffer<>();
    cursors = new SharedInstanceBuffer<>();
    colorCreator = new InstanceCreator<Integer, Color>() {
      @Override
      public Color createInstance( Integer value ) {
        return createColorInstance( value.intValue() );
      }
    };
    cursorCreator = new InstanceCreator<Integer, Cursor>() {
      @Override
      public Cursor createInstance( Integer style ) {
        return createCursorInstance( style.intValue() );
      }
    };
    fontCreator = new InstanceCreator<FontData, Font>() {
      @Override
      public Font createInstance( FontData fontData ) {
        return createFontInstance( fontData );
      }
    };
  }

  public Color getColor( int red, int green, int blue ) {
    return getColor( red, green, blue, 255 );
  }

  public Color getColor( int red, int green, int blue, int alpha ) {
    int colorNr = ColorUtil.computeColorNr( red, green, blue, alpha );
    return colors.get( Integer.valueOf( colorNr ), colorCreator );
  }

  public Font getFont( FontData fontData ) {
    return fonts.get( fontData, fontCreator );
  }

  public Cursor getCursor( int style ) {
    return cursors.get( Integer.valueOf( style ), cursorCreator );
  }

  private static Color createColorInstance( int colorNr ) {
    Class<?>[] paramTypes = new Class[] { int.class };
    Object[] paramValues = new Object[] { Integer.valueOf( colorNr ) };
    return ClassUtil.newInstance( Color.class, paramTypes, paramValues );
  }

  private static Font createFontInstance( FontData fontData ) {
    Class<?>[] paramTypes = new Class[] { FontData.class };
    Object[] paramValues = new Object[] { fontData };
    return ClassUtil.newInstance( Font.class, paramTypes, paramValues );
  }

  private static Cursor createCursorInstance( int style ) {
    Class<?>[] paramTypes = new Class[] { int.class };
    Object[] paramValues = new Object[] { Integer.valueOf( style ) };
    return ClassUtil.newInstance( Cursor.class, paramTypes, paramValues );
  }

}
