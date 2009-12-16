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
