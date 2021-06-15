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
package org.eclipse.jface.wizard;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract base implementation of a wizard page.
 * <p>
 * Subclasses must implement the <code>createControl</code> method
 * to create the specific controls for the wizard page.
 * </p>
 * <p>
 * Subclasses may call the following methods to configure the wizard page:
 * <ul>
 *  <li><code>setDescription</code></li>
 *  <li><code>setErrorMessage</code></li>
 *  <li><code>setImageDescriptor</code></li>
 *  <li><code>setMessage</code></li>
 *  <li><code>setPageComplete</code></li>
 *  <li><code>setPreviousPage</code></li>
 *  <li><code>setTitle</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may override these methods if required:
 * <ul>
 *  <li><code>performHelp</code> - may be reimplemented to display help for the page</li>  
 * <li><code>canFlipToNextPage</code> - may be extended or reimplemented</li>
 *  <li><code>isPageComplete</code> - may be extended </li>
 *  <li><code>setDescription</code> - may be extended </li>
 *  <li><code>setTitle</code> - may be extended </li>
 *  <li><code>dispose</code> - may be extended to dispose additional allocated SWT resources</li>
 * </ul>
 * </p>
 * <p>
 * Note that clients are free to implement <code>IWizardPage</code> from scratch
 * instead of subclassing <code>WizardPage</code>. Correct implementations of
 * <code>IWizardPage</code> will work with any correct implementation of 
 * <code>IWizard</code>.
 * </p>
 */
public abstract class WizardPage extends DialogPage implements IWizardPage {

    /**
     * This page's name.
     */
    private String name;

    /**
     * The wizard to which this page belongs; <code>null</code>
     * if this page has yet to be added to a wizard.
     */
    private IWizard wizard = null;

    /**
     * Indicates whether this page is complete.
     */
    private boolean isPageComplete = true;

    /**
     * The page that was shown right before this page became visible;
     * <code>null</code> if none.
     */
    private IWizardPage previousPage = null;

    /**
     * Creates a new wizard page with the given name, and
     * with no title or image.
     *
     * @param pageName the name of the page
     */
    protected WizardPage(String pageName) {
        this(pageName, null, (ImageDescriptor) null);
    }

