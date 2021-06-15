/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * Interface to provide checked and grayed state information about data in trees
 * or tables. The following chart determines the checkbox state:
 * <table border="1">
 * 	<tr align="center">
 * 		<td/>
 * 		<td/>
 * 		<td colspan="2"><strong><code>isGrayed()</code></strong></td>
 * 	</tr>
 * 	<tr align="center">
 * 		<td/>
 * 		<td/>
 * 		<td><code>false</code></td>
 * 		<td><code>true</code></td>
 * 	</tr>
 * 	<tr align="center">
 * 		<td rowspan="2"><strong><code>isChecked()</code></strong></td>
 * 		<td><code>false</code></td>
 * 		<td colspan="2">unchecked</td>
 * 	</tr>
 * 	<tr align="center">
 * 		<td><code>true</code></td>
 * 		<td>checked</td>
 * 		<td>grayed</td>
 * 	</tr>
 * </table>
 * @since 1.3
 */
public interface ICheckStateProvider {
	
	/**
	 * Indicates if an element's representation should appear as checked or 
	 * gray instead of unchecked. If this method returns <code>true</code>
	 * the {@link ICheckStateProvider#isGrayed(Object)} method will determine
	 * whether the check box displays a check mark ("checked") or a box 
	 * ("grayed").
	 * @param element 
	 * @return true if the element should be checked or grayed, false if it 
	 * 		should be unchecked
	 */
	public boolean isChecked(Object element);
	
	/**
	 * Indicates whether the check box associated with an element, when checked
	 * as indicated by the {@link ICheckStateProvider#isChecked(Object)} method,
	 * should display the gray (boxed) state instead of the check mark.  
	 * @param element
	 * @return true if the element should be gray
	 */
	public boolean isGrayed(Object element);
}
