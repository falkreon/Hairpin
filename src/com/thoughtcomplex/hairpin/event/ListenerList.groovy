package com.thoughtcomplex.hairpin.event

import com.thoughtcomplex.hairpin.WeakSet

import java.lang.ref.WeakReference
import java.lang.reflect.Method

/**
 * Created by Falkreon on 7/5/2014.
 */
class ListenerList<E extends Event> {
	private final Class<E> eventClass;
	private final List<Closure<Void>> closureCallbacks = [];
	private final WeakSet<Method> callbacks = new WeakSet<>();

	private ListenerList(Class<E> cl) {
		eventClass = cl;
		def m = this.&dispatch;
	}

	public void dispatch(E e) {


	}
}
