package com.thoughtcomplex.hairpin.classhackery

import com.thoughtcomplex.hairpin.ClassPattern
import com.thoughtcomplex.hairpin.CoreLoader
import com.thoughtcomplex.hairpin.Mapper
import com.thoughtcomplex.hairpin.ObfuscationMap

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Created by Falkreon on 7/3/2014.
 */
class StaticAnalyzer implements Runnable {
	private final String jarName;
	private final List<String> excludedPackages = [];
	private final List<ClassProfile> profiles = [];
	private ObfuscationMap map = null;
	private Mapper mapper = null;

	public StaticAnalyzer(String jarName) {
		this.jarName = jarName;
	}

	public StaticAnalyzer exclude(String... packages) {
		excludedPackages.addAll(packages);
		return this;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		System.out.println("Profiling jar, Stage 1 / 3...");
		Class found = null;

		JarFile jar = new JarFile(jarName);
		for(JarEntry entry : jar.entries()) {
			//TryLoad
			String name = entry.getName();
			if (!name.endsWith(".class")) continue;
			for(String s : excludedPackages) {
				if(name.startsWith(s.replace('.','/'))) continue;
			}

			if (name.startsWith("java/")) continue;
			if (name.startsWith("javax/")) continue;

			//System.out.println("ToLoad: "+name);
			InputStream entryStream = jar.getInputStream( entry );
			byte[] classData = entryStream.getBytes();
			ClassProfile profile = new ClassProfile(classData);
			String clippedName = name[0..<name.length()-6].replace("/",".");
			profiles.add(profile);
		}

		long profileDelta = System.currentTimeMillis()-startTime;
		int seconds = (int)(profileDelta/1000L);
		System.out.println(""+profiles.size()+" classes profiled. ("+seconds+" seconds)");

		System.out.println("Analyzing results (Applying direct mappings), Stage 2/3...")
		startTime = System.currentTimeMillis();
		List<ClassProfile> unmatchedProfiles = [];
		unmatchedProfiles.addAll(profiles);
		List<String> profileTrash = [];
		Map<String,List<ClassPattern>> undiscoveredPatterns = mapper.getPatterns().clone();
		List<ClassPattern> discoveredPatterns = [];

		//Map<String,String> obfuscationMap = [:];
		for(int i in 0..5) {
			System.out.println("Pass "+i+", "+unmatchedProfiles.size()+" profiles in pool, "+undiscoveredPatterns.size()+" patterns in pool.");
			undiscoveredPatterns.each { String deobfuscated, List<ClassPattern> characteristics ->
				List<String> matches = [];


				unmatchedProfiles.each { ClassProfile profile ->
					boolean matchesAll = true;
					for ( ClassPattern characteristic : characteristics ) {
						characteristic.fillIn( map );
						//String matchIndirect = characteristic.matchIndirect( profile );
						matchesAll &= ( characteristic.match( profile ));
						if ( !matchesAll ) return;
					}
					if ( matchesAll ) {
						matches.add(profile.getName());
						discoveredPatterns.add( deobfuscated );
					};
				}

				if (matches.size()>1) {
					System.out.println("Warning: More than one match for pattern \""+deobfuscated+"\": "+matches.toListString());
					matches.clear();
					//discoveredPatterns.clear();
				} else if (matches.size()==1) {
					map.registerClass( deobfuscated, matches.first() );
					profileTrash.add(matches.first());
					matches.clear();
				}
				//System.out.println("Mini-pass for profile "+deobfuscated+", discarding "+profileTrash.size()+" profile objects.");
				profileTrash.each { it ->
					unmatchedProfiles.remove( it );
				}
				profileTrash.clear();
			}

			discoveredPatterns.each { it ->
				undiscoveredPatterns.remove( it );
			}

			if (undiscoveredPatterns.isEmpty()) break;
			discoveredPatterns.clear();
		}

		long deltaDiscovery = System.currentTimeMillis() - startTime;
		int secondsDiscovery = (int)(deltaDiscovery/1000L);

		System.out.println("Deobfuscated "+obfuscationMap.de_to_obf.keySet().size()+" out of "+mapper.getPatterns().keySet().size()+" pattern entries. ("+secondsDiscovery+" seconds)");
		System.out.println("Failed mappings: "+undiscoveredPatterns.keySet().toListString());
		System.out.println("Analyzing results (Applying indirect mappings), Stage 3/3...");

		startTime = System.currentTimeMillis();

		undiscoveredPatterns.clear();
		undiscoveredPatterns.putAll((Map<String,List<ClassPattern>>)mapper.getIndirectPatterns());
		discoveredPatterns.clear();

		for(int i in 0..8) {
			for(Map.Entry<String,List<ClassPattern>> entry : undiscoveredPatterns) {
				for(ClassProfile profile : profiles) {
					for(ClassPattern pattern : entry.getValue()) {
						pattern.fillIn( map );
						String match = pattern.matchIndirect( profile );
						if ( match != null ) {
							discoveredPatterns.add( entry.getKey() );
							map.registerClass(entry.getKey(), profile.getName());
						}
					}
				}
			}
			discoveredPatterns.each { it -> undiscoveredPatterns.remove(it); }
			discoveredPatterns.clear();
		}

		deltaDiscovery = System.currentTimeMillis() - startTime;
		secondsDiscovery = (int)(deltaDiscovery/1000L);
		System.out.println("Indirect mappings completed in "+secondsDiscovery+" seconds. Total: "+obfuscationMap.de_to_obf.keySet().size()+" classes deobfuscated.");
		System.out.println("Failed indirect mappings: "+undiscoveredPatterns.keySet().toListString());
		//System.out.println("Obfuscation map: "+obfuscationMap.toMapString());

		System.out.println("Mappings Dump: "+obfuscationMap.de_to_obf.toMapString());
	}

	public ObfuscationMap map(Mapper m) {
		if (this.map!=null) return this.map;
		//System.out.println("Instance: "+CoreLoader.getInstance());
		this.map = new ObfuscationMap(CoreLoader.getInstance());
		this.mapper = m;
		run();
		return getObfuscationMap();
	}

	public ObfuscationMap getObfuscationMap() {
		return map;
	}
}
