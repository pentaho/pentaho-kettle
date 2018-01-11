package org.pentaho.di.ui.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

public class FileSpool {
	  private String targetFile;

	  public FileSpool() {
		  targetFile = "image.png";
	  }


	  public String getTargetFile(Shell sh, TransMeta transMeta ) {
		FileDialog fd = new FileDialog(sh, SWT.SAVE);  
		fd.setFilterNames(new String[] { "PNG Files"});
		fd.setFilterExtensions(new String[] { "*.png"}); 
		fd.setFilterPath(transMeta.getFilename().replaceAll("\\\\","/").substring(0, transMeta.getFilename().replaceAll("\\\\","/").lastIndexOf("/"))); 
		fd.setFileName(transMeta.getName() + ".png");
		
		targetFile = fd.open();

	    return targetFile;
	  }
	  
	  public String getTargetFile(Shell sh, JobMeta JobMeta ) {
		FileDialog fd = new FileDialog(sh, SWT.SAVE);  
		fd.setFilterNames(new String[] { "PNG Files"});
		fd.setFilterExtensions(new String[] { "*.png"}); 
		fd.setFilterPath(JobMeta.getFilename().replaceAll("\\\\","/").substring(0, JobMeta.getFilename().replaceAll("\\\\","/").lastIndexOf("/"))); 
		fd.setFileName(JobMeta.getName() + ".png");
		
		targetFile = fd.open();

	    return targetFile;
	  }
}
