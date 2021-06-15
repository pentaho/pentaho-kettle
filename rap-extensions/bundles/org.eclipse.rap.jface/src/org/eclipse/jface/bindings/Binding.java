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

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A binding is a link between user input and the triggering of a particular
 * command. The most common example of a binding is a keyboard shortcut, but
 * there are also mouse and gesture bindings.
 * </p>
 * <p>
 * Bindings are linked to particular conditions within the application. Some of
 * these conditions change infrequently (e.g., locale, scheme), while some will
 * tend to change quite frequently (e.g., context). This allows the bindings to
 * be tailored to particular situations. For example, a set of bindings may be
 * appropriate only inside a text editor.  Or, perhaps, a set of bindings might
 * be appropriate only for a given locale, such as bindings that coexist with
 * the Input Method Editor (IME) on Chinese locales.
 * </p>
 * <p>
 * It is also possible to remove a particular binding. This is typically done as
 * part of user configuration (e.g., user changing keyboard shortcuts). However,
 * it can also be helpful when trying to change a binding on a particular locale
 * or platform. An "unbinding" is really just a binding with no command
 * identifier. For it to unbind a particular binding, it must match that binding
 * in its context identifier and scheme identifier. Subclasses (e.g.,
 * <code>KeyBinding</code>) may require other properties to match (e.g.,
 * <code>keySequence</code>). If these properties match, then this is an
 * unbinding. Note: the locale and platform can be different.
 * </p>
 * <p>
 * For example, imagine you have a key binding that looks like this:
 * </p>
 * <code><pre>
 * KeyBinding(command, scheme, context, &quot;Ctrl+Shift+F&quot;)
 * </pre></code>
 * <p>
 * On GTK+, the "Ctrl+Shift+F" interferes with some native behaviour. To change
 * the binding, we first unbind the "Ctrl+Shift+F" key sequence by 
 * assigning it a null command on the gtk platform.  We then create a new binding
 * that maps the command to the "Esc Ctrl+F" key sequence.
 * </p>
 * <code><pre>
 *     KeyBinding("Ctrl+Shift+F",null,scheme,context,null,gtk,null,SYSTEM)
 *     KeyBinding("Esc Ctrl+F",parameterizedCommand,scheme,context,null,gtk,SYSTEM)
 * </pre></code>
 * <p>
 * Bindings are intended to be immutable objects.
 * </p>
 * 
 * @since 1.4
 */
public abstract class Binding {

	/**
	 * The constant integer hash code value meaning the hash code has not yet
	 * been computed.
	 */
	private static final int HASH_CODE_NOT_COMPUTED = -1;

	/**
	 * A factor for computing the hash code for all key bindings.
	 */
	private final static int HASH_FACTOR = 89;

	/**
	 * The seed for the hash code for all key bindings.
	 */
	private final static int HASH_INITIAL = Binding.class.getName().hashCode();

	/**
	 * The type of binding that is defined by the system (i.e., by the
	 * application developer). In the case of an application based on the
	 * Eclipse workbench, this is the registry.
	 */
	public static final int SYSTEM = 0;

	/**
	 * The type of binding that is defined by the user (i.e., by the end user of
	 * the application). In the case of an application based on the Eclipse
	 * workbench, this is the preference store.
	 */
	public static final int USER = 1;

	/**
	 * The parameterized command to which this binding applies. This value may
	 * be <code>null</code> if this binding is meant to "unbind" an existing
	 * binding.
	 */
	private final ParameterizedCommand command;

	/**
	 * The context identifier to which this binding applies. This context must
	 * be active before this key binding becomes active. This value will never
	 * be <code>null</code>.
	 */
	private final String contextId;

	/**
	 * The hash code for this key binding. This value is computed lazily, and
	 * marked as invalid when one of the values on which it is based changes.
	 */
	private transient int hashCode = HASH_CODE_NOT_COMPUTED;

	/**
	 * The locale in which this binding applies. This value may be
	 * <code>null</code> if this binding is meant to apply to all locales.
	 * This string should be in the same format returned by
	 * <code>Locale.getDefault().toString()</code>.
	 */
	private final String locale;

	/**
	 * The platform on which this binding applies. This value may be
	 * <code>null</code> if this binding is meant to apply to all platforms.
	 * This string should be in the same format returned by
	 * <code>SWT.getPlatform</code>.
	 */
	private final String platform;

