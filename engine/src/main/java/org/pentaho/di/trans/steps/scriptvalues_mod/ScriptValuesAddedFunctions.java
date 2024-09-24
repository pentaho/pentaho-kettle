// CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.scriptvalues_mod;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.loadfileinput.LoadFileInput;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptValuesAddedFunctions extends ScriptableObject {

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
    "moveFile", "execProcess", "isEmpty", "isMailValid", "escapeXml", "removeDigits", "initCap",
    "protectXMLCDATA", "unEscapeXml", "escapeSQL", "escapeHtml", "unEscapeHtml", "loadFileContent",
    "getOcuranceString", "removeCRLF" };


  enum VariableScope {
    SYSTEM,
    ROOT,
    PARENT,
    GRAND_PARENT
  }


  // This is only used for reading, so no concurrency problems.
  // todo: move in the real variables of the step.
  // private static VariableSpace variables = Variables.getADefaultVariableSpace();

  // Functions to Add
  // date2num, num2date,
  // fisc_date, isNull
  //

  public static String getDigitsOnly( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.getDigitsOnly( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call getDigitsOnly requires 1 argument." );

    }
  }

  public static boolean LuhnCheck( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    boolean returnCode = false;

    if ( ArgList.length == 1 ) {
      if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
        try {
          int sum = 0;
          int digit = 0;
          int addend = 0;
          boolean timesTwo = false;
          String argstring = Context.toString( ArgList[0] );

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
      throw Context.reportRuntimeError( "The function call LuhnCheck requires 1 argument." );

    }
    return returnCode;
  }

  public static int indexOf( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    int returnIndex = -1;

    if ( ArgList.length == 2 || ArgList.length == 3 ) {
      if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
        String string = Context.toString( ArgList[0] );
        String subString = Context.toString( ArgList[1] );

        int fromIndex = 0;
        if ( ArgList.length == 3 ) {
          fromIndex = (int) Math.round( Context.toNumber( ArgList[2] ) );
        }
        returnIndex = string.indexOf( subString, fromIndex );
      }
    } else {
      throw Context.reportRuntimeError( "The function call indexOf requires 2 or 3 arguments" );
    }
    return returnIndex;
  }

  public static Object getTransformationName( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      Object objTranName = Context.toString( actualObject.get( "_TransformationName_", actualObject ) );
      return objTranName;
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static void appendToFile( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
      try {
        FileOutputStream file = new FileOutputStream( Context.toString( ArgList[0] ), true );
        DataOutputStream out = new DataOutputStream( file );
        out.writeBytes( Context.toString( ArgList[1] ) );
        out.flush();
        out.close();
      } catch ( Exception er ) {
        throw Context.reportRuntimeError( er.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call appendToFile requires arguments." );
    }
  }

  public static Object getFiscalDate( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList ) ) {
          return null;
        } else if ( isUndefined( ArgList ) ) {
          return Context.getUndefinedValue();
        }
        java.util.Date dIn = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
        Calendar startDate = Calendar.getInstance();
        Calendar fisStartDate = Calendar.getInstance();
        Calendar fisOffsetDate = Calendar.getInstance();
        startDate.setTime( dIn );
        Format dfFormatter = new SimpleDateFormat( "dd.MM.yyyy" );
        String strOffsetDate = Context.toString( ArgList[1] ) + String.valueOf( startDate.get( Calendar.YEAR ) );
        java.util.Date dOffset = (java.util.Date) dfFormatter.parseObject( strOffsetDate );
        fisOffsetDate.setTime( dOffset );

        String strFisStartDate = "01.01." + String.valueOf( startDate.get( Calendar.YEAR ) + 1 );
        fisStartDate.setTime( (java.util.Date) dfFormatter.parseObject( strFisStartDate ) );
        int iDaysToAdd = (int) ( ( startDate.getTimeInMillis() - fisOffsetDate.getTimeInMillis() ) / 86400000 );
        fisStartDate.add( Calendar.DATE, iDaysToAdd );
        return fisStartDate.getTime();
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call getFiscalDate requires 2 arguments." );
    }

  }

  public static double getProcessCount( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    if ( ArgList.length == 1 ) {
      try {
        Object scmO = actualObject.get( "_step_", actualObject );
        StepInterface scm = (StepInterface) Context.jsToJava( scmO, StepInterface.class );
        String strType = Context.toString( ArgList[0] ).toLowerCase();

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
        // throw Context.reportRuntimeError(e.toString());
        return 0;
      }
    } else {
      throw Context.reportRuntimeError( "The function call getProcessCount requires 1 argument." );
    }
  }

  public static void writeToLog( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    switch ( ArgList.length ) {
      case 1:
        try {
          if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
            Object scmO = actualObject.get( "_step_", actualObject );
            ScriptValuesMod scm = (ScriptValuesMod) Context.jsToJava( scmO, ScriptValuesMod.class );
            String strMessage = Context.toString( ArgList[0] );
            scm.logDebug( strMessage );
          }
        } catch ( Exception e ) {
          // Ignore errors
        }
        break;
      case 2:
        try {
          if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
            Object scmO = actualObject.get( "_step_", actualObject );
            ScriptValuesMod scm = (ScriptValuesMod) Context.jsToJava( scmO, ScriptValuesMod.class );

            String strType = Context.toString( ArgList[0] ).toLowerCase();
            String strMessage = Context.toString( ArgList[1] );
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
        throw Context.reportRuntimeError( "The function call writeToLog requires 1 or 2 arguments." );
    }
  }

  private static boolean isUndefined( Object ArgList ) {
    return isUndefined( new Object[] { ArgList }, new int[] { 0 } );
  }

  private static boolean isUndefined( Object[] ArgList, int[] iArrToCheck ) {
    for ( int i = 0; i < iArrToCheck.length; i++ ) {
      if ( ArgList[iArrToCheck[i]].equals( Context.getUndefinedValue() ) ) {
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

  public static Object abs( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        } else {
          return new Double( Math.abs( Context.toNumber( ArgList[0] ) ) );
        }
      } catch ( Exception e ) {
        return null;
      }
    } else {
      throw Context.reportRuntimeError( "The function call abs requires 1 argument." );
    }
  }

  public static Object ceil( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return Double.NaN;
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        } else {
          return Math.ceil( Context.toNumber( ArgList[ 0 ] ) );
        }
      } catch ( Exception e ) {
        return null;
      }
    } else {
      throw Context.reportRuntimeError( "The function call ceil requires 1 argument." );
    }
  }

  public static Object floor( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        } else {
          return new Double( Math.floor( Context.toNumber( ArgList[0] ) ) );
        }
      } catch ( Exception e ) {
        return null;
        // throw Context.reportRuntimeError(e.toString());
      }
    } else {
      throw Context.reportRuntimeError( "The function call floor requires 1 argument." );
    }
  }

  public static Object getDayNumber( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        } else {
          java.util.Date dIn = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
          String strType = Context.toString( ArgList[1] ).toLowerCase();
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
        // throw Context.reportRuntimeError(e.toString());
      }
    } else {
      throw Context.reportRuntimeError( "The function call getDayNumber requires 2 arguments." );
    }
  }

  public static Object isWorkingDay( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        } else {
          java.util.Date dIn = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
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
      throw Context.reportRuntimeError( "The function call isWorkingDay requires 1 argument." );
    }
  }

  @SuppressWarnings( "unused" )
  public static Object fireToDB( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    Object oRC = new Object();
    if ( ArgList.length == 2 ) {
      try {
        Object scmO = actualObject.get( "_step_", actualObject );
        ScriptValuesMod scm = (ScriptValuesMod) Context.jsToJava( scmO, ScriptValuesMod.class );
        String strDBName = Context.toString( ArgList[0] );
        String strSQL = Context.toString( ArgList[1] );
        DatabaseMeta ci = DatabaseMeta.findDatabase( scm.getTransMeta().getDatabases(), strDBName );
        if ( ci == null ) {
          throw Context.reportRuntimeError( "Database connection not found: " + strDBName );
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
          throw Context.reportRuntimeError( er.toString() );
        }
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call fireToDB requires 2 arguments." );
    }
    return oRC;
  }

  public static Object dateDiff( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 3 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
          return Context.getUndefinedValue();
        } else {
          java.util.Date dIn1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
          java.util.Date dIn2 = (java.util.Date) Context.jsToJava( ArgList[1], java.util.Date.class );
          String strType = Context.toString( ArgList[2] ).toLowerCase();
          int iRC = 0;

          Calendar startDate = Calendar.getInstance();
          Calendar endDate = Calendar.getInstance();
          startDate.setTime( dIn1 );
          endDate.setTime( dIn2 );

          long endL = endDate.getTimeInMillis();
          long startL = startDate.getTimeInMillis();

          if ( compensateForLocalTime() ) {
            endL += endDate.getTimeZone().getOffset( endL );
            startL += startDate.getTimeZone().getOffset( startL );
          }

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
        throw Context.reportRuntimeError( e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call dateDiff requires 3 arguments." );
    }
  }

  /**
   * <p>Check if DateDiff should compensate for Local time based on the value of a Kettle property ({@link
   * Const#KETTLE_DATEDIFF_DST_AWARE}).</p>
   * <p>This influences calculations where the period between the given dates includes a DST change.</p>
   * <p>Returning {@code true} means that the difference should be done using the dates as local time; returning {@code
   * false}, means that dates should be used as UTC.</p>
   *
   * @return {@code true} if it is to compensate and {@code false} otherwise
   * @see Const#KETTLE_DATEDIFF_DST_AWARE
   */
  protected static boolean compensateForLocalTime() {
    String dstAware =
      Const.NVL( System.getProperty( Const.KETTLE_DATEDIFF_DST_AWARE ), Const.KETTLE_DATEDIFF_DST_AWARE_DEFAULT );

    return Const.KETTLE_DATEDIFF_DST_AWARE_DEFAULT.equals( dstAware );
  }

  public static Object getNextWorkingDay( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    // (Date dIn){
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        }
        java.util.Date dIn = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
        Calendar startDate = Calendar.getInstance();
        startDate.setTime( dIn );
        startDate.add( Calendar.DATE, 1 );
        while ( startDate.get( Calendar.DAY_OF_WEEK ) == Calendar.SATURDAY
          || startDate.get( Calendar.DAY_OF_WEEK ) == Calendar.SUNDAY ) {
          startDate.add( Calendar.DATE, 1 );
        }
        return startDate.getTime();
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call getNextWorkingDay requires 1 argument." );
    }
  }

  public static Object dateAdd( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 3 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
          return Context.getUndefinedValue();
        }
        java.util.Date dIn = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
        String strType = Context.toString( ArgList[1] ).toLowerCase();
        int iValue = (int) Context.toNumber( ArgList[2] );
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
            cal.add( Calendar.DATE, 1 );
            int day = cal.get( Calendar.DAY_OF_WEEK );
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
        throw Context.reportRuntimeError( e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call dateAdd requires 3 arguments." );
    }
  }

  public static String fillString( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return (String) Context.getUndefinedValue();
        }
        String fillChar = Context.toString( ArgList[0] );
        int count = (int) Context.toNumber( ArgList[1] );
        if ( fillChar.length() != 1 ) {
          throw Context.reportRuntimeError( "Please provide a valid Char to the fillString" );
        } else {
          char[] chars = new char[count];
          while ( count > 0 ) {
            chars[--count] = fillChar.charAt( 0 );
          }
          return new String( chars );
        }
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call fillString requires 2 arguments." );
    }
  }

  public static Object isCodepage( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    boolean bRC = false;
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return Context.getUndefinedValue();
        }
        String strValueToCheck = Context.toString( ArgList[0] );
        String strCodePage = Context.toString( ArgList[1] );
        byte[] bytearray = strValueToCheck.getBytes();
        CharsetDecoder d = Charset.forName( strCodePage ).newDecoder();
        CharBuffer r = d.decode( ByteBuffer.wrap( bytearray ) );
        r.toString();
        bRC = true;
      } catch ( Exception e ) {
        bRC = false;
      }
    } else {
      throw Context.reportRuntimeError( "The function call isCodepage requires 2 arguments." );
    }
    return Boolean.valueOf( bRC );
  }

  public static String ltrim( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) Context.getUndefinedValue();
        }
        String strValueToTrim = Context.toString( ArgList[0] );
        return strValueToTrim.replaceAll( "^\\s+", "" );
      } else {
        throw Context.reportRuntimeError( "The function call ltrim requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( "The function call ltrim is not valid : " + e.getMessage() );
    }
  }

  public static String rtrim( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) Context.getUndefinedValue();
        }
        String strValueToTrim = Context.toString( ArgList[0] );
        return strValueToTrim.replaceAll( "\\s+$", "" );
      } else {
        throw Context.reportRuntimeError( "The function call rtrim requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( "The function call rtrim is not valid : " + e.getMessage() );
    }
  }

  public static String lpad( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    // (String valueToPad, String filler, int size) {
    try {
      if ( ArgList.length == 3 ) {
        if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
          return (String) Context.getUndefinedValue();
        }
        String valueToPad = Context.toString( ArgList[0] );
        String filler = Context.toString( ArgList[1] );
        int size = (int) Context.toNumber( ArgList[2] );

        while ( valueToPad.length() < size ) {
          valueToPad = filler + valueToPad;
        }
        return valueToPad;
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( "The function call lpad requires 3 arguments." );
    }
    return null;
  }

  public static String rpad( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 3 ) {
        if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
          return (String) Context.getUndefinedValue();
        }
        String valueToPad = Context.toString( ArgList[0] );
        String filler = Context.toString( ArgList[1] );
        int size = (int) Context.toNumber( ArgList[2] );

        while ( valueToPad.length() < size ) {
          valueToPad = valueToPad + filler;
        }
        return valueToPad;
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( "The function call rpad requires 3 arguments." );
    }
    return null;
  }

  public static Object year( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        }
        java.util.Date dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
        Calendar cal = Calendar.getInstance();
        cal.setTime( dArg1 );
        return new Double( cal.get( Calendar.YEAR ) );
      } else {
        throw Context.reportRuntimeError( "The function call year requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static Object month( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        }
        java.util.Date dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
        Calendar cal = Calendar.getInstance();
        cal.setTime( dArg1 );
        return new Double( cal.get( Calendar.MONTH ) );
      } else {
        throw Context.reportRuntimeError( "The function call month requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }

  }

  public static Object quarter( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        }
        java.util.Date dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
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
        throw Context.reportRuntimeError( "The function call quarter requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static Object week( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return new Double( Double.NaN );
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        }
        java.util.Date dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
        Calendar cal = Calendar.getInstance();
        cal.setTime( dArg1 );
        return new Double( cal.get( Calendar.WEEK_OF_YEAR ) );
      } else {
        throw Context.reportRuntimeError( "The function call week requires 1 argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static Object str2RegExp( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String[] strArr = null;
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return Context.getUndefinedValue();
        }
        String strToMatch = Context.toString( ArgList[0] );
        Pattern p = Pattern.compile( Context.toString( ArgList[1] ) );
        Matcher m = p.matcher( strToMatch );
        if ( m.matches() && m.groupCount() > 0 ) {
          strArr = new String[m.groupCount()];
          for ( int i = 1; i <= m.groupCount(); i++ ) {
            strArr[i - 1] = m.group( i );
          }
        }
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call str2RegExp requires 2 arguments." );
    }
    return strArr;
  }

  public static void touch( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        File file = new File( Context.toString( ArgList[0] ) );
        boolean success = file.createNewFile();
        if ( !success ) {
          file.setLastModified( System.currentTimeMillis() );
        }
      } else {
        throw Context.reportRuntimeError( "The function call touch requires 1 valid argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static Object fileExists( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        File file = new File( Context.toString( ArgList[0] ) );
        return Boolean.valueOf( file.isFile() );
      } else {
        throw Context.reportRuntimeError( "The function call fileExists requires 1 valid argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static Object str2date( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    Object oRC = new Object();
    String sArg1 = "";
    String sArg2 = "";
    String sArg3 = "";
    String sArg4 = "";
    switch ( ArgList.length ) {
      case 0:
        throw Context.reportRuntimeError( "Please provide a valid string to the function call str2date." );
      case 1:
        try {
          if ( isNull( ArgList[0] ) ) {
            return null;
          } else if ( isUndefined( ArgList[0] ) ) {
            return Context.getUndefinedValue();
          }
          sArg1 = Context.toString( ArgList[0] );
          Format dfFormatter = new SimpleDateFormat();
          oRC = dfFormatter.parseObject( sArg1 );
          // if(Double.isNaN(sArg1)) throw Context.reportRuntimeError("The first Argument must be a Number.");
          // DecimalFormat formatter = new DecimalFormat();
          // sRC= formatter.format(sArg1);
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( "Could not apply local format for " + sArg1 + " : " + e.getMessage() );
        }
        break;
      case 2:
        try {
          if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
            return Context.getUndefinedValue();
          }
          sArg1 = Context.toString( ArgList[0] );
          sArg2 = Context.toString( ArgList[1] );
          Format dfFormatter = new SimpleDateFormat( sArg2 );
          oRC = dfFormatter.parseObject( sArg1 );
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( "Could not apply the given format "
            + sArg2 + " on the string for " + sArg1 + " : " + e.getMessage() );
        }
        break;
      case 3:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
            return Context.getUndefinedValue();
          }
          sArg1 = Context.toString( ArgList[0] );
          Format dfFormatter;
          sArg2 = Context.toString( ArgList[1] );
          sArg3 = Context.toString( ArgList[2] );
          if ( sArg3.length() == 2 ) {
            Locale dfLocale = EnvUtil.createLocale( sArg3 );
            dfFormatter = new SimpleDateFormat( sArg2, dfLocale );
            oRC = dfFormatter.parseObject( sArg1 );
          } else {
            throw Context.reportRuntimeError( "Locale " + sArg3 + " is not 2 characters long." );
          }
        } catch ( Exception e ) {
          throw Context
            .reportRuntimeError( "Could not apply the local format for locale "
              + sArg3 + " with the given format " + sArg2 + " on the string for " + sArg1 + " : "
              + e.getMessage() );
        }
        break;
      case 4:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2, 3 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2, 3 } ) ) {
            return Context.getUndefinedValue();
          }
          sArg1 = Context.toString( ArgList[0] );
          DateFormat dfFormatter;
          sArg2 = Context.toString( ArgList[1] );
          sArg3 = Context.toString( ArgList[2] );
          sArg4 = Context.toString( ArgList[3] );

          // If the timezone is not recognized, java will automatically
          // take GMT.
          TimeZone tz = TimeZone.getTimeZone( sArg4 );

          if ( sArg3.length() == 2 ) {
            Locale dfLocale = EnvUtil.createLocale( sArg3 );
            dfFormatter = new SimpleDateFormat( sArg2, dfLocale );
            dfFormatter.setTimeZone( tz );
            oRC = dfFormatter.parseObject( sArg1 );
          } else {
            throw Context.reportRuntimeError( "Locale " + sArg3 + " is not 2 characters long." );
          }
        } catch ( Exception e ) {
          throw Context
            .reportRuntimeError( "Could not apply the local format for locale "
              + sArg3 + " with the given format " + sArg2 + " on the string for " + sArg1 + " : "
              + e.getMessage() );
        }
        break;
      default:
        throw Context.reportRuntimeError( "The function call str2date requires 1, 2, 3, or 4 arguments." );
    }
    return oRC;
  }

  public static Object date2str( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    Object oRC = new Object();
    switch ( ArgList.length ) {
      case 0:
        throw Context.reportRuntimeError( "Please provide a valid date to the function call date2str." );
      case 1:
        try {
          if ( isNull( ArgList ) ) {
            return null;
          } else if ( isUndefined( ArgList ) ) {
            return Context.getUndefinedValue();
          }
          java.util.Date dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
          if ( dArg1.equals( null ) ) {
            return null;
          }
          Format dfFormatter = new SimpleDateFormat();
          oRC = dfFormatter.format( dArg1 );
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( "Could not convert to local format." );
        }
        break;
      case 2:
        try {
          if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
            return Context.getUndefinedValue();
          }
          java.util.Date dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
          String sArg2 = Context.toString( ArgList[1] );
          Format dfFormatter = new SimpleDateFormat( sArg2 );
          oRC = dfFormatter.format( dArg1 );
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( "Could not convert to the given format." );
        }
        break;
      case 3:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
            return Context.getUndefinedValue();
          }
          java.util.Date dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
          DateFormat dfFormatter;
          String sArg2 = Context.toString( ArgList[1] );
          String sArg3 = Context.toString( ArgList[2] );
          if ( sArg3.length() == 2 ) {
            Locale dfLocale = EnvUtil.createLocale( sArg3.toLowerCase() );
            dfFormatter = new SimpleDateFormat( sArg2, dfLocale );
            oRC = dfFormatter.format( dArg1 );
          } else {
            throw Context.reportRuntimeError( "Locale is not 2 characters long." );
          }
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( "Could not convert to the given local format." );
        }
        break;
      case 4:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2, 3 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2, 3 } ) ) {
            return Context.getUndefinedValue();
          }
          java.util.Date dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
          DateFormat dfFormatter;
          String sArg2 = Context.toString( ArgList[1] );
          String sArg3 = Context.toString( ArgList[2] );
          String sArg4 = Context.toString( ArgList[3] );

          // If the timezone is not recognized, java will automatically
          // take GMT.
          TimeZone tz = TimeZone.getTimeZone( sArg4 );

          if ( sArg3.length() == 2 ) {
            Locale dfLocale = EnvUtil.createLocale( sArg3.toLowerCase() );
            dfFormatter = new SimpleDateFormat( sArg2, dfLocale );
            dfFormatter.setTimeZone( tz );
            oRC = dfFormatter.format( dArg1 );
          } else {
            throw Context.reportRuntimeError( "Locale is not 2 characters long." );
          }
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( "Could not convert to the given local format." );
        }
        break;
      default:
        throw Context.reportRuntimeError( "The function call date2str requires 1, 2, 3, or 4 arguments." );
    }
    return oRC;
  }

  public static Object isRegExp( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    if ( ArgList.length >= 2 ) {
      if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
        return null;
      } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
        return Context.getUndefinedValue();
      }
      String strToMatch = Context.toString( ArgList[0] );
      for ( int i = 1; i < ArgList.length; i++ ) {
        Pattern p = Pattern.compile( Context.toString( ArgList[i] ) );
        Matcher m = p.matcher( strToMatch );
        if ( m.matches() ) {
          return new Double( i );
        }
      }
    }
    return new Double( -1 );
  }

  public static void sendMail( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

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
        throw Context.reportRuntimeError( "sendMail: " + e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call sendMail requires 5 arguments." );
    }
  }

  public static String upper( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) Context.getUndefinedValue();
        }
        sRC = Context.toString( ArgList[0] );
        sRC = sRC.toUpperCase();
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "The function call upper is not valid : " + e.getMessage() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call upper requires 1 argument." );
    }
    return sRC;
  }

  public static String lower( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) Context.getUndefinedValue();
        }
        sRC = Context.toString( ArgList[0] );
        sRC = sRC.toLowerCase();
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "The function call lower is not valid : " + e.getMessage() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call lower requires 1 argument." );
    }
    return sRC;
  }

  // Converts the given Numeric to a JScript String
  public static String num2str( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sRC = "";
    switch ( ArgList.length ) {
      case 0:
        throw Context.reportRuntimeError( "The function call num2str requires at least 1 argument." );
      case 1:
        try {
          if ( isNull( ArgList[0] ) ) {
            return null;
          } else if ( isUndefined( ArgList[0] ) ) {
            return (String) Context.getUndefinedValue();
          }
          double sArg1 = Context.toNumber( ArgList[0] );
          if ( Double.isNaN( sArg1 ) ) {
            throw Context.reportRuntimeError( "The first Argument must be a Number." );
          }
          DecimalFormat formatter = new DecimalFormat();
          sRC = formatter.format( sArg1 );
        } catch ( IllegalArgumentException e ) {
          throw Context.reportRuntimeError( "Could not apply the given format on the number : " + e.getMessage() );
        }
        break;
      case 2:
        try {
          if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
            return (String) Context.getUndefinedValue();
          }
          double sArg1 = Context.toNumber( ArgList[0] );
          if ( Double.isNaN( sArg1 ) ) {
            throw Context.reportRuntimeError( "The first Argument must be a Number." );
          }
          String sArg2 = Context.toString( ArgList[1] );
          DecimalFormat formatter = new DecimalFormat( sArg2 );
          sRC = formatter.format( sArg1 );
        } catch ( IllegalArgumentException e ) {
          throw Context.reportRuntimeError( "Could not apply the given format on the number : " + e.getMessage() );
        }
        break;
      case 3:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
            return null;
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
            return (String) Context.getUndefinedValue();
          }
          double sArg1 = Context.toNumber( ArgList[0] );
          if ( Double.isNaN( sArg1 ) ) {
            throw Context.reportRuntimeError( "The first Argument must be a Number." );
          }
          String sArg2 = Context.toString( ArgList[1] );
          String sArg3 = Context.toString( ArgList[2] );
          if ( sArg3.length() == 2 ) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols( EnvUtil.createLocale( sArg3.toLowerCase() ) );
            DecimalFormat formatter = new DecimalFormat( sArg2, dfs );
            sRC = formatter.format( sArg1 );
          }
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( e.toString() );
        }
        break;
      default:
        throw Context.reportRuntimeError( "The function call num2str requires 1, 2, or 3 arguments." );
    }

    return sRC;
  }

  // Converts the given String to a JScript Numeric
  public static Object str2num( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    double dRC = 0.00;
    switch ( ArgList.length ) {
      case 0:
        throw Context.reportRuntimeError( "The function call str2num requires at least 1 argument." );
      case 1:
        try {
          if ( isNull( ArgList[0] ) ) {
            return new Double( Double.NaN );
          } else if ( isUndefined( ArgList[0] ) ) {
            return Context.getUndefinedValue();
          }
          if ( ArgList[0].equals( null ) ) {
            return null;
          }
          String sArg1 = Context.toString( ArgList[0] );
          DecimalFormat formatter = new DecimalFormat();
          dRC = ( formatter.parse( Const.ltrim( sArg1 ) ) ).doubleValue();
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( "Could not convert the given String : " + e.getMessage() );
        }
        break;
      case 2:
        try {
          if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
            return new Double( Double.NaN );
          } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
            return Context.getUndefinedValue();
          }
          String sArg1 = Context.toString( ArgList[0] );
          String sArg2 = Context.toString( ArgList[1] );
          if ( sArg1.equals( "null" ) || sArg2.equals( "null" ) ) {
            return null;
          }
          DecimalFormat formatter = new DecimalFormat( sArg2 );
          dRC = ( formatter.parse( sArg1 ) ).doubleValue();
          return new Double( dRC );
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( "Could not convert the String with the given format :"
            + e.getMessage() );
        }
        // break;
      case 3:
        try {
          if ( isNull( ArgList, new int[] { 0, 1, 2 } ) ) {
            return new Double( Double.NaN );
          } else if ( isUndefined( ArgList, new int[] { 0, 1, 2 } ) ) {
            return Context.getUndefinedValue();
          }
          String sArg1 = Context.toString( ArgList[0] );
          String sArg2 = Context.toString( ArgList[1] );
          String sArg3 = Context.toString( ArgList[2] );
          if ( sArg3.length() == 2 ) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols( EnvUtil.createLocale( sArg3.toLowerCase() ) );
            DecimalFormat formatter = new DecimalFormat( sArg2, dfs );
            dRC = ( formatter.parse( sArg1 ) ).doubleValue();
            return new Double( dRC );
          }
        } catch ( Exception e ) {
          throw Context.reportRuntimeError( e.getMessage() );
        }
        break;
      default:
        throw Context.reportRuntimeError( "The function call str2num requires 1, 2, or 3 arguments." );
    }
    return new Double( dRC );
  }

  public static Object isNum( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        }
        double sArg1 = Context.toNumber( ArgList[0] );
        if ( Double.isNaN( sArg1 ) ) {
          return Boolean.FALSE;
        } else {
          return Boolean.TRUE;
        }
      } catch ( Exception e ) {
        return Boolean.FALSE;
      }
    } else {
      throw Context.reportRuntimeError( "The function call isNum requires 1 argument." );
    }
  }

  public static Object isDate( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        }
        /* java.util.Date d = (java.util.Date) */Context.jsToJava( ArgList[0], java.util.Date.class );
        return Boolean.TRUE;
      } catch ( Exception e ) {
        return Boolean.FALSE;
      }
    } else {
      throw Context.reportRuntimeError( "The function call isDate requires 1 argument." );
    }
  }

  public static Object decode( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length >= 2 ) {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return Context.getUndefinedValue();
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
        throw Context.reportRuntimeError( "The function call decode requires more than 1 argument." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( "The function call decode is not valid : " + e.getMessage() );
    }
  }

  public static String replace( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length >= 2 && ( ArgList.length - 1 ) % 2 == 0 ) {
        if ( isNull( ArgList, new int[] { 0, 1 } ) ) {
          return null;
        } else if ( isUndefined( ArgList, new int[] { 0, 1 } ) ) {
          return (String) Context.getUndefinedValue();
        }
        String objForReplace = Context.toString( ArgList[0] );
        for ( int i = 1; i < ArgList.length - 1; i = i + 2 ) {
          objForReplace =
            objForReplace.replaceAll( Context.toString( ArgList[i] ), Context.toString( ArgList[i + 1] ) );
        }
        return objForReplace;
      } else {
        throw Context.reportRuntimeError( "The function call replace is not valid (wrong number of arguments)" );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( "Function call replace is not valid : " + e.getMessage() );
    }
  }

  // Implementation of the JS AlertBox
  public static String Alert( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    SpoonInterface spoon = SpoonFactory.getInstance();
    if ( ArgList.length == 1 && spoon != null ) {
      String strMessage = Context.toString( ArgList[0] );
      boolean ok = spoon.messageBox( strMessage, "Alert", true, Const.INFO );
      if ( !ok ) {
        throw new RuntimeException( "Alert dialog cancelled by user." );
      }
    }

    return "";
  }

  // Setting EnvironmentVar
  public static void setEnvironmentVar( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sArg1 = "";
    String sArg2 = "";
    if ( ArgList.length == 2 ) {
      try {
        sArg1 = Context.toString( ArgList[0] );
        sArg2 = Context.toString( ArgList[1] );
        System.setProperty( sArg1, sArg2 );
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call setEnvironmentVar requires 2 arguments." );
    }
  }

  // Returning EnvironmentVar
  public static String getEnvironmentVar( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 1 ) {
      try {

        String sArg1 = Context.toString( ArgList[0] );
        // PDI-1276 Function getEnvironmentVar() does not work for user defined variables.
        // check if the system property exists, and if it does not, try getting a Kettle var instead
        if ( System.getProperties().containsValue( sArg1 ) ) {
          sRC = System.getProperty( sArg1, "" );
        } else {
          Object scmo = actualObject.get( "_step_", actualObject );
          Object scmO = Context.jsToJava( scmo, StepInterface.class );

          if ( scmO instanceof StepInterface ) {
            StepInterface scm = (StepInterface) Context.jsToJava( scmO, StepInterface.class );
            sArg1 = Context.toString( ArgList[0] );
            sRC = scm.getVariable( sArg1, "" );
          } else {
            // running in test mode, return ""
            sRC = "";
          }

        }

      } catch ( Exception e ) {
        sRC = "";
      }
    } else {
      throw Context.reportRuntimeError( "The function call getEnvironmentVar requires 1 argument." );
    }
    return sRC;
  }

  public static String trim( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sRC = "";
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) Context.getUndefinedValue();
        }
        sRC = Context.toString( ArgList[0] );
        sRC = Const.trim( sRC );
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "The function call trim is not valid : " + e.getMessage() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call trim requires 1 argument." );
    }
    return sRC;
  }

  public static String substr( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sRC = "";

    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) Context.getUndefinedValue();
        }
        sRC = Context.toString( ArgList[0] );
        int from = (int) Math.round( Context.toNumber( ArgList[1] ) );
        sRC = sRC.substring( from );
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "The function call substr is not valid : " + e.getMessage() );
      }
    } else if ( ArgList.length == 3 ) {
      try {
        int to;
        int strLen;

        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) Context.getUndefinedValue();
        }
        sRC = Context.toString( ArgList[0] );
        int from = (int) Math.round( Context.toNumber( ArgList[1] ) );
        int len = (int) Math.round( Context.toNumber( ArgList[2] ) );

        if ( from < 0 ) {
          throw Context.reportRuntimeError( "start smaller than 0" );
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
        throw Context.reportRuntimeError( "The function call substr is not valid : " + e.getMessage() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call substr requires 2 or 3 arguments." );
    }
    return sRC;
  }

  // Resolve an IP address
  public static String resolveIP( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sRC;
    if ( ArgList.length == 2 ) {
      try {
        InetAddress address = InetAddress.getByName( Context.toString( ArgList[0] ) );
        if ( Context.toString( ArgList[1] ).equals( "IP" ) ) {
          sRC = address.getHostName();
        } else {
          sRC = address.getHostAddress();
        }
        if ( sRC.equals( Context.toString( ArgList[0] ) ) ) {
          sRC = "-";
        }
      } catch ( Exception e ) {
        sRC = "-";
      }
    } else {
      throw Context.reportRuntimeError( "The function call resolveIP requires 2 arguments." );
    }

    return sRC;
  }

  // Loading additional JS Files inside the JavaScriptCode
  public static void LoadScriptFile( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    for ( int i = 0; i < ArgList.length; i++ ) { // don't worry about "undefined" arguments
      checkAndLoadJSFile( actualContext, actualObject, Context.toString( ArgList[i] ) );
    }
  }

  // Adding the ScriptsItemTab to the actual running Context
  public static void LoadScriptFromTab( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      for ( int i = 0; i < ArgList.length; i++ ) { // don't worry about "undefined" arguments
        String strToLoad = Context.toString( ArgList[i] );
        String strScript = actualObject.get( strToLoad, actualObject ).toString();
        actualContext.evaluateString( actualObject, strScript, "_" + strToLoad + "_", 0, null );
      }
    } catch ( Exception e ) {
      // System.out.println(e.toString());
    }
  }

  // Print
  public static void print( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    for ( int i = 0; i < ArgList.length; i++ ) { // don't worry about "undefined" arguments
      java.lang.System.out.print( Context.toString( ArgList[i] ) );
    }
  }

  // Prints Line to the actual System.out
  public static void println( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    print( actualContext, actualObject, ArgList, FunctionContext );
    java.lang.System.out.println();
  }

  // Returns the actual ClassName
  public String getClassName() {
    return "SciptValuesAddedFunctions";
  }

  // Evaluates the given ScriptFile
  private static void checkAndLoadJSFile( Context actualContext, Scriptable eval_scope, String fileName ) {
    Reader inStream = null;
    try {
      inStream = new InputStreamReader( KettleVFS.getInputStream( fileName ) );
      actualContext.evaluateReader( eval_scope, inStream, fileName, 1, null );
    } catch ( FileNotFoundException Signal ) {
      Context.reportError( "Unable to open file \"" + fileName + "\" (reason: \"" + Signal.getMessage() + "\")" );
    } catch ( WrappedException Signal ) {
      Context.reportError( "WrappedException while evaluating file \""
        + fileName + "\" (reason: \"" + Signal.getMessage() + "\")" );
    } catch ( EvaluatorException Signal ) {
      Context.reportError( "EvaluatorException while evaluating file \""
        + fileName + "\" (reason: \"" + Signal.getMessage() + "\")" );
    } catch ( JavaScriptException Signal ) {
      Context.reportError( "JavaScriptException while evaluating file \""
        + fileName + "\" (reason: \"" + Signal.getMessage() + "\")" );
    } catch ( IOException Signal ) {
      Context.reportError( "Error while reading file \""
        + fileName + "\" (reason: \"" + Signal.getMessage() + "\")" );
    } catch ( KettleFileException Signal ) {
      Context.reportError( "Error while reading file \""
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

  public static void setVariable( Context actualContext, Scriptable actualObject, Object[] arguments,
    Function functionContext ) {

    if ( arguments.length != 3 ) {
      throw Context.reportRuntimeError( "The function call setVariable requires 3 arguments." );
    }

    Object stepObject = Context.jsToJava( actualObject.get( "_step_", actualObject ), StepInterface.class );
    if ( stepObject instanceof StepInterface ) {
      StepInterface step = (StepInterface) stepObject;
      Trans trans = step.getTrans();
      final String variableName = Context.toString( arguments[ 0 ] );
      final String variableValue = Context.toString( arguments[ 1 ] );
      final VariableScope variableScope = getVariableScope( Context.toString( arguments[ 2 ] ) );

      // Set variable in step's scope so that it can be retrieved in the same step using getVariable
      step.setVariable( variableName, variableValue );

      switch ( variableScope ) {
        case PARENT:
          setParentScopeVariable( trans, variableName, variableValue );
          break;
        case GRAND_PARENT:
          setGrandParentScopeVariable( trans, variableName, variableValue );
          break;
        case ROOT:
          setRootScopeVariable( trans, variableName, variableValue );
          break;
        case SYSTEM:
          setSystemScopeVariable( trans, variableName, variableValue );
          break;
      }
    }
  }

  static void setRootScopeVariable( Trans trans, String variableName, String variableValue ) {
    trans.setVariable( variableName, variableValue );

    VariableSpace currentSpace = trans;
    VariableSpace parentSpace = trans.getParentVariableSpace();

    // Due to changes operated under BACKLOG-36347 and PDI-19604,
    // a Transformation can be, itself, its parent VariableScope.
    // We have to make this validation in order to avoid an infinite loop
    while ( currentSpace != parentSpace && parentSpace != null ) {
      parentSpace.setVariable( variableName, variableValue );
      currentSpace = parentSpace;
      parentSpace = parentSpace.getParentVariableSpace();
    }
  }

  static void setSystemScopeVariable( Trans trans, final String variableName, final String variableValue ) {
    System.setProperty( variableName, variableValue );

    // Set also all the way to the root as else we will take
    //  stale values
    setRootScopeVariable( trans, variableName, variableValue );
  }

  static void setParentScopeVariable( Trans trans, String variableName, String variableValue ) {
    trans.setVariable( variableName, variableValue );

    VariableSpace parentSpace = trans.getParentVariableSpace();
    if ( parentSpace != null ) {
      parentSpace.setVariable( variableName, variableValue );
    }
  }

  static void setGrandParentScopeVariable( Trans trans, String variableName, String variableValue ) {
    trans.setVariable( variableName, variableValue );

    VariableSpace parentSpace = trans.getParentVariableSpace();
    if ( parentSpace != null ) {
      parentSpace.setVariable( variableName, variableValue );
      VariableSpace grandParentSpace = parentSpace.getParentVariableSpace();
      if ( grandParentSpace != null ) {
        grandParentSpace.setVariable( variableName, variableValue );
      }
    }
  }


  static VariableScope getVariableScope( String codeOfScope ) {
    switch ( codeOfScope ) {
      case "s":
        return VariableScope.SYSTEM;
      case "r":
        return VariableScope.ROOT;
      case "p":
        return VariableScope.PARENT;
      case "g":
        return VariableScope.GRAND_PARENT;
      default:
        throw Context.reportRuntimeError( "The argument type of function call "
          + "setVariable should either be \"s\", \"r\", \"p\", or \"g\"." );
    }
  }

  // Returning EnvironmentVar
  public static String getVariable( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String sRC = "";
    String sArg1 = "";
    String sArg2 = "";
    if ( ArgList.length == 2 ) {
      try {
        Object scmo = actualObject.get( "_step_", actualObject );
        Object scmO = Context.jsToJava( scmo, StepInterface.class );

        if ( scmO instanceof StepInterface ) {
          StepInterface scm = (StepInterface) Context.jsToJava( scmO, StepInterface.class );

          sArg1 = Context.toString( ArgList[0] );
          sArg2 = Context.toString( ArgList[1] );
          return scm.getVariable( sArg1, sArg2 );
        } else {
          // running via the Test button in a dialog
          sArg2 = Context.toString( ArgList[1] );
          return sArg2;
        }
      } catch ( Exception e ) {
        sRC = "";
      }
    } else {
      throw Context.reportRuntimeError( "The function call getVariable requires 2 arguments." );
    }
    return sRC;
  }

  // Return the output row metadata
  public static RowMetaInterface getOutputRowMeta( Context actualContext, Scriptable actualObject,
    Object[] ArgList, Function FunctionContext ) {
    if ( ArgList.length == 0 ) {
      try {
        Object scmO = actualObject.get( "_step_", actualObject );
        try {
          ScriptValuesMod scm = (ScriptValuesMod) Context.jsToJava( scmO, ScriptValuesMod.class );
          return scm.getOutputRowMeta();
        } catch ( Exception e ) {
          ScriptValuesModDummy scm = (ScriptValuesModDummy) Context.jsToJava( scmO, ScriptValuesModDummy.class );
          return scm.getOutputRowMeta();
        }
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "Unable to get the output row metadata because of an error: "
          + Const.CR + e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call getOutputRowMeta doesn't require arguments." );
    }
  }

  // Return the input row metadata
  public static RowMetaInterface getInputRowMeta( Context actualContext, Scriptable actualObject,
    Object[] ArgList, Function FunctionContext ) {
    if ( ArgList.length == 0 ) {
      try {
        Object scmO = actualObject.get( "_step_", actualObject );
        try {
          ScriptValuesMod scm = (ScriptValuesMod) Context.jsToJava( scmO, ScriptValuesMod.class );
          return scm.getInputRowMeta();
        } catch ( Exception e ) {
          ScriptValuesModDummy scm = (ScriptValuesModDummy) Context.jsToJava( scmO, ScriptValuesModDummy.class );
          return scm.getInputRowMeta();
        }
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "Unable to get the input row metadata because of an error: "
          + Const.CR + e.toString() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call getInputRowMeta doesn't require arguments." );
    }
  }

  // Return the input row metadata
  public static Object[] createRowCopy( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        int newSize = (int) Math.round( Context.toNumber( ArgList[0] ) );

        Object scmO = actualObject.get( "row", actualObject );
        Object[] row = (Object[]) Context.jsToJava( scmO, ( new Object[] {} ).getClass() );

        return RowDataUtil.createResizedCopy( row, newSize );
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "Unable to create a row copy: " + Const.CR + e.toString() );
      }
    } else {
      throw Context
        .reportRuntimeError( "The function call createRowCopy requires a single arguments : the new size of the row" );
    }
  }

  // put a row out to the next steps...
  //
  public static void putRow( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        Object[] newRow = (Object[]) Context.jsToJava( ArgList[0], ( new Object[] {} ).getClass() );

        Object scmO = actualObject.get( "_step_", actualObject );
        try {
          ScriptValuesMod step = (ScriptValuesMod) Context.jsToJava( scmO, ScriptValuesMod.class );
          step.putRow( step.getOutputRowMeta(), newRow );
        } catch ( Exception e ) {
          ScriptValuesModDummy step = (ScriptValuesModDummy) Context.jsToJava( scmO, ScriptValuesModDummy.class );
          step.putRow( step.getOutputRowMeta(), newRow );
        }

      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "Unable to pass the new row to the next step(s) because of an error: "
          + Const.CR + e.toString() );
      }
    } else {
      throw Context
        .reportRuntimeError( "The function call putRow requires 1 argument : the output row data (Object[])" );
    }
  }

  public static void deleteFile( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        // Object act = actualObject.get("_step_", actualObject);
        // ScriptValuesMod act = (ScriptValuesMod)Context.toType(scm_delete, ScriptValuesMod.class);

        FileObject fileObject = null;

        try {
          fileObject = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          if ( fileObject.exists() ) {
            if ( fileObject.getType() == FileType.FILE ) {
              if ( !fileObject.delete() ) {
                Context.reportRuntimeError( "We can not delete file [" + Context.toString( ArgList[0] ) + "]!" );
              }
            }

          } else {
            Context.reportRuntimeError( "file [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call deleteFile is not valid." );
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
        throw Context.reportRuntimeError( "The function call deleteFile is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static void createFolder( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        FileObject fileObject = null;

        try {
          fileObject = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          if ( !fileObject.exists() ) {
            fileObject.createFolder();
          } else {
            Context.reportRuntimeError( "folder [" + Context.toString( ArgList[0] ) + "] already exist!" );
          }
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call createFolder is not valid." );
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
        throw Context.reportRuntimeError( "The function call createFolder is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static void copyFile( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    try {
      if ( ArgList.length == 3
        && !isNull( ArgList[0] ) && !isNull( ArgList[1] ) && !isUndefined( ArgList[0] )
        && !isUndefined( ArgList[1] ) ) {
        FileObject fileSource = null, fileDestination = null;

        try {
          // Source file to copy
          fileSource = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          // Destination filename
          fileDestination = KettleVFS.getFileObject( Context.toString( ArgList[1] ) );
          if ( fileSource.exists() ) {
            // Source file exists...
            if ( fileSource.getType() == FileType.FILE ) {
              // Great..source is a file ...
              boolean overwrite = false;
              if ( !ArgList[1].equals( null ) ) {
                overwrite = Context.toBoolean( ArgList[2] );
              }
              boolean destinationExists = fileDestination.exists();
              // Let's copy the file...
              if ( ( destinationExists && overwrite ) || !destinationExists ) {
                FileUtil.copyContent( fileSource, fileDestination );
              }

            }
          } else {
            Context.reportRuntimeError( "file to copy [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call copyFile throw an error : " + e.toString() );
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
        throw Context.reportRuntimeError( "The function call copyFileis not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static double getFileSize( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return 0;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          long filesize = 0;
          if ( file.exists() ) {
            if ( file.getType().equals( FileType.FILE ) ) {
              filesize = file.getContent().getSize();
            } else {
              Context.reportRuntimeError( "[" + Context.toString( ArgList[0] ) + "] is not a file!" );
            }
          } else {
            Context.reportRuntimeError( "file [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }
          return filesize;
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call getFileSize throw an error : " + e.toString() );
        } finally {
          if ( file != null ) {
            try {
              file.close();
            } catch ( Exception e ) {
              // Ignore close errors
            }
          }
        }

      } else {
        throw Context.reportRuntimeError( "The function call getFileSize is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static boolean isFile( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return false;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          boolean isafile = false;
          if ( file.exists() ) {
            if ( file.getType().equals( FileType.FILE ) ) {
              isafile = true;
            } else {
              Context.reportRuntimeError( "[" + Context.toString( ArgList[0] ) + "] is not a file!" );
            }
          } else {
            Context.reportRuntimeError( "file [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }
          return isafile;
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call is File throw an error : " + e.toString() );
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
        throw Context.reportRuntimeError( "The function call isFile is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static boolean isFolder( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return false;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          boolean isafolder = false;
          if ( file.exists() ) {
            if ( file.getType().equals( FileType.FOLDER ) ) {
              isafolder = true;
            } else {
              Context.reportRuntimeError( "[" + Context.toString( ArgList[0] ) + "] is not a folder!" );
            }
          } else {
            Context.reportRuntimeError( "folder [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }
          return isafolder;
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call isFolder throw an error : " + e.toString() );
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
        throw Context.reportRuntimeError( "The function call isFolder is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static String getShortFilename( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          String Filename = null;
          if ( file.exists() ) {
            Filename = file.getName().getBaseName().toString();

          } else {
            Context.reportRuntimeError( "file [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }

          return Filename;
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call getShortFilename throw an error : " + e.toString() );
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
        throw Context.reportRuntimeError( "The function call getShortFilename is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static String getFileExtension( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          String Extension = null;
          if ( file.exists() ) {
            Extension = file.getName().getExtension().toString();

          } else {
            Context.reportRuntimeError( "file [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }

          return Extension;
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call getFileExtension throw an error : " + e.toString() );
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
        throw Context.reportRuntimeError( "The function call getFileExtension is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static String getParentFoldername( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 1 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          String foldername = null;
          if ( file.exists() ) {
            foldername = KettleVFS.getFilename( file.getParent() );

          } else {
            Context.reportRuntimeError( "file [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }

          return foldername;
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call getParentFoldername throw an error : "
            + e.toString() );
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
        throw Context.reportRuntimeError( "The function call getParentFoldername is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static String getLastModifiedTime( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      if ( ArgList.length == 2 && !isNull( ArgList[0] ) && !isUndefined( ArgList[0] ) ) {
        if ( ArgList[0].equals( null ) ) {
          return null;
        }
        FileObject file = null;

        try {
          // Source file
          file = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          String dateformat = Context.toString( ArgList[1] );
          if ( isNull( dateformat ) ) {
            dateformat = "yyyy-MM-dd";
          }
          String lastmodifiedtime = null;
          if ( file.exists() ) {
            java.util.Date lastmodifiedtimedate = new java.util.Date( file.getContent().getLastModifiedTime() );
            java.text.DateFormat dateFormat = new SimpleDateFormat( dateformat );
            lastmodifiedtime = dateFormat.format( lastmodifiedtimedate );

          } else {
            Context.reportRuntimeError( "file [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }

          return lastmodifiedtime;
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call getLastModifiedTime throw an error : "
            + e.toString() );
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
        throw Context.reportRuntimeError( "The function call getLastModifiedTime is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static Object trunc( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    try {
      // 1 argument: normal truncation of numbers
      //
      if ( ArgList.length == 1 ) {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return Context.getUndefinedValue();
        }

        // This is the truncation of a number...
        //
        Double dArg1 = (Double) Context.jsToJava( ArgList[0], Double.class );
        return Double.valueOf( Math.floor( dArg1 ) );

      } else {
        throw Context.reportRuntimeError( "The function call trunc requires 1 argument, a number." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  @SuppressWarnings( "fallthrough" )
  public static Object truncDate( Context actualContext, Scriptable actualObject, Object[] ArgList, Function FunctionContext ) {
      // 2 arguments: truncation of dates to a certain precision
      //
    if ( ArgList.length == 2 ) {
      if ( isNull( ArgList[0] ) ) {
        return null;
      } else if ( isUndefined( ArgList[0] ) ) {
        return Context.getUndefinedValue();
      }

      // This is the truncation of a date...
      // The second argument specifies the level: ms, s, min, hour, day, month, year
      //
      Date dArg1 = null;
      Integer level = null;
      try {
        dArg1 = (java.util.Date) Context.jsToJava( ArgList[0], java.util.Date.class );
        level = (Integer) Context.jsToJava( ArgList[1], Integer.class );
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( e.toString() );
      }
      return truncDate( dArg1, level );
    } else {
      throw Context
        .reportRuntimeError( "The function call truncDate requires 2 arguments: a date and a level (int)" );
    }
  }

  @VisibleForTesting
  static Date truncDate( Date dArg1, Integer level ) {
    Calendar cal = Calendar.getInstance();
    cal.setTime( dArg1 );

    switch ( level.intValue() ) {
      // MONTHS
      case 5:
        cal.set( Calendar.MONTH, 0 );
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
        throw Context.reportRuntimeError( "Argument of TRUNC of date has to be between 0 and 5" );
    }
    return cal.getTime();
  }

  public static void moveFile( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {

    try {
      if ( ArgList.length == 3
        && !isNull( ArgList[0] ) && !isNull( ArgList[1] ) && !isUndefined( ArgList[0] )
        && !isUndefined( ArgList[1] ) ) {
        FileObject fileSource = null, fileDestination = null;

        try {
          // Source file to move
          fileSource = KettleVFS.getFileObject( Context.toString( ArgList[0] ) );
          // Destination filename
          fileDestination = KettleVFS.getFileObject( Context.toString( ArgList[1] ) );
          if ( fileSource.exists() ) {
            // Source file exists...
            if ( fileSource.getType() == FileType.FILE ) {
              // Great..source is a file ...
              boolean overwrite = false;
              if ( !ArgList[1].equals( null ) ) {
                overwrite = Context.toBoolean( ArgList[2] );
              }
              boolean destinationExists = fileDestination.exists();
              // Let's move the file...
              if ( ( destinationExists && overwrite ) || !destinationExists ) {
                fileSource.moveTo( fileDestination );
              }

            }
          } else {
            Context.reportRuntimeError( "file to move [" + Context.toString( ArgList[0] ) + "] can not be found!" );
          }
        } catch ( IOException e ) {
          throw Context.reportRuntimeError( "The function call moveFile throw an error : " + e.toString() );
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
        throw Context.reportRuntimeError( "The function call copyFile is not valid." );
      }
    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
  }

  public static String execProcess( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    String retval = null;
    if ( !isNull( ArgList ) && !isUndefined( ArgList ) ) {
      Process processrun = null;
      try {

        String ligne = "";
        StringBuilder buffer = new StringBuilder();
        processrun = Runtime.getRuntime().exec( Context.toString( ArgList[0] ) );

        // Get process response
        BufferedReader br = new BufferedReader( new InputStreamReader( processrun.getInputStream() ) );

        // Read response lines
        while ( ( ligne = br.readLine() ) != null ) {
          buffer.append( ligne );
        }
        // if (processrun.exitValue()!=0) throw Context.reportRuntimeError("Error while running " + arguments[0]);

        retval = buffer.toString();

      } catch ( Exception er ) {
        throw Context.reportRuntimeError( er.toString() );
      } finally {
        if ( processrun != null ) {
          processrun.destroy();
        }
      }
    } else {
      throw Context.reportRuntimeError( "The function call execProcess is not valid." );
    }
    return retval;
  }

  public static Boolean isEmpty( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        if ( isUndefined( ArgList[0] ) ) {
          throw new Exception( ArgList[0] + " is  undefined!" );
        }
        if ( isNull( ArgList[0] ) ) {
          return Boolean.TRUE;
        }
        if ( Context.toString( ArgList[0] ).length() == 0 ) {
          return Boolean.TRUE;
        } else {
          return Boolean.FALSE;
        }

      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "Error in isEmpty function: " + e.getMessage() );
      }
    } else {
      throw Context.reportRuntimeError( "The function call isEmpty is not valid" );
    }
  }

  public static Boolean isMailValid( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    Boolean isValid;
    if ( ArgList.length == 1 ) {
      try {
        if ( isUndefined( ArgList[0] ) ) {
          throw new Exception( ArgList[0] + " is  undefined!" );
        }
        if ( isNull( ArgList[0] ) ) {
          return Boolean.FALSE;
        }
        if ( Context.toString( ArgList[0] ).length() == 0 ) {
          return Boolean.FALSE;
        }

        String email = Context.toString( ArgList[0] );
        if ( email.indexOf( '@' ) == -1 || email.indexOf( '.' ) == -1 ) {
          return Boolean.FALSE;
        }

        isValid = Boolean.TRUE;

      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "Error in isMailValid function: " + e.getMessage() );
      }
      return isValid;
    } else {
      throw Context.reportRuntimeError( "The function call isMailValid is not valid" );
    }
  }

  public static String escapeXml( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.escapeXML( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call escapeXml requires 1 argument." );

    }
  }

  public static String escapeHtml( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.escapeHtml( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call escapeHtml requires 1 argument." );
    }
  }

  public static String unEscapeHtml( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.unEscapeHtml( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call unEscapeHtml requires 1 argument." );
    }
  }

  public static String unEscapeXml( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.unEscapeXml( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call unEscapeXml requires 1 argument." );

    }
  }

  public static String escapeSQL( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.escapeSQL( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call escapeSQL requires 1 argument." );

    }
  }

  public static String protectXMLCDATA( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.protectXMLCDATA( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call protectXMLCDATA requires 1 argument." );

    }
  }

  public static String removeDigits( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.removeDigits( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call removeDigits requires 1 argument." );

    }
  }

  public static String initCap( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      return Const.initCap( Context.toString( ArgList[0] ) );
    } else {
      throw Context.reportRuntimeError( "The function call initCap requires 1 argument." );

    }
  }

  public static Object loadFileContent( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    Object oRC = new Object();
    try {

      switch ( ArgList.length ) {
        case 0:
          throw Context.reportRuntimeError( "Please provide a filename to the function call loadFileContent." );
        case 1:
          try {
            if ( isNull( ArgList ) ) {
              return null;
            } else if ( isUndefined( ArgList ) ) {
              return Context.getUndefinedValue();
            }
            // Returns file content
            oRC = new String( LoadFileInput.getFileBinaryContent( Context.toString( ArgList[0] ) ) );
          } catch ( Exception e ) {
            throw Context
              .reportRuntimeError( "The function call loadFileContent throw an error : " + e.toString() );
          }
          break;
        case 2:
          try {
            if ( ArgList[0].equals( null ) ) {
              return null;
            } else if ( isUndefined( ArgList[0] ) ) {
              return Context.getUndefinedValue();
            }
            String encoding = null;
            if ( !isUndefined( ArgList[1] ) && !ArgList[1].equals( null ) ) {
              encoding = Context.toString( ArgList[1] );
            }
            // Returns file content
            oRC = new String( LoadFileInput.getFileBinaryContent( Context.toString( ArgList[0] ) ), encoding );
          } catch ( Exception e ) {
            throw Context
              .reportRuntimeError( "The function call loadFileContent throw an error : " + e.toString() );
          }
          break;

        default:
          throw Context.reportRuntimeError( "The function call loadFileContentrequires 1 ou 2 arguments." );
      }

    } catch ( Exception e ) {
      throw Context.reportRuntimeError( e.toString() );
    }
    return oRC;
  }

  public static int getOcuranceString( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    int nr = 0;
    if ( ArgList.length == 2 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return 0;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (Integer) Context.getUndefinedValue();
        }
        if ( isNull( ArgList[1] ) ) {
          return 0;
        } else if ( isUndefined( ArgList[1] ) ) {
          return (Integer) Context.getUndefinedValue();
        }
        String string = Context.toString( ArgList[0] );
        String searchFor = Context.toString( ArgList[1] );
        nr = Const.getOcuranceString( string, searchFor );
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "The function call getOcuranceString is not valid" );
      }
    } else {
      throw Context.reportRuntimeError( "The function call getOcuranceString is not valid" );
    }
    return nr;
  }

  public static String removeCRLF( Context actualContext, Scriptable actualObject, Object[] ArgList,
    Function FunctionContext ) {
    if ( ArgList.length == 1 ) {
      try {
        if ( isNull( ArgList[0] ) ) {
          return null;
        } else if ( isUndefined( ArgList[0] ) ) {
          return (String) Context.getUndefinedValue();
        }

        return Const.removeCRLF( Context.toString( ArgList[0] ) );
      } catch ( Exception e ) {
        throw Context.reportRuntimeError( "The function call removeCRLF is not valid" );
      }
    } else {
      throw Context.reportRuntimeError( "The function call removeCRLF is not valid" );
    }
  }
}
