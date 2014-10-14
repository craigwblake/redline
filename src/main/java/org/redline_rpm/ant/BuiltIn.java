package org.redline_rpm.ant;

import org.apache.tools.ant.Project;

public class BuiltIn {
	
	private final Project project;
	
	private String name;
	
	public BuiltIn( Project project) {
		this.project = project;
	}
	
	public void addText( String text) {
		this.name = project.replaceProperties(text);
	}
	
	public String getText() {
		return name;
	}

}
