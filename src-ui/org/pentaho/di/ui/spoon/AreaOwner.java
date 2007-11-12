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
package org.pentaho.di.ui.spoon;

import org.eclipse.swt.graphics.Rectangle;

/**
 * When we draw something in Spoon (TransPainter) we keep a list of all the
 * things we draw and the object that's behind it. That should make it a lot
 * easier to track what was drawn, setting tooltips, etc.
 * 
 * @author Matt
 * 
 */
public class AreaOwner {

	private Rectangle area;
	private Object parent;
	private Object owner;

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param heigth
	 * @param owner
	 */
	public AreaOwner(int x, int y, int width, int heigth, Object parent, Object owner) {
		super();
		this.area = new Rectangle(x, y, width, heigth);
		this.parent = parent;
		this.owner = owner;
	}
	
	/**
	 * Validate if a certain coordinate is contained in the area
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @return true if the specified coordinate is contained in the area
	 */
	public boolean contains(int x, int y) {
		return area.contains(x, y);
	}

	/**
	 * @return the area
	 */
	public Rectangle getArea() {
		return area;
	}

	/**
	 * @param area the area to set
	 */
	public void setArea(Rectangle area) {
		this.area = area;
	}

	/**
	 * @return the owner
	 */
	public Object getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(Object owner) {
		this.owner = owner;
	}

	/**
	 * @return the parent
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Object parent) {
		this.parent = parent;
	}

	

}
