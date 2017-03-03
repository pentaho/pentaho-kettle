/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import org.pentaho.di.artefact.ArtefactMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.ui.repository.pur.services.IConnectionAclService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtefactDelegate extends AbstractDelegate implements ITransformer, ISharedObjectsTransformer, java.io.Serializable {

    private static Class<?> PKG = ArtefactDelegate.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

    static final String NODE_ARTEFACT = "artefact";

    static final String PROP_NR_PARAMETERS = "NR_PARAMETERS";

    static final String NODE_PARAMETERS = "parameters";

    private static final String ARTEFACT_PARAM_PREFIX = "__ARTEFACT_PARAM__#";

    private static final String PARAM_KEY = "PARAM_KEY";

    private static final String PARAM_DESC = "PARAM_DESC";

    private static final String PARAM_DEFAULT = "PARAM_DEFAULT";

    private final Repository repo;
    private final IConnectionAclService unifiedRepositoryConnectionAclService;

    public ArtefactDelegate(final Repository repo, final IUnifiedRepository pur ) {
        super();
        this.repo = repo;
        this.unifiedRepositoryConnectionAclService = new UnifiedRepositoryConnectionAclService( pur );
    }

    @Override
    public RepositoryElementInterface dataNodeToElement(DataNode rootNode) throws KettleException {
        ArtefactMeta artefactMeta = new ArtefactMeta();
        dataNodeToElement(rootNode, artefactMeta);
        return artefactMeta;
    }

    @Override
    public void dataNodeToElement(DataNode rootNode, RepositoryElementInterface element) throws KettleException {
        ArtefactMeta artefactMeta = (ArtefactMeta) element;

        Map<String, Map<String, String>> attributesMap = new HashMap<String, Map<String, String>>();
        artefactMeta.setAttributesMap( attributesMap );

        // Load the details at the end, to make sure we reference the databases correctly, etc.
        //
        loadArtefactDetails(rootNode, artefactMeta);

        artefactMeta.eraseParameters();

        DataNode paramsNode = rootNode.getNode(NODE_PARAMETERS);

        int count = (int) paramsNode.getProperty(PROP_NR_PARAMETERS).getLong();
        for (int idx = 0; idx < count; idx++) {
            DataNode paramNode = paramsNode.getNode(ARTEFACT_PARAM_PREFIX + idx);
            String key = getString(paramNode, PARAM_KEY);
            String def = getString(paramNode, PARAM_DEFAULT);
            String desc = getString(paramNode, PARAM_DESC);
            artefactMeta.addParameterDefinition(key, def, desc);
        }

        artefactMeta.activateParameters();
    }

    protected void loadArtefactDetails(final DataNode rootNode, final ArtefactMeta artefactMeta) throws KettleException {

        AttributesMapUtil.loadAttributesMap(rootNode, artefactMeta);
    }

    @Override
    public SharedObjects loadSharedObjects(RepositoryElementInterface element, Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType) throws KettleException {
        ArtefactMeta artefactMeta = (ArtefactMeta) element;
        artefactMeta.setSharedObjects(artefactMeta.readSharedObjects());

        return artefactMeta.getSharedObjects();
    }

    @Override
    public void saveSharedObjects(RepositoryElementInterface element, String versionComment) throws KettleException {
        ArtefactMeta artefactMetaMeta = (ArtefactMeta) element;

    }

    @Override
    public DataNode elementToDataNode(RepositoryElementInterface element) throws KettleException {
        ArtefactMeta artefactMeta = (ArtefactMeta) element;

        DataNode rootNode = new DataNode( NODE_ARTEFACT );

        Map<String, Map<String, String>> attributesMap = new HashMap<String, Map<String, String>>();
        artefactMeta.setAttributesMap( attributesMap );

        // Parameters
        //
        String[] paramKeys = artefactMeta.listParameters();
        DataNode paramsNode = rootNode.addNode( NODE_PARAMETERS );
        paramsNode.setProperty( PROP_NR_PARAMETERS, paramKeys == null ? 0 : paramKeys.length );

        for ( int idx = 0; idx < paramKeys.length; idx++ ) {
            DataNode paramNode = paramsNode.addNode( ARTEFACT_PARAM_PREFIX + idx );
            String key = paramKeys[idx];
            String description = artefactMeta.getParameterDescription( paramKeys[idx] );
            String defaultValue = artefactMeta.getParameterDefault( paramKeys[idx] );

            paramNode.setProperty( PARAM_KEY, key != null ? key : "" ); //$NON-NLS-1$
            paramNode.setProperty( PARAM_DEFAULT, defaultValue != null ? defaultValue : "" ); //$NON-NLS-1$
            paramNode.setProperty( PARAM_DESC, description != null ? description : "" ); //$NON-NLS-1$
        }

        // Let's not forget to save the details of the artefact itself.
        // This includes logging information, parameters, etc.
        //
        saveArtefactDetails(rootNode, artefactMeta);

        return rootNode;
    }

    private void saveArtefactDetails( final DataNode rootNode, final ArtefactMeta artefactMeta ) throws KettleException {

        // Save the logging tables too..
        //
        RepositoryAttributeInterface attributeInterface = new PurRepositoryAttribute( rootNode, artefactMeta.getDatabases() );
        for ( LogTableInterface logTable : artefactMeta.getLogTables() ) {
            logTable.saveToRepository( attributeInterface );
        }

        // Save the artf attribute groups map
        //
        AttributesMapUtil.saveAttributesMap( rootNode, artefactMeta );
    }
}
