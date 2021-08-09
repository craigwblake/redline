package org.redline_rpm.header;

public class Header extends AbstractHeader {

	public Header() {
		for ( HeaderTag tag : HeaderTag.values()) tags.put( tag.getCode(), tag);
	}

	protected boolean pad() { return false; }

	public enum HeaderTag implements Tag {
		NAME( 1000, STRING_ENTRY, "name"),
		VERSION( 1001, STRING_ENTRY, "version"),
		RELEASE( 1002, STRING_ENTRY, "release"),
		EPOCH( 1003, INT32_ENTRY, "epoch"),
		SUMMARY( 1004, I18NSTRING_ENTRY, "summary"),
		DESCRIPTION( 1005, I18NSTRING_ENTRY, "description"),
		BUILDTIME( 1006, INT32_ENTRY, "buildtime"),
		BUILDHOST( 1007, STRING_ENTRY, "buildhost"),
		SIZE( 1009, INT32_ENTRY, "size"),
		DISTRIBUTION( 1010, STRING_ENTRY, "distribution"),
		VENDOR( 1011, STRING_ENTRY, "vendor"),
		LICENSE( 1014, STRING_ENTRY, "license"),
		PACKAGER( 1015, STRING_ENTRY, "packager"),
		GROUP( 1016, I18NSTRING_ENTRY, "group"),
		CHANGELOG( 1017, STRING_ARRAY_ENTRY, "changelog"), //
		URL( 1020, STRING_ENTRY, "url"),
		OS( 1021, STRING_ENTRY, "os"),
		ARCH( 1022, STRING_ENTRY, "arch"),
		SOURCERPM( 1044, STRING_ENTRY, "sourcerpm"),
		FILEVERIFYFLAGS( 1045, INT32_ENTRY, "fileverifyflags"),
		ARCHIVESIZE( 1046, INT32_ENTRY, "archivesize"),
		RPMVERSION( 1064, STRING_ENTRY, "rpmversion"),
		CHANGELOGTIME( 1080, INT32_ENTRY, "changelogtime"),
		CHANGELOGNAME( 1081, STRING_ARRAY_ENTRY, "changelogname"),
		CHANGELOGTEXT( 1082, STRING_ARRAY_ENTRY, "changelogtext"),
		COOKIE( 1094, STRING_ENTRY, "cookie"),
		OPTFLAGS( 1122, STRING_ENTRY, "optflags"),
		PAYLOADFORMAT( 1124, STRING_ENTRY, "payloadformat"),
		PAYLOADCOMPRESSOR( 1125, STRING_ENTRY, "payloadcompressor"),
		PAYLOADFLAGS( 1126, STRING_ENTRY, "payloadflags"),
		RHNPLATFORM( 1131, STRING_ENTRY, "rhnplatform"),
		PLATFORM( 1132, STRING_ENTRY, "platform"),
		FILECOLORS( 1140, INT32_ENTRY, "filecolors"),
		FILECLASS( 1141, INT32_ENTRY, "fileclass"),
		CLASSDICT( 1142, STRING_ARRAY_ENTRY, "classdict"),
		FILEDEPENDSX( 1143, INT32_ENTRY, "filedependsx"),
		FILEDEPENDSN( 1144, INT32_ENTRY, "filedependsn"),
		DEPENDSDICT( 1145, INT32_ENTRY, "dependsdict"),
		SOURCEPKGID( 1146, BIN_ENTRY, "sourcepkgid"),
		FILECONTEXTS( 1147, STRING_ARRAY_ENTRY, "filecontexts"),

		HEADERIMMUTABLE( 63, BIN_ENTRY, "headerimmutable"),
		HEADERI18NTABLE( 100, STRING_ARRAY_ENTRY, "headeri18ntable"),

		PREINSCRIPT( 1023, STRING_ENTRY, "prein"),
		POSTINSCRIPT( 1024, STRING_ENTRY, "postin"),
		PREUNSCRIPT( 1025, STRING_ENTRY, "preun"),
		POSTUNSCRIPT( 1026, STRING_ENTRY, "postun"),
		PREINPROG( 1085, STRING_ENTRY, "preinprog"),
		POSTINPROG( 1086, STRING_ENTRY, "postinprog"),
		PREUNPROG( 1087, STRING_ENTRY, "preunprog"),
		POSTUNPROG( 1088, STRING_ENTRY, "postunprog"),

