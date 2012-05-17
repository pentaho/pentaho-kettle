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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entries.sqoop.SqoopConfig;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base functionality to support a Sqoop job entry controller that provides most of the common functionality to back a XUL-based dialog.
 */
public abstract class AbstractSqoopJobEntryController extends AbstractXulEventHandler {

  private XulDomContainer container;
  private BindingFactory bindingFactory;
  private AbstractSqoopJobEntry sqoopJobEntry;
  private SqoopConfig config;

  private String dialogElementId;

  /**
   * Creates a new Sqoop job entry controller.
   *
   * @param container Container with dialog for which we will control
   * @param sqoopJobEntry Job entry the dialog is being created for
   * @param bindingFactory Binding factory to generate bindings
   * @param dialogElementId Element id within the XUL document that references the dialog this controller is backing
   */
  public AbstractSqoopJobEntryController(XulDomContainer container, AbstractSqoopJobEntry sqoopJobEntry, BindingFactory bindingFactory, String dialogElementId) {
    this.container = container;
    this.bindingFactory = bindingFactory;
    this.sqoopJobEntry = sqoopJobEntry;
    this.dialogElementId = dialogElementId;
    this.config = sqoopJobEntry.getSqoopConfig().clone();
  }

  /**
   * Create the necessary XUL {@link Binding}s to support the dialog's desired functionality.
   *
   * @param bindingFactory Binding factory to create bindings with
   * @param bindings       Collection to add created bindings to. This collection will be initialized ({@link org.pentaho.ui.xul.binding.Binding#fireSourceChanged()}) upon return.
   */
  protected abstract void createBindings(SqoopConfig config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings);

  /**
   * Initialize the dialog by creating bindings and firing initial sync ({@link org.pentaho.ui.xul.binding.Binding#fireSourceChanged()}.
   *
   * @throws XulException
   * @throws InvocationTargetException
   */
  public void init() throws XulException, InvocationTargetException {
    List<Binding> bindings = new ArrayList<Binding>();
    bindingFactory.setBindingType(Binding.Type.BI_DIRECTIONAL);
    createBindings(config, container, bindingFactory, bindings);

    for (Binding binding : bindings) {
      binding.fireSourceChanged();
    }
  }

  /**
   * Get the simple name for this controller. This controller can be referenced by this name in the XUL document.
   * @return
   */
  @Override
  public String getName() {
    return "controller";
  }

  /**
   * @return the element id of the XUL dialog element in the XUL document
   */
  public String getDialogElementId() {
    return dialogElementId;
  }

  /**
   * When the "OK" button is clicked in the dialog set the configuration object back into the job entry and close the dialog
   */
  public void accept() {
    sqoopJobEntry.setSqoopConfig(config);
    cancel();
  }

  /**
   * Close the dialog discarding all changes.
   */
  public void cancel() {
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getElementById(dialogElementId);

    Shell shell = (Shell) xulDialog.getRootObject();
    if (!shell.isDisposed()) {
      WindowProperty winprop = new WindowProperty(shell);
      PropsUI.getInstance().setScreen(winprop);
      ((Composite) xulDialog.getManagedObject()).dispose();
      shell.dispose();
    }
  }

}
