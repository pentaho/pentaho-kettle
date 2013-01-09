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

package org.pentaho.di.trans.steps.autodoc;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
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
    painter.setMagnification(0.5f);
    painter.setTranslationX(min.x);
    painter.setTranslationX(min.y);
	  painter.buildTransformationImage();
	  BufferedImage bufferedImage = (BufferedImage)gc.getImage();
	  int newWidth=bufferedImage.getWidth()-min.x;
	  int newHeigth=bufferedImage.getHeight()-min.y;
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
	
	 public void drawImage(final Graphics2D g2d, final Rectangle2D rectangle2d, ReportSubjectLocation location, boolean pixelateImages) throws KettleException {
	    
	    // Load the transformation
	    //
	    TransMeta transMeta = loadTransformation(location);
	    
	    Point min = transMeta.getMinimum();
	    Point area = transMeta.getMaximum();
	    int iconsize = 32;
	    
	    ScrollBarInterface bar = new ScrollBarInterface() {
	      public void setThumb(int thumb) {}
	      public int getSelection() { return 0; }
	    };
	    
	    // Paint the transformation...
	    //
	    Rectangle rect = new java.awt.Rectangle(0,0,area.x, area.y);
	    double magnificationX = rectangle2d.getWidth()/rect.getWidth();
	    double magnificationY = rectangle2d.getHeight()/rect.getHeight();
	    double magnification = Math.min(magnificationX, magnificationY);

	    SwingGC gc = new SwingGC(g2d, rect, iconsize, 0, 0);
	    gc.setDrawingPixelatedImages(pixelateImages);
	    
	    TransPainter painter = new TransPainter(gc, transMeta, area, bar, bar, null, null, null, new ArrayList<AreaOwner>(), new ArrayList<StepMeta>(), 
	        iconsize, 1, 0, 0, true, "FreeSans", 10);
	    painter.setMagnification((float)Math.min(magnification, 1));
	    if (pixelateImages) {
  	    painter.setTranslationX(100+min.x);
        painter.setTranslationY(100+min.y);
	    }
	    painter.buildTransformationImage();
	  }

}
