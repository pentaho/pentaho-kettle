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

package org.pentaho.di.trans.steps.yamlinput;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.vfs.KettleVFS;
import org.yaml.snakeyaml.Yaml;

/**
 * Read YAML files, parse them and convert them to rows and writes these to one or more output streams.
 *
 * @author Samatar
 * @since 20-06-2010
 */
public class YamlReader {

  private static final String DEFAULT_LIST_VALUE_NAME = "Value";

  private String filename;
  private String string;
  private FileObject file;

  // document
  // Store all documents available
  private List<Object> documents;
  // Store current document
  private Object document;
  // Store document iterator
  private Iterator<Object> documenti;

  // object current inside a document
  // In case we use a list
  private Object dataList;
  // Store object iterator
  private Iterator<Object> dataListi;

  private boolean useMap;

  private Yaml yaml;

  public YamlReader() {
    this.filename = null;
    this.string = null;
    this.file = null;
    this.documents = new ArrayList<Object>();
    this.useMap = true;
    this.dataList = null;
    this.yaml = new Yaml();
  }

  public void loadFile( FileObject file ) throws Exception {
    this.file = file;
    this.filename = KettleVFS.getFilename( file );
    loadFile( filename );
  }

  public void loadFile( String filename ) throws Exception {
    this.filename = filename;
    this.file = KettleVFS.getFileObject( filename );

    InputStream is = null;
    try {
      is = KettleVFS.getInputStream( getFile() );

      for ( Object data : getYaml().loadAll( is ) ) {
        documents.add( data );
        this.useMap = ( data instanceof Map );
      }

      this.documenti = documents.iterator();

    } finally {
      if ( is != null ) {
        is.close();
      }
    }
  }

  private Yaml getYaml() {
    return this.yaml;
  }

  public void loadString( String string ) throws Exception {
    this.string = string;
    for ( Object data : getYaml().loadAll( getStringValue() ) ) {
      documents.add( data );
      this.useMap = ( data instanceof Map );
    }
    this.documenti = documents.iterator();
  }

  public boolean isMapUsed() {
    return this.useMap;
  }

  @SuppressWarnings( "unchecked" )
  public Object[] getRow( RowMetaInterface rowMeta ) throws KettleException {

    Object[] retval = null;

    if ( getDocument() != null ) {
      if ( isMapUsed() ) {
        Map<Object, Object> map = (Map<Object, Object>) getDocument();
        retval = new Object[rowMeta.size()];
        for ( int i = 0; i < rowMeta.size(); i++ ) {
          ValueMetaInterface valueMeta = rowMeta.getValueMeta( i );
          Object o = null;
          if ( Utils.isEmpty( valueMeta.getName() ) ) {
            o = getDocument().toString();
          } else {
            o = map.get( valueMeta.getName() );
          }
          retval[i] = getValue( o, valueMeta );
        }

        // We have done with this document
        finishDocument();
      } else {
        if ( dataList != null ) {

          List<Object> list = (List<Object>) getDocument();
          if ( list.size() == 1 ) {
            Iterator<Object> it = list.iterator();
            Object value = it.next();
            Map<Object, Object> map = (Map<Object, Object>) value;
            retval = new Object[rowMeta.size()];
            for ( int i = 0; i < rowMeta.size(); i++ ) {
              ValueMetaInterface valueMeta = rowMeta.getValueMeta( i );
              Object o = null;
              if ( Utils.isEmpty( valueMeta.getName() ) ) {
                o = getDocument().toString();
              } else {
                o = map.get( valueMeta.getName() );
              }
              retval[i] = getValue( o, valueMeta );
            }
          } else {

            ValueMetaInterface valueMeta = rowMeta.getValueMeta( 0 );
            retval = new Object[1];

            retval[0] = getValue( dataList, valueMeta );
          }
          dataList = null;
        } else {
          // We are using List
          if ( dataListi.hasNext() ) {
            dataList = dataListi.next();
          } else {
            // We have done with this document
            finishDocument();
          }
        }
      }
    } else {
      // See if we have another document
      getNextDocument();
    }

    if ( retval == null && !isfinishedDocument() ) {
      return getRow( rowMeta );
    }
    return retval;
  }

  private Object getValue( Object value, ValueMetaInterface valueMeta ) {

    if ( value == null ) {
      return null;
    }
    Object o = null;

    if ( value instanceof List ) {
      value = getYaml().dump( value );
    }

    switch ( valueMeta.getType() ) {
      case ValueMetaInterface.TYPE_INTEGER:
        if ( value instanceof Integer ) {
          o = new Long( (Integer) value );
        } else if ( value instanceof BigInteger ) {
          o = new Long( ( (BigInteger) value ).longValue() );
        } else if ( value instanceof Long ) {
          o = new Long( (Long) value );
        } else {
          o = new Long( value.toString() );
        }
        break;
      case ValueMetaInterface.TYPE_NUMBER:
        if ( value instanceof Integer ) {
          o = new Double( (Integer) value );
        } else if ( value instanceof BigInteger ) {
          o = new Double( ( (BigInteger) value ).doubleValue() );
        } else if ( value instanceof Long ) {
          o = new Double( (Long) value );
        } else if ( value instanceof Double ) {
          o = value;
        } else {
          o = new Double( (String) value );
        }
        break;
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( value instanceof Integer ) {
          o = new BigDecimal( (Integer) value );
        } else if ( value instanceof BigInteger ) {
          o = new BigDecimal( (BigInteger) value );
        } else if ( value instanceof Long ) {
          o = new BigDecimal( (Long) value );
        } else if ( value instanceof Double ) {
          o = new BigDecimal( (Double) value );
        }
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        o = value;
        break;
      case ValueMetaInterface.TYPE_DATE:
        o = value;
        break;
      case ValueMetaInterface.TYPE_BINARY:
        o = value;
        break;
      default:
        String s = setMap( value );

        // Do trimming
        switch ( valueMeta.getTrimType() ) {
          case YamlInputField.TYPE_TRIM_LEFT:
            s = Const.ltrim( s );
            break;
          case YamlInputField.TYPE_TRIM_RIGHT:
            s = Const.rtrim( s );
            break;
          case YamlInputField.TYPE_TRIM_BOTH:
            s = Const.trim( s );
            break;
          default:
            break;
        }
        o = s;

        break;
    }
    return o;
  }

