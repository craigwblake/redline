package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;

public class Builder {

	protected Format format = new Format();
	protected Set< String> dirnames = new HashSet< String>();
	protected Set< String> filenames = new HashSet< String>();
	protected Map< String, File> files = new HashMap< String, File>();

	public Builder() {
		format.getHeader().addEntry( BUILDTIME, ( int) ( System.currentTimeMillis() / 1000));
		format.getHeader().addEntry( RPMVERSION, "4.4.2");
		format.getHeader().addEntry( PAYLOADFORMAT, "cpio");
		format.getHeader().addEntry( PAYLOADCOMPRESSOR, "gzip");
	}

	public void setPackage( CharSequence name, CharSequence version, CharSequence release) {
		format.getLead().setName( name + "-" + version + "-" + release);
		format.getHeader().addEntry( NAME, name);
		format.getHeader().addEntry( VERSION, version);
		format.getHeader().addEntry( RELEASE, release);
	}
	
	public void setBinary() {
		format.getLead().setType( 0);
	}
	
	public void setSource() {
		format.getLead().setType( 1);
	}

	public void setArch( Architecture arch) {
		format.getLead().setArch( arch);
		format.getHeader().addEntry( ARCH, os.toString().toLowerCase());
	}

	public void setOs( Os os) {
		format.getLead().setOs( os);
		format.getHeader().addEntry( OS, os.toString().toLowerCase());
	}

	public void setSummary( CharSequence summary) {
		format.getHeader().addEntry( SUMMARY, summary);
	}

	public void setDescription( CharSequence description) {
		format.getHeader().addEntry( DESCRIPTION, description);
	}

	public void setBuildHost( CharSequence host) {
		format.getHeader().addEntry( BUILDHOST, host);
	}

	public void setLicense( CharSequence license) {
		format.getHeader().addEntry( LICENSE, license);
	}

	public void setGroup( CharSequence group) {
		format.getHeader().addEntry( GROUP, group);
	}

	/**
	 * Add the specified files to the repository payload in the provided
	 * order.  The required header entries will automatically be generated
	 * to record the directory names and file names, as well as their
	 * digests.
	 *
	 * @param target the absolute path at which to install this file.
	 * @param file the file content to include in this rpm.
	 */
	public void addFile( CharSequence target, File file) {
		File file = new File( target);
		dirnames.add( file.getParent());
		filenames.add( file.getName());
		files.put( file.getCanonicalPath(), file);
	}

	public void build() {
		format.getHeader().addEntry( DIRNAMES, dirnames.toArray( new String[ dirnames.size()]));
		format.getHeader().addEntry( BASENAMES, filenames.toArray( new String[ filenames.size()]));

		Entry< byte[]> gpg = format.getSignature().addEntry( GPG, 65);
		Entry< byte[]> dsa = format.getSignature().addEntry( DSAHEADER, 65);
		Entry< String[]> sha = format.getSignature().addEntry( SHA1HEADER, 1);
		sha.setSize( 41);

		
	}
}
