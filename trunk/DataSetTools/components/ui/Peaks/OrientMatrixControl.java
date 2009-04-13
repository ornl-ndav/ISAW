/* 
 * File: OrientationMatrixControl.java
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
 *  $Author$
 *  $Date$            
 *  $Rev$
 */

package DataSetTools.components.ui.Peaks;

import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.Util.File.FileIO;
import gov.anl.ipns.Util.File.RobustFileFilter;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.ViewTools.Components.OneD.DataArray1D;
import gov.anl.ipns.ViewTools.Components.OneD.FunctionViewComponent;
import gov.anl.ipns.ViewTools.Components.OneD.VirtualArrayList1D;
import gov.anl.ipns.ViewTools.Components.*;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import DataSetTools.operator.Generic.TOF_SCD.GetUB;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;
import IPNSSrc.blind;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 *  Handles orientation matrix calculations, entry, saving etc.
 *  
 * @author Ruth
 *
 */
public class OrientMatrixControl extends JButton
{



   // JButton Text 
   public static String ORIENT_MAT              = "Orientation Matrix";

   //--------- sub menu options of ORIENT_MAT
   public static String LOAD_ORIENT1            = "Load Orientation matrix";

   public static String ENTER_ORIENT1           = "Enter Orientation matrix";

   public static String SAVE_ORIENT1            = "Save Orientation matrix";

   public static String SHOW_SEL_MAT            = "Show/Select Matrix from Set";
   
   public static String ADJUST_OR_MAT           ="Adjust Orientation Matrix";

   //------------------------------------------------------
   public static String VIEWS                   = "View in QViewer";

   //---------- sub menu options for VIEW ---------------
   public static String VIEW_ORIENT             = "View Orientation matrix";

   public static String VIEW_PLANEab            = "View Plane Family a*b*";

   public static String VIEW_PLANEac            = "View Plane Family a*c*";

   public static String VIEW_PLANEbc            = "View Plane Family b*c*";

   //---------------------------------------------------------
   public static String CALC_ORIENT             = "Calculate Orientation matrix(s)";

   //--------------- sub menu options for CALC_ORIENT ---------------
   public static String BLIND                   = "Blind";

   public static String AUTOMATIC               = "Automatic";

   public static String FOUR_PEAK               = "From 4 Peaks";

   public static String TWO_PEAK                = "From 2 Peaks with hkls";

   public static String THREE_PEAK              = "From 3 Peaks with hkls";

   //------------------------------------------
   int                  NMillerOffsetBins       = 50;

   //Initial Data
   View3D               View;

   Vector< IPeak >      Peaks;

   //Other controls
   View3DControl        V3DControl;

   Info                 textInfo;

   //Calculated variables
   boolean[]            omittedPeakIndex;

   float[][]            orientationMatrix       = null;

   float[][]            TranspOrientationMatrix = null;

   Vector< float[][] >  OrMatrices;

   float[]              err                     = null;

   //View orientation mat in 3D view
   boolean              showLatinOrientMenu;


   //Information Handlers 
   IOrientInfoHandler   OrientMatInfHandler , 
                        h_offsetInfHandler ,
                        k_offsetInfHandler , 
                        l_offsetInfHandler;

   // Other input objects
   PeakFilterer         peakFilter;

   SetPeaks             selectedPeaks;

   XtalLatticeControl   LatControl;

   //Utilities 
   MyActionListener     Listener;

   JPopupMenu           Menu;


   /**
    * Constructor
    * 
    * @param TextInf   The Info object to display text like information
    * @param view      The 3D View of peaks objects Q values
    * @param peaks     The peaks
    * @param v3DControl  The control object associated with view
    */
   public OrientMatrixControl( Info TextInf, 
                               View3D view,
                              Vector< IPeak > peaks, 
                              View3DControl v3DControl )
   {

      super( ORIENT_MAT );
      Peaks = peaks;
      textInfo = TextInf;

      omittedPeakIndex = null;
      View = view;
      V3DControl = v3DControl;

      Listener = new MyActionListener( this );
      Menu = new JPopupMenu( ORIENT_MAT );
      MakePopUpMenu();

      addActionListener( Listener );

      peakFilter = null;
      selectedPeaks = null;
      LatControl = null;

      showLatinOrientMenu = false;

      OrMatrices = new Vector< float[][] >();

   }


