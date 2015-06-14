package org.redline_rpm.ant;

import org.apache.tools.ant.types.EnumeratedAttribute;

import static org.redline_rpm.header.Flags.EQUAL;
import static org.redline_rpm.header.Flags.GREATER;
import static org.redline_rpm.header.Flags.LESS;

/**
 * Object describing a conflict with a
 * particular version of an RPM package.
 */
public class Conflicts {

	protected String name;
	protected String version = "";
	protected int comparison = 0;

	public void setName( String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setComparison( ComparisonEnum comparisonEnum) {
		String comparisonValue = comparisonEnum.getValue();
		if ("equal".equals(comparisonValue)) {
			this.comparison = EQUAL;
		} else if ("greater".equals(comparisonValue)) {
			this.comparison = GREATER;
		} else if ("greater|equal".equals(comparisonValue)) {
			this.comparison = GREATER | EQUAL;
		} else if ("less".equals(comparisonValue)) {
			this.comparison = LESS;
		} else { // must be ( comparisonValue.equals( "less|equal"))
			this.comparison = LESS | EQUAL;
		}
	}

	public int getComparison() {
		if ( 0 == comparison && 0 < version.length()) {
			return GREATER | EQUAL;
		}
		if ( 0 == version.length()) {
			return 0;
		}
		return this.comparison;
	}

	public void setVersion( String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	/**
	 * Enumerated attribute with the values "equal", "greater", "greater|equal", "less" and "less|equal".
	 */
	public static class ComparisonEnum extends EnumeratedAttribute {
		public String[] getValues() {
			return new String[] {"equal", "greater", "greater|equal", "less", "less|equal"};
		}
	}
}
