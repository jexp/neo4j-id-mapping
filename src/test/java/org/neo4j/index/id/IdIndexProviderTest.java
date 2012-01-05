package org.neo4j.index.id;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.id.impl.IdIndexImplementation;
import org.neo4j.test.ImpermanentGraphDatabase;

import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 04.01.12
 */
public class IdIndexProviderTest {

    private ImpermanentGraphDatabase gdb;
    private Index<Node> index;

    @Before
    public void setUp() throws Exception {
        gdb = new ImpermanentGraphDatabase();
        index = gdb.index().forNodes("nodes", IdIndexImplementation.INDEX_CONFIG);
    }

    @Test
    public void testAddToIndex() {
        index.add(gdb.getReferenceNode(),null,100);
        assertEquals(gdb.getReferenceNode(),index.get(null,100).getSingle());
    }
}
