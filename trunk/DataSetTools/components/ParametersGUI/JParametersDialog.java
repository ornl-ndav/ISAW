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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.40  2003/06/02 22:33:48  rmikk
 *  -Added IObserver to operator right before the getResult
 *   method is executed. Removed it right afterward.
 *  -Displayed the result of an operation using the
 *   NexIO.NxNodeUtil.Showw method which unravels Vectors
 *   and arrays.
 *
 *  Revision 1.39  2003/05/22 21:41:08  pfpeterson
 *  System.getProperty(String) changed to SharedData.getProperty(String)
 *  to insure that the properties file is loaded before call.
 *
 *  Revision 1.38  2003/04/11 21:58:48  pfpeterson
 *  Now deals with RuntimeException being thrown by Operator.getResult()
 *  and implemented a "Halt" button.
 *
 *  Revision 1.37  2003/03/06 22:56:20  pfpeterson
 *  No longer provides methods for receiving StatusPane. Access to
 *  StatusPane is done directly with SharedData.
 *
 *  Revision 1.36  2003/02/26 16:06:37  pfpeterson
 *  First pass at multi-threading the apply button.
 *
 *  Revision 1.35  2003/02/19 16:49:20  pfpeterson
 *  Now leaves the default value alone, if exists, in DataDirectoryString,
 *  LoadFileString, SaveFileString, and InstrumentNameString.
 *
 *  Revision 1.34  2003/01/29 17:08:51  pfpeterson
 *  Made the help window that is brought up not editable.
 *
 *  Revision 1.33  2002/12/09 16:31:59  pfpeterson
 *  Changed size of help window.
 *
 *  Revision 1.32  2002/12/08 22:12:38  dennis
 *  Added help button for new help system. (Ruth)
 *
 *  Revision 1.31  2002/11/27 23:12:35  pfpeterson
 *  standardized header
 *
 *  Revision 1.30  2002/10/14 16:02:29  pfpeterson
 *  Compares the parameter against the IParameterGUI interface rather
 *  than the ParameterGUI abstract class. Also fixed a bug where ArrayPG
 *  (that were not DataSetPG) did not produce a GUI.
 *
 *  Revision 1.29  2002/09/30 18:41:11  rmikk
 *  Fixed a null pointer exception error
 *
 *  Revision 1.28  2002/09/25 13:59:17  rmikk
 *  Added a call to init methods for DataSetPG's and non
 *    ArrayPG's so that the GUI is initialized
 *
 *  Revision 1.27  2002/09/23 14:11:35  rmikk
 *  Added support for ExitClass produced by the ExitDialog
 *    operator and initial support( unsuccessful) for the new
 *    Parameters.
 *
 *  Revision 1.26  2002/09/19 15:59:18  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.25  2002/07/15 19:30:32  dennis
 *  Commented out code that sets the cursor... this is an attempt to
 *  prevent crash in native code outside of VM when cursor is set on
 *  Linux.
 *
 *  Revision 1.24  2002/04/03 19:53:18  pfpeterson
 *  Added SampleDataSet and MonitorDataSet.
 *
 *  Revision 1.23  2002/04/02 22:50:53  pfpeterson
 *  Provides for LoadFileString and SaveFileString.
 *
 *  Revision 1.22  2002/02/22 20:34:02  pfpeterson
 *  Operator Reorganization.
 *
 *  Revision 1.21  2002/01/11 22:04:58  rmikk
 *  -Change DataDirectory to Data_Directory
 *  -Added a new ParameterGUI to deal with DataDirectories
 *   by popping up a file dialog box
 *
 *  Revision 1.20  2002/01/10 15:35:21  rmikk
 *  Added a Constructor to include a StatusPane.
 *  Added this StatusPane to operators that support the
 *    IusesStatusPane and java.beans.Customizer interface
 *     (i.e. had addPropertyChangeListener)
 *
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
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.Generic.Batch.*;
import Command.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import DataSetTools.util.*;
import javax.swing.text.*;
import java.lang.reflect.Array;
import javax.swing.text.html.*;
import ExtTools.SwingWorker;

