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

package org.eclipse.jface.bindings.keys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * A wrapper around the SWT text widget that traps literal key presses and
 * converts them into key sequences for display. There are two types of key
 * strokes that are displayed: complete and incomplete. A complete key stroke is
 * one with a natural key, while an incomplete one has no natural key.
 * Incomplete key strokes are only displayed until they are made complete or
 * their component key presses are released.
 * </p>
 * 
 * @since 1.4
 */
public final class KeySequenceText {

	/**
	 * A key listener that traps incoming events and displays them in the
	 * wrapped text field. It has no effect on traversal operations.
	 */
	private class KeyTrapListener implements Listener {
		/**
		 * The index at which insertion should occur. This is used if there is a
		 * replacement occurring in the middle of the stroke, and the first key
		 * stroke was incomplete.
		 */
		private int insertionIndex = -1;

		/**
		 * Resets the insertion index to point nowhere. In other words, it is
		 * set to <code>-1</code>.
		 */
		void clearInsertionIndex() {
			insertionIndex = -1;
		}

		/**
		 * Deletes the current selection. If there is no selection, then it
		 * deletes the last key stroke.
		 * 
		 * @param keyStrokes
		 *            The key strokes from which to delete. This list must not
		 *            be <code>null</code>, and must represent a valid key
		 *            sequence.
		 * @return An array of keystrokes minus the keystrokes that were
		 *         deleted.
		 */
		private final KeyStroke[] deleteKeyStroke(final KeyStroke[] keyStrokes) {
			clearInsertionIndex();

			if (hasSelection()) {
				/*
				 * Delete the current selection -- disallowing incomplete
				 * strokes in the middle of the sequence.
				 */
				final KeyStroke[][] deletedKeyStrokes = new KeyStroke[1][];
				deleteSelection(keyStrokes, false, deletedKeyStrokes);
				return deletedKeyStrokes[0];
			}

			// Remove the last key stroke.
			if (keyStrokes.length > 0) {
				final int newKeyStrokesLength = keyStrokes.length - 1;
				final KeyStroke[] newKeyStrokes = new KeyStroke[newKeyStrokesLength];
				System.arraycopy(keyStrokes, 0, newKeyStrokes, 0,
						newKeyStrokesLength);
				return newKeyStrokes;
			}

			return keyStrokes;
		}

		/**
		 * Handles the key pressed and released events on the wrapped text
		 * widget. This makes sure to either add the pressed key to the
		 * temporary key stroke, or complete the current temporary key stroke
		 * and prompt for the next. In the case of a key release, this makes
		 * sure that the temporary stroke is correctly displayed --
		 * corresponding with modifier keys that may have been released.
		 * 
		 * @param event
		 *            The triggering event; must not be <code>null</code>.
		 */
		public void handleEvent(Event event) {
			KeyStroke[] keyStrokes = getKeySequence().getKeyStrokes();

			// Dispatch the event to the correct handler.
			if (event.type == SWT.KeyDown) {
				keyStrokes = handleKeyDown(event, keyStrokes);
			} else if (event.type == SWT.KeyUp) {
				keyStrokes = handleKeyUp(event, keyStrokes);
			}

			// Update the underlying widget.
			setKeySequence(KeySequence.getInstance(keyStrokes));

			// Prevent the event from reaching the widget.
			event.doit = false;
		}

		/**
		 * Handles the case where the key event is an <code>SWT.KeyDown</code>
		 * event. This either causes a deletion (if it is an unmodified
		 * backspace key stroke), or an insertion (if it is any other key).
		 * 
		 * @param event
		 *            The trigger key down event; must not be <code>null</code>.
		 * @param keyStrokes
		 *            The current list of key strokes. This valud must not be
		 *            <code>null</code>, and it must represent a valid key
		 *            sequence.
		 */
		private KeyStroke[] handleKeyDown(Event event, KeyStroke[] keyStrokes) {
			// Is it an unmodified backspace character?
			if ((event.character == SWT.BS || event.character == SWT.DEL) && (event.stateMask == 0)) {
				return deleteKeyStroke(keyStrokes);
			}

			return insertKeyStroke(event, keyStrokes);
		}

