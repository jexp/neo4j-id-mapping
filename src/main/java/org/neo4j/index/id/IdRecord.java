package org.neo4j.index.id;

import org.neo4j.kernel.impl.nioneo.store.Abstract64BitRecord;

/**
 * @author mh
 * @since 22.12.11
 */
public class IdRecord extends Abstract64BitRecord {

    public static final long NO_ENTITY_ID = -1;

    private long entityId = IdRecord.NO_ENTITY_ID;

    public IdRecord(long id) {
        super(id);
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    @Override
    public String toString() {
        return new StringBuilder("MappedId[").append(getId()).append(",used=").append(inUse()).append(",entity=").append(
                entityId).append("]").toString();
    }
}
