//CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.artefact;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ExecutorInterface;
import org.pentaho.di.core.ExtensionDataInterface;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.*;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

import java.util.Date;
import java.util.Map;


public class Artefact implements VariableSpace, NamedParams, HasLogChannelInterface, LoggingObjectInterface, ExecutorInterface, ExtensionDataInterface {

    /** The log channel interface. */
    protected LogChannelInterface log;

    /** The log level. */
    protected LogLevel logLevel = LogLevel.BASIC;

    /** The name of the executing server */
    private String executingServer;

    private String executingUser;

    protected ArtefactMeta artefactMeta;
    private Map<String, Object> extensionDataMap;
    private LoggingObjectInterface parent;
    protected String containerObjectId;
    private NamedParams namedParams = new NamedParamsDefault();
    private VariableSpace variables = new Variables();

    @Override
    public String getExecutingServer() {
        if ( executingServer == null ) {
            setExecutingServer( Const.getHostname() );
        }
        return executingServer;
    }

    @Override
    public void setExecutingServer(String executingServer) {
        this.executingServer = executingServer;
    }

    @Override
    public String getExecutingUser() {
        return executingUser;
    }

    @Override
    public void setExecutingUser(String executingUser) {
        this.executingUser = executingUser;
    }

    @Override
    public Map<String, Object> getExtensionDataMap() {
        return extensionDataMap;
    }

    @Override
    public LogChannelInterface getLogChannel() {
        return log;
    }

    @Override
    public String getObjectName() {
        return getName();
    }

    public String getName() {
        if ( artefactMeta == null ) {
            return null;
        }

        return artefactMeta.getName();
    }

    @Override
    public RepositoryDirectoryInterface getRepositoryDirectory() {
        if ( artefactMeta == null ) {
            return null;
        }
        return artefactMeta.getRepositoryDirectory();
    }

    @Override
    public String getFilename() {
        if ( artefactMeta == null ) {
            return null;
        }
        return artefactMeta.getFilename();
    }

    @Override
    public ObjectId getObjectId() {
        if ( artefactMeta == null ) {
            return null;
        }
        return artefactMeta.getObjectId();
    }

    @Override
    public ObjectRevision getObjectRevision() {
        if ( artefactMeta == null ) {
            return null;
        }
        return artefactMeta.getObjectRevision();
    }

    @Override
    public String getLogChannelId() {
        return log.getLogChannelId();
    }

    @Override
    public LoggingObjectInterface getParent() {
        return parent;
    }

    @Override
    public LoggingObjectType getObjectType() {
        return LoggingObjectType.ARTEFACT;
    }

    @Override
    public String getObjectCopy() {
        return null;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public String getContainerObjectId() {
        return containerObjectId;
    }

    @Override
    public Date getRegistrationDate() {
        return null;
    }

    @Override
    public boolean isGatheringMetrics() {
        return log != null && log.isGatheringMetrics();
    }

    @Override
    public void setGatheringMetrics(boolean gatheringMetrics) {
        if ( log != null ) {
            log.setGatheringMetrics( gatheringMetrics );
        }
    }

    @Override
    public void setForcingSeparateLogging(boolean forcingSeparateLogging) {
        if ( log != null ) {
            log.setForcingSeparateLogging( forcingSeparateLogging );
        }
    }

    @Override
    public boolean isForcingSeparateLogging() {
        return log != null && log.isForcingSeparateLogging();
    }

    @Override
    public void addParameterDefinition(String key, String defValue, String description) throws DuplicateParamException {
        namedParams.addParameterDefinition( key, defValue, description );
    }

    @Override
    public void setParameterValue(String key, String value ) throws UnknownParamException {
        namedParams.setParameterValue( key, value );
    }

    @Override
    public String getParameterValue(String key ) throws UnknownParamException {
        return namedParams.getParameterValue( key );
    }

    @Override
    public String getParameterDescription(String key ) throws UnknownParamException {
        return namedParams.getParameterDescription( key );
    }

    @Override
    public String getParameterDefault(String key ) throws UnknownParamException {
        return namedParams.getParameterDefault( key );
    }

    @Override
    public String[] listParameters() {
        return namedParams.listParameters();
    }

    @Override
    public void eraseParameters() {
        namedParams.eraseParameters();
    }

    @Override
    public void copyParametersFrom(NamedParams namedParams) {
        namedParams.copyParametersFrom( namedParams );
    }

    @Override
    public void activateParameters() {
        String[] keys = listParameters();

        for ( String key : keys ) {
            String value;
            try {
                value = getParameterValue( key );
            } catch ( UnknownParamException e ) {
                value = "";
            }

            String defValue;
            try {
                defValue = getParameterDefault( key );
            } catch ( UnknownParamException e ) {
                defValue = "";
            }

            if ( Utils.isEmpty( value ) ) {
                setVariable( key, Const.NVL( defValue, "" ) );
            } else {
                setVariable( key, Const.NVL( value, "" ) );
            }
        }
    }

    @Override
    public void clearParameters() {
        namedParams.clearParameters();
    }

    @Override
    public void initializeVariablesFrom(VariableSpace parent) {
        variables.initializeVariablesFrom( parent );
    }

    @Override
    public void copyVariablesFrom(VariableSpace variableSpace ) {
        variables.copyVariablesFrom( variableSpace );
    }

    @Override
    public void shareVariablesWith(VariableSpace variableSpace) {
        variables = variableSpace;
    }

    @Override
    public VariableSpace getParentVariableSpace() {
        return variables.getParentVariableSpace();
    }

    @Override
    public void setParentVariableSpace(VariableSpace parent) {
        variables.setParentVariableSpace( parent );
    }

    @Override
    public void setVariable(String variableName, String variableValue ) {
        variables.setVariable( variableName, variableValue );
    }

    @Override
    public String getVariable(String variableName, String defaultValue ) {
        return variables.getVariable( variableName, defaultValue );
    }

    @Override
    public String getVariable(String variableName ) {
        return variables.getVariable( variableName );
    }

    @Override
    public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue ) {
        if ( !Utils.isEmpty( variableName ) ) {
            String value = environmentSubstitute( variableName );
            if ( !Utils.isEmpty( value ) ) {
                return ValueMetaString.convertStringToBoolean( value );
            }
        }
        return defaultValue;
    }

    @Override
    public String[] listVariables() {
        return variables.listVariables();
    }

    @Override
    public String environmentSubstitute(String aString) {
        return variables.environmentSubstitute( aString );
    }

    @Override
    public String[] environmentSubstitute(String[] aString) {
        return variables.environmentSubstitute( aString );
    }

    @Override
    public void injectVariables(Map<String, String> prop) {
        variables.injectVariables( prop );
    }

    @Override
    public String fieldSubstitute(String aString, RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
        return variables.fieldSubstitute( aString, rowMeta, rowData );
    }
}
