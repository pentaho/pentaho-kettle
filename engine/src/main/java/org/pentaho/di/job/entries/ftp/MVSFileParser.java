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

package org.pentaho.di.job.entries.ftp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPFileParser;

/**
 * MVS Folder Listing Parser The purpose of this parser is to be able handle responses from an MVS z/OS mainframe FTP
 * server.
 *
 * Many places on the 'net were consulted for input to this parser. Importantly, this information from
 * com.os.os2.networking.tcp-ip group:
 *
 * http://groups.google.com/group/comp.os.os2.networking.tcp-ip/msg/25acc89563f1e93e
 * http://groups.google.com/group/comp.
 * os.os2.networking.tcp-ip/browse_frm/thread/11af1ba1bc6b0edd?hl=en&lr&ie=UTF-8&oe=UTF
 * -8&rnum=6&prev=/groups?q%3DMVS%2BPartitioned
 * %2Bdata%2Bset%2Bdirectory%26hl%3Den%26lr%3D%26ie%3DUTF-8%26oe%3DUTF-8%26selm
 * %3D4e7k0p%2524t1v%2540blackice.winternet.com%26rnum%3D6&pli=1
 * http://publibz.boulder.ibm.com/cgi-bin/bookmgr_OS390/BOOKS/F1AA2032/1.5.15?SHELF=&DT=20001127174124
 *
 * Implementation Details 1- This supports folders and partitioned data sets only. This does not support JCL or HFS 2-
 * You must treat partitioned data sets (Dsorg PO/PO-E) like folders and CD to them 3- Dsorg=PS is a downloadable file
 * as are all the contents of a Partitioned Data Set. 4- When downloading from a folder, the Recfm must start with V or
 * F.
 *
 * Note - the location for this is completely up for debate. I modeled this after the ftpsget/FTPSConnection and how
 * ftpsput reaches up and into the ftpsget package to get it. However, I think a better solution is to have an
 * entry/common. James and I agreed (in Matt's absense) to model the behavior after something already existing rather
 * than introduce a new folder (like entry/common or entry/util).
 *
 * @author mbatchelor September 2010
 *
 */

public class MVSFileParser extends FTPFileParser {

  private static Class<?> PKG = MVSFileParser.class; // for i18n purposes, needed by Translator2!!

  /*** DO NOT TRANSLATE THESE ***/
  private static final String PARSER_KEY = "MVS";
  private static final String HEADER_VOLUME = "Volume";
  private static final String HEADER_NAME = "Name";
  private static final String LINE_TYPE_ARCIVE = "ARCIVE"; // *** NOT MISSPELLED ***
  private static final String ENTRY_FILE_TYPE = "PS";
  private static final String LINE_TYPE_MIGRATED = "Migrated";
  /*** ^^^ DO NOT TRANSLATE THESE ^^^ ***/
  private static final int FOLDER_HEADER_TYPE_IDX = 0;
  private static final int FOLDER_LISTING_LENGTH_NORMAL = 10;
  private static final int FOLDER_LISTING_LENGTH_ARCIVE = 8;

  private String dateFormatString; // String used to parse file dates
  private String alternateFormatString; // Alternate form of date string in case month/day are switched
  private SimpleDateFormat dateFormat; // The DateFormat object to parse dates with
  private SimpleDateFormat dateTimeFormat; // The DateFormat object to parse "last modified" date+time with.

  private boolean partitionedDataset = false; // If true, It's a partitioned data set listing

  private LogChannelInterface log;

  public MVSFileParser( LogChannelInterface log ) {
    this.log = log;
  }

  /************************ Abstract Class Implementations *************************/

  /*
   *
   * This method decides whether this parser can handle this directory listing
   *
   * Directory listing format ------------------------ Volume Unit Referred Ext Used Recfm Lrecl BlkSz Dsorg Dsname
   * BALP4B 3390 2010/09/09 6 57 FB 80 800 PO BMS BALP8E 3390 2010/09/07 1 2 FB 80 800 PO BMS.BACKUP ARCIVE Not Direct
   * Access Device KJ.IOP998.ERROR.PL.UNITTEST USS018 3308 2010/01/15 1 15 VB 259 8000 PS NFS.DOC Migrated
   * OAQPS.INTERIM.CNTYIM.V1.DATA
   *
   * Partitioned Dataset listing format: ----------------------------------- Name VV.MM Created Changed Size Init Mod Id
   * A 01.03 2007/10/22 2009/05/27 20:18 30 3 0 TR6JAM AAA 01.01 2007/06/01 2009/01/27 03:50 183 11 0 TR6AAJ AAJSUSU
   * 01.00 2005/08/29 2005/08/29 15:11 20 20 0 TR6MGM ADERESSO 01.01 2007/03/15 2007/03/15 16:38 45 45 0 TR6CCU
   *
   *
   * Note: Date Format needs to be deciphered since for other sites it looks like this: BALP4B 3390 09/12/95 6 57 FB 80
   * 800 PO BMS
   */

