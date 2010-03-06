/* 
 * File: GroupSelector.java
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
package DataSetTools.components.View;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import gov.anl.ipns.Parameters.*;
import gov.anl.ipns.Util.File.FileIO;
import gov.anl.ipns.Util.Messaging.IObserver;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.Numeric.floatPoint2D;
import gov.anl.ipns.Util.Sys.Browser;
import gov.anl.ipns.Util.Sys.EnableDisableActionListener;
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.AxisInfo;
import gov.anl.ipns.ViewTools.Components.IViewComponent;
import gov.anl.ipns.ViewTools.Components.ObjectState;
import gov.anl.ipns.ViewTools.Components.Region.RegionOp;
import gov.anl.ipns.ViewTools.Components.Region.RegionOpListWithColor;
import gov.anl.ipns.ViewTools.Components.TwoD.ImageViewComponent;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.Panels.Image.ImageJPanel2;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordBounds;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordTransform;
import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import DataSetTools.dataset.*;
import DataSetTools.operator.DataSet.Attribute.DS_Attribute;
import DataSetTools.operator.DataSet.Attribute.GetPixelInfo_op;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.SelectedData2D;
import DataSetTools.viewer.Table.RowColTimeVirtualArray;
import DataSetTools.viewer.ThreeD.ThreeDView;
import EventTools.EventList.FileUtil;
import IsawGUI.BrowserControl;

public class GroupSelector implements IObserver, ActionListener
{

   public static final String ASSIGN_GROUP           = "Assign to Group";

   public static final String REMOVE_GROUP           = "Remove Group";

   public static final String FORMULA_ASSIGN         = "Formula Assign";

   public static final String SET_DETECTOR_SELECTS   = "Set";

   public static final String CLEAR_DETECTOR_SELECTS = "Clear";

   public static final String SAVE_GROUPS            = "Save";

   public static final String VIEW_GROUPS            = "View Groups";

   public static final String SHOW_GROUPS            = "Show Groups";

   DataSet                    DS;

   JPanel                     CommandPanel;

   ThreeDView                 ThreeDPanel;

   RowColTimeVirtualArray     VirtArray2D;

   ImageViewComponent         DetectorPanel;

   JFrame                     LeftFrame, // holds ThreeDPanel
         RightFrame;                                                      // holds
                                                                          // DetectorPanel
                                                                          // of
                                                                          // VirtArray2D

   FilteredPG_TextField       Detectors_Gr;

   FilteredPG_TextField       Group;

   FilteredPG_TextField       RmvGroup;

   FilteredPG_TextField       Detectors_Rmve;

   FilteredPG_TextField       Detectors_Formula;

   FilteredPG_TextField       FormulaGroup;

   JComboBox                  formula;

   JCheckBox                  ViewGroup;

   IDataGrid[]                grids;

   int[]                      pixelGroup;

   int[]                      detectorIDPixel;

   int[]                      rowPixel;

   int[]                      colPixel;

   int                        startPixel;

   float                      L0;

   float                      T0;

   float                      MaxIntensity           = 20;

   int                        MaxGroupID             = 5;

   Vector< Integer >          GroupList;

   int                        LastGrid               = -1;

   boolean                    LastViewGroupMode      = false;

   Hashtable[]                SelectedRegionsSav;

   ControlCheckboxButton      SelectionEditor;

   ButtonControl              GroupEditor;

   ViewControl                GroupSelectorControl;

   /**
    * Reads in starting info from a NeXus file. Note if the Nexus files do not
    * have pixel_ids then use the setBankMapFile function to associate
    * pixel-Id's with pixels in each detector.
    * 
    * @param NeXusFileName
    *           The name of the NeXus file
    */
   public GroupSelector(String NeXusFileName)
   {

      NexusRetriever ret = new NexusRetriever( NeXusFileName );
      
      int nDataSets = ret.numDataSets( );
      
      Vector< UniformGrid > Grids = new Vector< UniformGrid >( );
      
      DS = new DataSet( );
      
      int start = 0;
      
      if ( ret.getType( 0 ) == Retriever.MONITOR_DATA_SET )
         start = 1;
  
      int minPixel_id = Integer.MAX_VALUE;
      int maxPixel_id = -1;
      
      MaxIntensity = 0;
      
      for( int i = start ; i < nDataSets ; i++ )
      {
         DataSet ds = ret.getDataSet( i );
         
         if ( i == start )
         {
            L0 = AttrUtil.getInitialPath( ds );
            T0 = AttrUtil.getT0Shift( ds );
         }
         
         if ( ds != null )
         {
            int[] gridIDs = Grid_util.getAreaGridIDs( ds );
            
            if ( gridIDs != null )
            {
               for( int g = 0 ; g < gridIDs.length ; g++ )
               {
                  IDataGrid gridx = Grid_util.getAreaGrid( ds , gridIDs[g] );

                  if ( gridx instanceof RowColGrid )
                  {
                     gridx = ( UniformGrid ) ( RowColGrid.GetDataGrid(
                           ( RowColGrid ) gridx , .05f ) );
                     
                     gridx.setData_entries( ds );
                  }

                  if ( gridx instanceof UniformGrid )
                  {
                     Grids.addElement( ( UniformGrid ) gridx );
                     
                     PixelInfoList plist = AttrUtil.getPixelInfoListValue(
                           Attribute.PIXEL_INFO_LIST , gridx.getData_entry( 1 ,
                                 1 ) );
                     
                     DataSet dss = MakeDataSet( gridx , plist.pixel( 0 ).ID( ) ,
                           L0 , T0 );
                     
                     UniformGrid gridxo = new UniformGrid(
                           ( UniformGrid ) gridx , true );
                     
                     gridxo.setData_entries( ds );
                     
                     gridx.setData_entries( dss );
                     
                     for( int r = 1 ; r <= gridx.num_rows( ) ; r++ )
                        for( int c = 1 ; c <= gridx.num_cols( ) ; c++ )
                        {
                           Data D = gridxo.getData_entry( r , c );
                           
                           float[] ys = D.getY_values( );
                           float[] errs = D.getErrors( );
                           float[] y_new = new float[]{ 0f , 0f };
                           
                           float[] E = new float[] { 0f , 0f };
                          
                          
                           for( int y = 0 ; y < ys.length ; y++ )
                           {
                              y_new[0] += ys[y];
                              
                              if ( errs != null )
                                 E[0] += errs[y] * errs[y];
                           }
                           
                           if ( y_new[0] > MaxIntensity )
                              MaxIntensity = y_new[0];
                           
                           if ( errs != null )
                              E[0] = ( float ) Math.sqrt( E[0] );

                           Data D1 = gridx.getData_entry( r , c );
                           
                           DS.addData_entry( D1 );
                           
                           D1.getY_values( )[0] = y_new[0];
                           
                           plist = AttrUtil.getPixelInfoListValue(
                                 Attribute.PIXEL_INFO_LIST , D );

                           PixelInfoList plistn = AttrUtil
                                 .getPixelInfoListValue(
                                       Attribute.PIXEL_INFO_LIST , D1 );
                           
                           int idd = plist.pixel( 0 ).ID( );
                           
                           if ( idd < minPixel_id )
                              minPixel_id = idd;

                           if ( idd > maxPixel_id )
                              maxPixel_id = idd;

                           if ( idd != plistn.pixel( 0 ).ID( ) )
                           {
                              D1.setAttribute( new PixelInfoListAttribute(
                                    Attribute.PIXEL_INFO_LIST ,
                                    new PixelInfoList( new DetectorPixelInfo(
                                                        idd , 
                                                        ( short ) r , 
                                                        ( short ) c ,
                                                         gridx ) ) ) );
                           }

                        }// for r/c =1 to nrows, ncols in grid
                  }// if gridx is a Uniform Grid

               }// for g=0 g< GridIDs.length

            }// if gridIDs != null

         }// if ds != null
      } // for i=start to nDataSets
      
      
      grids = Grids.toArray( new UniformGrid[ 0 ] );
      
      startPixel = minPixel_id;
      
      pixelGroup = new int[ maxPixel_id - minPixel_id + 1 ];
      
      FinishDataSet( DS );
      
      init( );

   }

   /**
    * 
    * @param DETCAlFileName
    * @param BankMapFile
    */
   public GroupSelector(String DETCAlFileName, String BankMapFile)
   {

      try
      {
         int[][] bankInfo = FileUtil.LoadBankFile( BankMapFile );
         Vector V = FileUtil.LoadDetCal( DETCAlFileName );

         DS = new DataSet( );
         L0 = ( ( Float ) V.elementAt( 1 ) ).floatValue( );
         T0 = ( ( Float ) V.elementAt( 2 ) ).floatValue( );
         
         grids = ( IDataGrid[] ) V.elementAt( 0 );

         int min_pixelID = Integer.MAX_VALUE;
         int max_pixelID = 0;
         
         for( int i = 0 ; i < bankInfo[4].length ; i++ )
         {
            if ( bankInfo[3][i] < min_pixelID )
               min_pixelID = bankInfo[3][i];
            
            if ( bankInfo[4][i] > max_pixelID )
               max_pixelID = bankInfo[4][i];

         }
         startPixel = min_pixelID;

         for( int i = 0 ; i < grids.length ; i++ )
         {
            int pixelID = bankInfo[3][findBankNum( grids[i].ID( ) , bankInfo )];

            DataSet ds = MakeDataSet( grids[i] , pixelID , L0 , T0 );

            if ( ds != null )
               for( int d = 0 ; d < ds.getNum_entries( ) ; d++ )
                  DS.addData_entry( ds.getData_entry( d ) );

         }
         
         pixelGroup = new int[ max_pixelID - min_pixelID + 1 ];
         // DS add information operators etc.

      } catch( Exception s )
      {
         JOptionPane.showMessageDialog( null , "Input Error:" + s );

         s.printStackTrace( );
         DS = null;
         CommandPanel = null;
         ThreeDPanel = null;
         DetectorPanel = null;
         grids = null;
         pixelGroup = null;
         L0 = T0 = Float.NaN;
      }

      FinishDataSet( DS );
      
      init( );
   }

   private DataSet MakeDataSet(IDataGrid grid, int startPixelID, float L0,
         float T0)
   {

      if ( grid == null )
         return null;

      if ( startPixelID < 0 )
         startPixelID = 0;

      if ( grid instanceof RowColGrid )
         grid = ( UniformGrid ) RowColGrid.GetDataGrid(
               ( RowColGrid ) grid , .1f );

      DataSet DS = new DataSet( );

      if ( !( grid instanceof UniformGrid ) )
         return null;
      
      UniformXScale xscl = new UniformXScale( 1000 , 1002 , 3 );
      
      int pixelID = startPixelID;
      
      for( int c = 1 ; c <= grid.num_cols( ) ; c++ )
         for( int r = 1 ; r <= grid.num_rows( ) ; r++ )
         {
            HistogramTable D = new HistogramTable( xscl , 
                                                  new float[]{ 20 , 0 }
                                                  , pixelID );
            
            D.setAttribute( new FloatAttribute( Attribute.INITIAL_PATH , L0 ) );
            D.setAttribute( new FloatAttribute( Attribute.T0_SHIFT , T0 ) );
            
            D.setAttribute( new PixelInfoListAttribute(
                                    Attribute.PIXEL_INFO_LIST ,
                                    new PixelInfoList(
                                         new DetectorPixelInfo( pixelID , 
                                                              ( short ) r ,
                                                              ( short ) c , 
                                                               grid ) ) ) );
            
            pixelID++ ;
            
            DS.addData_entry( D );
         }

      DS.setAttribute( new FloatAttribute( Attribute.INITIAL_PATH , L0 ) );
      DS.setAttribute( new FloatAttribute( Attribute.T0_SHIFT , T0 ) );

      grid.setData_entries( DS );

      DataSetTools.dataset.DataSetFactory.addOperators( DS );

      return DS;

   }

   public static int findBankNum(int GridID, int[][] bankInfo)
   {

      for( int i = 0 ; i < bankInfo[0].length ; i++ )
         
         if ( bankInfo[0][i] == GridID )
            
            return i;
      
      return -1;
   }

   /**
    * Sets up Units. Sets DetectorPosition Attribute. Sets up operators
    * 
    * @param DS
    *           the Dataset to be finished
    */
   private void FinishDataSet(DataSet DS)
   {

      detectorIDPixel = new int[ pixelGroup.length ];
      rowPixel = new int[ pixelGroup.length ];
      colPixel = new int[ pixelGroup.length ];

      DS.setX_units( "us" );
      DS.setX_label( "Time" );
      DS.setY_units( "Counts" );
      DS.setY_label( "Intensity" );

      for( int i = 0 ; i < grids.length ; i++ )
         Grid_util.setEffectivePositions( DS , grids[i].ID( ) );

      DataSetFactory.addOperators( DS );

      DS.addOperator( new GetPixelInfo_op( ) );
      DS.addOperator( new DiffractometerTofToD( ) );
      DS.addOperator( new DiffractometerTofToQ( ) );
      DS.addOperator( new DiffractometerTofToWavelength( ) );
      DS.addOperator( new DiffractometerTofToEnergy( ) );
      DS.addOperator( new pixelConversion( ) );
      DS.addOperator( new AssignGroupInfo( ) );

      for( int i = 0 ; i < DS.getNum_entries( ) ; i++ )
      {
         Data D = DS.getData_entry( i );
         
         PixelInfoList plist = AttrUtil.getPixelInfoListValue(
                                Attribute.PIXEL_INFO_LIST , 
                                D );
         
         if ( plist != null && plist.pixel( 0 ) != null )
         { // in case not new convention
            IPixelInfo pinf = plist.pixel( 0 );
            
            int row = ( int ) ( .5 + pinf.row( ) );
            int col = ( int ) ( .5 + pinf.col( ) );
            int det = pinf.DataGrid( ).ID( );
            
            int pixel = pinf.ID( );
            
            rowPixel[pixel - startPixel] = row;
            colPixel[pixel - startPixel] = col;
            
            detectorIDPixel[pixel - startPixel] = det;

         }

      }

   }

   /**
    * Sets up panels
    */
   private void init()
   {
      SelectedRegionsSav = new Hashtable[ grids.length ];
      
      GroupList = new Vector< Integer >( );
      
      LastGrid = grids[0].ID( );
      
      MakeMiddlePanel( );
      
      if ( DS != null )
      {
         DS.addIObserver( this );
         
         MakeLeftPanel( );
         
         MakeRightPanel( );
         
         DetectorPanel.addActionListener( this );
      }

   }

   
   private void addGroup(int Group, boolean add)
   {

      boolean findMax = false;
      
      if ( GroupList.contains( Group ) )
      {
         if ( !add )
         {
            GroupList.remove( new Integer( Group ) );
            
            if ( Group >= MaxGroupID )
               findMax = true;
            
         }
      } else if ( add )
      {
         GroupList.addElement( Group );
         
         if ( Group > MaxGroupID )
            MaxGroupID = Group;
         
      }

      if ( findMax )
         
         if ( GroupList.size( ) < 1 )
            
            MaxGroupID = 20;
      
         else
         {
            MaxGroupID = GroupList.elementAt( 0 );
            
            for( int i = 1 ; i < GroupList.size( ) ; i++ )
               
               if ( GroupList.elementAt( i ) > MaxGroupID )
                  
                  MaxGroupID = GroupList.elementAt( i );
         }

   }

   private void MakeMiddlePanel()
   {

      JFrame jf = new JFrame( "Select Pixel Groupings" );
      jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      
      Container jp = jf.getContentPane( );
      BoxLayout bl = new BoxLayout( jp , BoxLayout.Y_AXIS );
      jp.setLayout( bl );
      
      // ----------------------------------------
      JPanel TopMid = new JPanel( new GridLayout( 4 , 3 ) );
      
      TopMid.add( new JLabel( "Operation" , SwingConstants.LEFT ) );
      TopMid.add( new JLabel( "Detector(s)" , SwingConstants.CENTER ) );
      TopMid.add( new JLabel( "Group(s)" , SwingConstants.RIGHT ) );
      
      JButton but = new JButton( ASSIGN_GROUP );
      but.addActionListener( this );
      TopMid.add( but );
      Detectors_Gr = new FilteredPG_TextField( new IntListFilter( ) );
      TopMid.add( Detectors_Gr );      
      Group = new FilteredPG_TextField( new IntegerFilter( ) );
      TopMid.add( Group );

      but = new JButton( REMOVE_GROUP );
      but.addActionListener( this );
      TopMid.add( but );
      Detectors_Rmve = new FilteredPG_TextField( new IntListFilter( ) );
      TopMid.add( Detectors_Rmve );
      Detectors_Rmve.setToolTipText( "Leave blank to apply to ALL detectors" );
      RmvGroup = new FilteredPG_TextField( new IntegerFilter( ) );
      TopMid.add( RmvGroup );

      but = new JButton( FORMULA_ASSIGN );
      but.addActionListener( this );
      but.setToolTipText( "Uses formula below. If >0 true,else false" );
      TopMid.add( but );
      Detectors_Formula = new FilteredPG_TextField( new IntListFilter( ) );
      TopMid.add( Detectors_Formula );
      Detectors_Formula
            .setToolTipText( "Leave blank to apply to ALL detectors" );
      
      FormulaGroup = new FilteredPG_TextField( new IntegerFilter( ) );
      TopMid.add( FormulaGroup );
      TopMid.setBorder( new LineBorder( Color.black , 1 ) );

      jp.add( TopMid );
      // -------------------------------------------------------
      JPanel MidMid = new JPanel( new GridLayout( 1 , 2 ) );
      MidMid.add( new JLabel( "Formula" ) );

      formula = new JComboBox( );
      MidMid.add( formula );
      formula
            .setToolTipText( "<html><body>Use variable d,q,wl,row,col,det,pix. <BR>"
                  + "If result >0 or true, the pixel will be assigned to the Group"
                  + "</body></html>" );
      jp.add( MidMid );
      // ---------------------------------------------------

      jp.add( Box.createVerticalGlue( ) );
      // ---------------------------------------
      JPanel MidMidLow = new JPanel( );
      bl = new BoxLayout( MidMidLow , BoxLayout.X_AXIS );
      MidMidLow.setLayout( bl );
      
      but = new JButton( SET_DETECTOR_SELECTS );
      MidMidLow.add( but );
      but.addActionListener( this );
      
      MidMidLow.add( Box.createHorizontalGlue( ) );
      
      but = new JButton( CLEAR_DETECTOR_SELECTS );
      MidMidLow.add( but );
      but.addActionListener( this );
      
      MidMidLow.setBorder( new TitledBorder( new LineBorder( Color.black , 1 ) ,
            "Detector Panel Selections" ) );
      
      jp.add( MidMidLow );

      // --------------------------------------
      JPanel MidLow = new JPanel( new GridLayout( 1 , 3 ) );
      but = new JButton( SAVE_GROUPS );
      but.addActionListener( this );
      MidLow.add( but );

      ViewGroup = new JCheckBox( VIEW_GROUPS , false );
      ViewGroup.addActionListener( this );
      MidLow.add( ViewGroup );

      but = new JButton( SHOW_GROUPS );
      but.addActionListener( this );
      MidLow.add( but );

      jp.add( MidLow );
      
      //-------------------------Set up Help on JMenuBar-----------
      
      JMenuBar jfMenBar = new JMenuBar();
      JMenu HelpMenu = new JMenu(" Help");
      jfMenBar.add( HelpMenu);
      HelpMenu.addMenuListener( new  MenuListener()
      {
         
         @Override
         public void menuCanceled(MenuEvent arg0)
         {
            
         }

         @Override
         public void menuDeselected(MenuEvent arg0)
         {

            
         }

         @Override
         public void menuSelected(MenuEvent arg0)
         {

           new Browser( DataSetTools.util.FilenameUtil.helpDir(
                 "GroupSelector.html"));
           
         }
      });
      jf.setJMenuBar( jfMenBar);
      jf.setBounds( 500 , 0 , 500 , 400 );
      
      WindowShower.show( jf );

   }

   private void MakeLeftPanel()
   {

      if ( DS == null )
      {
         LeftFrame = null;
         return;
      }

      LeftFrame = new JFrame( "Three D Detector View" );
      
      LeftFrame.getContentPane( ).setLayout( new GridLayout( 1 , 1 ) );
      
      LeftFrame.setBounds( new Rectangle( 0 , 0 , 500 , 600 ) );
      
      ThreeDPanel = new ThreeDView( DS , null );
      
      LeftFrame.getContentPane( ).add( ThreeDPanel );
      
      WindowShower.show( LeftFrame );

   }

   private void MakeRightPanel()
   {

      if ( DS == null )
      {
         RightFrame = null;
         return;
      }

      RightFrame = new JFrame( "2D One Detector view" );
      RightFrame.getContentPane( ).setLayout( new GridLayout( 1 , 1 ) );
      RightFrame.setBounds( new Rectangle( 1000 , 0 , 600 , 400 ) );

      VirtArray2D = new RowColTimeVirtualArray( DS , 1000f , false , false ,
            null );
      VirtArray2D.ReverseY = true;
      VirtArray2D.getSharedControls( );// So they exist and are not null

      VirtArray2D.setAxisInfo( AxisInfo.Y_AXIS , 0 , 20 , "Intensity" ,
            "Counts" , AxisInfo.LINEAR );

      DetectorPanel = new ImageViewComponent( VirtArray2D , new ObjectState( ) );
      VirtArray2D.addDataChangeListener( this );

      ObjectState objState = DetectorPanel.getObjectState( false );
      if ( !objState.reset( ImageViewComponent.IMAGEJPANEL + "."
            + ImageJPanel2.MAXDATA , MaxIntensity ) )
         objState.insert( ImageViewComponent.IMAGEJPANEL + "."
               + ImageJPanel2.MAXDATA , MaxIntensity );
      if ( !objState.reset( ImageViewComponent.IMAGEJPANEL + "."
            + ImageJPanel2.MINDATA , 0 ) )
         objState.insert( ImageViewComponent.IMAGEJPANEL + "."
               + ImageJPanel2.MINDATA , 0 );

      if ( !objState.reset( ImageViewComponent.IMAGEJPANEL + "."
            + ImageJPanel2.AUTOSCALE , false ) )
         objState.insert( ImageViewComponent.IMAGEJPANEL + "."
               + ImageJPanel2.AUTOSCALE , false );

      DetectorPanel.setObjectState( objState );
      
      JPanel ControlPanel = new JPanel( );
      BoxLayout bl = new BoxLayout( ControlPanel , BoxLayout.Y_AXIS );

      ControlPanel.setLayout( bl );

      ViewControl[] VC = DetectorPanel.getControls( );
      for( int i = 0 ; i < VC.length ; i++ )
         
         if ( !( VC[i] instanceof PanViewControl ) )
            
            if ( ( i != 3 ) && ( i != 6 ) )
               
               ControlPanel.add( VC[i] );

      SelectionEditor = ( ControlCheckboxButton ) VC[5];// disabled at times
      GroupSelectorControl = VC[7];
      GroupEditor = ( ButtonControl ) VC[8];
      GroupEditor.setToolTipText(  "Only Group names that represent Integers are used" );
      
      SplitPaneWithState splitPane = new SplitPaneWithState(
                                        JSplitPane.HORIZONTAL_SPLIT , 
                                        DetectorPanel.getDisplayPanel( ) ,
                                        ControlPanel ,
                                        .80f );

      RightFrame.getContentPane( ).add( splitPane );
      
      WindowShower.show( RightFrame );
      
      splitPane.setDividerLocation( .80 );

   }

   private void RestoreSelections(int gridID)
   {

      if ( gridID == LastGrid && LastViewGroupMode == ViewGroup.isSelected( ) )
         return;

      if ( ViewGroup.isSelected( ) )
         return;

      int k;

      for( k = 0 ; k < grids.length && grids[k].ID( ) != gridID ; k++ )
      {
      }

      if ( !ViewGroup.isSelected( ) )
         if ( k >= 0 && k < grids.length )// restore selections for new grid
         {
            Hashtable table = SelectedRegionsSav[k];
            
            if ( table != null && table.size( ) > 1 )
            {
               Enumeration keys = table.keys( );
               Object Key = null;
               
               for( ; keys.hasMoreElements( ) ; )
               {
                  Key = keys.nextElement( );
                  
                  Object obj = table.get( Key );
                  
                  DetectorPanel.setSelectedRegions(
                        ( RegionOpListWithColor ) obj , ( String ) Key );
               }
            }
         }
      
      DetectorPanel.setAutoScale( false );
      
      DetectorPanel.getDisplayPanel( ).repaint( );
      
      LastGrid = gridID;
      
      LastViewGroupMode = ViewGroup.isSelected( );

   }

   @Override
   public void actionPerformed(ActionEvent arg0)
   {

      if ( arg0.getActionCommand( ).equals( "DataChange" ) )
      {         
         DetectorPanel.dataChanged( VirtArray2D );// Removes Selections
         
         int gridID = VirtArray2D.getDetNum( );

         if ( gridID == LastGrid && LastViewGroupMode == ViewGroup.isSelected( ) )
            return;

         int k;

         for( k = 0 ; k < grids.length && grids[k].ID( ) != gridID ; k++ )
         {
         }

         if ( !ViewGroup.isSelected( ) )
            if ( k >= 0 && k < grids.length )// restore selections for new grid
            {
               Hashtable table = SelectedRegionsSav[k];
               
               if ( table != null && table.size( ) > 1 )
               {
                  Enumeration keys = table.keys( );
                  Object Key = null;
                  
                  for( ; keys.hasMoreElements( ) ; )
                  {
                     Key = keys.nextElement( );
                     Object obj = table.get( Key );
                     
                     DetectorPanel.setSelectedRegions(
                           ( RegionOpListWithColor ) obj , ( String ) Key );
                  }
               }
            }

         DetectorPanel.setAutoScale( false );
         
         DetectorPanel.getDisplayPanel( ).repaint( );
         
         LastGrid = gridID;
         
         LastViewGroupMode = ViewGroup.isSelected( );

      } else if ( arg0.getActionCommand( )
            .equals( IObserver.POINTED_AT_CHANGED ) )
      {

         floatPoint2D X1 = DetectorPanel.getPointedAt( );
         
         SelectedData2D X = new SelectedData2D( ( int ) ( X1.y + .5 ) - 1 ,
               ( int ) ( X1.x + .5 ) - 1 ,
               ( ( RowColTimeVirtualArray ) VirtArray2D ).getTime( 1 , 1 ) );

         int Group = VirtArray2D.getGroupIndex( X );
         float Time = VirtArray2D.getTime( X );
         
         if ( Group < 0 )
            return;
         
         if ( Float.isNaN( Time ) )
            return;
         

         DS.setPointedAtX( Time );
         DS.setPointedAtIndex( Group );
         DS.notifyIObservers( IObserver.POINTED_AT_CHANGED );

         ThreeDPanel.redraw( IObserver.POINTED_AT_CHANGED );

         return;
      }
      if ( arg0.getActionCommand( ) == ASSIGN_GROUP )
      {
         try
         {
            int group = Integer.parseInt( Group.getText( ).trim( ) );
            
            if ( group > MaxGroupID )
               MaxGroupID = group;

            int[] detectors = IntList.ToArray( Detectors_Gr.getText( ).trim( ) );
            
            if ( detectors == null || detectors.length < 1 )
            {
               JOptionPane.showMessageDialog( null ,
                     "No detectors specified properly" );
               return;
            }

            for( int i = 0 ; i < detectors.length ; i++ )
            {
               int id = detectors[i];
               
               for( int j = 0 ; j < detectorIDPixel.length ; j++ )
                  
                  if ( id == detectorIDPixel[j] )
                     
                     pixelGroup[j] = group;
            }

            addGroup( group , true );

         } catch( Exception s )
         {
            JOptionPane.showMessageDialog( null ,
                  "Improper Group. Did not Assign" );
         }
         return;
      }

      if ( arg0.getActionCommand( ) == REMOVE_GROUP )
      {
         int group = Integer.parseInt( RmvGroup.getText( ).trim( ) );
         
         int[] detectors = IntList.ToArray( Detectors_Rmve.getText( ).trim( ) );
         
         boolean groupLeft = false;
         
         for( int i = 0 ; i < pixelGroup.length ; i++ )
            
            if ( pixelGroup[i] == group )
               
               if ( detectors == null || detectors.length < 1 )
               {
                  pixelGroup[i] = 0;
                  
                  addGroup( group , false );
                  
               } else
               {
                  for( int j = 0 ; j < detectors.length ; j++ )
                     
                     if ( detectorIDPixel[i] == detectors[j] )
                        
                        pixelGroup[i] = 0;
                  

                  if ( pixelGroup[i] == group )
                     
                     groupLeft = true;
               }

         if ( !groupLeft )
            
            addGroup( group , false );

         return;
      }

      if ( arg0.getActionCommand( ) == FORMULA_ASSIGN )
      {
         return;
      }

      if ( arg0.getActionCommand( ) == VIEW_GROUPS )
      {
         JCheckBox chBox = ( JCheckBox ) arg0.getSource( );
         
         float mult = 0;
         float maxIntensity = 0;

         if ( chBox.isSelected( ) )
            mult = MaxIntensity / MaxGroupID;
         
         DS.setAttribute( new FloatAttribute( "GroupIntesityMult" ,
               MaxIntensity / MaxGroupID ) );

         boolean MaxGroupIDpresent = false;
         
         for( int i = 0 ; i < grids.length ; i++ )
         {
            int ID = grids[i].ID( );
            
            for( int j = 0 ; j < detectorIDPixel.length ; j++ )
               
               if ( detectorIDPixel[j] == ID )
               {
                  int row = rowPixel[j];
                  int col = colPixel[j];
                  
                  int intensity = ( int ) ( pixelGroup[j] * mult );
                  
                  if ( pixelGroup[j] == MaxGroupID )
                     MaxGroupIDpresent = true;
                  
                  Data D = grids[i].getData_entry( row , col );
                  
                  float[] ys = D.getY_values( );
                  
                  ys[1] = intensity;
                  
                  if ( ys[1] > maxIntensity )
                     
                     maxIntensity = ys[1];
               }
         }
         
         DS.setPointedAtX( 1000.5f + mult );
         VirtArray2D.setTime( 1000.5f + mult );
         
         ThreeDPanel.redraw( IObserver.POINTED_AT_CHANGED );
         
         DetectorPanel.dataChanged( );// VirtArray2D will cause this to happen
         
         if ( mult <= 0 )
            
            RestoreSelections( VirtArray2D.getDetNum( ) );

         LastViewGroupMode = mult > 0;
         
         if ( !MaxGroupIDpresent )
            addGroup( MaxGroupID , false );
         
         int gridID = VirtArray2D.getDetNum( );
         
         if ( mult > 0 )// will be showing Group view
         {
            SaveSelections( gridID , true , true );// new ViewGroup value is th
            // incorrect one to use
            setEnabled( GroupEditor , false );
            setEnabled( GroupSelectorControl , false );
            setEnabled( SelectionEditor , false );

         } else
         // Will be showing DataView
         {
            RestoreSelections( gridID );
            setEnabled( GroupEditor , true );
            setEnabled( GroupSelectorControl , true );
            setEnabled( SelectionEditor , true );
         }

         DetectorPanel.setAutoScale( false );
         
         DetectorPanel.getDisplayPanel( ).repaint( );
         return;
      }
      if ( arg0.getActionCommand( ) == SET_DETECTOR_SELECTS
            || arg0.getActionCommand( ) == CLEAR_DETECTOR_SELECTS )
      {
         int sgn = 1;
         
         if ( arg0.getActionCommand( ) == CLEAR_DETECTOR_SELECTS )
            sgn = 0;

         SaveSelections( VirtArray2D.getDetNum( ) , false , false );

        // Arrays.fill( pixelGroup , 0 );
         
         for( int i = 0 ; i < grids.length ; i++ )
         {
            IDataGrid grid = grids[i];
            int ID = grid.ID( );
            
            Hashtable GridSelections = SelectedRegionsSav[i];
            
            if ( GridSelections != null )
            {
               Enumeration keys = GridSelections.keys( );

               while( keys.hasMoreElements( ) )
               {
                  Object Key = keys.nextElement( );
                  if ( Key instanceof String )
                     try
                     {
                        int group = sgn * Integer.parseInt( ( String ) Key );

                        RegionOpListWithColor oplist = ( RegionOpListWithColor ) GridSelections
                              .get( Key );

                        int nrows = grid.num_rows( );
                        int ncols = grid.num_cols( );
                        
                        CoordTransform world_to_array = new CoordTransform( );
                        
                        world_to_array.setDestination( new CoordBounds( 0f ,
                                                                        0f ,
                                                                        ncols , 
                                                                        nrows ) );
                        
                        world_to_array.setSource( new CoordBounds( .5f ,
                                                                   .5f ,
                                                                   ncols + .5f , 
                                                                   nrows + .5f ) );

                        Point[] pts = oplist.getSelectedPoints( world_to_array );

                        
                                              
                        if ( pts != null && pts.length > 1 )
                           
                           for( int k = 0 ; k < detectorIDPixel.length ; k++ )
                              
                              if ( detectorIDPixel[k] == ID )
                              {
                                 int p;
                                 
                                 for( p = 0 ; p < pts.length
                                       && ( ( pts[p].x + 1 ) != colPixel[k] || ( pts[p].y + 1 ) != rowPixel[k] ) ; p++ )
                                 {
                                 }

                                 if ( p < pts.length )
                                 {
                                    pixelGroup[k] = group;
                                   
                                 }
                              }

                        if ( group > 0 && pts != null && pts.length > 1 )
                           
                           addGroup( group , true );
                        

                     } catch( Exception s )
                     {
                        // Not a numeric group
                     }
               }
               // Should delete choices but may want to add.
               // Should not show selected in View mode
               // In view mode should disable ability to select
            }

         }// for i 0 .. ngrids

         boolean MaxGroupIDUsed = false;
         
         for( int k = 0 ; k < pixelGroup.length && !MaxGroupIDUsed ; k++ )
            
            if ( pixelGroup[k] == MaxGroupID )
               
               MaxGroupIDUsed = true;

         if ( !MaxGroupIDUsed )
            
            addGroup( MaxGroupID , false );

      } else if ( arg0.getActionCommand( ) == SHOW_GROUPS )
      {
         Hashtable< String , Integer > GroupVsPixelList = getHashTable(
               pixelGroup , startPixel );
         
         ShowTable( GroupVsPixelList );

      }

   }

   private Hashtable< String , Integer > getHashTable(int[] Groups,
         int startPixel)
   {

      Hashtable< String , Integer > Res = new Hashtable< String , Integer >( );
      
      int lastGroup = -1;
      
      int firstPixel = startPixel;
      
      int maxDigits = ( int ) ( .5 + Math .log10
                               ( startPixel + Groups.length + 1 ) );
      
      String fmt = "%0" + maxDigits + "d";
      
      String S = "";
      
      for( int i = 0 ; i < Groups.length ; i++ )
      {
         int group = Groups[i];
         
         if ( lastGroup == group )
         {

         } else if ( lastGroup >= 0 )
         {

            S = String.format( fmt , firstPixel );
            
            if ( i - 1 + startPixel > firstPixel )
               S += ":" + String.format( fmt , i - 1 );
            
            Res.put( S , new Integer( lastGroup ) );
            
            lastGroup = group;
            
            firstPixel = i + startPixel;
            
         } else
         {
            lastGroup = group;
            
            S = "";
         }
      }
      
      
      if ( firstPixel - startPixel + 1 < Groups.length )
      {

         S = String.format( fmt ,firstPixel) + ":" + 
                    String.format( fmt ,( Groups.length - 1 ));
         
         Res.put( S , new Integer( lastGroup ) );
         
      }
      return Res;
   }

   private void ShowTable(Hashtable< String , Integer > GroupVsPixelList)
   {

      if ( GroupVsPixelList == null )
         System.out.println( " No List" );
      
      Enumeration< String > Keys = GroupVsPixelList.keys( );
      
      Vector< String > Pix = new Vector< String >( );
      
      Vector< Integer > Grp = new Vector< Integer >( );
      
      while( Keys.hasMoreElements( ) )
      {
         String Pixels = Keys.nextElement( );
         Pix.add( Pixels );
         
         Integer group = GroupVsPixelList.get( Pixels );
         Grp.add( group );
         
        
      }
      
      Integer[] GroupList = new Integer[ Pix.size( ) ];
      Integer[] PixList = new Integer[ Pix.size( ) ];
      
      for( int i = 0 ; i < GroupList.length ; i++ )
      {
         GroupList[i] = PixList[i] = i;
      }
      
      Arrays.sort( GroupList , new RankCompare( Grp , true ) );
      Arrays.sort( PixList , new RankCompare( Pix , false ) );
      
      FinishJFrame jf = new FinishJFrame( "Groupings" );
      jf.getContentPane( ).setLayout( new BorderLayout( ) );
      JTextArea text = new JTextArea( 50 , 25 );
      
      jf.getContentPane( ).add( new JScrollPane( text ) , BorderLayout.CENTER );
      
      //---------------------top panel
      JPanel top = new JPanel( );
      JCheckBox GrVsPix = new JCheckBox( "Show Group vs Pixel" , false );
      top.add( GrVsPix );
      GrVsPix.addActionListener( new RankButtonListener( text , GroupList ,
            Grp , Pix , true ) );

      JCheckBox PixVsGrp = new JCheckBox( "Show Pixel vs Group" , true );
      top.add( PixVsGrp );
      PixVsGrp.addActionListener( new RankButtonListener( text , PixList , Grp ,
            Pix , false ) );

      ButtonGroup bgrp = new ButtonGroup( );
      bgrp.add( PixVsGrp );
      bgrp.add( GrVsPix );
      
      jf.getContentPane( ).add( top , BorderLayout.NORTH );
      
      jf.setSize( 600 , 800 );
      
      WindowShower.show( jf );
      
      PixVsGrp.doClick( );

   }

   private void setEnabled(Container control, boolean enable)
   {

      if ( control == null )
         return;
      
      if ( control instanceof AbstractButton )
      {
         ( ( AbstractButton ) control ).setEnabled( enable );
         
         return;
      }
      if ( control instanceof JComboBox )
      {
         ( ( JComboBox ) control ).setEnabled( enable );
      }
      int n = control.getComponentCount( );
      
      for( int i = 0 ; i < n ; i++ )
      {
         Component c = control.getComponent( i );
         
         if ( c instanceof Container )
            setEnabled( ( Container ) c , enable );
      }

   }

   @Override
   public void update(Object observedObj, Object reason)
   {

      if ( reason.equals( IViewComponent.POINTED_AT_CHANGED )
            && !( observedObj instanceof DataSet ) )
      {
         floatPoint2D X1 = DetectorPanel.getPointedAt( );
         
         SelectedData2D X = new SelectedData2D( ( int ) ( X1.y + .5 ) - 1 ,
                                                 ( int ) ( X1.x + .5 ) - 1 ,
                  ( ( RowColTimeVirtualArray ) VirtArray2D ).getTime( 1 , 1 ) );

         int Group = VirtArray2D.getGroupIndex( X );
         float Time = VirtArray2D.getTime( X );

         if ( Group < 0 )
            return;
         
         if ( Float.isNaN( Time ) )
            return;

         DS.setPointedAtX( Time );
         DS.setPointedAtIndex( Group );
         
         DS.notifyIObservers( IObserver.POINTED_AT_CHANGED );
         ThreeDPanel.redraw( IObserver.POINTED_AT_CHANGED );

         return;
      }

      
      if ( reason.equals( IObserver.POINTED_AT_CHANGED ) && observedObj != null
            && ( observedObj instanceof DataSet ) )
      {

         DataSet ds = ( DataSet ) observedObj;
         int i = ds.getPointedAtIndex( );
         
         ThreeDPanel.redraw( IObserver.POINTED_AT_CHANGED );
         
         if ( i < 0 || i >= ds.getNum_entries( ) )
         {
            Detectors_Gr.setText( "" );
            Detectors_Rmve.setText( "" );
            Detectors_Formula.setText( "" );

            return;
         }

         Data D = ds.getData_entry( i );
         PixelInfoList plist = AttrUtil.getPixelInfoListValue(
               Attribute.PIXEL_INFO_LIST , D );

         if ( plist == null )
            return;

         IPixelInfo pinf = plist.pixel( 0 );
         
         if ( pinf == null )
            return;

         int gridID = pinf.DataGrid( ).ID( );
         
         if ( gridID == LastGrid )
            return;
         
         DetectorPanel.disableSelectionEditor( );
         DetectorPanel.setAutoScale( false );
         
         int k;
         for( k = 0 ; k < grids.length && grids[k].ID( ) != LastGrid ; k++ )
         {
         }
         
         if ( !ViewGroup.isSelected( ) )
            
            if ( k >= 0 && k < grids.length )// Save current selections
            {
               SelectedRegionsSav[k] = new Hashtable( );
               
               String[] names = DetectorPanel.getSelectionNames( );
               
               if ( names != null )
                  for( int ii = 0 ; ii < names.length ; ii++ )
                  {
                     SelectedRegionsSav[k].put(  names[ii] ,
                                                  Clone( DetectorPanel
                                                     .getSelectedRegions(
                                                            names[ii] ) ) );
                     
                     DetectorPanel.clearSelection( names[ii] );
                  }
            }

         VirtArray2D.setDetNum( gridID );
         
         Append( "" + gridID , Detectors_Gr );
         Append( "" + gridID , Detectors_Rmve );
         Append( "" + gridID , Detectors_Formula );
      }

   }

   /**
    * Save selection info in 2D Detector View
    * 
    * @param grid
    *           The grid number
    * @param remove
    *           remove Selection info from base( new detector is replacing the
    *           current one)
    * @param Save
    *           save this selection even it selection says Group View
    */
   private void SaveSelections(int grid, boolean remove, boolean Save)
   {

      int k;
      for( k = 0 ; k < grids.length && grids[k].ID( ) != LastGrid ; k++ )
      {
      }
      if ( !ViewGroup.isSelected( ) || Save )
         
         if ( k >= 0 && k < grids.length )// Save current selections
         {
            SelectedRegionsSav[k] = new Hashtable( );
            
            String[] names = DetectorPanel.getSelectionNames( );
            
            if ( names != null )
               for( int ii = 0 ; ii < names.length ; ii++ )
               {
                  SelectedRegionsSav[k].put( names[ii] ,
                                            Clone( DetectorPanel
                                                     .getSelectedRegions( 
                                                           names[ii] ) ) );
                  
                  if ( remove )
                     
                     DetectorPanel.clearSelection( names[ii] );
               }
         }
   }

   private RegionOpListWithColor Clone(RegionOpListWithColor opList)
   {

      RegionOpListWithColor newOpList = new RegionOpListWithColor( );
      
      newOpList.setColor( opList.getColor( ) );
      
      newOpList.setOpacity( opList.getOpacity( ) );
      
      Vector< RegionOp > list = opList.getList( );
      
      if ( list != null )
         
         for( int i = 0 ; i < list.size( ) ; i++ )
            
            newOpList.add( list.elementAt( i ) );

      return newOpList;

   }
   

   private void Append(String grid, FilteredPG_TextField txtArea)
   {

      String S = txtArea.getText( );
      
      if ( S == null )
         S = "";
      
      S = S.trim( );
      
      if ( S.length( ) > 0 && !S.endsWith( "," ) && !S.endsWith( ":" ) )
         return;
      
      txtArea.setText( S + grid );
   }
   

   /**
    *  Returns filename if the file with filename does exist. Otherwise null is 
    *  returned
    *  
    * @param filename   The name of the file
    * 
    * @return filename if the file with filename does exist. Otherwise null is 
    *  returned
    */
   public static String Fix( String filename )
   {
      if( filename == null || filename.trim().length() < 1 )
         
         return null;
      
      if( (new File( filename)).exists())
         return filename;
      
      return null;
   }
   
   /**
    * @param args
    */
   public static void main(String[] args)
   {

      String NeXusFile = "C:/ISAW/SampleRuns/SNS/SNAP/SNAP_240.nxs";
      String DetCalFile = "C:/ISAW/InstrumentInfo/SNS/SNAP/SNAP.DetCal";
      String BankFile = "C:/ISAW/InstrumentInfo/SNS/SNAP/SNAP_bank.xml";
      
      if ( args != null && args.length == 1 )// Use Test data if arg is 1 or 2
         
         try
         {
            int k = Integer.parseInt( args[0].trim( ) );
            if ( k == 1 )
               DetCalFile = null;
            else if ( k == 2 )
               NeXusFile = null;
            else
               NeXusFile = DetCalFile = BankFile = null;

         } catch( Exception s1 )
         {
            NeXusFile = args[0];
            DetCalFile = BankFile = null;
         }
      if( args.length ==2)
      {
         NeXusFile = null;
         DetCalFile = args[0];
         BankFile = args[1];
      }
      
      NeXusFile = GroupSelector.Fix( NeXusFile );

      DetCalFile = GroupSelector.Fix( DetCalFile );
      BankFile = GroupSelector.Fix( BankFile );
      
      if ( ( NeXusFile == null && DetCalFile == null ) || args == null
            || args.length < 1 )
      {
         JPanel pan = new JPanel( new GridLayout( 4 , 2 ) );

         JCheckBox UseNexFile = new JCheckBox( "Geometry Info in NeXus File" ,
               false );

         FileChooserPanel NexFile = new FileChooserPanel(
               FileChooserPanel.LOAD_FILE );
         FileChooserPanel DetCalFileC = new FileChooserPanel(
               FileChooserPanel.LOAD_FILE );
         FileChooserPanel BankFileC = new FileChooserPanel(
               FileChooserPanel.LOAD_FILE );

         pan.add( UseNexFile );
         pan.add( new JLabel( ) );
         pan.add( new JLabel( "NeXus file Name(if top option checked" ) );
         pan.add( NexFile );
         pan.add( new JLabel( "Detector Position(DetCal) file" ) );
         pan.add( DetCalFileC );
         pan.add( new JLabel( "Bank Info file" ) );
         pan.add( BankFileC );

         UseNexFile.addActionListener( new EnableDisableActionListener(
               EnableDisableActionListener.Add( null , NexFile ) ,
               EnableDisableActionListener.Add( EnableDisableActionListener
                     .Add( null , DetCalFileC ) , BankFileC ) ) );
         UseNexFile.doClick( );

         if ( JOptionPane.showConfirmDialog( null , pan , "Input File(s)" ,
               JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION )
         {
            NeXusFile = NexFile.getTextField( ).getText( );
            DetCalFile = NexFile.getTextField( ).getText( );
            BankFile = BankFileC.getTextField( ).getText( );
            if ( UseNexFile.isSelected( ) )
               DetCalFile = null;
            else
               NeXusFile = null;

         } else
         {
            System.out.println( " No files chosen" );
            System.exit( 0 );
         }

      }else
         NeXusFile = null;

      //GroupSelector G;
      if ( NeXusFile != null )

          new GroupSelector( NeXusFile );

      else

         new GroupSelector( DetCalFile , BankFile );

   }

   class AssignGroupInfo extends DS_Attribute implements IDataBlockInfo
   {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public AssignGroupInfo()
      {

         super( "Group" );
      }

      public String getCommand()
      {

         return "Group";
      }

      @Override
      public Object getResult()
      {

         DataSet DS = getDataSet( );

         if ( DS == null )
            return -1;

         int i = ( ( IntegerPG ) getParameter( 0 ) ).getintValue( );
         if ( i < 0 || i >= DS.getNum_entries( ) )
            return -1;

         Float V = ( Float ) DS.getAttributeValue( "GroupIntesityMult" );
         if ( V == null || V.floatValue( ) == 0 )
            return DS.getData_entry( i ).getY_values( )[1];

         return DS.getData_entry( i ).getY_values( )[1] / V.floatValue( );
      }

      @Override
      public void setDefaultParameters()
      {

         this.clearParametersVector( );
         addParameter( new IntegerPG( "Data Block index" , 1 ) );

      }

      @Override
      public String DataInfo(int i)
      {

         DataSet DS = getDataSet( );

         if ( DS == null )
            return "NaN";

         if ( i < 0 || i >= DS.getNum_entries( ) )
            return "NaN";

         Float V = ( Float ) DS.getAttributeValue( "GroupIntesityMult" );
         if ( V == null || V.floatValue( ) == 0 )
            return "" + DS.getData_entry( i ).getY_values( )[1];

         return "" + DS.getData_entry( i ).getY_values( )[1] / V.floatValue( );

      }

      @Override
      public String DataInfoLabel(int i)
      {

         return "Group";
      }

   }

   class pixelConversion extends DS_Attribute implements IDataBlockInfo
   {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public pixelConversion()
      {

         super( "pixel& Grid IDs" );

      }

      public pixelConversion(DataSet DS, int dataBlock)
      {

         this( );
         getParameter( 0 ).setValue( dataBlock );
         setDataSet( DS );
      }

      @Override
      public String getCommand()
      {

         return "pixel_grid_ids";
      }

      @Override
      public Object getResult()
      {

         int i = ( ( IntegerPG ) getParameter( 0 ) ).getintValue( );
         try
         {
            return Integer.parseInt( DataInfo( i ).trim( ) );
         } catch( Exception s )
         {
            return -1;
         }

      }

      @Override
      public void setDefaultParameters()
      {

         this.clearParametersVector( );
         addParameter( new IntegerPG( "Data Block index" , 1 ) );

      }

      @Override
      public String DataInfo(int i)
      {

         DataSet DS = getDataSet( );
         if ( DS == null )
            return "NaN";

         if ( i < 0 || i >= DS.getNum_entries( ) )
            return "NaN";

         PixelInfoList plist = AttrUtil.getPixelInfoListValue(
               Attribute.PIXEL_INFO_LIST , DS.getData_entry( i ) );
         if ( plist == null )
            return "NaN, NaN";

         IPixelInfo pinf = plist.pixel( 0 );
         if ( pinf == null )
            return "NaN,NaN";

         return "" + pinf.ID( ) + "," + pinf.DataGrid( ).ID( );

      }

      @Override
      public String DataInfoLabel(int i)
      {

         return "pixel,grid ID";
      }

   }

   class RankCompare implements Comparator< Integer >
   {

      Vector  V;

      boolean isGroup;

      public RankCompare(Vector V, boolean isGroup)
      {

         this.V = V;
         this.isGroup = isGroup;
      }

      @Override
      public int compare(Integer o1, Integer o2)
      {

         if ( o1 == null )
            if ( o2 == null )
               return 0;
            else
               return -1;
         else if ( o2 == null )
            return 1;

         Object O1 = V.elementAt( o1.intValue( ) );
         Object O2 = V.elementAt( o2.intValue( ) );
         if ( O1 == null )
            if ( O2 == null )
               return 0;
            else
               return -1;
         else if ( O2 == null )
            return 1;
         if ( O1 instanceof String )
            return ( ( String ) O1 ).compareTo( ( String ) O2 );

         if ( O1 instanceof Integer )
         {
            int i1 = ( ( Integer ) O1 ).intValue( );
            int i2 = ( ( Integer ) O2 ).intValue( );
            if ( i1 < i2 )
               return -1;
            else if ( i1 > i2 )
               return 1;
            return 0;
         }

         return 0;
      }

   }

   class RankButtonListener implements ActionListener
   {

      JTextArea         text;

      Integer[]         PixList;

      Vector< Integer > Grp;

      Vector< String >  Pix;

      boolean           isGroup;

      public RankButtonListener(JTextArea text, Integer[] PixList,
            Vector< Integer > Grp, Vector< String > Pix, boolean isGroup)
      {

         this.text = text;
         this.PixList = PixList;
         this.Grp = Grp;
         this.Pix = Pix;
         this.isGroup = isGroup;

      }

      @Override
      public void actionPerformed(ActionEvent arg0)
      {

         text.setText( "" );
         if ( PixList == null || Grp == null || Pix == null )
         {
            text.setText( "No Data " );
            return;
         }
         if ( PixList.length != Grp.size( ) || PixList.length != Pix.size( ) )
         {
            text.setText( "Improper data" );
            return;
         }

         int size = PixList.length;
         for( int i = 0 ; i < PixList.length ; i++ )
         {
            int k = PixList[i].intValue( );
            if ( k >= 0 || k < size )
            {
               int grp = Grp.elementAt( k ).intValue( );
               String Pix1 = Pix.elementAt( k );
               if ( isGroup )
                  text.append( String.format( "%4d  %s\n" , grp , Pix1 ) );
               else
                  text.append( String.format( "%15s  %4d\n" , Pix1 , grp ) );
            }

         }

      }

   }
}
