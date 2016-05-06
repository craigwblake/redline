package org.redline_rpm.changelog;


import java.util.Date;

/**
 * This class defines a Plain Old Java Object encapsulating 
 * one entry in a Changelog for example:
  * 
   * Wed Nov 08 2006 George Washington
   - Add the foo feature
     Add the bar feature
 *
 * Copyright (c) 2007-2016 FreeCompany 
 */
public class ChangelogEntry {
	/**
	 * The date portion of the Changelog Entry
	 * In the above Example: Wed Nov 08 2006 
	 */
	private Date changeLogTime;
	/**
	 * The "user" or "name" portion of the Changelog Entry
	 * In the above Example: George Washington
	 * in other words, the rest of the first line of the entry, 
	 * not counting the date portion 
	 */
	private String userMakingChange;
	/**
	 * Freeform text on the second line and beyond of the Changelog Entry
	 * In the above Example: 
	   - Add the foo feature
	     Add the bar feature
	 Terminates with a line beginning with an asterisk, which defines a new Changelog entry.  
	 */
	private String description;
	
	public ChangelogEntry() {
	}
	
	public ChangelogEntry(	Date changeLogTime,
							String userMakingChange,
							String description) 
	{
		this.changeLogTime = changeLogTime;
		this.userMakingChange = userMakingChange;
		this.description = description;
	}
	
	public boolean isComplete() {
		return changeLogTime != null && userMakingChange != null && description != null;
	}

	public Date getChangeLogTime() {
		return changeLogTime;
	}

	public void setChangeLogTime(Date changeLogTime) {
		this.changeLogTime = changeLogTime;
	}

	public String getUserMakingChange() {
		return userMakingChange;
	}

	public void setUserMakingChange(String userMakingChange) {
		this.userMakingChange = userMakingChange;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
