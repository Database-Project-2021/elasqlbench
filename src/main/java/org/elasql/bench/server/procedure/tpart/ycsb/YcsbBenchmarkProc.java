package org.elasql.bench.server.procedure.tpart.ycsb;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasql.bench.ycsb.ElasqlYcsbConstants;
import org.elasql.cache.CachedRecord;
import org.elasql.procedure.tpart.TPartStoredProcedure;
import org.elasql.sql.RecordKey;
import org.elasql.storage.metadata.PartitionMetaMgr;
import org.vanilladb.bench.server.param.ycsb.YcsbBenchmarkProcParamHelper;
import org.vanilladb.bench.ycsb.YcsbConstants;
import org.vanilladb.core.sql.Constant;
import org.vanilladb.core.sql.VarcharConstant;

public class YcsbBenchmarkProc extends TPartStoredProcedure<YcsbBenchmarkProcParamHelper> {
	private static Logger logger = Logger.getLogger(YcsbBenchmarkProc.class.getName());

//	private static Map<Integer, RecordKey> keyPool =
//			new ConcurrentHashMap<Integer, RecordKey>();
	
	private static RecordKey[] keyPool = 
			new RecordKey[ElasqlYcsbConstants.RECORD_PER_PART * PartitionMetaMgr.NUM_PARTITIONS];
	
	public static void preloadKeys() {
		if (logger.isLoggable(Level.INFO))
			logger.info("Preloading keys for YCSB...");
		for (int i = 0; i < keyPool.length; i++)
			toRecordKey(i + 1);
		if (logger.isLoggable(Level.INFO))
			logger.info("Preloading keys for YCSB finishes.");
	}
	
	private static RecordKey toRecordKey(int ycsbId) {
		RecordKey key = keyPool[ycsbId - 1];
		if (key == null) {
			String idString = String.format(YcsbConstants.ID_FORMAT, ycsbId);
			key = new RecordKey("ycsb", "ycsb_id", new VarcharConstant(idString));
			keyPool[ycsbId - 1] = key;
		}
		return key;
	}

	private RecordKey[] readKeys;
	private RecordKey[] insertKeys;
	private Map<RecordKey, Constant> writeConstantMap = new HashMap<RecordKey, Constant>();
	
	public YcsbBenchmarkProc(long txNum) {
		super(txNum, new YcsbBenchmarkProcParamHelper());
	}
	
	@Override
	protected void prepareKeys() {
		// set read keys
		readKeys = new RecordKey[paramHelper.getReadCount()];
		for (int i = 0; i < paramHelper.getReadCount(); i++) {
			// create RecordKey for reading
			RecordKey key = toRecordKey(paramHelper.getReadId(i));
			readKeys[i] = key;
			addReadKey(key);
		}
		
		// set write keys
		for (int i = 0; i < paramHelper.getWriteCount(); i++) {
			// create record key for writing
			RecordKey key = toRecordKey(paramHelper.getWriteId(i));
			addWriteKey(key);
			
			// Create key-value pairs for writing
			Constant c = new VarcharConstant(paramHelper.getWriteValue(i));
			writeConstantMap.put(key, c);
		}
		
		// set insert keys
		insertKeys = new RecordKey[paramHelper.getInsertCount()];
		for (int i = 0; i < paramHelper.getInsertCount(); i++) {
			// create record key for inserting
			RecordKey key = toRecordKey(paramHelper.getInsertId(i));
			insertKeys[i] = key;
			addInsertKey(key);
		}
	}
	
	@Override
	protected void executeSql(Map<RecordKey, CachedRecord> readings) {
		CachedRecord rec;
		// SELECT ycsb_id, ycsb_1 FROM ycsb WHERE ycsb_id = ...
		for (int idx = 0; idx < paramHelper.getReadCount(); idx++) {
			rec = readings.get(readKeys[idx]);
			paramHelper.setYcsb((String) rec.getVal("ycsb_1").asJavaVal(), idx);
		}

		// UPDATE ycsb SET ycsb_1 = ... WHERE ycsb_id = ...
		for (Map.Entry<RecordKey, Constant> pair : writeConstantMap.entrySet()) {
			rec = readings.get(pair.getKey());
			rec.setVal("ycsb_1", pair.getValue());
			update(pair.getKey(), rec);
		}
		
		// INSERT INTO ycsb (ycsb_id, ycsb_1, ...) VALUES ("...", "...", ...)
		for (int i = 0; i < paramHelper.getInsertCount(); i++) {
			insert(insertKeys[i], paramHelper.getInsertVals(i));
		}
	}
	
	@Override
	public double getWeight() {
		return paramHelper.getWriteCount() + paramHelper.getReadCount();
	}

}
