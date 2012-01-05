package org.neo4j.index.id.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexImplementation;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.Config;

import java.util.Collections;
import java.util.Map;

/**
 * @author mh
 * @since 22.12.11
 */
public class IdIndexImplementation extends IndexImplementation {
    public static final String SERVICE_NAME = "id-mapping";
    public static final Map<String, String> INDEX_CONFIG = Collections.unmodifiableMap(MapUtil.stringMap(IndexManager.PROVIDER, SERVICE_NAME));

    private final GraphDatabaseService gdb;

    public IdIndexImplementation(GraphDatabaseService gdb, Config config) {
        this.gdb = gdb;
    }

    @Override
    public String getDataSourceName() {
        return null;
    }

    @Override
    public Index<Node> nodeIndex(String indexName, Map<String, String> config) {
        return new IdNodeIndex(indexName,config, gdb);
    }

    @Override
    public RelationshipIndex relationshipIndex(String indexName, Map<String, String> config) {
        return null;
    }

    @Override
    public Map<String, String> fillInDefaults(Map<String, String> config) {
        return config;
    }

    @Override
    public boolean configMatches(Map<String, String> storedConfig, Map<String, String> config) {
        return storedConfig.equals(config);
    }
}
