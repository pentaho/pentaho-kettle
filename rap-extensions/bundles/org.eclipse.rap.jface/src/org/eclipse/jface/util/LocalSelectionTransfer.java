/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.util;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * A LocalSelectionTransfer may be used for drag and drop operations
 * within the same instance of Eclipse.
 * The selection is made available directly for use in the DropTargetListener.
 * dropAccept method. The DropTargetEvent passed to dropAccept does not contain
 * the drop data. The selection may be used for validation purposes so that the
 * drop can be aborted if appropriate.
 *
 * This class is not intended to be subclassed.
 * 
 * @since 1.3
 */
public class LocalSelectionTransfer extends ByteArrayTransfer {

    // First attempt to create a UUID for the type name to make sure that
    // different Eclipse applications use different "types" of
    // <code>LocalSelectionTransfer</code>
    private static final String TYPE_NAME = "local-selection-transfer-format" + (new Long(System.currentTimeMillis())).toString(); //$NON-NLS-1$;

    private static final int TYPEID = registerType(TYPE_NAME);

    private static final LocalSelectionTransfer INSTANCE = new LocalSelectionTransfer();

    private ISelection selection;

    private long selectionSetTime;

    /**
     * Only the singleton instance of this class may be used. 
     */
    protected LocalSelectionTransfer() {
    	// do nothing
    }

    /**
     * Returns the singleton.
     * 
     * @return the singleton
     */
    public static LocalSelectionTransfer getTransfer() {
        return INSTANCE;
    }

    /**
     * Returns the local transfer data.
     * 
     * @return the local transfer data
     */
    public ISelection getSelection() {
        return selection;
    }

    /**
     * Tests whether native drop data matches this transfer type.
     * 
     * @param result result of converting the native drop data to Java
     * @return true if the native drop data does not match this transfer type.
     * 	false otherwise.
     */
    private boolean isInvalidNativeType(Object result) {
        return !(result instanceof byte[])
                || !TYPE_NAME.equals(new String((byte[]) result));
    }

    /**
     * Returns the type id used to identify this transfer.
     * 
     * @return the type id used to identify this transfer.
     */
    protected int[] getTypeIds() {
        return new int[] { TYPEID };
    }

    /**
     * Returns the type name used to identify this transfer.
     * 
     * @return the type name used to identify this transfer.
     */
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    /**
     * Overrides org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(Object,
     * TransferData).
     * Only encode the transfer type name since the selection is read and
     * written in the same process.
     * 
     * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
     */
    public void javaToNative(Object object, TransferData transferData) {
        byte[] check = TYPE_NAME.getBytes();
        super.javaToNative(check, transferData);
    }

    /**
     * Overrides org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(TransferData).
     * Test if the native drop data matches this transfer type.
     * 
     * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(TransferData)
     */
    public Object nativeToJava(TransferData transferData) {
        Object result = super.nativeToJava(transferData);
        if (isInvalidNativeType(result)) {
        	Policy.getLog().log(new Status(
                            IStatus.ERROR,
                            Policy.JFACE,
                            IStatus.ERROR,
                            JFaceResources.getString("LocalSelectionTransfer.errorMessage"), null)); //$NON-NLS-1$
        }
        return selection;
    }

    /**
     * Sets the transfer data for local use.
     * 
     * @param s the transfer data
     */
    public void setSelection(ISelection s) {
        selection = s;
    }

    /**
     * Returns the time when the selection operation
     * this transfer is associated with was started.
     * 
     * @return the time when the selection operation has started
     * 
     * <!-- @see org.eclipse.swt.events.TypedEvent#time -->
     */
    public long getSelectionSetTime() {
        return selectionSetTime;
    }

    /**
     * Sets the time when the selection operation this
     * transfer is associated with was started.
     * If assigning this from an SWT event, be sure to use
     * <code>setSelectionTime(event.time & 0xFFFF)</code>
     * 
     * @param time the time when the selection operation was started
     * 
     * <!-- @see org.eclipse.swt.events.TypedEvent#time -->
     */
    public void setSelectionSetTime(long time) {
        selectionSetTime = time;
    }
}
