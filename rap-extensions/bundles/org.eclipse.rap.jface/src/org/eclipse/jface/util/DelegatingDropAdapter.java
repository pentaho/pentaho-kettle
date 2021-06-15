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
package org.eclipse.jface.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * A <code>DelegatingDropAdapter</code> is a <code>DropTargetListener</code> that 
 * maintains and delegates to a set of {@link TransferDropTargetListener}s. Each 
 * <code>TransferDropTargetListener</code> can then be implemented as if it were 
 * the DropTarget's only <code>DropTargetListener</code>.
 * <p>
 * On <code>dragEnter</code>, <code>dragOperationChanged</code>, <code>dragOver</code>
 * and <code>drop</code>, a <i>current</i> listener is obtained from the set of all 
 * <code>TransferDropTargetListeners</code>. The current listener is the first listener 
 * to return <code>true</code> for 
 * {@link TransferDropTargetListener#isEnabled(DropTargetEvent)}.
 * The current listener is forwarded all <code>DropTargetEvents</code> until some other
 * listener becomes the current listener, or the drop terminates.
 * </p>
 * <p>
 * After adding all <code>TransferDropTargetListeners</code> to the 
 * <code>DelegatingDropAdapter</code> the combined set of <code>Transfers</code> should 
 * be set in the SWT <code>DropTarget</code>. <code>#getTransfers()</code> provides the 
 * set of <code>Transfer</code> types of all <code>TransferDropTargetListeners</code>. 
 * </p>
 * <p>
 * The following example snippet shows a <code>DelegatingDropAdapter</code> with two
 * <code>TransferDropTargetListeners</code>. One supports dropping resources and 
 * demonstrates how a listener can be disabled in the isEnabled method. 
 * The other listener supports text transfer. 
 * </p>
 * <code><pre>
 *		final TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
 * 		DelegatingDropAdapter dropAdapter = new DelegatingDropAdapter();
 *		dropAdapter.addDropTargetListener(new TransferDropTargetListener() {
 *			public Transfer getTransfer() {
 *				return ResourceTransfer.getInstance();
 *			}
 *			public boolean isEnabled(DropTargetEvent event) {
 *				// disable drop listener if there is no viewer selection
 *				if (viewer.getSelection().isEmpty())
 *					return false;
 *				return true;
 *			}
 *			public void dragEnter(DropTargetEvent event) {}
 *			public void dragLeave(DropTargetEvent event) {}
 *			public void dragOperationChanged(DropTargetEvent event) {}
 *			public void dragOver(DropTargetEvent event) {}
 *			public void drop(DropTargetEvent event) {
 *				if (event.data == null)
 *					return;
 *				IResource[] resources = (IResource[]) event.data;
 *				if (event.detail == DND.DROP_COPY) {
 *					// copy resources
 *				} else {
 *					// move resources
 *				}
 *					
 *			}
 *			public void dropAccept(DropTargetEvent event) {}
 *		});
 *		dropAdapter.addDropTargetListener(new TransferDropTargetListener() {
 *			public Transfer getTransfer() {
 *				return TextTransfer.getInstance();
 *			}
 *			public boolean isEnabled(DropTargetEvent event) {
 *				return true;
 *			}
 *			public void dragEnter(DropTargetEvent event) {}
 *			public void dragLeave(DropTargetEvent event) {}
 *			public void dragOperationChanged(DropTargetEvent event) {}
 *			public void dragOver(DropTargetEvent event) {}
 *			public void drop(DropTargetEvent event) {
 *				if (event.data == null)
 *					return;
 *				System.out.println(event.data);
 *			}
 *			public void dropAccept(DropTargetEvent event) {}
 *		});		
 *		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, dropAdapter.getTransfers(), dropAdapter);
 * </pre></code>
 * @since 1.3
 */
public class DelegatingDropAdapter implements DropTargetListener {
    private List listeners = new ArrayList();

    private TransferDropTargetListener currentListener;

    private int originalDropType;

    /**
     * Adds the given <code>TransferDropTargetListener</code>.
     * 
     * @param listener the new listener
     */
    public void addDropTargetListener(TransferDropTargetListener listener) {
        listeners.add(listener);
    }

