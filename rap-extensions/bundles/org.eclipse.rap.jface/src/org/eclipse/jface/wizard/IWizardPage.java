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

import org.eclipse.jface.dialogs.IDialogPage;

/**
 * Interface for a wizard page.
 * <p>
 * The class <code>WizardPage</code> provides an abstract implementation
 * of this interface. However, clients are also free to implement this 
 * interface if <code>WizardPage</code> does not suit their needs.
 * </p>
 */
public interface IWizardPage extends IDialogPage {
    /**
     * Returns whether the next page could be displayed.
     *
     * @return <code>true</code> if the next page could be displayed,
     *   and <code>false</code> otherwise
     */
    public boolean canFlipToNextPage();

    /**
     * Returns this page's name.
     *
     * @return the name of this page
     */
    public String getName();

    /**
     * Returns the wizard page that would to be shown if the user was to
     * press the Next button.
     *
     * @return the next wizard page, or <code>null</code> if none
     */
    public IWizardPage getNextPage();

    /**
     * Returns the wizard page that would to be shown if the user was to
     * press the Back button.
     *
     * @return the previous wizard page, or <code>null</code> if none
     */
    public IWizardPage getPreviousPage();

    /**
     * Returns the wizard that hosts this wizard page.
     *
     * @return the wizard, or <code>null</code> if this page has not been
     *   added to any wizard
     * @see #setWizard
     */
    public IWizard getWizard();

    /**
     * Returns whether this page is complete or not.
     * <p>
     * This information is typically used by the wizard to decide
     * when it is okay to finish.
     * </p>
     *
     * @return <code>true</code> if this page is complete, and
     *  <code>false</code> otherwise
     */
    public boolean isPageComplete();

    /**
     * Sets the wizard page that would typically be shown 
     * if the user was to press the Back button.
     * <p>
     * This method is called by the container.
     * </p>
     *
     * @param page the previous wizard page
     */
    public void setPreviousPage(IWizardPage page);

    /**
     * Sets the wizard that hosts this wizard page.
     * Once established, a page's wizard cannot be changed
     * to a different wizard.
     *
     * @param newWizard the wizard
     * @see #getWizard
     */
    public void setWizard(IWizard newWizard);
}
