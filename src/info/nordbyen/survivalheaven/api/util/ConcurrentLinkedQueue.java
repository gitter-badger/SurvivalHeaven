/**
 * This file is part of survivalheaven.org, licensed under the MIT License (MIT).
 *
 * Copyright (c) SurvivalHeaven.org <http://www.survivalheaven.org>
 * Copyright (c) NordByen.info <http://www.nordbyen.info>
 * Copyright (c) l0lkj.info <http://www.l0lkj.info>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package info.nordbyen.survivalheaven.api.util;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * An unbounded thread-safe {@linkplain Queue queue} based on linked nodes. This
 * queue orders elements FIFO (first-in-first-out). The <em>head</em> of the
 * queue is that element that has been on the queue the longest time. The
 * <em>tail</em> of the queue is that element that has been on the queue the
 * shortest time. New elements are inserted at the tail of the queue, and the
 * queue retrieval operations obtain elements at the head of the queue. A
 * <tt>ConcurrentLinkedQueue</tt> is an appropriate choice when many threads
 * will share access to a common collection. This queue does not permit
 * <tt>null</tt> elements.
 * 
 * <p>
 * This implementation employs an efficient "wait-free" algorithm based on one
 * described in <a href="http://www.cs.rochester.edu/u/michael/PODC96.html">
 * Simple, Fast, and Practical Non-Blocking and Blocking Concurrent Queue
 * Algorithms</a> by Maged M. Michael and Michael L. Scott.
 * 
 * <p>
 * Beware that, unlike in most collections, the <tt>size</tt> method is
 * <em>NOT</em> a constant-time operation. Because of the asynchronous nature of
 * these queues, determining the current number of elements requires a traversal
 * of the elements.
 * 
 * <p>
 * This class and its iterator implement all of the <em>optional</em> methods of
 * the {@link Collection} and {@link Iterator} interfaces.
 * 
 * <p>
 * Memory consistency effects: As with other concurrent collections, actions in
 * a thread prior to placing an object into a {@code ConcurrentLinkedQueue} <a
 * href="package-summary.html#MemoryVisibility"><i>happen-before</i></a> actions
 * subsequent to the access or removal of that element from the
 * {@code ConcurrentLinkedQueue} in another thread.
 * 
 * <p>
 * This class is a member of the <a href="{@docRoot}
 * /../technotes/guides/collections/index.html"> Java Collections Framework</a>.
 * 
 * @param <E>
 *            the type of elements held in this collection
 * 
 */
