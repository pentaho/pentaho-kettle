Core Kettle Plugin Documentation
--------------------------------

 The subfolders under the plugins/ folder are considered core Kettle plugins, plugins that are
 distributed with Kettle's core distribution but are useful to have as plugins for architectural and 
 dependency reasons.
 
 To add a core plugin:
  
  - Create a folder under plugins with the name of the plugin
  
  - Create src/, test/, lib/, and res/ subfolders for the various files that will be included in your plugin
  
  - Add your plugin folder name to the "plugin-ids" property in the "build.properties" file in the root folder
  
  An ivy.xml file must be located with in the plugin's root folder.  When creating a new plugin, the ivy.xml 
  file from an existing plugin can be copied.  In many cases, no editing of the ivy.xml file is needed.
  
 All core plugins get built as part of the core distribution. Alternatively, you can build the plugins standalone 
 by using the "-standalone" ant targets related to the plugins.  If you'd like to just build a single plugin, 
 you can do that by running the default Ant target of the build.xml file located in the desired plugin folder. 
 Alternatively, you can override the plugin-ids property in the root build.properties file to just reference 
 your plugin.
 
 To have core plugins available in Kettle when running from the Eclipse IDE, you'll need to run the 
 "create-dot-classpath" Ant target for the build.xml file in the plugin folder. Also in the kettle.properties 
 file in your (HOME)/.kettle folder, set the property KETTLE_PLUGIN_CLASSES to the fully-qualified name(s) of 
 your plugin class(es).
 
 Here is the current property setting to include all core Kettle plugins:
 
KETTLE_PLUGIN_CLASSES=org.pentaho.di.trans.steps.gpload.GPLoadMeta,org.pentaho.di.core.database.PALODatabaseMeta,org.pentaho.di.trans.steps.palo.cellinput.PaloCellInputMeta,org.pentaho.di.trans.steps.palo.celloutput.PaloCellOutputMeta,org.pentaho.di.trans.steps.palo.diminput.PaloDimInputMeta,org.pentaho.di.trans.steps.palo.dimoutput.PaloDimOutputMeta,org.pentaho.di.trans.steps.hl7input.HL7InputMeta,org.pentaho.di.job.entries.hl7mllpack.HL7MLLPAcknowledge,org.pentaho.di.job.entries.hl7mllpin.HL7MLLPInput,org.pentaho.di.job.entries.palo.JobEntryCubeCreate.PaloCubeCreate,org.pentaho.di.job.entries.palo.JobEntryCubeDelete.PaloCubeDelete,org.pentaho.di.core.database.OpenERPDatabaseMeta,org.pentaho.di.trans.steps.openerp.objectinput.OpenERPObjectInputMeta,org.pentaho.di.trans.steps.openerp.objectoutput.OpenERPObjectOutputMeta,org.pentaho.di.trans.steps.openerp.objectdelete.OpenERPObjectDeleteMeta,org.pentaho.reporting.birt.plugin.BIRTOutputMeta,org.pentaho.di.profiling.datacleaner.SpoonProfilePlugin,org.pentaho.di.starmodeler.StarModelerSpoonPlugin,org.pentaho.di.core.market.Market

