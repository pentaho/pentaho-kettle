/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.namedconfig.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectBase;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.w3c.dom.Node;

@MetaStoreElementType( name = "NamedConfiguration", description = "A NamedConfiguration" )
public class NamedConfiguration extends SharedObjectBase implements Cloneable, XMLInterface, SharedObjectInterface,
  VariableSpace, RepositoryElementInterface {

  public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.NAMEDCONFIG;
  public static final String XML_TAG = "namedconfig";  
  
  private VariableSpace variables = new Variables();
  
  private ObjectRevision objectRevision;
  private ObjectId id;
  
  @MetaStoreAttribute
  private String name;
  @MetaStoreAttribute
  private String type; // something like hadoop cluster, mongo, vfs browser
  @MetaStoreAttribute
  private String subType; // extra field for being more specific, for example, to keep track of which shim
  
  @MetaStoreAttribute
  private List<Group> groups;

  private long lastModifiedDate = System.currentTimeMillis();
  
  private boolean changed;
  
  // Comparator for sorting configurations alphabetically by name
  public static final Comparator<NamedConfiguration> comparator = new Comparator<NamedConfiguration>() {
    @Override
    public int compare( NamedConfiguration c1, NamedConfiguration c2 ) {
      return c1.getName().compareToIgnoreCase( c2.getName() );
    }
  };
  
  public NamedConfiguration() {
    this.groups = new ArrayList<Group>();
    this.changed = false;
    initializeVariablesFrom( null );    
  }

  /**
   * Constructs a new configuration using an XML string snippet. It expects the snippet to be enclosed in
   * <code>namedconfig</code> tags.
   *
   * @param xml
   *          The XML string to parse
   * @throws KettleXMLException
   *           in case there is an XML parsing error
   */
  public NamedConfiguration( String xml ) throws KettleXMLException {
    this( XMLHandler.getSubNode( XMLHandler.loadXMLString( xml ), XML_TAG ) );
  }

  /**
   * Reads the information from an XML Node into this new database connection.
   *
   * @param con
   *          The Node to read the data from
   * @throws KettleXMLException
   */
  public NamedConfiguration( Node conf ) throws KettleXMLException {
    this();

    try {
      setName( XMLHandler.getTagValue( conf, "name" ) );
      setType( XMLHandler.getTagValue( conf, "type" ) );
      setSubType( XMLHandler.getTagValue( conf, "subType" ) );
      String lastModifiedDateStr = XMLHandler.getTagValue( conf, "lastModifiedDate" );
      if ( lastModifiedDateStr != null && !"".equals( lastModifiedDateStr.trim() ) ) {
        lastModifiedDate = Long.parseLong( lastModifiedDateStr );
      }
          
      // read groups
      Node groupsnode = XMLHandler.getSubNode( conf, "groups" );
      if ( groupsnode != null ) {
        List<Node> groupnodes = XMLHandler.getNodes( groupsnode, "group" );
        for ( Node groupnode : groupnodes ) {
          Group group = new Group();
          String groupName = XMLHandler.getTagValue( groupnode, "name" );
          group.setName( groupName );
          Node propertiesnode = XMLHandler.getSubNode( groupnode, "properties" );
          if ( propertiesnode != null ) {
            List<Node> propertyNodes = XMLHandler.getNodes( propertiesnode, "property" );
            for ( Node propertyNode : propertyNodes ) {
              String propertyName = XMLHandler.getTagValue( propertyNode, "propertyName" );
              String displayName = XMLHandler.getTagValue( propertyNode, "displayName" );
              String propertyValue = XMLHandler.getTagValue( propertyNode, "propertyValue" );
              String type = XMLHandler.getTagValue( propertyNode, "type" );
              String uiType = XMLHandler.getTagValue( propertyNode, "uiType" );
              Node defaultValuesnode = XMLHandler.getSubNode( groupnode, "defaultValues" );
              List<String> defaultValues = new ArrayList<String>();
              if ( defaultValuesnode != null ) {
                List<Node> defaultValueNodes = XMLHandler.getNodes( defaultValuesnode, "defaultValue" );
                for ( Node defaultValueNode : defaultValueNodes ) {
                  defaultValues.add( XMLHandler.getTagValue( defaultValueNode, "value" ) );
                }                
              }
              Property property = new Property( propertyName, displayName, propertyValue, uiType, type, defaultValues );
              group.addProperty( property );
            }
          }
          groups.add( group );
        }
      }      
      
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load configuration info from XML node", e );
    }
  }  
  
  public void setProperty( String groupName, String propertyName, String propertyValue ) {
    Group group = null;
    if ( containsGroup( groupName ) ) {
      group = getGroup( groupName );
    } else {
      group = new Group( groupName );
      groups.add( group );
    }
    Property property = new Property( propertyName, propertyValue );
    group.addProperty( property );
  }

  public void deleteProperty( String groupName, String propertyName ) {
    if ( containsGroup( groupName ) ) {
      Group group = getGroup( groupName );
      group.deleteProperty( propertyName );
    }
  }

  public void deleteProperty( String groupName, Property property ) {
    if ( containsGroup( groupName ) ) {
      Group group = getGroup( groupName );
      group.deleteProperty( property );
    }
  }  
  
  public void addProperty( String groupName, Property property ) {
    Group group = null;
    if ( containsGroup( groupName ) ) {
      group = getGroup( groupName );
    } else {
      group = new Group( groupName );
      groups.add( group );
    }
    group.addProperty( property );
  }  
  
  public String getPropertyValue( String groupName, String propertyName ) {
    if ( containsGroup( groupName ) ) {
      Group group = getGroup( groupName );
      return group.getProperty( propertyName ).getPropertyValue();
    }
    return null;
  }
  
  public void setName( String name ) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public long getLastModifiedDate() {
    return lastModifiedDate;
  }  
  
  public void setType( String type ) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }  

  public void setSubType( String subType ) {
    this.subType = subType;
  }
  
  public String getSubType() {
    return subType;
  }    
  
  public Group getGroup( String name ) {
    for ( Group group : groups ) {
      if ( group.getName().equals( name ) ) {
        return group;
      }
    }
    return null;
  }

  public boolean containsGroup( String name ) {
    Group group = getGroup( name );
    return group != null;
  }

  public Group addGroup( String name ) {
    if ( !containsGroup( name ) ) {
      groups.add( new Group( name ) );
    }
    return getGroup( name );
  }

  public void deleteGroup( String name ) {
    if ( containsGroup( name ) ) {
      Group group = getGroup( name );
      groups.remove( group );
    }
  }
  
  public String getDescription() {
    // NOT USED
    return null;
  }

  public void setDescription( String description ) {
    // NOT USED
  }  
  
  public ObjectId getObjectId() {
    return id;
  }

  public void setObjectId( ObjectId id ) {
    this.id = id;
  }  
  
  public ObjectRevision getObjectRevision() {
    return objectRevision;
  }

  public void setObjectRevision( ObjectRevision objectRevision ) {
    this.objectRevision = objectRevision;
  }  
  
  public RepositoryObjectType getRepositoryElementType() {
    return REPOSITORY_ELEMENT_TYPE;
  }  
  
  /**
   * Not used in this case, simply return root /
   */
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return new RepositoryDirectory();
  }

  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory ) {
    throw new RuntimeException( "Setting a directory on a database connection is not supported" );
  }  
  
  public void copyVariablesFrom( VariableSpace space ) {
    variables.copyVariablesFrom( space );
  }

  public String environmentSubstitute( String aString ) {
    return variables.environmentSubstitute( aString );
  }

  public String[] environmentSubstitute( String[] aString ) {
    return variables.environmentSubstitute( aString );
  }

  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData )
    throws KettleValueException {
    return variables.fieldSubstitute( aString, rowMeta, rowData );
  }

  public VariableSpace getParentVariableSpace() {
    return variables.getParentVariableSpace();
  }

  public void setParentVariableSpace( VariableSpace parent ) {
    variables.setParentVariableSpace( parent );
  }

  public String getVariable( String variableName, String defaultValue ) {
    return variables.getVariable( variableName, defaultValue );
  }

  public String getVariable( String variableName ) {
    return variables.getVariable( variableName );
  }

  public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
    if ( !Const.isEmpty( variableName ) ) {
      String value = environmentSubstitute( variableName );
      if ( !Const.isEmpty( value ) ) {
        return ValueMeta.convertStringToBoolean( value );
      }
    }
    return defaultValue;
  }

  public void initializeVariablesFrom( VariableSpace parent ) {
    variables.initializeVariablesFrom( parent );
  }

  public String[] listVariables() {
    return variables.listVariables();
  }

  public void setVariable( String variableName, String variableValue ) {
    variables.setVariable( variableName, variableValue );
  }

  public void shareVariablesWith( VariableSpace space ) {
    variables = space;
  }

  public void injectVariables( Map<String, String> prop ) {
    variables.injectVariables( prop );
  }  
  
  public String getXML() {
    StringBuilder xml = new StringBuilder();
    xml.append( "  <" ).append( XML_TAG ).append( '>' ).append( Const.CR );
    xml.append( "    " ).append( XMLHandler.addTagValue( "name", name, true ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "type", getType(), true ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "subType", getSubType(), true ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "lastModifiedDate", getLastModifiedDate(), true ) );

    // add groups
    xml.append( "    <groups>" ).append( Const.CR );
    for ( Group group : groups ) {
      xml.append( "      <group>").append( Const.CR );
      xml.append( "        " ).append( XMLHandler.addTagValue( "name", group.getName(), true ) );
      // add properties
      xml.append( "        <properties>").append( Const.CR );
      for ( Property property : group.getProperties() ) {
        xml.append( "          <property>").append( Const.CR );
        xml.append( "            ").append( XMLHandler.addTagValue( "propertyName", property.getPropertyName(), true ) );
        xml.append( "            ").append( XMLHandler.addTagValue( "displayName", property.getDisplayName(), true ) );
        xml.append( "            ").append( XMLHandler.addTagValue( "propertyValue", property.getPropertyValue().toString(), true ) );
        if ( property.getType() != null ) {
          xml.append( "            ").append( XMLHandler.addTagValue( "type", property.getType(), true ) );
        }
        if ( property.getUiType() != null ) {
          xml.append( "            ").append( XMLHandler.addTagValue( "uiType", property.getUiType(), true ) );
        }
        xml.append( "            <defaultValues>").append( Const.CR );
        if ( property.getDefaultValues() != null ) {
          for ( Object defaultValue : property.getDefaultValues() ) {
            xml.append( "              ").append( XMLHandler.addTagValue( "defaultValue", defaultValue.toString(), true ) );
          }        
        }
        xml.append( "            </defaultValues>").append( Const.CR );
        xml.append( "          </property>").append( Const.CR );
      }
      xml.append( "        </properties>").append( Const.CR );
      xml.append( "      </group>").append( Const.CR );
    }
    xml.append( "    </groups>" ).append( Const.CR );    
    
    xml.append( "  </" + XML_TAG + ">" ).append( Const.CR );
    return xml.toString();
  }  

  public void setChanged() {
    setChanged( true );
  }

  public void setChanged( boolean ch ) {
    this.changed = ch;
  }

  public boolean hasChanged() {
    return changed;
  }

  public void clearChanged() {
    this.changed = false;
  }  
  
  /**
   * This method detects if this configuration can provide settings for the required set of parameters
   * that are asked for.  
   * 
   * @param required The request is made with a map of String,List<String> where the keys are the required
   * groups for which the corresponding properties belong.
   * 
   * @return true if all of the required properties can be provided
   */
  public boolean canProvide( Map<String, List<String>> required ) {
    if ( required != null ) {
      for ( String groupName : required.keySet() ) {
        // every group must be found
        Group group = getGroup( groupName );
        if ( group == null ) {
          // if we don't have the required group, return false
          return false;
        }
        List<String> propertyList = required.get( groupName );
        if ( propertyList != null ) {
          for ( String property : propertyList ) {
            // every property must be found
            if ( !group.containsProperty( property ) ) {
              // if the group does not contain the required property, return false
              return false;
            }
          }
        }
      }
    }
    // all conditions met, return true
    return true;
  }
  
  /**
   * This method will detect if this configuration is capable of satisfying the configuration
   * requirements of the passed NamedConfiguration.  This method will be typically used to detect
   * if a configuration can provide settings for another configuration.
   * 
   * Useful for checking if a configuration satisfies a template.
   * 
   * @param namedConfiguration
   * @return
   */
  public boolean doesConfigurationSatisfy( NamedConfiguration namedConfiguration ) {
    HashMap<String, List<String>> required = new HashMap<String, List<String>>();
    for ( Group group : namedConfiguration.groups ) {
      ArrayList<String> groupProperties = new ArrayList<String>();
      required.put( group.getName(), groupProperties );
      for ( Property property : group.getProperties() ) {
        groupProperties.add( property.getPropertyName() );
      }
    }
    return canProvide( required );
  }
  
  public List<Group> getGroups() {
    return groups;
  }

  public void replaceMeta( NamedConfiguration config ) {
    this.setName( config.getName() );
    this.setType( config.getType() );
    this.setSubType( config.getSubType() );
    this.setObjectId( config.getObjectId() );
    this.lastModifiedDate = System.currentTimeMillis();
    this.groups = new ArrayList<Group>();
    for ( Group group : config.groups ) {
      Group myGroup = this.addGroup( group.getName() );
      for ( Property property : group.getProperties() ) {
        Property newProperty = new Property( property.getPropertyName(), property.getDisplayName(), property.getPropertyValue(), property.getUiType(), property.getType(), property.getDefaultValues() );
        myGroup.addProperty( newProperty );
      }
    }
  }  
  
  public NamedConfiguration clone() {
    NamedConfiguration config = new NamedConfiguration();
    config.replaceMeta( this );
    return config;
  }  
  
}