public class ConcurrentLinkedQueue<E> extends AbstractQueue<E> implements
		Queue<E>, java.io.Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 196745693267521676L;

	/*
	 * This is a straight adaptation of Michael & Scott algorithm. For
	 * explanation, read the paper. The only (minor) algorithmic difference is
	 * that this version supports lazy deletion of internal nodes (method
	 * remove(Object)) -- remove CAS'es item fields to null. The normal queue
	 * operations unlink but then pass over nodes with null item fields.
	 * Similarly, iteration methods ignore those with nulls.
	 * 
	 * Also note that like most non-blocking algorithms in this package, this
	 * implementation relies on the fact that in garbage collected systems,
	 * there is no possibility of ABA problems due to recycled nodes, so there
	 * is no need to use "counted pointers" or related techniques seen in
	 * versions used in non-GC'ed settings.
	 */
	/**
	 * The Class Node.
	 * 
	 * @param <E>
	 *            the element type
	 */
	private static class Node<E> {

		/** The item. */
		private volatile E item;
		/** The next. */
		private volatile Node<E> next;
		/** The Constant nextUpdater. */
		@SuppressWarnings("rawtypes")
		private static final AtomicReferenceFieldUpdater<Node, Node> nextUpdater = AtomicReferenceFieldUpdater
				.newUpdater(Node.class, Node.class, "next");
		/** The Constant itemUpdater. */
		@SuppressWarnings("rawtypes")
		private static final AtomicReferenceFieldUpdater<Node, Object> itemUpdater = AtomicReferenceFieldUpdater
				.newUpdater(Node.class, Object.class, "item");

		/**
		 * Instantiates a new node.
		 * 
		 * @param x
		 *            the x
		 */
		@SuppressWarnings("unused")
		Node(final E x) {
			item = x;
		}

		/**
		 * Instantiates a new node.
		 * 
		 * @param x
		 *            the x
		 * @param n
		 *            the n
		 */
		Node(final E x, final Node<E> n) {
			item = x;
			next = n;
		}

		/**
		 * Gets the item.
		 * 
		 * @return the item
		 */
		E getItem() {
			return item;
		}

		/**
		 * Cas item.
		 * 
		 * @param cmp
		 *            the cmp
		 * @param val
		 *            the val
		 * @return true, if successful
		 */
		boolean casItem(final E cmp, final E val) {
			return itemUpdater.compareAndSet(this, cmp, val);
		}

		/**
		 * Sets the item.
		 * 
		 * @param val
		 *            the new item
		 */
		void setItem(final E val) {
			itemUpdater.set(this, val);
		}

		/**
		 * Gets the next.
		 * 
		 * @return the next
		 */
		Node<E> getNext() {
			return next;
		}

		/**
		 * Cas next.
		 * 
		 * @param cmp
		 *            the cmp
		 * @param val
		 *            the val
		 * @return true, if successful
		 */
		boolean casNext(final Node<E> cmp, final Node<E> val) {
			return nextUpdater.compareAndSet(this, cmp, val);
		}

		/**
		 * Sets the next.
		 * 
		 * @param val
		 *            the new next
		 */
		@SuppressWarnings("unused")
		void setNext(final Node<E> val) {
			nextUpdater.set(this, val);
		}
	}

	/** The Constant tailUpdater. */
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ConcurrentLinkedQueue, Node> tailUpdater = AtomicReferenceFieldUpdater
			.newUpdater(ConcurrentLinkedQueue.class, Node.class, "tail");
	/** The Constant headUpdater. */
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ConcurrentLinkedQueue, Node> headUpdater = AtomicReferenceFieldUpdater
			.newUpdater(ConcurrentLinkedQueue.class, Node.class, "head");

	/**
	 * Cas tail.
	 * 
	 * @param cmp
	 *            the cmp
	 * @param val
	 *            the val
	 * @return true, if successful
	 */
	private boolean casTail(final Node<E> cmp, final Node<E> val) {
		return tailUpdater.compareAndSet(this, cmp, val);
	}

	/**
	 * Cas head.
	 * 
	 * @param cmp
	 *            the cmp
	 * @param val
	 *            the val
	 * @return true, if successful
	 */
	private boolean casHead(final Node<E> cmp, final Node<E> val) {
		return headUpdater.compareAndSet(this, cmp, val);
	}

	/**
	 * Pointer to header node, initialized to a dummy node. The first actual
	 * node is at head.getNext().
	 */
	private transient volatile Node<E> head = new Node<E>(null, null);
	/** Pointer to last node on list *. */
	private transient volatile Node<E> tail = head;

	/**
	 * Creates a <tt>ConcurrentLinkedQueue</tt> that is initially empty.
	 */
	public ConcurrentLinkedQueue() {
	}

	/**
	 * Creates a <tt>ConcurrentLinkedQueue</tt> initially containing the
	 * elements of the given collection, added in traversal order of the
	 * collection's iterator.
	 * 
	 * @param c
	 *            the collection of elements to initially contain
	 */
	public ConcurrentLinkedQueue(final Collection<? extends E> c) {
		for (final Iterator<? extends E> it = c.iterator(); it.hasNext();) {
			add(it.next());
		}
	}

	// Have to override just to update the javadoc
	/**
	 * Inserts the specified element at the tail of this queue.
	 * 
	 * @param e
	 *            the e
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 */
	@Override
	public boolean add(final E e) {
		return offer(e);
	}

	/**
	 * Inserts the specified element at the tail of this queue.
	 * 
	 * @param e
	 *            the e
	 * @return <tt>true</tt> (as specified by {@link Queue#offer})
	 */
	@Override
	public boolean offer(final E e) {
		if (e == null)
			throw new NullPointerException();
		final Node<E> n = new Node<E>(e, null);
		for (;;) {
			final Node<E> t = tail;
			final Node<E> s = t.getNext();
			if (t == tail) {
				if (s == null) {
					if (t.casNext(s, n)) {
						casTail(t, n);
						return true;
					}
				} else {
					casTail(t, s);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#poll()
	 */
	@Override
	public E poll() {
		for (;;) {
			final Node<E> h = head;
			final Node<E> t = tail;
			final Node<E> first = h.getNext();
			if (h == head) {
				if (h == t) {
					if (first == null)
						return null;
					else {
						casTail(t, first);
					}
				} else if (casHead(h, first)) {
					final E item = first.getItem();
					if (item != null) {
						first.setItem(null);
						return item;
					}
					// else skip over deleted item, continue loop,
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#peek()
	 */
	@Override
	public E peek() { // same as poll except don't remove item
		for (;;) {
			final Node<E> h = head;
			final Node<E> t = tail;
			final Node<E> first = h.getNext();
			if (h == head) {
				if (h == t) {
					if (first == null)
						return null;
					else {
						casTail(t, first);
					}
				} else {
					final E item = first.getItem();
					if (item != null)
						return item;
					else {
						casHead(h, first);
					}
				}
			}
		}
	}

	/**
	 * Returns the first actual (non-header) node on list. This is yet another
	 * variant of poll/peek; here returning out the first node, not element (so
	 * we cannot collapse with peek() without introducing race.)
	 * 
	 * @return the node
	 */
	Node<E> first() {
		for (;;) {
			final Node<E> h = head;
			final Node<E> t = tail;
			final Node<E> first = h.getNext();
			if (h == head) {
				if (h == t) {
					if (first == null)
						return null;
					else {
						casTail(t, first);
					}
				} else {
					if (first.getItem() != null)
						return first;
					else {
						casHead(h, first);
					}
				}
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if this queue contains no elements.
	 * 
	 * @return <tt>true</tt> if this queue contains no elements
	 */
	@Override
	public boolean isEmpty() {
		return first() == null;
	}

	/**
	 * Returns the number of elements in this queue. If this queue contains more
	 * than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 * 
	 * <p>
	 * Beware that, unlike in most collections, this method is <em>NOT</em> a
	 * constant-time operation. Because of the asynchronous nature of these
	 * queues, determining the current number of elements requires an O(n)
	 * traversal.
	 * 
	 * @return the number of elements in this queue
	 */
	@Override
	public int size() {
		int count = 0;
		for (Node<E> p = first(); p != null; p = p.getNext()) {
			if (p.getItem() != null) {
				// Collections.size() spec says to max out
				if (++count == Integer.MAX_VALUE) {
					break;
				}
			}
		}
		return count;
	}

	/**
	 * Returns <tt>true</tt> if this queue contains the specified element. More
	 * formally, returns <tt>true</tt> if and only if this queue contains at
	 * least one element <tt>e</tt> such that <tt>o.equals(e)</tt>.
	 * 
	 * @param o
	 *            object to be checked for containment in this queue
	 * @return <tt>true</tt> if this queue contains the specified element
	 */
	@Override
	public boolean contains(final Object o) {
		if (o == null)
			return false;
		for (Node<E> p = first(); p != null; p = p.getNext()) {
			final E item = p.getItem();
			if ((item != null) && o.equals(item))
				return true;
		}
		return false;
	}

	/**
	 * Removes a single instance of the specified element from this queue, if it
	 * is present. More formally, removes an element <tt>e</tt> such that
	 * <tt>o.equals(e)</tt>, if this queue contains one or more such elements.
	 * Returns <tt>true</tt> if this queue contained the specified element (or
	 * equivalently, if this queue changed as a result of the call).
	 * 
	 * @param o
	 *            element to be removed from this queue, if present
	 * @return <tt>true</tt> if this queue changed as a result of the call
	 */
	@Override
	public boolean remove(final Object o) {
		if (o == null)
			return false;
		for (Node<E> p = first(); p != null; p = p.getNext()) {
			final E item = p.getItem();
			if ((item != null) && o.equals(item) && p.casItem(item, null))
				return true;
		}
		return false;
	}

	/**
	 * Returns an array containing all of the elements in this queue, in proper
	 * sequence.
	 * 
	 * <p>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this queue. (In other words, this method must allocate a
	 * new array). The caller is thus free to modify the returned array.
	 * 
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 * 
	 * @return an array containing all of the elements in this queue
	 */
	@Override
	public Object[] toArray() {
		// Use ArrayList to deal with resizing.
		final ArrayList<E> al = new ArrayList<E>();
		for (Node<E> p = first(); p != null; p = p.getNext()) {
			final E item = p.getItem();
			if (item != null) {
				al.add(item);
			}
		}
		return al.toArray();
	}

	/**
	 * Returns an array containing all of the elements in this queue, in proper
	 * sequence; the runtime type of the returned array is that of the specified
	 * array. If the queue fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this queue.
	 * 
	 * <p>
	 * If this queue fits in the specified array with room to spare (i.e., the
	 * array has more elements than this queue), the element in the array
	 * immediately following the end of the queue is set to <tt>null</tt>.
	 * 
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between
	 * array-based and collection-based APIs. Further, this method allows
	 * precise control over the runtime type of the output array, and may, under
	 * certain circumstances, be used to save allocation costs.
	 * 
	 * <p>
	 * Suppose <tt>x</tt> is a queue known to contain only strings. The
	 * following code can be used to dump the queue into a newly allocated array
	 * of <tt>String</tt>:
	 * 
	 * <pre>
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * String[] y = x.toArray(new String[0]);
	 * </pre>
	 * 
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to
	 * <tt>toArray()</tt>.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param a
	 *            the array into which the elements of the queue are to be
	 *            stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose
	 * @return an array containing all of the elements in this queue
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final T[] a) {
		// try to use sent-in array
		int k = 0;
		Node<E> p;
		for (p = first(); (p != null) && (k < a.length); p = p.getNext()) {
			final E item = p.getItem();
			if (item != null) {
				a[k++] = (T) item;
			}
		}
		if (p == null) {
			if (k < a.length) {
				a[k] = null;
			}
			return a;
		}
		// If won't fit, use ArrayList version
		final ArrayList<E> al = new ArrayList<E>();
		for (Node<E> q = first(); q != null; q = q.getNext()) {
			final E item = q.getItem();
			if (item != null) {
				al.add(item);
			}
		}
		return al.toArray(a);
	}

	/**
	 * Returns an iterator over the elements in this queue in proper sequence.
	 * The returned iterator is a "weakly consistent" iterator that will never
	 * throw {@link ConcurrentModificationException}, and guarantees to traverse
	 * elements as they existed upon construction of the iterator, and may (but
	 * is not guaranteed to) reflect any modifications subsequent to
	 * construction.
	 * 
	 * @return an iterator over the elements in this queue in proper sequence
	 */
	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}

	/**
	 * The Class Itr.
	 */
	private class Itr implements Iterator<E> {

		/**
		 * Next node to return item for.
		 */
		private Node<E> nextNode;
		/**
		 * nextItem holds on to item fields because once we claim that an
		 * element exists in hasNext(), we must return it in the following
		 * next() call even if it was in the process of being removed when
		 * hasNext() was called.
		 */
		private E nextItem;
		/**
		 * Node of the last returned item, to support remove.
		 */
		private Node<E> lastRet;

		/**
		 * Instantiates a new itr.
		 */
		Itr() {
			advance();
		}

		/**
		 * Moves to next valid node and returns item to return for next(), or
		 * null if no such.
		 * 
		 * @return the e
		 */
		private E advance() {
			lastRet = nextNode;
			final E x = nextItem;
			Node<E> p = (nextNode == null) ? first() : nextNode.getNext();
			for (;;) {
				if (p == null) {
					nextNode = null;
					nextItem = null;
					return x;
				}
				final E item = p.getItem();
				if (item != null) {
					nextNode = p;
					nextItem = item;
					return x;
				} else {
					p = p.getNext();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return nextNode != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public E next() {
			if (nextNode == null)
				throw new NoSuchElementException();
			return advance();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			final Node<E> l = lastRet;
			if (l == null)
				throw new IllegalStateException();
			// rely on a future traversal to relink.
			l.setItem(null);
			lastRet = null;
		}
	}

	/**
	 * Save the state to a stream (that is, serialize it).
	 * 
	 * @param s
	 *            the stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @serialData All of the elements (each an <tt>E</tt>) in the proper order,
	 *             followed by a null
	 */
	private void writeObject(final java.io.ObjectOutputStream s)
			throws java.io.IOException {
		// Write out any hidden stuff
		s.defaultWriteObject();
		// Write out all elements in the proper order.
		for (Node<E> p = first(); p != null; p = p.getNext()) {
			final Object item = p.getItem();
			if (item != null) {
				s.writeObject(item);
			}
		}
		// Use trailing null as sentinel
		s.writeObject(null);
	}

	/**
	 * Reconstitute the Queue instance from a stream (that is, deserialize it).
	 * 
	 * @param s
	 *            the stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	private void readObject(final java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		// Read in capacity, and any hidden stuff
		s.defaultReadObject();
		head = new Node<E>(null, null);
		tail = head;
		// Read in all elements and place in queue
		for (;;) {
			@SuppressWarnings("unchecked")
			final E item = (E) s.readObject();
			if (item == null) {
				break;
			} else {
				offer(item);
			}
		}
	}
}