    /**
     * The cursor has entered the drop target boundaries. The current listener is 
     * updated, and <code>#dragEnter()</code> is forwarded to the current listener.
     * 
     * @param event the drop target event 
     * @see DropTargetListener#dragEnter(DropTargetEvent)
     */
    public void dragEnter(DropTargetEvent event) {
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Drag Enter: " + toString()); //$NON-NLS-1$
        originalDropType = event.detail;
        updateCurrentListener(event);
    }

    /**
     * The cursor has left the drop target boundaries. The event is forwarded to the 
     * current listener.
     * 
     * @param event the drop target event
     * @see DropTargetListener#dragLeave(DropTargetEvent)
     */
    public void dragLeave(final DropTargetEvent event) {
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Drag Leave: " + toString()); //$NON-NLS-1$
        setCurrentListener(null, event);
    }

    /**
     * The operation being performed has changed (usually due to the user changing 
     * a drag modifier key while dragging). Updates the current listener and forwards 
     * this event to that listener.
     * 
     * @param event the drop target event
     * @see DropTargetListener#dragOperationChanged(DropTargetEvent)
     */
    public void dragOperationChanged(final DropTargetEvent event) {
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Drag Operation Changed to: " + event.detail); //$NON-NLS-1$
        originalDropType = event.detail;
        TransferDropTargetListener oldListener = getCurrentListener();
        updateCurrentListener(event);
        final TransferDropTargetListener newListener = getCurrentListener();
        // only notify the current listener if it hasn't changed based on the 
        // operation change. otherwise the new listener would get a dragEnter 
        // followed by a dragOperationChanged with the exact same event. 
        if (newListener != null && newListener == oldListener) {
            SafeRunnable.run(new SafeRunnable() {
                public void run() throws Exception {
                    newListener.dragOperationChanged(event);
                }
            });
        }
    }

    /**
     * The cursor is moving over the drop target. Updates the current listener and 
     * forwards this event to that listener. If no listener can handle the drag 
     * operation the <code>event.detail</code> field is set to <code>DND.DROP_NONE</code> 
     * to indicate an invalid drop.
     *   
     * @param event the drop target event
     * @see DropTargetListener#dragOver(DropTargetEvent)
     */
    public void dragOver(final DropTargetEvent event) {
        TransferDropTargetListener oldListener = getCurrentListener();
        updateCurrentListener(event);
        final TransferDropTargetListener newListener = getCurrentListener();

        // only notify the current listener if it hasn't changed based on the 
        // drag over. otherwise the new listener would get a dragEnter 
        // followed by a dragOver with the exact same event. 
        if (newListener != null && newListener == oldListener) {
        	SafeRunnable.run(new SafeRunnable() {
                public void run() throws Exception {
                    newListener.dragOver(event);
                }
            });
        }
    }

    /**
     * Forwards this event to the current listener, if there is one. Sets the
     * current listener to <code>null</code> afterwards.
     * 
     * @param event the drop target event
     * @see DropTargetListener#drop(DropTargetEvent)
     */
    public void drop(final DropTargetEvent event) {
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Drop: " + toString()); //$NON-NLS-1$
        updateCurrentListener(event);
        if (getCurrentListener() != null) {
        	SafeRunnable.run(new SafeRunnable() {
                public void run() throws Exception {
                    getCurrentListener().drop(event);
                }
            });
        }
        setCurrentListener(null, event);
    }

    /**
     * Forwards this event to the current listener if there is one.
     * 
     * @param event the drop target event
     * @see DropTargetListener#dropAccept(DropTargetEvent)
     */
    public void dropAccept(final DropTargetEvent event) {
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Drop Accept: " + toString()); //$NON-NLS-1$
        if (getCurrentListener() != null) {
        	SafeRunnable.run(new SafeRunnable() {
                public void run() throws Exception {
                    getCurrentListener().dropAccept(event);
                }
            });
        }
    }

    /**
     * Returns the listener which currently handles drop events.
     * 
     * @return the <code>TransferDropTargetListener</code> which currently 
     * 	handles drop events.
     */
    private TransferDropTargetListener getCurrentListener() {
        return currentListener;
    }

