/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.step.filestream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.streaming.api.StreamSource;
import org.pentaho.di.trans.streaming.common.BaseStreamStep;
import org.pentaho.di.trans.streaming.common.FixedTimeStreamWindow;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.base.Throwables.propagate;

/**
 * An example step plugin for purposes of demonstrating a strategy for handling streams of data.
 */
public class FileStream extends BaseStreamStep implements StepInterface {

  private static Class<?> PKG = FileStreamMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private FileStreamMeta fileStreamMeta;

  public FileStream( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                     Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    super.init( stepMetaInterface, stepDataInterface );

    Preconditions.checkNotNull( stepMetaInterface );
    fileStreamMeta = (FileStreamMeta) stepMetaInterface;

    String sourceFile = getFilePath( fileStreamMeta.getSourcePath() );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "line" ) );

    window = new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, getDuration(), getBatchSize() );

    try {
      source = new TailFileStreamSource( sourceFile, this );
    } catch ( FileNotFoundException e ) {
      logError( e.getLocalizedMessage(), e );
      return false;
    }
    return true;
  }

  @VisibleForTesting
  protected StreamSource<List<Object>> getStreamSource() {
    return source;
  }


  private String getFilePath( String path ) {
    try {
      final FileObject fileObject = KettleVFS.getFileObject( environmentSubstitute( path ) );
      if ( !fileObject.exists() ) {
        throw new FileNotFoundException( path );
      }
      return Paths.get( fileObject.getURL().toURI() ).normalize().toString();
    } catch ( URISyntaxException | FileNotFoundException | FileSystemException | KettleFileException e ) {
      propagate( e );
    }
    return null;
  }

}
