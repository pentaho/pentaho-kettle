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

package org.pentaho.di.core.plugins;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs.FileObject;
import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.UDJCStep;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.udjcstep.UDJCStepBase;
import org.pentaho.di.trans.steps.udjcstep.UDJCStepDef;
import org.pentaho.di.trans.steps.udjcstep.UDJCStepMetaBase;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UsageParameter;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the UDJC Step plugin type. This plugin type allows for the loading of User Defined Java Class steps
 * directly from an XML file, either a transformation (.ktr) file or any XML file containing a step definition for a
 * User Defined Java Class step. For example, the "Create plugin" button on the UDJC dialog will generate an XML file
 * containing only the UDJC step definition
 * 
 * @author Matt Burgess
 */
@PluginMainClassType(UDJCStepBase.class)
public class UDJCStepPluginType extends StepPluginType implements PluginTypeInterface {
	
	private static UDJCStepPluginType stepPluginType;
	
	private UDJCStepPluginType() {
		super(UDJCStep.class, "UDJCSTEP", "UDJCStep");
		populateFolders("steps");
	}
	
	public static UDJCStepPluginType getInstance() {
		if (stepPluginType==null) {
			stepPluginType=new UDJCStepPluginType();
		}
		return stepPluginType;
	}
	
	/**
	 * Scan & register internal step plugins
	 */
	protected void registerNatives() throws KettlePluginException {
		// No such thing
	}
	
	protected void registerXmlPlugins() throws KettlePluginException {
		for (PluginFolderInterface folder : pluginFolders) {
			
			if (folder.isPluginXmlFolder()) {
				
				List<FileObject> pluginKtrFiles = findStepPluginFiles(folder.getFolder());
				for (FileObject file : pluginKtrFiles) {
					
					try {
						Document document = XMLHandler.loadXMLFile(file);
						Node transNode = XMLHandler.getSubNode(document, "transformation");
						
						// Use transformation's steps if present, otherwise assume a list of <step> tags
						Node startNode = (transNode!=null) ? transNode : document;
						
						int numSteps = XMLHandler.countNodes(startNode, "step");
						for(int stepIndex=0; stepIndex<numSteps; stepIndex++) {
						
							Node stepNode = XMLHandler.getSubNodeByNr(startNode, "step", stepIndex);
							Node udjcNode = XMLHandler.getSubNode(stepNode, "type");
							if(udjcNode != null && udjcNode.getTextContent().trim().equals("UserDefinedJavaClass")) {
								registerUDJCPluginFromXmlResource(stepNode, KettleVFS.getFilename(file.getParent()), this.getClass(), false, file.getParent().getURL());
							}
						}
					} catch(Exception e) {
						// We want to report this plugin.xml error, perhaps an XML typo or something like that...
						//
						log.logError("Error found while reading step plugin.xml file: "+file.getName().toString(), e);
					}
				}
			}
		}
	}
	
