/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.GUISizeInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;

/**
 * Describes a note displayed on a Transformation, Job, Schema, or Report.
 *
 * @author Matt
 * @since 28-11-2003
 *
 */
public class NotePadMeta implements Cloneable, XMLInterface, GUIPositionInterface, GUISizeInterface {
  public static final String XML_TAG = "notepad";

  public static final int COLOR_RGB_BLACK_RED = 0;
  public static final int COLOR_RGB_BLACK_GREEN = 0;
  public static final int COLOR_RGB_BLACK_BLUE = 0;

  public static final int COLOR_RGB_DEFAULT_BG_RED = 255;
  public static final int COLOR_RGB_DEFAULT_BG_GREEN = 205;
  public static final int COLOR_RGB_DEFAULT_BG_BLUE = 112;

  public static final int COLOR_RGB_DEFAULT_BORDER_RED = 100;
  public static final int COLOR_RGB_DEFAULT_BORDER_GREEN = 100;
  public static final int COLOR_RGB_DEFAULT_BORDER_BLUE = 100;

  private String note;
  private String fontname;
  private int fontsize;
  private boolean fontbold;
  private boolean fontitalic;

  private int fontcolorred;
  private int fontcolorgreen;
  private int fontcolorblue;

  private int backgroundcolorred;
  private int backgroundcolorgreen;
  private int backgroundcolorblue;

  private int bordercolorred;
  private int bordercolorgreen;
  private int bordercolorblue;

  private boolean drawshadow;

  private Point location;
  public int width, height;
  private boolean selected;

  private boolean changed;

  private ObjectId id;

  public NotePadMeta() {
    note = null;
    location = new Point( -1, -1 );
    width = -1;
    height = -1;
    selected = false;
    setDefaultFont();

    backgroundcolorred = 0xFF;
    backgroundcolorgreen = 0xA5;
    backgroundcolorblue = 0x00;
  }

  public NotePadMeta( String n, int xl, int yl, int w, int h ) {
    note = n;
    location = new Point( xl, yl );
    width = w;
    height = h;
    selected = false;
    setDefaultFont();
  }

  public NotePadMeta( String n, int xl, int yl, int w, int h, String fontname, int fontsize, boolean fontbold,
    boolean fontitalic, int fontColorRed, int fontColorGreen, int fontColorBlue, int backGrounColorRed,
    int backGrounColorGreen, int backGrounColorBlue, int borderColorRed, int borderColorGreen,
    int borderColorBlue, boolean drawshadow ) {
    this.note = n;
    this.location = new Point( xl, yl );
    this.width = w;
    this.height = h;
    this.selected = false;
    this.fontname = fontname;
    this.fontsize = fontsize;
    this.fontbold = fontbold;
    this.fontitalic = fontitalic;
    // font color
    this.fontcolorred = fontColorRed;
    this.fontcolorgreen = fontColorGreen;
    this.fontcolorblue = fontColorBlue;
    // background color
    this.backgroundcolorred = backGrounColorRed;
    this.backgroundcolorgreen = backGrounColorGreen;
    this.backgroundcolorblue = backGrounColorBlue;
    // border color
    this.bordercolorred = borderColorRed;
    this.bordercolorgreen = borderColorGreen;
    this.bordercolorblue = borderColorBlue;
    this.drawshadow = drawshadow;
  }