   /**
    * Sets the orientation matrix and updates the Info viewers only 
    * of this information. To reflect this change in the Q viewer
    * press the appropriate View in Qviewer menu
    * 
    * @param OrientMat  the new orientation matrix
    */
   public void setOrientationMatrix( float[][] OrientMat )
   {

      if( OrientMat == null || OrientMat.length != 3
               || OrientMat[ 0 ].length != 3 || OrientMat[ 1 ].length != 3
               || OrientMat[ 2 ].length != 3 )

         OrientMat = null;

      String res = subs.ShowOrientationInfo( Peaks , OrientMat ,
               omittedPeakIndex , err , false );

      float[] xvals = new float[ NMillerOffsetBins + 1 ];

      xvals[ 0 ] = - .5f;

      float delta = 1f / NMillerOffsetBins;
      for( int i = 1 ; i < xvals.length ; i++ )
         xvals[ i ] = xvals[ i - 1 ] + delta;

      float[] hyvals = subs.getMillerOffsets( Peaks , omittedPeakIndex ,
               OrientMat , xvals , false , 'h' );

      float[] kyvals = subs.getMillerOffsets( Peaks , omittedPeakIndex ,
               OrientMat , xvals , false , 'k' );

      float[] lyvals = subs.getMillerOffsets( Peaks , omittedPeakIndex ,
               OrientMat , xvals , false , 'l' );


      if( orientationMatrix == null )
         if( OrientMat == null ) //old one null so everything should have been removed
            return;

         else
         {
            if( textInfo != null )
            {
               OrientMatInfHandler = new OrientMatInfoHandler( res );

               textInfo.addInfoHandler( "Orientation Matrix.matrix" ,
                        OrientMatInfHandler );

               h_offsetInfHandler = new MillerOffsetInfoHandler( 'h' , xvals ,
                        hyvals );

               k_offsetInfHandler = new MillerOffsetInfoHandler( 'k' , xvals ,
                        kyvals );

               l_offsetInfHandler = new MillerOffsetInfoHandler( 'l' , xvals ,
                        lyvals );

               textInfo.addInfoHandler( "Orientation Matrix.h offset" ,
                        h_offsetInfHandler );

               textInfo.addInfoHandler( "Orientation Matrix.k offset" ,
                        k_offsetInfHandler );

               textInfo.addInfoHandler( "Orientation Matrix.l offset" ,
                        l_offsetInfHandler );

            }
         }
      else if( OrientMat == null ) //remove items from menus
      {
         textInfo.removeInfoHandler( "Orientation Matrix.matrix" );

         textInfo.removeInfoHandler( "Orientation Matrix.h offset" );

         textInfo.removeInfoHandler( "Orientation Matrix.k offset" );

         textInfo.removeInfoHandler( "Orientation Matrix.l offset" );

         return;

      }

      orientationMatrix = OrientMat;

      Tran3D T = ( new Tran3D( orientationMatrix ) );

      T.transpose();

      TranspOrientationMatrix = T.get();

      OrientMatInfHandler.setNewData( res );

      h_offsetInfHandler.setNewData( addVec( addVec( null , xvals ) , hyvals ) );

      k_offsetInfHandler.setNewData( addVec( addVec( null , xvals ) , kyvals ) );

      l_offsetInfHandler.setNewData( addVec( addVec( null , xvals ) , lyvals ) );

      Listener.do3DView();
   }


   /**
    * Sets an orientation matrix or one in a list of orientation matrices 
    * if matnum >=0
    * 
    * @param orientMat  The orientation matrix or null to unset
    * 
    * @param matNum     The matnum or a negative number to set the
    *                   current orientation matrix.
    *                   
    * NOTE: currently only negative matnums will have an effect
    */
   public void setOrientationMatrix( float[][] orientMat , int matNum )
   {

      if( matNum >= 0 )

         return;// Not done yet

      setOrientationMatrix( gov.anl.ipns.MathTools.LinearAlgebra
               .copy( orientMat ) );
   }


   /**
    * Returns the indicated orientation matrix
    * @param matNum   The matnum or a negative number to set the
    *                   current orientation matrix.
    *                   
    * @return     the indicated orientation matrix
    */
   public float[][] getOrientationMatrix( int matNum )
   {

      if( matNum < 0 )
         return gov.anl.ipns.MathTools.LinearAlgebra.copy( orientationMatrix );

      return null;
   }


   /**
    * Sets the crystal lattice handler and incorporates it into the system
    * 
    * @param LatControl   The Crystal lattice controller
    * @param showInOrientMenu  if true the menu will appear with the
    *                       orientation menu items
    */
   public void setCrystalLatticeHandler( XtalLatticeControl LatControl ,
                                         boolean showInOrientMenu )
   {

      this.LatControl = LatControl;
      showLatinOrientMenu = showInOrientMenu;

      Menu = new JPopupMenu( ORIENT_MAT );

      MakePopUpMenu();
   }


   /**
    * Set the SetPeak object that contains information on the set peaks
    * @param SelPeaks  The SetPeak object
    */
   public void setPeakSelector( SetPeaks SelPeaks )
   {

      selectedPeaks = SelPeaks;
   }


   /**
    * Sets the PeakFilter object that is used to omit peaks
    * @param pkFilt  The PeakFilterer object
    */
   public void setPeakFilterer( PeakFilterer pkFilt )
   {

      this.peakFilter = pkFilt;
      pkFilt.addFilterListener( Listener );

   }


   /**
    * Sets omitted peaks
    * @param seqNums an array of peak sequence numbers
    */
   public void SetOmittedPeaks( int[] seqNums )
   {

      if( omittedPeakIndex != null )
      {
         Vector< Integer > Seqs = new Vector< Integer >();

         for( int i = 0 ; i < omittedPeakIndex.length ; i++ )
            if( omittedPeakIndex[ i ] )

               Seqs.add( i + 1 );

         int[] SeqsInt = new int[ Seqs.size() ];

         for( int i = 0 ; i < SeqsInt.length ; i++ )
            SeqsInt[ i ] = Seqs.elementAt( i ).intValue();

         View.IncludeSeqNums( SeqsInt );
      }

      omittedPeakIndex = new boolean[ Peaks.size() ];

      java.util.Arrays.fill( omittedPeakIndex , false );

      if( seqNums != null )

         for( int i = 0 ; i < seqNums.length ; i++ )
            omittedPeakIndex[ seqNums[ i ] - 1 ] = true;

      else
         omittedPeakIndex = null;

      View.omitSeqNums( seqNums );

      updateInf_OrientMat( seqNums );

      updateInf_hkl_Offset( seqNums );

   }

 //converts mask form to int[] form
   protected int[] getOmittedSeqNums( )
   {
      return getOmittedSeqNums( true );
   }
   //converts mask form to int[] form
   protected int[] getOmittedSeqNums( boolean omitted )
   {

      if( omittedPeakIndex == null )
         omittedPeakIndex = new boolean[0];

      int[] list = new int[ Peaks.size() ];

      int k = 0;
      
      for( int i = 0 ; i < omittedPeakIndex.length ; i++ )

         if( omittedPeakIndex[ i ] == omitted)

            list[ k++ ] = i + 1;
      
      for( int j= omittedPeakIndex.length; j< Peaks.size(); j++)
         
         if( false == omitted)
            
            list[k++]= j+1;
      
      int[] Res = new int[ k ];

      System.arraycopy( list , 0 , Res , 0 , k );

      return Res;

   }


