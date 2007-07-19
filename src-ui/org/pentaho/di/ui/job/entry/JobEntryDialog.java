package org.pentaho.di.ui.job.entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.PropsUI;

public class JobEntryDialog extends Dialog {

	protected JobEntryInterface jobEntryInt;
	protected Repository rep;
	protected JobMeta jobMeta;
	protected PropsUI props;
    protected Shell parent;
	
    public JobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta)
    {
        super(parent, SWT.NONE);
        props = PropsUI.getInstance();

        this.jobEntryInt = jobEntry;
        this.rep = rep;
        this.jobMeta = jobMeta;
    }

	
}
