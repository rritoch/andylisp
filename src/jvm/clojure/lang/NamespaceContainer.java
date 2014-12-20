/**
 *   Copyright (c) Ralph Ritoch. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/


// Installation: in Namespaces.java change
//final static ConcurrentHashMap<Symbol, Namespace> namespaces = new ConcurrentHashMap<Symbol, Namespace>();
// TO
//final static NamespaceContainer namespaces = new NamespaceContainer();

package clojure.lang;

import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.AbstractCollection;
import clojure.lang.Var;

public class NamespaceContainer implements Serializable {
	
	final static ConcurrentHashMap<Symbol, Namespace> root = new ConcurrentHashMap<Symbol, Namespace>();
	
	
	final static InheritableThreadLocal<ConcurrentHashMap<Symbol, Namespace>> NAMESPACE_CONTAINER = new InheritableThreadLocal<ConcurrentHashMap<Symbol, Namespace>>() {
		
		boolean inherited = false;
		
		protected ConcurrentHashMap<Symbol, Namespace> childValue(ConcurrentHashMap<Symbol, Namespace> parentValue) {
			//System.out.println("NAMESPACE_CONTAINER.childValue()");
			if (inherited) {
				return this.get();
			}
			inherited = true;

			if (parentValue == null) { 
				this.set(root); 
				return root;
			}
			this.set(parentValue);
		    return parentValue;
		}
		
		protected ConcurrentHashMap<Symbol, Namespace> initialValue() {
			//System.out.println("NAMESPACE_CONTAINER.initialValue()");
			return root;
		}
		
	};
	
	public static ConcurrentHashMap<Symbol, Namespace> getRoot() {
		return root;
	}
	
	public static ConcurrentHashMap<Symbol, Namespace> getCurrent() {
		return NAMESPACE_CONTAINER.get();
	}
	
	//final static Var NAMESPACE_CONTAINER = Var.create(root).setDynamic();
	
	final static ThreadLocal<IPersistentList> prev = new ThreadLocal<IPersistentList>() {
		protected IPersistentList initialValue() {
			return PersistentList.EMPTY;
		}
	};
	
	final static Symbol CLOJURE_NS = Symbol.create("clojure.core");
	
	public Collection<Namespace> values() {
		return ((ConcurrentHashMap<Symbol, Namespace>)NAMESPACE_CONTAINER.get()).values();
	}
	
	public Namespace get(Symbol name) {
		return ((ConcurrentHashMap<Symbol, Namespace>)NAMESPACE_CONTAINER.get()).get(name);	
	}
	
	public Namespace putIfAbsent (Symbol name, Namespace ns) {
		return ((ConcurrentHashMap<Symbol, Namespace>)NAMESPACE_CONTAINER.get()).putIfAbsent(name, ns);
	}
	
	public Namespace remove(Symbol name) {
		return ((ConcurrentHashMap<Symbol, Namespace>)NAMESPACE_CONTAINER.get()).remove(name);
	}
	
	public static NamespaceContainer.Ref enter(Ref r) {
		
		// Verify current namespace in dest
		if (!r.value.containsKey(((Namespace) RT.CURRENT_NS.deref()).getName())) {
			throw new RuntimeException("Current namespace is missing in target container");
		} else {
		
			// Verify deps in dest?
			// Assuming deps are already loaded if current namespace is there.
		
			// Do enter....
			ConcurrentHashMap<Symbol, Namespace> c = ((ConcurrentHashMap<Symbol, Namespace>)NAMESPACE_CONTAINER.get());
			if (c != root) prev.set((IPersistentList)((IPersistentCollection)prev.get()).cons(c));
			
			//if (null == NAMESPACE_CONTAINER.getThreadBinding()) {
			//	Var.pushThreadBindings(RT.map(NAMESPACE_CONTAINER,true));
			//} else {
				NAMESPACE_CONTAINER.set(r.value);
			//}
			return r;
		}
	}
	
	public static NamespaceContainer.Ref enter() {
		ConcurrentHashMap<Symbol, Namespace> c = new ConcurrentHashMap<Symbol, Namespace>();
		c.putIfAbsent(CLOJURE_NS,root.get(CLOJURE_NS));
		// Push current namespace and requires
		PersistentHashSet deps = depends((Namespace) RT.CURRENT_NS.deref(),true);
		Iterator i = deps.iterator();
		while(i.hasNext()) {
			Namespace n = (Namespace)i.next();
			c.putIfAbsent(n.getName(),n);
		}
		return enter(new Ref(c));
	}
	
	public static void exit() {
		IPersistentList pq = prev.get();
		
		if (((Counted)pq).count() > 0) {
			if (pq.peek() == null) {
				NAMESPACE_CONTAINER.set(root);
			} else {
				NAMESPACE_CONTAINER.set((ConcurrentHashMap<Symbol, Namespace>)pq.peek());
			}
			prev.set((IPersistentList)pq.pop());
		} else {
			NAMESPACE_CONTAINER.set(root);
			/*
			if (null != NAMESPACE_CONTAINER.getThreadBinding()) {
				Var.popThreadBindings();
			}
			*/
		}
	}
	
	public static PersistentHashSet depends(Namespace ns, boolean deep) {
		PersistentHashSet out;
		PersistentHashSet in = (PersistentHashSet)PersistentHashSet.EMPTY.cons(ns);
		Collection vals = ((PersistentHashMap)ns.getMappings()).values();
		Iterator vals_i = vals.iterator();
		while(vals_i.hasNext()) {
			Object v = vals_i.next();
			if (v instanceof Var) {
				in = (PersistentHashSet)in.cons(((Var)v).ns);
			}
		}
		if (deep) {
			out = (PersistentHashSet)PersistentHashSet.EMPTY.cons(ns);
			while(in.size() > 0) {
				Namespace ns_c = (Namespace) in.iterator().next();
				if (!out.contains(ns_c)) {
					Iterator d_i = depends(ns_c,false).iterator();
					while(d_i.hasNext()) {
						in = (PersistentHashSet)in.cons(d_i.next());
					}
				}
				out = (PersistentHashSet)out.cons(ns_c);
				in = (PersistentHashSet)in.disjoin(ns_c);
			}
		} else {
			out = in;
		}
		return out;
	}
	
	static class Ref {
		private final ConcurrentHashMap<Symbol, Namespace> value;
		Ref(ConcurrentHashMap<Symbol, Namespace> value) {
			this.value = value;
		}
	}
	
}