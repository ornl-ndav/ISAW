/* 
 * File: SetPeaks.java
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

import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.Util.Sys.WindowShower;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.table.*;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;


/**
 * Handler for setting peaks. It currently subclasses JButton
 * @see subs#getJMenuItem(AbstractButton)
 * @author Ruth
 *
 */
public class SetPeaks extends JButton
{
 
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Maximum number of peaks that can be set
    */
   public static int     MAX_SEL_PEAKS   = 12;
   
   /**
    * Action command when notifying action listeners that new peaks have 
    * been set.
    */
   public static String SET_PEAK_INFO_CHANGED ="Set Peaks Changed";

   /**
    * The Text on the JButton. 
    */
   public static String  SELECT_PEAK_BUT = "Select Peaks";

   //Menu 1 strings
   private static String SET_PEAK        = "Set/Alter  Peak";

   private static String CLEAR_SET_PEAKS = "Clear All set Peaks";

   private static String SET_HKL         = "Set/Alter  hkl val";

   //Menu 2 strings
   private static String SET_PEAK1       = "Set Peak 1 info";

   private static String SET_PEAK2       = "Set Peak 2 info";

   private static String SET_PEAK3       = "Set Peak 3 info";

   private static String SET_PEAK4       = "Set Peak 4 info";

   private static String SET_PEAKN       = "Set Peak more Peak info";

   private static String SET_PEAK_LIST  ="Set List of Peaks";
   // List of selected Peaks
   float[][]             SelectedPeaks;

   //List of specified hklVals
   float[][]             hklVals;

   //List of sequence number corresponding to SelectedPeaks.
   // Can be -1. Q vals can be entered without a sequence #
   int[]                 seqNums;

   //Max number of set peaks and set hkl's. Only consecutively set peaks
   //  are considered set
   int                   MaxPksSet , MaxhklSet;

   //Save values in case other entities want to use these as inputs
   // See setDisable
   float[][]             SAV_SelectedPeaks;

   float[][]             SAV_hklVals;

   int[]                 SAV_seqNums;

   int                   SAV_MaxPksSet , SAV_MaxhklSet;

   //Listener to all menu actions
   MyActionListenerForm      listener;

   //If new entries are disabled
   boolean               disable;

   // The 3D view of the peaks in reciprocal space( Q values)
   View3D                View;

   //The vector of peaks
   Vector< IPeak >       Peaks;
   
   JComponent      DisplayComponent  = null;//To kill spawned windows


   /**
    * Constructor
    * @param view     The 3D view of the peaks in reciprocal space( Q values)
    * @param peaks    The vector of peaks
    */
   public SetPeaks( View3D view, Vector< IPeak > peaks )
   {

      super( SELECT_PEAK_BUT );
      View = view;
      Peaks = peaks;

      listener = new MyActionListenerForm( );//new MyActionListener( this)
      
      addActionListener( listener );
      addSetPeakListeners( listener);
      disable = false;

      setToolTipText( "<html><body><UL>Selects peaks and hkl values for arguments"
               + " to other parts of the system<LI> Some Calculations for "
               + "orientation matrices<LI> The Info view for setting Peak hkl values"
               + "</ul></body></html)" );

      Clear();
   }
   
   public ActionListener getActionListener()
   {
      return listener;
   }

   public void kill()
   {
      SelectedPeaks=hklVals = null;
      seqNums = null;
      removeActionListener(listener );
      SAV_SelectedPeaks=SAV_hklVals = null;
      SAV_seqNums = null;
      View = null;
      Peaks = null;
      removeActionListener( listener);
      listener.kill();
      listener = null;
      if( SetPeakListeners != null)
        SetPeakListeners.clear();
      SetPeakListeners = null;
   }
   
   /**
    * This class spawns Windows.  If the component C being not viewable means
    * the window(s) should disappear, set it as a DisplayComponent.
    * 
    * @param C  The component whose visibility is tied to the visibility of
    *            the spawned window(s); 
    */
   public void setDisplayComponent( JComponent C)
   {
      DisplayComponent = C;
   }
   /*
    * @return the Q values associated with PeakNum. 0 is the first set peak
    *          and null is returned if not set.
    */
   public float[] getSetPeakQ( int PeakNum )
   {
      
      if( PeakNum < 0 || PeakNum >= SelectedPeaks.length )
         return null;

      for( int j = 0 ; j < 3 ; j++ )
         if( Float.isNaN( SelectedPeaks[ PeakNum ][ j ] ) )
            return null;

      return SelectedPeaks[ PeakNum ];
   }


   /*
    * @return the hkl values associated with PeakNum. 0 is the first set peak
    *          and null is returned if not set.
    */
   public float[] getSetPeak_hkl( int PeakNum )
   {
      
      if( PeakNum < 0 || PeakNum >= 3 )
         return null;

      for( int j = 0 ; j < 3 ; j++ )
         if( Float.isNaN( hklVals[ PeakNum ][ j ] ) )
            return null;

      return hklVals[ PeakNum ];
   }


   /**
    * @return the sequence number associated with PeakNum. 0 
    *                is the first set peak. -1 is returned if not set
    */
   public int getSetPeakSeqNum( int PeakNum )
   {
     
      if( PeakNum < 0 || PeakNum >= seqNums.length )
         return - 1;

      return seqNums[ PeakNum ];
   }


