package IsawGUI;


import javax.swing.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import javax.swing.text.html.*;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.print.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import javax.swing.WindowConstants;


public class Browser implements HyperlinkListener, 
                                ActionListener {
    String source = "http://www.uwstout.edu";
    final String GO = "Go";
    final String Back = "Back";
    final String Home = "Home";
    final String Print = "Print";
    JEditorPane ep = new JEditorPane(); //a JEditorPane allows display of HTML & RTF
    JToolBar tb = new JToolBar(); //a JToolBar sits above the JEditorPane & contains 
    JTextField tf = new JTextField(40); // the JTextField & go button
    JLabel address = new JLabel(" Address: ");
    JButton back = new JButton(Back);
    JButton go = new JButton(GO);
    JButton home = new JButton(Home);
    JButton print = new JButton(Print);
    BorderLayout bl = new BorderLayout();
    JPanel panel = new JPanel(bl);
    JFrame frame = new JFrame("ISaw browser");
    protected Vector history = null;
    private String fileName;

    public Browser() {
        this("http://www.uwstout.edu");
    }

    //public Browser(String file)
    public Browser(String source) {
        this.source = source;
        //fileName= file;
        openURL(source); //this method (defined below) opens a page with address source

        history = new Vector();
        history.add(source);
        back.setEnabled(false);

        ep.setEditable(false); //this makes the HTML viewable only in teh JEditorPane, ep
        ep.addHyperlinkListener(this); //this adds a listener for clicking on links

        JScrollPane scroll = new JScrollPane(ep); //this puts the ep inside a scroll pane

        panel.add(scroll, BorderLayout.CENTER); //adds the scroll pane to center of panel

        tf.setActionCommand(GO); //gives the ActionListener on tf a name for its ActionEvent
        tf.setActionCommand(Back);
        tf.setActionCommand(Home);
        //tf.setActionCommand(Print);
        print.addActionListener(new gov.anl.ipns.Util.Sys.PrintComponentActionListener(ep));

        tf.setEditable(true);
        tf.addActionListener(this); //adds an ActionListener to the JTextField (so user can

        go.addActionListener(this); //use "Enter Key")
        back.addActionListener(this); //use "Enter Key")
        home.addActionListener(this);

        tb.add(back); //this adds the back button to the JToolBar
        tb.add(home); //this adds the home button to the JToolBar
        tb.add(print); //this adds the print button to the JToolBar
        tb.add(address); //this adds the Label "Address:" to the JToolBar
        tb.add(tf); //this adds the JTextField to the JToolBar
        tb.add(go); //this adds the go button to the JToolBar

        panel.add(tb, BorderLayout.NORTH); //adds the JToolBar to the top (North) of panel
        frame.setContentPane(panel);
        frame.setSize(1000, 900);
        frame.setVisible(true);
    }// end Browser()

    private void getThePage(String location) {
        try {
            ep.setPage(location);
            tf.setText(location);
        } catch (IOException ioException) {
            System.out.println("Error retrieving specified URL");
	 
        }//end catch
    }//end method getthePage

    public void openURL(String urlString) {
        String start = urlString.substring(0, 4);

        if ((!start.equals("http")) && (!start.equals("file"))) //adds "http://" to the URL if needed
        {
            urlString = "http://" + urlString;
        }//end if
        try {
            URL url = new URL(urlString);

            ep.setPage(url); //this sets the ep page to the URL page
            tf.setText(urlString); //this sets the JTextField, tf, to the URL
        } catch (Exception e) {
            System.out.println("Can't open " + source + " " + e);
        }//end try-catch
    }//end openURL

    public void hyperlinkUpdate(HyperlinkEvent he) //this allows linking
    {

        HyperlinkEvent.EventType type = he.getEventType();

        if (type == HyperlinkEvent.EventType.ACTIVATED) {
            openURL(he.getURL().toExternalForm());
            if (history == null) history = new Vector();
            history.add(he.getURL().toExternalForm());

            if (history.size() > 1)
                back.setEnabled(true);

        }
    }//end hyperlinkUpdate()

    public void actionPerformed(ActionEvent ae) //for the GO and BACK buttons
    {
        String command = ae.getActionCommand();

        if (command.equals(GO)) {
            openURL(tf.getText());
            history.add(tf.getText());
        }

        if (command.equals(Home)) {
            openURL(source); 
        }

        if (command.equals(Print)) {
            PrinterJob printJob = PrinterJob.getPrinterJob();

            printJob.setPrintable(new Printable () {
                    public int print(Graphics g, PageFormat pf, int pageIndex) {
                        if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                        Graphics2D g2d = (Graphics2D) g;

                        g2d.translate(pf.getImageableX(), pf.getImageableY());
                     
                        return Printable.PAGE_EXISTS;
                    }
                }
            );
            
            if (printJob.printDialog())
                try { 
                    printJob.print();
                } catch (PrinterException pe) {
                    System.out.println("Error printing: " + pe);
                }
        }
        
        if (command.equals(Back)) {
            try {
                String lastURL = (String) history.lastElement();

                history.removeElement(lastURL);
                lastURL = (String) history.lastElement();
                // JOptionPane.showMessageDialog(null, lastURL);
                ep.setPage(lastURL);
                if (history.size() == 1)
                    back.setEnabled(false);
            } catch (Exception e) {
                System.out.println("ERROR: Trouble fetching URL" + e);
            }
        }
    }//end actionPerformed()

    public static void main(String[] args) {
        //Browser b = new Browser("c:");
        Browser b = new Browser();
    }

}// end Browser class
