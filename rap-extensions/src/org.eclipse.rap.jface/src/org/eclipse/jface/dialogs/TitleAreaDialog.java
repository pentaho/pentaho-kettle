/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Konstantin Scheglov <scheglov_ke@nlmk.ru > - Fix for bug 41172
 *     [Dialogs] Bug with Image in TitleAreaDialog
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 82064
 *     [Dialogs] TitleAreaDialog#setTitleImage cannot be called before open()
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog that has a title area for displaying a title and an image as well as
 * a common area for displaying a description, a message, or an error message.
 * <p>
 * This dialog class may be subclassed.
 */
public class TitleAreaDialog extends TrayDialog {
  /**
   * Image registry key for error message image.
   */
  public static final String DLG_IMG_TITLE_ERROR = DLG_IMG_MESSAGE_ERROR;

  /**
   * Image registry key for banner image (value
   * <code>"dialog_title_banner_image"</code>).
   */
  public static final String DLG_IMG_TITLE_BANNER = "dialog_title_banner_image";//$NON-NLS-1$

  /**
   * Message type constant used to display an info icon with the message.
   *
   * @deprecated
   */
  public final static String INFO_MESSAGE = "INFO_MESSAGE"; //$NON-NLS-1$

  /**
   * Message type constant used to display a warning icon with the message.
   *
   * @deprecated
   */
  public final static String WARNING_MESSAGE = "WARNING_MESSAGE"; //$NON-NLS-1$

  // Space between an image and a label
  private static final int H_GAP_IMAGE = 5;

  // Minimum dialog width (in dialog units)
  private static final int MIN_DIALOG_WIDTH = 350;

  // Minimum dialog height (in dialog units)
  private static final int MIN_DIALOG_HEIGHT = 150;

  private Label titleLabel;

  private Label titleImageLabel;

  private Label bottomFillerLabel;

  private Label leftFillerLabel;

  private RGB titleAreaRGB;

  Color titleAreaColor;

  private String message = ""; //$NON-NLS-1$

  private String errorMessage;

  private Text messageLabel;

  private Composite workArea;

  private Label messageImageLabel;

  private Image messageImage;

  private boolean showingError = false;

  private boolean titleImageLargest = true;

  private int messageLabelHeight;

  private Image titleAreaImage;

  private int xTrim;

  private int yTrim;

  /**
   * Instantiate a new title area dialog.
   *
   * @param parentShell
   *            the parent SWT shell
   */
  public TitleAreaDialog(Shell parentShell) {
    super(parentShell);
  }