		/**
		 * Handles the case where the key event is an <code>SWT.KeyUp</code>
		 * event. This resets the insertion index. If there is an incomplete
		 * stroke, then that incomplete stroke is modified to match the keys
		 * that are still held. If no keys are held, then the incomplete stroke
		 * is removed.
		 * 
		 * @param event
		 *            The triggering event; must not be <code>null</code>
		 * @param keyStrokes
		 *            The key strokes that are part of the current key sequence;
		 *            these key strokes are guaranteed to represent a valid key
		 *            sequence. This value must not be <code>null</code>.
		 */
		private final KeyStroke[] handleKeyUp(final Event event,
				final KeyStroke[] keyStrokes) {
			if (hasIncompleteStroke()) {
				/*
				 * Figure out the SWT integer representation of the remaining
				 * values.
				 */
				Event mockEvent = new Event();
				if ((event.keyCode & SWT.MODIFIER_MASK) != 0) {
					// This key up is a modifier key being released.
					mockEvent.stateMask = event.stateMask - event.keyCode;
				} else {
					/*
					 * This key up is the other end of a key down that was
					 * trapped by the operating system or window manager.
					 */
					mockEvent.stateMask = event.stateMask;
				}

				/*
				 * Get a reasonable facsimile of the stroke that is still
				 * pressed.
				 */
				int key = SWTKeySupport
						.convertEventToUnmodifiedAccelerator(mockEvent);
				KeyStroke remainingStroke = SWTKeySupport
						.convertAcceleratorToKeyStroke(key);
				final int keyStrokesLength = keyStrokes.length;
				final KeyStroke[] newKeyStrokes;
				if ((keyStrokesLength > 0)
						&& (remainingStroke.getModifierKeys() != 0)) {
					newKeyStrokes = new KeyStroke[keyStrokesLength];
					System.arraycopy(keyStrokes, 0, newKeyStrokes, 0,
							keyStrokesLength - 1);
					newKeyStrokes[keyStrokesLength - 1] = remainingStroke;

				} else if (keyStrokesLength > 0) {
					newKeyStrokes = new KeyStroke[keyStrokesLength - 1];
					System.arraycopy(keyStrokes, 0, newKeyStrokes, 0,
							keyStrokesLength - 1);

				} else if (remainingStroke.getModifierKeys() != 0) {
					newKeyStrokes = new KeyStroke[keyStrokesLength + 1];
					System.arraycopy(keyStrokes, 0, newKeyStrokes, 0,
							keyStrokesLength);
					newKeyStrokes[keyStrokesLength] = remainingStroke;

				} else {
					newKeyStrokes = keyStrokes;

				}

				return newKeyStrokes;
			}

			return keyStrokes;
		}

