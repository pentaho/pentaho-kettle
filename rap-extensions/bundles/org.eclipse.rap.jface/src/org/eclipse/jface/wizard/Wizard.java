/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract base implementation of a wizard. A typical client subclasses
 * <code>Wizard</code> to implement a particular wizard.
 * <p>
 * Subclasses may call the following methods to configure the wizard:
 * <ul>
 * <li><code>addPage</code></li>
 * <li><code>setHelpAvailable</code></li>
 * <li><code>setDefaultPageImageDescriptor</code></li>
 * <li><code>setDialogSettings</code></li>
 * <li><code>setNeedsProgressMonitor</code></li>
 * <li><code>setTitleBarColor</code></li>
 * <li><code>setWindowTitle</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may override these methods if required:
 * <ul>
 * <li>reimplement <code>createPageControls</code></li>
 * <li>reimplement <code>performCancel</code></li>
 * <li>extend <code>addPages</code></li>
 * <li>reimplement <code>performFinish</code></li>
 * <li>extend <code>dispose</code></li>
 * </ul>
 * </p>
 * <p>
 * Note that clients are free to implement <code>IWizard</code> from scratch
 * instead of subclassing <code>Wizard</code>. Correct implementations of
 * <code>IWizard</code> will work with any correct implementation of
 * <code>IWizardPage</code>.
 * </p>
 */
public abstract class Wizard implements IWizard {
    /**
     * Image registry key of the default image for wizard pages (value
     * <code>"org.eclipse.jface.wizard.Wizard.pageImage"</code>).
     */
    public static final String DEFAULT_IMAGE = "org.eclipse.jface.wizard.Wizard.pageImage";//$NON-NLS-1$

    /**
     * The wizard container this wizard belongs to; <code>null</code> if none.
     */
    private IWizardContainer container = null;

    /**
     * This wizard's list of pages (element type: <code>IWizardPage</code>).
     */
    private List pages = new ArrayList();

    /**
     * Indicates whether this wizard needs a progress monitor.
     */
    private boolean needsProgressMonitor = false;

    /**
     * Indicates whether this wizard needs previous and next buttons even if the
     * wizard has only one page.
     */
    private boolean forcePreviousAndNextButtons = false;

    /**
     * Indicates whether this wizard supports help.
     */
    private boolean isHelpAvailable = false;

    /**
     * The default page image for pages without one of their one;
     * <code>null</code> if none.
     */
    private Image defaultImage = null;

    /**
     * The default page image descriptor, used for creating a default page image
     * if required; <code>null</code> if none.
     */
    private ImageDescriptor defaultImageDescriptor = JFaceResources.getImageRegistry().getDescriptor(DEFAULT_IMAGE);

    /**
     * The color of the wizard title bar; <code>null</code> if none.
     */
    private RGB titleBarColor = null;

    /**
     * The window title string for this wizard; <code>null</code> if none.
     */
    private String windowTitle = null;

    /**
     * The dialog settings for this wizard; <code>null</code> if none.
     */
    private IDialogSettings dialogSettings = null;

    /**
     * Creates a new empty wizard.
     */
    protected Wizard() {
        super();
    }

    /**
     * Adds a new page to this wizard. The page is inserted at the end of the
     * page list.
     * 
     * @param page
     *            the new page
     */
    public void addPage(IWizardPage page) {
        pages.add(page);
        page.setWizard(this);
    }

