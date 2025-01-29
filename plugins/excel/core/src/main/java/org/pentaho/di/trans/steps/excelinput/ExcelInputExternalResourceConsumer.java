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


package org.pentaho.di.trans.steps.excelinput;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.excelinput.ExcelInput;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class ExcelInputExternalResourceConsumer extends BaseStepExternalResourceConsumer<ExcelInput, ExcelInputMeta> {

  @Override
  public boolean isDataDriven( ExcelInputMeta meta ) {
    return meta.isAcceptingFilenames();
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( Bowl bowl, ExcelInputMeta meta, IAnalysisContext context ) {
    Collection<IExternalResourceInfo> resources = Collections.emptyList();

    // We only need to collect these resources if we're not data-driven and there are no used variables in the
    // metadata relating to external files.
    if ( !isDataDriven( meta ) ) {
      StepMeta parentStepMeta = meta.getParentStepMeta();
      if ( parentStepMeta != null ) {
        TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
        if ( parentTransMeta != null ) {
          String[] paths = meta.getFilePaths( parentTransMeta.getBowl(), parentTransMeta );
          if ( paths != null ) {
            resources = new ArrayList<IExternalResourceInfo>( paths.length );

            for ( String path : paths ) {
              if ( !Const.isEmpty( path ) ) {
                try {

                  IExternalResourceInfo resource = ExternalResourceInfoFactory
                    .createFileResource( KettleVFS.getInstance( parentTransMeta.getBowl() )
                      .getFileObject( path ), true );
                  if ( resource != null ) {
                    resources.add( resource );
                  } else {
                    throw new KettleFileException( "Error getting file resource!" );
                  }
                } catch ( KettleFileException kfe ) {
                  // TODO throw or ignore?
                }
              }
            }
          }
        }
      }
    }
    return resources;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromRow(
    ExcelInput excelInput, RowMetaInterface rowMeta, Object[] row ) {
    Collection<IExternalResourceInfo> resources = new LinkedList<IExternalResourceInfo>();
    ExcelInputMeta meta = (ExcelInputMeta) excelInput.getStepMetaInterface();
    if ( meta == null ) {
      meta = (ExcelInputMeta) excelInput.getStepMeta().getStepMetaInterface();
    }

    try {
      String filename = rowMeta.getString( row, meta.getAcceptingField(), null );
      if ( !Const.isEmpty( filename ) ) {
        FileObject fileObject = KettleVFS.getInstance( excelInput.getTransMeta().getBowl() ).getFileObject( filename );
        resources.add( ExternalResourceInfoFactory.createFileResource( fileObject, true ) );
      }
    } catch ( KettleException kve ) {
      // TODO throw exception or ignore?
    }

    return resources;
  }

  @Override
  public Class<ExcelInputMeta> getMetaClass() {
    return ExcelInputMeta.class;
  }
}
