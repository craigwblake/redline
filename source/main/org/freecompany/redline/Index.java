package org.freecompany.redline;

public class Index< T> {

	protected T tag;
	protected Type type;
	protected int offset;
	protected int count;

	public void setTag( T tag) {
		this.tag = tag;
	}

	public T getTag() {
		return tag;
	}

	public void setType( Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setOffset( int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public void setCount( int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}
}
