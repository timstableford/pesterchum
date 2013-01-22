package pesterchum.client.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;

import pesterchum.client.connection.Interface;
import pesterchum.client.data.Message;
import pesterchum.client.gui.login.Login;
import pesterchum.client.resource.Img;
import pesterchum.client.resource.Resource;
import pesterchum.client.resource.ResourceLoader;

public class GUI extends JFrame{
	private static final long serialVersionUID = 1L;
	private ResourceLoader smilies;
	private Interface ifa; //this has the methods you will communicate with
	public GUI(){
		
		JPanel logoPlace, moods, buddyList;
		JButton chummy, palsy, chipper, bully, preppy, rancorous, abscond,
		addChum, block, pester;
		
		//menu objects
		JMenuBar menu;
		JMenu client, profile, help;
		JMenuItem options, memos, pesterLog, randomEcounter, userList, idle, addGroup,
		importThings, reconnect, exit, helpMe, about, reportBug;
		
		// menu at top
		// logo
		//list of chums
		//buttons add chum/block[red]/pester!
		//mychumhandle: mood/ nick/color
		//mood table 2x6 + abscond
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		smilies = new ResourceLoader();
		smilies.load("/smilies/smilies.xml");
		this.setSize(new Dimension(200, 400));
		this.getContentPane().setBackground(Color.ORANGE);
		
		//create menu bar + menus
		menu= new JMenuBar();
		client = new JMenu("client"); profile = new JMenu("profile"); help = new JMenu("help");
		menu.add(client); menu.add(profile); menu.add(help);
		
		//set up the Client menu
		options = new JMenuItem("options"); memos = new JMenuItem("memos");
		pesterLog = new JMenuItem("pesterlog"); randomEcounter = new JMenuItem("random ecounter"); 
		userList = new JMenuItem("user list"); idle = new JMenuItem("idle"); 
		addGroup = new JMenuItem("add group"); importThings = new JMenuItem("import"); 
		reconnect = new JMenuItem("reconnect"); exit = new JMenuItem("exit");
		
		client.add(options); client.add(memos); client.add(pesterLog); client.add(randomEcounter);
		client.add(userList); client.add(idle); client.add(addGroup); client.add(importThings);
		client.add(reconnect); client.add(exit);
		
		//set up the Help menu
		helpMe = new JMenuItem("help"); about = new JMenuItem("about"); reportBug = new JMenuItem("report a bug");
		
		help.add(helpMe); help.add(about); help.add(reportBug);
		
		//add the whole thing
		this.add(menu, BorderLayout.NORTH);
		
		//try for chumroll
		buddyList = new JPanel();	
		JTextArea chumroll = new JTextArea("set server connection to get and\ndisplay names 1 per line", 5, 15);
		chumroll.setEditable(false);
		//set proper layout for the buttons under list
		addChum = new JButton("add chum"); block = new JButton("block"); pester = new JButton("pester!"); 
		buddyList.add(chumroll); buddyList.add(addChum); buddyList.add(block); buddyList.add(pester);
		this.add(buddyList);
		
		//try for grid layout for moods - SWITCH to GridBag Layout
		moods = new JPanel();
		
		//buttons create + add
		chummy = new JButton("chummy"); palsy = new JButton("palsy"); chipper = new JButton("chipper"); bully = new JButton("bully");
		preppy = new JButton("peppy"); rancorous= new JButton("rancorous"); abscond = new JButton("abscond"); 
		
		moods.add(chummy); moods.add(bully); moods.add(palsy); moods.add(preppy); moods.add(chipper); moods.add(rancorous);moods.add(abscond);
		//set grid
		GridLayout gridLayout = new GridLayout(0,2);
		moods.setLayout(gridLayout);
		
		this.add(moods, BorderLayout.SOUTH);
		
		//logo time!
		//this.add(new JLabel(getIcon(smilies.getResource("apple"))), BorderLayout.CENTER);
		//this.add(new PLabel("Label", getIcon(smilies.getResource("apple"))),BorderLayout.CENTER);
		
		this.setVisible(true);
	}
	
	private ImageIcon getIcon(Resource res){
		if(res instanceof Img){
			Img img = (Img)res;
			ImageIcon i = new ImageIcon(img.getImage());
			return i;
		}
		return null;
	}
	
	public void setInterface(Interface i){
		this.ifa = i;
		//cant login until interface is set, this is pretty quick
		new Login("localhost",7423,ifa);
	}
	
	public void incomingMessage(Message message){
		System.out.println("Message from "+message.getFrom()+" - "+message.getMessage());
	}
	
	public void loginResponse(boolean success) {
		System.out.println("Login response - successful? "+success);
	}
	
	public void versionMismatch(int client, int server){
		
	}

}