public class JParametersDialog implements Serializable,
                               IObservable
{
    private Operator op;
    private IObserver io;
    IsawGUI.Util util = new IsawGUI.Util();
    Document sessionLog;  
    Vector vparamGUI = new Vector();
    JDialog opDialog;
    int Width = 0;
    Object Result = null;
    JLabel resultsLabel = new JLabel("    Result");
    ApplyButtonHandler APH;
    Vector ObjectParameters ;
                                 //allows acess to a dynamic list
                                 //of DataSet objects in the tree
                                 //w/o a reference to the actual tree.
    IDataSetListHandler ds_src;

    JButton apply = null;
    JButton exit = null;

  private static int screenwidth=0; // for help dialog
  private static int screenheight=0;
    
  private static final String APPLY="Apply";
  private static final String HALT="Halt";

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
        Width = 0;
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
        
        IParameter iparam;
        Parameter param;
        JParameterGUI paramGUI;
       
        //op.setDefaultParameters();
        ObjectParameters = new Vector();
        for (int i = 0; i<num_param; i++)
        {  
       //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%     
           Size1=-1;
           iparam = op.getParameter(i);
           if( iparam instanceof IParameterGUI)
             {if( iparam instanceof DataSetPG)
                ((DataSetPG)iparam).init((Object[])(ds_src.getDataSets()));
              else
                ((IParameterGUI)iparam).init(null);

              JComponent pp= ((IParameterGUI)iparam).getGUIPanel();
              if( pp == null)
                {System.out.println("GUIPanel null" + iparam.getClass());
                 return;
                }
            
              
              BB.add(pp);
              vparamGUI.addElement( null );

              Size1 = pp.getPreferredSize().height;
        
              if( Size1 < 0)
	        Size += 10;
              else
                Size += Size1;     
             }            
           else if(iparam instanceof Parameter){
              param=(Parameter)iparam;
          
            
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
                 //System.out.println("Num attributes="+
                 //      attr_list.getNum_attributes());
                 paramGUI = new JAttributeNameParameterGUI(param, attr_list);
                }


                //TODO: fix this so ds_src.getDataSets() is called every time
                //      an operation is done (e.g. the 'Apply' button is pressed)

              else if((param.getValue() instanceof SampleDataSet)  ){
                 DataSet ds[]=ds_src.getDataSets();
                 int num_sample=0;
                 int num_ds=Array.getLength(ds);

                 for( int count=num_ds-1 ; count>=0 ; count-- ){
                    String type = (String)
                       ds[count].getAttributeValue(Attribute.DS_TYPE);
                   /* System.out.println("DataSet: "+ds[count].toString()
                      +" "+type); */
                    if(type.equals(Attribute.SAMPLE_DATA)){
                       num_sample++;
                       }
                   }

                 DataSet just_sample[]=new DataSet[num_sample];
                 int countt=0;
                 for( int count=num_ds-1 ; count>=0 ; count-- ){
                    String type = (String)
                       ds[count].getAttributeValue(Attribute.DS_TYPE);
                    if(type.equals(Attribute.SAMPLE_DATA)){
                       just_sample[countt]=ds[count];
                       countt++;
                       }
                   }

                 paramGUI = new JDataSetParameterGUI(  param,  just_sample );
                }
              else if((param.getValue() instanceof MonitorDataSet)  ){
                 DataSet ds[]=ds_src.getDataSets();
                 int num_mon=0;
                 int num_ds=Array.getLength(ds);

                 for( int count=num_ds-1 ; count>=0 ; count-- ){
                   String type = (String)
                       ds[count].getAttributeValue(Attribute.DS_TYPE);
                   if(type.equals(Attribute.MONITOR_DATA)){
                       num_mon++;
                      }
                   }

                 DataSet just_mon[]=new DataSet[num_mon];
                 int countt=0;
                 for( int count=num_ds-1 ; count>=0 ; count-- ){
                   String type = (String)
                       ds[count].getAttributeValue(Attribute.DS_TYPE);
                   if(type.equals(Attribute.MONITOR_DATA)){
                       just_mon[countt]=ds[count];
                       countt++;
                      }
                   }

                 paramGUI = new JDataSetParameterGUI(  param,  just_mon );
                }
              else if((param.getValue() instanceof DataSet)  ){
                 paramGUI = new JDataSetParameterGUI(  param, 
                                                     ds_src.getDataSets()  );
                 }
              else if( param.getValue() instanceof DataDirectoryString )
                { 
                  String DirPath=param.getValue().toString();
                  if(DirPath==null || DirPath.length()<=0){
                    DirPath = SharedData.getProperty("Data_Directory");
                    if( DirPath != null )
                      DirPath = StringUtil.setFileSeparator(DirPath+"\\");
                    else
                      DirPath = "";

                    param.setValue( new DataDirectoryString(DirPath) );
                  }
                paramGUI = new JOneFileChooserParameterGUI( param ) ;
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
                 String XX = param.getValue().toString();
                 if(XX==null || XX.length()<=0)
                   SharedData.getProperty("DefaultInstrument");

                 if( XX == null )
                   XX = "";

                 param.setValue(new InstrumentNameString( XX ));
                 paramGUI= new JStringParameterGUI( param);
                }
              else if( param.getValue() instanceof LoadFileString){
                String FileName=param.getValue().toString();
                if(FileName==null || FileName.length()<=0)
                  FileName=SharedData.getProperty("Data_Directory")+"\\";
                if(FileName!=null && FileName.length()>0)
                  FileName=StringUtil.setFileSeparator(FileName);
                else
                  FileName="";

                param.setValue( new LoadFileString(FileName) );
                paramGUI=new JLoadFileParameterGUI(param);
                }
              else if( param.getValue() instanceof SaveFileString){
                 String FileName=param.getValue().toString();
                 if(FileName==null || FileName.length()<=0)
                   FileName=SharedData.getProperty("Data_Directory")+"\\";
                 if(FileName!=null && FileName.length()>0)
                   FileName=StringUtil.setFileSeparator(FileName);
                 else
                   FileName="";
                 
                 param.setValue( new SaveFileString(FileName) );
                 paramGUI=new JSaveFileParameterGUI(param);
                }
     
              else 
                {
                  SharedData.addmsg("Unsupported Parameter in "
                                    +"JParamatersDialog");
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
          }//for i < nparameters
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
               
        apply = new JButton(APPLY);
        exit = new JButton("Exit");
        JButton help  = new JButton( "Help" );
          
        buttonpanel.add(apply);
        apply.addActionListener( APH );
                 
        buttonpanel.add(exit);
        exit.addActionListener(new ExitButtonHandler());

        buttonpanel.add( help );
        help.addActionListener( new HelpButtonListener() );

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
      {       
      }
   public void componentResized(ComponentEvent e)
      {
      }
   public void componentMoved(ComponentEvent e)
      {
      }
   public void componentShown(ComponentEvent e)
      { 
  }
*/
  public class ApplyButtonHandler implements ActionListener,
                                  IObservable
  {
    IObserverList OL;
    SwingWorker worker=null;

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

      if(apply.getText().equals(HALT)){
        worker.interrupt();
        apply.setText(APPLY);
        exit.setEnabled(true);
        resultsLabel.setText("HALTED OPERATION: "+op.getCommand());
        return;
      }
      apply.setText(HALT);
      exit.setEnabled(false);
      resultsLabel.setText("working ...");

      if(op instanceof DataSetOperator)
      {
         s = ((DataSetOperator)op).getDataSet().toString();

         if (op.getNum_parameters() > 0)
                    s = s +", ";
      }

      for(int i = 0; i < op.getNum_parameters(); i++)
      {
        pGUI = (JParameterGUI)vparamGUI.elementAt( i );
        if( pGUI != null)
        if(  pGUI.getParameter() != null  &&  
             pGUI.getParameter().getValue() != null  )
        {                  
          op.setParameter( pGUI.getParameter(), i) ;
          s = s + pGUI.getParameter().getValue();
          if (i < op.getNum_parameters() - 1)
            s = s + ", ";
        }
      }

      if( op instanceof IusesStatusPane)
        ((IusesStatusPane)op).addStatusPane( SharedData.getStatusPane());
      if(op instanceof java.beans.Customizer)
        ((java.beans.Customizer)op)
                       .addPropertyChangeListener( SharedData.getStatusPane());
      
      // create a subclass of SwingWorker to call getResult()
      worker = new SwingWorker() {
          public Object construct(){
            try{
              if( op instanceof IObservable)
                {((IObservable)op).addIObserver(io);
                }
              Result=op.getResult();
              if( op instanceof IObservable)
                ((IObservable)op).deleteIObserver(io);             
              return Result;
            }catch(RuntimeException e){
              Result=e;
              return null;
            }
          }
          public void finished(){
            apply.setText(APPLY);
            exit.setEnabled(true);
            processResult();
          }
        };
      worker.start();
    }

    /**
     * Method for dealing with the result of an operator. This checks
     * the type of the result, notifies the appropriate listeners, and
     * sets the resultLabel in the dialog.
     */
    void processResult(){
      Object result = Result;
      String s="";

      if(result instanceof RuntimeException){
        ((RuntimeException)result).printStackTrace();
        String type=result.getClass().getName();
        int index=type.lastIndexOf(".");
        if(index<0) index=-1;
        resultsLabel.setText("FAILED: Encountered "+type.substring(index+1));
        return;
      }

      if( op instanceof IusesStatusPane)
        ((IusesStatusPane)op).addStatusPane( null);
      if(op instanceof java.beans.Customizer)
        ((java.beans.Customizer)op)
                    .removePropertyChangeListener( SharedData.getStatusPane());
      for( int i = 0; i < ObjectParameters.size(); i++ )
        { int k = ((Integer)ObjectParameters.elementAt(i)).intValue();
          String ParName = op.getParameter(k).getName();
          if( ParName == null)
              ParName = "Value?";
          op.setParameter( new Parameter( ParName , null ) , k );
        }
      if( result instanceof ExitClass)
      { opDialog.dispose();  
      }
      else if (result == null)
      {
         
            
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
        resultsLabel.setText("Result ="+ 
             (new NexIO.NxNodeUtils()).Showw( result));
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

  public class HelpButtonListener implements ActionListener{

    public void actionPerformed(ActionEvent ev){
      if(screenwidth==0 && screenheight==0){
        Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
        screenheight=screensize.height;
        screenwidth=(int)(screenheight*4/3);
      }
      
      JFrame jf = new JFrame( "operator "+op.getCommand());
      JEditorPane jedPane = new JEditorPane();
      jedPane.setEditable(false);
      jedPane.setEditorKit( new HTMLEditorKit() );
      jedPane.setText(SharedData.HTMLPageMaker.createHTML(op));
      jf.getContentPane().add( new JScrollPane(jedPane) );
      jf.setSize( (int)(screenwidth/2), (int)(3*screenheight/4) );
      jf.show();
    } 
  }
}
