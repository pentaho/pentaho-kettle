/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.i18n.BaseMessages;

/**
 * This class is used to write export feedback. Usually it is transaction or job export result info.
 *
 * Every time this object is created it gets time created info automatically.
 *
 * Usually every ExportFeedback instance is related with one transformation or job export result. They are organized as
 * a List. In special cases when we want a record not-about job or transformation - we can set {@link #isSimpleString()}
 * to true. In this case {@link #getItemName()} will be used as a simple string. Others fields will not be used.
 *
 * To get a String representation of this item call to {@link #toString()}.
 *
 */
public final class ExportFeedback {

  private static Class<?> PKG = ExportFeedback.class;

  static SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );

  private Date time = new Date();
  private Type type;

  private Status status;
  private String itemPath;
  private String itemName;
  private List<ImportValidationFeedback> result;

  private boolean isSimpleString;

  public enum Status {
    REJECTED( BaseMessages.getString( PKG, "ExportFeedback.Status.Rejected" ) ), EXPORTED( BaseMessages.getString( PKG,
        "ExportFeedback.Status.Exported" ) );
    private String status;

    Status( String status ) {
      this.status = status;
    }

    public String getStatus() {
      return this.status;
    }
  }

  public enum Type {
    JOB( BaseMessages.getString( PKG, "ExportFeedback.Type.Job" ) ), TRANSFORMATION( BaseMessages.getString( PKG,
        "ExportFeedback.Type.Transformation" ) );
    private String type;

    Type( String type ) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }

  public Type getType() {
    return type;
  }

  public void setType( Type type ) {
    this.type = type;
  }

  public Date getTime() {
    return time;
  }

  public void setTime( Date time ) {
    this.time = time;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus( Status status ) {
    this.status = status;
  }

  public String getItemPath() {
    return itemPath;
  }

  public void setItemPath( String itemPath ) {
    this.itemPath = itemPath;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName( String itemName ) {
    this.itemName = itemName;
  }

  public List<ImportValidationFeedback> getResult() {
    return result;
  }

  public void setResult( List<ImportValidationFeedback> result ) {
    this.result = result;
  }

  public boolean isSimpleString() {
    return isSimpleString;
  }

  public void setSimpleString( boolean isSimpleString ) {
    this.isSimpleString = isSimpleString;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( itemName == null ) ? 0 : itemName.hashCode() );
    result = prime * result + ( ( itemPath == null ) ? 0 : itemPath.hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    ExportFeedback other = (ExportFeedback) obj;
    if ( itemName == null ) {
      if ( other.itemName != null ) {
        return false;
      }
    } else if ( !itemName.equals( other.itemName ) ) {
      return false;
    }
    if ( itemPath == null ) {
      if ( other.itemPath != null ) {
        return false;
      }
    } else if ( !itemPath.equals( other.itemPath ) ) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String message = null;
    if ( this.isSimpleString ) {
      message =
          BaseMessages.getString( PKG, "ExportFeedback.Message.Simple", sdf.format( this.getTime() ), this
              .getItemName() );
      sb.append( message );
      sb.append( Const.CR );
      return sb.toString();
    }
    message =
        BaseMessages.getString( PKG, "ExportFeedback.Message.Main", sdf.format( this.getTime() ), this.getStatus()
            .getStatus(), this.getItemName(), this.getItemPath() );
    sb.append( message );
    sb.append( Const.CR );

    // write some about rejected rules.
    // note - this list contains only errors in current implementation
    // so we skip additional checks. If someday this behavior will be changed -
    // that the reason why are you read this comments.
    if ( getStatus().equals( ExportFeedback.Status.REJECTED ) ) {
      List<ImportValidationFeedback> fList = this.getResult();
      for ( ImportValidationFeedback res : fList ) {
        message = BaseMessages.getString( PKG, "ExportFeedback.Message.RuleViolated", res.getComment() );
        sb.append( message );
        sb.append( Const.CR );
      }
    }
    return sb.toString();
  }
}
