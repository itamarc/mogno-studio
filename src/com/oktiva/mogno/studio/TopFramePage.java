/*
 * TopFramePage.java
 *
 * Created on segunda, 24 de fevereiro de 2003 17:51
 * vim:fileencoding=utf-8:encoding=utf-8
 */
package com.oktiva.mogno.studio;

import com.oktiva.mogno.Application;
import com.oktiva.mogno.Component;
import com.oktiva.mogno.TopLevel;
import com.oktiva.mogno.Visual;
import com.oktiva.mogno.additional.ActionSpan;
import com.oktiva.mogno.html.H1;
import com.oktiva.mogno.html.Input;
import com.oktiva.mogno.html.Option;
import com.oktiva.mogno.html.Select;
import com.oktiva.util.FileUtil;
import com.oktiva.util.NumberUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class TopFramePage extends MognoStudioPage {
	static Logger logger = Logger.getLogger(TopFramePage.class.getName());
	String[] classes = null;
	Application app = new Application();
	TopLevel topLevel = null;
	public void onCreatePage(TopFramePage page)
	throws Exception {
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
		createClassOptions();
		createParentOptions(topLevel,"addCompParent");
	}
	public void onShowPage(TopFramePage page)
	throws Exception {
		logger.debug("onShowPage: "+page.name);
		HttpSession session = application.getSession();
		// preparar header
		String appName = (String)session.getAttribute("application");
		H1 h1 = (H1)getChild("h1");
		h1.content += " - "+appName+" - "+topLevel.name;
	}
	
	private void createClassOptions()
	throws IOException {
		String name = "addCompClass";
		Select sel = (Select)getChild(name);
		if (classes == null) {
			getComponentClasses();
		}
		int i=0;
		while (i<classes.length) {
			if (classes[i] == null || classes[i].equals("")) {
				continue;
			}
			int split = classes[i].lastIndexOf('.');
			String pack = classes[i].substring(0,split);
			String className = classes[i].substring(split+1);
			Option o = new Option();
			o.setName(name+"_"+classes[i]);
			o.setContent(className+" ("+pack+")");
			o.setValue(classes[i]);
			o.setParent(name);
			o.setSelect(name);
			o.setTop(i);
			registerChild(o);
			i++;
		}
	}
	private void getComponentClasses()
	throws IOException {
		String bar = File.separator;
		String file = application.getRootDir()+bar+"WEB-INF"+bar+"config"+bar+"componentClasses.txt";
		classes = FileUtil.readFileAsArray(file);
	}
	
	public void onClickBack(ActionSpan span)
	throws Exception {
		showPage("ApplicationPage");
	}
	public void onClickViewXml(ActionSpan span)
	throws Exception {
		showPage("ViewXmlPage");
	}
	public void onClickAdd(Input in)
	throws Exception {
		Select classSel = (Select)getChild("addCompClass");
		String className = (String)classSel.getValues().get(0);
		Select parentSel = (Select)getChild("addCompParent");
		String parent = (String)parentSel.getValues().get(0);
		// obter instância do componente criado
		Class ownedClass = Class.forName(className);
		Visual comp = (Visual)ownedClass.newInstance();
		comp.setDesigning(designing);
		comp.setParent(parent);
		// gerar nome
		comp.setName(createName(className));
		// obter proximo top
		Input inLeft = (Input)getChild("addCompLeft");
		Input inTop = (Input)getChild("addCompTop");
		if (NumberUtils.intValido(inTop.getValue())) {
			comp.setTop(Integer.parseInt(inTop.getValue()));
			if (NumberUtils.intValido(inLeft.getValue())) {
				int sugerido = Integer.parseInt(inLeft.getValue());
				if (sugerido < 0) {
					// adicionar linha no top que o cara disse
					addCompRow(parent, comp.getTop());
					comp.setLeft(0);
				} else {
					// adicionar o componente no top e left, movendo se tiver colizao
					comp.setLeft(sugerido);
					checkCollision(parent, comp.getTop(), comp.getLeft());
				}
			} else {
				// adicionar na linha com o ultimo left
				comp.setLeft(getNextLeft(parent, comp.getTop()));
			}
		} else {
			// O left fica so para o caso do cara estar querendo colocar um outro componente depois
			// com o left menor que o atual.
			if (NumberUtils.intValido(inLeft.value)) {
				comp.setLeft(Integer.parseInt(inLeft.getValue()));
			} else {
				comp.setLeft(0);
			}
			// adicionar no último top
			comp.setTop(getNextTop(parent));
		}
		// registrar o filho no topLevel
		topLevel.registerChild(comp);
		// store
		topLevel.store();
		// preparar JavaScript
		script="parent.objInspFrame.location.href='?mognoOrigin=ObjectInspectorPage&selectComponent="+comp.name+"';\nparent.previewFrame.location.reload();\nparent.compTreeFrame.location.reload();\n";
		// resetar form
		inTop.setValue("");
		inLeft.setValue("");
		resetParentSelect();
		// mostrar página
		application.outHtml(show());
	}
	private void resetParentSelect() {
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			String childName = (String)e.nextElement();
			Visual child = (Visual)topLevel.getChild(childName);
			if("addCompParent".equals(child.getParent())) {
				freeChild(child.getName());
			}
		}
		createParentOptions(topLevel,"addCompParent");
	}
	
	private String createName(String className) {
		String firstName;
		String newName;
		Vector v = new Vector();
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			v.add(e.nextElement());
		}
		Input inName = (Input)getChild("addCompName");
		if ("".equals(inName.getValue())) {
			int split = className.lastIndexOf('.');
			firstName = className.substring(split+1);
		} else {
			firstName = inName.getValue();
		}
		newName = firstName;
		if (!newName.equals(inName.getValue()) || v.contains(newName)) {
			int i=1;
			while(newName.equals(firstName)) {
				if(v.contains(newName+i)) {
					i++;
				} else {
					newName = newName+i;
				}
			}
		}
		inName.setValue("");
		return newName;
	}
	private int getNextTop(String parent) {
		int i=0;
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			String childName = (String)e.nextElement();
			Visual child = (Visual)topLevel.getChild(childName);
			if(child.getParent().equals(parent)) {
				if(child.getTop() >= i) {
					i = child.getTop()+1;
				}
			}
		}
		return i;
	}
	private int getNextLeft(String parent, int top) {
		int i=0;
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			String childName = (String)e.nextElement();
			Visual child = (Visual)topLevel.getChild(childName);
			if(child.getParent().equals(parent) && child.getTop() == top) {
				if(child.getLeft() >= i) {
					i = child.getLeft()+1;
				}
			}
		}
		return i;
	}
	private void checkCollision(String parent, int top, int left) {
		Enumeration e = topLevel.listChilds();
		boolean collision = false;
		while (e.hasMoreElements()) {
			String childName = (String)e.nextElement();
			Visual child = (Visual)topLevel.getChild(childName);
			if(child.getParent().equals(parent)) {
				if(child.getTop() == top && child.getLeft() == left) {
					collision = true;
					break;
				}
			}
		}
		if (collision) {
			e = topLevel.listChilds();
			while (e.hasMoreElements()) {
				String childName = (String)e.nextElement();
				Visual child = (Visual)topLevel.getChild(childName);
				if (child.getParent().equals(parent) && child.getTop() == top && child.getLeft() >= left) {
					// empura pro lado
					child.setLeft(child.getLeft()+1);
				}
			}
		}
	}
	private void addCompRow(String parent, int top) {
		Enumeration e = topLevel.listChilds();
		while (e.hasMoreElements()) {
			String childName = (String)e.nextElement();
			Visual child = (Visual)topLevel.getChild(childName);
			if (child.getParent().equals(parent) && child.getTop() >= top) {
				// empura pra baixo
				child.setTop(child.getTop()+1);
			}
		}
	}
}