  @Override
  public boolean isValidFormat( String[] listing ) {

    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Checking.Parser" ) );
    }
    if ( listing.length > 0 ) {
      String[] header = splitMVSLine( listing[0] ); // first line of MVS listings is a header
      if ( ( header.length == FOLDER_LISTING_LENGTH_NORMAL ) || ( header.length == FOLDER_LISTING_LENGTH_ARCIVE ) ) {
        if ( header[FOLDER_HEADER_TYPE_IDX].equals( HEADER_VOLUME ) ) {
          this.partitionedDataset = false; // This is a directory listing, not PDS listing
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.INFO.Detected.Dir" ) );
          }
          return isValidDirectoryFormat( listing );
        } else if ( header[FOLDER_HEADER_TYPE_IDX].equals( HEADER_NAME ) ) {
          this.partitionedDataset = true; // Suspect PDS listing.
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.INFO.Detected.PDS" ) );
          }
          return isValidPDSFormat( listing );
        }
      }
    }
    return false;
  }

  /**
   * This parses an individual line from the directory listing.
   *
   */
  @Override
  public FTPFile parse( String raw ) throws ParseException {
    String[] aLine = splitMVSLine( raw );
    FTPFile rtn = null;
    if ( this.partitionedDataset ) {
      rtn = parsePDSLine( aLine, raw ); // where the real work is done.
    } else { // Folder List
      rtn = parseFolder( aLine, raw );
    }
    return rtn;
  }

  /**
   * Could in theory be used to figure out the format of the date/time except that I'd need time on the server to see if
   * this actually works that way. For now, we ignore the locale and try to figure out the date format ourselves.
   */
  @Override
  public void setLocale( Locale arg0 ) {
    //
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Ignore.Locale" ) );
    }
  }

  /**
   * Returns parser name. By extensibility oversight in the third-party library we use, this isn't used to match the on
   * the server (unfortunately).
   */
  public String toString() {
    return PARSER_KEY;
  }

  /************************ Worker Methods *************************/

  /**
   * Parses a Partitioned Dataset Entry, and returns an FTPFile object.
   *
   * @param aLine
   *          Split line
   * @param raw
   *          Unparsed raw string
   * @return FTPFile unless it's the header row.
   * @throws ParseException
   */
  protected FTPFile parsePDSLine( String[] aLine, String raw ) throws ParseException {
    FTPFile rtn = null;
    if ( aLine[0].equals( HEADER_NAME ) ) {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Skip.Header" ) );
      }
      return null;
    }
    rtn = new FTPFile( raw );
    rtn.setName( aLine[0] );
    if ( dateTimeFormat == null ) {
      dateTimeFormat = new SimpleDateFormat( dateFormatString + " HH:mm" );
    }
    rtn.setCreated( dateFormat.parse( aLine[2] ) );
    String modDateTime = aLine[3] + ' ' + aLine[4];
    rtn.setLastModified( dateTimeFormat.parse( modDateTime ) );
    rtn.setDir( false );
    return rtn;
  }

  /**
   * Parses a line from a folder listing.
   *
   * Note: Returns NULL if it's the header line, if it is ARCIVE or Migrated, if the record format doesn't start with
   * 'F' or 'V', and if the dsorg doesn't start with 'P'.
   *
   * @param aLine
   *          Line split apart
   * @param raw
   *          Raw line from the transport
   * @return FTPFile for the line unless it is expressly exluded
   */
  protected FTPFile parseFolder( String[] aLine, String raw ) {
    if ( aLine[0].equals( HEADER_VOLUME ) ) {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Skip.Header" ) );
      }
      return null;
    }
    // Directory format
    if ( aLine[0].equals( LINE_TYPE_ARCIVE ) ) { // It's on tape somewhere
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Skip.ARCIVE" ) );
      }
      return null;
    }
    if ( aLine[0].equals( LINE_TYPE_MIGRATED ) ) { // It's been moved.
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Skip.Migrated" ) );
      }
      return null;
    }
    if ( aLine[5].charAt( 0 ) != 'F' && aLine[5].charAt( 0 ) != 'V' ) {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Skip.recf" ) );
      }
      return null;
    }
    if ( aLine[8].charAt( 0 ) != 'P' ) { // Only handle PO, PS, or PO-E
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Skip.dso" ) );
      }
      return null;
    }
    // OK, I think I can handle this.
    FTPFile rtn = new FTPFile( raw );
    rtn.setName( aLine[9] );
    // Fake out dates - these are all newly created files / folders
    rtn.setCreated( new Date() );
    rtn.setLastModified( new Date() );
    if ( aLine[8].equals( ENTRY_FILE_TYPE ) ) {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Found.File", aLine[9] ) );
      }
      // This is a file...
      rtn.setDir( false );
      long l = -1;
      try {
        l = Long.parseLong( aLine[4] );
      } catch ( Exception ignored ) {
        // Ignore errors
      }
      rtn.setSize( l );
    } else {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Found.Folder", aLine[9] ) );
      }
      rtn.setDir( true );
    }
    // Left this code here in case last time accessed becomes important.
    // For directory items, this is just the last time accessed
    // Date dt = dateFormat.parse(aLine[2]);
    //
    return rtn;
  }

  /************************ Utility Methods *************************/

  /**
   * This is a split + trim function. The String.split method doesn't work well if there are a multiple contiguous
   * white-space characters. StringTokenizer handles this very well. This should never fail to return an array, even if
   * the array is empty. In other words, this should never return null.
   *
   * @param raw
   *          The string to tokenize from the MainFrame
   * @return String array of all the elements from the parse.
   */
  protected String[] splitMVSLine( String raw ) {
    if ( raw == null ) {
      return new String[] {};
    }
    StringTokenizer st = new StringTokenizer( raw );
    String[] rtn = new String[st.countTokens()];
    int i = 0;
    while ( st.hasMoreTokens() ) {
      String nextToken = st.nextToken();
      rtn[i] = nextToken.trim();
      i++;
    }
    return rtn;
  }

  /**
   * Returns true if this seems to be a recognized MVS folder (not PDS) listing.
   *
   * @param listing
   * @return true if by all appearances this is a listing of an MVS folder
   */
  protected boolean isValidDirectoryFormat( String[] listing ) {
    String[] aLine;
    for ( int i = 1; i < listing.length; i++ ) {
      aLine = splitMVSLine( listing[i] );
      if ( ( aLine.length == 2 ) && ( aLine[0].equals( LINE_TYPE_MIGRATED ) ) ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Detected.Migrated" ) );
        }
      } else if ( aLine.length != 10 && ( !aLine[0].equals( LINE_TYPE_ARCIVE ) ) ) { // 10 = regular, ARCIVE=on tape
        log.logError( BaseMessages.getString( PKG, "MVSFileParser.ERROR.Invalid.Folder.Line", listing[i] ) );
        return false;
      }
      if ( dateFormatString != null ) {
        // validate date
        if ( !checkDateFormat( aLine[2] ) ) {
          return false;
        }
      } else {
        if ( aLine.length == 10 ) {
          // Try to parse the date.
          guessDateFormat( aLine[2] );
        }
      }
    }
    return true;
  }

  /**
   * Returns true if this seems to be a recognized MVS PDS listing (not folder).
   *
   * @param listing
   * @return true if by all appearances this is a listing of the contents of a PDS
   */
  protected boolean isValidPDSFormat( String[] listing ) {
    String[] aLine;
    for ( int i = 1; i < listing.length; i++ ) {
      aLine = splitMVSLine( listing[i] );
      if ( aLine.length != 9 ) { // 9 because there are two fields for changed...
        log.logError( BaseMessages.getString( PKG, "MVSFileParser.ERROR.Invalid.PDS.Line", listing[i] ) );
        return false;
      }
      if ( dateFormatString != null ) {
        if ( !checkDateFormat( aLine[3] ) ) {
          return false;
        }
      } else {
        guessDateFormat( aLine[2] );
      }
    }
    return true;
  }

  /*
   * This method will try the date format string to make sure it knows how to parse the dates. If it fails a parse it
   * will try the alternate format if available. For example, if the first three files have these dates: 2010/03/04
   * 2010/07/09 2010/23/06
   *
   * For the first two, either yyyy/MM/dd or yyyy/dd/MM would work. When the parse on 2010/23/06 fails, it will try the
   * alternate, succeed, and carry on.
   *
   * The weakness of this approach is if all files have valid inter- changable day/month on all dates. In that case, all
   * would be detected as yyyy/MM/dd which may be incorrect. If this is a problem, the correct fix is to set the date
   * format on the parser, or play with the Locale and see if that can be used to figure out what the real format from
   * the server is.
   */
  protected boolean checkDateFormat( String dateStr ) {
    try {
      dateFormat.parse( dateStr );
    } catch ( ParseException ex ) {
      if ( log.isDebug() ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Date.Parse.Error" ) );
        }
      }
      if ( ( alternateFormatString != null ) ) {
        if ( log.isDebug() ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Date.Parse.Choose.Alt" ) );
          }
        }
        dateFormatString = alternateFormatString;
        dateFormat = new SimpleDateFormat( dateFormatString );
        alternateFormatString = null;
        try {
          dateFormat.parse( dateStr );
        } catch ( ParseException ex2 ) {
          return false;
        }
      } else {
        log.logError( BaseMessages.getString( PKG, "MVSFileParser.ERROR.Date.Parse.Fail", dateStr ) );
        return false;
      }
    }
    return true;
  }

  /**
   * This method will look at the incoming date string and try to figure out the format of the date. Googling on the
   * internet showed several possible looks to the date:
   *
   * dd/MM/yy yy/MM/dd MM/dd/yy yyyy/MM/dd yyyy/dd/MM
   *
   * I never saw samples showing dd/MM/yyyy but I suppose it's possible. Not happy with this algorithm because it feels
   * clumsy. It works, but it's not very elegant (time crunch).
   *
   * @param dateStr
   */
  protected void guessDateFormat( String dateStr ) {
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Guess.Date" ) );
    }
    String[] dateSplit = dateStr.split( "/" );
    String yrFmt = null;
    int yrPos = -1;
    int dayPos = -1;
    // quick look for either yyyy/xx/xx or xx/xx/yyyy
    for ( int i = 0; i < dateSplit.length; i++ ) {
      int aDigit = Integer.parseInt( dateSplit[i] );
      if ( dateSplit[i].length() == 4 ) {
        yrFmt = "yyyy";
        yrPos = i;
      } else if ( aDigit > 31 ) {
        // found 2-digit year
        yrFmt = "yy";
        yrPos = i;
      } else if ( aDigit > 12 ) {
        // definitely found a # <=31,
        dayPos = i;
      }
    }
    if ( yrFmt != null ) {
      StringBuilder fmt = new StringBuilder();
      if ( dayPos >= 0 ) {
        // OK, we know everything.
        String[] tmp = new String[3];
        tmp[yrPos] = yrFmt;
        tmp[dayPos] = "dd";
        for ( int i = 0; i < tmp.length; i++ ) {
          fmt.append( i > 0 ? "/" : "" );
          fmt.append( tmp[i] == null ? "MM" : tmp[i] );
        }
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Guess.Date.Obvious" ) );
        }
      } else {
        // OK, we have something like 2010/01/01 - I can't
        // tell month from day. So, we'll guess. If it doesn't work on a later
        // date, we'll flip it (the alternate).

        StringBuilder altFmt = new StringBuilder();
        if ( yrPos == 0 ) {
          fmt.append( yrFmt ).append( "/MM/dd" );
          altFmt.append( yrFmt ).append( "/dd/MM" );
        } else {
          fmt.append( "MM/dd/" ).append( yrFmt );
          altFmt.append( "dd/MM/" ).append( yrFmt );
        }
        this.alternateFormatString = altFmt.toString();
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Guess.Date.Ambiguous" ) );
        }
      }
      this.dateFormatString = fmt.toString();
      this.dateFormat = new SimpleDateFormat( dateFormatString );
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages
          .getString( PKG, "MVSFileParser.DEBUG.Guess.Date.Decided", this.dateFormatString ) );
      }
      try {
        dateFormat.parse( dateStr );
      } catch ( ParseException ex ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Guess.Date.Unparsable", dateStr ) );
        }
      }
    } else {
      // looks ilke something like 01/02/05 - where's the year?
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MVSFileParser.DEBUG.Guess.Date.Year.Ambiguous" ) );
      }
      return;
    }

  }

  /*************************** Getters and Setters **************************/

  /**
   * @return true if listing is a PDS
   */
  public boolean isPartitionedDataset() {
    return this.partitionedDataset;
  }

  /**
   * Returns the date format string in use for parsing date in the listing.
   *
   * @return string format
   */
  public String getDateFormatString() {
    return this.dateFormatString;
  }

  /**
   * Provides ability to pre-specify the format that the parser will use to parse dates.
   *
   * @param value
   *          the string to set.
   */
  public void setDateFormatString( String value ) {
    this.dateFormatString = value;
  }

}
