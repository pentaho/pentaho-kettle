/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.template;

import java.io.Serializable;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.StylesUtil;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.remote.JsonMapping;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;


/**
 * Defines a region in a template. A cell can display a part of a connected data item. This part is
 * selected by the <em>bindingIndex</em>.
 * <p>
 * For horizontal positioning, two of the the properties <em>left</em>, <em>right<em>, and
 * <em>width</em> must be set. For vertical positioning, two properties out of <em>top</em>,
 * <em>bottom</em>, and <em>height</em> are required.
 * </p>
 *
 * @since 2.2
 */
public abstract class Cell<T extends Cell> implements Serializable  {

  private static final String PROPERTY_TYPE = "type";
  private static final String PROPERTY_LEFT = "left";
  private static final String PROPERTY_TOP = "top";
  private static final String PROPERTY_RIGHT = "right";
  private static final String PROPERTY_BOTTOM = "bottom";
  private static final String PROPERTY_WIDTH = "width";
  private static final String PROPERTY_HEIGHT = "height";
  private static final String PROPERTY_BINDING_INDEX = "bindingIndex";
  private static final String PROPERTY_SELECTABLE = "selectable";
  private static final String PROPERTY_NAME = "name";
  private static final String PROPERTY_FOREGROUND = "foreground";
  private static final String PROPERTY_BACKGROUND = "background";
  private static final String PROPERTY_FONT = "font";
  private static final String PROPERTY_H_ALIGNMENT = "horizontalAlignment";
  private static final String PROPERTY_V_ALIGNMENT = "verticalAlignment";

  private final String type;
  private String name;
  private int bindingIndex;
  private boolean isSelectable;
  private Color foreground;
  private Color background;
  private Font font;
  private Position left;
  private Position right;
  private Position top;
  private Position bottom;
  private Integer width;
  private Integer height;
  private int horizontalAlignment = SWT.NONE;
  private int verticalAlignment = SWT.NONE;

  /**
   * Constructs a new cell on the given template. Subclasses must provide a unique name.
   *
   * @param template the template that this cell will be part of, must not be <code>null</code>
   * @param type a unique type string to identify the cell type, must not be <code>null</code> or
   *          empty
   */
  public Cell( Template template, String type ) {
    ParamCheck.notNull( template, "template" );
    ParamCheck.notNullOrEmpty( type, "type" );
    this.type = type;
    bindingIndex = -1;
    template.addCell( this );
  }

  String getType() {
    return type;
  }

  /**
   * Sets a name for this cell. This name is used to identify the cell in a selection event.
   * If the cell is selectable, a selection event will have its <em>text</em> attribute set to
   * this name.
   *
   * @return the cell itself, to enable method chaining
   * @see #setSelectable(boolean)
   * @see org.eclipse.swt.widgets.Event#text
   */
  public T setName( String name ) {
    ParamCheck.notNullOrEmpty( name, "name" );
    this.name = name;
    return getThis();
  }

  String getName() {
    return name;
  }

  /**
   * Sets the index that is used to select the part (e.g. text/image) from the connected data item
   * to be displayed by this cell. A value of <code>-1</code> indicates that the cell is not bound.
   *
   * @param index the index of the part to display
   * @return the cell itself, to enable method chaining
   */
  public T setBindingIndex( int index ) {
    bindingIndex = index;
    return getThis();
  }

  int getBindingIndex() {
    return bindingIndex;
  }

  /**
   * Enables cell selection. If set to <code>true</code>, clicking this cell will not select the
   * item but still trigger a selection event on the control. This selection event will have its
   * <em>detail</em> field set to <code>RWT.CELL</code> and it's <em>text</em> field set to the name
   * of this cell. The default is <code>false</code>.
   *
   * @param selectable <code>true</code> to enable cell selection
   * @return the cell itself, to enable method chaining
   * @see #setName(String)
   * @see org.eclipse.rap.rwt.RWT#CELL
   */
  public T setSelectable( boolean selectable ) {
    isSelectable = selectable;
    return getThis();
  }

  boolean isSelectable() {
    return isSelectable;
  }

  /**
   * Sets the foreground color for this cell. The connected data item may override this color. If
   * the argument is null, the widget's default foreground color will be used.
   *
   * @param color the foreground color, or <code>null</code> to use the default
   * @return the cell itself, to enable method chaining
   */
  public T setForeground( Color color ) {
    foreground = color;
    return getThis();
  }

  Color getForeground() {
    return foreground;
  }

  /**
   * Sets the background color for this cell. The connected data item may override this color. If
   * the argument is null, the widget's default color will be used.
   *
   * @param color the background color, or <code>null</code> to use the default
   * @return the cell itself, to enable method chaining
   */
  public T setBackground( Color color ) {
    this.background = color;
    return getThis();
  }

  Color getBackground() {
    return background;
  }

