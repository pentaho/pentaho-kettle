/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.beam;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.Const;
import org.pentaho.di.engine.configuration.api.RunConfigurationDialog;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 8/22/17.
 */
public class BeamRunConfigurationUI implements RunConfigurationUI {

  private static Class<?> PKG = BeamRunConfigurationUI.class;
  private PropsUI props = PropsUI.getInstance();

  private BeamRunConfiguration beamRunConfiguration;

  public BeamRunConfigurationUI( BeamRunConfiguration beamRunConfiguration ) {
    this.beamRunConfiguration = beamRunConfiguration;
  }

  @Override public void attach( RunConfigurationDialog runConfigurationDialog ) {

    List<String> configs = new ArrayList<>();

    DelegatingMetaStore metaStore = Spoon.getInstance().getMetaStore();
    try {
      String namespace = "pentaho";

      IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, "Kettle Beam Job Config" );
      List<String> elementIds = metaStore.getElementIds( namespace, elementType );
      for ( String elementId : elementIds ) {
        IMetaStoreElement element = metaStore.getElement( namespace, elementType, elementId );
        configs.add( element.getName() );
      }
    } catch ( MetaStoreException e ) {
      e.printStackTrace();
    }

    GridData protocolLabelData = new GridData( SWT.NONE, SWT.FILL, false, false );

    GridLayout gridLayout = new GridLayout( 1, false );
    runConfigurationDialog.getGroup().setLayout( gridLayout );

    Label configLabel = new Label( runConfigurationDialog.getGroup(), SWT.LEFT );
    props.setLook( configLabel );
    configLabel.setText( "Kettle Beam Job Configuration" );
    configLabel.setLayoutData( protocolLabelData );

    Combo configCombo = new Combo( runConfigurationDialog.getGroup(), SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    for ( String config : configs ) {
      configCombo.add( config);
    }

    configCombo.setText( Const.NVL(beamRunConfiguration.getBeamJobConfig(), "") );
    configCombo.addListener( SWT.Selection, e->{beamRunConfiguration.setBeamJobConfig( configCombo.getText() );} );

    GridData configData = new GridData( SWT.NONE, SWT.FILL, false, false );
    configCombo.setLayoutData( configData );
  }

}
