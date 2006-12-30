package org.freecompany.redline.ant;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.freecompany.redline.Builder;
import org.freecompany.redline.header.Architecture;
import org.freecompany.redline.header.Os;
import org.freecompany.redline.header.RpmType;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.ZipFileSet;

import static org.freecompany.redline.header.Architecture.NOARCH;
import static org.freecompany.redline.header.Os.LINUX;
import static org.freecompany.redline.header.RpmType.BINARY;

public class RedlineTask extends Task {

	public static final String NAMESPACE = "http://freecompany.org/namespace/redline";

	protected String name;
	protected String version;
	protected String group;
	protected String release = "1";
	protected String host;
	protected String summary = "";
	protected String description = "";
	protected String license = "";
	protected String packager = System.getProperty( "user.name", "");
	protected String distribution = "";
	protected String vendor = "";
	protected String url = "";
	protected String provides;
	protected RpmType type = BINARY;
	protected Architecture architecture = NOARCH;
	protected Os os = LINUX;
	protected File destination;
	protected List< ZipFileSet> filesets = new ArrayList< ZipFileSet>();
	protected List< Link> links = new ArrayList< Link>();
	protected List< Depends> depends = new ArrayList< Depends>();

	public RedlineTask() {
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch ( UnknownHostException e) {
			host = "";
		}
	}

	public void execute() throws BuildException {
		if ( name == null) throw new BuildException( "Attribute 'name' is required.");
		if ( version == null) throw new BuildException( "Attribute 'version' is required.");
		if ( group == null) throw new BuildException( "Attribute 'group' is required.");

		Builder builder = new Builder();
		builder.setPackage( name, version, release);
		builder.setType( type);
		builder.setPlatform( architecture, os);
		builder.setGroup( group);
		builder.setBuildHost( host);
		builder.setSummary( summary);
		builder.setDescription( description);
		builder.setLicense( license);
		builder.setPackager( packager);
		builder.setDistribution( distribution);
		builder.setVendor( vendor);
		builder.setUrl( url);
		builder.setProvides( provides == null ? name : provides);

		try {
			for ( ZipFileSet fileset : filesets) {
				String prefix = fileset.getPrefix( getProject());
				if ( !prefix.endsWith( "/")) prefix += "/";
				DirectoryScanner scanner = fileset.getDirectoryScanner( getProject());
				
				for ( String entry : scanner.getIncludedFiles()) {
					File file = new File( scanner.getBasedir(), entry);
					builder.addFile( prefix + file.getName(), file, fileset.getFileMode( getProject()));
				}

			}
			for ( Link link : links) builder.addLink( link.getPath(), link.getTarget(), link.getPermissions());
			for ( Depends dependency : depends) builder.addDependencyMore( dependency.getName(), dependency.getVersion());
			
			log( "Created rpm: " + builder.build( destination));
		} catch ( IOException e) {
			throw new BuildException( "Error packaging distribution files.", e);
		} catch ( NoSuchAlgorithmException e) {
			throw new BuildException( "This system does not support MD5 digests.", e);
		}
	}

	public void setName( String name) { this.name = name; }
	public void setType( String type) { this.type = RpmType.valueOf( type); }
	public void setArchitecture( String architecture) { this.architecture = Architecture.valueOf( architecture); }
	public void setOs( String os) { this.os = Os.valueOf( os); }
	public void setVersion( String version) { this.version = version; }
	public void setRelease( String release) { this.release = release; }
	public void setGroup( String group) { this.group = group; }
	public void setHost( String host) { this.host = host; }
	public void setSummary( String summary) { this.summary = summary; }
	public void setDescription( String description) { this.description = description; }
	public void setLicense( String license) { this.license = license; }
	public void setPackager( String packager) { this.packager = packager; }
	public void setDistribution( String distribution) { this.distribution = distribution; }
	public void setVendor( String vendor) { this.vendor = vendor; }
	public void setUrl( String url) { this.url = url; }
	public void setProvides( String provides) { this.provides = provides; }
	public void setDestination( File destination) { this.destination = destination; }
	public void addZipfileset( ZipFileSet fileset) { filesets.add( fileset); }
	public void addLink( Link link) { links.add( link); }
	public void addDepends( Depends dependency) { depends.add( dependency); }
}
