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
  private Directory parent;
  
  public UIRepositoryContent() {
    super();
  }
  
  public UIRepositoryContent(RepositoryContent rc, Directory parent, Repository rep) {
    super(rc, rep);
    this.rc = rc;
    this.parent = parent;
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

  public Directory getParent() {
    return parent;
  }

  public void setParent(Directory parent) {
    this.parent = parent;
  }
  
  public List<UIRepositoryObjectRevision> getRevisions() throws Exception{
    List<UIRepositoryObjectRevision> returnRevs = new ArrayList<UIRepositoryObjectRevision>();
    
    List <ObjectRevision> or = getRepository().getRevisions(this);

    for (ObjectRevision rev : or) {
      returnRevs.add(new UIRepositoryObjectRevision(rev));
    }
    return returnRevs;
  }

  // TODO: Remove references to the Kettle object RepositoryDirectory
  public RepositoryDirectory getRepositoryDirectory() {
    return (RepositoryDirectory) parent;
  }

  public RepositoryObjectType getRepositoryElementType() {
    return rc.getObjectType();
  }

}
