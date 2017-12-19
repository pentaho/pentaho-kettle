/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mailinput;

import org.pentaho.di.i18n.BaseMessages;

/**
 * Describes an Mail input field
 *
 * @author Samatar Hassan
 * @since 24-03-2009
 */
public class MailInputField implements Cloneable {
  private static Class<?> PKG = MailInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int COLUMN_MESSAGE_NR = 0;
  public static final int COLUMN_SUBJECT = 1;
  public static final int COLUMN_SENDER = 2;
  public static final int COLUMN_REPLY_TO = 3;
  public static final int COLUMN_RECIPIENTS = 4;
  public static final int COLUMN_DESCRIPTION = 5;
  public static final int COLUMN_BODY = 6;
  public static final int COLUMN_RECEIVED_DATE = 7;
  public static final int COLUMN_SENT_DATE = 8;
  public static final int COLUMN_CONTENT_TYPE = 9;
  public static final int COLUMN_FOLDER_NAME = 10;
  public static final int COLUMN_SIZE = 11;
  public static final int COLUMN_FLAG_NEW = 12;
  public static final int COLUMN_FLAG_READ = 13;
  public static final int COLUMN_FLAG_FLAGGED = 14;
  public static final int COLUMN_FLAG_DRAFT = 15;
  public static final int COLUMN_FLAG_DELETED = 16;
  public static final int COLUMN_ATTACHED_FILES_COUNT = 17;
  public static final int COLUMN_HEADER = 18;
  public static final int COLUMN_BODY_CONTENT_TYPE = 19;

  public static final String[] ColumnCode = {
    "messagenumber", "subject", "sender", "replyto", "recipients", "description", "body", "receiveddate",
    "sendeddate", "contenttype", "folder", "size", "flag_new", "flag_read", "flag_flagged", "flag_draft",
    "flag_deleted", "attached_files_count", "header", "body_contenttype" };

  public static final String[] ColumnDesc = {
    BaseMessages.getString( PKG, "MailInputField.Column.MessageNumber" ),
    BaseMessages.getString( PKG, "MailInputField.Column.Subject" ),
    BaseMessages.getString( PKG, "MailInputField.Column.Sender" ),
    BaseMessages.getString( PKG, "MailInputField.Column.ReplyTo" ),
    BaseMessages.getString( PKG, "MailInputField.Column.Recipients" ),
    BaseMessages.getString( PKG, "MailInputField.Column.Description" ),
    BaseMessages.getString( PKG, "MailInputField.Column.Body" ),
    BaseMessages.getString( PKG, "MailInputField.Column.ReceivedDate" ),
    BaseMessages.getString( PKG, "MailInputField.Column.SentDate" ),
    BaseMessages.getString( PKG, "MailInputField.Column.ContentType" ),
    BaseMessages.getString( PKG, "MailInputField.Column.Folder" ),
    BaseMessages.getString( PKG, "MailInputField.Column.Size" ),
    BaseMessages.getString( PKG, "MailInputField.Column.FlagNew" ),
    BaseMessages.getString( PKG, "MailInputField.Column.FlagRead" ),
    BaseMessages.getString( PKG, "MailInputField.Column.FlagFlagged" ),
    BaseMessages.getString( PKG, "MailInputField.Column.FlagDraft" ),
    BaseMessages.getString( PKG, "MailInputField.Column.FlagDeleted" ),
    BaseMessages.getString( PKG, "MailInputField.Column.AttachedFilesCount" ),
    BaseMessages.getString( PKG, "MailInputField.Column.Header" ),
    BaseMessages.getString( PKG, "MailInputField.Column.BodyContentType" ), };

  private String name;
  private int column;

  public MailInputField( String fieldname ) {
    this.name = fieldname;
    this.column = COLUMN_MESSAGE_NR;
  }

  public MailInputField() {
    this( null );
  }

  public String getColumnDesc() {
    return getColumnDesc( column );
  }

  public static final String getColumnDesc( int i ) {
    if ( i < 0 || i >= ColumnDesc.length ) {
      return ColumnDesc[0];
    }
    return ColumnDesc[i];
  }

  public int getColumn() {
    return column;
  }

  public String getColumnCode() {
    return getColumnCode( column );
  }

  public static final String getColumnCode( int i ) {
    if ( i < 0 || i >= ColumnCode.length ) {
      return ColumnCode[0];
    }
    return ColumnCode[i];
  }

  public static final int getColumnByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ColumnCode.length; i++ ) {
      if ( ColumnCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public Object clone() {
    try {
      MailInputField retval = (MailInputField) super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  public String getName() {
    return name;
  }

  public void setName( String fieldname ) {
    this.name = fieldname;
  }

  public static final int getColumnByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ColumnDesc.length; i++ ) {
      if ( ColumnDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void setColumn( int column ) {
    this.column = column;
  }

}
