/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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


/**
 * Classes implementing this interface have a chance to manage their internal representation states
 * using the options dialog in Kettle.
 * 
 * Instances of this class are automatically added to the EnterOptionsDialog.
 * @author Alex Silva
 *
 */
public interface GUIOption<E> 
{
	/**
	 * How the GUI should display the preference represented by this class.
	 * @author Alex Silva
	 *
	 */
	enum DisplayType {CHECK_BOX,TEXT_FIELD,ACTION_BUTTON};
	
	public E getLastValue();
	
	/**
	 * Sets the value; should also persist it.
	 * @param value
	 */
	public void setValue(E value);
	
	public DisplayType getType();
	
	String getLabelText();
	
	
}
