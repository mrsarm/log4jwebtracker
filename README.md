Log4j Web Tracker
=================

**Log4j Web Tracker** is a _open source_ web tool to setup at runtime the [Apache Log4j](http://logging.apache.org/log4j/) configuration of the application loggers. Also have  a tab that allows read the log at runtime, or download them.

<img src="https://raw.github.com/mrsarm/log4jwebtracker/master/artwork/log4jwebtracker_config.png" />

Is distributed in a small .jar file _(log4jwebtracker.jar)_, which must be placed in the `WEB-INF/lib` folder of the java web application.

To configure it, in the `WEB-INF/web.xml` file you must add a mapping like this:

        <servlet>
            <servlet-name>TrackerServlet</servlet-name>
            <servlet-class>log4jwebtracker.servlet.TrackerServlet</servlet-class>
        </servlet>
        <servlet-mapping>
            <servlet-name>TrackerServlet</servlet-name>
            <url-pattern>/tracker/*</url-pattern>
        </servlet-mapping>

In the example the tool was mapped as `/tracker/*`, so if the application is accessible in [http://localhost:8080/myapp](http://localhost:8080/myapp), the correct URL to access to the tracker is:

[http://localhost:8080/myapp/tracker](http://localhost:8080/myapp/tracker)


Introduction
------------

In the first tab you can setup the level of each logger of the application, including the `root` logger. The configuration are applied at the moment, but not change the original configuration placed in `log4j.properties`, `log4j.xml` or any configuration file, if you restart the application, the original configuration is applied again.

<img src="https://raw.github.com/mrsarm/log4jwebtracker/master/artwork/log4jwebtracker_setting.png" />

In the second tab named _**Log**_, you can select a appender file and view the last content in the webpage (the numbers of line are configurable), or download them.

<img src="https://raw.github.com/mrsarm/log4jwebtracker/master/artwork/log4jwebtracker_log.png" />


Log4j configuration
-------------------

**Log4j** not need any special configuration for use this tool, you can use the usual way to configure for you. Many developers used a util class from **Spring** in the web context to do this (`org.springframework.web.util.Log4jConfigListener`).

Also can use the automatic setup (the configuration file must be placed on the `WEB-INF/classes` folder), or the manually basic setup, invoking `org.apache.log4j.PropertyConfigurator.configure(String configFilename)`.

**Log4jWebTracker** provide a servlet class to do that, but your use is optional:

        <servlet>
                <servlet-name>Log4jInitServlet</servlet-name>
                <servlet-class>log4jwebtracker.servlet.init.Log4jInitServlet</servlet-class>
                <init-param>
                        <param-name>log4jConfigLocation</param-name>
                        <param-value>WEB-INF/classes/log4j.xml</param-value>
                </init-param>
                <load-on-startup>1</load-on-startup>
        </servlet>



Requirements
------------

* Java 1.4+
* Servlet container 2.3+ (like Apache Tomcat 4.1 or higher, or WebSphere 5.0+, etc.)


Dependencies
------------
No dependencies with others libraries or frameworks, except of course **log4j** _(tested only with v1.2)_.


Maven projects
--------------

If your project is building using **Apache Maven 2** or above, put this artifact in your `pom.xml` to import the jar:

        <dependency>
            <groupId>log4jwebtracker</groupId>
            <artifactId>log4jwebtracker</artifactId>
            <version>1.0.1</version>
        </dependency>


The repository necessary to obtain the artifact is:

        <repository>
                <id>log4jwebtracker-releases</id>
                <url>http://repo.log4jwebtracker.com/maven2</url>
        </repository>


TO DO
-----

* Securize access (_anyway, can be done with filters, web security using web.xml policies, or others options_).
* Configure the appenders and patterns at runtime.
* Filter field with regular expressions to match the output of the log.
* _Send us your proposal..._


License
-------

The project is licensed under the terms of the [GNU Lesser GPL](http://www.gnu.org/licenses/lgpl.html) _(LGPL)_.


Binary and Source code
----------------------

This source code is available in [Github](https://github.com/mrsarm/log4jwebtracker).

Also the source code is available in .jar format [here](http://www.log4jwebtracker.com/documents/14713/14799/log4jwebtracker-sources.jar).

The binary version in .jar format, ready to use in your project is [here](http://www.log4jwebtracker.com/documents/14713/14799/log4jwebtracker.jar).


About
-----

The home page of this project is http://www.log4jwebtracker.com

**Author**: My name is **Mariano Ruiz**, I work as _Java & Web Developer_. I'm from Argentina.

**My Home**: http://www.mrdev.com.ar

**Email**: [log4jwebtracker@mrdev.com.ar](mailto:log4jwebtracker@mrdev.com.ar) | [marianoruiz@mrdev.com.ar](mailto:marianoruiz@mrdev.com.ar)