  public NotePadMeta( Node notepadnode ) throws KettleXMLException {
    try {
      note = XMLHandler.getTagValue( notepadnode, "note" );
      String sxloc = XMLHandler.getTagValue( notepadnode, "xloc" );
      String syloc = XMLHandler.getTagValue( notepadnode, "yloc" );
      String swidth = XMLHandler.getTagValue( notepadnode, "width" );
      String sheight = XMLHandler.getTagValue( notepadnode, "heigth" );
      int x = Const.toInt( sxloc, 0 );
      int y = Const.toInt( syloc, 0 );
      this.location = new Point( x, y );
      this.width = Const.toInt( swidth, 0 );
      this.height = Const.toInt( sheight, 0 );
      this.selected = false;
      this.fontname = XMLHandler.getTagValue( notepadnode, "fontname" );
      this.fontsize = Const.toInt( XMLHandler.getTagValue( notepadnode, "fontsize" ), -1 );
      this.fontbold = "Y".equalsIgnoreCase( XMLHandler.getTagValue( notepadnode, "fontbold" ) );
      this.fontitalic = "Y".equalsIgnoreCase( XMLHandler.getTagValue( notepadnode, "fontitalic" ) );
      // font color
      this.fontcolorred = Const.toInt( XMLHandler.getTagValue( notepadnode, "fontcolorred" ), COLOR_RGB_BLACK_RED );
      this.fontcolorgreen =
        Const.toInt( XMLHandler.getTagValue( notepadnode, "fontcolorgreen" ), COLOR_RGB_BLACK_GREEN );
      this.fontcolorblue =
        Const.toInt( XMLHandler.getTagValue( notepadnode, "fontcolorblue" ), COLOR_RGB_BLACK_BLUE );
      // background color
      this.backgroundcolorred =
        Const.toInt( XMLHandler.getTagValue( notepadnode, "backgroundcolorred" ), COLOR_RGB_DEFAULT_BG_RED );
      this.backgroundcolorgreen =
        Const.toInt( XMLHandler.getTagValue( notepadnode, "backgroundcolorgreen" ), COLOR_RGB_DEFAULT_BG_GREEN );
      this.backgroundcolorblue =
        Const.toInt( XMLHandler.getTagValue( notepadnode, "backgroundcolorblue" ), COLOR_RGB_DEFAULT_BG_BLUE );
      // border color
      this.bordercolorred =
        Const.toInt( XMLHandler.getTagValue( notepadnode, "bordercolorred" ), COLOR_RGB_DEFAULT_BORDER_RED );
      this.bordercolorgreen =
        Const.toInt( XMLHandler.getTagValue( notepadnode, "bordercolorgreen" ), COLOR_RGB_DEFAULT_BORDER_GREEN );
      this.bordercolorblue =
        Const.toInt( XMLHandler.getTagValue( notepadnode, "bordercolorblue" ), COLOR_RGB_DEFAULT_BORDER_BLUE );
      this.drawshadow = "Y".equalsIgnoreCase( XMLHandler.getTagValue( notepadnode, "drawshadow" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to read Notepad info from XML", e );
    }
  }

  public ObjectId getObjectId() {
    return id;
  }

  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  public void setLocation( int x, int y ) {
    if ( x != location.x || y != location.y ) {
      setChanged();
    }
    location.x = x;
    location.y = y;
  }

  public void setLocation( Point p ) {
    setLocation( p.x, p.y );
  }

  public Point getLocation() {
    return location;
  }

  /**
   * @return Returns the note.
   */
  public String getNote() {
    return this.note;
  }

  /**
   * @param note
   *          The note to set.
   */
  public void setNote( String note ) {
    this.note = note;
  }

  /**
   * @param green
   *          the border red color.
   */
  public void setBorderColorRed( int red ) {
    this.bordercolorred = red;
  }

  /**
   * @param green
   *          the border color green.
   */
  public void setBorderColorGreen( int green ) {
    this.bordercolorgreen = green;
  }

  /**
   * @param green
   *          the border blue color.
   */
  public void setBorderColorBlue( int blue ) {
    this.bordercolorblue = blue;
  }

  /**
   * @parm red the backGround red color.
   */
  public void setBackGroundColorRed( int red ) {
    this.backgroundcolorred = red;
  }

  /**
   * @parm green the backGround green color.
   */
  public void setBackGroundColorGreen( int green ) {
    this.backgroundcolorgreen = green;
  }

  /**
   * @parm green the backGround blue color.
   */
  public void setBackGroundColorBlue( int blue ) {
    this.backgroundcolorblue = blue;
  }

  /**
   * @param Returns
   *          the font color red.
   */
  public void setFontColorRed( int red ) {
    this.fontcolorred = red;
  }

  /**
   * @param Returns
   *          the font color green.
   */
  public void setFontColorGreen( int green ) {
    this.fontcolorgreen = green;
  }

  /**
   * @param Returns
   *          the font color blue.
   */
  public void setFontColorBlue( int blue ) {
    this.fontcolorblue = blue;
  }

  /**
   * @return Returns the selected.
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * @param selected
   *          The selected to set.
   */
  public void setSelected( boolean selected ) {
    this.selected = selected;
  }

  /**
   * Change a selected state to not-selected and vice-versa.
   */
  public void flipSelected() {
    this.selected = !this.selected;
  }

  /**
   * @param drawshadow
   *          The drawshadow to set.
   */
  public void setDrawShadow( boolean drawshadow ) {
    this.drawshadow = drawshadow;
  }

  /**
   * Change a drawshadow state
   */
  public boolean isDrawShadow() {
    return this.drawshadow;
  }

  public Object clone() {
    try {
      Object retval = super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  public void setChanged() {
    setChanged( true );
  }

  public void setChanged( boolean ch ) {
    changed = ch;
  }

  public boolean hasChanged() {
    return changed;
  }

  public String toString() {
    return note;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( "    <notepad>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "note", note ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "xloc", location.x ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "yloc", location.y ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "width", width ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "heigth", height ) );
    // Font
    retval.append( "      " ).append( XMLHandler.addTagValue( "fontname", fontname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fontsize", fontsize ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fontbold", fontbold ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fontitalic", fontitalic ) );
    // Font color
    retval.append( "      " ).append( XMLHandler.addTagValue( "fontcolorred", fontcolorred ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fontcolorgreen", fontcolorgreen ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fontcolorblue", fontcolorblue ) );
    // Background color
    retval.append( "      " ).append( XMLHandler.addTagValue( "backgroundcolorred", backgroundcolorred ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "backgroundcolorgreen", backgroundcolorgreen ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "backgroundcolorblue", backgroundcolorblue ) );
    // border color
    retval.append( "      " ).append( XMLHandler.addTagValue( "bordercolorred", bordercolorred ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "bordercolorgreen", bordercolorgreen ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "bordercolorblue", bordercolorblue ) );
    // draw shadow
    retval.append( "      " ).append( XMLHandler.addTagValue( "drawshadow", drawshadow ) );
    retval.append( "    </notepad>" ).append( Const.CR );

    return retval.toString();
  }

  /**
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * @param height
   *          the height to set
   */
  public void setHeight( int height ) {
    this.height = height;
  }

  /**
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * @param width
   *          the width to set
   */
  public void setWidth( int width ) {
    this.width = width;
  }

  /**
   * @return Returns the font name.
   */
  public String getFontName() {
    return this.fontname;
  }

  /**
   * @param note
   *          The font name.
   */
  public void setFontName( String fontname ) {
    this.fontname = fontname;
  }

  /**
   * @return Returns the font size.
   */
  public int getFontSize() {
    return this.fontsize;
  }

  /**
   * @param note
   *          The font bold.
   */
  public void setFontBold( boolean fontbold ) {
    this.fontbold = fontbold;
  }

  /**
   * @return Returns the font bold.
   */
  public boolean isFontBold() {
    return this.fontbold;
  }

  /**
   * @param note
   *          The font italic.
   */
  public void setFontItalic( boolean fontitalic ) {
    this.fontitalic = fontitalic;
  }

  /**
   * @return Returns the font italic.
   */
  public boolean isFontItalic() {
    return this.fontitalic;
  }

  /**
   * @return Returns the backGround color red.
   */
  public int getBorderColorRed() {
    return this.bordercolorred;
  }

  /**
   * @return Returns the backGround color green.
   */
  public int getBorderColorGreen() {
    return this.bordercolorgreen;
  }

  /**
   * @return Returns the backGround color blue.
   */
  public int getBorderColorBlue() {
    return this.bordercolorblue;
  }

  /**
   * @return Returns the backGround color red.
   */
  public int getBackGroundColorRed() {
    return this.backgroundcolorred;
  }

  /**
   * @return Returns the backGround color green.
   */
  public int getBackGroundColorGreen() {
    return this.backgroundcolorgreen;
  }

  /**
   * @return Returns the backGround color blue.
   */
  public int getBackGroundColorBlue() {
    return this.backgroundcolorblue;
  }

  /**
   * @return Returns the font color red.
   */
  public int getFontColorRed() {
    return this.fontcolorred;
  }

  /**
   * @return Returns the font color green.
   */
  public int getFontColorGreen() {
    return this.fontcolorgreen;
  }

  /**
   * @return Returns the font color blue.
   */
  public int getFontColorBlue() {
    return this.fontcolorblue;
  }

  /**
   * @param note
   *          The font name.
   */
  public void setFontSize( int fontsize ) {
    this.fontsize = fontsize;
  }

  private void setDefaultFont() {
    this.fontname = null;
    this.fontsize = -1;
    this.fontbold = false;
    this.fontitalic = false;

    // font color black
    this.fontcolorred = COLOR_RGB_BLACK_RED;
    this.fontcolorgreen = COLOR_RGB_BLACK_GREEN;
    this.fontcolorblue = COLOR_RGB_BLACK_BLUE;

    // background yellow
    this.backgroundcolorred = COLOR_RGB_DEFAULT_BG_RED;
    this.backgroundcolorgreen = COLOR_RGB_DEFAULT_BG_GREEN;
    this.backgroundcolorblue = COLOR_RGB_DEFAULT_BG_BLUE;

    // border gray
    this.bordercolorred = COLOR_RGB_DEFAULT_BORDER_RED;
    this.bordercolorgreen = COLOR_RGB_DEFAULT_BORDER_GREEN;
    this.bordercolorblue = COLOR_RGB_DEFAULT_BORDER_BLUE;

    this.drawshadow = true;
  }
}
