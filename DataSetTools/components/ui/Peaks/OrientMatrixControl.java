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
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */

package DataSetTools.components.ui.Peaks;

import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.Util.File.FileIO;
import gov.anl.ipns.Util.File.RobustFileFilter;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.OneD.DataArray1D;
import gov.anl.ipns.ViewTools.Components.OneD.FunctionViewComponent;
import gov.anl.ipns.ViewTools.Components.OneD.VirtualArrayList1D;
//import gov.anl.ipns.ViewTools.Components.*;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
//import javax.swing.filechooser.FileFilter;

import DataSetTools.operator.Generic.TOF_SCD.GetUB;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;
import DataSetTools.util.SharedData;
//import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import IPNSSrc.blind;
import Operators.TOF_SCD.ScalarJ_base;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.Vector;


/**
 *  Handles orientation matrix calculations, entry, saving etc.
 *  
 * @author Ruth
 *
 */
public class OrientMatrixControl extends JButton
{



   /**
    * Text on JButton and command to display Orient matrix operations.
    */
   public static String ORIENT_MAT              = "Orientation Matrix";

   //--------- sub menu options & command names of ORIENT_MAT
   /**
    * Menu option and command name for loading an orientation matrix
    */
   public static String LOAD_ORIENT1            = "*Load Orientation matrix";

   /**
    * Menu option and command name for Entering an orientation matrix
    */
   public static String ENTER_ORIENT1           = "Enter Orientation matrix"; 

   /**
    * Menu option and command name for saving an orientation matrix
    */
   public static String SAVE_ORIENT1            = "Save Orientation matrix";

   
   /**
    * Menu option and command name for showing a set of orientation matrices
    */
   public static String SHOW_SEL_MAT            = "Show/Select Matrix from Set";
 
   //---- ADJUST_OR_MAT isNot a command name has sub menu options
   public static String ADJUST_OR_MAT           ="*Adjust Orientation Matrix";

   //------------------------------------------------------
   public static String VIEWS                   = "View in QViewer";

   //---------- sub menu options & command names for VIEW ---------------

   
   /**
    * Menu option and command name to View Orientation matrix in 3D View"
    */
   public static String VIEW_ORIENT             = "View Orientation matrix";

   
   /**
    * Menu option and command name to View Plane Family a*b* in 3D View"
    */
   public static String VIEW_PLANEab            = "View Plane Family a*b*";

   
   /**
    * Menu option and command name to View Plane Family a*c* in 3D View"
    */
   public static String VIEW_PLANEac            = "View Plane Family a*c*";

   /**
    * Menu option and command name to View Plane Family b *c* in 3D View"
    */
   public static String VIEW_PLANEbc            = "View Plane Family b*c*";

   /**
    * Menu option and command name to View Predicted Peaks in 3D View"
    */
   public static String  VIEW_PRED_PEAKS        = "View Predicted Peaks";

   //---------------------------------------------------------
   public static String CALC_ORIENT             = "*Calculate Orientation matrix(s)";

   //--------------- sub menu options and command names for CALC_ORIENT ---------------

   /**
    * Menu option and command name Calculate the Orientation Matrix using 
    * Blind"
    */
   public static String BLIND                   = "Blind";

   /**
    * Menu option and command name Calculate the Orientation Matrix using 
    * an Automatic method
    */
   public static String AUTOMATIC               = "Automatic";

   /**
    * Menu option and command name Calculate the Orientation Matrix From
    *  4 Peaks
    */
   public static String FOUR_PEAK               = "From 4 Peaks";

   /**
    * Menu option and command name Calculate the Orientation Matrix From
    * 2 Peaks with hkls
    */
   public static String TWO_PEAK                = "From 2 Peaks with hkls";

   /**
    * Menu option and command name Calculate the Orientation Matrix From
    *  3 Peaks with hkls
    */
   public static String THREE_PEAK              = "From 3 Peaks with hkls";
   
//----------------- InfoHandler Type names --------------------
 /**
  * Key that this class uses to add the view of the orientation matrix
  * in InfoHandler.addInfo method
  */ 
 public static String  SHOW_ORIENT_MAT  =  "Orientation Matrix.matrix";
 
 /**
  * Key that this class uses to add the view of the offsets from an
  * integer for h in InfoHandler.addInfo method
  */ 
 public static String  SHOW_H_OFFSET  =   "Orientation Matrix.h offset";

 /**
  * Key that this class uses to add the view of the offsets from an
  * integer for k in InfoHandler.addInfo method
  */ 
 public static String  SHOW_K_OFFSET = "Orientation Matrix.k offset";

 /**
  * Key that this class uses to add the view of the offsets from an
  * integer for l in InfoHandler.addInfo method
  */ 
 public static String  SHOW_L_OFFSET  = "Orientation Matrix.l offset";
   //------------------------------------------
   int                  NMillerOffsetBins       = 50;

   //Initial Data
   WeakReference<View3D>               View;

   WeakReference<Vector< IPeak >>      WPeaks;

   //Other controls
   WeakReference<View3DControl>        V3DControl;

   WeakReference<Info>                 textInfo;

   //Calculated variables
   boolean[]            omittedPeakIndex;

   float[][]            orientationMatrix       = null;

   float[][]            TranspOrientationMatrix = null;

   Vector< float[][] >  OrMatrices;

   float[]              err                     = null;
   
   float                Dmin                    =-1;
   float                Dmax                    =-1;

   //View orientation mat in 3D view
   boolean              showLatinOrientMenu;


   //Information Handlers 
   IOrientInfoHandler   OrientMatInfHandler , 
                        h_offsetInfHandler ,
                        k_offsetInfHandler , 
                        l_offsetInfHandler;

   // Other input objects
   WeakReference<PeakFilterer>         peakFilter;

   WeakReference<SetPeaks>             selectedPeaks;

   WeakReference<XtalLatticeControl>   LatControl;

