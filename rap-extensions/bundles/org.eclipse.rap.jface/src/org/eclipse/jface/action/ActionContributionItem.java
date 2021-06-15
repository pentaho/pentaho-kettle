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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A contribution item which delegates to an action.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public class ActionContributionItem extends ContributionItem {
 
	/**
	 * Mode bit: Show text on tool items or buttons, even if an image is
	 * present. If this mode bit is not set, text is only shown on tool items if
	 * there is no image present.
	 */
	public static int MODE_FORCE_TEXT = 1;

	/** a string inserted in the middle of text that has been shortened */
	private static final String ellipsis = "..."; //$NON-NLS-1$

	/**
	 * Stores the result of the action. False when the action returned failure.
	 */
	private Boolean result = null;

	private static boolean USE_COLOR_ICONS = true;

	/**
	 * Returns whether color icons should be used in toolbars.
	 * 
	 * @return <code>true</code> if color icons should be used in toolbars,
	 *         <code>false</code> otherwise
	 */
	public static boolean getUseColorIconsInToolbars() {
		return USE_COLOR_ICONS;
	}

	/**
	 * Sets whether color icons should be used in toolbars.
	 * 
	 * @param useColorIcons
	 *            <code>true</code> if color icons should be used in toolbars,
	 *            <code>false</code> otherwise
	 */
	public static void setUseColorIconsInToolbars(boolean useColorIcons) {
		USE_COLOR_ICONS = useColorIcons;
	}

	/**
	 * The presentation mode.
	 */
	private int mode = 0;

	/**
	 * The action.
	 */
	private IAction action;

	/**
	 * The listener for changes to the text of the action contributed by an
	 * external source.
	 */
	private final IPropertyChangeListener actionTextListener = new IPropertyChangeListener() {

		/**
		 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			update(event.getProperty());
		}
	};

	/**
	 * Remembers all images in use by this contribution item
	 */
	private LocalResourceManager imageManager;

	/**
	 * Listener for SWT button widget events.
	 */
	private Listener buttonListener;

	/**
	 * Listener for SWT menu item widget events.
	 */
	private Listener menuItemListener;

	/**
	 * Listener for action property change notifications.
	 */
	private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			actionPropertyChange(event);
		}
	};

	/**
	 * Listener for SWT tool item widget events.
	 */
	private Listener toolItemListener;

	/**
	 * The widget created for this item; <code>null</code> before creation and
	 * after disposal.
	 */
	private Widget widget = null;

	private Listener menuCreatorListener;

	/**
	 * Creates a new contribution item from the given action. The id of the
	 * action is used as the id of the item.
	 * 
	 * @param action
	 *            the action
	 */
	public ActionContributionItem(IAction action) {
		super(action.getId());
		this.action = action;
	}

	/**
	 * Handles a property change event on the action (forwarded by nested
	 * listener).
	 */
	private void actionPropertyChange(final PropertyChangeEvent e) {
		// This code should be removed. Avoid using free asyncExec

		if (isVisible() && widget != null) {
			Display display = widget.getDisplay();
			if (display.getThread() == Thread.currentThread()) {
				update(e.getProperty());
			} else {
				display.asyncExec(new Runnable() {
					public void run() {
						update(e.getProperty());
					}
				});
			}

		}
	}

	/**
	 * Compares this action contribution item with another object. Two action
	 * contribution items are equal if they refer to the identical Action.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof ActionContributionItem)) {
			return false;
		}
		return action.equals(((ActionContributionItem) o).action);
	}

	/**
	 * The <code>ActionContributionItem</code> implementation of this
	 * <code>IContributionItem</code> method creates an SWT
	 * <code>Button</code> for the action using the action's style. If the
	 * action's checked property has been set, the button is created and primed
	 * to the value of the checked property.
	 */
	public void fill(Composite parent) {
		if (widget == null && parent != null) {
			int flags = SWT.PUSH;
			if (action != null) {
				if (action.getStyle() == IAction.AS_CHECK_BOX) {
					flags = SWT.TOGGLE;
				}
				if (action.getStyle() == IAction.AS_RADIO_BUTTON) {
					flags = SWT.RADIO;
				}
			}

			Button b = new Button(parent, flags);
			b.setData(this);
			b.addListener(SWT.Dispose, getButtonListener());
			// Don't hook a dispose listener on the parent
			b.addListener(SWT.Selection, getButtonListener());
			if (action.getHelpListener() != null) {
				b.addHelpListener(action.getHelpListener());
			}
			widget = b;

			update(null);

			// Attach some extra listeners.
			action.addPropertyChangeListener(propertyListener);
			if (action != null) {
				String commandId = action.getActionDefinitionId();
				ExternalActionManager.ICallback callback = ExternalActionManager
						.getInstance().getCallback();

				if ((callback != null) && (commandId != null)) {
					callback.addPropertyChangeListener(commandId,
							actionTextListener);
				}
			}
		}
	}

	/**
	 * The <code>ActionContributionItem</code> implementation of this
	 * <code>IContributionItem</code> method creates an SWT
	 * <code>MenuItem</code> for the action using the action's style. If the
	 * action's checked property has been set, a button is created and primed to
	 * the value of the checked property. If the action's menu creator property
	 * has been set, a cascading submenu is created.
	 */
	public void fill(Menu parent, int index) {
		if (widget == null && parent != null) {
			int flags = SWT.PUSH;
			if (action != null) {
				int style = action.getStyle();
				if (style == IAction.AS_CHECK_BOX) {
					flags = SWT.CHECK;
				} else if (style == IAction.AS_RADIO_BUTTON) {
					flags = SWT.RADIO;
				} else if (style == IAction.AS_DROP_DOWN_MENU) {
					flags = SWT.CASCADE;
				}
			}

			MenuItem mi = null;
			if (index >= 0) {
				mi = new MenuItem(parent, flags, index);
			} else {
				mi = new MenuItem(parent, flags);
			}
			widget = mi;

			mi.setData(this);
			mi.addListener(SWT.Dispose, getMenuItemListener());
			mi.addListener(SWT.Selection, getMenuItemListener());
			if (action.getHelpListener() != null) {
				mi.addHelpListener(action.getHelpListener());
			}

			if (flags == SWT.CASCADE) {
				// just create a proxy for now, if the user shows it then 
				// fill it in
				Menu subMenu = new Menu(parent);
				subMenu.addListener(SWT.Show, getMenuCreatorListener());
				subMenu.addListener(SWT.Hide, getMenuCreatorListener());
				mi.setMenu(subMenu);
			}

			update(null);

			// Attach some extra listeners.
			action.addPropertyChangeListener(propertyListener);
			if (action != null) {
				String commandId = action.getActionDefinitionId();
				ExternalActionManager.ICallback callback = ExternalActionManager
						.getInstance().getCallback();

				if ((callback != null) && (commandId != null)) {
					callback.addPropertyChangeListener(commandId,
							actionTextListener);
				}
			}
		}
	}

	/**
	 * The <code>ActionContributionItem</code> implementation of this ,
	 * <code>IContributionItem</code> method creates an SWT
	 * <code>ToolItem</code> for the action using the action's style. If the
	 * action's checked property has been set, a button is created and primed to
	 * the value of the checked property. If the action's menu creator property
	 * has been set, a drop-down tool item is created.
	 */
	public void fill(ToolBar parent, int index) {
		if (widget == null && parent != null) {
			int flags = SWT.PUSH;
			if (action != null) {
				int style = action.getStyle();
				if (style == IAction.AS_CHECK_BOX) {
					flags = SWT.CHECK;
				} else if (style == IAction.AS_RADIO_BUTTON) {
					flags = SWT.RADIO;
				} else if (style == IAction.AS_DROP_DOWN_MENU) {
					flags = SWT.DROP_DOWN;
				}
			}

			ToolItem ti = null;
			if (index >= 0) {
				ti = new ToolItem(parent, flags, index);
			} else {
				ti = new ToolItem(parent, flags);
			}
			ti.setData(this);
			ti.addListener(SWT.Selection, getToolItemListener());
			ti.addListener(SWT.Dispose, getToolItemListener());

			widget = ti;

			update(null);

			// Attach some extra listeners.
			action.addPropertyChangeListener(propertyListener);
			if (action != null) {
				String commandId = action.getActionDefinitionId();
				ExternalActionManager.ICallback callback = ExternalActionManager
						.getInstance().getCallback();

				if ((callback != null) && (commandId != null)) {
					callback.addPropertyChangeListener(commandId,
							actionTextListener);
				}
			}
		}
	}

	/**
	 * Returns the action associated with this contribution item.
	 * 
	 * @return the action
	 */
	public IAction getAction() {
		return action;
	}

	/**
	 * Returns the listener for SWT button widget events.
	 * 
	 * @return a listener for button events
	 */
	private Listener getButtonListener() {
		if (buttonListener == null) {
			buttonListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						Widget ew = event.widget;
						if (ew != null) {
							handleWidgetSelection(event, ((Button) ew)
									.getSelection());
						}
						break;
					}
				}
			};
		}
		return buttonListener;
	}

	/**
	 * Returns the listener for SWT menu item widget events.
	 * 
	 * @return a listener for menu item events
	 */
	private Listener getMenuItemListener() {
		if (menuItemListener == null) {
			menuItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						Widget ew = event.widget;
						if (ew != null) {
							handleWidgetSelection(event, ((MenuItem) ew)
									.getSelection());
						}
						break;
					}
				}
			};
		}
		return menuItemListener;
	}

	/**
	 * Returns the presentation mode, which is the bitwise-or of the
	 * <code>MODE_*</code> constants. The default mode setting is 0, meaning
	 * that for menu items, both text and image are shown (if present), but for
	 * tool items, the text is shown only if there is no image.
	 * 
	 * @return the presentation mode settings
	 * 
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Returns the listener for SWT tool item widget events.
	 * 
	 * @return a listener for tool item events
	 */
	private Listener getToolItemListener() {
		if (toolItemListener == null) {
			toolItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						Widget ew = event.widget;
						if (ew != null) {
							handleWidgetSelection(event, ((ToolItem) ew)
									.getSelection());
						}
						break;
					}
				}
			};
		}
		return toolItemListener;
	}

	/**
	 * Handles a widget dispose event for the widget corresponding to this item.
	 */
	private void handleWidgetDispose(Event e) {
		// Check if our widget is the one being disposed.
		if (e.widget == widget) {
			// Dispose of the menu creator.
			if (action.getStyle() == IAction.AS_DROP_DOWN_MENU
					&& menuCreatorCalled) {
				IMenuCreator mc = action.getMenuCreator();
				if (mc != null) {
					mc.dispose();
				}
			}

			// Unhook all of the listeners.
			action.removePropertyChangeListener(propertyListener);
			if (action != null) {
				String commandId = action.getActionDefinitionId();
				ExternalActionManager.ICallback callback = ExternalActionManager
						.getInstance().getCallback();

				if ((callback != null) && (commandId != null)) {
					callback.removePropertyChangeListener(commandId,
							actionTextListener);
				}
			}

			// Clear the widget field.
			widget = null;

			disposeOldImages();
		}
	}

	/**
	 * Handles a widget selection event.
	 */
	private void handleWidgetSelection(Event e, boolean selection) {

		Widget item = e.widget;
		if (item != null) {
			int style = item.getStyle();

			if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
				if (action.getStyle() == IAction.AS_CHECK_BOX) {
					action.setChecked(selection);
				}
			} else if ((style & SWT.RADIO) != 0) {
				if (action.getStyle() == IAction.AS_RADIO_BUTTON) {
					action.setChecked(selection);
				}
			} else if ((style & SWT.DROP_DOWN) != 0) {
				if (e.detail == 4) { // on drop-down button
					if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
						IMenuCreator mc = action.getMenuCreator();
						menuCreatorCalled = true;
						ToolItem ti = (ToolItem) item;
						// we create the menu as a sub-menu of "dummy" so that
						// we can use
						// it in a cascading menu too.
						// If created on a SWT control we would get an SWT
						// error...
						// Menu dummy= new Menu(ti.getParent());
						// Menu m= mc.getMenu(dummy);
						// dummy.dispose();
						if (mc != null) {
							Menu m = mc.getMenu(ti.getParent());
							if (m != null) {
								// position the menu below the drop down item
								Point point = ti.getParent().toDisplay(
										new Point(e.x, e.y));
								m.setLocation(point.x, point.y); // waiting
																	// for SWT
								// 0.42
								m.setVisible(true);
								return; // we don't fire the action
							}
						}
					}
				}
			}

			ExternalActionManager.IExecuteCallback callback = null;
			String actionDefinitionId = action.getActionDefinitionId();
			if (actionDefinitionId != null) {
				Object obj = ExternalActionManager.getInstance()
						.getCallback();
				if (obj instanceof ExternalActionManager.IExecuteCallback) {
					callback = (ExternalActionManager.IExecuteCallback) obj;
				}
			}

			// Ensure action is enabled first.
			// See 1GAN3M6: ITPUI:WINNT - Any IAction in the workbench can be
			// executed while disabled.
			if (action.isEnabled()) {
				boolean trace = Policy.TRACE_ACTIONS;

				long ms = 0L;
				if (trace) {
					ms = System.currentTimeMillis();
					System.out.println("Running action: " + action.getText()); //$NON-NLS-1$
				}				
				
				IPropertyChangeListener resultListener = null;
				if (callback != null) {
					resultListener = new IPropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent event) {
							// Check on result
							if (event.getProperty().equals(IAction.RESULT)) {
								if (event.getNewValue() instanceof Boolean) {
									result = (Boolean) event.getNewValue();
								}
							}
						}
					};
					action.addPropertyChangeListener(resultListener);
					callback.preExecute(action, e);
				}

				action.runWithEvent(e);

				if (callback != null) {
					if (result == null || result.equals(Boolean.TRUE)) {
						callback.postExecuteSuccess(action, Boolean.TRUE);
					} else {
						callback.postExecuteFailure(action,
								new ExecutionException(action.getText()
										+ " returned failure.")); //$NON-NLS-1$
					}
				}

				if (resultListener!=null) {
					result = null;
					action.removePropertyChangeListener(resultListener);
				}
				if (trace) {
					System.out.println((System.currentTimeMillis() - ms)
							+ " ms to run action: " + action.getText()); //$NON-NLS-1$
				}
			} else {
				if (callback != null) {
					callback.notEnabled(action, new NotEnabledException(action
							.getText()
							+ " is not enabled.")); //$NON-NLS-1$
				}
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on Object.
	 */
	public int hashCode() {
		return action.hashCode();
	}

	/**
	 * Returns whether the given action has any images.
	 * 
	 * @param actionToCheck
	 *            the action
	 * @return <code>true</code> if the action has any images,
	 *         <code>false</code> if not
	 */
	private boolean hasImages(IAction actionToCheck) {
		return actionToCheck.getImageDescriptor() != null
				|| actionToCheck.getHoverImageDescriptor() != null
				|| actionToCheck.getDisabledImageDescriptor() != null;
	}

	/**
	 * Returns whether the command corresponding to this action is active.
	 */
	private boolean isCommandActive() {
		IAction actionToCheck = getAction();

		if (actionToCheck != null) {
			String commandId = actionToCheck.getActionDefinitionId();
			ExternalActionManager.ICallback callback = ExternalActionManager
					.getInstance().getCallback();

			if (callback != null) {
				return callback.isActive(commandId);
			}
		}
		return true;
	}

	/**
	 * The action item implementation of this <code>IContributionItem</code>
	 * method returns <code>true</code> for menu items and <code>false</code>
	 * for everything else.
	 */
	public boolean isDynamic() {
		if (widget instanceof MenuItem) {
			// Optimization. Only recreate the item is the check or radio style
			// has changed.
			boolean itemIsCheck = (widget.getStyle() & SWT.CHECK) != 0;
			boolean actionIsCheck = getAction() != null
					&& getAction().getStyle() == IAction.AS_CHECK_BOX;
			boolean itemIsRadio = (widget.getStyle() & SWT.RADIO) != 0;
			boolean actionIsRadio = getAction() != null
					&& getAction().getStyle() == IAction.AS_RADIO_BUTTON;
			return (itemIsCheck != actionIsCheck)
					|| (itemIsRadio != actionIsRadio);
		}
		return false;
	}

	/*
	 * (non-Javadoc) Method declared on IContributionItem.
	 */
	public boolean isEnabled() {
		return action != null && action.isEnabled();
	}

	/**
	 * Returns <code>true</code> if this item is allowed to enable,
	 * <code>false</code> otherwise.
	 * 
	 * @return if this item is allowed to be enabled
	 */
	protected boolean isEnabledAllowed() {
		if (getParent() == null) {
			return true;
		}
		Boolean value = getParent().getOverrides().getEnabled(this);
		return (value == null) ? true : value.booleanValue();
	}

	/**
	 * The <code>ActionContributionItem</code> implementation of this
	 * <code>ContributionItem</code> method extends the super implementation
	 * by also checking whether the command corresponding to this action is
	 * active.
	 */
	public boolean isVisible() {
		return super.isVisible() && isCommandActive();
	}

	/**
	 * Sets the presentation mode, which is the bitwise-or of the
	 * <code>MODE_*</code> constants.
	 * 
	 * @param mode
	 *            the presentation mode settings
	 * 
	 */
	public void setMode(int mode) {
		this.mode = mode;
		update();
	}

	/**
	 * The action item implementation of this <code>IContributionItem</code>
	 * method calls <code>update(null)</code>.
	 */
	public final void update() {
		update(null);
	}

	/**
	 * Synchronizes the UI with the given property.
	 * 
	 * @param propertyName
	 *            the name of the property, or <code>null</code> meaning all
	 *            applicable properties
	 */
	public void update(String propertyName) {
		if (widget != null) {
			// determine what to do
			boolean textChanged = propertyName == null
					|| propertyName.equals(IAction.TEXT);
			boolean imageChanged = propertyName == null
					|| propertyName.equals(IAction.IMAGE);
			boolean tooltipTextChanged = propertyName == null
					|| propertyName.equals(IAction.TOOL_TIP_TEXT);
			boolean enableStateChanged = propertyName == null
					|| propertyName.equals(IAction.ENABLED)
					|| propertyName
							.equals(IContributionManagerOverrides.P_ENABLED);
			boolean checkChanged = (action.getStyle() == IAction.AS_CHECK_BOX || action
					.getStyle() == IAction.AS_RADIO_BUTTON)
					&& (propertyName == null || propertyName
							.equals(IAction.CHECKED));

			if (widget instanceof ToolItem) {
				ToolItem ti = (ToolItem) widget;
				String text = action.getText();
				// the set text is shown only if there is no image or if forced
				// by MODE_FORCE_TEXT
				boolean showText = text != null
						&& ((getMode() & MODE_FORCE_TEXT) != 0 || !hasImages(action));

				// only do the trimming if the text will be used
				if (showText && text != null) {
					text = Action.removeAcceleratorText(text);
					text = Action.removeMnemonics(text);
				}

				if (textChanged) {
					String textToSet = showText ? text : ""; //$NON-NLS-1$
					boolean rightStyle = (ti.getParent().getStyle() & SWT.RIGHT) != 0;
					if (rightStyle || !ti.getText().equals(textToSet)) {
						// In addition to being required to update the text if
						// it
						// gets nulled out in the action, this is also a
						// workaround
						// for bug 50151: Using SWT.RIGHT on a ToolBar leaves
						// blank space
						ti.setText(textToSet);
					}
				}

				if (imageChanged) {
					// only substitute a missing image if it has no text
					updateImages(!showText);
				}

				if (tooltipTextChanged || textChanged) {
					String toolTip = action.getToolTipText();
					if ((toolTip == null) || (toolTip.length() == 0)) {
						toolTip = text;
					}

					ExternalActionManager.ICallback callback = ExternalActionManager
							.getInstance().getCallback();
					String commandId = action.getActionDefinitionId();
					if ((callback != null) && (commandId != null)
							&& (toolTip != null)) {
						String acceleratorText = callback
								.getAcceleratorText(commandId);
						if (acceleratorText != null
								&& acceleratorText.length() != 0) {
							toolTip = JFaceResources.format(
									"Toolbar_Tooltip_Accelerator", //$NON-NLS-1$
									new Object[] { toolTip, acceleratorText });
						}
					}

					// if the text is showing, then only set the tooltip if
					// different
					if (!showText || toolTip != null && !toolTip.equals(text)) {
						ti.setToolTipText(toolTip);
					} else {
						ti.setToolTipText(null);
					}
				}

				if (enableStateChanged) {
					boolean shouldBeEnabled = action.isEnabled()
							&& isEnabledAllowed();

					if (ti.getEnabled() != shouldBeEnabled) {
						ti.setEnabled(shouldBeEnabled);
					}
				}

				if (checkChanged) {
					boolean bv = action.isChecked();

					if (ti.getSelection() != bv) {
						ti.setSelection(bv);
					}
				}
				return;
			}

			if (widget instanceof MenuItem) {
				MenuItem mi = (MenuItem) widget;

				if (textChanged) {
					int accelerator = 0;
					String acceleratorText = null;
					IAction updatedAction = getAction();
					String text = null;
					accelerator = updatedAction.getAccelerator();
					ExternalActionManager.ICallback callback = ExternalActionManager
							.getInstance().getCallback();

					// Block accelerators that are already in use.
					if ((accelerator != 0) && (callback != null)
							&& (callback.isAcceleratorInUse(accelerator))) {
						accelerator = 0;
					}

					/*
					 * Process accelerators on GTK in a special way to avoid Bug
					 * 42009. We will override the native input method by
					 * allowing these reserved accelerators to be placed on the
					 * menu. We will only do this for "Ctrl+Shift+[0-9A-FU]".
					 */
					final String commandId = updatedAction
							.getActionDefinitionId();
					// RAP [bm]: 
//					if (("gtk".equals(SWT.getPlatform())) && (callback instanceof IBindingManagerCallback) //$NON-NLS-1$
//							&& (commandId != null)) {
//						final IBindingManagerCallback bindingManagerCallback = (IBindingManagerCallback) callback;
//						final IKeyLookup lookup = KeyLookupFactory.getDefault();
//						final TriggerSequence[] triggerSequences = bindingManagerCallback
//								.getActiveBindingsFor(commandId);
//						for (int i = 0; i < triggerSequences.length; i++) {
//							final TriggerSequence triggerSequence = triggerSequences[i];
//							final Trigger[] triggers = triggerSequence
//									.getTriggers();
//							if (triggers.length == 1) {
//								final Trigger trigger = triggers[0];
//								if (trigger instanceof KeyStroke) {
//									final KeyStroke currentKeyStroke = (KeyStroke) trigger;
//									final int currentNaturalKey = currentKeyStroke
//											.getNaturalKey();
//									if ((currentKeyStroke.getModifierKeys() == (lookup
//											.getCtrl() | lookup.getShift()))
//											&& ((currentNaturalKey >= '0' && currentNaturalKey <= '9')
//													|| (currentNaturalKey >= 'A' && currentNaturalKey <= 'F') || (currentNaturalKey == 'U'))) {
//										accelerator = currentKeyStroke
//												.getModifierKeys()
//												| currentNaturalKey;
//										acceleratorText = triggerSequence
//												.format();
//										break;
//									}
//								}
//							}
//						}
//					}
					// RAPEND: [bm] 

					if (accelerator == 0) {
						if ((callback != null) && (commandId != null)) {
							acceleratorText = callback
									.getAcceleratorText(commandId);
						}
					}

					IContributionManagerOverrides overrides = null;

					if (getParent() != null) {
						overrides = getParent().getOverrides();
					}

					if (overrides != null) {
						text = getParent().getOverrides().getText(this);
					}

					mi.setAccelerator(accelerator);

					if (text == null) {
						text = updatedAction.getText();
					}

					if (text != null && acceleratorText == null) {
						// use extracted accelerator text in case accelerator
						// cannot be fully represented in one int (e.g.
						// multi-stroke keys)
						acceleratorText = LegacyActionTools
								.extractAcceleratorText(text);
						if (acceleratorText == null && accelerator != 0) {
							acceleratorText = Action
									.convertAccelerator(accelerator);
						}
					}

					if (text == null) {
						text = ""; //$NON-NLS-1$
					} else {
						text = Action.removeAcceleratorText(text);
					}

					if (acceleratorText == null) {
						mi.setText(text);
					} else {
						mi.setText(text + '\t' + acceleratorText);
					}
				}

				if (imageChanged) {
					updateImages(false);
				}

				if (enableStateChanged) {
					boolean shouldBeEnabled = action.isEnabled()
							&& isEnabledAllowed();

					if (mi.getEnabled() != shouldBeEnabled) {
						mi.setEnabled(shouldBeEnabled);
					}
				}

				if (checkChanged) {
					boolean bv = action.isChecked();

					if (mi.getSelection() != bv) {
						mi.setSelection(bv);
					}
				}

				return;
			}

			if (widget instanceof Button) {
				Button button = (Button) widget;

				if (imageChanged) {
					updateImages(false);
				}

				if (textChanged) {
					String text = action.getText();
					boolean showText = text != null && ((getMode() & MODE_FORCE_TEXT) != 0 || !hasImages(action));
					// only do the trimming if the text will be used
					if (showText) {
						text = Action.removeAcceleratorText(text);
					}
					String textToSet = showText ? text : ""; //$NON-NLS-1$
					button.setText(textToSet);
				}

				if (tooltipTextChanged) {
					button.setToolTipText(action.getToolTipText());
				}

				if (enableStateChanged) {
					boolean shouldBeEnabled = action.isEnabled()
							&& isEnabledAllowed();

					if (button.getEnabled() != shouldBeEnabled) {
						button.setEnabled(shouldBeEnabled);
					}
				}

				if (checkChanged) {
					boolean bv = action.isChecked();

					if (button.getSelection() != bv) {
						button.setSelection(bv);
					}
				}
				return;
			}
		}
	}

	/**
	 * Updates the images for this action.
	 * 
	 * @param forceImage
	 *            <code>true</code> if some form of image is compulsory, and
	 *            <code>false</code> if it is acceptable for this item to have
	 *            no image
	 * @return <code>true</code> if there are images for this action,
	 *         <code>false</code> if not
	 */
	private boolean updateImages(boolean forceImage) {

		ResourceManager parentResourceManager = JFaceResources.getResources();

		if (widget instanceof ToolItem) {
			if (USE_COLOR_ICONS) {
				ImageDescriptor image = action.getHoverImageDescriptor();
				if (image == null) {
					image = action.getImageDescriptor();
				}
				ImageDescriptor disabledImage = action
						.getDisabledImageDescriptor();

				// Make sure there is a valid image.
				if (image == null && forceImage) {
					image = ImageDescriptor.getMissingImageDescriptor();
				}

				LocalResourceManager localManager = new LocalResourceManager(
						parentResourceManager);

				// performance: more efficient in SWT to set disabled and hot
				// image before regular image
				((ToolItem) widget)
						.setDisabledImage(disabledImage == null ? null
								: localManager
										.createImageWithDefault(disabledImage));
				((ToolItem) widget).setImage(image == null ? null
						: localManager.createImageWithDefault(image));

				disposeOldImages();
				imageManager = localManager;

				return image != null;
			}
			ImageDescriptor image = action.getImageDescriptor();
			ImageDescriptor hoverImage = action.getHoverImageDescriptor();
			ImageDescriptor disabledImage = action.getDisabledImageDescriptor();

			// If there is no regular image, but there is a hover image,
			// convert the hover image to gray and use it as the regular image.
			// RAP [bm]: 
//			if (image == null && hoverImage != null) {
//				image = ImageDescriptor.createWithFlags(action
//						.getHoverImageDescriptor(), SWT.IMAGE_GRAY);
//			} else {
//				// If there is no hover image, use the regular image as the
//				// hover image,
//				// and convert the regular image to gray
//				if (hoverImage == null && image != null) {
//					hoverImage = image;
//					image = ImageDescriptor.createWithFlags(action
//							.getImageDescriptor(), SWT.IMAGE_GRAY);
//				}
//			}
//			
//			// Make sure there is a valid image.
//			if (/* hoverImage == null &&  */ image == null && forceImage) {
//				image = ImageDescriptor.getMissingImageDescriptor();
//			}
			if(image == null) {
				image = ImageDescriptor.getMissingImageDescriptor();
			}
			// RAPEND: [bm] 

			// Create a local resource manager to remember the images we've
			// allocated for this tool item
			LocalResourceManager localManager = new LocalResourceManager(
					parentResourceManager);

			// performance: more efficient in SWT to set disabled and hot image
			// before regular image
			((ToolItem) widget).setDisabledImage(disabledImage == null ? null
					: localManager.createImageWithDefault(disabledImage));
			((ToolItem) widget).setHotImage(hoverImage == null ? null
					: localManager.createImageWithDefault(hoverImage));
			((ToolItem) widget).setImage(image == null ? null : localManager
					.createImageWithDefault(image));

			// Now that we're no longer referencing the old images, clear them
			// out.
			disposeOldImages();
			imageManager = localManager;

			return image != null;
		} else if (widget instanceof Item || widget instanceof Button) {

			// Use hover image if there is one, otherwise use regular image.
			ImageDescriptor image = action.getHoverImageDescriptor();
			if (image == null) {
				image = action.getImageDescriptor();
			}
			// Make sure there is a valid image.
			if (image == null && forceImage) {
				image = ImageDescriptor.getMissingImageDescriptor();
			}

			// Create a local resource manager to remember the images we've
			// allocated for this widget
			LocalResourceManager localManager = new LocalResourceManager(
					parentResourceManager);

			if (widget instanceof Item) {
				((Item) widget).setImage(image == null ? null : localManager
						.createImageWithDefault(image));
			} else if (widget instanceof Button) {
				((Button) widget).setImage(image == null ? null : localManager
						.createImageWithDefault(image));
			}

			// Now that we're no longer referencing the old images, clear them
			// out.
			disposeOldImages();
			imageManager = localManager;

			return image != null;
		}
		return false;
	}

	/**
	 * Dispose any images allocated for this contribution item
	 */
	private void disposeOldImages() {
		if (imageManager != null) {
			imageManager.dispose();
			imageManager = null;
		}
	}

	/**
	 * Shorten the given text <code>t</code> so that its length doesn't exceed
	 * the width of the given ToolItem.The default implementation replaces
	 * characters in the center of the original string with an ellipsis ("...").
	 * Override if you need a different strategy.
	 * 
	 * @param textValue
	 *            the text to shorten
	 * @param item
	 *            the tool item the text belongs to
	 * @return the shortened string
	 * 
	 */
	protected String shortenText(String textValue, ToolItem item) {
		if (textValue == null) {
			return null;
		}

		GC gc = new GC(item.getParent());

		int maxWidth = item.getImage().getBounds().width * 4;

		if (gc.textExtent(textValue).x < maxWidth) {
			gc.dispose();
			return textValue;
		}

		for (int i = textValue.length(); i > 0; i--) {
			String test = textValue.substring(0, i);
			test = test + ellipsis;
			if (gc.textExtent(test).x < maxWidth) {
				gc.dispose();
				return test;
			}

		}
		gc.dispose();
		// If for some reason we fall through abort
		return textValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	public void dispose() {
		if (widget != null) {
			widget.dispose();
			widget = null;
		}
		holdMenu = null;
	}
	
	/**
	 * Handle show and hide on the proxy menu for IAction.AS_DROP_DOWN_MENU
	 * actions.
	 * 
	 * @return the appropriate listener
	 * @since 1.1
	 */
	private Listener getMenuCreatorListener() {
		if (menuCreatorListener == null) {
			menuCreatorListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Show:
						handleShowProxy((Menu) event.widget);
						break;
					case SWT.Hide:
						handleHideProxy((Menu) event.widget);
						break;
					}
				}
			};
		}
		return menuCreatorListener;
	}
	
	/**
	 * This is the easiest way to hold the menu until we can swap it in to the
	 * proxy.
	 */
	private Menu holdMenu = null;

	private boolean menuCreatorCalled = false;
	
	/**
	 * The proxy menu is being shown, we better get the real menu.
	 * 
	 * @param proxy
	 *            the proxy menu
	 * @since 1.1
	 */
	private void handleShowProxy(Menu proxy) {
		proxy.removeListener(SWT.Show, getMenuCreatorListener());
		IMenuCreator mc = action.getMenuCreator();
		menuCreatorCalled  = true;
		if (mc == null) {
			return;
		}
		holdMenu = mc.getMenu(proxy.getParentMenu());
		if (holdMenu == null) {
			return;
		}
		copyMenu(holdMenu, proxy);
	}

	/**
	 * Create MenuItems in the proxy menu that can execute the real menu items
	 * if selected. Create proxy menus for any real item submenus.
	 * 
	 * @param realMenu
	 *            the real menu to copy from
	 * @param proxy
	 *            the proxy menu to populate
	 * @since 1.1
	 */
	private void copyMenu(Menu realMenu, Menu proxy) {
		if (realMenu.isDisposed() || proxy.isDisposed()) {
			return;
		}
		
		// we notify the real menu so it can populate itself if it was
		// listening for SWT.Show
		realMenu.notifyListeners(SWT.Show, null);

		final Listener passThrough = new Listener() {
			public void handleEvent(Event event) {
				if (!event.widget.isDisposed()) {
					Widget realItem = (Widget) event.widget.getData();
					if (!realItem.isDisposed()) {
						int style = event.widget.getStyle();
						if (event.type == SWT.Selection
								&& ((style & (SWT.TOGGLE | SWT.CHECK | SWT.RADIO)) != 0)
								&& realItem instanceof MenuItem) {
							((MenuItem) realItem)
									.setSelection(((MenuItem) event.widget)
											.getSelection());
						}
						event.widget = realItem;
						realItem.notifyListeners(event.type, event);
					}
				}
			}
		};

		MenuItem[] items = realMenu.getItems();
		for (int i = 0; i < items.length; i++) {
			final MenuItem realItem = items[i];
			final MenuItem proxyItem = new MenuItem(proxy, realItem.getStyle());
			proxyItem.setData(realItem);
			proxyItem.setAccelerator(realItem.getAccelerator());
			proxyItem.setEnabled(realItem.getEnabled());
			proxyItem.setImage(realItem.getImage());
			proxyItem.setSelection(realItem.getSelection());
			proxyItem.setText(realItem.getText());

			// pass through any events
			proxyItem.addListener(SWT.Selection, passThrough);
			proxyItem.addListener(SWT.Arm, passThrough);
			proxyItem.addListener(SWT.Help, passThrough);

			final Menu itemMenu = realItem.getMenu();
			if (itemMenu != null) {
				// create a proxy for any sub menu items
				final Menu subMenu = new Menu(proxy);
				subMenu.setData(itemMenu);
				proxyItem.setMenu(subMenu);
				subMenu.addListener(SWT.Show, new Listener() {
					public void handleEvent(Event event) {
						event.widget.removeListener(SWT.Show, this);
						if (event.type == SWT.Show) {
							copyMenu(itemMenu, subMenu);
						}
					}
				});
				subMenu.addListener(SWT.Help, passThrough);
				subMenu.addListener(SWT.Hide, passThrough);
			}
		}
	}
	
	/**
	 * The proxy menu is being hidden, so we need to make it go away.
	 * 
	 * @param proxy
	 *            the proxy menu
	 * @since 1.1
	 */
	private void handleHideProxy(final Menu proxy) {
		proxy.removeListener(SWT.Hide, getMenuCreatorListener());
		proxy.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!proxy.isDisposed()) {
					MenuItem parentItem = proxy.getParentItem();
					proxy.dispose();
					parentItem.setMenu(holdMenu);
				}
				if (holdMenu != null && !holdMenu.isDisposed()) {
					holdMenu.notifyListeners(SWT.Hide, null);
				}
				holdMenu = null;
			}
		});
	}
	
	/**
	 * Return the widget associated with this contribution item. It should not
	 * be cached, as it can be disposed and re-created by its containing
	 * ContributionManager, which controls all of the widgets lifecycle methods.
	 * <p>
	 * This can be used to set layout data on the widget if appropriate. The
	 * actual type of the widget can be any valid control for this
	 * ContributionItem's current ContributionManager.
	 * </p>
	 * 
	 * @return the widget, or <code>null</code> depending on the lifecycle.
	 * @since 1.1
	 */
	public Widget getWidget() {
		return widget;
	}
}
