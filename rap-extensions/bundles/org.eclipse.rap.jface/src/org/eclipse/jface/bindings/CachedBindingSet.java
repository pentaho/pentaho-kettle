/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.bindings;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.util.Tracing;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A resolution of bindings for a given state. To see if we already have a
 * cached binding set, just create one of these binding sets and then look it up
 * in a map. If it is not already there, then add it and set the cached binding
 * resolution.
 * </p>
 * 
 * @since 1.4
 */
final class CachedBindingSet {

	/**
	 * A factor for computing the hash code for all cached binding sets.
	 */
	private final static int HASH_FACTOR = 89;

	/**
	 * The seed for the hash code for all cached binding sets.
	 */
	private final static int HASH_INITIAL = CachedBindingSet.class.getName()
			.hashCode();

	/**
	 * <p>
	 * A representation of the tree of active contexts at the time this cached
	 * binding set was computed. It is a map of context id (<code>String</code>)
	 * to context id (<code>String</code>). Each key represents one of the
	 * active contexts or one of its ancestors, while each value represents its
	 * parent. This is a way of perserving information about what the hierarchy
	 * looked like.
	 * </p>
	 * <p>
	 * This value will be <code>null</code> if the contexts were disregarded
	 * in the computation. It may also be empty. All of the keys are guaranteed
	 * to be non- <code>null</code>, but the values can be <code>null</code>
	 * (i.e., no parent).
	 * </p>
	 */
	private final Map activeContextTree;

	/**
	 * The map representing the resolved state of the bindings. This is a map of
	 * a trigger (<code>TriggerSequence</code>) to binding (<code>Binding</code>).
	 * This value may be <code>null</code> if it has not yet been initialized.
	 */
	private Map bindingsByTrigger = null;

	/**
	 * A map of triggers to collections of bindings. If this binding set
	 * contains conflicts, they are logged here.
	 */
	private Map conflictsByTrigger = null;

	/**
	 * The hash code for this object. This value is computed lazily, and marked
	 * as invalid when one of the values on which it is based changes.
	 */
	private transient int hashCode;

	/**
	 * Whether <code>hashCode</code> still contains a valid value.
	 */
	private transient boolean hashCodeComputed = false;

	/**
	 * <p>
	 * The list of locales that were active at the time this binding set was
	 * computed. This list starts with the most specific representation of the
	 * locale, and moves to more general representations. For example, this
	 * array might look like ["en_US", "en", "", null].
	 * </p>
	 * <p>
	 * This value will never be <code>null</code>, and it will never be
	 * empty. It must contain at least one element, but its elements can be
	 * <code>null</code>.
	 * </p>
	 */
	private final String[] locales;

	/**
	 * <p>
	 * The list of platforms that were active at the time this binding set was
	 * computed. This list starts with the most specific representation of the
	 * platform, and moves to more general representations. For example, this
	 * array might look like ["gtk", "", null].
	 * </p>
	 * <p>
	 * This value will never be <code>null</code>, and it will never be
	 * empty. It must contain at least one element, but its elements can be
	 * <code>null</code>.
	 * </p>
	 */
	private final String[] platforms;

	/**
	 * A map of prefixes (<code>TriggerSequence</code>) to a map of
	 * available completions (possibly <code>null</code>, which means there
	 * is an exact match). The available completions is a map of trigger (<code>TriggerSequence</code>)
	 * to command identifier (<code>String</code>). This value is
	 * <code>null</code> if it has not yet been initialized.
	 */
	private Map prefixTable = null;

	/**
	 * <p>
	 * The list of schemes that were active at the time this binding set was
	 * computed. This list starts with the active scheme, and then continues
	 * with all of its ancestors -- in order. For example, this might look like
	 * ["emacs", "default"].
	 * </p>
	 * <p>
	 * This value will never be <code>null</code>, and it will never be
	 * empty. It must contain at least one element. Its elements cannot be
	 * <code>null</code>.
	 * </p>
	 */
	private final String[] schemeIds;

