/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;

public abstract class BasePartitioner implements Partitioner {

	protected StepPartitioningMeta meta;
	protected int nrPartitions = -1;
	protected String id;
	protected String description;
	
	public BasePartitioner( ) {
	}
	
	public Partitioner clone() {
		Partitioner partitioner = getInstance();
		partitioner.setId( id );
		partitioner.setDescription(description);
		partitioner.setMeta(meta);
		return partitioner;
	}

	public int getNrPartitions() {
		return nrPartitions;
	}

	public void setNrPartitions(int nrPartitions) {
		this.nrPartitions = nrPartitions;
	}

	public void init(RowMetaInterface rowMeta ) throws KettleException
	{

        if( nrPartitions < 0 ) {
        	nrPartitions = meta.getPartitionSchema().getPartitionIDs().size();
        }

	}

	public StepPartitioningMeta getMeta() {
		return meta;
	}

	public void setMeta(StepPartitioningMeta meta) {
		this.meta = meta;
	}
	
	public abstract Partitioner getInstance();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
