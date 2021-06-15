/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation, EclipseSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    EclipseSource - ongoing development
 *******************************************************************************/
package org.eclipse.swt.dnd;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.dnd.DNDEvent;


/**
 * The DragSourceEvent contains the event information passed in the methods of
 * the DragSourceListener.
 *
 * @see DragSourceListener
 * @since 1.3
 */
public class DragSourceEvent extends TypedEvent {

  private static final long serialVersionUID = 1L;

  /**
   * The operation that was performed.
   *
   * @see DND#DROP_NONE
   * @see DND#DROP_MOVE
   * @see DND#DROP_COPY
   * @see DND#DROP_LINK
   * @see DND#DROP_TARGET_MOVE
   */
  public int detail;

  /**
   * In dragStart, the doit field determines if the drag and drop operation
   * should proceed; in dragFinished, the doit field indicates whether the
   * operation was performed successfully.
   * <p>
   * </p>
   * In dragStart:
   * <p>
   * Flag to determine if the drag and drop operation should proceed. The
   * application can set this value to false to prevent the drag from starting.
   * Set to true by default.
   * </p>
   * <p>
   * In dragSetData:
   * </p>
   * <p>
   * This will be set to true when the call to dragSetData is made. Set it to
   * false to cancel the drag.
   * </p>
   * <p>
   * In dragFinished:
   * </p>
   * <p>
   * Flag to indicate if the operation was performed successfully. True if the
   * operation was performed successfully.
   * </p>
   */
  public boolean doit;

  /**
   * In dragStart, the x coordinate (relative to the control) of the position
   * the mouse went down to start the drag.
   */
  public int x;

  /**
   * In dragStart, the y coordinate (relative to the control) of the position
   * the mouse went down to start the drag.
   */
  public int y;

  /**
   * The type of data requested. Data provided in the data field must be of the
   * same type.
   */
  public TransferData dataType;

  /**
   * The drag source image to be displayed during the drag.
   * <p>
   * A value of null indicates that no drag image will be displayed.
   * </p>
   * <p>
   * The default value is null.
   * </p>
   */
  public Image image;

  /**
   * In dragStart, the x offset (relative to the image) where the drag source
   * image will be displayed.
   */
  public int offsetX;

  /**
   * In dragStart, the y offset (relative to the image) where the drag source
   * image will be displayed.
   */
  public int offsetY;

  /**
   * Constructs a new instance of this class based on the
   * information in the given untyped event.
   *
   * @param e the untyped event containing the information
   * @since 2.0
   */
  public DragSourceEvent( DNDEvent e ) {
    super( e );
    this.data = e.data;
    this.detail = e.detail;
    this.doit = e.doit;
    this.dataType = e.dataType;
    this.x = e.x;
    this.y = e.y;
    this.image = e.image;
    this.offsetX = e.offsetX;
    this.offsetY = e.offsetY;
  }

  void updateEvent( DNDEvent e ) {
    e.widget = this.widget;
    e.time = this.time;
    e.data = this.data;
    e.detail = this.detail;
    e.doit = this.doit;
    e.dataType = this.dataType;
    e.x = this.x;
    e.y = this.y;
    e.image = this.image;
    e.offsetX = this.offsetX;
    e.offsetY = this.offsetY;
  }

  @Override
  public String toString() {
    String string = super.toString();
    return string.substring( 0, string.length() - 1 ) // remove trailing '}'
           + " operation="
           + detail
           + " type="
           + ( dataType != null ? dataType.type : 0 )
           + " doit="
           + doit
           + "}";
  }

}
