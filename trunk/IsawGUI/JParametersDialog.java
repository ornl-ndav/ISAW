/*
 * @(#)JParametersDialog.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import javax.swing.*;
import DataSetTools.dataset.*;
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

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JParametersDialog implements Serializable
{
    private Operator op;
    private JTreeUI jtui;
    Vector vparamGUI = new Vector();
    JDialog opDialog;
    JLabel resultsLabel = new JLabel("Result");
     JTree tree = null;
    public JParametersDialog(DataSetOperator op, JTreeUI jtui)
    {
        this.op =op;
        this.jtui = jtui;
        opDialog = new JDialog(new JFrame(), op.getTitle(),true);
        opDialog.setSize(700,460);
        opDialog.getContentPane().add(new JLabel("Operation "+op.getTitle()+" on tree node "+jtui.getSelectedNode()));
        
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

 
        int num_param = op.getNum_parameters();
        opDialog.getContentPane().setLayout(new GridLayout(num_param+4,1));
        Parameter param;
        JParameterGUI paramGUI;
        
        for (int i = 0; i<num_param; i++)
        {  
       //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%     
            
            param = op.getParameter(i);
            if (param.getValue() instanceof Float)
                paramGUI = new JFloatParameterGUI(param);
            else if(param.getValue() instanceof Integer)
                paramGUI = new JIntegerParameterGUI(param);
            else if(param.getValue() instanceof Boolean)
                 paramGUI = new JBooleanParameterGUI(param);
            else if(param.getValue() instanceof String)
                paramGUI = new JStringParameterGUI(param);
                else if(param.getValue() instanceof AttributeNameString)
                {
                    DataSet ds =op.getDataSet();
                    Data data = ds.getData_entry(0);
                    paramGUI = new JAttributeNameParameterGUI(param, data.getAttributeList());
                }
           else if(param.getValue() instanceof DataSet)
                 paramGUI = new JDataSetParameterGUI(param, jtui);
                 
                 
            else
            {
                System.out.println("Unsupported Parameter in JParamatersDialog");
                return ;
            }
               
                
            //Add other kinds of parameter types here.
            
            opDialog.getContentPane().add(paramGUI.getGUISegment());
            vparamGUI.addElement(paramGUI);
         //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
            
        }
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
      }
       
  
    
      public class ApplyButtonHandler implements ActionListener
      {
         public void actionPerformed(ActionEvent ev) {
              //  optionPane.setValue(btnString1);
             // System.out.println("buttonpressed" +ev);
             JParameterGUI pGUI;
             for(int i = 0; i<op.getNum_parameters(); i++)
             {
                pGUI = (JParameterGUI)vparamGUI.elementAt(i);
                op.setParameter(pGUI.getParameter(), i);
             
             }
             
             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             Object result = op.getResult();
             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
           if (result == null)
           {System.out.println("Result was null  :");
            
                resultsLabel.setText("Operation ' " +op.getTitle()+ " ' failed");
                //resultsLabel.setBackground(new java.awt.Color(225, 225, 0));
              //  resultsLabel.setBackground(new java.awt.Color(100,10,10));
          }
          else if (result instanceof DataSet)
            {
               // DataSet[] dss = new DataSet[1];
             //   dss[0] = (DataSet)result;
                jtui.addDataSet((DataSet)result);
 
                System.out.println("The new Title is    :" +((DataSet)result).getTitle());
                resultsLabel.setText("Operation ' " +op.getTitle()+ " '  completed");
            }
            else if (result instanceof Float)
            {
                Float value = (Float)result;
                 resultsLabel.setText("Operation ' " +op.getTitle()+ "  gave value "
                 +value.floatValue() );
            }
            else if (result instanceof String)
            {
                String value = (String)result;
                 resultsLabel.setText("Operation ' " +op.getTitle()+ "  gave result "
                 +value );
            }
            else if (result instanceof ErrorString)
            {
                ErrorString value = (ErrorString)result;
                 resultsLabel.setText("ERROR in " +op.getTitle()+ "  : "
                 +value );
            }
            else resultsLabel.setText("Operator result type not supported");
            }
      } 
    
    public class ExitButtonHandler implements ActionListener
    {
         public void actionPerformed(ActionEvent ev) {
              opDialog.dispose();     
    } 
    }
}
