/*
 * File: JParametersDialog.java
 *
 * Copyright (C) 1999-2001, Alok Chatterjee, Ruth Mikkelson
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
 * Contact : Alok Chatterjee achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 S. Cass Avenue, Bldg 360
 *           Argonne, IL 60440
 *           USA
 *
 * For further information, see http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.19  2001/11/19 19:01:53  dennis
 *   Added a new constructor that lets this dialog box be modal. (Ruth)
 *
 *  Revision 1.18  2001/11/09 18:16:20  dennis
 *  1. Eliminated reporting of the command is "UNKNOWN".
 *  2. Set the Result label to "" before executing a getResult.
 *  3. Introduced an addWindowListener method.
 *
 *  Revision 1.17  2001/08/16 22:27:20  rmikk
 *  Got the initial size of the dialog box closer for 0 arguments and many arguments
 *
 *  Revision 1.16  2001/08/15 02:10:17  rmikk
 *  Removed the setDefaultParameters. Now the parameter
 *  values( except Object types) will retain their values.
 *  Restored only the Object parameters to the default
 *    Object value of null;
 *
 *  Revision 1.15  2001/08/14 16:17:14  rmikk
 *  The Dialog Box in now NOT modal
 *
 *  Revision 1.14  2001/08/13 14:37:11  rmikk
 *  Increased size for operators with zero parameters.
 *  The first line under the title bar has the command name
 *      for the operation not the title (which is in the title bar ).
 *
 *  Revision 1.13  2001/08/10 18:40:14  rmikk
 *  Adjusted the size of the dialog box to reduce the amount
 *  of excess space when  there are a lot of options
 *
 *  Revision 1.12  2001/08/09 14:12:25  rmikk
 *  Added a getResult method to the dialog box so that Java
 *  programmers can actually get the result of an operation.
 *  This could potentially make JParametersDialog into an
 *  InputBox in Java Programs.
 *
 *  Revision 1.11  2001/08/07 21:00:25  rmikk
 *  Changed opDialog's layout to a Box layout.  Fine tuned
 *  colors and centering.
 *
 *  Revision 1.10  2001/08/06 22:15:21  rmikk
 *  Fixed IStringList and SpecialString parameter values to
 *  return those values
 *
 *  Revision 1.9  2001/08/06 20:16:29  rmikk
 *  Added IntListString parameter type.
 *  Added code to take care of DataSet[] results.
 *
 *  Revision 1.8  2001/07/18 16:27:47  neffk
 *  now uses an IDataSetListHandler object to get/keep a current list
 *  of DataSet objects.
 *
 *  Revision 1.7  2001/07/11 16:27:38  neffk
 *  added IntervalSelectionOP as a valid type of GUI parameter.
 *
 *  Revision 1.6  2001/06/26 18:37:40  dennis
 *  Added Copyright and GPL license.
 *  Removed un-needed imports and improved
 *  code format.
 *
 *
 * 5-12-2001   Ruth Mikkelson Changed parameter from DataSetOperator to 
 *                            Operator in Constructor so it can be used with 
 *                            generic operators
 * 5-12-2001   Ruth Mikkelson added the Array parameterGUI
 * 5-17-2001   A null result is no longer shows as an error
 */
 
//To Do:  Make it work when jtui is null
//        Extract the Display Result and place in gen utilities
//        class.  Needed for CommandPane too.
//        -IsawGUI: Get the JMenu up somewhere in Isaw's Menu bar
//         Use Command.opMenu to plug in
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import Command.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import DataSetTools.util.*;
import javax.swing.text.*;