		/**
		 * <p>
		 * Handles the case where a key down event is leading to a key stroke
		 * being inserted. The current selection is deleted, and an invalid
		 * remanents of the stroke are also removed. The insertion is carried
		 * out at the cursor position.
		 * </p>
		 * <p>
		 * If only a natural key is selected (as part of a larger key stroke),
		 * then it is possible for the user to press a natural key to replace
		 * the old natural key. In this situation, pressing any modifier keys
		 * will replace the whole thing.
		 * </p>
		 * <p>
		 * If the insertion point is not at the end of the sequence, then
		 * incomplete strokes will not be immediately inserted. Only when the
		 * sequence is completed is the stroke inserted. This is a requirement
		 * as the widget must always represent a valid key sequence. The
		 * insertion point is tracked using <code>insertionIndex</code>,
		 * which is an index into the key stroke array.
		 * </p>
		 * 
		 * @param event
		 *            The triggering key down event; must not be
		 *            <code>null</code>.
		 * @param keyStrokes
		 *            The key strokes into which the current stroke should be
		 *            inserted. This value must not be <code>null</code>, and
		 *            must represent a valid key sequence.
		 */
		private final KeyStroke[] insertKeyStroke(final Event event,
				KeyStroke[] keyStrokes) {
			// Compute the key stroke to insert.
			int key = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
			KeyStroke stroke = SWTKeySupport.convertAcceleratorToKeyStroke(key);

			/*
			 * Only insert the stroke if it is *not ScrollLock. Let's not get
			 * silly
			 */
			if ((SWT.NUM_LOCK == stroke.getNaturalKey())
					|| (SWT.CAPS_LOCK == stroke.getNaturalKey())
					|| (SWT.SCROLL_LOCK == stroke.getNaturalKey())) {
				return keyStrokes;
			}

			if (insertionIndex != -1) {
				// There is a previous replacement still going on.
				if (stroke.isComplete()) {
					keyStrokes = insertStrokeAt(keyStrokes, stroke,
							insertionIndex);
					clearInsertionIndex();
				}

			} else if (hasSelection()) {
				// There is a selection that needs to be replaced.
				final KeyStroke[][] deletedKeyStrokes = new KeyStroke[1][];
				insertionIndex = deleteSelection(keyStrokes, stroke
						.isComplete(), deletedKeyStrokes);
				keyStrokes = deletedKeyStrokes[0];
				if ((stroke.isComplete())
						|| (insertionIndex >= keyStrokes.length)) {
					keyStrokes = insertStrokeAt(keyStrokes, stroke,
							insertionIndex);
					clearInsertionIndex();
				}

			} else {
				// No selection, so remove the incomplete stroke, if any
				if ((hasIncompleteStroke()) && (keyStrokes.length > 0)) {
					final KeyStroke[] newKeyStrokes = new KeyStroke[keyStrokes.length - 1];
					System.arraycopy(keyStrokes, 0, newKeyStrokes, 0,
							keyStrokes.length - 1);
					keyStrokes = newKeyStrokes;
				}

				// And then add the new stroke.
				if ((keyStrokes.length == 0)
						|| (insertionIndex >= keyStrokes.length)
						|| (isCursorInLastPosition())) {
					keyStrokes = insertStrokeAt(keyStrokes, stroke,
							keyStrokes.length);
					clearInsertionIndex();
				} else {
					/*
					 * I'm just getting the insertionIndex here. No actual
					 * deletion should occur.
					 */
					final KeyStroke[][] deletedKeyStrokes = new KeyStroke[1][];
					insertionIndex = deleteSelection(keyStrokes, stroke
							.isComplete(), deletedKeyStrokes);
					keyStrokes = deletedKeyStrokes[0];
					if (stroke.isComplete()) {
						keyStrokes = insertStrokeAt(keyStrokes, stroke,
								insertionIndex);
						clearInsertionIndex();
					}
				}

			}

			return keyStrokes;
		}
	}

	/**
	 * A traversal listener that blocks all traversal except for tabs and arrow
	 * keys.
	 */
	private class TraversalFilter implements Listener {
		/**
		 * Handles the traverse event on the text field wrapped by this class.
		 * It swallows all traverse events example for tab and arrow key
		 * navigation. The other forms of navigation can be reached by tabbing
		 * off of the control.
		 * 
		 * @param event
		 *            The trigger event; must not be <code>null</code>.
		 */
		public void handleEvent(Event event) {
			switch (event.detail) {
			case SWT.TRAVERSE_ESCAPE:
// RAP [if] SWT constants missing
//			case SWT.TRAVERSE_MNEMONIC:
			case SWT.TRAVERSE_NONE:
//			case SWT.TRAVERSE_PAGE_NEXT:
//			case SWT.TRAVERSE_PAGE_PREVIOUS:
			case SWT.TRAVERSE_RETURN:
				event.type = SWT.None;
				event.doit = false;
				break;

			case SWT.TRAVERSE_TAB_NEXT:
			case SWT.TRAVERSE_TAB_PREVIOUS:
				// Check if modifiers other than just 'Shift' were
				// down.
				if ((event.stateMask & (SWT.MODIFIER_MASK ^ SWT.SHIFT)) != 0) {
					// Modifiers other than shift were down.
					event.type = SWT.None;
					event.doit = false;
					break;
				}

				//$FALL-THROUGH$ -- either no modifiers, or just shift.
//			case SWT.TRAVERSE_ARROW_NEXT:
//			case SWT.TRAVERSE_ARROW_PREVIOUS:
			default:
				// Let the traversal happen, but clear the incomplete
				// stroke
				if (hasIncompleteStroke()) {
					final KeyStroke[] oldKeyStrokes = getKeySequence()
							.getKeyStrokes();
					final int newKeyStrokesLength = oldKeyStrokes.length - 1;
					if (newKeyStrokesLength >= 1) {
						final KeyStroke[] newKeyStrokes = new KeyStroke[newKeyStrokesLength];
						System.arraycopy(oldKeyStrokes, 0, newKeyStrokes, 0,
								newKeyStrokesLength);
						setKeySequence(KeySequence.getInstance(newKeyStrokes));
					} else {
						setKeySequence(KeySequence.getInstance());
					}
				}
			}

		}
	}

