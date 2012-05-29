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

package org.pentaho.amazon.emr.ui;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.eclipse.swt.widgets.Text;
import org.pentaho.amazon.AbstractAmazonJobEntry;
import org.pentaho.amazon.AbstractAmazonJobExecutorController;
import org.pentaho.amazon.emr.job.AmazonElasticMapReduceJobExecutor;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class AmazonElasticMapReduceJobExecutorController extends AbstractAmazonJobExecutorController {

  private static final Class<?> PKG = AmazonElasticMapReduceJobExecutor.class;

  private AmazonElasticMapReduceJobExecutor jobEntry = null;

  @Override
  protected void syncModel() {
    super.syncModel();
    ExtTextbox tempBox = (ExtTextbox) getXulDomContainer().getDocumentRoot().getElementById(XUL_JAR_URL);
    this.jarUrl = ((Text) tempBox.getTextControl()).getText();
  }

  @Override
  protected String buildValidationErrorMessages() {
    String validationErrors = super.buildValidationErrorMessages();
    if (StringUtil.isEmpty(jarUrl)) {
      validationErrors += BaseMessages.getString(PKG, MSG_AMAZON_ELASTIC_MAP_REDUCE_JOB_EXECUTOR_JAR_URL_ERROR) + "\n";
    }
    return validationErrors;
  }

  public void browseJar() throws KettleException, FileSystemException {
    String[] fileFilters = new String[] { "*.jar;*.zip" };
    String[] fileFilterNames = new String[] { "Java Archives (jar)" };

    FileSystemOptions opts = getFileSystemOptions();

    FileObject selectedFile = browse(fileFilters, fileFilterNames, getVariableSpace().environmentSubstitute(jarUrl), opts, VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
    if (selectedFile != null) {
      setJarUrl(selectedFile.getName().getURI());
    }
  }

  @Override
  public <T extends AbstractAmazonJobEntry> T getJobEntry() {
    return (T)jobEntry;
  }

  @Override
  public <T extends AbstractAmazonJobEntry> void setJobEntry(T jobEntry) {
    this.jobEntry = (AmazonElasticMapReduceJobExecutor)jobEntry;
  }
}