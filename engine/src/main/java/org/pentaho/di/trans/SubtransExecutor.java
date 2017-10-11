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
package org.pentaho.di.trans;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.TransStepUtil;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorData;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorParameters;

import java.util.List;
import java.util.Optional;

/**
 * Will run the given sub-transformation with the rows passed to execute
 */
public class SubtransExecutor {
  private static final Class<?> PKG = SubtransExecutor.class;
  private Trans parentTrans;
  private TransMeta subtransMeta;
  private boolean shareVariables;
  private TransExecutorData transExecutorData;
  private TransExecutorParameters parameters;

  public SubtransExecutor( Trans parentTrans, TransMeta subtransMeta, boolean shareVariables,
                           TransExecutorData transExecutorData, TransExecutorParameters parameters ) {
    this.parentTrans = parentTrans;
    this.subtransMeta = subtransMeta;
    this.shareVariables = shareVariables;
    this.transExecutorData = transExecutorData;
    this.parameters = parameters;
  }

  public Optional<Result> execute( List<RowMetaAndData> rows ) throws KettleException {
    if ( rows.isEmpty() ) {
      return Optional.empty();
    }
    this.transExecutorData.groupTimeStart = System.currentTimeMillis();

    Trans subtrans = this.createSubtrans();
    this.transExecutorData.setExecutorTrans( subtrans );

    // Pass parameter values
    passParametersToTrans( subtrans, rows.get( 0 ) );

    Result result = new Result();
    result.setRows( rows );
    subtrans.setPreviousResult( result );

    subtrans.prepareExecution( this.parentTrans.getArguments() );
    subtrans.startThreads();

    subtrans.waitUntilFinished();

    return Optional.of( subtrans.getResult() );
  }

  private Trans createSubtrans() {
    Trans subTrans = new Trans( this.subtransMeta, this.parentTrans );
    subTrans.setParentTrans( this.parentTrans );
    subTrans.setRepository( this.parentTrans.getRepository() );
    subTrans.setLogLevel( this.parentTrans.getLogLevel() );
    subTrans.setArguments( this.parentTrans.getArguments() );
    if ( this.shareVariables ) {
      subTrans.shareVariablesWith( this.parentTrans );
    }

    subTrans.setInternalKettleVariables( this.parentTrans );
    subTrans.copyParametersFrom( this.subtransMeta );
    subTrans.setPreview( this.parentTrans.isPreview() );
    TransStepUtil.initServletConfig( this.parentTrans, subTrans );
    return subTrans;
  }

  private void passParametersToTrans( Trans internalTrans, RowMetaAndData rowMetaAndData ) throws KettleException {
    internalTrans.clearParameters();
    String[] parameterNames = internalTrans.listParameters();

    for ( int i = 0; i < this.parameters.getVariable().length; ++i ) {
      String variable = this.parameters.getVariable()[ i ];
      String fieldName = this.parameters.getField()[ i ];
      String inputValue = this.parameters.getInput()[ i ];
      String value;
      if ( !Utils.isEmpty( fieldName ) ) {
        int idx = rowMetaAndData.getRowMeta().indexOfValue( fieldName );
        if ( idx < 0 ) {
          throw new KettleException(
            BaseMessages.getString( PKG, "TransExecutor.Exception.UnableToFindField", new String[] { fieldName } ) );
        }

        value = rowMetaAndData.getString( idx, "" );
      } else {
        value = this.parentTrans.environmentSubstitute( inputValue );
      }

      if ( Const.indexOfString( variable, parameterNames ) < 0 ) {
        internalTrans.setVariable( variable, Const.NVL( value, "" ) );
      } else {
        internalTrans.setParameterValue( variable, Const.NVL( value, "" ) );
      }
    }

    internalTrans.activateParameters();
  }
}
