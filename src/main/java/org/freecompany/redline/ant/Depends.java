package org.freecompany.redline.ant;

/**
 * Object describing a dependency on a
 * particular version of an RPM package.
 */
public class Depends {

	protected String name;
	protected String version;

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
