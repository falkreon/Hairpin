package com.thoughtcomplex.hairpin.classhackery

/**
 * Created by Falkreon on 7/2/2014.
 */
class AccessModifier {
	private String humanReadable = "invalid";
	private Target target;
	private int flagsSrc;
	private EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);

	public static AccessModifier forMethod(int flagset) {
		AccessModifier result = new AccessModifier(flagset);
		result.target = Target.METHOD;
		result.humanReadable = "";
		if (result.flags.contains(Flag.PUBLIC)) result.humanReadable+="public";
		if (result.flags.contains(Flag.PRIVATE)) result.humanReadable+="private";
		if (result.flags.contains(Flag.PROTECTED)) result.humanReadable+="protected";
		if (result.flags.contains(Flag.STATIC)) result.humanReadable+=" static";
		if (result.flags.contains(Flag.FINAL)) result.humanReadable+=" final";
		if (result.flags.contains(Flag.SUPER_OR_SYNCHRONIZED)) result.humanReadable+=" synchronized";
		if (result.flags.contains(Flag.NATIVE)) result.humanReadable+=" native";
		if (result.flags.contains(Flag.ABSTRACT)) result.humanReadable+=" abstract";
		if (result.flags.contains(Flag.STRICT)) result.humanReadable+=" strictfp";
		if (result.flags.contains(Flag.SYNTHETIC)) result.humanReadable+=" synthetic";
		result.humanReadable = result.humanReadable.trim();
		if (result.humanReadable=="") result.humanReadable="invalid";
		return result;
	}

	public static AccessModifier forField(int flagset) {
		AccessModifier result = new AccessModifier(flagset);
		result.target = Target.FIELD;
		result.humanReadable = "";
		if (result.flags.contains(Flag.PUBLIC)) result.humanReadable+="public";
		if (result.flags.contains(Flag.PRIVATE)) result.humanReadable+="private";
		if (result.flags.contains(Flag.PROTECTED)) result.humanReadable+="protected";
		if (result.flags.contains(Flag.STATIC)) result.humanReadable+=" static";
		if (result.flags.contains(Flag.FINAL)) result.humanReadable+=" final";
		if (result.flags.contains(Flag.VOLATILE_OR_BRIDGE)) result.humanReadable+=" volatile";
		if (result.flags.contains(Flag.TRANSIENT_OR_VARARGS)) result.humanReadable+=" transient";
		if (result.flags.contains(Flag.SYNTHETIC)) result.humanReadable+=" synthetic";
		result.humanReadable = result.humanReadable.trim();
		if (result.humanReadable=="") result.humanReadable="invalid";
		return result;
	}

	public static AccessModifier forClass(int flagset) {
		AccessModifier result = new AccessModifier(flagset);
		result.target = Target.CLASS;
		result.humanReadable = "";
		if (result.flags.contains(Flag.PUBLIC)) result.humanReadable+="public";
		if (result.flags.contains(Flag.PRIVATE)) result.humanReadable+="private";
		if (result.flags.contains(Flag.PROTECTED)) result.humanReadable+="protected";
		if (result.flags.disjoint([Flag.PUBLIC, Flag.PRIVATE, Flag.PROTECTED])) result.humanReadable+="package_private";

		if (result.flags.contains(Flag.STATIC)) result.humanReadable+=" static";
		if (result.flags.contains(Flag.FINAL)) result.humanReadable+=" final";
		if (result.flags.contains(Flag.ABSTRACT)) result.humanReadable+=" abstract";
		if (result.flags.contains(Flag.SYNTHETIC)) result.humanReadable+=" synthetic";

		if (result.flags.contains(Flag.INTERFACE)) {
			result.humanReadable+=" interface";
		} else if (result.flags.contains(Flag.ENUM)) {
			result.humanReadable+=" enum";
		} else if (result.flags.contains(Flag.ANNOTATION)) {
			result.humanReadable+=" @interface";
		} else {
			result.humanReadable+=" class";
		}

		result.humanReadable = result.humanReadable.trim();
		if (result.humanReadable=="") result.humanReadable="invalid";
		return result;
	}

	private AccessModifier(int flagsrc) {
		flagsSrc = flagsrc;
		for(Flag flag : Flag.values()) {
			if ((flagsrc & flag.bitmask)!=0) flags.add(flag);
		}
	}

	public boolean isEnum() {
		return flags.contains(Flag.ENUM);
	}

	public boolean isInterface() {
		return flags.contains(Flag.INTERFACE);
	}

	public boolean isAbstract() {
		return flags.contains(Flag.ABSTRACT);
	}

	@Override
	public String toString() {
		return humanReadable;
	}

	public static enum Flag {
		PUBLIC                  ( 0x0001, "public"      ),
		PRIVATE                 ( 0x0002, "private"     ),
		PROTECTED               ( 0x0004, "protected"   ),
		STATIC                  ( 0x0008, "static"      ),
		FINAL                   ( 0x0010, "final"       ),
		SUPER_OR_SYNCHRONIZED   ( 0x0020, "(super)"     , "synchronized"    , "synchronized"),
		VOLATILE_OR_BRIDGE      ( 0x0040, "volatile"    , "volatile"        , "(bridge)"),
		TRANSIENT_OR_VARARGS    ( 0x0080, "transient"   , "transient"       , "(varargs)"),
		NATIVE                  ( 0x0100, "native"      ),
		INTERFACE               ( 0x0200, "interface"   ),
		ABSTRACT                ( 0x0400, "abstract"    ),
		STRICT                  ( 0x0800, "strictfp"    ),
		SYNTHETIC               ( 0x1000, "synthetic"   ),
		ANNOTATION              ( 0x2000, "@interface"  ),
		ENUM                    ( 0x4000, "(enum)"      );

		final int bitmask;
		final String header;
		final String methodHeader;
		Flag(int bitmask, String combinedHeader) {
			this(bitmask, combinedHeader, combinedHeader, combinedHeader);
		}
		Flag(int bitmask, String classHeader, String fieldHeader, String methodHeader) {
			this.bitmask    = bitmask;
			this.header     = header;
			this.methodHeader = methodHeader;
		}
	}
	private static enum Target {
		CLASS, FIELD, METHOD;
	}
}