   /**
    * Sets the Q values of a set peak.
    * @param PeakNum  The set peak num. It starts at 0 
    * @param qx        qx value
    * @param qy        qy value
    * @param qz        qz value
    */
   public void setPeakQ( int PeakNum , float qx , float qy , float qz )
   {

      if( PeakNum < 0 || PeakNum >= MAX_SEL_PEAKS )
         return;

      SelectedPeaks[ PeakNum ][ 0 ] = qx;
      SelectedPeaks[ PeakNum ][ 1 ] = qy;
      SelectedPeaks[ PeakNum ][ 2 ] = qz;
      seqNums[ PeakNum ] = - 1;
      fireSetPeakListeners( SET_PEAK_INFO_CHANGED);
   }


   /**
    * Sets the hkl and Q values for a set peak
    * @param PeakNum  The set peak num. It starts at 0 
    * @param pk       The selected peak.
    */
   public void setPeakQ( int PeakNum , IPeak pk )
   {

      if( PeakNum < 0 || PeakNum >= MAX_SEL_PEAKS || pk == null )
         return;

      float[] Qs = pk.getUnrotQ();

      setPeakQ( PeakNum , Qs[ 0 ] , Qs[ 1 ] , Qs[ 2 ] );

      seqNums[ PeakNum ] = pk.seqnum();
   }


   /**
    * Sets the hkl values only for a set peak
    * @param PeakNum  The set peak num. It starts at 0 
    * @param h        The h value
    * @param k        The k value
    * @param l        The l value
    */
   public void setPeakHKL( int PeakNum , float h , float k , float l )
   {

      if( PeakNum < 0 || PeakNum >= 3 )
         return;

      hklVals[ PeakNum ][ 0 ] = h;
      hklVals[ PeakNum ][ 1 ] = k;
      hklVals[ PeakNum ][ 2 ] = l;

      fireSetPeakListeners( SET_PEAK_INFO_CHANGED);

   }


   /**
    * Sets the hkl  values only  for a set peak
    * @param PeakNum  The set peak num. It starts at 0 
    * @param pk       The selected peak. 
    */
   public void setPeakHKL( int PeakNum , IPeak pk )
   {

      if( PeakNum < 0 || PeakNum >= MAX_SEL_PEAKS || pk == null )
         return;

      setPeakHKL( PeakNum , pk.h() , pk.k() , pk.l() );
   }


   Vector<ActionListener> SetPeakListeners = new Vector<ActionListener>();
   public void addSetPeakListeners( ActionListener  Listener)
   {
    if( Listener == null)
       return;
    
    if( SetPeakListeners.contains( Listener))
       return;
    
    SetPeakListeners.add( Listener);
    
   }
   
   public void removeSetPeakListener( ActionListener Listener)
   {
      if( Listener == null)
         return;
      
      if( SetPeakListeners.contains(  Listener ))
         SetPeakListeners.remove(  Listener );
   }
   
   public void removeAllSetPeakListeners()
   {
      SetPeakListeners.clear();
   }
   
