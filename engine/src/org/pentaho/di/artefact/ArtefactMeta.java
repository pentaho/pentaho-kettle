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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.attributes.AttributesUtil;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLFormatter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ArtefactMeta extends AbstractMeta implements XMLInterface, Comparator<ArtefactMeta>, Comparable<ArtefactMeta>, Cloneable, ResourceExportInterface, RepositoryElementInterface, LoggingObjectInterface {

    /**
     * The package name, used for internationalization of messages.
     */
    private static Class<?> PKG = Artefact.class; // for i18n purposes, needed by Translator2!!

    public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.ARTEFACT;

    public static final String XML_TAG = "artefact";
    protected static final String XML_TAG_INFO = "info";

    private String parentArtefact;

    protected LogChannelInterface log;

    public ArtefactMeta() {
        clear();
        initializeVariablesFrom( null );
    }

    @Override
    public int compareTo(ArtefactMeta o) {
        return compare(this, o);
    }

    @Override
    public int compare(ArtefactMeta t1, ArtefactMeta t2) {
        if (Utils.isEmpty(t1.getFilename())) {

            if (!Utils.isEmpty(t2.getFilename())) {
                return -1;
            }

            // First compare names...
            //
            if (Utils.isEmpty(t1.getName()) && !Utils.isEmpty(t2.getName())) {
                return -1;
            }
            if (!Utils.isEmpty(t1.getName()) && Utils.isEmpty(t2.getName())) {
                return 1;
            }
            int cmpName = t1.getName().compareTo(t2.getName());
            if (cmpName != 0) {
                return cmpName;
            }

            // Same name, compare Repository directory...
            //
            int cmpDirectory = t1.getRepositoryDirectory().getPath().compareTo(t2.getRepositoryDirectory().getPath());
            if (cmpDirectory != 0) {
                return cmpDirectory;
            }

            // Same name, same directory, compare versions
            //
            if (t1.getObjectRevision() != null && t2.getObjectRevision() == null) {
                return 1;
            }
            if (t1.getObjectRevision() == null && t2.getObjectRevision() != null) {
                return -1;
            }
            if (t1.getObjectRevision() == null && t2.getObjectRevision() == null) {
                return 0;
            }
            return t1.getObjectRevision().getName().compareTo(t2.getObjectRevision().getName());

        } else {
            if (Utils.isEmpty(t2.getFilename())) {
                return 1;
            }

            // First compare names
            //
            if (Utils.isEmpty(t1.getName()) && !Utils.isEmpty(t2.getName())) {
                return -1;
            }
            if (!Utils.isEmpty(t1.getName()) && Utils.isEmpty(t2.getName())) {
                return 1;
            }
            int cmpName = t1.getName().compareTo(t2.getName());
            if (cmpName != 0) {
                return cmpName;
            }

            // Same name, compare filenames...
            //
            return t1.getFilename().compareTo(t2.getFilename());
        }
    }

    @Override
    public String getXML() throws KettleException {
        Props props = null;
        if (Props.isInitialized()) {
            props = Props.getInstance();
        }

        StringBuilder retval = new StringBuilder(800);

        retval.append(XMLHandler.openTag(XML_TAG)).append(Const.CR);

        retval.append("  ").append(XMLHandler.openTag(XML_TAG_INFO)).append(Const.CR);

        retval.append("    ").append(XMLHandler.addTagValue("name", name));
        retval.append("    ").append(XMLHandler.addTagValue("parentArtefact", parentArtefact));

        retval.append("    ").append(XMLHandler.addTagValue("created_user", createdUser));
        retval.append("    ").append(XMLHandler.addTagValue("created_date", XMLHandler.date2string(createdDate)));
        retval.append("    ").append(XMLHandler.addTagValue("modified_user", modifiedUser));
        retval.append("    ").append(XMLHandler.addTagValue("modified_date", XMLHandler.date2string(modifiedDate)));

        retval.append("  ").append(XMLHandler.closeTag(XML_TAG_INFO)).append(Const.CR);

        retval.append(AttributesUtil.getAttributesXml(attributesMap));

        retval.append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);

        return XMLFormatter.format(retval.toString());
    }

    @Override
    public String getFileType() {
        return LastUsedFile.FILE_TYPE_TRANSFORMATION;
    }

    @Override
    public String[] getFilterNames() {
        return Const.getArtefactFilterNames();
    }

    @Override
    public String[] getFilterExtensions() {
        return Const.STRING_ARTEFACT_FILTER_EXT;
    }

    @Override
    public String getDefaultExtension() {
        return Const.STRING_ARTEFACT_DEFAULT_EXT;
    }

    @Override
    public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore) throws KettleException {
        try {
            // Handle naming for both repository and XML bases resources...
            //
            String baseName;
            String originalPath;
            String fullname;
            String extension = "art";
            if (Utils.isEmpty(getFilename())) {
                // Assume repository...
                //
                originalPath = directory.getPath();
                baseName = getName();
                fullname =
                        directory.getPath()
                                + (directory.getPath().endsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)
                                ? "" : RepositoryDirectory.DIRECTORY_SEPARATOR) + getName() + "." + extension; //
            } else {
                // Assume file
                //
                FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(getFilename()), space);
                originalPath = fileObject.getParent().getURL().toString();
                baseName = fileObject.getName().getBaseName();
                fullname = fileObject.getURL().toString();
            }

            String
                    exportFileName =
                    resourceNamingInterface
                            .nameResource(baseName, originalPath, extension, ResourceNamingInterface.FileNamingType.ARTEFACT);
            ResourceDefinition definition = definitions.get(exportFileName);
            if (definition == null) {
                // If we do this once, it will be plenty :-)
                //
                TransMeta transMeta = (TransMeta) this.realClone(false);
                // transMeta.copyVariablesFrom(space);

                // Add used resources, modify transMeta accordingly
                // Go through the list of steps, etc.
                // These critters change the steps in the cloned TransMeta
                // At the end we make a new XML version of it in "exported"
                // format...

                // loop over steps, databases will be exported to XML anyway.
                //
                for (StepMeta stepMeta : transMeta.getSteps()) {
                    stepMeta.exportResources(space, definitions, resourceNamingInterface, repository, metaStore);
                }

                // Change the filename, calling this sets internal variables
                // inside of the transformation.
                //
                transMeta.setFilename(exportFileName);

                // All objects get re-located to the root folder
                //
                transMeta.setRepositoryDirectory(new RepositoryDirectory());

                // Set a number of parameters for all the data files referenced so far...
                //
                Map<String, String> directoryMap = resourceNamingInterface.getDirectoryMap();
                if (directoryMap != null) {
                    for (String directory : directoryMap.keySet()) {
                        String parameterName = directoryMap.get(directory);
                        transMeta.addParameterDefinition(parameterName, directory, "Data file path discovered during export");
                    }
                }

                // At the end, add ourselves to the map...
                //
                String transMetaContent = transMeta.getXML();

                definition = new ResourceDefinition(exportFileName, transMetaContent);

                // Also remember the original filename (if any), including variables etc.
                //
                if (Utils.isEmpty(this.getFilename())) { // Repository
                    definition.setOrigin(fullname);
                } else {
                    definition.setOrigin(this.getFilename());
                }

                definitions.put(fullname, definition);
            }
            return exportFileName;
        } catch (FileSystemException e) {
            throw new KettleException(BaseMessages.getString(
                    PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", getFilename()), e);
        } catch (KettleFileException e) {
            throw new KettleException(BaseMessages.getString(
                    PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", getFilename()), e);
        }
    }

    public Object realClone(boolean doClear) {

        try {
            ArtefactMeta artefactMeta = (ArtefactMeta) super.clone();
            if (doClear) {
                artefactMeta.clear();
            } else {
                // Clear out the things we're replacing below
                artefactMeta.databases = new ArrayList<>();
                artefactMeta.notes = new ArrayList<>();
                artefactMeta.slaveServers = new ArrayList<>();
                artefactMeta.namedParams = new NamedParamsDefault();
            }
            for (DatabaseMeta db : databases) {
                artefactMeta.addDatabase((DatabaseMeta) db.clone());
            }

            for (NotePadMeta note : notes) {
                artefactMeta.addNote((NotePadMeta) note.clone());
            }

            for (SlaveServer slave : slaveServers) {
                artefactMeta.getSlaveServers().add((SlaveServer) slave.clone());
            }

            for (String key : listParameters()) {
                artefactMeta.addParameterDefinition(key, getParameterDefault(key), getParameterDescription(key));
            }

            return artefactMeta;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setInternalKettleVariables(VariableSpace var) {
        //nop
    }

    @Override
    protected void setInternalFilenameKettleVariables(VariableSpace var) {
        //nop
    }

    @Override
    protected void setInternalNameKettleVariable(VariableSpace var) {
        //nop
    }

    @Override
    public String getLogChannelId() {
        return log.getLogChannelId();
    }

    @Override
    public LoggingObjectType getObjectType() {
        return LoggingObjectType.ARTEFACTMETA;
    }

    @Override
    public boolean isGatheringMetrics() {
        return log.isGatheringMetrics();
    }

    @Override
    public void setGatheringMetrics(boolean gatheringMetrics) {
        log.setGatheringMetrics( gatheringMetrics );
    }

    @Override
    public void setForcingSeparateLogging(boolean forcingSeparateLogging) {
        log.setForcingSeparateLogging( forcingSeparateLogging );
    }

    @Override
    public boolean isForcingSeparateLogging() {
        return log.isForcingSeparateLogging();
    }

    @Override
    public RepositoryObjectType getRepositoryElementType() {
        return REPOSITORY_ELEMENT_TYPE;
    }

    public List<LogTableInterface> getLogTables() {
        ArrayList logTables = new ArrayList();
//        logTables.add(this.channelLogTable);
        return logTables;
    }
}
