package org.freecompany.redline;

import java.io.*;
import java.util.*;

public class Payload {

	protected List< File> files = new LinkedList< File>();

	public void add( File file) {
		files.add( file);
	}

	public List< File> getFiles() {
		return files;
	}
}
