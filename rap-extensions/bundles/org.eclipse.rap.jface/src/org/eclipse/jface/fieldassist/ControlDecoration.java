/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource - ongoing development
 *******************************************************************************/
package org.eclipse.jface.fieldassist;
//
import java.io.Serializable;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.ControlDecorator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * ControlDecoration renders an image decoration near a control. It allows
 * clients to specify an image and a position for the image relative to the
 * control. A ControlDecoration may be assigned description text, which can
 * optionally be shown when the user hovers over the image. Clients can decorate
 * any kind of control.
 * <p>
 * Decoration images always appear on the left or right side of the field, never
 * above or below it. Decorations can be positioned at the top, center, or
 * bottom of either side of the control. Future implementations may provide
 * additional positioning options for decorations.
 * <p>
 * ControlDecoration renders the image adjacent to the specified (already
 * created) control, with no guarantee that it won't be clipped or otherwise
 * obscured or overlapped by adjacent controls, including another
 * ControlDecoration placed in the same location. Clients should ensure that
 * there is adequate space adjacent to the control to show the decoration
 * properly.
 * <p>
 * Clients using ControlDecoration should typically ensure that enough margin
 * space is reserved for a decoration by altering the layout data margins,
 * although this is not assumed or required by the ControlDecoration
 * implementation.
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 *
 * @since 1.3
 *
 * @see FieldDecoration
 * @see FieldDecorationRegistry
 */
public class ControlDecoration implements Serializable {

  private ControlDecorator decorator;

  /**
   * Construct a ControlDecoration for decorating the specified control at the
   * specified position relative to the control. Render the decoration on top
   * of any Control that happens to appear at the specified location.
   * <p>
   * SWT constants are used to specify the position of the decoration relative
   * to the control. The position should include style bits describing both
   * the vertical and horizontal orientation. <code>SWT.LEFT</code> and
   * <code>SWT.RIGHT</code> describe the horizontal placement of the
   * decoration relative to the control, and the constants
   * <code>SWT.TOP</code>, <code>SWT.CENTER</code>, and
   * <code>SWT.BOTTOM</code> describe the vertical alignment of the
   * decoration relative to the control. Decorations always appear on either
   * the left or right side of the control, never above or below it. For
   * example, a decoration appearing on the left side of the field, at the
   * top, is specified as SWT.LEFT | SWT.TOP. If no position style bits are
   * specified, the control decoration will be positioned to the left and
   * center of the control (<code>SWT.LEFT | SWT.CENTER</code>).
   * </p>
   *
   * @param control
   *            the control to be decorated
   * @param position
   *            bit-wise or of position constants (<code>SWT.TOP</code>,
   *            <code>SWT.BOTTOM</code>, <code>SWT.LEFT</code>,
   *            <code>SWT.RIGHT</code>, and <code>SWT.CENTER</code>).
   */
  public ControlDecoration( Control control, int position ) {
    this( control, position, null );
  }

  /**
   * Construct a ControlDecoration for decorating the specified control at the
   * specified position relative to the control. Render the decoration only on
   * the specified Composite or its children. The decoration will be clipped
   * if it does not appear within the visible bounds of the composite or its
   * child composites.
   * <p>
   * SWT constants are used to specify the position of the decoration relative
   * to the control. The position should include style bits describing both
   * the vertical and horizontal orientation. <code>SWT.LEFT</code> and
   * <code>SWT.RIGHT</code> describe the horizontal placement of the
   * decoration relative to the control, and the constants
   * <code>SWT.TOP</code>, <code>SWT.CENTER</code>, and
   * <code>SWT.BOTTOM</code> describe the vertical alignment of the
   * decoration relative to the control. Decorations always appear on either
   * the left or right side of the control, never above or below it. For
   * example, a decoration appearing on the left side of the field, at the
   * top, is specified as SWT.LEFT | SWT.TOP. If no position style bits are
   * specified, the control decoration will be positioned to the left and
   * center of the control (<code>SWT.LEFT | SWT.CENTER</code>).
   * </p>
   *
   * @param control
   *            the control to be decorated
   * @param position
   *            bit-wise or of position constants (<code>SWT.TOP</code>,
   *            <code>SWT.BOTTOM</code>, <code>SWT.LEFT</code>,
   *            <code>SWT.RIGHT</code>, and <code>SWT.CENTER</code>).
   * @param composite
   *            The SWT composite within which the decoration should be
   *            rendered. The decoration will be clipped to this composite,
   *            but it may be rendered on a child of the composite. The
   *            decoration will not be visible if the specified composite or
   *            its child composites are not visible in the space relative to
   *            the control, where the decoration is to be rendered. If this
   *            value is <code>null</code>, then the decoration will be
   *            rendered on whichever composite (or composites) are located in
   *            the specified position.
   */
  public ControlDecoration( Control control, int position, Composite composite ) {
    decorator = new ControlDecorator( control, position, composite );
  }

