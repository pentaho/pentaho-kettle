/*******************************************************************************
 * Copyright (c) 2009, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract - initial implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.dnd;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.internal.dnd.DNDEvent;
import org.eclipse.swt.widgets.Widget;

/**
 * The DropTargetEvent contains the event information passed in the methods of
 * the DropTargetListener.
 *
 * @since 1.3
 */
public class DropTargetEvent extends TypedEvent {

  private static final long serialVersionUID = 1L;

  /**
   * The x-cordinate of the cursor relative to the <code>Display</code>
   */
  public int x;

  /**
   * The y-cordinate of the cursor relative to the <code>Display</code>
   */
  public int y;

  /**
   * The operation being performed.
   *
   * @see DND#DROP_NONE
   * @see DND#DROP_MOVE
   * @see DND#DROP_COPY
   * @see DND#DROP_LINK
   * @see DND#DROP_DEFAULT
   */

  public int detail;
  /**
   * A bitwise OR'ing of the operations that the DragSource can support (e.g.
   * DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK). The detail value must be a
   * member of this list or DND.DROP_NONE.
   *
   * @see DND#DROP_NONE
   * @see DND#DROP_MOVE
   * @see DND#DROP_COPY
   * @see DND#DROP_LINK
   * @see DND#DROP_DEFAULT
   */
  public int operations;

  /**
   * A bitwise OR'ing of the drag under effect feedback to be displayed to the
   * user (e.g. DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL |
   * DND.FEEDBACK_EXPAND).
   * <p>
   * A value of DND.FEEDBACK_NONE indicates that no drag under effect will be
   * displayed.
   * </p>
   * <p>
   * Feedback effects will only be applied if they are applicable.
   * </p>
   * <p>
   * The default value is DND.FEEDBACK_SELECT.
   * </p>
   *
   * @see DND#FEEDBACK_NONE
   * @see DND#FEEDBACK_SELECT
   * @see DND#FEEDBACK_INSERT_BEFORE
   * @see DND#FEEDBACK_INSERT_AFTER
   * @see DND#FEEDBACK_SCROLL
   * @see DND#FEEDBACK_EXPAND
   */
  public int feedback;

  /**
   * If the associated control is a table or tree, this field contains the item
   * located at the cursor coordinates.
   */
  public Widget item;

  /**
   * The type of data that will be dropped.
   */
  public TransferData currentDataType;

  /**
   * A list of the types of data that the DragSource is capable of providing.
   * The currentDataType must be a member of this list.
   */
  public TransferData[] dataTypes;

  /**
   * Constructs a new instance of this class based on the
   * information in the given untyped event.
   *
   * @param e the untyped event containing the information
   * @since 2.0
   */
  public DropTargetEvent( DNDEvent e ) {
    super( e );
    this.data = e.data;
    this.x = e.x;
    this.y = e.y;
    this.detail = e.detail;
    this.currentDataType = e.dataType;
    this.dataTypes = e.dataTypes;
    this.operations = e.operations;
    this.feedback = e.feedback;
    this.item = e.item;
  }

  void updateEvent( DNDEvent e ) {
    e.widget = this.widget;
    e.time = this.time;
    e.data = this.data;
    e.x = this.x;
    e.y = this.y;
    e.detail = this.detail;
    e.dataType = this.currentDataType;
    e.dataTypes = this.dataTypes;
    e.operations = this.operations;
    e.feedback = this.feedback;
    e.item = this.item;
  }

  @Override
  public String toString() {
    String string = super.toString();
    StringBuilder sb = new StringBuilder();
    sb.append( string.substring( 0, string.length() - 1 ) ); // remove trailing '}'
    sb.append( " x=" );
    sb.append( x );
    sb.append( " y=" );
    sb.append( y );
    sb.append( " item=" );
    sb.append( item );
    sb.append( " operations=" );
    sb.append( operations );
    sb.append( " operation=" );
    sb.append( detail );
    sb.append( " feedback=" );
    sb.append( feedback );
    sb.append( " dataTypes={ " );
    if( dataTypes != null ) {
      for( int i = 0; i < dataTypes.length; i++ ) {
        sb.append( dataTypes[ i ].type );
        sb.append( ' ' );
      }
    }
    sb.append( '}' );
    sb.append( " currentDataType=" );
    sb.append( currentDataType != null ? currentDataType.type : '0' );
    sb.append( '}' );
    return sb.toString();
  }

}
