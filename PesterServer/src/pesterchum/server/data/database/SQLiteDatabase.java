package pesterchum.server.data.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import pesterchum.server.Util;
import pesterchum.server.data.User;
import uk.co.tstableford.utilities.Utilities;

public class SQLiteDatabase implements Database{
	private static final int VERSION = 1;
	private Statement statement;
	private Connection connection;
	private static final JdomParser JDOM_PARSER = new JdomParser();
	public SQLiteDatabase(String database) throws ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:"+database);
		statement = connection.createStatement();
		statement.setQueryTimeout(30);  // set timeout to 30 sec.
		setup();
	}
	public boolean userExists(String user){
		try {
			ResultSet rs = statement.executeQuery("select * from users where name='"+Utilities.encodeHex(user.getBytes())+"'");
			if(rs.next()){
				return true;
			}else{
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
	}
	public boolean authenticate(User user, String password){
		try {
			ResultSet rs = statement.executeQuery("select * from users where name='"+Utilities.encodeHex(user.getUsername().getBytes())+"'");
			if(!rs.next()){
				user.setAuthenticated(false);
				return false;
			}else{
				String pass = rs.getString("password");
				if(pass.equals(Utilities.encodeHex(getHash(password).getBytes()))){
					user.setAuthenticated(true);
					JsonRootNode node = JDOM_PARSER.parse(new String(Utilities.decodeHex(rs.getString("friends"))));
					user.loadFriends(node);
					return true;
				}else{
					user.setAuthenticated(false);
					return false;
				}
			}
		} catch (SQLException | InvalidSyntaxException e) {
			user.setAuthenticated(false);
			return false;
		}
	}
	public boolean newUser(User user, String password){
		if(userExists(user.getUsername())){
			return false;
		}
		try {
			statement.executeUpdate("insert into users values("
					+"'"+Utilities.encodeHex(user.getUsername().getBytes())+"', "
					+"'"+Utilities.encodeHex(getHash(password).getBytes())+"', "
					+"'"+Utilities.encodeHex(Util.jsonToString(user.getFriendsJson().build()).getBytes())+"')");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		user.setAuthenticated(true);
		return true;
	}
	public void saveUser(User user){
		try {
			statement.executeUpdate("update users set friends='"+
					Utilities.encodeHex(Util.jsonToString(user.getFriendsJson().build()).getBytes())
					+"' where name='"+Utilities.encodeHex(user.getUsername().getBytes())+"'");
		} catch (SQLException e1) {
			System.err.println("Could not save user "+user.getUsername());
			e1.printStackTrace();
		}
	}
	private void setup() throws SQLException{
		if(exists("admin")){
			ResultSet rs = statement.executeQuery("select * from admin");
			if(rs.next()){
				int ver = rs.getInt("version");
				if(ver<VERSION){
					update(ver);
				}
			}else{
				initialSetup();
			}
		}else{
			initialSetup();
		}
	}
	private void initialSetup() throws SQLException{
		//setup admin table
		statement.executeUpdate("drop table if exists admin");
		statement.executeUpdate("create table admin (version integer)");
		statement.executeUpdate("insert into admin values("+VERSION+")");
		//setup user table
		statement.executeUpdate("drop table if exists users");
		statement.executeUpdate("create table users (name varchar(20) PRIMARY KEY, password string, friends string)");
	}
	private void update(int oldVersion){
		
	}
	public boolean close(){
		try{
			if(connection != null){
				connection.close();
			}
		}catch(SQLException e){
			return false;
		}
		return true;
	}
	private boolean exists(String table) {
	    try {
	         statement.execute("SELECT * FROM " + table);
	         return true;
	    } catch (SQLException e) {
	         return false;
	    }
	}
	private String getHash(String in){
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(in.getBytes());
			return new String(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

}