	protected PluginInterface registerUDJCPluginFromXmlResource( Node pluginNode, String path, Class<? extends PluginTypeInterface> pluginType, boolean nativePlugin, URL pluginFolder) throws KettlePluginException {
        try
        {
        	String name = getTagContent(pluginNode, "name"); //$NON-NLS-1$ 
        	String id = name.replace(" ", "");
            String description = getTagContent(pluginNode, "description"); //$NON-NLS-1$
            //String baseDir = path.substring(0, path.indexOf(File.separator+"plugins"));
            String endDir = path.substring(path.indexOf(Const.FILE_SEPARATOR+"plugins"));
            String[] paths = endDir.split(StringEscapeUtils.escapeJava(Const.FILE_SEPARATOR));
            int numLevelsDeep = paths.length-1;
            StringBuffer iconfileBuf = new StringBuffer(StringUtils.repeat("../",numLevelsDeep));
            iconfileBuf.append("ui/images/sScript.png");
            String iconfile = iconfileBuf.toString().replace("/", File.separator);
            String tooltip = description;
            String category = "Scripting";
            String errorHelpfile = getTagOrAttribute(pluginNode, "errorhelpfile"); //$NON-NLS-1$
            String documentationUrl = getTagOrAttribute(pluginNode, "documentation_url"); //$NON-NLS-1$
            String casesUrl = getTagOrAttribute(pluginNode, "cases_url"); //$NON-NLS-1$
            String forumUrl = getTagOrAttribute(pluginNode, "forum_url"); //$NON-NLS-1$
            
            // For convenience, use a UserDefinedJavaClassMeta to parse the DOM
            UserDefinedJavaClassMeta udjcm = new UserDefinedJavaClassMeta();
            udjcm.loadXML(pluginNode, null, null);
            Class<? extends UDJCStepBase> udjcStepClass = cookClasses(udjcm);
            if(udjcStepClass == null) throw new KettlePluginException(BaseMessages.getString(PKG, "PluginRegistry.RuntimeError.NoValidClassRequested.PLUGINREGISTRY002",udjcm.getName()));
            
            Node libsnode = XMLHandler.getSubNode(pluginNode, "libraries"); //$NON-NLS-1$
            int nrlibs = XMLHandler.countNodes(libsnode, "library"); //$NON-NLS-1$

            List<String> jarFiles = new ArrayList<String>();
            if( path != null ) {
                for (int j = 0; j < nrlibs; j++)
                {
                    Node libnode = XMLHandler.getSubNodeByNr(libsnode, "library", j); //$NON-NLS-1$
                    String jarfile = XMLHandler.getTagAttribute(libnode, "name"); //$NON-NLS-1$
                    jarFiles.add( new File(path + Const.FILE_SEPARATOR + jarfile).getAbsolutePath() );
                }
            }
            
            // Create and cook the meta class
            String newClassName = createClassNameFromStepName(udjcStepClass.getName());
            // TODO 
            if(newClassName.equals("UserDefinedClass")) {
            	newClassName = "NewUdjc";
            }
            
            // Create, cook, and load meta class
            Class<? extends UDJCStepMetaBase> udjcMetaClass = createMetaClass(udjcStepClass.getClassLoader(), udjcm, newClassName);
            
            // Localized categories, descriptions and tool tips
            //
            Map<String, String> localizedCategories = readPluginLocale(pluginNode, "localized_category", "category");
            category = getAlternativeTranslation(category, localizedCategories);
            
            //Map<String, String> localizedDescriptions = readPluginLocale(pluginNode, "localized_description", "description");
            //description = getAlternativeTranslation(description, localizedDescriptions);
            description = newClassName;
            id=newClassName;
            
            Map<String, String> localizedTooltips = readPluginLocale(pluginNode, "localized_tooltip", "tooltip");
            tooltip = getAlternativeTranslation(tooltip, localizedTooltips);
            
            if(tooltip == null) tooltip = description;
            
            String iconFilename = (path == null) ? iconfile : path + Const.FILE_SEPARATOR + iconfile;
            String errorHelpFileFull = errorHelpfile;
            if (!Const.isEmpty(errorHelpfile)) errorHelpFileFull = (path == null) ? errorHelpfile : path+Const.FILE_SEPARATOR+errorHelpfile;
            
            Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
            
            PluginMainClassType mainClassTypesAnnotation = pluginType.getAnnotation(PluginMainClassType.class);
            classMap.put(mainClassTypesAnnotation.value(), udjcMetaClass.getName());
            
            // process annotated extra types
            PluginExtraClassTypes classTypesAnnotation = pluginType.getAnnotation(PluginExtraClassTypes.class);
            if(classTypesAnnotation != null){
              for(int i=0; i< classTypesAnnotation.classTypes().length; i++){
                Class<?> classType = classTypesAnnotation.classTypes()[i];
                String className = getTagOrAttribute(pluginNode, classTypesAnnotation.xmlNodeNames()[i]); //$NON-NLS-1$
                
                classMap.put(classType, className);
              }
            }
            
            // process extra types added at runtime
            Map<Class<?>, String> objectMap = getAdditionalRuntimeObjectTypes();
            for(Map.Entry<Class<?>, String> entry : objectMap.entrySet()){
              String clzName = getTagOrAttribute(pluginNode, entry.getValue()); //$NON-NLS-1$
              classMap.put(entry.getKey(), clzName); 
            }
            
            PluginInterface pluginInterface = new Plugin(id.split(","), pluginType, mainClassTypesAnnotation.value(), category, description, description /*tooltip*/, iconFilename, false, nativePlugin, classMap, jarFiles, errorHelpFileFull, pluginFolder, documentationUrl, casesUrl, forumUrl);
            registry.registerPlugin(pluginType, pluginInterface);
            URLClassLoader ucl = new URLClassLoader(new URL[] {}, udjcMetaClass.getClassLoader());
            registry.addClassLoader(ucl, pluginInterface);
            
            
            return pluginInterface;
        }
        catch (Throwable e)
        {
            throw new KettlePluginException( BaseMessages.getString(PKG, "BasePluginType.RuntimeError.UnableToReadPluginXML.PLUGIN0001"), e); //$NON-NLS-1$
        }
    }
	
