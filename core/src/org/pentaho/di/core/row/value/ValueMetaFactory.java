package org.pentaho.di.core.row.value;

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

  public static ValueMetaInterface createValueMeta(String name, int type, int length, int precision)
      throws KettlePluginException {
    PluginInterface stringPlugin = pluginRegistry.getPlugin(ValueMetaPluginType.class, String.valueOf(type));
    if (stringPlugin == null) {
      throw new KettlePluginException("Unable to locate value meta plugin of type (id) " + type);
    }
    ValueMetaInterface valueMeta = pluginRegistry.loadClass(stringPlugin, ValueMetaInterface.class);
    valueMeta.setName(name);
    valueMeta.setLength(length, precision);
    return valueMeta;
  }

  public static ValueMetaInterface createValueMeta(String name, int type) throws KettlePluginException {
    return createValueMeta(name, type, -1, -1);
  }

  public static ValueMetaInterface createValueMeta(int type) throws KettlePluginException {
    return createValueMeta(null, type, -1, -1);
  }

  public static ValueMetaInterface cloneValueMeta(ValueMetaInterface source, int targetType) throws KettlePluginException {
    ValueMetaInterface target = createValueMeta(source.getName(), targetType, source.getLength(), source.getPrecision());
    target.setConversionMask(source.getConversionMask());
    target.setDecimalSymbol(source.getDecimalSymbol());
    target.setGroupingSymbol(source.getGroupingSymbol());
    target.setStorageType(source.getStorageType());
    if (source.getStorageMetadata()!=null) {
      target.setStorageMetadata(cloneValueMeta(source.getStorageMetadata(), source.getStorageMetadata().getType()));
    }
    target.setStringEncoding(source.getStringEncoding());
    target.setTrimType(source.getTrimType());
    target.setDateFormatLenient(source.isDateFormatLenient());
    target.setDateFormatLocale(source.getDateFormatLocale());
    target.setDateFormatTimeZone(source.getDateFormatTimeZone());
    target.setLenientStringToNumber(source.isLenientStringToNumber());
    target.setLargeTextField(source.isLargeTextField());
    target.setComments(source.getComments());
    target.setCaseInsensitive(source.isCaseInsensitive());
    target.setIndex(source.getIndex());
    
    target.setOrigin(source.getOrigin());
    
    target.setOriginalAutoIncrement(source.isOriginalAutoIncrement());
    target.setOriginalColumnType(source.getOriginalColumnType());
    target.setOriginalColumnTypeName(source.getOriginalColumnTypeName());
    target.setOriginalNullable(source.isOriginalNullable());
    target.setOriginalPrecision(source.getOriginalPrecision());
    target.setOriginalScale(source.getOriginalScale());
    target.setOriginalSigned(source.isOriginalSigned());
    
    return target;
  }
}
