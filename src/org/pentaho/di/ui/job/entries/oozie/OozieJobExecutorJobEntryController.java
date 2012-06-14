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
import org.pentaho.di.job.BlockableJobConfig;
import org.pentaho.di.job.JobEntryUtils;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorConfig;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorJobEntry;
import org.pentaho.di.ui.job.AbstractJobEntryController;
import org.pentaho.di.ui.job.JobEntryMode;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.swt.tags.SwtCheckbox;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.util.Collection;
import java.util.List;

/**
 * User: RFellows
 * Date: 6/4/12
 */
public class OozieJobExecutorJobEntryController extends AbstractJobEntryController<OozieJobExecutorConfig, OozieJobExecutorJobEntry> {

  public static final String OOZIE_JOB_EXECUTOR = "oozie-job-executor";
  private static final String VALUE = "value";
  public static final String ERROR_BROWSING_DIRECTORY = "ErrorBrowsingDirectory";
  public static final String FILE_FILTER_NAMES_PROPERTIES = "FileFilterNames.Properties";
  public static final String MODE_TOGGLE_LABEL = "mode-toggle-label";

  /**
   * The text for the Quick Setup/Advanced Options mode toggle (label)
   */
  private String modeToggleLabel;

  private SwtCheckbox blockingEnabledCheck = null;


  public OozieJobExecutorJobEntryController(JobMeta jobMeta, XulDomContainer container, OozieJobExecutorJobEntry jobEntry, BindingFactory bindingFactory) {
    super(jobMeta, container, jobEntry, bindingFactory);
  }

  @Override
  protected void beforeInit() {
    setModeToggleLabel(JobEntryMode.ADVANCED);
    customizeModeToggleLabel(getModeToggleLabelElementId());
    blockingEnabledCheck = (SwtCheckbox)container.getDocumentRoot().getElementById("blockingExecution");
  }

  @Override
  protected void syncModel() {
    // no custom model syncing needed, bindings are enough

    // except SWTCheckbox, it does not participate in xul binding as ov v 3.3
    blockingEnabledCheck.setChecked(JobEntryUtils.asBoolean(config.getBlockingExecution(), getJobEntry().getVariableSpace()));
  }

  @Override
  protected void createBindings(OozieJobExecutorConfig config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings) {
    bindingFactory.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bindings.add(bindingFactory.createBinding(config, BlockableJobConfig.JOB_ENTRY_NAME, BlockableJobConfig.JOB_ENTRY_NAME, VALUE));
    bindings.add(bindingFactory.createBinding(config, OozieJobExecutorConfig.OOZIE_URL, OozieJobExecutorConfig.OOZIE_URL, VALUE));
    bindings.add(bindingFactory.createBinding(config, OozieJobExecutorConfig.OOZIE_WORKFLOW_CONFIG, OozieJobExecutorConfig.OOZIE_WORKFLOW_CONFIG, VALUE));

    /////////////////////////////////////////////////////////////////////
    // Bindings to checkboxes aren't fully implemented in SWTCheckbox (v 3.3).
    // the act of clicking doesn't fire any event...
    // So, on accept of the dialog we have to get the state of the checkbox
    // and set that value in the config object.
    /////////////////////////////////////////////////////////////////////
//    bindings.add(bindingFactory.createBinding("blockingExecution", "checked", config, BlockableJobConfig.BLOCKING_EXECUTION, BindingConvertor.boolean2String()));

    bindingFactory.setBindingType(Binding.Type.ONE_WAY);
    bindings.add(bindingFactory.createBinding(this, "modeToggleLabel", getModeToggleLabelElementId(), VALUE));

  }

  @Override
  protected String getDialogElementId() {
    return OOZIE_JOB_EXECUTOR;
  }

  /**
   * @return the id of the element responsible for toggling between "Quick Setup" and "Advanced Options" modes
   */
  @Bindable
  public String getModeToggleLabelElementId() {
    return MODE_TOGGLE_LABEL;
  }

  @Bindable
  public String getModeToggleLabel() {
    return modeToggleLabel;
  }

  @Bindable
  public void setModeToggleLabel(String modeToggleLabel) {
    String prev = this.modeToggleLabel;
    this.modeToggleLabel = modeToggleLabel;
    firePropertyChange("modeToggleLabel", prev, modeToggleLabel);
  }

  @Override
  protected void setModeToggleLabel(JobEntryMode mode) {
    switch (mode) {
      case ADVANCED:
        setModeToggleLabel(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.AdvancedOptions.Button.Text"));
        break;
      default:
        setModeToggleLabel(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.BasicOptions.Button.Text"));
        break;
    }
  }

  /**
   * Make sure everything required is entered and valid
   */
  @Bindable
  public void testSettings() {
    List<String> warnings = jobEntry.getValidationWarnings(getConfig());
    if (!warnings.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String warning : warnings) {
        sb.append(warning).append("\n");
      }
      showErrorDialog(
          BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationError.Dialog.Title"),
          sb.toString());
      return;
    }
    showInfoDialog(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Info.Dialog.Title"),
        BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMsg.OK"));
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


  /**
   * Accept and apply the changes made in the dialog. Also, close the dialog
   */
  @Override
  @Bindable
  public void accept() {
    // Bindings to checkboxes aren't fully implemented in SWTCheckbox (as of v3.3 of xul). the act of clicking doesn't fire any event.
    config.setBlockingExecution(Boolean.toString(blockingEnabledCheck.isChecked()));

    jobEntry.setJobConfig(config);
    jobEntry.setChanged();
    cancel();
  }

}
