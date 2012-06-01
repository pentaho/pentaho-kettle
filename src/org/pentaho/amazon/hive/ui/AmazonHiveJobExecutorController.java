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

package org.pentaho.amazon.hive.ui;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.eclipse.swt.widgets.Text;
import org.pentaho.amazon.AbstractAmazonJobEntry;
import org.pentaho.amazon.AbstractAmazonJobExecutorController;
import org.pentaho.amazon.hive.job.AmazonHiveJobExecutor;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * AmazonHiveJobExecutorController:
 *   Handles the attribute dialog box UI for AmazonHiveJobExecutor class.
 */
public class AmazonHiveJobExecutorController extends AbstractAmazonJobExecutorController {

  private static final Class<?> PKG = AmazonHiveJobExecutor.class;

  // Define string names for the attributes.
  public static final String Q_URL = "qUrl"; //$NON-NLS-1$
  public static final String BOOTSTRAP_ACTIONS = "bootstrapActions"; //$NON-NLS-1$
  public static final String ALIVE = "alive"; //$NON-NLS-1$

  // Attributes
  private String qUrl = "";
  private String bootstrapActions = "";
  private boolean alive = false;

  private AmazonHiveJobExecutor jobEntry;

  @Override
  protected void syncModel() {
    super.syncModel();
    ExtTextbox tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("bootstrap-actions"); //$NON-NLS-1$
    this.bootstrapActions = ((Text) tempBox.getTextControl()).getText();
    tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById("q-url"); //$NON-NLS-1$
    this.qUrl = ((Text) tempBox.getTextControl()).getText();
  }

  @Override
  protected String buildValidationErrorMessages() {
    String validationErrors = super.buildValidationErrorMessages();
    if (StringUtil.isEmpty(qUrl)) {
      validationErrors += BaseMessages.getString(PKG, "AmazonElasticMapReduceJobExecutor.QURL.Error") + "\n"; //$NON-NLS-1$ //$NON-NLS-1$
    }
    return validationErrors;
  }

  @Override
  protected void configureJobEntry() {
    super.configureJobEntry();
    jobEntry.setQUrl(qUrl);
    jobEntry.setBootstrapActions(bootstrapActions);
    jobEntry.setAlive(isAlive());
  }

  /*
  * Initialize attributes.
  */
  public void init() {
    super.init();
    if (jobEntry != null) {
      setQUrl(jobEntry.getQUrl());
      setBootstrapActions(jobEntry.getBootstrapActions());
      setAlive(jobEntry.isAlive());
    }
  }


  /*
   * Open VFS Browser when the "Browse..." button next to the "Hive Script" text box is pressed in the attribute dialog box.
   */
  public void browseQ() throws KettleException, FileSystemException {
    String[] fileFilters = new String[] { "*.*" }; //$NON-NLS-1$
    String[] fileFilterNames = new String[] { "All" }; //$NON-NLS-1$

    FileSystemOptions opts = getFileSystemOptions();

    FileObject selectedFile = browse(fileFilters, fileFilterNames, getVariableSpace().environmentSubstitute(qUrl), opts, VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
    if (selectedFile != null) {
      setQUrl(selectedFile.getName().getURI());
    }
  }


  public String getQUrl() {
    return qUrl;
  }

  public void setQUrl(String qUrl) {
    String previousVal = this.qUrl;
    String newVal = qUrl;

    this.qUrl = qUrl;
    firePropertyChange(AmazonHiveJobExecutorController.Q_URL, previousVal, newVal);
  }

  public String getBootstrapActions() {
    return bootstrapActions;
  }

  public void setBootstrapActions(String bootstrapActions) {
    String previousVal = this.bootstrapActions;
    String newVal = bootstrapActions;

    this.bootstrapActions = bootstrapActions;
    firePropertyChange(AmazonHiveJobExecutorController.BOOTSTRAP_ACTIONS, previousVal, newVal);
  }

  public void invertAlive() {
    setAlive(!isAlive());
  }

  public void invertBlocking() {
    setBlocking(!isBlocking());
  }

  @Override
  public <T extends AbstractAmazonJobEntry> T getJobEntry() {
    return (T)this.jobEntry;
  }

  @Override
  public <T extends AbstractAmazonJobEntry> void setJobEntry(T jobEntry) {
    this.jobEntry = (AmazonHiveJobExecutor)jobEntry;
  }

  public boolean isAlive() {
    return alive;
  }

  public void setAlive(boolean alive) {
    boolean previousVal = this.alive;
    this.alive = alive;
    firePropertyChange(ALIVE, previousVal, alive);
  }

}