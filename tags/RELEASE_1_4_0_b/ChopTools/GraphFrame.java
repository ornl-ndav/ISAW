package ChopTools;
import java.io.*;
import java.awt.Insets;
import java.text.DateFormat;
import java.util.*;
import java.math.*;
import java.text.*;
import graph.*;
import java.beans.*;
import java.awt.*;

public class GraphFrame extends java.awt.Frame
{
	public GraphFrame()
	{
		//{{INIT_CONTROLS
		setLayout(null);
		setSize(500,500);
		setVisible(false);
		setTitle("TOF of javaChop");
		//}}

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		//}}

		//{{INIT_MENUS
		//}}

	}

	public GraphFrame(String title)
	{
		this();
		setTitle(title);
	}
	public void setVisible(boolean b)
	{
		if(b)
		{
			setLocation(350, 50);
		}
	super.setVisible(b);
	}


	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension d = getSize();

		super.addNotify();

		if (fComponentsAdjusted)
			return;

		// Adjust components according to the insets
		Insets ins = getInsets();
		setSize(ins.left + ins.right + d.width, ins.top + ins.bottom + d.height);
		Component components[] = getComponents();
		for (int i = 0; i < components.length; i++)
			{
			Point p = components[i].getLocation();
			p.translate(ins.left, ins.top);
			components[i].setLocation(p);
		}
		fComponentsAdjusted = true;
	}

	// Used for addNotify check.
	boolean fComponentsAdjusted = false;

	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
		Object object = event.getSource();
		if (object == GraphFrame.this)
			GraphFrame_WindowClosing(event);
		}
	}

	void GraphFrame_WindowClosing(java.awt.event.WindowEvent event)
	{
		dispose();		 // dispose of the Frame.
	}
	//{{DECLARE_CONTROLS
	//}}

	//{{DECLARE_MENUS
	//}}

}
