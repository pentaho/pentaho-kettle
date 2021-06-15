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
 * A group marker is a special kind of contribution item denoting
 * the beginning of a group. These groups are used to structure
 * the list of items. Unlike regular contribution items and
 * separators, group markers have no visual representation.
 * The name of the group is synonymous with the contribution item id.
 * <p>
 * This class may be instantiated; it is not intended to be 
 * subclassed outside the framework.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GroupMarker extends AbstractGroupMarker {
    /**
     * Create a new group marker with the given name.
     * The group name must not be <code>null</code> or the empty string.
     * The group name is also used as the item id.
     * 
     * @param groupName the name of the group
     */
    public GroupMarker(String groupName) {
        super(groupName);
    }

    /**
     * The <code>GroupMarker</code> implementation of this method
     * returns <code>false</code> since group markers are always invisible.
     */
    public boolean isVisible() {
        return false;
    }
}
