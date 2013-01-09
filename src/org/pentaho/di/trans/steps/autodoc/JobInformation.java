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
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPainter;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;

public class JobInformation {

	private static JobInformation jobInfo;
	
	private Repository repository;

	public static final JobInformation getInstance() {
		if (jobInfo==null) {
			throw new RuntimeException("The JobInformation singleton was not initialized!");
		}
		return jobInfo;
	}
	
	public static final void init(Repository repository) {
		jobInfo = new JobInformation();
		jobInfo.repository = repository;
	}
	
	private class JobInformationValues {
		public BufferedImage image;
		public JobMeta jobMeta;
	}

	private Map<ReportSubjectLocation, JobInformationValues>	map;
	
	private JobInformation() {
		this.map = new HashMap<ReportSubjectLocation, JobInformationValues>();
	}
	
	public BufferedImage getImage(ReportSubjectLocation location) throws KettleException {
		return getValues(location).image;
	}

	public JobMeta getJobMeta(ReportSubjectLocation location) throws KettleException {
		return getValues(location).jobMeta;
	}

	private JobInformationValues getValues(ReportSubjectLocation location) throws KettleException {
		JobInformationValues values = map.get(location);
		if (values==null) {
			values = loadValues(location);
			map.put(location, values);
		}
		return values;
	}
	
	private JobMeta loadJob(ReportSubjectLocation location) throws KettleException {
		JobMeta jobMeta;
		if (!Const.isEmpty(location.getFilename())) {
			jobMeta = new JobMeta(location.getFilename(), repository);
		} else {
			jobMeta = repository.loadJob(location.getName(), location.getDirectory(), null, null);
		}
		return jobMeta;
	}

	private JobInformationValues loadValues(ReportSubjectLocation location) throws KettleException {
		
		// Load the job
		//
		JobMeta jobMeta = loadJob(location);
		
		Point min = jobMeta.getMinimum();
		Point area = jobMeta.getMaximum();
		area.x+=30;
		area.y+=30;
		int iconsize = 32;
		
		ScrollBarInterface bar = new ScrollBarInterface() {
			public void setThumb(int thumb) {}
			public int getSelection() { return 0; }
		};
		
		// Paint the transformation...
		//
		GCInterface gc = new SwingGC(null, area, iconsize, 50, 20);
		JobPainter painter = new JobPainter(gc, jobMeta, area, bar, bar, null, null, null, new ArrayList<AreaOwner>(), new ArrayList<JobEntryCopy>(), iconsize, 1, 0, 0, true, "FreeSans", 10);
		painter.setMagnification(0.25f);
		painter.drawJob();
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

	    JobInformationValues values = new JobInformationValues();
	    values.jobMeta = jobMeta;
	    values.image = image;
	    
		return values;
	}

  public void drawImage(final Graphics2D g2d, final Rectangle2D rectangle2d, ReportSubjectLocation location, boolean pixelateImages) throws KettleException {
    
    // Load the job
    //
    JobMeta jobMeta = loadJob(location);
    
    Point min = jobMeta.getMinimum();
    Point area = jobMeta.getMaximum();
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
    JobPainter painter = new JobPainter(gc, jobMeta, area, bar, bar, null, null, null, new ArrayList<AreaOwner>(), new ArrayList<JobEntryCopy>(), iconsize, 1, 0, 0, true, "FreeSans", 10);
    painter.setMagnification((float)Math.min(magnification, 1));
    if (pixelateImages) {
      painter.setTranslationX(100+min.x);
      painter.setTranslationY(100+min.y);
    }
    painter.drawJob();
  }

}
