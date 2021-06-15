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
package org.eclipse.swt.internal.widgets.canvaskit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.remote.JsonMapping.toJson;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.graphics.GCAdapter;
import org.eclipse.swt.internal.graphics.GCOperation;
import org.eclipse.swt.internal.graphics.GCOperation.DrawArc;
import org.eclipse.swt.internal.graphics.GCOperation.DrawImage;
import org.eclipse.swt.internal.graphics.GCOperation.DrawLine;
import org.eclipse.swt.internal.graphics.GCOperation.DrawPath;
import org.eclipse.swt.internal.graphics.GCOperation.DrawPoint;
import org.eclipse.swt.internal.graphics.GCOperation.DrawPolyline;
import org.eclipse.swt.internal.graphics.GCOperation.DrawRectangle;
import org.eclipse.swt.internal.graphics.GCOperation.DrawRoundRectangle;
import org.eclipse.swt.internal.graphics.GCOperation.DrawText;
import org.eclipse.swt.internal.graphics.GCOperation.FillGradientRectangle;
import org.eclipse.swt.internal.graphics.GCOperation.SetClipping;
import org.eclipse.swt.internal.graphics.GCOperation.SetProperty;
import org.eclipse.swt.internal.graphics.GCOperation.SetTransform;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;


final class GCOperationWriter {

  private final Control control;
  private boolean initialized;
  private JsonArray operations;
  private int lineWidth;
  private RGB foreground;
  private RGB background;

  GCOperationWriter( Control control ) {
    this.control = control;
  }

  void initialize() {
    if( !initialized ) {
      lineWidth = 1;
      foreground = control.getForeground().getRGB();
      background = control.getBackground().getRGB();
      Rectangle paintRect = getPaintRect();
      JsonObject parameters = new JsonObject()
        .add( "x", paintRect.x )
        .add( "y", paintRect.y )
        .add( "width", paintRect.width )
        .add( "height", paintRect.height )
        .add( "font", toJson( control.getFont() ) )
        .add( "fillStyle", toJson( background ) )
        .add( "strokeStyle", toJson( foreground ) );
      getRemoteObject( getGcId( control ) ).call( "init", parameters );
      operations = new JsonArray();
      initialized = true;
    }
  }

  void write( GCOperation operation ) {
    initialize();
    if( operation instanceof DrawLine ) {
      drawLine( ( DrawLine )operation );
    } else if( operation instanceof DrawPoint ) {
      drawPoint( ( DrawPoint )operation );
    } else if( operation instanceof DrawRoundRectangle ) {
      drawRoundRectangle( ( DrawRoundRectangle )operation );
    } else if( operation instanceof FillGradientRectangle ) {
      fillGradientRectangle( ( FillGradientRectangle )operation );
    } else if( operation instanceof DrawRectangle ) {
      drawRectangle( ( DrawRectangle )operation );
    } else if( operation instanceof DrawArc ) {
      drawArc( ( DrawArc )operation );
    } else if( operation instanceof DrawPolyline ) {
      drawPolyline( ( DrawPolyline )operation );
    } else if( operation instanceof DrawImage ) {
      drawImage( ( DrawImage )operation );
    } else if( operation instanceof DrawText ) {
      drawText( ( DrawText )operation );
    } else if( operation instanceof DrawPath ) {
      drawPath( ( DrawPath )operation );
    } else if( operation instanceof SetProperty ) {
      setProperty( ( SetProperty )operation );
    } else if( operation instanceof SetClipping ) {
      setClipping( ( SetClipping )operation );
    } else if( operation instanceof SetTransform ) {
      setTransform( ( SetTransform )operation );
    } else {
      String name = operation.getClass().getName();
      throw new IllegalArgumentException( "Unsupported GCOperation: " + name );
    }
  }

  void render() {
    if( operations != null ) {
      if( !operations.isEmpty() ) {
        JsonObject parameters = new JsonObject().add( "operations", operations );
        getRemoteObject( getGcId( control ) ).call( "draw", parameters );
      }
      operations = null;
    }
  }

