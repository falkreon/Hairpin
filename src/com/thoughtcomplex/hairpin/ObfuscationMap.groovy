package com.thoughtcomplex.hairpin;

/**
 * Created by Falkreon on 7/4/2014.
 */
class ObfuscationMap {
	private ClassLoader loader;
	private final Map<String,String> de_to_obf = [:];
	private final Map<String,String> obf_to_de = [:];

	public ObfuscationMap(ClassLoader classLoader) {
		this.loader = classLoader;
	}


	public void registerClass(String de, String obf) {
		de_to_obf.put(de, obf);
		obf_to_de.put(obf,de);
	}

	public String deobfuscate(String obfuscated) {
		return obf_to_de[obfuscated];
	}

	public String obfuscate(String deobfuscated) {
		return de_to_obf[deobfuscated];
	}

	public Class getObfuscatedClass(String deobfuscated) {
		try {
			return Class.forName( de_to_obf[deobfuscated], true, loader);
		} catch (ClassNotFoundException ex) {
		} catch (NullPointerException ex) {}
		return Object.class;
	}

	public Class getNormalClass(String obfuscated) {
		//System.out.println("Obfuscation mapping: Grabbing class "+obfuscated);
		try {
			//System.out.println("b: "+loader);
			Class<?> c = Class.forName( obfuscated, true, loader );
			//System.out.println("c");
			return c;
		} catch (ClassNotFoundException ex) {
		} catch (NullPointerException ex) {}
		return null;
	}

	public String toSearge() {
		String result = "";

		for(Map.Entry<String, String> entry : de_to_obf) {
			result += "CL: "+entry.value + " " + entry.key+"\n";
		}

		return result;
	}


	public Object getInstance(String de, Object... args) {
		String obf = obfuscate(de);
		if (obf==null) return {}
		try {
			Class<?> obfClass = Class.forName(obf, true, loader);
			return obfClass.newInstance(args);
		} catch (Throwable t) {
			return {};
		}
	}
}
