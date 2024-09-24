/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;

import java.util.Map;

public class PentahoEnterpriseRepoFormComposite extends BaseRepoFormComposite {

  private Text txtUrl;


  public PentahoEnterpriseRepoFormComposite( Composite parent, int style ) {
    super( parent, style );
  }

  @Override
  protected Control uiAfterDisplayName() {
    this.props = PropsUI.getInstance();

    Label lUrl = new Label( this, SWT.NONE );
    lUrl.setText( "URL" );
    lUrl.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtDisplayName, CONTROL_MARGIN ).result() );
    props.setLook( lUrl );

    txtUrl = new Text( this, SWT.BORDER );
    txtUrl.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( lUrl, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    txtUrl.addModifyListener( lsMod );
    props.setLook( txtUrl );

    return txtUrl;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> ret = super.toMap();

    ret.put( BaseRepositoryMeta.ID, "PentahoEnterpriseRepository" );

    ret.put( "url", txtUrl.getText() );

    return ret;
  }


  @SuppressWarnings( "unchecked" )
  @Override
  public void populate( JSONObject source ) {
    super.populate( source );
    txtUrl.setText( (String) source.getOrDefault( "url", "http://localhost:8080/pentaho" ) );
    props.setLook( txtUrl );
  }

  @Override
  protected boolean validateSaveAllowed() {
    return super.validateSaveAllowed() && !Utils.isEmpty( txtUrl.getText() );
  }

}