    /**
     * Returns the transfer data type supported by the given listener.
     * Returns <code>null</code> if the listener does not support any of the   
     * specified data types.
     *  
     * @param dataTypes available data types
     * @param listener <code>TransferDropTargetListener</code> to use for testing 
     * 	supported data types.
     * @return the transfer data type supported by the given listener or 
     * 	<code>null</code>.
     */
    private TransferData getSupportedTransferType(TransferData[] dataTypes,
            TransferDropTargetListener listener) {
        for (int i = 0; i < dataTypes.length; i++) {
            if (listener.getTransfer().isSupportedType(dataTypes[i])) {
                return dataTypes[i];
            }
        }
        return null;
    }

    /**
     * Returns the combined set of <code>Transfer</code> types of all 
     * <code>TransferDropTargetListeners</code>.
     * 
     * @return the combined set of <code>Transfer</code> types
     */
    public Transfer[] getTransfers() {
        Transfer[] types = new Transfer[listeners.size()];
        for (int i = 0; i < listeners.size(); i++) {
            TransferDropTargetListener listener = (TransferDropTargetListener) listeners
                    .get(i);
            types[i] = listener.getTransfer();
        }
        return types;
    }

    /**
     * Returns <code>true</code> if there are no listeners to delegate events to.
     * 
     * @return <code>true</code> if there are no <code>TransferDropTargetListeners</code>
     *	<code>false</code> otherwise
     */
    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    /**
     * Removes the given <code>TransferDropTargetListener</code>.
     * Listeners should not be removed while a drag and drop operation is in progress.
     * 
     * @param listener the listener to remove
     */
    public void removeDropTargetListener(TransferDropTargetListener listener) {
        if (currentListener == listener) {
			currentListener = null;
		}
        listeners.remove(listener);
    }

    /**
     * Sets the current listener to <code>listener</code>. Sends the given 
     * <code>DropTargetEvent</code> if the current listener changes.
     * 
     * @return <code>true</code> if the new listener is different than the previous
     *	<code>false</code> otherwise
     */
    private boolean setCurrentListener(TransferDropTargetListener listener,
            final DropTargetEvent event) {
        if (currentListener == listener) {
			return false;
		}
        if (currentListener != null) {
        	SafeRunnable.run(new SafeRunnable() {
                public void run() throws Exception {
                    currentListener.dragLeave(event);
                }
            });
        }
        currentListener = listener;
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Current drop listener: " + listener); //$NON-NLS-1$
        if (currentListener != null) {
        	SafeRunnable.run(new SafeRunnable() {
                public void run() throws Exception {
                    currentListener.dragEnter(event);
                }
            });
        }
        return true;
    }

    /**
     * Updates the current listener to one that can handle the drop. There can be many
     * listeners and each listener may be able to handle many <code>TransferData</code> 
     * types. The first listener found that can handle a drop of one of the given 
     * <code>TransferData</code> types will be selected.
     * If no listener can handle the drag operation the <code>event.detail</code> field
     * is set to <code>DND.DROP_NONE</code> to indicate an invalid drop.
     *
     * @param event the drop target event
     */
    private void updateCurrentListener(DropTargetEvent event) {
        int originalDetail = event.detail;
        // revert the detail to the "original" drop type that the User indicated.
        // this is necessary because the previous listener may have changed the detail 
        // to something other than what the user indicated.
        event.detail = originalDropType;

        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            TransferDropTargetListener listener = (TransferDropTargetListener) iter
                    .next();
            TransferData dataType = getSupportedTransferType(event.dataTypes,
                    listener);
            if (dataType != null) {
                TransferData originalDataType = event.currentDataType;
                // set the data type supported by the drop listener
                event.currentDataType = dataType;
                if (listener.isEnabled(event)) {
                    // if the listener stays the same, set its previously determined  
                    // event detail 
                    if (!setCurrentListener(listener, event)) {
						event.detail = originalDetail;
					}
                    return;
                }
				event.currentDataType = originalDataType;
            }
        }
        setCurrentListener(null, event);
        event.detail = DND.DROP_NONE;
        
        // -always- ensure that expand/scroll are on...otherwise
        // if a valid drop target is a child of an invalid one
        // you can't get there...
        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
    }
}
