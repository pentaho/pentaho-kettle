package org.pentaho.di.ui.repo.dialog.dialogdynamic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;

import java.util.ArrayList;
import java.util.Map;

public class KettleDatabaseRepoFormComposite extends BaseRepoFormComposite {

  private Combo dbListCombo;
  private DatabaseDialog databaseDialog;


  public KettleDatabaseRepoFormComposite( Composite parent, int style )
  {
    super( parent, style );
  }

  @Override
  protected Control uiAfterDisplayName() {
    PropsUI props = PropsUI.getInstance();

    Label lLoc = new Label( this, SWT.NONE );
    lLoc.setText( "Database Connection" );
    lLoc.setLayoutData( new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtDisplayName, CONTROL_MARGIN ).result() );
    props.setLook( lLoc );

    dbListCombo = new Combo(this, SWT.READ_ONLY);
    System.out.println(" list fo dbs"+RepositoryConnectController.getInstance().getDatabases());
    if(RepositoryConnectController.getInstance().getDatabases().isEmpty()){
      System.out.println("db list is empty");
      dbListCombo.setItems( new String[] { "none"} );
    }
    else{
      JSONArray jsonArray = RepositoryConnectController.getInstance().getDatabases();
      ArrayList<String> listdata = new ArrayList<String>();
      System.out.println("json string :"+jsonArray.toJSONString());

      for (int i=0;i<jsonArray.size();i++){

        System.out.println(jsonArray.get( i ));
        listdata.add((String) jsonArray.get( i ));

      }
      if(listdata !=null && listdata.size()>0){
        System.out.println("Printing List Array");
        listdata.forEach(l -> System.out.println(l));
      }
      //dbListCombo.setItems( new String[] { "item 1", "item 2", "item 3" });
      dbListCombo.setItems( listdata.toArray(new String[listdata.size()]) );
    }

    //dbListCombo.setText( "none" );
    // dbListCombo.setItems( RepositoryConnectController.getInstance().getDatabases().toJSONString() );
    dbListCombo.setLayoutData( new FormDataBuilder().left( 0, 0 ).top( lLoc, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    dbListCombo.addModifyListener( lsMod );
    props.setLook( dbListCombo );


    Button createDbConBtn = new Button( this,SWT.PUSH );
    // TODO: BaseMessages
    createDbConBtn.setText( "create connection" );
    createDbConBtn.setLayoutData( new FormDataBuilder().left( dbListCombo, LABEL_CONTROL_MARGIN ).top( lLoc, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( createDbConBtn );

    createDbConBtn.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        DatabaseDialog databaseDialog = new DatabaseDialog( getShell(), new DatabaseMeta() );
        databaseDialog.open();
        DatabaseMeta databaseMeta = databaseDialog.getDatabaseMeta();
        //DatabaseMeta databaseMeta = new DatabaseMeta();
        //databaseMeta.initializeVariablesFrom( null );
        getDatabaseDialog().setDatabaseMeta( databaseMeta );
        //String name = getDatabaseDialog().open();
      }
    } );

    return createDbConBtn;
  }

  private DatabaseDialog getDatabaseDialog() {
    if ( databaseDialog != null ) {
      return databaseDialog;
    }
    databaseDialog = new DatabaseDialog( getShell() );
    return databaseDialog;
  }

  @Override
  public Map<String,Object> toMap() {
    Map<String,Object> ret = super.toMap();

    //TODO: Change to PurRepositoryMeta.REPOSITORY_TYPE_ID
    ret.put( BaseRepositoryMeta.ID, "KettleFileRepository" );
    //TODO: Change to PurRepositoryMeta.URL
    //ret.put( KettleFileRepositoryMeta.LOCATION, txtLocation.getText() );
    /*ret.put( KettleFileRepositoryMeta.SHOW_HIDDEN_FOLDERS,showHidden.getSelection() );
    ret.put( KettleFileRepositoryMeta.DO_NOT_MODIFY,doNotModify.getSelection());
*/
    return ret;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public void populate( JSONObject source ) {
    super.populate( source );
    //  txtLocation.setText( (String) source.getOrDefault( KettleFileRepositoryMeta.LOCATION, "" ) );
    //  showHidden.setSelection( (Boolean) source.getOrDefault( KettleFileRepositoryMeta.SHOW_HIDDEN_FOLDERS, false ) );
    // doNotModify.setSelection( (Boolean) source.getOrDefault( KettleFileRepositoryMeta.DO_NOT_MODIFY, false ) );
  }

  @Override
  protected boolean validateSaveAllowed() {
    return super.validateSaveAllowed() && !Utils.isEmpty( dbListCombo.getSelection().toString());
  }

}
