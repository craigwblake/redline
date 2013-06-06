package org.freecompany.redline;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.NUMBER;

public class XmlEditor implements Serializable {
	static final long serialVersionUID = 1L;

	private transient Stack< Node> context = new Stack< Node>();
	private transient boolean modified;
	private transient XPath xpath;

	public XmlEditor() {}
	public XmlEditor( Document document) { setDocument( document); }

	public void setDocument( Document document) {
		context.push( document.getDocumentElement());
		this.modified = false;
	}
	public Document getDocument() { return context.peek().getOwnerDocument(); }

	public void pushContext( Node node) { context.push( node); }
	public Node popContext() { return context.pop(); }
	public void clearContext() { while ( context.size() > 1) context.pop(); }

	public void startPrefixMapping( String uri, String prefix) {
		NAMESPACE_CONTEXT.addNamespace( uri, prefix);
	}

	public boolean isModified() { return modified; }
	public void setModified( boolean modified) { this.modified = modified; }

	public void read( File file) throws SAXException, IOException {
		setDocument( readDocument( file));
	}

	public void read( InputStream in) throws SAXException, IOException {
		setDocument( readDocument( in));
	}

	public void write( File file) throws IOException {
		modified = false;
		write( context.peek().getOwnerDocument(), file);
	}

	public void write( OutputStream out) throws IOException { modified = false; write( context.peek().getOwnerDocument(), out); }

	public void write( ContentHandler handler) throws IOException {
		try {
			TransformerFactory.newInstance().newTransformer().transform( new DOMSource( context.peek().getOwnerDocument()), new SAXResult( handler));
			modified = false;
		} catch ( TransformerConfigurationException e) {
			assert false;
			throw new RuntimeException( "no transformer", e);
		} catch ( TransformerException e) {
			Throwable t = e.getCause();
			if ( t instanceof IOException) throw ( IOException) t;
			assert false;
			throw new RuntimeException( "transformer failed", e);
		}
	}

	public Node createNode( String path) {
		modified = true;
		return findNode( path, true);
	}

	public Node findNode( String path) {
		return findNode( path, false);
	}

	private Node findNode( String path, boolean create) {
		try {
			Node result = ( Node) xpath( path).evaluate( context.peek(), NODE);
			if ( !create || result != null) return result;

			int pos = path.lastIndexOf( '/');
			Element parent;
			String name;
			if ( pos >= 0) {
				parent = ( Element) findNode( path.substring( 0, pos), true);
				name = path.substring( pos + 1);
			} else {
				parent = ( Element) context.peek();
				name = path;
			}
			return newNode( parent, name);
		} catch ( XPathExpressionException e) {
			throw new RuntimeException( path, e);
		}
	}

	public Iterable< Node> findNodes( String path) {
		try {
			final NodeList list = ( NodeList) xpath( path).evaluate( context.peek(), NODESET);
			return new Iterable< Node>() {
				public Iterator< Node> iterator() {
					return new Iterator< Node>() {
						int count;
						public boolean hasNext() { return count != list.getLength(); }
						public Node next() { return list.item( count++); }
						public void remove() { throw new UnsupportedOperationException(); }
					};
				}
			};
		} catch ( XPathExpressionException e) {
			throw new RuntimeException( path, e);
		}
	}

	public void setValue( String path, String value) {
		modified = true;
		setValue( createNode( path), value);
	}

	public String getValue( String path) {
		Node n = findNode( path);
		if ( n == null) return null;
		return n.getNodeValue();
	}

	public Number getValueAsNumber( String path) {
		try {
			return ( Number) xpath( path).evaluate( context.peek(), NUMBER);
		} catch ( XPathExpressionException e) {
			throw new RuntimeException( path, e);
		}
	}

	public String getValue( String path, String def) {
		String value = getValue( path);
		return value == null ? def : value;
	}
	
	public boolean getBoolean( String path) { return getBoolean( path, false); }
	
	public boolean getBoolean( String path, boolean def) {
		String value = getValue( path);
		return value == null ? def : Boolean.valueOf( value);
	}
	
	public int getInteger( String path) { return getInteger( path, 0); }
	
	public int getInteger( String path, int def) {
		String value = getValue( path);
		return value == null ? def : Integer.parseInt( value);
	}

	private URI getBaseURIFor( Node node) {
		try {
			return new URI( xpath( "ancestor-or-self::*/@xml:base").evaluate( node));
		} catch ( XPathExpressionException e) {
			assert false;
		} catch ( URISyntaxException e) {
			assert true;
		}
		return null;
	}