	private final String[] searchStrings = {" ", "-"};
	private final String[] replaceStrings = {"", "_"};
	private String createClassNameFromStepName(String name) {
		
		return StringUtils.replaceEach(name, searchStrings, replaceStrings);
	}

	protected String getTagContent(Node n, String tag) {
		if(n != null && tag != null) {
			return XMLHandler.getSubNode(n, tag).getTextContent();
		}
		return null;
	}
	
	protected List<FileObject> findStepPluginFiles(String folder) {
		
		return findPluginFiles(folder,".*\\.(ktr|step\\.xml)$");
	}

  @Override
  protected String extractCategory(Annotation annotation) {
    return ((UDJCStep) annotation).categoryDescription();
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return ((UDJCStep) annotation).description();
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((UDJCStep) annotation).id();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((UDJCStep) annotation).name();
  }
  
  @Override
  protected String extractImageFile(Annotation annotation) {
    return ((UDJCStep) annotation).image();
  }
  
  @Override
  protected boolean extractSeparateClassLoader(Annotation annotation) {
    return ((UDJCStep) annotation).isSeparateClassLoaderNeeded();
  }

  @Override
  protected String extractI18nPackageName(Annotation annotation) {
    return ((UDJCStep) annotation).i18nPackageName();
  }

  @Override
  protected void addExtraClasses(Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation) {	  
  }
  
  @SuppressWarnings("unchecked")
  public Class<UDJCStepBase> cookClasses(UserDefinedJavaClassMeta udjcm)
  {
      //cookErrors.clear();
	  for (UserDefinedJavaClassDef olddef : udjcm.getDefinitions())
      {
		  UDJCStepDef def = UDJCStepDef.fromUserDefinedJavaClassDef(olddef);
          if (def.isActive())
          {
              try
              {
                  Class<?> cookedClass = cookClass(def);
                  if (def.isTransformClass())
                  {
                      return (Class<UDJCStepBase>)cookedClass;
                  }
              }
              catch (Exception e)
              {
                  CompileException exception = new CompileException(e.getMessage(), null);
                  exception.setStackTrace(new StackTraceElement[] {});
                  //cookErrors.add(exception);
              }
          }
      }
      //changed=false;
      return null;
  }
  
  @SuppressWarnings("unchecked")
  private Class<? extends UDJCStepBase> cookClass(UDJCStepDef def) throws CompileException, ParseException, ScanException, IOException, RuntimeException, KettleStepException {
  	
      ClassBodyEvaluator cbe = new ClassBodyEvaluator();
      cbe.setClassName(def.getClassName());

      StringReader sr;
      if (def.isTransformClass())
      {
          cbe.setExtendedType(UDJCStepBase.class);
          sr = new StringReader(def.getTransformedSource());
      }
      else
      {
          sr = new StringReader(def.getSource());
      }

      cbe.setDefaultImports(new String[] {
    		  "org.pentaho.di.trans.*",
              "org.pentaho.di.trans.steps.userdefinedjavaclass.*",
              "org.pentaho.di.trans.steps.udjcstep.*",
              "org.pentaho.di.trans.step.*",
              "org.pentaho.di.core.row.*",
              "org.pentaho.di.core.*",
              "org.pentaho.di.core.exception.*"
      });
      cbe.cook(new Scanner(null, sr));

      return cbe.getClazz();
  }
  
