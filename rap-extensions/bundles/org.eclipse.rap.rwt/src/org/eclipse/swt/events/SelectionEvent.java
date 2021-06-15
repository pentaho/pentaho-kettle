/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.events;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;


/**
 * Instances of this class are sent as a result of
 * widgets being selected.
 * <p>
 * Note: The fields that are filled in depend on the widget.
 * </p>
 *
 * <p><strong>IMPORTANT:</strong> All <code>public static</code> members of
 * this class are <em>not</em> part of the RWT public API. They are marked
 * public only so that they can be shared within the packages provided by RWT.
 * They should never be accessed from application code.
 * </p>
 *
 * @see SelectionListener
 */
public class SelectionEvent extends TypedEvent {

  private static final long serialVersionUID = 1L;

  /**
   * The x location of the selected area.
   */
  public int x;

  /**
   * The y location of selected area.
   */
  public int y;

  /**
   * The width of selected area.
   */
  public int width;

  /**
   * The height of selected area.
   */
  public int height;

  /**
   * The state of the keyboard modifier keys at the time
   * the event was generated.
   *
   * @since 1.3
   */
  public int stateMask;

  /**
   * The text of the hyperlink that was selected.
   * This will be either the text of the hyperlink or the value of its HREF,
   * if one was specified.
   *
   * If the hyperlink was embedded in a widget using {@link org.eclipse.rap.rwt.RWT#MARKUP_ENABLED},
   * the client (especially Internet Explorer) may re-write the value to be an absolute URL.
   *
   * @see org.eclipse.swt.widgets.Link#setText(String)
   * @see org.eclipse.rap.rwt.RWT#HYPERLINK
   */
  public String text;

  /**
   * A flag indicating whether the operation should be allowed.
   * Setting this field to <code>false</code> will cancel the
   * operation, depending on the widget.
   */
  public boolean doit;

  /**
   * The item that was selected.
   */
  public Widget item;

  /**
   * Extra detail information about the selection, depending on the widget.
   * <!--
   * <p><b>Sash</b><ul>
   * <li>{@link org.eclipse.swt.SWT#DRAG}</li>
   * </ul></p><p><b>ScrollBar and Slider</b><ul>
   * <li>{@link org.eclipse.swt.SWT#DRAG}</li>
   * <li>{@link org.eclipse.swt.SWT#HOME}</li>
   * <li>{@link org.eclipse.swt.SWT#END}</li>
   * <li>{@link org.eclipse.swt.SWT#ARROW_DOWN}</li>
   * <li>{@link org.eclipse.swt.SWT#ARROW_UP}</li>
   * <li>{@link org.eclipse.swt.SWT#PAGE_DOWN}</li>
   * <li>{@link org.eclipse.swt.SWT#PAGE_UP}</li>
   * -->
   * </ul></p><p><b>Table and Tree</b><ul>
   * <li>{@link org.eclipse.swt.SWT#CHECK}</li>
   * <li>{@link org.eclipse.rap.rwt.RWT#HYPERLINK}</li>
   * <!--
   * </ul></p><p><b>Text</b><ul>
   * <li>{@link org.eclipse.swt.SWT#CANCEL}</li>
   * -->
   * </ul></p><p><b>CoolItem and ToolItem</b><ul>
   * <li>{@link org.eclipse.swt.SWT#ARROW}</li>
   * </ul></p>
   */
  public int detail;

  /**
   * Constructs a new instance of this class based on the
   * information in the given untyped event.
   *
   * @param e the untyped event containing the information
   */
  public SelectionEvent( Event e ) {
    super( e );
    item = e.item;
    x = e.x;
    y = e.y;
    width = e.width;
    height = e.height;
    detail = e.detail;
    stateMask = e.stateMask;
    text = e.text;
    doit = e.doit;
  }

  @Override
  public String toString() {
    String string = super.toString ();
    return   string.substring( 0, string.length() - 1 ) // remove trailing '}'
           + " item="
           + item
           + " detail="
           + detail
           + " x="
           + x
           + " y="
           + y
           + " width="
           + width
           + " height="
           + height
           + " stateMask="
           + stateMask
           + " text="
           + text
           + " doit="
           + doit
           + "}";
  }
}
