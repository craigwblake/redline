package org.freecompany.redline.ant;

/**
 * Object describing a symbolic link
 * to be generated on the target machine
 * during installation of the RPM file.
 */
public class Link {

	protected String path;
	protected String target;
	protected int permissions = -1;

	public void setPath( String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setTarget( String target) {
		this.target = target;
	}

	public String getTarget() {
		return target;
	}

	public void setPermissions( int permissions) {
		this.permissions = permissions;
	}

	public int getPermissions() {
		return permissions;
	}
}