	/**
	 * The map representing the resolved state of the bindings. This is a map of
	 * a command id (<code>String</code>) to triggers (<code>Collection</code>
	 * of <code>TriggerSequence</code>). This value may be <code>null</code>
	 * if it has not yet been initialized.
	 */
	private Map triggersByCommandId = null;

	/**
	 * Constructs a new instance of <code>CachedBindingSet</code>.
	 * 
	 * @param activeContextTree
	 *            The set of context identifiers that were active when this
	 *            binding set was calculated; may be empty. If it is
	 *            <code>null</code>, then the contexts were disregarded in
	 *            the computation. This is a map of context id (
	 *            <code>String</code>) to parent context id (
	 *            <code>String</code>). This is a way of caching the look of
	 *            the context tree at the time the binding set was computed.
	 * @param locales
	 *            The locales that were active when this binding set was
	 *            calculated. The first element is the currently active locale,
	 *            and it is followed by increasingly more general locales. This
	 *            must not be <code>null</code> and must contain at least one
	 *            element. The elements can be <code>null</code>, though.
	 * @param platforms
	 *            The platform that were active when this binding set was
	 *            calculated. The first element is the currently active
	 *            platform, and it is followed by increasingly more general
	 *            platforms. This must not be <code>null</code> and must
	 *            contain at least one element. The elements can be
	 *            <code>null</code>, though.
	 * @param schemeIds
	 *            The scheme that was active when this binding set was
	 *            calculated, followed by its ancestors. This may be
	 *            <code>null</code or empty. The
	 *            elements cannot be <code>null</code>.
	 */
	CachedBindingSet(final Map activeContextTree, final String[] locales,
			final String[] platforms, final String[] schemeIds) {
		if (locales == null) {
			throw new NullPointerException("The locales cannot be null."); //$NON-NLS-1$
		}

		if (locales.length == 0) {
			throw new NullPointerException("The locales cannot be empty."); //$NON-NLS-1$
		}

		if (platforms == null) {
			throw new NullPointerException("The platforms cannot be null."); //$NON-NLS-1$
		}

		if (platforms.length == 0) {
			throw new NullPointerException("The platforms cannot be empty."); //$NON-NLS-1$
		}

		this.activeContextTree = activeContextTree;
		this.locales = locales;
		this.platforms = platforms;
		this.schemeIds = schemeIds;
	}

	/**
	 * Compares this binding set with another object. The objects will be equal
	 * if they are both instance of <code>CachedBindingSet</code> and have
	 * equivalent values for all of their properties.
	 * 
	 * @param object
	 *            The object with which to compare; may be <code>null</code>.
	 * @return <code>true</code> if they are both instances of
	 *         <code>CachedBindingSet</code> and have the same values for all
	 *         of their properties; <code>false</code> otherwise.
	 */
	public final boolean equals(final Object object) {
		if (!(object instanceof CachedBindingSet)) {
			return false;
		}

		final CachedBindingSet other = (CachedBindingSet) object;

		if (!Util.equals(activeContextTree, other.activeContextTree)) {
			return false;
		}
		if (!Util.equals(locales, other.locales)) {
			return false;
		}
		if (!Util.equals(platforms, other.platforms)) {
			return false;
		}
		return Util.equals(schemeIds, other.schemeIds);
	}

	/**
	 * Returns the map of command identifiers indexed by trigger sequence.
	 * 
	 * @return A map of triggers (<code>TriggerSequence</code>) to bindings (<code>Binding</code>).
	 *         This value may be <code>null</code> if this was not yet
	 *         initialized.
	 */
	final Map getBindingsByTrigger() {
		return bindingsByTrigger;
	}

	/**
	 * Returns a map of conflicts for this set of contexts.
	 * 
	 * @return A map of trigger to a collection of Bindings. May be
	 *         <code>null</code>.
	 */
	final Map getConflictsByTrigger() {
		return conflictsByTrigger;
	}

