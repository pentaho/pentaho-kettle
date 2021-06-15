/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.action;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;

/**
 * The standard abstract implementation of an action.
 * <p>
 * Subclasses must implement the <code>IAction.run</code> method to carry out
 * the action's semantics.
 * </p>
 */
public abstract class Action extends AbstractAction implements IAction {

	private static final IMenuCreator VAL_DROP_DOWN_MENU = new IMenuCreator() {
		public void dispose() {
			// do nothing
		}

		public Menu getMenu(Control parent) {
			// do nothing
			return null;
		}

		public Menu getMenu(Menu parent) {
			// do nothing
			return null;
		}
	};

	/*
	 * The list of default values the action can have. These values will
	 * determine the style of the action.
	 */
	private static final String VAL_PUSH_BTN = "PUSH_BTN"; //$NON-NLS-1$

	private static final Integer VAL_RADIO_BTN_OFF = new Integer(0);

	private static final Integer VAL_RADIO_BTN_ON = new Integer(1);

	private static final Boolean VAL_TOGGLE_BTN_OFF = Boolean.FALSE;

	private static final Boolean VAL_TOGGLE_BTN_ON = Boolean.TRUE;

	/**
	 * Converts an accelerator key code to a string representation.
	 * 
	 * @param keyCode
	 *            the key code to be translated
	 * @return a string representation of the key code
	 */
	public static String convertAccelerator(int keyCode) {
		return LegacyActionTools.convertAccelerator(keyCode);
	}

	/**
	 * Parses the given accelerator text, and converts it to an accelerator key
	 * code.
	 * 
	 * @param acceleratorText
	 *            the accelerator text
	 * @return the SWT key code, or 0 if there is no accelerator
	 */
	public static int convertAccelerator(String acceleratorText) {
		return LegacyActionTools.convertAccelerator(acceleratorText);
	}

	/**
	 * Maps a standard keyboard key name to an SWT key code. Key names are
	 * converted to upper case before comparison. If the key name is a single
	 * letter, for example "S", its character code is returned.
	 * <p>
	 * The following key names are known (case is ignored):
	 * <ul>
	 * <li><code>"BACKSPACE"</code></li>
	 * <li><code>"TAB"</code></li>
	 * <li><code>"RETURN"</code></li>
	 * <li><code>"ENTER"</code></li>
	 * <li><code>"ESC"</code></li>
	 * <li><code>"ESCAPE"</code></li>
	 * <li><code>"DELETE"</code></li>
	 * <li><code>"SPACE"</code></li>
	 * <li><code>"ARROW_UP"</code>, <code>"ARROW_DOWN"</code>,
	 * <code>"ARROW_LEFT"</code>, and <code>"ARROW_RIGHT"</code></li>
	 * <li><code>"PAGE_UP"</code> and <code>"PAGE_DOWN"</code></li>
	 * <li><code>"HOME"</code></li>
	 * <li><code>"END"</code></li>
	 * <li><code>"INSERT"</code></li>
	 * <li><code>"F1"</code>, <code>"F2"</code> through <code>"F12"</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @param token
	 *            the key name
	 * @return the SWT key code, <code>-1</code> if no match was found
	 * @see org.eclipse.swt.SWT
	 */
	public static int findKeyCode(String token) {
		return LegacyActionTools.findKeyCode(token);
	}

	/**
	 * Maps an SWT key code to a standard keyboard key name. The key code is
	 * stripped of modifiers (SWT.CTRL, SWT.ALT, SWT.SHIFT, and SWT.COMMAND). If
	 * the key code is not an SWT code (for example if it a key code for the key
	 * 'S'), a string containing a character representation of the key code is
	 * returned.
	 * 
	 * @param keyCode
	 *            the key code to be translated
	 * @return the string representation of the key code
	 * @see org.eclipse.swt.SWT
	 */
	public static String findKeyString(int keyCode) {
		return LegacyActionTools.findKeyString(keyCode);
	}

	/**
	 * Maps standard keyboard modifier key names to the corresponding SWT
	 * modifier bit. The following modifier key names are recognized (case is
	 * ignored): <code>"CTRL"</code>, <code>"SHIFT"</code>,
	 * <code>"ALT"</code>, and <code>"COMMAND"</code>. The given modifier
	 * key name is converted to upper case before comparison.
	 * 
	 * @param token
	 *            the modifier key name
	 * @return the SWT modifier bit, or <code>0</code> if no match was found
	 * @see org.eclipse.swt.SWT
	 */
	public static int findModifier(String token) {
		return LegacyActionTools.findModifier(token);
	}

