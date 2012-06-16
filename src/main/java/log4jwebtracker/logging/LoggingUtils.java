package log4jwebtracker.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Logging utils methods.
 *
 * @author Mariano Ruiz
 */
public abstract class LoggingUtils {

	static synchronized public List getFileAppenders() {
		List list = new ArrayList();
		Enumeration e = LogManager.getRootLogger().getAllAppenders();
		while(e.hasMoreElements()) {
			Appender a = (Appender) e.nextElement();
			if(a instanceof FileAppender) {
				list.add(a);
			}
		}
		return list;
	}

	static synchronized public FileAppender getFileAppender(String appenderName) {
		Enumeration e = LogManager.getRootLogger().getAllAppenders();
		while(e.hasMoreElements()) {
			Appender a = (Appender) e.nextElement();
			if(a instanceof FileAppender && a.getName().equals(appenderName)) {
				return (FileAppender) a;
			}
		}
		return null;
	}

	static public boolean contains(List loggers, String loggerName) {
		int i=0;
		while(i<loggers.size()) {
			if(((Logger)loggers.get(i)).getName().equals(loggerName)) {
				return true;
			}
			i++;
		}
		return false;
	}

	static public List getLoggers() {
		Enumeration e = LogManager.getCurrentLoggers();
		List loggersList = new LinkedList();
		while(e.hasMoreElements()) {
			loggersList.add(e.nextElement());
		}
		Collections.sort(loggersList, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Logger log0 = (Logger) arg0;
				Logger log1 = (Logger) arg1;
				return log0.getName().compareTo(log1.getName());
			}
		});
		loggersList.add(0, LogManager.getRootLogger());
		return loggersList;
	}
}
