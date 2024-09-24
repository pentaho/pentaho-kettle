/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.pentaho.di.ui.trans.step.BaseStreamingDialog.INPUT_WIDTH;

/**
 * Authentication composite
 * Composite that contains username and password credentials
 */
public class AuthComposite extends Composite {

  private final PropsUI props;
  private final ModifyListener lsMod;
  private final TransMeta transMeta;

  private String authGroupLabel;
  private Group wAuthGroup;

  private String usernameLabel;
  private Label wlUsername;
  private TextVar wUsername;

  private String passwordLabel;
  private Label wlPassword;
  private TextVar wPassword;


  public AuthComposite( Composite composite, int style, PropsUI props, ModifyListener lsMod, TransMeta transMeta,
                        String credentialGroupName, String usernameLabel, String passwordLabel ) {
    super( composite, style );
    checkNotNull( props );
    checkNotNull( lsMod );
    checkNotNull( transMeta );

    this.props = props;
    this.lsMod = lsMod;
    this.transMeta = transMeta;

    this.authGroupLabel = credentialGroupName;
    this.usernameLabel = usernameLabel;
    this.passwordLabel = passwordLabel;

    layoutComposite();
  }

  private void layoutComposite() {
    FormLayout flAuthComp = new FormLayout();
    this.setLayout( flAuthComp );

    //authentication group
    wAuthGroup = new Group( this, SWT.SHADOW_ETCHED_IN );
    props.setLook( wAuthGroup );
    wAuthGroup.setText( authGroupLabel );

    FormLayout flAuthGroup = new FormLayout();
    flAuthGroup.marginHeight = 15;
    flAuthGroup.marginWidth = 15;
    wAuthGroup.setLayout( flAuthGroup );
    wAuthGroup.setLayoutData( new FormDataBuilder().fullSize().result() );

    //username
    wlUsername = new Label( wAuthGroup, SWT.LEFT );
    props.setLook( wlUsername );
    wlUsername.setText( usernameLabel );
    wlUsername.setLayoutData( new FormDataBuilder().left().top().result() );

    wUsername = new TextVar( transMeta, wAuthGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUsername );
    wUsername.addModifyListener( lsMod );
    wUsername.setLayoutData( new FormDataBuilder().top( wlUsername ).left().width( INPUT_WIDTH ).result() );

    //password
    wlPassword = new Label( wAuthGroup, SWT.LEFT );
    props.setLook( wlPassword );
    wlPassword.setText( passwordLabel );
    wlPassword.setLayoutData( new FormDataBuilder().left().top( wUsername, ConstUI.MEDUIM_MARGIN ).result() );

    wPassword = new PasswordTextVar( transMeta, wAuthGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    wPassword.setLayoutData( new FormDataBuilder().left().top( wlPassword ).width( INPUT_WIDTH ).result() );
  }

  public String getUsername() {
    return wUsername.getText();
  }

  public String getPassword() {
    return wPassword.getText();
  }

  public void setUsername( String username ) {
    this.wUsername.setText( username );
  }

  public void setPassword( String password ) {
    this.wPassword.setText( password );
  }

  @Override public void setVisible( boolean visible ) {
    super.setVisible( visible );
    wAuthGroup.setVisible( visible );
    wlUsername.setVisible( visible );
    wUsername.setVisible( visible );
    wlPassword.setVisible( visible );
    wPassword.setVisible( visible );
  }
}
