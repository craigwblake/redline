package org.redline_rpm.changelog;

import static org.redline_rpm.changelog.ParsingState.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * This object reads the Changelog file and attempts to parse its
 * contents into a List of ChangeLogEntry objects
 *
 */
class ChangelogParser {
	static final SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH);
	/**
	 * @param lines an array of lines read from the Changelog file
	 * @return a List of ChangeLogEntry objects
	 * @throws ChangelogParseException if any of the rules of a Changelog is 
	 * violated by the input
	 */
	public List<ChangelogEntry> parse(String[] lines) throws ChangelogParseException {
		fmt.setLenient(false);
		final int TIME_LEN = 15;
		List<ChangelogEntry> result = new LinkedList<ChangelogEntry>();
		if (lines.length == 0) {
			return result;
		}
		
		ParsingState state = NEW;
		Date lastTime = null;
		ChangelogEntry entry = new ChangelogEntry();
		String restOfLine = null;
		StringBuilder descr = new StringBuilder();
		int index = 0;
		String line = lines[index];
lineloop:
		while (true) {
			switch (state) {
			case NEW:
				if (line.startsWith("#")) {
					if (++index < lines.length) {
						line = lines[index];
						continue;
					} else {
						return result;
					}
				} else if (!line.startsWith("*")) {
					throw new NoInitialAsteriskException();
				}
				restOfLine = line.substring(1).trim();
				state = TIME;
				break;
			case TIME:
				if (restOfLine.length() < TIME_LEN) {
					throw new InvalidChangelogDateException(restOfLine);
				}
				String timestr = restOfLine.substring(0,TIME_LEN);
				try {
					Date entryTime = fmt.parse(timestr);
					if (lastTime != null && lastTime.before(entryTime)) {
						throw new DatesOutOfSequenceException();
					}
					entry.setChangeLogTime(entryTime);
					lastTime = entryTime;
				} catch (ParseException e) {
					throw new InvalidChangelogDateException(e.getMessage());
				}
				state = NAME;
				break;
			case NAME:
				String name = restOfLine.substring(TIME_LEN).trim();
				if (name.length() > 0) {
					entry.setUserMakingChange(name);
				}	
				state = TEXT;
				break;
			case TEXT:
				index++;
				if (index < lines.length) {
					line = lines[index];
					if (line.startsWith("#")) {
						continue;
					} else if (line.startsWith("*")) {
						// a new entry begins
						if (descr.length() > 1) {
							entry.setDescription(descr.toString().substring(0, descr.length() - 1));
						}	
						if (entry.isComplete()) {
							result.add(entry);
							entry = new ChangelogEntry();
							descr = new StringBuilder();
							state = NEW;
						} else {
							throw new IncompleteChangelogEntryException();
						}
					} else {
						descr.append(line).append('\n');
					}
				} else {
					break lineloop;
				}	
			}
		}
		if (descr.length() > 1) {
			entry.setDescription(descr.toString().substring(0, descr.length() - 1));
		}	
		if (entry.isComplete()) {
			result.add(entry);
		} else if (lines.length > 0){
			throw new IncompleteChangelogEntryException();
		}

		return result;
	}
	
	/**
	 * @param stream stream read from the Changelog file
	 * @return a List of ChangeLogEntry objects
	 * @throws IOException if the input stream cannot be read
	 * @throws ChangelogParseException if any of the rules of a Changelog is 
	 * violated by the input
	 */
	public List<ChangelogEntry> parse(InputStream stream) throws IOException, ChangelogParseException 
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		List<String> lines = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith("#")) {
				lines.add(line);
			}		
		}
		return parse(lines.toArray(new String[0]));
	}
}