  /**
   * Sets the font for this cell. The connected data item may override this font. If the argument is
   * null, the widget's default font will be used.
   *
   * @param font a font, or <code>null</code> to use the default
   * @return the cell itself, to enable method chaining
   */
  public T setFont( Font font ) {
    this.font = font;
    return getThis();
  }

  Font getFont() {
    return font;
  }

  /**
   * Sets the position of the left edge of the cell.
   *
   * @param offset the distance from the left edge of the template in px
   * @return the cell itself, to enable method chaining
   * @throws IllegalStateException if both <em>right</em> and <em>width</em> are already set
   */
  public T setLeft( int offset ) {
    return setLeft( 0, offset );
  }

  /**
   * Sets the position of the left edge of the cell.
   *
   * @param percentage the distance from the left edge of the template as a percentage of the
   *          template's width
   * @param offset a fixed offset in px to add to the percentage
   * @return the cell itself, to enable method chaining
   * @throws IllegalStateException if both <em>right</em> and <em>width</em> are already set
   * @since 2.3
   */
  public T setLeft( float percentage, int offset ) {
    checkHorizontalParameters( right, width );
    checkPercentage( percentage );
    left = new Position( percentage, offset );
    return getThis();
  }

  Position getLeft() {
    return left;
  }

  /**
   * Sets the position of the right edge of the cell.
   *
   * @param offset the distance from the right edge of the template in px
   * @return the cell itself, to enable method chaining
   * @throws IllegalStateException if both <em>left</em> and <em>width</em> are already set
   */
  public T setRight( int offset ) {
    return setRight( 0, offset );
  }

  /**
   * Sets the position of the right edge of the cell.
   *
   * @param percentage the distance from the right edge of the template as a percentage of the
   *          template's width
   * @param offset a fixed offset in px to add to the percentage
   * @return the cell itself, to enable method chaining
   * @throws IllegalStateException if both <em>left</em> and <em>width</em> are already set
   * @since 2.3
   */
  public T setRight( float percentage, int offset ) {
    checkHorizontalParameters( left, width );
    checkPercentage( percentage );
    this.right = new Position( percentage, offset );
    return getThis();
  }

  Position getRight() {
    return right;
  }

  /**
   * Sets the position of the upper edge of the cell.
   *
   * @param offset the distance from the upper edge of the template in px
   * @return the cell itself, to enable method chaining
   * @throws IllegalStateException if both <em>bottom</em> and <em>height</em> are already set
   */
  public T setTop( int offset ) {
    return setTop( 0, offset );
  }

  /**
   * Sets the position of the upper edge of the cell.
   *
   * @param percentage the distance from the right edge of the template as a percentage of the
   *          template's height
   * @param offset a fixed offset in px to add to the percentage
   * @return the cell itself, to enable method chaining
   * @throws IllegalStateException if both <em>bottom</em> and <em>height</em> are already set
   * @since 2.3
   */
  public T setTop( float percentage, int offset ) {
    checkVerticalParameters( bottom, height );
    checkPercentage( percentage );
    this.top = new Position( percentage, offset );
    return getThis();
  }

  Position getTop() {
    return top;
  }

  /**
   * Sets the position of the lower edge of the cell.
   *
   * @param offset the distance from the lower edge of the template in px
   * @return the cell itself, to enable method chaining
   * @throws IllegalStateException if both <em>top</em> and <em>height</em> are already set
   */
  public T setBottom( int offset ) {
    return setBottom( 0, offset );
  }

  /**
   * Sets the position of the lower edge of the cell.
   *
   * @param percentage the distance from the right edge of the template as a percentage of the
   *          template's height
   * @param offset a fixed offset in px to add to the percentage
   * @return the cell itself, to enable method chaining
   * @throws IllegalStateException if both <em>top</em> and <em>height</em> are already set
   * @since 2.3
   */
  public T setBottom( float percentage, int offset ) {
    checkVerticalParameters( top, height );
    checkPercentage( percentage );
    this.bottom = new Position( percentage, offset );
    return getThis();
  }

  Position getBottom() {
    return bottom;
  }

  /**
   * Sets the width of the cell.
   * A value of <code>SWT.DEFAULT</code> resets the width.
   *
   * @param width the width in px, must not be negative
   * @return the cell itself, to enable method chaining
   */
  public T setWidth( int width ) {
    checkHorizontalParameters( left, right );
    this.width = Integer.valueOf( width );
    return getThis();
  }

  Integer getWidth() {
    return width;
  }

  /**
   * Sets the height of the cell.
   * A value of <code>SWT.DEFAULT</code> resets the height.
   *
   * @param height the height in px, must not be negative
   * @return the cell itself, to enable method chaining
   */
  public T setHeight( int height ) {
    checkVerticalParameters( top, bottom );
    this.height = Integer.valueOf( height );
    return getThis();
  }

