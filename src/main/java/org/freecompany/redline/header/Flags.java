package org.freecompany.redline.header;

public interface Flags {
	/**
	 * compare flags
0 0x0
1  0x1
2 < 0x2
3 < 0x3
4 > 0x4
5 > 0x5
6 <> 0x6
7 <> 0x7
8 = 0x10
9 = 0x11
10 <= 0x12
11 <= 0x13
12 >= 0x14
13 >= 0x15
14 <>= 0x16
15 <>= 0x17
16 0x20
17  0x21
18 < 0x22
19 < 0x23
	 */

	int LESS = 0x02;
	int GREATER = 0x04;
	int EQUAL = 0x08;
	int LESS_OR_EQUAL= 0x0a;
	int GREATER_OR_EQUAL = 0x0c;
	int SCRIPT_POSTTRANS = 0x20;
	int PREREQ = 0x40;
	int SCRIPT_PRETRANS = 0x80;
	int INTERP = 0x100;
	int SCRIPT_PRE = 0x200;
	int SCRIPT_POST = 0x400;
	int SCRIPT_PREUN = 0x800;
	int SCRIPT_POSTUN = 0x1000;
	int SCRIPT_TRIGGERIN = 0x10000;
	int SCRIPT_TRIGGERUN = 0x20000;
	int SCRIPT_TRIGGERPOSTUN = 0x40000;
	int SCRIPT_TRIGGERPREIN = 0x2000000;
	int RPMLIB = (0x1000000 | PREREQ);
}
