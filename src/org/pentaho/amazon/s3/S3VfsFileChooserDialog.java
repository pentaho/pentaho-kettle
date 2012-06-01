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

package org.pentaho.amazon.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.GenericFileName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.amazon.AmazonSpoonPlugin;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.s3.vfs.S3FileObject;
import org.pentaho.vfs.ui.CustomVfsUiPanel;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class S3VfsFileChooserDialog extends CustomVfsUiPanel {

  // for message resolution
  private static Class<?> PKG = AmazonSpoonPlugin.class;

  // for logging
  private LogChannel log = new LogChannel(this);

  // URL label and field
  private Label wlAccessKey;
  private TextVar wAccessKey;
  private GridData fdlAccessKey, fdAccessKey;

  // Port label and field
  // private Label wlBucket;
  // private Text wBucket;
  // private GridData fdlBucket, fdBucket;

  // UserID label and field
  private Label wlSecretKey;
  private TextVar wSecretKey;
  private GridData fdlSecretKey, fdSecretKey;

  // Place holder - for creating a blank widget in a grid layout
  private Label wPlaceHolderLabel;
  private GridData fdlPlaceHolderLabel;

  // Connection button
  private Button wConnectionButton;
  private GridData fdConnectionButton;

  // Default root file - used to avoid NPE when rootFile was not provided
  // and the browser is resolved
  FileObject defaultInitialFile = null;

  // File objects to keep track of when the user selects the radio buttons
  FileObject localRootFile = null;
  String localOpenFromFolder = null;
  FileObject s3RootFile = null;
  String s3OpenFromFolder = null;

  FileObject rootFile = null;
  FileObject initialFile = null;
  VfsFileChooserDialog vfsFileChooserDialog = null;

  private String accessKey;
  private String secretKey;

  private StaticUserAuthenticator userAuthenticator = null;

  public S3VfsFileChooserDialog(VfsFileChooserDialog vfsFileChooserDialog, FileObject rootFile, FileObject initialFile) {
    super(AmazonSpoonPlugin.S3_SCHEME, AmazonSpoonPlugin.S3_SCHEME_DISPLAY_TEXT, vfsFileChooserDialog, SWT.NONE);

    this.vfsFileChooserDialog = vfsFileChooserDialog;
    this.rootFile = rootFile;
    this.initialFile = initialFile;

    setLayout(new GridLayout());

    // Create the s3 panel
    createConnectionPanel();
    initializeConnectionPanel();
  }

  private void createConnectionPanel() {

    // The Connection group
    Group connectionGroup = new Group(this, SWT.SHADOW_ETCHED_IN);
    connectionGroup.setText(BaseMessages.getString(PKG, "S3VfsFileChooserDialog.ConnectionGroup.Label")); //$NON-NLS-1$;
    GridLayout connectionGroupLayout = new GridLayout();
    connectionGroupLayout.marginWidth = 5;
    connectionGroupLayout.marginHeight = 5;
    connectionGroupLayout.verticalSpacing = 5;
    connectionGroupLayout.horizontalSpacing = 5;
    GridData gData = new GridData(SWT.FILL, SWT.FILL, true, false);
    connectionGroup.setLayoutData(gData);
    connectionGroup.setLayout(connectionGroupLayout);

    // The composite we need in the group
    Composite textFieldPanel = new Composite(connectionGroup, SWT.NONE);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    textFieldPanel.setLayoutData(gridData);
    textFieldPanel.setLayout(new GridLayout(3, false));

    // URL label and text field
    wlAccessKey = new Label(textFieldPanel, SWT.RIGHT);
    wlAccessKey.setText(BaseMessages.getString(PKG, "S3VfsFileChooserDialog.AccessKey.Label")); //$NON-NLS-1$
    fdlAccessKey = new GridData();
    fdlAccessKey.widthHint = 75;
    wlAccessKey.setLayoutData(fdlAccessKey);
    wAccessKey = new TextVar(getVariableSpace(), textFieldPanel, SWT.PASSWORD | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    fdAccessKey = new GridData();
    fdAccessKey.widthHint = 150;
    wAccessKey.setLayoutData(fdAccessKey);
    wAccessKey.setText(Props.getInstance().getCustomParameter("S3VfsFileChooserDialog.AccessKey", ""));

    wAccessKey.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent arg0) {
        handleConnectionButton();
      }
    });

    // // Place holder
    wPlaceHolderLabel = new Label(textFieldPanel, SWT.RIGHT);
    wPlaceHolderLabel.setText("");
    fdlPlaceHolderLabel = new GridData();
    fdlPlaceHolderLabel.widthHint = 75;
    wPlaceHolderLabel.setLayoutData(fdlPlaceHolderLabel);

    // UserID label and field
    wlSecretKey = new Label(textFieldPanel, SWT.RIGHT);
    fdlSecretKey = new GridData();
    fdlSecretKey.widthHint = 75;
    wlSecretKey.setLayoutData(fdlSecretKey);

    wSecretKey = new TextVar(getVariableSpace(), textFieldPanel, SWT.PASSWORD | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    fdSecretKey = new GridData();
    fdSecretKey.widthHint = 300;
    wSecretKey.setLayoutData(fdSecretKey);
    wSecretKey.setText(Props.getInstance().getCustomParameter("S3VfsFileChooserDialog.SecretKey", ""));

    wSecretKey.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent arg0) {
        handleConnectionButton();
      }
    });

    // bucket
    // wlBucket = new Label(textFieldPanel, SWT.RIGHT);
    //    wlBucket.setText(BaseMessages.getString(PKG, "S3VfsFileChooserDialog.Bucket.Label")); //$NON-NLS-1$
    // fdlBucket = new GridData();
    // fdlBucket.widthHint = 75;
    // wlBucket.setLayoutData(fdlBucket);
    //
    // wBucket = new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    // fdBucket = new GridData();
    // fdBucket.widthHint = 150;
    // wBucket.setLayoutData(fdBucket);
    // wBucket.addKeyListener(new KeyAdapter() {
    // public void keyPressed(KeyEvent e) {
    // handleConnectionButton();
    // }
    // });

    // Connection button
    wConnectionButton = new Button(textFieldPanel, SWT.CENTER);
    fdConnectionButton = new GridData();
    fdConnectionButton.widthHint = 75;
    wConnectionButton.setLayoutData(fdConnectionButton);

    wConnectionButton.setText(BaseMessages.getString(PKG, "S3VfsFileChooserDialog.ConnectionButton.Label"));
    wConnectionButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {

        try {
          // let's verify the accessKey/secretKey
          AmazonS3 s3Client = new AmazonS3Client(new AWSCredentials() {

            public String getAWSSecretKey() {
              return environmentSubstitute(wSecretKey.getText());
            }

            public String getAWSAccessKeyId() {
              return environmentSubstitute(wAccessKey.getText());
            }
          });
          s3Client.getS3AccountOwner();

          // s3 credentials valid, continue
          Props.getInstance().setCustomParameter("S3VfsFileChooserDialog.AccessKey", wAccessKey.getText());
          Props.getInstance().setCustomParameter("S3VfsFileChooserDialog.SecretKey", wSecretKey.getText());

          try {
            FileObject root = rootFile;
            root = resolveFile(buildS3FileSystemUrlString());
            vfsFileChooserDialog.setSelectedFile(root);
            vfsFileChooserDialog.setRootFile(root);
            rootFile = root;
          } catch (FileSystemException e1) {
            MessageBox box = new MessageBox(getShell());
            box.setText(BaseMessages.getString(PKG, "S3VfsFileChooserDialog.error")); //$NON-NLS-1$
            box.setMessage(e1.getMessage());
            log.logError(e1.getMessage(), e1);
            box.open();
            return;
          }

        } catch (AmazonS3Exception ex) {
          // if anything went wrong, we have to show an error dialog
          MessageBox box = new MessageBox(getShell());
          box.setText(BaseMessages.getString(PKG, "S3VfsFileChooserDialog.error")); //$NON-NLS-1$
          box.setMessage(ex.getMessage());
          log.logError(ex.getMessage(), ex);
          box.open();
          return;
        }
      }
    });

    // set the tab order
    textFieldPanel.setTabList(new Control[] { wAccessKey, wSecretKey, wConnectionButton });
    // textFieldPanel.setTabList(new Control[] { wAccessKey, wBucket, wSecretKey, wPassword, wConnectionButton });
  }

  private VariableSpace getVariableSpace() {
    if (Spoon.getInstance().getActiveTransformation() != null) {
      return Spoon.getInstance().getActiveTransformation();
    } else if (Spoon.getInstance().getActiveJob() != null) {
      return Spoon.getInstance().getActiveJob();
    } else {
      return new Variables();
    }
  }

  private String environmentSubstitute(String str) {
    return getVariableSpace().environmentSubstitute(str);
  }

  /**
   * Build a URL given Url and Port provided by the user.
   * 
   * @return
   * @TODO: relocate to a s3 helper class or similar
   */
  public String buildS3FileSystemUrlString() {
    return AmazonSpoonPlugin.S3_SCHEME + "://s3/";
  }

  @Override
  public void activate() {
    wAccessKey.setVariables(getVariableSpace());
    wSecretKey.setVariables(getVariableSpace());
    super.activate();
  }

  private void initializeConnectionPanel() {

    if (initialFile != null && initialFile instanceof S3FileObject) {
      // populate the server and port fields
      try {
        GenericFileName genericFileName = (GenericFileName) initialFile.getFileSystem().getRoot().getName();
        wAccessKey.setText(genericFileName.getUserName() == null ? "" : genericFileName.getUserName());
        wSecretKey.setText(genericFileName.getPassword()); //$NON-NLS-1$
        // wBucket.setText(String.valueOf(genericFileName.getPort()));
      } catch (FileSystemException fse) {
        showMessageAndLog("S3VfsFileChooserDialog.error", "S3VfsFileChooserDialog.FileSystem.error", fse.getMessage());
      }
    } else {

    }

    handleConnectionButton();
  }

  private void showMessageAndLog(String title, String message, String messageToLog) {
    MessageBox box = new MessageBox(getShell());
    box.setText(BaseMessages.getString(PKG, title)); //$NON-NLS-1$
    box.setMessage(BaseMessages.getString(PKG, message));
    log.logError(messageToLog);
    box.open();
  }

  private void handleConnectionButton() {
    if (!Const.isEmpty(wAccessKey.getText()) && !Const.isEmpty(wSecretKey.getText())) {
      accessKey = getVariableSpace().environmentSubstitute(wAccessKey.getText());
      secretKey = getVariableSpace().environmentSubstitute(wSecretKey.getText());
      wConnectionButton.setEnabled(true);
    } else {
      accessKey = null;
      secretKey = null;
      wConnectionButton.setEnabled(false);
    }
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public FileObject resolveFile(String fileUri) throws FileSystemException {
    try {
      return KettleVFS.getFileObject(fileUri, getVariableSpace(), getFileSystemOptions());
    } catch (KettleFileException e) {
      throw new FileSystemException(e);
    }
  }

  public FileObject resolveFile(String fileUri, FileSystemOptions opts) throws FileSystemException {
    try {
      return KettleVFS.getFileObject(fileUri, getVariableSpace(), opts);
    } catch (KettleFileException e) {
      throw new FileSystemException(e);
    }
  }

  protected FileSystemOptions getFileSystemOptions() throws FileSystemException {
    FileSystemOptions opts = new FileSystemOptions();

    if(!Const.isEmpty(getAccessKey()) || !Const.isEmpty(getSecretKey())) {
      // create a FileSystemOptions with user & password
      StaticUserAuthenticator userAuthenticator =
          new StaticUserAuthenticator(null,
              getVariableSpace().environmentSubstitute(getAccessKey()),
              getVariableSpace().environmentSubstitute(getSecretKey())
          );

      DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, userAuthenticator);
    }
    return opts;
  }
}