public class JParametersDialog implements Serializable,
                               IObservable
{
    private Operator op;
    private IObserver io;
    IsawGUI.Util util = new IsawGUI.Util();
    Document sessionLog;  
    Vector vparamGUI = new Vector();
    JDialog opDialog;
    Object Result = null;
    JLabel resultsLabel = new JLabel("    Result");
    ApplyButtonHandler APH;
    Vector ObjectParameters ;
                                 //allows acess to a dynamic list
                                 //of DataSet objects in the tree
                                 //w/o a reference to the actual tree.
    IDataSetListHandler ds_src;
    
    public JParametersDialog( Operator  op, 
                              IDataSetListHandler ds_src, 
                              Document  sessionLog, 
                              IObserver io )
     { this( op, ds_src,sessionLog, io , false);
       
       
      }
    public JParametersDialog( Operator  op, 
                              IDataSetListHandler ds_src, 
                              Document  sessionLog, 
                              IObserver io , boolean modal )
    {   
        this.op =op;
        this.ds_src = ds_src;
        this.sessionLog = sessionLog;    
        this.io = io;
        opDialog = new JDialog( new JFrame(), op.getTitle(), modal);
        //opDialog.addComponentListener( new MyComponentListener());       
        int Size = 0 ;
        int Size1 = 0;
        String SS ="Operation "+op.getCommand();
        int Width = 0;
        if(op instanceof DataSetOperator)
            SS = SS +" on "+((DataSetOperator)op).getDataSet();
	//#
        if(op.getCommand().equals("UNKNOWN")) SS ="";
        Box BB = new Box( BoxLayout.Y_AXIS);
        JLabel Header = new JLabel(SS ,SwingConstants.CENTER);
        Header.setForeground( Color.black);
        JPanel HeaderPanel = new JPanel();
        HeaderPanel.add( BB.createGlue());
        HeaderPanel.add( Header );
        HeaderPanel.add( BB.createGlue() );
        BB.add( HeaderPanel );
        Size1 = new JLabel( SS ).getPreferredSize().height;
      
        if( Size1 < 0)
	    Size += 10;
        else
           Size += Size1;     
        APH = new ApplyButtonHandler();
        if( io != null) 
              addIObserver(io);
        
        //Center the opdialog frame 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Width = screenSize.width;
        Dimension size = opDialog.getSize();
        screenSize.height = screenSize.height/2;
        screenSize.width = screenSize.width/2;
        size.height = size.height/2;
        size.width = size.width/2;
        int y = screenSize.height - size.height;
        int x = screenSize.width - size.width;
        //opDialog.setSize(570,450);
        opDialog.setLocation(x, y);

        int num_param = op.getNum_parameters();
       
	// opDialog.getContentPane().setLayout(new GridLayout(num_param+5,1));
        
        Parameter param;
        JParameterGUI paramGUI;
       
        //op.setDefaultParameters();
        ObjectParameters = new Vector();
        for (int i = 0; i<num_param; i++)
        {  
       //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%     
           Size1=-1;
           param = op.getParameter(i);            
          
            if(  param.getValue() instanceof String  &&
                               op instanceof IntervalSelectionOp  )
            {
              DataSet ds = ((DS_Attribute)op).getDataSet();
              AttributeList attrs = ds.getData_entry(0).getAttributeList();
              paramGUI = new JIntervalParameterGUI( param, attrs );
              
            }

           else if(param.getValue() == null)
             {paramGUI = new JObjectParameterGUI(param);
	      ObjectParameters.addElement( new Integer( i ));
             }
           else if (param.getValue() instanceof Float)
             paramGUI = new JFloatParameterGUI(param);
           else if(param.getValue() instanceof Integer)
             paramGUI = new JIntegerParameterGUI(param);
           else if(param.getValue() instanceof Boolean)
              paramGUI = new JBooleanParameterGUI(param);
           else if(param.getValue() instanceof String)
             paramGUI = new JStringParameterGUI(param);
           else if(param.getValue() instanceof IntListString)
             { //param.setValue( param.getValue().toString());
               paramGUI = new JStringParameterGUI(param);
             }
           else if(param.getValue() instanceof Vector)
             paramGUI = new JArrayParameterGUI(param);
           else if((param.getValue() instanceof AttributeNameString) &&
                   (op instanceof DataSetOperator))
           {
             DataSet ds =((DataSetOperator)op).getDataSet();
             AttributeList attr_list; 
                             // if the operator is a DataSet 
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
             System.out.println("Num attributes="+
                    attr_list.getNum_attributes());
             paramGUI = new JAttributeNameParameterGUI(param, attr_list);
           }


                //TODO: fix this so ds_src.getDataSets() is called every time
                //      an operation is done (e.g. the 'Apply' button is pressed)

           else if((param.getValue() instanceof DataSet)  )
                 paramGUI = new JDataSetParameterGUI(  param, 
                             ds_src.getDataSets()  );
                
           else if( param.getValue() instanceof DataDirectoryString )
           { 
            String DirPath = System.getProperty("DataDirectory");
            if( DirPath != null )
              DirPath = DataSetTools.util.StringUtil.
                                 fixSeparator(DirPath+"\\");
            else
              DirPath = "";
               param.setValue( new DataDirectoryString(DirPath) );

            paramGUI = new JStringParameterGUI( param ) ;
          }


       
        else if (param.getValue() instanceof IStringList )
         paramGUI = 
              new JIStringListParameterGUI( param ,
                                         (IStringList)(param.getValue()));
/*        {
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
*/

        else if( param.getValue() instanceof InstrumentNameString)
        {
          String XX = System.getProperty("DefaultInstrument");

          if( XX == null )
               XX = "";

          param.setValue(new InstrumentNameString( XX ));
          paramGUI= new JStringParameterGUI( param);
        }
     
        else 
        {
           System.out.println("Unsupported Parameter in JParamatersDialog");
                return ;
        }
        Size1 = paramGUI.getGUISegment().getPreferredSize().height;
        
        if( Size1 < 0)
	    Size += 10;
        else
           Size += Size1;     
                
        //Add other kinds of parameter types here.
            
	    //# opDialog.getContentPane().add(paramGUI.getGUISegment());
        BB.add(paramGUI.getGUISegment());
        vparamGUI.addElement(paramGUI);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
            
        }

        JPanel Filler = new JPanel();
        Filler.setPreferredSize( new Dimension(120,2000));
        BB.add( Filler ); 
        JPanel resultsPanel = new JPanel(new GridLayout( 1, 1 ) );
        resultsLabel.setForeground( Color.black);            
        resultsPanel.add( resultsLabel );
              
        BB.add(resultsPanel );
        Size1 = resultsLabel.getPreferredSize().height;
       
        if( Size1 < 0)
	    Size += 10;
        else
           Size += Size1;    
        JPanel buttonpanel = new JPanel( );
        buttonpanel.setLayout(new FlowLayout());
               
        JButton apply = new JButton("Apply");
        JButton exit = new JButton("Exit");
                  
        buttonpanel.add(apply);
        apply.addActionListener( APH );
                 
        buttonpanel.add(exit);
        exit.addActionListener(new ExitButtonHandler());
        Size1 = buttonpanel.getPreferredSize().height;
        
        if( Size1 < 0)
	    Size += 10;
        else
           Size += Size1;    
       
        BB.add(buttonpanel);
        opDialog.getContentPane().add( BB);
        //#
       
        Size += (num_param  + 4 )*2 + 38;
        
        opDialog.setSize((int)(.4* Width) , new Float(Size +.8).intValue());
        opDialog.validate();
                 
        opDialog.setVisible(true);
      }
       
  public void addIObserver(IObserver iobs) 
  {
    APH.addIObserver(iobs);
  }
               
  public void deleteIObserver(IObserver iobs) 
  {
    APH.deleteIObserver(iobs);
  }
               
  public void deleteIObservers() 
  {
    APH.deleteIObservers();
  }
  /** Returns the result of the last execution of the operator or
 *    null if the Apply button was not pressed
 *
 */
    public Object getLastResult()   
     {return Result;
     }  

  public void addWindowListener(WindowListener l) 
   { opDialog.addWindowListener( l );
   }
/*        
  public class MyComponentListener extends ComponentAdapter
  {
   public void componentHidden(ComponentEvent e)
      { System.out.println( "in Component Hidden ");       
      }
   public void componentResized(ComponentEvent e)
      {System.out.println("in Component resized");
      }
   public void componentMoved(ComponentEvent e)
      {System.out.println( "In Component moved");
      }
   public void componentShown(ComponentEvent e)
      { System.out.println( "In componentShown ");
      }
  }
*/
  public class ApplyButtonHandler implements ActionListener,
                                  IObservable
  {
    IObserverList OL;

    public ApplyButtonHandler()
    {
      OL = new IObserverList();
    }

    public void addIObserver(IObserver iobs) 
    {
      OL.addIObserver(iobs);
    }
               
    public void deleteIObserver(IObserver iobs) 
    {
      OL.deleteIObserver(iobs);
    }
               
    public void deleteIObservers() 
    {
      OL.deleteIObservers();
    }

    public void actionPerformed(ActionEvent ev) 
    {
             //  optionPane.setValue(btnString1);
             // System.out.println("buttonpressed" +ev);
      JParameterGUI pGUI;
      String s="";

      resultsLabel.setText("");

      if(op instanceof DataSetOperator)
      {
         s = ((DataSetOperator)op).getDataSet().toString();

         if (op.getNum_parameters() > 0)
                    s = s +", ";
      }

      for(int i = 0; i < op.getNum_parameters(); i++)
      {
        pGUI = (JParameterGUI)vparamGUI.elementAt( i );
        if(  pGUI.getParameter() != null  &&  
             pGUI.getParameter().getValue() != null  )
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
      Result = result;
      for( int i = 0; i < ObjectParameters.size(); i++ )
        { int k = ((Integer)ObjectParameters.elementAt(i)).intValue();
          String ParName = op.getParameter(k).getName();
          if( ParName == null)
              ParName = "Value?";
          op.setParameter( new Parameter( ParName , null ) , k );
        }
      opDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      if (result == null)
      {
         System.out.println("Result was null  :");
            
         resultsLabel.setText("Result was null  :");
         util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

              //resultsLabel.setBackground(new java.awt.Color(225, 225, 0));
              //  resultsLabel.setBackground(new java.awt.Color(100,10,10));
      }
     else if (result instanceof DataSet)
     {
             // DataSet[] dss = new DataSet[1];
             //   dss[0] = (DataSet)result;
                  //jtui.addDataSet((DataSet)result);
        
                  
        OL.notifyIObservers( this , (DataSet)result); 
                
        //System.out.println("The new Title is:" +((DataSet)result).getTitle());
        resultsLabel.setText("Operation completed");
        util.appendDoc(sessionLog,  
                ((DataSet)result).toString()+"="+op.getCommand()+"(" +s +")");
     }
     else if( result instanceof DataSet[] )
       { DataSet DSS[];
         DSS = (DataSet[])result; 
        
           for( int i = 0; i < DSS.length; i++)
              OL.notifyIObservers( this, DSS[i]);
        resultsLabel.setText("Operation completed");
        util.appendDoc(sessionLog,  
                "DS[]="+op.getCommand()+"(" +s +")");      
       }     
     else if (result instanceof Float)
     {
        Float value = (Float)result;
        resultsLabel.setText("Result:  " +value.floatValue() );
        util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");
     }
     else if (result instanceof String)
     {
        String value = (String)result;
        resultsLabel.setText("Result:  " +value );
        util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");

     }
     else if (result instanceof ErrorString)
     {
        ErrorString value = (ErrorString)result;
        resultsLabel.setText("Operation failed:"+value.toString() );
        util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");
     }
     else if( result instanceof int[])
     { 
        int X[] = (int[]) result;
        String SS = IntList.ToString( X );
        resultsLabel.setText(SS);
        util.appendDoc(sessionLog, op.getCommand()+"(" +s +")");
     }
     else if( result instanceof Vector )
     {
        resultsLabel.setText("Result ="+ 
                              execOneLine.Vect_to_String((Vector)result));
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
    public void actionPerformed(ActionEvent ev) 
    {
      opDialog.dispose();     
    } 
  }
}
