Log4j Web Tracker
=================

**Log4j Web Tracker** is an _open source_ web tool to setup at runtime the
[Apache Log4j](http://logging.apache.org/log4j/) configuration loggers of an application.
It also has a tab that allows read the logs at runtime, or download them.

<img src="https://raw.github.com/mrsarm/log4jwebtracker/master/artwork/log4jwebtracker_config.png" />

It's distributed in a small .jar file _(log4jwebtracker.jar)_, and it must be placed in the
`WEB-INF/lib` folder of the java web application.

To configure it, in the `WEB-INF/web.xml` file you have to add a mapping like this:

```xml
    <servlet>
        <servlet-name>TrackerServlet</servlet-name>
        <servlet-class>log4jwebtracker.servlet.TrackerServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>TrackerServlet</servlet-name>
        <url-pattern>/tracker/*</url-pattern>
    </servlet-mapping>
```

In the example the tool was mapped as `/tracker/*`, so if the application is accessible in
[http://localhost:8080/myapp](http://localhost:8080/myapp), the right URL to access the tracker is:

[http://localhost:8080/myapp/tracker](http://localhost:8080/myapp/tracker)


Introduction
------------

In the first tab you can setup the level of each logger of the application, including the `root` logger.
The configuration are applied at the moment, but not change the original configuration placed
in `log4j.properties`, `log4j.xml` or any configuration file, if you restart the application,
the original configuration will applied again.

<img src="https://raw.github.com/mrsarm/log4jwebtracker/master/artwork/log4jwebtracker_setting.png" />

In the second tab named _**Log**_, you can select an appender file and view the last content in the
webpage (the numbers of line are configurable), or download them.

<img src="https://raw.github.com/mrsarm/log4jwebtracker/master/artwork/log4jwebtracker_log.png" />


Log4j configuration
-------------------

**Log4j** not need any special configuration for use this tool, you can use the usual way to
configure the logging. Many developers used a util class from **Spring** in the web context
to do this (`org.springframework.web.util.Log4jConfigListener`).

Also you can use the automatic setup (the configuration file must be placed on the
`WEB-INF/classes` folder), or the manually basic setup, invoking
`org.apache.log4j.PropertyConfigurator.configure(String configFilename)`.

**Log4jWebTracker** provides a servlet class to do that, but it's optional:

```xml
    <servlet>
            <servlet-name>Log4jInitServlet</servlet-name>
            <servlet-class>log4jwebtracker.servlet.init.Log4jInitServlet</servlet-class>
            <init-param>
                    <param-name>log4jConfigLocation</param-name>
                    <param-value>WEB-INF/classes/log4j.xml</param-value>
            </init-param>
            <load-on-startup>1</load-on-startup>
    </servlet>
```


Requirements
------------

* Java 1.4+
* Servlet container 2.3+ (like Apache Tomcat 4.1 or higher, or WebSphere 5.0+, etc.)


Dependencies
------------
No dependencies with others libraries or frameworks, except of course **Log4j** _(tested only with v1.2)_.


Maven projects
--------------

If your project is building using **Apache Maven 2** or above, put this artifact in your `pom.xml`
to import the jar:

```xml
    <dependency>
        <groupId>log4jwebtracker</groupId>
        <artifactId>log4jwebtracker</artifactId>
        <version>1.0.2</version>
    </dependency>
```

The repository necessary to get the artifact is:

```xml
    <repository>
            <id>log4jwebtracker-releases</id>
            <url>http://repo.log4jwebtracker.com/maven2</url>
    </repository>
```


TO DO
-----

* Securize access (_anyway, can be done with filters, web security using web.xml policies, or others options_).
* Configure appenders and patterns at runtime.
* Filter field with regular expressions to match the output of the log.
* _Send us your proposal..._


License
-------

The project is licensed under the terms of the
[GNU Lesser GPL General Public License version 3](http://www.gnu.org/licenses/lgpl-3.0.html) _(LGPLv3)_.


Binary and Source Code
----------------------

This source code is available in [Github](https://github.com/mrsarm/log4jwebtracker).

Also the source is available in .jar format
[here](http://repo.log4jwebtracker.com/maven2/log4jwebtracker/log4jwebtracker/1.0.2/log4jwebtracker-1.0.2-sources.jar).

The binary version in .jar format, ready to use for your project is
[here](http://repo.log4jwebtracker.com/maven2/log4jwebtracker/log4jwebtracker/1.0.2/log4jwebtracker-1.0.2.jar).


About
-----

The home page of this project is http://www.log4jwebtracker.com

**Author**: My name is **Mariano Ruiz**, I work as _Software & Web Developer_. I'm from Argentina.

**My Home**: http://www.mrdev.com.ar

**Email**: [log4jwebtracker@mrdev.com.ar](mailto:log4jwebtracker@mrdev.com.ar) | [marianoruiz@mrdev.com.ar](mailto:marianoruiz@mrdev.com.ar)
