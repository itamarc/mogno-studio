/*
 * TopLevelPage.java
 *
 * Created on segunda, 24 de fevereiro de 2003 23:18
 */
package com.oktiva.mogno.studio;

import com.oktiva.mogno.Application;
import com.oktiva.mogno.TopLevel;
import com.oktiva.mogno.additional.ActionFrame;
import com.oktiva.mogno.additional.ActionSpan;
import com.oktiva.mogno.html.H1;
import com.oktiva.mogno.html.Input;
import com.oktiva.mogno.html.Option;
import com.oktiva.mogno.html.Select;
import com.oktiva.util.FileUtil;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class TopLevelPage extends MognoStudioPage {
	static Logger logger = Logger.getLogger(TopLevelPage.class.getName());
	String[] classes = null;
	Application app = new Application();
	TopLevel topLevel = null;
	public String frame = "";
	public void onCreatePage(TopLevelPage page)
	throws Exception {
		HttpSession session = application.getSession();
		String appName = (String)session.getAttribute("application");
		String workDir = (String)session.getAttribute("workDir");
		String bar = File.separator;
		// obter application
		//app.setDesigning(true);
		app.setRootDir(workDir+bar+appName);
		app.setDesigning(true);
		app.initialize("MognoApplication.xml");
	}
	public void onLoadTop(ActionFrame frame)
	throws Exception {
		showPage("TopFramePage");
	}
	public void onLoadObjectInspector(ActionFrame frame)
	throws Exception {
		showPage("ObjectInspectorPage");
	}
	public void onLoadCompTree(ActionFrame frame)
	throws Exception {
		showPage("CompTreeFramePage");
	}
	public void onLoadPreview(ActionFrame frame)
	throws Exception {
		HttpSession session = application.getSession();
		String topLevelName = (String)session.getAttribute("topLevel");
		topLevel = app.getTopLevel(topLevelName);
		application.outHtml(topLevel.show());
	}
}
