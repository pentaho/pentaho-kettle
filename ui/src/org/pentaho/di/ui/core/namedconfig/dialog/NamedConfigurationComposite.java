/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.namedconfig.model.Group;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.core.namedconfig.model.Property;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;

public class NamedConfigurationComposite extends Composite {

  private static Class<?> PKG = NamedConfigurationComposite.class;

  private PropsUI props;
  private GridData gridLabelData;
  private GridData gridFormData;

  public NamedConfigurationComposite( Shell parent, NamedConfiguration configuration, PropsUI props ) {
    super( parent, SWT.NONE );
    props.setLook( this );
    this.props = props;

    setLayout( new RowLayout( SWT.VERTICAL ) );
    processConfiguration( configuration );
  }

  private void processConfiguration( final NamedConfiguration configuration ) {
    createConfigurationGroup( configuration );

    List<Group> groups = configuration.getGroups();
    for ( Group group : groups ) {
      createGroup( group );
    }
  }

  private void createConfigurationGroup(  final NamedConfiguration configuration  ) {
    org.eclipse.swt.widgets.Group groupWidget = new org.eclipse.swt.widgets.Group( this, SWT.NONE );
    groupWidget.setText( BaseMessages.getString( PKG, "NamedConfiguarationDialog.NamedConfiguration.Configuration" ) );
    groupWidget.setLayout( new RowLayout( SWT.VERTICAL ) );
    props.setLook( groupWidget );   
    
    Composite mainParent = new Composite( groupWidget, SWT.NONE );
    props.setLook( mainParent );
    mainParent.setLayout( new GridLayout( 2, false ) );

    gridLabelData = new GridData();
    gridLabelData.widthHint = 100;

    gridFormData = new GridData();
    gridFormData.widthHint = 200;

    Label typeLabel = new Label( mainParent, SWT.NONE );
    typeLabel.setText( BaseMessages.getString( PKG, "NamedConfiguarationDialog.NamedConfiguration.Type" ) + ":" );
    typeLabel.setLayoutData( gridLabelData );
    props.setLook( typeLabel );
    
    Label typeValue = new Label( mainParent, SWT.NONE );
    typeValue.setText( configuration.getType() != null ? configuration.getType() : "" );
    typeValue.setLayoutData( gridFormData );
    props.setLook( typeValue );
    
    Label nameLabel = new Label( mainParent, SWT.NONE );
    nameLabel.setText( BaseMessages.getString( PKG, "NamedConfiguarationDialog.NamedConfiguration.Name" ) + ":" );
    nameLabel.setLayoutData( gridLabelData );
    props.setLook( nameLabel );
    
    final Text nameValue = new Text( mainParent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    nameValue.setText( configuration.getName() != null ? configuration.getName() : "" );
    nameValue.setLayoutData( gridFormData );
    props.setLook( nameValue );
    nameValue.addKeyListener( new KeyListener() {
      public void keyReleased( KeyEvent event ) {
        configuration.setName( nameValue.getText() );
      }

      public void keyPressed( KeyEvent event ) {
      }
    } );
  }
  
  private void createGroup( Group groupModel ) {

    org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group( this, SWT.NONE );
    group.setText( groupModel.getName() );
    group.setLayout( new RowLayout( SWT.VERTICAL ) );
    props.setLook( group );

    List<Property> properties = groupModel.getProperties();
    for ( final Property property : properties ) {

      Composite propertyParent = new Composite( group, SWT.NONE );
      props.setLook( propertyParent );
      propertyParent.setLayout( new GridLayout( 2, false ) );

      Label propertyLabel = new Label( propertyParent, SWT.NONE );
      propertyLabel.setText( property.getDisplayName() + ":" );
      propertyLabel.setLayoutData( gridLabelData );
      props.setLook( propertyLabel );

      final Text propertyValue = new Text( propertyParent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
      propertyValue.setLayoutData( gridFormData );
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
