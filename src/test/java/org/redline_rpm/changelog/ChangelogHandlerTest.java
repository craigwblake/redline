package org.redline_rpm.changelog;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.redline_rpm.Builder;

public class ChangelogHandlerTest {
	Builder builder;

	@Before
	public void setUp() throws Exception {
		builder = new Builder();
		
	}

	@Test
	public void testAddChangeLog() {
		try {
			builder.addChangelogFile(new File("non.existent.file"));
			fail("non-existent file throws FileNotFoundException: not thrown");
		} catch (IOException e) {
			assertTrue("non-existent file throws FileNotFoundException", e instanceof FileNotFoundException);
		} catch (ChangelogParseException e) {
			fail("non-existent file throws FileNotFoundException: ChangelogParseException thrown instead");
		}

	}
	@Test
	public void testBadChangeLog() throws URISyntaxException {
		try {
			builder.addChangelogFile(new File(this.getClass().getResource("bad.changelog").toURI()));
			fail("bad changelog file throws ChangelogParseException: not thrown");
		} catch (IOException e) {
			fail("bad changelog file throws ChangelogParseException: IOException thrown instead");
		} catch (ChangelogParseException e) {
			assertTrue("bad changelog file throws ChangelogParseException", e instanceof NoInitialAsteriskException);
		}
	}
	/**
	 * Test method for {@link org.redline_rpm.changelog.ChangelogParser#parse(java.lang.String[])}.
	 */
	@Test
	public void comments_ignored() throws URISyntaxException {
		try {
			builder.addChangelogFile(new File(this.getClass().getResource("changelog.with.comments").toURI()));
		} catch (IOException e) {
			fail("comments_ignored: IOException thrown instead");
		} catch (ChangelogParseException e) {
			fail("comments_ignored: failed");
		}
	}


}
