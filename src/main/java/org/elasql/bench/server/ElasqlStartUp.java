/*******************************************************************************
 * Copyright 2016, 2017 elasql.org contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.elasql.bench.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasql.bench.benchmarks.tpcc.ElasqlTpccBenchmarker;
import org.elasql.bench.server.metadata.MicroBenchPartitionPlan;
import org.elasql.bench.server.metadata.TpcePartitionPlan;
import org.elasql.bench.server.migration.tpcc.TpccMigrationMgr;
import org.elasql.bench.server.migration.tpcc.TpccMigrationSystemController;
import org.elasql.bench.server.procedure.calvin.micro.MicrobenchStoredProcFactory;
import org.elasql.bench.server.procedure.calvin.tpcc.TpccStoredProcFactory;
import org.elasql.bench.server.procedure.calvin.tpce.TpceStoredProcFactory;
import org.elasql.migration.MigrationMgr;
import org.elasql.migration.MigrationSystemController;
import org.elasql.procedure.DdStoredProcedureFactory;
import org.elasql.server.Elasql;
import org.elasql.storage.metadata.PartitionPlan;
import org.vanilladb.bench.BenchmarkerParameters;
import org.vanilladb.bench.server.SutStartUp;

public class ElasqlStartUp implements SutStartUp {
	private static Logger logger = Logger.getLogger(ElasqlStartUp.class
			.getName());
	
	private String dbName;
	private int nodeId;
	private boolean isSequencer;

	public void startup(String[] args) {
		if (logger.isLoggable(Level.INFO))
			logger.info("initializing benchmarker server...");
		
		try {
			parseArguments(args);
		} catch (IllegalArgumentException e) {
			System.out.println("Error: " + e.getMessage());
			System.out.println("Usage: ./startup [DB Name] [Node Id] ([Is Sequencer])");
		}
		
		Elasql.init(dbName, nodeId, isSequencer, getStoredProcedureFactory(), getPartitionPlan(),
				getMigrationMgr(), getMigrationSystemController());

		if (logger.isLoggable(Level.INFO))
			logger.info("ElaSQL server ready");
	}
	
	private void parseArguments(String[] args) throws IllegalArgumentException {
		if (args.length < 2) {
			throw new IllegalArgumentException("The number of arguments is less than 2");
		}
		
		// #1 DB Name
		dbName = args[0];
		
		// #2 Node Id
		try {
			nodeId = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(String.format("'%s' is not a number", args[1]));
		}
		
		// #3 Is sequencer ?
		isSequencer = false;
		if (args.length > 2) {
			try {
				int num = Integer.parseInt(args[2]);
				if (num == 1)
					isSequencer = true;
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("'%s' is not a number", args[2]));
			}
		}
	}
	
	private DdStoredProcedureFactory getStoredProcedureFactory() {
		DdStoredProcedureFactory factory = null;
		switch (Elasql.SERVICE_TYPE) {
		case NAIVE:
			factory = getNaiveSpFactory();
			break;
		case CALVIN:
			factory = getCalvinSpFactory();
			break;
		case TPART:
			factory = getTPartSpFactory();
			break;
		}
		return factory;
	}
	
	private DdStoredProcedureFactory getNaiveSpFactory() {
		DdStoredProcedureFactory factory = null;
		switch (BenchmarkerParameters.BENCH_TYPE) {
		case MICRO:
			throw new UnsupportedOperationException("No Micro for now");
		case TPCC:
			throw new UnsupportedOperationException("No TPC-C for now");
		case TPCE:
			throw new UnsupportedOperationException("No TPC-E for now");
		}
		return factory;
	}
	
	private DdStoredProcedureFactory getCalvinSpFactory() {
		DdStoredProcedureFactory factory = null;
		switch (BenchmarkerParameters.BENCH_TYPE) {
		case MICRO:
			if (logger.isLoggable(Level.INFO))
				logger.info("using Micro-benchmark stored procedures for Calvin");
			factory = new MicrobenchStoredProcFactory();
			break;
		case TPCC:
			if (logger.isLoggable(Level.INFO))
				logger.info("using TPC-C stored procedures for Calvin");
			factory = new TpccStoredProcFactory();
			break;
		case TPCE:
			if (logger.isLoggable(Level.INFO))
				logger.info("using TPC-E stored procedures for Calvin");
			factory = new TpceStoredProcFactory();
			break;
		}
		return factory;
	}
	
	private DdStoredProcedureFactory getTPartSpFactory() {
		DdStoredProcedureFactory factory = null;
		switch (BenchmarkerParameters.BENCH_TYPE) {
		case MICRO:
			throw new UnsupportedOperationException("No Micro for now");
		case TPCC:
			throw new UnsupportedOperationException("No TPC-C for now");
		case TPCE:
			throw new UnsupportedOperationException("No TPC-E for now");
		}
		return factory;
	}
	
	private PartitionPlan getPartitionPlan() {
		PartitionPlan partPlan = null;
		switch (BenchmarkerParameters.BENCH_TYPE) {
		case MICRO:
			partPlan = new MicroBenchPartitionPlan();
			break;
		case TPCC:
			partPlan = ElasqlTpccBenchmarker.getPartitionPlan();
			break;
		case TPCE:
			partPlan = new TpcePartitionPlan();
			break;
		}
		return partPlan;
	}
	
	private MigrationMgr getMigrationMgr() {
		MigrationMgr migraMgr = null;
		switch (BenchmarkerParameters.BENCH_TYPE) {
		case MICRO:
			throw new UnsupportedOperationException("No Micro for now");
		case TPCC:
			migraMgr = new TpccMigrationMgr();
			break;
		case TPCE:
			throw new UnsupportedOperationException("No TPC-E for now");
		}
		return migraMgr;
	}
	
	private MigrationSystemController getMigrationSystemController() {
		MigrationSystemController sysCon = null;
		switch (BenchmarkerParameters.BENCH_TYPE) {
		case MICRO:
			throw new UnsupportedOperationException("No Micro for now");
		case TPCC:
			sysCon = new TpccMigrationSystemController();
			break;
		case TPCE:
			throw new UnsupportedOperationException("No TPC-E for now");
		}
		return sysCon;
	}
}