	public URI getBaseURI() {
		return getBaseURIFor( context.peek());
	}

	public URI getValueAsURI( String path) {
		try {
			String result = getValue( path);
			if ( result == null || "".equals( result)) return null;
			return new URI( result);
		} catch ( URISyntaxException e) {
			return null;
		}
	}

	public URI getValueAsFullURI( String path) {
		URI base = getBaseURIFor( findNode( path));
		URI result = getValueAsURI( path);
		if ( result != null && base != null) result = base.resolve( result);
		return result;
	}

	public void deleteNodes( String path) {
		modified = true;
		try {
			NodeList list = ( NodeList) xpath( path).evaluate( context.peek(), NODESET);
			int n = list.getLength();
			for ( int i = 0; i < n; i++) {
				Node node = list.item( i);
				if ( node instanceof Attr) {
					Attr attr = ( Attr) node;
					attr.getOwnerElement().removeAttributeNode( attr);
				} else {
					node.getParentNode().removeChild( node);
				}
			}
		} catch ( XPathExpressionException e) {
			throw new RuntimeException( path, e);
		}
	}

	public Node appendNode( String path) {
		modified = true;
		int pos = path.lastIndexOf( '/');
		if ( pos >= 0) return newNode( appendNode( path.substring( 0, pos)), path.substring( pos + 1));
		return newNode( path);
	}

	public void appendCollection( String nodePath, Collection values) {
		modified = true;
        for (Object value : values) {
            setValue(appendNode(nodePath), value.toString());
        }
	}

	public void replaceCollection( String nodePath, Collection values) {
		modified = true;
		deleteNodes( nodePath);
		appendCollection( nodePath, values);
	}

	@SuppressWarnings( "unchecked")
	public Collection nodesToText( String path, Collection result) {
		Iterable< Node> nodes = findNodes( path);
		for ( Node node : nodes) {
			String string = normalizeString( node.getNodeValue());
			if ( string == null) continue;
			result.add( string);
		}
		return result;
	}

	public ContentHandler contentHandler() {
		return new ContentHandler() {
			private Node current = context.peek();
			private Document doc = context.peek().getOwnerDocument();

			public void characters( char[] ch, int start, int length) {
				String s = new String( ch, start, length);
				if ( current instanceof Text) {
					(( Text) current).appendData( s);
				} else {
					Text text = doc.createTextNode( s);
					current.appendChild( text);
					current = text;
				}
			}

			public void startElement( String namespaceURI, String localName, String qName, Attributes atts) {
				Element element = doc.createElementNS( namespaceURI, qName);
				int n = atts.getLength();
				for ( int i = 0; i < n; i++) {
					element.setAttribute( atts.getQName( i), atts.getValue( i));
				}
				if ( current instanceof Text) {
					current.getParentNode().appendChild( element);
				} else {
					current.appendChild( element);
				}
				current = element;
			}
			public void endElement( String namespaceURI, String localName, String qName) {
				if ( current instanceof Text) current = current.getParentNode();
				current = current.getParentNode();
			}

			public void processingInstruction( String target, String data) {
				ProcessingInstruction pi = doc.createProcessingInstruction( target, data);
				current.getParentNode().appendChild( pi);
				current = pi;
			}

			public void startDocument() {}
			public void endDocument() {}
			public void startPrefixMapping( String prefix, String uri) {}
			public void endPrefixMapping( String prefix) {}
			public void ignorableWhitespace( char[] ch, int start, int length) {}
			public void setDocumentLocator( Locator locator) {}
			public void skippedEntity( String name) {}
		};
	}
	public Node newNode( String name) { return newNode( context.peek(), name); }

	// Static API follows
	public static Document readDocument( File file) throws SAXException, IOException {
		InputStream in = new FileInputStream( file);
		Document result = readDocument( in);
		in.close();
		return result;
	}

