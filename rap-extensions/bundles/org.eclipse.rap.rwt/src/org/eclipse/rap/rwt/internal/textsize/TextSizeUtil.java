/*******************************************************************************
 * Copyright (c) 2007, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;


import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.util.EncodingUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.graphics.FontUtil;


public class TextSizeUtil {

  static final int STRING_EXTENT = 0;
  static final int TEXT_EXTENT = 1;
  static final int MARKUP_EXTENT = 2;

  public static Point stringExtent( Font font, String string, boolean markup ) {
    if( markup ) {
      return determineTextSize( font, string, SWT.DEFAULT, MARKUP_EXTENT );
    }
    return stringExtent( font, string );
  }

  public static Point stringExtent( Font font, String string ) {
    if( isEmptyString( string ) ) {
      return createSizeForEmptyString( font );
    }
    return determineTextSize( font, string, SWT.DEFAULT, STRING_EXTENT );
  }

  public static Point textExtent( Font font, String text, int wrapWidth, boolean markup ) {
    if( markup ) {
      return determineTextSize( font, text, wrapWidth, MARKUP_EXTENT );
    }
    return textExtent( font, text, wrapWidth );
  }

  public static Point textExtent( Font font, String text, int wrapWidth ) {
    return determineTextSize( font, text, wrapWidth, TEXT_EXTENT );
  }

  public static int getCharHeight( Font font ) {
    int result;
    if( containsProbeResult( font ) ) {
      result = lookupCharHeight( font );
    } else {
      result = estimateCharHeight( font );
      addProbeToMeasure( font );
    }
    return result;
  }

  public static float getAvgCharWidth( Font font ) {
    float result;
    if( containsProbeResult( font ) ) {
      result = lookupAvgCharWidth( font );
    } else {
      result = estimateAvgCharWidth( font );
      addProbeToMeasure( font );
    }
    return result;
  }

  public static boolean isTemporaryResize() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    Object attribute = serviceStore.getAttribute( TextSizeRecalculation.TEMPORARY_RESIZE );
    return Boolean.TRUE.equals( attribute );
  }

  //////////////////
  // Helping methods

  private static Point createSizeForEmptyString( Font font ) {
    return new Point( 0, getCharHeight( font ) );
  }

  private static boolean isEmptyString( String string ) {
    return string.length() == 0;
  }

  private static Point determineTextSize( Font font, String string, int wrapWidth, int mode ) {
    int normalizedWrapWidth = normalizeWrapWidth( wrapWidth );
    Point result = lookup( font, string, normalizedWrapWidth, mode );
    if( result == null ) {
      result = estimate( font, string, normalizedWrapWidth, mode );
      if( !isTemporaryResize() ) {
        addItemToMeasure( font, string, normalizedWrapWidth, mode );
      }
    }

    // TODO [rst] Still returns wrong result for texts that contain only
    //            whitespace (and possibly more that one line)
    if( isHeightZero( result ) ) {
      result = adjustHeightForWhitespaceTexts( font, result );
    }
    return result;
  }

  private static int normalizeWrapWidth( int wrapWidth ) {
    return wrapWidth <= 0 ? SWT.DEFAULT : wrapWidth;
  }

  private static boolean isHeightZero( Point result ) {
    return result.y == 0;
  }

  private static Point lookup( Font font, String string, int wrapWidth, int mode ) {
    String measurementString = createMeasurementString( string, mode );
    FontData fontData = FontUtil.getData( font );
    return TextSizeStorageUtil.lookup( fontData, measurementString, wrapWidth, mode );
  }

  private static Point estimate( Font font, String string, int wrapWidth, int mode ) {
    Point result;
    switch( mode ) {
      case STRING_EXTENT: {
        result = TextSizeEstimation.stringExtent( font, string );
      }
      break;
      case TEXT_EXTENT: {
        result = TextSizeEstimation.textExtent( font, string, wrapWidth );
      }
      break;
      case MARKUP_EXTENT: {
        result = TextSizeEstimation.markupExtent( font, string, wrapWidth );
      }
      break;
      default: {
        throw new IllegalStateException( "Unknown estimation mode." );
      }
    }
    return result;
  }

  private static void addItemToMeasure( Font font, String string, int wrapWidth, int mode ) {
    String measurementString = createMeasurementString( string, mode );
    MeasurementUtil.addItemToMeasure( measurementString, font, wrapWidth, mode );
  }

  private static String createMeasurementString( String string, int mode ) {
    return mode == STRING_EXTENT ? EncodingUtil.replaceNewLines( string, " " ) : string;
  }

  private static Point adjustHeightForWhitespaceTexts( Font font, Point result ) {
    return new Point( result.x, getCharHeight( font ) );
  }

  private static void addProbeToMeasure( Font font ) {
    MeasurementUtil.getMeasurementOperator().addProbeToMeasure( FontUtil.getData( font ) );
  }

  private static int estimateCharHeight( Font font ) {
    return TextSizeEstimation.getCharHeight( font );
  }

  private static int lookupCharHeight( Font font ) {
    return getProbeResult( font ).getSize().y;
  }

  private static boolean containsProbeResult( Font font ) {
    return ProbeResultStore.getInstance().containsProbeResult( FontUtil.getData( font ) );
  }

  private static float estimateAvgCharWidth( Font font ) {
    return TextSizeEstimation.getAvgCharWidth( font );
  }

  private static float lookupAvgCharWidth( Font font ) {
    return getProbeResult( font ).getAvgCharWidth();
  }

  private static ProbeResult getProbeResult( Font font ) {
    FontData data = FontUtil.getData( font );
    return ProbeResultStore.getInstance().getProbeResult( data );
  }

  private TextSizeUtil() {
    // prevent instance creation
  }
}
