package com.pentaho.di.repovfs.repo;

import java.util.Collections;
import java.util.List;

import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;

/**
 * This class is a proxy for {@linkplain RepositoryFileTreeDto} needed to avoid loading full files' tree during the only
 * request. It follows this contract: if <code>RepositoryFileTreeDto.getChildren()</code> returns <code>null</code>,
 * then it is considered to be not initialised.
 *
 * @author Andrey Khayrutdinov
 */
public class RepositoryFileTreeDtoProxy extends RepositoryFileTreeDto {

  private final RepositoryFileTreeDto dto;
  private final RepositoryClient client;

  public RepositoryFileTreeDtoProxy( RepositoryFileTreeDto dto, RepositoryClient client ) {
    this.dto = dto;
    this.client = client;

    if ( this.dto.getFile().isFolder() ) {
      this.dto.setChildren( null );
    } else {
      this.dto.setChildren( Collections.<RepositoryFileTreeDto>emptyList() );
    }
  }

  public List<RepositoryFileTreeDto> getChildren() {
    List<RepositoryFileTreeDto> children = dto.getChildren();
    if ( children == null ) {
      synchronized ( this ) {
        children = dto.getChildren();
        if ( children == null ) {
          children = client.loadChildren( dto.getFile().getPath() );
          dto.setChildren( children );
        }
      }
    }
    return children;
  }

  public RepositoryFileDto getFile() {
    return dto.getFile();
  }

  public void setFile( final RepositoryFileDto file ) {
    dto.setFile( file );
  }

  public void setChildren( final List<RepositoryFileTreeDto> children ) {
    dto.setChildren( children );
  }

  public RepositoryFileTreeDto getRealObject() {
    return dto;
  }
}