   //Utilities 
   MyActionListener     Listener;

   JPopupMenu           Menu;

   String               InputFileName = null;//
   
   Vector<WeakReference<ActionListener>>  OrientControlListeners;
   
   //-------------- Notification Messages --------------------
   
   public static String  INPUT_FILE_CHANGE  ="Input File Has Changed";
   
   public static String  ORIENT_MATRIX_CHANGED = 
                                     "Orientation Matrix has Changed";
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
      WPeaks = new WeakReference<Vector< IPeak >> (peaks);
      textInfo = new WeakReference<Info>(TextInf);

      setToolTipText("<html><body>*Only First two and last items<br>"+
               "can create an orientation matrix</body></html)");
      omittedPeakIndex = null;
      View = new WeakReference<View3D>(view);
      V3DControl = new WeakReference<View3DControl>(v3DControl);

      Listener = new MyActionListener( this );
      Menu = new JPopupMenu( ORIENT_MAT );
      MakePopUpMenu();

      addActionListener( Listener );

      peakFilter = null;
      selectedPeaks = null;
      LatControl = null;

      showLatinOrientMenu = false;
      OmittedMenuItems =";";
      
      OrMatrices = new Vector< float[][] >();
      
      OrientControlListeners = new Vector<WeakReference<ActionListener>>();

   }

   public void kill()
   {
      OrientControlListeners.clear();
      OrientControlListeners = null;
      removeActionListener( Listener );
      WPeaks = null;
      OrMatrices.clear();
      OrMatrices = null;
      orientationMatrix = null;
      View = null;
      TranspOrientationMatrix = null;
      if( OrientMatInfHandler != null)
      {
         if( textInfo != null && textInfo.get()!= null)
            textInfo.get().removeInfoHandler( OrientMatrixControl.SHOW_ORIENT_MAT );
         OrientMatInfHandler.kill();
      
      }
      if( h_offsetInfHandler != null)
      {
         h_offsetInfHandler.kill();
      }
      if( k_offsetInfHandler != null)
      {
         k_offsetInfHandler.kill();
      }
      if( l_offsetInfHandler != null)
         l_offsetInfHandler.kill();
      OrientMatInfHandler = h_offsetInfHandler = k_offsetInfHandler =
            l_offsetInfHandler = null;
      OrientMatInfHandler = h_offsetInfHandler = l_offsetInfHandler =
              k_offsetInfHandler;
      textInfo.get().removeInfoHandler( SHOW_H_OFFSET );

      textInfo.get().removeInfoHandler( SHOW_K_OFFSET );
      textInfo.get().removeInfoHandler( SHOW_L_OFFSET );
      h_offsetInfHandler = k_offsetInfHandler =l_offsetInfHandler = null;
      textInfo = null;
      if( peakFilter.get() != null)
          peakFilter.get().removeFilterListener( Listener );
      peakFilter = null;
      
      selectedPeaks = null;
      LatControl = null;
      Listener.kill();
      Listener = null;
      Menu = null;
      if( OrientControlListeners != null)
          OrientControlListeners.clear();
      OrientControlListeners = null;
      V3DControl = null;
      
      
   }
   private void fireOrientationMatrixListeners( String message)
   {
      for( int i=0; i< OrientControlListeners.size(); i++)
         if( OrientControlListeners.elementAt( i ).get() != null)
         (OrientControlListeners.elementAt( i )).get().actionPerformed(  
                  new ActionEvent(this, ActionEvent.ACTION_PERFORMED, message));
   }
   
   /**
    * Use this to perform operations on an instance of this class without a
    * GUI or using an alternative GUI
    * 
    * @return  The action listener for this  class
    * 
    * NOTE: to perform operations call this ActionListener's actionPerformed 
    *   method with command name String listed as a field above. 
    */
   public ActionListener  getActionListener()
   {
      return Listener;
   }
   
   
   public void addOrientationMatrixListener( ActionListener evt)
   {
      if( evt == null)
         return;
      
     for( int i=0; i< OrientControlListeners.size(); i++)
     {
        ActionListener E = OrientControlListeners.elementAt( i ).get();
        if( E != null && (E == evt))
           return;
     }
      
      OrientControlListeners.add( new WeakReference<ActionListener>(evt));
   }
   
   
   
   public void removeOrientationMatrixListener( ActionListener evt)
   {
      if( evt == null)
         return;
      
      for( int i=0; i< OrientControlListeners.size(); i++)
      {
         ActionListener E = OrientControlListeners.elementAt( i ).get();
         if( E != null && (E == evt))
         {
            OrientControlListeners.remove( i);
            return;
         }
      }
      
      
   }
   public void removeAllOrientationMatrixListener( )
   {
      
      OrientControlListeners.clear();
   }
   
   /**
    * Returns the name of the last file used to load the orientation matrix
    * or null if the orientation matrix was found some other way
    * 
    * @return  The filename with the current orientation matrix
    */
   public String getInputFileName()
   {
      return InputFileName;
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
      Vector<IPeak>Peaks = null;
      Peaks = WPeaks.get();
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
            if( textInfo != null && textInfo.get() != null )
            {
               OrientMatInfHandler = new OrientMatInfoHandler( res );

               textInfo.get().addInfoHandler( SHOW_ORIENT_MAT ,
                        OrientMatInfHandler );

               h_offsetInfHandler = new MillerOffsetInfoHandler( 'h' , xvals ,
                        hyvals );

               k_offsetInfHandler = new MillerOffsetInfoHandler( 'k' , xvals ,
                        kyvals );

               l_offsetInfHandler = new MillerOffsetInfoHandler( 'l' , xvals ,
                        lyvals );

               textInfo.get().addInfoHandler( SHOW_H_OFFSET , h_offsetInfHandler );

               textInfo.get().addInfoHandler( SHOW_K_OFFSET , k_offsetInfHandler );

               textInfo.get().addInfoHandler( SHOW_L_OFFSET , l_offsetInfHandler );

            }
         }
      else if( OrientMat == null ) //remove items from menus
      {
         textInfo.get().removeInfoHandler( "Orientation Matrix.matrix" );

         textInfo.get().removeInfoHandler( "Orientation Matrix.h offset" );

         textInfo.get().removeInfoHandler( "Orientation Matrix.k offset" );

         textInfo.get().removeInfoHandler( "Orientation Matrix.l offset" );

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
      

      fireOrientationMatrixListeners( ORIENT_MATRIX_CHANGED);
      
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

      if( matNum < 0 && orientationMatrix != null  )
         return gov.anl.ipns.MathTools.LinearAlgebra.copy( orientationMatrix );

      return null;
   }


   String               OmittedMenuItems;
   public void ManageShownMenus( String MenuItem, boolean show)
   {
      if( MenuItem == null)
         return;
      
      if( show)
      {
         int i= OmittedMenuItems.indexOf( ";"+MenuItem+";" );
         if( i < 0)
            return;
         OmittedMenuItems = OmittedMenuItems.substring( 0,i+1 )+
                OmittedMenuItems.substring( i+MenuItem.length()+2 );
         return;
      }
      int i= OmittedMenuItems.indexOf( ";"+MenuItem+";" );
      if(i>=0)
         return;
      
      OmittedMenuItems +=MenuItem+";";
      
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

      this.LatControl = new WeakReference<XtalLatticeControl>(LatControl);
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

      selectedPeaks = new WeakReference<SetPeaks>(SelPeaks);
   }


   /**
    * Sets the PeakFilter object that is used to omit peaks
    * @param pkFilt  The PeakFilterer object
    */
   public void setPeakFilterer( PeakFilterer pkFilt )
   {

      this.peakFilter = new WeakReference<PeakFilterer>(pkFilt);
      pkFilt.addFilterListener( Listener );

   }

   /**
    * Dmin and Dmax may be used for some auto indexing routines
    * 
    * @param Dmin   The new Dmin or -1 if undefined
    * @param Dmax   The new Dmax or -1 if undefined
    */
   public void setDMin_Max( float Dmin, float Dmax)
   {
      this.Dmin = Dmin;
      this.Dmax = Dmax;
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

         View.get().IncludeSeqNums( SeqsInt );
      }

      omittedPeakIndex = new boolean[ WPeaks.get().size() ];

      java.util.Arrays.fill( omittedPeakIndex , false );

      if( seqNums != null && omittedPeakIndex != null )

         for( int i = 0 ; i < seqNums.length && seqNums[i]-1<omittedPeakIndex.length; i++ )
            omittedPeakIndex[ seqNums[ i ] - 1 ] = true;

      else
         omittedPeakIndex = null;

      View.get().omitSeqNums( seqNums );

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

      int[] list = new int[ WPeaks.get().size() ];

      int k = 0;
      
      for( int i = 0 ; i < omittedPeakIndex.length ; i++ )

         if( omittedPeakIndex[ i ] == omitted)

            list[ k++ ] = i + 1;
      
      for( int j= omittedPeakIndex.length; j< WPeaks.get().size(); j++)
         
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
      InputFileName = filename;
      fireOrientationMatrixListeners( INPUT_FILE_CHANGE);
   }


   /**
    * Shows information about the current orientation matrix in an option 
    * dialog 
    * 
    * @param WithSelectPoint  Include information about the set peaks
    */
   public void showCurrentOrientationMatrix( boolean WithSelectPoint )
   {

      JOptionPane.showMessageDialog( null , ShowMatString( 
               orientationMatrix,WPeaks.get(), omittedPeakIndex, selectedPeaks.get() ) );
   }

   /**
    * Shows information on a list of orientation matrices and allows for 
    * selecting one of the options
    * 
    * @param Peaks        The list of peaks
    * @param OrMatrices   A Vector of orientation matrices
    *
    * @return A 3x3 array containing the selected orientation matrix.
    */
   public static float[][] showCurrentOrientationMatrices( Vector<IPeak>Peaks,
            Vector<float[][]> OrMatrices)
   {
      SetPeaks Setpks = null;
      boolean SelectMatrix = true;
      OrientMatListHandler handler = new OrientMatListHandler( OrMatrices , Setpks ,
               Peaks, SelectMatrix );
      float[][] M = ( handler ).run();
      return M;
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
         Setpks = selectedPeaks.get();

      float[][] M = ( new OrientMatListHandler( OrMatrices , Setpks ,
               WPeaks.get(), SelectMatrix ) ).run();
      
      this.InputFileName = null;
      
      if( M != null )
         setOrientationMatrix( M );

   }


   /**
    * Creates a String( text/plain) that presents information on one 
    * orientation matrix. More information is given with the more information
    * that is given.
    * 
  
    * @param UB                      The orientation matrix
    * @param peaks                   The Vector of Peaks or null to not show
    *                                   h,k,l integer offsets at various levels
    * @param omittedPeakIndex        the omitted peaks
    * @param selectedPeaks           The selected peaks or null to not show
    *                                   information on these peaks      
    * @return A String giving the orientation matrix, lattice parameters
    *         and statistics about the quality of the indexing.
    */
  public static String ShowMatString( float[][] UB, Vector<IPeak> peaks,
            boolean[] omittedPeakIndex, SetPeaks selectedPeaks)
   {

      String Text1;
      Vector< IPeak > pks = null;
      boolean WithSelectPoints = true;
      boolean WithPeaksInfo = true;
      if( WithPeaksInfo )
         if( peaks != null)
            pks = peaks;
         else 
            WithPeaksInfo = false;
      
      if( selectedPeaks == null || selectedPeaks.getSetPeakQ( 0 )== null)
         WithSelectPoints = false;

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
      if( selectedPeaks != null)
      for( int i = 0 ; i < peaks.size() ; i++ )
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

      ( Menu.add( CALC_ORIENT ) ).addActionListener( Listener );
      
      //( Menu.add( ENTER_ORIENT1 ) ).addActionListener( Listener );
       Menu.addSeparator();
      if( showLatinOrientMenu && LatControl != null )
      {

         ( Menu.add( XtalLatticeControl.CRYSTAL_LAT_INPUT_TEXT ) )
                  .addActionListener( LatControl.get().getListener() );
      }

      ( Menu.add( SAVE_ORIENT1 ) ).addActionListener( Listener );
      
      (Menu.add(SHOW_SEL_MAT )).addActionListener( Listener );
      

      ( Menu.add( VIEWS ) ).addActionListener( Listener );

       Menu.addSeparator();
      ( Menu.add( ADJUST_OR_MAT ) ).addActionListener( Listener );
      



   }


   // updates OrientMatInfHandler when orientation matrix changes
   private void updateInf_OrientMat( int[] seqNums )
   {

      if( OrientMatInfHandler == null )
         return;

      String res = subs.ShowOrientationInfo( WPeaks.get() , orientationMatrix ,
               omittedPeakIndex , err , false );

      OrientMatInfHandler.setNewData( res );

   }


   // updates the h,k,l_offsetHandlers when the orientation matrix changes
   // NOTE seqNums not used. omittedPeakIndex is used
   private void updateInf_hkl_Offset( int[] seqNums )
   {

      float[] xvals = new float[ NMillerOffsetBins + 1 ];
      Vector<IPeak>Peaks = null;
      if( WPeaks != null )
         Peaks = WPeaks.get();
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



      Component           button;

      String           lastFileName;

      CalculateListener CalcListener = new CalculateListener();

      int               planeNum     = - 1;
      
      public   String   SelView3DItem = "None";
      
      JMenuItem  None3DView;

      float[]  zero = new float[]{0f,0f,0f};
      
      
      WeakReference<OrientMatrixControl> WorMat;
      
      public MyActionListener( JButton but )
      {
         WorMat = new WeakReference<OrientMatrixControl>((OrientMatrixControl) but);
         button = but;
         lastFileName = System.getProperty( "ISAW_HOME" );
         None3DView= null;
      }
      
      public void kill()
      {
        // button = null;
         CalcListener = null;
         zero = null;
         None3DView = null;
         lastFileName = null;
         WorMat = null;         
         
      }

      /**
       * The action commands that are responded to are
       * 
       * 
       */
      public void do3DView(  )
      {
         OrientMatrixControl orMat = WorMat.get();
         if( !SelView3DItem.equals( OrientMatrixControl.VIEW_PRED_PEAKS ))
            orMat.View.get().showOrientPeaks( null);
         
         if( SelView3DItem.equals(  "None" ) ) 
         {  
            orMat.View.get().showPlanes( null , null , null , null );
            
         }else if( SelView3DItem == OrientMatrixControl.VIEW_ORIENT)
         {

            orMat.View.get().showOrientation( orMat.orientationMatrix , orMat.View.get().getLastSelectedSeqNum() );
            ( (OrientMatInfoHandler) orMat.OrientMatInfHandler ).setOrientationInfo(
                     orMat.View.get() , orMat.orientationMatrix );
            orMat.V3DControl.get().addSelectPeakHandler((ISelectPeakHandler) orMat.OrientMatInfHandler );

         }else if(  SelView3DItem == OrientMatrixControl.VIEW_PLANEab)
         {

            orMat.View.get().showPlanes( zero , orMat.TranspOrientationMatrix[ 0 ] ,
                     orMat.TranspOrientationMatrix[ 1 ] ,
                     orMat.TranspOrientationMatrix[ 2 ] );

         }else if(  SelView3DItem == OrientMatrixControl.VIEW_PLANEac)
         {
            orMat.View.get().showPlanes( zero , orMat.TranspOrientationMatrix[ 0 ] ,
                     orMat.TranspOrientationMatrix[ 2 ] ,
                     orMat.TranspOrientationMatrix[ 1 ] );

            
         }else if(  SelView3DItem == OrientMatrixControl.VIEW_PLANEbc)
         {

            orMat.View.get().showPlanes( zero , orMat.TranspOrientationMatrix[ 1 ] ,
                     orMat.TranspOrientationMatrix[ 2 ] ,
                     orMat.TranspOrientationMatrix[ 0 ] );

         }else if( SelView3DItem == OrientMatrixControl.VIEW_PRED_PEAKS)
         {
            orMat.View.get().showOrientPeaks( orMat.orientationMatrix);
         }

 
         
      }
      
      private Component GetVisible( Component kid)
      {
         
         if( kid.isShowing())
            return kid;
         if( kid.getParent() != null)
            return GetVisible( kid.getParent());
         else
            return kid;
      }
      /*
       * Executes the operation specified by the event's action command.
       * See
       *  (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

          OrientMatrixControl orMat = WorMat.get();
         
          String evt = e.getActionCommand();
         
         
         
         if( evt == OrientMatrixControl.ORIENT_MAT )
         {
            button =GetVisible( (Component)e.getSource());
            orMat.Menu.show( button , button .getWidth()*3/4 ,button.getHeight()/2);
            return;
         }
         if( evt == OrientMatrixControl.LOAD_ORIENT1 )
         {

            JFileChooser jf = new JFileChooser( lastFileName );
            RobustFileFilter F = ( new RobustFileFilter() );
            F.addExtension( "mat" );
            jf.setFileFilter( F );

            if( jf.showOpenDialog( null ) != JFileChooser.APPROVE_OPTION )
               return;

            String filename = jf.getSelectedFile().getAbsolutePath();

            lastFileName = filename;

            orMat.LoadOrientMatrix( filename );

            return;
         }

         if( evt == OrientMatrixControl.ENTER_ORIENT1 ) 
         {

            return; 
         }

         if( evt == OrientMatrixControl.SAVE_ORIENT1 )
         {
            if( orMat.orientationMatrix == null)
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
                     filename , orMat.orientationMatrix );
            if( Res != null)
               JOptionPane.showMessageDialog(null, Res);

            return;
            
            
         }if( evt ==  OrientMatrixControl.SHOW_SEL_MAT )
         {
            orMat.showCurrentOrientationMatrices( true, true );
            
            return; 
         }  
         
         
         if( evt ==  OrientMatrixControl.ADJUST_OR_MAT )
         {
              JPopupMenu pop = new JPopupMenu("Adjust Orient Matrix");
              (pop.add( "Index Peaks" )).addActionListener(this);
              (pop.add( "Niggli with Blind" )).addActionListener( this);
              (pop.add( "Niggli(experimental" )).addActionListener( this);
              (pop.add( "Show conventional cells" )).addActionListener( this);
              
              (pop.add( "Optimize" )).addActionListener( this );
              if( e.getSource() instanceof AbstractButton)
                   pop.show( button, button.getWidth()*3/4,button.getHeight()/2);
              else
                 pop.show( null, 100,100);
              

               return; 
         } 
        
         
         if( evt == OrientMatrixControl.VIEWS )
         {
            if( orMat.orientationMatrix == null )
               return;

            JPopupMenu pop = new JPopupMenu();
            ButtonGroup grp = new ButtonGroup();

            JMenuItem men = pop.add( new JCheckBoxMenuItem( OrientMatrixControl.VIEW_ORIENT ,planeNum == 0) );
            men.addActionListener( this );
            grp.add( men );

            men = pop
                .add( new JCheckBoxMenuItem( OrientMatrixControl.VIEW_PLANEab , planeNum == 1 ) );
            men.addActionListener( this );
            grp.add( men );

            men = pop
                 .add( new JCheckBoxMenuItem( OrientMatrixControl.VIEW_PLANEac , planeNum == 2 ) );
            men.addActionListener( this );
            grp.add( men );

            men = pop
                 .add( new JCheckBoxMenuItem( OrientMatrixControl.VIEW_PLANEbc , planeNum == 3 ) );
            men.addActionListener( this );
            grp.add( men );


            men = pop
               .add( new JCheckBoxMenuItem(OrientMatrixControl.VIEW_PRED_PEAKS , planeNum == 4 ) );
            men.addActionListener( this );
            grp.add( men );

            men = pop.add( new JCheckBoxMenuItem( "None" ,planeNum < 0) );
            men.addActionListener( this );
            grp.add( men );
   
            None3DView = men;
            pop.show( button , 0 , 0 );
            return;
         }

         if( evt != OrientMatrixControl.VIEW_ORIENT)
         {
            orMat.V3DControl.get().removeSelectPeakHandler( 
                     (ISelectPeakHandler )orMat.OrientMatInfHandler);
            orMat.View.get().showOrientation( null , -1 );
         }
         if( evt == OrientMatrixControl.VIEW_ORIENT )
         {
            boolean isSelected = true;
            if( planeNum ==0)
               isSelected = false;
            JCheckBoxMenuItem men = (JCheckBoxMenuItem) e.getSource();
            if( orMat.orientationMatrix == null || orMat.View == null )
            {
               men.setState( false );
               planeNum = -1;
               return;
            }
           
            float[][] mat = null;

            if(isSelected )
               mat = orMat.orientationMatrix;
            
            if( mat != null )
            {
               SelView3DItem = OrientMatrixControl.VIEW_ORIENT;
               ((OrientMatInfoHandler) orMat.OrientMatInfHandler ).setOrientationInfo(
                        orMat.View.get() , orMat.orientationMatrix );
               orMat.V3DControl.get().addSelectPeakHandler( 
                        (ISelectPeakHandler )orMat.OrientMatInfHandler);
            }
            else
            {
               SelView3DItem = "None";
               orMat.V3DControl.get().removeSelectPeakHandler( 
                        (ISelectPeakHandler )orMat.OrientMatInfHandler);
               
            }

            orMat.View.get().showOrientation( mat , orMat.View.get().getLastSelectedSeqNum() );
            orMat.View.get().showOrientPeaks( null );
            if( mat == null)
            {
               planeNum =-1;
               None3DView.setSelected(true);
            }
            else planeNum =0;

            return;

         }

         boolean isSelected = false;

         if( e.getSource() instanceof JCheckBoxMenuItem )
         {
            isSelected = ( (JCheckBoxMenuItem) e.getSource() ).isSelected();
          
         }

         float[] zero =
         {
                  0f , 0f , 0f
         };

         if( evt == OrientMatrixControl.VIEW_PLANEab )
         {
            if( planeNum == 1)
               isSelected =false;
            
            if( isSelected )

               orMat.View.get().showPlanes( zero , orMat.TranspOrientationMatrix[ 0 ] ,
                        orMat.TranspOrientationMatrix[ 1 ] ,
                        orMat.TranspOrientationMatrix[ 2 ] );

            else

               orMat.View.get().showPlanes( null , null , null , null );
            

            orMat.View.get().showOrientPeaks( null );

            planeNum = 1;
            
            if( isSelected )
               SelView3DItem = OrientMatrixControl.VIEW_PLANEab;
            else
            {
               SelView3DItem="None";
               planeNum = -1;
               None3DView.setSelected( true );
            }
            
            return;

         }

         if( evt == OrientMatrixControl.VIEW_PLANEac )
         {

            if( planeNum == 2)
               isSelected = false;
            if( isSelected )

               orMat.View.get().showPlanes( zero ,orMat.TranspOrientationMatrix[ 0 ] ,
                        orMat.TranspOrientationMatrix[ 2 ] ,
                        orMat.TranspOrientationMatrix[ 1 ] );

            else

               orMat.View.get().showPlanes( null , null , null , null );

            orMat.View.get().showOrientPeaks( null );
            if( isSelected)
               planeNum = 2;
            else 
            {
               planeNum = -1;

               None3DView.setSelected( true );
            }

            return;

         }

         if( evt == OrientMatrixControl.VIEW_PLANEbc )
         {
            if( planeNum == 3)
               isSelected = false;
            
            if( isSelected )


               orMat.View.get().showPlanes( zero , orMat.TranspOrientationMatrix[ 1 ] ,
                        orMat.TranspOrientationMatrix[ 2 ] ,
                        orMat.TranspOrientationMatrix[ 0 ] );

            else

               orMat.View.get().showPlanes( null , null , null , null );

            orMat.View.get().showOrientPeaks( null );
            planeNum = 3;

            
            if( isSelected )
               SelView3DItem = OrientMatrixControl.VIEW_PLANEbc;
            else
            {

               SelView3DItem = "None"; 
               planeNum = -1;

               None3DView.setSelected( true );
            }
            
            return;

         }
         if( evt == OrientMatrixControl.VIEW_PRED_PEAKS)
         {
            if( planeNum == 4)
               isSelected = false;
               
            planeNum =4;
            float[][] O = orMat.orientationMatrix;
            if( !isSelected)
                O= null;
            orMat.View.get().showOrientPeaks( O);
            if( O == null){
               planeNum = -1;

               None3DView.setSelected( true );
            }
         }

         if( evt.equals( "None" ) )
         {
            orMat.View.get().showPlanes( null , null , null , null );

            orMat.View.get().showOrientPeaks( null );
            planeNum = -1;
            SelView3DItem = "None";
            None3DView.setSelected( true );
         }

         if( evt == OrientMatrixControl.CALC_ORIENT )
         {
            if( CalcListener == null )
               CalcListener = new CalculateListener();

            CalcListener.MakeMenus( button );

            return;

         }


         if( evt == PeakFilterer.OMITTED_PEAKS_CHANGED )
         {
            if(orMat.peakFilter==null ||  orMat.peakFilter.get() == null)
               return;
            int[] omitted = orMat.peakFilter.get().getOmittedSequenceNumbers();

            orMat.SetOmittedPeaks( omitted );

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
            Res =( new  Operators.TOF_SCD.IndexJ_base( orMat.WPeaks.get(), orMat.orientationMatrix,
                     "", Float.parseFloat( Data[0].trim() ), Float.parseFloat( Data[1].trim() ),
                     Float.parseFloat( Data[2].trim() ))).getResult();
             if( !(Res instanceof ErrorString))
             {
                gov.anl.ipns.Util.Sys.SharedMessages.addmsg(   Res );
                if( orMat.peakFilter != null && orMat.peakFilter.get() != null )
                   orMat.peakFilter.get().set_hklMinMax();
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
            if( orMat.orientationMatrix == null)
            {
               gov.anl.ipns.Util.Sys.SharedMessages.addmsg(  "There is no orientation matrix");
               return;
            }
            blind Blind = new blind();
            Object Res = Blind.blaue( orMat.orientationMatrix);
            if( Res != null)
            {
               JOptionPane.showMessageDialog( null, "Error=="+Res );
               return;
            }
            orMat.setOrientationMatrix( LinearAlgebra.double2float( Blind.UB ));
            return;
         }

         if( evt.equals("Niggli(experimental" ))
         {
            orMat.setOrientationMatrix( subs.Nigglify( orMat.orientationMatrix ));
            return;
         }

         if( evt .equals("Optimize" ))
         {
            String filename =FileIO.appendPath( System.getProperty( "user.home"),"ISAW/tmp" );
            filename +="Lsqrs.mat";
            
            Object Res = Operators.TOF_SCD.LsqrsJ_base.LsqrsJ1(  orMat.WPeaks.get(),null, 
                     orMat.getOmittedSeqNums( false),  null,filename, 0,null,"triclinic");
            if( Res != null  && (Res instanceof ErrorString))
            {
               JOptionPane.showMessageDialog( null , "Error Least Squares "+ Res );
               return;
            }
            
            orMat.LoadOrientMatrix( filename );

            if( orMat.OrientMatInfHandler != null)
               orMat.OrientMatInfHandler.setNewData( subs.GetPeakFitInfo( orMat.WPeaks.get() ,
                        orMat.orientationMatrix , orMat.omittedPeakIndex ) );
            return;
         }
         if( evt.equals( "Show conventional cells" ))
         {
            ScalarJ_base Scalar = new ScalarJ_base( orMat.orientationMatrix,.1f,0);
            if(Scalar.getResult( ) instanceof ErrorString)
               SharedData.addmsg( "Error in Scalar: "+Scalar.getResult() );
            else
            {
                JTextArea info = new JTextArea(Scalar.getLogInfo().toString());
                FinishJFrame jf = new FinishJFrame("Conventional Cells");
                jf.getContentPane( ).add(new JScrollPane(info));
                jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
                jf.setSize( 500,800 );
                WindowShower.show( jf );
            }
            return;
         }
         if( evt.equals( OrientMatrixControl.BLIND ) || 
             evt .equals( OrientMatrixControl.AUTOMATIC )||
             evt.equals( OrientMatrixControl.FOUR_PEAK )||
             evt.equals( OrientMatrixControl.THREE_PEAK )||
             evt.equals( OrientMatrixControl.FOUR_PEAK ))
            CalcListener.actionPerformed(  e );
         

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
      public void MakeMenus( Component comp )
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

         int x = 0;
         int y = 0;
         
         if( comp != null)
         {
            x = (int)(comp.getWidth()*.8);
            y = (int)(comp.getHeight()*.8);
         }
         
         pop.show( comp , x , y );

      }


      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         String evtString = e.getActionCommand();
         String filename = InputFileName;
         InputFileName = null;
         if( evtString == BLIND || evtString == FOUR_PEAK )
         {
            blind BLIND = new blind();
            int nPeaksSet = NsetQ();
            
            if( nPeaksSet <4)
               return;
            
            if( evtString == FOUR_PEAK && nPeaksSet > 4 )
               nPeaksSet = 4;

            double[] xx = new double[ nPeaksSet + 3 ];
            double[] yy = new double[ nPeaksSet + 3 ];
            double[] zz = new double[ nPeaksSet + 3 ];
            if( selectedPeaks != null && selectedPeaks.get() != null)
            for( int i = 0 ; i < nPeaksSet ; i++ )
            {
               float[] f = selectedPeaks.get().getSetPeakQ( i );
               
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
          if(LatControl != null && LatControl.get() != null && LatControl.get().XtalParams != null)
          {
             float[] Params = LatControl.get().XtalParams;
             float[][]UB= null;
             try
             {
                Vector<IPeak> peaks = new Vector<IPeak>();
                for( int i=0;i< WPeaks.get().size();i++)
                {
                   if( omittedPeakIndex == null || omittedPeakIndex.length <i
                            || !omittedPeakIndex[i])
                      peaks.add( WPeaks.get().elementAt( i ));
                }
                
                float initial_tolerance = 0.12f;
                float required_fraction = 0.4f;
                int   fixed_peak_index  = 0;
                UB = Operators.TOF_SCD.IndexPeaks_Calc.             
                    IndexPeaksWithOptimizer( peaks , 
                          Params[0] , Params[1], Params[2] , 
                          Params[3] , Params[4] ,Params[5], 
                          initial_tolerance, required_fraction, fixed_peak_index );
                setOrientationMatrix(UB );
                showCurrentOrientationMatrix( false);
                
             }catch( Exception s)
             {
                gov.anl.ipns.Util.Sys.SharedMessages.addmsg( 
                         "Could not find matrix "+s );
                return;
             }
                            
             return;
          }else
         {
            
            float MaxXtalLengthReal = Dmax;
            if( Dmin < 0 || Dmax < 0 )
            {
               String S = JOptionPane.showInputDialog( "Enter min D and MaxD" ,
                        "3.1,12.5" );

               if( S == null )
                  return;


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
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            GetUB.DMIN = Dmin;
            GetUB.ELIM_EQ_CRYSTAL_PARAMS = false;
            OrMatrices = GetUB. getAllOrientationMatrices( WPeaks.get() , omittedPeakIndex ,
                     .01f , MaxXtalLengthReal );
            
            GetUB.DMIN = 1f;
            
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if( OrMatrices == null || OrMatrices.size() < 1 )
            {
               DataSetTools.util.SharedData.addmsg( "No orientation methods found" );
               return;
            }

            setOrientationMatrix( OrMatrices.elementAt( 0 ) );

            showCurrentOrientationMatrices( true , true );

            return;

         }


         if( evtString == TWO_PEAK )//Fix to return an array of orientation matrices
         {
            if( LatControl == null || selectedPeaks == null )
               return;

            OrMatrices.clear();
            if( selectedPeaks == null || selectedPeaks.get() == null)
               return;
            float[] q1 = selectedPeaks.get().getSetPeakQ( 0 );
            float[] q2 = selectedPeaks.get().getSetPeakQ( 1 );

            if( q1 == null || q2 == null )
               return;

            int[][] hklList;

            boolean peak1HKLset = false;

            if( selectedPeaks.get().getSetPeak_hkl( 0 ) != null )
            {
               hklList = new int[ 1 ][ 3 ];
               float[] F = selectedPeaks.get().getSetPeak_hkl( 0 );

               for( int i = 0 ; i < 3 ; i++ )
                  hklList[ 0 ][ i ] = (int) F[ i ];

               peak1HKLset = true;

            }
            else
            {
               hklList = subs.FindPossibleHKLs( LatControl.get().BMat , q1 ,
                        LatControl.get().Delta1 , LatControl.get().Delta2 ,
                        LatControl.get().Centering );

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
            if( selectedPeaks != null && selectedPeaks.get() != null)
            for( int i = 0 ; i < hklList.length ; i++ )
            {
               int[][] hklList2;

               if( peak1HKLset && selectedPeaks.get().getSetPeak_hkl( 1 ) != null )
               {
                  float[] F = selectedPeaks.get().getSetPeak_hkl( 1 );
                  hklList2 = new int[ 1 ][ 3 ];

                  for( int k = 0 ; k < 3 ; k++ )
                     hklList2[ 0 ][ k ] = (int) F[ k ];

               }
               else

                  hklList2 = subs.FindPossibleHKLs( LatControl.get().BMat , q1 , q2 ,
                           hklList[ i ] , LatControl.get().Delta1 ,
                           LatControl.get().Delta2 , LatControl.get().Centering );

               if( hklList2 != null )

                  for( int j = 0 ; j < hklList2.length ; j++ )
                  {
                     float[][] UB = LatControl.get().CalcUB( q1 , subs
                              .cvrt2float( hklList[ i ] ) , q2 , subs
                              .cvrt2float( hklList2[ j ] ) );

                     if( UB != null )
                        OrMatrices.add( UB );
                  }
            }

            if( OrMatrices != null && OrMatrices.size() >= 1 )
               setOrientationMatrix( OrMatrices.elementAt( 0 ) );

            showCurrentOrientationMatrices( true , true );

            return;


         }


         if( evtString == THREE_PEAK )
         {
            if( selectedPeaks == null || selectedPeaks.get() == null)
               return;
            float[][] UB = subs.CalcUB( selectedPeaks.get().getSetPeakQ( 0 ) ,
                     selectedPeaks.get().getSetPeak_hkl( 0 ) , selectedPeaks.get()
                              .getSetPeakQ( 1 ) , selectedPeaks.get()
                              .getSetPeak_hkl( 1 ) , selectedPeaks.get()
                              .getSetPeakQ( 2 ) , selectedPeaks.get()
                              .getSetPeak_hkl( 2 ) );

            setOrientationMatrix( UB );

            showCurrentOrientationMatrix( true );

            return;
         }
        InputFileName = filename;
      }


      private void showError( String message )
      {

         JOptionPane.showMessageDialog( null , message );
      }


      private int NsetQ()
      {

         int nPeaksSet = 0;
         if( selectedPeaks == null || selectedPeaks.get() == null)
            return 0;
         for( int i = 0 ; ( i < WPeaks.get().size() ) && nPeaksSet == i ; i++ )
            if( selectedPeaks.get().getSetPeakQ( i ) != null )

               nPeaksSet++ ;

         return nPeaksSet;
      }


      private int NsetHKL()
      {

         int nPeaksSet = 0;
         if( selectedPeaks == null || selectedPeaks.get() == null)
            return 0;
         for( int i = 0 ; ( i < 4 ) && nPeaksSet == i ; i++ )
            if( selectedPeaks.get().getSetPeak_hkl( i ) != null )

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
      WeakReference<View3D>   WView;

      WeakReference<float[][]> WOrientationMatrix;


      /**
       * Constructor
       * @param OrientationMatrix  String describing orientation matrix information
       */
      public OrientMatInfoHandler( String OrientationMatrix )
      {

         OrientMatInfo = OrientationMatrix;
         text = null;
         WView = null;
         WOrientationMatrix = null;
      }

      public void kill()
      {
         text = null;
         Panel = null;
         WView = null;
         WOrientationMatrix = null;
      }

      /**
       * Sets orientation information for SelectPeakHandler
       * @param view                The 3D  View of peaks Q space
       * @param orientationMatrix   The orientation matrix
       * 
       */
      public void setOrientationInfo( View3D view , float[][] orientationMatrix )
      {

         WView = new WeakReference<View3D>(view);
         WOrientationMatrix = new WeakReference<float[][]>(orientationMatrix);
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

         if( WView == null || WOrientationMatrix == null|| WView.get() == null  ||
                  WOrientationMatrix.get() == null)
            return;

         WView.get().showOrientation( WOrientationMatrix.get() , Peak.seqnum() );


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
       *   Used to call the show method if not in the swing thread
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

      public void kill()
      {
         X = Y = null;
         list = null;
         if( Panel != null)
            Panel.removeAll();
         Panel = null;
         if( view != null)
         view.kill();
         view = null;
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
         kill();

      }


   }

   /**
    * Handles showing a list of orientation matrices with options to select one as the
    * orientation matrix.
    * @author Ruth
    *
    */
  static class OrientMatListHandler implements ActionListener , ChangeListener
   {



      WeakReference<Vector< float[][] >> WorMatrices;

      WeakReference<SetPeaks>            Wsetpks;

      boolean             selectMatrix;

      int                 selectedMatNum;

      JSpinner            spinner;

      JTextArea           text;

      WeakReference<Vector<IPeak> >      WPeaks;
 

      /**
       * Constructor
       * @param OrMatrices  The Vector of orientation matrices
       * @param Setpks      The SetPeaks object with the set peak information
       * @param SelectMatrix  If true, an option to select one of the matrices 
       *                     will be included
       */
      public OrientMatListHandler( Vector< float[][] > OrMatrices,
               SetPeaks Setpks,  boolean SelectMatrix )
      {
         this( OrMatrices, Setpks, null, SelectMatrix);
      }
      /**
       * Constructor
       * @param OrMatrices  The Vector of orientation matrices
       * @param Setpks      The SetPeaks object with the set peak information
       * @param Peaks       The Vector of peaks
       * @param SelectMatrix  If true, an option to select one of the matrices 
       *                     will be included
       */
      public OrientMatListHandler( Vector< float[][] > OrMatrices,
               SetPeaks Setpks, Vector<IPeak> Peaks, boolean SelectMatrix )
      {

         WorMatrices =new WeakReference<Vector< float[][] >> (OrMatrices);
         Wsetpks =new WeakReference< SetPeaks>( Setpks);
         selectMatrix = SelectMatrix;
         this.WPeaks = new WeakReference<Vector<IPeak>>(Peaks);
         selectedMatNum = 0;
         spinner = null;
         text = null;
      }
      public void kill()
      {
         
         WorMatrices.get().clear();
         WorMatrices = null;
         Wsetpks = null;
         spinner = null;
         text = null;
         WPeaks = null;
      }

      public float[][] run()
      {
         Vector< float[][] > orMatrices =WorMatrices.get();
         if( orMatrices == null)
            return null;
         if( orMatrices == null || orMatrices.size() < 1 )
            return null;

         JPanel jp = new JPanel();
         jp.setLayout( new BorderLayout() );

         int n = 2;
        // if( selectMatrix )
        //    n++ ;

         JPanel jp1 = new JPanel();
         jp1.setLayout( new GridLayout( 1 , n ) );

         spinner = new JSpinner( new SpinnerNumberModel( 1 , 1 , orMatrices
                  .size()  , 1 ) );
         spinner.addChangeListener( this );

         jp1.add( new JLabel( "Mat Num" ,JLabel.RIGHT) );
         jp1.add( spinner );

         //JButton button = new JButton( "Select" );

        // button.addActionListener( this );

         //if( n > 2 )
         //   jp1.add( button );

         jp.add( jp1 , BorderLayout.NORTH );

         text = new JTextArea( 15 , 35 );
         text
                  .setText( ShowMatString(  orMatrices
                           .elementAt( 0 ),WPeaks.get(), null, null ) );

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
         Vector< float[][] > orMatrices =WorMatrices.get();
         if( orMatrices == null)
            return ;

         if( spinner == null || text == null )
            return;

         int MatNum = ( (Integer) spinner.getValue() ).intValue() - 1;

         if( MatNum < 0 || MatNum >= orMatrices.size() )
            return;
         
         selectedMatNum = MatNum;
         
         text.setText( ShowMatString(  orMatrices
                  .elementAt( MatNum ),WPeaks.get(),null,null ) );


      }

   }
}