	/**
     * The <code>Wizard</code> implementation of this <code>IWizard</code>
     * method does nothing. Subclasses should extend if extra pages need to be
     * added before the wizard opens. New pages should be added by calling
     * <code>addPage</code>.
     */
    public void addPages() {
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public boolean canFinish() {
        // Default implementation is to check if all pages are complete.
        for (int i = 0; i < pages.size(); i++) {
            if (!((IWizardPage) pages.get(i)).isPageComplete()) {
				return false;
			}
        }
        return true;
    }

    /**
     * The <code>Wizard</code> implementation of this <code>IWizard</code>
     * method creates all the pages controls using
     * <code>IDialogPage.createControl</code>. Subclasses should reimplement
     * this method if they want to delay creating one or more of the pages
     * lazily. The framework ensures that the contents of a page will be created
     * before attempting to show it.
     */
    public void createPageControls(Composite pageContainer) {
        // the default behavior is to create all the pages controls
        for (int i = 0; i < pages.size(); i++) {
            IWizardPage page = (IWizardPage) pages.get(i);
            page.createControl(pageContainer);
            // page is responsible for ensuring the created control is
            // accessable
            // via getControl.
            Assert.isNotNull(page.getControl());
        }
    }

    /**
     * The <code>Wizard</code> implementation of this <code>IWizard</code>
     * method disposes all the pages controls using
     * <code>DialogPage.dispose</code>. Subclasses should extend this method
     * if the wizard instance maintains addition SWT resource that need to be
     * disposed.
     */
    public void dispose() {
        // notify pages
        for (int i = 0; i < pages.size(); i++) {
			try {
                ((IWizardPage) pages.get(i)).dispose();
			} catch (Exception e) {
				Status status = new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR, e.getMessage(), e);
				Policy.getLog().log(status);
            }
        }
        // dispose of image
        if (defaultImage != null) {
            JFaceResources.getResources().destroyImage(defaultImageDescriptor);
            defaultImage = null;
        }
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public IWizardContainer getContainer() {
        return container;
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public Image getDefaultPageImage() {
        if (defaultImage == null) {
            defaultImage = JFaceResources.getResources().createImageWithDefault(defaultImageDescriptor);
        }
        return defaultImage;
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public IDialogSettings getDialogSettings() {
        return dialogSettings;
    }

    /*
     * (non-Javadoc) Method declared on IWizard. The default behavior is to
     * return the page that was added to this wizard after the given page.
     */
    public IWizardPage getNextPage(IWizardPage page) {
        int index = pages.indexOf(page);
        if (index == pages.size() - 1 || index == -1) {
			// last page or page not found
            return null;
		}
        return (IWizardPage) pages.get(index + 1);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public IWizardPage getPage(String name) {
        for (int i = 0; i < pages.size(); i++) {
            IWizardPage page = (IWizardPage) pages.get(i);
            String pageName = page.getName();
            if (pageName.equals(name)) {
				return page;
			}
        }
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public int getPageCount() {
        return pages.size();
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public IWizardPage[] getPages() {
        return (IWizardPage[]) pages.toArray(new IWizardPage[pages.size()]);
    }

    /*
     * (non-Javadoc) Method declared on IWizard. The default behavior is to
     * return the page that was added to this wizard before the given page.
     */
    public IWizardPage getPreviousPage(IWizardPage page) {
        int index = pages.indexOf(page);
        if (index == 0 || index == -1) {
			// first page or page not found
            return null;
		} 
		return (IWizardPage) pages.get(index - 1);
    }

    /**
     * Returns the wizard's shell if the wizard is visible. Otherwise
     * <code>null</code> is returned.
     * 
     * @return Shell
     */
    public Shell getShell() {
        if (container == null) {
			return null;
		}
        return container.getShell();
    }

    /*
     * (non-Javadoc) Method declared on IWizard. By default this is the first
     * page inserted into the wizard.
     */
    public IWizardPage getStartingPage() {
        if (pages.size() == 0) {
			return null;
		}
        return (IWizardPage) pages.get(0);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public RGB getTitleBarColor() {
        return titleBarColor;
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public String getWindowTitle() {
        return windowTitle;
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public boolean isHelpAvailable() {
        return isHelpAvailable;
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public boolean needsPreviousAndNextButtons() {
        return forcePreviousAndNextButtons || pages.size() > 1;
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public boolean needsProgressMonitor() {
        return needsProgressMonitor;
    }

    /**
     * The <code>Wizard</code> implementation of this <code>IWizard</code>
     * method does nothing and returns <code>true</code>. Subclasses should
     * reimplement this method if they need to perform any special cancel
     * processing for their wizard.
     */
    public boolean performCancel() {
        return true;
    }

    /**
     * Subclasses must implement this <code>IWizard</code> method to perform
     * any special finish processing for their wizard.
     */
    public abstract boolean performFinish();

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public void setContainer(IWizardContainer wizardContainer) {
        container = wizardContainer;
    }

    /**
     * Sets the default page image descriptor for this wizard.
     * <p>
     * This image descriptor will be used to generate an image for a page with
     * no image of its own; the image will be computed once and cached.
     * </p>
     * 
     * @param imageDescriptor
     *            the default page image descriptor
     */
    public void setDefaultPageImageDescriptor(ImageDescriptor imageDescriptor) {
        defaultImageDescriptor = imageDescriptor;
    }

    /**
     * Sets the dialog settings for this wizard.
     * <p>
     * The dialog settings is used to record state between wizard invocations
     * (for example, radio button selection, last import directory, etc.)
     * </p>
     * 
     * @param settings
     *            the dialog settings, or <code>null</code> if none
     * @see #getDialogSettings
     *  
     */
    public void setDialogSettings(IDialogSettings settings) {
        dialogSettings = settings;
    }

    /**
     * Controls whether the wizard needs Previous and Next buttons even if it
     * currently contains only one page.
     * <p>
     * This flag should be set on wizards where the first wizard page adds
     * follow-on wizard pages based on user input.
     * </p>
     * 
     * @param b
     *            <code>true</code> to always show Next and Previous buttons,
     *            and <code>false</code> to suppress Next and Previous buttons
     *            for single page wizards
     */
    public void setForcePreviousAndNextButtons(boolean b) {
        forcePreviousAndNextButtons = b;
    }

    /**
     * Sets whether help is available for this wizard.
     * <p>
	 * The result of this method is typically used by the container to show or hide the button
	 * labeled "Help".
	 * </p>
	 * <p>
	 * <strong>Note:</strong> This wizard's container might be a {@link TrayDialog} which provides
	 * its own help support.
     * </p>
     * 
	 * @param b <code>true</code> if help is available, <code>false</code> otherwise
     * @see #isHelpAvailable()
	 * @see TrayDialog#isHelpAvailable()
	 * @see TrayDialog#setHelpAvailable(boolean)
     */
    public void setHelpAvailable(boolean b) {
        isHelpAvailable = b;
    }

    /**
     * Sets whether this wizard needs a progress monitor.
     * 
     * @param b
     *            <code>true</code> if a progress monitor is required, and
     *            <code>false</code> if none is needed
     * @see #needsProgressMonitor()
     */
    public void setNeedsProgressMonitor(boolean b) {
        needsProgressMonitor = b;
    }

    /**
     * Sets the title bar color for this wizard.
     * 
     * @param color
     *            the title bar color
     */
    public void setTitleBarColor(RGB color) {
        titleBarColor = color;
    }

    /**
     * Sets the window title for the container that hosts this page to the given
     * string.
     * 
     * @param newTitle
     *            the window title for the container
     */
    public void setWindowTitle(String newTitle) {
        windowTitle = newTitle;
        if (container != null) {
			container.updateWindowTitle();
		}
    }
}
