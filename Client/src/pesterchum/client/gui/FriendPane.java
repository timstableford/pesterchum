package pesterchum.client.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import pesterchum.client.connection.Interface;

public class FriendPane extends JScrollPane implements ActionListener{
	private static final long serialVersionUID = 1L;
	private Interface ifa;
	private JPanel box;
	private List<FriendComponent> components;
	public FriendPane(Interface ifa){
		super();
		this.setBorder(new LineBorder(Color.yellow, 2));
		box = new JPanel(new GridBagLayout());
		box.setBorder(null);
		super.setViewportView(box);
		this.ifa = ifa;
		this.redraw();
	}
	public void redraw(){
		components = new ArrayList<FriendComponent>();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		box.removeAll();
		for(String f: ifa.getFriends()){
			FriendComponent fr = new FriendComponent(f);
			box.add(fr, c);
			fr.addActionListener(this);
			components.add(fr);
			c.gridy++;
		}
		this.validate();
		this.repaint();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String aC = e.getActionCommand();
		FriendComponent source = (FriendComponent)e.getSource();
		switch(aC){
		case "clicked":
			String user = source.getUsername();
			new Messaging(ifa, user);
			break;
		case "selected":
			for(FriendComponent f: components){
				if(f==source){
					f.setSelected(true);
				}else{
					f.setSelected(false);
				}
			}
			break;
		default: break;
		}
	}
}