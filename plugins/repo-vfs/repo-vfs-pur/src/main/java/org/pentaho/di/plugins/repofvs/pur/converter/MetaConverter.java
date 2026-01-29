package org.pentaho.di.plugins.repofvs.pur.converter;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.SharedObjectUtil;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.ConverterException;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Job / Transformation files from repository IDs into InputStreams.<br/>
 * Any use beyond {@link #convert(Serializable)} is not supported.
 */
public class MetaConverter<T extends AbstractMeta & XMLInterface> implements Converter {

  public interface RepoMetaLoader<T> {
    T loadMeta( ObjectId objectId ) throws KettleException;
  }

  private static final Logger log = LoggerFactory.getLogger( MetaConverter.class );

  private final RepoMetaLoader<T> loader;
  private final Bowl sharedObjsBowl;

  public MetaConverter( RepoMetaLoader<T> loader, Bowl repositoryBowl ) {
    this.loader = loader;
    this.sharedObjsBowl = repositoryBowl;
  }

  @Override
  public IRepositoryFileData convert( InputStream inputStream, String charset, String mimeType )
    throws ConverterException {
    throw new UnsupportedOperationException( "Unimplemented method 'convert(InputStream)'" );
  }

  @Override
  public InputStream convert( IRepositoryFileData data ) {
    throw new UnsupportedOperationException( "Unimplemented method 'convert(IRepositoryFileData)'" );
  }

  @Override
  public InputStream convert( Serializable fileId ) {
    try {
      var meta = loader.loadMeta( toObjectId( fileId ) );
      SharedObjectUtil.copySharedObjects( sharedObjsBowl, meta, true );
      SharedObjectUtil.stripObjectIds( meta );
      return convertMetaToInputStream( meta );
    } catch ( KettleException e ) {
      log.error( "Error loading file", e );
      return new ByteArrayInputStream( new byte[ 0 ] );
    }
  }

  private ObjectId toObjectId( Serializable fileId ) {
    if ( fileId instanceof String stringId ) {
      return new StringObjectId( stringId );
    } else {
      log.warn( "Unexpected file ID type {}", fileId.getClass() );
      return new StringObjectId( fileId.toString() );
    }
  }

  private InputStream convertMetaToInputStream( XMLInterface meta ) throws KettleException {
    String xmlContents = XMLHandler.getXMLHeader() + meta.getXML();
    return new ByteArrayInputStream( xmlContents.getBytes( StandardCharsets.UTF_8 ) );
  }

}