    /**
     * Creates a new wizard page with the given name, title, and image.
     *
     * @param pageName the name of the page
     * @param title the title for this wizard page,
     *   or <code>null</code> if none
     * @param titleImage the image descriptor for the title of this wizard page,
     *   or <code>null</code> if none
     */
    protected WizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(title, titleImage);
        Assert.isNotNull(pageName); // page name must not be null
        name = pageName;
    }

    /**
     * The <code>WizardPage</code> implementation of this <code>IWizardPage</code>
     * method returns <code>true</code> if this page is complete (<code>isPageComplete</code>)
     * and there is a next page to flip to. Subclasses may override (extend or reimplement).
     *
     * @see #getNextPage
     * @see #isPageComplete()
     */
    public boolean canFlipToNextPage() {
        return isPageComplete() && getNextPage() != null;
    }

    /**
     * Returns the wizard container for this wizard page.
     *
     * @return the wizard container, or <code>null</code> if this
     *   wizard page has yet to be added to a wizard, or the
     *   wizard has yet to be added to a container
     */
    protected IWizardContainer getContainer() {
        if (wizard == null) {
			return null;
		}
        return wizard.getContainer();
    }

    /**
     * Returns the dialog settings for this wizard page.
     *
     * @return the dialog settings, or <code>null</code> if none
     */
    protected IDialogSettings getDialogSettings() {
        if (wizard == null) {
			return null;
		}
        return wizard.getDialogSettings();
    }

    /* (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public Image getImage() {
        Image result = super.getImage();

        if (result == null && wizard != null) {
			return wizard.getDefaultPageImage();
		}

        return result;
    }

    /* (non-Javadoc)
     * Method declared on IWizardPage.
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * Method declared on IWizardPage.
     * The default behavior is to ask the wizard for the next page.
     */
    public IWizardPage getNextPage() {
        if (wizard == null) {
			return null;
		}
        return wizard.getNextPage(this);
    }

    /* (non-Javadoc)
     * Method declared on IWizardPage.
     * The default behavior is return the cached previous back or,
     * lacking that, to ask the wizard for the previous page.
     */
    public IWizardPage getPreviousPage() {
        if (previousPage != null) {
			return previousPage;
		}

        if (wizard == null) {
			return null;
		}

        return wizard.getPreviousPage(this);
    }

    /**
     * The <code>WizardPage</code> implementation of this method declared on
     * <code>DialogPage</code> returns the shell of the container.
     * The advantage of this implementation is that the shell is accessable
     * once the container is created even though this page's control may not 
     * yet be created.
     */
    public Shell getShell() {

        IWizardContainer container = getContainer();
        if (container == null) {
			return null;
		}

        // Ask the wizard since our contents may not have been created.
        return container.getShell();
    }

    /* (non-Javadoc)
     * Method declared on IWizardPage.
     */
    public IWizard getWizard() {
        return wizard;
    }

    /**
     * Returns whether this page is the current one in the wizard's container.
     *
     * @return <code>true</code> if the page is active,
     *  and <code>false</code> otherwise
     */
    protected boolean isCurrentPage() {
        return (getContainer() != null && this == getContainer()
                .getCurrentPage());
    }

    /**
     * The <code>WizardPage</code> implementation of this <code>IWizard</code> method 
     * returns the value of an internal state variable set by
     * <code>setPageComplete</code>. Subclasses may extend.
     */
    public boolean isPageComplete() {
        return isPageComplete;
    }

    /**
     * The <code>WizardPage</code> implementation of this <code>IDialogPage</code>
     * method extends the <code>DialogPage</code> implementation to update
     * the wizard container title bar. Subclasses may extend.
     */
    public void setDescription(String description) {
        super.setDescription(description);
        if (isCurrentPage()) {
			getContainer().updateTitleBar();
		}
    }

    /**
     * The <code>WizardPage</code> implementation of this method 
     * declared on <code>DialogPage</code> updates the container
     * if this is the current page.
     */
    public void setErrorMessage(String newMessage) {
        super.setErrorMessage(newMessage);
        if (isCurrentPage()) {
            getContainer().updateMessage();
        }
    }

    /**
     * The <code>WizardPage</code> implementation of this method 
     * declared on <code>DialogPage</code> updates the container
     * if this page is the current page.
     */
    public void setImageDescriptor(ImageDescriptor image) {
        super.setImageDescriptor(image);
        if (isCurrentPage()) {
			getContainer().updateTitleBar();
		}
    }

    /**
     * The <code>WizardPage</code> implementation of this method 
     * declared on <code>DialogPage</code> updates the container
     * if this is the current page.
     */
    public void setMessage(String newMessage, int newType) {
        super.setMessage(newMessage, newType);
        if (isCurrentPage()) {
			getContainer().updateMessage();
		}
    }

    /**
     * Sets whether this page is complete. 
     * <p>
     * This information is typically used by the wizard to decide
     * when it is okay to move on to the next page or finish up.
     * </p>
     *
     * @param complete <code>true</code> if this page is complete, and
     *   and <code>false</code> otherwise
     * @see #isPageComplete()
     */
    public void setPageComplete(boolean complete) {
        isPageComplete = complete;
        if (isCurrentPage()) {
			getContainer().updateButtons();
		}
    }

    /* (non-Javadoc)
     * Method declared on IWizardPage.
     */
    public void setPreviousPage(IWizardPage page) {
        previousPage = page;
    }

    /**
     * The <code>WizardPage</code> implementation of this <code>IDialogPage</code>
     * method extends the <code>DialogPage</code> implementation to update
     * the wizard container title bar. Subclasses may extend.
     */
    public void setTitle(String title) {
        super.setTitle(title);
        if (isCurrentPage()) {
            getContainer().updateTitleBar();
        }
    }

    /* (non-Javadoc)
     * Method declared on IWizardPage.
     */
    public void setWizard(IWizard newWizard) {
        wizard = newWizard;
    }

    /**
     * Returns a printable representation of this wizard page suitable
     * only for debug purposes.
     */
    public String toString() {
        return name;
    }
}