	/**
	 * The identifier of the scheme in which this binding applies. This value
	 * will never be <code>null</code>.
	 */
	private final String schemeId;

	/**
	 * The string representation of this binding. This string is for debugging
	 * purposes only, and is not meant to be displayed to the user. This value
	 * is computed lazily.
	 */
	protected transient String string = null;

	/**
	 * The type of binding this represents. This is used to distinguish between
	 * different priority levels for bindings. For example, in our case,
	 * <code>USER</code> bindings override <code>SYSTEM</code> bindings.
	 */
	private final int type;

	/**
	 * Constructs a new instance of <code>Binding</code>.
	 * 
	 * @param command
	 *            The parameterized command to which this binding applies; this
	 *            value may be <code>null</code> if the binding is meant to
	 *            "unbind" a previously defined binding.
	 * @param schemeId
	 *            The scheme to which this binding belongs; this value must not
	 *            be <code>null</code>.
	 * @param contextId
	 *            The context to which this binding applies; this value must not
	 *            be <code>null</code>.
	 * @param locale
	 *            The locale to which this binding applies; this value may be
	 *            <code>null</code> if it applies to all locales.
	 * @param platform
	 *            The platform to which this binding applies; this value may be
	 *            <code>null</code> if it applies to all platforms.
	 * @param windowManager
	 *            The window manager to which this binding applies; this value
	 *            may be <code>null</code> if it applies to all window
	 *            managers. This value is currently ignored.
	 * @param type
	 *            The type of binding. This should be either <code>SYSTEM</code>
	 *            or <code>USER</code>.
	 */
	protected Binding(final ParameterizedCommand command,
			final String schemeId, final String contextId, final String locale,
			final String platform, final String windowManager, final int type) {
		if (schemeId == null) {
			throw new NullPointerException("The scheme cannot be null"); //$NON-NLS-1$
		}

		if (contextId == null) {
			throw new NullPointerException("The context cannot be null"); //$NON-NLS-1$
		}

		if ((type != SYSTEM) && (type != USER)) {
			throw new IllegalArgumentException(
					"The type must be SYSTEM or USER"); //$NON-NLS-1$
		}

		this.command = command;
		this.schemeId = schemeId.intern();
		this.contextId = contextId.intern();
		this.locale = (locale == null) ? null : locale.intern();
		this.platform = (platform == null) ? null : platform.intern();
		this.type = type;
	}

	/**
	 * Tests whether this binding is intended to delete another binding. The
	 * receiver must have a <code>null</code> command identifier.
	 * 
	 * @param binding
	 *            The binding to test; must not be <code>null</code>.
	 *            This binding must be a <code>SYSTEM</code> binding.
	 * @return <code>true</code> if the receiver deletes the binding defined by
	 * 			the argument.
	 */
	final boolean deletes(final Binding binding) {
		boolean deletes = true;
		deletes &= Util.equals(getContextId(), binding.getContextId());
		deletes &= Util.equals(getTriggerSequence(), binding
				.getTriggerSequence());
		if (getLocale() != null) {
			deletes &= !Util.equals(getLocale(), binding.getLocale());
		}
		if (getPlatform() != null) {
			deletes &= !Util.equals(getPlatform(), binding.getPlatform());
		}
		deletes &= (binding.getType() == SYSTEM);
		deletes &= Util.equals(getParameterizedCommand(), null);

		return deletes;
	}

