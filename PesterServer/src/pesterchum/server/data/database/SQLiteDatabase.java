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

import pesterchum.server.Encryption;
import pesterchum.server.Util;
import pesterchum.server.data.User;

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
	public boolean userExists(User user){
		try {
			ResultSet rs = statement.executeQuery("select * from users where name='"+Encryption.encode(user.getUsername().getBytes())+"'");
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
			ResultSet rs = statement.executeQuery("select * from users where name='"+Encryption.encode(user.getUsername().getBytes())+"'");
			if(!rs.next()){
				user.setAuthenticated(false);
				return false;
			}else{
				String pass = rs.getString("password");
				if(pass.equals(Encryption.encode(getHash(password).getBytes()))){
					user.setAuthenticated(true);
					JsonRootNode node = JDOM_PARSER.parse(new String(Encryption.decode(rs.getString("friends"))));
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
		try {
			ResultSet rs = statement.executeQuery("select * from users where name='"+Encryption.encode(user.getUsername().getBytes())+"'");
			if(rs.next()){
				return false;
			}
		} catch (SQLException e) {
			//ignore because user simply not found, which is good
		}
		try {
			statement.executeUpdate("insert into users values("
					+"'"+Encryption.encode(user.getUsername().getBytes())+"', "
					+"'"+Encryption.encode(getHash(password).getBytes())+"', "
					+"'"+Encryption.encode(Util.jsonToString(user.getFriendsJson().build()).getBytes())+"')");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public void saveUser(User user){
		try {
			ResultSet rs = statement.executeQuery("select * from users where name='"+Encryption.encode(user.getUsername().getBytes())+"'");
			if(rs.next()){
				String passHash = rs.getString("password");
				statement.executeUpdate("insert or replace into users values("
						+Encryption.encode(user.getUsername().getBytes())+", "
						+passHash+", "
						+Encryption.encode(Util.jsonToString(user.getFriendsJson().build()).getBytes())+")");
			}
		} catch (SQLException e1) {
			System.err.println("Could not save user "+user.getUsername());
		}
		try {
			statement.executeUpdate("insert of replace into users values("
					+Encryption.encode(user.getUsername().getBytes())+", "
					+Encryption.encode(Util.jsonToString(user.getFriendsJson().build()).getBytes())+")");
		} catch (SQLException e) {
			System.err.println("Could not save user "+user.getUsername());
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
		//TODO remove this initial propgation
		User u = new User("frushiMoto");
		u.addFriend("hyperBadger");
		newUser(u, "password");
		User u2 = new User("hyperBadger");
		u2.addFriend("frushiMoto");
		newUser(u2, "password");
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
