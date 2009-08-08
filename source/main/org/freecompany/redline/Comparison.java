package org.freecompany.redline;

import java.util.*;

public class Comparison  {

	/**
	 * Creates a case sensitive comparator for use in collections.
	 */
	public static final Comparator comparator() {
		return comparator( true);
	}

	/**
	 * Creates a comparator for use in collections that is case senstive only if the
	 * provided flag is set to true.
	 */
	public static final Comparator comparator( final boolean sensitive) {
		return new Comparator() {
			public int compare( Object one, Object two) {
				return Comparison.compare(( CharSequence) one, ( CharSequence) two, sensitive);
			}
		};
	}

	/**
	 * Compares two chars for equality sensitive to case.
	 */
	public static final boolean equals( final char one, final char two) {
		return equals( one, two, true);
	}

	/**
	 * Compares two chars for equality sensitive to case only if the provided flag is true.
	 */
	public static final boolean equals( final char one, final char two, final boolean sensitive) {
		return compare( one, two, sensitive) == 0;
	}

	/**
	 * Compares two sequences for equality sensitive to case.
	 */
	public static final boolean equals( final CharSequence one, final CharSequence two) {
		return equals( one, two, true);
	}

	/**
	 * Compares two sequences for equality sensitive to case only if the provided flag is true.
	 */
	public static final boolean equals( final CharSequence one, final CharSequence two, final boolean sensitive) {
		return compare( one, two, sensitive) == 0;
	}

	/**
	 * Compares two sequences for equality sensitive to case.
	 */
	public static final int compare( final CharSequence one, final CharSequence two) {
		return compare( one, two, true);
	}

	/**
	 * Compares two sequences for equality sensitive to case only if the provided flag is true.
	 */
	public static final int compare( final CharSequence one, final CharSequence two, final boolean sensitive) {
		if ( one == null) return two == null ? 0 : -1;
		if ( two == null) return 1;
		final int length = one.length();
		if ( length != two.length()) return one.length() - two.length();
		int comparison;
		for ( int x = 0; x < length; x++) if (( comparison = compare( one.charAt( x), two.charAt( x), sensitive)) != 0) return comparison;
		return 0;
	}
	
	/**
	 * Compares two chars for equality sensitive to case.
	 */
	public static final int compare( final char one, final char two) {
		return compare( one, two, true);
	}
	
	/**
	 * Compares two chars for equality sensitive to case only if the provided flag is true.
	 */
	public static final int compare( final char one, final char two, final boolean sensitive) {
		if ( !sensitive) return Character.toLowerCase( one) - Character.toLowerCase( two);
		return one - two;
	}
}