   private void fireSetPeakListeners( String message)
   {
      for( int i=0; i< SetPeakListeners.size(); i++)
         SetPeakListeners.elementAt( i ).actionPerformed(   
                  new ActionEvent( this, ActionEvent.ACTION_PERFORMED, message));
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
    * Clears out all set peaks and hkl values
    */
   public void Clear()
   {

      SelectedPeaks = new float[ MAX_SEL_PEAKS ][ 3 ];

      hklVals = new float[ 3 ][ 3 ];
      seqNums = new int[ MAX_SEL_PEAKS ];

      SAV_SelectedPeaks = new float[ MAX_SEL_PEAKS ][ 3 ];

      SAV_hklVals = new float[ 3 ][ 3 ];
      SAV_seqNums = new int[ MAX_SEL_PEAKS ];
      for( int i = 0 ; i < MAX_SEL_PEAKS ; i++ )
      {
         Arrays.fill( SelectedPeaks[ i ] , Float.NaN );
         seqNums[ i ] = - 1;
         if( i < 3 )
            Arrays.fill( hklVals[ i ] , Float.NaN );

         Arrays.fill( SAV_SelectedPeaks[ i ] , Float.NaN );
         SAV_seqNums[ i ] = - 1;
         if( i < 3 )
            Arrays.fill( SAV_hklVals[ i ] , Float.NaN );

      }
      MaxPksSet = 0;
      MaxhklSet = 0;

      SAV_MaxPksSet = 0;
      SAV_MaxhklSet = 0;
      fireSetPeakListeners( SET_PEAK_INFO_CHANGED);
   }


   private void copyTo( float[][] from , float[][] to )
   {

      for( int i = 0 ; i < from.length ; i++ )
         System.arraycopy( from[ i ] , 0 , to[ i ] , 0 , to[ i ].length );
   }


   private void copyTo( int[] from , int[] to )
   {

      System.arraycopy( from , 0 , to , 0 , to.length );
   }


   /**
    * Saves the previous settings so these points are usable for other purposes
    * 
    * @param disable Cannot change selected peaks with hkl if disabled.
    */
   public void setDisable( boolean disable )
   {

      if( this.disable != disable )
         if( disable )
         {
            copyTo( SAV_SelectedPeaks , SelectedPeaks );
            copyTo( SAV_hklVals , hklVals );
            copyTo( SAV_seqNums , seqNums );
            MaxPksSet = SAV_MaxPksSet;
            MaxhklSet = SAV_MaxhklSet;

         }
         else
         {

            copyTo( SelectedPeaks , SAV_SelectedPeaks );
            copyTo( hklVals , SAV_hklVals );
            copyTo( seqNums , SAV_seqNums );
            SAV_MaxPksSet = MaxPksSet;
            SAV_MaxhklSet = MaxhklSet;
         }

      this.disable = disable;
   }
   
   private int FindLastSet( int PeakNum , int maxSetSoFar , float[][] List )
   {

      if( PeakNum < maxSetSoFar )
         return maxSetSoFar;

      if( List == null )
         return 0;

      for( int i = maxSetSoFar ; i < List.length ; i++ )
         for( int j = 0 ; j < 3 ; j++ )
            if( Float.isNaN( List[ i ][ j ] ) )

               return i;

      return List.length;

   }
   
   
   private void  ExpandDataBase( int LastRow )
   {
      
      if( seqNums.length <= LastRow)
      {
         int[] newSeqNums = new int[LastRow+1];
         Arrays.fill( newSeqNums , -1 );
         System.arraycopy( seqNums , 0 , newSeqNums , 0 , seqNums.length );
         seqNums = newSeqNums;
      }
      
      if( SelectedPeaks.length <=LastRow)
      {
         float[][] newSelPeaks = new float[LastRow+1][3];
         for( int i= SelectedPeaks.length; i< newSelPeaks.length; i++)
            Arrays.fill(newSelPeaks[i], 0f);
         System.arraycopy( SelectedPeaks , 0, newSelPeaks , 0 , 
                                          SelectedPeaks.length );
         SelectedPeaks = newSelPeaks;
      }
   }
   /**
    * This actionlistener produces a persistent Form instead of menus
    * @author ruth
    *
    */
   class MyActionListenerForm extends WindowAdapter implements ActionListener
   
   {

      FinishJFrame jf;

      JTextField   SeqList;

      JTable       table;
      
      JPanel       TableHolder;

      @Override
      public void actionPerformed(ActionEvent arg0)
      {

         if ( arg0.getActionCommand( ).equals( SetPeaks.SELECT_PEAK_BUT ) )
         {
            if ( jf != null )
               return;
            else
               MakeForm( arg0 );
            return;
         }

         if ( arg0.getSource( ) == SeqList )
         {
            int[] seqN = IntList.ToArray( SeqList.getText( ).trim( ) );
            if ( seqN != null && seqN.length > 0 )
            {
               if ( seqNums.length < seqN.length
                     || SelectedPeaks.length < seqN.length
                     || hklVals.length < 3 )
               {
                  seqNums = new int[ seqN.length + 1 ];
                  Arrays.fill( seqNums , -1 );
                  SelectedPeaks = new float[ seqN.length + 1 ][ 3 ];
                  hklVals = new float[ seqN.length + 1 ][ 3 ];

                  Arrays.fill( hklVals[seqN.length] , Float.NaN );
                  Arrays.fill( SelectedPeaks[seqN.length] , Float.NaN );
               }
               for( int i = 0 ; i < seqN.length ; i++ )
                  if ( seqN[i] >= 0 && seqN[i] < Peaks.size( ) )
                  {
                     seqNums[i] = seqN[i];
                     if ( i < 3 )
                        for( int j = 0 ; j < 3 ; j++ )
                           hklVals[i][j] = Float.NaN;
                     SelectedPeaks[i] = Peaks.elementAt( seqN[i] ).getUnrotQ( );
                  } else
                  {

                     for( int j = 0 ; j < 3 ; j++ )
                     {
                        if ( i < 3 )
                           hklVals[i][j] = Float.NaN;
                        SelectedPeaks[i][j] = Float.NaN;
                     }

                  }

            }
            listener.SetUpTable( seqNums , SelectedPeaks , hklVals );
         
            return;
         }// if argo.== SeqList

         if ( arg0.getSource( ) instanceof JTable )
         {
            if( (arg0.getModifiers( )& ActionEvent.SHIFT_MASK) >0)
               if( arg0.getActionCommand( ).startsWith("Select"))
                  if( table.getSelectedColumn( )==1)
                  {
                     int seqNum = View.getLastSelectedSeqNum( ) ;
                     int row = table.getSelectedRow( );
                     
                     if( row < 0 || row >= table.getRowCount( )|| seqNum < 0
                           || seqNum >= Peaks.size())
                        return;
                     ((MyJTable)table).removeSelectionListener( this );
                     table.setValueAt(  seqNum , row , 1 );
                     float[] q =Peaks.elementAt( seqNum-1 ).getUnrotQ( );
                     table.setValueAt( q[0] , row , 2 );
                     table.setValueAt( q[1] , row , 3 );
                     table.setValueAt( q[2] , row , 4 );
                     ((MyJTable)table).addSelectionListener( this );
                     seqNums[row]= seqNum;//Check if enough space is allocated

                     ExpandDataBase( row );
                     SelectedPeaks[row]= q;
                     table.repaint( );
                     return;
                     
                  }
           return;
         }
         if( arg0.getSource() instanceof TableModel)
         {
            int row = table.getEditingRow( );
            int col = table.getEditingColumn( );
            ExpandDataBase( row );
            if( col ==1)
            {
               int seqNum = ((Number)table.getValueAt( row , col )).intValue( );
               seqNums[row] = seqNum;
               float[] qs = new float[3];
               Arrays.fill(  qs, Float.NaN );
               if( seqNum >=1 && seqNum <= Peaks.size() )
               {
                  IPeak pk = Peaks.elementAt(seqNum - 1);
                  qs = pk.getUnrotQ( );
               }
               SelectedPeaks[row] = qs;
               ((MyJTable)table).removeSelectionListener( this );
               table.setValueAt( qs[0] , row , 2 );
               table.setValueAt( qs[1] , row , 3 );
               table.setValueAt( qs[2] , row , 4 );
               ((MyJTable)table).addSelectionListener( this );
              

               MaxPksSet = FindLastSet( row , MaxPksSet , SelectedPeaks );
               fireSetPeakListeners( SetPeaks.SET_PEAK_INFO_CHANGED );
               
            }else if( col > 1 && col <=4 )
            {
              ((MyJTable)table).removeSelectionListener( this );
              seqNums[row] = -1;  
              table.setValueAt( -1 , row , 1 );
              float q = ((Float)table.getValueAt( row , col )).floatValue( );
              SelectedPeaks[row][col-2]=q;
              ((MyJTable)table).addSelectionListener( this );
              
              int prevMaxPks = FindLastSet( row , MaxPksSet , SelectedPeaks );
              if( prevMaxPks != MaxPksSet)
                {
                 MaxPksSet = prevMaxPks;
                 fireSetPeakListeners( SetPeaks.SET_PEAK_INFO_CHANGED );
                }
              
            }else if( col <=7 && col>=5&& row <3)//hkl values
            {
               hklVals[row][col-5] = ((Float)table.getValueAt( row , col)).floatValue( );
               int prevMaxhkl = Math.min( 3 , FindLastSet( row , MaxhklSet , hklVals ));
               if( prevMaxhkl != MaxhklSet)
                 {
                  MaxhklSet = prevMaxhkl;
                  fireSetPeakListeners( SetPeaks.SET_PEAK_INFO_CHANGED );
                 }
            }
            table.repaint( );
            return;
         }

         if ( arg0.getActionCommand( ).equals( "Add New Row" ) )
         {
            ( ( MyJTable ) table ).addNewRow( );
            return;
         }
         
         
         if ( arg0.getActionCommand( ).equals( SET_PEAK_INFO_CHANGED ) )
         {
            // Somebody from outside set the peaks.
            if ( jf == null )
               return;

            SetUpTable( seqNums , SelectedPeaks , hklVals );
            return;

         }
         

      }

     
      private void MakeForm(ActionEvent evt)
      {

         jf = new FinishJFrame( "Select/Set Peak & hkl Arguments" );
         Point P = new Point( 0 , 0 );
         if ( evt.getSource( ) instanceof AbstractButton )
         {
            P = ( ( AbstractButton ) evt.getSource( ) ).getLocation( );
         }
         jf.setBounds( P.x , P.y , 500 ,300 );
         JPanel comp = new JPanel( );
         BoxLayout bl = new BoxLayout( comp , BoxLayout.Y_AXIS );
         comp.setLayout( bl );
         JPanel row1 = new JPanel( new GridLayout( 1 , 2 ) );
         row1.add( new JLabel( "List of sequence Numbers" ) );
         SeqList = new JTextField( 15 );
         SeqList.addActionListener( this );
         row1.add( SeqList );
         comp.add( row1 );
         JPanel row2= new JPanel();
         row2.setLayout(  new BorderLayout() );
         JLabel note =new JLabel( "<html><body>* Shift(hold) then  click 2nd column "+
                          "to set with currently<BR> selected "+
                          "peak in the QViewer</body></html>" );
         note.setBorder( new LineBorder( Color.black) );
         row2.add( note, BorderLayout.CENTER);
         JButton AddRow = new JButton("Add New Row");
         AddRow.addActionListener(  this );
         row2.add( AddRow, BorderLayout.EAST );
         comp.add(row2 );

         SetUpTable( seqNums , SelectedPeaks , hklVals );
         TableHolder = new JPanel( new GridLayout(1,1));
         TableHolder.add( new JScrollPane(table) );
         comp.add( TableHolder  );
         jf.getContentPane( ).setLayout( new GridLayout( 1 , 1 ) );
         jf.getContentPane( ).add( comp );
         if( DisplayComponent != null)
            DisplayComponent.addAncestorListener( 
                  new gov.anl.ipns.Util.Sys.WindowAncestorListener(jf));
         WindowShower.show( jf );
         jf.addWindowListener( this );

      }

      public void kill()
      {

      }

      public void SetUpTable(int[] seqNums, float[][] SelectedQs,
            float[][] Selhkl)
      {

         int nrow = 0;
         int ncols = 8;
         boolean done = false;
         if ( SelectedQs != null )
            for( int i = 0 ; i < SelectedQs.length && !done ; i++ )
               if ( Float.isNaN( SelectedQs[i][0] )
                     || Float.isNaN( SelectedQs[i][1] )
                     || Float.isNaN( SelectedQs[i][2] ) )
                  done = true;
               else
                  nrow = i + 1;
         if ( nrow < 3 )
            nrow = 3;
         if ( table != null && table.getRowCount( ) >= nrow )
         {
            ((MyJTable)table).removeSelectionListener( this );
            for( int i = 0 ; i < table.getRowCount( ) ; i++ )
            {
               if ( seqNums == null || seqNums.length <= i )
                  table.setValueAt( -1 , i , 1 );
               else
                  table.setValueAt( seqNums[i] , i , 1 );
               if ( SelectedQs == null || SelectedQs.length <= i
                     || SelectedQs[i].length < 3 )
               {
                  table.setValueAt( Float.NaN , i , 2 );
                  table.setValueAt( Float.NaN , i , 3 );
                  table.setValueAt( Float.NaN , i , 4 );
               } else
               {
                  table.setValueAt( SelectedQs[i][0] , i , 2 );
                  table.setValueAt( SelectedQs[i][1] , i , 3 );
                  table.setValueAt( SelectedQs[i][2] , i , 4 );
               }
               if ( Selhkl == null || Selhkl.length <= i
                     || Selhkl[i].length < 3 )
               {
                  table.setValueAt( Float.NaN , i , 5 );
                  table.setValueAt( Float.NaN , i , 6 );
                  table.setValueAt( Float.NaN , i , 7 );
               } else
               {
                  table.setValueAt( Selhkl[i][0] , i , 5 );
                  table.setValueAt( Selhkl[i][1] , i , 6 );
                  table.setValueAt( Selhkl[i][2] , i , 7 );
               }

            }

            ((MyJTable)table).addSelectionListener( this );
            table.repaint( );
            return;
         }
            
         String[] ColumnNames = new String[]{ "Sel Num" ,"Peak Seq Num*", 
                                        "Qx" ,"Qy" , "Qz" , "h","k" ,"l" };
         Float[][] rowData = new Float[ nrow ][ncols];
         for( int i = 0 ; i < nrow ; i++ )
         {
            
            rowData[i][0] = (float)(i+1);
            if ( seqNums != null && seqNums.length > i )
               rowData[i][1]= (float)seqNums[i] ;
            else
               rowData[i][1]= (float)-1 ;
            if ( SelectedQs != null && SelectedQs.length > i
                  && SelectedQs[i].length >= 3 )
            {
               rowData[i][2]=SelectedQs[i][0] ;

               rowData[i][3]=SelectedQs[i][1] ;
               rowData[i][4]=SelectedQs[i][2] ;
            } else
            {

               rowData[i][2] =Float.NaN ;
               rowData[i][3] =Float.NaN ;
               rowData[i][4]= Float.NaN ;
            }

            if ( Selhkl != null && Selhkl.length > i && Selhkl[i].length >= 3 )
            {
               rowData[i][5]=Selhkl[i][0] ;
               rowData[i][6]=Selhkl[i][1] ;
               rowData[i][7]=Selhkl[i][2] ;
            } else
            {

               rowData[i][5]=( Float.NaN );
               rowData[i][6]= Float.NaN ;
               rowData[i][7]=( Float.NaN );

            }

         }
         
         MyJTableModel model = new MyJTableModel(rowData , ColumnNames);
         table = new MyJTable( model );

         table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
         
         ((MyJTable)table).addSelectionListener( this );
         
         table.repaint( );
         
         if( TableHolder != null)
         {
            TableHolder.removeAll( );
            TableHolder.add( new JScrollPane(table) );
            TableHolder.validate( );
            TableHolder.invalidate( );
            TableHolder.repaint( );
         }

      }
      
      public void windowClosed(WindowEvent e)
      {
         jf = null;
      }
      
   }
   
   
   class MyJTableModel   extends AbstractTableModel 
                                                           
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;
      Vector<ActionListener> listeners = new Vector<ActionListener>();
      Object[] colNames = null;
      Float[][] data = null;
      int nrows, ncols;
    
      public MyJTableModel( Float[][] rowData, Object[] ncolNames)
      {
         super();
         
         colNames = ncolNames;
         data = rowData;
         ncols = 8;
         nrows = data.length;
      }
      
      
      @Override
      public int getColumnCount()
      {

        
         return ncols;
      }


      @Override
      public int getRowCount()
      {

        
         return nrows;
      }

      public void addNewRow()
      {
         nrows++;
         Float[][] newData = new Float[nrows][8];
         System.arraycopy( data , 0 , newData , 0, data.length );
         Arrays.fill( newData[nrows-1],2,8, Float.NaN);
         newData[nrows-1][0] = (float)nrows;
         newData[nrows-1][1] = (float)-1;
         data = newData;
         
      }

      public Class getColumnClass( int colIndex)
      {
         if( colIndex <=1)
            return Integer.class;
         
         return Float.class;
         
      }
      
      public Object getValueAt(int rowIndex,  int columnIndex)
      {
         if( rowIndex <0 || columnIndex <0 || data == null ||
               rowIndex >=data.length || columnIndex >=8)
            return Float.NaN;
         
        if(columnIndex <=1)
           return data[rowIndex][columnIndex].intValue();
       
        return  data[rowIndex][columnIndex];
        
      }
      
      public void setValueAt(Object aValue,
                             int rowIndex,
                           int columnIndex)
      {
         if( rowIndex <0 || columnIndex < 1 || data == null ||
               rowIndex >=data.length || columnIndex >=8)
            return;
         
         if( aValue == null || !(aValue instanceof Number))
            
            if( columnIndex <2)
               data[rowIndex][columnIndex] = (float)-1;
            else
               data[rowIndex][columnIndex] = Float.NaN;
         
         else if( columnIndex < 2)
            
            data[rowIndex][columnIndex] =(float)((Number)aValue).intValue( );
         
         else
            
            data[rowIndex][columnIndex] =((Number)aValue).floatValue( );
         
          notifySelectionListeners();  
      }
      
      public String getColumnName(int columnIndex)
      {
        
         
         if( columnIndex < 0)
            return "Colx";
         
         if( colNames== null || columnIndex >= colNames.length )
            return "Col "+columnIndex;
         
         return colNames[columnIndex].toString();
      }
      
      
      public void addSelectionListener( ActionListener  S)
      {
         if( !listeners.contains( S ))
            listeners.add( S );
      }
      
      public void removeSelectionListener( ActionListener S)
      {
         if( listeners.contains( S ))
            listeners.remove( S );
      }
      
      public boolean isCellEditable( int row, int col)
      {
         if( row <0 || row >= getRowCount( ))
            return false;
         
         if( col <=0 || col >= getColumnCount( ))
            return false;
         
         if( col >=5 && row >2)
            return false;
         
         return true;
      }
      
      private void notifySelectionListeners()
      {
         for( int i=0; i< listeners.size(); i++)
            listeners.elementAt( i ).actionPerformed(
                  new ActionEvent( this, ActionEvent.ACTION_PERFORMED,
                        "Value Edited"));
      }
   }
   
