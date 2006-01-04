/*
 * ObjectInspectorPage.java
 *
 * Created on terca, 25 de fevereiro de 2003 04:27
 * vim:fileencoding=utf-8:encoding=utf-8
 */
package com.oktiva.mogno.studio;

import com.oktiva.mogno.Application;
import com.oktiva.mogno.Component;
import com.oktiva.mogno.TopLevel;
import com.oktiva.mogno.Visual;
import com.oktiva.mogno.html.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;


/**
 * @version $Id$
 */
public class ObjectInspectorPage extends MognoStudioPage {
	static Logger logger = Logger.getLogger(ObjectInspectorPage.class.getName());
	Application app = new Application();
	TopLevel topLevel = null;
	
	public void onCreatePage(ObjectInspectorPage page)
	throws Exception {
		logger.debug("onCreatePage ObjectInspectorPage");
		HttpSession session = application.getSession();
		String appName = (String)session.getAttribute("application");
		String workDir = (String)session.getAttribute("workDir");
		String topLevelName = (String)session.getAttribute("topLevel");
		String selectedComponent = (String)session.getAttribute("selectedComponent");
		((Select)getChild("selectComponent")).getValues().add(0,selectedComponent);
		logger.debug("SelectedComponent: "+selectedComponent);
		String bar = File.separator;
		// obter application
		app.setRootDir(workDir+bar+appName);
		app.setDesigning(true);
		app.initialize("MognoApplication.xml");
		topLevel = app.getTopLevel(topLevelName);
		createComponentOptions();
		buildPropertiesTr(selectedComponent);
	}
	private void createComponentOptions() {
		logger.debug("CreateComponentOptions");
		String selName = "selectComponent";
		Select sel = (Select)getChild(selName);
		Vector v = new Vector();
		v.add(topLevel.getName());
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			v.add(e.nextElement());
		}
		Collections.sort(v);
		for (int i=0; i<v.size(); i++) {
			String childName = (String)v.get(i);
			Option o = new Option();
			o.setName(selName+"_"+childName);
			if(childName.equals(topLevel.getName())) {
				o.setContent(childName+" ("+topLevel.getClass().getName()+")");
			} else {
				o.setContent(childName+" ("+topLevel.getChildClass(childName).getName()+")");
			}
			o.setValue(childName);
			o.setParent(selName);
			o.setSelect(selName);
			o.setTop(i);
			registerChild(o);
		}
	}
	private void destroySelectOptions(String selectName) {
		Enumeration e = listChilds();
		while (e.hasMoreElements()) {
			Visual comp = (Visual)getChild((String)e.nextElement());
			if(comp instanceof Option) {
				if(selectName.equals(((Option)comp).getSelect())) {
					freeChild(comp.getName());
				}
			}
		}
	}
	private void changeComponentOptions() {
		destroySelectOptions("selectComponent");
		createComponentOptions();
	}
	public void onSelectComponentChange(Select sel)
	throws Exception {
		HttpSession session = application.getSession();
		String newComp = (String)sel.getValues().get(0);
		String oldComp = (String)session.getAttribute("selectedComponent");
		logger.debug("Novo componente: "+newComp);
		// Change properties tr
		changePropertiesTr(newComp);
		session.setAttribute("selectedComponent", newComp);
		application.outHtml(show());
	}
	private void changePropertiesTr(String newComp) {
		destroyPropertiesTr();
		buildPropertiesTr(newComp);
	}
	private void destroyPropertiesTr() {
		Enumeration e = listChilds();
		while(e.hasMoreElements()) {
			String childName = (String)e.nextElement();
			if(childName.startsWith("x_attrib_")) {
				freeChild(childName);
			}
		}
	}
	private void buildPropertiesTr(String compName) {
		Component comp = null;
		if(topLevel.getName().equals(compName)) {
			comp = topLevel;
		} else {
			comp = topLevel.getChild(compName);
		}
		if(comp != null) {
			Hashtable attrs = comp.attribsHash();
			Class myClass = comp.getClass();
			Field[] fs = myClass.getFields();
			Arrays.sort(fs, new FieldComparator());
			for (int i = 0;i < fs.length;i++) {
				String fieldName = fs[i].getName();
				buildTr(compName,i,fieldName,(String)attrs.get(fieldName));
			}
		} else {
			Tr tr = new Tr();
			tr.setName("x_attrib_tr_error");
			tr.setParent("content");
			tr.setTop(0);
			registerChild(tr);
			Td td = new Td();
			td.setName(tr.getName()+"_td1");
			td.setParent(tr.getName());
			td.setTop(0);
			td.setColspan("2");
			td.setContent("Component unknown: '"+compName+"'.");
			registerChild(td);
		}
	}
	private class FieldComparator implements Comparator {
		public int compare(java.lang.Object f1, java.lang.Object f2) {
			return ((Field)f1).getName().compareToIgnoreCase(((Field)f2).getName());
		}
	}
	private void buildTr(String compName, int i, String name, String value) {
		Tr tr = new Tr();
		tr.setName("x_attrib_tr_"+name);
		tr.setParent("content");
		tr.setTop(i);
		registerChild(tr);
		Td td = new Td();
		td.setName(tr.getName()+"_td1");
		td.setParent(tr.getName());
		td.setTop(0);
		td.setContent(name);
		registerChild(td);
		td = new Td();
		td.setName(tr.getName()+"_td2");
		td.setParent(tr.getName());
		td.setTop(1);
		registerChild(td);
		if(!name.equals("parent")) {
			Input in = new Input();
			in.setName("x_attrib_"+name);
			in.setValue(value);
			in.setParent(td.getName());
			in.setType("text");
			registerChild(in);
		} else {
			Select sel = new Select();
			sel.setName("x_attrib_"+name);
			sel.getValues().add(0,value);
			sel.setParent(td.getName());
			registerChild(sel);
			// If this component is the TopLevel itself
			if (topLevel.getName().equals(compName)) {
				Option o = new Option();
				String topLevelParent = topLevel.getParent();
				// If the TopLevel has no parent, put a empty option
				if (topLevelParent == null || "".equals(topLevelParent)) {
					o.setName(sel.getName()+"_TOP_LEVEL");
					o.setContent("");
					o.setValue("");
				// Else, if the parent of the TopLevel isn't in this page, put it's name anyway
				} else if (getChild(topLevelParent) == null) {
					o.setName(sel.getName()+"_"+topLevelParent);
					o.setContent(topLevelParent);
					o.setValue(topLevelParent);
				}
				o.setParent(sel.getName());
				o.setSelect(sel.getName());
				o.setTop(0);
				registerChild(o);
			}
			createParentOptions(topLevel, sel.getName());
			// A component can't be his own parent...
			freeChild(sel.getName()+"_"+compName);
		}
	}
	public void onSaveChangesClick(Input in)
	throws Exception {
		String compName = (String)((Select)getChild("selectComponent")).getValues().get(0);
		Component comp = null;
		if(topLevel.getName().equals(compName)) {
			comp = topLevel;
		} else {
			comp = topLevel.getChild(compName);
		}
		Hashtable data = new Hashtable();
		Hashtable attrs = comp.attribsHash();
		Class myClass = comp.getClass();
		Field[] fs = myClass.getFields();
		boolean rename = false;
		String newName = "";
		for (int i = 0;i < fs.length;i++) {
			String fieldName = fs[i].getName();
			if(!fieldName.equals("parent")) {
				Input inValue = (Input)getChild("x_attrib_"+fieldName);
				if (fieldName.equals("name")) {
					if(!compName.equals(inValue.getValue())) {
						rename = true;
						newName = inValue.getValue();
					}
				} else {
					if(!attrs.containsKey(fieldName) || !((String)attrs.get(fieldName)).equals(inValue.getValue())) {
						data.put(fieldName, inValue.getValue());
					}
				}
			} else {
				Select selValue = (Select)getChild("x_attrib_"+fieldName);
				if(!attrs.containsKey(fieldName) ||
				!((String)attrs.get(fieldName)).equals((String)selValue.getValues().get(0))) {
					data.put(fieldName, (String)selValue.getValues().get(0));
				}
			}
		}
		comp.setProperties(data);
		if(rename) {
			renameComponent(comp,newName);
			script = "window.top.location='?mognoOrigin=ApplicationPage&editTopLevel_"+topLevel.name+"=true';\n";
		} else {
			if (script == null) {
				script = "";
			}
			script += "parent.previewFrame.location.reload();parent.compTreeFrame.location.reload();";
		}
		topLevel.store();
		application.outHtml(show());
	}
	private void renameComponent(Component comp, String newName)
	throws Exception {
		String oldName = comp.getName();
		logger.debug("Renaming component '"+oldName+"' to '"+newName+"'");
		if(comp instanceof TopLevel) {
			topLevel.setName(newName);
			Hashtable data = app.getTopLevelsData();
			// Alterar o nome no hash de toplevels da aplicação
			String classe = (String)((Hashtable)data.get(oldName)).get("class");
			String xml = (String)((Hashtable)data.get(oldName)).get("xml");
			app.removeTopLevel(oldName);
			app.addTopLevel(newName, classe, xml);
			// Ajustar o top level default
			if(app.getDefaultTopLevel().equals(oldName)) {
				app.setDefaultTopLevel(newName);
			}
			app.store();
		} else {
			comp.setName(newName);
			topLevel.freeChild(oldName);
			topLevel.registerChild(comp);
		}
		changeComponent(newName);
		renameParentOnChilds(oldName,newName);
	}
	private void renameParentOnChilds(String oldName, String newName) {
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			Visual comp = (Visual)topLevel.getChild((String)e.nextElement());
			if(oldName.equals(comp.getParent())) {
				comp.setParent(newName);
			}
		}
	}
	
	private void changeComponent(String compName) {
		changeComponentOptions();
		HttpSession session = application.getSession();
		session.setAttribute("selectedComponent", compName);
		((Select)getChild("selectComponent")).setValue(compName);
		changePropertiesTr(compName);
	}
	
	public void onDeleteClick(Input in)
	throws Exception {
		String compName = (String)((Select)getChild("selectComponent")).getValue();
		deleteComponent(compName);
		topLevel.store();
		changeComponent(topLevel.getName());
		if (script == null) {
			script = "";
		}
		Date now = new Date();
		script += "parent.previewFrame.location.reload();parent.compTreeFrame.location.reload();parent.compTreeFrame.location.reload();parent.topFrame.location='?mognoOrigin=TopLevelPage&topFrame=true&killCacheVar="+now.getTime()+"';";
		application.outHtml(show());
	}
	
	private void deleteComponent(String compName) {
		logger.debug("Deleting component: "+compName);
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			Visual comp = (Visual)topLevel.getChild((String)e.nextElement());
			if(compName.equals(comp.getParent())) {
				deleteComponent(comp.getName());
			}
		}
		topLevel.freeChild(compName);
	}
}
