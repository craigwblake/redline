package org.redline_rpm.payload;

import java.io.Serializable;

/**
 * Constants taken from {@code lib/rpmlib.h}
 *
 * @see <a href="http://www.rpm.org/api/4.4.2.2/rpmlib_8h-source.html#l00473">rpm.org</a>
 */
public class Directive implements Serializable {
    public final static int RPMFILE_NONE         = 0;
    public final static int RPMFILE_CONFIG       = (1);
    public final static int RPMFILE_DOC          = (1 <<  1);
    public final static int RPMFILE_ICON         = (1 <<  2);
    public final static int RPMFILE_MISSINGOK    = (1 <<  3);
    public final static int RPMFILE_NOREPLACE    = (1 <<  4);
    public final static int RPMFILE_SPECFILE     = (1 <<  5);
    public final static int RPMFILE_GHOST        = (1 <<  6);
    public final static int RPMFILE_LICENSE      = (1 <<  7);
    public final static int RPMFILE_README       = (1 <<  8);
    public final static int RPMFILE_EXCLUDE      = (1 <<  9);
    public final static int RPMFILE_UNPATCHED    = (1 <<  10);
    public final static int RPMFILE_PUBKEY       = (1 <<  11);
    public final static int RPMFILE_POLICY       = (1 <<  12);

    public static final Directive NONE        = new Directive(RPMFILE_NONE);
    public static final Directive CONFIG      = new Directive(RPMFILE_CONFIG);
    public static final Directive DOC         = new Directive(RPMFILE_DOC);
    public static final Directive ICON        = new Directive(RPMFILE_ICON);
    public static final Directive MISSINGOK   = new Directive(RPMFILE_MISSINGOK);
    public static final Directive NOREPLACE   = new Directive(RPMFILE_NOREPLACE);
    public static final Directive SPECFILE    = new Directive(RPMFILE_SPECFILE);
    public static final Directive GHOST       = new Directive(RPMFILE_GHOST);
    public static final Directive LICENSE     = new Directive(RPMFILE_LICENSE);
    public static final Directive README      = new Directive(RPMFILE_README);
    public static final Directive EXCLUDE     = new Directive(RPMFILE_EXCLUDE);
    public static final Directive UNPATCHED   = new Directive(RPMFILE_UNPATCHED);
    public static final Directive PUBKEY      = new Directive(RPMFILE_PUBKEY);
    public static final Directive POLICY      = new Directive(RPMFILE_POLICY);

	private int flag;

    public Directive() {
    }

	public Directive( final int flag) {
		this.flag = flag;
	}

	public int flag() {
		return flag;
	}

    public void set(int val) {
        flag |= val;
    }

    public void unset(int val) {
        if((flag & val) > 0) {
            flag &= ~val;
        }
    }
}
