package org.freecompany.redline;

import org.freecompany.redline.header.Architecture;
import org.freecompany.redline.header.Format;
import org.freecompany.redline.header.Os;
import org.freecompany.redline.header.RpmType;
import org.freecompany.redline.payload.Contents;
import org.freecompany.redline.payload.CpioHeader;
import org.freecompany.util.xml.editor.XmlEditor;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;

import static org.freecompany.redline.header.RpmType.*;
import static org.freecompany.redline.header.Architecture.*;
import static org.freecompany.redline.header.Os.*;

public class Main {

	public static void main( String[] args) throws SAXException, NoSuchAlgorithmException, IOException {
		if ( args.length < 1) System.out.println( "Usage: java Main <config> <[file]...>");
		else new Main().run( new File( args[ 0]));
	}

	public void run( File file) throws SAXException, NoSuchAlgorithmException, IOException {
		XmlEditor editor = new XmlEditor();
		editor.read( file);
		run( editor, new File( "."));
	}

	public void run( XmlEditor editor, File destination) throws NoSuchAlgorithmException, IOException {
		editor.startPrefixMapping( "http://www.freecompany.org/namespace/redline", "rpm");
		Contents include = new Contents();

		for ( Node files : editor.findNodes( "rpm:files")) {
			try {
				editor.pushContext( files);
				int permission = editor.getInteger( "@permission", CpioHeader.PERMISSION);
				String parent = editor.getValue( "@parent");
				if ( !parent.endsWith( "/")) parent += "/";
				for ( Node file : editor.findNodes( "rpm:file")) {
					try {
						editor.pushContext( file);
						File source = new File( editor.getValue( "text()"));
						include.addFile( new File( parent, source.getName()).getPath(), source, editor.getInteger( "@permission", permission));
					} finally { 
						editor.popContext();
					}
				}
			} finally {
				editor.popContext();
			}
		}
		
		run( editor, editor.getValue( "rpm:name/text()"), editor.getValue( "rpm:version/text()"), editor.getValue( "rpm:release/text()", "1"), include, destination);
	}

	public void run( XmlEditor editor, String name, String version, String release, Contents include, File destination) throws NoSuchAlgorithmException, IOException {
		Builder builder = new Builder();
		builder.setPackage( name, version, release);
		
		RpmType type = RpmType.valueOf( editor.getValue( "rpm:type", BINARY.toString()));
		builder.setType( type);
		
		Architecture arch = Architecture.valueOf( editor.getValue( "rpm:architecture", NOARCH.toString()));
		Os os = Os.valueOf( editor.getValue( "rpm:os", LINUX.toString()));
		builder.setPlatform( arch, os);
		
		builder.setSummary( editor.getValue( "rpm:summary/text()"));
		builder.setDescription( editor.getValue( "rpm:description/text()"));
		builder.setBuildHost( editor.getValue( "rpm:host/text()", InetAddress.getLocalHost().getHostName()));
		builder.setLicense( editor.getValue( "rpm:license/text()"));
		builder.setGroup( editor.getValue( "rpm:group/text()"));
		builder.setPackager( editor.getValue( "rpm:packager/text()", System.getProperty( "user.name")));
		builder.setVendor( editor.getValue( "rpm:vendor/text()", null));
		builder.setUrl( editor.getValue( "rpm:url/text()", null));
		builder.setProvides( editor.getValue( "rpm:provides/text()", name));
		builder.setFiles( include);
		builder.build( destination);
	}
}
