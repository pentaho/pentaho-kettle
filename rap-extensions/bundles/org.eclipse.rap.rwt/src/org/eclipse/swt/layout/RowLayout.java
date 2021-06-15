/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Instances of this class determine the size and position of the 
 * children of a <code>Composite</code> by placing them either in 
 * horizontal rows or vertical columns within the parent <code>Composite</code>. 
 * <p>
 * <code>RowLayout</code> aligns all controls in one row if the
 * <code>type</code> is set to horizontal, and one column if it is
 * set to vertical. It has the ability to wrap, and provides configurable 
 * margins and spacing. <code>RowLayout</code> has a number of configuration 
 * fields. In addition, the height and width of each control in a 
 * <code>RowLayout</code> can be specified by setting a <code>RowData</code>
 * object into the control using <code>setLayoutData ()</code>.
 * </p>
 * <p>
 * The following example code creates a <code>RowLayout</code>, sets all 
 * of its fields to non-default values, and then sets it into a 
 * <code>Shell</code>. 
 * <pre>
 *    RowLayout rowLayout = new RowLayout();
 *    rowLayout.wrap = false;
 *    rowLayout.pack = false;
 *    rowLayout.justify = true;
 *    rowLayout.type = SWT.VERTICAL;
 *    rowLayout.marginLeft = 5;
 *    rowLayout.marginTop = 5;
 *    rowLayout.marginRight = 5;
 *    rowLayout.marginBottom = 5;
 *    rowLayout.spacing = 0;
 *    shell.setLayout(rowLayout);
 * </pre>
 * If you are using the default field values, you only need one line of code:
 * <pre>
 *    shell.setLayout(new RowLayout());
 * </pre>
 * </p>
 * 
 * @see RowData
 * @see <a href="http://www.eclipse.org/swt/snippets/#rowlayout">RowLayout snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: LayoutExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @since 1.0
 */
public final class RowLayout extends Layout {

  /**
   * type specifies whether the layout places controls in rows or 
   * columns.
   * 
   * The default value is HORIZONTAL.
   * 
   * Possible values are: <ul>
   *    <li>HORIZONTAL: Position the controls horizontally from left to right</li>
   *    <li>VERTICAL: Position the controls vertically from top to bottom</li>
   * </ul>
   */
  public int type = SWT.HORIZONTAL;
  
  /**
   * marginWidth specifies the number of pixels of horizontal margin
   * that will be placed along the left and right edges of the layout.
   *
   * The default value is 0.
   */
  public int marginWidth = 0;
  
  /**
   * marginHeight specifies the number of pixels of vertical margin
   * that will be placed along the top and bottom edges of the layout.
   *
   * The default value is 0.
   */
  public int marginHeight = 0;

  /**
   * spacing specifies the number of pixels between the edge of one cell
   * and the edge of its neighbouring cell.
   *
   * The default value is 3.
   */
  public int spacing = 3;
      
  /**
   * wrap specifies whether a control will be wrapped to the next
   * row if there is insufficient space on the current row.
   *
   * The default value is true.
   */
  public boolean wrap = true;

  /**
   * pack specifies whether all controls in the layout take
   * their preferred size.  If pack is false, all controls will 
   * have the same size which is the size required to accommodate the 
   * largest preferred height and the largest preferred width of all 
   * the controls in the layout.
   *
   * The default value is true.
   */
  public boolean pack = true;
  
  /**
   * fill specifies whether the controls in a row should be
   * all the same height for horizontal layouts, or the same
   * width for vertical layouts.
   *
   * The default value is false.
   */
  public boolean fill = false;

  /**
   * center specifies whether the controls in a row should be
   * centered vertically in each cell for horizontal layouts,
   * or centered horizontally in each cell for vertical layouts.
   *
   * The default value is false.
   * 
   * @since 1.2
   */
  public boolean center = false;
  
