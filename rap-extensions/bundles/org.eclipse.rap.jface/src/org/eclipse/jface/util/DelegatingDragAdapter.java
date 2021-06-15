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
package org.eclipse.jface.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * A <code>DelegatingDragAdapter</code> is a <code>DragSourceListener</code> that  
 * maintains and delegates to a set of {@link TransferDragSourceListener}s. Each 
 * TransferDragSourceListener can then be implemented as if it were the 
 * <code>DragSource's</code> only DragSourceListener.
 * <p>
 * When a drag is started, a subset of all <code>TransferDragSourceListeners</code>
 * is generated and stored in a list of <i>active</i> listeners. This subset is
 * calculated by forwarding {@link DragSourceListener#dragStart(DragSourceEvent)} to 
 * every listener, and checking if the {@link DragSourceEvent#doit doit} field is left 
 * set to <code>true</code>.
 * </p> 
 * The <code>DragSource</code>'s set of supported Transfer types ({@link
 * DragSource#setTransfer(Transfer[])}) is updated to reflect the Transfer types
 * corresponding to the active listener subset.
 * <p>
 * If and when {@link #dragSetData(DragSourceEvent)} is called, a single
 * <code>TransferDragSourceListener</code> is chosen, and only it is allowed to set the
 * drag data. The chosen listener is the first listener in the subset of active listeners
 * whose Transfer supports ({@link Transfer#isSupportedType(TransferData)}) the 
 * <code>dataType</code> in the <code>DragSourceEvent</code>.
 * </p>
 * <p>
 * The following example snippet shows a <code>DelegatingDragAdapter</code> with two
 * <code>TransferDragSourceListeners</code>. One implements drag of text strings, 
 * the other supports file transfer and demonstrates how a listener can be disabled using
 * the dragStart method. 
 * </p>
 * <code><pre>
 *		final TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
 *		
 *		DelegatingDragAdapter dragAdapter = new DelegatingDragAdapter();		
 *		dragAdapter.addDragSourceListener(new TransferDragSourceListener() {
 *			public Transfer getTransfer() {
 *				return TextTransfer.getInstance();
 *			}
 *			public void dragStart(DragSourceEvent event) {
 *				// always enabled, can control enablement based on selection etc.
 *			}
 *			public void dragSetData(DragSourceEvent event) {
 *				event.data = "Transfer data";
 *			}
 *			public void dragFinished(DragSourceEvent event) {
 *				// no clean-up required
 *			}
 *		});
 *		dragAdapter.addDragSourceListener(new TransferDragSourceListener() {
 *			public Transfer getTransfer() {
 *				return FileTransfer.getInstance();
 *			}
 *			public void dragStart(DragSourceEvent event) {
 *				// enable drag listener if there is a viewer selection
 *				event.doit = !viewer.getSelection().isEmpty();
 *			}
 *			public void dragSetData(DragSourceEvent event) {
 *				File file1 = new File("C:/temp/file1");
 *				File file2 = new File("C:/temp/file2");
 *				event.data = new String[] {file1.getAbsolutePath(), file2.getAbsolutePath()};
 *			}
 *			public void dragFinished(DragSourceEvent event) {
 *				// no clean-up required
 *			}
 *		});
 *		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, dragAdapter.getTransfers(), dragAdapter);
 * </pre></code>
 * @since 1.3
 */
public class DelegatingDragAdapter implements DragSourceListener {
    private List listeners = new ArrayList();

    private List activeListeners = new ArrayList();

    private TransferDragSourceListener currentListener;

    /**
     * Adds the given <code>TransferDragSourceListener</code>.
     * 
     * @param listener the new listener
     */
    public void addDragSourceListener(TransferDragSourceListener listener) {
        listeners.add(listener);
    }

    /**
     * The drop has successfully completed. This event is forwarded to the current 
     * drag listener.
     * Doesn't update the current listener, since the current listener  is already the one
     * that completed the drag operation.
     * 
     * @param event the drag source event
     * @see DragSourceListener#dragFinished(DragSourceEvent)
     */
    public void dragFinished(final DragSourceEvent event) {
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Drag Finished: " + toString()); //$NON-NLS-1$
        SafeRunnable.run(new SafeRunnable() {
            public void run() throws Exception {
                if (currentListener != null) {
                    // there is a listener that can handle the drop, delegate the event
                    currentListener.dragFinished(event);
                } else {
                    // The drag was canceled and currentListener was never set, so send the
                    // dragFinished event to all the active listeners. 
                    Iterator iterator = activeListeners.iterator();
                    while (iterator.hasNext()) {
						((TransferDragSourceListener) iterator.next())
                                .dragFinished(event);
					}
                }
            }
        });
        currentListener = null;
        activeListeners.clear();
    }

    /**
     * The drop data is requested.
     * Updates the current listener and then forwards the event to it.
     * 
     * @param event the drag source event
     * @see DragSourceListener#dragSetData(DragSourceEvent)
     */
    public void dragSetData(final DragSourceEvent event) {
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Drag Set Data: " + toString()); //$NON-NLS-1$

        updateCurrentListener(event); // find a listener that can provide the given data type
        if (currentListener != null) {
        	SafeRunnable.run(new SafeRunnable() {
                public void run() throws Exception {
                    currentListener.dragSetData(event);
                }
            });
        }
    }

    /**
     * A drag operation has started.
     * Forwards this event to each listener. A listener must set <code>event.doit</code> 
     * to <code>false</code> if it cannot handle the drag operation. If a listener can  
     * handle the drag, it is added to the list of active listeners.  
     * The drag is aborted if there are no listeners that can handle it.  
     * 
     * @param event the drag source event
     * @see DragSourceListener#dragStart(DragSourceEvent)
     */
    public void dragStart(final DragSourceEvent event) {
        //		if (Policy.DEBUG_DRAG_DROP)
        //			System.out.println("Drag Start: " + toString()); //$NON-NLS-1$
        boolean doit = false; // true if any one of the listeners can handle the drag
        List transfers = new ArrayList(listeners.size());

        activeListeners.clear();
        for (int i = 0; i < listeners.size(); i++) {
            final TransferDragSourceListener listener = (TransferDragSourceListener) listeners
                    .get(i);
            event.doit = true; // restore event.doit
            SafeRunnable.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.dragStart(event);
                }
            });
            if (event.doit) { // the listener can handle this drag
                transfers.add(listener.getTransfer());
                activeListeners.add(listener);
            }
            doit |= event.doit;
        }

        if (doit) {
			((DragSource) event.widget).setTransfer((Transfer[]) transfers
                    .toArray(new Transfer[transfers.size()]));
		}

        event.doit = doit;
    }

    /**
     * Returns the <code>Transfer<code>s from every <code>TransferDragSourceListener</code>.
     * 
     * @return the combined <code>Transfer</code>s
     */
    public Transfer[] getTransfers() {
        Transfer[] types = new Transfer[listeners.size()];
        for (int i = 0; i < listeners.size(); i++) {
            TransferDragSourceListener listener = (TransferDragSourceListener) listeners
                    .get(i);
            types[i] = listener.getTransfer();
        }
        return types;
    }

    /**
     * Returns <code>true</code> if there are no listeners to delegate drag events to.
     * 
     * @return <code>true</code> if there are no <code>TransferDragSourceListeners</code>
     * 	<code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    /**
     * Removes the given <code>TransferDragSourceListener</code>.
     * Listeners should not be removed while a drag and drop operation is in progress.
     *  
     * @param listener the <code>TransferDragSourceListener</code> to remove
     */
    public void removeDragSourceListener(TransferDragSourceListener listener) {
        listeners.remove(listener);
        if (currentListener == listener) {
			currentListener = null;
		}
        if (activeListeners.contains(listener)) {
			activeListeners.remove(listener);
		}
    }

    /**
     * Updates the current listener to one that can handle the drag. There can 
     * be many listeners and each listener may be able to handle many <code>TransferData</code> 
     * types.  The first listener found that supports one of the <code>TransferData</ode> 
     * types specified in the <code>DragSourceEvent</code> will be selected.
     * 
     * @param event the drag source event
     */
    private void updateCurrentListener(DragSourceEvent event) {
        currentListener = null;
        if (event.dataType == null) {
			return;
		}
        Iterator iterator = activeListeners.iterator();
        while (iterator.hasNext()) {
            TransferDragSourceListener listener = (TransferDragSourceListener) iterator
                    .next();

            if (listener.getTransfer().isSupportedType(event.dataType)) {
                //				if (Policy.DEBUG_DRAG_DROP)
                //					System.out.println("Current drag listener: " + listener); //$NON-NLS-1$			
                currentListener = listener;
                return;
            }
        }
    }

}
