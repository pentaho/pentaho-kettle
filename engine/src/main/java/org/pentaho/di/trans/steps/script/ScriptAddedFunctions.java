//CHECKSTYLE:FileLength:OFF
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

package org.pentaho.di.trans.steps.script;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

public class ScriptAddedFunctions {

  public static Object undefinedValue = null;

  public static final long serialVersionUID = 1L;

  public static final int STRING_FUNCTION = 0;
  public static final int NUMERIC_FUNCTION = 1;
  public static final int DATE_FUNCTION = 2;
  public static final int LOGIC_FUNCTION = 3;
  public static final int SPECIAL_FUNCTION = 4;
  public static final int FILE_FUNCTION = 5;

  public static String[] jsFunctionList = {
    "appendToFile", "getTransformationName", "writeToLog", "getFiscalDate", "getProcessCount", "ceil", "floor",
    "abs", "getDayNumber", "isWorkingDay", "fireToDB", "getNextWorkingDay", "quarter", "dateDiff", "dateAdd",
    "fillString", "isCodepage", "ltrim", "rtrim", "lpad", "rpad", "week", "month", "year", "str2RegExp",
    "fileExists", "touch", "isRegExp", "date2str", "str2date", "sendMail", "replace", "decode", "isNum",
    "isDate", "lower", "upper", "str2num", "num2str", "Alert", "setEnvironmentVar", "getEnvironmentVar",
    "LoadScriptFile", "LoadScriptFromTab", "print", "println", "resolveIP", "trim", "substr", "getVariable",
    "setVariable", "LuhnCheck", "getDigitsOnly", "indexOf", "getOutputRowMeta", "getInputRowMeta",
    "createRowCopy", "putRow", "deleteFile", "createFolder", "copyFile", "getFileSize", "isFile", "isFolder",
    "getShortFilename", "getFileExtension", "getParentFoldername", "getLastModifiedTime", "trunc", "truncDate",
    "moveFile", };

  // This is only used for reading, so no concurrency problems.
  // todo: move in the real variables of the step.
  // private static VariableSpace variables = Variables.getADefaultVariableSpace();

  // Functions to Add
  // date2num, num2date,
  // fisc_date, isNull
  //

