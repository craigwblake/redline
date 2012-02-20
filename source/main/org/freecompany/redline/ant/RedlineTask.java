package org.freecompany.redline.ant;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.freecompany.redline.Builder;
import org.freecompany.redline.header.Architecture;
import org.freecompany.redline.header.Header;
import org.freecompany.redline.header.Os;
import org.freecompany.redline.header.RpmType;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.TarFileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.freecompany.redline.payload.Directive;

import static org.freecompany.redline.Util.normalizePath;
import static org.freecompany.redline.header.Architecture.NOARCH;
import static org.freecompany.redline.header.Os.LINUX;
import static org.freecompany.redline.header.RpmType.BINARY;

/**
 * Ant task for creating an RPM file.
 */
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
	protected String sourcePackage = null;
	protected String provides;
	protected String prefixes;
	protected RpmType type = BINARY;
	protected Architecture architecture = NOARCH;
	protected Os os = LINUX;
	protected File destination;
	protected List< ArchiveFileSet> filesets = new ArrayList< ArchiveFileSet>();
	protected List< Link> links = new ArrayList< Link>();
	protected List< Depends> depends = new ArrayList< Depends>();
	protected File preInstallScript;
	protected File postInstallScript;
	protected File preUninstallScript;
	protected File postUninstallScript;

	public RedlineTask() {
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch ( UnknownHostException e) {
			host = "";
		}
	}

	@Override
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
		builder.setPrefixes( prefixes == null ? null : prefixes.split(","));
		if (sourcePackage != null) {
			builder.addHeaderEntry(Header.HeaderTag.SOURCERPM, sourcePackage);
		}

		try {
			builder.setPreInstallScript( preInstallScript);
			builder.setPostInstallScript( postInstallScript);
			builder.setPreUninstallScript( preUninstallScript);
			builder.setPostUninstallScript( postUninstallScript);

			for ( ArchiveFileSet fileset : filesets) {
				File archive = fileset.getSrc( getProject());
				String prefix = normalizePath( fileset.getPrefix( getProject()));
				if ( !prefix.endsWith( "/")) prefix += "/";
				DirectoryScanner scanner = fileset.getDirectoryScanner( getProject());

                int filemode = fileset.getFileMode( getProject()) & 07777;
				int dirmode = fileset.getDirMode( getProject()) & 07777;
				String username = null;
				String group = null;
                Directive directive = null;

				if (fileset instanceof TarFileSet) {
					TarFileSet tarFileSet = (TarFileSet)fileset;
					username = tarFileSet.getUserName();
					group = tarFileSet.getGroup();
                    if (fileset instanceof RpmFileSet) {
                        RpmFileSet rpmFileSet = (RpmFileSet)fileset;
                        directive = rpmFileSet.getDirective();
                    }
				}

				// include any directories, including empty ones, duplicates will be ignored when we scan included files
				for (String entry : scanner.getIncludedDirectories()) {
					String dir = normalizePath(prefix + entry);
					if (!entry.equals("")) builder.addDirectory(dir, dirmode, directive, username, group, true);
				}

				for ( String entry : scanner.getIncludedFiles()) {
					if ( archive != null) {
						URL url = new URL( "jar:" + archive.toURL() + "!/" + entry);
						builder.addURL( prefix + entry, url, filemode, dirmode, directive, username, group);
					} else {
						File file = new File( scanner.getBasedir(), entry);
						builder.addFile(prefix + entry, file, filemode, dirmode, directive, username, group);
					}
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

	public void restrict( String name) {
		for ( Iterator< Depends> i = depends.iterator(); i.hasNext();) {
			final Depends dependency = i.next();
			if ( dependency.getName().equals( name)) i.remove();
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
	public void setPrefixes( String prefixes) { this.prefixes = prefixes; }
	public void setDestination( File destination) { this.destination = destination; }
	public void addZipfileset( ZipFileSet fileset) { filesets.add( fileset); }
	public void addTarfileset( TarFileSet fileset) { filesets.add( fileset); }
    public void addRpmfileset( RpmFileSet fileset) { filesets.add( fileset); }
	public void addLink( Link link) { links.add( link); }
	public void addDepends( Depends dependency) { depends.add( dependency); }
	public void setPreInstallScript( File preInstallScript) { this.preInstallScript = preInstallScript; }
	public void setPostInstallScript( File postInstallScript) { this.postInstallScript = postInstallScript; }
	public void setPreUninstallScript( File preUninstallScript) { this.preUninstallScript = preUninstallScript; }
	public void setPostUninstallScript( File postUninstallScript) { this.postUninstallScript = postUninstallScript; }
	public void setSourcePackage( String sourcePackage) { this.sourcePackage = sourcePackage; }
}
