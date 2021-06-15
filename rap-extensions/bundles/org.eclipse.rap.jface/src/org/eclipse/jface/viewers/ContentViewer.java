/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

/**
 * A content viewer is a model-based adapter on a widget which accesses its
 * model by means of a content provider and a label provider.
 * <p>
 * A viewer's model consists of elements, represented by objects.
 * A viewer defines and implements generic infrastructure for handling model 
 * input, updates, and selections in terms of elements.
 * Input is obtained by querying an <code>IContentProvider</code> which returns
 * elements. The elements themselves are not displayed directly.  They are
 * mapped to labels, containing text and/or an image, using the viewer's 
 * <code>ILabelProvider</code>.
 * </p>
 * <p>
 * Implementing a concrete content viewer typically involves the following steps:
 * <ul>
 * <li>
 * create SWT controls for viewer (in constructor) (optional)
 * </li>
 * <li>
 * initialize SWT controls from input (inputChanged)
 * </li>
 * <li>
 * define viewer-specific update methods
 * </li>
 * <li>
 * support selections (<code>setSelection</code>, <code>getSelection</code>)
 * </ul>
 * </p>
 */
public abstract class ContentViewer extends Viewer {

    /**
     * This viewer's content provider, or <code>null</code> if none.
     */
    private IContentProvider contentProvider = null;

    /**
     * This viewer's input, or <code>null</code> if none.
     * The viewer's input provides the "model" for the viewer's content.
     */
    private Object input = null;

    /**
     * This viewer's label provider. Initially <code>null</code>, but
     * lazily initialized (to a <code>SimpleLabelProvider</code>).
     */
    private IBaseLabelProvider labelProvider = null;

    /**
     * This viewer's label provider listener.
     * Note: Having a viewer register a label provider listener with
     * a label provider avoids having to define public methods
     * for internal events.
     */
    private final ILabelProviderListener labelProviderListener = new ILabelProviderListener() {
    	private boolean logWhenDisposed = true; // initially true, set to false
        
        public void labelProviderChanged(LabelProviderChangedEvent event) {
        	Control control = getControl();
        	if (control == null || control.isDisposed()) {
    			if (logWhenDisposed) {
    				String message = "Ignored labelProviderChanged notification because control is diposed." + //$NON-NLS-1$
    						" This indicates a potential memory leak."; //$NON-NLS-1$
    				if (!InternalPolicy.DEBUG_LOG_LABEL_PROVIDER_NOTIFICATIONS_WHEN_DISPOSED) {
    					// stop logging after the first
    					logWhenDisposed = false;
    					message += " This is only logged once per viewer instance," + //$NON-NLS-1$
    							" but similar calls will still be ignored."; //$NON-NLS-1$
    				}
    				Policy.getLog().log(
    						new Status(IStatus.WARNING, Policy.JFACE, message,
    								new RuntimeException()));
    			}
        		return;
        	}
            ContentViewer.this.handleLabelProviderChanged(event);
        }
    };

    /**
     * Creates a content viewer with no input, no content provider, and a
     * default label provider.
     */
    protected ContentViewer() {
    }

    /**
     * Returns the content provider used by this viewer, 
     * or <code>null</code> if this view does not yet have a content
     * provider.
     * <p>
     * The <code>ContentViewer</code> implementation of this method returns the content
     * provider recorded is an internal state variable. 
     * Overriding this method is generally not required; 
     * however, if overriding in a subclass, 
     * <code>super.getContentProvider</code> must be invoked.
     * </p>
     *
     * @return the content provider, or <code>null</code> if none
     */
    public IContentProvider getContentProvider() {
        return contentProvider;
    }

    /**
     * The <code>ContentViewer</code> implementation of this <code>IInputProvider</code> 
     * method returns the current input of this viewer, or <code>null</code>
     * if none. The viewer's input provides the "model" for the viewer's
     * content.
     */
    public Object getInput() {
        return input;
    }

    /**
     * Returns the label provider used by this viewer.
     * <p>
     * The <code>ContentViewer</code> implementation of this method returns the label
     * provider recorded in an internal state variable; if none has been
     * set (with <code>setLabelProvider</code>) a default label provider
     * will be created, remembered, and returned.
     * Overriding this method is generally not required; 
     * however, if overriding in a subclass,
     * <code>super.getLabelProvider</code> must be invoked.
     * </p>
     *
     * @return a label provider
     */
    public IBaseLabelProvider getLabelProvider() {
        if (labelProvider == null) {
			labelProvider = new LabelProvider();
		}
        return labelProvider;
    }

