# Change Log

## Unreleased

## 0.9.0.22 - 2020-07-27
### Changed
- [docker] Changed Tomcat from 8.5.X to 9.X as the official Tomcat Docker tag `tomcat:jdk8` refers to `tomcat:9-jdk8`.

### Fixed
- Update RAP/RWT to 3.12.0 to fix #194
- Fixed duplicate folders in Repository Explorer (#199)

## 0.9.0.21 - 2020-02-07
### Changed
- Remove Notify.js (and jQuery) and notify only at console
- Rebased to 9.3.0.0-SNAPSHOT

### Removed
- Do not show "Add Driver" menuitem of HadoopClusterPopupMenuExtension (see README how to add Hadoop drivers)

### Fixed
- Fixed static images and css on Carte (#187)

### Security
- Update RAP/RWT to 3.11.0
- Update Selenium to 3.141.59

## 0.8.3.20 - 2019-11-20
### Added
- Add File/Logout menuitem that allows users to logout when they are login (#175)
- MainToolbar and/or MenuBar can be hidden (#179)

### Changed
- Update Spring to 4.3.22 and Spring Security to 4.2.9
- Update RAP/RWT to 3.9.0

## 0.8.3.19 - 2019-07-19
### Changed
- Rebased to 8.3.0.0-371.

### Fixed
- Partially fix connections/ui (#166)

## 0.8.2.19 - 2019-05-24
### Added
- Add Jenkinsfile

### Changed
- Update Smiley's HTTP Proxy Servlet to 1.11
- Allow encoded slash (%2F) in URL (`-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true`)
- [docker] Merge Dockerfile-base and Dockerfile-full, and rename it as Dockerfile
- allowUrlEncodedSlash at Spring Security
- pentaho-vfs-browser now depends on the official rap distribution
- Use //# for sourceURL instead of deprecated //@
- Separate out Clipboard for extensibility

### Removed
- "Connect Now" button from the repository manager dialog to prevent connectDropdown from becoming defunct
- [docker] Remove Dockerfile-bare

### Fixed
- Fix "Explore" and "Clear Shared Object Cache" remain disabled even after a database repository is connected (HiromuHota/webspoon-docker#20)
- Prevent the parent Composite from getting focused (#145)
- Let Const.getUser() work if cid is available (#147)
- Populate every folder in advance (#146)
- Fix 412 error right after getting connected to a repository (#148)
- Ensure selected step/job entry can be copied/cut (#155)
- Clipboard for TableView (#156)

### Security
- [docker] Change the user from root to tomcat

## 0.8.2.18 - 2019-01-17
### Added
- [docker] Copy the samples dir under ~/.kettle/data for convenience
- `WebSpoonUtils.getUISession()` to get the UISession from any child Thread of UIThread

### Changed
- Ask where to save when saving an imported file (#127)
- Update RAP/RWT to 3.7.0 (#132)
- Set test-id to only widgets in need (#140)
- Move Spoon.setTestId to WebSpoonUtils.setTestId

### Removed
- Trans.get/setDisplay and Job.get/setDisplay

### Fixed
- Fixed the file/folder deletion/creation/rename in a repository (#135)
- Gets the active file name from Spoon to set fileToSave (#134)
- Fixed GetFieldsDialog (#136)
- saveSettings on disposing a display (#137)
- [rap] Use the cursor location as the menu location when it is not set on Menu.setVisible
- Escape the characters in a String for JavaScript (#139)

## 0.8.2.17 - 2018-12-20
### Changed
- Rebased to 8.2.0.0-342.
- Tag naming convention of Docker image (see [here](https://github.com/HiromuHota/webspoon-docker#tags) for details).

### Deprecated
- [docker] "XXX-full" became deprecated (discontinued at 8.3/9.0) and there will be only 0.8.2.17, latest, nightly, etc. No change to previous tags up to 0.8.1.17.

### Fixed
- Ignore corrupted hops (HiromuHota/pdi-git-plugin#22).

## 0.8.1.17 - 2018-11-26
### Added
- Log the current directory path thru JUL (#116)

### Changed
- Update RAP/RWT to 3.6.0
- Update Selenium version to 3.14.0
- Open a help page in a separate tab (#120)
- Export To XML with its original name instead of a random string (#124)
- saveXMLFileToVfs uses meta.getName() instead of "Untitled" (#128)

### Fixed
- Fix popup menus in Filter Rows (fix #112)
- Avoid static modifier for session unique instance (fix #117)
- Use internalization key instead of hardcode (thanks @Valeran86)
- Cancel the traverse event invoked by TAB (+SHIFT) key (fix #123)
- Explicitly set a new menu to a control (fix #122)
- Adjust the dialog height (fix #125)

## 0.8.1.16 - 2018-06-29
### Changed
- Rebased to 8.1.0.0-365

## 0.8.0.16 - 2018-05-17
### Added
- Include WEB-INF/classes/log4j.xml to configure log4j logging framework
- Support the system clipboard
- Restored "Snapshot Canvas"

### Changed
- Let logger "org.pentaho.di" write out to logs/pdi.log
- Log4j writes to spoon.log unless otherwise configured like "org.pentaho.di"
- Change the logging framework for Carte config to Log4j
- Use spring-tx which supersedes spring-dao as of 2.5

### Removed
- Remove apacheds-all to resolve version conflicts of slf4j
- Remove the example ldif file (fix #102)

### Fixed
- [#103] Unintentional triggering of the selection mode

### Security
- vulnerable component spring-security: upgrade from 4.1.3 to 4.1.5 (https://pivotal.io/security/cve-2018-1199)
- Update vulnerable component: Spring Ldap Core (https://pivotal.io/security/cve-2017-8028)
- Update vulnerable component: Spring Framework (https://pivotal.io/security/cve-2018-1275)

## 0.8.0.15 - 2018-03-19
### Added
- Canvas drawing instructions at client-side to improve interactivity
- Support attribute "disabled" to turn menuitems on/off

### Changed
- No drawGrid instructions from the server-side to improve responsiveness
- Disable the capability manager and kettle.properties editor only when they are disabled="true" in menubar.xul

### Fixed
- [#99] Clear the entire canvas before drawing

## 0.8.0.14 - 2018-02-07
### Added
- Add a custom security manager (disabled by default)
- Add jars required for LDAP authentication and example .ldif file

### Changed
- Update RAP/RWT to 3.3.0
- Exclude jetty jars
- Add pentaho-platform-scheduler
- Add jersey-spring
- Add required spring jars for user auth
- [#90] Configure Carte if slave-server-config.xml exists
- Add the patched pentaho-vfs-browser as a dependency
- Secure Carte endpoints by BASIC-Auth
- Move user-service element under authentication-manager/authentication-provider

### Fixed
- [#45] Fixed the popupmenu location (by the patched pentaho-vfs-browser)
- [#85] Fixed "No suitable driver found for Hive2/Impala" error
- [#86] Fixed "NoClassDefFoundError: org/eclipse/jface/window/DefaultToolTip" error
- [#88] Include commons-fileupload (required for "Import from an XML file")
- [#92] Fixed "UI session stops working after 500 (Internal Server Error)"
- [#94] Adding `save as` behaviour for `FileDialog.open()` (Thanks to @Valeran86)
- [HiromuHota/rap#1] Set correct message on finish button in Wizard (Thanks to @Valeran86)

## 0.8.0.13 - 2017-11-17
### Changed
- kettle-engine needs patching
- Move WebSpoonTest.java to integration

## 0.7.1.13 - 2017-10-20

### Changed
- Make repositories-plugin multi-user enabled

### Fixed
- [#58] Cursor position of a script editor is not accurate
- [#75] File > Open... does not open a Kettle file
- [#78] Checkboxes not drawn for the database connection pooling parameters
- [#80] Location is not reflected in the Repository Manager dialog
- Use logError instead of ErrorDialog b/c shell is not yet available in the Spoon constructor

## 0.7.1.12 - 2017-08-16

### Added
- Add some more Selenium UI tests to detect regressions caused by version upgrade

### Changed
- Eable open/save menus even when not connected to a repository
- Firewall / port forward for the OSGI Service (e.g., 9051) is no longer required

### Fixed
- Restored the Repository dialog that was missing when deployed to a url other than localhost
- Upgrade xalan from 2.6.0 to 2.7.2 to resolve the issue that Karaf not starting up when webSpoon deployed in a Docker container

## 0.7.1.11 - 2017-07-14

### Changed
- More multi-user friendly by assigning `.kettle` and `.pentaho/metastore` to each user
- VfsFileChooserDialog for FileDialog
- Move OSGi HTTP service to /osgi (to align with Pentaho Server)
- Move ui/* from WEB-INF/classes/ui/ to the inside of kettle-ui-swt-XXX.jar
- Create webSpoon\_OSS\_Licenses.html for webSpoon-specific OSS
- ShowExitWarning=Y by default and respect the property
- Disable the capability manager and kettle.properties editor when used by multi-user

### Fixed
- Metrics tabs in Test Data Service dialog
- PDI-14492: Copy Table Wizard_UI Issue

## 0.7.1.10 - 2017-06-14

### Changed
- VfsFileChooserDialog for DirectoryDialog
- Use headless Chrome for UI testing
- Disable keyboard shortcut F4 because it is invoked when Enter key is pressed for unknown reason
- Disable keyboard shortcuts under "Edit" menu while a dialog (e.g., step dialog) is focused
- Do not share logs across sessions and do not show the general logs

### Removed
- Remove the Timer for setControlStates() and execute it when trans/job Finished
- Stop using DelayTimer because it does not effectively work in webSpoon

### Fixed
- Make RunConfigurationPopupMenu multi-session enabled
- Fix the "SWTError: BrowserFunction is disposed" when pressing Connect Now
- Refresh the log display periodically
- Restore the ability to connect to a repository by query parameters
- Restore the connectivity to Pentaho Repository and Database Repository
- Catch any exception when executing trans.startThreads() or job.start()
- Move steps/job entries only if a cursor moves

## 0.7.1.9 - 2017-05-23

### Changed
- Rebased to 7.1.0.0-12

## 0.7.0.9 - 2017-05-05

### Added
- Add login page using Spring Security (optional, disabled by default)

### Changed
- Change the build process: ui/ for kettle-ui-swt.jar and assembly/ for spoon.war
- Disable open/save menu items when not connected to a repository
- Disable broken menu items

### Fixed
- Fix disappearing buttons (OK, Preview, Cancel) for Generate Rows, DB Procedure Call, and Add constants
- Make Modified Java Script Value, User Defined Java Class, and Script to be multi-session compatible
- Set location of the context menu properly
- Correct the window icon size of KettleWaitBox and KettleDialog
- Restore Export to XML, Export Linked Resources to XML, and repository export
- Restore Drag&Drop to open a Kettle file
- Restore "Show Arguments" menuitem
- Fix UI redrawing for a trans/job that takes a long time to finish
- Fix the window icon and header of the DB Connection dialog

## 0.7.0.8 - 2017-03-21

### Added
- Start Carte with webSpoon

### Changed
- Rebased to 7.0.0.0-25
- Change url-pattern from `http://address:8080/spoon/` to `http://address:8080/spoon/spoon`
- Build a jar file for kettle-ui-swt
- Refactor build.xml to quickly build a WAR file

### Fixed
- Restore repositories-plugin
- Restore the use of dummyGC in TableView

## 0.6.1.7 - 2017-02-28

### Changed
- Rework: Take parameters in url to open a file
- Updated `pentaho-xul-swt` and `org.eclipse.rap.rwt`

### Fixed
- Restore shortcut keys (may conflict with browser's shortcut keys)
- Restore cut/copy/paste of step/job entry
- Restore png image loading
- Restore Metrics drawing
- Restore welcome page

## 0.6.1.6 - 2017-02-08

### Added
- Automated UI testing using Selenium.

### Changed
- Make Spoon.class a session-unique singleton.
- Remove plugins folder from war file.

### Fixed
- Many bugfixes related to multi-session use.
- Restore scrollbar and proper zooming.
- Restore Get Fields of Fixed Input.
- Fix the partially broken DB connection dialog.
- Make "Open Referenced Object" clickable.

## 0.6.1.5 - 2017-01-10

### Added
- Add exit confirmation.
- Add license notices for third-party libraries.
- Take parameters in url to open a file (experimental).
- Set favicon.

### Changed
- Update dependencies to align w/ the official dist.

### Fixed
- Leverage RAP's "Server Push" to trigger UI update.
- Restore Help - About.
- Restore repository export.
- Restore ConditionEditor in FilterRows.
- Fix the hop creation error for multiple streams.

## 0.6.1.4 - 2016-12-21

### Changed
- Rebase to 6.1.0.1-R (6.1.0.1-196).
- Change versioning (this is the 4th patch applied to 6.1).
- Change how to deploy (need the `system/karaf` folder).

### Fixed
- Restore the ability to launch Apache Karaf.
- Restore the marketplace.
- Restore toolTip and helpTip.
- Fix broken unit tests.

## 0.0.0.3 - 2016-11-22
### Fixed
- Restore the missing menubar.
- Revert the type of menuitem from "checkbox" to "push_button" (a fix has been made to pentaho-xul-swt regarding this).
- Fix the issue #3 "Certain keys cannot be typed" by disabling shortcut keys.

## 0.0.0.2 - 2016-11-09
### Fixed
- Fix the main Shell resizing: now the app area aligns with the browser window size.
- Fix the Job icon drawing, hop creation, and logging.
- Fix the <b>Run</b> button: now it works even when another session starts.

## 0.0.0.1 - 2016-11-01
Open-sourced.