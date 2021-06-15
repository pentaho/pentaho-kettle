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
package org.eclipse.jface.action;

import java.io.Serializable;

import org.eclipse.core.commands.IHandlerAttributes;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;

/**
 * An action represents the non-UI side of a command which can be triggered
 * by the end user. Actions are typically associated with buttons, menu items,
 * and items in tool bars. The controls for a command are built by some container,
 * which furnished the context where these controls appear and configures 
 * them with data from properties declared by the action. When the end user
 * triggers the command via its control, the action's <code>run</code>
 * method is invoked to do the real work.
 * <p>
 * Actions support a predefined set of properties (and possibly others as well).
 * Clients of an action may register property change listeners so that they get 
 * notified whenever the value of a property changes.
 * </p>
 * <p>
 * Clients should subclass the abstract base class <code>Action</code> to define 
 * concrete actions rather than implementing <code>IAction</code> from scratch.
 * </p>
 * <p>
 * This interface exists only to define the API for actions.
 * It is not intended to be implemented by clients.
 * </p>
 * 
 * @see Action
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.0
 */
public interface IAction extends Serializable {

    /**
     * Action style constant (value <code>0</code>) indicating action style 
     * is not specified yet. By default, the action will assume a push button
     * style. If <code>setChecked</code> is called, then the style will change
     * to a check box, or if <code>setMenuCreator</code> is called, then the
     * style will change to a drop down menu.
     * 
     */
    public static int AS_UNSPECIFIED = 0x00;

    /**
     * Action style constant (value <code>1</code>) indicating action is 
     * a simple push button.
     */
    public static int AS_PUSH_BUTTON = 0x01;

    /**
     * Action style constant (value <code>2</code>) indicating action is 
     * a check box (or a toggle button).
     */
    public static int AS_CHECK_BOX = 0x02;

    /**
     * Action style constant (value <code>4</code>) indicating action is 
     * a drop down menu.
     */
    public static int AS_DROP_DOWN_MENU = 0x04;

    /**
     * Action style constant (value <code>8</code>) indicating action is 
     * a radio button.
     * 
     */
    public static int AS_RADIO_BUTTON = 0x08;

    /**
     * Property name of an action's text (value <code>"text"</code>).
     */
    public static final String TEXT = "text"; //$NON-NLS-1$

    /**
     * Property name of an action's enabled state
     * (value <code>"enabled"</code>).
     */
    public static final String ENABLED = "enabled"; //$NON-NLS-1$

    /**
     * Property name of an action's image (value <code>"image"</code>).
     */
    public static final String IMAGE = "image"; //$NON-NLS-1$

    /**
     * Property name of an action's tooltip text (value <code>"toolTipText"</code>).
     */
    public static final String TOOL_TIP_TEXT = "toolTipText"; //$NON-NLS-1$

    /**
     * Property name of an action's description (value <code>"description"</code>).
     * Typically the description is shown as a (longer) help text in the status line.
     */
    public static final String DESCRIPTION = "description"; //$NON-NLS-1$

    /**
     * Property name of an action's checked status (value
     * <code>"checked"</code>). Applicable when the style is
     * <code>AS_CHECK_BOX</code> or <code>AS_RADIO_BUTTON</code>.
     */
    public static final String CHECKED = "checked"; //$NON-NLS-1$

    /**
     * Property name of an action's success/fail result
     * (value <code>"result"</code>). The values are
     * <code>Boolean.TRUE</code> if running the action succeeded and 
     * <code>Boolean.FALSE</code> if running the action failed or did not
     * complete.
     * <p>
     * Not all actions report whether they succeed or fail. This property
     * is provided for use by actions that may be invoked by clients that can
     * take advantage of this information when present (for example, actions
     * used in cheat sheets). Clients should always assume that running the
     * action succeeded in the absence of notification to the contrary. 
     * </p>
     * 
     */
    public static final String RESULT = "result"; //$NON-NLS-1$
    
    /**
     * Property name of an action's handler. Some actions delegate some or all
     * of their behaviour or state to another object. In this case, if the
     * object to which behaviour has been delegated changes, then a property
     * change event should be sent with this name.
     * 
     * This is used to support backward compatibility of actions within the
     * commands framework.
     * 
     */
	public static final String HANDLED = IHandlerAttributes.ATTRIBUTE_HANDLED;

    /**
     * Adds a property change listener to this action.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener a property change listener
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener);

    // RAP [bm]: MenuItem#getAccelerator
    /**
     * Returns the accelerator keycode for this action.
     * The result is the bit-wise OR of zero or more modifier masks
     * and a key, as explained in <code>MenuItem.getAccelerator</code>.
     *
     * @return the accelerator keycode
     * <!-- @see org.eclipse.swt.widgets.MenuItem#getAccelerator() -->
     */
    public int getAccelerator();

    /**
     * Returns the action definition id of this action.
     * 
     * @return the action definition id of this action, or
     * <code>null</code> if none
     */
    public String getActionDefinitionId();

