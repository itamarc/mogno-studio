/*
 * CompTreeFramePage.java
 *
 * Created on terça, 25 de fevereiro de 2003 13:57
 */
package com.oktiva.mogno.studio;

import com.oktiva.mogno.Application;
import com.oktiva.mogno.Container;
import com.oktiva.mogno.TopLevel;
import com.oktiva.mogno.Visual;
import com.oktiva.mogno.additional.ActiveList;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CompTreeFramePage extends MognoStudioPage {
	static Logger logger = Logger.getLogger(CompTreeFramePage.class.getName());
	Application app = new Application();
	TopLevel topLevel = null;
	
	public void onCreatePage(CompTreeFramePage page)
	throws Exception {
		logger.info("onCreatePage");
		HttpSession session = application.getSession();
		String appName = (String)session.getAttribute("application");
		String workDir = (String)session.getAttribute("workDir");
		String topLevelName = (String)session.getAttribute("topLevel");
		String bar = File.separator;
		// obter application
		app.setRootDir(workDir+bar+appName);
		app.setDesigning(true);
		app.initialize("MognoApplication.xml");
		topLevel = app.getTopLevel(topLevelName);
		ActiveList list = (ActiveList)getChild("list");
		list.setList(formatList(topLevel,topLevel.getFullComponentsVector()));
	}
	private String formatList(TopLevel topLevel, Vector v) {
		char[] spaces = new char[256];
		Arrays.fill(spaces,' ');
		StringBuffer buf = new StringBuffer(256);
		IntHashtable seen = new IntHashtable();
		buf.append(topLevel.getName()).append("|title=\"");
		buf.append(topLevel.getClass().getName().replaceFirst("com\\.oktiva\\.mogno\\.","...")).append("\"");
		buf.append(" onclick=\"parent.objInspFrame.location.href=");
		buf.append("'?mognoOrigin=ObjectInspectorPage&selectComponent=").append(topLevel.getName()).append("';\"");
		int level = 0;
		seen.put(topLevel.getName(),level);
		String last = topLevel.getName();
		String lastParent = "";
		//para cada componente da lista
		for(int i=1; i<v.size(); i++) {
			Visual comp = (Visual)v.get(i);
			if(comp == null) {
				continue;
			}
			//se parent for diferente do parent do anterior
			if(!lastParent.equals(comp.getParent())) {
				//se parent for o anterior
				if(comp.getParent().equals(last)) {
					level++;
				} else {
					if(seen.containsKey(comp.getParent())) {
						level = seen.getInt(comp.getParent()) + 1;
					} else {
						logger.warn("Parent not seen: "+comp.getParent());
					}
				}
			}
			seen.put(comp.getName(),level);
			last = comp.getName();
			lastParent = comp.getParent();
			buf.append("\n").append(String.copyValueOf(spaces,0,level)).append(comp.getName());
			buf.append("|title=\"(").append(comp.getTop());
			buf.append(",").append(comp.getLeft()).append(") ");
			buf.append(comp.getClass().getName().replaceFirst("com\\.oktiva\\.mogno\\.","...")).append("\"");
			buf.append(" onclick=\"parent.objInspFrame.location.href=");
			buf.append("'?mognoOrigin=ObjectInspectorPage&selectComponent=").append(comp.getName()).append("';\"");
		}
		return buf.toString();
	}
	private class IntHashtable extends Hashtable {
		public void put(Object key, int value) {
			this.put(key, new Integer(value));
		}
		public int getInt(Object key) {
			return ((Integer)get(key)).intValue();
		}
	}
}