		PRETRANSSCRIPT( 1151, STRING_ENTRY, "pretrans"),
		POSTTRANSSCRIPT( 1152, STRING_ENTRY, "posttrans"),
		PRETRANSPROG( 1153, STRING_ENTRY, "pretransprog"),
		POSTTRANSPROG( 1154, STRING_ENTRY, "pretransprog"),

		TRIGGERSCRIPTS( 1065, STRING_ARRAY_ENTRY, "triggerscripts"),
		TRIGGERNAME( 1066, STRING_ARRAY_ENTRY, "triggername"),
		TRIGGERVERSION( 1067, STRING_ARRAY_ENTRY, "triggerversion"),
		TRIGGERFLAGS( 1068, INT32_ENTRY, "triggerflags"),
		TRIGGERINDEX( 1069, INT32_ENTRY, "triggerindex"),
		TRIGGERSCRIPTPROG( 1092, STRING_ARRAY_ENTRY, "triggerscriptprog"),

		OLDFILENAMES( 1027, STRING_ARRAY_ENTRY, "oldfilenames"),
		FILESIZES( 1028, INT32_ENTRY, "filesizes"),
		FILEMODES( 1030, INT16_ENTRY, "filemodes"),
		FILERDEVS( 1033, INT16_ENTRY, "filerdevs"),
		FILEMTIMES( 1034, INT32_ENTRY, "filemtimes"),
		FILEDIGESTS( 1035, STRING_ARRAY_ENTRY, "filedigests"),
		FILELINKTOS( 1036, STRING_ARRAY_ENTRY, "filelinktos"),
		FILEFLAGS( 1037, INT32_ENTRY, "fileflags"),
		FILEUSERNAME( 1039, STRING_ARRAY_ENTRY, "fileusername"),
		FILEGROUPNAME( 1040, STRING_ARRAY_ENTRY, "filegroupname"),
		FILEDEVICES( 1095, INT32_ENTRY, "filedevices"),
		FILEINODES( 1096, INT32_ENTRY, "fileinodes"),
		FILELANGS( 1097, STRING_ARRAY_ENTRY, "filelangs"),
		PREFIXES( 1098, STRING_ARRAY_ENTRY, "prefixes"),
		DIRINDEXES( 1116, INT32_ENTRY, "dirindexes"),
		BASENAMES( 1117, STRING_ARRAY_ENTRY, "basenames"),
		DIRNAMES( 1118, STRING_ARRAY_ENTRY, "dirnames"),

		PROVIDENAME( 1047, STRING_ARRAY_ENTRY, "providename"),
		REQUIREFLAGS( 1048, INT32_ENTRY, "requireflags"),
		REQUIRENAME( 1049, STRING_ARRAY_ENTRY, "requirename"),
		REQUIREVERSION( 1050, STRING_ARRAY_ENTRY, "requireversion"),
		CONFLICTFLAGS( 1053, INT32_ENTRY, "conflictflags"),
		CONFLICTNAME( 1054, STRING_ARRAY_ENTRY, "conflictname"),
		CONFLICTVERSION( 1055, STRING_ARRAY_ENTRY, "conflictversion"),
		OBSOLETENAME( 1090, STRING_ARRAY_ENTRY, "obsoletename"),
		PROVIDEFLAGS( 1112, INT32_ENTRY, "provideflags"),
		PROVIDEVERSION( 1113, STRING_ARRAY_ENTRY, "provideversion"),
		OBSOLETEFLAGS( 1114, INT32_ENTRY, "obsoleteflags"),
		OBSOLETEVERSION( 1115, STRING_ARRAY_ENTRY, "obsoleteversion"),
		DISTURL( 1123, STRING_ENTRY, "disturl"),
		DISTTAG( 1155, STRING_ENTRY, "disttag"),

		BUGURL( 5012, STRING_ENTRY, "bugurl"),
		ENCODING( 5062, STRING_ENTRY, "encoding"),
		PAYLOADDIGEST( 5092, STRING_ARRAY_ENTRY, "payloaddigest"),
		PAYLOADDIGESTALGO( 5093, INT32_ENTRY, "payloaddigestalgo"),
		PAYLOADDIGESTALT( 5097, STRING_ARRAY_ENTRY, "payloaddigestalt");

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
