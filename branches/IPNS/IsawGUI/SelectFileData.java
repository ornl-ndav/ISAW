/*
 * @(#)SelectFileData.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawTools
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
    private JRadioButton jrb1, jrb2;
    private JCheckBox jck1, jck2;
    private JTextField jta1, jta2;
    private JPanel segment1, segment2, segment3, segment4, segment5;
    private JLabel resultsLabel = new JLabel("Result");
    
    String[] file_name;
    boolean keep= true; 
    boolean sum, summed;
    private JTreeUI treeUI;  
    
    public SelectFileData(String[] f_name, JTreeUI treeUI) 
    {
         this.treeUI = treeUI;
 
         file_name = new String[f_name.length];
         for (int i =0; i<f_name.length; i++)
         {
            file_name[i] = f_name[i];
            //System.out.println("Print the files in f_name " +file_name[i]);
         }
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
        
        segment2.add(jrb1);
        segment2.add(jrb2);
        segment2.setBorder(new TitledBorder(""));
        ButtonGroup group1 = new ButtonGroup();
        group1.add(jrb1);
	    group1.add(jrb2);
        
        segment3 = new JPanel();
        segment3.setLayout(new GridLayout(1,2));
        segment3.add(new JLabel(""));
        jck1 = new JCheckBox("Sum over Spectra",false);
        jck2 = new JCheckBox("Sum over Files", false);
 
        
        segment3.add(jck1);
       // segment3.add(jck2);
 

        //segment3.setBorder(new TitledBorder(""));
        
        segment4 = new JPanel();
        segment4.setLayout(new GridLayout(1,2));
        segment4.add(new JLabel("Range Minimum: "));
        jta1 = new JTextField();
        jta1.setSize(200, 100);
       
       //jta1.setNextFocusableComponent(jta2);
      
        segment4.add(jta1);

         
        segment5 = new JPanel();
        segment5.setLayout(new GridLayout(1,2));
        segment5.add(new JLabel("Range Maximum: "));
        jta2 = new JTextField();
        jta2.setSize(200, 100);
        segment5.add(jta2);
        
       // ButtonGroup group2 = new ButtonGroup();
	   // group2.add(jck1);
	   // group2.add(jck2);

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

        jrb1.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent ex) 
            {
              keep =true;
             if(!jrb1.isSelected()) 
             {
               // jrb2.setEnabled(false);
                keep = false;
             }
            }
        }) ;

        
         jrb2.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent ex) 
            {
                if(jrb2.isSelected()) 
                {
                   // jrb1.setEnabled(false);
                    keep = false;
                }
            }
        }) ;
        
         jck1.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent ex) 
            {
                if(!jck1.isSelected()) 
                  sum = false;
                else sum = true;
            }
        }) ;
        
         jck2.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent ex) 
            {
                if(!jck2.isSelected()) 
                    summed =false;
                else summed = true;
       
            }
        }) ;
        

    }
    
    
    public class ApplyButtonHandler implements ActionListener
      {
         public void actionPerformed(ActionEvent ev) 
         {    
            String attrname = (String)combobox.getSelectedItem();
            AttributeNameString attr_name = new AttributeNameString(attrname);
            float min = Float.valueOf(jta1.getText()).floatValue();
            float max = Float.valueOf(jta2.getText()).floatValue();

 
          for (int i =0; i<file_name.length; i++)
          System.out.println("Print the files in file_name  " +file_name[i]);
          
          System.out.println("The attributes in keep are "+keep);
          System.out.println("The attributes in attr_list are " +attr_name ); 
          
          
          System.out.println("The attributes in attr_list are " +min ); 
          System.out.println("The attributes in attr_list are " +max ); 
          System.out.println("The attributes in sum are "+sum); 
          System.out.println("The attributes in summed are "+summed);
         opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
           if(sum)
            extract_summed_Data(file_name, attr_name, keep, min, max);
            else 
            extract_Data(file_name, attr_name, keep, min, max);
            opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            resultsLabel.setText("Loading Files Completed");
         }
      } 
    
    public class ExitButtonHandler implements ActionListener
    {
         public void actionPerformed(ActionEvent ev){
            opDialog.dispose();  
         }
    }
    
    public Object extract_Data(String[] file_name, AttributeNameString attr_name,
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
                                       // SelectData operator
      op = new SelectData( ds, attr_name, keep, min, max );
      ds = (DataSet)op.getResult();   

      if ( i <= 0 )                    // first time through, initialize new_ds
        new_ds = (DataSet)ds.empty_clone();     // and attributes from the selected ds
      else
        new_ds.CombineAttributeList( ds );  // subsequently, combine attributes
                                            // from the selected ds

                                      // add the data entries from the selected
                                      // DataSet to the new DataSet
      for ( int k = 0; k < ds.getNum_entries(); k++ )
        new_ds.addData_entry( ds.getData_entry(k) );
    }

    System.out.println("Processing file FINISHED"  );
    treeUI.addDataSet(new_ds);
    return new_ds;
  }  
 
 

  public Object extract_summed_Data(String[] file_name, AttributeNameString attr_name, boolean keep, float min, float max )
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
                                   // SumSelectedData operator
      op = new SumSelectedData( ds, attr_name,keep, min, max );
      ds = (DataSet)op.getResult();   

      if ( i <= 0 )                    // first time through, initialize new_ds
        new_ds =(DataSet) ds.empty_clone();     // and attributes from the first sum ds
      else
        new_ds.CombineAttributeList( ds );  // subsequently, combine attributes
                                            // from the summed ds

      d = ds.getData_entry(0);         // add the one data entry from the  
      d.setGroup_ID( i );              // summed DataSet as entry #i
      new_ds.addData_entry( d );           
    }
  System.out.println("Processing file FINISHED"  );
  treeUI.addDataSet(new_ds);
    return new_ds;
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
  
  /*
  public Object extract_summed_File_Data(String[] file_name, AttributeNameString attr_name, boolean keep, float min, float max )
  {
    DataSet          ds;
    DataSet          new_ds = null;
    Data             d;
    RunfileRetriever r;
    int              ds_num;
    boolean          is_histogram = false;
    DataSetOperator  op;
    
    /////////////////////////////////
    
     int size = file_name.length;
     
            for (int i =0; i<size; i++)
            {
             
                          
            }
    
    
    /////////////////////////////

    for ( int i = 0; i < file_name.length; i++ )
    {
        RunfileRetriever r = new RunfileRetriever( file_name[i] );
        int numberOfDataSets = r.numDataSets();
        DataSet[] dss = new DataSet[numberOfDataSets];

         for (int j = 0; j< numberOfDataSets;j++)
              dss[j] = r.getDataSet(j);
       DataSet ds_to_add = (DataSet)dss[j+1];

                                     // get the current data set
    DataSet ds = dss[0];

    System.out.println( "ds        = " + ds );
    System.out.println( "ds_to_add = " + ds_to_add );

    if ( !ds.SameUnits( ds_to_add ) )// DataSets are NOT COMPATIBLE TO COMBINE
      {
        ErrorString message = new ErrorString(
                           "ERROR: DataSets have different units" );
        System.out.println( message );
        return message;
      }
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = (DataSet)ds.empty_clone(); 
    new_ds.addLog_entry( "Added " + ds_to_add );

                                            // do the operation
    int num_data = ds.getNum_entries();
    Data data,
         add_data,
         new_data;
    for ( int i = 0; i < num_data; i++ )
    {
      data = ds.getData_entry( i );        // get reference to the data entry

      add_data = ds_to_add.getData_entry_with_id( data.getGroup_ID() );
 
      if ( add_data != null )              // there is a corresponding entry
      {                                    // to try to add
        new_data = data.add( add_data );  
        if ( new_data != null )            // if they could be added
          new_ds.addData_entry( new_data );      
      }
  System.out.println("Processing file FINISHED"  );
  treeUI.addDataSet(new_ds);
    return new_ds;
  }  
 */
}
