/*
 * @(#)Load.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.*;
import java.io.File.*;
import javax.swing.*;
import javax.swing.event.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class LoadFiles extends JFrame 
                      implements ListSelectionListener {
    private JList listA;
    private JList listB;
    private DefaultListModel listModelA;
    private DefaultListModel listModelB;

    private static final String addString = "Add";
    private static final String removeString = "Remove";
    private static final String nextString = "Load";
    private static final String cancelString = "Cancel";
    private JButton removeButton;
    private JButton addButton;
    private JButton nextButton;
    private JButton cancelButton ;
    private JTextField fileName;
    private Vector theResults;
    private Vector loadedElements;
    private JTreeUI treeUI;
    private FileDialog fd;
    private String dir;
    public LoadFiles(JTreeUI treeUI) {
        super("File Loader");
         this.treeUI = treeUI;
        listModelA = new DefaultListModel();
        listModelB = new DefaultListModel();
       
        fd = new FileDialog(new Frame(), "Choose Folder", FileDialog.LOAD);
        if (dir!=null)
        fd.setDirectory(dir);
        //fd.setDirectory("H:\\UPLOAD\\Data");
        fd.show();
        theResults = new Vector();
        loadedElements = new Vector();
        File f = new File(fd.getDirectory(), fd.getFile());
        dir = fd.getDirectory();
        
        String[] fileList = new File(dir).list();//dir listing
     
        for(int i = 0; i<fileList.length; i++)
        {
           // fileList[i] = dir + fileList[i];
            theResults.addElement( fileList[i]);
           // if( fileList[i].toString().endsWith(".RUN")||fileList[i].toString().endsWith(".run"))
            if( fileList[i].toString().endsWith(".RUN")||
            fileList[i].toString().endsWith(".run")||
            fileList[i].toString().endsWith("RUN;1")||
            fileList[i].toString().endsWith(".zip"))
            listModelA.addElement(fileList[i]);
        }
         if(theResults != null)
        
        //Create the list and put it in a scroll pane
        listA = new JList(listModelA);
        listB = new JList(listModelB);
     
        listA.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listB.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listA.setSelectedIndex(0);
        listA.addListSelectionListener(this);
        listB.addListSelectionListener(this);
        JScrollPane listScrollPaneA = new JScrollPane(listA);
        JScrollPane listScrollPaneB = new JScrollPane(listB);
         
        JButton addButton = new JButton(addString);
        addButton.setActionCommand(addString);
        addButton.addActionListener(new AddListener());

        removeButton = new JButton(removeString);
        removeButton.setActionCommand(removeString);
        removeButton.addActionListener(new RemoveListener());
        
        nextButton = new JButton(nextString);
        nextButton.setActionCommand(nextString);
        nextButton.addActionListener(new nextListener());
        
        cancelButton = new JButton(cancelString);
        cancelButton.setActionCommand(cancelString);
        cancelButton.addActionListener(new cancelListener());

        fileName = new JTextField(25);
        fileName.addActionListener(new AddListener());

        
        String name = listA.getSelectedValue().toString();
        fileName.setText(dir+name);
        
        
       JLabel JLabel1 = new JLabel();
       JLabel JLabel2 = new JLabel();
       
        getContentPane().setLayout(null);
 
        JLabel1.setText("Select Files from List ");
		getContentPane().add(JLabel2);
		JLabel1.setBounds(48,4,152,28);
        
        JLabel2.setText("Files to Load ");
		getContentPane().add(JLabel1);
		JLabel2.setBounds(332,4,142,28);
		
		
		getContentPane().add(listScrollPaneA);
		listScrollPaneA.setBounds(43,40,158,184);
		
		getContentPane().add(listScrollPaneB);
		listScrollPaneB.setBounds(332,40,163,184);
	
		getContentPane().add(fileName);
		fileName.setBounds(5,236,250,24);
		
		getContentPane().add(addButton);
		addButton.setBounds(216,100,100,24);
			
		getContentPane().add(removeButton);
		removeButton.setBounds(216,140,100,24);
		
		getContentPane().add(nextButton);
		nextButton.setBounds(332,236,77,24);
		
		getContentPane().add(cancelButton);
		cancelButton.setBounds(410,236,80,24); 
         
    }

    class RemoveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) 
        {
      
            try{
                    if(listB.getSelectedIndex()!= -1)
                    {
                    Object[] rightSelected = listB.getSelectedValues();
                    int[] rightSelectedIndices= listB.getSelectedIndices();
                    String[] value = new String[rightSelectedIndices.length];
                    
                    DefaultListModel listModelB = (DefaultListModel)listB.getModel();
                     DefaultListModel listModelA = (DefaultListModel)listA.getModel();
                    for (int i = 0; i<rightSelectedIndices.length; ++i)
                    {
                        listModelB.removeElementAt(rightSelectedIndices[i] -i);
                        listB.setSelectedIndex(i);
                    }
                    for(int i = 0; i<rightSelectedIndices.length; i++)
                    {
                        value[i] = rightSelected[i].toString();
                        listModelA.addElement(value[i]);
                    }
                    int size = listModelB.getSize();

                    if (size == 0)
                    {
                       // removeButton.setEnabled(false);
                    }

                }
        }
        catch(Exception ex){System.out.println("The exception is " +ex);}
        }
    }

    //This listener is shared by the text field and the add button
    class AddListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

    try{
        if(listA.getSelectedIndices()!= null){
            Object[] leftSelected = listA.getSelectedValues();
            String[] value = new String[leftSelected.length];
           
                
             int[] leftSelectedIndices = listA.getSelectedIndices();
             DefaultListModel listModelA = (DefaultListModel)listA.getModel();
             for (int i = 0; i<leftSelectedIndices.length; ++i)
             {
             listModelA.removeElementAt(leftSelectedIndices[i] -i);
            
             listA.setSelectedIndex(i);
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

      class nextListener implements ActionListener {
              public void actionPerformed(ActionEvent e) {    
            System.out.println("Now inside the Load Button  ");
            int size = listB.getModel().getSize();
            String[] file_name = new String[size];
            
            file_name[0] =fd.getDirectory()+listB.getModel().getElementAt(0).toString();
            System.out.println("name of the file or type is" +file_name[0]);
            
           /* if(file_name[0].endsWith("zip"))
            {
                try{
                ZipFile f = new ZipFile(file_name[0]);
                Enumeration entries = f.entries();
                System.out.println("Decompressing "+file_name[0]+" ...");
                while(entries.hasMoreElements())
                {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    System.out.println("  "+entry.getName());
                
                   InputStream in = f.getInputStream(entry);
                  
                   FileOutputStream out = new FileOutputStream(entry.getName());
                    for(int ch=in.read();ch!=-1;ch=in.read()) 
                    out.write(ch);
                   
                    out.close();
                    in.close(); 
                }
                f.close(); 
                }
                catch(Exception ex ){System.out.println("the exception in zip is "+ex);}
            } 
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        */
            for (int i =0; i<size; i++)
            {
             
            file_name[i] =fd.getDirectory()+listB.getModel().getElementAt(i).toString();
            System.out.println("Print the files in listB  " +file_name[i]);
             RunfileRetriever r = new RunfileRetriever( file_name[i] );
             int numberOfDataSets = r.numDataSets();
                         DataSet[] dss = new DataSet[numberOfDataSets];

                        for (int j = 0; j< numberOfDataSets;j++)
                            dss[j] = r.getDataSet(j);
                            treeUI.addDataSets(dss, listB.getModel().getElementAt(i).toString());
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
           
             dispose();  
      
        }
    }
    
    class cancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {  
         dispose();    
        }
    }
    
    
   /* public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {

            if (listB.getSelectedIndex() == -1) {
                removeButton.setEnabled(false); 
            } 
            else 
            {
                //Selection, update text field.
                removeButton.setEnabled(true);
                String name = listB.getSelectedValue().toString();
                fileName.setText(fd.getDirectory()+name);
            } 
            
            if (listA.getSelectedIndex() != -1) 
            { 
                String name = listA.getSelectedValue().toString();
                fileName.setText(fd.getDirectory()+name);
            }
            
        }
    }*/
     public void valueChanged(ListSelectionEvent e) {}
    
}
