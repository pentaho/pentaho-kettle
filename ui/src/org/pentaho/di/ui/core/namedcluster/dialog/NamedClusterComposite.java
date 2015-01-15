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

package org.pentaho.di.ui.core.namedcluster.dialog;

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
import org.pentaho.di.core.namedcluster.model.NamedCluster;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.TextVar;

public class NamedClusterComposite extends Composite {

  private static Class<?> PKG = NamedClusterComposite.class;

  private PropsUI props;

  private GridData gridData;
  private GridData numberGridData;
  private GridData labelGridData;
  private GridData urlGridData;

  private static final int TEXT_FLAGS = SWT.SINGLE | SWT.LEFT | SWT.BORDER;
  private static final int PASSWORD_FLAGS = TEXT_FLAGS | SWT.PASSWORD;
  
  private interface Callback {
    public void invoke( NamedCluster nc, TextVar textVar, String value );
  }
  
  public NamedClusterComposite( Composite parent, NamedCluster namedCluster, PropsUI props ) {
    super( parent, SWT.NONE );
    props.setLook( this );
    this.props = props;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    setLayout( formLayout );
    
    gridData = new GridData();
    gridData.widthHint = 300;

    numberGridData = new GridData();
    numberGridData.widthHint = 80;
    
    labelGridData = new GridData();
    labelGridData.widthHint = 300;

    urlGridData = new GridData();
    urlGridData.widthHint = 400;    
    
    processNamedCluster( this, namedCluster );
  }

  private void processNamedCluster( final Composite c, final NamedCluster cluster ) {
    
    Composite confUI = createConfigurationUI( c, cluster );

    // Create a horizontal separator
    Label topSeparator = new Label(c, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fd = new FormData( 455, 1 );
    fd.top = new FormAttachment( confUI );
    topSeparator.setLayoutData( fd );    
    
    final ScrolledComposite sc1 = new ScrolledComposite( c, SWT.V_SCROLL );
    props.setLook( sc1 );
    fd = new FormData( 445, 360 );
    fd.top = new FormAttachment( topSeparator, 15 );
    sc1.setLayoutData( fd );
    
    // Create a child composite to hold the controls
    final Composite c1 = new Composite( sc1, SWT.NONE );
    props.setLook( c1 );   
    sc1.setContent( c1 );
    c1.setLayout( new GridLayout( 1, false ) );
    
    createHdfsGroup( c1, cluster );
    createNameNodeGroup( c1, cluster );
    createJobTrackerGroup( c1, cluster );
    createZooKeeperGroup( c1, cluster );
    createOozieGroup( c1, cluster );
    
    c1.setSize( c1.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    
    // Create a horizontal separator
    Label bottomSeparator = new Label( c, SWT.HORIZONTAL | SWT.SEPARATOR );
    fd = new FormData( 455, 1 );
    fd.top = new FormAttachment( sc1, 20 );
    bottomSeparator.setLayoutData( fd );
  }

  private Composite createConfigurationUI( final Composite c, final NamedCluster namedCluster  ) {
    Composite mainParent = new Composite( c, SWT.NONE );
    props.setLook( mainParent );
    mainParent.setLayout( new GridLayout( 1, false ) );
    FormData fd = new FormData( 440, 70 );
    mainParent.setLayoutData( fd );
    
    GridData textGridData = new GridData();
    textGridData.widthHint = 250;
    
    createLabel( mainParent, BaseMessages.getString( PKG, "NamedClusterDialog.NamedCluster.Name" ), SWT.NONE );
    
    final Text nameValue = new Text( mainParent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    nameValue.setText( "" + namedCluster.getName() );
    nameValue.setLayoutData( textGridData );
    props.setLook( nameValue );
    nameValue.addKeyListener( new KeyListener() {
      public void keyReleased( KeyEvent event ) {
        namedCluster.setName( nameValue.getText() );
      }

      public void keyPressed( KeyEvent event ) {
      }
    } );
    
    return mainParent;
  }
  
  private Label createLabel( Composite parent, String text, int swtFlags ) {
    Label label = new Label( parent, SWT.NONE );
    label.setText( text );
    label.setLayoutData( labelGridData );
    props.setLook( label );
    return label;
  }
  
  private TextVar createTextVar( final NamedCluster c, Composite parent, String val, GridData gd, int flags, final Callback cb ) {
    final TextVar textVar = new TextVar( c, parent, SWT.BORDER );
    textVar.setText( val );
    textVar.setLayoutData( gd );
    props.setLook( textVar );
    
    textVar.addKeyListener( new KeyListener() {
      public void keyReleased( KeyEvent event ) {
        cb.invoke( c, textVar, textVar.getText() );
      }

      public void keyPressed( KeyEvent event ) {
      }
    } );    
    
    return textVar;
  }  
  
  private Composite createGroup( Composite parent, String groupLabel ) {
    org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group( parent, SWT.NONE );
    group.setText( groupLabel );
    group.setLayout( new RowLayout( SWT.VERTICAL ) );
    props.setLook( group );
    GridData groupGridData = new GridData();
    groupGridData.widthHint = 430;
    group.setLayoutData( groupGridData );
    
    // property parent composite
    Composite pp = new Composite( group, SWT.NONE );
    props.setLook( pp );
    pp.setLayout( new GridLayout( 1, false ) );
    return pp;
  }  
  
  private void createHdfsGroup( Composite parentComposite, final NamedCluster c ) {
    Composite pp = createGroup( parentComposite, BaseMessages.getString( PKG, "NamedClusterDialog.HDFS" ) );

    // hdfs host label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Hostname" ), SWT.NONE );
    // hdfs host input
    Callback hdfsHostCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        nc.setHdfsHost( value );
      }
    };
    createTextVar( c, pp, "" + c.getHdfsHost(), gridData, TEXT_FLAGS, hdfsHostCB );
    
    // hdfs port label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Port" ), SWT.NONE );
    // hdfs port input
    Callback hdfsPortCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        try {
          nc.setHdfsPort( Integer.parseInt( value ) );
        } catch ( NumberFormatException nfe ) {
          textVar.setText( "" + nc.getHdfsPort() );
        }
      }
    };
    createTextVar( c, pp, "" + c.getHdfsPort(), numberGridData, TEXT_FLAGS, hdfsPortCB );
    
