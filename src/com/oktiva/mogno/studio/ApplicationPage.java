/*
 * ApplicationPage.java
 *
 * Created on 20 de Fevereiro de 2003, 02:27
 */
package com.oktiva.mogno.studio;

import com.oktiva.mogno.Application;
import com.oktiva.mogno.Component;
import com.oktiva.mogno.Visual;
import com.oktiva.mogno.html.H1;
import com.oktiva.mogno.html.Input;
import com.oktiva.mogno.html.Option;
import com.oktiva.mogno.html.P;
import com.oktiva.mogno.html.Radiogroup;
import com.oktiva.mogno.html.Select;
import com.oktiva.mogno.html.Td;
import com.oktiva.mogno.html.Tr;
import com.oktiva.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ApplicationPage extends MognoStudioPage {
	static Logger logger = Logger.getLogger(ApplicationPage.class.getName());
	String[] classes = null;
	Application app = new Application();
	public void onCreatePage(ApplicationPage page)
	throws Exception {
		HttpSession session = application.getSession();
		String appName = (String)session.getAttribute("application");
		String workDir = (String)session.getAttribute("workDir");
		String bar = File.separator;
		// obter application
		app.setRootDir(workDir+bar+appName);
		app.setDesigning(true);
		app.initialize("MognoApplication.xml");
		// criar linhas para toplevels
		createTableRows();
	}
	public void onShowPage(ApplicationPage page)
	throws Exception {
		logger.debug("onShowPage: "+page.name);
		HttpSession session = application.getSession();
		// preparar header
		String appName = (String)session.getAttribute("application");
		H1 h1 = (H1)getChild("h1");
		h1.setContent(h1.getContent()+" - "+appName);
	}
	private void createTableRows()
	throws IOException {
		Hashtable topLevels = app.getTopLevelsData();
		String defaultTopLevel = app.getDefaultTopLevel();
		Object[] keys = topLevels.keySet().toArray();
		Arrays.sort(keys);
		for (int i=0; i<keys.length; i++) {
			String name = (String)keys[i];
			String classe = (String)((Hashtable)topLevels.get(name)).get("class");
			String xml = (String)((Hashtable)topLevels.get(name)).get("xml");
			createTopLevelRow(name, classe, xml, i+2, defaultTopLevel);			
		}
	}
	
	public void createTopLevelRow(String name, String classe, String xml, int top, String defaultTopLevel)
	throws IOException {
		Vector v = new Vector();
		//		td1 - topLevelName*	Input
		Input in = new Input();
		in.setStyle("width:100%");
		in.setName("topLevelName_"+name);
		in.setType("text");
		in.setValue(name);
		v.add(in);
		//		td2 - topLevelClass*	Select
		v.add(getSelectClass("topLevelClass_"+name,classe));
		//		td3 - xmlFileName*	Input
		in = new Input();
		in.setStyle("width:100%");
		in.setName("xmlFileName_"+name);
		in.setType("text");
		in.setValue(xml);
		v.add(in);
		//		td4 - topLevelDefault*	Input radio
		in = new Input();
		in.setName("topLevelDefault_"+name);
		in.setValue("topLevelDefault_"+name);
		in.setType("radio");
		in.setRadiogroup("defaultTopLevel");
		if (name.equals(defaultTopLevel)) {
			Radiogroup r = (Radiogroup)getChild("defaultTopLevel");
			r.setValue("topLevelDefault_"+name);
		}
		v.add(in);
		//		td5 - topLevelStatus*	P
		P p = new P();
		p.setName("topLevelStatus_"+name);
		p.setContent(getTopLevelStatus(xml));
		v.add(p);
		//		td6 - editTopLevel*	Input submit
		in = new Input();
		in.setName("editTopLevel_"+name);
		in.setType("submit");
		in.setValue("Edit");
		in.setEvOnClick("onClickEdit");
		v.add(in);
		//		td7 - removeTopLevel*	Input submit
		in = new Input();
		in.setName("removeTopLevel_"+name);
		in.setType("submit");
		in.setValue("Remove");
		in.setEvOnClick("onClickRemove");
		v.add(in);
		createTableRow(name,top,"table",v);
	}
	
	private void createTableRow(String topLevelName, int top, String parent, Vector childs) {
		String rowName = "tr_"+topLevelName;
		// criar tr
		Tr tr = new Tr();
		tr.setName(rowName);
		tr.setTop(top);
		tr.setLeft(0);
		tr.setParent(parent);
		registerChild(tr);
		// para cada child
		for (int i=0; i<childs.size(); i++) {
			// criar td
			Td td = new Td();
			td.setName(rowName+"_td"+i);
			td.setTop(0);
			td.setLeft(i);
			td.setParent(rowName);
			registerChild(td);
			// registrar child
			Visual child = (Visual)childs.get(i);
			child.setParent(td.getName());
			registerChild(child);
			logger.debug("Registered Visual '"+child.getName()+"' in Td '"+td.getName()+"'");
		}
	}
	private Select getSelectClass(String name, String selected)
	throws IOException {
		Select sel = new Select();
		sel.setName(name);
		sel.getValues().add(selected);
		sel.setStyle("width:100%");
		if (classes == null) {
			getTopLevelClasses();
		}
		boolean found = false;
		int i=0;
		while (i<classes.length) {
			if (classes[i] == null || classes[i].equals("")) {
				continue;
			}
			Option o = new Option();
			o.setName(name+"_"+classes[i]);
			o.setContent(classes[i]);
			o.setValue(classes[i]);
			o.setParent(name);
			o.setSelect(name);
			o.setId(name);
			o.setTop(i);
			registerChild(o);
			if (classes[i].equals(selected)) {
				found = true;
			}
			i++;
		}
		if (!found) {
			Option o = new Option();
			o.setName(name+"_"+selected);
			o.setContent(selected);
			o.setValue(selected);
			o.setParent(name);
			o.setSelect(name);
			o.setTop(i);
			registerChild(o);
		}
		return sel;
	}
	private void getTopLevelClasses()
	throws IOException {
		String bar = File.separator;
		String file = application.getRootDir()+bar+"WEB-INF"+bar+"config"+bar+"topLevelClasses.txt";
		classes = FileUtil.readFileAsArray(file);
	}
	private String getTopLevelStatus(String xml) {
		String bar = File.separator;
		String filePath = app.getRootDir()+bar+"WEB-INF"+bar+"xml"+bar+xml;
		File xmlFile = new File(filePath);
		if (xmlFile.exists()) {
			return "Created";
		} else {
			return "Not Created";
		}
	}
	public void onClickAdd(Input button)
	throws Exception {
		String pageName = "page";
		int i = 1;
		while (pageName.equals("page")) {
			if (!app.getTopLevelsData().containsKey(pageName+i)) {
				pageName = pageName+i;
			}
			i++;
		}
		app.addTopLevel(pageName, "com.oktiva.mogno.html.Page",pageName+".xml");
		createTableRows();
		app.store();
		application.outHtml(show());
	}
	public void onClickEdit(Input button)
	throws Exception {
		String topLevelName = button.getName();
		topLevelName = topLevelName.replaceAll("editTopLevel_","");
		HttpSession session = application.getSession();
		session.setAttribute("topLevel", topLevelName);
		String xml = app.getTopLevelXmlFileName(topLevelName);
		if(getTopLevelStatus(xml).equals("Not Created")) {
			session.setAttribute("selectedComponent", topLevelName);
			createTopLevelXml(app.getRootDir(), topLevelName, xml);
		}
		String selComp = (String)session.getAttribute("selectedComponent");
		if(selComp == null || app.getTopLevel(topLevelName).getChild(selComp) == null) {
			session.setAttribute("selectedComponent", topLevelName);
		}
		showPage("TopLevelPage");
	}
	public void onClickRemove(Input button)
	throws Exception {
		String topLevelName = button.getName();
		topLevelName = topLevelName.replaceAll("removeTopLevel_","");
		if (topLevelName.equals(app.getDefaultTopLevel())) {
			P p = (P)getChild("error");
			p.setContent(p.getContent()+"Can't remove the default top level.");
		} else {
			// Remover o TopLevel da aplicação
			app.removeTopLevel(topLevelName);
			app.store();
			// Remover os inputs e o p do estado
			removeInputs(topLevelName);
			// Remover os tds e o tr
			removeTr(topLevelName);
		}
		application.outHtml(show());
	}
	private void removeInputs(String name) {
		freeChild("topLevelName_"+name);
		freeSelectAndOptions("topLevelClass_"+name);
		freeChild("xmlFileName_"+name);
		freeChild("topLevelDefault_"+name);
		freeChild("topLevelStatus_"+name);
		freeChild("editTopLevel_"+name);
		freeChild("removeTopLevel_"+name);
	}
	private void freeSelectAndOptions(String name) {
		Enumeration e = listChilds();
		while(e.hasMoreElements()) {
			String childName = (String)e.nextElement();
			Visual child = (Visual)getChild(childName);
			if(child instanceof Option && ((Option)child).select.equals(name)) {
				freeChild(childName);
			}
		}
		freeChild(name);
	}
	private void removeTr(String name) {
		freeChild("tr_"+name);
		freeChild("tr_"+name+"_td0");
		freeChild("tr_"+name+"_td1");
		freeChild("tr_"+name+"_td2");
		freeChild("tr_"+name+"_td3");
		freeChild("tr_"+name+"_td4");
		freeChild("tr_"+name+"_td5");
		freeChild("tr_"+name+"_td6");
	}
	public void onClickSave(Input button)
	throws Exception {
		Hashtable data = app.getTopLevelsData();
		Enumeration e = data.keys();
		while(e.hasMoreElements()) {
			String oldName = (String)e.nextElement();
			Input in = (Input)getChild("topLevelName_"+oldName);
			if (in.value == null || in.value.equals("")) {
				in.styleClass="error";
				P p = (P)getChild("error");
				p.setContent("The top level name can't be empty.");
				application.outHtml(show());
				return;
			}
		}
		e = data.keys();
		while(e.hasMoreElements()) {
			String topLevelName = (String)e.nextElement();
			// Ajustar XML
			Input in = (Input)getChild("xmlFileName_"+topLevelName);
			if(in.value == null || in.value.equals("")) {
				in.setValue(((Input)getChild("topLevelName_"+topLevelName)).value+".xml");
			}
			((Hashtable)data.get(topLevelName)).put("xml",in.value);
			// Ajustar Classe
			Select sel = (Select)getChild("topLevelClass_"+topLevelName);
			((Hashtable)data.get(topLevelName)).put("class",sel.getValues().get(0));
		}
		// Ajustar default top level
		Radiogroup rg = (Radiogroup)getChild("defaultTopLevel");
		String defaultName = rg.getValue().replaceAll("topLevelDefault_","");
		app.setDefaultTopLevel(defaultName);
		// Ajustar nome
		e = data.keys();
		while(e.hasMoreElements()) {
			String oldName = (String)e.nextElement();
			Input in = (Input)getChild("topLevelName_"+oldName);
			String newName = in.getValue();
			if(oldName.equals(newName)) {
				continue;
			}
			// Alterar o nome no hash de toplevels da aplicação
			String classe = (String)((Hashtable)data.get(oldName)).get("class");
			String xml = (String)((Hashtable)data.get(oldName)).get("xml");
			app.removeTopLevel(oldName);
			app.addTopLevel(newName, classe, xml);
			// Ajustar o top level default
			if(app.getDefaultTopLevel().equals(oldName)) {
				app.setDefaultTopLevel(newName);
			}
			renameComponents(oldName, newName);
		}
		// Salvar
		app.store();
		application.outHtml(show());
	}
	private void renameComponents(String oldName, String newName)
	throws IOException {
		logger.debug("Renaming components from '"+oldName+"' to '"+newName+"'");
		// Alterar os nomes dos componentes desta página
		String[] comps = {"topLevelName_","xmlFileName_","topLevelDefault_",
		"topLevelStatus_","editTopLevel_","removeTopLevel_"};
		for (int i=0; i<comps.length; i++) {
			Component child = getChild(comps[i]+oldName);
			child.setName(comps[i]+newName);
			freeChild(comps[i]+oldName);
			registerChild(child);
		}
		renameSelect("topLevelClass_"+oldName, "topLevelClass_"+newName);
	}
	private void renameSelect(String oldName, String newName)
	throws IOException {
		logger.debug("Renaming select from '"+oldName+"' to '"+newName+"'");
		if (classes == null) {
			getTopLevelClasses();
		}
		for(int i=0; i<classes.length; i++) {
			if (classes[i] == null || classes[i].equals("")) {
				continue;
			}
			freeChild(oldName+"_"+classes[i]);
		}
		Select oldSel = (Select)getChild(oldName);
		String selected = (String)oldSel.getValues().get(0);
		String parent = oldSel.getParent();
		freeChild(oldName);
		Select sel = getSelectClass(newName, selected);
		sel.setParent(parent);
		registerChild(sel);
	}
	public void onClickChangeApp(Input in)
	throws Exception {
		HttpSession session = application.getSession();
		session.removeAttribute("application");
		session.removeAttribute("topLevel");
		session.removeAttribute("selectedComponent");
		showPage("StartPage");
	}
	public void onClickViewAppXml(Input in)
	throws Exception {
		showPage("ViewAppXmlPage");
	}
}
