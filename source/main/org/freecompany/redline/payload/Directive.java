package org.freecompany.redline.payload;

public enum Directive {

	NONE( 0x0),
	CONFIG( 0x01),
	DOC( 0x02);

	private final int flag;

	Directive( final int flag) {
		this.flag = flag;
	}

	public int flag() {
		return flag;
	}
}
