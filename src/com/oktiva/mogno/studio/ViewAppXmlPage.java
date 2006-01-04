/*
 * ViewAppXmlPage.java
 *
 * Created on terca, 20 de maio de 2003 02:59
 * vim:fileencoding=utf-8:encoding=utf-8
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
public class ViewAppXmlPage extends MognoStudioPage {
	static Logger logger = Logger.getLogger(ViewAppXmlPage.class.getName());
	String xmlFile = null;
	
	public void onCreatePage(ViewAppXmlPage page)
	throws Exception {
		HttpSession session = application.getSession();
		String appName = (String)session.getAttribute("application");
		String workDir = (String)session.getAttribute("workDir");
		String bar = File.separator;
		// obter application xml
		xmlFile = workDir +bar+ appName +bar+ "WEB-INF" +bar+ "xml" +bar+ "MognoApplication.xml";
		H1 h1 = (H1)getChild("h1");
		h1.setContent(h1.getContent()+" - "+appName);
	}
	public void onShowPage(ViewAppXmlPage page)
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
		showPage("ApplicationPage");
	}
	public void onClickDiscardChanges(Input in)
	throws Exception {
		showPage("ApplicationPage");
	}
}
