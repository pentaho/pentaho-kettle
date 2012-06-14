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

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.hadoop.HadoopSpoonPlugin;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entries.sqoop.SqoopImportConfig;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.util.Collection;

import static org.pentaho.di.job.entries.sqoop.SqoopImportConfig.TARGET_DIR;

/**
 * Controller for the Sqoop Import Dialog.
 */
public class SqoopImportJobEntryController extends AbstractSqoopJobEntryController<SqoopImportConfig> {

  public SqoopImportJobEntryController(JobMeta jobMeta, XulDomContainer container, AbstractSqoopJobEntry<SqoopImportConfig> sqoopJobEntry, BindingFactory bindingFactory) {
    super(jobMeta, container, sqoopJobEntry, bindingFactory);
  }

  @Override
  public String getDialogElementId() {
    return "sqoop-import";
  }

  @Override
  protected void createBindings(SqoopImportConfig config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings) {
    super.createBindings(config, container, bindingFactory, bindings);

    bindings.add(bindingFactory.createBinding(config, TARGET_DIR, TARGET_DIR, "value"));
  }

  public void browseForTargetDirectory() {
    FileObject path = null;
    try {
      // TODO Build proper URL for path
//      path = resolveFile(getConfig().getExportDir());
    } catch (Exception e) {
      // Ignore, use null (default VFS browse path)
    }
    try {
      FileObject targetDir = browseVfs(null, path, VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY, HadoopSpoonPlugin.HDFS_SCHEME, HadoopSpoonPlugin.HDFS_SCHEME, false);
      if (targetDir != null) {
        getConfig().setTargetDir(targetDir.getName().getPath());
      }
    } catch (KettleFileException e) {
      getJobEntry().logError(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorBrowsingDirectory"), e);
    }
  }
}
