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
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Control;

/**
 * Instances of this class are used to define the attachments 
 * of a control in a <code>FormLayout</code>. 
 * <p>
 * To set a <code>FormData</code> object into a control, you use the 
 * <code>setLayoutData ()</code> method. To define attachments for the 
 * <code>FormData</code>, set the fields directly, like this:
 * <pre>
 *    FormData data = new FormData();
 *    data.left = new FormAttachment(0,5);
 *    data.right = new FormAttachment(100,-5);
 *    button.setLayoutData(formData);
 * </pre>
 * </p>
 * <p>
 * <code>FormData</code> contains the <code>FormAttachments</code> for 
 * each edge of the control that the <code>FormLayout</code> uses to
 * determine the size and position of the control. <code>FormData</code>
 * objects also allow you to set the width and height of controls within
 * a <code>FormLayout</code>. 
 * </p>
 * 
 * @see FormLayout
 * @see FormAttachment
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * 
 * @since 1.0
 */
public final class FormData implements SerializableCompatibility {
  /**
   * width specifies the preferred width in pixels. This value
   * is the wHint passed into Control.computeSize(int, int, boolean) 
   * to determine the preferred size of the control.
   *
   * The default value is SWT.DEFAULT.
   *
   * @see Control#computeSize(int, int, boolean)
   */
  public int width = SWT.DEFAULT;
  /**
   * height specifies the preferred height in pixels. This value
   * is the hHint passed into Control.computeSize(int, int, boolean) 
   * to determine the preferred size of the control.
   *
   * The default value is SWT.DEFAULT.
   *
   * @see Control#computeSize(int, int, boolean)
   */
  public int height = SWT.DEFAULT;
  /**
   * left specifies the attachment of the left side of 
   * the control.
   */
  public FormAttachment left;
  /**
   * right specifies the attachment of the right side of
   * the control.
   */
  public FormAttachment right;
  /**
   * top specifies the attachment of the top of the control.
   */
  public FormAttachment top;
  /**
   * bottom specifies the attachment of the bottom of the
   * control.
   */
  public FormAttachment bottom;
  
