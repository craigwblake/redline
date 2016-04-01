package com.att.voicetone.gradle.plugins.rpm.changelog;

import static java.lang.Math.toIntExact;
import static org.redline_rpm.header.Header.HeaderTag.CHANGELOGNAME;
import static org.redline_rpm.header.Header.HeaderTag.CHANGELOGTEXT;
import static org.redline_rpm.header.Header.HeaderTag.CHANGELOGTIME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.time.ZoneId;
import java.util.List;

import org.redline_rpm.Builder;

public class ChangelogHandler {
	private final Builder builder;
	public ChangelogHandler(Builder builder) {
		this.builder = builder;
	}
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
		int unixdate = ((int) epochMillis/1000);
		builder.addHeaderEntry(CHANGELOGTIME, unixdate);
		builder.addHeaderEntry(CHANGELOGNAME, entry.getUserMakingChange());
		builder.addHeaderEntry(CHANGELOGTEXT, entry.getDescription());
	}
}
