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
		NAME( 1000, 6, "name"),
		VERSION( 1001, 6, "version"),
		RELEASE( 1002, 6, "release"),
		SUMMARY( 1004, 9, "summary"),
		DESCRIPTION( 1005, 9, "description"),
		BUILDTIME( 1006, 4, "buildtime"),
		BUILDHOST( 1007, 6, "buildhost"),
		SIZE( 1009, 4, "size"),
		LICENSE( 1014, 6, "license"),
		GROUP( 1016, 9, "group"),
		OS( 1021, 6, "os"),
		ARCH( 1022, 6, "arch"),
		SOURCERPM( 1044, 6, "sourcerpm"),
		FILEVERIFYFLAGS( 1045, 6, "fileverifyflags"),
		ARCHIVESIZE( 1046, 6, "archivesize"),
		RPMVERSION( 1064, 6, "rpmversion"),
		CHANGELOGTIME( 1080, 6, "changelogtime"),
		CHANGELOGNAME( 1081, 6, "changelogname"),
		CHANGELOGTEXT( 1082, 6, "changelogtext"),
		COOKIE( 1094, 6, "cookie"),
		OPTFLAGS( 1122, 6, "optflags"),
		PAYLOADFORMAT( 1124, 6, "payloadformat"),
		PAYLOADCOMPRESSOR( 1125, 6, "payloadcompressor"),
		PAYLOADFLAGS( 1126, 6, "payloadflags"),
		RHNPLATFORM( 1131, 6, "rhnplatform"),
		PLATFORM( 1132, 6, "platform"),

		HEADERSIGNATURES( 62, 6, "headersignatures"),
		HEADERIMMUTABLE( 63, 6, "headerimmutable"),
		HEADERI18NTABLE( 100, 6, "headeri18ntable"),

		PREINPROG( 1085, 6, "preinprog"),
		POSTINPROG( 1086, 6, "postinprog"),
		PREUNPROG( 1087, 6, "preunprog"),
		POSTUNPROG( 1088, 6, "postunprog"),

		OLDFILENAMES( 1027, 6, "oldfilenames"),
		FILESIZES( 1028, 4, "filesizes"),
		FILEMODES( 1030, 3, "filemodes"),
		FILERDEVS( 1033, 3, "filerdevs"),
		FILEMTIMES( 1034, 4, "filemtimes"),
		FILEMD5S( 1035, 8, "filemd5s"),
		FILELINKTOS( 1036, 8, "filelinktos"),
		FILEFLAGS( 1037, 4, "fileflags"),
		FILEUSERNAME( 1039, 8, "fileusername"),
		FILEGROUPNAME( 1040, 8, "filegroupname"),
		FILEDEVICES( 1095, 4, "filedevices"),
		FILEINODES( 1096, 4, "fileinodes"),
		FILELANGS( 1097, 8, "filelangs"),
		DIRINDEXES( 1116, 6, "dirindexes"),
		BASENAMES( 1117, 8, "basenames"),
		DIRNAMES( 1118, 8, "dirnames"),

		PROVIDENAME( 1047, 6, "providename"),
		REQUIREFLAGS( 1048, 4, "requireflags"),
		REQUIRENAME( 1049, 6, "requirename"),
		REQUIREVERSION( 1050, 6, "requireversion"),
		CONFLICTFLAGS( 1053, 6, "conflictflags"),
		CONFLICTNAME( 1054, 6, "conflictname"),
		CONFLICTVERSION( 1055, 6, "conflictversion"),
		OBSOLETENAME( 1090, 6, "obsoletename"),
		PROVIDEFLAGS( 1112, 4, "provideflags"),
		PROVIDEVERSION( 1113, 6, "provideversion"),
		OBSOLETEFLAGS( 1114, 6, "obsoleteflags"),
		OBSOLETEVERSION( 1115, 6, "obsoleteversion");

		private int code;
		private int type;
		private String name;

		private HeaderTag( final int code, final int type, final String name) {
			this.code = code;
			this.type = type;
			this.name = name;
		}

		public int getCode() { return code; }
		public int getType() { return type; }
		public String getName() { return name; }
	}
}
