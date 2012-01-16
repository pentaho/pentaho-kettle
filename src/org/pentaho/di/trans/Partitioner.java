/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.w3c.dom.Node;

public interface Partitioner {

	public abstract Partitioner getInstance();
	
	public int getPartition(RowMetaInterface rowMeta, Object[] r ) throws KettleException;
	
	public void setMeta(StepPartitioningMeta meta);

	public String getId( );
	
	public String getDescription( );

	public void setId( String id );
	
	public void setDescription( String description );
	
	public String getDialogClassName();
	
	public Partitioner clone();
	
    public String getXML();
    
	public void loadXML(Node partitioningMethodNode) throws KettleXMLException;

    /**
     * Saves partitioning properties in the repository for the given step.
     * @param rep the repository to save in
     * @param id_transformation the ID of the transformation
     * @param id_step the ID of the step
     * @throws KettleDatabaseException In case anything goes wrong
     */
    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException;

    public void loadRep(Repository rep, ObjectId id_step) throws KettleException;

}
