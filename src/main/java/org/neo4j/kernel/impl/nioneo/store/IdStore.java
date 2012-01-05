package org.neo4j.kernel.impl.nioneo.store;

import org.neo4j.index.id.IdRecord;
import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.impl.util.StringLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @since 22.12.11
 */
public class IdStore extends AbstractStore implements Store, RecordStore<IdRecord> {
    public static final String TYPE_DESCRIPTOR = "IdStore";

    // in_use(byte)+entity_id(int)
    public static final int RECORD_SIZE = 5;

    private IdStore(String fileName, Map<?, ?> config) {
        super(fileName, config, IdType.NODE);
    }

    private static File getStoreFile(String fileName, Map<?, ?> config) {
        // neo_store
        final File file = new File(new File(new File(config.get("store_dir").toString(), "index"), "id-mapping"), fileName + ".db");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    @Override
    public void accept(RecordStore.Processor processor, IdRecord record) {
        // TODO processor.processRecord(this, record);
    }

    @Override
    public String getTypeDescriptor() {
        return TYPE_DESCRIPTOR;
    }

    @Override
    public int getRecordSize() {
        return RECORD_SIZE;
    }

    @Override
    public int getRecordHeaderSize() {
        return getRecordSize();
    }

    /**
     * Creates a new node store contained in <CODE>fileName</CODE> If filename
     * is <CODE>null</CODE> or the file already exists an
     * <CODE>IOException</CODE> is thrown.
     *
     * @param fileName File name of the new node store
     * @param config   Map of configuration parameters
     */
    public static IdStore createStore(String fileName, Map<?, ?> config) {
        IdGeneratorFactory idGeneratorFactory = (IdGeneratorFactory) config.get(IdGeneratorFactory.class);
        final File storeFile = getStoreFile(fileName, config);
        if (!storeFile.exists()) {
            createEmptyStore(storeFile.getAbsolutePath(), buildTypeDescriptorAndVersion(TYPE_DESCRIPTOR), idGeneratorFactory);
        }
        return new IdStore(storeFile.getAbsolutePath(),config);
    }

    public IdRecord getRecord(long id) {
        PersistenceWindow window = acquireWindow(id, OperationType.READ);
        try {
            return getRecord(id, window, RecordLoad.CHECK);
        } finally {
            releaseWindow(window);
        }
    }

    @Override
    public IdRecord forceGetRecord(long id) {
        PersistenceWindow window = null;
        try {
            window = acquireWindow(id, OperationType.READ);
        } catch (InvalidRecordException e) {
            return new IdRecord(id); // inUse=false by default
        }

        try {
            return getRecord(id, window, RecordLoad.FORCE);
        } finally {
            releaseWindow(window);
        }
    }

    public void updateRecord(IdRecord record, boolean recovered) {
        assert recovered;
        setRecovered();
        try {
            updateRecord(record);
            registerIdFromUpdateRecord(record.getId());
        } finally {
            unsetRecovered();
        }
    }

    @Override
    public void forceUpdateRecord(IdRecord record) {
        PersistenceWindow window = acquireWindow(record.getId(),
                OperationType.WRITE);
        try {
            updateRecord(record, window, true);
        } finally {
            releaseWindow(window);
        }
    }

    public void updateRecord(IdRecord record) {
        PersistenceWindow window = acquireWindow(record.getId(),
                OperationType.WRITE);
        try {
            updateRecord(record, window, false);
        } finally {
            releaseWindow(window);
        }
    }

    private IdRecord getRecord(long id, PersistenceWindow window,
                               RecordLoad load) {
        Buffer buffer = window.getOffsettedBuffer(id);

        // [    ,   x] in use bit
        // [    ,xxx ] higher bits for entity id
        // [    ,    ]
        long inUseByte = buffer.get();

        boolean inUse = (inUseByte & 0x1) == Record.IN_USE.intValue();
        if (!inUse) {
            switch (load) {
                case NORMAL:
                    throw new InvalidRecordException("Record[" + id + "] not in use");
                case CHECK:
                    return null;
            }
        }

        long entityId = buffer.getUnsignedInt();

        long entityIdModifier = (inUseByte & 0xEL) << 31;

        IdRecord IdRecord = new IdRecord(id);
        IdRecord.setInUse(inUse);
        IdRecord.setEntityId(longFromIntAndMod(entityId, entityIdModifier));
        return IdRecord;
    }

    private void updateRecord(IdRecord record, PersistenceWindow window, boolean force) {
        long id = record.getId();
        Buffer buffer = window.getOffsettedBuffer(id);
        if (record.inUse() || force) {
            long entityId = record.getEntityId();

            short entityIdModifier = entityId == IdRecord.NO_ENTITY_ID ? 0 : (short) ((entityId & 0x700000000L) >> 31);

            // [    ,   x] in use bit
            // [    ,xxx ] higher bits for entity id
            short inUseUnsignedByte = (record.inUse() ? Record.IN_USE : Record.NOT_IN_USE).byteValue();
            inUseUnsignedByte = (short) (inUseUnsignedByte | entityIdModifier);
            buffer.put((byte) inUseUnsignedByte).putInt((int) entityId);
        } else {
            buffer.put(Record.NOT_IN_USE.byteValue());
            if (!isInRecoveryMode()) {
                freeId(id);
            }
        }
    }

    @Override
    public List<WindowPoolStats> getAllWindowPoolStats() {
        List<WindowPoolStats> list = new ArrayList<WindowPoolStats>();
        list.add(getWindowPoolStats());
        return list;
    }

    @Override
    public void logIdUsage(StringLogger logger) {
        NeoStore.logIdUsage(logger, this);
    }
}
