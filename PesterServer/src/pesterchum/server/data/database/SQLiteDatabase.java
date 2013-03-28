package pesterchum.server.data.database;

import java.awt.Color;
import java.io.IOException;
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
import pesterchum.server.data.ICData;
import pesterchum.server.data.Manager;
import pesterchum.server.data.Packet;
import pesterchum.server.data.User;
import uk.co.tstableford.utilities.Log;
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
	public void storePacket(Packet m){
		try {
			String save = "insert into packets values("
					+"'"+m.getHash()+"', "
					+"'"+Utilities.encodeHex(m.getFrom().getBytes("UTF-8"))+"', "
					+"'"+Utilities.encodeHex(m.getTo().getBytes("UTF-8"))+"', "
					+"'"+Utilities.encodeHex(Util.jsonToString(m.getJson().build()).getBytes("UTF-8"))+"', "
					+"'"+Long.toString(m.getTime())+"')";
			statement.executeUpdate(save);
		} catch (SQLException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	public List<Packet> getPackets(String username){
		List<Packet> packets = new ArrayList<Packet>();
		try{
			ResultSet rs = statement.executeQuery("select * from packets where touser='"+Utilities.encodeHex(username.getBytes("UTF-8"))+"'");
			while(rs.next()){
				String content = new String(Utilities.decodeHex(rs.getString("content")));
				JsonRootNode n = JDOM_PARSER.parse(content);
				Packet p = Manager.parsePacket(new ICData("packet", n, null));
				if(p.getHash().equals(rs.getString("hash"))){
					packets.add(p);
				}else{
					Log.getInstance().error("error retrieving packet - hash does not match");
				}
			}
			statement.executeUpdate("delete from packets where touser='"+Utilities.encodeHex(username.getBytes("UTF-8"))+"'");
		}catch(SQLException | InvalidSyntaxException | IOException e){
			Log.getInstance().error("Error parsing packet from database");
		}
		return packets;
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
					String[] c = rs.getString("color").split(",");
					user.setColor(new Color(
							Integer.parseInt(c[0]),
							Integer.parseInt(c[1]),
							Integer.parseInt(c[2])
							));
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
					+"'"+Utilities.encodeHex(Util.jsonToString(user.getFriendsJson().build()).getBytes("UTF-8"))+"', "
					+"'"+user.getColor().getRed()+","+user.getColor().getGreen()+","+user.getColor().getBlue()+"'"
					+")");
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
			statement.executeUpdate("update users set color='"+
					user.getColor().getRed()+","+user.getColor().getGreen()+","+user.getColor().getBlue()
					+"' where name='"+Utilities.encodeHex(user.getUsername().getBytes("UTF-8"))+"'");
		} catch (SQLException | UnsupportedEncodingException e1) {
			Log.getInstance().error("Could not save user "+user.getUsername());
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
		statement.executeUpdate("create table users (name varchar(40) PRIMARY KEY, password string, friends string, color string)");
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