    /**
     * Returns the action's description if it has one.
     * Otherwise it returns <code>getToolTipText()</code>.
     * 
     * @return a description for the action; may be <code>null</code> 
     */
    public String getDescription();

    /**
     * Returns the disabled image for this action as an image descriptor.
     * <p>
     * This method is associated with the <code>IMAGE</code> property;
     * property change events are reported when its value changes.
     * </p>
     *
     * @return the image, or <code>null</code> if this action has no image
     * @see #IMAGE
     */
    public ImageDescriptor getDisabledImageDescriptor();

    /**
     * Returns a help listener for this action. 
     *
     * @return a help listener for this action
     */
    public HelpListener getHelpListener();

    /**
     * Returns the hover image for this action as an image descriptor.
     * <p>
     * Hover images will be used on platforms that support changing the image
     * when the user hovers over the item. This method is associated with 
     * the <code>IMAGE</code> property;
     * property change events are reported when its value changes.
     * </p>
     *
     * @return the image, or <code>null</code> if this action has no image
     * @see #IMAGE
     */
    public ImageDescriptor getHoverImageDescriptor();

    /**
     * Returns a unique identifier for this action, or <code>null</code> if it has
     * none.
     *
     * @return the action id, or <code>null</code> if none
     */
    public String getId();

    /**
     * Returns the image for this action as an image descriptor.
     * <p>
     * This method is associated with the <code>IMAGE</code> property;
     * property change events are reported when its value changes.
     * </p>
     *
     * @return the image, or <code>null</code> if this action has no image
     * @see #IMAGE
     */
    public ImageDescriptor getImageDescriptor();

    /**
     * Returns the menu creator for this action.
     *
     * @return the menu creator, or <code>null</code> if none
     */
    public IMenuCreator getMenuCreator();

    /**
     * Return this action's style.
     *
     * @return one of <code>AS_PUSH_BUTTON</code>, <code>AS_CHECK_BOX</code>,
     * <code>AS_RADIO_BUTTON</code> and <code>AS_DROP_DOWN_MENU</code>.
     */
    public int getStyle();

    /**
     * Returns the text for this action.
     * <p>
     * This method is associated with the <code>TEXT</code> property;
     * property change events are reported when its value changes.
     * </p>
     *
     * @return the text, or <code>null</code> if none
     * @see #TEXT
     */
    public String getText();

    /**
     * Returns the tool tip text for this action.
     * <p>
     * This method is associated with the <code>TOOL_TIP_TEXT</code> property;
     * property change events are reported when its value changes.
     * </p>
     *
     * @return the tool tip text, or <code>null</code> if none
     * @see #TOOL_TIP_TEXT
     */
    public String getToolTipText();

    /**
     * Returns the checked status of this action. Applicable only if the style is
     * <code>AS_CHECK_BOX</code> or <code>AS_RADIO_BUTTON</code>.
     * <p>
     * This method is associated with the <code>CHECKED</code> property;
     * property change events are reported when its value changes.
     * </p>
     *
     * @return the checked status
     * @see #CHECKED
     */
    public boolean isChecked();

    /**
     * Returns whether this action is enabled.
     * <p>
     * This method is associated with the <code>ENABLED</code> property;
     * property change events are reported when its value changes.
     * </p>
     *
     * @return <code>true</code> if enabled, and
     *   <code>false</code> if disabled
     * @see #ENABLED
     */
    public boolean isEnabled();

    /**
	 * Returns whether this action is handled. In the default case, this is
	 * always <code>true</code>. However, if the action delegates some of its
	 * behaviour to some other object, then this method should answer whether
	 * such an object is currently available.
	 * 
	 * @return <code>true</code> if all of the action's behaviour is
	 *         available; <code>false</code> otherwise.
	 */
	public boolean isHandled();

    /**
     * Removes the given listener from this action.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener a property change listener
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener);

    /**
     * Runs this action.
     * Each action implementation must define the steps needed to carry out this action.
     * The default implementation of this method in <code>Action</code>
     * does nothing.
     */
    public void run();

    /**
     * Runs this action, passing the triggering SWT event.
     * As of 2.0, <code>ActionContributionItem</code> calls this method
     * instead of <code>run()</code>.  
     * The default implementation of this method in <code>Action</code>
     * simply calls <code>run()</code> for backwards compatibility.
     * 
     * @param event the SWT event which triggered this action being run 
     */
    public void runWithEvent(Event event);

    /**
     * Sets the action definition id of this action.
     * 
     * @param id the action definition id
     */
    public void setActionDefinitionId(String id);

    /**
     * Sets the checked status of this action. Applicable for the styles
     * <code>AS_CHECK_BOX</code> or <code>AS_RADIO_BUTTON</code>.
     * <p>
     * Fires a property change event for the <code>CHECKED</code> property
     * if the checked status actually changes as a consequence.
     * </p>
     *
     * @param checked the new checked status
     * @see #CHECKED
     */
    public void setChecked(boolean checked);

