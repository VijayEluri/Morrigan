package net.sparktank.sqlitewrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public abstract class GenericSqliteLayer implements IGenericDbLayer {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Instance properties.
	
	private final String dbFilePath;
	private final boolean autoCommit;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Constructors.
	
	protected GenericSqliteLayer (String dbFilePath, boolean autoCommit) throws DbException {
		this.dbFilePath = dbFilePath;
		this.autoCommit = autoCommit;
		
		try {
			initDatabaseTables();
		} catch (Exception e) {
			throw new DbException(e);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
	
	@Override
	public void dispose () {
		try {
			disposeDbCon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Properties.
	
	@Override
	public String getDbFilePath() {
		return this.dbFilePath;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	DB connection.
	
	private Connection dbConnection = null;
	
	private Connection makeConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		String url = "jdbc:sqlite:/" + this.dbFilePath;
		Connection con = DriverManager.getConnection(url);
		
		con.setAutoCommit(this.autoCommit);
//		System.err.println("AutoCommit=" + this.dbConnection.getAutoCommit() + " for '"+getDbFilePath()+"'.");
		
		return con;
	}
	
	protected Connection getDbCon () throws ClassNotFoundException, SQLException {
		if (this.dbConnection==null || this.dbConnection.isClosed()) {
			this.dbConnection = makeConnection();
		}
//		else if (!this.dbConnection.isValid(10)) {
//			this.dbConnection.close();
//			this.dbConnection = makeConnection();
//		}
		
		if (this.dbConnection == null) throw new IllegalArgumentException("Failed to make driver class instance.");
		
		return this.dbConnection;
	}
	
	private void disposeDbCon () throws SQLException {
		if (this.dbConnection!=null) {
			this.dbConnection.close();
		}
	}
	
	@Override
	public void commitOrRollBack () throws DbException {
		try {
			getDbCon().commit();
		}
		catch (Exception e) {
			rollback();
			throw new DbException(e);
		}
	}
	
	@Override
	public void rollback () throws DbException {
		try {
			getDbCon().rollback();
		} catch (Exception e) {
			throw new DbException(e);
		}
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Init.
	
	private void initDatabaseTables () throws SQLException, ClassNotFoundException {
		Statement stat = getDbCon().createStatement();
		
		List<SqlCreateCmd> tblCreateCmds = getTblCreateCmds();
		
		for (SqlCreateCmd sqlCreateCmd : tblCreateCmds) {
			try {
				ResultSet rs;
				rs = stat.executeQuery(sqlCreateCmd.getTblExistsSql());
				try {
					if (!rs.next()) { // True if there are rows in the result.
						stat.executeUpdate(sqlCreateCmd.getTblCreateSql());
					}
				}
				catch (SQLException e) {
					System.err.println("SQLException while executing '"+sqlCreateCmd.getTblCreateSql()+"'.");
					throw e;
				}
				finally {
					rs.close();
				}
			}
			finally {
				stat.close();
			}
		}
		
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	static public class SqlCreateCmd {
		
		final private String tblExistsSql;
		final private String tblCreateSql;
		
		public SqlCreateCmd (String tblExistsSql, String tblCreateSql) {
			this.tblExistsSql = tblExistsSql;
			this.tblCreateSql = tblCreateSql;
		}
		
		public String getTblExistsSql() {
			return this.tblExistsSql;
		}
		
		public String getTblCreateSql() {
			return this.tblCreateSql;
		}
		
	}
	
	protected abstract List<SqlCreateCmd> getTblCreateCmds ();
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

}
