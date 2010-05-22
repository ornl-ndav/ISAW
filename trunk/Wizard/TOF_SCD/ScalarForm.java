package Wizard.TOF_SCD;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.Parameters.*;
import gov.anl.ipns.Util.Messaging.IObserver;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import javax.swing.JPanel;

import Command.ScriptUtil;
import DataSetTools.wizard.Form;
import DataSetTools.wizard.OperatorForm;
import DataSetTools.wizard.Wizard;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Controls.ScalarHandlePanel;
import MessageTools.Message;
import Operators.TOF_SCD.readOrient;


public class ScalarForm extends Form 
{

   private final int UB_PARAM =0;
   private final int SYM_CENT_PARAM =1;
   private final int TOLERANCE =2;
   private final int SORT_ON   = 3;
   private final int SEL_TRANS_INDX = 4;
   private final int RESULT_PARAM  =5;
   ScalarHandlePanel ScalarPanel;
   OutSideInputListener  outListener;
   
   public ScalarForm(String title)
   {

      this( title, false );
      
    
   }
   
   public ScalarForm( String title, boolean hasConstantParams)
   {
      super( title, hasConstantParams);
      outListener = new OutSideInputListener();
      ScalarPanel = new ScalarHandlePanel( new float[][]{{1f,0f,0f},{0f,1f,0f},{0f,0f,1f}});
      setDefaultParameters();
      ScalarPanel.addActionListener( new ChangeInputListener( this ));
   }

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   
  
   @Override
   public JPanel getPanel()
   {
      return ScalarPanel.getPanel( );

     
   }
   
   public void makeGUI()
   {
      outListener.update( getParameter(0) , ParameterGUI.VALUE_CHANGED  );
      super.makeGUI( );
   }
 
   @Override
   protected Object validateSelf()
   {

     
      return super.validateSelf( );
   }

 

   @Override
   public void setDefaultParameters()
   {

     super.clearParametersVector( );
     addParameter( new ArrayPG("Orientation Matrix", "[[1,0,0],[0,1,0],[0,0,1]]"));
     addParameter( new StringPG("Centerings&Symm","TTTTTTTTTTTTTTT"));
     addParameter( new FloatPG("Tolerance", .1f));
     addParameter( new StringPG("Sort Criteria","Symmetry" ));
     addParameter( new IntegerPG("Selected Transf Indx", 0));
     setResultParam( new ArrayPG("transformation","[[1,0,0],[0,1,0],[0,0,1]]"));
     
     setParamTypes( new int[]{UB_PARAM}, new int[]{SYM_CENT_PARAM,TOLERANCE,SORT_ON,SEL_TRANS_INDX},
           new int[]{RESULT_PARAM});
     
     ((ArrayPG)getParameter(0)).addIObserver(outListener);
     ((StringPG)getParameter(1)).addIObserver( outListener);
     ((FloatPG)getParameter(2)).addIObserver(outListener);
     ((StringPG)getParameter(3)).addIObserver( outListener);
     ((IntegerPG)getParameter(4)).addIObserver(outListener);
     ((ArrayPG)getParameter(5)).addIObserver( outListener);
   }

   public boolean setParameter( IParameter param, int indx )
   {
      ((ParameterGUI)getParameter(indx)).deleteIObserver( outListener );
      boolean res = super.setParameter(  param , indx );
      if( res)
         ((ParameterGUI)getParameter(indx)).addIObserver( outListener );
      return res;
   }
   
   //setParameter  override incase the parameter is changed
   @Override
   public Object getResult()
   {
      float[][] transf = ScalarPanel.getTransformation( );
      
      if( transf == null || transf.length !=3)
         return new ErrorString("No Transformation has been selected");
      
      
      ScriptUtil.display( "Result="+ gov.anl.ipns.Util.Sys.StringUtil.toString( transf ) );
      
      Vector V = (Vector)Command.ScriptUtil.ToVec( transf);
      
      getResultParam().setValue(  V );
      
      return transf;
   }
   
   
  

   /**
    * @param args
    */
   public static void main(String[] args)
   {

     Wizard test = new Wizard("Test");
     
     test.addForm( new OperatorForm( new readOrient(), new ArrayPG("Orientation Matrix",
                            "[[1,0,0],[0,1,0],[0,0,1]"),new int[0]));
     test.addForm( new ScalarForm("Scalar"));
     
     int[][] paramTable = { {1,0}};
     test.linkFormParameters( paramTable );
     test.wizardLoader( null );

   }
   
   class ChangeInputListener implements ActionListener
   {

      ScalarForm frm;
      Vector parameters;
      public ChangeInputListener( ScalarForm frm)
      {
         this.frm = frm;
         this.parameters = parameters;
         
         
      }
    
      @Override
      public void actionPerformed(ActionEvent e)
      {
         ScalarHandlePanel Panel = frm.ScalarPanel;
         
         float Delta = Panel.getDelta( );
         String Centerings =Panel.getSymmCenterings( );
         String SortOn = Panel.getSortOn( );
         int transfIndx = Panel.getSelectedTransfIndex( );
         parameters = frm.parameters;
         ((IParameter)parameters.get(TOLERANCE)).setValue( Delta);
         ((IParameter)parameters.get(SYM_CENT_PARAM)).setValue( Centerings);
         ((IParameter)parameters.get(SORT_ON)).setValue( SortOn);
         ((IParameter)parameters.get(SEL_TRANS_INDX)).setValue( transfIndx);
         
      }
      
   }
   
   class OutSideInputListener implements IObserver
   {

      @Override
      public void update(Object observedObj, Object reason)
      {

        if( reason.equals(  ParameterGUI.VALUE_CHANGED )&&
              observedObj instanceof ParameterGUI)
        {
          Vector V = (Vector)((ArrayPG)getParameter(0)).getValue( );
          if( V != null && V.size() == 3)             
          {
             float[][] Ormat = new float[3][];
             for( int i=0; i<3;i++)
             {
                Ormat[i] = NexIO.Util.ConvertDataTypes.floatArrayValue( V.elementAt(i) );
                
             }
             
            
             ScalarPanel.receive(  new Message( Commands.SET_ORIENTATION_MATRIX,
                   LinearAlgebra.getTranspose(  Ormat ), true ));
              
          }
         String R = getParameter( SYM_CENT_PARAM).getValue( ).toString( );
         ScalarPanel.setSymmCenterings(R );
         
         float tolerance = ((FloatPG)getParameter(TOLERANCE)).getfloatValue( );
         ScalarPanel.setDelta( tolerance );
         
         String srt = getParameter(SORT_ON).getValue( ).toString();
         ScalarPanel.setSortOn( srt );
         
         int indx =((IntegerPG)getParameter(SEL_TRANS_INDX)).getintValue( );
         ScalarPanel.setSelectedTranfIndex( indx );
        }
         
      }
      
   }


}
