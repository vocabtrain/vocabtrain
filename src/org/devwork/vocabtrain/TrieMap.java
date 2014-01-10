package org.devwork.vocabtrain;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;



public class TrieMap implements Map<String,String>
{
	private int size = 0;
	private Node root = new Node(' ', null);
	
	public TrieMap()
	{
	}

	private class Node
	{
		private char key;
		private String value;
		private Collection<Node> children = new LinkedList<Node>();
		
		Node(char key, String data)
		{
			this.key = key;
			this.value = data;
		}
		
		Node getChild(char data)
		{
			if(children == null) return null;
			
			for(Node child : children)
			{
				if(child.key == data)
					return child;
			}
			return null;
		}
	}


	@Override
	public void clear() 
	{
		root = null;
	}

	@Override
	public boolean containsKey(Object key) 
	{
		if(! (key instanceof String)) 
			throw new ClassCastException("Cannot cast " + key.getClass() + " to String!");
		return containsKey((String) key);
	}
	
	public boolean containsKey(String key) 
	{
		return getNode(key) == null ? false : true;
	}
	

	@Override
	public boolean containsValue(Object value) 
	{
		return values().contains(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() 
	{
		//TODO
		return null;
	}

	@Override
	public String get(Object key) throws ClassCastException
	{
		if(! (key instanceof String)) 
			throw new ClassCastException("Cannot cast " + key.getClass() + " to String!");
		return get((String) key); 
	
	}
	public String get(String key)
	{
		Node n = getNode(key);
		return n == null ? null : n.value;
	}
	private Node getNode(String key)
	{
		Node current = root;

		for(int i = 0; i < key.length(); ++i)
		{
			if(current.getChild(key.charAt(i)) == null) return null;
			current = current.getChild(key.charAt(i));
		}
		return current;
	}
	public String getTokenized(String key)
	{
		StringBuilder result = new StringBuilder();
		Node current = root;
		Node parent = root;
		if(key == null) return null;
		for(int i = 0; i < key.length(); ++i)
		{
			if(current.getChild(key.charAt(i)) == null)	
			{
				if(parent == current || current.value == null)
					result.append(key.charAt(i));
				else 
				{
					result.append(current.value);
					--i;
				}
				current = parent = root;
				continue;
			}
			parent = current;
			current = current.getChild(key.charAt(i));
		}
		if(current != null && current.value != null)
			result.append(current.value);
		return result.toString();
	}
	

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Set<String> keySet() 
	{
		Set<String> collection = new java.util.HashSet<String>();
		keys(collection);
		return collection;
	}
	private void keys(Collection<String> collection)
	{
		keys(collection, root, "");
	}
	private void keys(Collection<String> collection, Node current, String key)
	{
		Iterator<Node> it = current.children.iterator();
		while(it.hasNext())
		{
			Node n = it.next();
			if(n.value != null)
				collection.add(key + n.key);
			keys(collection, n, key + n.key);
		}
	}
	
	

	@Override
	public String put(String key, String value) 
	{
			assert key.length() != 0;
			Node current = root;
			for(int i = 0; i < key.length(); ++i)
			{
				Node child = current.getChild(key.charAt(i));
				if(child != null) current = child;
				else
				{
					Node n = new Node(key.charAt(i), null); 
					current.children.add(n);
					current = n; 
				}
			}
			String pre = current.value;
			if(pre == null) ++size;
			current.value = value;
			return pre;
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		Iterator<? extends String> key = m.keySet().iterator();
		Iterator<? extends String> value = m.values().iterator();
		while(key.hasNext() && value.hasNext())
		{
			put(key.next(), value.next());
		}
		
	}

	@Override
	public String remove(Object key) throws ClassCastException
	{
		if(! (key instanceof String)) 
			throw new ClassCastException("Cannot cast " + key.getClass() + " to String!");
		return remove((String) key);
	}
	
	public String remove(String key) 
	{
		assert key.length() != 0;
		
		LinkedList<Node> nodes = new LinkedList<Node>();
		{
			nodes.add(root);
			Node current = root;
			for(int i = 0; i < key.length(); ++i)
			{	
				if(current.getChild(key.charAt(i)) == null) break;
				current = current.getChild(key.charAt(i));
				nodes.addFirst(current);
			}
		}
		if(nodes.isEmpty()) return null;
		if(nodes.element().value == null) return null;
		
		Iterator<Node> it = nodes.iterator();
		Node child = it.next();
		String prevValue = child.value;
		child.value = null;
		while(it.hasNext())
		{
			child = it.next();
			if(child.children.size() != 0) break; 
			Node parent = child;
			parent.children.remove(child);
		}
		return prevValue;
	}
	

	@Override
	public int size() 
	{
		return size;
	}

	@Override
	public Collection<String> values() 
	{
		LinkedList<String> collection = new LinkedList<String>();
		values(collection);
		return collection;
	}
	
	private void values(Collection<String> collection)
	{
		values(collection, root);
	}
	private void values(Collection<String> collection, Node current)
	{
		Iterator<Node> it = current.children.iterator();
		while(it.hasNext())
		{
			Node n = it.next();
			if(n.value != null)
				collection.add(n.value);
			values(collection, n);
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		LinkedList<String> values = new LinkedList<String>();
		LinkedList<String> keys = new LinkedList<String>();
		values(values);
		keys(keys);
		
		Iterator<String> valuesit = values.iterator();
		Iterator<String> keysit = keys.iterator();
		while(valuesit.hasNext())
		{
			sb.append(keysit.next());
			sb.append(" : ");
			sb.append(valuesit.next());
			sb.append("\n");
		}
		return sb.toString();
	}
	
}

