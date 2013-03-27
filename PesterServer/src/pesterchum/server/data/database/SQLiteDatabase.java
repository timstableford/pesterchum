package pesterchum.server.data.database;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import pesterchum.server.Util;
import pesterchum.server.data.Message;
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
	public void storeMessage(Message m){
		try {
			String save = "insert into messages values("
					+"'"+m.getHash()+"', "
					+"'"+Utilities.encodeHex(m.getFrom().getBytes("UTF-8"))+"', "
					+"'"+Utilities.encodeHex(m.getTo().getBytes("UTF-8"))+"', "
					+"'"+Utilities.encodeHex(m.getContent().getBytes("UTF-8"))+"', "
					+"'"+Long.toString(m.getTime())+"')";
			statement.executeUpdate(save);
		} catch (SQLException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	public List<Message> getMessages(String username){
		List<Message> messages = new ArrayList<Message>();
		try{
			ResultSet rs = statement.executeQuery("select * from messages where touser='"+Utilities.encodeHex(username.getBytes("UTF-8"))+"'");
			while(rs.next()){
				String from = new String(Utilities.decodeHex(rs.getString("fromuser")));
				String content = new String(Utilities.decodeHex(rs.getString("content")));
				Long time = Long.parseLong(rs.getString("sent"));
				Message m = new Message(from, username, content);
				m.setTime(time);
				if(m.getHash().equals(rs.getString("hash"))){
					messages.add(m);
				}else{
					System.err.println("error retrieving message - hash does not match");
				}
			}
			statement.executeUpdate("delete from messages where touser='"+Utilities.encodeHex(username.getBytes("UTF-8"))+"'");
		}catch(SQLException | UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return messages;
	}
	public boolean userExists(String user){
		try {
			ResultSet rs = statement.executeQuery("select * from users where name='"+Utilities.encodeHex(user.getBytes("UTF-8"))+"'");
			if(rs.next()){
				return true;
			}else{
				return false;
			}
		} catch (SQLException | UnsupportedEncodingException e) {
			return false;
		}
	}
	public boolean authenticate(User user, String password){
		try {
			ResultSet rs = statement.executeQuery("select * from users where name='"+Utilities.encodeHex(user.getUsername().getBytes("UTF-8"))+"'");
			if(!rs.next()){
				user.setAuthenticated(false);
				return false;
			}else{
				String pass = rs.getString("password");
				if(pass.equals(Utilities.encodeHex(getHash(password).getBytes("UTF-8")))){
					user.setAuthenticated(true);
					JsonRootNode node = JDOM_PARSER.parse(new String(Utilities.decodeHex(rs.getString("friends"))));
					user.loadFriends(node);
					return true;
				}else{
					user.setAuthenticated(false);
					return false;
				}
			}
		} catch (SQLException | InvalidSyntaxException | UnsupportedEncodingException e) {
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
					+"'"+Utilities.encodeHex(user.getUsername().getBytes("UTF-8"))+"', "
					+"'"+Utilities.encodeHex(getHash(password).getBytes("UTF-8"))+"', "
					+"'"+Utilities.encodeHex(Util.jsonToString(user.getFriendsJson().build()).getBytes("UTF-8"))+"')");
		} catch (SQLException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		user.setAuthenticated(true);
		return true;
	}
	public void saveUser(User user){
		try {
			statement.executeUpdate("update users set friends='"+
					Utilities.encodeHex(Util.jsonToString(user.getFriendsJson().build()).getBytes("UTF-8"))
					+"' where name='"+Utilities.encodeHex(user.getUsername().getBytes("UTF-8"))+"'");
		} catch (SQLException | UnsupportedEncodingException e1) {
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
		statement.executeUpdate("create table users (name varchar(40) PRIMARY KEY, password string, friends string)");
		//setup message storage
		statement.executeUpdate("drop table if exists messages");
		statement.executeUpdate("create table messages (hash varchar(50) PRIMARY KEY, fromuser varchar(40), touser varchar(40), content string, sent string)");
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
			messageDigest.update(in.getBytes("UTF-8"));
			return new String(messageDigest.digest());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return null;
		}
	}

}
