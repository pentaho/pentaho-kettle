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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * An instance of <code>IScheme</code> is a handle representing a binding
 * scheme as defined by the extension point <code>org.eclipse.ui.bindings</code>.
 * The identifier of the handle is the identifier of the scheme being represented.
 * </p>
 * <p>
 * An instance of <code>IScheme</code> can be obtained from an instance of
 * <code>ICommandManager</code> for any identifier, whether or not a scheme
 * with that identifier is defined in the plugin registry.
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime
 * plugin activation and deactivation. If a scheme is defined, that means that
 * its corresponding plug-in is active. If the plug-in is then deactivated, the
 * scheme will still exist but it will be undefined. An attempt to use an
 * undefined scheme will result in a <code>NotDefinedException</code>
 * being thrown.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 1.4
 * @see ISchemeListener
 * @see org.eclipse.core.commands.CommandManager
 */
public final class Scheme extends NamedHandleObject implements Comparable {

    /**
     * The collection of all objects listening to changes on this scheme. This
     * value is <code>null</code> if there are no listeners.
     */
    private Set listeners = null;

    /**
     * The parent identifier for this scheme. This is the identifier of the
     * scheme from which this scheme inherits some of its bindings. This value
     * can be <code>null</code> if the scheme has no parent.
     */
    private String parentId = null;

    /**
     * Constructs a new instance of <code>Scheme</code> with an identifier.
     * 
     * @param id
     *            The identifier to create; must not be <code>null</code>.
     */
    Scheme(final String id) {
        super(id);
    }

    /**
     * Registers an instance of <code>ISchemeListener</code> to listen for
     * changes to attributes of this instance.
     * 
     * @param schemeListener
     *            the instance of <code>ISchemeListener</code> to register.
     *            Must not be <code>null</code>. If an attempt is made to
     *            register an instance of <code>ISchemeListener</code> which
     *            is already registered with this instance, no operation is
     *            performed.
     */
    public final void addSchemeListener(final ISchemeListener schemeListener) {
        if (schemeListener == null) {
            throw new NullPointerException("Can't add a null scheme listener."); //$NON-NLS-1$
        }

        if (listeners == null) {
            listeners = new HashSet();
        }

        listeners.add(schemeListener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public final int compareTo(final Object object) {
        final Scheme scheme = (Scheme) object;
        int compareTo = Util.compare(this.id, scheme.id);
        if (compareTo == 0) {
            compareTo = Util.compare(this.name, scheme.name);
            if (compareTo == 0) {
                compareTo = Util.compare(this.parentId, scheme.parentId);
                if (compareTo == 0) {
                    compareTo = Util.compare(this.description,
                            scheme.description);
                    if (compareTo == 0) {
                        compareTo = Util.compare(this.defined, scheme.defined);
                    }
                }
            }
        }

        return compareTo;
    }

    /**
     * <p>
     * Defines this scheme by giving it a name, and possibly a description and a
     * parent identifier as well. The defined property for the scheme automatically 
     * becomes <code>true</code>.
     * </p>
     * <p>
     * Notification is sent to all listeners that something has changed.
     * </p>
     * 
     * @param name
     *            The name of this scheme; must not be <code>null</code>.
     * @param description
     *            The description for this scheme; may be <code>null</code>.
     * @param parentId
     *            The parent identifier for this scheme; may be
     *            <code>null</code>.
     */
    public final void define(final String name, final String description,
            final String parentId) {
        if (name == null) {
            throw new NullPointerException(
                    "The name of a scheme cannot be null"); //$NON-NLS-1$
        }

        final boolean definedChanged = !this.defined;
        this.defined = true;

        final boolean nameChanged = !Util.equals(this.name, name);
        this.name = name;

        final boolean descriptionChanged = !Util.equals(this.description,
                description);
        this.description = description;

        final boolean parentIdChanged = !Util.equals(this.parentId, parentId);
        this.parentId = parentId;

        fireSchemeChanged(new SchemeEvent(this, definedChanged, nameChanged,
                descriptionChanged, parentIdChanged));
    }

    /**
     * Notifies all listeners that this scheme has changed. This sends the given
     * event to all of the listeners, if any.
     * 
     * @param event
     *            The event to send to the listeners; must not be
     *            <code>null</code>.
     */
    private final void fireSchemeChanged(final SchemeEvent event) {
        if (event == null) {
            throw new NullPointerException(
                    "Cannot send a null event to listeners."); //$NON-NLS-1$
        }

        if (listeners == null) {
            return;
        }

        final Iterator listenerItr = listeners.iterator();
        while (listenerItr.hasNext()) {
            final ISchemeListener listener = (ISchemeListener) listenerItr
                    .next();
            listener.schemeChanged(event);
        }
    }

    /**
     * <p>
     * Returns the identifier of the parent of the scheme represented by this
     * handle.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the identifier of the parent of the scheme represented by this
     *         handle. May be <code>null</code>.
     * @throws NotDefinedException
     *             if the scheme represented by this handle is not defined.
     */
    public final String getParentId() throws NotDefinedException {
        if (!defined) {
            throw new NotDefinedException(
                    "Cannot get the parent identifier from an undefined scheme. "  //$NON-NLS-1$
            		+ id);
        }

        return parentId;
    }

    /**
     * Unregisters an instance of <code>ISchemeListener</code> listening for
     * changes to attributes of this instance.
     * 
     * @param schemeListener
     *            the instance of <code>ISchemeListener</code> to unregister.
     *            Must not be <code>null</code>. If an attempt is made to
     *            unregister an instance of <code>ISchemeListener</code> which
     *            is not already registered with this instance, no operation is
     *            performed.
     */
    public final void removeSchemeListener(final ISchemeListener schemeListener) {
        if (schemeListener == null) {
            throw new NullPointerException("Cannot remove a null listener."); //$NON-NLS-1$
        }

        if (listeners == null) {
            return;
        }

        listeners.remove(schemeListener);

        if (listeners.isEmpty()) {
            listeners = null;
        }
    }

    /**
     * The string representation of this command -- for debugging purposes only.
     * This string should not be shown to an end user.
     * 
     * @return The string representation; never <code>null</code>.
     */
    public final String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Scheme("); //$NON-NLS-1$
            stringBuffer.append(id);
            stringBuffer.append(',');
            stringBuffer.append(name);
            stringBuffer.append(',');
            stringBuffer.append(description);
            stringBuffer.append(',');
            stringBuffer.append(parentId);
            stringBuffer.append(',');
            stringBuffer.append(defined);
            stringBuffer.append(')');
            string = stringBuffer.toString();
        }
        return string;
    }

    /**
     * Makes this scheme become undefined. This has the side effect of changing
     * the name, description and parent identifier to <code>null</code>.
     * Notification is sent to all listeners.
     */
    public final void undefine() {
        string = null;

        final boolean definedChanged = defined;
        defined = false;

        final boolean nameChanged = name != null;
        name = null;

        final boolean descriptionChanged = description != null;
        description = null;

        final boolean parentIdChanged = parentId != null;
        parentId = null;

        fireSchemeChanged(new SchemeEvent(this, definedChanged, nameChanged,
                descriptionChanged, parentIdChanged));
    }
}
