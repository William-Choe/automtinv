package com.neu.mtinv.util;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class MysqlCon {
	String sDBDriver;
	String url;
	String user;
	String password;
	private Connection conn;
	private Statement stmt;
	ResultSet rs;


	public MysqlCon(String ip, String uu, String pass, String sid) {
		sDBDriver = "com.mysql.jdbc.Driver";
//		sDBDriver = "com.mysql.cj.jdbc.Driver";
		url = "jdbc:mysql://" + ip + ":3306/" + sid + "?useUnicode=true&characterEncoding=gbk&serverTimezone=UTC&useSSL=false";
		user = uu;
		password = pass;
		rs = null;
		try {
			Class.forName(sDBDriver);
		} catch (ClassNotFoundException classnotfoundexception) {
			log.error("TrpasCon() error1:", classnotfoundexception);
		}
		try {
			conn = DriverManager.getConnection(url, user, password);
			stmt = conn.createStatement();
		} catch (SQLException sqlexception) {
			log.error("TrpasCon() error2:", sqlexception);
		}
	}

	
	public ResultSet executeQuery(String sql) throws SQLException {
		return stmt.executeQuery(sql);
	}

	
	public Statement getStmt() {
		return this.stmt;
	}

	
	public Connection getCon() {
		return this.conn;
	}

	
	public void setCommit() throws SQLException {
		conn.setAutoCommit(false);
	}
	
	
	public void rollback() throws SQLException {
		conn.rollback();
	}

	
	public int executeUpdate(String sql) throws SQLException {
		int count = stmt.executeUpdate(sql);
		return count;
	}
	
	public boolean execute(String sql) throws SQLException {
		boolean tof = stmt.execute(sql);
		return tof;
	}

	
	public void commit() throws SQLException {
		conn.commit();
	}

	
	public void close() {

		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
			log.error("rs ERR !", e);
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (Exception e) {
			log.error("stmt ERR !", e);
		}
		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			log.error("conn ERR !", e);
		}
	}
	

}