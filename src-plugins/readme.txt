Core Kettle Plugin Documentation
--------------------------------

 the following folders are considered core kettle plugins, plugins that are
 distributed with kettles core distribution but are useful to have as plugins for architectural and 
 dependency reasons.
 
 to add a core plugin:
  
  - create a folder under src-plugins with the name of the plugin
  
  - create src, test, lib, and res subfolders for the various files that will be included in your plugin
  
  - add your plugin folder name to the plugins property in build.properties
  
  - if you would like your plugin's jar and zip to get published to artifactory, update the
    build-res/publish.properties with your plugin folder.
    
    An ivy.xml file must be located with in the plugin's root folder.  When creating a new plugin
    the ivy.xml file from an existing plugin can be copied.  No editing is needed.
  
 all core plugins get built as part of the core dist, also you can build the plugins standalone by using 
 the "-standalone" ant targets related to the plugins.  If you'd like to just build a single plugin, 
 you can do that by overriding the plugins property to just reference your plugin.
 
 To have core plugins function in eclipse, you'll need to add the plugin's dependencies to your 
 .classpath file and set the property -DKETTLE_PLUGIN_CLASSES to the full name of your plugin class names.
 
 Here is the current core kettle plugins eclipse flag:
 
-DKETTLE_PLUGIN_CLASSES=org.pentaho.di.trans.steps.gpload.GPLoadMeta,org.pentaho.di.core.database.PALODatabaseMeta,org.pentaho.di.trans.steps.palo.cellinput.PaloCellInputMeta,org.pentaho.di.trans.steps.palo.celloutput.PaloCellOutputMeta,org.pentaho.di.trans.steps.palo.diminput.PaloDimInputMeta,org.pentaho.di.trans.steps.palo.dimoutput.PaloDimOutputMeta,org.pentaho.di.trans.steps.hl7input.HL7InputMeta,org.pentaho.di.job.entries.hl7mllpack.HL7MLLPAcknowledge,org.pentaho.di.job.entries.hl7mllpin.HL7MLLPInput,org.pentaho.di.job.entries.palo.cubecreate.PaloCubeCreate,org.pentaho.di.job.entries.palo.cubedelete.PaloCubeDelete,org.pentaho.di.core.database.OpenERPDatabaseMeta,org.pentaho.di.trans.steps.openerp.objectinput.OpenERPObjectInputMeta,org.pentaho.di.trans.steps.openerp.objectoutput.OpenERPObjectOutputMeta,org.pentaho.di.trans.steps.openerp.objectdelete.OpenERPObjectDeleteMeta