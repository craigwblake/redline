package org.freecompany.redline.payload;

public enum Directive {

	DOC( 0x02);

	private final int flag;

	Directive( final int flag) {
		this.flag = flag;
	}

	public int flag() {
		return flag;
	}
}
