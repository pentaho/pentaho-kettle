package org.pentaho.di.trans.steps.autodoc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.ScrollBarInterface;
import org.pentaho.di.core.gui.SwingGC;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPainter;
import org.pentaho.di.trans.step.StepMeta;

public class TransformationInformation {

	private static TransformationInformation transInfo;
	
	private Repository repository;
	
	public static final TransformationInformation getInstance() {
		if (transInfo==null) {
			throw new RuntimeException("The TransformationInformation singleton was not initialized!");
		}
		return transInfo;
	}
	
	public static final void init(Repository repository) {
		transInfo = new TransformationInformation();
		transInfo.repository = repository;
	}
	
	private class TransformationInformationValues {
		public BufferedImage image;
		public TransMeta transMeta;
	}

	private Map<ReportSubjectLocation, TransformationInformationValues>	map;
	
	private TransformationInformation() {
		this.map = new HashMap<ReportSubjectLocation, TransformationInformationValues>();
	}
	
	public BufferedImage getImage(ReportSubjectLocation location) throws KettleException {
		return getValues(location).image;
	}

	public TransMeta getTransMeta(ReportSubjectLocation location) throws KettleException {
		return getValues(location).transMeta;
	}

	private TransformationInformationValues getValues(ReportSubjectLocation location) throws KettleException {
		TransformationInformationValues values = map.get(location);
		if (values==null) {
			values = loadValues(location);
			
			// Dump the other values, keep things nice & tidy.
			//
			map.clear();
			map.put(location, values);
		}
		return values;
	}
	
	private TransMeta loadTransformation(ReportSubjectLocation location) throws KettleException {
		TransMeta transMeta;
		if (!Const.isEmpty(location.getFilename())) {
			transMeta = new TransMeta(location.getFilename());
		} else {
			transMeta = repository.loadTransformation(location.getName(), location.getDirectory(), null, true, null);
		}
		return transMeta;
	}
	
	private TransformationInformationValues loadValues(ReportSubjectLocation location) throws KettleException {
		
		// Load the transformation
		//
		TransMeta transMeta = loadTransformation(location);
		
		Point min = transMeta.getMinimum();
		Point area = transMeta.getMaximum();
		area.x+=100;
		area.y+=100;
		int iconsize = 32;
		
		ScrollBarInterface bar = new ScrollBarInterface() {
			public void setThumb(int thumb) {}
			public int getSelection() { return 0; }
		};
		
		// Paint the transformation...
		//
		GCInterface gc = new SwingGC(null, area, iconsize, 50, 20);
		TransPainter painter = new TransPainter(gc, transMeta, area, bar, bar, null, null, null, new ArrayList<AreaOwner>(), new ArrayList<StepMeta>(), 
		    iconsize, 1, 0, 0, true, "FreeSans", 10);
	    painter.buildTransformationImage();
	    BufferedImage bufferedImage = (BufferedImage)gc.getImage();
	    int newWidth=bufferedImage.getWidth()-min.x-50;
	    int newHeigth=bufferedImage.getHeight()-min.y-50;
	    BufferedImage image = new BufferedImage(newWidth, newHeigth, bufferedImage.getType());
	    image.getGraphics().drawImage(
	    		bufferedImage, 
	    		0, 0, newWidth, newHeigth, 
	    		min.x, min.y, min.x+newWidth, min.y+newHeigth, 
	    		null
	    	);

	    TransformationInformationValues values = new TransformationInformationValues();
	    values.transMeta = transMeta;
	    values.image = image;
	    
		return values;
	}
}