	/**
	 * Returns the map of prefixes to a map of trigger sequence to command
	 * identifiers.
	 * 
	 * @return A map of prefixes (<code>TriggerSequence</code>) to a map of
	 *         available completions (possibly <code>null</code>, which means
	 *         there is an exact match). The available completions is a map of
	 *         trigger (<code>TriggerSequence</code>) to command identifier (<code>String</code>).
	 *         This value may be <code>null</code> if it has not yet been
	 *         initialized.
	 */
	final Map getPrefixTable() {
		return prefixTable;
	}

	/**
	 * Returns the map of triggers indexed by command identifiers.
	 * 
	 * @return A map of command identifiers (<code>String</code>) to
	 *         triggers (<code>Collection</code> of
	 *         <code>TriggerSequence</code>). This value may be
	 *         <code>null</code> if this was not yet initialized.
	 */
	final Map getTriggersByCommandId() {
		return triggersByCommandId;
	}

	/**
	 * Computes the hash code for this cached binding set. The hash code is
	 * based only on the immutable values. This allows the set to be created and
	 * checked for in a hashed collection <em>before</em> doing any
	 * computation.
	 * 
	 * @return The hash code for this cached binding set.
	 */
	public final int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR
					+ Util.hashCode(activeContextTree);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(locales);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(platforms);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(schemeIds);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	/**
	 * Sets the map of command identifiers indexed by trigger.
	 * 
	 * @param commandIdsByTrigger
	 *            The map to set; must not be <code>null</code>. This is a
	 *            map of triggers (<code>TriggerSequence</code>) to binding (<code>Binding</code>).
	 */
	final void setBindingsByTrigger(final Map commandIdsByTrigger) {
		if (commandIdsByTrigger == null) {
			throw new NullPointerException(
					"Cannot set a null binding resolution"); //$NON-NLS-1$
		}

		this.bindingsByTrigger = commandIdsByTrigger;
	}

	/**
	 * Sets the map of conflicting bindings by trigger.
	 * 
	 * @param conflicts
	 *            The map to set; must not be <code>null</code>.
	 */
	final void setConflictsByTrigger(final Map conflicts) {
		if (conflicts == null) {
			throw new NullPointerException(
					"Cannot set a null binding conflicts"); //$NON-NLS-1$
		}
		conflictsByTrigger = conflicts;
	}

	/**
	 * Sets the map of prefixes to a map of trigger sequence to command
	 * identifiers.
	 * 
	 * @param prefixTable
	 *            A map of prefixes (<code>TriggerSequence</code>) to a map
	 *            of available completions (possibly <code>null</code>, which
	 *            means there is an exact match). The available completions is a
	 *            map of trigger (<code>TriggerSequence</code>) to command
	 *            identifier (<code>String</code>). Must not be
	 *            <code>null</code>.
	 */
	final void setPrefixTable(final Map prefixTable) {
		if (prefixTable == null) {
			this.prefixTable = Collections.EMPTY_MAP;
			if (BindingManager.DEBUG) {
				Tracing.printTrace("BINDINGS", "Cannot set a null prefix table, set to EMPTY_MAP"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}

		this.prefixTable = prefixTable;
	}

	/**
	 * Sets the map of triggers indexed by command identifiers.
	 * 
	 * @param triggersByCommandId
	 *            The map to set; must not be <code>null</code>. This is a
	 *            map of command identifiers (<code>String</code>) to
	 *            triggers (<code>Collection</code> of
	 *            <code>TriggerSequence</code>).
	 */
	final void setTriggersByCommandId(final Map triggersByCommandId) {
		if (triggersByCommandId == null) {
			throw new NullPointerException(
					"Cannot set a null binding resolution"); //$NON-NLS-1$
		}

		this.triggersByCommandId = triggersByCommandId;
	}
}
