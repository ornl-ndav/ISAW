/*
 * File: ShowDocument.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */
package IsawGUI;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.MalformedURLException;

public class ShowDocument extends Applet 
                          implements ActionListener {
    URLWindow urlWindow;

    public void init() {
        Button button = new Button("Bring up URL window");
        button.addActionListener(this);
        add(button);
System.out.println("Inside the applet");
        urlWindow = new URLWindow(getAppletContext());
        urlWindow.pack();
    }

    public void destroy() {
        urlWindow.setVisible(false);
        urlWindow = null;
    }

    public void actionPerformed(ActionEvent event) {
        urlWindow.setVisible(true);
    }
}

class URLWindow extends Frame 
                implements ActionListener {
    TextField urlField;
    Choice choice;
    AppletContext appletContext;

    public URLWindow(AppletContext appletContext) {
        super("Show a Document!");

        this.appletContext = appletContext;

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridBag);

        Label label1 = new Label("URL of document to show:",
				 Label.RIGHT);
        gridBag.setConstraints(label1, c);
        add(label1);

        urlField = new TextField("http://java.sun.com/", 40);
        urlField.addActionListener(this);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        gridBag.setConstraints(urlField, c);
        add(urlField);

        Label label2 = new Label("Window/frame to show it in:",
				 Label.RIGHT);
        c.gridwidth = 1;
        c.weightx = 0.0;
        gridBag.setConstraints(label2, c);
        add(label2);

        choice = new Choice();
        choice.addItem("(browser's choice)"); //don't specify
        choice.addItem("My Personal Window"); //a window named
					//"My Personal Window"
        choice.addItem("_blank"); //a new, unnamed window
        choice.addItem("_self"); 
        choice.addItem("_parent"); 
        choice.addItem("_top"); //the Frame that contained this
				//applet
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        gridBag.setConstraints(choice, c);
        add(choice);

        Button button = new Button("Show document");
        button.addActionListener(this);
        c.weighty = 1.0;
        c.ipadx = 10;
        c.ipady = 10;
        c.insets = new Insets(5,0,0,0);
        c.anchor = GridBagConstraints.SOUTH;
        gridBag.setConstraints(button, c);
        add(button);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                setVisible(false);
            }
        });
    } 
    
    public void actionPerformed(ActionEvent event) {
        String urlString = urlField.getText();
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + urlString);
        }

        if (url != null) {
            if (choice.getSelectedIndex() == 0) {
                appletContext.showDocument(url);
            } else {
                appletContext.showDocument(url, 
				  choice.getSelectedItem());
            }
        }
    }
}

