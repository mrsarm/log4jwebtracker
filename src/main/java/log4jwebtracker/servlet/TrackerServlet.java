package log4jwebtracker.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import log4jwebtracker.io.StreamUtils;
import log4jwebtracker.logging.LoggingUtils;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Tracker servlet.
 *
 * @author Mariano Ruiz
 */
public class TrackerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(TrackerServlet.class);
	
	private static final int BUFFER_SIZE = 1024 * 16;

	private byte[] jqueryMin = null;
	private byte[] jqueryWordWrap = null;
	private byte[] css = null;
	private byte[] logo = null;

	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Action URI, eg. /webtracker/tracker/config
		String action = request.getRequestURI();
		// Servlet URI, eg. /webtracker/tracker
		String baseAction = request.getContextPath() + request.getServletPath();
		if(request.getPathInfo()==null || request.getPathInfo().equals("") || request.getPathInfo().equals("/")) {
			response.sendRedirect(response.encodeRedirectURL(baseAction + "/config"));
		// If JS resource
		} else if(request.getPathInfo().startsWith("/js/")) {
			if(request.getPathInfo().equals("/js/jquery-1.6.4.min.js")) {
				doResource(request, response, getJQueryMin(), "application/javascript");
			} else if(request.getPathInfo().equals("/js/jquery.wordWrap.js")) {
				doResource(request, response, getJQueryWordWrap(), "application/javascript");
			} else {
				logger.warn("Request javascript resource " + action + " not found.");
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		// If CSS resource
		} else if(request.getPathInfo().startsWith("/css/")) {
			if(request.getPathInfo().equals("/css/tracker.css")) {
				doResource(request, response, getCSS(), "text/css");
			} else {
				logger.warn("Request CSS resource " + action + " not found.");
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		// If image resource
		} else if(request.getPathInfo().startsWith("/img/")) {
			if(request.getPathInfo().equals("/img/logo.png")) {
				doResource(request, response, getLogo(), "image/png");
			} else {
				logger.warn("Request image resource " + action + " not found.");
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		// If log request
		} else if(request.getPathInfo().startsWith("/taillog")) {
			doTailLog(request, response, action, baseAction);
		// If ajax log request
		} else if(request.getPathInfo().startsWith("/getlog")) {
			doGetLog(request, response, action, baseAction);
		// Page resource
		} else {
			doPage(request, response, action, baseAction);
		}
	}

	private void doResource(
			HttpServletRequest request, HttpServletResponse response,
			byte[] buffer, String contentType)
					throws ServletException, IOException {
		ServletOutputStream output = response.getOutputStream();
		response.setContentType(contentType);
		response.setContentLength(buffer.length);
		output.write(buffer, 0, buffer.length);
		output.flush();
		output.close();
	}

	private void doPage(
			HttpServletRequest request, HttpServletResponse response,
			String action, String baseAction)
					throws ServletException, IOException {
		request.setAttribute("action", action);
		request.setAttribute("baseAction", baseAction);
		if(request.getPathInfo().equals("/log")) {
			doLog(request, response, action, baseAction);
		} else if(request.getPathInfo().equals("/config")) {
			doConfiguration(request, response, action, baseAction);
		} else {
			logger.warn("Request page " + action + " not found.");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private void doConfiguration(
			HttpServletRequest request, HttpServletResponse response,
			String action, String baseAction)
					throws ServletException, IOException {
		List loggers = LoggingUtils.getLoggers();
		request.setAttribute("loggers", loggers);
		Enumeration e = request.getParameterNames();
		while(e.hasMoreElements()) {
			String parameterName = (String) e.nextElement();
			if(parameterName.equals("root")) {
				Level level = Level.toLevel(request.getParameter(parameterName));
				Logger root = LogManager.getRootLogger();
				synchronized(root) {
					root.setLevel(level);
				}
				if(logger.isDebugEnabled()) {
					logger.debug(parameterName + '=' + level.toString());
				}
			} else {
				if(LoggingUtils.contains(loggers, parameterName)) {
					Level level = Level.toLevel(request.getParameter(parameterName));
					Logger logg = LogManager.getLogger(parameterName);
					synchronized(logg) {
						logg.setLevel(level);
					}
					if(logger.isDebugEnabled()) {
						logger.debug(parameterName + '=' + level.toString());
					}
				} else {
					logger.warn("Logger name " + parameterName + " not exist.");
				}
			}
		}
		//getServletConfig().getServletContext()
		//	.getRequestDispatcher("/tracker.jsp")
		//	.forward(request, response);
		doHTML(request, response);
	}

	private void doLog(
			HttpServletRequest request, HttpServletResponse response,
			String action, String baseAction)
					throws ServletException, IOException {
		request.setAttribute("fileAppenders", LoggingUtils.getFileAppenders());
		//getServletConfig().getServletContext()
		//	.getRequestDispatcher("/tracker.jsp")
		//	.forward(request, response);
		doHTML(request, response);
	}

	private void doTailLog(HttpServletRequest request,
			HttpServletResponse response, String action, String baseAction)
					throws ServletException, IOException {
		String appenderName = request.getParameter("appender");
		if(appenderName!=null) {
			int lines = 20;
			if(request.getParameter("lines")!=null) {
				try {
					lines = Integer.parseInt(request.getParameter("lines"));
				} catch(NumberFormatException e) {
					logger.warn("Number format 'lines' parameter invalid = "
							+ request.getParameter("lines"));
				}
			}
			FileAppender fileAppender = LoggingUtils.getFileAppender(appenderName);
			if(fileAppender!=null) {
				OutputStream output = response.getOutputStream();
				try {
					String contentType = "text/plain";
					if(fileAppender.getEncoding()!=null) {
						contentType += "; charset=" + fileAppender.getEncoding();
					}
					response.setContentType(contentType);
					response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
					response.setHeader("Pragma", "no-cache");        // HTTP 1.0
					response.setDateHeader("Expires", -1);           // prevents caching
					RandomAccessFile inputFile = new RandomAccessFile(fileAppender.getFile(), "r");
					StreamUtils.tailFile(inputFile, output, BUFFER_SIZE, lines);
					inputFile.close();
				} catch(IOException e) {
					logger.error("Error getting the file appender="
					           + fileAppender.getFile(), e);
					output.write("TrackerError: Check the log manually.".getBytes());
				}
				output.flush();
				output.close();
			} else {
				logger.error("FileAppender with name=" + appenderName + " not exist.");
			}
		} else {
			logger.error("No appender name parameter specified.");
		}
	}

	private void doGetLog(HttpServletRequest request,
			HttpServletResponse response, String action, String baseAction)
					throws ServletException, IOException {
		String appenderName = request.getParameter("appender");
		if(appenderName!=null) {
			FileAppender fileAppender = LoggingUtils.getFileAppender(appenderName);
			if(fileAppender!=null) {
				File file = new File(fileAppender.getFile());
				OutputStream output = response.getOutputStream();
				try {
					String contentType = "text/plain";
					if(fileAppender.getEncoding()!=null) {
						contentType += "; charset=" + fileAppender.getEncoding();
					}
					response.setContentType(contentType);
					response.setContentLength((int) file.length());
					response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
					response.setHeader("Pragma", "no-cache");        // HTTP 1.0
					response.setDateHeader("Expires", -1);           // prevents caching
					response.setHeader("Content-Disposition",
							"attachment; filename=\""
							+ file.getName() + "\"");
					InputStream fileStream = new FileInputStream(fileAppender.getFile());
					StreamUtils.readStream(fileStream, output, BUFFER_SIZE);
					fileStream.close();
				} catch(IOException e) {
					response.setHeader("Content-Disposition", "");
					logger.error("Error getting the file appender="
					           + fileAppender.getFile(), e);
					output.write("TrackerError: Check the log manually.".getBytes());
					output.close();
				}
				output.flush();
				output.close();
			} else {
				logger.error("FileAppender with name=" + appenderName + " not exist.");
			}
		} else {
			logger.error("No appender name parameter specified.");
		}
	}



	public void doHTML(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException, ServletException {

		ServletOutputStream out = response.getOutputStream();;

		response.setContentType("text/html; charset=UTF-8");

		out.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><meta http-equiv=\"Pragma\" content=\"no-cache\" /><meta http-equiv=\"Expires\" content=\"-1\" /><link rel=\"stylesheet\" type=\"text/css\" href=\"");
		out.print((String) request.getAttribute("baseAction"));
		out.print("/css/tracker.css\" /><script type=\"text/javascript\" src=\"");
		out.print((String) request.getAttribute("baseAction"));
		out.print("/js/jquery-1.6.4.min.js\"></script><script type=\"text/javascript\" src=\"");
		out.print((String) request.getAttribute("baseAction"));
		out.print("/js/jquery.wordWrap.js\"></script><script>$(document).ready( function() {");
		if (request.getAttribute("action").toString().indexOf("/config") != -1) {
			out.print("$('#filter').keyup(function() {var filterKey = this.value.toLowerCase();$('#loggers tbody tr').filter(function() {return $('select', this)[0].name.toLowerCase().indexOf(filterKey) == -1;}).hide();$('#loggers tbody tr').filter(function() {return $('select', this)[0].name.toLowerCase().indexOf(filterKey) != -1;}).show();});$('select').change(function() {$(this).parent().submit();});");
		}
		out.print("");
		if (request.getAttribute("action").toString().indexOf("/log") != -1) {
			out.print("$('#wrapCheck').click(function() {var textarea = $('#logText').get();if( ! $(this).attr('checked') ) {$(textarea).wordWrap('on');} else {$(textarea).wordWrap('off');}});var refresh = function() {$('#loading-mask').text('Loading...');$('#loading-mask').removeClass('error');$('#loading-mask').show();var data = {appender: $('#appender').val()};if($('#lines').val()!='') {data.lines = $('#lines').val();}$.ajax({url: '");
			out.print((String) request.getAttribute("baseAction"));
			out.print("/taillog',data: data,dataType: 'text',cache: false,success: function(data) {if(data.indexOf('TrackerError')==-1) {$('#loading-mask').hide();$('#logText').val(data);$('#logText').get(0).scrollTop = $('#logText').get(0).scrollHeight;} else {$('#loading-mask').addClass('error');$('#loading-mask').text('Error: Check the log manually');}}});};$('#refresh').click(refresh);$('#lines').bind('keypress', function(e) { if(e.keyCode==13) { refresh(); return false; } });$('#download').click(function() {});");
		}
		out.print("});</script><!--[if !IE 7]><style type=\"text/css\">#wrap { display:table;height:100% }</style><![endif]--></head><body><div id=\"wrap\"><div id=\"header\"><h1><a href=\"");
		out.print((String) request.getAttribute("baseAction"));
		out.print("\"       title=\"Log4j Web Tracker\"       style=\"background: url('");
		out.print((String) request.getAttribute("baseAction"));
		out.print("/img/logo.png') no-repeat; display: block; height: 96px;\"></a></h1></div><div id=\"navcontainer\"><ul id=\"navlist\"><li><a href=\"");
		out.print((String) request.getAttribute("baseAction"));
		out.print("/config\"   ");
		if (request.getAttribute("action").toString().indexOf("/config") != -1) {
			out.print("class=\"active\" ");
		}
		out.print(">Configuration</a></li><li><a href=\"");
		out.print((String) request.getAttribute("baseAction"));
		out.print("/log\"   ");
		if (request.getAttribute("action").toString().indexOf("/log") != -1) {
			out.print("class=\"active\" ");
		}
		out.print(" >Log</a></li></ul></div><div class=\"clear\"></div><div id=\"main\">");

		if (request.getAttribute("action").toString().indexOf("/config") != -1) {
			List loggers = (List) request.getAttribute("loggers");

			out.print("<div id=\"configuration\"><div id=\"filterContainer\"><div id=\"filterTextContainer\"><p>Filter:</p></div><div id=\"filterInputContainer\"><input type=\"text\" id=\"filter\" name=\"filter\"       placeholder=\"Enter the name or part of it\" /></div></div><div class=\"clear\"></div><div id=\"loggersContainer\"><table id=\"loggers\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><thead><tr><th>Logger</th><th>Level</th></tr></thead><tbody>");

			for (int i = 0; i < loggers.size(); i++) {
				Logger logger = (Logger) loggers.get(i);

				out.print("<tr class=\"logger-");
				out.print(i % 2 == 0 ? "pair" : "odd");
				out.print("\"><td class=\"logger-name\"><label for=\"");
				out.print(logger.getName());
				out.print("\">");
				out.print(logger.getName());
				out.print("</label></td><td class=\"logger-level\"><form action=\"");
				out.print((String) request.getAttribute("baseAction"));
				out.print("/config\" method=\"post\"><select id=\"");
				out.print(logger.getName());
				out.print("\" name=\"");
				out.print(logger.getName());
				out.print("\"><option value=\"TRACE\" ");
				if (logger.getEffectiveLevel().toString() == "TRACE") {
					out.print(" selected=\"selected\" ");
				}
				out.print(">TRACE</option><option value=\"DEBUG\" ");
				if (logger.getEffectiveLevel().toString() == "DEBUG") {
					out.print(" selected=\"selected\" ");
				}
				out.print(">DEBUG</option><option value=\"INFO\" ");
				if (logger.getEffectiveLevel().toString() == "INFO") {
					out.print(" selected=\"selected\" ");
				}
				out.print(">INFO</option><option value=\"WARN\" ");
				if (logger.getEffectiveLevel().toString() == "WARN") {
					out.print(" selected=\"selected\" ");
				}
				out.print(">WARN</option><option value=\"ERROR\" ");
				if (logger.getEffectiveLevel().toString() == "ERROR") {
					out.print(" selected=\"selected\" ");
				}
				out.print(">ERROR</option><option value=\"FATAL\" ");
				if (logger.getEffectiveLevel().toString() == "FATAL") {
					out.print(" selected=\"selected\" ");
				}
				out.print(">FATAL</option><option value=\"OFF\" ");
				if (logger.getEffectiveLevel().toString() == "OFF") {
					out.print(" selected=\"selected\" ");
				}
				out.print(">OFF</option>");
				out.print(logger.getEffectiveLevel().toString());
				out.print("</select></form></td></tr>");

			}

			out.print("</tbody></table></div></div>");
		}
		out.print("");
		if (request.getAttribute("action").toString().indexOf("/log") != -1) {
			out.print("<div id=\"log\"><div id=\"options\"><div style=\"float: left;\"><input type=\"checkbox\" id=\"wrapCheck\" />&nbsp;<label for=\"wrapCheck\">No wrap log</label></div><form action=\"");
			out.print((String) request.getAttribute("baseAction"));
			out.print("/getlog\"><div style=\"float: right;\"><label for=\"appender\">File appender: </label><select id=\"appender\" name=\"appender\">");

			List fileAppenders = (List) request
					.getAttribute("fileAppenders");
			for (int i = 0; i < fileAppenders.size(); i++) {
				FileAppender fap = (FileAppender) fileAppenders.get(i);

				out.print("<option value=\"");
				out.print(fap.getName());
				out.print("\">");
				out.print(fap.getName());
				out.print("</option>");
			}
			out.print("</select><label for=\"lines\">Lines: </label><input type=\"number\" id=\"lines\" name=\"lines\" value=\"20\" size=\"4\" style=\"margin-right: 5px;\" /><button type=\"button\" id=\"refresh\">Refresh</button><button type=\"submit\" id=\"download\">Download</button></div></form><div style=\"overflow: hidden;\"><div id=\"loading-mask\" style=\"display: none;\"></div></div></div><textarea id=\"logText\" rows=\"20\"></textarea></div>");
		}
		out.print("</div></div><div id=\"footer\"><div id=\"back-link\"><span><a href=\"");
		out.print(request.getContextPath());
		out.print("/\">&uarr; Go to the application</a></span></div><div id=\"copyright\"><span>Copyright 2011&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<i>Power by&nbsp;&nbsp;</i><a href=\"http://www.log4jwebtracker.com\" target=\"_blank\">Log4j Web Tracker</a> v1.0.1</span></div></div></body></html>");
	}

	synchronized private byte[] getJQueryMin() throws IOException {
		if(jqueryMin==null) {
			InputStream in = this.getClass().getResourceAsStream("js/jquery-1.6.4.min.js");
			jqueryMin = toByteArray(in);
			in.close();
		}
		return jqueryMin;
	}

	synchronized private byte[] getJQueryWordWrap() throws IOException {
		if(jqueryWordWrap==null) {
			InputStream in = this.getClass().getResourceAsStream("js/jquery.wordWrap.js");
			jqueryWordWrap = toByteArray(in);
			in.close();
		}
		return jqueryWordWrap;
	}

	synchronized private byte[] getCSS() throws IOException {
		if(css==null) {
			InputStream in = this.getClass().getResourceAsStream("css/tracker.css");
			css = toByteArray(in);
			in.close();
		}
		return css;
	}

	synchronized private byte[] getLogo() throws IOException {
		if(logo==null) {
			InputStream in = this.getClass().getResourceAsStream("img/logo.png");
			logo = toByteArray(in);
			in.close();
		}
		return logo;
	}
	
	/**
     * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * 
     * @param input  the <code>InputStream</code> to read from
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamUtils.readStream(input, output, BUFFER_SIZE);
        return output.toByteArray();
    }
}
