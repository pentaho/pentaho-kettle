package org.pentaho.di.core.logging;

public enum LoggingObjectType {
	// Runtime...
	//
	TRANS, STEP, JOB, JOBENTRY, DATABASE,
	
	// Metadata...
	//
	TRANSMETA, STEPMETA, JOBMETA,
	
	// User Interface...
	//
	SPOON, STEPDIALOG, JOBENTRYDIALOG,
	
	// Web server + HttpServlet...
	//
	CARTE, SERVLET,
	
	// Repository
	//
	REPOSITORY,
	
	// General
	//
	GENERAL,
}