   class MyJTable   extends JTable
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;
      
      Vector<ActionListener> listeners;
      MyJTableModel  model;
      public MyJTable( TableModel model )
      {
         super( model);
         listeners = new Vector<ActionListener>();
         this.model = null;
         if( model instanceof MyJTableModel)
            this.model = (MyJTableModel)model;
         
         this.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         
      }
      
      public void addNewRow()
      {
       if( model != null)
       {
          model.addNewRow();
          repaint();
          invalidate();
          validate();
          repaint();
       }
      }
      public void addSelectionListener( ActionListener list)
      {
         if( list == null || listeners.contains(  list ))
            return;
         
         listeners.add(  list );
          
         if( model != null)
            model.addSelectionListener( list);
       
      }
      public void removeSelectionListener( ActionListener list)
      {
         if( list == null || !listeners.contains(  list ))
            return;
         
         listeners.remove(  list );
         if( model != null)
            model.removeSelectionListener( list);
      }
      public void valueChanged(ListSelectionEvent e)
      {
        
         notifySelectionListeners();
      }
      
      public void columnSelectionChanged(ListSelectionEvent e)
      {
        //When want correct row do not notify
        // notifySelectionListeners();
         
      }

      private void notifySelectionListeners()
      {
         for( int i=0; i< listeners.size(); i++)
            listeners.elementAt( i ).actionPerformed(
                  new ActionEvent( this, ActionEvent.ACTION_PERFORMED,
                        "Selection Changed", ActionEvent.SHIFT_MASK));
      }
      
   }
   /**
    * Handles all actions initiated by menus
    * @author Ruth
    *
    */
   class MyActionListener implements ActionListener
   {



      JButton but         = null;

      boolean specifyPeak = true;

      SetPeaks stPks;
      public MyActionListener( SetPeaks stPks)
      {
         this.stPks = stPks;
      }
      
      public void kill()
      {
         but = null;
         stPks = null;
      }

      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         if( stPks.disable )
            return;

         String evtString = e.getActionCommand();

         if( evtString == SetPeaks.SELECT_PEAK_BUT
                  && ( e.getSource() instanceof JButton ) )
         {
            but = (JButton) e.getSource();
            MakePopUpMenu( (JButton) e.getSource() );
            return;

         }
         if( evtString == SetPeaks.SET_PEAK )
         {
            specifyPeak = true;
            MakePopUpMenu1( but );
            return;

         }
         if( evtString == SetPeaks.SET_HKL )
         {
            specifyPeak = false;
            MakePopUpMenu1( but );
            return;

         }
         if( evtString == SetPeaks.CLEAR_SET_PEAKS )
         {
            specifyPeak = true;
            stPks.Clear();
            return;
         }


         if( evtString == SetPeaks.SET_PEAK1 )
         {
            MakeMenu2( specifyPeak , 0 , but );
            return;
         }
         if( evtString == SetPeaks.SET_PEAK2 )
         {
            MakeMenu2( specifyPeak , 1 , but );

            return;
         }
         if( evtString == SetPeaks.SET_PEAK3 )
         {
            MakeMenu2( specifyPeak , 2 , but );

            return;
         }
         if( evtString == SetPeaks.SET_PEAK4 )
         {
            MakeMenu2( specifyPeak , 3 , but );

            return;
         }
         if( evtString == SetPeaks.SET_PEAKN )
         {
            MakeMenu2( specifyPeak , 4 , but );

            return;
         }
         if( evtString == SetPeaks.SET_PEAK_LIST )
         {
            String S = JOptionPane
                     .showInputDialog( but ,
                          "Enter List of Peak sequence \n "+
                          "Numbers separated by commas\n"+
                          "Or use \":\" for a range of values");
            if( S == null )
               return;
            S = S.trim();
            if( S.length() >= 1 )
               if( S.startsWith( "[" ) )
                  S = S.substring( 1 );
            S = S.trim();
            if( S.length() >= 1 )

               if( S.endsWith( "]" ) )
                  S = S.substring( 0 , S.length() - 1 );
            S = S.trim();
           
            float[] undef =
            {
                     Float.NaN , Float.NaN , Float.NaN
            };
            int[] SNums =IntList.ToArray(S );
            if( SNums == null || SNums.length < 1 )
            {
               stPks.SelectedPeaks = new float[ SetPeaks.MAX_SEL_PEAKS ][ 3 ];
               stPks.seqNums = new int[ SetPeaks.MAX_SEL_PEAKS ];
               Arrays.fill( stPks.seqNums , - 1 );
               Arrays.fill( stPks.SelectedPeaks , undef );
               stPks.fireSetPeakListeners( SetPeaks.SET_PEAK_INFO_CHANGED );
               return;
            }
            
            int i = - 1;
            
            if( SNums.length > SetPeaks.MAX_SEL_PEAKS )
            {
               stPks.SelectedPeaks = new float[ SNums.length ][ 3 ];
               stPks.seqNums = new int[ SNums.length ];
            }
            for( i = 0 ; i < SNums.length ; i++ )
            {
               stPks.seqNums[ i ] = SNums[ i ];
               stPks.SelectedPeaks[ i ] = Peaks.elementAt( SNums[ i ]-1 ).getUnrotQ();
            }
            if( SNums.length < SetPeaks.MAX_SEL_PEAKS )
            {
               Arrays.fill( stPks.SelectedPeaks , SNums.length ,
                        stPks.SelectedPeaks.length , undef );
               Arrays.fill( stPks.seqNums , SNums.length , stPks.seqNums.length , - 1 );
            }

            stPks.fireSetPeakListeners( SetPeaks.SET_PEAK_INFO_CHANGED );
            return;
         }
         if( evtString.equals( "Help" ) )
         {
           String  SS = "Q";

            if( ! specifyPeak )
               SS = "hkl";

            JEditorPane jed = new JEditorPane(
                     "text/html" ,
                     "<html><body> Sets the "
                              + SS
                              + " values for the "
                              + "selected peaks. The seq num can be set or set to -1 if the "
                              + SS
                              + " values do not come from a peak.<P> The next"
                              + ""
                              + " dialog box gives the following options<UL><LI>Use last "
                              + "selected peak from the 3D Q-Viewer to specify the selection"
                              + "<LI> Specify choice using the sequence number <LI> or just"
                              + " enter the desired 3 values<LI> For Selected peaks past the "
                              + "4th a spinner will allow for specifing which peak to set"
                              + "</ul><P> If \"Use QView checkbox\""
                              + " is selected all other info is ignored. If the seq number is"
                              + " positive all data under this information is ignored<P>"
                              + "If the "
                              + SS
                              + " or seq num values for a given selected Peak"
                              + " have already been set, these values will be the default"
                              + " values in the displayed text boxes</body></html>" );

            JOptionPane.showMessageDialog( null , jed );

            return;

         }
      }


      private void update( JPopupMenu pop , String Text , String ToolTip )
      {

         JMenuItem jmen = pop.add( Text );
         jmen.addActionListener( this );
         jmen.setToolTipText( ToolTip );

      }


      public void MakePopUpMenu( JButton BUT )
      {

         if( disable )
            return;

         JPopupMenu pop = new JPopupMenu();

         update( pop , SetPeaks.SET_PEAK , "Sets the Q vals and seq num for a peak" );

         update( pop , SetPeaks.SET_HKL , "Sets the hkl vals for a peak" );
         
         update(pop, SetPeaks.SET_PEAK_LIST,"Sets a list of Peak Sequence nums");

         update( pop , SetPeaks.CLEAR_SET_PEAKS , "Clears all settings" );

         pop.show( BUT , BUT.getWidth()*3/4 , BUT.getHeight()/2 );

      }


      public void MakePopUpMenu1( JButton item )
      {

         if( disable )
            return;

         JPopupMenu pop = new JPopupMenu();

         pop.add( SetPeaks.SET_PEAK1 ).addActionListener( this );

         pop.add( SetPeaks.SET_PEAK2 ).addActionListener( this );

         pop.add( SetPeaks.SET_PEAK3 ).addActionListener( this );

         pop.add( SetPeaks.SET_PEAK4 ).addActionListener( this );

         pop.add( SetPeaks.SET_PEAKN ).addActionListener( this );

         pop.add( "Help" ).addActionListener( this );

         pop.show( item , 0 , 0 );

      }


      /**
       * Currently Uses a JOptionPane with an interesting JPanel as message. Events
       * are not needed because it is a dialog box.
       * 
       * @param specifyPeak    Gets Q values if specifyPeak is true otherwise 
       *                       gets hkl values
       * @param PeakNum       The set Peak number. If 5 or more, a spinner will
       *                      allow for changing this value
       * @param men           The JButton on which to hang the OptionPane on.
       */
      public void MakeMenu2( boolean SpecifyPeak , int PeakNum , JComponent men )
      {

         int seqNum = - 1;

         if( stPks.View != null )

            seqNum = stPks.View.getLastSelectedSeqNum();

         JPanel message = new JPanel();

         int n = 1;

         if( SpecifyPeak )
            n++ ;
         if( PeakNum > 3 )
            n++ ;
         if( seqNum > 0 )
            n++ ;

         message.setLayout( new GridLayout( n , 1 ) );

         JSpinner spin = null;
         JCheckBox inView = null;
         if( seqNum >= 1 )

            inView = new JCheckBox( "Use Q View Selection" , SpecifyPeak );


         //------------------ spinner
         if( PeakNum > 3 )

            spin = new JSpinner( new SpinnerNumberModel( 5 , 5 ,
                     SetPeaks.MAX_SEL_PEAKS + 1 , 1 ) );

         if( spin != null )
         {
            JPanel p = new JPanel();
            p.setLayout( new GridLayout( 1 , 2 ) );

            p.add( new JLabel( "Peak to set " , SwingConstants.RIGHT ) );
            p.add( spin );

            message.add( p );

         }

         //------------------------ Use data selected from view
         if( inView != null )
            message.add( inView );

         //----------------------- seq num text
         JTextField text = null;
         if( SpecifyPeak )
         {
            JPanel pp = new JPanel();
            pp.setLayout( new GridLayout( 1 , 2 ) );

            pp.add( new JLabel( "Sequence Number" ) );
            text = new JTextField( "" + stPks.seqNums[ PeakNum ] );

            pp.add( text );

            text.setToolTipText( "<html><body>If enabled and 1 or more, the"
                     + " peak with this seq num values <BR> are used NOT the "
                     + "values from bottom text entry area</body></html>" );

            message.add( pp );
         }


         JPanel p = new JPanel();
         String lab = "Enter Q vals";
         String InitVal = "1.1 , 2.3, -4";

         if( ! SpecifyPeak )
         {
            lab = "Enter h,k,l value";

            if( PeakNum < 3 && ! Float.isNaN( stPks.hklVals[ PeakNum ][ 0 ] )
                     && ! Float.isNaN( stPks.hklVals[ PeakNum ][ 1 ] )
                     && ! Float.isNaN( stPks.hklVals[ PeakNum ][ 2 ] ) )
               InitVal = stPks.hklVals[ PeakNum ][ 0 ] + "," + stPks.hklVals[ PeakNum ][ 1 ]
                        + "," + stPks.hklVals[ PeakNum ][ 2 ];

         }
         else if( ! Float.isNaN( stPks.SelectedPeaks[ PeakNum ][ 0 ] )
                  && ! Float.isNaN( stPks.SelectedPeaks[ PeakNum ][ 1 ] )
                  && ! Float.isNaN( stPks.SelectedPeaks[ PeakNum ][ 2 ] ) )

            InitVal = stPks.SelectedPeaks[ PeakNum ][ 0 ] + ","
                     + stPks.SelectedPeaks[ PeakNum ][ 1 ] + ","
                     + stPks.SelectedPeaks[ PeakNum ][ 2 ];

         p.add( new JLabel( lab ) );
         message.add( p );


         //-------------- disable handling --
         if( inView != null )
         {
            inView.addActionListener( new DisableCompListener( true ,
                     new JComponent[]
                     {
                        text
                     } ) );
            if( inView.isSelected() && text != null )
               text.setEnabled( false );
         }

         String Title ="Input Q Values";
         if( !SpecifyPeak)
            Title ="Input hkl Values";
         
         String S = (String)JOptionPane.showInputDialog( men , message ,Title,
                   JOptionPane.QUESTION_MESSAGE, null,null,InitVal );

         if( S == null )
            return;

         if( PeakNum > 3 && spin != null )
            PeakNum = ( (Integer) spin.getValue() ).intValue() - 1;

         float[] q = new float[ 3 ];

         int SNum = - 1;
         try
         {
            if( text != null)
            SNum = Integer.parseInt( text.getText().trim() );

         }
         catch( Exception ss )
         {}

         if( SNum > 0 && SNum < Peaks.size()
                  && ( inView == null || ! inView.isSelected() ) )
         {

            if( SpecifyPeak )
               seqNums[ PeakNum ] = SNum;

            IPeak pk = stPks.Peaks.elementAt( SNum - 1 );
            q = pk.getUnrotQ();

         }
         else if( seqNum > 0 && inView != null && inView.isSelected() )
         {
            if( SpecifyPeak )
               stPks.seqNums[ PeakNum ] = seqNum;

            IPeak pk = stPks.Peaks.elementAt( seqNum - 1 );
            q = pk.getUnrotQ();

         }
         else if( S != null )
         {

            if( SpecifyPeak )
               stPks.seqNums[ PeakNum ] = - 1;

            String[] SS = S.split( "," );
            if( SS.length != 3 )

               q[ 0 ] = q[ 1 ] = q[ 2 ] = Float.NaN;

            else

               for( int i = 0 ; i < 3 ; i++ )
                  try
                  {
                     q[ i ] = Float.parseFloat( SS[ i ].trim() );
                  }
                  catch( Exception sss )
                  {
                     q[ 0 ] = q[ 1 ] = q[ 2 ] = Float.NaN;
                  }
         }

         if( SpecifyPeak )

            stPks.SelectedPeaks[ PeakNum ] = q;

         else

            stPks.hklVals[ PeakNum ] = q;

         if( SpecifyPeak && PeakNum == stPks.MaxPksSet )

            stPks.MaxPksSet = FindLastSet( PeakNum , stPks.MaxPksSet , stPks.SelectedPeaks );

         else if( ! SpecifyPeak && PeakNum == stPks.MaxhklSet )

            stPks.MaxhklSet = FindLastSet( PeakNum , stPks.MaxhklSet , stPks.hklVals );
         
         stPks.fireSetPeakListeners( SetPeaks.SET_PEAK_INFO_CHANGED );

      }


     
   }

}
