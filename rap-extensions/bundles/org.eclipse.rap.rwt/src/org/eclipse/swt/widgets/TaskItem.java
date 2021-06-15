/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;


/**
 * Instances of this class represent a task item.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 1.4
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TaskItem extends Item {

  private TaskBar parent;

  /**
   * Constructs a new instance of this class given its parent
   * (which must be a <code>Tray</code>) and a style value
   * describing its behavior and appearance. The item is added
   * to the end of the items maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in
   * class <code>SWT</code> which is applicable to instances of this
   * class, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>SWT</code> style constants. The class description
   * lists the style constants that are applicable to the class.
   * Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a composite control which will be the parent of the new instance (cannot be null)
   * @param style the style of control to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  TaskItem( TaskBar parent, int style ) {
    super( parent, style );
    this.parent = parent;
  }

  /**
   * Returns the receiver's pop up menu if it has one, or null
   * if it does not.
   *
   * @return the receiver's menu
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Menu getMenu() {
    checkWidget();
    return null;
  }

  /**
   * Returns the receiver's overlay image if it has one, or null
   * if it does not.
   *
   * @return the receiver's overlay image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Image getOverlayImage() {
    checkWidget();
    return null;
  }

  /**
   * Returns the receiver's overlay text, which will be an empty
   * string if it has never been set.
   *
   * @return the receiver's overlay text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getOverlayText() {
    checkWidget();
    return "";
  }

  /**
   * Returns the receiver's parent, which must be a <code>TaskBar</code>.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   */
  public TaskBar getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Returns the receiver's progress.
   *
   * @return the receiver's progress
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getProgress() {
    checkWidget();
    return 0;
  }

  /**
   * Returns the receiver's progress state.
   *
   * @return the receiver's progress state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getProgressState() {
    checkWidget();
    return 0;
  }

  /**
   * Sets the receiver's pop up menu to the argument. The way the menu is
   * shown is platform specific.
   *
   * <p>
   * This feature might not be available for the receiver on all
   * platforms. The application code can check if it is supported
   * by calling the respective get method. When the feature is not
   * available, the get method will always return the NULL.</p>
   *
   * <p>
   * For better cross platform support, the application code should
   * set this feature on the <code>TaskItem</code> for application.<br>
   * On Windows, this feature will only work on RCP applications.</p>
   *
   * <p>
   * The menu should be fully created before this method is called.
   * Dynamic changes to the menu after the method is called will not be reflected
   * in the native menu.</p>
   *
   * @param menu the new pop up menu
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_MENU_NOT_POP_UP - the menu is not a pop up menu</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setMenu( Menu menu ) {
    checkWidget();
  }

  /**
   * Sets the receiver's overlay image, which may be null
   * indicating that no image should be displayed. The bounds
   * for the overlay image is determined by the platform and in
   * general it should be a small image.
   *
   * <p>
   * This feature might not be available for the receiver on all
   * platforms. The application code can check if it is supported
   * by calling the respective get method. When the feature is not
   * available, the get method will always return the NULL.</p>
   *
   * <p>
   * For better cross platform support, the application code should
   * first try to set this feature on the <code>TaskItem</code> for the
   * main shell then on the <code>TaskItem</code> for the application.</p>
   *
   * @param overlayImage the new overlay image (may be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the overlayImage has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setOverlayImage( Image overlayImage ) {
    checkWidget();
  }

  /**
   * Sets the receiver's overlay text. The space available to display the
   * overlay text is platform dependent and in general it should be no longer
   * than a few characters.
   *
   * <p>
   * This feature might not be available for the receiver on all
   * platforms. The application code can check if it is supported
   * by calling the respective get method. When the feature is not
   * available, the get method will always return an empty string.</p>
   *
   * <p>
   * For better cross platform support, the application code should
   * first try to set this feature on the <code>TaskItem</code> for the
   * main shell then on the <code>TaskItem</code> for the application.</p>
   *
   * @param overlayText the new overlay text
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the overlayText is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setOverlayText( String overlayText ) {
    checkWidget();
  }

  /**
   * Sets the receiver's progress, the progress represents a percentage and
   * should be in range from 0 to 100. The progress is only shown when the progress
   * state is different than <code>SWT#DEFAULT</code>.
   *
   * <p>
   * This feature might not be available for the receiver on all
   * platforms. The application code can check if it is supported
   * by calling the respective get method. When the feature is not
   * available, the get method will always return zero.</p>
   *
   * <p>
   * For better cross platform support, the application code should
   * first try to set this feature on the <code>TaskItem</code> for the
   * main shell then on the <code>TaskItem</code> for the application.</p>
   *
   * @param progress the new progress
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * #see {@link #setProgressState(int)}
   */
  public void setProgress( int progress ) {
    checkWidget();
  }

  /**
   * Sets the receiver's progress state, the state can be one of
   * the following:
   * <p><ul>
   * <li>{@link SWT#DEFAULT}</li>
   * <li>{@link SWT#NORMAL}</li>
   * <li>{@link SWT#PAUSED}</li>
   * <li>{@link SWT#ERROR}</li>
   * <li>{@link SWT#INDETERMINATE}</li>
   * </ul></p>
   *
   * The percentage of progress shown by the states <code>SWT#NORMAL</code>, <code>SWT#PAUSED</code>,
   * <code>SWT#ERROR</code> is set with <code>setProgress()</code>. <br>
   * The state <code>SWT#DEFAULT</code> indicates that no progress should be shown.
   *
   * <p>
   * This feature might not be available for the receiver on all
   * platforms. The application code can check if it is supported
   * by calling the respective get method. When the feature is not
   * available, the get method will always return <code>SWT#DEFAULT</code>.</p>
   *
   * <p>
   * For better cross platform support, the application code should
   * first try to set this feature on the <code>TaskItem</code> for the
   * main shell then on the <code>TaskItem</code> for the application.</p>
   *
   * @param progressState the new progress state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * #see {@link #setProgress(int)}
   */
  public void setProgressState( int progressState ) {
    checkWidget();
  }

}
