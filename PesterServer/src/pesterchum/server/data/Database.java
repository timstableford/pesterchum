package pesterchum.server.data;

public class Database {
	public Database(){
		
	}
	public boolean authenticate(User user, String password){
		boolean authenticated = true;
		/*
		 * 1)Hash Password
		 * 2)Check against database
		 * 4)Set authenticated
		 * 3)Dispose of password
		 */
		user.setAuthenticated(true);
		return authenticated;
	}
}
