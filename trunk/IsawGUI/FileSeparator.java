/*
 * @(#)FileSeparator.java     1.0  99/09/10  
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawTools
 *
 * GUI for Dongfeng to separate Runfiles into Sample and Background runs
 * and perform different operations on them.
 */

package IsawGUI;


import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class FileSeparator extends JFrame implements ListSelectionListener
{
    
	private Vector vrunfiles;
	private Vector vsamplefiles;
	private Vector vbackgroundfiles;
	private FileDialog fd;
	private DefaultListModel listModelA;  
	private DefaultListModel listModelB;  
	private DefaultListModel listModelC;  
	private JList RunfilesList;
	private JList SamplefilesList;
	private JList BackgroundfilesList;
	
	private JButton sampleAddButton;
	private JButton sampleRemoveButton;
	private JButton backgroundAddButton;
	private JButton backgroundRemoveButton;
	private JButton cancelButton;
	private JButton applyButton;
	private JButton calibResultButton;
	private JButton chkGroupingButton;
	private JButton removeNoisyDetButton;
	
	
	public FileSeparator()
	{
	    super("File Separator");
	    listModelA = new DefaultListModel();
        listModelB = new DefaultListModel();
        listModelC = new DefaultListModel();
        
	    JLabel JLabel1 = new JLabel();
	    JLabel JLabel2 = new JLabel();
	    JLabel JLabel3 = new JLabel();
	    
	    sampleAddButton = new JButton();
	    sampleRemoveButton = new JButton();
	    backgroundAddButton = new JButton();
	    backgroundRemoveButton = new JButton();
	    cancelButton = new JButton();
	    applyButton = new JButton();
	    calibResultButton = new JButton();
	    chkGroupingButton = new JButton();
	    removeNoisyDetButton = new JButton();
	    
	    fd = new FileDialog(new Frame(), "Choose Folder", FileDialog.LOAD);
        fd.setDirectory("H:\\UPLOAD\\Data\\hrmecs");
        fd.show();
        
        vrunfiles = new Vector();
        vsamplefiles = new Vector();
        vbackgroundfiles = new Vector();
        
        File f = new File(fd.getDirectory(), fd.getFile());
        String dir = fd.getDirectory();
        String[] fileList = new File(dir).list();//dir listing
     
        for(int i = 0; i<fileList.length; i++)
        {
           
            vrunfiles.addElement( fileList[i]);
           // if( fileList[i].toString().endsWith(".RUN")||fileList[i].toString().endsWith(".run"))
            if( fileList[i].toString().endsWith(".RUN")||fileList[i].toString().endsWith(".run")||fileList[i].toString().endsWith("RUN;1"))
            listModelA .addElement(fileList[i]);
        }
     
        RunfilesList = new JList(listModelA);
	    SamplefilesList = new JList(listModelB);
	    BackgroundfilesList = new JList(listModelC);
	    
	    RunfilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        SamplefilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        BackgroundfilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        RunfilesList.setSelectedIndex(0);
        SamplefilesList.setSelectedIndex(0);
        BackgroundfilesList.setSelectedIndex(0);
        
        RunfilesList.addListSelectionListener(this);
        SamplefilesList.addListSelectionListener(this);
        BackgroundfilesList.addListSelectionListener(this);
	    
	    JScrollPane listScrollPaneA = new JScrollPane(RunfilesList);
        JScrollPane listScrollPaneB = new JScrollPane(SamplefilesList);
        JScrollPane listScrollPaneC = new JScrollPane(BackgroundfilesList);
	    
		getContentPane().setLayout(null);
		setSize(675,624);
		setVisible(true);
		
		getContentPane().add(listScrollPaneA);
		listScrollPaneA.setBounds(67,60,180,384);
		
		getContentPane().add(listScrollPaneB);
		listScrollPaneB.setBounds(439,60,180,168);
		
		getContentPane().add(listScrollPaneC);
		listScrollPaneC.setBounds(439,276,180,168);
		
		JLabel1.setText("Runfiles");
		getContentPane().add(JLabel1);
		JLabel1.setBounds(68,24,171,27);
		
		JLabel2.setText("Sample Runfiles");
		getContentPane().add(JLabel2);
		JLabel2.setBounds(452,24,156,27);
		
		JLabel3.setText("Background Runfiles");
		getContentPane().add(JLabel3);
		JLabel3.setBounds(452,240,156,27);
		
		sampleAddButton.setText("Add");
		sampleAddButton.setActionCommand("Add");
		sampleAddButton.addActionListener(new sampleAddButtonListener());
		getContentPane().add(sampleAddButton);
		sampleAddButton.setBounds(296,96,91,38);
		
		sampleRemoveButton.setText("Remove");
		sampleRemoveButton.setActionCommand("Remove");
		sampleRemoveButton.addActionListener(new sampleRemoveButtonListener());
		getContentPane().add(sampleRemoveButton);
		sampleRemoveButton.setBounds(296,156,91,38);
		
		backgroundAddButton.setText("Add");
		backgroundAddButton.setActionCommand("Add");
		backgroundAddButton.addActionListener(new backgroundAddButtonListener());
		getContentPane().add(backgroundAddButton);
		backgroundAddButton.setBounds(296,312,91,38);
		
		backgroundRemoveButton.setText("Remove");
		backgroundRemoveButton.setActionCommand("Remove");
		backgroundRemoveButton.addActionListener(new backgroundRemoveButtonListener());
		getContentPane().add(backgroundRemoveButton);
		backgroundRemoveButton.setBounds(296,372,91,38);
		
		cancelButton.setText("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(new cancelButtonListener());
		getContentPane().add(cancelButton);
		cancelButton.setBounds(223,572,91,38);
		
		applyButton.setText("Apply");
		applyButton.setActionCommand("Apply");
		applyButton.addActionListener(new applyButtonListener());
		getContentPane().add(applyButton);
		applyButton.setBounds(367,572,91,38);
		
		calibResultButton.setText("Calibration Results");
		calibResultButton.setActionCommand("Calibration Results");
		calibResultButton.addActionListener(new calibResultButtonListener());
		getContentPane().add(calibResultButton);
		calibResultButton.setBounds(48,490,180,38);
		
		chkGroupingButton.setText("Check Grouping");
		chkGroupingButton.setActionCommand("Check Grouping");
		chkGroupingButton.addActionListener(new chkGroupingButtonListener());
		getContentPane().add(chkGroupingButton);
		chkGroupingButton.setBounds(252,490,180,38);
		
		removeNoisyDetButton.setText("Remove Noisy Detectors");
		removeNoisyDetButton.setActionCommand("Remove Noisy Detectors");
		removeNoisyDetButton.addActionListener(new removeNoisyDetButtonListener());
		getContentPane().add(removeNoisyDetButton);
		removeNoisyDetButton.setBounds(456,490,180,36);
		
		
	}
	
	class sampleAddButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                    //Enter Code here for sampleAddButton
                    System.out.println("Now clicked sampleAddButton");
                    
                    if(RunfilesList.getSelectedIndices()!= null)
                    {
                        Object[] leftSelected = RunfilesList.getSelectedValues();
                        String[] value = new String[leftSelected.length];
                       
                            
                        int[] leftSelectedIndices = RunfilesList.getSelectedIndices();
                        DefaultListModel listModelA = (DefaultListModel)RunfilesList.getModel();
                        for (int i = 0; i<leftSelectedIndices.length; ++i)
                        {
                        listModelA.removeElementAt(leftSelectedIndices[i] -i);
                        
                        RunfilesList.setSelectedIndex(i);
                        }
                        for(int i = 0; i<leftSelected.length; i++)
                        {
                        value[i] = leftSelected[i].toString();
                        // System.out.println("The file name is "+ value[i]);
                        listModelB.addElement(value[i]);
                        }
                    }
                }
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
    class sampleRemoveButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                //Enter Code here for sampleRemoveButton
                 System.out.println("Now clicked sampleRemoveButton");
                 
                 if(SamplefilesList.getSelectedIndex()!= -1)
                    {
                        int[] rightSelectedIndices = SamplefilesList.getSelectedIndices();
                        DefaultListModel listModelB = (DefaultListModel)SamplefilesList.getModel();
                        for (int i = 0; i<rightSelectedIndices.length; ++i)
                        {
                            listModelB.removeElementAt(rightSelectedIndices[i] -i);
                            SamplefilesList.setSelectedIndex(i);
                        }
                        int size = listModelB.getSize();

                        if (size == 0)
                        {
                            //sampleRemoveButton.setEnabled(false);
                        }

                    }
                }
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
    class backgroundAddButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                //Enter Code here for backgroundAddButton
                 System.out.println("Now clicked backgroundAddButton");
                 
                 if(RunfilesList.getSelectedIndices()!= null)
                    {
                        Object[] leftSelected = RunfilesList.getSelectedValues();
                        String[] value = new String[leftSelected.length];
                       
                            
                        int[] leftSelectedIndices = RunfilesList.getSelectedIndices();
                        DefaultListModel listModelA = (DefaultListModel)RunfilesList.getModel();
                        for (int i = 0; i<leftSelectedIndices.length; ++i)
                        {
                        listModelA.removeElementAt(leftSelectedIndices[i] -i);
                        
                        RunfilesList.setSelectedIndex(i);
                        }
                        for(int i = 0; i<leftSelected.length; i++)
                        {
                        value[i] = leftSelected[i].toString();
                        // System.out.println("The file name is "+ value[i]);
                        listModelC.addElement(value[i]);
                        }
                    }
                }
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
    class backgroundRemoveButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                //Enter Code here for backgroundRemoveButton
                 System.out.println("Now clicked backgroundRemoveButton");
                 
                 
                 if(BackgroundfilesList.getSelectedIndex()!= -1)
                    {
                        int[] rightSelectedIndices = BackgroundfilesList.getSelectedIndices();
                        DefaultListModel listModelC = (DefaultListModel)BackgroundfilesList.getModel();
                        for (int i = 0; i<rightSelectedIndices.length; ++i)
                        {
                            listModelC.removeElementAt(rightSelectedIndices[i] -i);
                            BackgroundfilesList.setSelectedIndex(i);
                        }
                        int size = listModelC.getSize();

                        if (size == 0)
                        {
                           // backgroundRemoveButton.setEnabled(false);
                        }

                    }
                 
                }
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
    class cancelButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                //Enter Code here for cancelButton
                 System.out.println("Now clicked cancelButton");
                 dispose();   
                }
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
    class applyButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                //Enter Code here for applyButton
                 System.out.println("Now clicked applyButton");
                }
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
    class calibResultButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                //Enter Code here for calibResultButton
                 System.out.println("Now clicked calibResultButton");
                 
                 System.out.println("Sample list is : " +listModelB);
                 
                 System.out.println("BG list is : " +listModelC);
                 IsawHelp("Now clicked calibResultButton");
                }
                
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
    class chkGroupingButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                //Enter Code here for chkGroupingButton
                 System.out.println("Now clicked checkGroupingButton");
                 
                 System.out.println("Sample list is : " +listModelB);
                 
                 System.out.println("BG list is : " +listModelC);
                 IsawHelp("Now clicked CheckGroupingButton");
                }
                
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
    
    class removeNoisyDetButtonListener implements ActionListener 
	{
        public void actionPerformed(ActionEvent e) 
        {

            try{
                //Enter Code here for removeNoisyDetButton
                 System.out.println("Now clicked removeNoisyDetButton");
                 
                 System.out.println("Sample list is : " +listModelB);
                 
                 System.out.println("BG list is : " +listModelC);
                 IsawHelp("Now clicked removeNoisyDetButton");
                 
                }
                
            catch(Exception ex){System.out.println("The exception is " +ex);}
             
        }
    }
    
     public void IsawHelp(String info)
    {
        
        JFrame mm = new JFrame();
        JDialog hh = new JDialog(mm, "ISAW View Help");
        hh.setSize(188,70);
        //Center the opdialog frame 
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension size = hh.getSize();
	    screenSize.height = screenSize.height/2;
	    screenSize.width = screenSize.width/2;
	    size.height = size.height/2;
	    size.width = size.width/2;
	    int y = screenSize.height - size.height;
	    int x = screenSize.width - size.width;
	    hh.setLocation(x-200, y-200);
        JTextArea textArea = new JTextArea(info);
        textArea.setLineWrap(true);
                
        JScrollPane helpScroll = new JScrollPane(textArea);
        hh.getContentPane().add(helpScroll);   
        hh.setVisible(true);
    }
    
     public void valueChanged(ListSelectionEvent e) {}
    

}
