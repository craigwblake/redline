package org.redline_rpm.ant;

import org.apache.tools.zip.UnixStat;

/**
 * Object describing a %ghost file
 * to be added to the rpm without the
 * file needing to exist beforehand.
 */
public class Ghost {

    protected String path;
    protected String username;
    protected String group;
    protected int filemode = -1;
    protected int dirmode = -1;

    public String getPath() {
        return this.path;
    }
    public void setPath( String path) {
        this.path = path;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername( String username) {
        this.username = username;
    }
    public String getGroup() {
        return this.group;
    }
    public void setGroup( String group) {
        this.group = group;
    }
    public int getFilemode() {
        return this.filemode;
    }
    public void setFilemode( String filemode) {
        this.filemode = UnixStat.FILE_FLAG | Integer.parseInt(filemode, 8);
    }
    public int getDirmode() {
        return this.dirmode;
    }
    public void setDirmode( String dirmode) {
        this.dirmode = UnixStat.DIR_FLAG | Integer.parseInt(dirmode, 8);
    }
}
