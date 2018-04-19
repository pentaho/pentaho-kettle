package org.pentaho.di.ui.core.dialog.geopreview.canvas;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import java.util.Observable;
import java.util.Observer;

/**
 * Extends canvas and act also as an observer on MapController
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class GeoCanvas extends Canvas implements Observer
{
	public GeoCanvas(Composite parent, int style){
		super(parent, style);
	}

	public void update(Observable observable, Object o){
		this.redraw();
	}
}
