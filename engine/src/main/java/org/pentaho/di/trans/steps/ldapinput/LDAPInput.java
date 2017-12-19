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

package org.pentaho.di.trans.steps.ldapinput;

import java.util.HashSet;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Read LDAP Host, convert them to rows and writes these to one or more output streams.
 *
 * @author Samatar
 * @since 21-09-2007
 */
public class LDAPInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = LDAPInputMeta.class; // for i18n purposes, needed by Translator2!!

  private LDAPInputMeta meta;
  private LDAPInputData data;

  public LDAPInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    if ( !data.dynamic ) {
      if ( first ) {

        first = false;

        // Create the output row meta-data
        data.outputRowMeta = new RowMeta();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore ); // get the
                                                                                                      // metadata
                                                                                                      // populated

        // Create convert meta-data objects that will contain Date & Number formatters
        //
        data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

        // Search records once
        search( data.staticSearchBase, data.staticFilter );
      }
    }

    Object[] outputRowData = null;

    try {
      outputRowData = getOneRow();

      if ( outputRowData == null ) {
        setOutputDone();
        return false;
      }

      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "LDAPInput.log.ReadRow" ), data.outputRowMeta
          .getString( outputRowData ) );
      }

      if ( checkFeedback( getLinesInput() ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LDAPInput.log.LineRow" ) + getLinesInput() );
        }
      }

      return true;

    } catch ( Exception e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "LDAPInput.log.Exception", e.getMessage() ) );
        setErrors( 1 );
        logError( Const.getStackTracker( e ) );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), outputRowData, 1, errorMessage, null, "LDAPINPUT001" );
      }
    }
    return true;
  }

  private boolean dynamicSearch() throws KettleException {

    data.readRow = getRow();
    // Get row from input rowset & set row busy!
    if ( data.readRow == null ) {
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "LDAPInput.Log.FinishedProcessing" ) );
      }
      setOutputDone();
      return false;
    }
    if ( first ) {
      first = false;

      if ( meta.isDynamicSearch() ) {
        if ( Utils.isEmpty( meta.getDynamicSearchFieldName() ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "LDAPInput.Error.DynamicSearchFieldMissing" ) );
        }
      }
      if ( meta.isDynamicFilter() ) {
        if ( Utils.isEmpty( meta.getDynamicFilterFieldName() ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "LDAPInput.Error.DynamicFilterFieldMissing" ) );
        }
      }

      // Create the output row meta-data
      data.nrIncomingFields = getInputRowMeta().size();
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore ); // get the metadata
                                                                                                    // populated

      // Create convert meta-data objects that will contain Date & Number formatters
      //
      data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

      if ( meta.isDynamicSearch() ) {
        data.indexOfSearchBaseField = getInputRowMeta().indexOfValue( meta.getDynamicSearchFieldName() );
        if ( data.indexOfSearchBaseField < 0 ) {
          // The field is unreachable !
          throw new KettleException( BaseMessages.getString( PKG, "LDAPInput.Exception.CouldnotFindField", meta
            .getDynamicSearchFieldName() ) );
        }
      }
      if ( meta.isDynamicFilter() ) {
        data.indexOfFilterField = getInputRowMeta().indexOfValue( meta.getDynamicFilterFieldName() );
        if ( data.indexOfFilterField < 0 ) {
          // The field is unreachable !
          throw new KettleException( BaseMessages.getString( PKG, "LDAPInput.Exception.CouldnotFindField", meta
            .getDynamicFilterFieldName() ) );
        }
      }
    } // end if

    String searchBase = data.staticSearchBase;
    if ( data.indexOfSearchBaseField > 0 ) {
      // retrieve dynamic search base value
      searchBase = getInputRowMeta().getString( data.readRow, data.indexOfSearchBaseField );
    }
    String filter = data.staticFilter;
    if ( data.indexOfFilterField >= 0 ) {
      // retrieve dynamic filter string
      filter = getInputRowMeta().getString( data.readRow, data.indexOfFilterField );
    }

    search( searchBase, filter );

    return true;
  }

  private Object[] getOneRow() throws KettleException {

    if ( data.dynamic ) {
      while ( data.readRow == null || ( data.attributes = data.connection.getAttributes() ) == null ) {
        // no records to retrieve
        // we need to perform another search with incoming row
        if ( !dynamicSearch() ) {
          // we finished with incoming rows
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "LDAPInput.Log.FinishedProcessing" ) );
          }
          return null;
        }
      }
    } else {
      // search base is static
      // just try to return records
      data.attributes = data.connection.getAttributes();
    }
    if ( data.attributes == null ) {
      // no more records
      return null;
    }
    return buildRow();

  }

  private Object[] buildRow() throws KettleException {

    // Build an empty row based on the meta-data
    Object[] outputRowData = buildEmptyRow();

    if ( data.dynamic ) {
      // Reserve room for new row
      System.arraycopy( data.readRow, 0, outputRowData, 0, data.readRow.length );
    }

    try {

      // Execute for each Input field...
      for ( int i = 0; i < meta.getInputFields().length; i++ ) {

        LDAPInputField field = meta.getInputFields()[i];
        // Get attribute value
        int index = data.nrIncomingFields + i;
        Attribute attr = data.attributes.get( field.getRealAttribute() );
        if ( attr != null ) {
          // Let's try to get value of this attribute
          outputRowData[index] = getAttributeValue( field, attr, index, outputRowData[index] );
        }

        // Do we need to repeat this field if it is null?
        if ( field.isRepeated() ) {
          if ( data.previousRow != null && outputRowData[index] == null ) {
            outputRowData[index] = data.previousRow[index];
          }
        }

      } // End of loop over fields...

      int fIndex = data.nrIncomingFields + data.nrfields;

      // See if we need to add the row number to the row...
      if ( meta.includeRowNumber() && !Utils.isEmpty( meta.getRowNumberField() ) ) {
        outputRowData[fIndex] = new Long( data.rownr );
      }

      RowMetaInterface irow = getInputRowMeta();

      data.previousRow = irow == null ? outputRowData : irow.cloneRow( outputRowData ); // copy it to make
      // surely the next step doesn't change it in between...
      data.rownr++;

      incrementLinesInput();

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "LDAPInput.Exception.CanNotReadLDAP" ), e );
    }
    return outputRowData;
  }

  private Object getAttributeValue( LDAPInputField field, Attribute attr, int i, Object outputRowData ) throws Exception {

    if ( field.getType() == ValueMetaInterface.TYPE_BINARY ) {
      // It's a binary field
      // no need to convert, just return the value as it
      try {
        return attr.get();
      } catch ( java.lang.ClassCastException e ) {
        return attr.get().toString().getBytes();
      }
    }

    String retval = null;
    if ( field.getReturnType() == LDAPInputField.FETCH_ATTRIBUTE_AS_BINARY
      && field.getType() == ValueMetaInterface.TYPE_STRING ) {
      // Convert byte[] to string
      return LDAPConnection.extractBytesAndConvertToString( attr, field.isObjectSid() );
    }

    // extract as string
    retval = extractString( attr );

    // DO Trimming!
    switch ( field.getTrimType() ) {
      case LDAPInputField.TYPE_TRIM_LEFT:
        retval = Const.ltrim( retval );
        break;
      case LDAPInputField.TYPE_TRIM_RIGHT:
        retval = Const.rtrim( retval );
        break;
      case LDAPInputField.TYPE_TRIM_BOTH:
        retval = Const.trim( retval );
        break;
      default:
        break;
    }

    // DO CONVERSIONS...
    //
    ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta( i );
    ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta( i );
    return targetValueMeta.convertData( sourceValueMeta, retval );

  }

  private String extractString( Attribute attr ) throws Exception {
    StringBuilder attrStr = new StringBuilder();
    for ( NamingEnumeration<?> eattr = attr.getAll(); eattr.hasMore(); ) {
      if ( attrStr.length() > 0 ) {
        attrStr.append( data.multi_valuedFieldSeparator );
      }
      attrStr.append( eattr.next().toString() );
    }
    return attrStr.toString();
  }

  private void connectServerLdap() throws KettleException {
    // Limit returned attributes to user selection
    data.attrReturned = new String[meta.getInputFields().length];

    data.attributesBinary = new HashSet<String>();
    // Get user selection attributes
    for ( int i = 0; i < meta.getInputFields().length; i++ ) {
      LDAPInputField field = meta.getInputFields()[i];
      // get real attribute name
      String name = environmentSubstitute( field.getAttribute() );
      field.setRealAttribute( name );

      // specify attributes to be returned in binary format
      if ( field.getReturnType() == LDAPInputField.FETCH_ATTRIBUTE_AS_BINARY ) {
        data.attributesBinary.add( name );
      }

      data.attrReturned[i] = name;
    }

    // Define new LDAP connection
    data.connection = new LDAPConnection( log, this, meta, data.attributesBinary );

    for ( int i = 0; i < data.attrReturned.length; i++ ) {
      LDAPInputField field = meta.getInputFields()[i];
      // Do we need to sort based on some attributes?
      if ( field.isSortedKey() ) {
        data.connection.addSortingAttributes( data.attrReturned[i] );
      }
    }

    if ( meta.UseAuthentication() ) {
      String username = environmentSubstitute( meta.getUserName() );
      String password = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( meta.getPassword() ) );
      data.connection.connect( username, password );
    } else {
      data.connection.connect();
    }

    // Time Limit
    if ( meta.getTimeLimit() > 0 ) {
      data.connection.setTimeLimit( meta.getTimeLimit() * 1000 );
    }
    // Set the page size?
    if ( meta.isPaging() ) {
      data.connection.SetPagingSize( Const.toInt( environmentSubstitute( meta.getPageSize() ), -1 ) );
    }
  }

  private void search( String searchBase, String filter ) throws KettleException {

    // Set the filter string. The more exact of the search string
    // Set the Search base.This is the place where the search will
    data.connection.search( searchBase, filter, meta.getRowLimit(), data.attrReturned, meta.getSearchScope() );
  }

  /**
   * Build an empty row based on the meta-data...
   *
   * @return
   */

  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    return rowData;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (LDAPInputMeta) smi;
    data = (LDAPInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.rownr = 1L;
      // Get multi valued field separator
      data.multi_valuedFieldSeparator = environmentSubstitute( meta.getMultiValuedSeparator() );
      data.nrfields = meta.getInputFields().length;
      // Set the filter string
      data.staticFilter = environmentSubstitute( meta.getFilterString() );
      // Set the search base
      data.staticSearchBase = environmentSubstitute( meta.getSearchBase() );

      data.dynamic = ( meta.isDynamicSearch() || meta.isDynamicFilter() );
      try {
        // Try to connect to LDAP server
        connectServerLdap();

        return true;

      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "LDAPInput.ErrorInit", e.toString() ) );
        stopAll();
        setErrors( 1 );
      }
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (LDAPInputMeta) smi;
    data = (LDAPInputData) sdi;
    if ( data.connection != null ) {
      try {
        // close connection
        data.connection.close();
      } catch ( KettleException e ) {
        logError( BaseMessages.getString( PKG, "LDAPInput.Exception.ErrorDisconecting", e.toString() ) );
      }
    }
    data.attributesBinary = null;

    super.dispose( smi, sdi );
  }

}