  int cacheWidth = -1, cacheHeight = -1;
  int defaultWhint, defaultHhint, defaultWidth = -1, defaultHeight = -1;
  int currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;
  FormAttachment cacheLeft, cacheRight, cacheTop, cacheBottom;
  boolean isVisited, needed;
  
/**
 * Constructs a new instance of FormData using
 * default values.
 */
public FormData () {
}
  
/**
 * Constructs a new instance of FormData according to the parameters.
 * A value of SWT.DEFAULT indicates that no minimum width or
 * no minimum height is specified.
 * 
 * @param width a minimum width for the control
 * @param height a minimum height for the control
 */
public FormData (int width, int height) {
  this.width = width;
  this.height = height;
}

void computeSize (Control control, int wHint, int hHint, boolean flushCache) {
  if (cacheWidth != -1 && cacheHeight != -1) return;
  if (wHint == this.width && hHint == this.height) {
    if (defaultWidth == -1 || defaultHeight == -1 || wHint != defaultWhint || hHint != defaultHhint) {
      Point size =  control.computeSize (wHint, hHint, flushCache);
      defaultWhint = wHint;
      defaultHhint = hHint;
      defaultWidth = size.x;
      defaultHeight = size.y;
    }
    cacheWidth = defaultWidth;
    cacheHeight = defaultHeight;
    return;
  }
  if (currentWidth == -1 || currentHeight == -1 || wHint != currentWhint || hHint != currentHhint) {
    Point size =  control.computeSize (wHint, hHint, flushCache);
    currentWhint = wHint;
    currentHhint = hHint;
    currentWidth = size.x;
    currentHeight = size.y;
  }
  cacheWidth = currentWidth;
  cacheHeight = currentHeight;
}

void flushCache () {
  cacheWidth = cacheHeight = -1;
  defaultHeight = defaultWidth = -1;
  currentHeight = currentWidth = -1;
}

int getWidth (Control control, boolean flushCache) {
  needed = true;
  computeSize (control, width, height, flushCache);
  return cacheWidth;
}

int getHeight (Control control, boolean flushCache) {
  computeSize (control, width, height, flushCache);
  return cacheHeight;
}

FormAttachment getBottomAttachment (Control control, int spacing, boolean flushCache) {
  if (cacheBottom != null) return cacheBottom;
  if (isVisited) return cacheBottom = new FormAttachment (0, getHeight (control, flushCache));
  if (bottom == null) {
    if (top == null) return cacheBottom = new FormAttachment (0, getHeight (control, flushCache));
    return cacheBottom = getTopAttachment (control, spacing, flushCache).plus (getHeight (control, flushCache));
  }
  Control bottomControl = bottom.control;
  if (bottomControl != null) {
    if (bottomControl.isDisposed ()) {
      bottom.control = bottomControl = null;
    } else {
      if (bottomControl.getParent () != control.getParent ()) {
        bottomControl = null;
      }
    }
  }
  if (bottomControl == null) return cacheBottom = bottom;
  isVisited = true;
  FormData bottomData = (FormData) bottomControl.getLayoutData ();
  FormAttachment bottomAttachment = bottomData.getBottomAttachment (bottomControl, spacing, flushCache);
  switch (bottom.alignment) {
    case SWT.BOTTOM: 
      cacheBottom = bottomAttachment.plus (bottom.offset);
      break;
    case SWT.CENTER: {
      FormAttachment topAttachment = bottomData.getTopAttachment (bottomControl, spacing, flushCache);
      FormAttachment bottomHeight = bottomAttachment.minus (topAttachment);
      cacheBottom = bottomAttachment.minus (bottomHeight.minus (getHeight (control, flushCache)).divide (2));
      break;
    }
    default: {
      FormAttachment topAttachment = bottomData.getTopAttachment (bottomControl, spacing, flushCache);
      cacheBottom = topAttachment.plus (bottom.offset - spacing); 
      break;
    }
  }
  isVisited = false;
  return cacheBottom;
}

FormAttachment getLeftAttachment (Control control, int spacing, boolean flushCache) {
  if (cacheLeft != null) return cacheLeft;
  if (isVisited) return cacheLeft = new FormAttachment (0, 0);
  if (left == null) {
    if (right == null) return cacheLeft = new FormAttachment (0, 0);
    return cacheLeft = getRightAttachment (control, spacing, flushCache).minus (getWidth (control, flushCache));
  }
  Control leftControl = left.control;
  if (leftControl != null) {
    if (leftControl.isDisposed ()) {
      left.control = leftControl = null;
    } else {
      if (leftControl.getParent () != control.getParent ()) {
        leftControl = null;
      }
    }
  }
  if (leftControl == null) return cacheLeft = left;
  isVisited = true;
  FormData leftData = (FormData) leftControl.getLayoutData ();
  FormAttachment leftAttachment = leftData.getLeftAttachment (leftControl, spacing, flushCache);
  switch (left.alignment) {
    case SWT.LEFT:
      cacheLeft = leftAttachment.plus (left.offset);
      break;
    case SWT.CENTER: {
      FormAttachment rightAttachment = leftData.getRightAttachment (leftControl, spacing, flushCache);
      FormAttachment leftWidth = rightAttachment.minus (leftAttachment);
      cacheLeft = leftAttachment.plus (leftWidth.minus (getWidth (control, flushCache)).divide (2));
      break;
    }
    default: {
      FormAttachment rightAttachment = leftData.getRightAttachment (leftControl, spacing, flushCache);
      cacheLeft = rightAttachment.plus (left.offset + spacing); 
    }
  }
  isVisited = false; 
  return cacheLeft;
}
  
String getName () {
  String string = getClass ().getName ();
  int index = string.lastIndexOf ('.');
  if (index == -1) return string;
  return string.substring (index + 1, string.length ());
}

FormAttachment getRightAttachment (Control control, int spacing, boolean flushCache) {
  if (cacheRight != null) return cacheRight;
  if (isVisited) return cacheRight = new FormAttachment (0, getWidth (control, flushCache));
  if (right == null) {
    if (left == null) return cacheRight = new FormAttachment (0, getWidth (control, flushCache));
    return cacheRight = getLeftAttachment (control, spacing, flushCache).plus (getWidth (control, flushCache));
  }
  Control rightControl = right.control;
  if (rightControl != null) {
    if (rightControl.isDisposed ()) {
      right.control = rightControl = null;
    } else {
      if (rightControl.getParent () != control.getParent ()) {
        rightControl = null;
      }
    }
  }
  if (rightControl == null) return cacheRight = right;
  isVisited = true;
  FormData rightData = (FormData) rightControl.getLayoutData ();
  FormAttachment rightAttachment = rightData.getRightAttachment (rightControl, spacing, flushCache);
  switch (right.alignment) {
    case SWT.RIGHT: 
      cacheRight = rightAttachment.plus (right.offset);
      break;
    case SWT.CENTER: {
      FormAttachment leftAttachment = rightData.getLeftAttachment (rightControl, spacing, flushCache);
      FormAttachment rightWidth = rightAttachment.minus (leftAttachment);
      cacheRight = rightAttachment.minus (rightWidth.minus (getWidth (control, flushCache)).divide (2));
      break;
    }
    default: {
      FormAttachment leftAttachment = rightData.getLeftAttachment (rightControl, spacing, flushCache);
      cacheRight = leftAttachment.plus (right.offset - spacing);
      break;
    }
  }
  isVisited = false;
  return cacheRight;
}

FormAttachment getTopAttachment (Control control, int spacing, boolean flushCache) {
  if (cacheTop != null) return cacheTop;
  if (isVisited) return cacheTop = new FormAttachment (0, 0);
  if (top == null) {
    if (bottom == null) return cacheTop = new FormAttachment (0, 0);
    return cacheTop = getBottomAttachment (control, spacing, flushCache).minus (getHeight (control, flushCache));
  }
  Control topControl = top.control;
  if (topControl != null) {
    if (topControl.isDisposed ()) {
      top.control = topControl = null;
    } else {
      if (topControl.getParent () != control.getParent ()) {
        topControl = null;
      }
    }
  }
  if (topControl == null) return cacheTop = top;
  isVisited = true;
  FormData topData = (FormData) topControl.getLayoutData ();
  FormAttachment topAttachment = topData.getTopAttachment (topControl, spacing, flushCache);
  switch (top.alignment) {
    case SWT.TOP:
      cacheTop = topAttachment.plus (top.offset);
      break;
    case SWT.CENTER: {
      FormAttachment bottomAttachment = topData.getBottomAttachment (topControl, spacing, flushCache);
      FormAttachment topHeight = bottomAttachment.minus (topAttachment);
      cacheTop = topAttachment.plus (topHeight.minus (getHeight (control, flushCache)).divide (2));
      break;
    }
    default: {
      FormAttachment bottomAttachment = topData.getBottomAttachment (topControl, spacing, flushCache);
      cacheTop = bottomAttachment.plus (top.offset + spacing);
      break;
    }
  }
  isVisited = false;
  return cacheTop;
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the FormData object
 */
public String toString () {
  String string = getName()+" {";
  if (width != SWT.DEFAULT) string += "width="+width+" ";
  if (height != SWT.DEFAULT) string += "height="+height+" ";
  if (left != null) string += "left="+left+" ";
  if (right != null) string += "right="+right+" ";
  if (top != null) string += "top="+top+" ";
  if (bottom != null) string += "bottom="+bottom+" ";
  string = string.trim();
  string += "}";
  return string;
}

}