	/**
	 * Returns a string representation of an SWT modifier bit (SWT.CTRL,
	 * SWT.ALT, SWT.SHIFT, and SWT.COMMAND). Returns <code>null</code> if the
	 * key code is not an SWT modifier bit.
	 * 
	 * @param keyCode
	 *            the SWT modifier bit to be translated
	 * @return the string representation of the SWT modifier bit, or
	 *         <code>null</code> if the key code was not an SWT modifier bit
	 * @see org.eclipse.swt.SWT
	 */
	public static String findModifierString(int keyCode) {
		return LegacyActionTools.findModifierString(keyCode);
	}

	/**
	 * Convenience method for removing any optional accelerator text from the
	 * given string. The accelerator text appears at the end of the text, and is
	 * separated from the main part by a single tab character <code>'\t'</code>.
	 * 
	 * @param text
	 *            the text
	 * @return the text sans accelerator
	 */
	public static String removeAcceleratorText(String text) {
		return LegacyActionTools.removeAcceleratorText(text);
	}

	/**
	 * Convenience method for removing any mnemonics from the given string. For
	 * example, <code>removeMnemonics("&Open")</code> will return
	 * <code>"Open"</code>.
	 * 
	 * @param text
	 *            the text
	 * @return the text sans mnemonics
	 * 
	 */
	public static String removeMnemonics(String text) {
		return LegacyActionTools.removeMnemonics(text);
	}

	/**
	 * This action's accelerator; <code>0</code> means none.
	 */
	private int accelerator = 0;

	/**
	 * This action's action definition id, or <code>null</code> if none.
	 */
	private String actionDefinitionId;

	/**
	 * This action's description, or <code>null</code> if none.
	 */
	private String description;

	/**
	 * This action's disabled image, or <code>null</code> if none.
	 */
	private ImageDescriptor disabledImage;

	/**
	 * Indicates this action is enabled.
	 */
	private boolean enabled = true;

	/**
	 * An action's help listener, or <code>null</code> if none.
	 */
	private HelpListener helpListener;

	/**
	 * This action's hover image, or <code>null</code> if none.
	 */
	private ImageDescriptor hoverImage;

	/**
	 * This action's id, or <code>null</code> if none.
	 */
	private String id;

	/**
	 * This action's image, or <code>null</code> if none.
	 */
	private ImageDescriptor image;

	/**
	 * This action's text, or <code>null</code> if none.
	 */
	private String text;

	/**
	 * This action's tool tip text, or <code>null</code> if none.
	 */
	private String toolTipText;

	/**
	 * Holds the action's menu creator (an IMenuCreator) or checked state (a
	 * Boolean for toggle button, or an Integer for radio button), or
	 * <code>null</code> if neither have been set.
	 * <p>
	 * The value of this field affects the value of <code>getStyle()</code>.
	 * </p>
	 */
	private Object value = null;

	/**
	 * Creates a new action with no text and no image.
	 * <p>
	 * Configure the action later using the set methods.
	 * </p>
	 */
	protected Action() {
		// do nothing
	}

	/**
	 * Creates a new action with the given text and no image. Calls the zero-arg
	 * constructor, then <code>setText</code>.
	 * 
	 * @param text
	 *            the string used as the text for the action, or
	 *            <code>null</code> if there is no text
	 * @see #setText
	 */
	protected Action(String text) {
		this();
		setText(text);
	}

	/**
	 * Creates a new action with the given text and image. Calls the zero-arg
	 * constructor, then <code>setText</code> and
	 * <code>setImageDescriptor</code>.
	 * 
	 * @param text
	 *            the action's text, or <code>null</code> if there is no text
	 * @param image
	 *            the action's image, or <code>null</code> if there is no
	 *            image
	 * @see #setText
	 * @see #setImageDescriptor
	 */
	protected Action(String text, ImageDescriptor image) {
		this(text);
		setImageDescriptor(image);
	}

