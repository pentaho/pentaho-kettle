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
package org.eclipse.jface.action;

/**
 * This interface is used by instances of <code>IContributionItem</code>
 * to determine if the values for certain properties have been overriden
 * by their manager.
 * <p>
 * This interface is internal to the framework; it should not be implemented outside
 * the framework.
 * </p>
 * 
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IContributionManagerOverrides {
    /**
     * Id for the enabled property. Value is <code>"enabled"</code>.
     * 
     */
    public final static String P_ENABLED = "enabled"; //$NON-NLS-1$

    /**
     * Find out the enablement of the item
     * @param item the contribution item for which the enable override value is 
     * determined
     * @return <ul>
     * 				<li><code>Boolean.TRUE</code> if the given contribution item should be enabled</li>
     * 				<li><code>Boolean.FALSE</code> if the item should be disabled</li>
     * 				<li><code>null</code> if the item may determine its own enablement</li>
     * 			</ul>
     */
    public Boolean getEnabled(IContributionItem item);

    /**
     * This is not intended to be called outside of the workbench. This method
     * is intended to be deprecated in 3.1.
     * 
     * TODO deprecate for 3.1 
     * @param item the contribution item for which the accelerator value is determined
     * @return the accelerator
     */
    public Integer getAccelerator(IContributionItem item);

    /**
     * This is not intended to be called outside of the workbench. This method
     * is intended to be deprecated in 3.1.
     * 
     * TODO deprecate for 3.1
     * @param item the contribution item for which the accelerator text is determined
     * @return the text for the accelerator
     */
    public String getAcceleratorText(IContributionItem item);

    /**
     * This is not intended to be called outside of the workbench. This method
     * is intended to be deprecated in 3.1.
     * 
     * TODO deprecate for 3.1
     * @param item the contribution item for which the text is determined
     * @return the text
     */
    public String getText(IContributionItem item);
    
    /**
     * Visiblity override.
     * 
     * @param item the contribution item in question
     * @return  <ul>
     * 				<li><code>Boolean.TRUE</code> if the given contribution item should be visible</li>
     * 				<li><code>Boolean.FALSE</code> if the item should not be visible</li>
     * 				<li><code>null</code> if the item may determine its own visibility</li>
     * 			</ul>
     * @since 1.3
     */
    public Boolean getVisible(IContributionItem item);
}
