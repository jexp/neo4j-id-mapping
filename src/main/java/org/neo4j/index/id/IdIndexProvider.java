package org.neo4j.index.id;

import org.neo4j.graphdb.index.IndexImplementation;
import org.neo4j.graphdb.index.IndexProvider;
import org.neo4j.index.id.impl.IdIndexImplementation;
import org.neo4j.index.impl.lucene.LuceneIndexImplementation;
import org.neo4j.kernel.KernelData;

/**
 * @author mh
 * @since 22.12.11
 */
public class IdIndexProvider extends IndexProvider {

    public IdIndexProvider() {
        super(IdIndexImplementation.SERVICE_NAME);
    }

    @Override
    public IndexImplementation load(KernelData kernel) throws Exception {
        return new IdIndexImplementation(kernel.graphDatabase(), kernel.getConfig() );
    }
}