  /**
   * justify specifies whether the controls in a row should be
   * fully justified, with any extra space placed between the controls.
   *
   * The default value is false.
   */
  public boolean justify = false;

  /**
   * marginLeft specifies the number of pixels of horizontal margin
   * that will be placed along the left edge of the layout.
   *
   * The default value is 3.
   */
  public int marginLeft = 3;

  /**
   * marginTop specifies the number of pixels of vertical margin
   * that will be placed along the top edge of the layout.
   *
   * The default value is 3.
   */
  public int marginTop = 3;

  /**
   * marginRight specifies the number of pixels of horizontal margin
   * that will be placed along the right edge of the layout.
   *
   * The default value is 3.
   */
  public int marginRight = 3;

  /**
   * marginBottom specifies the number of pixels of vertical margin
   * that will be placed along the bottom edge of the layout.
   *
   * The default value is 3.
   */
  public int marginBottom = 3;

/**
 * Constructs a new instance of this class.
 */
public RowLayout () {
}

/**
 * Constructs a new instance of this class given the type.
 *
 * @param type the type of row layout
 */
public RowLayout (int type) {
  this.type = type;
}

protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
  Point extent;
  if (type == SWT.HORIZONTAL) {
    extent = layoutHorizontal (composite, false, (wHint != SWT.DEFAULT) && wrap, wHint, flushCache);
  } else {
    extent = layoutVertical (composite, false, (hHint != SWT.DEFAULT) && wrap, hHint, flushCache);
  }
  if (wHint != SWT.DEFAULT) extent.x = wHint;
  if (hHint != SWT.DEFAULT) extent.y = hHint;
  return extent;
}

Point computeSize (Control control, boolean flushCache) {
  int wHint = SWT.DEFAULT, hHint = SWT.DEFAULT;
  RowData data = (RowData) control.getLayoutData ();
  if (data != null) {
    wHint = data.width;
    hHint = data.height;
  }
  return control.computeSize (wHint, hHint, flushCache);
}

protected boolean flushCache (Control control) {
  return true;
}

String getName () {
  String string = getClass ().getName ();
  int index = string.lastIndexOf ('.');
  if (index == -1) return string;
  return string.substring (index + 1, string.length ());
}

protected void layout (Composite composite, boolean flushCache) {
  Rectangle clientArea = composite.getClientArea ();
  if (type == SWT.HORIZONTAL) {
    layoutHorizontal (composite, true, wrap, clientArea.width, flushCache);
  } else {
    layoutVertical (composite, true, wrap, clientArea.height, flushCache);
  }
}

