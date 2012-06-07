/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.ui.job.entries.sqoop;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entries.sqoop.SqoopExportConfig;
import org.pentaho.di.job.entries.sqoop.SqoopExportJobEntry;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;

/**
 * Dialog for the Sqoop Export job entry.
 *
 * @see org.pentaho.di.job.entries.sqoop.SqoopExportJobEntry
 */
public class SqoopExportJobEntryDialog extends AbstractSqoopJobEntryDialog<SqoopExportConfig> {

  public SqoopExportJobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) throws XulException, InvocationTargetException {
    super(parent, jobEntry, rep, jobMeta);
  }

  @Override
  protected String getXulFile() {
    return "org/pentaho/di/ui/job/entries/sqoop/xul/SqoopExportJobEntry.xul";
  }

  @Override
  protected Class<?> getMessagesClass() {
    return SqoopExportJobEntry.class;
  }

  @Override
  protected AbstractSqoopJobEntryController<SqoopExportConfig> createController(XulDomContainer container, AbstractSqoopJobEntry<SqoopExportConfig> jobEntry, BindingFactory bindingFactory) {
    return new SqoopExportJobEntryController(jobMeta, container, jobEntry, bindingFactory);
  }
}
