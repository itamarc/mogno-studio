/*
 * MognoStudioPage.java
 *
 * Created on quinta, 20 de fevereiro de 2003 15:55
 */
package com.oktiva.mogno.studio;

import com.oktiva.mogno.Application;
import com.oktiva.mogno.Container;
import com.oktiva.mogno.InitializeException;
import com.oktiva.mogno.TopLevel;
import com.oktiva.mogno.additional.DerivedPage;
import com.oktiva.mogno.html.Option;
import com.oktiva.mogno.html.Page;
import com.oktiva.mogno.html.Select;
import com.oktiva.util.FileUtil;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class MognoStudioPage extends Page {
	static Logger logger = Logger.getLogger(MognoStudioPage.class.getName());
	
	protected void createParentOptions(TopLevel topLevel, String selName) {
		Select sel = (Select)getChild(selName);
		Vector v = new Vector();
		v.add(topLevel.getName());
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			String compName = (String)e.nextElement();
			if (topLevel.getChild(compName) instanceof Container) {
				v.add(compName);
			}
		}
		if (topLevel instanceof DerivedPage) {
			addDerivedParentOptions(v, (DerivedPage)topLevel);
		}
		createOptions(v, sel);
	}
	protected void addDerivedParentOptions(Vector v, DerivedPage topLevel) {
		Application app = topLevel.getApplication();
		try {
			TopLevel base = app.getTopLevel(topLevel.getBasePage());
			v.add(base.getName()+" [D]");
			Enumeration e = base.listChilds();
			while (e.hasMoreElements()) {
				String compName = (String)e.nextElement();
				if (base.getChild(compName) instanceof Container) {
					v.add(compName+" [D]");
				}
			}
		} catch (ClassNotFoundException e) {
			logger.warn("Ignoring ClassNotFoundException when trying to get basePage "+topLevel.getBasePage());
		} catch (InstantiationException e) {
			logger.warn("Ignoring InstantiationException when trying to get basePage "+topLevel.getBasePage());
		} catch (IllegalAccessException e) {
			logger.warn("Ignoring IllegalAccessException when trying to get basePage "+topLevel.getBasePage());
		} catch (InitializeException e) {
			logger.warn("Ignoring InitializeException when trying to get basePage "+topLevel.getBasePage());
		} catch (Exception e) {
			logger.warn("Ignoring "+e.getClass().getName()+" when trying to get basePage "+topLevel.getBasePage());
		}
	}
	protected void createTopLevelOptions(Application app, String selName) {
		Select sel = (Select)getChild(selName);
		Vector v = new Vector();
		Enumeration e = app.getTopLevelsData().keys();
		while (e.hasMoreElements()) {
			v.add(e.nextElement());
		}
		createOptions(v, sel);
	}
	protected void createOptions(Vector v, Select sel) {
		String selName = sel.getName();
		Collections.sort(v,new StringIgnoreCaseComparator());
		int init = sel.getValues().size();
		for (int i=0; i<v.size(); i++) {
			String option = (String)v.get(i);
			Option o = new Option();
			o.setName(selName+"_"+option);
			o.setContent(option);
			if (option.endsWith(" [D]")) {
				o.setValue(option.substring(0,option.length()-4));
			} else {
				o.setValue(option);
			}
			o.setParent(selName);
			o.setSelect(selName);
			o.setTop(i+init);
			registerChild(o);
		}
	}
	private class StringIgnoreCaseComparator implements Comparator {
		public int compare(java.lang.Object f1, java.lang.Object f2) {
			return ((String)f1).compareToIgnoreCase((String)f2);
		}
	}
	protected void createTopLevelXml(String rootDir, String topLevelName, String xml)
	throws Exception {
		createTopLevelXml(rootDir, topLevelName, xml, "");
	}
	protected void createTopLevelXml(String rootDir, String topLevelName, String xml, String propsAdic)
	throws Exception {
		String bar = File.separator;
		String filePath = rootDir+bar+"WEB-INF"+bar+"xml"+bar+xml;
		File xmlFile = new File(filePath);
		logger.debug("Writing file '"+xmlFile.getCanonicalPath()+"'");
		String xmlSkel = "<?xml version=\"1.0\" standalone=\"yes\"?>\n" +
		"<MognoComponent xmlns=\"http://www.oktiva.com.br/mogno\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.oktiva.com.br/mogno\n" +
		"Component.xsd\">\n" +
		"    <prop name=\"name\" value=\""+topLevelName+"\" />\n" + propsAdic +
		"</MognoComponent>\n";
		FileUtil.writeFile(xmlSkel, xmlFile);
	}
	protected String getSelectFirstValue(String selectName) {
		Select sel = (Select)getChild(selectName);
		if(sel == null || sel.getValues().size() == 0) {
			return null;
		} else {
			return sel.getValue();
		}
	}
}
