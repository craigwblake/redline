package org.freecompany.redline.header;

public class Flags {

	public static int LESS = 0x02;
	public static int GREATER = 0x04;
	public static int EQUAL = 0x08;
	public static int PREREQ = 0x40;
	public static int INTERP = 0x100;
	public static int SCRIPT_PRE = 0x200;
	public static int SCRIPT_POST = 0x400;
	public static int SCRIPT_PRERUN = 0x800;
	public static int SCRIPT_POSTRUN = 0x1000;
}
