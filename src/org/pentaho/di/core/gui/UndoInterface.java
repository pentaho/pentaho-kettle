/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.gui;

import org.pentaho.di.core.undo.TransAction;



public interface UndoInterface
{
    /**
     * Add an undo operation to the undo list
     *
     * @param from array of objects representing the old state
     * @param to array of objectes representing the new state
     * @param pos An array of object locations
     * @param prev An array of points representing the old positions
     * @param curr An array of points representing the new positions
     * @param type_of_change The type of change that's being done to the transformation.
     * @param nextAlso indicates that the next undo operation needs to follow this one.
     */
    public void addUndo(Object from[], Object to[], int pos[], Point prev[], Point curr[], int type_of_change, boolean nextAlso);
    
    /**
     * Get the maximum number of undo operations possible
     *
     * @return The maximum number of undo operations that are allowed.
     */
    public int getMaxUndo();

    /**
     * Sets the maximum number of undo operations that are allowed.
     *
     * @param mu The maximum number of undo operations that are allowed.
     */
    public void setMaxUndo(int mu);

    /**
     * Get the previous undo operation and change the undo pointer
     *
     * @return The undo transaction to be performed.
     */
    public TransAction previousUndo();

    /**
     * View current undo, don't change undo position
     *
     * @return The current undo transaction
     */
    public TransAction viewThisUndo();

    /**
     * View previous undo, don't change undo position
     *
     * @return The previous undo transaction
     */
    public TransAction viewPreviousUndo();
    
    /**
     * Get the next undo transaction on the list. Change the undo pointer.
     *
     * @return The next undo transaction (for redo)
     */
    public TransAction nextUndo();
    
    /**
     * Get the next undo transaction on the list.
     *
     * @return The next undo transaction (for redo)
     */
    public TransAction viewNextUndo();

}
