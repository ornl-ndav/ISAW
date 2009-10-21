/* 
 * File: FindOrientationMatrix.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
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
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */
package Operators.TOF_SCD;

import gov.anl.ipns.Parameters.*;
import gov.anl.ipns.Util.Messaging.IObserver;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.Sys.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

import DataSetTools.components.ParametersGUI.JParametersDialog;
import DataSetTools.components.ui.Peaks.*;
import DataSetTools.operator.Generic.GenericOperator;

import DataSetTools.util.SharedData;



/**
 * This class puts an operator wrapper around the new find initial orientation 
 * matrix system.  It also has a main method to invoke this operator so this
 * system can be run as an application.
 * 
 * @author Ruth
 *
 */
public class FindOrientationMatrix extends GenericOperator 
                                  implements WindowListener, IObserver, 
                                  ActionListener
                                   
{

   private static String[] XtalTypes        =
   {
       "P" , "A" , "B" , "C" ,
       "F" , "I" , "R"
   };
   
   Vector Peaks;
   View3D view;
   OrientMatrixControl orientMatCtrl;
   PeakFilterer pkfilter;
   SetPeaks  setPeaks;
   XtalLatticeControl Xtal;
   View3DItems ViewIn3D;
   Info inf;
   FinishJFrame QViewer;
   FinishJFrame InfoViewer;
   public FindOrientationMatrix(  )
   {
      super( "New Initial Orientation Matrix");
    
     
      setDefaultParameters();
      Peaks = null;
      view = null;

      orientMatCtrl= null;
      pkfilter= null;
      setPeaks= null;
      Xtal= null;
      ViewIn3D= null;
      inf = null;
      QViewer = InfoViewer = null;
    
   }
  
   
   public void setDefaultParameters()
   {
      
      this.clearParametersVector();
      addParameter( new LoadFilePG( "Peaks" , System.getProperty( "Data_Directory" ) ) );

      addParameter( new DataDirPG( "Output Path" , System
               .getProperty( "Data_Directory" ) ) );

      addParameter( new StringPG( "FileName for resultant matrix" , "" ) );

      addParameter( new ArrayPG( "Crystal Params" , "3,3,5,90,100,20" ) );

      addParameter( new ChoiceListPG( "Crystal Type" , XtalTypes ) );

      addParameter( new FloatPG( "Length Tol(Crystal Params)" , .01f ) );

      addParameter( new FloatPG( "Dot Prod Tol(Crystal Params)" , .03f ) );
      
      Vector V = new Vector(3);
      V.add(false); V.add(1); V.add(0);
      addParameter( new BooleanEnablePG( "Show Peaks" , V ) );//7

      addParameter( new StringPG( "Prefix on file" , "" ) );//8

      addParameter( new FloatPG( "Minimum d-spacing" , -1 ) );

      addParameter( new FloatPG( "Maximum d-spacing" , -1) );
      
       ((ParameterGUI)getParameter(0)).addIObserver( this );
      addParameter( new ButtonPG( "Show Q view"));    //11
      addParameter( new ButtonPG( OrientMatrixControl.ORIENT_MAT ));//12
      addParameter( new ButtonPG("View in 3D" ));//13

      addParameter( new ButtonPG( "Filter Out Peaks"));//14

      addParameter( new ButtonPG( SetPeaks.SELECT_PEAK_BUT ));//15	
      addParameter( new ButtonPG("Show Info"));
     
   }
   
   @Override
   public Object getResult()
   {
      if( orientMatCtrl == null || orientMatCtrl.getOrientationMatrix( -1 )==null)
         return new ErrorString("No Matrix set up");
      float[][] UB =  orientMatCtrl.getOrientationMatrix( -1 );
      String filename = getParameter(1).getValue().toString()+getParameter(2).getValue().toString();
      return DataSetTools.operator.Generic.TOF_SCD.Util.WriteMatrix(filename,UB );
   }
   
   
   
   
  
   /**
    * 
    * @param args
    * TODO: have argument the peaks file. If no arguments have it pop up a dialog box to get
    * the name of the peaks file. Then put the Wizard form in a JFrame( with menu items??) 
    */
   public static void main( String[] args )
   {
      DataSetTools.util.SharedData dat = new DataSetTools.util.SharedData();
      FindOrientationMatrix Fmat = new FindOrientationMatrix();
      JParametersDialog dial = ( new JParametersDialog( Fmat, null, null, null));
      

   }


   /* (non-Javadoc)
    * @see DataSetTools.operator.Operator#clone()
    */
   @Override
   public Object clone()
   {
      FindOrientationMatrix Mat = new FindOrientationMatrix();
      Mat.CopyParametersFrom( this );
     
      return Mat;
   }


   /* (non-Javadoc)
    * @return "FindOrientationMatrix", the command name for this operator for the 
    * ISAW Scripting language
    * @see DataSetTools.operator.Operator#getCommand()
    */
   @Override
   public String getCommand()
   {

      
      return "FindOrientationMatrix";
   }


   /* (non-Javadoc)
    * @see DataSetTools.operator.Operator#getDocumentation()
    */
   @Override
   public String getDocumentation()
   {

      StringBuffer buff = new StringBuffer();
      buff = buff.append("This  determines an orientation ");
      buff = buff.append("matrix. It is returned when the windows are closed\n To change/calculate a new ");
      buff = buff.append("orientation matrix press or see documentation ");
      buff = buff.append("on \"Orientation Matrix\" button\n ");
      buff = buff.append(" There are options to view information in the 3D ");
      buff = buff.append("Q viewer(\"View 3D Objects\" and choices under ");
      buff = buff.append("\"Orientation Matrix\" buttons ),Filter out peaks");
      buff = buff.append(" (\"Peak Filterer\" button), and Set Peaks for ") ;
      buff = buff.append("arguments for other operations(\"Select Peaks\" ");
      buff = buff.append("button)\n");
      buff = buff.append("@param PEAKS The name of the peaks file)"); 
      buff = buff.append("@param  OUT_FILE_PATH  The Path to the output"+
                               " directory"); 
      buff = buff.append("@param OUT_FILE he filename for the resultant " +
           "orientation matrix"); 
      buff = buff.append("@param XTAL_PARAMS The Crystal parameters a,b ,c"+
               ",alpha, beta and gamma"); 
      buff = buff.append("@param XTAL_TYPE The Centering type for this "+
                                                 "crystal"); 
      buff = buff.append("@param XTAL_PARAMS_ERR1 Tolerance in the lengths of"+
                     " crystal sides"); 
      buff = buff.append("@param XTAL_PARAMS_ERR2 Tolerance in the dot "+
               "products of two sides"); 
      buff = buff.append("@param SHOW_PEAKS If peaks images are shown and"+
               " they are saved, the images of the peaks will be"+
               " available"); 
      buff = buff.append("@param FILE_PREFIX The prefix for the peak"+
               " image files. These peak image files must be in the "+
                "default directory"); 
      buff = buff.append("@param DMIN   the minimum d-spacing for this"+
                               " crystal"); 
      buff = buff.append("@param DMAX  the maximum d-spacing for this"+
                             " crystal"); 
      buff.append( "@return Success or Failure when the window is closed");
      return buff.toString();
      
   }


   /* (non-Javadoc)
    * @see DataSetTools.operator.Generic.TOF_SCD.GenericTOF_SCD#getCategoryList()
    */
   @Override
   public String[] getCategoryList()
   {

      
      return new String[]{"Macros", "Instrument Type","TOF_NSCD","NEW_SNS"};
   }


   /* (non-Javadoc)
    * @see gov.anl.ipns.Util.Messaging.IObserver#update(java.lang.Object, java.lang.Object)
    */
   @Override
   public void update( Object observed_obj , Object reason )
   {
      if( reason != IParameterGUI.VALUE_CHANGED)
         return;
      if( observed_obj == getParameter( 0 ) )// new filename
      {
         try
         {
            Peaks = (Vector) ( new DataSetTools.operator.Generic.TOF_SCD.ReadPeaks(
                     getParameter( 0 ).getValue().toString() ) ).getResult();
            view = new View3D( Peaks );

            View3DControl vcontrol = new View3DControl( view , Peaks );

            inf = new Info( vcontrol );

            if( ( (BooleanEnablePG) getParameter( 7 ) ).getbooleanValue() )
            {
               PeakImageInfoHandler pkImage = new PeakImageInfoHandler( null ,
                        getParameter( 8 ).getValue().toString() );
               inf.addInfoHandler( "Peak Image" , pkImage );
            }


            orientMatCtrl = new OrientMatrixControl( inf , view , Peaks ,
                     vcontrol );
            ;

            setPeaks = new SetPeaks( view , Peaks );

            ViewIn3D = new View3DItems( view , Peaks.size() , setPeaks );

            pkfilter = new PeakFilterer( Peaks );
            orientMatCtrl.setPeakFilterer( pkfilter );


            orientMatCtrl.setPeakSelector( setPeaks );

            Xtal = new XtalLatticeControl( setPeaks ,
                     inf );
            orientMatCtrl.setCrystalLatticeHandler( Xtal , true );
            for( int i = 3 ; i < 11 ; i++ )
            {
               ( (ParameterGUI) getParameter( i ) ).addIObserver( this );
               update1( (ParameterGUI) observed_obj );
            }

            ((ButtonPG)getParameter(12)).addActionListener( orientMatCtrl.getActionListener());
            ((ButtonPG)getParameter(13)).addActionListener( ViewIn3D.getListener());
            ((ButtonPG)getParameter(14)).addActionListener( pkfilter.getActionListener());
            ((ButtonPG)getParameter(15)).addActionListener(setPeaks.getActionListener());
            
            ((ButtonPG)getParameter(16)).addActionListener(this);

            ((ButtonPG)getParameter(11)).addActionListener(this);
         
            return;


         }
         catch( Exception ss )
         {
            SharedData.addmsg( "Could not read peaks from "
                     + getParameter( 0 ).getValue() );
         }
      }

      if( ! ( observed_obj instanceof ParameterGUI ) )
         return;

      update1( (ParameterGUI) observed_obj );

   }


   private void update1( ParameterGUI param )
   {

      if( param == null )
         return;
      String prompt = param.getName();
     
      if( prompt.equals( "Crystal Params" ) )
      {
         if( !(param instanceof ArrayPG))
            return;
         Vector V = (Vector)(param.getValue());
         float[] X = new float[6];
         if(V == null || V.size() < 6)
         {

            Xtal.setXtalParams(null );
            return;
         }
         try
         {
         for( int i=0; i< 6; i++)
            X[i] = ((Number)V.elementAt( i )).floatValue();
         }catch( Exception s)
         {
            X = null;
         }
         
         Xtal.setXtalParams(X );
         return;
      }else
      if( prompt.equals( "Crystal Type" ) )
      {
         Xtal.setCentering( param.getValue().toString().charAt( 0 ) );
      }else
      if( prompt.equals( "Length Tol(Crystal Params)" ) )
      {
          Xtal.setDeltas( ((FloatPG)param).getfloatValue() , -1 );
      }else
      if( prompt.equals( "Dot Prod Tol(Crystal Params)" ) )
      {

         Xtal.setDeltas( -1,((FloatPG)param).getfloatValue()  );
      }else
      if( prompt.equals( "Show Peaks" ) )
      {

      }else
      if( prompt.equals( "Prefix on file" ) )
      {

      }else
      if( prompt.equals( "Minimum d-spacing" ) )
      {
         orientMatCtrl.setDMin_Max( ((FloatPG)param).getfloatValue() , -1 );
      }else
      if( prompt.equals( "Maximum d-spacing" ) )
      {
         orientMatCtrl.setDMin_Max( -1, ((FloatPG)param).getfloatValue()  );
      }
   }
   
   
   public void actionPerformed( ActionEvent evt)//To Show QViewer and info views
   {
      if( evt.getActionCommand().equals(  "Show Q view"))
      {
       if( QViewer == null  )  
       {
          QViewer = new FinishJFrame("Peaks in Q");
          QViewer.getContentPane().setLayout(  new GridLayout(1,1) );
          QViewer.getContentPane().add(  view );
          QViewer.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
          QViewer.addWindowListener(  this );
          QViewer.setSize(  500,500 );
          WindowShower.show(QViewer);
       }
      }
   
      else if( evt.getActionCommand().equals( "Show Info"))
      {
         if( InfoViewer != null)
            return;
         InfoViewer = new FinishJFrame("Information");
         InfoViewer.getContentPane().setLayout(  new GridLayout(1,1) );
         InfoViewer.getContentPane().add(  inf);
         InfoViewer.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
         InfoViewer.addWindowListener(  this );
         InfoViewer.setSize(  800,800 );
         WindowShower.show(InfoViewer);
      }
             
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
    */
   @Override
   public void windowActivated( WindowEvent e )
   {

      // TODO Auto-generated method stub
      
   }


   /* (non-Javadoc)
    * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
    */
   @Override
   public void windowClosed( WindowEvent e )
   {
     
     if( e.getSource() == QViewer)
        QViewer = null;
     if( e.getSource() == InfoViewer)
        InfoViewer = null;
      
      
   }


   /* (non-Javadoc)
    * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
    */
   @Override
   public void windowClosing( WindowEvent e )
   {

      // TODO Auto-generated method stub
      
   }


   /* (non-Javadoc)
    * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
    */
   @Override
   public void windowDeactivated( WindowEvent e )
   {

      // TODO Auto-generated method stub
      
   }


   /* (non-Javadoc)
    * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
    */
   @Override
   public void windowDeiconified( WindowEvent e )
   {

      // TODO Auto-generated method stub
      
   }


   /* (non-Javadoc)
    * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
    */
   @Override
   public void windowIconified( WindowEvent e )
   {

      // TODO Auto-generated method stub
      
   }


   /* (non-Javadoc)
    * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
    */
   @Override
   public void windowOpened( WindowEvent e )
   {

      // TODO Auto-generated method stub
      
   }
  
}