	public static Document readDocument( InputStream in) throws SAXException, IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware( true);
			return factory.newDocumentBuilder().parse( in);
		} catch ( ParserConfigurationException e) {
			assert false;
			throw new RuntimeException( "usable DOM parser", e);
		}
	}

	public static void write( Document doc, File file) throws IOException {
		OutputStream out = new FileOutputStream( file);
		write( doc, out);
		out.close();
	}

	public static void write( Document doc, OutputStream out) throws IOException {
		try {
			doc.normalize();
			Source source = new DOMSource( doc);
			Result result = new StreamResult( out);
			Transformer xslt = TransformerFactory.newInstance().newTransformer();
			xslt.setOutputProperty( OutputKeys.ENCODING, "iso-8859-1");
			xslt.setOutputProperty( OutputKeys.INDENT, "yes");
			xslt.transform( source, result);
		} catch ( TransformerConfigurationException e) {
			assert false;
			throw new RuntimeException( "no transformer", e);
		} catch ( TransformerException e) {
			Throwable t = e.getCause();
			if ( t instanceof IOException) throw ( IOException) t;
			assert false;
			throw new RuntimeException( "transformer failed", e);
		}
	}

	private static void setValue( Node node, String value) {
		if ( node instanceof Text) {
			node.setNodeValue( value);
		} else if ( node instanceof Attr) {
			if ( value == null) {
				Attr attr = ( Attr) node;
				attr.getOwnerElement().removeAttributeNode( attr);
			} else {
				node.setNodeValue( value);
			}
		} else {
			deleteNodeAndFollowingSiblings( node.getFirstChild());
			if ( value != null) node.appendChild( node.getOwnerDocument().createTextNode( value));
		}
	}

	private static void deleteNodeAndFollowingSiblings( Node node) {
		if ( node == null) return;
		Node parent = node.getParentNode();
		while ( true) {
			Node sib = node.getNextSibling();
			if ( sib == null) break;
			parent.removeChild( sib);
		}
		parent.removeChild( node);
	}

	private static Node newNode( Node parent, String name) {
		Document doc = parent.getOwnerDocument();
		if ( "text()".equals( name)) {
			Text child = doc.createTextNode( "");
			parent.appendChild( child);
			return child;
		}
		if ( name.charAt( 0) == '@') {
			name = name.substring( 1);
			Attr attr = doc.createAttributeNS( mapNamespacePrefix( name), name);
			(( Element) parent).setAttributeNode( attr);
			return attr;
		}
		Element child = doc.createElementNS( mapNamespacePrefix( name), name);
		parent.appendChild( child);
		return child;
	}

	private static String mapNamespacePrefix( String name) {
		int pos = name.indexOf( ':');
		if ( pos < 0) return null;
		String prefix = name.substring( 0, pos);
		return NAMESPACE_CONTEXT.getNamespaceURI( prefix);
	}

	public static String normalizeString( String string) {
		if ( string == null) return null;
		string = string.trim();
		if ( "".equals( string)) return null;
		return string;
	}

	private void writeObject( final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		long length;
		CountingOutputStream buf = new CountingOutputStream();
		write( buf);
		length = buf.size();
		out.writeLong( length);
		write( out);
	}

	private void readObject( final ObjectInputStream in) throws IOException, ClassNotFoundException {
		try {
			in.defaultReadObject();
			read( new InputStream() {
					private long length = in.readLong();
					public int read() throws IOException {
						if ( length <= 0) return -1;
						length--;
						return in.read();
					}
					public int read( byte[] b) throws IOException {
						return read( b, 0, b.length);
					}
					public int read( byte[] b, int off, int len) throws IOException {
						if ( length <= 0) return -1;
						if ( len > length) len = ( int) length;
						int result = in.read( b, off, len);
						length -= result;
						return result;
					}
					public long skip( long n) throws IOException {
						if ( length <= 0) return 0;
						if ( n > length) n = length;
						long result = in.skip( n);
						length -= result;
						return result;
					}
					});
		} catch ( SAXException e) {
			IOException x = new IOException();
			x.initCause( e);
			throw x;
		}
	}

	private static final SimpleNamespaceContext NAMESPACE_CONTEXT = new SimpleNamespaceContext();
	static {
		NAMESPACE_CONTEXT.addNamespace( "http://www.w3.org/XML/1998/namespace", "xml");
	}

	private XPathExpression xpath( String path) throws XPathExpressionException {
		if ( xpath == null) xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext( NAMESPACE_CONTEXT);
		return xpath.compile( path);
	}

	private static class CountingOutputStream extends FilterOutputStream {
		private long count;
		public CountingOutputStream() { super( null); }
		public CountingOutputStream( OutputStream out) { super( out); }
		public void write( int b) throws IOException {
			count++;
			if ( out != null) out.write( b);
		}
		public void write( byte[] b, int off, int len) throws IOException {
			count += len;
			if ( out != null) out.write( b, off, len);
		}
		public void flush() throws IOException {
			if ( out != null) out.flush();
		}
		public long size() { return count; }
	}
}
