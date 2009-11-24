package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryObjectRevision extends XulEventSourceAdapter  {
  
  protected ObjectRevision obj;

  public UIRepositoryObjectRevision() {
    super();
  }

  public UIRepositoryObjectRevision(ObjectRevision obj) {
    super();
    this.obj = obj;
  }
  
  public String getName() {
    return obj.getName();
  }

  public String getComment() {
    return obj.getComment();
  }

  public Date getCreationDate() {
    return obj.getCreationDate();
  }

  public String getFormatCreationDate() {
    Date date =  getCreationDate();
    String str = null;
    if (date != null){
      SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
      str = sdf.format(date);
    }
    return str;
  }

  public String getLogin() {
    return obj.getLogin();
  }
  

}
