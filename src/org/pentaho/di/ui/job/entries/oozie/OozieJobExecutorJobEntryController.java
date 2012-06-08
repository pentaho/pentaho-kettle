/*
 * ******************************************************************************
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package org.pentaho.di.ui.job.entries.oozie;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.AbstractJobEntry;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.BlockableJobConfig;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorConfig;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorJobEntry;
import org.pentaho.di.ui.job.AbstractJobEntryController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.util.Collection;

/**
 * User: RFellows
 * Date: 6/4/12
 */
public class OozieJobExecutorJobEntryController extends AbstractJobEntryController<OozieJobExecutorConfig, OozieJobExecutorJobEntry> {

  public static final String OOZIE_JOB_EXECUTOR = "oozie-job-executor";
  private static final String VALUE = "value";
  public static final String ERROR_BROWSING_DIRECTORY = "ErrorBrowsingDirectory";
  public static final String FILE_FILTER_NAMES_PROPERTIES = "FileFilterNames.Properties";

  public OozieJobExecutorJobEntryController(JobMeta jobMeta, XulDomContainer container, OozieJobExecutorJobEntry jobEntry, BindingFactory bindingFactory) {
    super(jobMeta, container, jobEntry, bindingFactory);
  }

  @Override
  protected void syncModel() {
    // no custom model syncing needed, bindings are enough
  }

  @Override
  protected void createBindings(OozieJobExecutorConfig config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings) {
    bindingFactory.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bindings.add(bindingFactory.createBinding(config, BlockableJobConfig.JOB_ENTRY_NAME, BlockableJobConfig.JOB_ENTRY_NAME, VALUE));
    bindings.add(bindingFactory.createBinding(config, OozieJobExecutorConfig.OOZIE_URL, OozieJobExecutorConfig.OOZIE_URL, VALUE));
    bindings.add(bindingFactory.createBinding(config, OozieJobExecutorConfig.OOZIE_WORKFLOW_CONFIG, OozieJobExecutorConfig.OOZIE_WORKFLOW_CONFIG, VALUE));
  }

  @Override
  protected String getDialogElementId() {
    return OOZIE_JOB_EXECUTOR;
  }

  /**
   * Make sure everything required is entered and valid
   */
  @Bindable
  public void test() {
    // verify the oozie url is valid
    // verify there is a properties file selected and it resolves
    // verify that the properties file defines, at minimum the prop OozieClient.APP_PATH (oozie.wf.application.path)
    // verify there is a job name?
  }

  /**
   * Open the VFS file browser to allow for selection of the workflow job properties configuration file.
   */
  @Bindable
  public void browseWorkflowConfig() {
    FileObject path = null;
    try {
      path = KettleVFS.getFileObject(getConfig().getOozieWorkflowConfig());
    } catch (Exception e) {
      // Ignore, use null (default VFS browse path)
    }
    try {
      FileObject exportDir = browseVfs(null, path, VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY, null, "file", true);
      if (exportDir != null) {
        getConfig().setOozieWorkflowConfig(exportDir.getName().getURI());
      }
    } catch (KettleFileException e) {
      getJobEntry().logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, ERROR_BROWSING_DIRECTORY), e);
    }
  }

  @Override
  protected String[] getFileFilters() {
    return new String[] {"*.properties"};
  }

  @Override
  protected String[] getFileFilterNames() {
    return new String[] {BaseMessages.getString(OozieJobExecutorJobEntry.class, FILE_FILTER_NAMES_PROPERTIES)};
  }
}
