package org.redline_rpm.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.TarFileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.redline_rpm.Builder;
import org.redline_rpm.changelog.ChangelogParseException;
import org.redline_rpm.header.Architecture;
import org.redline_rpm.header.Header;
import org.redline_rpm.header.Os;
import org.redline_rpm.header.RpmType;
import org.redline_rpm.payload.Directive;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.redline_rpm.Util.normalizePath;
import static org.redline_rpm.header.Architecture.NOARCH;
import static org.redline_rpm.header.Os.LINUX;
import static org.redline_rpm.header.RpmType.BINARY;

/**
 * Ant task for creating an RPM file.
 */
public class RedlineTask extends Task {
	public static final String NAMESPACE = "http://freecompany.org/namespace/redline";

	protected String name;
	protected String epoch = "0";
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
	protected List< EmptyDir> emptyDirs = new ArrayList< EmptyDir>();
	protected List< Ghost> ghosts = new ArrayList< Ghost>();
	protected List< Link> links = new ArrayList< Link>();
	protected List< Depends> depends = new ArrayList< Depends>();
	protected List< Provides> moreProvides = new ArrayList< Provides>();
	protected List< Conflicts> conflicts = new ArrayList< Conflicts>();
	protected List< Obsoletes> obsoletes = new ArrayList< Obsoletes>();

	protected List< TriggerPreIn> triggersPreIn = new ArrayList< TriggerPreIn>();
	protected List< TriggerIn> triggersIn = new ArrayList< TriggerIn>();
	protected List< TriggerUn> triggersUn = new ArrayList< TriggerUn>();
	protected List< TriggerPostUn> triggersPostUn = new ArrayList< TriggerPostUn>();
	
	protected List< BuiltIn> builtIns = new ArrayList< BuiltIn>();

	protected File preTransScript;
	protected File preInstallScript;
	protected File postInstallScript;
	protected File preUninstallScript;
	protected File postUninstallScript;
	protected File postTransScript;

    protected File privateKeyRingFile;
    protected String privateKeyId;
    protected String privateKeyPassphrase;
    
    protected File changeLog;

	public RedlineTask() {
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch ( UnknownHostException e) {
			host = "";
		}
	}

