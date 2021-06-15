/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.State;
import org.eclipse.jface.menus.IMenuStateIds;

/**
 * <p>
 * A piece of boolean state grouped with other boolean states. Of these states,
 * only one may have a value of {@link Boolean#TRUE} at any given point in time.
 * The values of all other states must be {@link Boolean#FALSE}.
 * </p>
 * <p>
 * If this state is registered using {@link IMenuStateIds#STYLE}, then it will
 * control the presentation of the command if displayed in the menus, tool bars
 * or status line.
 * </p>
 * <p>
 * Clients may instantiate or extend this interface.
 * </p>
 * 
 * @since 1.0
 */
public class RadioState extends ToggleState {

	/**
	 * The manager of radio groups within the application. This ensures that
	 * only one member of a radio group is active at any one time, and tracks
	 * group memberships.
	 */
	private static final class RadioStateManager {

		/**
		 * A group of radio states with the same identifier.
		 */
		private static final class RadioGroup implements IStateListener {

			/**
			 * The active state. If there is no active state, then this value is
			 * <code>null</code>.
			 */
			private RadioState active = null;

			/**
			 * The current members in this group. If there are no members, then
			 * this value is <code>nlistenerull</code>.
			 */
			private Set members = null;

			/**
			 * Activates a memeber. This checks to see if there are any other
			 * active members. If there are, they are deactivated.
			 * 
			 * @param state
			 *            The state that should become active; must not be
			 *            <code>null</code>.
			 */
			private final void activateMember(final RadioState state) {
				if (active!=null && active != state) {
					active.setValue(Boolean.FALSE);
				}
				active = state;
			}

			/**
			 * Adds a member to this radio group. If the state being added is
			 * active, then it replaces the currently active group memeber as
			 * the active state.
			 * 
			 * @param state
			 *            The state to add; must not be <code>null</code>.
			 */
			private final void addMember(final RadioState state) {
				if (members == null) {
					members = new HashSet(5);
				}

				members.add(state);
				state.addListener(this);

				final Object value = state.getValue();
				if (value instanceof Boolean) {
					if (((Boolean) value).booleanValue()) {
						activateMember(state);
					}
				}
			}

			public final void handleStateChange(final State state,
					final Object oldValue) {
				final Object newValue = state.getValue();
				if (newValue instanceof Boolean) {
					if (((Boolean) newValue).booleanValue()) {
						activateMember((RadioState) state);
					}
				}
			}

			/**
			 * Removes a member from this radio group. If the state was the
			 * active state, then there will be no active state.
			 * 
			 * @param state
			 *            The state to remove; must not be <code>null</code>.
			 */
			private final void removeMember(final RadioState state) {
				state.removeListener(this);
				if (active == state) {
					active = null;
				}

				if (members == null) {
					return;
				}
				members.remove(state);
			}
		}

		/**
		 * The map of radio states indexed by identifier (<code>String</code>).
		 * The radio states is either a single <code>RadioState</code>
		 * instance or a <code>Collection</code> of <code>RadioState</code>
		 * instances.
		 */
		private static Map radioStatesById = null;

		/**
		 * Activates a particular state within a given group.
		 * 
		 * @param identifier
		 *            The identifier of the group to which the state belongs;
		 *            must not be <code>null</code>.
		 * @param state
		 *            The state to activate; must not be <code>null</code>.
		 */
		private static final void activateGroup(final String identifier,
				final RadioState state) {
			if (radioStatesById == null) {
				return;
			}

			final Object currentValue = radioStatesById.get(identifier);
			if (currentValue instanceof RadioGroup) {
				final RadioGroup radioGroup = (RadioGroup) currentValue;
				radioGroup.activateMember(state);
			}
		}

		/**
		 * Registers a piece of state with the radio manager.
		 * 
		 * @param identifier
		 *            The identifier of the radio group; must not be
		 *            <code>null</code>.
		 * @param state
		 *            The state to register; must not be <code>null</code>.
		 */
		private static final void registerState(final String identifier,
				final RadioState state) {
			if (radioStatesById == null) {
				radioStatesById = new HashMap();
			}

			final Object currentValue = radioStatesById.get(identifier);
			final RadioGroup radioGroup;
			if (currentValue instanceof RadioGroup) {
				radioGroup = (RadioGroup) currentValue;
			} else {
				radioGroup = new RadioGroup();
				radioStatesById.put(identifier, radioGroup);
			}
			radioGroup.addMember(state);
		}

		/**
		 * Unregisters a piece of state from the radio manager.
		 * 
		 * @param identifier
		 *            The identifier of the radio group; must not be
		 *            <code>null</code>.
		 * @param state
		 *            The state to unregister; must not be <code>null</code>.
		 */
		private static final void unregisterState(final String identifier,
				final RadioState state) {
			if (radioStatesById == null) {
				return;
			}

			final Object currentValue = radioStatesById.get(identifier);
			if (currentValue instanceof RadioGroup) {
				final RadioGroup radioGroup = (RadioGroup) currentValue;
				radioGroup.removeMember(state);
			}
		}
	}

	/**
	 * The identifier of the radio group to which this state belongs. This value
	 * may be <code>null</code> if this state doesn't really belong to a group
	 * (yet).
	 */
	private String radioGroupIdentifier = null;

	/**
	 * Unregisters this state from the manager, which detaches the listeners.
	 */
	public void dispose() {
		setRadioGroupIdentifier(null);
	}

	/**
	 * Sets the identifier of the radio group for this piece of state. If the
	 * identifier is cleared, then the state is unregistered.
	 * 
	 * @param identifier
	 *            The identifier of the radio group for this state; may be
	 *            <code>null</code> if the identifier is being cleared.
	 * 
	 */
	public final void setRadioGroupIdentifier(final String identifier) {
		if (identifier == null) {
			RadioStateManager.unregisterState(radioGroupIdentifier, this);
			radioGroupIdentifier = null;
		} else {
			radioGroupIdentifier = identifier;
			RadioStateManager.registerState(identifier, this);
		}
	}

	/**
	 * Sets the value for this object. This notifies the radio state manager of
	 * the change.
	 * 
	 * @param value
	 *            The new value; should be a <code>Boolean</code>.
	 */
	public void setValue(final Object value) {
		if (!(value instanceof Boolean)) {
			throw new IllegalArgumentException(
					"RadioState takes a Boolean as a value"); //$NON-NLS-1$
		}

		if (((Boolean) value).booleanValue() && (radioGroupIdentifier != null)) {
			RadioStateManager.activateGroup(radioGroupIdentifier, this);
		}

		super.setValue(value);
	}
}
