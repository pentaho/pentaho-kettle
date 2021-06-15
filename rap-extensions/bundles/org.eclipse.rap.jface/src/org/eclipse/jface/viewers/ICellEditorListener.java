/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.io.Serializable;

/**
 * A listener which is notified of significant events in the
 * life of a cell editor.
 * <p>
 * This interface should be implemented by classes that wish to
 * react to cell editor activity.
 * </p>
 * <p>
 * Note: the cell editor is not passed as a parameter to any
 * of these methods; so the assumption is that the listener
 * knows which cell editor is talking to it.
 * </p>
 */
public interface ICellEditorListener extends Serializable {
    /**
     * Notifies that the end user has requested applying a value.
     * All cell editors send this notification.
     * <p>
     * The normal reaction is to update the model with the current cell editor value.
     * However, if the value is not valid, it should not be applied. 
     * A typical text-based cell editor would send this message
     * when the end user hits Return, whereas other editors would
     * send it whenever their value changes.
     * </p>
     */
    public void applyEditorValue();

    /**
     * Notifies that the end user has canceled editing.
     * All cell editors send this notification.
     * A listener should <b>not</b> update the model based on this
     * notification; see <code>applyEditorValue</code>.
     */
    public void cancelEditor();

    /**
     * Notifies that the end user is changing the value in the cell editor. This
     * notification is normally sent only by text-based editors in response to a
     * keystroke, so that the listener may show an error message reflecting the
     * current valid state. This notification is sent while the value is being
     * actively edited, before the value is applied or canceled.  A listener should
     * <b>not</b> update the model based on this notification; see
     * <code>applyEditorValue</code>.
     * <p>
     * If the <code>newValidState</code> parameter is <code>true</code>,
     * the new value may be retrieved by calling <code>ICellEditor.getValue</code>
     * on the appropriate cell editor.
     * </p>
     * 
     * @param oldValidState the valid state before the end user changed the value
     * @param newValidState the current valid state
     */
    public void editorValueChanged(boolean oldValidState, boolean newValidState);
}
