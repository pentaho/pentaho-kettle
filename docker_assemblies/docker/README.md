# How to build an image

```
$ docker build --no-cache -t hiromuhota/webspoon:latest .
```

# Tags

| Tag | 0.8.1.ZZ and lower | 0.8.2.ZZ | 0.8.3.ZZ and higher |
| --- | --- | --- | --- |
| nightly | Latest commit of webSpoon without plugins | Latest commit of webSpoon with plugins | <-- Ditto |
| nightly-full | Latest commit of webSpoon with plugins | Identical to nightly, but deprecated | Discontinued |
| latest | Latest release of webSpoon without plugins | Latest release of webSpoon with plugins | <-- Ditto |
| latest-full | Latest release of webSpoon with plugins | Identical to latest, but deprecated | Discontinued |
| 0.X.Y.ZZ | 0.X.Y.ZZ of webSpoon without plugins | 0.X.Y.ZZ of webSpoon with plugins | <-- Ditto |
| 0.X.Y.ZZ-full | 0.X.Y.ZZ of webSpoon with plugins | Identical to 0.X.Y.ZZ, but deprecated | Discontinued |

# How to use the image

## Basic usage

```
$ docker run -d -p 8080:8080 hiromuhota/webspoon
```

Please access `http://ip-address:8080/spoon/spoon`.

## Advanced usage

### Java heap size

The Java heap size is configured as `-Xms1024m -Xmx2048m` by default, but can be overridden as `-Xms1024m -Xmx1920m` for example when a server has only 2GB of memory.

```
$ docker run -d -p 8080:8080 \
-e JAVA_OPTS="-Xms1024m -Xmx1920m" \
hiromuhota/webspoon
```

### User config and data persistence/share

If the configuration files should be shared among containers, add `-v kettle:/home/tomcat/.kettle -v pentaho:/home/tomcat/.pentaho` as

```
$ docker run -d -p 8080:8080 \
-v kettle:/home/tomcat/.kettle -v pentaho:/home/tomcat/.pentaho \
hiromuhota/webspoon
```

or execute the following docker-compose command

```
$ docker-compose up -d
```

### webSpoon config

From 0.8.0.14, spoon.war is pre-extracted at `$CATALINA_HOME/webapps/spoon` so that configs such as `web.xml` can be configured at run-time using a bind mount.
If you want to enable user authentication, for example, download [web.xml](https://github.com/HiromuHota/pentaho-kettle/blob/webspoon-8.2/assemblies/static/src/main/resources-filtered/WEB-INF/web.xml) and edit it as described [here](https://github.com/HiromuHota/pentaho-kettle#user-authentication).
Then add `-v $(pwd)/web.xml:/usr/local/tomcat/webapps/spoon/WEB-INF/web.xml` to the command.

```
$ docker run -d -p 8080:8080 \
-v $(pwd)/web.xml:/usr/local/tomcat/webapps/spoon/WEB-INF/web.xml \
hiromuhota/webspoon
```

Similarly, `$CATALINA_HOME/webapps/spoon/WEB-INF/spring/security.xml` can be configured at run-time.

### Security manager

To enable the [custom security manager](https://github.com/HiromuHota/pentaho-kettle/wiki/Admin%3A-Security#file-access-control-by-a-custom-security-manager-experimental), enable [user authentication
](https://github.com/HiromuHota/pentaho-kettle/wiki/Admin%3A-Security#user-authentication) and add `-e CATALINA_OPTS="-Djava.security.manager=org.pentaho.di.security.WebSpoonSecurityManager -Djava.security.policy=/usr/local/tomcat/conf/catalina.policy"` to the run command.

```
$ docker run -d -p 8080:8080 \
-e CATALINA_OPTS="-Djava.security.manager=org.pentaho.di.security.WebSpoonSecurityManager -Djava.security.policy=/usr/local/tomcat/conf/catalina.policy" \
-v $(pwd)/web.xml:/usr/local/tomcat/webapps/spoon/WEB-INF/web.xml \
-v $(pwd)/catalina.policy:/usr/local/tomcat/conf/catalina.policy \
hiromuhota/webspoon
```

## Debug

```
$ docker run -d -p 8080:8080 -p 8000:8000 \
-e JPDA_ADDRESS=8000 \
-e CATALINA_OPTS="-Dorg.eclipse.rap.rwt.developmentMode=true" \
hiromuhota/webspoon catalina.sh jpda run
```
