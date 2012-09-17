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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.BlockableJobConfig;
import org.pentaho.di.job.JobEntryMode;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.PropertyEntry;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorConfig;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorJobEntry;
import org.pentaho.di.ui.job.AbstractJobEntryController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

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
  public static final String ADVANCED_TABLE = "advanced-table";
  public static final String CHILDREN = "children";
  public static final String ELEMENTS = "elements";

  protected AbstractModelList<PropertyEntry> advancedArguments;
  private transient boolean advancedArgumentsChanged = false;
  protected XulTree variablesTree = null;

  /**
   * The text for the Quick Setup/Advanced Options mode toggle (label)
   */
  private String modeToggleLabel;

  public OozieJobExecutorJobEntryController(JobMeta jobMeta, XulDomContainer container, OozieJobExecutorJobEntry jobEntry, BindingFactory bindingFactory) {
    super(jobMeta, container, jobEntry, bindingFactory);
    advancedArguments = new AbstractModelList<PropertyEntry>();

    if(jobEntry.getJobConfig().getWorkflowProperties().size() > 0) {
      advancedArguments.addAll(jobEntry.getJobConfig().getWorkflowProperties());
    }

  }

  @Override
  protected void beforeInit() {
    setMode(jobEntry.getJobConfig().getModeAsEnum());
    variablesTree = (XulTree) container.getDocumentRoot().getElementById(ADVANCED_TABLE);
  }

  @Override
  protected void syncModel() {

    if(!shouldUseAdvancedProperties()) {
      // sync properties to advanced args
      advancedArguments.clear();
      if(config.getWorkflowProperties() != null) {
        config.getWorkflowProperties().clear();
      }
    } else {
      if(advancedArguments.size() == 0 && !StringUtil.isEmpty(config.getOozieWorkflowConfig())) {
        preFillAdvancedArgs();
      }
      // advanced mode was used to modify/create properties
      // save the args out...
      ArrayList<PropertyEntry> m = new ArrayList<PropertyEntry>(advancedArguments);
      config.setWorkflowProperties(m);
    }

    config.setMode(jobEntryMode);
  }

  private void preFillAdvancedArgs() {
    try {
      if(jobEntry != null && config != null) {
        Properties props = jobEntry.getProperties(config);
        for (Map.Entry<Object, Object> prop : props.entrySet()) {
          if(prop.getKey() instanceof String && prop.getValue() instanceof String) {
            PropertyEntry pEntry = new PropertyEntry((prop.getKey()).toString(), prop.getValue().toString());
            advancedArguments.add(pEntry);
          }
        }
      }
    } catch (Exception e) {
      // could not read in the props...
    }
  }

  /**
   * Determines if the advanced properties should be used instead of the quick-setup defined workflow properties file
   * @return
   */
  protected boolean shouldUseAdvancedProperties() {
    return jobEntryMode == JobEntryMode.ADVANCED_LIST;
  }

  /**
   * make this available for unit testing
   * @param mode
   */
  protected void setJobEntryMode(JobEntryMode mode) {
    this.jobEntryMode = mode;
  }

  @Override
  protected void createBindings(OozieJobExecutorConfig config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings) {
    bindingFactory.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bindings.add(bindingFactory.createBinding(config, BlockableJobConfig.JOB_ENTRY_NAME, BlockableJobConfig.JOB_ENTRY_NAME, VALUE));
    bindings.add(bindingFactory.createBinding(config, OozieJobExecutorConfig.OOZIE_URL, OozieJobExecutorConfig.OOZIE_URL, VALUE));
    bindings.add(bindingFactory.createBinding(config, OozieJobExecutorConfig.OOZIE_WORKFLOW_CONFIG, OozieJobExecutorConfig.OOZIE_WORKFLOW_CONFIG, VALUE));

    bindings.add(bindingFactory.createBinding(config, BlockableJobConfig.BLOCKING_POLLING_INTERVAL, BlockableJobConfig.BLOCKING_POLLING_INTERVAL, VALUE));

    BindingConvertor<String, Boolean> string2BooleanConvertor = new BindingConvertor<String, Boolean>() {
      @Override
      public String targetToSource(Boolean aBoolean) {
        String val = aBoolean.toString();
        return val;
      }

      @Override
      public Boolean sourceToTarget(String s) {
        Boolean val = Boolean.valueOf(s);
        return val;
      }
    };
    bindings.add(bindingFactory.createBinding(config, BlockableJobConfig.BLOCKING_EXECUTION, BlockableJobConfig.BLOCKING_EXECUTION, "checked", string2BooleanConvertor));

    bindingFactory.setBindingType(Binding.Type.ONE_WAY);
    bindings.add(bindingFactory.createBinding(this, "modeToggleLabel", getModeToggleLabelElementId(), VALUE));

    // only enable the polling interval text box is blocking is checked
    bindings.add(bindingFactory.createBinding(config, BlockableJobConfig.BLOCKING_EXECUTION, BlockableJobConfig.BLOCKING_POLLING_INTERVAL, "!disabled", string2BooleanConvertor));

    BindingConvertor<AbstractModelList<PropertyEntry>, Collection<PropertyEntry>> propsChangedBindingConvertor = new BindingConvertor<AbstractModelList<PropertyEntry>, Collection<PropertyEntry>>() {
      @Override
      public Collection<PropertyEntry> sourceToTarget(AbstractModelList<PropertyEntry> propertyEntries) {
        // user has modified the properties in advanced mode, set the flag...
        advancedArgumentsChanged = true;
        return propertyEntries;
      }
      @Override
      public AbstractModelList<PropertyEntry> targetToSource(Collection<PropertyEntry> propertyEntries) {
        // one-way convertor, don't need this
        return null;
      }
    };

    bindings.add(bindingFactory.createBinding(advancedArguments, CHILDREN, variablesTree, ELEMENTS, propsChangedBindingConvertor));

  }

  @Bindable
  public void addNewProperty() {
    advancedArgumentsChanged = true;
    advancedArguments.add(new PropertyEntry("key", "value"));
  }

  @Bindable
  public void removeProperty() {
    advancedArgumentsChanged = true;
    Collection<PropertyEntry> selected = variablesTree.getSelectedItems();
    for (PropertyEntry pe : selected) {
      try {
        advancedArguments.remove(pe);
      } catch (Exception e) {
        // The implementation of the SwtTree selection model is buggy. if you have an item (row) selected
        // but a field is in edit mode and try to remove the item, we get a failure (sometimes).
        // just set the children manually in this case to make sure we are in sync.
        variablesTree.setElements(advancedArguments);
      }
    }
  }


  /**
   * Accept and apply the changes made in the dialog. Also, close the dialog
   */
  @Override
  @Bindable
  public void accept() {
    syncModel();
    super.accept();
  }
  public AbstractModelList<PropertyEntry> getAdvancedArguments() {
    return advancedArguments;
  }

  public void setAdvancedArguments(AbstractModelList<PropertyEntry> advancedArguments) {
    advancedArgumentsChanged = true;
    this.advancedArguments = advancedArguments;
  }

  @Bindable
  public boolean isAdvancedArgumentsChanged() {
    return advancedArgumentsChanged;
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
      case ADVANCED_LIST:
        setModeToggleLabel(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.AdvancedOptions.Button.Text"));
        break;
      case QUICK_SETUP:
        setModeToggleLabel(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.BasicOptions.Button.Text"));
        break;
      default:
        throw new RuntimeException("unsupported JobEntryMode");
    }
  }

  /**
   * Make sure everything required is entered and valid
   */
  @Bindable
  public void testSettings() {
    syncModel();
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
      path = KettleVFS.getFileObject(jobEntry.getVariableSpace().environmentSubstitute(getConfig().getOozieWorkflowConfig()));
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
