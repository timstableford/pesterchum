package pesterchum.client.gui.theme;

import java.awt.Color;

import javax.swing.JFrame;

import pesterchum.client.resource.ResourceLoader;

public class PFrame extends JFrame{
	private static final long serialVersionUID = -1247010000409362602L;

	public PFrame(){
		super();
		this.getContentPane().setBackground(Color.ORANGE);
		this.setIconImage(ResourceLoader.getIcon());
	}
}