   /**
    * Loads in the orientation matrix and incorporates into the system
    * 
    * @param filename  The filename with the orientation matrix
    */
   public void LoadOrientMatrix( String filename )
   {

      Object res = Operators.TOF_SCD.IndexJ.readOrient( filename );

      if( res instanceof float[][] )

         setOrientationMatrix( (float[][]) res );

   }


   /**
    * Shows information about the current orientation matrix in an option 
    * dialog 
    * 
    * @param WithSelectPoint  Include information about the set peaks
    */
   public void showCurrentOrientationMatrix( boolean WithSelectPoint )
   {

      JOptionPane.showMessageDialog( null , ShowMatString( true , false ,
               orientationMatrix ) );
   }


   /**
    * Shows information about the current orientation matrices in an option 
    * dialog 
    * 
    * @param WithSelectPeaks Include information about the set peaks
    * 
    * @param SelectMatrix   Allow for the selecting one orientation matrix
    */
   public void showCurrentOrientationMatrices( boolean WithSelectPeaks ,
            boolean SelectMatrix )
   {

      SetPeaks Setpks = null;

      if( WithSelectPeaks )
         Setpks = selectedPeaks;

      float[][] M = ( new OrientMatListHandler( OrMatrices , Setpks ,
               SelectMatrix ) ).run();

      if( M != null )
         setOrientationMatrix( M );

   }


   // Shows string information about a UB matrix
   private String ShowMatString( boolean WithSelectPoints ,
            boolean WithPeaksInfo , float[][] UB )
   {

      String Text1;
      Vector< IPeak > pks = null;

      if( WithPeaksInfo )
         pks = Peaks;

      Text1 = subs.ShowOrientationInfo( pks , UB , omittedPeakIndex , null ,
               false );

      Text1 += "\n";

      if( ! WithSelectPoints )
         return Text1;

      if( UB == null )
         return Text1 + "Orientation Matrix is null";

      float[][] UBinv = LinearAlgebra.getInverse( UB );

      if( UBinv == null )
         return Text1 + "Orientation Matrix is NOT invertible";

      Text1 += " Selected Peak Indices \n";
      Text1 += "Seq \n";
      Text1 += "Num    qx     qy    qz     h    k     l\n";

      for( int i = 0 ; i < SetPeaks.MAX_SEL_PEAKS ; i++ )
      {

         float[] Qs = selectedPeaks.getSetPeakQ( i );
         if( Qs == null )
            return Text1;

         Text1 += String.format( "%6d " , selectedPeaks.getSetPeakSeqNum( i ) );

         Text1 += String.format( "%5.2f %5.2f %5.2f " , Qs[ 0 ] , Qs[ 1 ] ,
                  Qs[ 2 ] );

         for( int row = 0 ; row < 3 ; row++ )
            Text1 += String
                     .format( "%5.2f " , UBinv[ row ][ 0 ] * Qs[ 0 ]
                              + UBinv[ row ][ 1 ] * Qs[ 1 ] + UBinv[ row ][ 2 ]
                              * Qs[ 2 ] );

         Text1 += "\n";

      }

      return Text1;
   }


   //Makes the original pop up menu
   private void MakePopUpMenu()
   {

      ( Menu.add( LOAD_ORIENT1 ) ).addActionListener( Listener );

      ( Menu.add( ENTER_ORIENT1 ) ).addActionListener( Listener );

      if( showLatinOrientMenu && LatControl != null )
      {

         ( Menu.add( XtalLatticeControl.CRYSTAL_LAT_INPUT_TEXT ) )
                  .addActionListener( LatControl.getListener() );
      }

      ( Menu.add( SAVE_ORIENT1 ) ).addActionListener( Listener );
      
      (Menu.add(SHOW_SEL_MAT )).addActionListener( Listener );
      

      ( Menu.add( VIEWS ) ).addActionListener( Listener );
      ( Menu.add( ADJUST_OR_MAT ) ).addActionListener( Listener );
      

      ( Menu.add( CALC_ORIENT ) ).addActionListener( Listener );


   }


   // updates OrientMatInfHandler when orientation matrix changes
   private void updateInf_OrientMat( int[] seqNums )
   {

      if( OrientMatInfHandler == null )
         return;

      String res = subs.ShowOrientationInfo( Peaks , orientationMatrix ,
               omittedPeakIndex , err , false );

      OrientMatInfHandler.setNewData( res );

   }


   // updates the h,k,l_offsetHandlers when the orientation matrix changes
   // NOTE seqNums not used. omittedPeakIndex is used
   private void updateInf_hkl_Offset( int[] seqNums )
   {

      float[] xvals = new float[ NMillerOffsetBins + 1 ];

      xvals[ 0 ] = - .5f;
      float delta = 1f / NMillerOffsetBins;

      for( int i = 1 ; i < xvals.length ; i++ )
         xvals[ i ] = xvals[ i - 1 ] + delta;

      if( h_offsetInfHandler != null )
      {
         float[] hyvals = subs.getMillerOffsets( Peaks , omittedPeakIndex ,
                  orientationMatrix , xvals , false , 'h' );

         h_offsetInfHandler
                  .setNewData( addVec( addVec( null , xvals ) , hyvals ) );
      }

      if( k_offsetInfHandler != null )
      {
         float[] hyvals = subs.getMillerOffsets( Peaks , omittedPeakIndex ,
                  orientationMatrix , xvals , false , 'k' );

         k_offsetInfHandler
                  .setNewData( addVec( addVec( null , xvals ) , hyvals ) );
      }

      if( l_offsetInfHandler != null )
      {
         float[] hyvals = subs.getMillerOffsets( Peaks , omittedPeakIndex ,
                  orientationMatrix , xvals , false , 'l' );

         l_offsetInfHandler
                  .setNewData( addVec( addVec( null , xvals ) , hyvals ) );
      }


   }


