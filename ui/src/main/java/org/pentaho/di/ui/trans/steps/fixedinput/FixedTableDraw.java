/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.fixedinput;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.steps.fixedinput.FixedFileInputField;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

/**
 * Widget to draw the character of a fixed length text-file in a graphical way.
 *
 * @author Matt
 * @since 17-04-2004
 */
public class FixedTableDraw extends Canvas {
  private Display display;
  private Color bg;
  // private Font font;
  private Color black, red, blue, lgray;
  private Point offset;
  private ScrollBar hori;
  private ScrollBar vert;
  private Image dummy_image;
  private GC dummy_gc;
  private int maxlen;

  private int fontheight;
  private int fontwidth;

  private Image cache_image;
  private int prev_fromx, prev_tox, prev_fromy, prev_toy;

  private List<FixedFileInputField> fields;

  private List<String> rows;

  private static final int LEFT = 50;
  private static final int TOP = 30;
  private static final int MARGIN = 10;

  private int potential_click;

  private WizardPage wPage;
  private String prevfieldname;

  public FixedTableDraw( Composite parent, PropsUI props, WizardPage wPage, List<FixedFileInputField> fields ) {
    super( parent, SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.V_SCROLL );
    this.wPage = wPage;
    this.fields = fields;

    prevfieldname = "";

    potential_click = -1;

    // Cache displayed text...
    cache_image = null;
    prev_fromx = -1;
    prev_tox = -1;
    prev_fromy = -1;
    prev_toy = -1;

    display = parent.getDisplay();
    bg = GUIResource.getInstance().getColorBackground();
    // font = GUIResource.getInstance().getFontGrid();
    fontheight = props.getGridFont().getHeight();

    black = GUIResource.getInstance().getColorBlack();
    red = GUIResource.getInstance().getColorRed();
    blue = GUIResource.getInstance().getColorBlue();
    lgray = GUIResource.getInstance().getColorLightGray();

    hori = getHorizontalBar();
    vert = getVerticalBar();

    // Determine font width...
    dummy_image = new Image( display, 1, 1 );
    dummy_gc = new GC( dummy_image );
    // dummy_gc.setFont(font);
    String teststring = "ABCDEF";
    if ( Const.isRunningOnWebspoonMode() ) {
      try {
        Class textSizeUtil = Class.forName( "org.eclipse.rap.rwt.internal.textsize.TextSizeUtil" );
        Method textExtent = textSizeUtil.getDeclaredMethod( "textExtent", Font.class, String.class, int.class );
        fontwidth =  Math.round( ( (Point) textExtent.invoke( null, getFont(), teststring, 0 ) ).x / teststring.length() );
      } catch ( ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
        e.printStackTrace();
      }
    } else {
      fontwidth = Math.round( dummy_gc.textExtent( teststring ).x / teststring.length() );
    }

    setBackground( bg );
    // setFont(font);

    addPaintListener( new PaintListener() {
      public void paintControl( PaintEvent e ) {
        FixedTableDraw.this.paintControl( e );
      }
    } );

    if ( !Const.isRunningOnWebspoonMode() ) {
      addDisposeListener( new DisposeListener() {
        public void widgetDisposed( DisposeEvent arg0 ) {
          dummy_gc.dispose();
          dummy_image.dispose();

          if ( cache_image != null ) {
            cache_image.dispose();
          }
        }
      } );
    }


    hori.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        redraw();
      }
    } );
    vert.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        redraw();
      }
    } );
    hori.setThumb( 100 );
    vert.setThumb( 100 );

    // Mouse events!

    addMouseListener( new MouseAdapter() {
      public void mouseDown( MouseEvent e ) {
        Point offset = getOffset();
        int posx = (int) Math.round( (double) ( e.x - LEFT - MARGIN - offset.x ) / ( (double) fontwidth ) );

        if ( posx > 0 ) {
          potential_click = posx;

          redraw();
        }
      }

      public void mouseUp( MouseEvent e ) {
        if ( potential_click > 0 ) {
          setMarker( potential_click ); // clear or set!
          potential_click = -1;

          redraw();
        }
      }
    } );

    addMouseMoveListener( new MouseMoveListener() {
      public void mouseMove( MouseEvent e ) {
        if ( e == null || offset == null || fontwidth == 0 ) {
          return;
        }

        int posx = (int) Math.round( (double) ( e.x - LEFT - MARGIN - offset.x ) / ( (double) fontwidth ) );

        // Clicked and mouse is down: move marker to a new location...
        if ( potential_click >= 0 ) {
          if ( posx > 0 ) {
            potential_click = posx;
            redraw();
          }
        }

        FixedFileInputField field = getFieldOnPosition( posx );
        if ( field != null && !field.getName().equalsIgnoreCase( prevfieldname ) ) {
          setToolTipText( field.getName()
            + " is a " + ValueMetaFactory.getValueMetaName( field.getType() )
            + ".  The width is " + field.getWidth() );
          prevfieldname = field.getName();
        }
      }
    } );
  }

  private FixedFileInputField getFieldOnPosition( int x ) {
    int position = 0;
    for ( int i = 0; i < fields.size(); i++ ) {
      FixedFileInputField field = fields.get( i );
      int width = field.getWidth();
      if ( position <= x && position + width > x ) {
        return field;
      }
      position += width;
    }
    return null;
  }

  private int getFieldPosition( FixedFileInputField look ) {
    int position = 0;
    for ( FixedFileInputField field : fields ) {
      if ( field.equals( look ) ) {
        // Same name:
        return position;
      }
      int width = field.getWidth();
      position += width;
    }
    return -1;
  }

  private void setMarker( int x ) {
    int idx = -1;
    int highest_smaller = -1;

    int position = 0;
    for ( int i = 0; i < fields.size() && idx < 0; i++ ) {
      FixedFileInputField field = fields.get( i );

      int width = field.getWidth();

      if ( position == potential_click ) {
        idx = i;
      }
      if ( highest_smaller < 0 && position + width >= x ) {
        highest_smaller = i; // The first time this occurs is OK.
      }

      position += width;
    }

    // OK, so we need to add a new field on the location of the previous one
    // Actually, we make the previous field shorter
    //
    // Field 1: pos=0, length=100
    //
    // becomes
    //
    // Field1: pos=0, length=50
    // Field2: pos=51, length=50
    //
    // We know the number of the new field : lowest_larger.
    //
    // Note: We should always have one field @ position 0, length max
    //
    if ( idx < 0 ) { // Position is not yet in the list, the field is not deleted, but added
      if ( highest_smaller >= 0 ) {
        // OK, let's add a new field, but split the length of the previous field
        // We want to keep this list sorted, so add at position lowest_larger.
        // We want to change the previous entry and add another after it.
        FixedFileInputField prevfield = fields.get( highest_smaller );
        int prevPosition = getFieldPosition( prevfield );
        int newlength = prevfield.getWidth() - ( x - prevPosition );
        FixedFileInputField field = new FixedFileInputField();
        field.setName( getNewFieldname() );
        field.setWidth( newlength );
        fields.add( highest_smaller + 1, field );

        // Don't forget to make the previous field shorter
        prevfield.setWidth( x - prevPosition );
      }
    } else {
      if ( highest_smaller >= 0 ) {
        // Now we need to remove the field with the same starting position
        // The previous field need to receive extra length
        //
        FixedFileInputField prevfield = fields.get( highest_smaller );
        FixedFileInputField field = fields.get( idx );
        prevfield.setWidth( prevfield.getWidth() + field.getWidth() );
        // Remove the field
        fields.remove( idx );
      }
    }

    // Something has changed: change wizard page...
    wPage.setPageComplete( wPage.canFlipToNextPage() );
  }

  // Loop from 1 to ... and see if we have an empty Fieldnr available.
  private String getNewFieldname() {
    int nr = 1;
    String name = "Field" + nr;
    while ( fieldExists( name ) ) {
      nr++;
      name = "Field" + nr;
    }
    return name;
  }

  private boolean fieldExists( String name ) {
    for ( FixedFileInputField field : fields ) {
      if ( name.equalsIgnoreCase( field.getName() ) ) {
        return true;
      }
    }
    return false;
  }

  public void setRows( List<String> rows ) {
    this.rows = rows;
    maxlen = getMaxLength();
    redraw();
  }

  // Draw the widget.
  public void paintControl( PaintEvent e ) {
    offset = getOffset();
    if ( offset == null ) {
      return;
    }

    Point area = getArea();
    Point max = getMaximum();
    Point thumb = getThumb( area, max );

    hori.setThumb( thumb.x );
    vert.setThumb( thumb.y );

    // From where do we need to draw?
    int fromy = -offset.y / ( fontheight + 2 );
    int toy = fromy + ( area.y / ( fontheight + 2 ) );
    int fromx = -offset.x / fontwidth;
    int tox = fromx + ( area.x / fontwidth );

    Image image = null;
    GC gc = null;

    if ( fromx != prev_fromx || fromy != prev_fromy || tox != prev_tox || toy != prev_toy ) {
      if ( Const.isRunningOnWebspoonMode() ) {
        // Directly draw on the canvas
        gc = new GC( this );
      } else {
        if ( cache_image != null ) {
          cache_image.dispose();
          cache_image = null;
        }
        cache_image = new Image( display, area.x, area.y );
        gc = new GC( cache_image );
      }

      // We have a cached image: draw onto it!
      int linepos = TOP - 5;

      gc.setBackground( bg );
      gc.fillRectangle( LEFT, TOP, area.x, area.y );

      // We draw in black...
      gc.setForeground( black );
      // gc.setFont(font);

      // Draw the text
      //
      for ( int i = fromy; i < rows.size() && i < toy; i++ ) {
        String str = rows.get( i );
        for ( int p = fromx; p < str.length() && p < tox; p++ ) {
          gc.drawText( "" + str.charAt( p ), LEFT + MARGIN + p * fontwidth + offset.x, TOP
            + i * ( fontheight + 2 ) + offset.y, true );
        }

        if ( str.length() < tox ) {
          gc.setForeground( red );
          gc.setBackground( red );
          int x_oval = LEFT + MARGIN + str.length() * fontwidth + offset.x;
          int y_oval = TOP + i * ( fontheight + 2 ) + offset.y;
          gc.drawOval( x_oval, y_oval, fontwidth, fontheight );
          gc.fillOval( x_oval, y_oval, fontwidth, fontheight );
          gc.setForeground( black );
          gc.setBackground( bg );
        }
      }

      // Draw the rulers
      // HORIZONTAL
      gc.setBackground( lgray );
      gc.fillRectangle( LEFT + MARGIN, 0, area.x, linepos + 1 );
      gc.setBackground( bg );

      gc.drawLine( LEFT + MARGIN, linepos, area.x, linepos );

      // Little tabs, small ones, every 5 big one, every 10 the number above...
      for ( int i = fromx; i < maxlen + 10 && i < tox + 10; i++ ) {
        String number = "" + i;
        int numsize = number.length() * fontwidth;

        if ( i > 0 && ( i % 10 ) == 0 ) {
          gc.drawText(
            "" + i, LEFT + MARGIN + i * fontwidth - numsize / 2 + offset.x, linepos - 10 - fontheight, true );
        }

        if ( i > 0 && ( i % 5 ) == 0 ) {
          gc.drawLine(
            LEFT + MARGIN + i * fontwidth + offset.x, linepos, LEFT + MARGIN + i * fontwidth + offset.x,
            linepos - 5 );
        } else {
          gc.drawLine(
            LEFT + MARGIN + i * fontwidth + offset.x, linepos, LEFT + MARGIN + i * fontwidth + offset.x,
            linepos - 3 );
        }
      }

      // VERTICIAL
      gc.setBackground( lgray );
      gc.fillRectangle( 0, TOP, LEFT, area.y );

      gc.drawLine( LEFT, TOP, LEFT, area.y );

      for ( int i = fromy; i < rows.size() && i < toy; i++ ) {
        String number = "" + ( i + 1 );
        int numsize = number.length() * fontwidth;
        gc.drawText( number, LEFT - 5 - numsize, TOP + i * ( fontheight + 2 ) + offset.y, true );
        gc.drawLine( LEFT, TOP + ( i + 1 ) * ( fontheight + 2 ) + offset.y, LEFT - 5, TOP
          + ( i + 1 ) * ( fontheight + 2 ) + offset.y );
      }

      if ( !Const.isRunningOnWebspoonMode() ) {
        gc.dispose();
      }
    }

    if ( !Const.isRunningOnWebspoonMode() ) {
      image = new Image( display, area.x, area.y );
      gc = new GC( image );
      // Draw the cached image onto the canvas image:
      gc.drawImage( cache_image, 0, 0 );
    }

    // Also draw the markers...
    gc.setForeground( red );
    gc.setBackground( red );
    int position = 0;
    for ( FixedFileInputField field : fields ) {
      int x = position;
      if ( x >= fromx && x <= tox ) {
        drawMarker( gc, x, area.y );
      }
      position += field.getWidth();
    }
    if ( potential_click >= 0 ) {
      gc.setForeground( blue );
      gc.setBackground( blue );
      drawMarker( gc, potential_click, area.y );
    }

    if ( !Const.isRunningOnWebspoonMode() ) {
      // Draw the image:
      e.gc.drawImage( image, 0, 0 );
      image.dispose();
    }
    gc.dispose();
  }

  private void drawMarker( GC gc, int x, int maxy ) {
    int[] triangle =
      new int[] {
        LEFT + MARGIN + x * fontwidth + offset.x, TOP - 4, LEFT + MARGIN + x * fontwidth + offset.x + 3,
        TOP + 1, LEFT + MARGIN + x * fontwidth + offset.x - 3, TOP + 1 };
    gc.fillPolygon( triangle );
    gc.drawPolygon( triangle );
    gc
      .drawLine(
        LEFT + MARGIN + x * fontwidth + offset.x, TOP + 1, LEFT + MARGIN + x * fontwidth + offset.x, maxy );
  }

  private Point getOffset() {
    Point area = getArea();
    Point max = getMaximum();
    Point thumb = getThumb( area, max );
    Point offset = getOffset( thumb, area );

    return offset;
  }

  private Point getThumb( Point area, Point max ) {
    Point thumb = new Point( 0, 0 );
    if ( max.x <= area.x ) {
      thumb.x = 100;
    } else {
      thumb.x = Math.round( 100 * area.x / max.x );
    }
    if ( max.y <= area.y ) {
      thumb.y = 100;
    } else {
      thumb.y = Math.round( 100 * area.y / max.y );
    }

    return thumb;
  }

  private Point getOffset( Point thumb, Point area ) {
    Point p = new Point( 0, 0 );
    Point sel = new Point( hori.getSelection(), vert.getSelection() );

    if ( thumb.x == 0 || thumb.y == 0 ) {
      return p;
    }

    p.x = Math.round( -sel.x * area.x / thumb.x );
    p.y = Math.round( -sel.y * area.y / thumb.y );

    return p;
  }

  private Point getMaximum() {
    int maxx = 0;
    int maxy = ( rows.size() + 10 ) * ( fontheight + 2 );

    for ( int i = 0; i < rows.size(); i++ ) {
      String str = rows.get( i );
      int len = ( str.length() + 10 ) * fontwidth;

      if ( maxx < len ) {
        maxx = len;
      }
    }

    return new Point( maxx, maxy );
  }

  private int getMaxLength() {
    int maxx = 0;

    for ( int i = 0; i < rows.size(); i++ ) {
      String str = rows.get( i );
      int len = str.length();

      if ( maxx < len ) {
        maxx = len;
      }
    }

    return maxx;
  }

  private Point getArea() {
    Rectangle rect = getClientArea();
    Point area = new Point( rect.width, rect.height );

    return area;
  }

  public List<FixedFileInputField> getFields() {
    return fields;
  }

  public void setFields( List<FixedFileInputField> fields ) {
    this.fields = fields;
  }

  public void clearFields() {
    fields = new Vector<FixedFileInputField>();
  }

}
