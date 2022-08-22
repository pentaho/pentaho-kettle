/*

*/
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
 ******************************************************************************//*


package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.endpoints.RepositoryEndpoint;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RepositoryDialogSWT extends Shell {
    private Text username;
    private Text password;
    private Button btnConnect;
    private RepositoryDialogSWT shell;
    private RepositoryEndpoint repoendpoint;

    public void createDialog() {
        try {
            Display display = Display.getDefault();
            this.shell = new RepositoryDialogSWT(display);
            this.shell.open();
            this.shell.layout();
            while (!this.shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    */
/**
     * Create the shell.
     * @param display
     *//*

    public RepositoryDialogSWT(Display display) {
        super(display, SWT.SHELL_TRIM | SWT.BORDER);
        System.out.println("here 2");
        Label lblRepositoryConnection = new Label(this, SWT.CENTER);
        lblRepositoryConnection.setBounds(286, 10, 320, 42);
        Font boldFont = new Font( lblRepositoryConnection.getDisplay(), new FontData( "Arial", 12, SWT.BOLD ) );
        lblRepositoryConnection.setFont(boldFont);
        lblRepositoryConnection.setText("Repository Connection");

        Label lblConnectTo = new Label(this, SWT.NONE);
        lblConnectTo.setBounds(48, 77, 133, 25);
        lblConnectTo.setText("Connect to");

        Label lblLocalrepo = new Label(this, SWT.NONE);
        lblLocalrepo.setBounds(48, 108, 81, 25);
        lblLocalrepo.setText("local_repo");

        Label lblUsername = new Label(this, SWT.NONE);
        lblUsername.setBounds(48, 165, 81, 25);
        lblUsername.setText("username:");

        username = new Text(this, SWT.BORDER);
        username.setBounds(48, 196, 364, 31);
        username.setText("default_username");

        Label lblPassword = new Label(this, SWT.NONE);
        lblPassword.setBounds(48, 233, 81, 25);
        lblPassword.setText("password:");

        password = new Text(this, SWT.BORDER | SWT.PASSWORD);
        password.setBounds(48, 264, 364, 31);
        password.setText("default_password");

        btnConnect = new Button(this, SWT.PUSH);
        btnConnect.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                System.out.println("button pressed");
                String ip_username=username.getText();
                String ip_password=password.getText();
                System.out.println("rcvd id and password");
                System.out.println("rcvd username :"+ip_username);
                System.out.println("rcvd password :"+ip_password);

                if(ip_username==""){
                    System.out.println("blank ip username");
                    //new ErrorDialogSWT("blank ip username");
                    MessageBox messageBox = new MessageBox(shell);
                    messageBox.setMessage("Enter the User Name");
                    messageBox.open();
                }
                if(ip_password==""){
                    System.out.println("blank password");
                    MessageBox messageBox = new MessageBox(shell, SWT.OK |
                            SWT.ICON_WARNING |SWT.CANCEL);
                    messageBox.setMessage("Enter the Password");
                    messageBox.open();
                }
                else{
                    System.out.println("not blank ip username and password");

                    repoendpoint.login(ip_reponame,ip_username,ip_password);
                    */
/*MessageBox messageBox=new MessageBox(shell,SWT.OK|SWT.CANCEL);
                    System.out.println("msg box obj created");
                    messageBox.setText("Login Form");
                    messageBox.setMessage("Welcome:" + ip_username);
                    messageBox.open();*//*




                }
            }
        });
        btnConnect.addControlListener(new ControlAdapter() {

        });
        btnConnect.setBounds(48, 330, 105, 35);
        btnConnect.setText("connect");
        createContents();
    }

    */
/**
     * Create contents of the shell.
     *//*

    protected void createContents() {
        setText("Repository connection dialog");
        setSize(796, 458);
        System.out.println("here");
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
*/
/*    public Text getUserName() {
        return username;
    }
    public Text getPassword() {
        return password;
    }*//*

    public Button getBtnConnect() {
        System.out.println("here 1");
        return btnConnect;
    }
}
*/
