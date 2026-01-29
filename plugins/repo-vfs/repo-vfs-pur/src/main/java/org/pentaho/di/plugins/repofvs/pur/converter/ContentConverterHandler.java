package org.pentaho.di.plugins.repofvs.pur.converter;

import org.pentaho.di.core.Const;
import org.pentaho.di.repository.Repository;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;

import java.util.Map;

/**
 * Used to convert repository files to InputStream
 */
public class ContentConverterHandler implements IRepositoryContentConverterHandler {

  private final Converter simpleConverter;
  private final Converter transConverter;
  private final Converter jobConverter;

  public ContentConverterHandler( Repository repository ) {
    this.simpleConverter = new StreamConverter( repository.getUnderlyingRepository() );
    var bowl = repository.getBowl();
    this.transConverter = new MetaConverter<>( objId -> repository.loadTransformation( objId, null ), bowl );
    this.jobConverter = new MetaConverter<>( objId -> repository.loadJob( objId, null ), bowl );
  }

  @Override
  public Converter getConverter( String extension ) {
    if ( extension == null ) {
      return simpleConverter;
    }
    return switch ( extension.toLowerCase() ) {
      case Const.STRING_JOB_DEFAULT_EXT -> jobConverter;
      case Const.STRING_TRANS_DEFAULT_EXT -> transConverter;
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
