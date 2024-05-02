/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.www;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import javax.servlet.ServletException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.debug.BreakPointListener;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PreviewStepData extends BaseHttpServlet implements CartePluginInterface {
    private static Class<?> PKG = PreviewStepData.class;
    public static final String CONTEXT_PATH = "/kettle/preview-step-data";
    public static final String XML_TAG = "preview-step-data";
    private static final String XML_REQUEST_BODY = "Xml request body";
    private static final String UNABLE_TO_FIND_TRANS = "Unable to find transformation";
    private static final int DEFAULT_PREVIEW_SIZE = 1000;
    private String previewStepName = "previewStepNames";
    private static final String TRANS = "trans";
    private int previewSize;

    final class MetaAndData {
        public RowMetaInterface bufferRowMeta;
        public List<Object[]> bufferRowData;
    }

    public PreviewStepData() {
    }
    public PreviewStepData( TransformationMap transformationMap ) {
        super( transformationMap );
    }

    @Override
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String previewStepNames = request.getParameter( previewStepName );
        String previewSizeParam = request.getParameter( "previewSize" );
        response.setStatus( HttpServletResponse.SC_OK );
        if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
            return;
        }
        if ( log.isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "PreviewStepData.Log.PreviewStepDataRequested" ) );
        }
        PrintWriter out = response.getWriter();
        if (StringUtils.isNotBlank(previewSizeParam)) {
            previewSize = NumberUtils.toInt(previewSizeParam, DEFAULT_PREVIEW_SIZE);
        }
        final MetaAndData metaData = new MetaAndData();
        TransDebugMeta transDebugMeta = null;
        metaData.bufferRowMeta = getPreviewRowsMeta( previewStepNames, transDebugMeta );
        metaData.bufferRowData = getPreviewRows ( previewStepNames, transDebugMeta );
        TransMeta transMeta = null;
        try {
            InputStream requestInputStream = request.getInputStream();
            if ( requestInputStream == null ) {
                sendBadRequest( response, XML_REQUEST_BODY );
                return;
            }
            transMeta = new TransMeta( requestInputStream, null, true, null, null );
            Trans trans = new Trans( transMeta );
            executeTrans( trans );
            setupTransMeta( trans, previewStepNames, previewSize );
            final List<String> previewComplete = new ArrayList<String>();
            transDebugMeta.addBreakPointListers( new BreakPointListener() {
                public void breakPointHit( TransDebugMeta transDebugMeta, StepDebugMeta stepDebugMeta,
                                           RowMetaInterface rowBufferMeta, List<Object[]> rowBuffer ) {
                    String previewStepNames = stepDebugMeta.getStepMeta().getName();
                    previewComplete.add( previewStepNames );
                }
            } );

            transDebugMeta.addRowListenersToTransformation( trans );
            trans.startThreads();
            RowListener rowListener = new RowListener() {
                public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
                    if ( metaData.bufferRowData.size() < previewSize ) {
                        metaData.bufferRowMeta = rowMeta;
                        metaData.bufferRowData.add( row );
                    }
                }
                public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
                    if ( metaData.bufferRowData.size() < previewSize ) {
                        metaData.bufferRowData.add( row );
                    }
                }
                public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
                }
            };

            int bufferSize = 0;
            while ( metaData.bufferRowData.size() < previewSize ) {
                try {
                    Thread.sleep( 500 );
                } catch ( InterruptedException e ) {
                    // Ignore errors
                }
                bufferSize++;
                if( bufferSize > metaData.bufferRowData.size() ){
                    break;
                }
            }
            trans.stopAll();
            JSONObject jsonOutput = generateJSON( metaData, previewStepNames );

            response.setContentType( "application/json" );
            response.setCharacterEncoding( "UTF-8" );
            response.getWriter().write( jsonOutput.toString() );
        } catch ( Exception ex ) {
            /* When we get to this point KettleAuthenticationException has already been wrapped in an Execution Exception
               and that in a KettleException */
            exceptionHandling( ex, response, out, transMeta.getFilename() );
        }
    }

    private  void sendBadRequest( HttpServletResponse response, String parameterName ) throws IOException {
        response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        PrintWriter out = response.getWriter();
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString( PKG, "PreviewStepData.Error.MissingMandatoryParameter", parameterName ) ) );
    }

    private void setupTransMeta( Trans trans, String previewStepNames, int previewSize ) {
        TransDebugMeta transDebugMeta = new TransDebugMeta( trans.getTransMeta() );
        StepMeta stepMeta = trans.getTransMeta().findStep( previewStepNames );
        StepDebugMeta stepDebugMeta = new StepDebugMeta( stepMeta );
        stepDebugMeta.setReadingFirstRows( true );
        stepDebugMeta.setRowCount( previewSize );
        transDebugMeta.getStepDebugMetaMap().put( stepMeta, stepDebugMeta );
    }

    public void exceptionHandling(Exception ex, HttpServletResponse response, PrintWriter out, String transOption) throws IOException {
        Throwable kettleExceptionCause = ex.getCause();
        if ( kettleExceptionCause != null && kettleExceptionCause instanceof ExecutionException ) {
            Throwable executionExceptionCause = kettleExceptionCause.getCause();
            if ( executionExceptionCause != null && executionExceptionCause instanceof KettleAuthenticationException ) {
                response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString( PKG,
                        "PreviewStepData.Error.Authentication", getContextPath() ) ) );
            }
        } else if ( ex.getMessage().contains(UNABLE_TO_FIND_TRANS) ) {
            response.setStatus( HttpServletResponse.SC_NOT_FOUND );
            out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString( PKG,
                    "PreviewStepData.Error.UnableToFindTransformation", transOption ) ) );
        } else {
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            out.println( new WebResult(WebResult.STRING_ERROR, BaseMessages.getString( PKG,
                    "PreviewStepData.Error.UnexpectedError", Const.CR + Const.getStackTracker(ex) ) ) );
        }
    }

    public RowMetaInterface getPreviewRowsMeta( String previewStepNames, TransDebugMeta transDebugMeta ) {
        if ( transDebugMeta == null ) {
            return null;
        }
        RowMetaInterface bufferRowMeta;
        for ( StepMeta stepMeta : transDebugMeta.getStepDebugMetaMap().keySet() ) {
            if ( stepMeta.getName().equals( previewStepNames ) ) {
                StepDebugMeta stepDebugMeta = transDebugMeta.getStepDebugMetaMap().get( stepMeta );
                bufferRowMeta = stepDebugMeta.getRowBufferMeta();
                return bufferRowMeta;
            }
        }
        return null;
    }

    public List<Object[]> getPreviewRows( String previewStepNames, TransDebugMeta transDebugMeta ) {
        if ( transDebugMeta == null ) {
            return null;
        }
        List<Object[]> bufferRowData;
        for ( StepMeta stepMeta : transDebugMeta.getStepDebugMetaMap().keySet() ) {
            if ( stepMeta.getName().equals( previewStepNames ) ) {
                StepDebugMeta stepDebugMeta = transDebugMeta.getStepDebugMetaMap().get( stepMeta );
                bufferRowData = stepDebugMeta.getRowBuffer();
                return bufferRowData;
            }
        }
        return null;
    }

    protected void executeTrans( Trans trans ) throws KettleException {
        trans.setPreview( true );
        trans.prepareExecution( null );
    }

    public JSONObject generateJSON( MetaAndData metaData, String stepName ) {
        JSONArray columnInfoArray = new JSONArray();
        for ( int i = 0; i < metaData.bufferRowMeta.size(); i++ ) {
            String columnName = metaData.bufferRowMeta.getValueMeta(i).getName();
            columnInfoArray.add( columnName );
        }
        JSONArray rowsArray = new JSONArray();
        for ( int i = 0; i < metaData.bufferRowData.size(); i++ ) {
            Object[] row = metaData.bufferRowData.get(i);

            JSONArray dataArray = new JSONArray();
            int columnCount = Math.min( row.length, columnInfoArray.size() );
            for ( int j = 0; j < columnCount; j++ ) {
                Object data = row[j];
                dataArray.add( data != null ? data.toString() : null );
            }

            JSONObject rowObject = new JSONObject();
            rowObject.put( "data", dataArray );

            rowsArray.add( rowObject );
        }
        JSONObject stepJSON = new JSONObject();
        stepJSON.put( "columnInfo", columnInfoArray );
        stepJSON.put( "rows", rowsArray );
        stepJSON.put( "stepName", stepName );
        return stepJSON;
    }

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        super.doGet( request, response );
    }

    public String toString() {
        return "Preview Step Data Handler";
    }

    public String getService() {
        return CONTEXT_PATH + " (" + toString() + ")";
    }

    public String getContextPath() {
        return CONTEXT_PATH;
    }
}
