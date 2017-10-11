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

package org.pentaho.di.job.entries.getpop;

import org.pentaho.di.i18n.BaseMessages;

/**
 * MailConnection handles the process of connecting to, reading from POP3/IMAP.
 *
 * @author Samatar
 * @since 01-04-2009
 *
 */

public class MailConnectionMeta {
  private static Class<?> PKG = JobEntryGetPOP.class; // for i18n purposes, needed by Translator2!!

  public static final String FOLDER_SEPARATOR = "/";

  public static final int PROTOCOL_POP3 = 0;
  public static final int PROTOCOL_IMAP = 1;
  public static final int PROTOCOL_MBOX = 2;

  public static final String INBOX_FOLDER = "INBOX";
  public static final String PROTOCOL_STRING_IMAP = "IMAP";
  public static final String PROTOCOL_STRING_POP3 = "POP3";
  public static final String[] protocolCodes = new String[] { "POP3", "IMAP", "MBOX" };
  public static final String PROTOCOL_STRING_MBOX = protocolCodes[PROTOCOL_MBOX];

  public static final int DEFAULT_IMAP_PORT = 110;
  public static final int DEFAULT_POP3_PORT = 110;
  public static final int DEFAULT_SSL_POP3_PORT = 995;
  public static final int DEFAULT_SSL_IMAP_PORT = 993;

  public static final String[] actionTypeDesc = new String[] {
    BaseMessages.getString( PKG, "JobGetPOP.ActionType.GetMessages.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.ActionType.MoveMessages.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.ActionType.DeleteMessages.Label" ),

  };
  public static final String[] actionTypeCode = new String[] { "get", "move", "delete" };
  public static final int ACTION_TYPE_GET = 0;
  public static final int ACTION_TYPE_MOVE = 1;
  public static final int ACTION_TYPE_DELETE = 2;

  public static final String[] conditionDateDesc = new String[] {
    BaseMessages.getString( PKG, "JobGetPOP.ConditionIgnore.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.ConditionEqual.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.ConditionSmaller.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.ConditionGreater.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.ConditionBetween.Label" )

  };
  public static final String[] conditionDateCode = new String[] {
    "ignore", "equal", "smaller", "greater", "between" };
  public static final int CONDITION_DATE_IGNORE = 0;
  public static final int CONDITION_DATE_EQUAL = 1;
  public static final int CONDITION_DATE_SMALLER = 2;
  public static final int CONDITION_DATE_GREATER = 3;
  public static final int CONDITION_DATE_BETWEEN = 4;

  public static final String[] valueIMAPListDesc = new String[] {
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetAll.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetNew.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetOld.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetRead.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetUnread.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetFlagged.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetUnFlagged.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetDraft.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetNotDraft.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetAnswered.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.IMAPListGetNotAnswered.Label" ),

  };
  public static final String[] valueIMAPListCode = new String[] {
    "imaplistall", "imaplistnew", "imaplistold", "imaplistread", "imaplistunread", "imaplistflagged",
    "imaplistnotflagged", "imaplistdraft", "imaplistnotdraft", "imaplistanswered", "imaplistnotanswered" };
  public static final int VALUE_IMAP_LIST_ALL = 0;
  public static final int VALUE_IMAP_LIST_NEW = 1;
  public static final int VALUE_IMAP_LIST_OLD = 2;
  public static final int VALUE_IMAP_LIST_READ = 3;
  public static final int VALUE_IMAP_LIST_UNREAD = 4;
  public static final int VALUE_IMAP_LIST_FLAGGED = 5;
  public static final int VALUE_IMAP_LIST_NOT_FLAGGED = 6;
  public static final int VALUE_IMAP_LIST_DRAFT = 7;
  public static final int VALUE_IMAP_LIST_NOT_DRAFT = 8;
  public static final int VALUE_IMAP_LIST_ANWERED = 9;
  public static final int VALUE_IMAP_LIST_NOT_ANSWERED = 10;

