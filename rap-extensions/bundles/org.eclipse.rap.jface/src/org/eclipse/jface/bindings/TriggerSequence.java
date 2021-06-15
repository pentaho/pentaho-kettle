/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.bindings;

import org.eclipse.jface.util.Util;

/**
 * <p>
 * A sequence of one or more triggers. None of these triggers may be
 * <code>null</code>.
 * </p>
 * 
 * @since 1.4
 */
public abstract class TriggerSequence {
	
	/**
	 * The value to see that hash code to if the hash code is not yet computed.
	 */
	private static final int HASH_CODE_NOT_COMPUTED = -1;

	/**
	 * A factor for computing the hash code for all trigger sequences.
	 */
	private static final int HASH_FACTOR = 89;

	/**
	 * An internal constant used only in this object's hash code algorithm.
	 */
	private static final int HASH_INITIAL = TriggerSequence.class.getName()
			.hashCode();

	/**
	 * The hash code for this object. This value is computed lazily, and marked
	 * as invalid when one of the values on which it is based changes.  This
	 * values is <code>HASH_CODE_NOT_COMPUTED</code> iff the hash code has not
	 * yet been computed.
	 */
	protected transient int hashCode = HASH_CODE_NOT_COMPUTED;

	/**
	 * The list of trigger in this sequence. This value is never
	 * <code>null</code>, and never contains <code>null</code> elements.
	 */
	protected final Trigger[] triggers;

	/**
	 * Constructs a new instance of <code>TriggerSequence</code>.
	 * 
	 * @param triggers
	 *            The triggers contained within this sequence; must not be
	 *            <code>null</code> or contain <code>null</code> elements.
	 *            May be empty.
	 */
	public TriggerSequence(final Trigger[] triggers) {
		if (triggers == null) {
			throw new NullPointerException("The triggers cannot be null"); //$NON-NLS-1$
		}

		for (int i = 0; i < triggers.length; i++) {
			if (triggers[i] == null) {
				throw new IllegalArgumentException(
						"All triggers in a trigger sequence must be an instance of Trigger"); //$NON-NLS-1$
			}
		}

		final int triggerLength = triggers.length;
		this.triggers = new Trigger[triggerLength];
		System.arraycopy(triggers, 0, this.triggers, 0, triggerLength);
	}

	/**
	 * Returns whether or not this key sequence ends with the given key
	 * sequence.
	 * 
	 * @param triggerSequence
	 *            a trigger sequence. Must not be <code>null</code>.
	 * @param equals
	 *            whether or not an identical trigger sequence should be
	 *            considered as a possible match.
	 * @return <code>true</code>, iff the given trigger sequence ends with
	 *         this trigger sequence.
	 */
	public final boolean endsWith(final TriggerSequence triggerSequence,
			final boolean equals) {
		if (triggerSequence == null) {
			throw new NullPointerException(
					"Cannot end with a null trigger sequence"); //$NON-NLS-1$
		}

		return Util.endsWith(triggers, triggerSequence.triggers, equals);
	}

	public final boolean equals(final Object object) {
		// Check if they're the same.
		if (object == this) {
			return true;
		}

		// Check if they're the same type.
		if (!(object instanceof TriggerSequence)) {
			return false;
		}

		final TriggerSequence triggerSequence = (TriggerSequence) object;
		return Util.equals(triggers, triggerSequence.triggers);
	}

	/**
	 * Formats this trigger sequence into the current default look.
	 * 
	 * @return A string representation for this trigger sequence using the
	 *         default look; never <code>null</code>.
	 */
	public abstract String format();

	/**
	 * <p>
	 * Returns a list of prefixes for the current sequence. A prefix is any
	 * leading subsequence in a <code>TriggerSequence</code>. A prefix is
	 * also an instance of <code>TriggerSequence</code>.
	 * </p>
	 * <p>
	 * For example, consider a trigger sequence that consists of four triggers:
	 * A, B, C and D. The prefixes would be "", "A", "A B", and "A B C". The
	 * list of prefixes must always be the same as the size of the trigger list.
	 * </p>
	 * 
	 * @return The array of possible prefixes for this sequence. This array must
	 *         not be <code>null</code>, but may be empty. It must only
	 *         contains instances of <code>TriggerSequence</code>.
	 */
	public abstract TriggerSequence[] getPrefixes();

	/**
	 * Returns the list of triggers.
	 * 
	 * @return The triggers; never <code>null</code> and guaranteed to only
	 *         contain instances of <code>Trigger</code>.
	 */
	public final Trigger[] getTriggers() {
		final int triggerLength = triggers.length;
		final Trigger[] triggerCopy = new Trigger[triggerLength];
		System.arraycopy(triggers, 0, triggerCopy, 0, triggerLength);
		return triggerCopy;
	}

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		if (hashCode == HASH_CODE_NOT_COMPUTED) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(triggers);
			if (hashCode == HASH_CODE_NOT_COMPUTED) {
				hashCode++;
			}
		}

		return hashCode;
	}

	/**
	 * Returns whether or not this trigger sequence is empty.
	 * 
	 * @return <code>true</code>, iff the trigger sequence is empty.
	 */
	public final boolean isEmpty() {
		return (triggers.length == 0);
	}

	/**
	 * Returns whether or not this trigger sequence starts with the given
	 * trigger sequence.
	 * 
	 * @param triggerSequence
	 *            a trigger sequence. Must not be <code>null</code>.
	 * @param equals
	 *            whether or not an identical trigger sequence should be
	 *            considered as a possible match.
	 * @return <code>true</code>, iff the given trigger sequence starts with
	 *         this key sequence.
	 */
	public final boolean startsWith(final TriggerSequence triggerSequence,
			final boolean equals) {
		if (triggerSequence == null) {
			throw new NullPointerException(
					"A trigger sequence cannot start with null"); //$NON-NLS-1$
		}

		return Util.startsWith(triggers, triggerSequence.triggers, equals);
	}
}