  private void drawLine( DrawLine operation ) {
    float offset = getOffset( false );
    addClientOperation( "beginPath" );
    addClientOperation( "moveTo", operation.x1 + offset, operation.y1 + offset );
    addClientOperation( "lineTo", operation.x2 + offset, operation.y2 + offset );
    addClientOperation( "stroke" );
  }

  private void drawPoint( DrawPoint operation ) {
    float x = operation.x;
    float y = operation.y;
    addClientOperation( "save" );
    operations.add( new JsonArray()
      .add( "fillStyle" )
      .add( toJson( foreground ) ) );
    addClientOperation( "lineWidth", 1 );
    addClientOperation( "beginPath" );
    addClientOperation( "rect", x, y, 1, 1 );
    addClientOperation( "fill" );
    addClientOperation( "restore" );
  }

  private void drawRectangle( DrawRectangle operation ) {
    float offset = getOffset( operation.fill );
    float x = operation.x + offset;
    float y = operation.y + offset;
    float width = operation.width;
    float height = operation.height;
    addClientOperation( "beginPath" );
    addClientOperation( "rect", x, y, width, height );
    addClientOperation( operation.fill ? "fill" : "stroke" );
  }

  private void fillGradientRectangle( FillGradientRectangle operation )  {
    boolean vertical = operation.vertical;
    float width = operation.width;
    float height = operation.height;
    float x1 = operation.x;
    float y1 = operation.y;
    boolean swapColors = false;
    if( width < 0 ) {
      x1 += width;
      if( !vertical ) {
        swapColors  = true;
      }
    }
    if( height < 0 ) {
      y1 += height;
      if( vertical ) {
        swapColors = true;
      }
    }
    RGB startColor = swapColors ? background : foreground;
    RGB endColor = swapColors ? foreground : background ;
    float x2 = vertical ? x1 : x1 + Math.abs( width );
    float y2 = vertical ? y1 + Math.abs( height ) : y1;
    addClientOperation( "save" );
    addClientOperation( "createLinearGradient", x1, y1, x2, y2 );
    operations.add( new JsonArray()
      .add( "addColorStop" )
      .add( 0 )
      .add( toJson( startColor ) ) );
    operations.add( new JsonArray()
      .add( "addColorStop" )
      .add( 1 )
      .add( toJson( endColor ) ) );
    addClientOperation( "fillStyle", "linearGradient" );
    addClientOperation( "beginPath" );
    addClientOperation( "rect", x1, y1, width, height );
    addClientOperation( "fill" );
    addClientOperation( "restore" );
  }

  private void drawRoundRectangle( DrawRoundRectangle operation ) {
    // NOTE: the added "+1" in arcSize is the result of a visual comparison of RAP to SWT/Win.
    float offset = getOffset( operation.fill );
    float x = operation.x + offset;
    float y = operation.y + offset;
    float w = operation.width;
    float h = operation.height;
    float rx = ( ( float )operation.arcWidth ) / 2 + 1;
    float ry = ( ( float )operation.arcHeight ) / 2 + 1;
    addClientOperation( "beginPath" );
    addClientOperation( "moveTo", x, y + ry );
    addClientOperation( "lineTo", x, y + h - ry );
    addClientOperation( "quadraticCurveTo", x, y + h, x + rx, y + h );
    addClientOperation( "lineTo", x + w - rx, y + h );
    addClientOperation( "quadraticCurveTo", x + w, y + h, x + w, y + h - ry );
    addClientOperation( "lineTo", x + w, y + ry );
    addClientOperation( "quadraticCurveTo", x + w, y, x + w - rx, y );
    addClientOperation( "lineTo", x + rx, y );
    addClientOperation( "quadraticCurveTo", x, y, x, y + ry );
    addClientOperation( operation.fill ? "fill" : "stroke" );
  }

