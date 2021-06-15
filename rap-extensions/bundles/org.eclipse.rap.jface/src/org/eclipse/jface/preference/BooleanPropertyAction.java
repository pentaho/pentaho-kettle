/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.preference;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * The BooleanPropertyAction is an action that set the values of a 
 * boolean property in the preference store.
 */

public class BooleanPropertyAction extends Action {

    private IPreferenceStore preferenceStore;

    private String property;

    /**
     * Create a new instance of the receiver.
     * @param title The displayable name of the action.
     * @param preferenceStore The preference store to propogate changes to
     * @param property The property that is being updated
     * @throws IllegalArgumentException Thrown if preferenceStore or
     * property are <code>null</code>.
     */
    public BooleanPropertyAction(String title,
            IPreferenceStore preferenceStore, String property)
            throws IllegalArgumentException {
        super(title, AS_CHECK_BOX);

        if (preferenceStore == null || property == null) {
			throw new IllegalArgumentException();
		}

        this.preferenceStore = preferenceStore;
        this.property = property;
        final String finalProprety = property;

        preferenceStore
                .addPropertyChangeListener(new IPropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (finalProprety.equals(event.getProperty())) {
							setChecked(Boolean.TRUE.equals(event.getNewValue()));
						}
                    }
                });

        setChecked(preferenceStore.getBoolean(property));
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        preferenceStore.setValue(property, isChecked());
    }
}
