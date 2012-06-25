package org.freecompany.redline.ant;

import org.apache.tools.ant.types.EnumeratedAttribute;

import static org.freecompany.redline.header.Flags.EQUAL;
import static org.freecompany.redline.header.Flags.GREATER;
import static org.freecompany.redline.header.Flags.LESS;

/**
 * Object describing a dependency on a
 * particular version of an RPM package.
 */
public class Depends {

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
		if ( comparisonValue.equals( "equal")) {
			this.comparison = EQUAL;
		} else if ( comparisonValue.equals( "greater")) {
			this.comparison = GREATER;
		} else if ( comparisonValue.equals( "greater|equal")) {
			this.comparison = GREATER | EQUAL;
		} else if ( comparisonValue.equals( "less")) {
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
	 * Enumerated attribute with the values "asis", "add" and "remove".
	 */
	public static class ComparisonEnum extends EnumeratedAttribute {
		public String[] getValues() {
			return new String[] {"equal", "greater", "greater|equal", "less", "less|equal"};
		}
	}
}
