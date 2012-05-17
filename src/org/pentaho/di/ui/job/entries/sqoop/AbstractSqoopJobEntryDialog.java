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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Base functionality for a XUL-based Sqoop job entry dialog
 */
public abstract class AbstractSqoopJobEntryDialog extends JobEntryDialog implements JobEntryDialogInterface {

  protected ResourceBundle bundle = new ResourceBundle() {
    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(getMessagesClass(), key);
    }

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }
  };

  private XulDomContainer container;
  private AbstractSqoopJobEntryController controller;

  protected AbstractSqoopJobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) throws XulException {
    super(parent, jobEntry, rep, jobMeta);
    init(AbstractSqoopJobEntry.class.cast(jobEntry));
  }

  /**
   * @return the name of the class to use to look up localized messages
   */
  protected abstract Class<?> getMessagesClass();

  /**
   * @return the file name for the XUL document to load for this dialog
   */
  protected abstract String getXulFile();

  /**
   * Create the controller for this dialog
   *
   * @param container      XUL DOM container loaded from the file path returned by {@link #getXulFile()}
   * @param jobEntry       Job entry this dialog supports
   * @param bindingFactory Binding factory to create bindings with
   * @return Controller capable of handling requests for this dialog
   */
  protected abstract AbstractSqoopJobEntryController createController(XulDomContainer container, AbstractSqoopJobEntry jobEntry, BindingFactory bindingFactory);

  /**
   * Initialize this dialog for the job entry instance provided.
   *
   * @param jobEntry The job entry this dialog supports.
   */
  protected void init(AbstractSqoopJobEntry jobEntry) throws XulException {
    SwtXulLoader swtXulLoader = new SwtXulLoader();
    // Register the settings manager so dialog position and size is restored
    swtXulLoader.setSettingsManager(XulSpoonSettingsManager.getInstance());
    swtXulLoader.registerClassLoader(getClass().getClassLoader());
    // Register Kettle's variable text box so we can reference it from XUL
    swtXulLoader.register("VARIABLETEXTBOX", ExtTextbox.class.getName());
    swtXulLoader.setOuterContext(shell);

    // Load the XUL document with the dialog defined in it
    container = swtXulLoader.loadXul(getXulFile(), bundle);

    // Create the controller with a default binding factory for the document we just loaded
    BindingFactory bf = new DefaultBindingFactory();
    bf.setDocument(container.getDocumentRoot());
    controller = createController(container, jobEntry, bf);
    container.addEventHandler(controller);

    // Load up the SWT-XUL runtime and initialize it with our container
    final XulRunner runner = new SwtXulRunner();
    runner.addContainer(container);
    runner.initialize();
  }

  @Override
  public JobEntryInterface open() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById(controller.getDialogElementId());
    dialog.show();
    return jobEntryInt;
  }
}
