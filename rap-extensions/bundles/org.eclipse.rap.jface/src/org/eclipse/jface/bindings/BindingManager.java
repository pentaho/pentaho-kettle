/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.bindings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.HandleObjectManager;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.Util;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * A central repository for bindings -- both in the defined and undefined
 * states. Schemes and bindings can be created and retrieved using this manager.
 * It is possible to listen to changes in the collection of schemes and bindings
 * by adding a listener to the manager.
 * </p>
 * <p>
 * The binding manager is very sensitive to performance. Misusing the manager
 * can render an application unenjoyable to use. As such, each of the public
 * methods states the current run-time performance. In future releases, it is
 * guaranteed that the method will run in at least the stated time constraint --
 * though it might get faster. Where possible, we have also tried to be memory
 * efficient.
 * </p>
 *
 * @since 1.4
 */
public final class BindingManager extends HandleObjectManager implements
		IContextManagerListener, ISchemeListener {

	/**
	 * This flag can be set to <code>true</code> if the binding manager should
	 * print information to <code>System.out</code> when certain boundary
	 * conditions occur.
	 */
	public static boolean DEBUG = false;

	/**
	 * Returned for optimized lookup.
	 */
	private static final TriggerSequence[] EMPTY_TRIGGER_SEQUENCE = new TriggerSequence[0];

	/**
	 * The separator character used in locales.
	 */
	private static final String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$

	private Map currentConflicts = null;

	/**
	 * </p>
	 * A utility method for adding entries to a map. The map is checked for
	 * entries at the key. If such an entry exists, it is expected to be a
	 * <code>Collection</code>. The value is then appended to the collection.
	 * If no such entry exists, then a collection is created, and the value
	 * added to the collection.
	 * </p>
	 *
	 * @param map
	 *            The map to modify; if this value is <code>null</code>, then
	 *            this method simply returns.
	 * @param key
	 *            The key to look up in the map; may be <code>null</code>.
	 * @param value
	 *            The value to look up in the map; may be <code>null</code>.
	 */
	private static final void addReverseLookup(final Map map, final Object key,
			final Object value) {
		if (map == null) {
			return;
		}

		final Object currentValue = map.get(key);
		if (currentValue != null) {
			final Collection values = (Collection) currentValue;
			values.add(value);
		} else { // currentValue == null
			final Collection values = new ArrayList(1);
			values.add(value);
			map.put(key, values);
		}
	}

	/**
	 * <p>
	 * Takes a fully-specified string, and converts it into an array of
	 * increasingly less-specific strings. So, for example, "en_GB" would become
	 * ["en_GB", "en", "", null].
	 * </p>
	 * <p>
	 * This method runs in linear time (O(n)) over the length of the string.
	 * </p>
	 *
	 * @param string
	 *            The string to break apart into its less specific components;
	 *            should not be <code>null</code>.
	 * @param separator
	 *            The separator that indicates a separation between a degrees of
	 *            specificity; should not be <code>null</code>.
	 * @return An array of strings from the most specific (i.e.,
	 *         <code>string</code>) to the least specific (i.e.,
	 *         <code>null</code>).
	 */
	private static final String[] expand(String string, final String separator) {
		// Test for boundary conditions.
		if (string == null || separator == null) {
			return new String[0];
		}

		final List strings = new ArrayList();
		final StringBuffer stringBuffer = new StringBuffer();
		string = string.trim(); // remove whitespace
		if (string.length() > 0) {
			final StringTokenizer stringTokenizer = new StringTokenizer(string,
					separator);
			while (stringTokenizer.hasMoreElements()) {
				if (stringBuffer.length() > 0) {
					stringBuffer.append(separator);
				}
				stringBuffer.append(((String) stringTokenizer.nextElement())
						.trim());
				strings.add(stringBuffer.toString());
			}
		}
		Collections.reverse(strings);
		strings.add(Util.ZERO_LENGTH_STRING);
		strings.add(null);
		return (String[]) strings.toArray(new String[strings.size()]);
	}

	/**
	 * The active bindings. This is a map of triggers (
	 * <code>TriggerSequence</code>) to bindings (<code>Binding</code>).
	 * This value will only be <code>null</code> if the active bindings have
	 * not yet been computed. Otherwise, this value may be empty.
	 */
	private Map activeBindings = null;

	/**
	 * The active bindings indexed by fully-parameterized commands. This is a
	 * map of fully-parameterized commands (<code>ParameterizedCommand</code>)
	 * to triggers ( <code>TriggerSequence</code>). This value will only be
	 * <code>null</code> if the active bindings have not yet been computed.
	 * Otherwise, this value may be empty.
	 */
	private Map activeBindingsByParameterizedCommand = null;

	private Set triggerConflicts = new HashSet();

	/**
	 * The scheme that is currently active. An active scheme is the one that is
	 * currently dictating which bindings will actually work. This value may be
	 * <code>null</code> if there is no active scheme. If the active scheme
	 * becomes undefined, then this should automatically revert to
	 * <code>null</code>.
	 */
	private Scheme activeScheme = null;

	/**
	 * The array of scheme identifiers, starting with the active scheme and
	 * moving up through its parents. This value may be <code>null</code> if
	 * there is no active scheme.
	 */
	private String[] activeSchemeIds = null;

	/**
	 * The number of bindings in the <code>bindings</code> array.
	 */
	private int bindingCount = 0;

	/**
	 * A cache of context IDs that weren't defined.
	 */
	private Set bindingErrors = new HashSet();

	/**
	 * The array of all bindings currently handled by this manager. This array
	 * is the raw list of bindings, as provided to this manager. This value may
	 * be <code>null</code> if there are no bindings. The size of this array
	 * is not necessarily the number of bindings.
	 */
	private Binding[] bindings = null;

	/**
	 * A cache of the bindings previously computed by this manager. This value
	 * may be empty, but it is never <code>null</code>. This is a map of
	 * <code>CachedBindingSet</code> to <code>CachedBindingSet</code>.
	 */
	private Map cachedBindings = new HashMap();

	/**
	 * The command manager for this binding manager. This manager is only needed
	 * for the <code>getActiveBindingsFor(String)</code> method. This value is
	 * guaranteed to never be <code>null</code>.
	 */
	private final CommandManager commandManager;

	/**
	 * The context manager for this binding manager. For a binding manager to
	 * function, it needs to listen for changes to the contexts. This value is
	 * guaranteed to never be <code>null</code>.
	 */
	private final ContextManager contextManager;

	/**
	 * The locale for this manager. This defaults to the current locale. The
	 * value will never be <code>null</code>.
	 */
	private String locale = Locale.getDefault().toString();

	/**
	 * The array of locales, starting with the active locale and moving up
	 * through less specific representations of the locale. For example,
	 * ["en_US", "en", "", null]. This value will never be <code>null</code>.
	 */
	private String[] locales = expand(locale, LOCALE_SEPARATOR);

	/**
	 * The platform for this manager. This defaults to the current platform. The
	 * value will never be <code>null</code>.
	 */
	private String platform = Util.getWS();

	/**
	 * The array of platforms, starting with the active platform and moving up
	 * through less specific representations of the platform. For example,
	 * ["gtk", "", null]. This value will never be <code>null,/code>.
	 */
	private String[] platforms = expand(platform, Util.ZERO_LENGTH_STRING);

	/**
	 * A map of prefixes (<code>TriggerSequence</code>) to a map of
	 * available completions (possibly <code>null</code>, which means there
	 * is an exact match). The available completions is a map of trigger (<code>TriggerSequence</code>)
	 * to bindings (<code>Binding</code>). This value may be
	 * <code>null</code> if there is no existing solution.
	 */
	private Map prefixTable = null;

	/**
	 * <p>
	 * Constructs a new instance of <code>BindingManager</code>.
	 * </p>
	 * <p>
	 * This method completes in amortized constant time (O(1)).
	 * </p>
	 *
	 * @param contextManager
	 *            The context manager that will support this binding manager.
	 *            This value must not be <code>null</code>.
	 * @param commandManager
	 *            The command manager that will support this binding manager.
	 *            This value must not be <code>null</code>.
	 */
	public BindingManager(final ContextManager contextManager,
			final CommandManager commandManager) {
		if (contextManager == null) {
			throw new NullPointerException(
					"A binding manager requires a context manager"); //$NON-NLS-1$
		}

		if (commandManager == null) {
			throw new NullPointerException(
					"A binding manager requires a command manager"); //$NON-NLS-1$
		}

		this.contextManager = contextManager;
		contextManager.addContextManagerListener(this);
		this.commandManager = commandManager;
	}

	/**
	 * <p>
	 * Adds a single new binding to the existing array of bindings. If the array
	 * is currently <code>null</code>, then a new array is created and this
	 * binding is added to it. This method does not detect duplicates.
	 * </p>
	 * <p>
	 * This method completes in amortized <code>O(1)</code>.
	 * </p>
	 *
	 * @param binding
	 *            The binding to be added; must not be <code>null</code>.
	 */
	public final void addBinding(final Binding binding) {
		if (binding == null) {
			throw new NullPointerException("Cannot add a null binding"); //$NON-NLS-1$
		}

		if (bindings == null) {
			bindings = new Binding[1];
		} else if (bindingCount >= bindings.length) {
			final Binding[] oldBindings = bindings;
			bindings = new Binding[oldBindings.length * 2];
			System.arraycopy(oldBindings, 0, bindings, 0, oldBindings.length);
		}
		bindings[bindingCount++] = binding;
		clearCache();
	}

	/**
	 * <p>
	 * Adds a listener to this binding manager. The listener will be notified
	 * when the set of defined schemes or bindings changes. This can be used to
	 * track the global appearance and disappearance of bindings.
	 * </p>
	 * <p>
	 * This method completes in amortized constant time (<code>O(1)</code>).
	 * </p>
	 *
	 * @param listener
	 *            The listener to attach; must not be <code>null</code>.
	 */
	public final void addBindingManagerListener(
			final IBindingManagerListener listener) {
		addListenerObject(listener);
	}

	/**
	 * <p>
	 * Builds a prefix table look-up for a map of active bindings.
	 * </p>
	 * <p>
	 * This method takes <code>O(mn)</code>, where <code>m</code> is the
	 * length of the trigger sequences and <code>n</code> is the number of
	 * bindings.
	 * </p>
	 *
	 * @param activeBindings
	 *            The map of triggers (<code>TriggerSequence</code>) to
	 *            command ids (<code>String</code>) which are currently
	 *            active. This value may be <code>null</code> if there are no
	 *            active bindings, and it may be empty. It must not be
	 *            <code>null</code>.
	 * @return A map of prefixes (<code>TriggerSequence</code>) to a map of
	 *         available completions (possibly <code>null</code>, which means
	 *         there is an exact match). The available completions is a map of
	 *         trigger (<code>TriggerSequence</code>) to command identifier (<code>String</code>).
	 *         This value will never be <code>null</code>, but may be empty.
	 */
	private final Map buildPrefixTable(final Map activeBindings) {
		final Map prefixTable = new HashMap();

		final Iterator bindingItr = activeBindings.entrySet().iterator();
		while (bindingItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) bindingItr.next();
			final TriggerSequence triggerSequence = (TriggerSequence) entry
					.getKey();

			// Add the perfect match.
			if (!prefixTable.containsKey(triggerSequence)) {
				prefixTable.put(triggerSequence, null);
			}

			final TriggerSequence[] prefixes = triggerSequence.getPrefixes();
			final int prefixesLength = prefixes.length;
			if (prefixesLength == 0) {
				continue;
			}

			// Break apart the trigger sequence.
			final Binding binding = (Binding) entry.getValue();
			for (int i = 0; i < prefixesLength; i++) {
				final TriggerSequence prefix = prefixes[i];
				final Object value = prefixTable.get(prefix);
				if ((prefixTable.containsKey(prefix)) && (value instanceof Map)) {
					((Map) value).put(triggerSequence, binding);
				} else {
					final Map map = new HashMap();
					prefixTable.put(prefix, map);
					map.put(triggerSequence, binding);
				}
			}
		}

		return prefixTable;
	}

	/**
	 * <p>
	 * Clears the cache, and the existing solution. If debugging is turned on,
	 * then this will also print a message to standard out.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 */
	private final void clearCache() {
		if (DEBUG) {
			Tracing.printTrace("BINDINGS", "Clearing cache"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		cachedBindings.clear();
		clearSolution();
	}

	/**
	 * <p>
	 * Clears the existing solution.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 */
	private final void clearSolution() {
		setActiveBindings(null, null, null, null);
	}

	/**
	 * Compares the identifier of two schemes, and decides which scheme is the
	 * youngest (i.e., the child) of the two. Both schemes should be active
	 * schemes.
	 *
	 * @param schemeId1
	 *            The identifier of the first scheme; must not be
	 *            <code>null</code>.
	 * @param schemeId2
	 *            The identifier of the second scheme; must not be
	 *            <code>null</code>.
	 * @return <code>0</code> if the two schemes are equal of if neither
	 *         scheme is active; <code>1</code> if the second scheme is the
	 *         youngest; and <code>-1</code> if the first scheme is the
	 *         youngest.
	 */
	private final int compareSchemes(final String schemeId1,
			final String schemeId2) {
		if (!schemeId2.equals(schemeId1)) {
			for (int i = 0; i < activeSchemeIds.length; i++) {
				final String schemePointer = activeSchemeIds[i];
				if (schemeId2.equals(schemePointer)) {
					return 1;

				} else if (schemeId1.equals(schemePointer)) {
					return -1;

				}

			}
		}

		return 0;
	}

	/**
	 * <p>
	 * Computes the bindings given the context tree, and inserts them into the
	 * <code>commandIdsByTrigger</code>. It is assumed that
	 * <code>locales</code>,<code>platforsm</code> and
	 * <code>schemeIds</code> correctly reflect the state of the application.
	 * This method does not deal with caching.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @param activeContextTree
	 *            The map representing the tree of active contexts. The map is
	 *            one of child to parent, each being a context id (
	 *            <code>String</code>). The keys are never <code>null</code>,
	 *            but the values may be (i.e., no parent). This map may be
	 *            empty. It may be <code>null</code> if we shouldn't consider
	 *            contexts.
	 * @param bindingsByTrigger
	 *            The empty of map that is intended to be filled with triggers (
	 *            <code>TriggerSequence</code>) to bindings (
	 *            <code>Binding</code>). This value must not be
	 *            <code>null</code> and must be empty.
	 * @param triggersByCommandId
	 *            The empty of map that is intended to be filled with command
	 *            identifiers (<code>String</code>) to triggers (
	 *            <code>TriggerSequence</code>). This value must either be
	 *            <code>null</code> (indicating that these values are not
	 *            needed), or empty (indicating that this map should be
	 *            computed).
	 */
	private final void computeBindings(final Map activeContextTree,
			final Map bindingsByTrigger, final Map triggersByCommandId,
			final Map conflictsByTrigger) {
		/*
		 * FIRST PASS: Remove all of the bindings that are marking deletions.
		 */
		final Binding[] trimmedBindings = removeDeletions(bindings);

		/*
		 * SECOND PASS: Just throw in bindings that match the current state. If
		 * there is more than one match for a binding, then create a list.
		 */
		final Map possibleBindings = new HashMap();
		final int length = trimmedBindings.length;
		for (int i = 0; i < length; i++) {
			final Binding binding = trimmedBindings[i];
			boolean found;

			// Check the context.
			final String contextId = binding.getContextId();
			if ((activeContextTree != null)
					&& (!activeContextTree.containsKey(contextId))) {
				continue;
			}

			// Check the locale.
			if (!localeMatches(binding)) {
				continue;
			}

			// Check the platform.
			if (!platformMatches(binding)) {
				continue;
			}

			// Check the scheme ids.
			final String schemeId = binding.getSchemeId();
			found = false;
			if (activeSchemeIds != null) {
				for (int j = 0; j < activeSchemeIds.length; j++) {
					if (Util.equals(schemeId, activeSchemeIds[j])) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				continue;
			}

			// Insert the match into the list of possible matches.
			final TriggerSequence trigger = binding.getTriggerSequence();
			final Object existingMatch = possibleBindings.get(trigger);
			if (existingMatch instanceof Binding) {
				possibleBindings.remove(trigger);
				final Collection matches = new ArrayList();
				matches.add(existingMatch);
				matches.add(binding);
				possibleBindings.put(trigger, matches);

			} else if (existingMatch instanceof Collection) {
				final Collection matches = (Collection) existingMatch;
				matches.add(binding);

			} else {
				possibleBindings.put(trigger, binding);
			}
		}

		MultiStatus conflicts = new MultiStatus("org.eclipse.jface", 0, //$NON-NLS-1$
				"Keybinding conflicts occurred.  They may interfere with normal accelerator operation.", //$NON-NLS-1$
				null);
		/*
		 * THIRD PASS: In this pass, we move any non-conflicting bindings
		 * directly into the map. In the case of conflicts, we apply some
		 * further logic to try to resolve them. If the conflict can't be
		 * resolved, then we log the problem.
		 */
		final Iterator possibleBindingItr = possibleBindings.entrySet()
				.iterator();
		while (possibleBindingItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) possibleBindingItr.next();
			final TriggerSequence trigger = (TriggerSequence) entry.getKey();
			final Object match = entry.getValue();
			/*
			 * What we do depends slightly on whether we are trying to build a
			 * list of all possible bindings (disregarding context), or a flat
			 * map given the currently active contexts.
			 */
			if (activeContextTree == null) {
				// We are building the list of all possible bindings.
				final Collection bindings = new ArrayList();
				if (match instanceof Binding) {
					bindings.add(match);
					bindingsByTrigger.put(trigger, bindings);
					addReverseLookup(triggersByCommandId, ((Binding) match)
							.getParameterizedCommand(), trigger);

				} else if (match instanceof Collection) {
					bindings.addAll((Collection) match);
					bindingsByTrigger.put(trigger, bindings);

					final Iterator matchItr = bindings.iterator();
					while (matchItr.hasNext()) {
						addReverseLookup(triggersByCommandId,
								((Binding) matchItr.next())
										.getParameterizedCommand(), trigger);
					}
				}

			} else {
				// We are building the flat map of trigger to commands.
				if (match instanceof Binding) {
					final Binding binding = (Binding) match;
					bindingsByTrigger.put(trigger, binding);
					addReverseLookup(triggersByCommandId, binding
							.getParameterizedCommand(), trigger);

				} else if (match instanceof Collection) {
					final Binding winner = resolveConflicts((Collection) match,
							activeContextTree);
					if (winner == null) {
						// warn once ... so as not to flood the logs
						conflictsByTrigger.put(trigger, match);
						if (triggerConflicts.add(trigger)) {
							final StringWriter sw = new StringWriter();
							final BufferedWriter buffer = new BufferedWriter(sw);
							try {
								buffer.write("A conflict occurred for "); //$NON-NLS-1$
								buffer.write(trigger.toString());
								buffer.write(':');
								Iterator i = ((Collection) match).iterator();
								while (i.hasNext()) {
									buffer.newLine();
									buffer.write(i.next().toString());
								}
								buffer.flush();
							} catch (IOException e) {
								// we should not get this
							}
							conflicts.add(new Status(IStatus.WARNING,
									"org.eclipse.jface", //$NON-NLS-1$
									sw.toString()));
						}
						if (DEBUG) {
							Tracing.printTrace("BINDINGS", //$NON-NLS-1$
									"A conflict occurred for " + trigger); //$NON-NLS-1$
							Tracing.printTrace("BINDINGS", "    " + match); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else {
						bindingsByTrigger.put(trigger, winner);
						addReverseLookup(triggersByCommandId, winner
								.getParameterizedCommand(), trigger);
					}
				}
			}
		}
		if (conflicts.getSeverity() != IStatus.OK) {
			Policy.getLog().log(conflicts);
		}
	}

	/**
	 * <p>
	 * Notifies this manager that the context manager has changed. This method
	 * is intended for internal use only.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 */
	public final void contextManagerChanged(
			final ContextManagerEvent contextManagerEvent) {
		if (contextManagerEvent.isActiveContextsChanged()) {
// clearSolution();
			recomputeBindings();
		}
	}

	/**
	 * Returns the number of strokes in an array of triggers. It is assumed that
	 * there is one natural key per trigger. The strokes are counted based on
	 * the type of key. Natural keys are worth one; ctrl is worth two; shift is
	 * worth four; and alt is worth eight.
	 *
	 * @param triggers
	 *            The triggers on which to count strokes; must not be
	 *            <code>null</code>.
	 * @return The value of the strokes in the triggers.
	 */
	private final int countStrokes(final Trigger[] triggers) {
		int strokeCount = triggers.length;
		for (int i = 0; i < triggers.length; i++) {
			final Trigger trigger = triggers[i];
			if (trigger instanceof KeyStroke) {
				final KeyStroke keyStroke = (KeyStroke) trigger;
				final int modifierKeys = keyStroke.getModifierKeys();
				final IKeyLookup lookup = KeyLookupFactory.getDefault();
				if ((modifierKeys & lookup.getAlt()) != 0) {
					strokeCount += 8;
				}
				if ((modifierKeys & lookup.getCtrl()) != 0) {
					strokeCount += 2;
				}
				if ((modifierKeys & lookup.getShift()) != 0) {
					strokeCount += 4;
				}
				if ((modifierKeys & lookup.getCommand()) != 0) {
					strokeCount += 2;
				}
			} else {
				strokeCount += 99;
			}
		}

		return strokeCount;
	}

	/**
	 * <p>
	 * Creates a tree of context identifiers, representing the hierarchical
	 * structure of the given contexts. The tree is structured as a mapping from
	 * child to parent.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the height of the context tree.
	 * </p>
	 *
	 * @param contextIds
	 *            The set of context identifiers to be converted into a tree;
	 *            must not be <code>null</code>.
	 * @return The tree of contexts to use; may be empty, but never
	 *         <code>null</code>. The keys and values are both strings.
	 */
	private final Map createContextTreeFor(final Set contextIds) {
		final Map contextTree = new HashMap();

		final Iterator contextIdItr = contextIds.iterator();
		while (contextIdItr.hasNext()) {
			String childContextId = (String) contextIdItr.next();
			while (childContextId != null) {
				// Check if we've already got the part of the tree from here up.
				if (contextTree.containsKey(childContextId)) {
					break;
				}

				// Retrieve the context.
				final Context childContext = contextManager
						.getContext(childContextId);

				// Add the child-parent pair to the tree.
				try {
					final String parentContextId = childContext.getParentId();
					contextTree.put(childContextId, parentContextId);
					childContextId = parentContextId;
				} catch (final NotDefinedException e) {
					break; // stop ascending
				}
			}
		}

		return contextTree;
	}

	/**
	 * <p>
	 * Creates a tree of context identifiers, representing the hierarchical
	 * structure of the given contexts. The tree is structured as a mapping from
	 * child to parent. In this tree, the key binding specific filtering of
	 * contexts will have taken place.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n^2)</code>, where <code>n</code>
	 * is the height of the context tree.
	 * </p>
	 *
	 * @param contextIds
	 *            The set of context identifiers to be converted into a tree;
	 *            must not be <code>null</code>.
	 * @return The tree of contexts to use; may be empty, but never
	 *         <code>null</code>. The keys and values are both strings.
	 */
	private final Map createFilteredContextTreeFor(final Set contextIds) {
		// Check to see whether a dialog or window is active.
		boolean dialog = false;
		boolean window = false;
		Iterator contextIdItr = contextIds.iterator();
		while (contextIdItr.hasNext()) {
			final String contextId = (String) contextIdItr.next();
			if (IContextIds.CONTEXT_ID_DIALOG.equals(contextId)) {
				dialog = true;
				continue;
			}
			if (IContextIds.CONTEXT_ID_WINDOW.equals(contextId)) {
				window = true;
				continue;
			}
		}

		/*
		 * Remove all context identifiers for contexts whose parents are dialog
		 * or window, and the corresponding dialog or window context is not
		 * active.
		 */
		contextIdItr = contextIds.iterator();
		while (contextIdItr.hasNext()) {
			String contextId = (String) contextIdItr.next();
			Context context = contextManager.getContext(contextId);
			try {
				String parentId = context.getParentId();
				while (parentId != null) {
					if (IContextIds.CONTEXT_ID_DIALOG.equals(parentId)) {
						if (!dialog) {
							contextIdItr.remove();
						}
						break;
					}
					if (IContextIds.CONTEXT_ID_WINDOW.equals(parentId)) {
						if (!window) {
							contextIdItr.remove();
						}
						break;
					}
					if (IContextIds.CONTEXT_ID_DIALOG_AND_WINDOW
							.equals(parentId)) {
						if ((!window) && (!dialog)) {
							contextIdItr.remove();
						}
						break;
					}

					context = contextManager.getContext(parentId);
					parentId = context.getParentId();
				}
			} catch (NotDefinedException e) {
				// since this context was part of an undefined hierarchy,
				// I'm going to yank it out as a bad bet
				contextIdItr.remove();

				// This is a logging optimization, only log the error once.
				if (context == null || !bindingErrors.contains(context.getId())) {
					if (context != null) {
						bindingErrors.add(context.getId());
					}

					// now log like you've never logged before!
					Policy
							.getLog()
							.log(
									new Status(
											IStatus.ERROR,
											Policy.JFACE,
											IStatus.OK,
											"Undefined context while filtering dialog/window contexts", //$NON-NLS-1$
											e));
				}
			}
		}

		return createContextTreeFor(contextIds);
	}

	/**
	 * <p>
	 * Notifies all of the listeners to this manager that the defined or active
	 * schemes of bindings have changed.
	 * </p>
	 * <p>
	 * The time this method takes to complete is dependent on external
	 * listeners.
	 * </p>
	 *
	 * @param event
	 *            The event to send to all of the listeners; must not be
	 *            <code>null</code>.
	 */
	private final void fireBindingManagerChanged(final BindingManagerEvent event) {
		if (event == null) {
			throw new NullPointerException();
		}

		final Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IBindingManagerListener listener = (IBindingManagerListener) listeners[i];
			listener.bindingManagerChanged(event);
		}
	}

	/**
	 * <p>
	 * Returns the active bindings. The caller must not modify the returned map.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the active bindings are
	 * not yet computed, then this completes in <code>O(nn)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @return The map of triggers (<code>TriggerSequence</code>) to
	 *         bindings (<code>Binding</code>) which are currently active.
	 *         This value may be <code>null</code> if there are no active
	 *         bindings, and it may be empty.
	 */
	private final Map getActiveBindings() {
		if (activeBindings == null) {
			recomputeBindings();
		}

		return activeBindings;
	}

	/**
	 * <p>
	 * Returns the active bindings indexed by command identifier. The caller
	 * must not modify the returned map.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the active bindings are
	 * not yet computed, then this completes in <code>O(nn)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @return The map of fully-parameterized commands (<code>ParameterizedCommand</code>)
	 *         to triggers (<code>TriggerSequence</code>) which are
	 *         currently active. This value may be <code>null</code> if there
	 *         are no active bindings, and it may be empty.
	 */
	private final Map getActiveBindingsByParameterizedCommand() {
		if (activeBindingsByParameterizedCommand == null) {
			recomputeBindings();
		}

		return activeBindingsByParameterizedCommand;
	}

	/**
	 * <p>
	 * Computes the bindings for the current state of the application, but
	 * disregarding the current contexts. This can be useful when trying to
	 * display all the possible bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @return A map of trigger (<code>TriggerSequence</code>) to bindings (
	 *         <code>Collection</code> containing <code>Binding</code>).
	 *         This map may be empty, but it is never <code>null</code>.
	 */
	public final Map getActiveBindingsDisregardingContext() {
		if (bindings == null) {
			// Not yet initialized. This is happening too early. Do nothing.
			return Collections.EMPTY_MAP;
		}

		// Build a cached binding set for that state.
		final CachedBindingSet bindingCache = new CachedBindingSet(null,
				locales, platforms, activeSchemeIds);

		/*
		 * Check if the cached binding set already exists. If so, simply set the
		 * active bindings and return.
		 */
		CachedBindingSet existingCache = (CachedBindingSet) cachedBindings
				.get(bindingCache);
		if (existingCache == null) {
			existingCache = bindingCache;
			cachedBindings.put(existingCache, existingCache);
		}
		Map commandIdsByTrigger = existingCache.getBindingsByTrigger();
		if (commandIdsByTrigger != null) {
			if (DEBUG) {
				Tracing.printTrace("BINDINGS", "Cache hit"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			return Collections.unmodifiableMap(commandIdsByTrigger);
		}

		// There is no cached entry for this.
		if (DEBUG) {
			Tracing.printTrace("BINDINGS", "Cache miss"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Compute the active bindings.
		commandIdsByTrigger = new HashMap();
		final Map triggersByParameterizedCommand = new HashMap();
		final Map conflictsByTrigger = new HashMap();
		computeBindings(null, commandIdsByTrigger,
				triggersByParameterizedCommand, conflictsByTrigger);
		existingCache.setBindingsByTrigger(commandIdsByTrigger);
		existingCache.setTriggersByCommandId(triggersByParameterizedCommand);
		existingCache.setConflictsByTrigger(conflictsByTrigger);
		return Collections.unmodifiableMap(commandIdsByTrigger);
	}

	/**
	 * <p>
	 * Computes the bindings for the current state of the application, but
	 * disregarding the current contexts. This can be useful when trying to
	 * display all the possible bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @return A map of trigger (<code>TriggerSequence</code>) to bindings (
	 *         <code>Collection</code> containing <code>Binding</code>).
	 *         This map may be empty, but it is never <code>null</code>.
	 */
	private final Map getActiveBindingsDisregardingContextByParameterizedCommand() {
		if (bindings == null) {
			// Not yet initialized. This is happening too early. Do nothing.
			return Collections.EMPTY_MAP;
		}

		// Build a cached binding set for that state.
		final CachedBindingSet bindingCache = new CachedBindingSet(null,
				locales, platforms, activeSchemeIds);

		/*
		 * Check if the cached binding set already exists. If so, simply set the
		 * active bindings and return.
		 */
		CachedBindingSet existingCache = (CachedBindingSet) cachedBindings
				.get(bindingCache);
		if (existingCache == null) {
			existingCache = bindingCache;
			cachedBindings.put(existingCache, existingCache);
		}
		Map triggersByParameterizedCommand = existingCache
				.getTriggersByCommandId();
		if (triggersByParameterizedCommand != null) {
			if (DEBUG) {
				Tracing.printTrace("BINDINGS", "Cache hit"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			return Collections.unmodifiableMap(triggersByParameterizedCommand);
		}

		// There is no cached entry for this.
		if (DEBUG) {
			Tracing.printTrace("BINDINGS", "Cache miss"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Compute the active bindings.
		final Map commandIdsByTrigger = new HashMap();
		final Map conflictsByTrigger = new HashMap();
		triggersByParameterizedCommand = new HashMap();
		computeBindings(null, commandIdsByTrigger,
				triggersByParameterizedCommand, conflictsByTrigger);
		existingCache.setBindingsByTrigger(commandIdsByTrigger);
		existingCache.setTriggersByCommandId(triggersByParameterizedCommand);
		existingCache.setConflictsByTrigger(conflictsByTrigger);

		return Collections.unmodifiableMap(triggersByParameterizedCommand);
	}

	/**
	 * <p>
	 * Computes the bindings for the current state of the application, but
	 * disregarding the current contexts. This can be useful when trying to
	 * display all the possible bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @return All of the active bindings (<code>Binding</code>), not sorted
	 *         in any fashion. This collection may be empty, but it is never
	 *         <code>null</code>.
	 */
	public final Collection getActiveBindingsDisregardingContextFlat() {
		final Collection bindingCollections = getActiveBindingsDisregardingContext()
				.values();
		final Collection mergedBindings = new ArrayList();
		final Iterator bindingCollectionItr = bindingCollections.iterator();
		while (bindingCollectionItr.hasNext()) {
			final Collection bindingCollection = (Collection) bindingCollectionItr
					.next();
			if ((bindingCollection != null) && (!bindingCollection.isEmpty())) {
				mergedBindings.addAll(bindingCollection);
			}
		}

		return mergedBindings;
	}

	/**
	 * <p>
	 * Returns the active bindings for a particular command identifier, but
	 * discounting the current contexts. This method operates in O(n) time over
	 * the number of bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the active bindings are
	 * not yet computed, then this completes in <code>O(nn)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @param parameterizedCommand
	 *            The fully-parameterized command whose bindings are requested.
	 *            This argument may be <code>null</code>.
	 * @return The array of active triggers (<code>TriggerSequence</code>)
	 *         for a particular command identifier. This value is guaranteed to
	 *         never be <code>null</code>, but it may be empty.
	 */
	public final TriggerSequence[] getActiveBindingsDisregardingContextFor(
			final ParameterizedCommand parameterizedCommand) {
		final Object object = getActiveBindingsDisregardingContextByParameterizedCommand()
				.get(parameterizedCommand);
		if (object instanceof Collection) {
			final Collection collection = (Collection) object;
			return (TriggerSequence[]) collection
					.toArray(new TriggerSequence[collection.size()]);
		}

		return EMPTY_TRIGGER_SEQUENCE;
	}

	/**
	 * <p>
	 * Returns the active bindings for a particular command identifier. This
	 * method operates in O(n) time over the number of bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the active bindings are
	 * not yet computed, then this completes in <code>O(nn)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @param parameterizedCommand
	 *            The fully-parameterized command whose bindings are requested.
	 *            This argument may be <code>null</code>.
	 * @return The array of active triggers (<code>TriggerSequence</code>)
	 *         for a particular command identifier. This value is guaranteed to
	 *         never be <code>null</code>, but it may be empty.
	 */
	public final TriggerSequence[] getActiveBindingsFor(
			final ParameterizedCommand parameterizedCommand) {
		final Object object = getActiveBindingsByParameterizedCommand().get(
				parameterizedCommand);
		if (object instanceof Collection) {
			final Collection collection = (Collection) object;
			return (TriggerSequence[]) collection
					.toArray(new TriggerSequence[collection.size()]);
		}

		return EMPTY_TRIGGER_SEQUENCE;
	}

	/**
	 * <p>
	 * Returns the active bindings for a particular command identifier. This
	 * method operates in O(n) time over the number of bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the active bindings are
	 * not yet computed, then this completes in <code>O(nn)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @param commandId
	 *            The identifier of the command whose bindings are requested.
	 *            This argument may be <code>null</code>. It is assumed that
	 *            the command has no parameters.
	 * @return The array of active triggers (<code>TriggerSequence</code>)
	 *         for a particular command identifier. This value is guaranteed not
	 *         to be <code>null</code>, but it may be empty.
	 */
	public final TriggerSequence[] getActiveBindingsFor(final String commandId) {
		final ParameterizedCommand parameterizedCommand = new ParameterizedCommand(
				commandManager.getCommand(commandId), null);
		return getActiveBindingsFor(parameterizedCommand);
	}

	/**
	 * A variation on {@link BindingManager#getActiveBindingsFor(String)} that
	 * returns an array of bindings, rather than trigger sequences. This method
	 * is needed for doing "best" calculations on the active bindings.
	 *
	 * @param commandId
	 *            The identifier of the command for which the active bindings
	 *            should be retrieved; must not be <code>null</code>.
	 * @return The active bindings for the given command; this value may be
	 *         <code>null</code> if there are no active bindings.
	 */
	private final Binding[] getActiveBindingsFor1(final ParameterizedCommand command) {
		final TriggerSequence[] triggers = getActiveBindingsFor(command);
		if (triggers.length == 0) {
			return null;
		}

		final Map activeBindings = getActiveBindings();
		if (activeBindings != null) {
			final Binding[] bindings = new Binding[triggers.length];
			for (int i = 0; i < triggers.length; i++) {
				final TriggerSequence triggerSequence = triggers[i];
				final Object object = activeBindings.get(triggerSequence);
				final Binding binding = (Binding) object;
				bindings[i] = binding;
			}
			return bindings;
		}

		return null;
	}

	/**
	 * <p>
	 * Gets the currently active scheme.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 *
	 * @return The active scheme; may be <code>null</code> if there is no
	 *         active scheme. If a scheme is returned, it is guaranteed to be
	 *         defined.
	 */
	public final Scheme getActiveScheme() {
		return activeScheme;
	}

	/**
	 * Gets the best active binding for a command. The best binding is the one
	 * that would be most appropriate to show in a menu. Bindings which belong
	 * to a child scheme are given preference over those in a parent scheme.
	 * Bindings which belong to a particular locale or platform are given
	 * preference over those that do not. The rest of the calculaton is based
	 * most on various concepts of "length", as well as giving some modifier
	 * keys preference (e.g., <code>Alt</code> is less likely to appear than
	 * <code>Ctrl</code>).
	 *
	 * @param commandId
	 *            The identifier of the command for which the best active
	 *            binding should be retrieved; must not be <code>null</code>.
	 * @return The trigger sequence for the best binding; may be
	 *         <code>null</code> if no bindings are active for the given
	 *         command.
	 */
	public final TriggerSequence getBestActiveBindingFor(final String commandId) {
		return getBestActiveBindingFor(new ParameterizedCommand(commandManager.getCommand(commandId), null));
	}

	/**
	 * @param command
	 * @return
	 * 		a trigger sequence, or <code>null</code>
	 */
	public final TriggerSequence getBestActiveBindingFor(final ParameterizedCommand command) {
		final Binding[] bindings = getActiveBindingsFor1(command);
		if ((bindings == null) || (bindings.length == 0)) {
			return null;
		}

		Binding bestBinding = bindings[0];
		int compareTo;
		for (int i = 1; i < bindings.length; i++) {
			final Binding currentBinding = bindings[i];

			// Bindings in a child scheme are always given preference.
			final String bestSchemeId = bestBinding.getSchemeId();
			final String currentSchemeId = currentBinding.getSchemeId();
			compareTo = compareSchemes(bestSchemeId, currentSchemeId);
			if (compareTo > 0) {
				bestBinding = currentBinding;
			}
			if (compareTo != 0) {
				continue;
			}

			/*
			 * Bindings with a locale are given preference over those that do
			 * not.
			 */
			final String bestLocale = bestBinding.getLocale();
			final String currentLocale = currentBinding.getLocale();
			if ((bestLocale == null) && (currentLocale != null)) {
				bestBinding = currentBinding;
			}
			if (!(Util.equals(bestLocale, currentLocale))) {
				continue;
			}

			/*
			 * Bindings with a platform are given preference over those that do
			 * not.
			 */
			final String bestPlatform = bestBinding.getPlatform();
			final String currentPlatform = currentBinding.getPlatform();
			if ((bestPlatform == null) && (currentPlatform != null)) {
				bestBinding = currentBinding;
			}
			if (!(Util.equals(bestPlatform, currentPlatform))) {
				continue;
			}

			/*
			 * Check to see which has the least number of triggers in the
			 * trigger sequence.
			 */
			final TriggerSequence bestTriggerSequence = bestBinding
					.getTriggerSequence();
			final TriggerSequence currentTriggerSequence = currentBinding
					.getTriggerSequence();
			final Trigger[] bestTriggers = bestTriggerSequence.getTriggers();
			final Trigger[] currentTriggers = currentTriggerSequence
					.getTriggers();
			compareTo = bestTriggers.length - currentTriggers.length;
			if (compareTo > 0) {
				bestBinding = currentBinding;
			}
			if (compareTo != 0) {
				continue;
			}

			/*
			 * Compare the number of keys pressed in each trigger sequence. Some
			 * types of keys count less than others (i.e., some types of
			 * modifiers keys are less likely to be chosen).
			 */
			compareTo = countStrokes(bestTriggers)
					- countStrokes(currentTriggers);
			if (compareTo > 0) {
				bestBinding = currentBinding;
			}
			if (compareTo != 0) {
				continue;
			}

			// If this is still a tie, then just chose the shortest text.
			compareTo = bestTriggerSequence.format().length()
					- currentTriggerSequence.format().length();
			if (compareTo > 0) {
				bestBinding = currentBinding;
			}
		}

		return bestBinding.getTriggerSequence();
	}

	/**
	 * Gets the formatted string representing the best active binding for a
	 * command. The best binding is the one that would be most appropriate to
	 * show in a menu. Bindings which belong to a child scheme are given
	 * preference over those in a parent scheme. The rest of the calculaton is
	 * based most on various concepts of "length", as well as giving some
	 * modifier keys preference (e.g., <code>Alt</code> is less likely to
	 * appear than <code>Ctrl</code>).
	 *
	 * @param commandId
	 *            The identifier of the command for which the best active
	 *            binding should be retrieved; must not be <code>null</code>.
	 * @return The formatted string for the best binding; may be
	 *         <code>null</code> if no bindings are active for the given
	 *         command.
	 */
	public final String getBestActiveBindingFormattedFor(final String commandId) {
		final TriggerSequence binding = getBestActiveBindingFor(commandId);
		if (binding != null) {
			return binding.format();
		}

		return null;
	}
	/**
	 * <p>
	 * Returns the set of all bindings managed by this class.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 *
	 * @return The array of all bindings. This value may be <code>null</code>
	 *         and it may be empty.
	 */
	public final Binding[] getBindings() {
		if (bindings == null) {
			return null;
		}

		final Binding[] returnValue = new Binding[bindingCount];
		System.arraycopy(bindings, 0, returnValue, 0, bindingCount);
		return returnValue;
	}

	/**
	 * <p>
	 * Returns the array of schemes that are defined.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 *
	 * @return The array of defined schemes; this value may be empty or
	 *         <code>null</code>.
	 */
	public final Scheme[] getDefinedSchemes() {
		return (Scheme[]) definedHandleObjects
				.toArray(new Scheme[definedHandleObjects.size()]);
	}

	/**
	 * <p>
	 * Returns the active locale for this binding manager. The locale is in the
	 * same format as <code>Locale.getDefault().toString()</code>.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 *
	 * @return The active locale; never <code>null</code>.
	 */
	public final String getLocale() {
		return locale;
	}

	/**
	 * <p>
	 * Returns all of the possible bindings that start with the given trigger
	 * (but are not equal to the given trigger).
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the bindings aren't
	 * currently computed, then this completes in <code>O(n)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @param trigger
	 *            The prefix to look for; must not be <code>null</code>.
	 * @return A map of triggers (<code>TriggerSequence</code>) to bindings (<code>Binding</code>).
	 *         This map may be empty, but it is never <code>null</code>.
	 */
	public final Map getPartialMatches(final TriggerSequence trigger) {
		final Map partialMatches = (Map) getPrefixTable().get(trigger);
		if (partialMatches == null) {
			return Collections.EMPTY_MAP;
		}

		return partialMatches;
	}

	/**
	 * <p>
	 * Returns the command identifier for the active binding matching this
	 * trigger, if any.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the bindings aren't
	 * currently computed, then this completes in <code>O(n)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @param trigger
	 *            The trigger to match; may be <code>null</code>.
	 * @return The binding that matches, if any; <code>null</code> otherwise.
	 */
	public final Binding getPerfectMatch(final TriggerSequence trigger) {
		return (Binding) getActiveBindings().get(trigger);
	}

	/**
	 * <p>
	 * Returns the active platform for this binding manager. The platform is in
	 * the same format as <code>SWT.getPlatform()</code>.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 *
	 * @return The active platform; never <code>null</code>.
	 */
	public final String getPlatform() {
		return platform;
	}

	/**
	 * <p>
	 * Returns the prefix table. The caller must not modify the returned map.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the active bindings are
	 * not yet computed, then this completes in <code>O(n)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @return A map of prefixes (<code>TriggerSequence</code>) to a map of
	 *         available completions (possibly <code>null</code>, which means
	 *         there is an exact match). The available completions is a map of
	 *         trigger (<code>TriggerSequence</code>) to binding (<code>Binding</code>).
	 *         This value will never be <code>null</code> but may be empty.
	 */
	private final Map getPrefixTable() {
		if (prefixTable == null) {
			recomputeBindings();
		}

		return prefixTable;
	}

	/**
	 * <p>
	 * Gets the scheme with the given identifier. If the scheme does not already
	 * exist, then a new (undefined) scheme is created with that identifier.
	 * This guarantees that schemes will remain unique.
	 * </p>
	 * <p>
	 * This method completes in amortized <code>O(1)</code>.
	 * </p>
	 *
	 * @param schemeId
	 *            The identifier for the scheme to retrieve; must not be
	 *            <code>null</code>.
	 * @return A scheme with the given identifier.
	 */
	public final Scheme getScheme(final String schemeId) {
		checkId(schemeId);

		Scheme scheme = (Scheme) handleObjectsById.get(schemeId);
		if (scheme == null) {
			scheme = new Scheme(schemeId);
			handleObjectsById.put(schemeId, scheme);
			scheme.addSchemeListener(this);
		}

		return scheme;
	}

	/**
	 * <p>
	 * Ascends all of the parents of the scheme until no more parents are found.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the height of the context tree.
	 * </p>
	 *
	 * @param schemeId
	 *            The id of the scheme for which the parents should be found;
	 *            may be <code>null</code>.
	 * @return The array of scheme ids (<code>String</code>) starting with
	 *         <code>schemeId</code> and then ascending through its ancestors.
	 */
	private final String[] getSchemeIds(String schemeId) {
		final List strings = new ArrayList();
		while (schemeId != null) {
			strings.add(schemeId);
			try {
				schemeId = getScheme(schemeId).getParentId();
			} catch (final NotDefinedException e) {
				Policy.getLog().log(
						new Status(IStatus.ERROR, Policy.JFACE, IStatus.OK,
								"Failed ascending scheme parents", //$NON-NLS-1$
								e));
				return new String[0];
			}
		}

		return (String[]) strings.toArray(new String[strings.size()]);
	}

	/**
	 * <p>
	 * Returns whether the given trigger sequence is a partial match for the
	 * given sequence.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the bindings aren't
	 * currently computed, then this completes in <code>O(n)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @param trigger
	 *            The sequence which should be the prefix for some binding;
	 *            should not be <code>null</code>.
	 * @return <code>true</code> if the trigger can be found in the active
	 *         bindings; <code>false</code> otherwise.
	 */
	public final boolean isPartialMatch(final TriggerSequence trigger) {
		return (getPrefixTable().get(trigger) != null);
	}

	/**
	 * <p>
	 * Returns whether the given trigger sequence is a perfect match for the
	 * given sequence.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>. If the bindings aren't
	 * currently computed, then this completes in <code>O(n)</code>, where
	 * <code>n</code> is the number of bindings.
	 * </p>
	 *
	 * @param trigger
	 *            The sequence which should match exactly; should not be
	 *            <code>null</code>.
	 * @return <code>true</code> if the trigger can be found in the active
	 *         bindings; <code>false</code> otherwise.
	 */
	public final boolean isPerfectMatch(final TriggerSequence trigger) {
		return getActiveBindings().containsKey(trigger);
	}

	/**
	 * <p>
	 * Tests whether the locale for the binding matches one of the active
	 * locales.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of active locales.
	 * </p>
	 *
	 * @param binding
	 *            The binding with which to test; must not be <code>null</code>.
	 * @return <code>true</code> if the binding's locale matches;
	 *         <code>false</code> otherwise.
	 */
	private final boolean localeMatches(final Binding binding) {
		boolean matches = false;

		final String locale = binding.getLocale();
		if (locale == null) {
			return true; // shortcut a common case
		}

		for (int i = 0; i < locales.length; i++) {
			if (Util.equals(locales[i], locale)) {
				matches = true;
				break;
			}
		}

		return matches;
	}

	/**
	 * <p>
	 * Tests whether the platform for the binding matches one of the active
	 * platforms.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of active platforms.
	 * </p>
	 *
	 * @param binding
	 *            The binding with which to test; must not be <code>null</code>.
	 * @return <code>true</code> if the binding's platform matches;
	 *         <code>false</code> otherwise.
	 */
	private final boolean platformMatches(final Binding binding) {
		boolean matches = false;

		final String platform = binding.getPlatform();
		if (platform == null) {
			return true; // shortcut a common case
		}

		for (int i = 0; i < platforms.length; i++) {
			if (Util.equals(platforms[i], platform)) {
				matches = true;
				break;
			}
		}

		return matches;
	}

	/**
	 * <p>
	 * This recomputes the bindings based on changes to the state of the world.
	 * This computation can be triggered by changes to contexts, the active
	 * scheme, the locale, or the platform. This method tries to use the cache
	 * of pre-computed bindings, if possible. When this method completes,
	 * <code>activeBindings</code> will be set to the current set of bindings
	 * and <code>cachedBindings</code> will contain an instance of
	 * <code>CachedBindingSet</code> representing these bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n+pn)</code>, where <code>n</code>
	 * is the number of bindings, and <code>p</code> is the average number of
	 * triggers in a trigger sequence.
	 * </p>
	 */
	private final void recomputeBindings() {
		if (bindings == null) {
			// Not yet initialized. This is happening too early. Do nothing.
			setActiveBindings(Collections.EMPTY_MAP, Collections.EMPTY_MAP,
					Collections.EMPTY_MAP, Collections.EMPTY_MAP);
			return;
		}

		// Figure out the current state.
		final Set activeContextIds = new HashSet(contextManager
				.getActiveContextIds());
		final Map activeContextTree = createFilteredContextTreeFor(activeContextIds);

		// Build a cached binding set for that state.
		final CachedBindingSet bindingCache = new CachedBindingSet(
				activeContextTree, locales, platforms, activeSchemeIds);

		/*
		 * Check if the cached binding set already exists. If so, simply set the
		 * active bindings and return.
		 */
		CachedBindingSet existingCache = (CachedBindingSet) cachedBindings
				.get(bindingCache);
		if (existingCache == null) {
			existingCache = bindingCache;
			cachedBindings.put(existingCache, existingCache);
		}
		Map commandIdsByTrigger = existingCache.getBindingsByTrigger();
		if (commandIdsByTrigger != null) {
			if (DEBUG) {
				Tracing.printTrace("BINDINGS", "Cache hit"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			setActiveBindings(commandIdsByTrigger, existingCache
					.getTriggersByCommandId(), existingCache.getPrefixTable(),
					existingCache.getConflictsByTrigger());
			return;
		}

		// There is no cached entry for this.
		if (DEBUG) {
			Tracing.printTrace("BINDINGS", "Cache miss"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Compute the active bindings.
		commandIdsByTrigger = new HashMap();
		final Map triggersByParameterizedCommand = new HashMap();
		final Map conflictsByTrigger = new HashMap();
		computeBindings(activeContextTree, commandIdsByTrigger,
				triggersByParameterizedCommand, conflictsByTrigger);
		existingCache.setBindingsByTrigger(commandIdsByTrigger);
		existingCache.setTriggersByCommandId(triggersByParameterizedCommand);
		existingCache.setConflictsByTrigger(conflictsByTrigger);
		setActiveBindings(commandIdsByTrigger, triggersByParameterizedCommand,
				buildPrefixTable(commandIdsByTrigger),
				conflictsByTrigger);
		existingCache.setPrefixTable(prefixTable);
	}

	/**
	 * <p>
	 * Remove the specific binding by identity. Does nothing if the binding is
	 * not in the manager.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @param binding
	 *            The binding to be removed; must not be <code>null</code>.
	 */
	public final void removeBinding(final Binding binding) {
		if (bindings == null || bindings.length < 1) {
			return;
		}

		final Binding[] newBindings = new Binding[bindings.length];
		boolean bindingsChanged = false;
		int index = 0;
		for (int i = 0; i < bindingCount; i++) {
			final Binding b = bindings[i];
			if (b == binding) {
				bindingsChanged = true;
			} else {
				newBindings[index++] = b;
			}
		}

		if (bindingsChanged) {
			this.bindings = newBindings;
			bindingCount = index;
			clearCache();
		}
	}

	/**
	 * <p>
	 * Removes a listener from this binding manager.
	 * </p>
	 * <p>
	 * This method completes in amortized <code>O(1)</code>.
	 * </p>
	 *
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removeBindingManagerListener(
			final IBindingManagerListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * <p>
	 * Removes any binding that matches the given values -- regardless of
	 * command identifier.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @param sequence
	 *            The sequence to match; may be <code>null</code>.
	 * @param schemeId
	 *            The scheme id to match; may be <code>null</code>.
	 * @param contextId
	 *            The context id to match; may be <code>null</code>.
	 * @param locale
	 *            The locale to match; may be <code>null</code>.
	 * @param platform
	 *            The platform to match; may be <code>null</code>.
	 * @param windowManager
	 *            The window manager to match; may be <code>null</code>. TODO
	 *            Currently ignored.
	 * @param type
	 *            The type to look for.
	 *
	 */
	public final void removeBindings(final TriggerSequence sequence,
			final String schemeId, final String contextId, final String locale,
			final String platform, final String windowManager, final int type) {
		if ((bindings == null) || (bindingCount < 1)) {
			return;
		}

		final Binding[] newBindings = new Binding[bindings.length];
		boolean bindingsChanged = false;
		int index = 0;
		for (int i = 0; i < bindingCount; i++) {
			final Binding binding = bindings[i];
			boolean equals = true;
			equals &= Util.equals(sequence, binding.getTriggerSequence());
			equals &= Util.equals(schemeId, binding.getSchemeId());
			equals &= Util.equals(contextId, binding.getContextId());
			equals &= Util.equals(locale, binding.getLocale());
			equals &= Util.equals(platform, binding.getPlatform());
			equals &= (type == binding.getType());
			if (equals) {
				bindingsChanged = true;
			} else {
				newBindings[index++] = binding;
			}
		}

		if (bindingsChanged) {
			this.bindings = newBindings;
			bindingCount = index;
			clearCache();
		}
	}

	/**
	 * <p>
	 * Attempts to remove deletion markers from the collection of bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @param bindings
	 *            The bindings from which the deleted items should be removed.
	 *            This array should not be <code>null</code>, but may be
	 *            empty.
	 * @return The array of bindings with the deletions removed; never
	 *         <code>null</code>, but may be empty. Contains only instances
	 *         of <code>Binding</code>.
	 */
	private final Binding[] removeDeletions(final Binding[] bindings) {
		final Map deletions = new HashMap();
		final Binding[] bindingsCopy = new Binding[bindingCount];
		System.arraycopy(bindings, 0, bindingsCopy, 0, bindingCount);
		int deletedCount = 0;

		// Extract the deletions.
		for (int i = 0; i < bindingCount; i++) {
			final Binding binding = bindingsCopy[i];
			if ((binding.getParameterizedCommand() == null)
					&& (localeMatches(binding)) && (platformMatches(binding))) {
				final TriggerSequence sequence = binding.getTriggerSequence();
				final Object currentValue = deletions.get(sequence);
				if (currentValue instanceof Binding) {
					final Collection collection = new ArrayList(2);
					collection.add(currentValue);
					collection.add(binding);
					deletions.put(sequence, collection);
				} else if (currentValue instanceof Collection) {
					final Collection collection = (Collection) currentValue;
					collection.add(binding);
				} else {
					deletions.put(sequence, binding);
				}
				bindingsCopy[i] = null;
				deletedCount++;
			}
		}

		if (DEBUG) {
			Tracing.printTrace("BINDINGS", "There are " + deletions.size() //$NON-NLS-1$ //$NON-NLS-2$
					+ " deletion markers"); //$NON-NLS-1$
		}

		// Remove the deleted items.
		for (int i = 0; i < bindingCount; i++) {
			final Binding binding = bindingsCopy[i];
			if (binding != null) {
				final Object deletion = deletions.get(binding
						.getTriggerSequence());
				if (deletion instanceof Binding) {
					if (((Binding) deletion).deletes(binding)) {
						bindingsCopy[i] = null;
						deletedCount++;
					}

				} else if (deletion instanceof Collection) {
					final Collection collection = (Collection) deletion;
					final Iterator iterator = collection.iterator();
					while (iterator.hasNext()) {
						final Object deletionBinding = iterator.next();
						if (deletionBinding instanceof Binding) {
							if (((Binding) deletionBinding).deletes(binding)) {
								bindingsCopy[i] = null;
								deletedCount++;
								break;
							}
						}
					}

				}
			}
		}

		// Compact the array.
		final Binding[] returnValue = new Binding[bindingCount - deletedCount];
		int index = 0;
		for (int i = 0; i < bindingCount; i++) {
			final Binding binding = bindingsCopy[i];
			if (binding != null) {
				returnValue[index++] = binding;
			}
		}

		return returnValue;
	}

	/**
	 * <p>
	 * Attempts to resolve the conflicts for the given bindings.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @param bindings
	 *            The bindings which all match the same trigger sequence; must
	 *            not be <code>null</code>, and should contain at least two
	 *            items. This collection should only contain instances of
	 *            <code>Binding</code> (i.e., no <code>null</code> values).
	 * @param activeContextTree
	 *            The tree of contexts to be used for all of the comparison. All
	 *            of the keys should be active context identifiers (i.e., never
	 *            <code>null</code>). The values will be their parents (i.e.,
	 *            possibly <code>null</code>). Both keys and values are
	 *            context identifiers (<code>String</code>). This map should
	 *            never be empty, and must never be <code>null</code>.
	 * @return The binding which best matches the current state. If there is a
	 *         tie, then return <code>null</code>.
	 */
	private final Binding resolveConflicts(final Collection bindings,
			final Map activeContextTree) {
		/*
		 * This flag is used to indicate when the bestMatch binding conflicts
		 * with another binding. We keep the best match binding so that we know
		 * if we find a better binding. However, if we don't find a better
		 * binding, then we known to return null.
		 */
		boolean conflict = false;

		final Iterator bindingItr = bindings.iterator();
		Binding bestMatch = (Binding) bindingItr.next();

		/*
		 * Iterate over each binding and compare it with the best match. If a
		 * better match is found, then replace the best match and set the
		 * conflict flag to false. If a conflict is found, then leave the best
		 * match and set the conflict flag. Otherwise, just continue.
		 */
		while (bindingItr.hasNext()) {
			final Binding current = (Binding) bindingItr.next();

			/*
			 * SCHEME: Test whether the current is in a child scheme. Bindings
			 * defined in a child scheme will always take priority over bindings
			 * defined in a parent scheme.
			 */
			final String currentSchemeId = current.getSchemeId();
			final String bestSchemeId = bestMatch.getSchemeId();
			final int compareTo = compareSchemes(bestSchemeId, currentSchemeId);
			if (compareTo > 0) {
				bestMatch = current;
				conflict = false;
			}
			if (compareTo != 0) {
				continue;
			}

			/*
			 * CONTEXTS: Check for context superiority. Bindings defined in a
			 * child context will take priority over bindings defined in a
			 * parent context -- assuming that the schemes lead to a conflict.
			 */
			final String currentContext = current.getContextId();
			final String bestContext = bestMatch.getContextId();
			if (!currentContext.equals(bestContext)) {
				boolean goToNextBinding = false;

				// Ascend the current's context tree.
				String contextPointer = currentContext;
				while (contextPointer != null) {
					if (contextPointer.equals(bestContext)) {
						// the current wins
						bestMatch = current;
						conflict = false;
						goToNextBinding = true;
						break;
					}
					contextPointer = (String) activeContextTree
							.get(contextPointer);
				}

				// Ascend the best match's context tree.
				contextPointer = bestContext;
				while (contextPointer != null) {
					if (contextPointer.equals(currentContext)) {
						// the best wins
						goToNextBinding = true;
						break;
					}
					contextPointer = (String) activeContextTree
							.get(contextPointer);
				}

				if (goToNextBinding) {
					continue;
				}
			}

			/*
			 * TYPE: Test for type superiority.
			 */
			if (current.getType() > bestMatch.getType()) {
				bestMatch = current;
				conflict = false;
				continue;
			} else if (bestMatch.getType() > current.getType()) {
				continue;
			}

			// We could not resolve the conflict between these two.
			conflict = true;
		}

		// If the best match represents a conflict, then return null.
		if (conflict) {
			return null;
		}

		// Otherwise, we have a winner....
		return bestMatch;
	}

	/**
	 * <p>
	 * Notifies this manager that a scheme has changed. This method is intended
	 * for internal use only.
	 * </p>
	 * <p>
	 * This method calls out to listeners, and so the time it takes to complete
	 * is dependent on third-party code.
	 * </p>
	 *
	 * @param schemeEvent
	 *            An event describing the change in the scheme.
	 */
	public final void schemeChanged(final SchemeEvent schemeEvent) {
		if (schemeEvent.isDefinedChanged()) {
			final Scheme scheme = schemeEvent.getScheme();
			final boolean schemeIdAdded = scheme.isDefined();
			boolean activeSchemeChanged = false;
			if (schemeIdAdded) {
				definedHandleObjects.add(scheme);
			} else {
				definedHandleObjects.remove(scheme);

				if (activeScheme == scheme) {
					activeScheme = null;
					activeSchemeIds = null;
					activeSchemeChanged = true;

					// Clear the binding solution.
					clearSolution();
				}
			}

			if (isListenerAttached()) {
				fireBindingManagerChanged(new BindingManagerEvent(this, false,
						null, activeSchemeChanged, scheme, schemeIdAdded,
						false, false));
			}
		}
	}

	/**
	 * Sets the active bindings and the prefix table. This ensures that the two
	 * values change at the same time, and that any listeners are notified
	 * appropriately.
	 *
	 * @param activeBindings
	 *            This is a map of triggers ( <code>TriggerSequence</code>)
	 *            to bindings (<code>Binding</code>). This value will only
	 *            be <code>null</code> if the active bindings have not yet
	 *            been computed. Otherwise, this value may be empty.
	 * @param activeBindingsByCommandId
	 *            This is a map of fully-parameterized commands (<code>ParameterizedCommand</code>)
	 *            to triggers ( <code>TriggerSequence</code>). This value
	 *            will only be <code>null</code> if the active bindings have
	 *            not yet been computed. Otherwise, this value may be empty.
	 * @param prefixTable
	 *            A map of prefixes (<code>TriggerSequence</code>) to a map
	 *            of available completions (possibly <code>null</code>, which
	 *            means there is an exact match). The available completions is a
	 *            map of trigger (<code>TriggerSequence</code>) to binding (<code>Binding</code>).
	 *            This value may be <code>null</code> if there is no existing
	 *            solution.
	 */
	private final void setActiveBindings(final Map activeBindings,
			final Map activeBindingsByCommandId, final Map prefixTable,
			final Map conflicts) {
		this.activeBindings = activeBindings;
		final Map previousBindingsByParameterizedCommand = this.activeBindingsByParameterizedCommand;
		this.activeBindingsByParameterizedCommand = activeBindingsByCommandId;
		this.prefixTable = prefixTable;
		currentConflicts = conflicts;

		fireBindingManagerChanged(new BindingManagerEvent(this, true,
				previousBindingsByParameterizedCommand, false, null, false,
				false, false));

		// RAP [if] Update client key binding list
		updateKeyBindingList();
		// ENDRAP [if]
	}

	// RAP [if] Update client key binding list
	private void updateKeyBindingList() {
	  if( activeBindings != null ) {
	    List keyBindingList = new ArrayList();
	    final Iterator bindingItr =  activeBindings.entrySet().iterator();
	    while( bindingItr.hasNext() ) {
	      final Map.Entry entry = ( Map.Entry )bindingItr.next();
	      Binding binding = ( Binding )entry.getValue();
	      if( binding instanceof KeyBinding ) {
	        KeyBinding keyBinding = ( KeyBinding )binding;
	        KeySequence keySequence = keyBinding.getKeySequence();
	        KeyStroke[] keyStroke = keySequence.getKeyStrokes();
	        if( keyStroke.length > 0 ) {
	          keyBindingList.add( keyStroke[ 0 ].toString() );
	        }
	      }
	    }
	    String[] array = new String[ keyBindingList.size() ];
        keyBindingList.toArray( array );
        Display display = Display.getCurrent();
        if( display == null ) {
          throw new IllegalStateException();
        }
        display.setData( RWT.ACTIVE_KEYS, array );
        display.setData( RWT.CANCEL_KEYS, array );
	  }
	}
	// ENDRAP [if]

  /**
	 * Provides the current conflicts in the bindings as a Map The key will
	 * be {@link TriggerSequence} and the value will be the {@link Collection} of
	 * {@link Binding}
	 *
	 * @return Read-only {@link Map} of the current conflicts. If no conflicts,
	 *         then return an empty map. Never <code>null</code>
	 */
	public Map getCurrentConflicts() {
		if (currentConflicts == null)
			return Collections.EMPTY_MAP;
		return Collections.unmodifiableMap(currentConflicts);
	}

	/**
	 * Provides the current conflicts in the keybindings for the given
	 * TriggerSequence as a {@link Collection} of {@link Binding}
	 *
	 * @param sequence The sequence for which conflict info is required
	 *
	 * @return Collection of KeyBinding. If no conflicts,
	 *         then returns a <code>null</code>
	 */
	public Collection getConflictsFor(TriggerSequence sequence) {
		return (Collection) getCurrentConflicts().get(sequence);
	}


	/**
	 * <p>
	 * Selects one of the schemes as the active scheme. This scheme must be
	 * defined.
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the height of the context tree.
	 * </p>
	 *
	 * @param scheme
	 *            The scheme to become active; must not be <code>null</code>.
	 * @throws NotDefinedException
	 *             If the given scheme is currently undefined.
	 */
	public final void setActiveScheme(final Scheme scheme)
			throws NotDefinedException {
		if (scheme == null) {
			throw new NullPointerException("Cannot activate a null scheme"); //$NON-NLS-1$
		}

		if ((scheme == null) || (!scheme.isDefined())) {
			throw new NotDefinedException(
					"Cannot activate an undefined scheme. " //$NON-NLS-1$
							+ scheme.getId());
		}

		if (Util.equals(activeScheme, scheme)) {
			return;
		}

		activeScheme = scheme;
		activeSchemeIds = getSchemeIds(activeScheme.getId());
		clearSolution();
		fireBindingManagerChanged(new BindingManagerEvent(this, false, null,
				true, null, false, false, false));
	}

	/**
	 * <p>
	 * Changes the set of bindings for this binding manager. Changing the set of
	 * bindings all at once ensures that: (1) duplicates are removed; and (2)
	 * avoids unnecessary intermediate computations. This method clears the
	 * existing bindings, but does not trigger a recomputation (other method
	 * calls are required to do that).
	 * </p>
	 * <p>
	 * This method completes in <code>O(n)</code>, where <code>n</code> is
	 * the number of bindings.
	 * </p>
	 *
	 * @param bindings
	 *            The new array of bindings; may be <code>null</code>. This
	 *            set is copied into a local data structure.
	 */
	public final void setBindings(Binding[] bindings) {
		if (bindings != null) {
			// discard bindings not applicable for this platform
			List newList = new ArrayList();
			for (int i = 0; i < bindings.length; i++) {
				Binding binding = bindings[i];
				String p = binding.getPlatform();
				if (p == null) {
					newList.add(binding);
				} else if (p.equals(platform)) {
					newList.add(binding);
				}
			}
			bindings = (Binding[]) newList.toArray(new Binding[newList.size()]);
		}
		//Check for equality after the munge
		if (Arrays.equals(this.bindings, bindings)) {
			return; // nothing has changed
		}

		if ((bindings == null) || (bindings.length == 0)) {
			this.bindings = null;
			bindingCount = 0;
		} else {
			this.bindings = bindings;
			bindingCount = bindings.length;
		}
		clearCache();
	}

	/**
	 * <p>
	 * Changes the locale for this binding manager. The locale can be used to
	 * provide locale-specific bindings. If the locale is different than the
	 * current locale, this will force a recomputation of the bindings. The
	 * locale is in the same format as
	 * <code>Locale.getDefault().toString()</code>.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 *
	 * @param locale
	 *            The new locale; must not be <code>null</code>.
	 * @see Locale#getDefault()
	 */
	public final void setLocale(final String locale) {
		if (locale == null) {
			throw new NullPointerException("The locale cannot be null"); //$NON-NLS-1$
		}

		if (!Util.equals(this.locale, locale)) {
			this.locale = locale;
			this.locales = expand(locale, LOCALE_SEPARATOR);
			clearSolution();
			fireBindingManagerChanged(new BindingManagerEvent(this, false,
					null, false, null, false, true, false));
		}
	}

	/**
	 * <p>
	 * Changes the platform for this binding manager. The platform can be used
	 * to provide platform-specific bindings. If the platform is different than
	 * the current platform, then this will force a recomputation of the
	 * bindings. The locale is in the same format as
	 * <code>SWT.getPlatform()</code>.
	 * </p>
	 * <p>
	 * This method completes in <code>O(1)</code>.
	 * </p>
	 *
	 * @param platform
	 *            The new platform; must not be <code>null</code>.
	 * @see org.eclipse.swt.SWT#getPlatform()
	 * @see Util#getWS()
	 */
	public final void setPlatform(final String platform) {
		if (platform == null) {
			throw new NullPointerException("The platform cannot be null"); //$NON-NLS-1$
		}

		if (!Util.equals(this.platform, platform)) {
			this.platform = platform;
			this.platforms = expand(platform, Util.ZERO_LENGTH_STRING);
			clearSolution();
			fireBindingManagerChanged(new BindingManagerEvent(this, false,
					null, false, null, false, false, true));
		}
	}
}
