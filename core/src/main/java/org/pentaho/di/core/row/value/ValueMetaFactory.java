/********************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.row.value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * This class will hand out value meta objects from the plugin registry.
 *
 * @author matt
 *
 */
public class ValueMetaFactory {

  public static PluginRegistry pluginRegistry = PluginRegistry.getInstance();

  public static ValueMetaInterface createValueMeta( String name, int type, int length, int precision ) throws KettlePluginException {
    PluginInterface stringPlugin = pluginRegistry.getPlugin( ValueMetaPluginType.class, String.valueOf( type ) );
    if ( stringPlugin == null ) {
      throw new KettlePluginException( "Unable to locate value meta plugin of type (id) " + type );
    }
    ValueMetaInterface valueMeta = pluginRegistry.loadClass( stringPlugin, ValueMetaInterface.class );
    valueMeta.setName( name );
    valueMeta.setLength( length, precision );
    return valueMeta;
  }

  public static ValueMetaInterface createValueMeta( String name, int type ) throws KettlePluginException {
    return createValueMeta( name, type, -1, -1 );
  }

  public static ValueMetaInterface createValueMeta( int type ) throws KettlePluginException {
    return createValueMeta( null, type, -1, -1 );
  }

  public static ValueMetaInterface cloneValueMeta( ValueMetaInterface source ) throws KettlePluginException {
    return cloneValueMeta( source, source.getType() );
  }

  public static ValueMetaInterface cloneValueMeta( ValueMetaInterface source, int targetType ) throws KettlePluginException {
    ValueMetaInterface target = null;

    // If we're Cloneable and not changing types, call clone()
    if ( source.getType() == targetType ) {
      target = source.clone();
    } else {
      target = createValueMeta( source.getName(), targetType, source.getLength(), source.getPrecision() );
    }

    cloneInfo( source, target );

    return target;
  }

  public static void cloneInfo( ValueMetaInterface source, ValueMetaInterface target ) throws KettlePluginException {
    target.setConversionMask( source.getConversionMask() );
    target.setDecimalSymbol( source.getDecimalSymbol() );
    target.setGroupingSymbol( source.getGroupingSymbol() );
    target.setStorageType( source.getStorageType() );
    if ( source.getStorageMetadata() != null ) {
      target.setStorageMetadata( cloneValueMeta( source.getStorageMetadata(), source
        .getStorageMetadata().getType() ) );
    }
    target.setStringEncoding( source.getStringEncoding() );
    target.setTrimType( source.getTrimType() );
    target.setDateFormatLenient( source.isDateFormatLenient() );
    target.setDateFormatLocale( source.getDateFormatLocale() );
    target.setDateFormatTimeZone( source.getDateFormatTimeZone() );
    target.setLenientStringToNumber( source.isLenientStringToNumber() );
    target.setLargeTextField( source.isLargeTextField() );
    target.setComments( source.getComments() );
    target.setCaseInsensitive( source.isCaseInsensitive() );
    target.setCollatorDisabled( source.isCollatorDisabled() );
    target.setCollatorStrength( source.getCollatorStrength() );
    target.setIndex( source.getIndex() );

    target.setOrigin( source.getOrigin() );

    target.setOriginalAutoIncrement( source.isOriginalAutoIncrement() );
    target.setOriginalColumnType( source.getOriginalColumnType() );
    target.setOriginalColumnTypeName( source.getOriginalColumnTypeName() );
    target.setOriginalNullable( source.isOriginalNullable() );
    target.setOriginalPrecision( source.getOriginalPrecision() );
    target.setOriginalScale( source.getOriginalScale() );
    target.setOriginalSigned( source.isOriginalSigned() );
  }

  public static String[] getValueMetaNames() {
    List<String> strings = new ArrayList<String>();
    List<PluginInterface> plugins = pluginRegistry.getPlugins( ValueMetaPluginType.class );
    for ( PluginInterface plugin : plugins ) {
      int id = Integer.valueOf( plugin.getIds()[0] );
      if ( id > 0 && id != ValueMetaInterface.TYPE_SERIALIZABLE ) {
        strings.add( plugin.getName() );
      }
    }
    return strings.toArray( new String[strings.size()] );
  }