	/**
	 * The manager resposible for installing and removing the traversal filter
	 * when the key sequence entry widget gains and loses focus.
	 */
	private class TraversalFilterManager implements FocusListener {
		/** The managed filter. We only need one instance. */
		private TraversalFilter filter = new TraversalFilter();
		
		private boolean filtering = false;

		/**
		 * Attaches the global traversal filter.
		 * 
		 * @param event
		 *            Ignored.
		 */
		public void focusGained(FocusEvent event) {
			Display.getCurrent().addFilter(SWT.Traverse, filter);
			filtering = true;
		}

		/**
		 * Detaches the global traversal filter.
		 * 
		 * @param event
		 *            Ignored.
		 */
		public void focusLost(FocusEvent event) {
			Display.getCurrent().removeFilter(SWT.Traverse, filter);
			filtering = false;
		}
		
		/**
		 * Remove the traverse filter if we close without focusOut.
		 */
		public void dispose() {
			if (filtering) {
				Display.getCurrent().removeFilter(SWT.Traverse, filter);
			}
		}
	}

	/**
	 * A modification listener that makes sure that external events to this
	 * class (i.e., direct modification of the underlying text) do not break
	 * this class' view of the world.
	 */
	private class UpdateSequenceListener implements ModifyListener {
		/**
		 * Handles the modify event on the underlying text widget.
		 * 
		 * @param event
		 *            The triggering event; ignored.
		 */
		public void modifyText(ModifyEvent event) {
			try {
				// The original sequence.
				KeySequence originalSequence = getKeySequence();

				// The new sequence drawn from the text.
				String contents = getText();
				KeySequence newSequence = KeySequence.getInstance(contents);

				// Check to see if they're the same.
				if (!originalSequence.equals(newSequence)) {
					setKeySequence(newSequence);
				}

			} catch (ParseException e) {
				// Abort any cut/paste-driven modifications
				setKeySequence(getKeySequence());
			}
		}
	}

	static {
		TreeSet trappedKeys = new TreeSet();
		trappedKeys.add(SWTKeySupport.convertAcceleratorToKeyStroke(SWT.TAB));
		trappedKeys.add(SWTKeySupport.convertAcceleratorToKeyStroke(SWT.TAB
				| SWT.SHIFT));
		trappedKeys.add(SWTKeySupport.convertAcceleratorToKeyStroke(SWT.BS));
		List trappedKeyList = new ArrayList(trappedKeys);
		TRAPPED_KEYS = Collections.unmodifiableList(trappedKeyList);
	}

	/** An empty string instance for use in clearing text values. */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * The special integer value for the maximum number of strokes indicating
	 * that an infinite number should be allowed.
	 */
	public static final int INFINITE = -1;

	/**
	 * The name of the property representing the current key sequence in this
	 * key sequence widget.
	 */
	public static final String P_KEY_SEQUENCE = "org.eclipse.jface.bindings.keys.KeySequenceText.KeySequence"; //$NON-NLS-1$

