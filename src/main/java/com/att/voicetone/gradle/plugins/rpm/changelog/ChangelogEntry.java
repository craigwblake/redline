package com.att.voicetone.gradle.plugins.rpm.changelog;


import java.util.Date;

public class ChangelogEntry {
	private Date changeLogTime;
	private String userMakingChange;
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
