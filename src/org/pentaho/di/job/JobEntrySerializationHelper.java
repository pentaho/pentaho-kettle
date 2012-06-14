/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.job;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entries.sqoop.Password;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JobEntrySerializationHelper implements Serializable {

  private static final String INDENT_STRING = "    ";

  /**
   * This method will perform the work that used to be done by hand in each kettle input meta for: readData(Node node). We handle all primitive types,
   * complex user types, arrays, lists and any number of nested object levels, via recursion of this method.
   * 
   * @param object
   *          The object to be persisted
   * @param node
   *          The node to 'attach' our XML to
   */
  public static void read(Object object, Node node) {
    // get this classes declared fields, public, private, protected, package, everything, but not super
    Field declaredFields[] = getAllDeclaredFields(object.getClass());
    for (Field field : declaredFields) {

      // ignore fields which are final, static or transient
      if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
        continue;
      }

      // if the field is not accessible (private), we'll open it up so we can operate on it
      boolean accessible = field.isAccessible();
      if (!accessible) {
        field.setAccessible(true);
      }
      try {
        // check if we're going to try to read an array
        if (field.getType().isArray()) {
          try {
            // get the node (if available) for the field
            Node fieldNode = XMLHandler.getSubNode(node, field.getName());
            if (fieldNode == null) {
              // doesn't exist (this is possible if fields were empty/null when persisted)
              continue;
            }
            // get the Java classname for the array elements
            String fieldClassName = XMLHandler.getTagAttribute(fieldNode, "class");
            Class clazz = null;
            // primitive types require special handling
            if (fieldClassName.equals("boolean")) {
              clazz = boolean.class;
            } else if (fieldClassName.equals("int")) {
              clazz = int.class;
            } else if (fieldClassName.equals("float")) {
              clazz = float.class;
            } else if (fieldClassName.equals("double")) {
              clazz = double.class;
            } else if (fieldClassName.equals("long")) {
              clazz = long.class;
            } else {
              // normal, non primitive array class
              clazz = Class.forName(fieldClassName);
            }
            // get the child nodes for the field
            NodeList childrenNodes = fieldNode.getChildNodes();

            // create a new, appropriately sized array
            int arrayLength = 0;
            for (int i = 0; i < childrenNodes.getLength(); i++) {
              Node child = childrenNodes.item(i);
              // ignore TEXT_NODE, they'll cause us to have a larger count than reality, even if they are empty
              if (child.getNodeType() != Node.TEXT_NODE) {
                arrayLength++;
              }
            }
            // create a new instance of our array
            Object array = Array.newInstance(clazz, arrayLength);
            // set the new array on the field (on object, passed in)
            field.set(object, array);

            int arrayIndex = 0;
            for (int i = 0; i < childrenNodes.getLength(); i++) {
              Node child = childrenNodes.item(i);
              if (child.getNodeType() == Node.TEXT_NODE) {
                continue;
              }

              // roll through all of our array elements setting them as encountered
              if (String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)) {
                Constructor constructor = clazz.getConstructor(String.class);
                Object instance = constructor.newInstance(XMLHandler.getTagAttribute(child, "value"));
                Array.set(array, arrayIndex++, instance);
              } else if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
                Object value = Boolean.valueOf(XMLHandler.getTagAttribute(child, "value"));
                Array.set(array, arrayIndex++, value);
              } else if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
                Object value = Integer.valueOf(XMLHandler.getTagAttribute(child, "value"));
                Array.set(array, arrayIndex++, value);
              } else if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
                Object value = Float.valueOf(XMLHandler.getTagAttribute(child, "value"));
                Array.set(array, arrayIndex++, value);
              } else if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
                Object value = Double.valueOf(XMLHandler.getTagAttribute(child, "value"));
                Array.set(array, arrayIndex++, value);
              } else if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
                Object value = Long.valueOf(XMLHandler.getTagAttribute(child, "value"));
                Array.set(array, arrayIndex++, value);
              } else {
                // create an instance of 'fieldClassName'
                Object instance = clazz.newInstance();
                // add the instance to the array
                Array.set(array, arrayIndex++, instance);
                // read child, the same way as the parent
                read(instance, child);
              }
            }
          } catch (Throwable t) {
            t.printStackTrace();
            // TODO: log this
          }
        } else if (Collection.class.isAssignableFrom(field.getType())) {
          // handle collections
          try {
            // get the node (if available) for the field
            Node fieldNode = XMLHandler.getSubNode(node, field.getName());
            if (fieldNode == null) {
              // doesn't exist (this is possible if fields were empty/null when persisted)
              continue;
            }
            // get the Java classname for the array elements
            String fieldClassName = XMLHandler.getTagAttribute(fieldNode, "class");
            Class clazz = Class.forName(fieldClassName);

            // create a new, appropriately sized array
            Collection collection = (Collection) field.getType().newInstance();
            field.set(object, collection);

            // iterate over all of the array elements and add them one by one as encountered
            NodeList childrenNodes = fieldNode.getChildNodes();
            for (int i = 0; i < childrenNodes.getLength(); i++) {
              Node child = childrenNodes.item(i);
              if (child.getNodeType() == Node.TEXT_NODE) {
                continue;
              }

              // create an instance of 'fieldClassName'
              if (String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)) {
                Constructor constructor = clazz.getConstructor(String.class);
                Object instance = constructor.newInstance(XMLHandler.getTagAttribute(child, "value"));
                collection.add(instance);
              } else {
                // read child, the same way as the parent
                Object instance = clazz.newInstance();
                // add the instance to the array
                collection.add(instance);
                read(instance, child);
              }
            }
          } catch (Throwable t) {
            t.printStackTrace();
            // TODO: log this
          }
        } else {
          // we're handling a regular field (not an array or list)
          try {
            String value = XMLHandler.getTagValue(node, field.getName());
            if (value == null) {
              continue;
            }

            if (field.isAnnotationPresent(Password.class)) {
              value = Encr.decryptPasswordOptionallyEncrypted(value);
            }

            // System.out.println("Setting " + field.getName() + "(" + field.getType().getSimpleName() + ") = " + value + " on: " + object.getClass().getName());
            if (field.getType().isPrimitive() && "".equals(value)) {
              // skip setting of primitives if we see null
            } else if ("".equals(value)) {
              field.set(object, value);
            } else if (field.getType().isPrimitive()) {
              // special primitive handling
              if (double.class.isAssignableFrom(field.getType())) {
                field.set(object, Double.parseDouble(value));
              } else if (float.class.isAssignableFrom(field.getType())) {
                field.set(object, Float.parseFloat(value));
              } else if (long.class.isAssignableFrom(field.getType())) {
                field.set(object, Long.parseLong(value));
              } else if (int.class.isAssignableFrom(field.getType())) {
                field.set(object, Integer.parseInt(value));
              } else if (byte.class.isAssignableFrom(field.getType())) {
                field.set(object, value.getBytes());
              } else if (boolean.class.isAssignableFrom(field.getType())) {
                field.set(object, "true".equalsIgnoreCase(value));
              }
            } else if (String.class.isAssignableFrom(field.getType()) || Number.class.isAssignableFrom(field.getType()) || Boolean.class.isAssignableFrom(field.getType())) {
              Constructor constructor = field.getType().getConstructor(String.class);
              Object instance = constructor.newInstance(value);
              field.set(object, instance);
            } else {
              // we don't know what we're handling, but we'll give it a shot
              Node fieldNode = XMLHandler.getSubNode(node, field.getName());
              if (fieldNode == null) {
                // doesn't exist (this is possible if fields were empty/null when persisted)
                continue;
              }
              // get the Java classname for the array elements
              String fieldClassName = XMLHandler.getTagAttribute(fieldNode, "class");
              Class clazz = Class.forName(fieldClassName);
              Object instance = clazz.newInstance();
              field.set(object, instance);
              read(instance, fieldNode);
            }
          } catch (Throwable t) {
            // TODO: log this
            t.printStackTrace();
          }
        }
      } finally {
        if (!accessible) {
          field.setAccessible(false);
        }
      }
    }
  }

  /**
   * This method will perform the work that used to be done by hand in each kettle input meta for: getXML(). We handle all primitive types, complex user types,
   * arrays, lists and any number of nested object levels, via recursion of this method.
   * 
   * @param object
   * @param buffer
   */
  public static void write(Object object, int indentLevel, StringBuffer buffer) {

    // don't even attempt to persist
    if (object == null) {
      return;
    }

    // get this classes declared fields, public, private, protected, package, everything, but not super
    Field declaredFields[] = getAllDeclaredFields(object.getClass());
    for (Field field : declaredFields) {

      // ignore fields which are final, static or transient
      if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
        continue;
      }

      // if the field is not accessible (private), we'll open it up so we can operate on it
      boolean accessible = field.isAccessible();
      if (!accessible) {
        field.setAccessible(true);
      }

      try {
        Object fieldValue = field.get(object);
        // no value? null? skip it!
        if (fieldValue == null || "".equals(fieldValue)) {
          continue;
        }

        if (field.isAnnotationPresent(Password.class) && String.class.isAssignableFrom(field.getType())) {
          fieldValue = Encr.encryptPasswordIfNotUsingVariables(String.class.cast(fieldValue));
        }

        if (field.getType().isPrimitive() || String.class.isAssignableFrom(field.getType()) || Number.class.isAssignableFrom(field.getType()) || Boolean.class.isAssignableFrom(field.getType())) {
          indent(buffer, indentLevel);
          buffer.append(XMLHandler.addTagValue(field.getName(), fieldValue.toString()));
        } else if (field.getType().isArray()) {
          // write array values
          int length = Array.getLength(fieldValue);

          // open node (add class name attribute)
          indent(buffer, indentLevel);
          buffer.append("<" + field.getName() + " class=\"" + fieldValue.getClass().getComponentType().getName() + "\">").append(Const.CR);

          for (int i = 0; i < length; i++) {
            Object childObject = Array.get(fieldValue, i);
            // handle all strings/numbers
            if (String.class.isAssignableFrom(childObject.getClass()) || Number.class.isAssignableFrom(childObject.getClass())) {
              indent(buffer, indentLevel + 1);
              buffer.append("<").append(fieldValue.getClass().getComponentType().getSimpleName());
              buffer.append(" value=\"" + childObject.toString() + "\"/>").append(Const.CR);
            } else if (Boolean.class.isAssignableFrom(childObject.getClass()) || boolean.class.isAssignableFrom(childObject.getClass())) {
              // handle booleans (special case)
              indent(buffer, indentLevel + 1);
              buffer.append("<").append(fieldValue.getClass().getComponentType().getSimpleName());
              buffer.append(" value=\"" + childObject.toString() + "\"/>").append(Const.CR);
            } else {
              // array element is a user defined/complex type, recurse into it
              indent(buffer, indentLevel + 1);
              buffer.append("<" + fieldValue.getClass().getComponentType().getSimpleName() + ">").append(Const.CR);
              write(childObject, indentLevel + 1, buffer);
              indent(buffer, indentLevel + 1);
              buffer.append("</" + fieldValue.getClass().getComponentType().getSimpleName() + ">").append(Const.CR);
            }
          }
          // close node
          buffer.append("    </" + field.getName() + ">").append(Const.CR);
        } else if (Collection.class.isAssignableFrom(field.getType())) {
          // write collection values
          Collection collection = (Collection) fieldValue;
          if (collection.size() == 0) {
            continue;
          }
          Class listClass = collection.iterator().next().getClass();

          // open node (add class name attribute)
          indent(buffer, indentLevel);
          buffer.append("<" + field.getName() + " class=\"" + listClass.getName() + "\">").append(Const.CR);

          for (Object childObject : collection) {
            // handle all strings/numbers
            if (String.class.isAssignableFrom(childObject.getClass()) || Number.class.isAssignableFrom(childObject.getClass())) {
              indent(buffer, indentLevel + 1);
              buffer.append("<").append(listClass.getSimpleName());
              buffer.append(" value=\"" + childObject.toString() + "\"/>").append(Const.CR);
            } else if (Boolean.class.isAssignableFrom(childObject.getClass()) || boolean.class.isAssignableFrom(childObject.getClass())) {
              // handle booleans (special case)
              indent(buffer, indentLevel + 1);
              buffer.append("<").append(listClass.getSimpleName());
              buffer.append(" value=\"" + childObject.toString() + "\"/>").append(Const.CR);
            } else {
              // array element is a user defined/complex type, recurse into it
              indent(buffer, indentLevel + 1);
              buffer.append("<" + listClass.getSimpleName() + ">").append(Const.CR);
              write(childObject, indentLevel + 1, buffer);
              indent(buffer, indentLevel + 1);
              buffer.append("</" + listClass.getSimpleName() + ">").append(Const.CR);
            }
          }
          // close node
          indent(buffer, indentLevel);
          buffer.append("</" + field.getName() + ">").append(Const.CR);
        } else {
          // if we don't now what it is, let's treat it like a first class citizen and try to write it out
          // open node (add class name attribute)
          indent(buffer, indentLevel);
          buffer.append("<" + field.getName() + " class=\"" + fieldValue.getClass().getName() + "\">").append(Const.CR);
          write(fieldValue, indentLevel + 1, buffer);
          // close node
          indent(buffer, indentLevel);
          buffer.append("</" + field.getName() + ">").append(Const.CR);
        }
      } catch (Throwable t) {
        t.printStackTrace();
        // TODO: log this
      } finally {
        if (!accessible) {
          field.setAccessible(false);
        }
      }
    }

  }

  /**
   * Get all declared fields of the provided class including any inherited class fields.
   *
   * @param aClass Class to look up fields for
   * @return All declared fields for the class provided
   */
  private static Field[] getAllDeclaredFields(Class<?> aClass) {
    List<Field> fields = new ArrayList<Field>();
    while (aClass != null) {
      fields.addAll(Arrays.asList(aClass.getDeclaredFields()));
      aClass = aClass.getSuperclass();
    }
    return fields.toArray(new Field[0]);
  }

  /**
   * Handle saving of the input (object) to the kettle repository using the most simple method available, by calling
   * write and then saving the xml as an attribute.
   * 
   * @param object
   * @param rep
   * @param id_job
   * @param id_jobentry
   * @throws KettleException
   */
  public static void saveRep(Object object, Repository rep, ObjectId id_job, ObjectId id_jobentry) throws KettleException {
    StringBuffer sb = new StringBuffer(1024);
    sb.append("<job-xml>");
    write(object, 0, sb);
    sb.append("</job-xml>");
    rep.saveJobEntryAttribute(id_job, id_jobentry, "job-xml", sb.toString());
  }

  /**
   * Handle reading of the input (object) from the kettle repository by getting the xml from the repository attribute string
   * and then re-hydrate the object with our already existing read method.
   * 
   * @param object
   * @param rep
   * @param id_job
   * @param databases
   * @param slaveServers
   * @throws KettleException
   */
  public static void loadRep(Object object, Repository rep, ObjectId id_job, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    try {
      String xml = rep.getJobEntryAttributeString(id_job, "job-xml");
      ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
      read(object, doc.getDocumentElement());
    } catch (ParserConfigurationException ex) {
      throw new KettleException(ex.getMessage(), ex);
    } catch (SAXException ex) {
      throw new KettleException(ex.getMessage(), ex);
    } catch (IOException ex) {
      throw new KettleException(ex.getMessage(), ex);
    }
  }

  private static void indent(StringBuffer sb, int indentLevel) {
    for (int i = 0; i < indentLevel; i++) {
      sb.append(INDENT_STRING);
    }
  }

}