  public static String[] getAllValueMetaNames() {
    List<String> strings = new ArrayList<String>();
    List<PluginInterface> plugins = pluginRegistry.getPlugins( ValueMetaPluginType.class );
    for ( PluginInterface plugin : plugins ) {
      String id = plugin.getIds()[0];
      if ( !( "0".equals( id ) ) ) {
        strings.add( plugin.getName() );
      }
    }
    return strings.toArray( new String[strings.size()] );
  }

  public static String getValueMetaName( int type ) {
    for ( PluginInterface plugin : pluginRegistry.getPlugins( ValueMetaPluginType.class ) ) {
      if ( Integer.toString( type ).equals( plugin.getIds()[0] ) ) {
        return plugin.getName();
      }
    }
    return "-";
  }

  public static int getIdForValueMeta( String valueMetaName ) {
    for ( PluginInterface plugin : pluginRegistry.getPlugins( ValueMetaPluginType.class ) ) {
      if ( valueMetaName != null && valueMetaName.equalsIgnoreCase( plugin.getName() ) ) {
        return Integer.valueOf( plugin.getIds()[0] );
      }
    }
    return ValueMetaInterface.TYPE_NONE;
  }

  public static List<ValueMetaInterface> getValueMetaPluginClasses() throws KettlePluginException {

    List<ValueMetaInterface> list = new ArrayList<ValueMetaInterface>();

    List<PluginInterface> plugins = pluginRegistry.getPlugins( ValueMetaPluginType.class );
    for ( PluginInterface plugin : plugins ) {
      ValueMetaInterface valueMetaInterface = (ValueMetaInterface) pluginRegistry.loadClass( plugin );
      list.add( valueMetaInterface );
    }

    return list;
  }

  /**
   * <p>This method makes attempt to guess kettle value meta interface based on Object class.
   * This may be the case when we somehow obtain an Object as a result of any calculation,
   * and we are trying to assign some ValueMeta for it.</p>
   *
   * <p>As an example - we have target value meta Number (which is java Double under the hood)
   * and value as a BigDecimal.<br />
   * This BigDecimal can be converted to a Double value.<br />
   * we have {@link ValueMetaInterface#convertData(ValueMetaInterface, Object)} call for this
   * where is ValueMetaInterface object is our target value meta, Object is a BigDecimal - so
   * we need to pass ValueMetaBigNumber as a first parameter, value Object as a second and
   * as the result we will have target Double (ValueMetaNumber) value so we can safely
   * put it into output rowset.</p>
   *
   * <p>Something similar we had for ValueMetaBase.getValueFromSQLType(...) to guess value meta
   * for java sql type.</p>
   *
   * <p>Currently this method does not have support for plugin value meta. Hope if this approach
   * will be found usable this may be implemented later.</p>
   *
   * @param object object to guess applicable ValueMetaInterface.
   * @return
   * @see ValueMetaInterface if the kettle value meta is recognized, null otherwise.
   */
  public static ValueMetaInterface guessValueMetaInterface( Object object ) {
    if ( object instanceof Number ) {
      // this is numeric object
      if ( object instanceof BigDecimal ) {
        return new ValueMetaBigNumber();
      } else if ( object instanceof Double ) {
        return new ValueMetaNumber();
      } else if ( object instanceof Long ) {
        return new ValueMetaInteger();
      }
    } else if ( object instanceof String ) {
      return new ValueMetaString();
    } else if ( object instanceof Date ) {
      return new ValueMetaDate();
    } else if ( object instanceof Boolean ) {
      return new ValueMetaBoolean();
    } else if ( object instanceof byte[] ) {
      return new ValueMetaBinary();
    }
    // ask someone else
    return null;
  }
}