	/**
	 * Creates a new action with the given text and style.
	 * 
	 * @param text
	 *            the action's text, or <code>null</code> if there is no text
	 * @param style
	 *            one of <code>AS_PUSH_BUTTON</code>,
	 *            <code>AS_CHECK_BOX</code>, <code>AS_DROP_DOWN_MENU</code>,
	 *            <code>AS_RADIO_BUTTON</code>, and
	 *            <code>AS_UNSPECIFIED</code>.
	 */
	protected Action(String text, int style) {
		this(text);
		switch (style) {
		case AS_PUSH_BUTTON:
			value = VAL_PUSH_BTN;
			break;
		case AS_CHECK_BOX:
			value = VAL_TOGGLE_BTN_OFF;
			break;
		case AS_DROP_DOWN_MENU:
			value = VAL_DROP_DOWN_MENU;
			break;
		case AS_RADIO_BUTTON:
			value = VAL_RADIO_BTN_OFF;
			break;
		}
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public int getAccelerator() {
		return accelerator;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 * 
	 */
	public String getActionDefinitionId() {
		return actionDefinitionId;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public String getDescription() {
		if (description != null) {
			return description;
		}
		return getToolTipText();
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		return disabledImage;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public HelpListener getHelpListener() {
		return helpListener;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public ImageDescriptor getHoverImageDescriptor() {
		return hoverImage;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public ImageDescriptor getImageDescriptor() {
		return image;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public IMenuCreator getMenuCreator() {
		// The default drop down menu value is only used
		// to mark this action requested style. So do not
		// return it. For backward compatibility reasons.
		if (value == VAL_DROP_DOWN_MENU) {
			return null;
		}
		if (value instanceof IMenuCreator) {
			return (IMenuCreator) value;
		}
		return null;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public int getStyle() {
		// Infer the style from the value field.
		if (value == VAL_PUSH_BTN || value == null) {
			return AS_PUSH_BUTTON;
		}
		if (value == VAL_TOGGLE_BTN_ON || value == VAL_TOGGLE_BTN_OFF) {
			return AS_CHECK_BOX;
		}
		if (value == VAL_RADIO_BTN_ON || value == VAL_RADIO_BTN_OFF) {
			return AS_RADIO_BUTTON;
		}
		if (value instanceof IMenuCreator) {
			return AS_DROP_DOWN_MENU;
		}

		// We should never get to this line...
		return AS_PUSH_BUTTON;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public String getText() {
		return text;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public String getToolTipText() {
		return toolTipText;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public boolean isChecked() {
		return value == VAL_TOGGLE_BTN_ON || value == VAL_RADIO_BTN_ON;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public boolean isHandled() {
		return true;
	}

	/**
	 * Reports the outcome of the running of this action via the
	 * {@link IAction#RESULT} property.
	 * 
	 * @param success
	 *            <code>true</code> if the action succeeded and
	 *            <code>false</code> if the action failed or was not completed
	 * @see IAction#RESULT
	 */
	public final void notifyResult(boolean success) {
		// avoid Boolean.valueOf(boolean) to allow compilation against JCL
		// Foundation (bug 80059)
		firePropertyChange(RESULT, null, success ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * The default implementation of this <code>IAction</code> method does
	 * nothing. Subclasses should override this method if they do not need
	 * information from the triggering event, or override
	 * <code>runWithEvent(Event)</code> if they do.
	 */
	public void run() {
		// do nothing
	}

	/**
	 * The default implementation of this <code>IAction</code> method ignores
	 * the event argument, and simply calls <code>run()</code>. Subclasses
	 * should override this method if they need information from the triggering
	 * event, or override <code>run()</code> if not.
	 * 
	 * @param event
	 *            the SWT event which triggered this action being run
	 */
	public void runWithEvent(Event event) {
		run();
	}

	/*
	 * @see IAction#setAccelerator(int)
	 */
	public void setAccelerator(int keycode) {
		this.accelerator = keycode;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setActionDefinitionId(String id) {
		actionDefinitionId = id;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setChecked(boolean checked) {
		Object newValue = null;

		// For backward compatibility, if the style is not
		// set yet, then convert it to a toggle button.
		if (value == null || value == VAL_TOGGLE_BTN_ON
				|| value == VAL_TOGGLE_BTN_OFF) {
			newValue = checked ? VAL_TOGGLE_BTN_ON : VAL_TOGGLE_BTN_OFF;
		} else if (value == VAL_RADIO_BTN_ON || value == VAL_RADIO_BTN_OFF) {
			newValue = checked ? VAL_RADIO_BTN_ON : VAL_RADIO_BTN_OFF;
		} else {
			// Some other style already, so do nothing.
			return;
		}

		if (newValue != value) {
			value = newValue;
			if (checked) {
				firePropertyChange(CHECKED, Boolean.FALSE, Boolean.TRUE);
			} else {
				firePropertyChange(CHECKED, Boolean.TRUE, Boolean.FALSE);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setDescription(String text) {

		if ((description == null && text != null)
				|| (description != null && text == null)
				|| (description != null && text != null && !text
						.equals(description))) {
			String oldDescription = description;
			description = text;
			firePropertyChange(DESCRIPTION, oldDescription, description);
		}
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setDisabledImageDescriptor(ImageDescriptor newImage) {
		if (disabledImage != newImage) {
			ImageDescriptor oldImage = disabledImage;
			disabledImage = newImage;
			firePropertyChange(IMAGE, oldImage, newImage);
		}
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			Boolean oldVal = this.enabled ? Boolean.TRUE : Boolean.FALSE;
			Boolean newVal = enabled ? Boolean.TRUE : Boolean.FALSE;
			this.enabled = enabled;
			firePropertyChange(ENABLED, oldVal, newVal);
		}
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setHelpListener(HelpListener listener) {
		helpListener = listener;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setHoverImageDescriptor(ImageDescriptor newImage) {
		if (hoverImage != newImage) {
			ImageDescriptor oldImage = hoverImage;
			hoverImage = newImage;
			firePropertyChange(IMAGE, oldImage, newImage);
		}
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void setImageDescriptor(ImageDescriptor newImage) {
		if (image != newImage) {
			ImageDescriptor oldImage = image;
			image = newImage;
			firePropertyChange(IMAGE, oldImage, newImage);
		}
	}

	/**
	 * Sets the menu creator for this action.
	 * <p>
	 * Note that if this method is called, it overrides the check status.
	 * </p>
	 * 
	 * @param creator
	 *            the menu creator, or <code>null</code> if none
	 */
	public void setMenuCreator(IMenuCreator creator) {
		// For backward compatibility, if the style is not
		// set yet, then convert it to a drop down menu.
		if (value == null) {
			value = creator;
			return;
		}

		if (value instanceof IMenuCreator) {
			value = creator == null ? VAL_DROP_DOWN_MENU : creator;
		}
	}

	/**
	 * Sets the text for this action.
	 * <p>
	 * Fires a property change event for the <code>TEXT</code> property if the
	 * text actually changes as a consequence.
	 * </p>
	 * <p>
	 * The accelerator is identified by the last index of a tab character. If
	 * there are no tab characters, then it is identified by the last index of a
	 * '@' character. If neither, then there is no accelerator text. Note that
	 * if you want to insert a '@' character into the text (but no accelerator,
	 * you can simply insert a '@' or a tab at the end of the text.
	 * </p>
	 * 
	 * @param text
	 *            the text, or <code>null</code> if none
	 */
	public void setText(String text) {
		String oldText = this.text;
		int oldAccel = this.accelerator;
		this.text = text;
		if (text != null) {
			String acceleratorText = LegacyActionTools
					.extractAcceleratorText(text);
			if (acceleratorText != null) {
				int newAccelerator = LegacyActionTools
						.convertLocalizedAccelerator(acceleratorText);
				// Be sure to not wipe out the accelerator if nothing found
				if (newAccelerator > 0) {
					setAccelerator(newAccelerator);
				}
			}
		}
		if (!(this.accelerator == oldAccel && (oldText == null ? this.text == null
				: oldText.equals(this.text)))) {
			firePropertyChange(TEXT, oldText, this.text);
		}
	}

	/**
	 * Sets the tool tip text for this action.
	 * <p>
	 * Fires a property change event for the <code>TOOL_TIP_TEXT</code>
	 * property if the tool tip text actually changes as a consequence.
	 * </p>
	 * 
	 * @param toolTipText
	 *            the tool tip text, or <code>null</code> if none
	 */
	public void setToolTipText(String toolTipText) {
		String oldToolTipText = this.toolTipText;
		if (!(oldToolTipText == null ? toolTipText == null : oldToolTipText
				.equals(toolTipText))) {
			this.toolTipText = toolTipText;
			firePropertyChange(TOOL_TIP_TEXT, oldToolTipText, toolTipText);
		}
	}

}
