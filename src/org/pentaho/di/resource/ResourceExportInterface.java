package org.pentaho.di.resource;

import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;

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
	 * @param resourceNamingInterface The resource naming interface allows the object to name appropriately
	 * @return The filename for this object. (also contained in the definitions map)
	 * @throws KettleException in case something goes wrong during the export
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface) throws KettleException;
}
