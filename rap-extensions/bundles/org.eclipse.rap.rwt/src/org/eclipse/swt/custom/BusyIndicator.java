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
package org.eclipse.swt.custom;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Support for showing a Busy Cursor during a long running process.
 */
public class BusyIndicator {

//    static int nextBusyId = 1;
//    static final String BUSYID_NAME = "SWT BusyIndicator"; //$NON-NLS-1$
//    static final String BUSY_CURSOR = "SWT BusyIndicator Cursor"; //$NON-NLS-1$

/**
 * Runs the given <code>Runnable</code> while providing
 * busy feedback using this busy indicator.
 * 
 * @param display the display on which the busy feedback should be
 *        displayed.  If the display is null, the Display for the current
 *        thread will be used.  If there is no Display for the current thread,
 *        the runnable code will be executed and no busy feedback will be displayed.
 * @param runnable the runnable for which busy feedback is to be shown.
 *        Must not be null.
 * 
* @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
 * </ul>
 */

public static void showWhile(Display display, Runnable runnable) {
    if (runnable == null)
        SWT.error(SWT.ERROR_NULL_ARGUMENT);
//    if (display == null) {
//        display = Display.getCurrent();
//        if (display == null) {
//            runnable.run();
//            return;
//        }
//    }
    
//    Integer busyId = new Integer(nextBusyId);
//    nextBusyId++;
//    Cursor cursor = display.getSystemCursor(SWT.CURSOR_WAIT);
//    Shell[] shells = display.getShells();
//    for (int i = 0; i < shells.length; i++) {
//        Integer id = (Integer)shells[i].getData(BUSYID_NAME);
//        if (id == null) {
//            shells[i].setCursor(cursor);
//            shells[i].setData(BUSYID_NAME, busyId);
//        }
//    }
        
//    try {
        runnable.run();
//    } finally {
//        shells = display.getShells();
//        for (int i = 0; i < shells.length; i++) {
//            Integer id = (Integer)shells[i].getData(BUSYID_NAME);
//            if (id == busyId) {
//                shells[i].setCursor(null);
//                shells[i].setData(BUSYID_NAME, null);
//            }
//        }
//    }
}
}
