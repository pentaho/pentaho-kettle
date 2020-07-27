# Web-based Spoon (aka webSpoon)

webSpoon is a web-based graphical designer for Pentaho Data Integration with the same look & feel as Spoon.
Kettle transformation/job files can be designed and executed in your favorite web browser.
This is one of the community activities and not supported by Pentaho.

## Use cases

- Data security
- Remote use
- Ease of management
- Cloud

Please see [public talks](https://www.slideshare.net/HiromuHota/presentations) for more details.

# How to use

webSpoon is available at `http://localhost:8080/spoon/spoon`.
Depending on how it is deployed, the IP address and port number might be different.
Please refer to the [wiki](https://github.com/HiromuHota/pentaho-kettle/wiki) and [issues](https://github.com/HiromuHota/pentaho-kettle/issues) for how to use it.

Carte is available at `http://localhost:8080/spoon/kettle`.
Refer to [Carte APIs](https://help.pentaho.com/Documentation/9.0/Developer_center/REST_API_Reference/Carte) for how to use it.

# How to deploy

There are two ways: with Docker and without Docker.
Docker is recommended because it is simple hence error-free.

## With Docker (recommended)

```
$ docker run -d -p 8080:8080 hiromuhota/webspoon
```

## Without Docker

Please refer to the [wiki](https://github.com/HiromuHota/pentaho-kettle/wiki/System-Requirements) for system requirements.

1. Unzip `pdi-ce-9.0.0.0-423.zip`, then copy `system` and `plugins` folders to `$CATALINA_HOME`.
2. Run install.sh at `$CATALINA_HOME`.
3. Allow encoded slash (`org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true`).
3. (Re)start the Tomcat.

The actual commands look like below:

```bash
$ export version=0.9.0.22
$ export dist=9.0.0.0-423
$ export CATALINA_HOME=/home/vagrant/apache-tomcat-8.5.23

$ cd ~/
$ unzip ~/Downloads/pdi-ce-$dist.zip
$ cd $CATALINA_HOME
$ cp -r ~/data-integration/system ./
$ cp -r ~/data-integration/plugins ./

# see [1] below
$ wget https://raw.githubusercontent.com/HiromuHota/pentaho-kettle/$version/docker/install.sh
$ chmod +x install.sh
$ ./install.sh

# see [2] below
$ export CATALINA_OPTS="-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true"
$ ./bin/startup.sh
```

[1]: 0.9.0.21 and before, use `https://raw.githubusercontent.com/HiromuHota/webspoon-docker/$version/install.sh` instead.

[2]: Instead of exporting `CATALINA_OPTS` like above, `org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true` can be added to `conf/catalina.properties`.

# How to config (optional)

## Carte

If `$CATALINA_HOME/system/kettle/slave-server-config.xml` exists, the embedded Carte servlet can be configured accordingly.
See [here](https://wiki.pentaho.com/display/EAI/Carte+Configuration) for the config format.
An example config xml looks like this:

```xml
<slave_config>
  <max_log_lines>10000</max_log_lines>
  <max_log_timeout_minutes>2880</max_log_timeout_minutes>
  <object_timeout_minutes>240</object_timeout_minutes>
  <repository>
    <name>test</name>
    <username>username</username>
    <password>password</password>
  </repository>
</slave_config>
```

Note that only repositories defined in `$HOME/.kettle/repositories.xml` can be used.
Also note that it is NOT recommended to define `repository` element in the XML when the following user authentication is enabled.
Issues such as [#91](https://github.com/HiromuHota/pentaho-kettle/issues/91) can happen.

## Security

See [here](https://github.com/HiromuHota/pentaho-kettle/wiki/Admin%3A-Security) for HTTPS, user authentication/authorization, steps and job entries restriction.

## Third-party plugins, JDBC drivers, and Hadoop drivers

Put
third-party plugins in `$CATALINA_HOME/plugins`,
JDBC drivers in `$CATALINA_HOME/lib`,
Hadoop drivers in `$CATALINA_HOME/system/karaf/deploy`
as below:

```
$CATALINA_HOME
├── system
│   └── karaf
|       └── deploy
|           └── YourDriver.kar
├── plugins
│   ├── YourPlugin
│   │   └── YourPlugin.jar
│   ├── ...
├── lib
│   ├── YourJDBC.jar
│   ├── ...
├── webapps
│   ├── spoon
│   ├── ...
```

## Customize UI component

See [here](https://github.com/HiromuHota/pentaho-kettle/wiki/Admin%3A-Custom-UI).

# How to develop

Spoon relies on SWT for UI widgets, which is great for being OS agnostic, but it only runs as a desktop app.
RAP/RWT provides web UIs with SWT API, so replacing SWT with RAP/RWT allows Spoon to run as a web app with a little code change.
Having said that, some APIs are not implemented; hence, a little more code change is required than it sounds.

## Design philosophy

1. Minimize the difference from the original Spoon.
2. Optimize webSpoon as a web application.

## Branches and Versioning

I started this project in the webspoon branch, branched off from the branch 6.1 of between 6.1.0.5-R and 6.1.0.6-R.
Soon I realized that I should have branched off from one of released versions.
So I decided to make two branches: webspoon-6.1 and webspoon-7.0, each of which was rebased onto 6.1.0.1-R and 7.0.0.0-R, respectively.

webSpoon uses 4 digits versioning with the following rules:

- The 1st digit is always 0 (never be released as a separate software).
- The 2nd and 3rd digits represent the base Kettle version, e.g., 6.1, 7.0.
- The last digit represents the patch version.

As a result, the next (pre-)release version will be 0.6.1.4, meaning it is based on the Kettle version 6.1 with the 4th patch.
There could be a version of 0.7.0.4, which is based on the Kettle version 7.0 with (basically) the same patch.

## Build and locally publish dependent libraries

Please build and locally-publish the following dependent libraries.

- pentaho-xul-swt
- org.eclipse.rap.rwt
- org.eclipse.rap.jface
- org.eclipse.rap.fileupload
- org.eclipse.rap.filedialog
- org.eclipse.rap.rwt.testfixture
- pentaho-vfs-browser

### pentaho-xul-swt

```bash
$ git clone -b webspoon-9.0 https://github.com/HiromuHota/pentaho-commons-xul.git
$ cd pentaho-commons-xul
$ mvn clean install -pl swt
```

### rap

```bash
$ git clone -b webspoon-3.12.0 https://github.com/HiromuHota/rap.git
$ cd rap
$ mvn clean install
```

### pentaho-vfs-browser

```bash
$ git clone -b webspoon-9.0 https://github.com/HiromuHota/apache-vfs-browser.git
$ cd apache-vfs-browser
$ mvn clean install
```

## Build webSpoon

**Make sure patched dependent libraries have been published locally**

```bash
$ git clone -b webspoon-9.0 https://github.com/HiromuHota/pentaho-kettle.git
$ cd pentaho-kettle
$ mvn clean install
```

Optionally you can specify `-Dmaven.test.skip=true` (or `-DskipTests`) to skip the tests

## UI testing using Selenium

Currently, only Google Chrome browser has been tested for when running UI test cases.
The tests run in headless mode unless a parameter `-Dheadless.unittest=false` is passed.
To run tests in headless mode, the version of Chrome should be higher than 59.

The default url is `http://localhost:8080/spoon`.
Pass a parameter like below if webSpoon is deployed to a different url.

The following command runs all the unit test cases including UI in non-headless mode.

```bash
$ cd integration
$ mvn clean test -Dtest.baseurl=http://localhost:8080/spoon -Dheadless.unittest=false
```

# Notices

- Pentaho is a registered trademark of Hitachi Vantara Corporation.
- Google Chrome browser is a trademark of Google Inc.
- Other company and product names mentioned in this document may be the trademarks of their respective owners.
