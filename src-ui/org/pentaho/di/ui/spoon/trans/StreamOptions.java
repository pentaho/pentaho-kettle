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
package org.pentaho.di.ui.spoon.trans;

import java.util.List;

import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public class StreamOptions {
	private List<StreamInterface> options;
	private Point	location;
	
	/**
	 * @param options
	 * @param location 
	 */
	public StreamOptions(List<StreamInterface> options, Point location) {
		this.options = options;
		this.location = location;
	}

	/**
	 * @return the options
	 */
	public List<StreamInterface> getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(List<StreamInterface> options) {
		this.options = options;
	}

	/**
	 * @return the location
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Point location) {
		this.location = location;
	}
	
}