    /**
     * Sets this action's description.
     * Typically the description is shown as a (longer) help text in the status line.
     * <p>
     * Fires a property change event for the <code>DESCRIPTION</code> property
     * if the description actually changes as a consequence.
     * </p>
     *
     * @param text the description, or <code>null</code> to clear the description
     * @see #DESCRIPTION
     */
    public void setDescription(String text);

    /**
     * Sets the disabled image for this action, as an image descriptor.
     * <p>
     * Disabled images will be used on platforms that support changing the image
     * when the item is disabled.Fires a property change event for 
     * the <code>IMAGE</code> property
     * if the image actually changes as a consequence.
     * </p>
     *
     * @param newImage the image, or <code>null</code> if this 
     *   action should not have an image
     * @see #IMAGE
     */
    public void setDisabledImageDescriptor(ImageDescriptor newImage);

    /**
     * Sets the enabled state of this action.
     * <p>
     * When an action is in the enabled state, the control associated with 
     * it is active; triggering it will end up inkoking this action's 
     * <code>run</code> method.
     * </p>
     * <p>
     * Fires a property change event for the <code>ENABLED</code> property
     * if the enabled state actually changes as a consequence.
     * </p>
     *
     * @param enabled <code>true</code> to enable, and
     *   <code>false</code> to disable
     * @see #ENABLED
     */
    public void setEnabled(boolean enabled);

    /**
     * Sets a help listener for this action. 
     *
     * @param listener a help listener for this action
     */
    public void setHelpListener(HelpListener listener);

    /**
     * Sets the hover image for this action, as an image descriptor.
     * <p>
     * Hover images will be used on platforms that support changing the image
     * when the user hovers over the item.Fires a property change event for 
     * the <code>IMAGE</code> property
     * if the image actually changes as a consequence.
     * </p>
     *
     * @param newImage the image, or <code>null</code> if this 
     *   action should not have an image
     * @see #IMAGE
     */
    public void setHoverImageDescriptor(ImageDescriptor newImage);

    /**
     * Sets the unique identifier for this action. This is used to identify actions
     * when added to a contribution manager.
     * It should be set when the action is created.  It should not be modified once
     * the action is part of an action contribution item.
     *
     * @param id the action id
     *
     * @see ActionContributionItem
     * @see IContributionItem#getId
     */
    public void setId(String id);

	/**
	 * Sets the image for this action, as an image descriptor.
	 * <p>
	 * Fires a property change event for the <code>IMAGE</code> property if the
	 * image actually changes as a consequence.
	 * </p>
	 * <p>
	 * Note: This operation is a hint and is not supported in all contexts on
	 * platforms that do not have this concept (for example, Windows NT).
	 * Furthermore, some platforms (such as GTK), cannot display both a check
	 * box and an image at the same time. Instead, they hide the image and
	 * display the check box.
	 * </p>
	 * 
	 * @param newImage
	 *            the image, or <code>null</code> if this action should not have
	 *            an image
	 * @see #IMAGE
	 */
    public void setImageDescriptor(ImageDescriptor newImage);

    /**
     * Sets the menu creator for this action. Applicable for style
     * <code>AS_DROP_DOWN_MENU</code>.
     *
     * @param creator the menu creator, or <code>null</code> if none
     */
    public void setMenuCreator(IMenuCreator creator);

    /**
     * Sets the text for this action.
     * <p>
     * An accelerator specification may follow the actual text, separated from it by
     * an '@' or a '\t' character.  An accelerator specification consists of zero or more 
     * modifier tokens followed by a key code token.  The tokens are separated by a '+' character.
     * </p>
     * <p>
     * Fires a property change event for the <code>TEXT</code> property
     * if the text actually changes as a consequence.
     * </p>
     *
     * @param text the text, or <code>null</code> if none
     * @see #TEXT
     * @see Action#findModifier
     * @see Action#findKeyCode
     */
    public void setText(String text);

    /**
     * Sets the tool tip text for this action.
     * <p>
     * Fires a property change event for the <code>TOOL_TIP_TEXT</code> property
     * if the tool tip text actually changes as a consequence.
     * </p>
     *
     * @param text the tool tip text, or <code>null</code> if none
     * @see #TOOL_TIP_TEXT
     */
    public void setToolTipText(String text);

    /**
     * <p>
     * Sets the accelerator keycode that this action maps to. This is a bitwise OR
     * of zero or more SWT key modifier masks (i.e. SWT.CTRL or SWT.ALT) and a
     * character code. For example, for Ctrl+Z, use <code>SWT.CTRL | 'Z'</code>.
     * Use 0 for no accelerator.
     * </p>
     * <p>
     * This method should no longer be used for actions in the Eclipse workbench.
     * <code>IWorkbenchCommandSupport</code> and
     * <code>IWorkbenchContextSupport</code> provide all the functionality
     * required for key bindings. If you set an accelerator using this method, then
     * it will not work in the workbench if it conflicts any existing key binding,
     * or if there is a different key binding defined for this action's definition
     * id. The definition id should be used instead -- referring to the command in
     * the workbench from which the key binding should be retrieved.
     * </p>
     * 
     * @param keycode
     *            the keycode to be accepted.
     */
    public void setAccelerator(int keycode);
}
