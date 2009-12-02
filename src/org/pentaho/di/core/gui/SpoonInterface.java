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

import org.pentaho.di.trans.TransMeta;

public interface SpoonInterface extends OverwritePrompter {

    public static final int STATE_CORE_OBJECTS_NONE     = 1;   // No core objects
    public static final int STATE_CORE_OBJECTS_CHEF     = 2;   // Chef state: job entries
    public static final int STATE_CORE_OBJECTS_SPOON    = 3;   // Spoon state: steps

	public static final String XUL_FILE_MENUBAR = "ui/menubar.xul";

	public static final String XUL_FILE_MENUS = "ui/menus.xul";

    public boolean addSpoonBrowser(String name, String urlString);

    public void addTransGraph(TransMeta transMeta);

    public Object[] messageDialogWithToggle( String dialogTitle, Object image, String message, int dialogImageType, String buttonLabels[], int defaultIndex, String toggleMessage, boolean toggleState );

    public boolean messageBox( String message, String text, boolean allowCancel, int type );
    
    public Object getSelectionObject();
}