  public static String getDigitsOnly( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.getDigitsOnly( (String) ArgList[0] ); // TODO AKRETION ensure
    } else {
      throw new RuntimeException( "The function call getDigitsOnly requires 1 argument." );

    }
  }

  public static boolean LuhnCheck( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    boolean returnCode = false;

    if ( ArgList.length == 1 ) {
      if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
        try {
          int sum = 0;
          int digit = 0;
          int addend = 0;
          boolean timesTwo = false;
          String argstring = (String) ArgList[0];

          for ( int i = argstring.length() - 1; i >= 0; i-- ) {
            digit = Integer.parseInt( argstring.substring( i, i + 1 ) );
            if ( timesTwo ) {
              addend = digit * 2;
              if ( addend > 9 ) {
                addend -= 9;
              }
            } else {
              addend = digit;
            }
            sum += addend;
            timesTwo = !timesTwo;
          }

          int modulus = sum % 10;
          if ( modulus == 0 ) {
            returnCode = true;
          }
        } catch ( Exception e ) {
          // No Need to throw exception
          // This means that input can not be parsed to Integer
        }

      }
    } else {
      throw new RuntimeException( "The function call LuhnCheck requires 1 argument." );

    }
    return returnCode;
  }

  public static int indexOf( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    int returnIndex = -1;

    if ( ArgList.length == 2 || ArgList.length == 3 ) {
      if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
        String string = (String) ArgList[0];
        String subString = (String) ArgList[1];

        int fromIndex = 0;
        if ( ArgList.length == 3 ) {
          fromIndex = (int) Math.round( (Double) ArgList[2] );
        }
        returnIndex = string.indexOf( subString, fromIndex );
      }
    } else {
      throw new RuntimeException( "The function call indexOf requires 2 or 3 arguments" );
    }
    return returnIndex;
  }

  public static Object getTransformationName( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      Object objTranName = actualObject.get( "_TransformationName_" );
      return objTranName;
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static void appendToFile( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
      try {
        FileOutputStream file = new FileOutputStream( (String) ArgList[0], true );
        DataOutputStream out = new DataOutputStream( file );
        out.writeBytes( (String) ArgList[1] );
        out.flush();
        out.close();
      } catch ( Exception er ) {
        throw new RuntimeException( er.toString() );
      }
    } else {
      throw new RuntimeException( "The function call appendToFile requires arguments." );
    }
  }

  public static Object getFiscalDate( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList ) ) {
          return null;
        } else if ( isUndefined( ArgList ) ) {
          return undefinedValue;
        }
        java.util.Date dIn = (java.util.Date) ArgList[0];
        Calendar startDate = Calendar.getInstance();
        Calendar fisStartDate = Calendar.getInstance();
        Calendar fisOffsetDate = Calendar.getInstance();
        startDate.setTime( dIn );
        Format dfFormatter = new SimpleDateFormat( "dd.MM.yyyy" );
        String strOffsetDate = (String) ArgList[1] + String.valueOf( startDate.get( Calendar.YEAR ) );
        java.util.Date dOffset = (java.util.Date) dfFormatter.parseObject( strOffsetDate );
        fisOffsetDate.setTime( dOffset );

        String strFisStartDate = "01.01." + String.valueOf( startDate.get( Calendar.YEAR ) + 1 );
        fisStartDate.setTime( (java.util.Date) dfFormatter.parseObject( strFisStartDate ) );
        int iDaysToAdd = (int) ( ( startDate.getTimeInMillis() - fisOffsetDate.getTimeInMillis() ) / 86400000 );
        fisStartDate.add( Calendar.DATE, iDaysToAdd );
        return fisStartDate.getTime();
      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call getFiscalDate requires 2 arguments." );
    }

  }

  public static double getProcessCount( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    if ( ArgList.length == 1 ) {
      try {
        Object scmO = actualObject.get( "_step_" );
        ScriptInterface scm = (ScriptInterface) scmO;
        String strType = ( (String) ArgList[0] ).toLowerCase();

        if ( strType.equals( "i" ) ) {
          return scm.getLinesInput();
        } else if ( strType.equals( "o" ) ) {
          return scm.getLinesOutput();
        } else if ( strType.equals( "r" ) ) {
          return scm.getLinesRead();
        } else if ( strType.equals( "u" ) ) {
          return scm.getLinesUpdated();
        } else if ( strType.equals( "w" ) ) {
          return scm.getLinesWritten();
        } else if ( strType.equals( "e" ) ) {
          return scm.getLinesRejected();
        } else {
          return 0;
        }
      } catch ( Exception e ) {
        // throw new RuntimeException(e.toString());
        return 0;
      }
    } else {
      throw new RuntimeException( "The function call getProcessCount requires 1 argument." );
    }
  }

  public static void writeToLog( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    switch ( ArgList.length ) {
      case 1:
        try {
          if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
            Object scmO = actualObject.get( "_step_" );
            Script scm = (Script) scmO;
            String strMessage = (String) ArgList[0];
            scm.logDebug( strMessage );
          }
        } catch ( Exception e ) {
          // Ignore errors
        }
        break;
      case 2:
        try {
          if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
            Object scmO = actualObject.get( "_step_" );
            Script scm = (Script) scmO;

            String strType = ( (String) ArgList[0] ).toLowerCase();
            String strMessage = (String) ArgList[1];
            if ( strType.equals( "b" ) ) {
              scm.logBasic( strMessage );
            } else if ( strType.equals( "d" ) ) {
              scm.logDebug( strMessage );
            } else if ( strType.equals( "l" ) ) {
              scm.logDetailed( strMessage );
            } else if ( strType.equals( "e" ) ) {
              scm.logError( strMessage );
            } else if ( strType.equals( "m" ) ) {
              scm.logMinimal( strMessage );
            } else if ( strType.equals( "r" ) ) {
              scm.logRowlevel( strMessage );
            }
          }
        } catch ( Exception e ) {
          // Ignore errors
        }
        break;
      default:
        throw new RuntimeException( "The function call writeToLog requires 1 or 2 arguments." );
    }
  }

  private static boolean isUndefined( Object ArgList ) {
    return isUndefined( new Object[] { ArgList }, new int[] { 0 } );
  }

  private static boolean isUndefined( Object[] ArgList, int[] iArrToCheck ) {
    for ( int i = 0; i < iArrToCheck.length; i++ ) {
      if ( ArgList[iArrToCheck[i]].equals( undefinedValue ) ) {
        return true;
      }
    }
    return false;
  }

  private static boolean isNull( Object ArgList ) {
    return isNull( new Object[] { ArgList }, new int[] { 0 } );
  }

  private static boolean isNull( Object[] ArgList ) {
    for ( int i = 0; i < ArgList.length; i++ ) {
      if ( ArgList[i] == null || ArgList[i].equals( null ) ) {
        return true;
      }
    }
    return false;
  }

  private static boolean isNull( Object[] ArgList, int[] iArrToCheck ) {
    for ( int i = 0; i < iArrToCheck.length; i++ ) {
      if ( ArgList[iArrToCheck[i]] == null || ArgList[iArrToCheck[i]].equals( null ) ) {
        return true;
      }
    }
    return false;
  }

  public static Object abs( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        } else {
          return new Double( Math.abs( (Double) ArgList[0] ) );
        }
      } catch ( Exception e ) {
        return null;
      }
    } else {
      throw new RuntimeException( "The function call abs requires 1 argument." );
    }
  }

  public static Object ceil( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        } else {
          return new Double( Math.ceil( (Double) ArgList[0] ) );
        }
      } catch ( Exception e ) {
        return null;
      }
    } else {
      throw new RuntimeException( "The function call ceil requires 1 argument." );
    }
  }

  public static Object floor( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        } else {
          return new Double( Math.floor( (Double) ArgList[0] ) );
        }
      } catch ( Exception e ) {
        return null;
        // throw new RuntimeException(e.toString());
      }
    } else {
      throw new RuntimeException( "The function call floor requires 1 argument." );
    }
  }

  public static Object getDayNumber( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        } else {
          java.util.Date dIn = (java.util.Date) ArgList[0];
          String strType = ( (String) ArgList[1] ).toLowerCase();
          Calendar startDate = Calendar.getInstance();
          startDate.setTime( dIn );
          if ( strType.equals( "y" ) ) {
            return new Double( startDate.get( Calendar.DAY_OF_YEAR ) );
          } else if ( strType.equals( "m" ) ) {
            return new Double( startDate.get( Calendar.DAY_OF_MONTH ) );
          } else if ( strType.equals( "w" ) ) {
            return new Double( startDate.get( Calendar.DAY_OF_WEEK ) );
          } else if ( strType.equals( "wm" ) ) {
            return new Double( startDate.get( Calendar.DAY_OF_WEEK_IN_MONTH ) );
          }
          return new Double( startDate.get( Calendar.DAY_OF_YEAR ) );
        }
      } catch ( Exception e ) {
        return null;
        // throw new RuntimeException(e.toString());
      }
    } else {
      throw new RuntimeException( "The function call getDayNumber requires 2 arguments." );
    }
  }

  public static Object isWorkingDay( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        } else {
          java.util.Date dIn = (java.util.Date) ArgList[0];
          Calendar startDate = Calendar.getInstance();
          startDate.setTime( dIn );
          if ( startDate.get( Calendar.DAY_OF_WEEK ) != Calendar.SATURDAY
            && startDate.get( Calendar.DAY_OF_WEEK ) != Calendar.SUNDAY ) {
            return Boolean.TRUE;
          }
          return Boolean.FALSE;
        }
      } catch ( Exception e ) {
        return null;
      }
    } else {
      throw new RuntimeException( "The function call isWorkingDay requires 1 argument." );
    }
  }

  @SuppressWarnings( "unused" )
  public static Object fireToDB( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    Object oRC = new Object();
    if ( ArgList.length == 2 ) {
      try {
        Object scmO = actualObject.get( "_step_" );
        Script scm = (Script) scmO;
        String strDBName = (String) ArgList[0];
        String strSQL = (String) ArgList[1];
        DatabaseMeta ci = DatabaseMeta.findDatabase( scm.getTransMeta().getDatabases(), strDBName );
        if ( ci == null ) {
          throw new RuntimeException( "Database connection not found: " + strDBName );
        }
        ci.shareVariablesWith( scm );

        Database db = new Database( scm, ci );
        db.setQueryLimit( 0 );
        try {
          if ( scm.getTransMeta().isUsingUniqueConnections() ) {
            synchronized ( scm.getTrans() ) {
              db.connect( scm.getTrans().getTransactionId(), scm.getPartitionID() );
            }
          } else {
            db.connect( scm.getPartitionID() );
          }

          ResultSet rs = db.openQuery( strSQL );
          ResultSetMetaData resultSetMetaData = rs.getMetaData();
          int columnCount = resultSetMetaData.getColumnCount();
          if ( rs != null ) {
            List<Object[]> list = new ArrayList<Object[]>();
            while ( rs.next() ) {
              Object[] objRow = new Object[columnCount];
              for ( int i = 0; i < columnCount; i++ ) {
                objRow[i] = rs.getObject( i + 1 );
              }
              list.add( objRow );
            }
            Object[][] resultArr = new Object[list.size()][];
            list.toArray( resultArr );
            db.disconnect();
            return resultArr;
          }
        } catch ( Exception er ) {
          throw new RuntimeException( er.toString() );
        }
      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call fireToDB requires 2 arguments." );
    }
    return oRC;
  }

  public static Object dateDiff( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 3 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
          return undefinedValue;
        } else {
          java.util.Date dIn1 = (java.util.Date) ArgList[0];
          java.util.Date dIn2 = (java.util.Date) ArgList[1];
          String strType = ( (String) ArgList[2] ).toLowerCase();
          int iRC = 0;

          Calendar startDate = Calendar.getInstance();
          Calendar endDate = Calendar.getInstance();
          startDate.setTime( dIn1 );
          endDate.setTime( dIn2 );

          /*
           * Changed by: Ingo Klose, SHS VIVEON AG, Date: 27.04.2007
           *
           * Calculating time differences using getTimeInMillis() leads to false results when crossing Daylight
           * Savingstime borders. In order to get correct results the time zone offsets have to be added.
           *
           * Fix: 1. calculate correct milli seconds for start and end date 2. replace endDate.getTimeInMillis() with
           * endL and startDate.getTimeInMillis() with startL
           */
          long endL = endDate.getTimeInMillis() + endDate.getTimeZone().getOffset( endDate.getTimeInMillis() );
          long startL =
            startDate.getTimeInMillis() + startDate.getTimeZone().getOffset( startDate.getTimeInMillis() );

          if ( strType.equals( "y" ) ) {
            return new Double( endDate.get( Calendar.YEAR ) - startDate.get( Calendar.YEAR ) );
          } else if ( strType.equals( "m" ) ) {
            int iMonthsToAdd = ( endDate.get( Calendar.YEAR ) - startDate.get( Calendar.YEAR ) ) * 12;
            return new Double( ( endDate.get( Calendar.MONTH ) - startDate.get( Calendar.MONTH ) ) + iMonthsToAdd );
          } else if ( strType.equals( "d" ) ) {
            return new Double( ( ( endL - startL ) / 86400000 ) );
          } else if ( strType.equals( "wd" ) ) {
            int iOffset = -1;
            if ( endDate.before( startDate ) ) {
              iOffset = 1;
            }
            while ( ( iOffset == 1 && endL < startL ) || ( iOffset == -1 && endL > startL ) ) {
              int day = endDate.get( Calendar.DAY_OF_WEEK );
              if ( ( day != Calendar.SATURDAY ) && ( day != Calendar.SUNDAY ) ) {
                iRC++;
              }
              endDate.add( Calendar.DATE, iOffset );
              endL = endDate.getTimeInMillis() + endDate.getTimeZone().getOffset( endDate.getTimeInMillis() );
            }
            return new Double( iRC );
          } else if ( strType.equals( "w" ) ) {
            int iDays = (int) ( ( endL - startL ) / 86400000 );
            return new Double( iDays / 7 );
          } else if ( strType.equals( "ss" ) ) {
            return new Double( ( ( endL - startL ) / 1000 ) );
          } else if ( strType.equals( "mi" ) ) {
            return new Double( ( ( endL - startL ) / 60000 ) );
          } else if ( strType.equals( "hh" ) ) {
            return new Double( ( ( endL - startL ) / 3600000 ) );
          } else {
            return new Double( ( ( endL - startL ) / 86400000 ) );
          }
          /*
           * End Bugfix
           */
        }
      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call dateDiff requires 3 arguments." );
    }
  }

  public static Object getNextWorkingDay( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    // (Date dIn){
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }
        java.util.Date dIn = (java.util.Date) ArgList[0];
        Calendar startDate = Calendar.getInstance();
        startDate.setTime( dIn );
        startDate.add( Calendar.DATE, 1 );
        while ( startDate.get( Calendar.DAY_OF_WEEK ) == Calendar.SATURDAY
          || startDate.get( Calendar.DAY_OF_WEEK ) == Calendar.SUNDAY ) {
          startDate.add( Calendar.DATE, 1 );
        }
        return startDate.getTime();
      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call getNextWorkingDay requires 1 argument." );
    }
  }

  public static Object dateAdd( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 3 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
          return undefinedValue;
        }
        java.util.Date dIn = (java.util.Date) ArgList[0];
        String strType = ( (String) ArgList[1] ).toLowerCase();
        int iValue = (Integer) ArgList[2];
        Calendar cal = Calendar.getInstance();
        cal.setTime( dIn );
        if ( strType.equals( "y" ) ) {
          cal.add( Calendar.YEAR, iValue );
        } else if ( strType.equals( "m" ) ) {
          cal.add( Calendar.MONTH, iValue );
        } else if ( strType.equals( "d" ) ) {
          cal.add( Calendar.DATE, iValue );
        } else if ( strType.equals( "w" ) ) {
          cal.add( Calendar.WEEK_OF_YEAR, iValue );
        } else if ( strType.equals( "wd" ) ) {
          int iOffset = 0;
          while ( iOffset < iValue ) {
            int day = cal.get( Calendar.DAY_OF_WEEK );
            cal.add( Calendar.DATE, 1 );
            if ( ( day != Calendar.SATURDAY ) && ( day != Calendar.SUNDAY ) ) {
              iOffset++;
            }
          }
        } else if ( strType.equals( "hh" ) ) {
          cal.add( Calendar.HOUR, iValue );
        } else if ( strType.equals( "mi" ) ) {
          cal.add( Calendar.MINUTE, iValue );
        } else if ( strType.equals( "ss" ) ) {
          cal.add( Calendar.SECOND, iValue );
        }
        return cal.getTime();
      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call dateAdd requires 3 arguments." );
    }
  }

  public static String fillString( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return (String) undefinedValue;
        }
        String fillChar = (String) ArgList[0];
        int count = (Integer) ArgList[1];
        if ( fillChar.length() != 1 ) {
          throw new RuntimeException( "Please provide a valid Char to the fillString" );
        } else {
          char[] chars = new char[count];
          while ( count > 0 ) {
            chars[--count] = fillChar.charAt( 0 );
          }
          return new String( chars );
        }
      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call fillString requires 2 arguments." );
    }
  }

  public static Object isCodepage( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    boolean bRC = false;
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return undefinedValue;
        }
        String strValueToCheck = (String) ArgList[0];
        String strCodePage = (String) ArgList[1];
        byte[] bytearray = strValueToCheck.getBytes();
        CharsetDecoder d = Charset.forName( strCodePage ).newDecoder();
        CharBuffer r = d.decode( ByteBuffer.wrap( bytearray ) );
        r.toString();
        bRC = true;
      } catch ( Exception e ) {
        bRC = false;
      }
    } else {
      throw new RuntimeException( "The function call isCodepage requires 2 arguments." );
    }
    return Boolean.valueOf( bRC );
  }

  public static String ltrim( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) undefinedValue;
        }
        String strValueToTrim = (String) ArgList[0];
        return strValueToTrim.replaceAll( "^\\s+", "" );
      } else {
        throw new RuntimeException( "The function call ltrim requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "The function call ltrim is not valid : " + e.getMessage() );
    }
  }

  public static String rtrim( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) undefinedValue;
        }
        String strValueToTrim = (String) ArgList[0];
        return strValueToTrim.replaceAll( "\\s+$", "" );
      } else {
        throw new RuntimeException( "The function call rtrim requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "The function call rtrim is not valid : " + e.getMessage() );
    }
  }

  public static String lpad( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    // (String valueToPad, String filler, int size) {
    try {
      if ( ArgList.length == 3 ) {
        if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
          return (String) undefinedValue;
        }
        String valueToPad = (String) ArgList[0];
        String filler = (String) ArgList[1];
        int size = (Integer) ArgList[2];

        while ( valueToPad.length() < size ) {
          valueToPad = filler + valueToPad;
        }
        return valueToPad;
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "The function call lpad requires 3 arguments." );
    }
    return null;
  }

  public static String rpad( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 3 ) {
        if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
          return (String) undefinedValue;
        }
        String valueToPad = (String) ArgList[0];
        String filler = (String) ArgList[1];
        int size = (Integer) ArgList[2];

        while ( valueToPad.length() < size ) {
          valueToPad = valueToPad + filler;
        }
        return valueToPad;
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "The function call rpad requires 3 arguments." );
    }
    return null;
  }

  public static Object year( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }
        java.util.Date dArg1 = (java.util.Date) ArgList[0];
        Calendar cal = Calendar.getInstance();
        cal.setTime( dArg1 );
        return new Double( cal.get( Calendar.YEAR ) );
      } else {
        throw new RuntimeException( "The function call year requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static Object month( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }
        java.util.Date dArg1 = (java.util.Date) ArgList[0];
        Calendar cal = Calendar.getInstance();
        cal.setTime( dArg1 );
        return new Double( cal.get( Calendar.MONTH ) );
      } else {
        throw new RuntimeException( "The function call month requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }

  }

  public static Object quarter( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }
        java.util.Date dArg1 = (java.util.Date) ArgList[0];
        Calendar cal = Calendar.getInstance();
        cal.setTime( dArg1 );

        // Patch by Ingo Klose: calendar months start at 0 in java.
        int iMonth = cal.get( Calendar.MONTH );
        if ( iMonth <= 2 ) {
          return new Double( 1 );
        } else if ( iMonth <= 5 ) {
          return new Double( 2 );
        } else if ( iMonth <= 8 ) {
          return new Double( 3 );
        } else {
          return new Double( 4 );
        }
      } else {
        throw new RuntimeException( "The function call quarter requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static Object week( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }
        java.util.Date dArg1 = (java.util.Date) ArgList[0];
        Calendar cal = Calendar.getInstance();
        cal.setTime( dArg1 );
        return new Double( cal.get( Calendar.WEEK_OF_YEAR ) );
      } else {
        throw new RuntimeException( "The function call week requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static Object str2RegExp( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String[] strArr = null;
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return undefinedValue;
        }
        String strToMatch = (String) ArgList[0];
        Pattern p = Pattern.compile( (String) ArgList[1] );
        Matcher m = p.matcher( strToMatch );
        if ( m.matches() && m.groupCount() > 0 ) {
          strArr = new String[m.groupCount()];
          for ( int i = 1; i <= m.groupCount(); i++ ) {
            strArr[i - 1] = m.group( i );
          }
        }
      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call str2RegExp requires 2 arguments." );
    }
    return strArr;
  }

  public static void touch( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        File file = new File( (String) ArgList[0] );
        boolean success = file.createNewFile();
        if ( !success ) {
          file.setLastModified( System.currentTimeMillis() );
        }
      } else {
        throw new RuntimeException( "The function call touch requires 1 valid argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static Object fileExists( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        File file = new File( (String) ArgList[0] );
        return Boolean.valueOf( file.isFile() );
      } else {
        throw new RuntimeException( "The function call fileExists requires 1 valid argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static Object str2date( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    Object oRC = new Object();
    String sArg1 = "";
    String sArg2 = "";
    String sArg3 = "";
    String sArg4 = "";
    switch ( ArgList.length ) {
      case 0:
        throw new RuntimeException( "Please provide a valid string to the function call str2date." );
      case 1:
        try {
          if ( isNull( ArgList[0] ) ) {
            return null;
          } else if ( isUndefined( ArgList[0] ) ) {
            return undefinedValue;
          }
          sArg1 = (String) ArgList[0];
          Format dfFormatter = new SimpleDateFormat();
          oRC = dfFormatter.parseObject( sArg1 );
          // if(Double.isNaN(sArg1)) throw new RuntimeException("The first Argument must be a Number.");
          // DecimalFormat formatter = new DecimalFormat();
          // sRC= formatter.format(sArg1);
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not apply local format for " + sArg1 + " : " + e.getMessage() );
        }
        break;
      case 2:
        try {
          if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
            return undefinedValue;
          }
          sArg1 = (String) ArgList[0];
          sArg2 = (String) ArgList[1];
          Format dfFormatter = new SimpleDateFormat( sArg2 );
          oRC = dfFormatter.parseObject( sArg1 );
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not apply the given format "
            + sArg2 + " on the string for " + sArg1 + " : " + e.getMessage() );
        }
        break;
      case 3:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
            return undefinedValue;
          }
          sArg1 = (String) ArgList[0];
          Format dfFormatter;
          sArg2 = (String) ArgList[1];
          sArg3 = (String) ArgList[2];
          if ( sArg3.length() == 2 ) {
            Locale dfLocale = EnvUtil.createLocale( sArg3 );
            dfFormatter = new SimpleDateFormat( sArg2, dfLocale );
            oRC = dfFormatter.parseObject( sArg1 );
          } else {
            throw new RuntimeException( "Locale " + sArg3 + " is not 2 characters long." );
          }
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not apply the local format for locale "
            + sArg3 + " with the given format " + sArg2 + " on the string for " + sArg1 + " : " + e.getMessage() );
        }
        break;
      case 4:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2, 3 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2, 3 } ) ) {
            return undefinedValue;
          }
          sArg1 = (String) ArgList[0];
          DateFormat dfFormatter;
          sArg2 = (String) ArgList[1];
          sArg3 = (String) ArgList[2];
          sArg4 = (String) ArgList[3];

          // If the timezone is not recognized, java will automatically
          // take GMT.
          TimeZone tz = TimeZone.getTimeZone( sArg4 );

          if ( sArg3.length() == 2 ) {
            Locale dfLocale = EnvUtil.createLocale( sArg3 );
            dfFormatter = new SimpleDateFormat( sArg2, dfLocale );
            dfFormatter.setTimeZone( tz );
            oRC = dfFormatter.parseObject( sArg1 );
          } else {
            throw new RuntimeException( "Locale " + sArg3 + " is not 2 characters long." );
          }
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not apply the local format for locale "
            + sArg3 + " with the given format " + sArg2 + " on the string for " + sArg1 + " : " + e.getMessage() );
        }
        break;
      default:
        throw new RuntimeException( "The function call str2date requires 1, 2, 3, or 4 arguments." );
    }
    return oRC;
  }

  public static Object date2str( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    Object oRC = new Object();
    switch ( ArgList.length ) {
      case 0:
        throw new RuntimeException( "Please provide a valid date to the function call date2str." );
      case 1:
        try {
          if ( isNull( ArgList ) ) {
            return null;
          } else if ( isUndefined( ArgList ) ) {
            return undefinedValue;
          }
          java.util.Date dArg1 = (java.util.Date) ArgList[0];
          if ( dArg1.equals( null ) ) {
            return null;
          }
          Format dfFormatter = new SimpleDateFormat();
          oRC = dfFormatter.format( dArg1 );
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not convert to local format." );
        }
        break;
      case 2:
        try {
          if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
            return undefinedValue;
          }
          java.util.Date dArg1 = (java.util.Date) ArgList[0];
          String sArg2 = (String) ArgList[1];
          Format dfFormatter = new SimpleDateFormat( sArg2 );
          oRC = dfFormatter.format( dArg1 );
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not convert to the given format." );
        }
        break;
      case 3:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
            return undefinedValue;
          }
          java.util.Date dArg1 = (java.util.Date) ArgList[0];
          DateFormat dfFormatter;
          String sArg2 = (String) ArgList[1];
          String sArg3 = (String) ArgList[2];
          if ( sArg3.length() == 2 ) {
            Locale dfLocale = EnvUtil.createLocale( sArg3.toLowerCase() );
            dfFormatter = new SimpleDateFormat( sArg2, dfLocale );
            oRC = dfFormatter.format( dArg1 );
          } else {
            throw new RuntimeException( "Locale is not 2 characters long." );
          }
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not convert to the given local format." );
        }
        break;
      case 4:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2, 3 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2, 3 } ) ) {
            return undefinedValue;
          }
          java.util.Date dArg1 = (java.util.Date) ArgList[0];
          DateFormat dfFormatter;
          String sArg2 = (String) ArgList[1];
          String sArg3 = (String) ArgList[2];
          String sArg4 = (String) ArgList[3];

          // If the timezone is not recognized, java will automatically
          // take GMT.
          TimeZone tz = TimeZone.getTimeZone( sArg4 );

          if ( sArg3.length() == 2 ) {
            Locale dfLocale = EnvUtil.createLocale( sArg3.toLowerCase() );
            dfFormatter = new SimpleDateFormat( sArg2, dfLocale );
            dfFormatter.setTimeZone( tz );
            oRC = dfFormatter.format( dArg1 );
          } else {
            throw new RuntimeException( "Locale is not 2 characters long." );
          }
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not convert to the given local format." );
        }
        break;
      default:
        throw new RuntimeException( "The function call date2str requires 1, 2, 3, or 4 arguments." );
    }
    return oRC;
  }

  public static Object isRegExp( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    if ( ArgList.length >= 2 ) {
      if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
        return null;
      } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
        return undefinedValue;
      }
      String strToMatch = (String) ArgList[0];
      for ( int i = 1; i < ArgList.length; i++ ) {
        Pattern p = Pattern.compile( (String) ArgList[i] );
        Matcher m = p.matcher( strToMatch );
        if ( m.matches() ) {
          return new Double( i );
        }
      }
    }
    return new Double( -1 );
  }

  public static void sendMail( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    boolean debug = false;

    // Arguments:
    // String smtp, String from, String recipients[ ], String subject, String message
    if ( ArgList.length == 5 ) {

      try {
        // Set the host smtp address
        Properties props = new Properties();
        props.put( "mail.smtp.host", ArgList[0] );

        // create some properties and get the default Session
        Session session = Session.getDefaultInstance( props, null );
        session.setDebug( debug );

        // create a message
        Message msg = new MimeMessage( session );

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress( (String) ArgList[1] );
        msg.setFrom( addressFrom );

        // Get Recipients
        String[] strArrRecipients = ( (String) ArgList[2] ).split( "," );

        InternetAddress[] addressTo = new InternetAddress[strArrRecipients.length];
        for ( int i = 0; i < strArrRecipients.length; i++ ) {
          addressTo[i] = new InternetAddress( strArrRecipients[i] );
        }
        msg.setRecipients( Message.RecipientType.TO, addressTo );

        // Optional : You can also set your custom headers in the Email if you Want
        msg.addHeader( "MyHeaderName", "myHeaderValue" );

        // Setting the Subject and Content Type
        msg.setSubject( (String) ArgList[3] );
        msg.setContent( ArgList[4], "text/plain" );
        Transport.send( msg );
      } catch ( Exception e ) {
        throw new RuntimeException( "sendMail: " + e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call sendMail requires 5 arguments." );
    }
  }

  public static String upper( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) undefinedValue;
        }
        sRC = (String) ArgList[0];
        sRC = sRC.toUpperCase();
      } catch ( Exception e ) {
        throw new RuntimeException( "The function call upper is not valid : " + e.getMessage() );
      }
    } else {
      throw new RuntimeException( "The function call upper requires 1 argument." );
    }
    return sRC;
  }

  public static String lower( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) undefinedValue;
        }
        sRC = (String) ArgList[0];
        sRC = sRC.toLowerCase();
      } catch ( Exception e ) {
        throw new RuntimeException( "The function call lower is not valid : " + e.getMessage() );
      }
    } else {
      throw new RuntimeException( "The function call lower requires 1 argument." );
    }
    return sRC;
  }

  // Converts the given Numeric to a JScript String
  public static String num2str( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sRC = "";
    switch ( ArgList.length ) {
      case 0:
        throw new RuntimeException( "The function call num2str requires at least 1 argument." );
      case 1:
        try {
          if ( isNull( ArgList[0] ) ) {
            return null;
          } else if ( isUndefined( ArgList[0] ) ) {
            return (String) undefinedValue;
          }
          double sArg1 = (Double) ArgList[0];
          if ( Double.isNaN( sArg1 ) ) {
            throw new RuntimeException( "The first Argument must be a Number." );
          }
          DecimalFormat formatter = new DecimalFormat();
          sRC = formatter.format( sArg1 );
        } catch ( IllegalArgumentException e ) {
          throw new RuntimeException( "Could not apply the given format on the number : " + e.getMessage() );
        }
        break;
      case 2:
        try {
          if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
            return (String) undefinedValue;
          }
          double sArg1 = (Double) ArgList[0];
          if ( Double.isNaN( sArg1 ) ) {
            throw new RuntimeException( "The first Argument must be a Number." );
          }
          String sArg2 = (String) ArgList[1];
          DecimalFormat formatter = new DecimalFormat( sArg2 );
          sRC = formatter.format( sArg1 );
        } catch ( IllegalArgumentException e ) {
          throw new RuntimeException( "Could not apply the given format on the number : " + e.getMessage() );
        }
        break;
      case 3:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
            return (String) undefinedValue;
          }
          double sArg1 = (Double) ArgList[0];
          if ( Double.isNaN( sArg1 ) ) {
            throw new RuntimeException( "The first Argument must be a Number." );
          }
          String sArg2 = (String) ArgList[1];
          String sArg3 = (String) ArgList[2];
          if ( sArg3.length() == 2 ) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols( EnvUtil.createLocale( sArg3.toLowerCase() ) );
            DecimalFormat formatter = new DecimalFormat( sArg2, dfs );
            sRC = formatter.format( sArg1 );
          }
        } catch ( Exception e ) {
          throw new RuntimeException( e.toString() );
        }
        break;
      default:
        throw new RuntimeException( "The function call num2str requires 1, 2, or 3 arguments." );
    }

    return sRC;
  }

  // Converts the given String to a JScript Numeric
  public static Object str2num( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    double dRC = 0.00;
    switch ( ArgList.length ) {
      case 0:
        throw new RuntimeException( "The function call str2num requires at least 1 argument." );
      case 1:
        try {
          if ( isNull( ArgList[0] ) ) {
            return new Double( Double.NaN );
          } else if ( isUndefined( ArgList[0] ) ) {
            return undefinedValue;
          }
          if ( ArgList[0].equals( null ) ) {
            return null;
          }
          String sArg1 = (String) ArgList[0];
          DecimalFormat formatter = new DecimalFormat();
          dRC = ( formatter.parse( Const.ltrim( sArg1 ) ) ).doubleValue();
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not convert the given String : " + e.getMessage() );
        }
        break;
      case 2:
        try {
          if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
            return new Double( Double.NaN );
          } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
            return undefinedValue;
          }
          String sArg1 = (String) ArgList[0];
          String sArg2 = (String) ArgList[1];
          if ( sArg1.equals( "null" ) || sArg2.equals( "null" ) ) {
            return null;
          }
          DecimalFormat formatter = new DecimalFormat( sArg2 );
          dRC = ( formatter.parse( sArg1 ) ).doubleValue();
          return new Double( dRC );
        } catch ( Exception e ) {
          throw new RuntimeException( "Could not convert the String with the given format :" + e.getMessage() );
        }
        // break;
      case 3:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
            return new Double( Double.NaN );
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
            return undefinedValue;
          }
          String sArg1 = (String) ArgList[0];
          String sArg2 = (String) ArgList[1];
          String sArg3 = (String) ArgList[2];
          if ( sArg3.length() == 2 ) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols( EnvUtil.createLocale( sArg3.toLowerCase() ) );
            DecimalFormat formatter = new DecimalFormat( sArg2, dfs );
            dRC = ( formatter.parse( sArg1 ) ).doubleValue();
            return new Double( dRC );
          }
        } catch ( Exception e ) {
          throw new RuntimeException( e.getMessage() );
        }
        break;
      default:
        throw new RuntimeException( "The function call str2num requires 1, 2, or 3 arguments." );
    }
    return new Double( dRC );
  }

  public static Object isNum( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }
        double sArg1 = (Double) ArgList[0];
        if ( Double.isNaN( sArg1 ) ) {
          return Boolean.FALSE;
        } else {
          return Boolean.TRUE;
        }
      } catch ( Exception e ) {
        return Boolean.FALSE;
      }
    } else {
      throw new RuntimeException( "The function call isNum requires 1 argument." );
    }
  }

  public static Object isDate( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }
        /* java.util.Date d = (java.util.Date) */
        return Boolean.TRUE;
      } catch ( Exception e ) {
        return Boolean.FALSE;
      }
    } else {
      throw new RuntimeException( "The function call isDate requires 1 argument." );
    }
  }

  public static Object decode( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length >= 2 ) {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return undefinedValue;
        }
        Object objToCompare = ArgList[0];
        for ( int i = 1; i < ArgList.length - 1; i = i + 2 ) {
          if ( ArgList[i].equals( objToCompare ) ) {
            return ArgList[i + 1];
          }
        }
        if ( ArgList.length % 2 == 0 ) {
          return ArgList[ArgList.length - 1];
        } else {
          return objToCompare;
        }
      } else {
        throw new RuntimeException( "The function call decode requires more than 1 argument." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "The function call decode is not valid : " + e.getMessage() );
    }
  }

  public static String replace( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length >= 2 && ( ArgList.length - 1 ) % 2 == 0 ) {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return (String) undefinedValue;
        }
        String objForReplace = (String) ArgList[0];
        for ( int i = 1; i < ArgList.length - 1; i = i + 2 ) {
          objForReplace = objForReplace.replaceAll( (String) ArgList[i], (String) ArgList[i + 1] );
        }
        return objForReplace;
      } else {
        throw new RuntimeException( "The function call replace is not valid (wrong number of arguments)" );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "Function call replace is not valid : " + e.getMessage() );
    }
  }

  // Implementation of the JS AlertBox
  public static String Alert( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    SpoonInterface spoon = SpoonFactory.getInstance();
    if ( ArgList.length == 1 && spoon != null ) {
      String strMessage = (String) ArgList[0];
      spoon.messageBox( strMessage, "Alert", false, Const.INFO );
    }

    return "";
  }

  // Setting EnvironmentVar
  public static void setEnvironmentVar( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sArg1 = "";
    String sArg2 = "";
    if ( ArgList.length == 2 ) {
      try {
        sArg1 = (String) ArgList[0];
        sArg2 = (String) ArgList[1];
        System.setProperty( sArg1, sArg2 );
      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call setEnvironmentVar requires 2 arguments." );
    }
  }

  // Returning EnvironmentVar
  public static String getEnvironmentVar( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 1 ) {
      try {
        String sArg1 = (String) ArgList[0];
        sRC = System.getProperty( sArg1, "" );
      } catch ( Exception e ) {
        sRC = "";
      }
    } else {
      throw new RuntimeException( "The function call getEnvironmentVar requires 1 argument." );
    }
    return sRC;
  }

  public static String trim( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) undefinedValue;
        }
        sRC = (String) ArgList[0];
        sRC = Const.trim( sRC );
      } catch ( Exception e ) {
        throw new RuntimeException( "The function call trim is not valid : " + e.getMessage() );
      }
    } else {
      throw new RuntimeException( "The function call trim requires 1 argument." );
    }
    return sRC;
  }

  public static String substr( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sRC = "";

    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) undefinedValue;
        }
        sRC = (String) ArgList[0];
        int from = (int) Math.round( (Double) ArgList[1] );
        sRC = sRC.substring( from );
      } catch ( Exception e ) {
        throw new RuntimeException( "The function call substr is not valid : " + e.getMessage() );
      }
    } else if ( ArgList.length == 3 ) {
      try {
        int to;
        int strLen;

        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) undefinedValue;
        }
        sRC = (String) ArgList[0];
        int from = (int) Math.round( (Double) ArgList[1] );
        int len = (int) Math.round( (Double) ArgList[2] );

        if ( from < 0 ) {
          throw new RuntimeException( "start smaller than 0" );
        }
        if ( len < 0 ) {
          len = 0; // Make it compatible with Javascript substr
        }

        to = from + len;
        strLen = sRC.length();
        if ( to > strLen ) {
          to = strLen;
        }
        sRC = sRC.substring( from, to );
      } catch ( Exception e ) {
        throw new RuntimeException( "The function call substr is not valid : " + e.getMessage() );
      }
    } else {
      throw new RuntimeException( "The function call substr requires 2 or 3 arguments." );
    }
    return sRC;
  }

  // Resolve an IP address
  public static String resolveIP( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 2 ) {
      try {
        InetAddress addr = InetAddress.getByName( (String) ArgList[0] );
        if ( ( (String) ArgList[1] ).equals( "IP" ) ) {
          sRC = addr.getHostName();
        } else {
          sRC = addr.getHostAddress();
        }
        if ( sRC.equals( ArgList[0] ) ) {
          sRC = "-";
        }
      } catch ( Exception e ) {
        sRC = "-";
      }
    } else {
      throw new RuntimeException( "The function call resolveIP requires 2 arguments." );
    }

    return sRC;
  }

  // Loading additional JS Files inside the JavaScriptCode
  public static void LoadScriptFile( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    for ( int i = 0; i < ArgList.length; i++ ) { // don't worry about "undefined" arguments
      checkAndLoadJSFile( actualContext, actualObject, (String) ArgList[i] );
    }
  }

  // Adding the ScriptsItemTab to the actual running Context
  public static void LoadScriptFromTab( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      for ( int i = 0; i < ArgList.length; i++ ) { // don't worry about "undefined" arguments
        String strToLoad = (String) ArgList[i];
        String strScript = actualObject.get( strToLoad ).toString();
        actualContext.eval( strScript, actualObject );
      }
    } catch ( Exception e ) {
      // System.out.println(e.toString());
    }
  }

  // Print
  public static void print( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    for ( int i = 0; i < ArgList.length; i++ ) { // don't worry about "undefined" arguments
      java.lang.System.out.print( (String) ArgList[i] );
    }
  }

  // Prints Line to the actual System.out
  public static void println( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    print( actualContext, actualObject, ArgList, FunctionContext );
    java.lang.System.out.println();
  }

  // Returns the actual ClassName
  public String getClassName() {
    return "SciptValuesAddedFunctions";
  }

  // Evaluates the given ScriptFile
  private static void checkAndLoadJSFile( ScriptEngine actualContext, Bindings eval_scope, String fileName ) {
    Reader inStream = null;
    try {
      inStream = new InputStreamReader( KettleVFS.getInputStream( fileName ) );
      actualContext.eval( inStream, eval_scope );
    } catch ( KettleFileException Signal ) {
      /*
       * //TODO AKRETION: see if we can find better catches compatibles with JSR223 catch (FileNotFoundException Signal)
       * { new RuntimeException("Unable to open file \"" + fileName + "\" (reason: \"" + Signal.getMessage() + "\")"); }
       * catch (WrappedException Signal) { new RuntimeException("WrappedException while evaluating file \"" + fileName +
       * "\" (reason: \"" + Signal.getMessage() + "\")"); } catch (EvaluatorException Signal) { new
       * RuntimeException("EvaluatorException while evaluating file \"" + fileName + "\" (reason: \"" +
       * Signal.getMessage() + "\")"); } catch (JavaScriptException Signal) { new
       * RuntimeException("JavaScriptException while evaluating file \"" + fileName + "\" (reason: \"" +
       * Signal.getMessage() + "\")"); } catch (IOException Signal) { new RuntimeException("Error while reading file \""
       * + fileName + "\" (reason: \"" + Signal.getMessage() + "\")" ); }
       */
      new RuntimeException( "Error while reading file \""
        + fileName + "\" (reason: \"" + Signal.getMessage() + "\")" );
    } catch ( ScriptException Signal ) {
      new RuntimeException( "Error while reading file \""
        + fileName + "\" (reason: \"" + Signal.getMessage() + "\")" );
    } finally {
      try {
        if ( inStream != null ) {
          inStream.close();
        }
      } catch ( Exception Signal ) {
        // Ignore
      }
    }
  }

  // Setting Variable
  public static void setVariable( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sArg1 = "";
    String sArg2 = "";
    String sArg3 = "";
    if ( ArgList.length == 3 ) {
      try {
        Object scmo = actualObject.get( "_step_" );
        Object scmO = scmo;
        if ( scmO instanceof ScriptInterface ) {
          ScriptInterface scm = (ScriptInterface) scmO;

          sArg1 = (String) ArgList[0];
          sArg2 = (String) ArgList[1];
          sArg3 = (String) ArgList[2];

          if ( "s".equals( sArg3 ) ) {
            // System wide properties
            System.setProperty( sArg1, sArg2 );

            // Set also all the way to the root as else we will take
            // stale values
            scm.setVariable( sArg1, sArg2 );

            VariableSpace parentSpace = scm.getParentVariableSpace();
            while ( parentSpace != null ) {
              parentSpace.setVariable( sArg1, sArg2 );
              parentSpace = parentSpace.getParentVariableSpace();
            }
          } else if ( "r".equals( sArg3 ) ) {
            // Upto the root... this should be the default.
            scm.setVariable( sArg1, sArg2 );

            VariableSpace parentSpace = scm.getParentVariableSpace();
            while ( parentSpace != null ) {
              parentSpace.setVariable( sArg1, sArg2 );
              parentSpace = parentSpace.getParentVariableSpace();
            }
          } else if ( "p".equals( sArg3 ) ) {
            // Upto the parent
            scm.setVariable( sArg1, sArg2 );

            VariableSpace parentSpace = scm.getParentVariableSpace();
            if ( parentSpace != null ) {
              parentSpace.setVariable( sArg1, sArg2 );
            }
          } else if ( "g".equals( sArg3 ) ) {
            // Upto the grand parent
            scm.setVariable( sArg1, sArg2 );

            VariableSpace parentSpace = scm.getParentVariableSpace();
            if ( parentSpace != null ) {
              parentSpace.setVariable( sArg1, sArg2 );
              VariableSpace grandParentSpace = parentSpace.getParentVariableSpace();
              if ( grandParentSpace != null ) {
                grandParentSpace.setVariable( sArg1, sArg2 );
              }
            }
          } else {
            throw new RuntimeException(
              "The argument type of function call setVariable should either be \"s\", \"r\", \"p\", or \"g\"." );
          }
        }

        // Else: Ignore for now... if we're executing via the Test Button

      } catch ( Exception e ) {
        throw new RuntimeException( e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call setVariable requires 3 arguments." );
    }
  }

  // Returning EnvironmentVar
  public static String getVariable( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    String sRC = "";
    String sArg1 = "";
    String sArg2 = "";
    if ( ArgList.length == 2 ) {
      try {
        Object scmo = actualObject.get( "_step_" );
        Object scmO = scmo;

        if ( scmO instanceof Script ) {
          Script scm = (Script) scmO;

          sArg1 = (String) ArgList[0];
          sArg2 = (String) ArgList[1];
          return scm.getVariable( sArg1, sArg2 );
        } else {
          // running via the Test button in a dialog
          sArg2 = (String) ArgList[1];
          return sArg2;
        }
      } catch ( Exception e ) {
        sRC = "";
      }
    } else {
      throw new RuntimeException( "The function call getVariable requires 2 arguments." );
    }
    return sRC;
  }

  // Return the output row metadata
  public static RowMetaInterface getOutputRowMeta( ScriptEngine actualContext, Bindings actualObject,
    Object[] ArgList, Object FunctionContext ) {
    if ( ArgList.length == 0 ) {
      try {
        Object scmO = actualObject.get( "_step_" );
        try {
          Script scm = (Script) scmO;
          return scm.getOutputRowMeta();
        } catch ( Exception e ) {
          ScriptDummy scm = (ScriptDummy) scmO;
          return scm.getOutputRowMeta();
        }
      } catch ( Exception e ) {
        throw new RuntimeException( "Unable to get the output row metadata because of an error: "
          + Const.CR + e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call getOutputRowMeta doesn't require arguments." );
    }
  }

  // Return the input row metadata
  public static RowMetaInterface getInputRowMeta( ScriptEngine actualContext, Bindings actualObject,
    Object[] ArgList, Object FunctionContext ) {
    if ( ArgList.length == 0 ) {
      try {
        Object scmO = actualObject.get( "_step_" );
        try {
          Script scm = (Script) scmO;
          return scm.getInputRowMeta();
        } catch ( Exception e ) {
          ScriptDummy scm = (ScriptDummy) scmO;
          return scm.getInputRowMeta();
        }
      } catch ( Exception e ) {
        throw new RuntimeException( "Unable to get the input row metadata because of an error: "
          + Const.CR + e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call getInputRowMeta doesn't require arguments." );
    }
  }

  // Return the input row metadata
  public static Object[] createRowCopy( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        int newSize = (int) Math.round( (Double) ArgList[0] );

        Object scmO = actualObject.get( "row" );
        Object[] row = (Object[]) scmO; // TODO AKRETION ensure

        return RowDataUtil.createResizedCopy( row, newSize );
      } catch ( Exception e ) {
        throw new RuntimeException( "Unable to create a row copy: " + Const.CR + e.toString() );
      }
    } else {
      throw new RuntimeException(
        "The function call createRowCopy requires a single arguments : the new size of the row" );
    }
  }

  // put a row out to the next steps...
  //
  public static void putRow( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        Object[] newRow = (Object[]) ArgList[0];

        Object scmO = actualObject.get( "_step_" );
        try {
          Script step = (Script) scmO;
          step.putRow( step.getOutputRowMeta(), newRow );
        } catch ( Exception e ) {
          ScriptDummy step = (ScriptDummy) scmO;
          step.putRow( step.getOutputRowMeta(), newRow );
        }

      } catch ( Exception e ) {
        throw new RuntimeException( "Unable to pass the new row to the next step(s) because of an error: "
          + Const.CR + e.toString() );
      }
    } else {
      throw new RuntimeException( "The function call putRow requires 1 argument : the output row data (Object[])" );
    }
  }

  public static void deleteFile( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        // Object act = actualObject.get("_step_", actualObject);
        // ScriptValuesMod act = (ScriptValuesMod)Context.toType(scm_delete, ScriptValuesMod.class);

        FileObject fileObject = null;

        try {
          fileObject = KettleVFS.getFileObject( (String) ArgList[0] );
          if ( fileObject.exists() ) {
            if ( fileObject.getType() == FileType.FILE ) {
              if ( !fileObject.delete() ) {
                new RuntimeException( "We can not delete file [" + (String) ArgList[0] + "]!" );
              }
            }

          } else {
            new RuntimeException( "file [" + (String) ArgList[0] + "] can not be found!" );
          }
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call deleteFile is not valid." );
        } finally {
          if ( fileObject != null ) {
            try {
              fileObject.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call deleteFile is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static void createFolder( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        FileObject fileObject = null;

        try {
          fileObject = KettleVFS.getFileObject( (String) ArgList[0] );
          if ( !fileObject.exists() ) {
            fileObject.createFolder();
          } else {
            new RuntimeException( "folder [" + (String) ArgList[0] + "] already exist!" );
          }
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call createFolder is not valid." );
        } finally {
          if ( fileObject != null ) {
            try {
              fileObject.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call createFolder is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static void copyFile( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    try {
      if ( ArgList.length == 3
        && !isNull( ArgList[0] ) && !isNull( ArgList[1] ) && !isUndefined( ArgList[0] )
        && !isUndefined( ArgList[1] ) ) {
        FileObject fileSource = null, fileDestination = null;

        try {
          // Source file to copy
          fileSource = KettleVFS.getFileObject( (String) ArgList[0] );
          // Destination filename
          fileDestination = KettleVFS.getFileObject( (String) ArgList[1] );
          if ( fileSource.exists() ) {
            // Source file exists...
            if ( fileSource.getType() == FileType.FILE ) {
              // Great..source is a file ...
              boolean overwrite = false;
              if ( !ArgList[1].equals( null ) ) {
                overwrite = (Boolean) ArgList[2];
              }
              boolean destinationExists = fileDestination.exists();
              // Let's copy the file...
              if ( ( destinationExists && overwrite ) || !destinationExists ) {
                FileUtil.copyContent( fileSource, fileDestination );
              }

            }
          } else {
            new RuntimeException( "file to copy [" + (String) ArgList[0] + "] can not be found!" );
          }
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call copyFile throw an error : " + e.toString() );
        } finally {
          if ( fileSource != null ) {
            try {
              fileSource.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
          if ( fileDestination != null ) {
            try {
              fileDestination.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call copyFileis not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static double getFileSize( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return 0;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( (String) ArgList[0] );
          long filesize = 0;
          if ( file.exists() ) {
            if ( file.getType().equals( FileType.FILE ) ) {
              filesize = file.getContent().getSize();
            } else {
              new RuntimeException( "[" + (String) ArgList[0] + "] is not a file!" );
            }
          } else {
            new RuntimeException( "file [" + (String) ArgList[0] + "] can not be found!" );
          }
          return filesize;
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call getFileSize throw an error : " + e.toString() );
        } finally {
          if ( file != null ) {
            try {
              file.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call getFileSize is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static boolean isFile( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return false;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( (String) ArgList[0] );
          boolean isafile = false;
          if ( file.exists() ) {
            if ( file.getType().equals( FileType.FILE ) ) {
              isafile = true;
            } else {
              new RuntimeException( "[" + (String) ArgList[0] + "] is not a file!" );
            }
          } else {
            new RuntimeException( "file [" + (String) ArgList[0] + "] can not be found!" );
          }
          return isafile;
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call is File throw an error : " + e.toString() );
        } finally {
          if ( file != null ) {
            try {
              file.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call isFile is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static boolean isFolder( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return false;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( (String) ArgList[0] );
          boolean isafolder = false;
          if ( file.exists() ) {
            if ( file.getType().equals( FileType.FOLDER ) ) {
              isafolder = true;
            } else {
              new RuntimeException( "[" + (String) ArgList[0] + "] is not a folder!" );
            }
          } else {
            new RuntimeException( "folder [" + (String) ArgList[0] + "] can not be found!" );
          }
          return isafolder;
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call isFolder throw an error : " + e.toString() );
        } finally {
          if ( file != null ) {
            try {
              file.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call isFolder is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static String getShortFilename( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( (String) ArgList[0] );
          String Filename = null;
          if ( file.exists() ) {
            Filename = file.getName().getBaseName().toString();

          } else {
            new RuntimeException( "file [" + (String) ArgList[0] + "] can not be found!" );
          }

          return Filename;
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call getShortFilename throw an error : " + e.toString() );
        } finally {
          if ( file != null ) {
            try {
              file.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call getShortFilename is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static String getFileExtension( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( (String) ArgList[0] );
          String Extension = null;
          if ( file.exists() ) {
            Extension = file.getName().getExtension().toString();

          } else {
            new RuntimeException( "file [" + (String) ArgList[0] + "] can not be found!" );
          }

          return Extension;
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call getFileExtension throw an error : " + e.toString() );
        } finally {
          if ( file != null ) {
            try {
              file.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call getFileExtension is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static String getParentFoldername( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( (String) ArgList[0] );
          String foldername = null;
          if ( file.exists() ) {
            foldername = KettleVFS.getFilename( file.getParent() );

          } else {
            new RuntimeException( "file [" + (String) ArgList[0] + "] can not be found!" );
          }

          return foldername;
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call getParentFoldername throw an error : " + e.toString() );
        } finally {
          if ( file != null ) {
            try {
              file.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call getParentFoldername is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static String getLastModifiedTime( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      if ( ArgList.length == 2 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( (String) ArgList[0] );
          String dateformat = (String) ArgList[1];
          if ( isNull( dateformat ) ) {
            dateformat = "yyyy-MM-dd";
          }
          String lastmodifiedtime = null;
          if ( file.exists() ) {
            java.util.Date lastmodifiedtimedate = new java.util.Date( file.getContent().getLastModifiedTime() );
            java.text.DateFormat dateFormat = new SimpleDateFormat( dateformat );
            lastmodifiedtime = dateFormat.format( lastmodifiedtimedate );

          } else {
            new RuntimeException( "file [" + (String) ArgList[0] + "] can not be found!" );
          }

          return lastmodifiedtime;
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call getLastModifiedTime throw an error : " + e.toString() );
        } finally {
          if ( file != null ) {
            try {
              file.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call getLastModifiedTime is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static Object trunc( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      // 1 argument: normal truncation of numbers
      //
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }

        // This is the truncation of a number...
        //
        Double dArg1 = (Double) ArgList[0];
        return Double.valueOf( Math.floor( dArg1 ) );

      } else {
        throw new RuntimeException( "The function call trunc requires 1 argument, a number." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  @SuppressWarnings( "fallthrough" )
  public static Object truncDate( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {
    try {
      // 2 arguments: truncation of dates to a certain precision
      //
      if ( ArgList.length == 2 ) {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return undefinedValue;
        }

        // This is the truncation of a date...
        // The second argument specifies the level: ms, s, min, hour, day, month, year
        //
        java.util.Date dArg1 = (java.util.Date) ArgList[0];
        Calendar cal = Calendar.getInstance();
        cal.setTime( dArg1 );

        Integer level = (Integer) ArgList[1];

        switch ( level.intValue() ) {
        // MONTHS
          case 5:
            cal.set( Calendar.MONTH, 1 );
            // DAYS
          case 4:
            cal.set( Calendar.DAY_OF_MONTH, 1 );
            // HOURS
          case 3:
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            // MINUTES
          case 2:
            cal.set( Calendar.MINUTE, 0 );
            // SECONDS
          case 1:
            cal.set( Calendar.SECOND, 0 );
            // MILI-SECONDS
          case 0:
            cal.set( Calendar.MILLISECOND, 0 );
            break;
          default:
            throw new RuntimeException( "Argument of TRUNC of date has to be between 0 and 5" );
        }

        return cal.getTime();
      } else {
        throw new RuntimeException( "The function call truncDate requires 2 arguments: a date and a level (int)" );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

  public static void moveFile( ScriptEngine actualContext, Bindings actualObject, Object[] ArgList,
    Object FunctionContext ) {

    try {
      if ( ArgList.length == 3
        && !isNull( ArgList[0] ) && !isNull( ArgList[1] ) && !isUndefined( ArgList[0] )
        && !isUndefined( ArgList[1] ) ) {
        FileObject fileSource = null, fileDestination = null;

        try {
          // Source file to move
          fileSource = KettleVFS.getFileObject( (String) ArgList[0] );
          // Destination filename
          fileDestination = KettleVFS.getFileObject( (String) ArgList[1] );
          if ( fileSource.exists() ) {
            // Source file exists...
            if ( fileSource.getType() == FileType.FILE ) {
              // Great..source is a file ...
              boolean overwrite = false;
              if ( !ArgList[1].equals( null ) ) {
                overwrite = (Boolean) ArgList[2];
              }
              boolean destinationExists = fileDestination.exists();
              // Let's move the file...
              if ( ( destinationExists && overwrite ) || !destinationExists ) {
                fileSource.moveTo( fileDestination );
              }

            }
          } else {
            new RuntimeException( "file to move [" + (String) ArgList[0] + "] can not be found!" );
          }
        } catch ( IOException e ) {
          throw new RuntimeException( "The function call moveFile throw an error : " + e.toString() );
        } finally {
          if ( fileSource != null ) {
            try {
              fileSource.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
          if ( fileDestination != null ) {
            try {
              fileDestination.close();
            } catch ( Exception e ) {
              // Ignore errors
            }
          }
        }

      } else {
        throw new RuntimeException( "The function call copyFile is not valid." );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString() );
    }
  }

}
