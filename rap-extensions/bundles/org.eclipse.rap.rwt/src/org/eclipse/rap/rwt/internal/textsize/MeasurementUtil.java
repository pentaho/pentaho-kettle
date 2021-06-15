/*******************************************************************************
 * Copyright (c) 2011, 2014 Frank Appel and others.
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

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;
import static org.eclipse.rap.rwt.internal.textsize.MeasurementOperator.METHOD_MEASURE_ITEMS;
import static org.eclipse.rap.rwt.internal.textsize.MeasurementOperator.PARAM_ITEMS;
import static org.eclipse.rap.rwt.internal.textsize.MeasurementOperator.TYPE;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.internal.graphics.FontUtil;


public class MeasurementUtil {

  private static final String ATTR_OPERATOR = MeasurementUtil.class.getName() + "#operator";

  public static void installMeasurementOperator( UISession uiSession ) {
    uiSession.setAttribute( ATTR_OPERATOR, new MeasurementOperator() );
  }

  public static void appendStartupTextSizeProbe( ProtocolMessageWriter writer ) {
    JsonArray startupProbes = getStartupProbes();
    if( !startupProbes.isEmpty() ) {
      JsonObject parameters = new JsonObject().add( PARAM_ITEMS, startupProbes );
      writer.appendCall( TYPE, METHOD_MEASURE_ITEMS, parameters );
    }
  }

  private static JsonArray getStartupProbes() {
    Probe[] probes = getApplicationContext().getProbeStore().getProbes();
    JsonArray result = new JsonArray();
    for( Probe probe : probes ) {
      result.add( createProbeParamObject( probe ) );
    }
    return result;
  }

  public static void renderMeasurementItems() {
    getMeasurementOperator().renderMeasurementItems();
  }

  static void addItemToMeasure( String toMeasure, Font font, int wrapWidth, int mode ) {
    FontData fontData = FontUtil.getData( font );
    MeasurementItem newItem = new MeasurementItem( toMeasure, fontData, wrapWidth, mode );
    getMeasurementOperator().addItemToMeasure( newItem );
  }

  public static MeasurementOperator getMeasurementOperator() {
    UISession uiSession = ContextProvider.getUISession();
    return ( MeasurementOperator )uiSession.getAttribute( ATTR_OPERATOR );
  }

  static JsonArray createItemParamObject( MeasurementItem item ) {
    FontData fontData = item.getFontData();
    JsonArray result = new JsonArray()
      .add( getId( item ) )
      .add( item.getTextToMeasure() )
      .add( createJsonArray( ProtocolUtil.parseFontName( fontData.getName() ) ) )
      .add( fontData.getHeight() )
      .add( ( fontData.getStyle() & SWT.BOLD ) != 0 )
      .add( ( fontData.getStyle() & SWT.ITALIC ) != 0 )
      .add( item.getWrapWidth() )
      .add( isMarkup( item.getMode() ) );
    return result;
  }

  static JsonArray createProbeParamObject( Probe probe ) {
    FontData fontData = probe.getFontData();
    JsonArray result = new JsonArray()
      .add( getId( probe ) )
      .add( probe.getText() )
      .add( createJsonArray( ProtocolUtil.parseFontName( fontData.getName() ) ) )
      .add( fontData.getHeight() )
      .add( ( fontData.getStyle() & SWT.BOLD ) != 0 )
      .add( ( fontData.getStyle() & SWT.ITALIC ) != 0 )
      .add( -1 )
      .add( true );
    return result;
  }

  static String getId( Probe probe ) {
    return getId( probe.getFontData() );
  }

  static String getId( MeasurementItem item ) {
    return "t" + Integer.toString( item.hashCode() );
  }

  static String getId( FontData fontData ) {
    return "p" + Integer.toString( fontData.hashCode() );
  }

  private static boolean isMarkup( int mode ) {
    return mode == TextSizeUtil.MARKUP_EXTENT;
  }

  private MeasurementUtil() {
    // prevent instance creation
  }

}
