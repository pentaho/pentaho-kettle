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
package org.pentaho.di.resource;

import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;

/**
 * The classes implementing this interface allow their used resources to be exported.
 *  
 * @author Matt
 *
 */
public interface ResourceExportInterface {

	/**
	 * Exports the object to a flat-file system, adding content with filename keys to a set of definitions.
	 * The supplied resource naming interface allows the object to name appropriately without worrying about those parts of the implementation specific details.
	 *  
	 * @param space The variable space to resolve (environment) variables with.
	 * @param definitions The map containing the filenames and content
	 * @param namingInterface The resource naming interface allows the object to name appropriately
	 * @param repository the repository object to load from
	 * 
	 * @return The filename for this object. (also contained in the definitions map)
	 * @throws KettleException in case something goes wrong during the export
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface, Repository repository) throws KettleException;
}
