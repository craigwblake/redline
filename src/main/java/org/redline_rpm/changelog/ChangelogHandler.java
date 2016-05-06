package org.redline_rpm.changelog;

import static org.redline_rpm.header.Header.HeaderTag.CHANGELOGNAME;
import static org.redline_rpm.header.Header.HeaderTag.CHANGELOGTEXT;
import static org.redline_rpm.header.Header.HeaderTag.CHANGELOGTIME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.redline_rpm.header.Header;

/**
 * This class manages the process of adding a Changelog to the header.
 * 
 * Copyright (c) 2007-2016 FreeCompany 
 */
public class ChangelogHandler {
	private final Header header;
	public ChangelogHandler(Header header) {
		this.header = header;
	}
	/**
	 * Adds the specified changelog file to the rpm
	 * @param changelogFile the changelog file to be added
	 * @throws IOException if the specified file cannot be read
	 * @throws ChangelogParseException if the file violates the requirements of a Changelog
	 */
	public void addChangeLog(File changelogFile) throws IOException, ChangelogParseException {
		// parse the change log to a list of entries
		InputStream changelog = new FileInputStream(changelogFile);
		ChangelogParser parser = new ChangelogParser();
		List<ChangelogEntry> entries = parser.parse(changelog);
		for (ChangelogEntry entry : entries) {
			addChangeLogEntry(entry);
		}
	}
	
	private void addChangeLogEntry( ChangelogEntry entry) {
		long epochMillis = entry.getChangeLogTime().getTime();
		long epochSecs = epochMillis/1000L; // seconds since the epoch
		int unixdate = (int) epochSecs; 
		
		header.addOrAppendEntry(CHANGELOGTIME, new int[] {unixdate});
		header.addOrAppendEntry(CHANGELOGNAME, new String[] {entry.getUserMakingChange()});
		header.addOrAppendEntry(CHANGELOGTEXT, new String[] {entry.getDescription()});

	}
}
