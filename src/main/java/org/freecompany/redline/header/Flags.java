package org.freecompany.redline.header;

public class Flags {

	public static int LESS = 0x02;
	public static int GREATER = 0x04;
	public static int EQUAL = 0x08;
	public static int SCRIPT_POSTTRANS = 0x20;
	public static int PREREQ = 0x40;
	public static int SCRIPT_PRETRANS = 0x80;
	public static int INTERP = 0x100;
	public static int SCRIPT_PRE = 0x200;
	public static int SCRIPT_POST = 0x400;
	public static int SCRIPT_PREUN = 0x800;
	public static int SCRIPT_POSTUN = 0x1000;
	public static int SCRIPT_TRIGGERIN = 0x10000;
	public static int SCRIPT_TRIGGERUN = 0x20000;
	public static int SCRIPT_TRIGGERPOSTUN = 0x40000;
	public static int SCRIPT_TRIGGERPREIN = 0x2000000;
	public static int RPMLIB = (0x1000000 | PREREQ);

}
