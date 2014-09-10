package com.thoughtcomplex.hairpin

import java.lang.ref.WeakReference

/**
 * Created by Falkreon on 7/6/2014.
 */
class WeakSet<E> implements Set<E> {
	private final HashSet<WeakReference<E>> storage;
	//private WeakReference<E>[] storage;
	//private int lastIndex;

	public WeakSet() {
		storage = [];
	}

	public void clear() {
		storage.clear();
	}

	/**
	 * Included here for completeness, this method creates strong references to everything in the list, and is therefore
	 * not recommended due to code smell.
	 * @return An array containing strong references to the objects still contained
	 */
	@Override
	public int size() {
		return storage.count { it.get()!=null; }
	}

	@Override
	public boolean isEmpty() {
		return storage.find { it.get()!=null; } == null;
	}

	@Override
	public boolean contains( final Object o ) {
		return storage.find { it -> E e = it.get(); e.equals(o); } != null;
	}

	@Override
	WeakSetIterator<E> iterator() {
		return new WeakSetIterator<E>(storage.iterator());
	}

	@Override
	public Object[] toArray() {
		List<E> intermediate = [];
		storage.each {
			E e = it.get();
			if (e!=null) intermediate.add(e);
		}
		return intermediate as E[];
	}

	public void sweep() {

	}

	@Override
	public <F> Object[] toArray( final F[] es ) {
		if (es.length>storage.size()) {
			int loc = 0;
			for(WeakReference<F> e : storage) {
				F cur = e.get();
				if (cur!=null) {
					es[loc] = cur;
					cur = null;
					++loc;
				}
			}
			return es;
		} else {
			return toArray();
		}
	}

	@Override
	boolean add( final E e ) {
		return storage.add(new WeakReference(e));
	}

	@Override
	boolean remove( final Object o ) {
		//Manually iterate so we can use Iterator.remove();
		Iterator<WeakReference<E>> iterator = storage.iterator();
		while (iterator.hasNext()) {
			WeakReference<E> e = iterator.next();
			if ( o.equals( e.get() ) ) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	boolean containsAll( final Collection<?> objects ) {
		/* This procedure can look a little weird at times, so here's what's happening:
		 * O(m*n), for each Object argument, check to see if any weakReference points to it.
		 * And all because WeakReference.equals() is inherited from Object.
		 */

	objectLoop:
		for(Object o : objects) {
			for(WeakReference<E> e : storage) {
				if (o.equals(e.get())) continue objectLoop;
			}
			return false;
		}
		return true;
	}

	@Override
	boolean addAll( final Collection<? extends E> es ) {
		for(E e : es) {
			storage.add(new WeakReference<E>(e));
		}
	}

	@Override
	boolean retainAll( final Collection<?> objects ) {
		throw new UnsupportedOperationException("This collection does not support the RetainAll operation.");
	}

	@Override
	boolean removeAll( final Collection<?> objects ) {
		boolean changed = false;
		for (Object o : objects) {
			changed |= remove(o);
		}

		return changed;
	}

	public static class WeakSetIterator<E> implements Iterator<E> {
		private final Iterator<WeakReference<E>> underlying;

		private WeakSetIterator(Iterator<WeakReference<E>> e) {
			underlying = e;
		}

		@Override
		public boolean hasNext() {
			return underlying.hasNext();
		}

		@Override
		public E next() {
			E e = null;
			while (underlying.hasNext()) {
				e = underlying.next()?.get();
				if (e!=null) return e;
			}

			return e;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove is not supported.");
		}
	}
}
