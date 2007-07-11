package org.pentaho.di.ui.job.entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Props;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;

public class JobEntryDialog extends Dialog {

	protected JobEntryInterface jobEntryInt;
	protected Repository rep;
	protected JobMeta jobMeta;
	protected Props props;
    protected Shell parent;
	
    public JobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta)
    {
        super(parent, SWT.NONE);
        props = Props.getInstance();

        this.jobEntryInt = jobEntry;
        this.rep = rep;
        this.jobMeta = jobMeta;
    }

	
}
