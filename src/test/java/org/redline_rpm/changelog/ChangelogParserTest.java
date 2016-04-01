/**
 * 
 */
package org.redline_rpm.changelog;

import org.junit.Assert;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author SC1478
 *
 */
public class ChangelogParserTest 
implements ParserExceptionClient
{
	
	ChangelogParser parser;
	List<ChangelogEntry> changelogs;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parser = new ChangelogParser();
		changelogs = null;
	}

	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void test_parses_correctly_formatted_changelog() {
		String[] lines = {
		      			"* Tue Feb 24 2015 George Washington",
		      			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua",
		      			"* Tue Feb 10 2015 George Washington",
		      			"quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
		};

		try {
			changelogs = parser.parse(lines);
			Assert.assertEquals("parses correctly formatted changelog", 2, changelogs.size());
		} catch (ChangelogParseException e) {
			Assert.fail("parses correctly formatted changelog");
		}
	}

	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void error_thrown_if_dates_out_of_order() {
		String[] lines = {
				"* Tue Feb 10 2015 George Washington",
				"quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
				"* Tue Feb 24 2015 George Washington",
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		};

		try {
			changelogs = parser.parse(lines);
			Assert.fail("error thrown if dates out of order");
		} catch (ChangelogParseException e) {
			Assert.assertTrue(e instanceof DatesOutOfSequenceException);
			Assert.assertEquals("error thrown if dates out of order", OUT_OF_SEQUENCE, e.getMessage());
		}
	}

	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void error_thrown_on_wrong_date_format() {
		// 2/24/2015 was a Tuesday
		String[] lines = {
				"* 02/24/2015 George Washington",
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		};

		try {
			changelogs = parser.parse(lines);
			Assert.fail("error thrown on wrong date format");
		} catch (ChangelogParseException e) {
			Assert.assertTrue(e instanceof InvalidChangelogDateException);
			Assert.assertTrue("error thrown on wrong date format", e.getMessage().startsWith(INVALID_DATE));
		}
	}

	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void error_thrown_on_incorrect_day_of_week() {
		// 2/24/2015 was a Tuesday
		String[] lines = {
				"* Wed Feb 24 2015 George Washington",
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		};

		try {
			changelogs = parser.parse(lines);
			Assert.fail("error thrown on incorrect day of week");
		} catch (ChangelogParseException e) {
			Assert.assertTrue(e instanceof InvalidChangelogDateException);
			Assert.assertTrue("error thrown on incorrect day of week", e.getMessage().startsWith(INVALID_DATE));
		}
	}

	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void error_thrown_on_no_description() {
		String[] lines = {
				"* Tue Feb 24 2015 George Washington",
				"* Tue Feb 10 2015 George Washington",
		};

		try {
			changelogs = parser.parse(lines);
			Assert.fail("error thrown on no description");
		} catch (ChangelogParseException e) {
			Assert.assertTrue(e instanceof IncompleteChangelogEntryException);
			Assert.assertEquals("error thrown on no description", INCOMPLETE_ENTRY, e.getMessage());
		}
	}

	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void error_thrown_on_no_initial_asterisk() {
		String[] lines = {
				"Tue Feb 24 2015 George Washington",
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		};

		try {
			changelogs = parser.parse(lines);
			Assert.fail("error thrown on no initial asterisk");
		} catch (ChangelogParseException e) {
			Assert.assertTrue(e instanceof NoInitialAsteriskException);
			Assert.assertEquals("error thrown on no initial asterisk", MUST_START_WITH_ASTERISK, e.getMessage());
		}
	}

	
	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void error_thrown_on_no_user_name() {
		String[] lines = {
				"* Tue Feb 24 2015",
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		};

		try {
			changelogs = parser.parse(lines);
			Assert.fail("error thrown on no user name");
		} catch (ChangelogParseException e) {
			Assert.assertTrue(e instanceof IncompleteChangelogEntryException);
			Assert.assertEquals("error thrown on no user name", INCOMPLETE_ENTRY, e.getMessage());
		}
	}
	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void error_thrown_on_no_user_name_on_first_line() {
		String[] lines = {
				"* Tue Feb 24 2015",
				"George Washington",
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		};

		try {
			changelogs = parser.parse(lines);
			Assert.fail("error thrown on no user name on first line");
		} catch (ChangelogParseException e) {
			Assert.assertTrue(e instanceof IncompleteChangelogEntryException);
			Assert.assertEquals("error thrown on no user name on first line", INCOMPLETE_ENTRY, e.getMessage());
		}
	}

	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.io.InputStream)}.
	 */
	@Test
	public void parses_file_correctly() {
		try {
			changelogs = parser.parse(this.getClass().getResourceAsStream("changelog"));
			Assert.assertEquals("parses file correctly", 10, changelogs.size());
		} catch (ChangelogParseException e) {
			Assert.fail("parses file correctly");
		} catch (IOException e) {
			Assert.fail("parses file correctly: " + e.getMessage());
		}

	}

}
