/*
 * ViewXmlPage.java
 *
 * Created on segunda, 24 de fevereiro de 2003 23:18
 */
package com.oktiva.mogno.studio;

import com.oktiva.mogno.Application;
import com.oktiva.mogno.TopLevel;
import com.oktiva.mogno.html.H1;
import com.oktiva.mogno.html.Input;
import com.oktiva.mogno.html.Textarea;
import com.oktiva.util.FileUtil;
import java.io.File;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ViewXmlPage extends MognoStudioPage {
	static Logger logger = Logger.getLogger(ViewXmlPage.class.getName());
	String xmlFile = null;
	
	public void onCreatePage(ViewXmlPage page)
	throws Exception {
		HttpSession session = application.getSession();
		String appName = (String)session.getAttribute("application");
		String workDir = (String)session.getAttribute("workDir");
		String topLevelName = (String)session.getAttribute("topLevel");
		String bar = File.separator;
		// obter application
		Application app = new Application();
		app.setRootDir(workDir+bar+appName);
		app.setDesigning(true);
		app.initialize("MognoApplication.xml");
		String xmlFileName = app.getTopLevelXmlFileName(topLevelName);
		xmlFile = workDir +bar+ appName +bar+ "WEB-INF" +bar+ "xml" +bar+ xmlFileName;
		H1 h1 = (H1)getChild("h1");
		h1.setContent(h1.getContent()+" - "+appName+" - "+topLevelName);
	}
	public void onShowPage(ViewXmlPage page)
	throws Exception {
		String xml = FileUtil.readFile(xmlFile);
		Textarea xmlText = (Textarea)getChild("xmlText");
		xmlText.setContent(xml);
	}
	public void onClickSaveChanges(Input in)
	throws Exception {
		Textarea xmlText = (Textarea)getChild("xmlText");
		String xml = xmlText.getContent();
		FileUtil.writeFile(xml,xmlFile);
		showPage("TopLevelPage");
	}
	public void onClickDiscardChanges(Input in)
	throws Exception {
		showPage("TopLevelPage");
	}
}
