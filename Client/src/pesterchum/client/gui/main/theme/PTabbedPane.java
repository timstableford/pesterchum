package pesterchum.client.gui.main.theme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.border.LineBorder;

public class PTabbedPane extends PPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private PPanel buttons, content;
	private HashMap<PButton, JComponent> components;
	public PTabbedPane(){
		this.setBorder(new LineBorder(Color.YELLOW));
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		buttons = new PPanel();
		buttons.setBorder(new LineBorder(Color.YELLOW));
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		content = new PPanel();
		content.setBorder(null);
		components = new HashMap<PButton, JComponent>();
		this.add(buttons, c);
		c.weighty = 1;
		c.gridy = 1;
		this.add(content, c);
	}
	public void addTab(String text, JComponent component){
		PButton b = new PButton(text);
		buttons.add(b);
		if(components.size()<=0){
			setContent(component);
		}
		components.put(b, component);
		b.addActionListener(this);
		this.validate();
	}
	public void setContent(JComponent component){
		content.removeAll();
		content.add(component, BorderLayout.CENTER);
		content.validate();
		content.repaint();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		setContent(components.get((PButton)e.getSource()));
		
	}
}
