/* 
 * File:rientMat.java
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

package Wizard.TOF_SCD;


// import gov.anl.ipns.Parameters.IParameterGUI;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import java.util.*;
import gov.anl.ipns.Parameters.*;
import gov.anl.ipns.Util.Messaging.IObserver;
import gov.anl.ipns.Util.Sys.WindowShower;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.operator.Generic.TOF_SCD.Util;
import DataSetTools.wizard.Form;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;

import DataSetTools.components.ui.Peaks.*;

/**
 * Specialized Form that connects with the utilitis in
 * DataSetTools.components.ui.Peaks
 * 
 * @author Ruth
 * 
 */
public class OrientMat extends Form implements ActionListener , IObserver ,
                            AncestorListener
{



   /**
    * Parameter 0 for this form. The Vector of Peaks (Invisible)
    */
   public static int       PEAKS            = 0;

   /**
    * Parameter 1 for this form. The Path to the output directory(Invisible)
    */
   public static int       OUT_FILE_PATH    = 1;

   /**
    * Parameter 2 for this form. The filename for the resultant orientation
    * matrix
    */
   public static int       OUT_FILE         = 2;


   /**
    * Parameter 3 for this form. The Crystal parameters a,b ,c,alpha, beta and
    * gamma
    */
   public static int       XTAL_PARAMS      = 3;


   /**
    * Parameter 4 for this form. The Centering type for this crystal
    */
   public static int       XTAL_TYPE        = 4;


   /**
    * Parameter 5 for this form. Tolerance in the lengths of crystal sides
    */
   public static int       XTAL_PARAMS_ERR1 = 5;


   /**
    * Parameter 6 for this form. Tolerance in the dot products of two sides
    */
   public static int       XTAL_PARAMS_ERR2 = 6;


   
   /**
    * Parameter 7 for this form. The input matrix filename if there is one
    * (Invisible).Saved
    */
   public static int       IN_FILE          = 7;


   /**
    * Parameter 8 for this form. The list of omiited peak sequence numbers
    * (Invisible). Saved
    */
   public static int       OMITTED          = 8;


   /**
    * Parameter 9 for this form. The list of selected sequence numbers
    * (Invisible) Saved. Used as arguments in the system.
    */
   public static int       SELECTED_SEQ     = 9;


   /**
    * Parameter 10 for this form. The specified hkl values for the selected
    * peaks.(Invisible) Saved.
    */

   public static int       SELECT_HKL       = 10;


   /**
    * Parameter 11 for this form. (Invisible). If peaks images are shown and
    * they are saved, the images of the peaks will be available.
    */
   public static int       SHOW_PEAKS       = 11;


   /**
    * Parameter 12 for this form. (Invisible). The prefix for the peak image
    * files. These peak image files must be in the default directory
    */
   public static int       FILE_PREFIX      = 12;


   /**
    * Parameter 13 for this form(Visible). This gives the minimum d-spacing
    * for this crystal
    */
   
   public static int       DMIN            =13;
   

   /**
    * Parameter 14 for this form(Visible). This gives the maximum d-spacing
    * for this crystal
    */
   
   public static int       DMAX            =14;
   
   
   /**
    * Parameter 15 or the result parameter for this form. This has the resultant
    * orientation matrix
    */
   public static int       ORIENT_MAT       = 15;


   private int[]           ConstParams      =
                                            {
            PEAKS , OUT_FILE_PATH , SHOW_PEAKS , FILE_PREFIX
                                            };

   private int[]           VarParams        =
                                            {
            OUT_FILE , XTAL_PARAMS , XTAL_TYPE , XTAL_PARAMS_ERR1 ,
            XTAL_PARAMS_ERR2 , IN_FILE , OMITTED , SELECTED_SEQ , SELECT_HKL,
            DMIN, DMAX
                                            };

   private int[]           ResParm          =
                                            {
                                               ORIENT_MAT
                                            };


   // Will add following buttons
   JButton                 ShowQViewer;

   // Buttons/Systems from the DataSetTools.components.ui.Peaks system
   OrientMatrixControl     Orient;

   SetPeaks                setPeaks;

   XtalLatticeControl      XtalLat;

   PeakFilterer            PeakFilter;

   View3DControl           V3DControl;

   View3D                  V3d;

   View3DItems             V3DItems;

   Info                    OutInfo;

   PeakImageInfoHandler    PeakImg;


   float[][]               orientMat;


   private static String[] XtalTypes        =
                                            {
                                    "P" , "A" , "B" , "C" ,
                                    "F" , "I" , "R"
                                            };

   JFrame                  QViewFrame;
   
   boolean                 NewOrientMatrixSet;  
   
   boolean                 DisableGraphicsOutput;

   JPanel_UnOptiimized jp;
   TabPane_UnOptimized tab;
   JPanel ControlPanel;
   JPanel_UnOptiimized Opns  ;
   /**
    * Constructor
    */
   public OrientMat()
   {

      super( "Orientation Matrix" );
      orientMat = null;
      QViewFrame = null;
      setDefaultParameters();
      NewOrientMatrixSet = false;
      DisableGraphicsOutput= false;

   }


   /*
    * (non-Javadoc)
    * 
    * @see DataSetTools.operator.Operator#getDocumentation()
    */
   @Override
   public String getDocumentation()
   {
       StringBuffer buff = new StringBuffer();
       buff = buff.append("This form is used to determine an orientation ");
       buff = buff.append("matrix\n To change/calculate a new ");
       buff = buff.append("orientation matrix press or see documentation ");
       buff = buff.append("on \"Orientation Matrix\" button\n ");
       buff = buff.append(" There are options to view information in the 3D ");
       buff = buff.append("Q viewer(\"View 3D Objects\" and choices under ");
       buff = buff.append("\"Orientation Matrix\" buttons ),Filter out peaks");
       buff = buff.append(" (\"Peak Filterer\" button), and Set Peaks for ") ;
       buff = buff.append("arguments for other operations(\"Select Peaks\" ");
       buff = buff.append("button)\n");
       buff = buff.append("@param PEAKS The Vector of Peaks (Invisible)"); 
       buff = buff.append("@param  OUT_FILE_PATH  The Path to the output"+
                                " directory(Invisible)"); 
       buff = buff.append("@param OUT_FILE he filename for the resultant " +
       		"orientation matrix(Visible)"); 
       buff = buff.append("@param XTAL_PARAMS The Crystal parameters a,b ,c"+
                ",alpha, beta and gamma(Visible)"); 
       buff = buff.append("@param XTAL_TYPE The Centering type for this "+
                                                  "crystal"); 
       buff = buff.append("@param XTAL_PARAMS_ERR1 Tolerance in the lengths of"+
                      " crystal sides"); 
       buff = buff.append("@param XTAL_PARAMS_ERR2 Tolerance in the dot "+
                "products of two sides"); 
       buff = buff.append("@param IN_FILE The input matrix filename if there"+
                    " is one (Invisible)"); 
       buff = buff.append("@param OMITTED he list of omitted peak sequence"+
                    " numbers (Invisible)"); 
       buff = buff.append("@param SELECTED_SEQ The list of selected sequence"+
                             " numbers (Invisible)"); 
       buff = buff.append("@param SELECT_HKL e specified hkl values for the"+
                    " selected peaks.(Invisible)"); 
       buff = buff.append("@param SHOW_PEAKS If peaks images are shown and"+
                " they are saved, the images of the peaks will be"+
                " available(Invisible"); 
       buff = buff.append("@param FILE_PREFIX The prefix for the peak"+
                " image files. These peak image files must be in the "+
                 "default directory"); 
       buff = buff.append("@param DMIN   the minimum d-spacing for this"+
                                " crystal"); 
       buff = buff.append("@param DMAX  the maximum d-spacing for this"+
                              " crystal"); 
       buff = buff.append("@param ORIENT_MAT the resultant orientation"+
                " matrix"); 
       
       String path = System.getProperty( "Help_Directory" );
       if( path != null)
       {
          path = path.replace('\\','/');
          if( !path.endsWith( "/" ))
             path +="/";
          path =path+ "wizard/";
      
          buff = buff.append("\n\n <UL><B>Buttons</B>");
          try
         {
            buff = buff.append( "<LI> <a href=\"" + URLL( path + "QView.html" )
                     + "\"> Show QView- Shows the peaks in Q space</a>" );
            buff = buff.append( "<LI>  <a href=\""
                     + URLL( path + "OrientMat.html" )
                     + "\">Orientation Matrix</a>" );
            buff = buff.append( "<LI><a href=\"" + URLL( path + "Vin3D.html" )
                     + "\"> View 3D Objects</a>" );
            buff = buff.append( "<LI> <a href=\""
                     + URLL( path + "PFilter.html" ) + "\">Peak Filterer</a>" );
            buff = buff.append( "<LI><a href=\"" + URLL( path + "PPeaks.html" )
                     + "\"> Select Peaks</a>" );
         }
         catch( Exception s )
         {

         }
          buff=buff.append( "</UL>" );
       }
       
      return buff.toString();
   }

   private String URLL( String filename) throws MalformedURLException
   {
     
      return (new File(filename)).toURL().toString();
     
   }
   /*
    * (non-Javadoc)
    * 
    * @see DataSetTools.operator.Operator#setDefaultParameters() TODO select
    *      peaks save info when given qx,qy,qz
    */
   @Override
   public void setDefaultParameters()
   {

      this.clearParametersVector();
      addParameter( new PlaceHolderPG( "Peaks" , new Vector() ) );

      addParameter( new DataDirPG( "Output Path" , System
               .getProperty( "Data_Directory" ) ) );

      addParameter( new StringPG( "FileName for resultant matrix" , "" ) );

      addParameter( new ArrayPG( "Crystal Params" , "3,3,5,90,100,20" ) );

      addParameter( new ChoiceListPG( "Crystal Type" , XtalTypes ) );

      addParameter( new FloatPG( "Length Tol(Crystal Params)" , .01f ) );

      addParameter( new FloatPG( "Dot Prod Tol(Crystal Params)" , .03f ) );

      addParameter( new StringPG( "Input Filename" , "" ) );

      addParameter( new ArrayPG( "Omitted Seq Nums" , null ) );

      addParameter( new ArrayPG( "Selected Seq Nums" , null ) );

      addParameter( new ArrayPG( "Selected hkl vals" , null ) );

      addParameter( new BooleanPG( "Show Peaks" , false ) );

      addParameter( new StringPG( "Prefix on file" , "" ) );

      addParameter( new FloatPG( "Minimum d-spacing" , -1 ) );

      addParameter( new FloatPG( "Maximum d-spacing" , -1) );

      setResultParam( new ArrayPG( "Orientation Matrix" , null ) );

      this.setParamTypes( ConstParams , VarParams , ResParm );
   }

   /**
    * For Non Wizard use of the panel
    * @return  A JPanel displaying the elements of this wizard form
    */
   public JPanel MakePanel()
   {
      makeGUI();
      return getPanel();
   }

   /**
    * Releases all stored structures. Do not kill this Form until after 
    */
   public void kill()
   {
      if( Orient != null)
         Orient.kill();
      Orient = null;
      if( setPeaks != null)
         setPeaks.kill();
      setPeaks= null;
      if( XtalLat != null)
         XtalLat.kill();
      XtalLat = null;
      if( PeakFilter != null)
         PeakFilter.kill();
      PeakFilter = null;
      if( V3DControl != null)
         V3DControl.kill();
      V3DControl = null;
      if( OutInfo != null)
         OutInfo.kill();
      OutInfo = null;
      if( PeakImg != null)
         PeakImg.kill();
      PeakImg = null;
     
      orientMat = null;
      if( QViewFrame != null)
         QViewFrame.dispose();
      QViewFrame = null;
      V3d = null;
      
      if( V3DItems != null)
         V3DItems.kill();
      V3DItems = null;
      ShowQViewer = null;
      if( jp != null)
         jp.removeAll();
      if( tab != null)
         tab.removeAll();
            
      jp =  null;
      tab = null;
      JPanel pan = getPanel();
      if( pan != null)
      {
         pan.removeAncestorListener( this );
         pan.removeAll();
      }
      if( ControlPanel != null)
         ControlPanel.removeAll();
      if( Opns != null)
         Opns.removeAll();
      ControlPanel = null;
      Opns = null;
      
   }
   /*
    * (non-Javadoc) Will display some GUI's(Others will be invisible only for
    * wizard save file). Will add other buttons like to show QView Some GUI's
    * will be listeners take the place of certain menu items in
    * 
    * @see DataSetTools.wizard.Form#makeGUI()
    */
   @Override
   protected void makeGUI()
   {

      Vector Peaks = new Vector();
      if( getParameter( 0 ).getValue() instanceof Vector )
         Peaks = (Vector) getParameter( 0 ).getValue();

      V3d = new View3D( Peaks );

      setPeaks = new SetPeaks( V3d , Peaks );

      V3DControl = new View3DControl( V3d , Peaks );

      OutInfo = new Info( V3DControl );

      XtalLat = new XtalLatticeControl( setPeaks , OutInfo );

      Orient = new OrientMatrixControl( OutInfo , V3d , Peaks , V3DControl );
      Orient.setCrystalLatticeHandler( XtalLat , false );

      PeakFilter = new PeakFilterer( Peaks );
      PeakFilter.addFilterListener( this );

      Orient.setPeakFilterer( PeakFilter );
      Orient.setPeakSelector( setPeaks );

      V3DItems = new View3DItems( V3d , Peaks.size() , setPeaks );

      // -------- Get previous values out of parameters and into System-------
      // ---------Add listeners to system so parameters are changed when
      // corresponding ------
      // ----------- internal values change -------------------
      Vector VRes = (Vector)((ArrayPG)getParameter( ORIENT_MAT )).getValue();
      float[][] Res = Convert2floatAA( VRes);
      
      if( Res == null || Res.length !=3 || Res[0].length!=3 ||
               Res[1].length!= 3|| Res[2].length !=3 )
         Res = null;
      Orient.setOrientationMatrix( Res );
      NewOrientMatrixSet = false;
      Orient.addOrientationMatrixListener( this );

      
      String orFileName = getParameter(IN_FILE).getValue().toString();
      if( orFileName != null && orFileName.length()>0 &&
                  (new File(orFileName)).exists())
      {
         Orient.LoadOrientMatrix( orFileName);
      }
      
      Vector V = (Vector) getParameter( OMITTED ).getValue();
      int[] seq = ConvertTointArray( V );
      Orient.SetOmittedPeaks( seq );

      update( getParameter( XTAL_PARAMS ) , ParameterGUI.VALUE_CHANGED );
      update( getParameter( XTAL_TYPE ) , ParameterGUI.VALUE_CHANGED );
      update( getParameter( XTAL_PARAMS_ERR1 ) , ParameterGUI.VALUE_CHANGED );
      update( getParameter( XTAL_PARAMS_ERR2 ) , ParameterGUI.VALUE_CHANGED );
      update( getParameter( DMIN ) , ParameterGUI.VALUE_CHANGED );
      update( getParameter( DMAX ) , ParameterGUI.VALUE_CHANGED );

      ( (ParameterGUI) getParameter( XTAL_PARAMS ) ).addIObserver( this );
      ( (ParameterGUI) getParameter( XTAL_TYPE ) ).addIObserver( this );
      ( (ParameterGUI) getParameter( XTAL_PARAMS_ERR1 ) ).addIObserver( this );
      ( (ParameterGUI) getParameter( XTAL_PARAMS_ERR2 ) ).addIObserver( this );
      ( (ParameterGUI) getParameter( DMIN ) ).addIObserver( this );
      ( (ParameterGUI) getParameter( DMAX ) ).addIObserver( this );

      V = (Vector) getParameter( SELECTED_SEQ ).getValue();
      SetSelectedPeaksSeqNums( V , setPeaks , Peaks );
      V = (Vector) getParameter( SELECT_HKL ).getValue();
      SetSelectedPeaksHKL( V , setPeaks , Peaks );


      setPeaks.addSetPeakListeners( this );

      if( ( (BooleanPG) ( getParameter( SHOW_PEAKS ) ) ).getbooleanValue() )
         if( System.getProperty( "KeepPeakImageFiles" ) != null )
         {
            String S = getParameter( FILE_PREFIX ).getValue().toString();
            if( S.length() > 0 )
            {
               PeakImg = new PeakImageInfoHandler( null , S );
               OutInfo.addInfoHandler( "Peak Images" , PeakImg );
            }

         }

      // ---------- Create the GUI-----------------------

      if( panel == null )
         getPanel();

      
      panel.removeAll();
      panel.setLayout( new GridLayout( 1 , 1 ) );

      tab = new TabPane_UnOptimized();

      ControlPanel = new JPanel();
      BoxLayout bx = new BoxLayout( ControlPanel , BoxLayout.Y_AXIS );
      ControlPanel.setLayout( bx );
      ControlPanel.add( ( (ParameterGUI) getParameter( OUT_FILE ) )
               .getGUIPanel( true ) );
      ControlPanel.add( ( (ParameterGUI) getParameter( XTAL_PARAMS ) )
               .getGUIPanel( true ) );
      ControlPanel.add( ( (ParameterGUI) getParameter( XTAL_TYPE ) )
               .getGUIPanel( true ) );
      ControlPanel.add( ( (ParameterGUI) getParameter( XTAL_PARAMS_ERR1 ) )
               .getGUIPanel( true ) );
      ControlPanel.add( ( (ParameterGUI) getParameter( XTAL_PARAMS_ERR2 ) )
               .getGUIPanel( true ) );
      ControlPanel.add( ( (ParameterGUI) getParameter( DMIN ) )
               .getGUIPanel( true ) );
      ControlPanel.add( ( (ParameterGUI) getParameter( DMAX ) )
               .getGUIPanel( true ) );
      ControlPanel.add( Box.createVerticalGlue() );
      ShowQViewer = new JButton( "Show QView" );

      ShowQViewer.addActionListener( this );
      ControlPanel.add( ShowQViewer );
      ControlPanel.add( V3DItems );
      JButton butt = new JButton(OrientMatrixControl.ORIENT_MAT );
      ControlPanel.add( butt );
      butt.addActionListener( this);
      ControlPanel.add( PeakFilter );
      ControlPanel.add( setPeaks );
      ControlPanel.add( Box.createVerticalGlue() );
      IParameterGUI ResParam = getResultParam();
      ControlPanel.add( ResParam.getGUIPanel( true ) );
      ResParam.setEnabled( false );
      
     

      jp = new JPanel_UnOptiimized();

      jp.setLayout( new GridLayout( 1 , 1 ) );
      jp.add( ControlPanel );
      tab.addTab( "Operations" , jp );

      // Next Tab
      Opns = new JPanel_UnOptiimized();

      Opns.setLayout( new GridLayout( 1 , 1 ) );
      Opns.add( OutInfo );
      tab.addTab( "Information" , Opns );

      panel.add( tab );
      
      panel.addAncestorListener( this );

   }


   // Sets Selected Peaks
   private void SetSelectedPeaksSeqNums( Vector V , SetPeaks stPeaks ,
            Vector Peaks )
   {

      int[] seqNums = ConvertTointArray( V );

      if( seqNums == null || seqNums.length < 1 || Peaks == null )
         return;

      for( int i = 0 ; i < seqNums.length ; i++ )
         if( seqNums[ i ] >= 1 && seqNums[ i ] <= Peaks.size() )
         {
            stPeaks.setPeakQ( i , (IPeak) Peaks.elementAt( seqNums[ i ]-1 ) );
         }

   }


   // Sets selected peak hkl values
   private void SetSelectedPeaksHKL( Vector V , SetPeaks stPeaks , Vector Peaks )
   {

      if( V == null || V.size() < 1 || Peaks == null )
         return;
      
      for( int i = 0 ; i < V.size() ; i++ )
      {
         Vector V1 = (Vector) V.elementAt( i );
         
         if( V1 != null && V1.size() == 3 )
         {
            stPeaks.setPeakHKL( i , ( (Float) V1.elementAt( 0 ) ).floatValue() ,
                     ( (Float) V1.elementAt( 1 ) ).floatValue() , ( (Float) V1
                              .elementAt( 2 ) ).floatValue() );
         }
      }

   }


   private float[][] Convert2floatAA( Vector V)
   {
      if( V == null || V.size() < 1)
         return null;
      float[][] Res = new float[ V.size()][];
      for( int i=0; i< V.size(); i++)
      {
         Object V1 = V.elementAt( i );
         if( V1 instanceof Vector)
            Res[i] = Convert2floatA( (Vector)V1);
         else
            return null;
         
      }
        
     return Res;
     
   }
   
   private float[] Convert2floatA( Vector V)
   {
      if( V == null || V.size() < 1)
         return null;
      float[] Res = new float[V.size()];
      for( int i=0; i< V.size(); i++)
         try
        {
            Res[i] = ((Float)V.elementAt( i )).floatValue();   
         }catch( Exception s)
         {
            return null;
         }
      return Res;
   }
   private int[] ConvertTointArray( Vector V )
   {

      if( V == null || V.size() < 1 )
         return null;
      int[] Res = new int[ V.size() ];

      for( int i = 0 ; i < V.size() ; i++ )
         if( V.elementAt( i ) instanceof Number )
            Res[ i ] = ( (Number) V.elementAt( i ) ).intValue();
         else
            return null;

      return Res;

   }


   @Override
   /**
    * Reads the last orientation matrix that was set and saves it.
    */
   public Object getResult()
   {

      if( !NewOrientMatrixSet)//No new orientation matrix has been calculated
      {
         
         
         Orient.getActionListener().actionPerformed( new ActionEvent( this,
                                 ActionEvent.ACTION_PERFORMED,
                                 OrientMatrixControl.AUTOMATIC));
        
         NewOrientMatrixSet = false;
        
        
      }
      if( Orient.getInputFileName() == null)
         getParameter(IN_FILE).setValue( "" );
      
      float[][] orMat = Orient.getOrientationMatrix( - 1 );

      String fileName = gov.anl.ipns.Util.File.FileIO.CreateExecFileName(  getParameter(
               OUT_FILE_PATH ).getValue().toString() ,getParameter( OUT_FILE )
               .getValue().toString(),false) ;

      Object Res = Util.WriteMatrix( fileName , orMat );

      if( Res == null )
         Res = "Success ";
      else
         JOptionPane.showMessageDialog( null , "Could not save file "+ fileName );
     
      gov.anl.ipns.Util.Sys.SharedMessages.addmsg( Res );
      for( int i=0; i<=ORIENT_MAT; i++)
         ((IParameterGUI)getParameter(i)).setValidFlag( true );
      
      NewOrientMatrixSet = false;
      
      return orMat;
   }


   /*
    * (non-Javadoc)
    * Connects changes in the internal Peaks ui to the parameters so that the
    * values will be retained when returning to the form or after loading in
    * the wizard save file.
    * 
    * Also, Creates and handles the Qviewer
    * 
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   @Override
   public void actionPerformed( ActionEvent e )
   {

      String ActionCommand = e.getActionCommand();
      if( ActionCommand == OrientMatrixControl.INPUT_FILE_CHANGE )
      {
         ( (StringPG) getParameter( IN_FILE ) ).setValue( Orient
                  .getInputFileName() );
         
         getParameter( ORIENT_MAT )
                  .setValue( Orient.getOrientationMatrix( - 1 ) );
        
         return;
      }
      
      if( ActionCommand == PeakFilterer.OMITTED_PEAKS_CHANGED )
      {
        
         int[] seq = PeakFilter.getOmittedSequenceNumbers();
         Vector V = (Vector) DataSetTools.operator.Utils.ToVec( seq );
         getParameter( OMITTED ).setValue( V );
         return;
         
      }
      
      if( ActionCommand == SetPeaks.SET_PEAK_INFO_CHANGED )
      {
        
         Vector V = new Vector();
         boolean done = false;
         for( int i = 0 ; !done ; i++ )
         {

            int seqNum = setPeaks.getSetPeakSeqNum( i );
            if( seqNum <0)
               done = true;
            
             V.addElement( seqNum );
            /*
             * Vector V1 = new Vector(3); if( Qs == null || Qs.length !=3)
             * V.addElement( new Vector() ); else { V1.addElement(Qs[0]);
             * V1.addElement(Qs[1]); V1.addElement(Qs[2]); V.addElement( V1); }
             */
         }
         
         getParameter( SELECTED_SEQ ).setValue( V.clone() );
         V = new Vector();
         
         for( int i = 0 ; i < 3 ; i++ )
         {
            float[] hkls = setPeaks.getSetPeak_hkl( i );
            if( hkls == null || hkls.length != 3 )
               
               V.addElement( new Vector() );
            
            else
            {
               Vector V1 = new Vector();
               V1.addElement( hkls[ 0 ] );
               V1.addElement( hkls[ 1 ] );
               V1.addElement( hkls[ 2 ] );
               V.addElement( V1 );
            }
         }
         
         getParameter( SELECT_HKL ).setValue( V );
         
         return;
      }

      
      if( ActionCommand.equals( "Show QView" ) )
      {
         if( QViewFrame != null )
            return;
         
         QViewFrame = new JFrame( "Q Viewer" );
         
         QViewFrame.getContentPane().setLayout( new GridLayout( 1 , 1 ) );
         
         QViewFrame.getContentPane().add( V3d );
         
         Dimension Dim = Toolkit.getDefaultToolkit().getScreenSize();
         
         QViewFrame.setSize( Dim.width / 3 , Dim.height / 2 );
         
         QViewFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
         
         QViewFrame.addWindowListener( new MyWindowListener() );
         
         
         WindowShower.show( QViewFrame );

      }
      
      if( ActionCommand.equals( OrientMatrixControl.ORIENT_MATRIX_CHANGED ))
         NewOrientMatrixSet = true;
      
      if( ActionCommand.equals(OrientMatrixControl.ORIENT_MAT ))
      {
         if(Orient == null )
            return;
                 
         update( (  getParameter( XTAL_PARAMS )) ,ParameterGUI.VALUE_CHANGED );
               
         update( (  getParameter( XTAL_TYPE ) ),ParameterGUI.VALUE_CHANGED );
                 
         update( (  getParameter( XTAL_PARAMS_ERR1 ) ),ParameterGUI.VALUE_CHANGED );
                  
         update( (  getParameter( XTAL_PARAMS_ERR2 ) ),ParameterGUI.VALUE_CHANGED );
                 
         update( (  getParameter( DMIN ) ),ParameterGUI.VALUE_CHANGED );
                 
         update( (  getParameter( DMAX ) ),ParameterGUI.VALUE_CHANGED );
         
        Orient.getActionListener().actionPerformed( e );
       
         
      }

   }

   
   
   class MyWindowListener extends WindowAdapter
   {



      public void windowClosed( WindowEvent e )
      {

         QViewFrame = null;
      }
   }


   
   
   /*
    * (non-Javadoc)
    * Invoked when a user changes a ParameterGUI(Visible).  The new value 
    *    is transferred To the underlying DataSetTools.components.ui.Peaks 
    *    system
    * @see gov.anl.ipns.Util.Messaging.IObserver#update(java.lang.Object,
    *      java.lang.Object)
    */
   @Override
   public void update( Object observed_obj , Object reason )
   {
      
      if(  
               reason != ParameterGUI.VALUE_CHANGED)
         return;
      
      if( observed_obj == null )
         return;
      
      if( ! ( observed_obj instanceof ParameterGUI ) )
         return;
      
     
      
      if( observed_obj == getParameter( XTAL_PARAMS ) )
      {
         Vector V = (Vector) ( (ArrayPG) getParameter( XTAL_PARAMS ) )
                  .getValue();
         
         if( V == null || V.size() != 6 )
         {
            XtalLat.setXtalParams( null );
         }
         
         
         float[] params = new float[ 6 ];
         for( int i = 0 ; i < 6 ; i++ )
            try
            {  
               if( V != null)
                  params[ i ] = ( (Number) V.elementAt( i ) ).floatValue();
            }
            catch( Exception s )
            {
               XtalLat.setXtalParams( null );
               return;
            }
         XtalLat.setXtalParams( params );


      }
      else if( observed_obj == getParameter( XTAL_PARAMS_ERR1 )
               || observed_obj == getParameter( XTAL_PARAMS_ERR2 ) )
      {
         float delta1 = ( (FloatPG) getParameter( XTAL_PARAMS_ERR1 ) )
                  .getfloatValue();
         
         float delta2 = ( (FloatPG) getParameter( XTAL_PARAMS_ERR2 ) )
                  .getfloatValue();
         
         XtalLat.setDeltas( delta1 , delta2 );
         
      }
      else if( observed_obj == getParameter( XTAL_TYPE ) )
      {
         XtalLat
                  .setCentering( getParameter( XTAL_TYPE ).getValue()
                           .toString().charAt( 0 ) );
         

      }
      else if( observed_obj == getParameter( DMIN )||
               observed_obj == getParameter( DMAX))//Will let getResult save it
      {
           float dmin = ((FloatPG)getParameter(DMIN)).getfloatValue();
           float dmax = ((FloatPG)getParameter(DMAX)).getfloatValue();
           Orient.setDMin_Max( dmin , dmax );
      }


   }
   
   

   /* (non-Javadoc)
    * @see javax.swing.event.AncestorListener#ancestorAdded(javax.swing.event.AncestorEvent)
    */
   @Override
   public void ancestorAdded( AncestorEvent event )
   {

      
      
   }


   /* (non-Javadoc)
    * @see javax.swing.event.AncestorListener#ancestorMoved(javax.swing.event.AncestorEvent)
    */
   @Override
   public void ancestorMoved( AncestorEvent event )
   {

     
      
   }


   /* (non-Javadoc)
    * @see javax.swing.event.AncestorListener#ancestorRemoved(javax.swing.event.AncestorEvent)
    */
   @Override
   public void ancestorRemoved( AncestorEvent event )
   {
     if( QViewFrame == null)
        return;
    
     QViewFrame.dispose();
     QViewFrame = null;
     getPanel().removeAll();
     
      
   }

  
}
//------------------- UnOptimimized JPanel and JTabbedPane----------------
class JPanel_UnOptiimized extends JPanel
{



   public boolean isOptimizedDrawingEnabled()
   {

      return false;
   }
}

class TabPane_UnOptimized extends JTabbedPane
{



   public boolean isOptimizedDrawingEnabled()
   {

      return false;
   }
}
