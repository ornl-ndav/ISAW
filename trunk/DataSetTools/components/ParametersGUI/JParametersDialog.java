/*
 * @(#)JParametersDialog.java     1.0  99/09/02  Alok Chatterjee
 *
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 *
 * 5-12-2001   Ruth Mikkelson Changed parameter from DataSetOperator to Operator in 
 *             Constructor so it can be used with generic operators
 * 5-12-2001   Ruth Mikkelson added the Array parameterGUI
 * 5-17-2001   A null result is no longer shows as an error
 */
 
//To Do:  Make it work when jtui is null
//        Extract the Display Result and place in gen utilities
//          class.  Needed for CommandPane too.
//        -IsawGUI: Get the JMenu up somewhere in Isaw's Menu bar
//         Use Command.opMenu to plug in
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.*;
import Command.*;
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

public class JParametersDialog implements Serializable,
                               IObservable
{
    private Operator op;
    private DataSet  ds_list[];  
    IsawGUI.Util util = new IsawGUI.Util();
    Document sessionLog;  
    Vector vparamGUI = new Vector();
    JDialog opDialog;
    JLabel resultsLabel = new JLabel("    Result");
      ApplyButtonHandler APH;

    
    
    public JParametersDialog(Operator op, DataSet ds_list[], Document sessionLog)
    {   
        this.op =op;
        this.ds_list = ds_list;
	this.sessionLog = sessionLog;    
        
        opDialog = new JDialog( new JFrame(), op.getTitle(),true);
      
        opDialog.setSize(570,450);
        String SS ="Operation "+op.getTitle();
        

        if(op instanceof DataSetOperator)
            SS = SS +" on tree node "+((DataSetOperator)op).getDataSet();
        opDialog.getContentPane().add(new JLabel(SS));
        APH = new ApplyButtonHandler();

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
       
        opDialog.getContentPane().setLayout(new GridLayout(num_param+5,1));
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
            else if(param.getValue() instanceof Vector)
                paramGUI = new JArrayParameterGUI(param);
            else if((param.getValue() instanceof AttributeNameString) &&(op instanceof DataSetOperator))
  		  {
    			DataSet ds =((DataSetOperator)op).getDataSet();
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
           else if((param.getValue() instanceof DataSet)  )
                 paramGUI = new JDataSetParameterGUI(param, ds_list);
                
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
                 apply.addActionListener( APH );
                 
                 buttonpanel.add(exit);
                 exit.addActionListener(new ExitButtonHandler());
                 opDialog.getContentPane().add(buttonpanel);
                 opDialog.validate();
                 
                 
                 opDialog.setVisible(true);
                

      }
       
  public void addIObserver(IObserver iobs) 
     {APH.addIObserver(iobs);
     }
               
  public void deleteIObserver(IObserver iobs) 
    {APH.deleteIObserver(iobs);
     }
               
  public void deleteIObservers() 
     {APH.deleteIObservers();
     }
                

  

    
      public class ApplyButtonHandler implements ActionListener,
                                      IObservable

      {
         IObserverList OL;
        public ApplyButtonHandler()
           {OL = new IObserverList();
           }
         public void addIObserver(IObserver iobs) 
           {OL.addIObserver(iobs);
            }
               
          public void deleteIObserver(IObserver iobs) 
            {OL.deleteIObserver(iobs);
             }
               
         public void deleteIObservers() 
          {OL.deleteIObservers();
             }

         public void actionPerformed(ActionEvent ev) {
              //  optionPane.setValue(btnString1);
             // System.out.println("buttonpressed" +ev);
             JParameterGUI pGUI;
             String s="";
             if(op instanceof DataSetOperator)
                {s = ((DataSetOperator)op).getDataSet().toString();
                 if (op.getNum_parameters() > 0)
                    s = s +", ";
                 }

             for(int i = 0; i < op.getNum_parameters(); i++)
             {
                pGUI = (JParameterGUI)vparamGUI.elementAt( i );
                if( pGUI.getParameter() != null)
		    if(pGUI.getParameter().getValue() !=null)
                  {                  
                    op.setParameter( pGUI.getParameter(), i) ;
                    s = s + pGUI.getParameter().getValue();
                    if (i < op.getNum_parameters() - 1)
                      s = s + ", ";
                  }
             
             }
             //util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");
             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             
              Object result = op.getResult();
		            



             opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
           if (result == null)
           {System.out.println("Result was null  :");
            
                resultsLabel.setText("Result was null  :");
                util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

                //resultsLabel.setBackground(new java.awt.Color(225, 225, 0));
              //  resultsLabel.setBackground(new java.awt.Color(100,10,10));
          }
          else if (result instanceof DataSet)
            {
               // DataSet[] dss = new DataSet[1];
             //   dss[0] = (DataSet)result;
               // jtui.addDataSet((DataSet)result);
              OL.notifyIObservers( this , (DataSet)result);	
                
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
                 resultsLabel.setText("Operation failed:"+value.toString() );
                 util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

            }
            else if( result instanceof int[])
              { int X[] = (int[]) result;
                String SS = IntList.ToString( X );
                resultsLabel.setText(SS);
                util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

               }
            else if( result instanceof Vector )
               {resultsLabel.setText("Result ="+ execOneLine.Vect_to_String((Vector)result));
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
