package com.thoughtcomplex.hairpin.classhackery
/**
 * Created by Falkreon on 6/30/2014.
 */
class DeclarationHelper {
	static final Map<EnumSet<AccessFlag>, String> declarations = [
			(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.SUPER)) : "public class",
			(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.SUPER, AccessFlag.ABSTRACT)) : "public abstract class",
			(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.SUPER, AccessFlag.FINAL)) : "public final class",
			(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.INTERFACE, AccessFlag.ABSTRACT)) : "public interface",
			(EnumSet.of(AccessFlag.FINAL, AccessFlag.SUPER)) : "private final class",
			(EnumSet.of(AccessFlag.PUBLIC, AccessFlag.FINAL, AccessFlag.SUPER, AccessFlag.ENUM)) : "public enum",
			];
	public static String getClassDeclaration(AccessModifier access, String fullyQualifiedName) {
		return access.toString() + " " + getUnqualifiedName( fullyQualifiedName );
	}

	public static String getUnqualifiedName(String fullyQualifiedName) {
		return fullyQualifiedName.split("\\.").last();
	}

	public static String getPackageDeclaration(String fullyQualifiedName) {
		if (fullyQualifiedName.contains('.')) {
			String[] exploded = fullyQualifiedName.split("\\.");
			return "package " + exploded.take(exploded.length-1).join(".")+";";
			//return "package "+getUnqualifiedName(fullyQualifiedName)+";";
		} else {
			return "package *;";
		}
	}

	public static enum AccessFlag {
		PUBLIC      ( 0x0001, "public"      ),
		FINAL       ( 0x0010, "final"       ),
		SUPER       ( 0x0020, "super"       ),
		INTERFACE   ( 0x0200, "interface"   ),
		ABSTRACT    ( 0x0400, "abstract"    ),
		SYNTHETIC   ( 0x1000, "synthetic"   ),
		ANNOTATION  ( 0x2000, "@interface"  ),
		ENUM        ( 0x4000, "enum"        );

		final int bitmask;
		final String header;
		AccessFlag(int bitmask, String header) {
			this.bitmask    = bitmask;
			this.header     = header;
		}
	}

	public static String translateDescriptor(String descriptor) {
		List<String> descriptorTargets = [];
		String part = descriptor;
		//System.out.println("Descriptor: "+descriptor);
		while (part.length()>0) {
			String result = "";
			while ( part.startsWith( "[" ) ) {
				part = part[1..<part.length()];
				result = result + "[]";
			}

			boolean lName = false;
			switch ( part[0] ) {
				case 'B':
					result = "byte" + result;
					break;
				case 'C':
					result = "char" + result;
					break;
				case 'D':
					result = "double" + result;
					break;
				case 'F':
					result = "float" + result;
					break;
				case 'I':
					result = "int" + result;
					break;
				case 'J':
					result = "long" + result;
					break;
				case 'S':
					result = "short" + result;
					break;
				case 'Z':
					result = "boolean" + result;
					break;
				case 'L':
					lName = true;
					break;
				case 'V':
					result = "void" + result;
					break;
				default:
					result = "INVALID"+part[0] + result;
					break;
			}
			part = part[1..<part.length()];
			if ( lName ) {
				int endCharacter = part.indexOf( ';' );
				String named = part[0..endCharacter-1].replace("/",".");
				if (part.length()>endCharacter+1) {
					part = part[endCharacter + 1..<part.length()];
				} else {
					part = "";
				}

				result = named + result;
			}
			descriptorTargets.add(result);
			lName = false;
		}
		//System.out.println(">"+descriptor+"->"+result);
		return descriptorTargets.toListString().replace("[","").replace("]","");
	}

	public static String getFieldAccessFlags(int accessFlags) {
		String result = "";

		if ((accessFlags & 0x0001) !=0) result += "public ";
		if ((accessFlags & 0x0002) !=0) result += "private ";
		if ((accessFlags & 0x0004) !=0) result += "protected ";
		if ((accessFlags & 0x0008) !=0) result += "static ";
		if ((accessFlags & 0x0010) !=0) result += "final ";
		if ((accessFlags & 0x0020) !=0) result += "synchronized ";
		if ((accessFlags & 0x0040) !=0) result += "bridge_or_volatile "
		if ((accessFlags & 0x0080) !=0) result += "transient_or_varargs ";
		if ((accessFlags & 0x0100) !=0) result += "native ";
		if ((accessFlags & 0x0400) !=0) result += "abstract ";
		if ((accessFlags & 0x0800) !=0) result += "strictfp ";
		if ((accessFlags & 0x1000) !=0) result += "synthetic ";
		if ((accessFlags & 0x4000) !=0) result += "enum_value ";

		return result;
	}

	public static String translateMethodDescriptor(ClassConstant descriptor, String methodName) {
		//System.out.println("Complete method descriptor: "+descriptor.data);
		String result = "";
		String part = descriptor.data;
		if (part.startsWith("(")) {
			//System.out.println("Method Descriptor executing!");
			//Method descriptor
			int closeParenLoc = part.indexOf(")");
			if (closeParenLoc>0) {
				String argumentsDescriptor = "";
				if(closeParenLoc-1>0) {
					argumentsDescriptor = part[1..<closeParenLoc];
				}

				String returnDescriptor = part[closeParenLoc+1..<part.length()];

				//System.out.println("Parsing "+translateDescriptor(returnDescriptor)+"("+translateDescriptor(argumentsDescriptor)+")");
				return translateDescriptor(returnDescriptor)+" "+methodName+"("+translateDescriptor(argumentsDescriptor)+");";
			}
		}
		return "void "+methodName+"();";
		//return translateDescriptor(descriptor.data?:"LUnknown;");
	}
}
