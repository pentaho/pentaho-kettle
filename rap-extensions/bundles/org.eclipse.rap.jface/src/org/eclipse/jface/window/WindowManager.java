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
package org.eclipse.jface.window;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;

/**
 * A manager for a group of windows. Window managers are an optional JFace 
 * feature used in applications which create many different windows (dialogs,
 * wizards, etc.) in addition to a main window. A window manager can be used to
 * remember all the windows that an application has created (independent of 
 * whether they are presently open or closed). There can be several window
 * managers, and they can be arranged into a tree. This kind of organization 
 * makes it simple to close whole subgroupings of windows.
 * <p>
 * Creating a window manager is as simple as creating an instance of 
 * <code>WindowManager</code>. Associating a window with a window manager is 
 * done with <code>WindowManager.add(Window)</code>. A window is automatically
 * removed from its window manager as a side effect of closing the window.
 * </p>
 *
 * @see Window
 */
public class WindowManager implements Serializable {

    /**
     * List of windows managed by this window manager
     * (element type: <code>Window</code>).
     */
    private ArrayList windows = new ArrayList();

    /**
     * List of window managers who have this window manager
     * as their parent (element type: <code>WindowManager</code>).
     */
    private List subManagers;

    /**
     * Creates an empty window manager without a parent window
     * manager (that is, a root window manager).
     */
    public WindowManager() {
    }

    /**
     * Creates an empty window manager with the given
     * window manager as parent.
     *
     * @param parent the parent window manager
     */
    public WindowManager(WindowManager parent) {
        Assert.isNotNull(parent);
        parent.addWindowManager(this);
    }

    /**
     * Adds the given window to the set of windows managed by
     * this window manager. Does nothing is this window is
     * already managed by this window manager.
     *
     * @param window the window
     */
    public void add(Window window) {
        if (!windows.contains(window)) {
            windows.add(window);
            window.setWindowManager(this);
        }
    }

    /**
     * Adds the given window manager to the list of
     * window managers that have this one as a parent.
     * </p>
     * @param wm the child window manager
     */
    private void addWindowManager(WindowManager wm) {
        if (subManagers == null) {
			subManagers = new ArrayList();
		}
        if (!subManagers.contains(wm)) {
            subManagers.add(wm);
        }
    }

    /**
     * Attempts to close all windows managed by this window manager,
     * as well as windows managed by any descendent window managers.
     *
     * @return <code>true</code> if all windows were sucessfully closed, 
     * and <code>false</code> if any window refused to close
     */
    public boolean close() {
        List t = (List) windows.clone(); // make iteration robust 
        Iterator e = t.iterator();
        while (e.hasNext()) {
            Window window = (Window) e.next();
            boolean closed = window.close();
            if (!closed) {
				return false;
			}
        }
        if (subManagers != null) {
            e = subManagers.iterator();
            while (e.hasNext()) {
                WindowManager wm = (WindowManager) e.next();
                boolean closed = wm.close();
                if (!closed) {
					return false;
				}
            }
        }
        return true;
    }

    /**
     * Returns this window manager's number of windows
     * 
     * @return the number of windows
     */
    public int getWindowCount() {
        return windows.size();
    }

    /**
     * Returns this window manager's set of windows.
     *
     * @return a possibly empty list of window
     */
    public Window[] getWindows() {
        Window bs[] = new Window[windows.size()];
        windows.toArray(bs);
        return bs;
    }

    /**
     * Removes the given window from the set of windows managed by
     * this window manager. Does nothing is this window is
     * not managed by this window manager.
     *
     * @param window the window
     */
    public final void remove(Window window) {
        if (windows.contains(window)) {
            windows.remove(window);
            window.setWindowManager(null);
        }
    }
}
