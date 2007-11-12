/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
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
