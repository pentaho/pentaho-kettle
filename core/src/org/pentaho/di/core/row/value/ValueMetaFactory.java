package org.pentaho.di.core.row.value;

import java.util.ArrayList;
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

  public static ValueMetaInterface cloneValueMeta(ValueMetaInterface source) throws KettlePluginException {
    return cloneValueMeta(source, source.getType());
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
  
  public static String[] getValueMetaNames() {
    List<String> strings = new ArrayList<String>();
    List<PluginInterface> plugins = pluginRegistry.getPlugins(ValueMetaPluginType.class);
    for (PluginInterface plugin : plugins) {
      int id = Integer.valueOf(plugin.getIds()[0]);
      if (id>0 && id!=ValueMetaInterface.TYPE_SERIALIZABLE) {
        strings.add(plugin.getName());
      }
    }
    return strings.toArray(new String[strings.size()]);
  }
  
  public static String[] getAllValueMetaNames() {
    List<String> strings = new ArrayList<String>();
    List<PluginInterface> plugins = pluginRegistry.getPlugins(ValueMetaPluginType.class);
    for (PluginInterface plugin : plugins) {
      String id = plugin.getIds()[0];
      if (!("0".equals(id))) {
        strings.add(plugin.getName());
      }
    }
    return strings.toArray(new String[strings.size()]);
  }

  public static String getValueMetaName(int type) {
    for (PluginInterface plugin : pluginRegistry.getPlugins(ValueMetaPluginType.class)) {
      if (Integer.toString(type).equals(plugin.getIds()[0])) {
        return plugin.getName();
      }
    }
    return "-";
  }

  public static int getIdForValueMeta(String valueMetaName) {
    for (PluginInterface plugin : pluginRegistry.getPlugins(ValueMetaPluginType.class)) {
      if (valueMetaName!=null && valueMetaName.equalsIgnoreCase(plugin.getName())) {
        return Integer.valueOf(plugin.getIds()[0]);
      }
    }
    return ValueMetaInterface.TYPE_NONE;
  }
  
  public static List<ValueMetaInterface> getValueMetaPluginClasses() throws KettlePluginException {
    
    List<ValueMetaInterface> list = new ArrayList<ValueMetaInterface>();
    
    List<PluginInterface> plugins = pluginRegistry.getPlugins(ValueMetaPluginType.class);
    for (PluginInterface plugin : plugins) {
      ValueMetaInterface valueMetaInterface = (ValueMetaInterface)pluginRegistry.loadClass(plugin);
      list.add(valueMetaInterface);
    }
    
    return list;
  }
}