Point layoutHorizontal (Composite composite, boolean move, boolean wrap, int width, boolean flushCache) {
  Control [] children = composite.getChildren ();
  int count = 0;
  for (int i=0; i<children.length; i++) {
    Control control = children [i];
    RowData data = (RowData) control.getLayoutData ();
    if (data == null || !data.exclude) {
      children [count++] = children [i];
    } 
  }
  if (count == 0) {
    return new Point (marginLeft + marginWidth * 2 + marginRight, marginTop + marginHeight * 2 + marginBottom);
  }
  int childWidth = 0, childHeight = 0, maxHeight = 0;
  if (!pack) {
    for (int i=0; i<count; i++) {
      Control child = children [i];
      Point size = computeSize (child, flushCache);
      childWidth = Math.max (childWidth, size.x);
      childHeight = Math.max (childHeight, size.y);
    }
    maxHeight = childHeight;
  }
  int clientX = 0, clientY = 0;
  if (move) {
    Rectangle rect = composite.getClientArea ();
    clientX = rect.x;
    clientY = rect.y;
  }
  int [] wraps = null;
  boolean wrapped = false;
  Rectangle [] bounds = null;
  if (move && (justify || fill || center)) {
    bounds = new Rectangle [count];
    wraps = new int [count];
  }
  int maxX = 0, x = marginLeft + marginWidth, y = marginTop + marginHeight;
  for (int i=0; i<count; i++) {
    Control child = children [i];
    if (pack) {
      Point size = computeSize (child, flushCache);
      childWidth = size.x;
      childHeight = size.y;
    }
    if (wrap && (i != 0) && (x + childWidth > width)) {
      wrapped = true;
      if (move && (justify || fill || center)) wraps [i - 1] = maxHeight;
      x = marginLeft + marginWidth;
      y += spacing + maxHeight;
      if (pack) maxHeight = 0;
    }
    if (pack || fill || center) {
      maxHeight = Math.max (maxHeight, childHeight);
    }
    if (move) {
      int childX = x + clientX, childY = y + clientY;
      if (justify || fill || center) {
        bounds [i] = new Rectangle (childX, childY, childWidth, childHeight);
      } else {
        child.setBounds (childX, childY, childWidth, childHeight);
      }
    }
    x += spacing + childWidth;
    maxX = Math.max (maxX, x);
  }
  maxX = Math.max (clientX + marginLeft + marginWidth, maxX - spacing);
  if (!wrapped) maxX += marginRight + marginWidth;
  if (move && (justify || fill || center)) {
    int space = 0, margin = 0;
    if (!wrapped) {
      space = Math.max (0, (width - maxX) / (count + 1));
      margin = Math.max (0, ((width - maxX) % (count + 1)) / 2);
    } else {
      if (fill || justify || center) {
        int last = 0;
        if (count > 0) wraps [count - 1] = maxHeight;
        for (int i=0; i<count; i++) {
          if (wraps [i] != 0) {
            int wrapCount = i - last + 1;
            if (justify) {
              int wrapX = 0;
              for (int j=last; j<=i; j++) {
                wrapX += bounds [j].width + spacing;
              }
              space = Math.max (0, (width - wrapX) / (wrapCount + 1));
              margin = Math.max (0, ((width - wrapX) % (wrapCount + 1)) / 2);
            }
            for (int j=last; j<=i; j++) {
              if (justify) bounds [j].x += (space * (j - last + 1)) + margin;
              if (fill) {
                bounds [j].height = wraps [i];
              } else {
                if (center) {
                  bounds [j].y += Math.max (0, (wraps [i] - bounds [j].height) / 2);
                }
              }
            }
            last = i + 1;
          }
        }
      }
    }
    for (int i=0; i<count; i++) {
      if (!wrapped) {
        if (justify) bounds [i].x += (space * (i + 1)) + margin;
        if (fill) {
          bounds [i].height = maxHeight;
        } else {
          if (center) {
            bounds [i].y += Math.max (0, (maxHeight - bounds [i].height) / 2);
          }
        }
      }
      children [i].setBounds (bounds [i]);
    }
  }
  return new Point (maxX, y + maxHeight + marginBottom + marginHeight);
}

