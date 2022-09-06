package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import java.util.ArrayList;
import java.util.Map;

public class KettleDatabaseRepoFormComposite extends BaseRepoFormComposite {

  private Combo dbListCombo;
  protected Runnable dbListRefresh;
  private static final Class<?> PKG = KettleDatabaseRepoFormComposite.class;

  public KettleDatabaseRepoFormComposite( Composite parent, int style )
  {
    super( parent, style );
  }

  @Override
  protected Control uiAfterDisplayName() {
    PropsUI props = PropsUI.getInstance();

    Label lLoc = new Label( this, SWT.NONE );
    lLoc.setText( BaseMessages.getString( PKG, "repositories.databaseconnection.label" ));
    lLoc.setLayoutData( new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtDisplayName, CONTROL_MARGIN ).result() );
    props.setLook( lLoc );

    dbListCombo = new Combo(this, SWT.READ_ONLY);
    if(RepositoryConnectController.getInstance().getDatabases().isEmpty()){
      dbListCombo.setItems( new String[] {"none"} );
    }
    else{
      ArrayList<String> listData = convertJSONArrayToList(RepositoryConnectController.getInstance().getDatabases());
      dbListCombo.setItems( listData.toArray( new String[listData.size()] ) );
    }

    dbListCombo.setLayoutData( new FormDataBuilder().left( 0, 0 ).top( lLoc, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    dbListCombo.addModifyListener( lsMod );
    props.setLook( dbListCombo );


    Button createDbConBtn = new Button( this,SWT.PUSH );
    createDbConBtn.setText( BaseMessages.getString( PKG, "repositories.create.label" ));
    createDbConBtn.setLayoutData( new FormDataBuilder().left( dbListCombo, LABEL_CONTROL_MARGIN ).top( lLoc, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( createDbConBtn );

    Button updateDbConBtn = new Button( this,SWT.PUSH );
    updateDbConBtn.setText( BaseMessages.getString( PKG, "repositories.edit.label" ) );
    updateDbConBtn.setLayoutData( new FormDataBuilder().left( createDbConBtn, LABEL_CONTROL_MARGIN ).top( lLoc, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( updateDbConBtn );

    updateDbConBtn.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        RepositoryConnectController.getInstance().editDatabaseConnection((dbListCombo.getItem(dbListCombo.getSelectionIndex())));
        dbListRefresh.run();
      }
    } );

    Button deleteDbConBtn = new Button( this,SWT.PUSH );
    deleteDbConBtn.setText( BaseMessages.getString( PKG, "repositories.delete.label" ) );
    deleteDbConBtn.setLayoutData( new FormDataBuilder().left( updateDbConBtn, LABEL_CONTROL_MARGIN ).top( lLoc, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( deleteDbConBtn );

    deleteDbConBtn.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        RepositoryConnectController.getInstance().deleteDatabaseConnection(dbListCombo.getItem(dbListCombo.getSelectionIndex()));
        dbListRefresh.run();
      }
    } );

    createDbConBtn.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        RepositoryConnectController.getInstance().createConnection();
        dbListRefresh.run();
      }
    } );

    dbListRefresh = () -> {
      ArrayList<String> listData = convertJSONArrayToList(RepositoryConnectController.getInstance().getDatabases());
      dbListCombo.setItems( listData.toArray( new String[listData.size()] ) );
    };
    return createDbConBtn;
  }

  private ArrayList<String> convertJSONArrayToList(JSONArray jsonArray){
    ArrayList<String> listData = new ArrayList<String>();
    if(!jsonArray.isEmpty()) {
      for (int i = 0; i < jsonArray.size(); i++) {
        listData.add(((JSONObject) jsonArray.get(i)).get("name").toString());
      }
    }
    return listData;
  }

  @Override
  public Map<String,Object> toMap() {
    Map<String,Object> ret = super.toMap();

    //TODO: Change to PurRepositoryMeta.REPOSITORY_TYPE_ID
    ret.put( BaseRepositoryMeta.ID, "KettleDatabaseRepository" );
    //TODO: Change to PurRepositoryMeta.URL
    ret.put( KettleDatabaseRepositoryMeta.DATABASE_CONNECTION,dbListCombo.getItem( dbListCombo.getSelectionIndex() ) );

    return ret;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public void populate( JSONObject source ) {
    super.populate( source );
}

  @Override
  protected boolean validateSaveAllowed() {
    return super.validateSaveAllowed() && !Utils.isEmpty( dbListCombo.getSelection().toString());
  }
}
