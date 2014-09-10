package com.thoughtcomplex.hairpin

import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Created by Falkreon on 7/9/2014.
 */
class PluginManager {
	private static final Map<String,Object> plugins = [:];
	private static final ReentrantReadWriteLock pluginsMutex = new ReentrantReadWriteLock();

	public static Object getPlugin(String name) {
		pluginsMutex.readLock().lock();
			Object p = plugins.get(name.toLowerCase());
		pluginsMutex.readLock().unlock();
		if (p==null) p = [run:{}];
		return p;
	}

	public static void registerPlugin(String name, Object plugin) throws DuplicateRegistrationException {
		pluginsMutex.writeLock().lock();
			if (plugins.containsKey(name.toLowerCase())) throw new DuplicateRegistrationException("Two plugins cannot be registered with the same name.");
			plugins.put(name.toLowerCase(), plugin);
		pluginsMutex.writeLock().unlock();
	}

	public static void initializePlugins() {
		pluginsMutex.readLock().lock();
			for(Object o : plugins.values()) {
				if (!o.metaClass.hasProperty("_hairpin_initialized")) {
					o.metaClass._hairpin_initialized = false;
				}

				if (!o._hairpin_initialized) {
					//TODO: Find EntryPoint and run

					o._hairpin_initialized = true;
				}
			}
		pluginsMutex.readLock().unlock();
	}

}
