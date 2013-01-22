package pesterchum.server.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import pesterchum.server.Util;

public class Interface implements Incoming{
	private Database database;
	private DocumentBuilder builder;
	public Interface(Database database){
		this.database = database;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
	}
	private boolean authenticated(ICData data){
		if(data.getSource().getUser()!=null&&data.getSource().getUser().authenticated()){
			return true;
		}
		return false;
	}
	@Override
	public void processIncoming(ICData data) {
		if(authenticated(data)){
			switch(data.getName()){
			case "message":

				break;
			default:
				System.err.println("Unknown data from "+data.getSource().getSource()+" - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "login":
				processLogin(data);
				break;
			default:
				System.err.println("Unknown data from "+data.getSource().getSource()+" - "+data.getData());
			}
		}
	}
	private void processLogin(ICData data){
		Document docin;
		try {
			docin = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
			Element e = Util.getFirst(docin, "login");
			String un = Util.getTagValue("username", e);
			String pw = Util.getTagValue("password", e);
			User u = new User(un);
			database.authenticate(u, pw);
			Document doc = builder.newDocument();
			Element root = doc.createElement("login");
			doc.appendChild(root);
			
			Element username = doc.createElement("username");
			username.appendChild(doc.createTextNode(un));
			root.appendChild(username);
			
			Element suc = doc.createElement("success");
			suc.appendChild(doc.createTextNode(u.authenticated()+""));
			root.appendChild(suc);
			
			data.getSource().sendData(Util.docToString(doc));
			
			if(u.authenticated()){
				data.getSource().setUser(u);
			}
		} catch (SAXException | IOException e1) {
			System.err.println("Could not authenticate login for "+data.getSource().getSource());
		}
	}
}
