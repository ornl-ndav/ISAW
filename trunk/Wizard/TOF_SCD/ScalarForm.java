/* 
 * File: ScalarForm.java
 *
 * Copyright (C) 2010, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author:$
 *  $Date:$            
 *  $Rev:$
 */
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
   boolean showApplyButton;
   
   /**
    * Constructor
    * @param title   Title
    */
   public ScalarForm(String title)
   {

      this( title, false );
      
    
   }
   
   /**
    * Constructor
    * 
    * @param title     Title
    * 
    * @param hasConstantParams   true if there are constant parameters
    */
   public ScalarForm( String title, boolean hasConstantParams, boolean showApplyButton)
   {
      super( title, hasConstantParams);
      outListener = new OutSideInputListener();
      ScalarPanel = new ScalarHandlePanel( new float[][]{{1f,0f,0f},{0f,1f,0f},{0f,0f,1f}}, false);
      setDefaultParameters();
      ScalarPanel.addActionListener( new ChangeInputListener( this ));
      this.showApplyButton = showApplyButton;
   }
   
   public ScalarForm(String title, boolean hasConstantParams)
   {
      this( title, hasConstantParams, true);
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
   
   /**
    *  Copies parameter values to under ScalarPanel
    */
   public void makeGUI()
   {
      outListener.update( getParameter(0) , ParameterGUI.VALUE_CHANGED  );
      super.makeGUI( );
   }
 
  

   
   /**
    * Set the parameters to save the state of the underlying ScalarPanel
    */
   @Override
   public void setDefaultParameters()
   {

     super.clearParametersVector( );
     addParameter( new ArrayPG("Orientation Matrix", "[[1,0,0],[0,1,0],[0,0,1]]"));
     addParameter( new StringPG("Centerings&Symm","TTTTTTTTTTTTTTT"));
     addParameter( new FloatPG("Tolerance", .1f));
     addParameter( new StringPG("Sort Criteria","Symmetry" ));
     addParameter( new IntegerPG("Selected Transf Indx", 0));
     setResultParam( new StringPG("transformation","[[1,0,0],[0,1,0],[0,0,1]]"));
     
     setParamTypes( new int[]{UB_PARAM}, new int[]{SYM_CENT_PARAM,TOLERANCE,SORT_ON,SEL_TRANS_INDX},
           new int[]{RESULT_PARAM});
     
     ((ArrayPG)getParameter(0)).addIObserver(outListener);
     ((StringPG)getParameter(1)).addIObserver( outListener);
     ((FloatPG)getParameter(2)).addIObserver(outListener);
     ((StringPG)getParameter(3)).addIObserver( outListener);
     ((IntegerPG)getParameter(4)).addIObserver(outListener);
     
   }

   
   /**
    * Sets the given parameter and add itself as an IObserver to the
    * new parameter
    * 
    * @param param  The new parameter
    * @param indx   The parameter index to be replaced
    */
   public boolean setParameter( IParameter param, int indx )
   {
      ((ParameterGUI)getParameter(indx)).deleteIObserver( outListener );
      boolean res = super.setParameter(  param , indx );
      if( res)
         ((ParameterGUI)getParameter(indx)).addIObserver( outListener );
      return res;
   }
   
   /**
    * Reads the selected transformation from the underlying ScalarPanel.
    * This also sets the result parameter( StringPG) and returns the vector
    * form of this result
    */
   @Override
   public Object getResult()
   {
      float[][] transf = ScalarPanel.getTransformation( );
      
      if( transf == null || transf.length !=3)
         return new ErrorString("No Transformation has been selected");
      
      String Result = gov.anl.ipns.Util.Sys.StringUtil.toString( transf ) ;
      ScriptUtil.display( "Result="+ Result);
      
      Vector V = (Vector)Command.ScriptUtil.ToVec( transf);
      
      getResultParam().setValue( Result );
      
      for( int i=0; i< this.getNum_parameters( ); i++)
         ((IParameterGUI)getParameter(i)).setValidFlag( true );
      getResultParam().setValidFlag( true );
      
      return V;
   }
   
   
  

   /**
    * Test program with a simple wizard
    * 
    * @param args     None are used
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
   
   /**
    * Listens for changes in the underlying ScalarPanel, changing the values
    * of the corresponding parameters that are used to save the state
    * 
    * @author ruth
    *
    */
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
   
   /**
    * Listens for changes to the parameters from outside, translating this into the
    * underlying ScalarPanel.
    * 
    * @author ruth
    *
    */
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
                   Commands.MakeSET_ORIENTATION_MATRIX_arg( 
                         LinearAlgebra.getTranspose(  Ormat ), null), true ));
              
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
