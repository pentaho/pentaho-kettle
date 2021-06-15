/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * TreeViewerLabelProvider is the ViewerLabelProvider that handles TreePaths.
 * 
 * @since 1.0
 * 
 */
public class TreeColumnViewerLabelProvider extends
		TableColumnViewerLabelProvider {
	private ITreePathLabelProvider treePathProvider = new ITreePathLabelProvider() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreePathLabelProvider#updateLabel(org.eclipse.jface.viewers.ViewerLabel,
		 *      org.eclipse.jface.viewers.TreePath)
		 */
		public void updateLabel(ViewerLabel label, TreePath elementPath) {
			// Do nothing by default

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			// Do nothing by default

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			// Do nothing by default

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			// Do nothing by default

		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

	};

	/**
	 * Create a new instance of the receiver with the supplied labelProvider.
	 * 
	 * @param labelProvider
	 */
	public TreeColumnViewerLabelProvider(IBaseLabelProvider labelProvider) {
		super(labelProvider);
	}

	/**
	 * Update the label for the element with TreePath.
	 * 
	 * @param label
	 * @param elementPath
	 */
	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		treePathProvider.updateLabel(label, elementPath);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerLabelProvider#setProviders(java.lang.Object)
	 */
	public void setProviders(Object provider) {
		super.setProviders(provider);
		if (provider instanceof ITreePathLabelProvider)
			treePathProvider = (ITreePathLabelProvider) provider;
	}

	/**
	 * Return the ITreePathLabelProvider for the receiver.
	 * 
	 * @return Returns the treePathProvider.
	 */
	public ITreePathLabelProvider getTreePathProvider() {
		return treePathProvider;
	}

}
