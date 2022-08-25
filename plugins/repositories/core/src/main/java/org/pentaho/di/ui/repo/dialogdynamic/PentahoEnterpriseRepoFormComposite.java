package org.pentaho.di.ui.repo.dialogdynamic;

import java.util.Map;
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

public class PentahoEnterpriseRepoFormComposite extends BaseRepoFormComposite {

  protected Text txtUrl;
  protected PropsUI props;
  
  public PentahoEnterpriseRepoFormComposite( Composite parent, int style )
  {
    super( parent, style );
    this.props=PropsUI.getInstance();
  }
  
  @Override
  protected Control uiAfterDisplayName() {
    this.props = PropsUI.getInstance();

    Label lUrl = new Label( this, SWT.NONE );
    lUrl.setText( "URL" );
    lUrl.setLayoutData( new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtDisplayName, CONTROL_MARGIN ).result() );
    props.setLook( lUrl );

    txtUrl = new Text( this, SWT.BORDER );
    txtUrl.setLayoutData( new FormDataBuilder().left( 0, 0 ).top( lUrl, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    txtUrl.addModifyListener( lsMod );
    props.setLook( txtUrl );

    return txtUrl;
  }

  @Override
  public Map<String,Object> toMap() {
    Map<String,Object> ret = super.toMap();
    
    //TODO: Change to PurRepositoryMeta.REPOSITORY_TYPE_ID
    ret.put( BaseRepositoryMeta.ID, "PentahoEnterpriseRepository" );
    //TODO: Change to PurRepositoryMeta.URL
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
