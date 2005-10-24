package org.freecompany.redline.header;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Header extends AbstractHeader {

	public Header() {
		for ( HeaderTag tag : HeaderTag.values()) tags.put( tag.getCode(), tag);
	}

	public enum HeaderTag implements Tag {
		NAME( 1000, "name"),
		VERSION( 1001, "version"),
		RELEASE( 1002, "release"),
		SUMMARY( 1004, "summary"),
		DESCRIPTION( 1005, "description"),
		BUILDTIME( 1006, "buildtime"),
		BUILDHOST( 1007, "buildhost"),
		SIZE( 1009, "size"),
		LICENSE( 1014, "license"),
		GROUP( 1016, "group"),
		OS( 1021, "os"),
		ARCH( 1022, "arch"),
		SOURCERPM( 1044, "sourcerpm"),
		FILEVERIFYFLAGS( 1045, "fileverifyflags"),
		ARCHIVESIZE( 1046, "archivesize"),
		RPMVERSION( 1064, "rpmversion"),
		CHANGELOGTIME( 1080, "changelogtime"),
		CHANGELOGNAME( 1081, "changelogname"),
		CHANGELOGTEXT( 1082, "changelogtext"),
		COOKIE( 1094, "cookie"),
		OPTFLAGS( 1122, "optflags"),
		PAYLOADFORMAT( 1124, "payloadformat"),
		PAYLOADCOMPRESSOR( 1125, "payloadcompressor"),
		PAYLOADFLAGS( 1126, "payloadflags"),
		RHNPLATFORM( 1131, "rhnplatform"),
		PLATFORM( 1132, "platform"),

		HEADERSIGNATURES( 62, "headersignatures"),
		HEADERIMMUTABLE( 63, "headerimmutable"),
		HEADERI18NTABLE( 100, "headeri18ntable"),

		PREINPROG( 1085, "preinprog"),
		POSTINPROG( 1086, "postinprog"),
		PREUNPROG( 1087, "preunprog"),
		POSTUNPROG( 1088, "postunprog"),

		OLDFILENAMES( 1027, "oldfilenames"),
		FILESIZES( 1028, "filesizes"),
		FILEMODES( 1030, "filemodes"),
		FILERDEVS( 1033, "filerdevs"),
		FILEMTIMES( 1034, "filemtimes"),
		FILEMD5S( 1035, "filemd5s"),
		FILELINKTOS( 1036, "filelinktos"),
		FILEFLAGS( 1037, "fileflags"),
		FILEUSERNAME( 1039, "fileusername"),
		FILEGROUPNAME( 1040, "filegroupname"),
		FILEDEVICES( 1095, "filedevices"),
		FILEINODES( 1096, "fileinodes"),
		FILELANGS( 1097, "filelangs"),
		DIRINDEXES( 1116, "dirindexes"),
		BASENAMES( 1117, "basenames"),
		DIRNAMES( 1118, "dirnames"),

		PROVIDENAME( 1047, "providename"),
		REQUIREFLAGS( 1048, "requireflags"),
		REQUIRENAME( 1049, "requirename"),
		REQUIREVERSION( 1050, "requireversion"),
		CONFLICTFLAGS( 1053, "conflictflags"),
		CONFLICTNAME( 1054, "conflictname"),
		CONFLICTVERSION( 1055, "conflictversion"),
		OBSOLETENAME( 1090, "obsoletename"),
		PROVIDEFLAGS( 1112, "provideflags"),
		PROVIDEVERSION( 1113, "provideversion"),
		OBSOLETEFLAGS( 1114, "obsoleteflags"),
		OBSOLETEVERSION( 1115, "obsoleteversion");

		private int code;
		private String name;

		private HeaderTag( final int code, final String name) {
			this.code = code;
			this.name = name;
		}

		public int getCode() { return code; }
		public String getName() { return name; }
	}
}
