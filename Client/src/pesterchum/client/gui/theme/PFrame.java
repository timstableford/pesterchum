package pesterchum.client.gui.theme;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import pesterchum.client.resource.ResourceLoader;

public class PFrame extends JFrame implements MouseListener, MouseMotionListener{
	private static final long serialVersionUID = -1247010000409362602L;
	protected int posX,posY;
	public PFrame(){
		super();
		posX = 0;
		posY = 0;
		this.getContentPane().setBackground(Color.ORANGE);
		this.setIconImage(ResourceLoader.getIcon());
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		Border b = new LineBorder(Color.YELLOW, 2);
		JComponent c = (JComponent)getContentPane();
		c.setBorder(b);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		posX=e.getX();
		posY=e.getY();
	}
	@Override
	public void mouseDragged(MouseEvent e){
		setLocation(e.getXOnScreen()-posX,e.getYOnScreen()-posY);
	}
	@Override
	public void mouseMoved(MouseEvent e) {}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
}
