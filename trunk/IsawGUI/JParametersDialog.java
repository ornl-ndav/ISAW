/*
 * @(#)JParametersDialog.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

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
import javax.swing.text.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class JParametersDialog implements Serializable
{
    private DataSetOperator op;
    private JTreeUI jtui;  
    Util util = new Util();
    Document sessionLog;  
    Vector vparamGUI = new Vector();
    JDialog opDialog;
    JLabel resultsLabel = new JLabel("    Result");
     JTree tree = null;
    public JParametersDialog(DataSetOperator op, JTreeUI jtui, Document sessionLog)
    {
        this.op =op;
        this.jtui = jtui;
	  this.sessionLog = sessionLog;
        opDialog = new JDialog(new JFrame(), op.getTitle(),true);
        opDialog.setSize(570,450);
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
        opDialog.getContentPane().setLayout(new GridLayout(num_param+5







,1));
        Parameter param;
        JParameterGUI paramGUI;
        op.setDefaultParameters();
        for (int i = 0; i<num_param; i++)
        {  
       //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%     
            
            param = op.getParameter(i);
            if(param.getValue() == null)
                paramGUI = new JObjectParameterGUI(param);
            else if (param.getValue() instanceof Float)
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
    			AttributeList attr_list; // if the operator is a DataSet 
                             // operator, get the list from the
                             // DataSet, otherwise get it from the
                             // data block.
    			if ( op.getTitle().indexOf( "DataSet" ) >= 0 )
      		attr_list = ds.getAttributeList();
    		else
      	{
         		Data data = ds.getData_entry(0);
         		if ( data != null )
           		attr_list = data.getAttributeList();
         	else
           		attr_list = new AttributeList();
       	}
    			paramGUI = new JAttributeNameParameterGUI(param, attr_list);
  		}
           else if(param.getValue() instanceof DataSet)
                 paramGUI = new JDataSetParameterGUI(param, jtui);
                
            else if( param.getValue() instanceof DataDirectoryString )
             { String DirPath = System.getProperty("DataDirectory");
               if( DirPath != null )
                   DirPath = DataSetTools.util.StringUtil.fixSeparator(DirPath+"\\");
               else
                   DirPath = "";
               param.setValue( DirPath );
               paramGUI = new JStringParameterGUI( param) ;
              }

         /*@@@@@
            else if (param.getValue() instanceof DSFieldString)
            {String Fields[] = {"Title","X_label", "X_units","PointedAtIndex","SelectFlagOn",
                       "SelectFlagOff","SelectFlag","Y_label","Y_units","MaxGroupID",
                        "MaxXSteps","MostRecentlySelectedIndex","NumSelected" , "XRange",
                       "YRange"};
             AttributeList A = new AttributeList();
            // Attribute A1;
             for( int k =0; k< Fields.length; k++)
              {
                    A.addAttribute( new StringAttribute( Fields[k] , ""));
              }

             paramGUI =  new JAttributeNameParameterGUI(param  , A);
            }
         @@@@*/

	else if (param.getValue() instanceof IStringList )
            {
            	 AttributeList A = new AttributeList();
             	int num_strings =
		((IStringList)param.getValue()).num_strings();
             	for ( int k = 0; k < num_strings; k++ )
             	{
              		 String str =
			((IStringList)param.getValue()).getString(k);
               		A.addAttribute( new StringAttribute( str, "" ));
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
             String s= op.getDataSet().toString();
             if (op.getNum_parameters()>0)
                  s = s +", ";

             for(int i = 0; i<op.getNum_parameters(); i++)
             {
                pGUI = (JParameterGUI)vparamGUI.elementAt(i);
                op.setParameter(pGUI.getParameter(), i);
                s = s + pGUI.getParameter().getValue();
                if (i<op.getNum_parameters()-1)
                  s = s +", ";
             
             }
             //util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");
             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             
              Object result = op.getResult();
		            



             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
           if (result == null)
           {System.out.println("Result was null  :");
            
                resultsLabel.setText("Operation failed");
                 util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

                //resultsLabel.setBackground(new java.awt.Color(225, 225, 0));
              //  resultsLabel.setBackground(new java.awt.Color(100,10,10));
          }
          else if (result instanceof DataSet)
            {
               // DataSet[] dss = new DataSet[1];
             //   dss[0] = (DataSet)result;
                jtui.addDataSet((DataSet)result);	
                
                System.out.println("The new Title is    :" +((DataSet)result).getTitle());
                resultsLabel.setText("Operation completed");
                util.appendDoc(sessionLog,  ((DataSet)result).toString()+"="+op.getCommand()+"(" +s +")");

            }
            else if (result instanceof Float)
            {
                Float value = (Float)result;
                 resultsLabel.setText("Result:  "
                 +value.floatValue() );
                 util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

            }
            else if (result instanceof String)
            {
                String value = (String)result;
                 resultsLabel.setText("Result:  "
                 +value );
                 util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

            }
            else if (result instanceof ErrorString)
            {
                ErrorString value = (ErrorString)result;
                 resultsLabel.setText("Operation failed" );
                 util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

            }
            else if( result instanceof int[])
              { int X[] = (int[]) result;
                String SS = IntList.ToString( X );
                resultsLabel.setText(SS);
                util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

               }
            else
                {
                  resultsLabel.setText("Result ="+ result.toString());
                 util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");
                }
            }
      } 
    
    public class ExitButtonHandler implements ActionListener
    {
         public void actionPerformed(ActionEvent ev) {
              opDialog.dispose();     
    } 
    }
}
