package org.pentaho.di.plugins.repofvs.pur.converter;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;

import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the node converters from pdi's pur plugin via reflection.
 * Most files are stored in regular binary/text format and can be fetched using a simple InputStream.
 * Transformations and Jobs are stored in the repository as nodes, and need special handling to be converted to an InputStream.
 * Such converters are supplied in the `pur` PDI plugin. As another PDI plugin, we don't have direct access to those classes.
 * This and other classes in this package enable fetching those converters from another plugin, and ensuring all calls to them
 * have that plugin's classloader in context in case additional classes are loaded during runtime.
 */
public class RepoContentConverterHandler implements IRepositoryContentConverterHandler {

  private static final String PUR_PLUGIN_ID = "PentahoEnterpriseRepository";
  private static final String TRANS_CONVERTER_CLASS = "com.pentaho.repository.importexport.StreamToTransNodeConverter";
  private static final String JOB_CONVERTER_CLASS = "com.pentaho.repository.importexport.StreamToJobNodeConverter";

  private static final Logger log = LoggerFactory.getLogger( RepoContentConverterHandler.class );

  private final Supplier<PluginRegistry> registry = PluginRegistry::getInstance;
  private final Supplier<Converter> transConverter;
  private final Supplier<Converter> jobConverter;
  private final Converter simpleConverter;

  public RepoContentConverterHandler( IUnifiedRepository pur ) {
    var purPluginClassLoader = new LazyLoader<>( this::getPurClassLoader );
    transConverter = new ReflectedNodeConverter( TRANS_CONVERTER_CLASS, pur, purPluginClassLoader );
    jobConverter = new ReflectedNodeConverter( JOB_CONVERTER_CLASS, pur, purPluginClassLoader );
    simpleConverter = new StreamConverter( pur );
  }

  private ClassLoader getPurClassLoader() {
    PluginRegistry plugReg = registry.get();
    PluginInterface plugin = plugReg.findPluginWithId( RepositoryPluginType.class, PUR_PLUGIN_ID );
    try {
      return plugReg.getClassLoader( plugin );
    } catch ( KettlePluginException e ) {
      log.error( "Cannot get PUR Plugin class loader", e );
      return null;
    }
  }

  @Override
  public Converter getConverter( String extension ) {
    if ( extension == null ) {
      return simpleConverter;
    }
    return switch ( extension.toLowerCase() ) {
      case Const.STRING_JOB_DEFAULT_EXT -> jobConverter.get();
      case Const.STRING_TRANS_DEFAULT_EXT -> transConverter.get();
      default -> simpleConverter;
    };
  }

  @Override
  public Map<String, Converter> getConverters() {
    throw new UnsupportedOperationException( "Use getConverter" );
  }

  @Override
  public void addConverter( String extension, Converter converter ) {
    throw new UnsupportedOperationException( "Cannot add converters" );
  }

}
