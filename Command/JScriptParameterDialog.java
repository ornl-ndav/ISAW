/*
 * @(#)JScriptParameterDialog.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package Command;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.File;
import java.io.*;
import javax.swing.JDialog.*;
import javax.swing.tree.TreeCellRenderer.*;
import javax.swing.tree.*;
import javax.swing.JTree.*;
import DataSetTools.util.*;
import java.util.zip.*;
import java.io.Serializable;
import IsawGUI.*;
/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JScriptParameterDialog implements Serializable
{
   
  
    Vector V;
    Vector vparamGUI = new Vector();
    JDialog opDialog;
    JLabel Mess;
    
    public Vector getResult()
    { return V;
    }
     
    //First entry is a Descriptive String 
    // Other Entries are of type JParameterGUI
    public JScriptParameterDialog(Vector V, DataSet[] DS ) //DataSetOperator op, JTreeUI jtui)
    {
        this.V = V;
        if( this.V == null ) V = new Vector();
        opDialog = new JDialog(new JFrame(), "Data Entry" ,true);
        opDialog.setSize(700,600);
        opDialog.getContentPane().add(new JLabel( (String)(V.firstElement() ) ));
        
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

 
	    int num_param = V.size() - 1 ;
	 
        opDialog.getContentPane().setLayout(new GridLayout(num_param+5,1));
        Parameter param;
        JParameterGUI paramGUI;
      
        for (int i = 0; i<num_param; i++)
        {  
       //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%     
            
            param =((JParameterGUI)( V.get(i+1))).getParameter();
	    //System.out.println("JScrParGUI DT="+param.getValue());
            if( param.getValue() == null )
                 paramGUI = new JObjectParameterGUI(param);
            else if (param.getValue() instanceof Float)
                paramGUI = new JFloatParameterGUI(param);
            else if(param.getValue() instanceof Integer)
                paramGUI = new JIntegerParameterGUI(param);
            else if(param.getValue() instanceof Boolean)
                 paramGUI = new JBooleanParameterGUI(param);
            else if(param.getValue() instanceof String)
                paramGUI = new JStringParameterGUI(param);
           
           else if(param.getValue() instanceof DataSet)
	        paramGUI = new JlocDataSetParameterGUI(param , DS );    
          
                 
            else if( param.getValue() instanceof DataDirectoryString )
             { String DirPath = System.getProperty("DataDirectory");
               if( DirPath != null )
                   DirPath = DataSetTools.util.StringUtil.fixSeparator(DirPath+"\\");
               else
                   DirPath = "";
               param.setValue( DirPath );
               paramGUI = new JStringParameterGUI( param) ;
              }
          else if (param.getValue() instanceof DSFieldString)
            {String Fields[] = {"Title","X_label", "X_units","PointedAtIndex","SelectFlagOn",
                       "SelectFlagOff","SelectFlag","Y_label","Y_units","MaxGroupID",
                        "MaxXSteps","MostRecentlySelectedIndex","NumSelected" , "XRange",
                       "YRange"};
             AttributeList A = new AttributeList();
            // Attribute A1;
             for( int k =0; k< Fields.length; k++)
              {
                    A.addAttribute( new StringAttribute( Fields[i] , ""));
              }

             paramGUI =  new JAttributeNameParameterGUI(param  , A);
            }
          else if( param.getValue() instanceof InstrumentNameString)
            {String XX = System.getProperty("DefaultInstrument");
             if( XX == null )
               XX = "";
            param.setValue(XX);
            paramGUI= new JStringParameterGUI( param);
            }    
            else
            {
                System.out.println("Unsupported Parameter in JParamatersDialog"+param.getValue().getClass());
                return ;
            }
               
                
            //Add other kinds of parameter types here.
            
            opDialog.getContentPane().add(paramGUI.getGUISegment());
            vparamGUI.addElement(paramGUI);
         //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
            
        }
               
                 JPanel buttonpanel = new JPanel();
                 buttonpanel.setLayout(new FlowLayout());
                 JButton apply = new JButton("Apply");
                 JButton exit = new JButton("Exit");
                 buttonpanel.add(apply);
                 apply.addActionListener(new ApplyButtonHandler());
                 buttonpanel.add(exit);
                 exit.addActionListener(new ExitButtonHandler());
                 opDialog.getContentPane().add(buttonpanel);
                 Mess = new JLabel("   ");
                 opDialog.getContentPane().add(Mess);
                 opDialog.setVisible(true);
      }
       
     public void setMessage( String S)
       { Mess.setText(S);
      }
    
      public class ApplyButtonHandler implements ActionListener
      {
         public void actionPerformed(ActionEvent ev) {
              //  optionPane.setValue(btnString1);
             // System.out.println("buttonpressed" +ev);
             JParameterGUI pGUI;
             for(int i = 0; i < vparamGUI.size(); i++)
             {
                pGUI = (JParameterGUI)vparamGUI.elementAt( i  );
                V.setElementAt(pGUI, i + 1  );
             
             }
             
             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             //Object result = op.getResult();
             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               opDialog.dispose();     
	   
	 }
           
      }
       
    
    public class ExitButtonHandler implements ActionListener
    {
         public void actionPerformed(ActionEvent ev) {
	       V = null;
              opDialog.dispose();     
                                                    } 
    }
      
}
