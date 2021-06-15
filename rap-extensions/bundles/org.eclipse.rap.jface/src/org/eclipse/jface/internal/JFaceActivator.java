/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal;

import org.osgi.framework.*;

/**
 * JFaceActivator is the activator class for the JFace plug-in when it is being used
 * within a full Eclipse install.
 * @since 1.0
 *
 */
public class JFaceActivator implements BundleActivator {

	private static BundleContext bundleContext;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		InternalPolicy.OSGI_AVAILABLE = true;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		InternalPolicy.OSGI_AVAILABLE = false;
		bundleContext = null;
	}

	/**
	 * Return the bundle context for this bundle, or <code>null</code> if
	 * there is not one. (for instance if the bundle is not activated or we aren't
	 * running OSGi.
	 * 
	 * @return the bundle context or <code>null</code>
	 */
	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	/**
	 * Return the Bundle object for JFace. Returns <code>null</code> if it is not
	 * available.
	 * 
	 * @return the bundle or <code>null</code>
	 */
	public static Bundle getBundle() {
		return bundleContext == null ? null : bundleContext.getBundle();
	}

}