  public static final String[] afterGetIMAPDesc = new String[] {
    BaseMessages.getString( PKG, "JobGetPOP.afterGetIMAP.Nothing.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.afterGetIMAP.Delete.Label" ),
    BaseMessages.getString( PKG, "JobGetPOP.afterGetIMAP.MoveTo.Label" )

  };
  public static final String[] afterGetIMAPCode = new String[] { "nothing", "delete", "move" };
  public static final int AFTER_GET_IMAP_NOTHING = 0;
  public static final int AFTER_GET_IMAP_DELETE = 1;
  public static final int AFTER_GET_IMAP_MOVE = 2;

  public MailConnectionMeta() {
    super();
  }

  public static String getValueImapListCode( int i ) {
    if ( i < 0 || i >= valueIMAPListCode.length ) {
      return valueIMAPListCode[0];
    }
    return valueIMAPListCode[i];
  }

  public static int getConditionByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < conditionDateCode.length; i++ ) {
      if ( conditionDateCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getActionTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < actionTypeCode.length; i++ ) {
      if ( actionTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getAfterGetIMAPByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < afterGetIMAPCode.length; i++ ) {
      if ( afterGetIMAPCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getValueImapListByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < valueIMAPListCode.length; i++ ) {
      if ( valueIMAPListCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getValueListImapListByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < valueIMAPListCode.length; i++ ) {
      if ( valueIMAPListCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getActionTypeCode( int i ) {
    if ( i < 0 || i >= actionTypeCode.length ) {
      return actionTypeCode[0];
    }
    return actionTypeCode[i];
  }

  public static String getAfterGetIMAPCode( int i ) {
    if ( i < 0 || i >= afterGetIMAPCode.length ) {
      return afterGetIMAPCode[0];
    }
    return afterGetIMAPCode[i];
  }

  public static String getConditionDateCode( int i ) {
    if ( i < 0 || i >= conditionDateCode.length ) {
      return conditionDateCode[0];
    }
    return conditionDateCode[i];
  }

  public static int getValueImapListByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < valueIMAPListDesc.length; i++ ) {
      if ( valueIMAPListDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getValueImapListByCode( tt );
  }

  public static String getConditionDateDesc( int i ) {
    if ( i < 0 || i >= conditionDateDesc.length ) {
      return conditionDateDesc[0];
    }
    return conditionDateDesc[i];
  }

  public static String getActionTypeDesc( int i ) {
    if ( i < 0 || i >= actionTypeDesc.length ) {
      return actionTypeDesc[0];
    }
    return actionTypeDesc[i];
  }

  public static String getAfterGetIMAPDesc( int i ) {
    if ( i < 0 || i >= afterGetIMAPDesc.length ) {
      return afterGetIMAPDesc[0];
    }
    return afterGetIMAPDesc[i];
  }

  public static String getValueImapListDesc( int i ) {
    if ( i < 0 || i >= valueIMAPListDesc.length ) {
      return valueIMAPListDesc[0];
    }
    return valueIMAPListDesc[i];
  }

  public static int getConditionDateByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < conditionDateDesc.length; i++ ) {
      if ( conditionDateDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getConditionDateByCode( tt );
  }

  public static int getActionTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < actionTypeDesc.length; i++ ) {
      if ( actionTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getActionTypeByCode( tt );
  }

  public static int getAfterGetIMAPByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < afterGetIMAPDesc.length; i++ ) {
      if ( afterGetIMAPDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getAfterGetIMAPByCode( tt );
  }

  public static int getConditionDateByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < conditionDateCode.length; i++ ) {
      if ( conditionDateCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getProtocolFromString( String protocolCode, int defaultProtocol ) {
    if ( protocolCode == null ) {
      return defaultProtocol;
    }
    if ( protocolCode.toUpperCase().equals( PROTOCOL_STRING_IMAP ) ) {
      return PROTOCOL_IMAP;
    } else if ( protocolCode.toUpperCase().equals( PROTOCOL_STRING_POP3 ) ) {
      return PROTOCOL_POP3;
    } else if ( protocolCode.toUpperCase().equals( PROTOCOL_STRING_MBOX ) ) {
      return PROTOCOL_MBOX;
    }
    return defaultProtocol;
  }
}