  /*
   * @see Dialog.createContents(Composite)
   */
  protected Control createContents(Composite parent) {
    // create the overall composite
    Composite contents = new Composite(parent, SWT.NONE);
    contents.setLayoutData(new GridData(GridData.FILL_BOTH));
    // initialize the dialog units
    initializeDialogUnits(contents);
    FormLayout layout = new FormLayout();
    contents.setLayout(layout);
    // Now create a work area for the rest of the dialog
    workArea = new Composite(contents, SWT.NONE);
    GridLayout childLayout = new GridLayout();
    childLayout.marginHeight = 0;
    childLayout.marginWidth = 0;
    childLayout.verticalSpacing = 0;
    workArea.setLayout(childLayout);
    Control top = createTitleArea(contents);
    resetWorkAreaAttachments(top);
    workArea.setFont(JFaceResources.getDialogFont());
    // initialize the dialog units
    initializeDialogUnits(workArea);
    // create the dialog area and button bar
    dialogArea = createDialogArea(workArea);
    buttonBar = createButtonBar(workArea);

    // computing trim for later
    Rectangle rect = messageLabel.computeTrim(0, 0, 100, 100);
    xTrim = rect.width - 100;
    yTrim = rect.height - 100;

    // need to react to new size of title area
    getShell().addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        layoutForNewMessage(true);
      }
    });
    return contents;
  }

  /**
   * Creates and returns the contents of the upper part of this dialog (above
   * the button bar).
   * <p>
   * The <code>Dialog</code> implementation of this framework method creates
   * and returns a new <code>Composite</code> with no margins and spacing.
   * Subclasses should override.
   * </p>
   *
   * @param parent
   *            The parent composite to contain the dialog area
   * @return the dialog area control
   */
  protected Control createDialogArea(Composite parent) {
    // create the top level composite for the dialog area
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;
    layout.horizontalSpacing = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setFont(parent.getFont());
    // Build the separator line
    Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
      | SWT.SEPARATOR);
    titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    return composite;
  }

  /**
   * Creates the dialog's title area.
   *
   * @param parent
   *            the SWT parent for the title area widgets
   * @return Control with the highest x axis value.
   */
  private Control createTitleArea(Composite parent) {

    // add a dispose listener
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (titleAreaColor != null) {
          titleAreaColor.dispose();
        }
      }
    });
    // Determine the background color of the title bar
    Display display = parent.getDisplay();
    Color background;
    Color foreground;
    if (titleAreaRGB != null) {
      titleAreaColor = new Color(display, titleAreaRGB);
      background = titleAreaColor;
      foreground = null;
    } else {
      background = JFaceColors.getBannerBackground(display);
      foreground = JFaceColors.getBannerForeground(display);
    }

    parent.setBackground(background);
    final int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    final int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    // Dialog image @ right
    titleImageLabel = new Label(parent, SWT.CENTER);
    titleImageLabel.setBackground(background);
    if (titleAreaImage == null)
      titleImageLabel.setImage(JFaceResources
        .getImage(DLG_IMG_TITLE_BANNER));
    else
      titleImageLabel.setImage(titleAreaImage);

    FormData imageData = new FormData();
    imageData.top = new FormAttachment(0, 0);
    // Note: do not use horizontalSpacing on the right as that would be a
    // regression from
    // the R2.x style where there was no margin on the right and images are
    // flush to the right
    // hand side. see reopened comments in 41172
    imageData.right = new FormAttachment(100, 0); // horizontalSpacing
    titleImageLabel.setLayoutData(imageData);
    // Title label @ top, left
    titleLabel = new Label(parent, SWT.LEFT);
    JFaceColors.setColors(titleLabel, foreground, background);
    titleLabel.setFont(JFaceResources.getBannerFont());
    titleLabel.setText(" ");//$NON-NLS-1$
    FormData titleData = new FormData();
    titleData.top = new FormAttachment(0, verticalSpacing);
    titleData.right = new FormAttachment(titleImageLabel);
    titleData.left = new FormAttachment(0, horizontalSpacing);
    titleLabel.setLayoutData(titleData);
    // Message image @ bottom, left
    messageImageLabel = new Label(parent, SWT.CENTER);
    messageImageLabel.setBackground(background);
    // Message label @ bottom, center
    messageLabel = new Text(parent, SWT.WRAP | SWT.READ_ONLY);
    JFaceColors.setColors(messageLabel, foreground, background);
    messageLabel.setText(" \n "); // two lines//$NON-NLS-1$
    messageLabel.setFont(JFaceResources.getDialogFont());
    messageLabelHeight = messageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
    // RAP [if] Re-layout after TSD
    messageLabel.addListener( SWT.Resize, new Listener() {
      public void handleEvent( Event event ) {
        messageLabelHeight = messageLabel.computeSize( SWT.DEFAULT, SWT.DEFAULT ).y;
        setLayoutsForNormalMessage( verticalSpacing, horizontalSpacing );
        determineTitleImageLargest();
        layoutForNewMessage( true );
      }
    } );
    // ENDRAP [if]
    // Filler labels
    leftFillerLabel = new Label(parent, SWT.CENTER);
    leftFillerLabel.setBackground(background);
    bottomFillerLabel = new Label(parent, SWT.CENTER);
    bottomFillerLabel.setBackground(background);
    setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);
    //		determineTitleImageLargest();
    if (titleImageLargest)
      return titleImageLabel;
    return messageLabel;
  }

  /**
   * Determine if the title image is larger than the title message and message
   * area. This is used for layout decisions.
   */
  private void determineTitleImageLargest() {
    int titleY = titleImageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
    int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    int labelY = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
    labelY += verticalSpacing;
    labelY += messageLabelHeight;
    labelY += verticalSpacing;
    titleImageLargest = titleY > labelY;
  }

  /**
   * Set the layout values for the messageLabel, messageImageLabel and
   * fillerLabel for the case where there is a normal message.
   *
   * @param verticalSpacing
   *            int The spacing between widgets on the vertical axis.
   * @param horizontalSpacing
   *            int The spacing between widgets on the horizontal axis.
   */
  private void setLayoutsForNormalMessage(int verticalSpacing,
                                          int horizontalSpacing) {
    FormData messageImageData = new FormData();
    messageImageData.top = new FormAttachment(titleLabel, verticalSpacing);
    messageImageData.left = new FormAttachment(0, H_GAP_IMAGE);
    messageImageLabel.setLayoutData(messageImageData);
    FormData messageLabelData = new FormData();
    messageLabelData.top = new FormAttachment(titleLabel, verticalSpacing);
    messageLabelData.right = new FormAttachment(titleImageLabel);
    messageLabelData.left = new FormAttachment(messageImageLabel,
      horizontalSpacing);
    messageLabelData.height = messageLabelHeight;
    if (titleImageLargest)
      messageLabelData.bottom = new FormAttachment(titleImageLabel, 0,
        SWT.BOTTOM);
    messageLabel.setLayoutData(messageLabelData);
    FormData fillerData = new FormData();
    fillerData.left = new FormAttachment(0, horizontalSpacing);
    fillerData.top = new FormAttachment(messageImageLabel, 0);
    fillerData.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
    bottomFillerLabel.setLayoutData(fillerData);
    FormData data = new FormData();
    data.top = new FormAttachment(messageImageLabel, 0, SWT.TOP);
    data.left = new FormAttachment(0, 0);
    data.bottom = new FormAttachment(messageImageLabel, 0, SWT.BOTTOM);
    data.right = new FormAttachment(messageImageLabel, 0);
    leftFillerLabel.setLayoutData(data);
  }

  /**
   * The <code>TitleAreaDialog</code> implementation of this
   * <code>Window</code> methods returns an initial size which is at least
   * some reasonable minimum.
   *
   * @return the initial size of the dialog
   */
  protected Point getInitialSize() {
    Point shellSize = super.getInitialSize();
    return new Point(Math.max(
      convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH), shellSize.x),
      Math.max(convertVerticalDLUsToPixels(MIN_DIALOG_HEIGHT),
        shellSize.y));
  }

  /**
   * Retained for backward compatibility.
   *
   * Returns the title area composite. There is no composite in this
   * implementation so the shell is returned.
   *
   * @return Composite
   * @deprecated
   */
  protected Composite getTitleArea() {
    return getShell();
  }

  /**
   * Returns the title image label.
   *
   * @return the title image label
   */
  protected Label getTitleImageLabel() {
    return titleImageLabel;
  }

  /**
   * Display the given error message. The currently displayed message is saved
   * and will be redisplayed when the error message is set to
   * <code>null</code>.
   *
   * @param newErrorMessage
   *            the newErrorMessage to display or <code>null</code>
   */
  public void setErrorMessage(String newErrorMessage) {
    // Any change?
    if (errorMessage == null ? newErrorMessage == null : errorMessage
      .equals(newErrorMessage))
      return;
    errorMessage = newErrorMessage;

    // Clear or set error message.
    if (errorMessage == null) {
      if (showingError) {
        // we were previously showing an error
        showingError = false;
      }
      // show the message
      // avoid calling setMessage in case it is overridden to call
      // setErrorMessage,
      // which would result in a recursive infinite loop
      if (message == null) // this should probably never happen since
        // setMessage does this conversion....
        message = ""; //$NON-NLS-1$
      updateMessage(message);
      messageImageLabel.setImage(messageImage);
      setImageLabelVisible(messageImage != null);
    } else {
      // Add in a space for layout purposes but do not
      // change the instance variable
      String displayedErrorMessage = " " + errorMessage; //$NON-NLS-1$
      updateMessage(displayedErrorMessage);
      if (!showingError) {
        // we were not previously showing an error
        showingError = true;
        messageImageLabel.setImage(JFaceResources
          .getImage(DLG_IMG_TITLE_ERROR));
        setImageLabelVisible(true);
      }
    }
    layoutForNewMessage(false);
  }

  /**
   * Re-layout the labels for the new message.
   *
   * @param forceLayout
   *            <code>true</code> to force a layout of the shell
   */
  private void layoutForNewMessage(boolean forceLayout) {
    int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    // If there are no images then layout as normal
    if (errorMessage == null && messageImage == null) {
      setImageLabelVisible(false);
      setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);
    } else {
      messageImageLabel.setVisible(true);
      bottomFillerLabel.setVisible(true);
      leftFillerLabel.setVisible(true);
      /**
       * Note that we do not use horizontalSpacing here as when the
       * background of the messages changes there will be gaps between the
       * icon label and the message that are the background color of the
       * shell. We add a leading space elsewhere to compendate for this.
       */
      FormData data = new FormData();
      data.left = new FormAttachment(0, H_GAP_IMAGE);
      data.top = new FormAttachment(titleLabel, verticalSpacing);
      messageImageLabel.setLayoutData(data);
      data = new FormData();
      data.top = new FormAttachment(messageImageLabel, 0);
      data.left = new FormAttachment(0, 0);
      data.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
      data.right = new FormAttachment(messageImageLabel, 0, SWT.RIGHT);
      bottomFillerLabel.setLayoutData(data);
      data = new FormData();
      data.top = new FormAttachment(messageImageLabel, 0, SWT.TOP);
      data.left = new FormAttachment(0, 0);
      data.bottom = new FormAttachment(messageImageLabel, 0, SWT.BOTTOM);
      data.right = new FormAttachment(messageImageLabel, 0);
      leftFillerLabel.setLayoutData(data);
      FormData messageLabelData = new FormData();
      messageLabelData.top = new FormAttachment(titleLabel,
        verticalSpacing);
      messageLabelData.right = new FormAttachment(titleImageLabel);
      messageLabelData.left = new FormAttachment(messageImageLabel, 0);
      messageLabelData.height = messageLabelHeight;
      if (titleImageLargest)
        messageLabelData.bottom = new FormAttachment(titleImageLabel,
          0, SWT.BOTTOM);
      messageLabel.setLayoutData(messageLabelData);
    }

    if (forceLayout) {
      getShell().layout();
    } else {
      // Do not layout before the dialog area has been created
      // to avoid incomplete calculations.
      if (dialogArea != null)
        workArea.getParent().layout(true);
    }

    // RAP [if] ToolTip is missing
    //		int messageLabelUnclippedHeight = messageLabel.computeSize(messageLabel.getSize().x - xTrim, SWT.DEFAULT, true).y;
    //		boolean messageLabelClipped = messageLabelUnclippedHeight > messageLabel.getSize().y - yTrim;
    //		if (messageLabel.getData() instanceof ToolTip) {
    //			ToolTip toolTip = (ToolTip) messageLabel.getData();
    //			toolTip.hide();
    //			toolTip.deactivate();
    //			messageLabel.setData(null);
    //		}
    //		if (messageLabelClipped) {
    //			ToolTip tooltip = new ToolTip(messageLabel, ToolTip.NO_RECREATE, false) {
    //
    //				protected Composite createToolTipContentArea(Event event, Composite parent) {
    //					Composite result = new Composite(parent, SWT.NONE);
    //					result.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    //					result.setLayout(new GridLayout());
    //					Text text = new Text(result, SWT.WRAP);
    //					text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    //					text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    //					text.setText(messageLabel.getText());
    //					GridData gridData = new GridData();
    //					gridData.widthHint = messageLabel.getSize().x;
    //					text.setLayoutData(gridData);
    //					Dialog.applyDialogFont(result);
    //					return result;
    //				}
    //				public Point getLocation(Point tipSize, Event event) {
    //					return messageLabel.getShell().toDisplay(messageLabel.getLocation());
    //				}
    //			};
    //			messageLabel.setData(tooltip);
    //			tooltip.setPopupDelay(0);
    //			tooltip.activate();
    //		}
  }

  /**
   * Set the message text. If the message line currently displays an error,
   * the message is saved and will be redisplayed when the error message is
   * set to <code>null</code>.
   * <p>
   * Shortcut for <code>setMessage(newMessage, IMessageProvider.NONE)</code>
   * </p>
   * This method should be called after the dialog has been opened as it
   * updates the message label immediately.
   *
   * @param newMessage
   *            the message, or <code>null</code> to clear the message
   */
  public void setMessage(String newMessage) {
    setMessage(newMessage, IMessageProvider.NONE);
  }

  /**
   * Sets the message for this dialog with an indication of what type of
   * message it is.
   * <p>
   * The valid message types are one of <code>NONE</code>,
   * <code>INFORMATION</code>,<code>WARNING</code>, or
   * <code>ERROR</code>.
   * </p>
   * <p>
   * Note that for backward compatibility, a message of type
   * <code>ERROR</code> is different than an error message (set using
   * <code>setErrorMessage</code>). An error message overrides the current
   * message until the error message is cleared. This method replaces the
   * current message and does not affect the error message.
   * </p>
   *
   * @param newMessage
   *            the message, or <code>null</code> to clear the message
   * @param newType
   *            the message type
   */
  public void setMessage(String newMessage, int newType) {
    Image newImage = null;
    if (newMessage != null) {
      switch (newType) {
        case IMessageProvider.NONE:
          break;
        case IMessageProvider.INFORMATION:
          newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_INFO);
          break;
        case IMessageProvider.WARNING:
          newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_WARNING);
          break;
        case IMessageProvider.ERROR:
          newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_ERROR);
          break;
      }
    }
    showMessage(newMessage, newImage);
  }

  /**
   * Show the new message and image.
   *
   * @param newMessage
   * @param newImage
   */
  private void showMessage(String newMessage, Image newImage) {
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=249915
    if (newMessage == null)
      newMessage = ""; //$NON-NLS-1$

    // Any change?
    if (message.equals(newMessage) && messageImage == newImage) {
      return;
    }
    message = newMessage;

    // Message string to be shown - if there is an image then add in
    // a space to the message for layout purposes
    String shownMessage = (newImage == null) ? message : " " + message; //$NON-NLS-1$
    messageImage = newImage;
    if (!showingError) {
      // we are not showing an error
      updateMessage(shownMessage);
      messageImageLabel.setImage(messageImage);
      setImageLabelVisible(messageImage != null);
      layoutForNewMessage(false);
    }
  }

  /**
   * Update the contents of the messageLabel.
   *
   * @param newMessage
   *            the message to use
   */
  private void updateMessage(String newMessage) {
    messageLabel.setText(newMessage);
  }

  /**
   * Sets the title to be shown in the title area of this dialog.
   *
   * @param newTitle
   *            the title show
   */
  public void setTitle(String newTitle) {
    if (titleLabel == null)
      return;
    String title = newTitle;
    if (title == null)
      title = "";//$NON-NLS-1$
    titleLabel.setText(title);
  }

  /**
   * Sets the title bar color for this dialog.
   *
   * @param color
   *            the title bar color
   */
  public void setTitleAreaColor(RGB color) {
    titleAreaRGB = color;
  }

  /**
   * Sets the title image to be shown in the title area of this dialog.
   *
   * @param newTitleImage
   *            the title image to be shown
   */
  public void setTitleImage(Image newTitleImage) {

    titleAreaImage = newTitleImage;
    if (titleImageLabel != null) {
      titleImageLabel.setImage(newTitleImage);
      determineTitleImageLargest();
      Control top;
      if (titleImageLargest)
        top = titleImageLabel;
      else
        top = messageLabel;
      resetWorkAreaAttachments(top);
    }
  }

  /**
   * Make the label used for displaying error images visible depending on
   * boolean.
   *
   * @param visible
   *            If <code>true</code> make the image visible, if not then
   *            make it not visible.
   */
  private void setImageLabelVisible(boolean visible) {
    messageImageLabel.setVisible(visible);
    bottomFillerLabel.setVisible(visible);
    leftFillerLabel.setVisible(visible);
  }

  /**
   * Reset the attachment of the workArea to now attach to top as the top
   * control.
   *
   * @param top
   */
  private void resetWorkAreaAttachments(Control top) {
    FormData childData = new FormData();
    childData.top = new FormAttachment(top);
    childData.right = new FormAttachment(100, 0);
    childData.left = new FormAttachment(0, 0);
    childData.bottom = new FormAttachment(100, 0);
    workArea.setLayoutData(childData);
  }

  /**
   * Returns the current message text for this dialog.  This message is
   * displayed in the message line of the dialog when the error message
   * is <code>null</code>.  If there is a non-null error message, this
   * message is not shown, but is stored so that it can be shown in
   * the message line whenever {@link #setErrorMessage(String)} is called with
   * a <code>null</code> parameter.
   *
   * @return the message text, which is never <code>null</code>.
   *
   * @see #setMessage(String)
   * @see #setErrorMessage(String)
   *
   * @since 3.6
   */

  public String getMessage() {
    return message;
  }

  /**
   * Returns the current error message being shown in the dialog, or
   * <code>null</code> if there is no error message being shown.
   *
   * @return the error message, which may be <code>null</code>.
   *
   * @see #setErrorMessage(String)
   * @see #setMessage(String)
   *
   * @since 3.6
   */

  public String getErrorMessage() {
    return errorMessage;
  }
}