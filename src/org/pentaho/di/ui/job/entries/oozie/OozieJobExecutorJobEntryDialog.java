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

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorJobEntry;
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
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * User: RFellows
 * Date: 6/4/12
 */
public class OozieJobExecutorJobEntryDialog extends JobEntryDialog implements JobEntryDialogInterface {

  private static final String OOZIE_JOB_EXECUTOR_XUL = "org/pentaho/di/ui/job/entries/oozie/xul/OozieJobExecutor.xul";
  public static final String VARIABLETEXTBOX = "VARIABLETEXTBOX";

  private OozieJobExecutorJobEntryController controller = null;
  private XulDomContainer container = null;

  public OozieJobExecutorJobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) throws XulException {
    super(parent, jobEntry, rep, jobMeta);
    init(OozieJobExecutorJobEntry.class.cast(jobEntry));
  }

  protected void init(OozieJobExecutorJobEntry jobEntry) throws XulException {
    SwtXulLoader xulLoader = new SwtXulLoader();
    xulLoader.setSettingsManager(XulSpoonSettingsManager.getInstance());
    xulLoader.registerClassLoader(getClass().getClassLoader());

    // register the variable-aware text box for use in XUL
    xulLoader.register(VARIABLETEXTBOX, ExtTextbox.class.getName());
    xulLoader.setOuterContext(shell);

    // Load the XUL document with the dialog defined in it
    container = xulLoader.loadXul(getXulFile(), bundle);

    BindingFactory bf = new DefaultBindingFactory();
    bf.setDocument(container.getDocumentRoot());
    controller = createController(jobEntry, container, bf);
    container.addEventHandler(controller);

    // Load up the SWT-XUL runtime and initialize it with our container
    final XulRunner runner = new SwtXulRunner();
    runner.addContainer(container);
    runner.initialize();
  }

  protected OozieJobExecutorJobEntryController createController(OozieJobExecutorJobEntry jobEntry, XulDomContainer container, BindingFactory bindingFactory) {
    return new OozieJobExecutorJobEntryController(jobMeta, container, jobEntry, bindingFactory);
  }

  @Override
  public JobEntryInterface open() {
    return controller.open();
  }

  protected String getXulFile() {
    return OOZIE_JOB_EXECUTOR_XUL;
  }

  protected ResourceBundle bundle = new ResourceBundle() {
    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(OozieJobExecutorJobEntry.class, key);
    }

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }
  };
}