  private void drawArc( DrawArc operation ) {
    double factor = Math.PI / 180;
    float offset = getOffset( operation.fill );
    float rx = operation.width / 2;
    float ry = operation.height / 2;
    float cx = operation.x + rx + offset ;
    float cy = operation.y + ry + offset;
    float startAngle = round( operation.startAngle * factor * -1, 4 );
    float arcAngle = round( operation.arcAngle * factor * -1, 4 );
    addClientOperation( "save" );
    addClientOperation( "beginPath" );
    operations.add( new JsonArray()
      .add( "ellipse" )
      .add( cx )
      .add( cy )
      .add( rx )
      .add( ry )
      .add( 0 )
      .add( startAngle )
      .add( startAngle + arcAngle )
      .add( arcAngle < 0 )
    );
    if( operation.fill ) {
      addClientOperation( "lineTo", 0, 0 );
      addClientOperation( "closePath" );
    }
    addClientOperation( operation.fill ? "fill" : "stroke" );
    addClientOperation( "restore" );
  }

  private void drawPolyline( DrawPolyline operation ) {
    int[] points = operation.points;
    float offset = getOffset( operation.fill );
    addClientOperation( "beginPath" );
    for( int i = 0; i < points.length; i += 2 ) {
      if( i == 0 ) {
        addClientOperation( "moveTo", points[ i ] + offset, points[ i + 1 ] + offset );
      } else {
        addClientOperation( "lineTo", points[ i ] + offset, points[ i + 1 ] + offset );
      }
    }
    if( operation.close && points.length > 1 ) {
      addClientOperation( "lineTo", points[ 0 ] + offset, points[ 1 ] + offset );
    }
    addClientOperation( operation.fill ? "fill" : "stroke" );
  }

  private void drawImage( DrawImage operation ) {
    String path = ImageFactory.getImagePath( operation.image );
    if( operation.simple ) {
      addClientOperation( "drawImage", path, operation.destX, operation.destY );
    } else {
      addClientOperation(
        "drawImage",
        path,
        operation.srcX,
        operation.srcY,
        operation.srcWidth,
        operation.srcHeight,
        operation.destX,
        operation.destY,
        operation.destWidth,
        operation.destHeight
      );
    }

  }

  private void drawText( DrawText operation ) {
    boolean fill = ( operation.flags & SWT.DRAW_TRANSPARENT ) == 0;
    boolean drawMnemonic = ( operation.flags & SWT.DRAW_MNEMONIC ) != 0;
    boolean drawDelemiter = ( operation.flags & SWT.DRAW_DELIMITER ) != 0;
    boolean drawTab = ( operation.flags & SWT.DRAW_TAB ) != 0;
    operations.add( new JsonArray()
      .add( fill ? "fillText" : "strokeText" )
      .add( operation.text )
      .add( drawMnemonic )
      .add( drawDelemiter )
      .add( drawTab )
      .add( operation.x )
      .add( operation.y ) );
  }

  private void drawPath( DrawPath operation ) {
    renderPath( operation.types, operation.points );
    addClientOperation( operation.fill ? "fill" : "stroke" );
  }

  private void setProperty( SetProperty operation ) {
    String name;
    JsonValue value;
    switch( operation.id ) {
      case SetProperty.FOREGROUND:
        name = "strokeStyle";
        foreground = ( RGB )operation.value;
        value = toJson( foreground );
      break;
      case SetProperty.BACKGROUND:
        name = "fillStyle";
        background = ( RGB )operation.value;
        value = toJson( background );
      break;
      case SetProperty.ALPHA:
        float alpha = ( ( Integer )operation.value ).floatValue();
        float globalAlpha = round( alpha / 255, 2 );
        name = "globalAlpha";
        value = JsonValue.valueOf( globalAlpha );
      break;
      case SetProperty.LINE_WIDTH:
        name = "lineWidth";
        int width = ( ( Integer )operation.value ).intValue();
        width = width < 1 ? 1 : width;
        value = JsonValue.valueOf( width );
        lineWidth = width;
      break;
      case SetProperty.LINE_CAP:
        name = "lineCap";
        switch( ( ( Integer )operation.value ).intValue() ) {
          default:
          case SWT.CAP_FLAT:
            value = JsonValue.valueOf( "butt" );
          break;
          case SWT.CAP_ROUND:
            value = JsonValue.valueOf( "round" );
          break;
          case SWT.CAP_SQUARE:
            value = JsonValue.valueOf( "square" );
          break;
        }
      break;
      case SetProperty.LINE_JOIN:
        name = "lineJoin";
        switch( ( ( Integer )operation.value ).intValue() ) {
          default:
          case SWT.JOIN_BEVEL:
            value = JsonValue.valueOf( "bevel" );
            break;
          case SWT.JOIN_MITER:
            value = JsonValue.valueOf( "miter" );
            break;
          case SWT.JOIN_ROUND:
            value = JsonValue.valueOf( "round" );
            break;
        }
      break;
      case SetProperty.FONT:
        name = "font";
        value = toJson( ( FontData )operation.value );
      break;
      default:
        String msg = "Unsupported operation id: " + operation.id;
        throw new RuntimeException( msg );
    }
    operations.add( new JsonArray().add( name ).add( value ) );
  }