  Integer getHeight() {
    return height;
  }

  /**
   * Defines how the content of this cell should be positioned horizontally.
   *
   * @param alignment the horizontal alignment, must be one of: SWT.BEGINNING (or SWT.LEFT),
   *          SWT.CENTER, SWT.END (or SWT.RIGHT)
   * @return the cell itself, to enable method chaining
   */
  public T setHorizontalAlignment( int alignment ) {
    horizontalAlignment = alignment;
    return getThis();
  }

  int getHorizontalAlignment() {
    return horizontalAlignment;
  }

  /**
   * Defines how the content of this cell should be positioned vertically.
   *
   * @param alignment the horizontal alignment, must be one of: SWT.BEGINNING (or SWT.TOP),
   *          SWT.CENTER, SWT.END (or SWT.BOTTOM)
   * @return the cell itself, to enable method chaining
   */
  public T setVerticalAlignment( int alignment ) {
    verticalAlignment = alignment;
    return getThis();
  }

  int getVerticalAlignment() {
    return verticalAlignment;
  }

  /**
   * Creates a JSON representation of this cell. Subclasses can override this method, but must call
   * super and add additional attributes like this:
   * <pre>
   * protected JsonObject toJson() {
   *   JsonObject json = super.toJson();
   *   json.add( "foo", getFoo() );
   *   ...
   *   return json;
   * }
   * </pre>
   * @return a json object that represents this cell
   */
  protected JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.add( PROPERTY_TYPE, type );
    if( left != null ) {
      jsonObject.add( PROPERTY_LEFT, left.toJson() );
    }
    if( right != null ) {
      jsonObject.add( PROPERTY_RIGHT, right.toJson() );
    }
    if( top != null ) {
      jsonObject.add( PROPERTY_TOP, top.toJson() );
    }
    if( bottom != null ) {
      jsonObject.add( PROPERTY_BOTTOM, bottom.toJson() );
    }
    if( width != null ) {
      jsonObject.add( PROPERTY_WIDTH, width.intValue() );
    }
    if( height != null ) {
      jsonObject.add( PROPERTY_HEIGHT, height.intValue() );
    }
    if( bindingIndex != -1 ) {
      jsonObject.add( PROPERTY_BINDING_INDEX, bindingIndex );
    }
    if( isSelectable ) {
      jsonObject.add( PROPERTY_SELECTABLE, isSelectable );
    }
    if( name != null ) {
      jsonObject.add( PROPERTY_NAME, name );
    }
    if( foreground != null ) {
      jsonObject.add( PROPERTY_FOREGROUND, JsonMapping.toJson( foreground ) );
    }
    if( background != null ) {
      jsonObject.add( PROPERTY_BACKGROUND, JsonMapping.toJson( background ) );
    }
    if( font != null ) {
      jsonObject.add( PROPERTY_FONT, JsonMapping.toJson( font ) );
    }
    if( horizontalAlignment != SWT.NONE ) {
      jsonObject.add( PROPERTY_H_ALIGNMENT, hAlignmentToString( horizontalAlignment ) );
    }
    if( verticalAlignment != SWT.NONE ) {
      jsonObject.add( PROPERTY_V_ALIGNMENT, vAlignmentToString( verticalAlignment ) );
    }
    return jsonObject;
  }

  @SuppressWarnings( "unchecked" )
  private T getThis() {
    return ( T )this;
  }

  private void checkHorizontalParameters( Object value1, Object value2 ) {
    if( value1 != null && value2 != null ) {
      throw new IllegalStateException( "Can only set two horizontal attributes" );
    }
  }

  private void checkVerticalParameters( Object value1, Object value2 ) {
    if( value1 != null && value2 != null ) {
      throw new IllegalStateException( "Can only set two vertical attributes" );
    }
  }

  private static void checkPercentage( float percentage ) {
    if( percentage < 0 || percentage > 100 ) {
      throw new IllegalArgumentException( "Percentage out of range: " + percentage );
    }
  }

  private static String hAlignmentToString( int alignment ) {
    int style = translate( translate( alignment, SWT.BEGINNING, SWT.LEFT ), SWT.END, SWT.RIGHT );
    String[] styles = StylesUtil.filterStyles( style, "LEFT", "RIGHT", "CENTER" );
    if( styles.length != 0 ) {
      return styles[ 0 ];
    }
    return null;
  }

  private static String vAlignmentToString( int alignment ) {
    int style = translate( translate( alignment, SWT.BEGINNING, SWT.TOP ), SWT.END, SWT.BOTTOM );
    String[] styles = StylesUtil.filterStyles( style, "TOP", "BOTTOM", "CENTER" );
    if( styles.length != 0 ) {
      return styles[ 0 ];
    }
    return null;
  }

  private static int translate( int style, int from, int to ) {
    return ( style & from ) == from ? to : style;
  }

}
