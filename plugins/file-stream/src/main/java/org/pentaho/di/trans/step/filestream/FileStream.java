/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.filestream;

import com.google.common.base.Preconditions;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.SubtransExecutor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorData;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorParameters;
import org.pentaho.di.trans.streaming.common.BaseStreamStep;
import org.pentaho.di.trans.streaming.common.FixedTimeStreamWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An example step plugin for purposes of demonstrating a strategy for handling streams of data.
 */
public class FileStream extends BaseStreamStep<List<String>> implements StepInterface {

  private static Class<?> PKG = FileStreamMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private FileStreamMeta fileStreamMeta;
  private SubtransExecutor subtransExecutor;

  private final Logger logger = LoggerFactory.getLogger( getClass() );

  public FileStream( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                     Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    Preconditions.checkNotNull( stepMetaInterface );
    fileStreamMeta = (FileStreamMeta) stepMetaInterface;

    Preconditions.checkNotNull( fileStreamMeta.getTransformationPath() );

    try {
      subtransExecutor = new SubtransExecutor(
        getTrans(), new TransMeta( fileStreamMeta.getTransformationPath() ), true,
        new TransExecutorData(), new TransExecutorParameters() );

    } catch ( KettleXMLException | KettleMissingPluginsException e ) {
      logger.error( e.getLocalizedMessage(), e );
    }
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "line" ) );

    window = new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, getDuration(), getBatchSize() );
    try {
      source = new TailFileStreamSource( fileStreamMeta.getSourcePath() );
    } catch ( FileNotFoundException e ) {
      logger.error( e.getLocalizedMessage(), e );
    }
    return super.init( stepMetaInterface, stepDataInterface );
  }

  private int getBatchSize() {
    try {
      return Integer.parseInt( fileStreamMeta.getBatchSize() );

    } catch ( NumberFormatException nfe ) {
      return 50;
    }
  }

  private long getDuration() {
    try {
      return Long.parseLong( fileStreamMeta.getBatchDuration() );
    } catch ( NumberFormatException nfe ) {
      return 5000l;
    }
  }

  @Override public Collection<StepStatus> subStatuses() {
    return subtransExecutor != null ? subtransExecutor.getStatuses().values() : Collections.emptyList();
  }

}
