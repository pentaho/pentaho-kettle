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
package org.pentaho.di.ui.spoon.delegates;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.InstanceCreationException;
import org.pentaho.di.ui.spoon.Spoon;

public class SpoonDelegates
{
	public SpoonJobDelegate jobs;

	public SpoonTabsDelegate tabs;

	public SpoonTransformationDelegate trans;

	public SpoonSlaveDelegate slaves;

	public SpoonTreeDelegate tree;

	public SpoonStepsDelegate steps;

	public SpoonDBDelegate db;

	public SpoonDelegates(Spoon spoon)
	{
		tabs = new SpoonTabsDelegate(spoon);
		tree = new SpoonTreeDelegate(spoon);
		slaves = new SpoonSlaveDelegate(spoon);
		steps = new SpoonStepsDelegate(spoon);
		db = new SpoonDBDelegate(spoon);
		update(spoon);
	}

	public void update(Spoon spoon) {
	  SpoonJobDelegate origJobs = jobs;
    try {
      jobs = (SpoonJobDelegate)SpoonDelegateRegistry.getInstance().constructSpoonJobDelegate(spoon);
    } catch (InstanceCreationException e) {
      jobs = new SpoonJobDelegate(spoon);
    }
    if (origJobs != null) {
      // preserve open jobs
      for (JobMeta jobMeta : origJobs.getLoadedJobs()) {
        jobs.addJob(jobMeta);
      }
    }
    SpoonTransformationDelegate origTrans = trans;
    try {
      trans = (SpoonTransformationDelegate)SpoonDelegateRegistry.getInstance().constructSpoonTransDelegate(spoon);
    } catch (InstanceCreationException e) {
      trans = new SpoonTransformationDelegate(spoon);
    }  
    if (origTrans != null) {
      // preseve open trans
      for (TransMeta transMeta : origTrans.getLoadedTransformations()) {
        trans.addTransformation(transMeta);
      }
    }
	}
}
