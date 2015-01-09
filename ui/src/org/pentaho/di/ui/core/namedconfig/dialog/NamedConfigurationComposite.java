/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.namedconfig.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.namedconfig.model.Group;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.core.namedconfig.model.Property;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.TextVar;

public class NamedConfigurationComposite extends Composite {

  private static Class<?> PKG = NamedConfigurationComposite.class;

  private PropsUI props;

  public NamedConfigurationComposite( Composite parent, NamedConfiguration configuration, PropsUI props ) {
    super( parent, SWT.NONE );
    props.setLook( this );
    this.props = props;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    setLayout( formLayout );
    
    processConfiguration( this, configuration );
  }

  private void processConfiguration( final Composite c, final NamedConfiguration configuration ) {
    
    Composite confUI = createConfigurationUI( c, configuration );

    // Create a horizontal separator
    Label topSeparator = new Label(c, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fd = new FormData( 455, 1 );
    fd.top = new FormAttachment( confUI );
    topSeparator.setLayoutData( fd );    
    
    final ScrolledComposite sc1 = new ScrolledComposite( c, SWT.V_SCROLL );
    props.setLook( sc1 );
    fd = new FormData( 445, 325 );
    fd.top = new FormAttachment( topSeparator, 15 );
    sc1.setLayoutData( fd );
    
    // Create a child composite to hold the controls
    final Composite c1 = new Composite( sc1, SWT.NONE );
    props.setLook( c1 );   
    sc1.setContent( c1 );
    c1.setLayout( new GridLayout( 1, false ) );
    
    List<Group> groups = configuration.getGroups();
    for ( Group group : groups ) {
      createGroup( c1, group, configuration );
    }
    
    c1.setSize( c1.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    
    // Create a horizontal separator
    Label bottomSeparator = new Label( c, SWT.HORIZONTAL | SWT.SEPARATOR );
    fd = new FormData( 455, 1 );
    fd.top = new FormAttachment( sc1, 20 );
    bottomSeparator.setLayoutData( fd );
    
  }

  private Composite createConfigurationUI( final Composite c, final NamedConfiguration configuration  ) {
    Composite mainParent = new Composite( c, SWT.NONE );
    props.setLook( mainParent );
    mainParent.setLayout( new GridLayout( 1, false ) );
    FormData fd = new FormData( 440, 110 );
    mainParent.setLayoutData( fd );
    
    GridData textGridData = new GridData();
    textGridData.widthHint = 300;
    
    GridData labelGridData = new GridData();
    labelGridData.widthHint = 400;

    Label nameLabel = new Label( mainParent, SWT.NONE );
    nameLabel.setText( BaseMessages.getString( PKG, "NamedConfiguarationDialog.NamedConfiguration.Name" ) + ":" );
    nameLabel.setLayoutData( labelGridData );
    props.setLook( nameLabel );
    
    final Text nameValue = new Text( mainParent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    nameValue.setText( configuration.getName() != null ? configuration.getName() : "" );
    nameValue.setLayoutData( textGridData );
    props.setLook( nameValue );
    nameValue.addKeyListener( new KeyListener() {
      public void keyReleased( KeyEvent event ) {
        configuration.setName( nameValue.getText() );
      }

      public void keyPressed( KeyEvent event ) {
      }
    } );
    
    Label typeLabel = new Label( mainParent, SWT.NONE );
    typeLabel.setText( BaseMessages.getString( PKG, "NamedConfiguarationDialog.NamedConfiguration.Type" ) + ":" );
    typeLabel.setLayoutData( labelGridData );
    props.setLook( typeLabel );
    
    Label typeValue = new Label( mainParent, SWT.NONE );
    typeValue.setText( configuration.getType() != null ? configuration.getType() : "" );
    typeValue.setLayoutData( labelGridData );
    props.setLook( typeValue );
   
    return mainParent;
  }
  
  private void createGroup( Composite parentComposite, Group groupModel, NamedConfiguration configuration ) {

    org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group( parentComposite, SWT.NONE );
    group.setText( groupModel.getName() );
    group.setLayout( new RowLayout( SWT.VERTICAL ) );
    props.setLook( group );
    GridData groupGridData = new GridData();
    groupGridData.widthHint = 430;
    group.setLayoutData( groupGridData );
    
    GridData gridFormData = new GridData();
    gridFormData.widthHint = 350;

    GridData numberFormData = new GridData();
    numberFormData.widthHint = 80;
    
    GridData labelGridData = new GridData();
    labelGridData.widthHint = 400;
    
    List<Property> properties = groupModel.getProperties();
    for ( final Property property : properties ) {

      Composite propertyParent = new Composite( group, SWT.NONE );
      props.setLook( propertyParent );
      propertyParent.setLayout( new GridLayout( 1, false ) );

      Label propertyLabel = new Label( propertyParent, SWT.NONE );
      propertyLabel.setText( property.getDisplayName() + ":" );
      propertyLabel.setLayoutData( labelGridData );
      props.setLook( propertyLabel );

      final TextVar propertyValue;
      
      if ( property.getType() != null && "password".equals( property.getType() ) ) {
        propertyValue = new TextVar( configuration, propertyParent, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.PASSWORD );
      } else {
        propertyValue = new TextVar( configuration, propertyParent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
      }
      
      if ( property.getType() != null && "java.lang.Integer".equals( property.getType() ) ) {
        propertyValue.setLayoutData( numberFormData );
      } else {
        propertyValue.setLayoutData( gridFormData );
      }
      
      propertyValue.setText( property.getPropertyValue() != null ? property.getPropertyValue().toString() : "" );
      propertyValue.addKeyListener( new KeyListener() {
        public void keyReleased( KeyEvent event ) {
          property.setPropertyValue( propertyValue.getText() );
        }

        public void keyPressed( KeyEvent event ) {
        }
      } );
    }
  }
  
}
