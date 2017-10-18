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

package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.w3c.dom.Node;

/**
 * Defines methods needed for partitioner plugins. The main responsibilities are:
 * <ul>
 * <li><b>Maintain partitioner settings</b><br/>
 * The implementing class typically keeps track of partitioner settings using private fields with corresponding getters
 * and setters. The dialog class implementing StepDialogInterface is using the getters and setters to copy the user
 * supplied configuration in and out of the dialog.
 * <p>
 * The following interface method also falls into the area of maintaining settings:
 * <p>
 * <i><a href="#clone()">public Object clone()</a></i>
 * <p>
 * This method is called when a step containing partitioning configuration is duplicated in Spoon. It needs to return a
 * deep copy of this partitioner object. It is essential that the implementing class creates proper deep copies if the
 * configuration is stored in modifiable objects, such as lists or custom helper objects. The copy is created by first
 * calling super.clone(), and deep-copying any fields the partitioner may have declared.
 * <p>
 * <i><a href="#getInstance()">public Partitioner getInstance()</a></i>
 * <p>
 * This method is required to return a new instance of the partitioner class, with the plugin id and plugin description
 * inherited from the instance this function is called on.</li>
 * <li><b>Serialize partitioner settings</b><br/>
 * The plugin needs to be able to serialize its settings to both XML and a PDI repository. The interface methods are as
 * follows.
 * <p>
 * <i><a href="#getXML()">public String getXML()</a></i>
 * <p>
 * This method is called by PDI whenever the plugin needs to serialize its settings to XML. It is called when saving a
 * transformation in Spoon. The method returns an XML string, containing the serialized settings. The string contains a
 * series of XML tags, typically one tag per setting. The helper class org.pentaho.di.core.xml.XMLHandler is typically
 * used to construct the XML string.
 * <p>
 * <i><a href="#loadXML(org.w3c.dom.Node)">public void loadXML(...)</a></i>
 * <p>
 * This method is called by PDI whenever a plugin needs to read its settings from XML. The XML node containing the
 * plugin's settings is passed in as an argument. Again, the helper class org.pentaho.di.core.xml.XMLHandler is
 * typically used to conveniently read the settings from the XML node.
 * <p>
 * <i><a href=
 * "#saveRep(org.pentaho.di.repository.Repository, org.pentaho.di.repository.ObjectId,
 *    org.pentaho.di.repository.ObjectId)"
 * >public void saveRep(...)</a></i>
 * <p>
 * This method is called by PDI whenever a plugin needs to save its settings to a PDI repository. The repository object
 * passed in as the first argument provides a convenient set of methods for serializing settings. The transformation id
 * and step id passed in should be used as identifiers when calling the repository serialization methods.
 * <p>
 * <i><a href="#loadRep(org.pentaho.di.repository.Repository, org.pentaho.di.repository.ObjectId)">public void
 * loadRep(...)</a></i>
 * <p>
 * This method is called by PDI whenever a plugin needs to read its configuration from a PDI repository. The step id
 * given in the arguments should be used as the identifier when using the repositories serialization methods.</li>
 * <li><b>Provide access to dialog class</b></li>
 * PDI needs to know which class will take care of the settings dialog for the plugin. The interface method must return
 * the name of the class implementing the StepDialogInterface for the partitioner.
 * <p>
 * <i><a href="#getDialogClassName()">public String getDialogClassName()</i></a> </li>
 * <li><b>Partition incoming rows during runtime</b><br/>
 * The class implementing Partitioner executes the actual logic that distributes the rows to available partitions.
 * <p>
 * This method is called with the row structure and the actual row as arguments. It must return the partition this row
 * will be sent to. The total number of partitions is available in the inherited field nrPartitions, and the return
 * value must be between 0 and nrPartitions-1.
 * <p>
 * <i><a href="">public int getPartition(...)</a></i></li>
 * </ul>
 */
public interface Partitioner {

  /**
   * Gets the single instance of Partitioner.
   *
   * @return single instance of Partitioner
   */
  public abstract Partitioner getInstance();

  /**
   * Gets the partition.
   *
   * @param rowMeta
   *          the row meta
   * @param r
   *          the r
   * @return the partition
   * @throws KettleException
   *           the kettle exception
   */
  public int getPartition( RowMetaInterface rowMeta, Object[] r ) throws KettleException;

  /**
   * Sets the meta.
   *
   * @param meta
   *          the new meta
   */
  public void setMeta( StepPartitioningMeta meta );

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId();

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the id.
   *
   * @param id
   *          the new id
   */
  public void setId( String id );

  /**
   * Sets the description.
   *
   * @param description
   *          the new description
   */
  public void setDescription( String description );

  /**
   * Gets the dialog class name.
   *
   * @return the dialog class name
   */
  public String getDialogClassName();

  /**
   * Clone.
   *
   * @return the partitioner
   */
  public Partitioner clone();

  /**
   * Gets the xml.
   *
   * @return the xml
   */
  public String getXML();

  /**
   * Load xml.
   *
   * @param partitioningMethodNode
   *          the partitioning method node
   * @throws KettleXMLException
   *           the kettle xml exception
   */
  public void loadXML( Node partitioningMethodNode ) throws KettleXMLException;

  /**
   * Saves partitioning properties in the repository for the given step.
   *
   * @param rep
   *          the repository to save in
   * @param id_transformation
   *          the ID of the transformation
   * @param id_step
   *          the ID of the step
   * @throws KettleException
   *           In case anything goes wrong
   */
  public void saveRep( Repository rep, ObjectId id_transformation, ObjectId id_step ) throws KettleException;

  /**
   * Load rep.
   *
   * @param rep
   *          the rep
   * @param id_step
   *          the id_step
   * @throws KettleException
   *           the kettle exception
   */
  public void loadRep( Repository rep, ObjectId id_step ) throws KettleException;

}