  /**
   * Adds the listener to the collection of listeners who will be notified
   * when the decoration is selected, by sending it one of the messages
   * defined in the <code>SelectionListener</code> interface.
   * <p>
   * <code>widgetSelected</code> is called when the decoration is selected
   * (by mouse click). <code>widgetDefaultSelected</code> is called when the
   * decoration is double-clicked.
   * </p>
   * <p>
   * The <code>widget</code> field in the SelectionEvent will contain the
   * Composite on which the decoration is rendered that received the click.
   * The <code>x</code> and <code>y</code> fields will be in coordinates
   * relative to that widget. The <code>data</code> field will contain the
   * decoration that received the event.
   * </p>
   *
   * @param listener
   *            the listener which should be notified
   *
   * @see org.eclipse.swt.events.SelectionListener
   * @see org.eclipse.swt.events.SelectionEvent
   * @see #removeSelectionListener
   */
  public void addSelectionListener( SelectionListener listener ) {
    decorator.addSelectionListener( listener );
  }

  /**
   * Removes the listener from the collection of listeners who will be
   * notified when the decoration is selected.
   *
   * @param listener
   *            the listener which should no longer be notified. This message
   *            has no effect if the listener was not previously added to the
   *            receiver.
   *
   * @see org.eclipse.swt.events.SelectionListener
   * @see #addSelectionListener
   */
  public void removeSelectionListener( SelectionListener listener ) {
    decorator.removeSelectionListener( listener );
  }

  /**
   * Dispose this ControlDecoration. Unhook any listeners that have been
   * installed on the target control. This method has no effect if the
   * receiver is already disposed.
   */
  public void dispose() {
    decorator.dispose();
  }

  /**
   * Get the control that is decorated by the receiver.
   *
   * @return the Control decorated by the receiver. May be <code>null</code>
   *         if the control has been uninstalled.
   */
  public Control getControl() {
    return decorator.getControl();
  }

  /**
   * Show the control decoration. This message has no effect if the decoration
   * is already showing. If {@link #setShowOnlyOnFocus(boolean)} is set to
   * <code>true</code>, the decoration will only be shown if the control
   * has focus.
   */
  public void show() {
    decorator.show();
  }

  /**
   * Hide the control decoration and any associated hovers. This message has
   * no effect if the decoration is already hidden.
   */
  public void hide() {
    decorator.hide();
  }

  /**
   * Get the description text that may be shown in a hover for this
   * decoration.
   *
   * @return the text to be shown as a description for the decoration, or
   *         <code>null</code> if none has been set.
   */
  public String getDescriptionText() {
    return decorator.getText();
  }

  /**
   * Set the description text that may be shown in a hover for this
   * decoration.
   *
   * @param text
   *            the text to be shown as a description for the decoration, or
   *            <code>null</code> if none has been set.
   */
  public void setDescriptionText( String text ) {
    if( !decorator.isDisposed() ) {
      decorator.setText( text );
    }
  }

  /**
   * Get the image shown in this control decoration.
   *
   * @return the image to be shown adjacent to the control, or
   *         <code>null</code> if one has not been set.
   */
  public Image getImage() {
    return decorator.getImage();
  }

  /**
   * Set the image shown in this control decoration. Update the rendered
   * decoration.
   *
   * @param image
   *            the image to be shown adjacent to the control. Should never be
   *            <code>null</code>.
   */
  public void setImage( Image image ) {
    if( !decorator.isDisposed() ) {
      decorator.setImage( image );
    }
  }

