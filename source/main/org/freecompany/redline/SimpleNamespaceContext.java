package org.freecompany.redline;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.XMLConstants.XML_NS_PREFIX;
import static javax.xml.XMLConstants.XML_NS_URI;

public class SimpleNamespaceContext implements NamespaceContext {
	protected Map< String, String> map = new HashMap< String, String>();

	public void addNamespace( String namespaceURI, String prefix) {
		map.put( prefix, namespaceURI);
	}

	public void removeNamespace( String prefix) {
		map.remove( prefix);
	}

	public String getNamespaceURI( String prefix) {
		if ( prefix == null) throw new IllegalArgumentException( prefix);
		String result = map.get( prefix);
		if ( result != null) return result;
		if ( DEFAULT_NS_PREFIX.equals( prefix)) return NULL_NS_URI;
		if ( XML_NS_PREFIX.equals( prefix)) return XML_NS_URI;
		if ( XMLNS_ATTRIBUTE.equals( prefix)) return XMLNS_ATTRIBUTE_NS_URI;
		return NULL_NS_URI;
	}

	protected String commonPrefixCheck( String namespaceURI, boolean defaultNS) {
		if ( namespaceURI == null) throw new IllegalArgumentException( namespaceURI);
		if ( defaultNS && getNamespaceURI( DEFAULT_NS_PREFIX).equals( namespaceURI)) return DEFAULT_NS_PREFIX;
		if ( XML_NS_URI.equals( namespaceURI)) return XML_NS_PREFIX;
		if ( XMLNS_ATTRIBUTE_NS_URI.equals( namespaceURI)) return XMLNS_ATTRIBUTE;
		return null;
	}

	public String getPrefix( String namespaceURI) {
		String result = commonPrefixCheck( namespaceURI, true);
		if ( result == null) {
			Iterator i = prefixIterator( namespaceURI);
			if ( i.hasNext()) return ( String) i.next();
		}
		return null;
	}

	public Iterator< String> getPrefixes( String namespaceURI) {
		String result = commonPrefixCheck( namespaceURI, false);
		if ( result == null && getNamespaceURI( DEFAULT_NS_PREFIX).equals( namespaceURI)) result = DEFAULT_NS_PREFIX;
		if ( result != null) return Collections.singleton( result).iterator();
		return prefixIterator( namespaceURI);
	}

	protected Iterator< String> prefixIterator( final String namespaceURI) {
		return new Iterator< String>() {
			private Iterator< Map.Entry< String, String>> iterator = map.entrySet().iterator();
			public boolean hasNext() {
				while ( iterator.hasNext()) {
					if ( namespaceURI.equals( iterator.next().getValue())) return true;
				}
				return false;
			}
			public String next() {
				return iterator.next().getKey();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