    /**
     * Handles a dispose event on this viewer's control.
     * <p>
     * The <code>ContentViewer</code> implementation of this method disposes of this
     * viewer's label provider and content provider (if it has one).
     * Subclasses should override this method to perform any additional
     * cleanup of resources; however, overriding methods must invoke
     * <code>super.handleDispose</code>.
     * </p>
     *
     * @param event a dispose event
     */
    protected void handleDispose(DisposeEvent event) {
        if (contentProvider != null) {
            contentProvider.inputChanged(this, getInput(), null);
            contentProvider.dispose();
            contentProvider = null;
        }
        if (labelProvider != null) {
            labelProvider.removeListener(labelProviderListener);
            labelProvider.dispose();
            labelProvider = null;
        }
        input = null;
    }

    /**
     * Handles a label provider changed event.
     * <p>
     * The <code>ContentViewer</code> implementation of this method calls <code>labelProviderChanged()</code>
     * to cause a complete refresh of the viewer.
     * Subclasses may reimplement or extend. 
     * </p>
     * @param event the change event
     */
    protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {
        labelProviderChanged();
    }

    /**
     * Adds event listener hooks to the given control.
     * <p>
     * All subclasses must call this method when their control is
     * first established.
     * </p> 
     * <p>
     * The <code>ContentViewer</code> implementation of this method hooks 
     * dispose events for the given control.
     * Subclasses may override if they need to add other control hooks;
     * however, <code>super.hookControl</code> must be invoked.
     * </p>
     *
     * @param control the control
     */
    protected void hookControl(Control control) {
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                handleDispose(event);
            }
        });
    }

    /**
     * Notifies that the label provider has changed.
     * <p>
     * The <code>ContentViewer</code> implementation of this method calls <code>refresh()</code>.
     * Subclasses may reimplement or extend.
     * </p>
     */
    protected void labelProviderChanged() {
        refresh();
    }

    /**
     * Sets the content provider used by this viewer.
     * <p>
     * The <code>ContentViewer</code> implementation of this method records the 
     * content provider in an internal state variable.
     * Overriding this method is generally not required; 
     * however, if overriding in a subclass,
     * <code>super.setContentProvider</code> must be invoked.
     * </p>
     *
     * @param contentProvider the content provider
     * @see #getContentProvider
     */
    public void setContentProvider(IContentProvider contentProvider) {
        Assert.isNotNull(contentProvider);
        IContentProvider oldContentProvider = this.contentProvider;
        this.contentProvider = contentProvider;
        if (oldContentProvider != null) {
            Object currentInput = getInput();
            oldContentProvider.inputChanged(this, currentInput, null);
            oldContentProvider.dispose();
            contentProvider.inputChanged(this, null, currentInput);
            refresh();
        }
    }

    /**
     * The <code>ContentViewer</code> implementation of this <code>Viewer</code>
     * method invokes <code>inputChanged</code> on the content provider and then the
     * <code>inputChanged</code> hook method. This method fails if this viewer does
     * not have a content provider. Subclassers are advised to override 
     * <code>inputChanged</code> rather than this method, but may extend this method
     * if required.
     */
    public void setInput(Object input) {
    	Control control = getControl();
		if (control == null || control.isDisposed()) {
			throw new IllegalStateException(
					"Need an underlying widget to be able to set the input." + //$NON-NLS-1$
							"(Has the widget been disposed?)"); //$NON-NLS-1$
		}
        Assert
                .isTrue(getContentProvider() != null,
                        "ContentViewer must have a content provider when input is set."); //$NON-NLS-1$

        Object oldInput = getInput();
        contentProvider.inputChanged(this, oldInput, input);
        this.input = input;

        // call input hook
        inputChanged(this.input, oldInput);
    }

    /**
     * Sets the label provider for this viewer.
     * <p>
     * The <code>ContentViewer</code> implementation of this method ensures that the
     * given label provider is connected to this viewer and the
     * former label provider is disconnected from this viewer.
     * Overriding this method is generally not required; 
     * however, if overriding in a subclass,
     * <code>super.setLabelProvider</code> must be invoked.
     * </p>
     *
     * @param labelProvider the label provider, or <code>null</code> if none
     */
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        IBaseLabelProvider oldProvider = this.labelProvider;
        // If it hasn't changed, do nothing.
        // This also ensures that the provider is not disposed
        // if set a second time.
        if (labelProvider == oldProvider) {
            return;
        }
        if (oldProvider != null) {
            oldProvider.removeListener(this.labelProviderListener);
        }
        this.labelProvider = labelProvider;
        if (labelProvider != null) {
            labelProvider.addListener(this.labelProviderListener);
        }
        refresh();

        // Dispose old provider after refresh, so that items never refer to stale images.
        if (oldProvider != null) {
    		internalDisposeLabelProvider(oldProvider);
        }
    }

	/**
	 * @param oldProvider
	 * 
	 * @since 1.1
	 */
	void internalDisposeLabelProvider(IBaseLabelProvider oldProvider) {
		oldProvider.dispose();
	}
}