	@Override
	public void execute() {
		if ( name == null) throw new BuildException( "Attribute 'name' is required.");
		if ( version == null) throw new BuildException( "Attribute 'version' is required.");
		if ( group == null) throw new BuildException( "Attribute 'group' is required.");
		
		int numEpoch;
		try {
			numEpoch = Integer.parseInt( epoch);
		} catch(Exception e) {
			throw new IllegalArgumentException( "Epoch must be integer: " + epoch);
		}

		Builder builder = new Builder();
		builder.setPackage( name, version, release, numEpoch);
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
		if(provides != null) {
			builder.setProvides( provides);
		}
		builder.setPrefixes( prefixes == null ? null : prefixes.split(","));
        builder.setPrivateKeyRingFile( privateKeyRingFile);
        builder.setPrivateKeyId( privateKeyId);
        builder.setPrivateKeyPassphrase( privateKeyPassphrase);
		if (sourcePackage != null) {
			builder.addHeaderEntry(Header.HeaderTag.SOURCERPM, sourcePackage);
		}
		
		// add built-ins
		for ( BuiltIn builtIn : builtIns) {
			String text = builtIn.getDirectory();
			if (text != null && !text.trim().equals("")) {
				builder.addBuiltinDirectory( builtIn.getDirectory());
			}
		}

		try {
			if ( null != preTransScript) {
				builder.setPreTransScript( preTransScript);
			}
			if ( null != preInstallScript) {
				builder.setPreInstallScript( preInstallScript);
			}
			if ( null != postInstallScript) {
				builder.setPostInstallScript( postInstallScript);
			}
			if ( null != preUninstallScript) {
				builder.setPreUninstallScript( preUninstallScript);
			}
			if ( null != postUninstallScript) {
				builder.setPostUninstallScript( postUninstallScript);
			}
			if ( null != postTransScript) {
				builder.setPostTransScript( postTransScript);
			}
			if ( null != changeLog) {
				builder.addChangelogFile(changeLog);
			}

			for ( EmptyDir emptyDir : emptyDirs) {
				builder.addDirectory(emptyDir.getPath(), emptyDir.getDirmode(), Directive.NONE, emptyDir.getUsername(), emptyDir.getGroup(), true);
			}

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
                boolean addParents = true;

				if (fileset instanceof TarFileSet) {
					TarFileSet tarFileSet = (TarFileSet)fileset;
					username = tarFileSet.getUserName();
					group = tarFileSet.getGroup();
                    if (fileset instanceof RpmFileSet) {
                        RpmFileSet rpmFileSet = (RpmFileSet)fileset;
                        directive = rpmFileSet.getDirective();
                    }
				}

                if (fileset instanceof RpmFileSet) {
                    RpmFileSet rpmFileSet = (RpmFileSet) fileset;
                    addParents = rpmFileSet.getAddParents();
                }

				// include any directories, including empty ones, duplicates will be ignored when we scan included files
				for (String entry : scanner.getIncludedDirectories()) {
					String dir = normalizePath(prefix + entry);
					if (!"".equals(entry)) builder.addDirectory(dir, dirmode, directive, username, group, true);
				}

				for ( String entry : scanner.getIncludedFiles()) {
					if ( archive != null) {
						URL url = new URL( "jar:" + archive.toURI().toURL() + "!/" + entry);
						builder.addURL( prefix + entry, url, filemode, dirmode, directive, username, group);
					} else {
						File file = new File( scanner.getBasedir(), entry);
						builder.addFile(prefix + entry, file, filemode, dirmode, directive, username, group, addParents);
					}
				}
			}
			for ( Ghost ghost : ghosts) {
				builder.addFile( ghost.getPath(), null, ghost.getFilemode(), ghost.getDirmode(), ghost.getDirective(), ghost.getUsername(), ghost.getGroup());
			}
			for ( Link link : links) builder.addLink( link.getPath(), link.getTarget(), link.getPermissions());
			for ( Depends dependency : depends) builder.addDependency( dependency.getName(), dependency.getComparison(), dependency.getVersion());
			for ( Provides provision : moreProvides) builder.addProvides( provision.getName(), provision.getVersion());
			for ( Conflicts conflict : conflicts) builder.addConflicts( conflict.getName(), conflict.getComparison(), conflict.getVersion());
			for ( Obsoletes obsoletion : obsoletes) builder.addObsoletes( obsoletion.getName(), obsoletion.getComparison(), obsoletion.getVersion());

			for ( TriggerPreIn triggerPreIn : triggersPreIn) builder.addTrigger( triggerPreIn.getScript(), "", triggerPreIn.getDepends(), triggerPreIn.getFlag());
			for ( TriggerIn triggerIn : triggersIn) builder.addTrigger( triggerIn.getScript(), "", triggerIn.getDepends(), triggerIn.getFlag());
			for ( TriggerUn triggerUn : triggersUn) builder.addTrigger( triggerUn.getScript(), "", triggerUn.getDepends(), triggerUn.getFlag());
			for ( TriggerPostUn triggerPostUn : triggersPostUn) builder.addTrigger( triggerPostUn.getScript(), "", triggerPostUn.getDepends(), triggerPostUn.getFlag());

			log( "Created rpm: " + builder.build( destination));
		} catch ( IOException e) {
			throw new BuildException( "Error packaging distribution files.", e);
		} catch ( NoSuchAlgorithmException e) {
			throw new BuildException( "This system does not support MD5 digests.", e);
		} catch (ChangelogParseException e) {
			throw new BuildException( "Error parsing Changelog", e);
		}
	}

	public void restrict( String name) {
		for ( Iterator< Depends> i = depends.iterator(); i.hasNext();) {
			final Depends dependency = i.next();
			if ( dependency.getName().equals( name)) i.remove();
		}
	}

	public void setName( String name) { this.name = name; }
	public void setEpoch( String epoch) { this.epoch = epoch; }
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
	public void addGhost( Ghost ghost) { ghosts.add( ghost); }
	public void addEmptyDir( EmptyDir emptyDir) { emptyDirs.add( emptyDir); }
	public void addLink( Link link) { links.add( link); }
	public void addDepends( Depends dependency) { depends.add( dependency); }
	public void addProvides( Provides provision) { moreProvides.add( provision); }
	public void addConflicts( Conflicts conflict) { conflicts.add( conflict); }
	public void addObsoletes( Obsoletes obsoletion) { obsoletes.add( obsoletion); }
	public void addTriggerPreIn( TriggerPreIn triggerPreIn) { triggersPreIn.add( triggerPreIn); }
	public void addTriggerIn( TriggerIn triggerIn) { triggersIn.add( triggerIn); }
	public void addTriggerUn( TriggerUn triggerUn) { triggersUn.add( triggerUn); }
	public void addTriggerPostUn( TriggerPostUn triggerPostUn) { triggersPostUn.add( triggerPostUn); }
	public void setPreTransScript( File preTransScript) { this.preTransScript = preTransScript; }
	public void setPreInstallScript( File preInstallScript) { this.preInstallScript = preInstallScript; }
	public void setPostInstallScript( File postInstallScript) { this.postInstallScript = postInstallScript; }
	public void setPreUninstallScript( File preUninstallScript) { this.preUninstallScript = preUninstallScript; }
	public void setPostUninstallScript( File postUninstallScript) { this.postUninstallScript = postUninstallScript; }
	public void setPostTransScript( File postTransScript) { this.postTransScript = postTransScript; }
	public void setSourcePackage( String sourcePackage) { this.sourcePackage = sourcePackage; }
    public void setPrivateKeyRingFile( File privateKeyRingFile) { this.privateKeyRingFile = privateKeyRingFile; }
    public void setPrivateKeyId( String privateKeyId ) { this.privateKeyId = privateKeyId; }
    public void setPrivateKeyPassphrase( String privateKeyPassphrase ) { this.privateKeyPassphrase = privateKeyPassphrase; }
    public void addBuiltin( BuiltIn builtIn) { builtIns.add(builtIn); }
	public void setChangeLog(File changeLog) { this.changeLog = changeLog; }
	
}
