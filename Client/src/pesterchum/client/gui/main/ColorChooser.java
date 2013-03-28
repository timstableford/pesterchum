package pesterchum.client.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JColorChooser;
import javax.swing.JFrame;

import pesterchum.client.gui.main.theme.PButton;
import pesterchum.client.gui.main.theme.PFrame;
import pesterchum.client.gui.main.theme.PMenuBar;
import pesterchum.client.gui.main.theme.PMenuItem;

public class ColorChooser extends PFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	private GUI gui;
	private PButton choose;
	private JColorChooser jcc;
	private PMenuItem min, quit;
	public ColorChooser(GUI gui){
		this.setUndecorated(true);
		this.gui = gui;
		this.jcc = new JColorChooser();
		this.choose = new PButton("Choose");
		this.choose.addActionListener(this);
		this.add(createMenuBar(),BorderLayout.NORTH);
		this.add(jcc, BorderLayout.CENTER);
		this.add(choose, BorderLayout.SOUTH);
		this.pack();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	public PMenuBar createMenuBar(){
		PMenuBar mb = new PMenuBar();
		mb.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		//draw menu

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
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==choose){
			Color c = jcc.getColor();
			gui.setColor(c);
			this.dispose();
		}else if(e.getSource()==min){
			this.setState(Frame.ICONIFIED);
		}else if(e.getSource()==quit){
			this.dispose();
		}
	}
	

}
