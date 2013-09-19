/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
