package org.elasql.bench.server.procedure.tpart;

import java.util.Map;

import org.elasql.cache.CachedRecord;
import org.elasql.procedure.tpart.TPartStoredProcedure;
import org.elasql.server.Elasql;
import org.elasql.sql.PrimaryKey;
import org.vanilladb.core.sql.storedprocedure.StoredProcedureParamHelper;

public class StartProfilingProc extends TPartStoredProcedure<StoredProcedureParamHelper> {

	public StartProfilingProc(long txNum) {
		super(txNum, StoredProcedureParamHelper.newDefaultParamHelper());
	}

	@Override
	protected void prepareKeys() {
		// do nothing

	}

	@Override
	protected void executeSql(Map<PrimaryKey, CachedRecord> readings) {
		Elasql.stopProfilerAndReport();
	}
	
	@Override
	public ProcedureType getProcedureType() {
		return ProcedureType.UTILITY;
	}
	
	@Override
	public double getWeight() {
		return 0;
	}

	@Override
	public int getCacheWriteNum(){
		return 0;
	}

	@Override
	public int getCacheInsertNum(){
		return 0;
	}
}

