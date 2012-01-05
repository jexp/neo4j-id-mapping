package org.neo4j.index.id.impl;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author mh
 * @since 22.12.11
 */
public class WrappingIndexHits<T extends PropertyContainer> implements IndexHits<T> {

    private final Collection<T> delegate;
    private Iterator<T> mainIterator;

    public WrappingIndexHits(Collection<T> delegate) {
        this.delegate = delegate;
        this.mainIterator = delegate.iterator();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void close() {
    }

    @Override
    public T getSingle() {
        final Iterator<T> it = iterator();
        return it.hasNext() ? it.next() : null;
    }

    @Override
    public float currentScore() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean hasNext() {
        return mainIterator.hasNext();
    }

    @Override
    public T next() {
       return mainIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