   // Adds a value to a vec and returns the resultant vector. Chaining
   private Vector addVec( Vector V , Object O )
   {

      if( V == null )
         V = new Vector();

      V.addElement( O );

      return V;
   }


   /**
    * Listener to most of the menu items
    * @author Ruth
    *
    */
   class MyActionListener implements ActionListener
   {



      JButton           button;

      String            lastFileName;

      CalculateListener CalcListener = null;

      int               planeNum     = - 1;
      
      public   String   SelView3DItem = "None";

      float[]  zero = new float[]{0f,0f,0f};
      public MyActionListener( JButton but )
      {

         button = but;
         lastFileName = System.getProperty( "ISAW_HOME" );
      }

      /**
       * The action commands that are responded to are
       * 
       * 
       */
      public void do3DView(  )
      {
         if( SelView3DItem.equals(  "None" ) ) 
            
            View.showPlanes( null , null , null , null );
         
         else if( SelView3DItem == VIEW_ORIENT)
         {

            View.showOrientation( orientationMatrix , View.getLastSelectedSeqNum() );
            ( (OrientMatInfoHandler) OrientMatInfHandler ).setOrientationInfo(
                     View , orientationMatrix );

         }else if(  SelView3DItem == VIEW_PLANEab)
         {

            View.showPlanes( zero , TranspOrientationMatrix[ 0 ] ,
                     TranspOrientationMatrix[ 1 ] ,
                     TranspOrientationMatrix[ 2 ] );

         }else if(  SelView3DItem == VIEW_PLANEac)
         {
            View.showPlanes( zero , TranspOrientationMatrix[ 0 ] ,
                     TranspOrientationMatrix[ 2 ] ,
                     TranspOrientationMatrix[ 1 ] );

            
         }else if(  SelView3DItem == VIEW_PLANEbc)
         {

            View.showPlanes( zero , TranspOrientationMatrix[ 1 ] ,
                     TranspOrientationMatrix[ 2 ] ,
                     TranspOrientationMatrix[ 0 ] );

         }

 
         
      }
      
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         String evt = e.getActionCommand();
         if( evt == ORIENT_MAT )
         {
            Menu.show( button , 0 , 0 );
            return;
         }
         if( evt == LOAD_ORIENT1 )
         {

            JFileChooser jf = new JFileChooser( lastFileName );
            RobustFileFilter F = ( new RobustFileFilter() );
            F.addExtension( "mat" );
            jf.setFileFilter( F );

            if( jf.showOpenDialog( null ) != JFileChooser.APPROVE_OPTION )
               return;

            String filename = jf.getSelectedFile().getAbsolutePath();

            lastFileName = filename;

            LoadOrientMatrix( filename );

            return;
         }

         if( evt == ENTER_ORIENT1 ) 
         {

            return; 
         }

         if( evt == SAVE_ORIENT1 )
         {
            if( orientationMatrix == null)
            {
               JOptionPane.showMessageDialog( null , 
                            "There is no orientation matrix to save" );
               return;
            }
            JFileChooser jf = new JFileChooser( lastFileName );
            RobustFileFilter F = ( new RobustFileFilter() );
            F.addExtension( "mat" );
            jf.setFileFilter( F );

            if( jf.showSaveDialog( null ) != JFileChooser.APPROVE_OPTION )
               return;

            String filename = jf.getSelectedFile().getAbsolutePath();

            lastFileName = filename;

            Object Res= DataSetTools.operator.Generic.TOF_SCD.Util.WriteMatrix(
                     filename , orientationMatrix );
            if( Res != null)
               JOptionPane.showMessageDialog(null, Res);

            return;
            
            
         }if( evt ==  SHOW_SEL_MAT )
         {
            showCurrentOrientationMatrices( true, true );
            
            return; 
         }  
         
         
         if( evt ==  ADJUST_OR_MAT )
         {
              JPopupMenu pop = new JPopupMenu("Adjust Orient Matrix");
              (pop.add( "Index Peaks" )).addActionListener(this);
              (pop.add( "Niggli with Blind" )).addActionListener( this);
              (pop.add( "Niggli(experimental" )).addActionListener( this);
              (pop.add( "Optimize" )).addActionListener( this );
              if( e.getSource() instanceof AbstractButton)
                   pop.show( button, 0,0);
              else
                 pop.show( null, 100,100);
              

               return; 
         } 
        
         
         if( evt == VIEWS )
         {
            if( orientationMatrix == null )
               return;

            JPopupMenu pop = new JPopupMenu();
            ButtonGroup grp = new ButtonGroup();

            JMenuItem men = pop.add( new JCheckBoxMenuItem( VIEW_ORIENT ) );
            men.addActionListener( this );
            grp.add( men );

            men = pop
                .add( new JCheckBoxMenuItem( VIEW_PLANEab , planeNum == 1 ) );
            men.addActionListener( this );
            grp.add( men );

            men = pop
                 .add( new JCheckBoxMenuItem( VIEW_PLANEac , planeNum == 2 ) );
            men.addActionListener( this );
            grp.add( men );

            men = pop
                 .add( new JCheckBoxMenuItem( VIEW_PLANEbc , planeNum == 3 ) );
            men.addActionListener( this );
            grp.add( men );


            men = pop.add( new JCheckBoxMenuItem( "None" ) );
            men.addActionListener( this );
            grp.add( men );

            pop.show( button , 0 , 0 );

         }