  @SuppressWarnings("unchecked")
  private Class<? extends UDJCStepMetaBase> createMetaClass(ClassLoader parentClassLoader, UserDefinedJavaClassMeta udjcm, String stepClassname) throws CompileException, ParseException, ScanException, IOException, RuntimeException {
	  	
	// Create meta child class source
      String metaClassName = stepClassname + "Meta";
      StringBuffer sourceBuffer = new StringBuffer(String.format("public %s() {\n\tsuper();\n",metaClassName));
      // Add usage parameters
      sourceBuffer.append("\tUsageParameter usageParameter;\n\n");
      for(UsageParameter up : udjcm.getUsageParameters()) {
      	sourceBuffer.append("\tusageParameter = new UsageParameter();\n");
      	sourceBuffer.append(String.format("\tusageParameter.tag = \"%s\";\n",up.tag));
      	sourceBuffer.append(String.format("\tusageParameter.value = \"%s\";\n",up.value));
      	sourceBuffer.append(String.format("\tusageParameter.description = \"%s\";\n",up.description));
      	sourceBuffer.append("\tusageParameters.add(usageParameter);\n\n");
      }
      
      // Add definitions
      for(UserDefinedJavaClassDef def : udjcm.getDefinitions()) {
    	  sourceBuffer.append(String.format("\tdefinitions.add(new UDJCStepDef(UDJCStepDef.ClassType.%s, \"%s\",\"%s\"));\n", def.getClassType(), def.getClassName(), StringEscapeUtils.escapeJava(def.getSource())));
      }
      
      // Fields are being added by the data.parameter map, don't duplicate here
      /*for(org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta.FieldInfo fi : udjcm.getFieldInfo()) {
      	sourceBuffer.append(String.format("\tfields.add(new FieldInfo(\"%s\", %d, %d, %d));\n",fi.name, fi.type, fi.length, fi.precision));
      }*/
      
      // Add info steps
      for(org.pentaho.di.trans.steps.userdefinedjavaclass.StepDefinition sd: udjcm.getInfoStepDefinitions()) {
    	  sourceBuffer.append(String.format("\tinfoStepDefinitions.add(new StepDefinition(\"%s\", \"%s\", %s, \"%s\"));\n",sd.tag, sd.stepName,"null",sd.description));
      }
      
      // Add target steps
      for(org.pentaho.di.trans.steps.userdefinedjavaclass.StepDefinition sd: udjcm.getTargetStepDefinitions()) {
    	  sourceBuffer.append(String.format("\ttargetStepDefinitions.add(new StepDefinition(\"%s\", \"%s\", %s, \"%s\"));\n",sd.tag, sd.stepName,"null",sd.description));
      }
      
      sourceBuffer.append("}\n");
      sourceBuffer.append("public StepInterface getStep(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) {\n");
      sourceBuffer.append(String.format("return new %s(stepMeta,stepDataInterface,copyNr,transMeta,trans);\n}\n",stepClassname));
      sourceBuffer.append("public StepDataInterface getStepData() { return new UserDefinedJavaClassData(); }\n");
      
      // Add getFields method
      sourceBuffer.append("public void getFields(RowMetaInterface row, String originStepname, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {\n");
      sourceBuffer.append("\ttry {\n");
      sourceBuffer.append(String.format("\t\tClass clazz = %s.class;\n",stepClassname));
      sourceBuffer.append("\t\tMethod getFieldsMethod = clazz.getMethod(\"getFields\", new Class[] {boolean.class, RowMetaInterface.class, String.class, RowMetaInterface[].class, StepMeta.class, VariableSpace.class, List.class});\n");
      sourceBuffer.append("\t\tgetFieldsMethod.invoke(null, new Object[] {isClearingResultFields(), row, originStepname, info, nextStep, space, getFieldInfo()});\n");
      sourceBuffer.append("\t} catch (Exception e) {\n");
      sourceBuffer.append(String.format("\t\tthrow new KettleStepException(\"Error executing %s.getFields(): \", e);\n",stepClassname));
      sourceBuffer.append("\t}\n}");            
      
      String source = sourceBuffer.toString();
      
      ClassBodyEvaluator cbe = new ClassBodyEvaluator();
      cbe.setParentClassLoader(parentClassLoader);
      cbe.setClassName(metaClassName);

      StringReader sr;
      cbe.setExtendedType(UDJCStepMetaBase.class);
      sr = new StringReader(source);
      
      cbe.setDefaultImports(new String[] {
    		  "java.lang.reflect.*",
    		  "java.util.*",
    		  "org.pentaho.di.trans.*",
              "org.pentaho.di.trans.steps.userdefinedjavaclass.*",
              "org.pentaho.di.trans.steps.udjcstep.*",
              "org.pentaho.di.trans.step.*",
              "org.pentaho.di.core.row.*",
              "org.pentaho.di.core.*",
              "org.pentaho.di.core.exception.*",
              "org.pentaho.di.core.variables.VariableSpace",
              stepClassname
      });
      cbe.cook(new Scanner(null, sr));

      return cbe.getClazz();
  }
}
