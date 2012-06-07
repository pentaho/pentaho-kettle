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

package org.pentaho.di.ui.job;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.AbstractJobEntry;
import org.pentaho.di.job.BlockableJobConfig;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: RFellows
 * Date: 6/6/12
 */
public abstract class AbstractJobEntryController<C extends BlockableJobConfig, E extends AbstractJobEntry<C>> extends AbstractXulEventHandler {
  // Generically typed fields
  protected C config;     // BlockableJobConfig
  protected E jobEntry;   // AbstractJobEntry<BlockableJobConfig>

  // common fields
  protected XulDomContainer container;
  protected BindingFactory bindingFactory;
  protected List<Binding> bindings;
  protected JobMeta jobMeta;

  @SuppressWarnings("unchecked")
  public AbstractJobEntryController(JobMeta jobMeta, XulDomContainer container, E jobEntry, BindingFactory bindingFactory) {
    super();
    this.jobMeta = jobMeta;
    this.jobEntry = jobEntry;
    this.container = container;
    this.config = (C) jobEntry.getJobConfig().clone();
    this.bindingFactory = bindingFactory;
  }

  /**
   * @return the simple name for this controller. This controller can be referenced by this name in the XUL document.
   */
  @Override
  public String getName() {
    return "controller";
  }

  /**
   * Opens the dialog
   * @return
   */
  public JobEntryInterface open() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById(getDialogElementId());
    dialog.show();
    return jobEntry;
  }

  /**
   * Initialize the dialog by loading model data, creating bindings and firing initial sync
   * ({@link org.pentaho.ui.xul.binding.Binding#fireSourceChanged()}.
   *
   * @throws org.pentaho.ui.xul.XulException
   * @throws java.lang.reflect.InvocationTargetException
   */
  public void init() throws XulException, InvocationTargetException {
    bindings = new ArrayList<Binding>();

    // override hook
    beforeInit();

    try {

      createBindings(config, container, bindingFactory, bindings);
      syncModel();

      for (Binding binding : bindings) {
        binding.fireSourceChanged();
      }
    } finally {
      // override hook
      afterInit();
    }

  }

  /**
   * Accept and apply the changes made in the dialog. Also, close the dialog
   */
  @Bindable
  public void accept() {
    jobEntry.setJobConfig(config);
    jobEntry.setChanged();
    cancel();
  }

  /**
   * Close the dialog without saving any changes
   */
  @Bindable
  public void cancel() {
    removeBindings();
    XulDialog xulDialog = getDialog();

    Shell shell = (Shell) xulDialog.getRootObject();
    if (!shell.isDisposed()) {
      WindowProperty winprop = new WindowProperty(shell);
      PropsUI.getInstance().setScreen(winprop);
      ((Composite) xulDialog.getManagedObject()).dispose();
      shell.dispose();
    }
  }

  /**
   * Remove and destroy all bindings from {@link #bindings}.
   */
  protected void removeBindings() {
    if(bindings == null) {
      return;
    }
    for (Binding binding : bindings) {
      binding.destroyBindings();
    }
    bindings.clear();
  }

  /**
   * Look up the dialog reference from the document.
   *
   * @return The dialog element referred to by {@link #getDialogElementId()}
   */
  protected SwtDialog getDialog() {
    return (SwtDialog) getXulDomContainer().getDocumentRoot().getElementById(getDialogElementId());
  }


  /**
   * @return the job entry this controller will modify configuration for
   */
  protected E getJobEntry() {
    return jobEntry;
  }

  /**
   * Override this to execute some code prior to the init function running
   */
  protected void beforeInit() {
    return;
  }

  /**
   * Override this to execute some code after the init function is complete
   */
  protected void afterInit() {
    return;
  }


  /**
   * Show an error dialog with the title and message provided.
   *
   * @param title   Dialog window title
   * @param message Dialog message
   */
  protected void showErrorDialog(String title, String message) {
    MessageBox mb = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
    mb.setText(title);
    mb.setMessage(message);
    mb.open();
  }

  /**
   * Show an error dialog with the title, message, and toggle button to see the entire stacktrace produced by {@code t}.
   *
   * @param title   Dialog window title
   * @param message Dialog message
   * @param t       Cause for this error
   */
  protected void showErrorDialog(String title, String message, Throwable t) {
    new ErrorDialog(getShell(), title, message, t);
  }

  /**
   * @return the shell for the currently visible dialog. This will be used to display additional dialogs/popups.
   */
  protected Shell getShell() {
    return getDialog().getShell();
  }

  ////////////////////
  // abstract methods
  ////////////////////
  protected abstract void syncModel();

  protected abstract void createBindings(C config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings);

  protected abstract String getDialogElementId();

}
