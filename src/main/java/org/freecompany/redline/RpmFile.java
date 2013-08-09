package org.freecompany.redline;

import java.io.File;

import org.freecompany.redline.payload.Directive;

/**
 * Representation of File for rpm Builder with metadata
 * 
 */
public class RpmFile {
	
	
	public final static int PERMISIONS_NOT_SET=-1;
	
	
	final File source;
	String destination=null;
	int filePermissions=PERMISIONS_NOT_SET;
	int dirPermissions=PERMISIONS_NOT_SET;
	Directive directive=null;
	String uname=null;
	String gname=null;
	boolean addParents=true;

	public RpmFile(File file) {
		source=file;
		destination=file.getAbsolutePath();
	}

	public boolean isAddParents() {
		return addParents;
	}

	public RpmFile setAddParents(boolean addParents) {
		this.addParents = addParents;
		return this;
	}

	public RpmFile setDirective(Directive directive) {
		this.directive = directive;
		return this;
	}

	public RpmFile setGroupName(String gname) {
		this.gname = gname;
		return this;
	}

	public RpmFile setDestination(String destination) {
		this.destination = destination;
		return this;
	}

	public RpmFile setFilePermissions(int permissions) {
		this.filePermissions = permissions;
		return this;
	}
	public RpmFile setDirPermissions(int dirPermissions) {
		this.dirPermissions = dirPermissions;
		return this;
	}

	public RpmFile setUserName(String uname) {
		this.uname = uname;
		return this;
	}

	/**
	 * if destination is not set than it is same as source - default behavior
	 * @return
	 */
	public String getDestination() {
		return destination;
	}

	public int getFilePermissions() {
		return filePermissions;
	}
	public int getDirPermissions() {
		return dirPermissions;
	}

	public Directive getDirective() {
		return directive;
	}

	public String getUname() {
		return uname;
	}

	public String getGroupName() {
		return gname;
	}

	public File getSource() {
		return source;
	}



}
