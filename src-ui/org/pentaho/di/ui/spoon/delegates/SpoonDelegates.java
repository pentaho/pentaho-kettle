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
