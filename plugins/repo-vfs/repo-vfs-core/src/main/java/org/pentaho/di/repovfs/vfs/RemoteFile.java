package org.pentaho.di.repovfs.vfs;

import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;

import java.util.Optional;

/** For attaching and caching remote files */
public class RemoteFile {

  public interface Fetcher {
    Optional<RepositoryFileDto> fetchFileInfo();
    Optional<RepositoryFileTreeDto> fetchFileTree();
  }

  private boolean attached;
  private Optional<RepositoryFileDto> file = Optional.empty();
  private final Fetcher fetcher;
  private Optional<RepositoryFileDto[]> children = Optional.empty();
  private boolean fetchChildren = true;

  public RemoteFile( Fetcher fetcher ) {
    this.fetcher = fetcher;
  }

  public RemoteFile( Fetcher fetcher, RepositoryFileDto file ) {
    this.fetcher = fetcher;
    this.attached = true;
    this.file = Optional.of( file );
  }

  public synchronized void detach() {
    this.file = Optional.empty();
    this.children = Optional.empty();
    this.attached = false;
  }

  public synchronized void attachToFile( Optional<RepositoryFileDto> file ) {
    this.file = file;
    attached = true;
  }

  public synchronized void attachToTree( Optional<RepositoryFileTreeDto> fileTree ) {
    this.file = fileTree.map( RepositoryFileTreeDto::getFile );
    this.children = fileTree.map( this::getChildren ).orElse( Optional.empty() );
    attached = true;
  }

  private Optional<RepositoryFileDto[]> getChildren( RepositoryFileTreeDto tree ) {
    var childTree = tree.getChildren();
    if ( childTree != null ) {
      return Optional.of( childTree.stream().map( RepositoryFileTreeDto::getFile ).toArray(
        RepositoryFileDto[]::new ) );
    } else {
      return Optional.empty();
    }
  }

  /** gets remote file. Does nothing if already attached */
  public synchronized void attach() {
    if ( !attached ) {
      if ( fetchChildren ) {
        attachToTree( fetcher.fetchFileTree() );
      } else {
        attachToFile( fetcher.fetchFileInfo() );
      }
    }
  }

  public boolean isAttached() {
    return attached;
  }

  public Optional<RepositoryFileDto> getFile() {
    return file;
  }

  public synchronized void setChildrenCache( RepositoryFileDto[] children ) {
    this.children = Optional.of( children );
  }

  public synchronized Optional<RepositoryFileDto[]> getChildrenCache() {
    return children;
  }

}
