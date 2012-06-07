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

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entries.sqoop.SqoopImportConfig;
import org.pentaho.di.job.entries.sqoop.SqoopImportJobEntry;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;

/**
 * Dialog for the Sqoop Import Job Entry
 *
 * @see org.pentaho.di.job.entries.sqoop.SqoopImportJobEntry
 */
public class SqoopImportJobEntryDialog extends AbstractSqoopJobEntryDialog<SqoopImportConfig> {

  public SqoopImportJobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) throws XulException {
    super(parent, jobEntry, rep, jobMeta);
  }

  @Override
  protected Class<?> getMessagesClass() {
    return SqoopImportJobEntry.class;
  }

  @Override
  protected AbstractSqoopJobEntryController<SqoopImportConfig> createController(XulDomContainer container, AbstractSqoopJobEntry<SqoopImportConfig> jobEntry, BindingFactory bindingFactory) {
    return new SqoopImportJobEntryController(jobMeta, container, jobEntry, bindingFactory);
  }

  @Override
  protected String getXulFile() {
    return "org/pentaho/di/ui/job/entries/sqoop/xul/SqoopImportJobEntry.xul";
  }
}