         if( evt == VIEW_ORIENT )
         {
            JCheckBoxMenuItem men = (JCheckBoxMenuItem) e.getSource();
            if( orientationMatrix == null || View == null )
            {
               men.setState( false );
               return;
            }
           
            float[][] mat = null;

            if( men.getState() )
               mat = orientationMatrix;
            
            if( mat != null )
               SelView3DItem = VIEW_ORIENT;
            else
               SelView3DItem = "None";

            View.showOrientation( mat , View.getLastSelectedSeqNum() );
            ( (OrientMatInfoHandler) OrientMatInfHandler ).setOrientationInfo(
                     View , orientationMatrix );

            if( mat != null )

               V3DControl.addSelectPeakHandler( 
                             (OrientMatInfoHandler) OrientMatInfHandler );

            else

               V3DControl.removeSelectPeakHandler( 
                        (OrientMatInfoHandler) OrientMatInfHandler );


            return;

         }

         boolean isSelected = false;

         if( e.getSource() instanceof JCheckBoxMenuItem )
            isSelected = ( (JCheckBoxMenuItem) e.getSource() ).isSelected();

         float[] zero =
         {
                  0f , 0f , 0f
         };

         if( evt == VIEW_PLANEab )
         {
            if( isSelected )

               View.showPlanes( zero , TranspOrientationMatrix[ 0 ] ,
                        TranspOrientationMatrix[ 1 ] ,
                        TranspOrientationMatrix[ 2 ] );

            else

               View.showPlanes( null , null , null , null );

            planeNum = 1;
            
            if( isSelected )
               SelView3DItem = VIEW_PLANEab;
            else
               SelView3DItem="None";
            
            return;

         }

         if( evt == VIEW_PLANEac )
         {

            if( isSelected )

               View.showPlanes( zero , TranspOrientationMatrix[ 0 ] ,
                        TranspOrientationMatrix[ 2 ] ,
                        TranspOrientationMatrix[ 1 ] );

            else

               View.showPlanes( null , null , null , null );

            planeNum = 2;

            return;

         }

         if( evt == VIEW_PLANEbc )
         {
            if( isSelected )


               View.showPlanes( zero , TranspOrientationMatrix[ 1 ] ,
                        TranspOrientationMatrix[ 2 ] ,
                        TranspOrientationMatrix[ 0 ] );

            else

               View.showPlanes( null , null , null , null );

            planeNum = 3;

            
            if( isSelected )
               SelView3DItem = VIEW_PLANEbc;
            else
               SelView3DItem = "None";
            
            return;

         }


         if( evt.equals( "None" ) )
         {
            View.showPlanes( null , null , null , null );
            
           
            SelView3DItem = "None";
         }

         if( evt == CALC_ORIENT )
         {
            if( CalcListener == null )
               CalcListener = new CalculateListener();

            CalcListener.MakeMenus( button );

            return;

         }


         if( evt == PeakFilterer.OMITTED_PEAKS_CHANGED )
         {
            int[] omitted = peakFilter.getOmittedSequenceNumbers();

            SetOmittedPeaks( omitted );

         }
         

         if( evt.equals("Index Peaks" ))
            
            
         {
            String S = JOptionPane.showInputDialog(  "Enter delta h,delta k, delta l separated by commas" );
            if( S == null)
               return;
            Object Res = null;
            try
            {
            String[] Data = S.split( "," );
            Res =( new  Operators.TOF_SCD.IndexJ_base( Peaks, orientationMatrix,
                     "", Float.parseFloat( Data[0].trim() ), Float.parseFloat( Data[1].trim() ),
                     Float.parseFloat( Data[2].trim() ))).getResult();
             if( !(Res instanceof ErrorString))
             {
                gov.anl.ipns.Util.Sys.SharedMessages.addmsg(   Res );
                if( peakFilter != null )
                   peakFilter.set_hklMinMax();
                return;
             }
                
            }catch( Exception ss){
               Res = ss.toString();
            }
            
            JOptionPane.showMessageDialog( null, "Error=="+Res );
            return;
         }

         if( evt.equals("Niggli with Blind" ))
         {
            if( orientationMatrix == null)
            {
               gov.anl.ipns.Util.Sys.SharedMessages.addmsg(  "There is no orientation matrix");
               return;
            }
            blind Blind = new blind();
            Object Res = Blind.blaue( orientationMatrix);
            if( Res != null)
            {
               JOptionPane.showMessageDialog( null, "Error=="+Res );
               return;
            }
            setOrientationMatrix( LinearAlgebra.double2float( Blind.UB ));
            return;
         }

         if( evt.equals("Niggli(experimental" ))
         {
            setOrientationMatrix( subs.Nigglify( orientationMatrix ));
            return;
         }

