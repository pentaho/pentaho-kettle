KETTLE MONITORING EXTENSION POINT PLUGIN
========================================

Objective
---------

The goal of this project is to add support for monitoring all relevant events.

Underlying structure is [PDI Extension Point Plugins](http://wiki.pentaho.com/display/EAI/PDI+Extension+Point+Plugins). 


Getting started
---------------

The first thing you should do is to confirm you have ant installed ( http://ant.apache.org/ ).

To prep the project, you first need to resolve/include the necessary dependencies.
Ivy.xml file contains a list of the dependencies needed to successfully compile this project.

### To fetch dependencies and populate /lib folder 

From the project root and using command-line simply type *ant resolve*


How to use
----------

Following this steps should get you going:

### Compile the project

Just run *ant* and you should be all set


### Deploying the plugin in your kettle environment

### A ) Kettle client (standalone application)

Copy the zip folder in ./dist folder and unzip it at 

data-integration/plugins/ 


### B ) Kettle server (a.k.a DI Server)

Copy the zip folder in ./dist folder and unzip it at 

data-integration-server/pentaho-solutions/system/kettle/plugins


Additional information
----------------------

In kettle's monitoring plugin root directory you'll find a properties file 
called 'monitoring.properties'  that will allow you to to some basic log configuration.
