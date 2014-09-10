package com.thoughtcomplex.hairpin

import com.thoughtcomplex.hairpin.classhackery.ClassProfile

/**
 * Created by Falkreon on 6/28/2014.
 */
public abstract class ClassPattern {

	public abstract boolean match(ClassProfile profile);
	public void fillIn(ObfuscationMap obfuscationMap) {}
	public String matchIndirect(ClassProfile profile) { return null; }

	private static ClassPattern enumPattern = new ClassPattern() {
		@Override
		public boolean match(ClassProfile profile) {
			return profile.isEnum();
		}
	}

	private static ClassPattern abstractPattern = new ClassPattern() {
		@Override
		public boolean match(ClassProfile profile) {
			return profile.isAbstract();
		}
	}

	public static ClassPattern usesStrings(String... strings) {
		return new Strings(strings);
	}

	public static class Strings extends ClassPattern {
		java.lang.String[] strings = new java.lang.String[0];
		public Strings(String... strings) {
			this.strings = strings;
		}

		@Override
		public boolean match(ClassProfile profile) {
			List<String> constantStrings = profile.getStringConstants();

			for(String s : strings) {
				if (!constantStrings.contains(s)) return false;
			}
			return true;
		}
	}



	public static ClassPattern isEnum() {
		return enumPattern;
	}

	public static ClassPattern isAbstract() {
		return abstractPattern;
	}

	public static class Reference extends ClassPattern {
		private final boolean obfuscated;
		private final String[] classNames;
		private String[] obfuscatedClassNames = null;

		public Reference(String... classNames) {
			this.classNames = classNames;
			obfuscated = false;
		}
		private Reference(boolean obfuscated, String... classNames) {
			this.classNames = classNames;
			this.obfuscated = obfuscated;
		}

		@Override
		public void fillIn(ObfuscationMap obfuscationMap) {
			if (!obfuscated) return;
			if (obfuscatedClassNames==null) {
				obfuscatedClassNames = new String[classNames.length];
			}

			for(int i in 0..<classNames.length) {
				obfuscatedClassNames[i] = obfuscationMap.obfuscate(classNames[i]);
			}

			/*
			classNames.eachWithIndex { String s, int i ->
				if (obfuscatedClassNames[i]!=null) return;
				obfuscatedClassNames = obfuscationMap.obfuscate(classNames[i]);
			}

			obfuscationTable.each { String k, String v ->
				int i = classNames.findIndexOf { k };
				if (i>=0) obfuscatedClassNames[k] = v;
			}*/
		}

		@Override
		public boolean match(ClassProfile profile) {
			List<java.lang.String> constantStrings = profile.getClassReferences();

			for(java.lang.String s : classNames) {
				if (!constantStrings.contains(s)) return false;
			}
			return true;
		}
	}

	public static ClassPattern subclassOf(final String s) {
		return new SuperClassPattern(s);
	}

	private static class SuperClassPattern extends ClassPattern {
		private String obfuscated = null;
		private final String deobfuscated;
		public SuperClassPattern(String superclass) {
			this.deobfuscated = superclass;
		}
		@Override
		public boolean match(ClassProfile profile) {
			if (this.obfuscated==null) return false;
			String superclass = profile.superclass.replace("/",".");
			return obfuscated==superclass;
		}

		@Override
		public void fillIn(ObfuscationMap obfuscationMap) {
			if (obfuscated==null) obfuscated = obfuscationMap.obfuscate( deobfuscated );
		}
	}

	public static ClassPattern doesImplement(String s) {
		return new ImplementsClassPattern(s);
	}

	public static ClassPattern doesImplementNormal(String s) {
		ImplementsClassPattern result = new ImplementsClassPattern(s);
		result.isObfuscated=false;
		return result;
	}

	private static class ImplementsClassPattern extends ClassPattern {
		private final String interfaceName;
		private String obfuscated;
		private boolean isObfuscated = true;
		public ImplementsClassPattern(String interfaceName) {
			this.interfaceName = interfaceName;
		}
		@Override
		public boolean match(ClassProfile profile) {
			if (isObfuscated) {
				if (obfuscated == null) return false;
				return profile.doesImplement( obfuscated );
			} else {
				return profile.doesImplement(interfaceName);
			}
		}
		@Override
		public void fillIn(ObfuscationMap obfuscationMap) {
			if (isObfuscated && obfuscated == null) obfuscated = obfuscationMap.obfuscate( interfaceName );
		}
	}

	public static ClassPattern implementedBy(String classname) {
		return new ImplementedBy(classname);
	}

	public static class ImplementedBy extends ClassPattern {
		private final String className;
		private String obfuscated;
		public ImplementedBy(String classname) {
			this.className = classname;
		}
		@Override
		public boolean match(ClassProfile profile) {
			return false;
		}
		@Override
		public String matchIndirect(ClassProfile profile) {
			if (obfuscated==null) return null;
			if (profile.thisclass.replace("/",".")==obfuscated) {
				if (!profile.interfaces.isEmpty()) return profile.interfaces.first();
			}
			return null;
		}
		@Override
		public void fillIn(ObfuscationMap obfuscationMap) {
			if (obfuscated==null) obfuscated = obfuscationMap.obfuscate(className);
		}
	}
}