  /**
   * Get the boolean that controls whether the decoration is shown only when
   * the control has focus. The default value of this setting is
   * <code>false</code>.
   *
   * @return <code>true</code> if the decoration should only be shown when
   *         the control has focus, and <code>false</code> if it should
   *         always be shown. Note that if the control is not capable of
   *         receiving focus (<code>SWT.NO_FOCUS</code>), then the
   *         decoration will never show when this value is <code>true</code>.
   */
  public boolean getShowOnlyOnFocus() {
    return decorator.getShowOnlyOnFocus();
  }

  /**
   * Set the boolean that controls whether the decoration is shown only when
   * the control has focus. The default value of this setting is
   * <code>false</code>.
   *
   * @param showOnlyOnFocus
   *            <code>true</code> if the decoration should only be shown
   *            when the control has focus, and <code>false</code> if it
   *            should always be shown. Note that if the control is not
   *            capable of receiving focus (<code>SWT.NO_FOCUS</code>),
   *            then the decoration will never show when this value is
   *            <code>true</code>.
   */
  public void setShowOnlyOnFocus( boolean showOnlyOnFocus ) {
    decorator.setShowOnlyOnFocus( showOnlyOnFocus );
  }

  /**
   * Get the boolean that controls whether the decoration's description text
   * should be shown in a hover when the user hovers over the decoration. The
   * default value of this setting is <code>true</code>.
   *
   * @return <code>true</code> if a hover popup containing the decoration's
   *         description text should be shown when the user hovers over the
   *         decoration, and <code>false</code> if a hover should not be
   *         shown.
   */
  public boolean getShowHover() {
    return decorator.getShowHover();
  }

  /**
   * Set the boolean that controls whether the decoration's description text
   * should be shown in a hover when the user hovers over the decoration. The
   * default value of this setting is <code>true</code>.
   *
   * @param showHover
   *            <code>true</code> if a hover popup containing the
   *            decoration's description text should be shown when the user
   *            hovers over the decoration, and <code>false</code> if a
   *            hover should not be shown.
   */
  public void setShowHover( boolean showHover ) {
    decorator.setShowHover( showHover );
  }

  /**
   * Get the margin width in pixels that should be used between the decorator
   * and the horizontal edge of the control. The default value of this setting
   * is <code>0</code>.
   *
   * @return the number of pixels that should be reserved between the
   *         horizontal edge of the control and the adjacent edge of the
   *         decoration.
   */
  public int getMarginWidth() {
    return decorator.getMarginWidth();
  }

  /**
   * Set the margin width in pixels that should be used between the decorator
   * and the horizontal edge of the control. The default value of this setting
   * is <code>0</code>.
   *
   * @param marginWidth
   *            the number of pixels that should be reserved between the
   *            horizontal edge of the control and the adjacent edge of the
   *            decoration.
   */
  public void setMarginWidth( int marginWidth ) {
    decorator.setMarginWidth( marginWidth );
  }

  /**
   * Return a boolean indicating whether the decoration is visible. This
   * method considers the visibility state of the decoration (
   * {@link #hide()} and {@link #show()}), the visibility state of the
   * associated control ({@link Control#isVisible()}), and the focus state
   * of the control if applicable ({@link #setShowOnlyOnFocus(boolean)}.
   * When this method returns <code>true</code>, it means that the decoration
   * should be visible. However, this method does not consider the case where
   * the decoration should be visible, but is obscured by another window or
   * control, or positioned off the screen. In these cases, the decoration
   * will still be considered visible.
   *
   * @return <code>true</code> if the decoration is visible, and
   *         <code>false</code> if it is not.
   *
   * @see #setShowOnlyOnFocus(boolean)
   * @see #hide()
   * @see #show()
   */
  public boolean isVisible() {
    return decorator.isVisible();
  }

  /**
   * Controls whether the use of <em>markup</em> in description text is enabled. The call to
   * <code>enableMarkup()</code> must be placed directly after the control decoration is created.
   *
   * @rwtextension This method is not available in RCP.
   * @since 3.5
   */
  public void enableMarkup() {
    decorator.setData( RWT.MARKUP_ENABLED, true );
  }

}
