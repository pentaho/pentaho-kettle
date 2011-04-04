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
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.JobPainter;

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
		painter.drawJob();
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

	    JobInformationValues values = new JobInformationValues();
	    values.jobMeta = jobMeta;
	    values.image = image;
	    
		return values;
	}
}
