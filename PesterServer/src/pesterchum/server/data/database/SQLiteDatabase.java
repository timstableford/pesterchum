package pesterchum.server.data.database;

import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SQLiteDatabase implements Database{
	public SQLiteDatabase(File database) throws SqlJetException{
		SqlJetDb db = SqlJetDb.open(database, true);
		// set DB option that have to be set before running any transactions: 
		db.getOptions().setAutovacuum(true);
		// set DB option that have to be set in a transaction: 
		db.runTransaction(new ISqlJetTransaction() {
			public Object run(SqlJetDb db) throws SqlJetException {
				db.getOptions().setUserVersion(1);
				return true;
			}
		}, SqlJetTransactionMode.WRITE);
	}
}
