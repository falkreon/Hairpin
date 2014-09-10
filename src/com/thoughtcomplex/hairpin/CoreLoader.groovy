package com.thoughtcomplex.hairpin

//import com.thoughtcomplex.hairpin.api.Hairpin
import com.thoughtcomplex.hairpin.classhackery.ClassProfile
//import RegistryMaterials

import java.lang.ref.WeakReference
import java.lang.reflect.Array
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Created by Falkreon on 6/28/2014.
 */
class CoreLoader extends ClassLoader {
	private static CoreLoader _instance = null;
	private static jarName = "";
	private List<String> unimportantPackages = [
			"com.google","org.apache", "net.rudp", "it.unimi", "io.netty", "org.slf4j", "javassist", "java", "javax"
	];

	private final Map<String, ClassProfile> classProfiles = [:];
	private ClassLoader child;
	private final List<WeakReference<Class<?>>> workingSet = [];
	private final List<WeakReference<Class<?>>> loadedClasses = [];

	public CoreLoader() {
		super();
		URLClassLoader thing = getChildLoaderThing();
		//thing.addURL(new File("cores\\libraries\\net\\sf\\jopt-simple\\jopt-simple\\4.6\\jopt-simple-4.6.jar").toURL());
		//thing.addURL(new File("cores\\libraries\\net\\sf\\jopt-simple\\jopt-simple\\4.6\\jopt-simple-4.6.jar").toURL());
	}

	public static CoreLoader getInstance() {
		if (CoreLoader._instance==null) CoreLoader._instance=new CoreLoader();
		_instance.getChildLoaderThing();
		return CoreLoader._instance;
	}

	@Override
	public Class findClass(String name) {
		//System.out.println("Top-level class load: "+name);
		getChildLoaderThing().findClass(name);
	}



	private URLClassLoader getChildLoaderThing() {

		if (child==null) {
			File jarFile = new File( jarName );

			if ( !jarFile.exists() ) {
				System.out.println( "Could not load the jar!" );
				return null;
			}
			child = new URLClassLoader( [jarFile.toURL()] as URL[], this.getClass().getClassLoader() ) {
				@Override
				public Class findClass( String s ) {
					//Intercept
					//System.out.println("Internal findClass for "+s);

					for(String it : unimportantPackages) {
						if (s.startsWith(it+".")) return super.findClass(s);
					}

					//Ignore Groovy synthetic classes
					if (s.endsWith("Customizer")) return super.findClass(s);
					if (s.endsWith("BeanInfo")) return super.findClass(s);

					//This is an intercepted class load.

					//System.out.println( "Intercepted class load: " + s );
					byte[] data = getClassData(s);
					if (data==null || data.length<10) return null;
					try {
						//ClassProfile profile = new ClassProfile( data );
						//classProfiles.put( s, profile );
						//System.out.println( profile.toString() );
					} catch (Throwable t) {
						//System.out.println("Failed to generate class profile.");
						t.printStackTrace();
						System.exit(1);
					}
					//System.out.println("Acquired data for "+s+": "+data.length+" bytes.");


					//System.out.println("Looking for loaded class "+s);

					Class c = super.findLoadedClass( s );
					if (c!=null) return c;

					//System.out.println("Loading fresh class "+s);

					try {
						c = super.findClass( s );
						//System.out.println("Loaded class: "+c);
						if ( c != null ) {
							//registerClass( c );
						}
						return c;
					} catch (ClassCircularityError err) {
						System.out.println("Class "+s+" couldn't be loaded, probably because it's indirectly its own ancestor.");
						//Class result = ClassLoader.getSystemClassLoader().findClass(s);
						//if (result==null) System.out.println("Additionally, the System ClassLoader couldn't load the class!");
						return null;
					}
				}

				private byte[] getClassData(String s) {
					String resourceName = s.replace('.','/')+".class";
					//System.out.println("Attempting to get resource "+resourceName);
					InputStream input = getResourceAsStream(resourceName);
					//InputStream input = ClassLoader.getResourceAsStream( resourceName );
					if (input==null) {
						//System.out.println("Resource doesn't exist: "+resourceName);
						return [];
					}
					byte[] result = input.getBytes();
					input.close();
					return result;
				}
			}
		}
		return child;
	}

	public static void addJar(String jar) {
		getInstance().getChildLoaderThing().addURL(new File(jar).toURL());
	}

	public static void addJars(String... jarNames) {
		for(String jar : jarNames) addJar(jar);
	}

	public static void init(String jarName) {
		this.jarName = jarName;
		getInstance();
	}
	/*
	private static final List<Class> mundaneClasses = [
	    String.class, Integer.class, Double.class, Float.class,
		Long.class, Byte.class, Short.class, Object.class,
		Array.class, ArrayList.class, HashMap.class
	];*/
	/*
	private void registerClass(Class c) {
		//Validate input
		if (mundaneClasses.contains(c)) return;
		if (loadedClasses.contains(c)) return;
		workingSet.add(new WeakReference<Class<?>>(c));
	}

	private void flipRegistration() {
		loadedClasses.addAll(workingSet);
		workingSet.clear();
	}*/
}
