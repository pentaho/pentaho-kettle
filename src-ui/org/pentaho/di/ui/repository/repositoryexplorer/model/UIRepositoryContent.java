package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.repository.Directory;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryContent;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementLocation;
import org.pentaho.di.repository.RepositoryElementLocationInterface;
import org.pentaho.di.repository.RepositoryObjectType;

public class UIRepositoryContent extends UIRepositoryObject implements RepositoryElementLocationInterface{

  private RepositoryContent rc;
  private UIRepositoryObjectRevisions revisions;
  protected UIRepositoryDirectory uiParent;
  
  public UIRepositoryContent() {
    super();
  }
  
  public UIRepositoryContent(RepositoryContent rc, UIRepositoryDirectory parent, Repository rep) {
    super(rc, rep);
    this.rc = rc;
    this.uiParent = parent;
  }
  @Override
  public String getDescription() {
    return rc.getDescription();
  }

  @Override
  public String getLockMessage() {
    return rc.getLockMessage();
  }

  @Override
  public Date getModifiedDate() {
    return rc.getModifiedDate();
  }

  @Override
  public String getModifiedUser() {
    return rc.getModifiedUser();
  }

  @Override
  public String getType() {
    return rc.getObjectType().name();
  }

  @Override
  public String getFormatModifiedDate() {
    Date date =  rc.getModifiedDate();
    String str = null;
    if (date != null){
      SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
      str = sdf.format(date);
    }
    return str;
  }

  public UIRepositoryObjectRevisions getRevisions() throws Exception{
    if (revisions != null){
      return revisions;
    }
    
    revisions = new UIRepositoryObjectRevisions();
    
    List <ObjectRevision> or = getRepository().getRevisions(this);

    for (ObjectRevision rev : or) {
      revisions.add(new UIRepositoryObjectRevision(rev));
    }
    return revisions;
  }

  // TODO: Remove references to the Kettle object RepositoryDirectory
  public RepositoryDirectory getRepositoryDirectory() {
    return (RepositoryDirectory) uiParent.getDirectory();
  }

  public RepositoryObjectType getRepositoryElementType() {
    return rc.getObjectType();
  }

  public void setName(String name) throws Exception{
    if (rc.getName().equalsIgnoreCase(name)){
      return;
    }
    rc.setName(name);
  }
  
  
  @Override
  public String getImage() {
    //TODO: a generic image for unknown content?
    return "";
  }

  @Override
  public void delete() throws Exception {
    
  }


}
