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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.hadoop.HadoopSpoonPlugin;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.AbstractJobEntry;
import org.pentaho.di.job.BlockableJobConfig;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.vfs.VfsFileChooserHelper;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.swt.tags.SwtDialog;
import org.pentaho.ui.xul.swt.tags.SwtLabel;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: RFellows
 * Date: 6/6/12
 */
public abstract class AbstractJobEntryController<C extends BlockableJobConfig, E extends AbstractJobEntry<C>> extends AbstractXulEventHandler {

  public static final String[] DEFAULT_FILE_FILTERS = new String[]{"*.*"};

  // Generically typed fields
  protected C config;     // BlockableJobConfig
  protected E jobEntry;   // AbstractJobEntry<BlockableJobConfig>

  // common fields
  protected XulDomContainer container;
  protected BindingFactory bindingFactory;
  protected List<Binding> bindings;
  protected JobMeta jobMeta;

  protected JobEntryMode jobEntryMode = JobEntryMode.BASIC;

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
   * Show an information dialog with the title and message provided.
   *
   * @param title   Dialog window title
   * @param message Dialog message
   */
  protected void showInfoDialog(String title, String message) {
    MessageBox mb = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
    mb.setText(title);
    mb.setMessage(message);
    mb.open();
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

  /**
   * Browse for a file or directory with the VFS Browser.
   *
   * @param root       Root object
   * @param initial    Initial file or folder the browser should open to
   * @param dialogMode Mode to open dialog in: e.g. {@link org.pentaho.vfs.ui.VfsFileChooserDialog#VFS_DIALOG_OPEN_FILE_OR_DIRECTORY}
   * @param schemeRestriction Scheme to limit the user to browsing from
   * @param defaultScheme Scheme to select by default in the selection dropdown
   * @return The selected file object, {@code null} if no object is selected
   * @throws org.pentaho.di.core.exception.KettleFileException Error accessing the root file using the initial file, when {@code root} is not provided
   */
  protected FileObject browseVfs(FileObject root, FileObject initial, int dialogMode, String schemeRestriction, String defaultScheme, boolean showFileScheme) throws KettleFileException {

    if (initial == null) {
      initial = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
    }
    if (root == null) {
      try {
        root = initial.getFileSystem().getRoot();
      } catch (FileSystemException e) {
        throw new KettleFileException(e);
      }
    }

    VfsFileChooserHelper fileChooserHelper = new VfsFileChooserHelper(getShell(), Spoon.getInstance().getVfsFileChooserDialog(root, initial), jobEntry);
    fileChooserHelper.setDefaultScheme(defaultScheme);
    fileChooserHelper.setSchemeRestriction(schemeRestriction);
    fileChooserHelper.setShowFileScheme(showFileScheme);
    try {
      return fileChooserHelper.browse(getFileFilters(), getFileFilterNames(), initial.getName().getURI(), dialogMode);
    } catch (KettleException e) {
      throw new KettleFileException(e);
    } catch (FileSystemException e) {
      throw new KettleFileException(e);
    }

  }

  protected String[] getFileFilters() {
    return DEFAULT_FILE_FILTERS;
  }

  /**
   * Used by browseVfs method as names corresponding to the file filters. Override if {@code getFileFilters} is overridden.
   * @return
   */
  protected String[] getFileFilterNames() {
    return new String[]{BaseMessages.getString(getClass(), "System.FileType.AllFiles")};
  }

  /**
   * @return the current configuration object. This configuration may be discarded if the dialog is canceled.
   */
  public C getConfig() {
    return config;
  }

  public void setConfig(C config) {
    this.config = config;
  }

  /**
   * @return the job meta for the job entry we're editing
   */
  public JobMeta getJobMeta() {
    return jobMeta;
  }

  public void setJobMeta(JobMeta jobMeta) {
    this.jobMeta = jobMeta;
  }

  /**
   * Toggle between Advanced and Basic configuration modes
   */
  public void toggleMode() {
    JobEntryMode prev = jobEntryMode;
    jobEntryMode = (jobEntryMode == JobEntryMode.ADVANCED ? JobEntryMode.BASIC : JobEntryMode.ADVANCED);
    XulDeck deck = (XulDeck) getXulDomContainer().getDocumentRoot().getElementById(getModeDeckElementId());
    deck.setSelectedIndex(deck.getSelectedIndex() == 0 ? 1 : 0);

    // Synchronize the model every time we swap modes so the UI is always up to date. This is required since we don't
    // set argument item values directly or listen for their changes
    syncModel();

    // Swap the label on the button
    setModeToggleLabel(prev);   // the label should display the inverse of the current mode

  }

  /**
   * Customizes the label that is used to toggle between quick setup and advanced options.
   * <p>
   *   - Set the label color to blue
   *   - Underline the label
   *   - Attaches a left-click listener on the label to perform the toggling
   * </p>
   *
   * @param elementId Mode toggle element to attach listener on
   */
  protected void customizeModeToggleLabel(String elementId) {
    SwtLabel label = (SwtLabel) getXulDomContainer().getDocumentRoot().getElementById(elementId);
    // Only decorate the label if it's not a link. This was added in pentaho-xul-swt after PDI 4.3.0.
    // TODO Remove this logic once pentaho-xul-swt is upgraded past 3.3 (when SwtLabel can support hyperlinks)
    if (label != null && label.getManagedObject() instanceof CLabel) {
      CLabel cLabel = (CLabel) label.getManagedObject();

      FontData[] fontDatas = cLabel.getFont().getFontData();
      for (FontData fontData : fontDatas) {
        fontData.setStyle(SWT.BOLD);
      }
      final Font font = new Font(cLabel.getDisplay(), fontDatas);
      cLabel.setFont(font);

      final Cursor cursor = new Cursor(cLabel.getDisplay(), SWT.CURSOR_HAND);
      cLabel.setCursor(cursor);

      final Color color = new Color(cLabel.getDisplay(), 0, 0, 255);
      cLabel.setForeground(color);

      cLabel.addDisposeListener(new DisposeListener() {
        @Override
        public void widgetDisposed(DisposeEvent disposeEvent) {
          color.dispose();
          cursor.dispose();
          font.dispose();
        }
      });

      cLabel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseUp(MouseEvent e) {
          if (e.button == 1) {
            toggleMode();
          }
        }
      });
    }
  }

  /**
   * The mode deck element defined in your xul. Override this to customize the element id
   * @return
   */
  protected String getModeDeckElementId() {
    return "modeDeck";
  }

  ////////////////////
  // abstract methods
  ////////////////////
  protected abstract void syncModel();

  protected abstract void createBindings(C config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings);

  protected abstract String getDialogElementId();

  protected abstract void setModeToggleLabel(JobEntryMode mode);

}
