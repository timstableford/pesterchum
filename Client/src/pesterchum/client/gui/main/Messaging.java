package pesterchum.client.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;

import pesterchum.client.Util;
import pesterchum.client.connection.Interface;
import pesterchum.client.data.Message;
import pesterchum.client.gui.main.theme.PButton;
import pesterchum.client.gui.main.theme.PFrame;
import pesterchum.client.gui.main.theme.PMenuBar;
import pesterchum.client.gui.main.theme.PMenuItem;
import pesterchum.client.gui.main.theme.POpaqueLabel;
import pesterchum.client.gui.main.theme.PPanel;
import pesterchum.client.gui.main.theme.PTextField;
import uk.co.tstableford.utilities.Log;

public class Messaging extends PFrame implements ActionListener, KeyListener{
	private static final long serialVersionUID = 1L;
	private String user;
	private PMenuItem min,quit;
	private GUI gui;
	private Interface ifa;
	private JEditorPane text;
	private PTextField input;
	private PButton send;
	public Messaging(Interface ifa, String user, GUI parent){
		this.setLocationRelativeTo(parent);
		this.setUndecorated(true);
		this.setSize(400,300);
		this.setTitle(user);
		this.user = user;
		this.gui = parent;
		this.ifa = ifa;
		this.add(createMenuBar(), BorderLayout.NORTH);
		this.add(createMessagingPanel(), BorderLayout.CENTER);
		this.setVisible(true);
		input.requestFocus();
	}
	public PMenuBar createMenuBar(){
		PMenuBar mb = new PMenuBar();
		mb.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		//draw menu
		c.weightx = 0;
		mb.add(new JLabel(this.user), c);

		//spacer
		c.gridx = 5;
		c.weightx = 1;
		mb.add(Box.createHorizontalGlue(), c);
		c.weightx = 0;
		//icons
		c.gridx = 6;
		min = new PMenuItem("_");
		mb.add(min, c);
		min.addActionListener(this);
		c.gridx = 7;
		quit = new PMenuItem("X");
		mb.add(quit, c);
		quit.addActionListener(this);
		return mb;
	}
	public PPanel createMessagingPanel(){
		PPanel p = new PPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,6,3,6);
		//messaging window
		//(pesterlog text)
		POpaqueLabel l = new POpaqueLabel(ifa.translate("pesterlog")+":", SwingConstants.LEFT);
		p.add(l, c);
		//and the actual text
		c.gridy = 1;
		c.gridwidth = 4;
		c.weightx = 1;
		c.weighty = 1;
		text = new JEditorPane();
		text.setEditable(false);
		text.setContentType("text/html");
		JScrollPane scrollPane = new JScrollPane(text); 
		scrollPane.setBorder(new LineBorder(new Color(255,140,0), 2));

		p.add(scrollPane, c);
		//input box & send
		c.gridwidth = 1;
		c.weighty = 0;
		c.gridy = 2;
		//input box
		c.insets = new Insets(0,6,3,1);
		c.gridx = 0;
		input = new PTextField();
		input.addKeyListener(this);
		p.add(input, c);
		//send
		c.insets = new Insets(0,1,3,6);
		c.weightx = 0.1;
		c.gridx = 1;
		send = new PButton(ifa.translate("send"));
		send.addActionListener(this);
		p.add(send, c);
		return p;
	}
	public String getUser(){
		return user;
	}
	public void incoming(Message message){
		String m =  "["+Util.initial(message.getFrom())+"] "+message.getContent();
		HTMLEditorKit editor = (HTMLEditorKit) text.getEditorKit();
		StringReader reader = new StringReader(m);
		try {
			editor.read(reader, text.getDocument(), text.getDocument().getLength());
		}catch(BadLocationException | IOException ex) {
			Log.getInstance().error("Could not append to message - "+m);
		}

	}
	private String parseMessage(String message){
		//this is for parsing outgoing messages
		StringBuffer mb = new StringBuffer(message);
		//set the color
		Color c = ifa.getUser().getColor();
		mb.insert(0, "<font color=\"rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")\">");
		mb.append("</font>");
		return mb.toString();
	}
	private void send(){
		String message = parseMessage(input.getText());
		Message m = new Message(ifa.getUser().getUsername(), this.user, message);
		incoming(m);
		ifa.sendMessage(m);
		input.setText("");
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			send();
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if(s==min){
			this.setState(Frame.ICONIFIED);
		}else if(s==quit){
			gui.closeChat(this);
			this.dispose();
		}else if(s==send){
			this.send();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
}
