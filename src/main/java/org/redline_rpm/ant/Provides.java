package org.redline_rpm.ant;

/**
 * Object describing a provided capability (virtual package).
 */
public class Provides {

	protected String name;
	protected String version = "";
	protected int comparison = 0;

	public void setName( String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setVersion( String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

}