	/**
	 * Tests whether this binding is equal to another object. Bindings are only
	 * equal to other bindings with equivalent values.
	 * 
	 * @param object
	 *            The object with which to compare; may be <code>null</code>.
	 * @return <code>true</code> if the object is a binding with equivalent
	 *         values for all of its properties; <code>false</code> otherwise.
	 */
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;

		}
		if (!(object instanceof Binding)) {
			return false;
		}

		final Binding binding = (Binding) object;
		if (!Util.equals(getParameterizedCommand(), binding
				.getParameterizedCommand())) {
			return false;
		}
		if (!Util.equals(getContextId(), binding.getContextId())) {
			return false;
		}
		if (!Util.equals(getTriggerSequence(), binding.getTriggerSequence())) {
			return false;
		}
		if (!Util.equals(getLocale(), binding.getLocale())) {
			return false;
		}
		if (!Util.equals(getPlatform(), binding.getPlatform())) {
			return false;
		}
		if (!Util.equals(getSchemeId(), binding.getSchemeId())) {
			return false;
		}
		return (getType() == binding.getType());
	}

	/**
	 * Returns the parameterized command to which this binding applies. If the
	 * identifier is <code>null</code>, then this binding is "unbinding" an
	 * existing binding.
	 * 
	 * @return The fully-parameterized command; may be <code>null</code>.
	 */
	public final ParameterizedCommand getParameterizedCommand() {
		return command;
	}

	/**
	 * Returns the identifier of the context in which this binding applies.
	 * 
	 * @return The context identifier; never <code>null</code>.
	 */
	public final String getContextId() {
		return contextId;
	}

	/**
	 * Returns the locale in which this binding applies. If the locale is
	 * <code>null</code>, then this binding applies to all locales. This
	 * string is the same format as returned by
	 * <code>Locale.getDefault().toString()</code>.
	 * 
	 * @return The locale; may be <code>null</code>.
	 */
	public final String getLocale() {
		return locale;
	}

	/**
	 * Returns the platform on which this binding applies. If the platform is
	 * <code>null</code>, then this binding applies to all platforms. This
	 * string is the same format as returned by <code>SWT.getPlatform()</code>.
	 * 
	 * @return The platform; may be <code>null</code>.
	 */
	public final String getPlatform() {
		return platform;
	}

	/**
	 * Returns the identifier of the scheme in which this binding applies.
	 * 
	 * @return The scheme identifier; never <code>null</code>.
	 */
	public final String getSchemeId() {
		return schemeId;
	}

	/**
	 * Returns the sequence of trigger for a given binding. The triggers can be
	 * anything, but above all it must be hashable. This trigger sequence is
	 * used by the binding manager to distinguish between different bindings.
	 * 
	 * @return The object representing an input event that will trigger this
	 *         binding; must not be <code>null</code>.
	 */
	public abstract TriggerSequence getTriggerSequence();

	/**
	 * Returns the type for this binding. As it stands now, this value will
	 * either be <code>SYSTEM</code> or <code>USER</code>. In the future,
	 * more types might be added.
	 * 
	 * @return The type for this binding.
	 */
	public final int getType() {
		return type;
	}

	/**
	 * Computes the hash code for this key binding based on all of its
	 * attributes.
	 * 
	 * @return The hash code for this key binding.
	 */
	public final int hashCode() {
		if (hashCode == HASH_CODE_NOT_COMPUTED) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR
					+ Util.hashCode(getParameterizedCommand());
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(getContextId());
			hashCode = hashCode * HASH_FACTOR
					+ Util.hashCode(getTriggerSequence());
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(getLocale());
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(getPlatform());
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(getSchemeId());
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(getType());
			if (hashCode == HASH_CODE_NOT_COMPUTED) {
				hashCode++;
			}
		}

		return hashCode;
	}

	/**
	 * The string representation of this binding -- for debugging purposes only.
	 * This string should not be shown to an end user. This should be overridden
	 * by subclasses that add properties.
	 * 
	 * @return The string representation; never <code>null</code>.
	 */
	public String toString() {
		if (string == null) {
			
			final StringWriter sw = new StringWriter();
			final BufferedWriter stringBuffer = new BufferedWriter(sw);
			try {
				stringBuffer.write("Binding("); //$NON-NLS-1$
				stringBuffer.write(getTriggerSequence().toString());
				stringBuffer.write(',');
				stringBuffer.newLine();
				stringBuffer.write('\t');
				stringBuffer.write(command==null?"":command.toString()); //$NON-NLS-1$
				stringBuffer.write(',');
				stringBuffer.newLine();
				stringBuffer.write('\t');
				stringBuffer.write(schemeId);
				stringBuffer.write(',');
				stringBuffer.newLine();
				stringBuffer.write('\t');
				stringBuffer.write(contextId);
				stringBuffer.write(',');
				stringBuffer.write(locale==null?"":locale); //$NON-NLS-1$
				stringBuffer.write(',');
				stringBuffer.write(platform==null?"":platform); //$NON-NLS-1$
				stringBuffer.write(',');
				stringBuffer.write((type == SYSTEM) ? "system" : "user"); //$NON-NLS-1$//$NON-NLS-2$
				stringBuffer.write(')');
				stringBuffer.flush();
			} catch (IOException e) {
				// shouldn't get this
			}
			string = sw.toString();
		}

		return string;
	}
}
