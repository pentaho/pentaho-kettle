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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.widgets.Event;

/**
 * <p>
 * A manager for a callback facility which is capable of querying external
 * interfaces for additional information about actions and action contribution
 * items. This information typically includes things like accelerators and
 * textual representations.
 * </p>
 * <p>
 * <em>It is only necessary to use this mechanism if you will be using a mix of
 * actions and commands, and wish the interactions to work properly.</em>
 * </p>
 * <p>
 * For example, in the Eclipse workbench, this mechanism is used to allow the
 * command architecture to override certain values in action contribution items.
 * </p>
 * <p>
 * This class is not intended to be called or extended by any external clients.
 * </p>
 * 
 * @since 1.0
 */
public final class ExternalActionManager {

	/**
	 * A simple implementation of the <code>ICallback</code> mechanism that
	 * simply takes a <code>BindingManager</code> and a
	 * <code>CommandManager</code>.
	 * <p>
	 * <b>Note:</b> this class is not intended to be subclassed by clients.
	 * </p>
	 * 
	 */
	public static class CommandCallback implements
			IBindingManagerListener, IBindingManagerCallback, IExecuteCallback {

		/**
		 * The internationalization bundle for text produced by this class.
		 */
		private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
				.getBundle(ExternalActionManager.class.getName());

		/**
		 * The callback capable of responding to whether a command is active.
		 */
		private final IActiveChecker activeChecker;
		
		/**
		 * Check the applicability of firing an execution event for an action.
		 */
		private final IExecuteApplicable applicabilityChecker;

		/**
		 * The binding manager for your application. Must not be
		 * <code>null</code>.
		 */
		private final BindingManager bindingManager;

		/**
		 * Whether a listener has been attached to the binding manager yet.
		 */
		private boolean bindingManagerListenerAttached = false;

		/**
		 * The command manager for your application. Must not be
		 * <code>null</code>.
		 */
		private final CommandManager commandManager;

		/**
		 * A set of all the command identifiers that have been logged as broken
		 * so far. For each of these, there will be a listener on the
		 * corresponding command. If the command ever becomes defined, the item
		 * will be removed from this set and the listener removed. This value
		 * may be empty, but never <code>null</code>.
		 */
		private final Set loggedCommandIds = new HashSet();

		/**
		 * The list of listeners that have registered for property change
		 * notification. This is a map of command identifiers (<code>String</code>)
		 * to listeners (<code>IPropertyChangeListener</code> or
		 * <code>ListenerList</code> of <code>IPropertyChangeListener</code>).
		 */
		private final Map registeredListeners = new HashMap();

		/**
		 * Constructs a new instance of <code>CommandCallback</code> with the
		 * workbench it should be using. All commands will be considered active.
		 * 
		 * @param bindingManager
		 *            The binding manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * @param commandManager
		 *            The command manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * 
		 */
		public CommandCallback(final BindingManager bindingManager,
				final CommandManager commandManager) {
			this(bindingManager, commandManager, new IActiveChecker() {
				public boolean isActive(String commandId) {
					return true;
				}

			}, new IExecuteApplicable() {
				public boolean isApplicable(IAction action) {
					return true;
				}
			});
		}
		/**
		 * Constructs a new instance of <code>CommandCallback</code> with the
		 * workbench it should be using.
		 * 
		 * @param bindingManager
		 *            The binding manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * @param commandManager
		 *            The command manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * @param activeChecker
		 *            The callback mechanism for checking whether a command is
		 *            active; must not be <code>null</code>.
		 * 
		 */
		public CommandCallback(final BindingManager bindingManager,
				final CommandManager commandManager,
				final IActiveChecker activeChecker) {
			this(bindingManager, commandManager, activeChecker,
					new IExecuteApplicable() {
				public boolean isApplicable(IAction action) {
					return true;
				}
			});
		}
		/**
		 * Constructs a new instance of <code>CommandCallback</code> with the
		 * workbench it should be using.
		 * 
		 * @param bindingManager
		 *            The binding manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * @param commandManager
		 *            The command manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * @param activeChecker
		 *            The callback mechanism for checking whether a command is
		 *            active; must not be <code>null</code>.
		 * @param checker
		 *            The callback to check if an IAction should fire execution
		 *            events.
		 * 
		 * @since 1.1
		 */
		public CommandCallback(final BindingManager bindingManager,
				final CommandManager commandManager,
				final IActiveChecker activeChecker,
				final IExecuteApplicable checker) {
			if (bindingManager == null) {
				throw new NullPointerException(
						"The callback needs a binding manager"); //$NON-NLS-1$
			}

			if (commandManager == null) {
				throw new NullPointerException(
						"The callback needs a command manager"); //$NON-NLS-1$
			}

			if (activeChecker == null) {
				throw new NullPointerException(
						"The callback needs an active callback"); //$NON-NLS-1$
			}
			if (checker == null) {
				throw new NullPointerException(
						"The callback needs an applicable callback"); //$NON-NLS-1$
			}

			this.activeChecker = activeChecker;
			this.bindingManager = bindingManager;
			this.commandManager = commandManager;
			this.applicabilityChecker = checker;
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#addPropertyChangeListener(String,
		 *      IPropertyChangeListener)
		 */
		public final void addPropertyChangeListener(final String commandId,
				final IPropertyChangeListener listener) {
			Object existing = registeredListeners.get(commandId);
			if (existing instanceof ListenerList) {
				((ListenerList) existing).add(listener);
			} else if (existing != null) {
				ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
				listeners.add(existing);
				listeners.add(listener);
				registeredListeners.put(commandId, listeners);
			} else {
				registeredListeners.put(commandId, listener);
			}
			if (!bindingManagerListenerAttached) {
				bindingManager.addBindingManagerListener(this);
				bindingManagerListenerAttached = true;
			}
		}

		public final void bindingManagerChanged(final BindingManagerEvent event) {
			if (event.isActiveBindingsChanged()) {
				final Iterator listenerItr = registeredListeners.entrySet()
						.iterator();
				while (listenerItr.hasNext()) {
					final Map.Entry entry = (Map.Entry) listenerItr.next();
					final String commandId = (String) entry.getKey();
					final Command command = commandManager
							.getCommand(commandId);
					final ParameterizedCommand parameterizedCommand = new ParameterizedCommand(
							command, null);
					if (event.isActiveBindingsChangedFor(parameterizedCommand)) {
						Object value = entry.getValue();
						PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(event
								.getManager(), IAction.TEXT, null, null);
						if (value instanceof ListenerList) {
							Object[] listeners= ((ListenerList) value).getListeners();
							for (int i = 0; i < listeners.length; i++) {
								final IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
								listener.propertyChange(propertyChangeEvent);
							}
						} else {
							final IPropertyChangeListener listener = (IPropertyChangeListener) value;
							listener.propertyChange(propertyChangeEvent);
						}
					}
				}
			}
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#getAccelerator(String)
		 */
		public final Integer getAccelerator(final String commandId) {
			final TriggerSequence triggerSequence = bindingManager
					.getBestActiveBindingFor(commandId);
			if (triggerSequence != null) {
				final Trigger[] triggers = triggerSequence.getTriggers();
				if (triggers.length == 1) {
					final Trigger trigger = triggers[0];
					if (trigger instanceof KeyStroke) {
						final KeyStroke keyStroke = (KeyStroke) trigger;
						final int accelerator = SWTKeySupport
								.convertKeyStrokeToAccelerator(keyStroke);
						return new Integer(accelerator);
					}
				}
			}
			
			return null;
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#getAcceleratorText(String)
		 */
		public final String getAcceleratorText(final String commandId) {
			final TriggerSequence triggerSequence = bindingManager
					.getBestActiveBindingFor(commandId);
			if (triggerSequence == null) {
				return null;
			}

			return triggerSequence.format();
		}

		/**
		 * Returns the active bindings for a particular command identifier.
		 * 
		 * @param commandId
		 *            The identifier of the command whose bindings are
		 *            requested. This argument may be <code>null</code>. It
		 *            is assumed that the command has no parameters.
		 * @return The array of active triggers (<code>TriggerSequence</code>)
		 *         for a particular command identifier. This value is guaranteed
		 *         not to be <code>null</code>, but it may be empty.
		 */
		public final TriggerSequence[] getActiveBindingsFor(
				final String commandId) {
			return bindingManager.getActiveBindingsFor(commandId);
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#isAcceleratorInUse(int)
		 */
		public final boolean isAcceleratorInUse(final int accelerator) {
			final KeySequence keySequence = KeySequence
					.getInstance(SWTKeySupport
							.convertAcceleratorToKeyStroke(accelerator));
			return bindingManager.isPerfectMatch(keySequence)
					|| bindingManager.isPartialMatch(keySequence);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * Calling this method with an undefined command id will generate a log
		 * message.
		 */
		public final boolean isActive(final String commandId) {
			if (commandId != null) {
				final Command command = commandManager.getCommand(commandId);

				if (!command.isDefined()
						&& (!loggedCommandIds.contains(commandId))) {
					// The command is not yet defined, so we should log this.
					final String message = MessageFormat.format(Util
							.translateString(RESOURCE_BUNDLE,
									"undefinedCommand.WarningMessage", null), //$NON-NLS-1$
							new String[] { command.getId() });
					IStatus status = new Status(IStatus.ERROR,
							"org.eclipse.jface", //$NON-NLS-1$
							0, message, new Exception());
					Policy.getLog().log(status);

					// And remember this item so we don't log it again.
					loggedCommandIds.add(commandId);
					command.addCommandListener(new ICommandListener() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.ui.commands.ICommandListener#commandChanged(org.eclipse.ui.commands.CommandEvent)
						 */
						public final void commandChanged(
								final CommandEvent commandEvent) {
							if (command.isDefined()) {
								command.removeCommandListener(this);
								loggedCommandIds.remove(commandId);
							}
						}
					});

					return true;
				}

				return activeChecker.isActive(commandId);
			}

			return true;
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#removePropertyChangeListener(String,
		 *      IPropertyChangeListener)
		 */
		public final void removePropertyChangeListener(final String commandId,
				final IPropertyChangeListener listener) {
			Object existing= registeredListeners.get(commandId);
			if (existing == listener) {
				registeredListeners.remove(commandId);
				if (registeredListeners.isEmpty()) {
					bindingManager.removeBindingManagerListener(this);
					bindingManagerListenerAttached = false;
				}
			} else if (existing instanceof ListenerList) {
				ListenerList existingList = (ListenerList) existing;
				existingList.remove(listener);
				if (existingList.size() == 1) {
					registeredListeners.put(commandId, existingList.getListeners()[0]);
				}
			}
		}

		public void preExecute(IAction action, Event event) {
			String actionDefinitionId = action.getActionDefinitionId();
			if (actionDefinitionId==null 
					|| !applicabilityChecker.isApplicable(action)) {
				return;
			}
			Command command = commandManager.getCommand(actionDefinitionId);
			ExecutionEvent executionEvent = new ExecutionEvent(command,
					Collections.EMPTY_MAP, event, null);

			commandManager.firePreExecute(actionDefinitionId, executionEvent);
		}

		public void postExecuteSuccess(IAction action, Object returnValue) {
			String actionDefinitionId = action.getActionDefinitionId();
			if (actionDefinitionId==null 
					|| !applicabilityChecker.isApplicable(action)) {
				return;
			}
			commandManager.firePostExecuteSuccess(actionDefinitionId, returnValue);
		}

		public void postExecuteFailure(IAction action,
				ExecutionException exception) {
			String actionDefinitionId = action.getActionDefinitionId();
			if (actionDefinitionId==null 
					|| !applicabilityChecker.isApplicable(action)) {
				return;
			}
			commandManager.firePostExecuteFailure(actionDefinitionId, exception);
		}

		public void notDefined(IAction action, NotDefinedException exception) {
			String actionDefinitionId = action.getActionDefinitionId();
			if (actionDefinitionId==null 
					|| !applicabilityChecker.isApplicable(action)) {
				return;
			}
			commandManager.fireNotDefined(actionDefinitionId, exception);
		}

		public void notEnabled(IAction action, NotEnabledException exception) {
			String actionDefinitionId = action.getActionDefinitionId();
			if (actionDefinitionId==null 
					|| !applicabilityChecker.isApplicable(action)) {
				return;
			}
			commandManager.fireNotEnabled(actionDefinitionId, exception);
		}
	}

	/**
	 * Defines a callback mechanism for developer who wish to further control
	 * the visibility of legacy action-based contribution items.
	 * 
	 */
	public static interface IActiveChecker {
		/**
		 * Checks whether the command with the given identifier should be
		 * considered active. This can be used in systems using some kind of
		 * user interface filtering (e.g., activities in the Eclipse workbench).
		 * 
		 * @param commandId
		 *            The identifier for the command; must not be
		 *            <code>null</code>
		 * @return <code>true</code> if the command is active;
		 *         <code>false</code> otherwise.
		 */
		public boolean isActive(String commandId);
	}

	/**
	 * <p>
	 * A callback which communicates with the applications binding manager. This
	 * interface provides more information from the binding manager, which
	 * allows greater integration. Implementing this interface is preferred over
	 * {@link ExternalActionManager.ICallback}.
	 * </p>
	 * <p>
	 * Clients may implement this interface, but must not extend.
	 * </p>
	 * 
	 */
	public static interface IBindingManagerCallback extends ICallback {

		/**
		 * <p>
		 * Returns the active bindings for a particular command identifier.
		 * </p>
		 * 
		 * @param commandId
		 *            The identifier of the command whose bindings are
		 *            requested. This argument may be <code>null</code>. It
		 *            is assumed that the command has no parameters.
		 * @return The array of active triggers (<code>TriggerSequence</code>)
		 *         for a particular command identifier. This value is guaranteed
		 *         not to be <code>null</code>, but it may be empty.
		 */
		public TriggerSequence[] getActiveBindingsFor(String commandId);
	}
	
	/**
	 * An overridable mechanism to filter certain IActions from the execution
	 * bridge.
	 * 
	 * @since 1.1
	 */
	public static interface IExecuteApplicable {
		/**
		 * Allow the callback to filter out actions that should not fire
		 * execution events.
		 * 
		 * @param action
		 *            The action with an actionDefinitionId
		 * @return true if this action should be considered.
		 */
		public boolean isApplicable(IAction action);
	}
	
	/**
	 * <p>
	 * A callback for executing execution events. Allows
	 * <code>ActionContributionItems</code> to fire useful events.
	 * </p>
	 * <p>
	 * Clients must not implement this interface and must not extend.
	 * </p>
	 * 
	 * @since 1.1
	 * 
	 */
	public static interface IExecuteCallback {
		
		/**
		 * Fires a <code>NotEnabledException</code> because the action was not
		 * enabled.
		 * 
		 * @param action
		 * 			The action contribution that caused the exception,
		 * 			never <code>null</code>.
		 * @param exception
		 * 			The <code>NotEnabledException</code>, never <code>null</code>.
		 */
		public void notEnabled(IAction action, NotEnabledException exception);

		/**
		 * Fires a <code>NotDefinedException</code> because the action was not
		 * defined.
		 * 
		 * @param action
		 * 			The action contribution that caused the exception,
		 * 			never <code>null</code>.
		 * @param exception
		 * 			The <code>NotDefinedException</code>, never <code>null</code>.
		 */
		public void notDefined(IAction action, NotDefinedException exception);
		
		/**
		 * Fires an execution event before an action is run.
		 * 
		 * @param action
		 *            The action contribution that requires an
		 *            execution event to be fired. Cannot be <code>null</code>.
		 * @param e
		 *            The SWT Event, may be <code>null</code>.
		 * 
		 */
		public void preExecute(IAction action,
				Event e);
		
		/**
		 * Fires an execution event when the action returned a success.
		 * 
		 * @param action
		 *            The action contribution that requires an
		 *            execution event to be fired. Cannot be <code>null</code>.
		 * @param returnValue
		 *            The command's result, may be <code>null</code>.
		 * 
		 */
		public void postExecuteSuccess(IAction action,
				Object returnValue);
		
		/**
		 * Creates an <code>ExecutionException</code> when the action returned
		 * a failure.
		 * 
		 * @param action
		 * 			The action contribution that caused the exception,
		 * 			never <code>null</code>.
		 * @param exception
		 * 			The <code>ExecutionException</code>, never <code>null</code>.
		 */
		public void postExecuteFailure(IAction action,
				ExecutionException exception);
	}

	/**
	 * A callback mechanism for some external tool to communicate extra
	 * information to actions and action contribution items.
	 * 
	 */
	public static interface ICallback {

		/**
		 * <p>
		 * Adds a listener to the object referenced by <code>identifier</code>.
		 * This listener will be notified if a property of the item is to be
		 * changed. This identifier is specific to mechanism being used. In the
		 * case of the Eclipse workbench, this is the command identifier.
		 * </p>
		 * <p>
		 * Has no effect if an identical listener has already been added for
		 * the <code>identifier</code>.
		 * </p>
		 * 
		 * @param identifier
		 *            The identifier of the item to which the listener should be
		 *            attached; must not be <code>null</code>.
		 * @param listener
		 *            The listener to be added; must not be <code>null</code>.
		 */
		public void addPropertyChangeListener(String identifier,
				IPropertyChangeListener listener);

		/**
		 * An accessor for the accelerator associated with the item indicated by
		 * the identifier. This identifier is specific to mechanism being used.
		 * In the case of the Eclipse workbench, this is the command identifier.
		 * 
		 * @param identifier
		 *            The identifier of the item from which the accelerator
		 *            should be obtained ; must not be <code>null</code>.
		 * @return An integer representation of the accelerator. This is the
		 *         same accelerator format used by SWT.
		 */
		public Integer getAccelerator(String identifier);

		/**
		 * An accessor for the accelerator text associated with the item
		 * indicated by the identifier. This identifier is specific to mechanism
		 * being used. In the case of the Eclipse workbench, this is the command
		 * identifier.
		 * 
		 * @param identifier
		 *            The identifier of the item from which the accelerator text
		 *            should be obtained ; must not be <code>null</code>.
		 * @return A string representation of the accelerator. This is the
		 *         string representation that should be displayed to the user.
		 */
		public String getAcceleratorText(String identifier);

		/**
		 * Checks to see whether the given accelerator is being used by some
		 * other mechanism (outside of the menus controlled by JFace). This is
		 * used to keep JFace from trying to grab accelerators away from someone
		 * else.
		 * 
		 * @param accelerator
		 *            The accelerator to check -- in SWT's internal accelerator
		 *            format.
		 * @return <code>true</code> if the accelerator is already being used
		 *         and shouldn't be used again; <code>false</code> otherwise.
		 */
		public boolean isAcceleratorInUse(int accelerator);

		/**
		 * Checks whether the item matching this identifier is active. This is
		 * used to decide whether a contribution item with this identifier
		 * should be made visible. An inactive item is not visible.
		 * 
		 * @param identifier
		 *            The identifier of the item from which the active state
		 *            should be retrieved; must not be <code>null</code>.
		 * @return <code>true</code> if the item is active; <code>false</code>
		 *         otherwise.
		 */
		public boolean isActive(String identifier);

		/**
		 * Removes a listener from the object referenced by
		 * <code>identifier</code>. This identifier is specific to mechanism
		 * being used. In the case of the Eclipse workbench, this is the command
		 * identifier.
		 * 
		 * @param identifier
		 *            The identifier of the item to from the listener should be
		 *            removed; must not be <code>null</code>.
		 * @param listener
		 *            The listener to be removed; must not be <code>null</code>.
		 */
		public void removePropertyChangeListener(String identifier,
				IPropertyChangeListener listener);

	}

	/**
	 * The singleton instance of this class. This value may be <code>null</code>--
	 * if it has not yet been initialized.
	 */
// RAP [fappel]: change to session singleton due to memory leaking problems
//	private static ExternalActionManager instance;

	/**
	 * Retrieves the current singleton instance of this class.
	 * 
	 * @return The singleton instance; this value is never <code>null</code>.
	 */
	public static ExternalActionManager getInstance() {
// RAP [fappel]: change to session singleton due to memory leaking problems
//		if (instance == null) {
//			instance = new ExternalActionManager();
//		}
//
//		return instance;
	  return SingletonUtil.getSessionInstance( ExternalActionManager.class );
	}

	/**
	 * The callback mechanism to use to retrieve extra information.
	 */
	private ICallback callback;

	/**
	 * Constructs a new instance of <code>ExternalActionManager</code>.
	 */
	private ExternalActionManager() {
		// This is a singleton class. Only this class should create an instance.
	}

	/**
	 * An accessor for the current call back.
	 * 
	 * @return The current callback mechanism being used. This is the callback
	 *         that should be queried for extra information about actions and
	 *         action contribution items. This value may be <code>null</code>
	 *         if there is no extra information.
	 */
	public ICallback getCallback() {
		return callback;
	}

	/**
	 * A mutator for the current call back
	 * 
	 * @param callbackToUse
	 *            The new callback mechanism to use; this value may be
	 *            <code>null</code> if the default is acceptable (i.e., no
	 *            extra information will provided to actions).
	 */
	public void setCallback(ICallback callbackToUse) {
		callback = callbackToUse;
	}
}
