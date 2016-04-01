package com.att.voicetone.gradle.plugins.rpm.changelog

import spock.lang.Specification

class ChangelogParserSpec extends Specification
implements ParserExceptionClient {

	def "parses correctly formatted changelog"() {
		when:
		String[] lines = [
			"* Tue Feb 24 2015 Steve Cohen <sc1478@att.com>",
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua",
			"* Tue Feb 10 2015 Steve Cohen <sc1478@att.com>",
			"quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
		]
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(lines)

		then:
		changelogs.size() == 2
	}	
	
	def "error thrown if dates out of order"() {
		when:
		String[] lines = [
			"* Tue Feb 10 2015 Steve Cohen <sc1478@att.com>",
			"quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
			"* Tue Feb 24 2015 Steve Cohen <sc1478@att.com>",
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		]
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(lines)

		then:
		def e=thrown(DatesOutOfSequenceException)
		e.message == OUT_OF_SEQUENCE
	}
	
	def "error thrown on wrong date format"() {
		when:
		String[] lines = [
			"* 2/24/2015 Steve Cohen <sc1478@att.com>",
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		]
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(lines)
		
		then:
		def e=thrown(InvalidChangelogDateException)
		e.message.startsWith(INVALID_DATE)
	}
		
	def "error thrown on incorrect date"() {
		when:
		// 2/24/2015 was a Tuesday
		String[] lines = [
			"* Wed Feb 24 2015 Steve Cohen <sc1478@att.com>",
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		]
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(lines)
		
		then:
		def e=thrown(InvalidChangelogDateException)
		e.message.startsWith(INVALID_DATE)
	}
	
	def "error thrown on no description"() {
		when:
		String[] lines = [
			"* Tue Feb 24 2015 Steve Cohen <sc1478@att.com>",
			"* Tue Feb 10 2015 Steve Cohen <sc1478@att.com>",
		]
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(lines)
		then:
		
		def e=thrown(IncompleteChangelogEntryException)
		e.message.equals(INCOMPLETE_ENTRY)

	}
	
	def "error thrown on no initial asterisk"() {
		when:
		String[] lines = [
			"Tue Feb 24 2015 Steve Cohen <sc1478@att.com>",
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		]
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(lines)
		then:
		
		def e=thrown(NoInitialAsteriskException)
		e.message.equals(MUST_START_WITH_ASTERISK)

	}
	def "error thrown on no user name"() {
		when:
		String[] lines = [
			"* Tue Feb 24 2015",
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		]
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(lines)
		then:
		
		def e=thrown(IncompleteChangelogEntryException)
		e.message.equals(INCOMPLETE_ENTRY)
	}

	def "error thrown on no user name on first line"() {
		when:
		String[] lines = [
			"* Tue Feb 24 2015",
			"Steve Cohen <sc1478@att.com",
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
		]
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(lines)
		then:
		
		def e=thrown(IncompleteChangelogEntryException)
		e.message.equals(INCOMPLETE_ENTRY)
	}
	

	def "parses file correctly"() {
		when:
		ChangelogParser parser = new ChangelogParser()
		List<ChangelogEntry> changelogs = parser.parse(this.class.getResourceAsStream("changelog"))
		
		then:
		changelogs.size() == 10
		
	}
		


}
