/*
 * @(#)SelectFileData.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import  java.io.*;
import  java.awt.*;
import  java.math.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.ButtonGroup.*;
import javax.swing.border.*;
import javax.swing.JDialog.*;
import javax.swing.JRadioButton.*;
import  DataSetTools.dataset.*;
import  DataSetTools.retriever.*;
import  DataSetTools.util.*;
import DataSetTools.operator.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class SelectFileData extends JPanel implements Serializable
{
    private JDialog opDialog;
    private JTextArea dsText;
    private JComboBox combobox;
    private JRadioButton jrb1, jrb2, jrb3;
    private JCheckBox jck1, jck2;
    private JTextField jta1, jta2;
    private JPanel segment1, segment2, segment3, segment4, segment5;
    private JLabel resultsLabel = new JLabel("Result");
    private DataSet new_ds;
    
    String[] file_name;
    String dirName;
    boolean keep= true; 
    boolean keepAll = false;
    boolean sumSpectra, sumFiles;
    private JTreeUI treeUI;  
    
    public SelectFileData(String[] f_name, JTreeUI treeUI) 
    {
         this.treeUI = treeUI;
        
         
         file_name = new String[f_name.length];
         for (int i =0; i<f_name.length; i++)
            file_name[i] = f_name[i];
            
        opDialog = new JDialog();
        opDialog.setSize(650,480);
        opDialog.getContentPane().add(new JLabel("Load Files based on Selected Attribute Range"));
        
        //Center the opdialog frame 
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension size = opDialog.getSize();
	    screenSize.height = screenSize.height/2;
	    screenSize.width = screenSize.width/2;
	    size.height = size.height/2;
	    size.width = size.width/2;
	    int y = screenSize.height - size.height;
	    int x = screenSize.width - size.width;
	    opDialog.setLocation(x, y);
	    opDialog.getContentPane().setLayout(new GridLayout(8,2));
	    

	    segment1 = new JPanel();
        segment1.setLayout(new GridLayout(1,2));
        segment1.add(new JLabel("Range Parameter: "));
        combobox = new JComboBox();
        combobox.addItem(Attribute.GROUP_ID);
        combobox.addItem(Attribute.RAW_ANGLE);
        combobox.addItem(Attribute.DETECTOR_POS);
        segment1.add(combobox);
            
        segment2 = new JPanel();
        segment2.setLayout(new GridLayout(1,2));
        segment2.add(new JLabel(""));
        jrb1 = new JRadioButton("Include Selected Range",true);
        jrb2 = new JRadioButton("Exclude Selected Range",false);
        jrb3 = new JRadioButton("Include All Range",false);
        
        segment2.add(jrb1);
        segment2.add(jrb2);
        segment2.add(jrb3);
        segment2.setBorder(new TitledBorder(""));
        ButtonGroup group1 = new ButtonGroup();
        group1.add(jrb1);
	    group1.add(jrb2);
	    group1.add(jrb3);
        
        segment3 = new JPanel();
        segment3.setLayout(new GridLayout(1,2));
        segment3.add(new JLabel(""));
        jck1 = new JCheckBox("Sum over Spectra",false);
        jck2 = new JCheckBox("Sum over Files", false);

        segment3.add(jck1);
        segment3.add(jck2);
        
        segment4 = new JPanel();
        segment4.setLayout(new GridLayout(1,2));
        segment4.add(new JLabel("Range Minimum: "));
        jta1 = new JTextField();
        jta1.setSize(200, 100);
        segment4.add(jta1);

         
        segment5 = new JPanel();
        segment5.setLayout(new GridLayout(1,2));
        segment5.add(new JLabel("Range Maximum: "));
        jta2 = new JTextField();
        jta2.setSize(200, 100);
        segment5.add(jta2);

        opDialog.getContentPane().add(segment2);
        opDialog.getContentPane().add(segment1);
        opDialog.getContentPane().add(segment4);
        opDialog.getContentPane().add(segment5);
        opDialog.getContentPane().add(segment3);
        opDialog.getContentPane().add(resultsLabel);
        
        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new FlowLayout());
        JButton apply = new JButton("Apply");
        JButton exit = new JButton("Exit");
        buttonpanel.add(apply);
        apply.addActionListener(new ApplyButtonHandler());
        buttonpanel.add(exit);
        exit.addActionListener(new ExitButtonHandler());
        opDialog.getContentPane().add(buttonpanel);
        opDialog.setVisible(true);

        jrb1.addItemListener(new ItemListener()
     
        {
            public void itemStateChanged(ItemEvent ex) 
            {
              keep =true;
              
             if(!jrb1.isSelected()) 
             {
                keep = true;
                keepAll = false;
             }
            }
        }) ;

        
         jrb2.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent ex) 
            {
               if(jrb2.isSelected()) 
                {
                    keep = false;
                    keepAll = false;  
                }
            }
        }) ;
        
        jrb3.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent ex) 
            {
               if(jrb3.isSelected()) 
                {
                    keepAll = true;
                    keep=false;
                }
            }
        });
        
         jck1.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent ex) 
            {
                if(!jck1.isSelected()) 
                 {resultsLabel.setText("Result:");
                  sumSpectra = false;}
                else sumSpectra = true;
            }
        }) ;
        
         jck2.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent ex) 
            {
                if(!jck2.isSelected()) 
                { resultsLabel.setText("Result:");
                    sumFiles =false;}
                else sumFiles = true;
       
            }
        }) ;
        

    }
    
   
            
    public class ApplyButtonHandler implements ActionListener
      {
         public void actionPerformed(ActionEvent ev) 
         {    
            String attrname = (String)combobox.getSelectedItem();
            String attr_name = new String(attrname);
            float min = Float.valueOf(jta1.getText()).floatValue();
            float max = Float.valueOf(jta2.getText()).floatValue();
            String s1 = jta1.getText();
            String s2= jta2.getText();
            //if(s1.equalsIgnoreCase("NaN") || s2.equalsIgnoreCase("NaN") )
            //min=5;
            //max=10;
 
          //for (int i =0; i<file_name.length; i++)
          /*System.out.println("Print the files in file_name  " +file_name[i]);
          
          System.out.println("The attributes in keep are "+keep);
          System.out.println("The attributes in attr_list are " +attr_name ); 
          
          
          System.out.println("The attributes in attr_list are " +min ); 
          System.out.println("The attributes in attr_list are " +max ); 
          System.out.println("The attributes in sum are "+sum); 
          System.out.println("The attributes in sumFiles are "+sumFiles);*/
           
         opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
           if(sumSpectra && !sumFiles)
           {    DataSet new_ds = extract_summed_Data(file_name, attr_name, keep, min, max);
                treeUI.addDataSet(new_ds);
                
           }
           if(!sumFiles && !sumSpectra) 
            {
                extract_Data(file_name, attr_name, keep, min, max);
          
            }    
            
           if(sumFiles && sumSpectra)
            {
                DataSet new_ds = extract_summed_Data(file_name, attr_name, keep, min, max);
                DataSetOperator  op1;
                op1 = new SumByAttribute(new_ds, attr_name, keep, 0, file_name.length-1);
                DataSet mergedDS1 = (DataSet)op1.getResult(); 
                treeUI.addDataSet(mergedDS1);
                
            }
            
            if(sumFiles && !sumSpectra) 
            {
                
               
                //sumAll_Files(file_name, attr_name, keep, min, max);
                sumAll_Files(file_name); 
                System.out.println("now before executing sumAll_Files");
                
            }    
             if(keepAll) 
            {

                //sumAll_Files(file_name, attr_name, keep, min, max);
                sumAll_Files(file_name); 
                System.out.println("now before executing sumAll_Files");
                
            }    
           
            opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            resultsLabel.setText("Result: Loading Files Completed");
           System.out.println("The keepAll are "+keepAll); 
         }
        
      } 
    
    public class ExitButtonHandler implements ActionListener
    {
         public void actionPerformed(ActionEvent ev){
            opDialog.dispose();  
         }
    }
    
    public Object extract_Data(String[] file_name, String attr_name,
                                boolean keep, float min, float max )
  {
    DataSet          ds;
    DataSet          new_ds = null;
    RunfileRetriever r;
    int              ds_num;
    boolean          is_histogram = false;
    DataSetOperator  op;
    

    if ( file_name.length <= 0 )       // nothing to process
      return null;
      
//file_name.length gives us the size of the array of file names to be loaded
    for ( int i = 0; i < file_name.length; i++ )
    {
      System.out.println("Processing file " + i );

      r = new RunfileRetriever( file_name[i] );

      ds_num = 0;                                    // get the first histogram
      is_histogram = false;
      while ( ds_num < r.numDataSets() && !is_histogram ) 
        if ( r.getType(ds_num) == r.HISTOGRAM_DATA_SET ) 
          is_histogram = true;
        else
          ds_num++; 
  
      ds = r.getDataSet( ds_num );          
                                       // form DataSet with selected entries
                                       // from the histogram using the
                                       // ExtractByAttribute operator
      ds.addLog_entry("Loaded Runfiles " +ds.toString())   ;                              
      op = new ExtractByAttribute( ds, attr_name, keep, min, max );
      ds = (DataSet)op.getResult();   
      
      if ( i <= 0 )                    // first time through, initialize new_ds
        new_ds = (DataSet)ds.empty_clone();     // and attributes from the selected ds
      else
        new_ds.CombineAttributeList( ds );  // subsequently, combine attributes
                                            // from the selected ds
       // new_ds.addLog_entry( "Loaded Runfiles " +ds.toString());
                                      // add the data entries from the selected
                                      // DataSet to the new DataSet
      for ( int k = 0; k < ds.getNum_entries(); k++ )
        new_ds.addData_entry( ds.getData_entry(k) );
    }

    System.out.println("Processing file FINISHED"  );
    treeUI.addDataSet(new_ds);
    return new_ds;
  }  
 
 

  public DataSet extract_summed_Data(String[] file_name, String attr_name, boolean keep, float min, float max )
  {
    DataSet          ds;
    DataSet          new_ds = null;
    Data             d;
    RunfileRetriever r;
    int              ds_num;
    boolean          is_histogram = false;
    DataSetOperator  op;

    for ( int i = 0; i < file_name.length; i++ )
    {
      System.out.println("Processing file " + i );

      r = new RunfileRetriever( file_name[i] );

      ds_num = 0;                                    // get the first histogram
      is_histogram = false;
      while ( ds_num < r.numDataSets() && !is_histogram ) 
        if ( r.getType(ds_num) == r.HISTOGRAM_DATA_SET ) 
          is_histogram = true;
        else
          ds_num++; 

      ds = r.getDataSet( ds_num );          
                                   // form simple DataSet with one entry by
                                   // summing the selected spectra using the
                                   // SumByAttribute operator
       ds.addLog_entry("Loaded  " +ds.toString())   ;                             
                                  
      op = new SumByAttribute( ds, attr_name,keep, min, max );
      ds = (DataSet)op.getResult();   

      if ( i <= 0 )                    // first time through, initialize new_ds
        new_ds =(DataSet) ds.empty_clone();     // and attributes from the first sum ds
      else
        new_ds.CombineAttributeList( ds );  // subsequently, combine attributes
                                            // from the summed ds
      //  new_ds.addLog_entry( "Loaded Runfiles " +ds.toString());
      d = ds.getData_entry(0);         // add the one data entry from the  
      d.setGroup_ID( i );              // summed DataSet as entry #i
      new_ds.addData_entry( d );           
    }
  System.out.println("Processing file FINISHED"  );
  //treeUI.addDataSet(new_ds);
    return new_ds;
  }  
  
  
  public DataSet[] sumAll_Files(String[] file_name //,String attr_name,
                               // boolean keep, float min, float max 
                               )
  {
    DataSet []         ds;
  
    RunfileRetriever r0,r ;
    //int              ds_num;
    //boolean          is_histogram = false;
    DataSetOperator  op;
    

    if ( file_name.length <= 0 )       // nothing to process
      return null;
      
      r0 = new RunfileRetriever( file_name[0] );
      int numberOfDataSets0 = r0.numDataSets();
      ds = new DataSet[numberOfDataSets0];
        for (int i = 0; i< numberOfDataSets0;i++)
             ds[i] = r0.getDataSet(i);
      
//file_name.length gives us the size of the array of file names to be loaded
    for ( int i = 1; i < file_name.length; i++ )
    {
      System.out.println("Processing file " + i );

      r = new RunfileRetriever( file_name[i] );
      int numberOfDataSets = r.numDataSets();
      DataSet[] dss = new DataSet[numberOfDataSets];
      
      for (int j = 0; j< numberOfDataSets;j++)
      {     
            dss[j] = r.getDataSet(j);
            op = new DataSetAdd(ds[j], dss[j], true);
            ds[j] = (DataSet)op.getResult();
           //ds[j].addLog_entry( "Loaded Runfiles " +ds[j].toString());
       }                 
    }
     for (int j = 0; j< numberOfDataSets0;j++)
        treeUI.addDataSet(ds[j]);
    System.out.println("Processing file FINISHED"  );
    //treeUI.addDataSet(new_ds);
    return ds;
  }  
  
  public void keyPressed(KeyEvent evt)
  {
  int keycode = evt.getKeyCode();
  if(keycode == KeyEvent.VK_TAB && evt.isShiftDown())
   {Component current_field = (Component) evt.getComponent();
           if (current_field ==jta1)
           System.out.println("Now in focus");
        jta2.transferFocus();
   }

  }
}
