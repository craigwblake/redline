package org.freecompany.redline;

/**
 * A 96 byte RPM file lead section, which specifies information such as the name
 * of the package.  This section is obsolete and now used primarily to identify
 * the file type.
 */
public class Lead {

	protected int major;
	protected int minor;
	protected Type type;
	protected Architecture architecture;
	protected CharSequence name;

	public void setMajor( int major) {
		this.major = major;
	}

	public int getMajor() {
		return major;
	}

	public void setMinor( int minor) {
		this.minor = minor;
	}

	public int getMinor() {
		return minor;
	}

	public void setType( Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setArchitecture( Architecture architecture) {
		this.architecture = architecture;
	}

	public Architecture getArchitecture() {
		return architecture;
	}

	public void setName( CharSequence name) {
		this.name = name;
	}

	public CharSequence getName() {
		return name;
	}
}
