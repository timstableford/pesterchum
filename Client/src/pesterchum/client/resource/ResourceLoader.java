package pesterchum.client.resource;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ResourceLoader {
	private static Image icon;
	private static final String icon_location = "/pesterchum.png";
	protected HashMap<String, Resource> resources;
	public ResourceLoader(){
		resources = new HashMap<String, Resource>();
	}
	public Resource getResource(String name){
		return resources.get(name);
	}
	public Image getImage(String name){
		return ((Img)(getResource(name))).getImage();
	}
	public static Image getIcon(){
		if(icon==null){
			icon = (new Img(icon_location)).getImage();
		}
		return icon;
	}
	public boolean load(String file){
		Document doc;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(getClass().getResourceAsStream(file));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			return false;
		}
		NodeList resources = doc.getElementsByTagName("resource");
		for (int i=0; i<resources.getLength(); i++){
			Node resource = resources.item(i);
			if(resource.getNodeType()==Node.ELEMENT_NODE){
				Element res = (Element)resource;
				Resource r;
				switch(getTagValue("type", res)){
				case "image":
					Img img = new Img(getTagValue("name", res), getTagValue("path", res));
					if(img.getImage()==null){ return false; }
					r = (Resource)img;
					break;
				default:
					return false;
				}
				this.resources.put(r.getName(), r);
			}
		}
		return true;
	}
	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();
	}
}