  private void setClipping( SetClipping operation ) {
    if( operation.isReset() ) {
      addClientOperation( "resetClip" );
    } else {
      addClientOperation( "save" );
      if( operation.isRectangular() ) {
        Rectangle rect = operation.rectangle;
        addClientOperation( "beginPath" );
        addClientOperation( "rect", rect.x, rect.y, rect.width, rect.height );
      } else {
        renderPath( operation.types, operation.points );
      }
      addClientOperation( "clip" );
    }
  }

  private void setTransform( SetTransform operation ) {
    addClientOperation( "setTransform", operation.elements );
  }

  private void renderPath( byte[] types, float[] points ) {
    addClientOperation( "beginPath" );
    for( int i = 0, j = 0; i < types.length; i++ ) {
      switch( types[ i ] ) {
        case SWT.PATH_MOVE_TO:
          addClientOperation( "moveTo", points[ j++ ], points[ j++ ] );
        break;
        case SWT.PATH_LINE_TO:
          addClientOperation( "lineTo", points[ j++ ], points[ j++ ] );
        break;
        case SWT.PATH_CUBIC_TO:
          addClientOperation( "bezierCurveTo",
                              points[ j++ ],
                              points[ j++ ],
                              points[ j++ ],
                              points[ j++ ],
                              points[ j++ ],
                              points[ j++ ] );
        break;
        case SWT.PATH_QUAD_TO:
          addClientOperation( "quadraticCurveTo",
                              points[ j++ ],
                              points[ j++ ],
                              points[ j++ ],
                              points[ j++ ] );
        break;
        case SWT.PATH_CLOSE:
          addClientOperation( "closePath" );
        break;
        default:
          String msg = "Unsupported point type: " + types[ i ];
          throw new RuntimeException( msg );
      }
    }
  }

  private void addClientOperation( String name, float... args ) {
    JsonArray operation = new JsonArray().add( name );
    for( int i = 0; i < args.length; i++ ) {
      operation.add( args[ i ] );
    }
    operations.add( operation );
  }

  private void addClientOperation( String name, String argText, float... args ) {
    JsonArray operation = new JsonArray().add( name ).add( argText );
    for( int i = 0; i < args.length; i++ ) {
      operation.add( args[ i ] );
    }
    operations.add( operation );
  }

  private float getOffset( boolean fill ) {
    float result = 0;
    if( !fill && lineWidth % 2 != 0 ) {
      result = ( float )0.5;
    }
    return result;
  }

  private Rectangle getPaintRect() {
    Rectangle paintRect = control.getAdapter( GCAdapter.class ).getPaintRect();
    if( paintRect == null ) {
      Point size = control.getSize();
      paintRect = new Rectangle( 0, 0, size.x, size.y );
    }
    return paintRect;
  }

  static float round( double value, int decimals ) {
    int factor = ( int )Math.pow( 10, decimals );
    return ( ( float )Math.round( factor * value ) ) / factor;
  }

  static String getGcId( Widget widget ) {
    return getId( widget ) + ".gc";
  }

}