	/**
	 * The keys trapped by this widget. This list is guaranteed to be roughly
	 * accurate. Perfection is not possible, as SWT does not export traversal
	 * keys as constants.
	 */
	public static final List TRAPPED_KEYS;

	/**
	 * The key filter attached to the underlying widget that traps key events.
	 */
	private final KeyTrapListener keyFilter = new KeyTrapListener();

	/**
	 * The text of the key sequence -- containing only the complete key strokes.
	 */
	private KeySequence keySequence = KeySequence.getInstance();

	/**
	 * Those listening to changes to the key sequence in this widget. This value
	 * may be <code>null</code> if there are no listeners.
	 */
	private Collection listeners = null;

	/** The maximum number of key strokes permitted in the sequence. */
	private int maxStrokes = INFINITE;

	/** The text widget that is wrapped for this class. */
	private final Text text;

	/**
	 * The listener that makes sure that the text widget remains up-to-date with
	 * regards to external modification of the text (e.g., cut & pasting).
	 */
	private final UpdateSequenceListener updateSequenceListener = new UpdateSequenceListener();

	/**
	 * Constructs an instance of <code>KeySequenceTextField</code> with the
	 * text field to use. If the platform is carbon (MacOS X), then the font is
	 * set to be the same font used to display accelerators in the menus.
	 * 
	 * @param wrappedText
	 *            The text widget to wrap; must not be <code>null</code>.
	 */
	public KeySequenceText(Text wrappedText) {
		text = wrappedText;

		// Set the font if the platform is carbon.
		if (Util.isMac()) {
			// Don't worry about this font name here; it is the official menu
			// font and point size on the Mac.
			final Font font = new Font(text.getDisplay(),
					"Lucida Grande", 13, SWT.NORMAL); //$NON-NLS-1$
			text.setFont(font);
			text.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					font.dispose();
				}
			});
		}

		// Add the key listener.
		text.addListener(SWT.KeyUp, keyFilter);
		text.addListener(SWT.KeyDown, keyFilter);

		final TraversalFilterManager traversalFilterManager = new TraversalFilterManager();
		text.addFocusListener(traversalFilterManager);
		text.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				traversalFilterManager.dispose();
			} 
		});

		// Add an internal modify listener.
		text.addModifyListener(updateSequenceListener);
	}

	/**
	 * Adds a property change listener to this key sequence widget. It will be
	 * notified when the key sequence changes.
	 * 
	 * @param listener
	 *            The listener to be notified when changes occur; must not be
	 *            <code>null</code>.
	 */
	public final void addPropertyChangeListener(
			final IPropertyChangeListener listener) {
		if (listener == null) {
			return;
		}

		if (listeners == null) {
			listeners = new ArrayList(1);
		}

		listeners.add(listener);
	}

	/**
	 * Clears the text field and resets all the internal values.
	 */
	public void clear() {
		final KeySequence oldKeySequence = keySequence;
		keySequence = KeySequence.getInstance();
		text.setText(EMPTY_STRING);
		firePropertyChangeEvent(oldKeySequence);
	}

	/**
	 * Removes the key strokes from the list corresponding the selection. If
	 * <code>allowIncomplete</code>, then invalid key sequences will be
	 * allowed (i.e., those with incomplete strokes in the non-terminal
	 * position). Otherwise, incomplete strokes will be removed. This modifies
	 * <code>keyStrokes</code> in place, and has no effect on the text widget
	 * this class wraps.
	 * 
	 * @param keyStrokes
	 *            The list of key strokes from which the selection should be
	 *            removed; must not be <code>null</code>.
	 * @param allowIncomplete
	 *            Whether incomplete strokes should be allowed to exist in the
	 *            list after the deletion.
	 * @param deletedKeyStrokes
	 *            The list of keystrokes that were deleted by this operation.
	 *            Declared as final since it will hold a reference to the new
	 *            keyStroke array that has deleted the selected keystrokes.
	 * @return The index at which a subsequent insert should occur. This index
	 *         only has meaning to the <code>insertStrokeAt</code> method.
	 */
	private final int deleteSelection(final KeyStroke[] keyStrokes,
			final boolean allowIncomplete, final KeyStroke[][] deletedKeyStrokes) {
		// Get the current selection.
		Point selection = text.getSelection();
		int start = selection.x;
		int end = selection.y;

		/*
		 * Using the key sequence format method, discover the point at which
		 * adding key strokes passes or equals the start of the selection. In
		 * other words, find the first stroke that is part of the selection.
		 * Keep track of the text range under which the stroke appears (i.e.,
		 * startTextIndex->string.length() is the first selected stroke).
		 */
		String string = new String();
		List currentStrokes = new ArrayList();
		int startTextIndex = 0; // keeps track of the start of the stroke
		final int keyStrokesLength = keyStrokes.length;
		int i;
		for (i = 0; (i < keyStrokesLength) && (string.length() < start); i++) {
			startTextIndex = string.length();
			currentStrokes.add(keyStrokes[i]);
			string = KeySequence.getInstance(currentStrokes).format();
		}

		/*
		 * If string.length() == start, then the cursor is positioned between
		 * strokes (i.e., selection is outside of a stroke).
		 */
		int startStrokeIndex;
		if (string.length() == start) {
			startStrokeIndex = currentStrokes.size();
		} else {
			startStrokeIndex = currentStrokes.size() - 1;
		}

		/*
		 * Check to see if the cursor is only positioned, rather than actually
		 * selecting something. We only need to compute the end if there is a
		 * selection.
		 */
		int endStrokeIndex;
		if (start == end) {
			// return the current keystrokes, nothing has to be deleted
			deletedKeyStrokes[0] = keyStrokes;
			return startStrokeIndex;
		}

		for (; (i < keyStrokesLength) && (string.length() < end); i++) {
			currentStrokes.add(keyStrokes[i]);
			string = KeySequence.getInstance(currentStrokes).format();
		}
		endStrokeIndex = currentStrokes.size() - 1;
		if (endStrokeIndex < 0) {
			endStrokeIndex = 0;
		}

		/*
		 * Remove the strokes that are touched by the selection. Keep track of
		 * the first stroke removed.
		 */
		final int newLength = keyStrokesLength
				- (endStrokeIndex - startStrokeIndex + 1);
		deletedKeyStrokes[0] = new KeyStroke[newLength];
		final KeyStroke startStroke = keyStrokes[startStrokeIndex];
		KeyStroke keyStrokeResult[] = new KeyStroke[newLength];
		System.arraycopy(keyStrokes, 0, keyStrokeResult, 0, startStrokeIndex);
		System.arraycopy(keyStrokes, endStrokeIndex + 1, keyStrokeResult,
				startStrokeIndex, keyStrokesLength - endStrokeIndex - 1);
		System.arraycopy(keyStrokeResult, 0, deletedKeyStrokes[0], 0, newLength);

		/*
		 * Allow the first stroke removed to be replaced by an incomplete
		 * stroke.
		 */
		if (allowIncomplete) {
			final int modifierKeys = startStroke.getModifierKeys();
			KeyStroke incompleteStroke = KeyStroke.getInstance(modifierKeys,
					KeyStroke.NO_KEY);
			int incompleteStrokeLength = incompleteStroke.format().length();
			if ((startTextIndex + incompleteStrokeLength) <= start) {
				final KeyStroke[] added = new KeyStroke[newLength + 1];
				System.arraycopy(deletedKeyStrokes[0], 0, added, 0,
						startStrokeIndex);
				added[startStrokeIndex] = incompleteStroke;
				System.arraycopy(deletedKeyStrokes[0], startStrokeIndex, added,
						startStrokeIndex + 1, newLength - startStrokeIndex);
				deletedKeyStrokes[0] = added;
			}
		}

		return startStrokeIndex;
	}

	/**
	 * Fires a property change event to all of the listeners.
	 * 
	 * @param oldKeySequence
	 *            The old key sequence; must not be <code>null</code>.
	 */
	protected final void firePropertyChangeEvent(
			final KeySequence oldKeySequence) {
		if (listeners != null) {
			final Iterator listenerItr = listeners.iterator();
			final PropertyChangeEvent event = new PropertyChangeEvent(this,
					P_KEY_SEQUENCE, oldKeySequence, getKeySequence());
			while (listenerItr.hasNext()) {
				final IPropertyChangeListener listener = (IPropertyChangeListener) listenerItr
						.next();
				listener.propertyChange(event);
			}
		}
	}

	/**
	 * An accessor for the <code>KeySequence</code> that corresponds to the
	 * current state of the text field. This includes incomplete strokes.
	 * 
	 * @return The key sequence representation; never <code>null</code>.
	 */
	public KeySequence getKeySequence() {
		return keySequence;
	}

	/**
	 * An accessor for the underlying text widget's contents.
	 * 
	 * @return The text contents of this entry; never <code>null</code>.
	 */
	private String getText() {
		return text.getText();
	}

	/**
	 * Tests whether the current key sequence has a stroke with no natural key.
	 * 
	 * @return <code>true</code> is there is an incomplete stroke;
	 *         <code>false</code> otherwise.
	 */
	private boolean hasIncompleteStroke() {
		return !keySequence.isComplete();
	}

	/**
	 * Tests whether the current text widget has some text selection.
	 * 
	 * @return <code>true</code> if the number of selected characters it
	 *         greater than zero; <code>false</code> otherwise.
	 */
	private boolean hasSelection() {
		return (text.getSelectionCount() > 0);
	}

	/**
	 * Inserts the key stroke at the current insertion point. This does a
	 * regular delete and insert, as if the key had been pressed.
	 * 
	 * @param stroke
	 *            The key stroke to insert; must not be <code>null</code>.
	 */
	public void insert(KeyStroke stroke) {
		if (!stroke.isComplete()) {
			return;
		}

		// Copy the key strokes in the current key sequence.
		final KeySequence keySequence = getKeySequence();
		final KeyStroke[] oldKeyStrokes = keySequence.getKeyStrokes();
		final KeyStroke[] newKeyStrokes;
		if ((hasIncompleteStroke()) && (!keySequence.isEmpty())) {
			final int newKeyStrokesLength = oldKeyStrokes.length - 1;
			newKeyStrokes = new KeyStroke[newKeyStrokesLength];
			System.arraycopy(oldKeyStrokes, 0, newKeyStrokes, 0,
					newKeyStrokesLength);
		} else {
			newKeyStrokes = oldKeyStrokes;
		}

		KeyStroke[][] deletedKeyStrokes = new KeyStroke[1][];
		int index = deleteSelection(newKeyStrokes, false, deletedKeyStrokes);
		if (index == -1) {
			index = 0;
		}

		final KeyStroke[] keyStrokes = insertStrokeAt(newKeyStrokes, stroke, index);
		keyFilter.clearInsertionIndex();
		setKeySequence(KeySequence.getInstance(keyStrokes));
	}

	/**
	 * Inserts the stroke at the given index in the list of strokes. If the
	 * stroke currently at that index is incomplete, then it tries to merge the
	 * two strokes. If merging is a complete failure (unlikely), then it will
	 * simply overwrite the incomplete stroke. If the stroke at the index is
	 * complete, then it simply inserts the stroke independently.
	 * 
	 * @param keyStrokes
	 *            The list of key strokes in which the key stroke should be
	 *            appended; must not be <code>null</code>.
	 * @param stroke
	 *            The stroke to insert; should not be <code>null</code>.
	 * @param index
	 *            The index at which to insert; must be a valid index into the
	 *            list of key strokes.
	 */
	private final KeyStroke[] insertStrokeAt(final KeyStroke[] keyStrokes,
			KeyStroke stroke, int index) {
		final int keyStrokesLength = keyStrokes.length;
		final KeyStroke currentStroke = (index >= keyStrokesLength) ? null
				: keyStrokes[index];
		if ((currentStroke != null) && (!currentStroke.isComplete())) {
			int modifierKeys = currentStroke.getModifierKeys();
			final int naturalKey = stroke.getNaturalKey();
			modifierKeys |= stroke.getModifierKeys();
			keyStrokes[index] = KeyStroke.getInstance(modifierKeys, naturalKey);
			return keyStrokes;
		}

		final KeyStroke[] newKeyStrokes = new KeyStroke[keyStrokesLength + 1];
		System.arraycopy(keyStrokes, 0, newKeyStrokes, 0, index);
		newKeyStrokes[index] = stroke;
		if (index < keyStrokesLength) {
			System.arraycopy(keyStrokes, index, newKeyStrokes, index + 1,
					keyStrokesLength-index);
		}
		return newKeyStrokes;
	}

	/**
	 * Tests whether the cursor is in the last position. This means that the
	 * selection extends to the last position.
	 * 
	 * @return <code>true</code> if the selection extends to the last
	 *         position; <code>false</code> otherwise.
	 */
	private boolean isCursorInLastPosition() {
		return (text.getSelection().y >= getText().length());
	}

	/**
	 * Removes the given listener from this key sequence widget.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removePropertyChangeListener(
			final IPropertyChangeListener listener) {
		if ((listener == null) || (listeners == null)) {
			return;
		}

		listeners.remove(listener);
	}

	/**
	 * <p>
	 * A mutator for the key sequence stored within this widget. The text and
	 * caret position are updated.
	 * </p>
	 * <p>
	 * All sequences are limited to maxStrokes number of strokes in length. If
	 * there are already that number of strokes, then it does not show
	 * incomplete strokes, and does not keep track of them.
	 * </p>
	 * 
	 * @param newKeySequence
	 *            The new key sequence for this widget; may be <code>null</code>
	 *            if none.
	 */
	public void setKeySequence(KeySequence newKeySequence) {
		final KeySequence oldKeySequence = keySequence;

		if (newKeySequence == null) {
			text.setText(""); //$NON-NLS-1$
		} else {
			keySequence = newKeySequence;			
		}
		
		// Trim any extra strokes.
		if (maxStrokes != INFINITE) {
			final KeyStroke[] oldKeyStrokes = keySequence.getKeyStrokes();
			if (maxStrokes < oldKeyStrokes.length) {
				final KeyStroke[] newKeyStrokes = new KeyStroke[maxStrokes];
				System
						.arraycopy(oldKeyStrokes, 0, newKeyStrokes, 0,
								maxStrokes);
				keySequence = KeySequence.getInstance(newKeyStrokes);
			}
		}

		// Check to see if the text has changed.
		String currentString = getText();
		String newString = keySequence.format();
		if (!currentString.equals(newString)) {
			// We need to update the text
			text.removeModifyListener(updateSequenceListener);
			text.setText(keySequence.format());
			text.addModifyListener(updateSequenceListener);
			text.setSelection(getText().length());
		}

		firePropertyChangeEvent(oldKeySequence);
	}

	/**
	 * Returns the maximum number of strokes that are permitted in this widget
	 * at one time.
	 * 
	 * @return The maximum number of strokes; will be a positive integer or
	 *         <code>INFINITE</code>.
	 */
	public int getKeyStrokeLimit() {
		return maxStrokes;
	}

	/**
	 * A mutator for the maximum number of strokes that are permitted in this
	 * widget at one time.
	 * 
	 * @param keyStrokeLimit
	 *            The maximum number of strokes; must be a positive integer or
	 *            <code>INFINITE</code>.
	 */
	public void setKeyStrokeLimit(int keyStrokeLimit) {
		if (keyStrokeLimit > 0 || keyStrokeLimit == INFINITE) {
			this.maxStrokes = keyStrokeLimit;
		} else {
			throw new IllegalArgumentException();
		}

		// Make sure we are obeying the new limit.
		setKeySequence(getKeySequence());
	}
}