Point layoutVertical (Composite composite, boolean move, boolean wrap, int height, boolean flushCache) {
  Control [] children = composite.getChildren ();
  int count = 0;
  for (int i=0; i<children.length; i++) {
    Control control = children [i];
    RowData data = (RowData) control.getLayoutData ();
    if (data == null || !data.exclude) {
      children [count++] = children [i];
    } 
  }
  if (count == 0) {
    return new Point (marginLeft + marginWidth * 2 + marginRight, marginTop + marginHeight * 2 + marginBottom);
  }
  int childWidth = 0, childHeight = 0, maxWidth = 0;
  if (!pack) {
    for (int i=0; i<count; i++) {
      Control child = children [i];
      Point size = computeSize (child, flushCache);
      childWidth = Math.max (childWidth, size.x);
      childHeight = Math.max (childHeight, size.y);
    }
    maxWidth = childWidth;
  }
  int clientX = 0, clientY = 0;
  if (move) {
    Rectangle rect = composite.getClientArea ();
    clientX = rect.x;
    clientY = rect.y;
  }
  int [] wraps = null;
  boolean wrapped = false;
  Rectangle [] bounds = null;
  if (move && (justify || fill || center)) {
    bounds = new Rectangle [count];
    wraps = new int [count];
  }
  int maxY = 0, x = marginLeft + marginWidth, y = marginTop + marginHeight;
  for (int i=0; i<count; i++) {
    Control child = children [i];
    if (pack) {
      Point size = computeSize (child, flushCache);
      childWidth = size.x;
      childHeight = size.y;
    }
    if (wrap && (i != 0) && (y + childHeight > height)) {
      wrapped = true;
      if (move && (justify || fill || center)) wraps [i - 1] = maxWidth;
      x += spacing + maxWidth;
      y = marginTop + marginHeight;
      if (pack) maxWidth = 0;
    }
    if (pack || fill || center) {
      maxWidth = Math.max (maxWidth, childWidth);
    }
    if (move) {
      int childX = x + clientX, childY = y + clientY;
      if (justify || fill || center) {
        bounds [i] = new Rectangle (childX, childY, childWidth, childHeight);
      } else {
        child.setBounds (childX, childY, childWidth, childHeight);
      }
    }
    y += spacing + childHeight;
    maxY = Math.max (maxY, y);
  }
  maxY = Math.max (clientY + marginTop + marginHeight, maxY - spacing);
  if (!wrapped) maxY += marginBottom + marginHeight;
  if (move && (justify || fill || center)) {
    int space = 0, margin = 0;
    if (!wrapped) {
      space = Math.max (0, (height - maxY) / (count + 1));
      margin = Math.max (0, ((height - maxY) % (count + 1)) / 2);
    } else {
      if (fill || justify || center) {
        int last = 0;
        if (count > 0) wraps [count - 1] = maxWidth;
        for (int i=0; i<count; i++) {
          if (wraps [i] != 0) {
            int wrapCount = i - last + 1;
            if (justify) {
              int wrapY = 0;
              for (int j=last; j<=i; j++) {
                wrapY += bounds [j].height + spacing;
              }
              space = Math.max (0, (height - wrapY) / (wrapCount + 1));
              margin = Math.max (0, ((height - wrapY) % (wrapCount + 1)) / 2);
            }
            for (int j=last; j<=i; j++) {
              if (justify) bounds [j].y += (space * (j - last + 1)) + margin;
              if (fill) {
                bounds [j].width = wraps [i];
              } else {
                if (center) {
                  bounds [j].x += Math.max (0, (wraps [i] - bounds [j].width) / 2);
                }
              }
            }
            last = i + 1;
          }
        }
      }
    }
    for (int i=0; i<count; i++) {
      if (!wrapped) {
        if (justify) bounds [i].y += (space * (i + 1)) + margin;
        if (fill) {
          bounds [i].width = maxWidth;
        } else {
          if (center) {
            bounds [i].x += Math.max (0, (maxWidth - bounds [i].width) / 2);
          }
        }

      }
      children [i].setBounds (bounds [i]);
    }
  }
  return new Point (x + maxWidth + marginRight + marginWidth, maxY);
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the layout
 */
public String toString () {
  String string = getName ()+" {";
  string += "type="+((type != SWT.HORIZONTAL) ? "SWT.VERTICAL" : "SWT.HORIZONTAL")+" ";
  if (marginWidth != 0) string += "marginWidth="+marginWidth+" ";
  if (marginHeight != 0) string += "marginHeight="+marginHeight+" ";
  if (marginLeft != 0) string += "marginLeft="+marginLeft+" ";
  if (marginTop != 0) string += "marginTop="+marginTop+" ";
  if (marginRight != 0) string += "marginRight="+marginRight+" ";
  if (marginBottom != 0) string += "marginBottom="+marginBottom+" ";
  if (spacing != 0) string += "spacing="+spacing+" ";
  string += "wrap="+wrap+" ";
  string += "pack="+pack+" ";
  string += "fill="+fill+" ";
  string += "justify="+justify+" ";
  string = string.trim();
  string += "}";
  return string;
}
}
