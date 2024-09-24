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

package org.pentaho.di.job.entries.dtdvalidator;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'dtdvalidator' job entry.
 * 
 * @author Samatar Hassan
 * @since 30-04-2007
 * 
 */
@JobEntry( id = "DTD_VALIDATOR", i18nPackageName = "org.pentaho.di.job.entries.dtdvalidator", image = "DTD.svg",
    name = "DTD_VALIDATOR.Name", description = "DTD_VALIDATOR.Description",
    categoryDescription = "DTD_VALIDATOR.Category",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/DTD+Validator+%28Job+Entry%29" )
public class JobEntryDTDValidator extends JobEntryBase implements Cloneable, JobEntryInterface {
  private String xmlfilename;
  private String dtdfilename;
  private boolean dtdintern;

  public JobEntryDTDValidator( String n ) {
    super( n, "" );
    xmlfilename = null;
    dtdfilename = null;
    dtdintern = false;
  }

  public JobEntryDTDValidator() {
    this( "" );
  }

  public Object clone() {
    JobEntryDTDValidator je = (JobEntryDTDValidator) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 50 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "xmlfilename", xmlfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "dtdfilename", dtdfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "dtdintern", dtdintern ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {

    try {
      super.loadXML( entrynode, databases, slaveServers );
      xmlfilename = XMLHandler.getTagValue( entrynode, "xmlfilename" );
      dtdfilename = XMLHandler.getTagValue( entrynode, "dtdfilename" );
      dtdintern = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "dtdintern" ) );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'DTDvalidator' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers ) throws KettleException {
    try {
      xmlfilename = rep.getJobEntryAttributeString( id_jobentry, "xmlfilename" );
      dtdfilename = rep.getJobEntryAttributeString( id_jobentry, "dtdfilename" );
      dtdintern = rep.getJobEntryAttributeBoolean( id_jobentry, "dtdintern" );

    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'DTDvalidator' from the repository for id_jobentry="
          + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "xmlfilename", xmlfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "dtdfilename", dtdfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "dtdintern", dtdintern );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'DTDvalidator' to the repository for id_job="
          + id_job, dbe );
    }
  }

  public String getRealxmlfilename() {
    return environmentSubstitute( xmlfilename );
  }

  public String getRealDTDfilename() {
    return environmentSubstitute( dtdfilename );
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( true );

    String realxmlfilename = getRealxmlfilename();
    String realDTDfilename = getRealDTDfilename();

    // Define a new DTD validator instance
    DTDValidator validator = new DTDValidator( log );
    // Set XML filename
    validator.setXMLFilename( realxmlfilename );
    if ( dtdintern ) {
      // The DTD is intern to XML file
      validator.setInternDTD( true );
    } else {
      // The DTD is extern
      // set the DTD filename
      validator.setDTDFilename( realDTDfilename );
    }
    // Validate the XML file and return the status
    boolean status = validator.validate();
    if ( !status ) {
      // The XML file is invalid!
      log.logError( validator.getErrorMessage() );
      result.setResult( false );
      result.setNrErrors( validator.getNrErrors() );
      result.setLogText( validator.getErrorMessage() );
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public void setxmlFilename( String filename ) {
    this.xmlfilename = filename;
  }

  public String getxmlFilename() {
    return xmlfilename;
  }

  public void setdtdFilename( String filename ) {
    this.dtdfilename = filename;
  }

  public String getdtdFilename() {
    return dtdfilename;
  }

  public boolean getDTDIntern() {
    return dtdintern;
  }

  public void setDTDIntern( boolean dtdinternin ) {
    this.dtdintern = dtdinternin;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( ( !Utils.isEmpty( dtdfilename ) ) && ( !Utils.isEmpty( xmlfilename ) ) ) {
      String realXmlFileName = jobMeta.environmentSubstitute( xmlfilename );
      String realXsdFileName = jobMeta.environmentSubstitute( dtdfilename );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realXmlFileName, ResourceType.FILE ) );
      reference.getEntries().add( new ResourceEntry( realXsdFileName, ResourceType.FILE ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace( ctx, getVariables() );
    putValidators( ctx, notBlankValidator(), fileExistsValidator() );
    andValidator().validate( this, "dtdfilename", remarks, ctx );
    andValidator().validate( this, "xmlFilename", remarks, ctx );
  }
}
