package org.redline_rpm.ant;

import org.apache.tools.zip.UnixStat;
import org.redline_rpm.payload.Directive;

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
    protected Directive directive = new Directive();

    public Ghost() {
        this.directive.set(Directive.RPMFILE_GHOST);
    }

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
    public Directive getDirective() {
        return this.directive;
    }

    public void setConfig(boolean config) {
        if(config) {
            this.directive.set(Directive.RPMFILE_CONFIG);
        } else {
            this.directive.unset(Directive.RPMFILE_CONFIG);
        }
    }
}