    // hdfs user label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Username" ), SWT.NONE );
    // hdfs user input
    Callback hdfsUsernameCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        nc.setHdfsUsername( value );
      }
    };
    createTextVar( c, pp, "" + c.getHdfsUsername(), gridData, TEXT_FLAGS, hdfsUsernameCB );
    
    // hdfs password label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Password" ), SWT.NONE );
    // hdfs user input
    Callback hdfsPasswordCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        nc.setHdfsPassword( value );
      }
    };
    createTextVar( c, pp, "" + c.getHdfsPassword(), gridData, PASSWORD_FLAGS, hdfsPasswordCB );       
  }  
  
  private void createNameNodeGroup( Composite parentComposite, final NamedCluster c ) {
    Composite pp = createGroup( parentComposite, BaseMessages.getString( PKG, "NamedClusterDialog.NameNode" ) );
    
    // hdfs host label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Hostname" ), SWT.NONE );
    // hdfs host input
    Callback hostCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        nc.setNameNodeHost( value );
      }
    };
    createTextVar( c, pp, "" + c.getNameNodeHost(), gridData, TEXT_FLAGS, hostCB );
    
    // hdfs port label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Port" ), SWT.NONE );
    // hdfs port input
    Callback portCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        try {
          nc.setNameNodePort( Integer.parseInt( value ) );
        } catch ( NumberFormatException nfe ) {
          textVar.setText( "" + nc.getNameNodePort() );
        }        
      }
    };
    createTextVar( c, pp, "" + c.getNameNodePort(), numberGridData, TEXT_FLAGS, portCB );
  }    
  
  private void createJobTrackerGroup( Composite parentComposite, final NamedCluster c ) {
    Composite pp = createGroup( parentComposite, BaseMessages.getString( PKG, "NamedClusterDialog.JobTracker" ) );
    
    // hdfs host label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Hostname" ), SWT.NONE );
    // hdfs host input
    Callback hostCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        nc.setJobTrackerHost( value );
      }
    };
    createTextVar( c, pp, "" + c.getJobTrackerHost(), gridData, TEXT_FLAGS, hostCB );
    
    // hdfs port label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Port" ), SWT.NONE );
    // hdfs port input
    Callback portCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        try {
          nc.setJobTrackerPort( Integer.parseInt( value ) );
        } catch ( NumberFormatException nfe ) {
          textVar.setText( "" + nc.getJobTrackerPort() );
        }   
      }
    };
    createTextVar( c, pp, "" + c.getJobTrackerPort(), numberGridData, TEXT_FLAGS, portCB );
  }      
  
  private void createZooKeeperGroup( Composite parentComposite, final NamedCluster c ) {
    Composite pp = createGroup( parentComposite, BaseMessages.getString( PKG, "NamedClusterDialog.ZooKeeper" ) );
    
    // hdfs host label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Hostname" ), SWT.NONE );
    // hdfs host input
    Callback hostCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        nc.setZooKeeperHost( value );
      }
    };
    createTextVar( c, pp, "" + c.getZooKeeperHost(), gridData, TEXT_FLAGS, hostCB );
    
    // hdfs port label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.Port" ), SWT.NONE );
    // hdfs port input
    Callback portCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        try {
          nc.setZooKeeperPort( Integer.parseInt( value ) );
        } catch ( NumberFormatException nfe ) {
          textVar.setText( "" + nc.getZooKeeperPort() );
        }
      }
    };
    createTextVar( c, pp, "" + c.getZooKeeperPort(), numberGridData, TEXT_FLAGS, portCB );
  }    
  
  private void createOozieGroup( Composite parentComposite, final NamedCluster c ) {
    Composite pp = createGroup( parentComposite, BaseMessages.getString( PKG, "NamedClusterDialog.Oozie" ) );

    // oozie label
    createLabel( pp, BaseMessages.getString( PKG, "NamedClusterDialog.URL" ), SWT.NONE );
    // oozie url
    Callback hostCB = new Callback() {
      public void invoke( NamedCluster nc, TextVar textVar, String value ) {
        nc.setOozieUrl( value );
      }
    };
    createTextVar( c, pp, "" + c.getOozieUrl(), urlGridData, TEXT_FLAGS, hostCB );
  }     
  
}