         if( evt .equals("Optimize" ))
         {
            String filename =FileIO.appendPath( System.getProperty( "user.home"),"ISAW/tmp" );
            filename +="Lsqrs.mat";
            
            Object Res = Operators.TOF_SCD.LsqrsJ_base.LsqrsJ1(  Peaks,null, 
                     getOmittedSeqNums( false),  null,filename, 0,null,"triclinic");
            if( Res != null  && (Res instanceof ErrorString))
            {
               JOptionPane.showMessageDialog( null , "Error Least Squares "+ Res );
               return;
            }
            
            LoadOrientMatrix( filename );

            if( OrientMatInfHandler != null)
                OrientMatInfHandler.setNewData( subs.GetPeakFitInfo( Peaks ,
                         orientationMatrix , omittedPeakIndex ) );
            return;
         }

      }

   }

   /**
    * Listeners for the Calculation menu items
    * @author Ruth
    *
    */
   class CalculateListener implements ActionListener
   {



      /** 
       * Makes the calculation menu items
       * @param comp
       */
      public void MakeMenus( JComponent comp )
      {

         JPopupMenu pop = new JPopupMenu( "Calc Methods" );

         int nPeaksSet = NsetQ();
         int nHKLsSet = NsetHKL();

         JMenuItem men;
         if( nPeaksSet >= 4 )
         {
            men = ( pop.add( BLIND ) );
            men.addActionListener( this );
            men.setToolTipText( "IPNS blind method" );
         }

         men = ( pop.add( AUTOMATIC ) );
         men.addActionListener( this );
         men.setToolTipText( "Automatic Method based on Rossman" );

         if( nPeaksSet >= 4 )
         {

            men = ( pop.add( FOUR_PEAK ) );
            men.addActionListener( this );
            men.setToolTipText( "4 peaks represent a primitive cell" );

         }

         if( nPeaksSet >= 2 )
         {
            men = ( pop.add( TWO_PEAK ) );
            men.addActionListener( this );
            men
                     .setToolTipText( "Use 2 peaks,their hkl vals, and Lattice Parameters" );
         }

         if( nPeaksSet >= 3 && nHKLsSet >= 3 )
         {
            men = ( pop.add( THREE_PEAK ) );
            men.addActionListener( this );
            men.setToolTipText( "Use 3 peaks,their hkl vals" );
         }

         pop.show( comp , 0 , 0 );

      }


      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         String evtString = e.getActionCommand();

         if( evtString == BLIND || evtString == FOUR_PEAK )
         {
            blind BLIND = new blind();
            int nPeaksSet = NsetQ();

            if( evtString == FOUR_PEAK && nPeaksSet > 4 )
               nPeaksSet = 4;

            double[] xx = new double[ nPeaksSet + 3 ];
            double[] yy = new double[ nPeaksSet + 3 ];
            double[] zz = new double[ nPeaksSet + 3 ];

            for( int i = 0 ; i < nPeaksSet ; i++ )
            {
               float[] f = selectedPeaks.getSetPeakQ( i );
               xx[ i ] = f[ 0 ];
               yy[ i ] = f[ 1 ];
               zz[ i ] = f[ 2 ];

            }

            int[] seqNums = new int[ nPeaksSet ];
            java.util.Arrays.fill( seqNums , 1 );

            boolean OK = BLIND.abid( xx , yy , zz );

            if( ! OK )
            {
               showError( "abid error could not sort " );
               return;
            }

            ErrorString error = BLIND.bias( nPeaksSet + 3 , xx , yy , zz ,
                     seqNums );
            if( error != null )
            {

               showError( error.toString() );
               return;
            }

            setOrientationMatrix( LinearAlgebra.double2float( BLIND.UB ) );

            showCurrentOrientationMatrix( true );

            return;

         }


         if( evtString == AUTOMATIC )
         {

            String S = JOptionPane.showInputDialog( "Enter min D and MaxD" ,
                     "3.1,12.5" );

            if( S == null )
               return;

            float MaxXtalLengthReal;

            try
            {
               String[] SS = S.split( "," );
               if( SS == null || SS.length < 2 )
                  return;

               MaxXtalLengthReal = Float.parseFloat( SS[ 1 ] );

            }
            catch( Exception ss )
            {
               MaxXtalLengthReal = - 1;
               return;
            }

            OrMatrices = GetUB. getAllOrientationMatrices( Peaks , omittedPeakIndex ,
                     .02f , MaxXtalLengthReal );

            if( OrMatrices == null || OrMatrices.size() < 1 )
               return;

            setOrientationMatrix( OrMatrices.elementAt( 0 ) );

            showCurrentOrientationMatrices( true , true );

            return;

         }


         if( evtString == TWO_PEAK )//Fix to return an array of orientation matrices
         {
            if( LatControl == null || selectedPeaks == null )
               return;

            OrMatrices.clear();

            float[] q1 = selectedPeaks.getSetPeakQ( 0 );
            float[] q2 = selectedPeaks.getSetPeakQ( 1 );

            if( q1 == null || q2 == null )
               return;

            int[][] hklList;

            boolean peak1HKLset = false;

            if( selectedPeaks.getSetPeak_hkl( 0 ) != null )
            {
               hklList = new int[ 1 ][ 3 ];
               float[] F = selectedPeaks.getSetPeak_hkl( 0 );

               for( int i = 0 ; i < 3 ; i++ )
                  hklList[ 0 ][ i ] = (int) F[ i ];

               peak1HKLset = true;

            }
            else
            {
               hklList = subs.FindPossibleHKLs( LatControl.BMat , q1 ,
                        LatControl.Delta1 , LatControl.Delta2 ,
                        LatControl.Centering );

            }

            if( hklList == null || hklList.length < 1 )
            {
               JOptionPane
                        .showMessageDialog(
                                 null ,
                                 "There are no possible hkl's for "
                                          + "selected Peak 1 with given Crystal Parameters" );
               return;

            }

            for( int i = 0 ; i < hklList.length ; i++ )
            {
               int[][] hklList2;

               if( peak1HKLset && selectedPeaks.getSetPeak_hkl( 1 ) != null )
               {
                  float[] F = selectedPeaks.getSetPeak_hkl( 1 );
                  hklList2 = new int[ 1 ][ 3 ];

                  for( int k = 0 ; k < 3 ; k++ )
                     hklList2[ 0 ][ k ] = (int) F[ k ];

               }
               else

                  hklList2 = subs.FindPossibleHKLs( LatControl.BMat , q1 , q2 ,
                           hklList[ i ] , LatControl.Delta1 ,
                           LatControl.Delta2 , LatControl.Centering );

               if( hklList2 != null )

                  for( int j = 0 ; j < hklList2.length ; j++ )
                  {
                     float[][] UB = LatControl.CalcUB( q1 , subs
                              .cvrt2float( hklList[ i ] ) , q2 , subs
                              .cvrt2float( hklList2[ j ] ) );

                     if( UB != null )
                        OrMatrices.add( UB );
                  }
            }

            if( OrMatrices != null && OrMatrices.size() > 1 )
               setOrientationMatrix( OrMatrices.elementAt( 0 ) );

            showCurrentOrientationMatrices( true , true );

            return;


         }


         if( evtString == THREE_PEAK )
         {
            float[][] UB = subs.CalcUB( selectedPeaks.getSetPeakQ( 0 ) ,
                     selectedPeaks.getSetPeak_hkl( 0 ) , selectedPeaks
                              .getSetPeakQ( 1 ) , selectedPeaks
                              .getSetPeak_hkl( 1 ) , selectedPeaks
                              .getSetPeakQ( 2 ) , selectedPeaks
                              .getSetPeak_hkl( 2 ) );

            setOrientationMatrix( UB );

            showCurrentOrientationMatrix( true );

            return;
         }

      }


      private void showError( String message )
      {

         JOptionPane.showMessageDialog( null , message );
      }


      private int NsetQ()
      {

         int nPeaksSet = 0;

         for( int i = 0 ; ( i < SetPeaks.MAX_SEL_PEAKS ) && nPeaksSet == i ; i++ )
            if( selectedPeaks.getSetPeakQ( i ) != null )

               nPeaksSet++ ;

         return nPeaksSet;
      }


      private int NsetHKL()
      {

         int nPeaksSet = 0;

         for( int i = 0 ; ( i < 4 ) && nPeaksSet == i ; i++ )
            if( selectedPeaks.getSetPeak_hkl( i ) != null )

               nPeaksSet++ ;

         return nPeaksSet;
      }
   }

   // Information handlers for InfoHandler's that may need to be changed at times when a new
   // peak is set or the display or rotated
   interface IOrientInfoHandler extends InfoHandler
   {



      /* (non-Javadoc)
       * @see DataSetTools.components.ui.Peaks.InfoHandler#show(DataSetTools.operator.Generic.TOF_SCD.IPeak, gov.anl.ipns.MathTools.Geometry.Tran3D, javax.swing.JPanel)
       */
      @Override
      public void show( IPeak pk , Tran3D transformation , JPanel panel );


      /**
       * Sets in new values.  Ususally constructor data.  If the display is showing it should
       * reflect the new values
       * 
       * @param newData  The new data
       */
      public void setNewData( Object newData );
   }

   /**
    *  Displays orientation matrix information in info area and also on the 
    *  3D reciprocal space viewer
    * @author Ruth
    *
    */
   class OrientMatInfoHandler extends Thread implements IOrientInfoHandler ,
            AncestorListener , ISelectPeakHandler
   {



      String    OrientMatInfo;

      JTextArea text;

      JPanel    Panel;

      //-------SelectPeak Handler
      View3D    View;

      float[][] OrientationMatrix;


      /**
       * Constructor
       * @param OrientationMatrix  String describing orientation matrix information
       */
      public OrientMatInfoHandler( String OrientationMatrix )
      {

         OrientMatInfo = OrientationMatrix;
         text = null;
         View = null;
         OrientationMatrix = null;
      }


      /**
       * Sets orientation information for SelectPeakHandler
       * @param view                The 3D  View of peaks Q space
       * @param orientationMatrix   The orientation matrix
       * 
       */
      public void setOrientationInfo( View3D view , float[][] orientationMatrix )
      {

         View = view;
         OrientationMatrix = orientationMatrix;
      }


      /* 
       * Sets new data. Here only the String describing the orientation matrix is set 
       * (non-Javadoc)
       * @see DataSetTools.components.ui.Peaks.OrientMatrixControl.IOrientInfoHandler#setNewData(java.lang.Object)
       */
      @Override
      public void setNewData( Object newData )
      {

         if( newData != null && newData instanceof String )

            OrientMatInfo = (String) newData;

         else

            return;

         if( text == null || Panel == null ) //Not currently showing
            return;

         if( EventQueue.isDispatchThread() )
         {
            show( null , null , Panel );

         }
         else
            SwingUtilities.invokeLater( this );

      }


      /* 
       * Shows the information about the orientation matrix in the panel
       * @see DataSetTools.components.ui.Peaks.OrientMatrixControl.IOrientInfoHandler#show(DataSetTools.operator.Generic.TOF_SCD.IPeak, gov.anl.ipns.MathTools.Geometry.Tran3D, javax.swing.JPanel)
       */
      @Override
      public void show( IPeak pk , Tran3D transformation , JPanel panel )
      {

         if( panel == null )
            return;

         Panel = panel;
         if( text == null )
            text = new JTextArea( 20 , 30 );

         panel.removeAll();

         panel.setLayout( new GridLayout( 1 , 1 ) );

         text.setText( OrientMatInfo );

         panel.add( text );

         text.setToolTipText( subs.getCoordinateInformation( true ) );

         panel.validate();

         panel.repaint();
      }


      /* 
       * Just displays the peak in the 3D view of reciprocal space. It is assumed that the newData 
       * gives the changed textual information
       * (non-Javadoc)
       * @see DataSetTools.components.ui.Peaks.ISelectPeakHandler#SelectPeak(DataSetTools.operator.Generic.TOF_SCD.IPeak)
       */
      @Override
      public void SelectPeak( IPeak Peak )
      {

         if( View == null || OrientationMatrix == null )
            return;

         View.showOrientation( OrientationMatrix , Peak.seqnum() );


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

         Panel = null;
         text = null;

      }


      /**
       *   Used to call the show method if non in the swing thread
       */
      public void run()
      {

         show( null , null , Panel );
      }

   }

   /**
    *  Displays a graph of the offsets from an integer of a miller index
    * @author Ruth
    *
    */
   class MillerOffsetInfoHandler extends Thread implements IOrientInfoHandler ,
            AncestorListener
   {



      char                  indexChar;

      float[]               X;

      float[]               Y;

      VirtualArrayList1D    list;

      JPanel                Panel = null;

      FunctionViewComponent view;


      /**
       * Constructor
       * @param index   h,k,or l describing which miller index applies
       * @param x       An array of x values representing offsets from an integer
       * @param y       An array of y values related to number(tot intensity) of peaks
       *                at the offset described by x 
       */
      public MillerOffsetInfoHandler( char index, float[] x, float[] y )
      {

         indexChar = index;
         X = x;
         Y = y;
         view = null;
         list = null;
      }


      /* 
       * Used to show info if not running in Swing thread
       * (non-Javadoc)
       * @see java.lang.Thread#run()
       */
      @Override
      public void run()
      {

         show( null , null , Panel );
      }


      /* 
       * Sets new data into view.  
       * @param newData A vector with two elements, the x float array and the y float array
       * (non-Javadoc)
       * @see DataSetTools.components.ui.Peaks.OrientMatrixControl.IOrientInfoHandler#setNewData(java.lang.Object)
       */
      @Override
      public void setNewData( Object newData )
      {

         if( newData == null || ! ( newData instanceof Vector ) )
            return;

         Vector V = (Vector) newData;

         if( V.size() != 2 )
            return;

         try
         {
            X = (float[]) V.firstElement();
            Y = (float[]) V.lastElement();

         }
         catch( Exception s )
         {
            return;
         }

         if( EventQueue.isDispatchThread() )
         {
            show( null , null , Panel );


         }
         else

            SwingUtilities.invokeLater( this );

      }


      /* (non-Javadoc)
       * @see DataSetTools.components.ui.Peaks.OrientMatrixControl.IOrientInfoHandler#show(DataSetTools.operator.Generic.TOF_SCD.IPeak, gov.anl.ipns.MathTools.Geometry.Tran3D, javax.swing.JPanel)
       */
      @Override
      public void show( IPeak pk , Tran3D transformation , JPanel panel )
      {

         Panel = panel;

         if( panel == null || X == null || Y == null )
            return;

         panel.removeAll();

         panel.setLayout( new GridLayout( 1 , 1 ) );

         if( view == null || list == null )
         {
            list = new VirtualArrayList1D( new DataArray1D( X , Y ) );
            list.setTitle( indexChar + " offset from integer" );
            view = new FunctionViewComponent( list );

         }
         else
            list.setXYValues( X , Y , null , "data" , 0 );

         javax.swing.JPanel viewPanel = view.getDisplayPanel();

         viewPanel.addAncestorListener( this );

         panel.add( viewPanel );

         panel.validate();
         panel.repaint();

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

         view = null;
         list = null;


      }


   }

   /**
    * Handles showing a list of orientation matrices with options to select one as the
    * orientation matrix.
    * @author Ruth
    *
    */
   class OrientMatListHandler implements ActionListener , ChangeListener
   {



      Vector< float[][] > orMatrices;

      SetPeaks            setpks;

      boolean             selectMatrix;

      int                 selectedMatNum;

      JSpinner            spinner;

      JTextArea           text;


      /**
       * Constructor
       * @param OrMatrices  The Vector of orientation matrices
       * @param Setpks      The SetPeaks object with the set peak information
       * @param SelectMatrix  If true, an option to select one of the matrices 
       *                     will be included
       */
      public OrientMatListHandler( Vector< float[][] > OrMatrices,
               SetPeaks Setpks, boolean SelectMatrix )
      {

         orMatrices = OrMatrices;
         setpks = Setpks;
         selectMatrix = SelectMatrix;

         selectedMatNum = - 1;
         spinner = null;
         text = null;
      }


      public float[][] run()
      {

         if( orMatrices == null || orMatrices.size() < 1 )
            return null;

         JPanel jp = new JPanel();
         jp.setLayout( new BorderLayout() );

         int n = 2;
         if( selectMatrix )
            n++ ;

         JPanel jp1 = new JPanel();
         jp1.setLayout( new GridLayout( 1 , n ) );

         spinner = new JSpinner( new SpinnerNumberModel( 1 , 1 , orMatrices
                  .size() + 1 , 1 ) );
         spinner.addChangeListener( this );

         jp1.add( new JLabel( "Mat Num" ) );
         jp1.add( spinner );

         JButton button = new JButton( "Select" );

         button.addActionListener( this );

         if( n > 2 )
            jp1.add( button );

         jp.add( jp1 , BorderLayout.NORTH );

         text = new JTextArea( 15 , 45 );
         text
                  .setText( ShowMatString( true , true , orMatrices
                           .elementAt( 0 ) ) );

         jp.add(  text  , BorderLayout.CENTER );

         if( JOptionPane.showConfirmDialog( null , jp , "Orientation Matrices" ,
                  JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION )

            if( selectedMatNum >= 0 && selectedMatNum <= orMatrices.size() )
               return orMatrices.elementAt( selectedMatNum );

         return null;

      }


      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         if( spinner != null )
            selectedMatNum = ( (Integer) spinner.getValue() ).intValue() - 1;

      }


      /* (non-Javadoc)
       * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
       */
      @Override
      public void stateChanged( ChangeEvent e )
      {

         if( spinner == null || text == null )
            return;

         int MatNum = ( (Integer) spinner.getValue() ).intValue() - 1;

         if( MatNum < 0 || MatNum >= orMatrices.size() )
            return;

         text.setText( ShowMatString( true , true , orMatrices
                  .elementAt( MatNum ) ) );


      }

   }
}
