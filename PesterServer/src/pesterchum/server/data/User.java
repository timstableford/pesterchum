package pesterchum.server.data;

public class User {
	private String username;
	private boolean authenticated;
	public User(String username){
		this.username = username;
		this.authenticated = false;
	}
	public boolean authenticate(String password){
		//TODO implement authentication
		/*
		 * 1)Hash Password
		 * 2)Check against database
		 * 4)Set authenticated
		 * 3)Dispose of password
		 */
		this.authenticated = true;
		return true;
	}
	public boolean authenticated(){
		return this.authenticated;
	}
	public String getUsername(){
		return this.username;
	}
}