  @SuppressWarnings( "unchecked" )
  private void getNextDocument() {
    // See if we have another document
    if ( this.documenti.hasNext() ) {
      // We have another document
      this.document = this.documenti.next();
      if ( !isMapUsed() ) {
        List<Object> list = (List<Object>) getDocument();
        dataListi = list.iterator();
      }
    }
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  private String setMap( Object value ) {
    String result = value.toString();
    if ( value instanceof Map ) {

      Map<Object, Object> map = (Map<Object, Object>) value;
      Iterator it = map.entrySet().iterator();

      int nr = 0;
      while ( it.hasNext() ) {
        Map.Entry pairs = (Map.Entry) it.next();
        String res = pairs.getKey().toString() + ":  " + setMap( pairs.getValue() );
        if ( nr == 0 ) {
          result = "{" + res;
        } else {
          result += "," + res;
        }
        nr++;
      }
      if ( nr > 0 ) {
        result += "}";
      }
    }
    return result;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  public RowMeta getFields() {
    RowMeta rowMeta = new RowMeta();

    Iterator<Object> ito = documents.iterator();
    while ( ito.hasNext() ) {
      Object data = ito.next();
      if ( data instanceof Map ) {
        // First check if we deals with a map

        Map<Object, Object> map = (Map<Object, Object>) data;
        Iterator it = map.entrySet().iterator();
        while ( it.hasNext() ) {
          Map.Entry pairs = (Map.Entry) it.next();
          String valueName = pairs.getKey().toString();
          ValueMetaInterface valueMeta;
          try {
            valueMeta = ValueMetaFactory.createValueMeta( valueName, getType( pairs.getValue() ) );
          } catch ( KettlePluginException e ) {
            valueMeta = new ValueMetaNone( valueName );
          }
          rowMeta.addValueMeta( valueMeta );
        }
      } else if ( data instanceof List ) {

        rowMeta = new RowMeta();
        // Maybe we deals with List
        List<Object> list = (List<Object>) data;
        Iterator<Object> it = list.iterator();
        Object value = it.next();

        if ( list.size() == 1 ) {
          Map<Object, Object> map = (Map<Object, Object>) value;
          Iterator its = map.entrySet().iterator();
          while ( its.hasNext() ) {
            Map.Entry pairs = (Map.Entry) its.next();
            String valueName = pairs.getKey().toString();
            ValueMetaInterface valueMeta;
            try {
              valueMeta = ValueMetaFactory.createValueMeta( valueName, getType( pairs.getValue() ) );
            } catch ( KettlePluginException e ) {
              valueMeta = new ValueMetaNone( valueName );
            }
            rowMeta.addValueMeta( valueMeta );
          }
        } else {
          ValueMetaInterface valueMeta;
          try {
            valueMeta = ValueMetaFactory.createValueMeta( DEFAULT_LIST_VALUE_NAME, getType( value ) );
          } catch ( KettlePluginException e ) {
            valueMeta = new ValueMetaNone( DEFAULT_LIST_VALUE_NAME );
          }
          rowMeta.addValueMeta( valueMeta );
        }
      }
    }

    return rowMeta;
  }

  private int getType( Object value ) {

    if ( value instanceof Integer ) {
      return ValueMetaInterface.TYPE_INTEGER;
    }
    if ( value instanceof Double ) {
      return ValueMetaInterface.TYPE_NUMBER;
    } else if ( value instanceof Long ) {
      return ValueMetaInterface.TYPE_INTEGER;
    } else if ( value instanceof Date ) {
      return ValueMetaInterface.TYPE_DATE;
    } else if ( value instanceof java.sql.Date ) {
      return ValueMetaInterface.TYPE_DATE;
    } else if ( value instanceof Timestamp ) {
      return ValueMetaInterface.TYPE_DATE;
    } else if ( value instanceof Boolean ) {
      return ValueMetaInterface.TYPE_BOOLEAN;
    } else if ( value instanceof BigInteger ) {
      return ValueMetaInterface.TYPE_BIGNUMBER;
    } else if ( value instanceof BigDecimal ) {
      return ValueMetaInterface.TYPE_BIGNUMBER;
    } else if ( value instanceof Byte ) {
      return ValueMetaInterface.TYPE_BINARY;
    }
    return ValueMetaInterface.TYPE_STRING;
  }

  private Object getDocument() {
    return this.document;
  }

  private void finishDocument() {
    this.document = null;
  }

  private boolean isfinishedDocument() {
    return ( this.document == null );
  }

  public void close() throws Exception {
    if ( file != null ) {
      file.close();
    }
    this.documents = null;
    this.yaml = null;
  }

  public FileObject getFile() {
    return this.file;
  }

  public String getStringValue() {
    return this.string;
  }
}
