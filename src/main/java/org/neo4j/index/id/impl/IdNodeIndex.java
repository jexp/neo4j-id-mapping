package org.neo4j.index.id.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.id.IdRecord;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.impl.nioneo.store.IdStore;

import java.util.Collections;
import java.util.Map;

/**
 * @author mh
 * @since 22.12.11
 */
public class IdNodeIndex implements Index<Node> {
    private static final WrappingIndexHits<Node> EMPTY_INDEX_HITS = new WrappingIndexHits<Node>(Collections.<Node>emptyList());
    private final IdStore idStore;

    private final String indexName;
    private final GraphDatabaseService gdb;

    public IdNodeIndex(String indexName, Map<String, String> indexConfig, GraphDatabaseService gdb) {
        this.indexName = indexName;
        this.gdb = gdb;
        final Map<Object, Object> storeConfig = ((AbstractGraphDatabase) gdb).getConfig().getParams();
        idStore = IdStore.createStore(indexName, storeConfig);
    }

    /**
     * @param entity node to be indexed
     * @param key is ignored
     * @param value is assumed to be a numeric value, will be converted to long
     */
    public void add(Node entity, String key, Object value) {
        final long mappedId = mappedId(value);
        assureIdAvailable(mappedId);
        IdRecord record = idStore.getRecord(mappedId);
        if (record==null) {
            record = new IdRecord(mappedId);
        }
        record.setInUse(true);
        record.setEntityId(entity.getId());
        idStore.updateRecord(record);
    }

    private void assureIdAvailable(long mappedId) {
        long delta = mappedId - idStore.getHighId();
        for (long i=0;i<delta;i++) {
            idStore.nextId(); // todo? setHighId  nextIdBatch
        }
    }

    private long mappedId(Object value) {
        return ((Number) value).longValue();
    }

    public void remove(Node entity, String key, Object value) {
        final IdRecord record = idStore.getRecord(mappedId(value));
        if (record!=null) {
            record.setInUse(false);
            record.setEntityId(IdRecord.NO_ENTITY_ID);
            idStore.updateRecord(record);
        }
    }

    public void remove(Node entity, String key) {
        throw new UnsupportedOperationException();
    }

    public void remove(Node entity) {
        throw new UnsupportedOperationException();
    }

    public void delete() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return indexName;
    }

    public Class<Node> getEntityType() {
        return Node.class;
    }

    public IndexHits<Node> get(String key, Object value) {
        final IdRecord record = idStore.getRecord(mappedId(value));
        if (record!=null && record.inUse()) {
            long nodeId = record.getEntityId();
            return new WrappingIndexHits<Node>(Collections.singleton(gdb.getNodeById(nodeId)));
        }
        return EMPTY_INDEX_HITS;
    }

    public IndexHits<Node> query(String key, Object queryOrQueryObject) {
        throw new UnsupportedOperationException();
    }

    public IndexHits<Node> query(Object queryOrQueryObject) {
        throw new UnsupportedOperationException();
    }

    public boolean isWriteable() {
        return true;
    }
}
