

/* 
 * File:  DataSetGRCTArrayMaker.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.9  2004/08/04 22:11:17  rmikk
 * Made the pointed at more robust and accurate.
 * Incorporated and tested the ObjectState into this module
 *
 * Revision 1.8  2004/07/29 19:44:56  rmikk
 * Fixed some errors
 * Can now change the number of bins and bin ranges for All? dimensions
 *
 * Revision 1.7  2004/07/29 15:40:39  rmikk
 * Fixed some javadoc errors
 * Fixed an off by one error
 *
 * Revision 1.6  2004/07/29 13:50:30  rmikk
 * Improved the results of the pointed At performance
 * redraw from an external event implemented
 * AxisInfo updated to reflect real axis information
 * The layout of the dimension chooser was changed
 *
 * Revision 1.5  2004/07/28 18:20:57  rmikk
 * Incorporated Object state
 * Now does pointed At
 *
 * Revision 1.4  2004/05/26 15:52:32  rmikk
 * Fixed off by one errors
 * Fixed row/col mixup between the two models
 *
 * Revision 1.3  2004/05/24 13:47:19  rmikk
 * Added Documentation
 * Implemented change in the number of steps on the XScaleUserUI
 *
 * Revision 1.2  2004/05/18 13:58:07  rmikk
 * -Added more titles to the Animation controllers
 * -Time channels now done correctly
 * -Size of the dimension Frame decreased
 *
 * Revision 1.1  2004/05/17 13:55:08  rmikk
 * Initial Checkin
 *
 */
package DataSetTools.viewer;


import DataSetTools.dataset.*;
import java.util.*;
import java.awt.event.*;
import gov.anl.ipns.ViewTools.UI.*;
import DataSetTools.components.ui.*;
//import DataSetTools.viewer.*;
//import gov.anl.ipns.ViewTools.*;
import javax.swing.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import java.awt.*;
import gov.anl.ipns.Util.Sys.*;
//import DataSetTools.viewer.Table.*;
//import gov.anl.ipns.ViewTools.Components.Transparency.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Messaging.*;

/**
 *  This class is an array producer for Gridded data sets.  Row, column, time
 *  grid and DataSet are the choices for dimensions.  Any one dimension can 
 *  represent a display row and any other one can represent a display column. 
 *  The other dimensions( if more than 1 ) have animation controllers to step 
 *   through their values
 */
public class DataSetGRCTArrayMaker  implements IArrayMaker_DataSet, 
                                               IVirtualArray2D, IPreserveState {

    DataSet[] DataSets;
    
    ObjectState state;
    String[] Title1 ={"Time","Row", "Col","Grid", "DataSet"
                      };
    IHandler[] Handler = new IHandler[ 5 ] ;
    float[] pixel_min;//4 is DS Num,3-GridNum, 2-col,1-row, 0 is time indicies
    float[] pixel_max;// index in given dimension
    int[] GridNums;

    float MinTime, 
          MaxTime;
    int NtimeChan = 0;// Number of channels over all, 0 use channels
    int NrowChan = 0;// Number of row channels
    int NColChan = 0;// Number of col channels
    int NDssChan = 0;// Number of data set channels
    int NGridChan = 0;// Number of grid Channels
    int MaxChannels, 
        MaxRows, 
        MaxCols;

    int[] Permutation; //Permutation[0] is cols, Permuation[1] is rows
    AnimationController[]  ACS;
    XScaleChooserUI[] Xscales;
    DataSetXConversionsTable  DSConversions;
    JPanel[] savXScales = null;
    MyJPanel order;
    int NstepDims = 5;
    //----------------- GUI Elements-----------------
    JButton Dimension;
   
    ViewControl[] AnimXscls;
   
    /**
     *  Constructor
     *  @param DSS  the array of DataSets to view
     *  @param state  The viewer State
     */
    public DataSetGRCTArrayMaker( DataSet[] DSS, ObjectState state ) {
        this.state = state;
        DataSets = DSS;
        init();
    }

    /**
     *  Constructor
     *  @param DS  the DataSet to view
     *  @param state  The viewer State
     */

    public DataSetGRCTArrayMaker( DataSet DS, ObjectState state ) {
        this.state = state;
        DataSets = new DataSet[ 1 ] ;
        DataSets[ 0 ]  = DS;
        init();
    }


   float[] initPixel_min = { 0f,1f,1f,0f,0f };
   float[] initPixel_max = {1f,2f,2f,1f,1f};
   int[] initPermutation ={0 , 1, 2, 3, 4};

    // Initializes min's and max's in each dimension( DS,Grid,Col,Row and time)
    //   and initializes all controls, and dimension handlers. The Object state has
    // not been initialized yet
    private void init() {
        //----------- Get ranges of rows,cols,etc in the set of data sets-----
        Vector V = new Vector();

        MaxChannels = -1;
        MaxRows = -1;
        MaxCols = -1;
        MinTime = Float.POSITIVE_INFINITY;
        MaxTime = Float.NEGATIVE_INFINITY;
     
        for ( int i = 0; i < DataSets.length; i++ ) {
            int[] grid = NexIO.Write.NxWriteData.getAreaGrids( DataSets[ i ] );

            if ( grid != null )
                for ( int k = 0; k < grid.length; k++ ) {
                    if ( V.indexOf( new Integer( grid[ k ] ) ) < 0 ) {
                        V.addElement( new Integer( grid[ k ] ) );
                    }

                    IDataGrid G = NexIO.Write.NxWriteData.getAreaGrid( 
                                                DataSets[ i ] , grid[ k ] );

                    if ( G.num_rows() > MaxRows ) 
                        MaxRows = G.num_rows();

                    if ( G.num_cols() > MaxCols )
                        MaxCols = G.num_cols();

                    XScale D = G.getData_entry( 1, 1 ).getX_scale();

                    if ( D.getNum_x() > MaxChannels )
                        MaxChannels = D.getNum_x();

                    if ( D.getStart_x() < MinTime )
                        MinTime = D.getStart_x();

                    if ( D.getEnd_x() > MaxTime )
                        MaxTime = D.getEnd_x();
      
                }
        }
        //-------------Set the array of Grid Nums ------------------
        GridNums = new int[ V.size() ] ;
        for ( int i = 0; i < V.size(); i++ )
            GridNums[ i ]  = ( ( Integer) ( V.elementAt( i ) ) ).intValue();
        java.util.Arrays.sort( GridNums );        
       

        //--------------Set up Controls --------------------   
        // Eliminate cases so do not step thru dimensions of length 2
     
        
        if( state == null)
           state = getObjectState( true);
        SetUpVariables( state);
        ACS = new AnimationController[ 5 ] ;
        Xscales = new XScaleChooserUI[ 5 ] ;
        if ( DataSets.length < 2 ) {

            NstepDims--;
            pixel_min[ 4 ]  = 0;
            pixel_max[ 4 ]  = 1;
            ACS[ 4 ]  = null;
            Xscales[ 4 ]  = null; 
     
        } else {

            int start =0;
            if( state != null)
               start = this.getInt((Integer) state.get("MinDSindx"),0);
            int end =DataSets.length;
            if( state != null)
              end = this.getInt((Integer) state.get("MaxDSindx"),end);
            int nsteps = 0;
            if( state != null)
                nsteps = this.getInt((Integer) state.get("NDssChan"),nsteps);
            
            ACS[ 4 ]  = new AnimationController();
            Xscales[ 4 ]  = new XScaleChooserUI( "DataSet", "index", start,
                                                  end, nsteps );

            Xscales[ 4 ] .addActionListener( new DataSetXsclActionListener() );
            float[ ] xvals = new float[ end-start ] ;
               
            for ( int i = start; i < end; i++ )
                xvals[ i-start ]  = ( float) i;
            ACS[ 4 ] .setFrame_values( xvals );
            ACS[ 4 ] .setBorderTitle( "DataSet" );
            ACS[ 4 ] .setTextLabel( "DataSet" );
            ACS[ 4 ] .addActionListener( new DataSetAnimActionListener() );

        }
    
        if ( GridNums.length < 2 ) {

            Permutation[ 4 ]  = 3;
            Permutation[ 3 ]  = 4;
            NstepDims--;
            pixel_min[ 3 ]  = 0;
            pixel_max[ 3 ]  = 1;
            ACS[ 3 ]  = null;
            Xscales[ 3 ]  = null;

        } else {
             int start =0;
             if( state != null)
                 start = this.getInt((Integer) state.get("MinGridindx"),0);
             int end =GridNums.length;
             if( state != null)
               end = this.getInt((Integer) state.get("MaxGridindx"),end);
             int nsteps = 0;
             if( state != null)
               nsteps = this.getInt((Integer) state.get("NGridChan"),nsteps);
            ACS[ 3 ]  = new AnimationController();
            Xscales[ 3 ]  = new XScaleChooserUI( "Grids", "index", start, end, nsteps );
            Xscales[ 3 ] .addActionListener( new GridXsclActionListener() );
            float[] xvals = new float[  end-start ] ;

            for ( int i = start; i < end; i++ )
                xvals[ i-start ]  = (float) GridNums[ i ] ;
            ACS[ 3 ] .setFrame_values( xvals );
            ACS[ 3 ] .setBorderTitle( "Grid" );
            ACS[ 3 ] .setTextLabel( "ID" );
            ACS[ 3 ] .addActionListener( new GridAnimActionListener() );

        }
     
        if ( MaxCols < 2 ) {

            int save = Permutation[ NstepDims - 1 ] ;

            Permutation[ NstepDims - 1 ]  = 2;
            Permutation[ 2 ]  = save;
            NstepDims--;
            pixel_min[ 2 ]  = 1;
            pixel_max[ 2 ]  = 2;
            ACS[ 2 ]  = null;
            Xscales[ 2 ]  = null;

        } else {
            int start =1;
            if( state != null)
              start = this.getInt((Integer) state.get("StartCol"),0);
            int end =MaxCols;
            if( state != null)
              end = this.getInt((Integer) state.get("EndCol"),end);
            int nsteps = 0;
            if( state != null)
              nsteps = this.getInt((Integer) state.get("NcolChan"),nsteps);
            ACS[ 2 ]  = new AnimationController();
            Xscales[ 2 ]  = new XScaleChooserUI( "Column" ,  "" , start , 
                                                         end , nsteps );

            Xscales[ 2 ] .addActionListener( new ColXsclActionListener() );
            float[  ]  xvals = new float[ end-start+1 ] ;

            for ( int i = start; i <= end; i++ )
                xvals[ i-start ]  = (float) ( i );

            ACS[ 2 ] .setFrame_values( xvals );
            ACS[ 2 ] .setBorderTitle( "Column" );
            ACS[ 2 ] .setTextLabel( "Column" );
            ACS[ 2 ] .addActionListener( new ColAnimActionListener( ) );

        }
     
        if ( MaxRows < 2 ) {

            int save = Permutation[ NstepDims - 1 ] ;

            Permutation[ NstepDims - 1 ]  = 1;
            Permutation[ 1 ]  = save;
            NstepDims--;
            pixel_min[ 1 ]  = 1;
            pixel_max[ 1 ]  = 2;
            ACS[ 1 ]  = null;
            Xscales[ 1 ]  = null;

        } else {
            int start =1;
            if( state != null)
              start = this.getInt((Integer) state.get("StartRow"),0);
            int end =MaxRows;
            if( state != null)
               end = this.getInt((Integer) state.get("EndRow"),end);
            int nsteps = 0;
            if( state != null)
              nsteps = this.getInt((Integer) state.get("NrowChan"),nsteps);
            ACS[ 1 ]  = new AnimationController();
            Xscales[ 1 ]  = new XScaleChooserUI( "Row", "", start,
                                                       end, nsteps );

            Xscales[ 1 ] .addActionListener( new RowXsclActionListener() );
            float[ ] xvals = new float[ end-start+1 ] ;

            for ( int i = start; i <= end; i++ )
                xvals[ i-start ]  = (float) ( i  );

            ACS[ 1 ] .setFrame_values( xvals );
            ACS[ 1 ] .setBorderTitle( "Row" );
            ACS[ 1 ] .setTextLabel( "Row" );
            ACS[ 1 ] .addActionListener( new RowAnimActionListener( ) );

        }

        if ( MaxChannels < 2 ) {

            int save = Permutation[ NstepDims - 1 ] ;

            Permutation[ NstepDims - 1 ]  = 0;
            Permutation[ 0 ]  = save;
            NstepDims--;
            pixel_min[ 0 ]  = 0;
            pixel_max[ 0 ]  = 1;
            ACS[ 0 ]  = null;
            Xscales[ 0 ]  = null;

        } else {

            float start =0;
            if( state != null)
              start = this.getFloat((Float) state.get("MinTimex"),start);
            float end =MaxChannels;
            if( state != null)
              end = this.getFloat((Float) state.get("MaxTimex"),end);
            int nsteps = 0;
            if( state != null)
            nsteps = this.getInt((Integer) state.get("NtimeChan"),nsteps);
            ACS[ 0 ]  = new AnimationController();
            String units ="us";
            if( nsteps ==0)
               units ="channel";
            Xscales[ 0 ]  = new XScaleChooserUI( "Time", units, start, 
                                                               end, nsteps );
            Xscales[ 0 ] .addActionListener( new TimeXsclActionListener() );
            float[] xvals= null;
            if( nsteps ==0){
              xvals = new float[ (int)end-(int)start ] ;

              for ( int i = (int)start; i < (int)end; i++ )
                 xvals[ i -(int)start]  = (float)i;//MinTime+i*( MaxTime-MinTime )/MaxChannels;
            }else{
               xvals = new float[nsteps];
               for( int i=0;i<nsteps; i++)
                  xvals[i] = start + i*(end-start)/nsteps;
            }
            ACS[ 0 ] .setFrame_values( xvals );
            ACS[ 0 ] .setBorderTitle( "Time" );
            if( nsteps ==0)
               ACS[ 0 ] .setTextLabel( "Channel" );
            else 
               ACS[0].setTextLabel("us");
            ACS[ 0 ] .addActionListener( new TimeAnimActionListener( ) );

        }

        for ( int i = 0; i < 2; i++ )
            SetEnabled( ACS[ Permutation[ i ] ], false );

            //-----------Set up Control list---------------
        //Dimension = new JButton( "Dimensions" );
        //Dimension.addActionListener( new ButtonListener() );
        order = new MyJPanel( NstepDims, Permutation);
        AnimXscls = new ViewControl[ 2 + 2 * NstepDims ] ;
        AnimXscls[ 0 ]  = new ViewControlMaker(order );
        DSConversions = new DataSetXConversionsTable(DataSets[0]);
        AnimXscls[ 1 ] =  new ViewControlMaker( DSConversions.getTable());
        savXScales = new JPanel[5];
        for ( int i = 0; i < NstepDims; i++ ) {

            AnimXscls[ 2 + 2 * i ]  = 
                        new ViewControlMaker( ACS[ Permutation[ i ] ] );
            AnimXscls[2 +2*i].setTitle(Title1[Permutation[i]] );
            savXScales[Permutation[i]] = new JPanel( new GridLayout(1,1));
            savXScales[Permutation[i]].add(Xscales[ Permutation[ i ] ]);
            AnimXscls[ 3 + 2 * i ]  =
                        new ViewControlMaker( savXScales[Permutation[i]] );
            AnimXscls[3 +2*i].setTitle(Title1[Permutation[i]] );

        }


   
    }//init

    private void SetUpVariables( ObjectState state){
      if( state == null)
         return;
        pixel_min = (float[])state.get("pixel_min");
        pixel_max = (float[])state.get("pixel_max");
        
        Permutation =(int[])state.get("Permutation");
      
        //---------- Set up Handlers--------------------------
        Handler[ 0 ]  = new TimeHandler( getFloat((Float)state.get("MinTimex"),0f),
                                         getFloat((Float)state.get("MaxTimex"),MaxTime),
                                         MaxChannels, getInt((Integer)state.get("NtimeChan"),0));//0f, MaxTime, MaxChannels, 0 );
        Handler[ 1 ]  = new RowHandler( getInt((Integer)state.get("StartRow"),1),
                                        getInt((Integer)state.get("EndRow"),MaxRows),
                                        getInt((Integer)state.get("NrowChan"),0));//1, MaxRows, 0 );
        Handler[ 2 ]  = new ColHandler( getInt((Integer)state.get("StartCol"),1),
                                        getInt((Integer)state.get("EndCol"),MaxCols),
                                        getInt((Integer)state.get("NcolChan"),0));//1, MaxCols, 0 );
        Handler[ 3 ]  = new GridHandler( getInt((Integer)state.get("MinGridindx"),0),
                                         getInt((Integer)state.get("MaxGridindx"), GridNums.length - 1),
                                         getInt((Integer)state.get("NGridChan"),0));//0, GridNums.length - 1, 0 );
        Handler[ 4 ]  = new DataSetHandler( getInt((Integer)state.get("MinDSindx"),0),
                                            getInt((Integer)state.get("MaxDSindx"), DataSets.length - 1),
                                            getInt((Integer)state.get("NDssChan"),0));//0, DataSets.length - 1, 0 );

       
    }
   
    //Makes Controls have the appropriate values
    String[] ControlLabel ={"Time",     "Row",    "Column",  "Grid",       "DataSet"};
    String[] ControlUnits ={"Channel",  "",        "",        "index",     "index"};
    String[] ControlMinVal ={"MinTimex","StartRow","StartCol","MinGridindx", "MinDSindex"};
    String[] ControlMaxVal ={"MaxTimex","EndRow",  "EndCol",  "MaxGridindx","MaxDSindex"};
    String[] ControlNSteps ={"NtimeChan","NrowChan","NcolChan","NGridChan",  "NDssChan"};
    private void SetUpControls( ObjectState state){
       if( state.get("NtimeChan").equals(new Integer(0)))
           ControlUnits[0] = "Channel";
       else
           ControlUnits[0] ="us";
       order.showPermutation( Permutation);
       for( int i=0; i< 5; i++)
         if( ACS[i] != null){
          float start =0;
          if( i==0)
            start = this.getFloat((Float) state.get(ControlMinVal[i]),start);
          else
            start = ((Integer)state.get(ControlMinVal[i])).intValue();
          float end =MaxChannels;
          if( i==0)
            end = this.getFloat((Float) state.get(ControlMaxVal[i]),end);
          else
            end = ((Integer)state.get(ControlMaxVal[i])).intValue();
          int nsteps = 0;
          nsteps = this.getInt((Integer) state.get(ControlNSteps[i]),nsteps);
          //ACS[ i ]  = new AnimationController();
          String units =ControlUnits[i];
          if( nsteps ==0)
              if( i == 0)
                  units ="Channel";
          //Xscales[ i ]  = new XScaleChooserUI( ControlLabel[i], units, start, 
           //                                                  end, nsteps );
          Xscales[i].set(units, start,  end, nsteps );
          
          float[] xvals= null;
          if( nsteps ==0){
            xvals = new float[ (int)end-(int)start ] ;

          for ( int j = (int)start; j < (int)end; j++ ){
                xvals[ j -(int)start]  = (float)j;//MinTime+i*( MaxTime-MinTime )/MaxChannels;
           }
          }else{
             xvals = new float[nsteps];
             for( int j=0;j < nsteps; j++ ){
               xvals[j] = start + j*(end-start)/nsteps;
             }
          }
          ACS[ i ] .setFrame_values( xvals );
          ACS[ i ] .setBorderTitle( ControlLabel[i]);
          if( i ==3)
              units ="IDs";
          ACS[i].setTextLabel(units);
       }
    }


    
   //---------------------- IPreserveState Methods-----------------------
   public void setObjectState(ObjectState new_state){
      if( new_state != null)
         state = new_state;
      else
         state = getObjectState( true);
       
      SetUpVariables( state);     
      SetUpControls( state);
      notifyListeners( IArrayMaker.DATA_CHANGED );
      
         
   }
   
   public ObjectState getObjectState(boolean is_default){
     
      if( is_default){
        ObjectState st = new ObjectState();
        st.insert("Permutation",initPermutation );
        st.insert("pixel_min", initPixel_min);
        st.insert("pixel_max",initPixel_max);
        st.insert("MinTimex", new Float(0));
        st.insert("MaxTimex", new Float(MaxChannels));
        st.insert("NtimeChan",new Integer(0));
        st.insert("NrowChan", new Integer(0));
        st.insert("NcolChan", new Integer(0));
        st.insert("StartRow", new Integer(1));
        st.insert("EndRow", new Integer(MaxRows));
        st.insert("StartCol", new Integer(1));
        st.insert("EndCol", new Integer(MaxCols));
        st.insert("NDssChan", new Integer(0));
        st.insert("MinDSindx", new Integer(0));
        st.insert("MaxDSindx" , new Integer(DataSets.length));
        st.insert("MinGridindx" , new Integer(0));
        st.insert("MaxGridindx", new Integer( GridNums.length));
        st.insert("NGridChan", new Integer(0));
        
        return st;
      }else{
        return state;
      }
      
   }
   
   private float getFloat( Float f, float def_val){
     if( f == null)
        return def_val;
     if( Float.isNaN(f.floatValue()))
        return def_val;
     return f.floatValue();
     
   }
   private int getInt( Integer i, int def_val){
       if( i == null)
         return def_val;
      return i.intValue();
   }

    //----------------------- IArrayMaker Methods ---------------------
    /**
     * Return controls needed by the component as follows:
     * First the permutation selection JPanel
     * Next the XConversions table
     * Then the animation controller followed by the XScale Chooser for each
     * non trivial dimension.  First dimension is Time, then row, then col,
     * then grid, then data set
     */ 
    public JComponent[] getSharedControls() {

        return AnimXscls;

    }
   
    public JComponent[] getPrivateControls() {

        return new JComponent[ 0 ] ;

    }

    /**
     * Return view menu items needed by the component.
     */   
    public ViewMenuItem[] getSharedMenuItems() {

        return new ViewMenuItem[ 0 ] ;

    }

    public ViewMenuItem[] getPrivateMenuItems() {

        return new ViewMenuItem[ 0 ] ;

    }
  


    public String[] getSharedMenuItemPath() {

        return new String[ 0 ] ;

    }

    public String[] getPrivateMenuItemPath() {

        return new String[ 0 ] ;
    }




    Vector actionListeners = new Vector();

    /**
     *    Adds an ActionListener to this VirtualArray. See above for
     *    action events that will be sent to the listeners
     */
    public void addActionListener( ActionListener listener ) {

        if ( listener == null )
            return;

        if ( actionListeners.indexOf( listener ) >= 0 )
            return;

        actionListeners.add( listener );

    }




    void notifyListeners( String reason ) {

        Object src = this;

        for ( int i = 0; i < NstepDims; i++ )
            if ( i < 2 )// NOT steppable cause in display
                SetEnabled( ACS[ Permutation[ i ]  ] , false );
            else 
                SetEnabled( ACS[ Permutation[ i ] ], true );
    
        ActionEvent evt = new ActionEvent( src, ActionEvent.ACTION_PERFORMED, 
                                                                 reason, 0 );

        for ( int i = 0; i < actionListeners.size(); i++ )
            ((ActionListener) ( actionListeners.elementAt( i) ) ).
                                                      actionPerformed( evt );
    
    }



    private void SetEnabled( Container Cont, boolean status ) {

        Cont.setEnabled( status );
        for ( int i = 0; i < Cont.getComponentCount(); i++ ) {
            Component comp = Cont.getComponent( i );

            if ( comp instanceof Container )
                SetEnabled( (Container) comp, status );
            else
                comp.setEnabled( status );

        }

    }



    /**
     * Remove a specified listener from this view component.
     */ 
    public void removeActionListener( ActionListener act_listener ) {

        actionListeners.remove( act_listener );

    }
  


    /**
     * Remove all listeners from this view component.
     */ 
    public void removeAllActionListeners() {

        actionListeners = new Vector();

    }




    /**
     *    Invoked whenever there is an action event on and instance of
     *    a class which is being listened for.  Also, anyone can invoke the
     *    method.  See above the action commands that must be supported
     */
    public void actionPerformed( ActionEvent evt ) {}

    public IVirtualArray getArray() {

        return this;

    }




    /**
     * Used to dispose of orphan windows and other resources when the
     * parent is removed from display
     */


    public void kill() {

        if ( Frm != null )
            Frm.dispose();

        Frm = null;

    }




    //------------------ IArrayMaker_DataSet Methods-----------------------



    public int getGroupIndex( ISelectedData Info ) {

        return -1;

    }
    
    // row and col are real row and col coords
    private float getTime( float row, float col){
     
      
      float time;
      if(Permutation[0]==0 )
        time =col;
      else if( Permutation[1] ==0)
        time =row;
      else
         time = (pixel_min[0]+pixel_max[0])/2;
      return time;
    }
    private int getDataBlockIndex( float rowDispl, float colDispl){
      int DisplayRow = (int)(rowDispl+.5f);
      int DisplayCol = (int)(colDispl+.5f);
      int row, col;//row on detector and column on detector
          if(Permutation[0] ==1)
             row = (int)(Handler[1].getMax(DisplayCol-1)+Handler[1].getMin(DisplayCol-1))/2;
          else if( Permutation[1] ==1)
              row =(int)((Handler[1].getMax(DisplayRow-1)+Handler[1].getMin(DisplayRow-1))/2);
          else
             row =(int)(pixel_min[1]+pixel_max[1])/2;
         if( check(row,1, MaxRows)) return -1;
     
         if(Permutation[0] ==2)
            col = (int)(Handler[2].getMax(DisplayCol-1)+Handler[2].getMin(DisplayCol-1))/2;
         else if( Permutation[1] ==2)
            col =(int)((Handler[2].getMax(DisplayRow-1)+Handler[2].getMin(DisplayRow-1))/2);
         else
            col =(int)(pixel_min[2]+pixel_max[2])/2;
         if( check(col,1, MaxCols)) return -1;
         int GridNum;
         if(Permutation[0] ==3)
            GridNum = (int)(Handler[3].getMax(DisplayCol)+Handler[3].getMin(DisplayCol))/2;
         else if( Permutation[1] ==3)
            GridNum =(int)((Handler[3].getMax(DisplayRow)+Handler[3].getMin(DisplayRow))/2);
         else
            GridNum =(int)(pixel_min[3]+pixel_max[3])/2;
         if( check(GridNum,0, Grids.length-1)) return -1;
     
         Data D=Grids[GridNum].getData_entry(row,col); 
         int DSindx=DataSets[0].getIndex_of_data(D);
         return DSindx;
    }

   /**
    * Sets the XConversion table.Assumes only one data set currently
    * @param fpt   Real coordinates of view where pointed at occurs
    */
   public void setPointedAt( floatPoint2D fpt){
      float time;
      if( Grids == null)
         return;
      time = getTime( fpt.y, fpt.x);
      //NOTE: getMax and getMin for row and column handlers expect the first row
      //     to start at 0 not 1
      //    The ImageViewComponent starts where XAxisInfo says it starts.
     int DSindx = getDataBlockIndex(fpt.y, fpt.x);
     if( DSindx < 0) 
       return;
     Data D = DataSets[0].getData_entry(DSindx);
     if( D == null)
       return;
     if( getInt((Integer)state.get("NtimeChan"),0)==0){//time is an index convert to time
         XScale xscl = D.getX_scale();
         time = .5f*( xscl.getX((int)(time))+
                  xscl.getX((int)(Math.min( time+1, xscl.getEnd_x() ))));
     }
         
    
     this.DSConversions.showConversions( time, DSindx);
     DataSets[0].setPointedAtIndex( DSindx);
     DataSets[0].setPointedAtX( time);
     DataSets[0].notifyIObservers( IObserver.POINTED_AT_CHANGED );
   }
   
   // Returns true if does not match
   private boolean check( int n1, int min, int max){
      if( n1 < min)
        return true;
      if( n1 > max)
        return true;
      return false;
   }
   private Vector GetPixelInfoOp( DataSet DS, int dbIndx){
     Vector V;
         Object O = (new DataSetTools.operator.DataSet.Attribute.GetPixelInfo_op(DS, dbIndx))
             .getResult();
         if(!(O instanceof Vector))
             return null;
         V =(Vector)O;
         return V;
   }
   //Gets pointed At from the DataSet
   public floatPoint2D redrawNewSelect( String reason ){
      if( reason == IObserver.POINTED_AT_CHANGED){
        int DSindx = DataSets[0].getPointedAtIndex();
        float time = DataSets[0].getPointedAtX();
        this.DSConversions.showConversions( time, DSindx);
        Vector V = GetPixelInfoOp( DataSets[0], DSindx);
        if( (V == null) ||(V.size()<3))
          return null;
        int GridIndx = Arrays.binarySearch(GridNums, 
                     ((Integer)V.elementAt(2)).intValue());
        if( GridIndx < 0)
          return null;
        int rowDet = ((Integer)V.elementAt(1)).intValue();
        int colDet =   ((Integer)V.elementAt(0)).intValue();
        boolean changed=Setind( 1,rowDet);
        changed = changed || Setind( 2, colDet);
        changed = changed ||Setind( 3, GridIndx);
        int timeChan=-1;
        if( state.get("NtimeChan").equals( new Integer(0))){
          timeChan = DataSets[0].getData_entry(DSindx).getX_scale().getI(time);
          changed = changed ||Setind(0,timeChan);
        }else{
          changed= changed || SetindF(0, time);
        }
        if( changed )notifyListeners( IArrayMaker.DATA_CHANGED );
        return getDisplayRowCol( rowDet,colDet,GridIndx, time, timeChan);
      }//if PointedAtChanged
      return null;
   }
   
   private floatPoint2D getDisplayRowCol( int rowDet, int colDet, int GridIndx
         ,float time,int timechan){
     float[] Displayrowcol = new float[2];
     Arrays.fill(Displayrowcol, -1f);
     for(int i =0; i<2;i++)
     if(Permutation[i] ==0)
       if( state.get("NtimeChan").equals(new Integer(0)))
         Displayrowcol[i] = timechan;//Handler[0]
       else
        Displayrowcol[i] = time;//indexOf(0, time);
      
     else if( Permutation[i]==1){
        Displayrowcol[i] = rowDet;//indexOf( 1,rowDet);
     }else if( Permutation[i]==2){
       Displayrowcol[i] =colDet;//indexOf( 2,colDet);
     }else if( Permutation[i]==3){
       Displayrowcol[i] = GridIndx;//indexOf( 3,GridIndx);
     }else 
       return null;
     if( Displayrowcol[0]<0)
         return null;
     if( Displayrowcol[1]<0)
          return null;
     return new floatPoint2D(Displayrowcol[0], Displayrowcol[1]);
         
    
       
    }
   private int indexOf(int dim, float val){
     int Nsteps = Handler[dim].getNSteps();
     float first = Handler[dim].getMin(0);
     float last = Handler[dim].getMax(Nsteps-1);
     if( val < first) return -1;
     if( val > last) return -1;
     return (int)((val-first)/(last-first)*Nsteps); 
   }
   
   //Sets the indices and Animation Control frame for
   // Dimensions not in display 
   private boolean Setind( int dim, int val){
     int indx = -1;
     if( Permutation[0] == dim)
        return false;
     if( Permutation[1] == dim)
        return false;
    if( ACS[dim]== null)
      return false;
    indx =indexOf( dim, (float)val);
    if( ACS[dim].getFrameNumber() == indx)
      return false;
    pixel_min[dim] = Handler[dim].getMin(indx);
    pixel_max[dim] = Handler[dim].getMax(indx);
    if(ACS[dim]!= null)
       ACS[dim].setFrameNumber( indx);
    return true;
    
   }
   private boolean SetindF( int dim, float val){
      int indx = -1;
      if( Permutation[0] == dim)
         return false;
      if( Permutation[1] == dim)
         return false;
      if( ACS[dim] == null)
         return false;
     indx =indexOf(dim,val);
     if( ACS[dim].getFrameNumber() == indx)
       return false; 
     pixel_min[dim] = Handler[dim].getMin(indx);
     pixel_max[dim] = Handler[dim].getMax(indx);
     ACS[dim].setFrameNumber( indx);
     return true;
    }
    /**
     *    Returns the time corresponding to the given Selected Data
     *    @param  Info  Should be a SelectedData2D Object
     */
    public float getTime( ISelectedData Info ) {

        return Float.NaN;

    }




    /**
     *    Returns the selected data corresponding to the give PointedAt
     *    condition
     *    @param PointedAtGroupIndex   The index in the DataSet of the group
     *             that is being pointed at
     *    @param PointedAtTime The time in question when the pointing takes
     *          place
     *    @return  a SelectedData2D containing the row,column and time 
     *              corresponding to the selected condition
     */
    public ISelectedData getSelectedData( int PointedAtGroupIndex,
        float PointedAtTime ) {

                                         	
        return null;

    }
  



    public void setTime( float time ) {}



    public void SelectRegion( ISelectedRegion region ) {}



    /**
     * Sets the attributes of the data array within a AxisInfo wrapper.
     * This method will take in an integer to determine which axis
     * info is being altered.
     *
     *  @param  axis Use AxisInfo.X_AXIS ( 0 ) or AxisInfo.Y_AXIS ( 1 ).
     *  @param  min Minimum value for this axis.
     *  @param  max Maximum value for this axis.
     *  @param  label Label associated with the axis.
     *  @param  units Units associated with the values for this axis.
     *  @param  islinear Is axis linear ( true ) or logarithmic ( false)
     */
    public void setAxisInfo( int axis, float min, float max,
        String label, String units, boolean islinear ) {}




    //------------------------------ IVirtualArray2D Methods ----------------


  
    /**
     * Sets the attributes of the data array within a AxisInfo wrapper.
     * This method will take in an integer to determine which axis
     * info is being altered.
     * 
     *  @param  axis Use AxisInfo.X_AXIS ( 0 ) or AxisInfo.Y_AXIS ( 1 ).
     *  @param  info The axis info object associated with the axis specified.
     */
    public void setAxisInfo( int axis, AxisInfo info ) {}
  



    /*
     **************************************************************************
     * The following methods must include implementation to prevent
     * the user from exceeding the initial array size determined
     * at creation of the array. If an M x N array is specified,
     * the parameters must not exceed ( M-1,N-1 ). 
     **************************************************************************
     */
    // Checks the bound of the number row to be between 0 and maxRows-1
    private int AdjustRowCol( int row, int maxRows ) {
        if ( row < 0 ) 
            return 0;

        if ( row >= maxRows ) 
            return maxRows - 1;

        return row;
    }

    /**
     * Get values for a portion or all of a row.
     * The "from" and "to" values must be direct array reference, i.e.
     * because the array positions start at zero, not one, this must be
     * accounted for. If the array passed in exceeds the bounds of the array, 
     * get values for array elements and ignore extra values.
     *
     *  @param  row_number   the row number being altered
     *  @param  from  the column number of first element to be altered
     *  @param  to    the column number of the last element to be altered
     *  @return If row, from, and to are valid, an array of floats containing
     *	     the specified section of the row is returned.
     *	     If row, from, or to are invalid, an empty 1-D array is returned.
     */
    public float[] getRowValues( int row_number, int from, int to ) {

        row_number = AdjustRowCol( row_number, getNumRows());
        from = AdjustRowCol( from, getNumColumns() );
        to = AdjustRowCol( to, getNumColumns() );
        if ( from > to ) 
            return new float[ 0 ] ;

        float[] Res = new float[ to - from + 1 ] ;

        for ( int i = from; i <= to; i++ )
            Res[ i - from ]  = getDataValue( row_number, i );

        return Res;

    }
  


    /**
     * Set values for a portion or all of a row.
     * The "from" and "to" values must be direct array reference, i.e.
     * because the array positions start at zero, not one, this must be
     * accounted for. If the array passed in exceeds the bounds of the array, 
     * set values for array elements and ignore extra values.
     *
     *  @param values  array of elements to be put into the row
     *  @param row     row number of desired row
     *  @param start   what column number to start at
     */
    public void setRowValues( float[] values, int row, int start ) {}
  


    /**
     * Get values for a portion or all of a column.
     * The "from" and "to" values must be direct array reference, i.e.
     * because the array positions start at zero, not one, this must be
     * accounted for. If the array passed in exceeds the bounds of the array, 
     * get values for array elements and ignore extra values.
     *
     *  @param  column_number  column number of desired column
     *  @param  from    the row number of first element to be altered
     *  @param  to      the row number of the last element to be altered
     *  @return If column, from, and to are valid, an array of floats 
     *	     containing the specified section of the row is returned.
     *	     If row, from, or to are invalid, an empty 1-D array is returned.
     */
    public float[] getColumnValues( int column_number, int from, int to ) {

        if ( column_number < 0 )
            return new float[ 0 ] ;

        if ( column_number >= getNumColumns() ) 
            return new float[ 0 ] ;

        if ( from < 0 ) 
            from = 0;

        if ( to < 0 ) 
            to = 0;

        if ( from >= getNumRows() ) 
            from = getNumRows() - 1;

        if ( to >= getNumRows() ) 
            to = getNumRows() - 1;

        if ( from > to ) 
            return new float[ 0 ] ;

        float[] Res = new float[ to - from + 1 ] ;

        for ( int i = from; i <= to; i++ )
            Res[ i - from ]  = getDataValue( i, column_number );

        return Res;

    }
  


    /**
     * Set values for a portion or all of a column.
     * The "from" and "to" values must be direct array reference, i.e.
     * because the array positions start at zero, not one, this must be
     * accounted for. If the array passed in exceeds the bounds of the array, 
     * set values for array elements and ignore extra values.
     *
     *  @param values  array of elements to be put into the column
     *  @param column  column number of desired column
     *  @param start   what row number to start at
     */
    public void setColumnValues( float[] values, int column, int start ) {}
  



    /**
     * Get value for a single array element.
     *
     *  @param  row     row number of element
     *  @param  column  column number of element
     *  @return If element is found, the float value for that element is 
     *	     returned. If element is not found, zero is returned.
     */ 
    public float getDataValue( int row, int column ) {

        int f = Permutation[ 1 ] ;
        int n = Permutation[ 0 ] ;
        float[] spixel_min=new float[5], 
              spixel_max = new float[5];
        System.arraycopy(pixel_min,0,spixel_min,0,5);

	    System.arraycopy(pixel_max,0,spixel_max,0,5);
        spixel_min[ f ]  = Handler[f ] .getMin( row );
        spixel_max[ f ]  = Handler[ f ] .getMax( row );
        spixel_min[ n ]  = Handler[ n ] .getMin( column );
        spixel_max[ n ]  = Handler[ n ] .getMax( column );
        float Res=Handler[ 4 ] .getValue( DataSets, spixel_min, spixel_max );
        return Res;
     
    }
  


    /**
     * Set value for a single array element.
     *
     *  @param  row     row number of element
     *  @param  column  column number of element
     *  @param  value   value that element will be set to
     */
    public void setDataValue( int row, int column, float value ) {}
  



    /**
     * Returns the values in the specified region.
     * The vertical dimensions of the region are specified by starting 
     * at first row and ending at the last row. The horizontal dimensions 
     * are determined by the first column and last column. 
     *
     *  @param  first_row  first row of the region
     *  @param  last_row	last row of the region
     *  @param  first_column  first column of the region
     *  @param  last_column	last column of the region
     *  @return If a portion of the array is specified, a 2-D array copy of 
     *	     this portion will be returned. 
     *	     If all of the array is specified, a reference to the actual array
     *	     will be returned.
     */
    public float[][] getRegionValues( int first_row, int last_row,
        int first_column, int last_column ) {

        first_row = AdjustRowCol( first_row, getNumRows() );
        last_row = AdjustRowCol( last_row, getNumRows() );
        first_column = AdjustRowCol( first_column, getNumColumns() );
        last_column = AdjustRowCol( last_column, getNumColumns() );
        if ( first_row > last_row ) 
           first_row = last_row;

        if ( first_column > last_column ) 
           first_column = last_column;
     
        float[][]Res = new float[ last_row - first_row + 1 ] 
                                        [ last_column - first_column + 1 ] ;
    
        for ( int i = first_row; i <= last_row; i++ )
           Res[ i ]  = getRowValues( i, first_column, last_column );
           
        return Res;

    }




    /**  
     * Sets values for a specified rectangular region. This method takes 
     * in a 2D array that is already organized into rows and columns
     * corresponding to a portion of the virtual array that will be altered.
     *
     *  @param  values	2-D array of float values 
     *  @param  row_start  first row of the region being altered
     *  @param  col_start  first column of the region being altered
     */
    public void setRegionValues( float[][] values, 
        int row_start,
        int col_start ) {}
        		       



    /**
     * Returns number of rows in the array.
     *
     *  @return This returns the number of rows in the array. 
     */ 
    public int getNumRows() {

        int f = Permutation[ 1 ] ;

        return Handler[ f ] .getNSteps();

    }




    /**
     * Returns number of columns in the array.
     *
     *  @return This returns the number of columns in the array. 
     */
    public int getNumColumns() {

        int f = Permutation[ 0 ] ;

        return Handler[ f ] .getNSteps();

    }
  



    /**
     * Set the error values that correspond to the data. The dimensions of the
     * error values array should match the dimensions of the data array. Zeroes
     * will be used to fill undersized error arrays. Values that are in an 
     * array that exceeds the data array will be ignored.
     *
     *  @param  error_values The array of error values corresponding to the 
     *                                                   data.
     *  @return true if data array dimensions match the error array dimensions.
     */
    public boolean setErrors( float[][] error_values) {

        return true;

    }
  



    /**
     * Get the error values corresponding to the data. If no error values have
     * been set, the square-root of the data value will be returned.
     *
     *  @return error values of the data.
     */
    public float[][] getErrors() {

        return null;

    }
  



    /**
     * Use this method to specify whether to use error values that were passed
     * into the setErrors() method or to use the square-root of the data value.
     *
     *  @param  use_sqrt If true, use square-root.
     *                   If false, use set error values if they exist.
     */
    public void setSquareRootErrors( boolean use_sqrt ) {}
 




    /**
     * Get an error value for a given row and column. Returns Float.NaN if
     * row or column are invalid.
     *
     *  @param  row Row number.
     *  @param  column Column number.
     *  @return error value for data at [ row,column ] . If row or column is 
     *          invalid, Float.NaN is returned.
     */
    public float getErrorValue( int row, int column ) {

        return 0.0f;

    }




    //------------- IVirtualArray Methods-----------------------------


     
    /**
     * This method will return the title assigned to the data. 
     *
     *  @return title assigned to the data.
     */
    public String getTitle() {

        return DataSets[ 0 ] .toString();

    }
  



    /**
     * This method will assign a title to the data. 
     *
     *  @param  title - title describing the data
     */
    public void setTitle( String title ) {}





    /**
     * Set all values in the array to a value. This method will usually
     * serve to "initialize" or zero out the array. 
     *
     *  @param  value - single value used to set all other values in the array
     */
    public void setAllValues( float value ) {}
 



    /**
     * Gets the dimension of the VirtualArray. For example, IVirtualArray1D = 1,
     * IVirtualArray2D = 2.
     *
     *  @return dimension of VirtualArray. This value is an primative integer
     *          not a Dimension.
     */
    public int getDimension() {

        return 2;

    }




    /**
     * Get detailed information about this axis.
     *
     *  @param  axiscode The integer code for the axis, starting at 0.
     *  @return The axis info for the axis specified.
     *  @see    gov.anl.ipns.ViewTools.Components.AxisInfo
     */
    public AxisInfo getAxisInfo( int axiscode ) {

        if ( axiscode == AxisInfo.X_AXIS ) {

            int f = Permutation[ 0 ] ;

            return getAxisInfo( f, false );

        } else if ( axiscode == AxisInfo.Y_AXIS ) {

            return getAxisInfo( Permutation[ 1 ] , true );

        } else 
            return new AxisInfo();

    }


   //flip
    private AxisInfo getAxisInfo( int dim, boolean b ) {

        int f = dim;
        int sgn = 1;
        if(b)
          sgn = -1;

        if ( dim == 0 ){//time
            float Mintimex = ((Float) state.get("MinTimex")).floatValue();
            float Maxtimex =((Float) state.get("MaxTimex")).floatValue();
           
            if(b){
               float x = Mintimex;
               Mintimex = Maxtimex;
               Maxtimex=x;
             
            }
            float nchan = ((Integer)state.get("NtimeChan")).intValue()-1;
            if ( (Xscales[ 0 ] .getXScale() != null) &&(nchan >0) )
                return new AxisInfo(Mintimex,  Maxtimex, 
                                       DataSets[ 0 ].getX_label(), 
                                         DataSets[ 0 ] .getX_units(), true );
            else
                 return new AxisInfo( Mintimex-sgn*.5f, Maxtimex+sgn*.5f, 
                                                        "Channel", "", true );     


        }else if ( f == 1 ){//row
            String end ="EndRow";
            String start ="StartRow";
            if(b){
              end = start;
              start = "EndRow";
            }
            return new AxisInfo( (float)getInt((Integer)state.get(start),MaxRows) -sgn*.5f, 
                                 (float)getInt((Integer)state.get(end),1) +sgn*.5f, 
                                                      "Row", "", true );

        }else if ( f == 2 ){//col
          String end ="EndCol";
          String start ="StartCol";
          if(b){
             end = start;
             start = "EndCol";
           }
            return new AxisInfo( (float)getInt((Integer)state.get(start),MaxRows)-sgn*.5f, 
                                 (float)getInt((Integer)state.get(end),1)+sgn*.5f, 
                                                       "Col", "", true );

        }else if ( f == 3 ){//grid
          String end ="MaxGridindx";
          String start ="MinGridindx";
          if(b){
            end = start;
            start = "MaxGridindx";
         }
            return new AxisInfo( (float)getInt((Integer)state.get(start),GridNums.length-1)-sgn*.5f, 
                                 (float)getInt((Integer)state.get(end),0)+sgn*.5f, 
                                                       "Grid", "", true );

        }else if ( f == 4 ){//row
          String end ="MaxDSindx";
          String start ="MinDSindx";
          if(b){
             end = start;
             start = "MaxDSindx";
          }
          return new AxisInfo( (float)getInt((Integer)state.get(start),DataSets.length-1)-sgn*.5f, 
                                 (float)getInt((Integer)state.get(end),0)+sgn*.5f, 
                                                        "DataSet", "", true );

        }else

            return null;
  
    }
  



    //================================ Handlers and Listeners =============


    public void setGrids( DataSet DS ) {

        Grids = null;
        Grids = new IDataGrid[ GridNums.length ] ;
        for ( int i = 0; i < Grids.length; i++ ) {
            Grids[ i ]  = 
                     NexIO.Write.NxWriteData.getAreaGrid( DS, GridNums[ i ]  );

        }

    }




    /**
     * 
     * @author MikkelsonR
     *
     * The interface that all Handlers for a dimension must implement
     */
    interface IHandler {

        /**
         * Returns the Minimum index associated with the i-th slice
         * @param i  the slice
         * @return Minimum index associated with the i-th slice
         */
        public float getMin( int i );
   


        /**
         * Returns the Maximum index associated with the i-th slice
         * @param i  The slice
         * @return  The Maximum index associated with the i-th slice
         */
        public float getMax( int i );
   


        /** 
         * Returns the Number of slices
         * @return  the number of slices
         */
        public int getNSteps();
   


        /**
         *  Returns the sum, possibly interpolated, y values associated with 
         * the time channels between minInds and maxInds 
         * @param DSS   The set of DataSets to be considered
         * @param minInds  The list of minimum indicies for each dimension 
         *                       to be considered
         * @param maxInds  The list of maximum indicies for each dimension 
         *                           to be considered
         * @return  The sum of the y values, possibly interpolated, in the 
         *            given ranges of indicies. 
         */
        public float getValue( DataSet[] DSS, float[] minInds, 
                                                        float[] maxInds );

    }


    IDataGrid[] Grids = null;
    int DSnum = -1;


    /**
     * 
     * @author MikkelsonR
     *   This class handles the DataSet Information. It keeps track of the 
     * first and last index of the DataSets desired and the number of slices
     * so they can be binned.  The getvalue calls the GridHandler to construct
     * the value.
     * 
     */
    class DataSetHandler implements IHandler {

        int startIndx, endIndx;
        int Nslices;
        float D; // Number of indicies per slice
  
        /**
         *  Constructor
         * @param startIndx  Starting index for the data sets of interest
         * @param endIndx    Ending index of the data sets of interest
         * @param Nslices    The number of slices or 0( index ) if all slices 
         *                               are used in the range
         */
        public DataSetHandler( int startIndx, int endIndx, int Nslices ) {  

            this.startIndx = Math.max( startIndx, 0 );
            this.endIndx = Math.min( endIndx, DataSets.length );
            if ( this.startIndx > this.endIndx ) {
                int x = this.startIndx;

                this.startIndx = this.endIndx;
                this.endIndx = x;
            }

            this.Nslices = Nslices;
            if ( Nslices > this.endIndx - this.startIndx )
                this.Nslices = this.endIndx - this.startIndx;

            if ( Nslices > 0 )
                D = ( this.endIndx - this.startIndx ) / (float) Nslices;
            else 
                D = 1;
                
            if(!state.reset("MinDSindx",new Integer(this.startIndx)))
                 state.insert("MinDSindx",new Integer(this.startIndx));    
            if(!state.reset("MaxDSindx",new Integer(this.endIndx)))
                state.insert("MaxDSindx",new Integer(this.endIndx));    
            if(!state.reset("NDssChan",new Integer(this.Nslices)))
                state.insert("NDssChan",new Integer(this.Nslices));
        }
  



        /**
         *   Returns the minimum DataSet index for slice i( 0..NSlices-1 )
         *   @param i   The slice of interest
         */
        public float getMin( int i ) {

            if ( Nslices == 0 )
                return (int) Math.min( i + startIndx, endIndx );

            return (int) Math.min( i * D + startIndx, endIndx );

        }
   



        /**
         *  Returns the maximun DataSet index for slice i( 0..NSlices-1 )
         * @param i  The slice of interest
         */
        public float getMax( int i ) {

            if ( Nslices <= 0 ) 
                return (int) Math.min( startIndx + i + 1, endIndx +1 );

            return (int) Math.min( i * D + D + startIndx, endIndx +1);

        }




        /**
         *  Returns the Number of slices
         */
        public int getNSteps() {

            if ( Nslices <= 0 )
                return endIndx - startIndx + 1;

            return Nslices;

        }
   


        /**
         *   Returns the sum of the interpolated y-values for all time channels 
         *   in DSS whose indicies are between minInds[ associated dimension ]  
         *   and maxInds[ associated dimension ] 
         * @param   DSS  The array of data sets to be viewed
         * @param  minInds  the set of minimum indicies to be used( time may 
         *                                           be min times )
         * @param maxInds   The set of maximum indices to be used in the sum.
         */
        public float getValue( DataSet[] DSS, float[] minInds, float[] maxInds ) {

            float[] savMin = new float[ 5 ] , 
                    savMax = new float[ 5 ] ;

            System.arraycopy( minInds, 0, savMin, 0, 5 );
            System.arraycopy( maxInds, 0, savMax, 0, 5 );
            float Res = 0;
            float first = Float.NaN;

            for ( int i = (int) minInds[ 4 ] ; i < (int) maxInds[ 4 ] ; i++ ) {

                if ( i != DSnum ) {
                    setGrids( DSS[ i ]  );
                    DSnum = i;
                }

                savMin[ 4 ]  = i;
                savMax[ 4 ]  = i + 1;
               
                Res += Handler[ 3 ] .getValue( DSS, savMin, savMax );
                if ( Float.isNaN( first ) )
                    first = Res;

            }
			if(!Float.isNaN(first))
              Res -= first * ( minInds[ 4 ]  - (int) minInds[ 4 ]  );
            if ( maxInds[ 4 ]  == (int) maxInds[ 4 ] ) 
                return Res;

            savMin[ 4 ]  = (int) maxInds[ 4 ] ;
            savMax[ 4 ]  = 1 + savMin[ 4 ] ;
            if ( savMax[ 4 ]  != DSnum )
                setGrids( DSS[ (int) savMax[ 4 ] ] );

            first = Handler[ 3 ] .getValue( DSS, savMin, savMax );
            Res += ( maxInds[ 4 ]  - (int) maxInds[ 4 ] ) * first;

            return Res; 

        }

    }




    /**
     * 
     * @author MikkelsonR
     
     *   This class handles the Grid Information. It keeps track of the 
     * first and last index of the GridNums array desired and the number of 
     * Grid slices so they can be interpolated.  The getvalue calls the 
     * current ColHandler to construct the value.
     *  
     */
    class GridHandler implements IHandler {

        int startIndx, 
            endIndx;
        int Nslices;
        float D;
  
        /**
         *  Constructor
         * @param startIndx  Starting index for the Grids of interest
         * @param endIndx    Ending index of the Grids of interest
         * @param Nslices    The number of slices or 0( index ) if all slices
         *               are used in the range
         */
        public GridHandler( int startIndx, int endIndx, int Nslices ) {

            this.startIndx = Math.max( startIndx, 0 );
            this.endIndx = Math.min( endIndx, GridNums.length  ); 
            if ( this.startIndx > this.endIndx ) {

                int x = this.startIndx;

                this.startIndx = this.endIndx;
                this.endIndx = x;
            }

            this.Nslices = Nslices;
            if ( Nslices > endIndx - startIndx )
                Nslices = endIndx - startIndx;

            if ( Nslices > 0 )
                D = ( endIndx - startIndx ) / (float) Nslices;
            else 
                D = 1;
                
            if(!state.reset("MinGridindx", new Integer(this.startIndx)))
                state.insert("MinGridindx", new Integer(this.startIndx));   
            if(!state.reset("MaxGridindx", new Integer(this.endIndx)))
                state.insert("MaxGridindx", new Integer(this.endIndx));   
            if(!state.reset("NGridChan", new Integer(this.Nslices)))
                state.insert("NGridChan", new Integer(this.Nslices));
        }
  


        /**
         * Returns the Minimum index of the DataSets Array associated with 
         * the i-th slice
         * @param i  the slice
         * @return Minimum index in the DataSets Array associated with the i-th slice
         */
        public float getMin( int i ) {

            if ( Nslices <= 0 )
                return (int) Math.min( i + startIndx, endIndx-1 );

            return (int) Math.min( i * D + startIndx, endIndx-1 );

        }
   


        /**
         * Returns the Maximum index in the DataSets array associated with 
         * the i-th slice
         * @param i  the slice
         * @return Maximum index of the DataSets associated with the i-th slice
         */
        public float getMax( int i ) {

            if ( Nslices <= 0 )
                return (int) Math.min( i + 1 + startIndx, endIndx  );

            return (int) Math.min( i * D + 1 + startIndx, endIndx  );

        }
  


        /**
         *  Returns the Number of slices
         */

        public int getNSteps() {

            if ( Nslices <= 0 )
                return endIndx - startIndx + 1;

            return Nslices;

        }
   


        /**
         *  Returns the sum, possibly interpolated, of y values associated with 
         * the time channels between minInds and maxInds 
         * @param DSS   The set of DataSets to be considered
         * @param minInds  The list of minimum indicies for each dimension 
         *                       to be considered
         * @param maxInds  The list of maximum indicies for each dimension 
         *                           to be considered
         * @return  The sum of the y values, possibly interpolated, in the 
         *            given ranges of indicies. 
         */

        public float getValue( DataSet[ ] DSS, float[] minInds, 
                                                         float[] maxInds ) {

            //DataSet DS = DSS[ (int)minInds[ 4 ]  ] ;
            float[] savMin = new float[ 5 ] ,
                    savMax = new float[ 5 ] ;

            System.arraycopy( minInds, 0, savMin, 0, 5 );
            System.arraycopy( maxInds, 0, savMax, 0, 5 );
            float Res = 0;
            float first = Float.NaN;

            for ( int i = (int) minInds[ 3 ] ; i < (int) maxInds[ 3 ] ; i++ ) {

                savMin[ 3 ]  = i;
                savMax[ 3 ]  = i + 1;
                if ( Grids[ i ]  != null ) {

                    Res += Handler[ 2 ] .getValue( DSS, savMin, savMax );
                    if ( Float.isNaN( first ) )
                        first = Res;

                }

            }
			if(!Float.isNaN(first))
              Res -= first * ( minInds[ 3 ]  - (int) minInds[ 3 ] );
            if ( maxInds[ 3 ]  == (int) maxInds[ 3 ] ) 
                return Res;

            savMin[ 3 ]  = (int) maxInds[ 3 ] ;
            savMax[ 3 ]  = 1 + savMin[ 3 ] ;
            if ( Grids[ (int) savMin[ 3 ] ] != null ) {

                first = Handler[ 2 ] .getValue( DSS, savMin, savMax );
                Res += ( maxInds[ 3 ]  - (int) maxInds[ 3 ] ) * first;

            }

            return Res;

        }

    }
  
  


    /**
     * 
     * @author MikkelsonR
     *
     *   This class handles the Column Information. It keeps track of the 
     * first and last columns and the number of slices
     * so they can be binned.  The getvalue calls the RowHandler to construct
     * the value.
     */
    class ColHandler implements IHandler {

        int startCol, 
            endCol,
            Nslices;
        float D;
  
        /**
         *  Constructor
         * @param startCol  Starting Column of interest
         * @param endCol    Ending Column of interest
         * @param Nslices    The number of slices or 0( index ) if all slices 
         *                   are used in the range
         */
        public ColHandler( int startCol, int endCol, int Nslices ) {

            this.startCol = Math.max( 1, startCol );
            this.endCol = Math.min( endCol, MaxCols );
            if ( this.startCol > this.endCol ) {

                int x = this.startCol;

                this.startCol = this.endCol;
                this.endCol = x;

            }
     
            this.Nslices = Nslices;
            if ( Nslices > endCol - startCol )
                this.Nslices = endCol - startCol;

            if ( Nslices > 0 )
                D = ( endCol - startCol ) / (float) Nslices;
            else 
                D = 1;
            if(!state.reset("StartCol",new Integer( this.startCol)))
               state.insert("StartCol",new Integer(this.startCol));
            if(!state.reset("EndCol",new Integer( this.endCol)))
               state.insert("EndCol",new Integer(this.endCol));
            if(!state.reset("NcolChan",new Integer( this.Nslices)))
               state.insert("NcolChan",new Integer(this.Nslices));
        }
   


        /**
         * Returns the Minimum index associated with the i-th slice
         * @param i  the slice
         * @return Minimum index associated with the i-th slice
         */
        public float getMin( int i ) {

            if ( Nslices <= 0 )
                 return (int) Math.min( i  + startCol, endCol );

            return (int) Math.min( i * D + startCol, endCol );
      
        }




        /**
         * Returns the Maximum index into GridNums array associated with 
         * the i-th slice
         * @param i  the slice
         * @return Maximum index in the GridNums array associated with 
         *   the i-th slice
         */
        public float getMax( int i ) {

            if ( Nslices <= 0 )
                 return (int) Math.min( i + 1 + startCol, endCol +1 );

            return Math.min( i * D + 1 + startCol, endCol +1 );

        }




        /**
         *  Returns the Number of slices
         */

        public int getNSteps() {

            if ( Nslices <= 0 )
                return endCol - startCol + 1;

            return Nslices;

        }
    



        /**
         *  Returns the sum, possibly interpolated, y values associated with 
         * the time channels between minInds and maxInds 
         * @param DSS   The set of DataSets to be considered
         * @param minInds  The list of minimum indicies for each dimension 
         *                       to be considered
         * @param maxInds  The list of maximum indicies for each dimension 
         *                           to be considered
         * @return  The sum of the y values, possibly interpolated, in the 
         *            given ranges of indicies. 
         */

        public float getValue( DataSet[] DSS, float[] minInds, 
                                                      float[] maxInds ) {

            // DataSet DS = DSS[ (int)minInds[ 4 ] ];
            // int gridNum = GridNums[ (int)minInds[ 3 ] ];
            float[] savMin = new float[ 5 ] ,
                    savMax = new float[ 5 ] ;

            System.arraycopy( minInds, 0, savMin, 0, 5 );
            System.arraycopy( maxInds, 0, savMax, 0, 5 );
            float Res = 0;
            float first = Float.NaN;

            for ( int i = (int) minInds[ 2 ] ; i < (int) maxInds[ 2 ] ; i++ ) {

                savMin[ 2 ]  = i;
                savMax[ 2 ]  = i + 1;
    
                Res += Handler[ 1 ] .getValue( DSS, savMin, savMax );
                if ( Float.isNaN( first ) )
                    first = Res;
    
            }
            if(!Float.isNaN(first))
              Res -= first * ( minInds[ 2 ]  - (int) minInds[ 2 ]  );
            if ( maxInds[ 2 ]  == (int) maxInds[ 2 ] ) 
                return Res;

            savMin[ 2 ]  = (int) maxInds[ 2 ] ;
            savMax[ 2 ]  = 1 + savMin[ 2 ] ;
            first = Handler[ 1 ] .getValue( DSS, savMin, savMax );
            Res += ( maxInds[ 2 ]  - (int) maxInds[ 2 ] ) * first;
   
            return Res;
     
        }
    }


    /**
     * 
     *   This class handles the Row Information. It keeps track of the 
     * first and last row and the number of slices
     * so they can be binned.  The getvalue calls the current TimeHandler to 
     * construct the value.
     */
    class RowHandler implements IHandler {

        int startRow, 
            endRow,
            Nslices;
        float D;
  
        /**
         *  Constructor
         * @param startRow  Starting row of interest
         * @param endRow   Ending row of interest
         * @param Nslices    The number of slices or 0( index ) if all slices 
         *                   are used in the range
         */
        public RowHandler( int startRow, int endRow, int Nslices ) {

            this.startRow = Math.max( startRow, 1 );
            this.endRow = Math.min( endRow, MaxRows );
            if ( this.startRow > this.endRow ) {
                int x = this.startRow;

                this.startRow = this.endRow;
                this.endRow = x;
   
            }
        
            this.Nslices = Nslices;
            if ( Nslices >= this.endRow - this.startRow )
                Nslices = this.endRow - this.startRow;

            if ( Nslices > 0 )
                D = ( this.endRow - this.startRow ) / (float) Nslices;
            else 
                D = -1;
            if(!state.reset("StartRow",new Integer(this.startRow)))
                state.insert("StartRow",new Integer(this.startRow));
            if(!state.reset("EndRow",new Integer(this.endRow)))
                state.insert("EndRow",new Integer(this.endRow));
            if(!state.reset("NrowChan",new Integer(this.Nslices)))
                state.insert("NrowChan",new Integer(this.Nslices));
        }



        /**
         * Returns the Minimum row number( starting at 1 ) associated with the 
         *     i-th slice
         * @param i  the slice
         * @return Minimum row number associated with the i-th slice
         */
        public float getMin( int i ) {

            if ( Nslices <= 0 )
                 return i + startRow;

            return i * D + startRow;

        }
   


        /**
         * Returns the Maximum row number( starting at 1 ) associated with 
         * the i-th slice
         * @param i  the slice
         * @return Maximum row number associated with the i-th slice
         */
        public float getMax( int i ) {

            if ( Nslices <= 0 )
                return (int) Math.min( i + 1 + startRow, endRow +1 );

            return (int) Math.min( i * D + D + startRow, endRow +1 );

        }


        /**
         *  Returns the Number of slices
         */

        public int getNSteps() {

            if ( Nslices <= 0 )
                return endRow - startRow + 1;

            return Nslices;

        }
   


        /**
         *  Returns the sum, possibly interpolated, y values associated with 
         * the time channels between minInds and maxInds 
         * @param DSS   The set of DataSets to be considered
         * @param minInds  The list of minimum indicies for each dimension 
         *                       to be considered
         * @param maxInds  The list of maximum indicies for each dimension 
         *                           to be considered
         * @return  The sum of the y values, possibly interpolated, in the 
         *            given ranges of indicies. 
         */

        public float getValue( DataSet[] DSS, float[] minInds, 
                                                    float[] maxInds ) {

            float[] savMin = new float[ 5 ] , 
                    savMax = new float[ 5 ] ;

            System.arraycopy( minInds, 0, savMin, 0, 5 );
            System.arraycopy( maxInds, 0, savMax, 0, 5 );
            
            float Res = 0;
            float first = Float.NaN;

            for ( int i = (int) minInds[ 1 ] ; i < (int) maxInds[ 1 ] ; i++ ) {
                savMin[ 1 ]  = i;
                savMax[ 1 ]  = i + 1;
    
                Res += Handler[ 0 ] .getValue( DSS, savMin, savMax );
                if ( Float.isNaN( first ) )
                    first = Res;
    
            }

			if(!Float.isNaN(first))
              Res -= first * ( minInds[ 1 ]  - (int) minInds[ 1 ] );
            if ( maxInds[ 1 ]  == (int) maxInds[ 1 ] ) 
                return Res;

            savMin[ 1 ]  = (int) maxInds[ 1 ] ;
            savMax[ 1 ]  = 1 + savMin[ 1 ] ;
            first = Handler[ 0 ] .getValue( DSS, savMin, savMax );
            Res += ( maxInds[ 1 ]  - (int) maxInds[ 1 ] ) * first;
   
            return Res;

        }

    }




    /**
     * 
     *   This class handles the Time Information. It keeps track of the 
     * first and last index and the number of slices
     * so they can be binned.
     */

    class TimeHandler implements IHandler {

        float MinTime,
              MaxTime;
        int NumChannels,
            Nslices;

        float D;
  
        /**
         *  Constructor
         * @param MinTime  Starting time( or Channel if Nslices=0 ) of interest
         * @param MaxTime    Ending time of interest
         * @param NumChannels    The number of time channels to use( 
         *                                           if Nslices=0 )
         * @param Nslices  The number of slices or 0( index ) if all slices 
         *                               are used in the range
         */
        public TimeHandler( float MinTime, float MaxTime, int NumChannels, int Nslices ) {

            this.MinTime = MinTime;//or MinChannel
            this.MaxTime = MaxTime;
            this.NumChannels = NumChannels;
     
            if ( this.MinTime < 0 ) 
                this.MinTime = 0;

            if ( Nslices <= 0 )
                this.NumChannels = (int) Math.min( MaxChannels - (int) this.MinTime, NumChannels );

            this.Nslices = Nslices;
            if ( Nslices < 0 )
                Nslices = 0;
     
            if ( Nslices > 0 )
                D = ( MaxTime - MinTime ) / (float) Nslices;
            else 
                D = -1;
            if( state == null)
               return;
            if( !state.reset("MinTimex",new Float(this.MinTime)))
                state.insert("MinTimex",new Float(this.MinTime));
            if( !state.reset("MaxTimex",new Float(this.MaxTime)))
                state.insert("MaxTimex",new Float(this.MaxTime));
            if( !state.reset("NtimeChan",new Integer(this.Nslices)))
                state.insert("NtimeChan",new Integer(this.Nslices));
           

        }
   



        /**
         * Returns the Minimum index associated with the i-th slice
         * @param i  the slice
         * @return Minimum index associated with the i-th slice
         */
        public float getMin( int i ) {

            if ( Nslices <= 0 ) 
                return i + (int) MinTime;

            return i * D + (int) MinTime;

        }



        /**
         *  Returns the Number of slices
         */
  
        public int getNSteps() {

            if ( Nslices <= 0 )
                return NumChannels;

            return Nslices;

        }
   




        /**
         * Returns the Maximum index associated with the i-th slice
         * @param i  the slice
         * @return Maximum index associated with the i-th slice
         */
        public float getMax( int i ) {

            if ( Nslices <= 0 )
                return i + 1 + (int) MinTime;

            return i * D + D + (int) MinTime;

        }
    



        /**
         *  Returns the sum, possibly interpolated, y values associated with 
         * the time channels between minInds and maxInds 
         * @param DSS   The set of DataSets to be considered
         * @param minInds  The list of minimum indicies for each dimension 
         *                       to be considered
         * @param maxInds  The list of maximum indicies for each dimension 
         *                           to be considered
         * @return  The sum of the y values, possibly interpolated, in the 
         *            given ranges of indicies. 
         */

        public float getValue( DataSet[] DSS, float[] minInds,
                                                       float[] maxInds ) {


            //DataSet DS = DSS[ (int)minInds[ 4 ]  ] ;
            //int gridNum = GridNums[ (int)minInds[ 3 ] ];
            int col = (int) minInds[ 2 ] ;
            int row = (int) minInds[ 1 ] ;
            IDataGrid grid = Grids[ ( int) minInds[ 3 ] ];
            Data D = grid.getData_entry( row, col );

            if ( D == null )
                return 0.0f;

            float Ind_min = minInds[ 0 ] ;
            float Ind_max = maxInds[ 0 ] ;
            XScale xscl = D.getX_scale();

            if ( Nslices != 0 ) {// indicies are times->indicies

                Ind_min = GetI( xscl, Ind_min );
                Ind_max = GetI( xscl, Ind_max );
        
            }

            float Res = 0;
            float first = Float.NaN;
            float[ ]  yvalues = D.getY_values();

            for ( int i = (int) Ind_min; ( i < (int) Ind_max) && 
                                              ( i < yvalues.length ); i++ ) {

                Res += yvalues[ i ] ;
       
                if ( Float.isNaN( first ) )
                    first = Res;
    
            }
			if(!Float.isNaN(first))
               Res -= first * ( minInds[ 1 ]  - (int) minInds[ 1 ] );
            if ( Ind_max == (int) Ind_max )
                return Res;

            if ( (int) Ind_max >= yvalues.length )
                return Res;

            if ( Ind_max < 0 )
                return Res;

            first = yvalues[ (int) Ind_max ] ;
            Res += ( maxInds[ 1 ]  - (int) maxInds[ 1 ] ) * first;
   
            return Res;

        }

    }



    //  Utility to return the interpolated index value of a time in the 
    //   xscl.getXs() array.
    float GetI( XScale xscl, float time ) {

        int i = xscl.getI( time );
        float time1 = xscl.getX( i - 1 );
        float time2 = xscl.getX( i );

        if ( Float.isNaN( time1 ) )
            return 0;

        if ( Float.isNaN( time2 ) )
            return (float) i;

        return i - 1 + ( time - time1 ) / ( time2 - time1 );

    }




    //---------------------Action Listeners------------------------------


    // --- Get range of data sets going in the handlers ------------

    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the XScaleChooserUI for the DataSet
     */
    class DataSetXsclActionListener implements ActionListener {

        int MaxDSS;

        /**
         *  Constructor
         *
         */
        public DataSetXsclActionListener() {

            MaxDSS = DataSets.length;

        }
  


        /**
         *  This method is invoked when the XScaleChooserUI generated an Action. 
         * The Handler for DataSets is changed, Listeners are notified and the
         * associated AnimationController is also changed to reflect these 
         * changes
         *  @param  evt  The event generated by the XScaleChooserUI.
         */
        public void actionPerformed( ActionEvent evt ) {

            XScaleChooserUI xscl = Xscales[ 4 ] ;
   
            if ( xscl == null )
               return;
            if( xscl.getNum_x()==0)
                Handler[ 4 ]  = new DataSetHandler( 0, DataSets.length - 1, 0 );
            else
                Handler[ 4 ]  = new DataSetHandler( (int) xscl.getStart_x(), 
                               (int) xscl.getEnd_x(), xscl.getNum_x() - 1 );

            notifyListeners( IArrayMaker.DATA_CHANGED );
            SetUpAnimControl( ACS[ 4 ] , Handler[ 4 ] );

        }
    }




    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the AnimationController for the 
     * DataSet
     *
     */
    class DataSetAnimActionListener implements ActionListener {

        /**
         *  Changes the min and max range for the DataSet dimension and 
         *  notifies  listeners
         * @param evt  the event generated by the AnimationController
         */
        public void actionPerformed( ActionEvent evt ) {

            int r = ACS[ 4 ] .getFrameNumber();

            pixel_min[ 4 ]  = Handler[ 4 ] .getMin( r );
            pixel_max[ 4 ]  = Handler[ 4 ] .getMax( r );
            state.reset("pixel_min", pixel_min);
            state.reset("pixel_max", pixel_max);
            notifyListeners( IArrayMaker.DATA_CHANGED );

        }

    }





    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the XScaleChooserUI for the Grid
     */
    class GridXsclActionListener implements ActionListener {

        int MaxGrids;
        public GridXsclActionListener() {

            MaxGrids = GridNums.length;

        }
  


        /**
         *  This method is invoked when the XScaleChooserUI generated an 
         *  Action. The Handler for the Grid dimension is changed, Listeners 
         *  are notified and the associated AnimationController is also changed
         *  to reflect these changes
         *  @param  evt  The event generated by the XScaleChooserUI.
         */
        public void actionPerformed( ActionEvent evt ) {

            XScaleChooserUI xscl = Xscales[ 3 ] ;

            if ( xscl == null )
                 return;
            if( xscl.getNum_x() ==0)
                Handler[ 3 ]  = new GridHandler( 0, GridNums.length - 1, 0 );
            else
                Handler[ 3 ]  = new GridHandler( (int) xscl.getStart_x(), 
                                 (int) xscl.getEnd_x(), xscl.getNum_x() - 1 );

            notifyListeners( IArrayMaker.DATA_CHANGED );
            SetUpAnimControl( ACS[ 3 ] , Handler[ 3 ] );

        }
  
    }





    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the AnimationController for the Grid
     * dimension
     *
     */

    class GridAnimActionListener implements ActionListener {

        /**
         *  Changes the min and max range for the Grid dimension and notifies 
         *   listeners
         * @param evt  the event generated by the AnimationController
         */

        public void actionPerformed( ActionEvent evt) {

            int r = ACS[ 3 ] .getFrameNumber();

            pixel_min[ 3 ]  = Handler[ 3 ] .getMin( r );
            
            pixel_max[ 3 ]  = Handler[ 3 ] .getMax( r );
            state.reset("pixel_min", pixel_min);
            state.reset("pixel_max", pixel_max);
            notifyListeners( IArrayMaker.DATA_CHANGED );

        }

    }




    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the XScaleChooserUI for the Column 
     * dimension
     */
    class ColXsclActionListener implements ActionListener {

        //int MaxCols;
        public ColXsclActionListener() {// this.MaxCols = MaxCols;
        }
  


        /**
         *  This method is invoked when the XScaleChooserUI generates an Action. 
         * The Handler for the Column dimensionis changed, Listeners are 
         * notified and the associated AnimationController is also changed to
         * reflect these changes
         *  @param  evt  The event generated by the XScaleChooserUI.
         */
        public void actionPerformed( ActionEvent evt ) {

            XScaleChooserUI xscl = Xscales[ 2 ] ;

            if ( xscl == null )
               return;
            if( xscl.getNum_x()==0)
                Handler[ 2 ]  = new ColHandler( (int) xscl.getStart_x(), 
                                               (int) xscl.getEnd_x(), 0 );
            else
                Handler[ 2 ]  = new ColHandler( (int) xscl.getStart_x(), 
                          (int) xscl.getEnd_x(), xscl.getNum_x() - 1 );

            notifyListeners( IArrayMaker.DATA_CHANGED );
            SetUpAnimControl( ACS[ 2 ] , Handler[ 2 ]  );

        }
    
    }




    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the AnimationController for the Column 
     * dimension
     *
     */

    class ColAnimActionListener implements ActionListener {

        /**
         *  Changes the min and max range for the Column dimension and notifies 
         *   listeners
         * @param evt  the event generated by the AnimationController
         */

        public void actionPerformed( ActionEvent evt ) {

            int r = ACS[ 2 ] .getFrameNumber();

            pixel_min[ 2 ]  = Handler[ 2 ] .getMin( r );
            pixel_max[ 2 ]  = Handler[ 2 ] .getMax( r );

            state.reset("pixel_min", pixel_min);
            state.reset("pixel_max", pixel_max);
            notifyListeners( IArrayMaker.DATA_CHANGED );

        }

    }





    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the XScaleChooserUI for the Row 
     * dimension
     *
     */
    class RowXsclActionListener implements ActionListener {
        // int MaxRows;
        public RowXsclActionListener() {//this.MaxRows = MaxRows;
        }



        /**
         *  This method is invoked when the XScaleChooserUI generates an  
         * Action. The Handler for the Row dimensionis changed, Listeners are 
         * notified and the associated AnimationController is also changed to
         *  reflect these changes
         *  @param  evt  The event generated by the XScaleChooserUI.
         */
        public void actionPerformed( ActionEvent evt ) {

            XScaleChooserUI xscl = Xscales[ 1 ] ;

            if ( xscl == null )
               return;
            if( xscl.getNum_x() ==0)
                Handler[ 1 ]  = new RowHandler( (int)xscl.getStart_x(), (int)xscl.getEnd_x(), 0 );
            else
                Handler[ 1 ]  = new RowHandler( (int) xscl.getStart_x(), 
                                  (int) xscl.getEnd_x(), xscl.getNum_x() - 1 );

            notifyListeners( IArrayMaker.DATA_CHANGED );

            SetUpAnimControl( ACS[ 1 ] , Handler[ 1 ] );

        }

    }





    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the AnimationController for the Row 
     *  dimension
     *
     */

    class RowAnimActionListener implements ActionListener {

        /**
         *  Changes the min and max range for the Row dimension and notifies 
         *   listeners
         * @param evt  the event generated by the AnimationController
         */

        public void actionPerformed( ActionEvent evt ) {

            int r = ACS[ 1 ] .getFrameNumber();

            pixel_min[ 1 ]  = Handler[ 1 ] .getMin( r );
            pixel_max[ 1 ]  = Handler[ 1 ] .getMax( r );
            state.reset("pixel_min", pixel_min);
            state.reset("pixel_max", pixel_max);
            notifyListeners( IArrayMaker.DATA_CHANGED );
     
        }

    }


    boolean inChannel = true;

    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the XScaleChooserUI for the Time 
     * dimension
     *
     */
    class TimeXsclActionListener implements ActionListener {

        //float MinTime,MaxTime;
        // int MaxChannels;
        public TimeXsclActionListener() {//this.MinTime = MinTime;
            //this.MaxTime = MaxTime;
            //this.MaxChannels = MaxChannels;
        }
  


        /**
         *  This method is invoked when the XScaleChooserUI generates an Action. 
         *  The Handler for the Time dimensionis changed, Listeners are notified
         *  and the associated AnimationController is also changed to reflect
         *  these changes
         *  @param  evt  The event generated by the XScaleChooserUI.
         */
        public void actionPerformed( ActionEvent evt ) {

           //XScale xscl = Xscales[ 0 ] .getXScale();
           float val = ACS[0].getFrameValue();
           XScaleChooserUI xscl = Xscales[0];
           if(Xscales[ 0 ] == null){
             return;
             
           }else if( Xscales[ 0 ].getNum_x() ==0){
                 if( state.get("NtimeChan").equals(new Integer(0)))
                       Handler[0] = new TimeHandler( xscl.getStart_x(),
                                     xscl.getEnd_x(), MaxChannels,0);
                 else{// Change from time to channel
                    float low_r = Math.max(0,(xscl.getStart_x()-MinTime)/(MaxTime-MinTime) );
                    float top_r = Math.min(1,(xscl.getEnd_x()-MinTime)/(MaxTime-MinTime) );
                    if( top_r < low_r) return;
                    Handler[0]= new TimeHandler( MaxChannels*low_r, MaxChannels*top_r, MaxChannels,0);
                   
                    Xscales[0].set("Channel",MaxChannels*low_r, MaxChannels*top_r, 0);
                    Xscales[0].invalidate();
                   
                 }
             
           }else{
              
              if(  state.get("NtimeChan").equals(new Integer(0))){ //Changed from chan to time
                 float low_r = Math.max(0,xscl.getStart_x()/MaxChannels);
                 float top_r = Math.min(1,xscl.getEnd_x()/MaxChannels);
                 if( low_r >= top_r) return;
                 low_r = MinTime + low_r*(MaxTime-MinTime);
                 top_r = MinTime + top_r*(MaxTime-MinTime);
                 Handler[0] = new TimeHandler(low_r,top_r, MaxChannels,xscl.getNum_x() );
                
                Xscales[0].set("us",low_r,top_r, xscl.getNum_x());
                
                Xscales[0].invalidate();
                
              }else{//Only ranges are changed
                 Handler[0]= new TimeHandler( xscl.getStart_x(),xscl.getEnd_x(),MaxChannels,
                       xscl.getNum_x());
              }
                              
           }
            /*float val = ACS[ 0 ] .getFrameValue();
            boolean change = false;

            if ( xscl == null ) {

                if ( !inChannel )
                    change = true;

                inChannel = true;
                Handler[ 0 ]  = new TimeHandler( 0f, MaxTime, MaxChannels, 0 );

            } else {

                if ( inChannel )
                    change = true;

                inChannel = false;
                Handler[ 0 ]  = new TimeHandler( xscl.getStart_x(), 
                            xscl.getEnd_x(),MaxChannels, xscl.getNum_x() - 1 );


            }
            */
            float[  ]  xvals = new float[  Handler[ 0 ] .getNSteps() ] ;

            for ( int i = 0; i < xvals.length; i++ )
                xvals[ i ]  = ( Handler[ 0 ] .getMax( i ) + 
                                    Handler[ 0 ] .getMin( i ) ) / 2.0f;

            ACS[ 0 ] .setFrame_values( xvals );

            if ( xscl == null )
                ACS[ 0 ] .setBorderTitle( "Channel" );
            else
                ACS[ 0 ] .setBorderTitle( DataSets[ 0 ] .getX_label() ); 

            if ( xscl == null )
                ACS[ 0 ] .setTextLabel( "Channel" );
            else
                ACS[ 0 ] .setTextLabel( DataSets[ 0 ] .getX_units() );

            
            ACS[ 0 ] .setFrameValue( val ); 

            notifyListeners( IArrayMaker.DATA_CHANGED );
   
        }

    }


    /**
     * 
     * @author MikkelsonR
     *
     * Handles the events generated by the AnimationController for the Time 
     *  dimension
     */
    class TimeAnimActionListener implements ActionListener {

        /**
         *  Changes the min and max range for the Time dimension and notifies 
         *   listeners
         * @param evt  the event generated by the AnimationController
         */

        public void actionPerformed( ActionEvent evt ) {

            int r = ACS[ 0 ] .getFrameNumber();

            pixel_min[ 0 ]  = Handler[ 0 ] .getMin( r );
            pixel_max[ 0 ]  = Handler[ 0 ] .getMax( r );
            state.reset("pixel_min", pixel_min);
            state.reset("pixel_max", pixel_max);
            notifyListeners( IArrayMaker.DATA_CHANGED );

        }

    }



    // Utility that Sets up the not-time Animation controllers to correspond 
    //  with their associated handler and XScaleChooserUI
    private void  SetUpAnimControl( AnimationController AC, IHandler handler ){

        float val = AC.getFrameValue();
        float[ ] xvals = new float[ handler.getNSteps() ] ;
	
        for ( int i = 0; i < xvals.length; i++ )
            xvals[ i ]  = .5f * ( handler.getMin( i ) + handler.getMax( i ) );

        AC.setFrame_values( xvals );
        AC.setFrameValue( val );
	
    }

    FinishJFrame Frm = null;

    /**
     * 
     * @author MikkelsonR
     *  This class listens for action events on the dimension button
     * 
     */
    class ButtonListener implements ActionListener {
   
        /**
         *  This method is invoked when the dimension button is pressed. It 
         *  creates a Frame with with listboxes to select the horizontal and 
         *  vertical dimension to be displayed in a 2D display
         */
        public void actionPerformed( ActionEvent evt ) {
   	  
            if ( Frm == null ) {

                Frm = new FinishJFrame( "Dimension order" );
                Frm.addWindowListener( new FrmWindowListener() );
                Frm.setDefaultCloseOperation( WindowConstants.
                                                        DISPOSE_ON_CLOSE );
                Frm.setSize( 250, 150 );
                Frm.getContentPane().add( order  ) ;
                Frm.show();

            }
      
        }



        /**
         *  Clears the Frame
         *
         */
        public void ClearFrame() {

            Frm = null;

        }

    }




    /**
     * 
     * @author MikkelsonR
     *  Sets the dimension window to null when closed.
     */
    class FrmWindowListener extends WindowAdapter {
	
        public void windowClosed( WindowEvent e ) {
  	  
            Frm = null;
        }

    }





    /**
     * 
     * @author MikkelsonR
     *
     * This JPanel is the contents of the dimension frame that pops up when
     * the dimension JButton is pressed.  This panel provides for selecting
     * the dimension that will be mapped to the horizontal axis and the
     * dimension that will be mapped to the vertical axis in a 2D display
     */
    class MyJPanel extends JPanel {

        int NstepDims;
        int[] Permutation;
        JList Coord1, Coord2;
        String[] DimNames = {"Time", "Row", "Col", "Grid", "DataSet"};
   
        /**
         * Constructor
         * @param NstepDims  The number of dimensions( DataSet,Grid,Row,Col,
         *                    Time ) that have more than one possible value
         * @param Permutation The list of dimensions( 0-Time,1-Row,2-Col,
         *                    3-Grid,4-DataSet ) The 0th entry is the 
         *                    horizontal dimension and the 1st entry is the 
         *                    vertical dimension
         */
        public MyJPanel( int NstepDims, int[] Permutation) {

            super( new BorderLayout() );
            this.NstepDims = NstepDims;
            this.Permutation = Permutation;
            ListElement[] S = new ListElement[ NstepDims ] ;
     
            for ( int i = 0; i < NstepDims; i++ )
                S[ i ]  = new ListElement( DimNames[ Permutation[ i ] ], 
                                                           Permutation[ i ] );
     
            Coord1 = new JList( S );
            Coord2 = new JList( S );
            Coord1.setBorder( BorderFactory.createTitledBorder( 
                    BorderFactory.createLoweredBevelBorder(),
                    "Horz axis"
                ) 
            );
            Coord2.setBorder( BorderFactory.createTitledBorder(
                    BorderFactory.createLoweredBevelBorder(),
                    "Vert axis"
                ) 
            );
            JPanel jjp = new JPanel( new GridLayout(1,2));
            jjp.add( Coord1 );
            jjp.add( Coord2 );
            add( jjp, BorderLayout.CENTER);
            Coord1.setSelectedIndex(Permutation[0]);
            Coord2.setSelectedIndex(Permutation[1]);
            JButton but = new JButton( "Submit" );

            but.addActionListener( new SubmitButtonListener( this ) );
            add( but, BorderLayout.SOUTH );

        }


        public void showPermutation(int[] Perm ){
            Permutation = Perm;
            Coord1.setSelectedIndex(Permutation[0]);
            Coord2.setSelectedIndex(Permutation[1]);

         }

        /**
         * Invoked when the submit button is pressed. This method adjusts the
         * Permutation array and notifies all listeners
         *
         */  
        public void update() {

            ListElement x = ( ListElement) Coord1.getSelectedValue();
            ListElement y = ( ListElement) Coord2.getSelectedValue();

            if ( ( x == null ) || ( y == null ) ) {

                JOptionPane.showMessageDialog( null, 
                                        "Select an item in BOTH lists" );
                return;

            }

            if ( x == y ) {

                JOptionPane.showMessageDialog( null, 
                               "Select an diffent items from the two lists" );
                return;
            }

            int xind = x.getValue();
            int yind = y.getValue();

            swap( Permutation, 0, xind );
            swap( Permutation, 1, yind ); 
            state.reset("Permutation", Permutation);
            notifyListeners( IArrayMaker.DATA_CHANGED );

        }
  




        // Utiltiy method to swap two positions in an array
        private void swap( int[] P, int indx, int value ) {

            int j = -1;

            for ( int i = 0; ( i < P.length ) && ( j < 0 ); i++ )
                if ( P[ i ]  == value )
                    j = i;


            if ( j < 0 )
                return;

            P[ j ]  = P[ indx ] ;
            P[ indx ]  = value; 

        }

    }//MyJPanel




    /**
     * 
     * @author MikkelsonR
     *
     *  A container for two values whose toString() method returns the name 
     *  value
     */
    class ListElement {

        int value;
        String name;
  
        /**
         *  Constructor
         * @param name   The name to be stored
         * @param value  The value to be stored
         */
        public ListElement( String name, int value ) {

            this.value = value;
            this.name = name;

        }
  


        /**
         *  Returns the name 
         */
        public String toString() {

            return name;

        }
  


        /**
         *  Returns the value
         * 
         */
        public int getValue() {

            return value;

        }


    }




    /**
     *
     * @author MikkelsonR
     *
     *  ActionListener for the Submit button in the dimension window
     * 
     */
    class SubmitButtonListener implements ActionListener {

        MyJPanel pan;



        /**
         * Constructor
         * @param pan  The MyJPanel with the submit button and an update method
         * 
         */
        public SubmitButtonListener( MyJPanel pan ) {

            this.pan = pan;

        }
  



        /**
         *  Invokes the corresponding MyJPanel's update method
         */
        public void actionPerformed( ActionEvent evt ) {

            pan.update();


        }
    }




    /**
     *  Test program for this class
     * @param args  args[ 0 ]  is the filename with a DataSet to be considered
     */
    public static void main( String[] args ) {

        JFrame fr = new JFrame( "Test" );
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setSize( 900, 500 );
        fr.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        DataSet[] DSS = null;

        try {
            DSS = Command.ScriptUtil.load( args[ 0 ] );
        } catch ( Exception s) {
            System.exit( 0 );
        }

        DataSet DS = DSS[ DSS.length - 1 ] ;

        fr.getContentPane().add( new DataSetViewerMaker1( DS, null,
                new DataSetGRCTArrayMaker( DS, null ),
              //new LargeJTableViewComponent( null, new dummyIVirtualArray2D() )
                new gov.anl.ipns.ViewTools.Components.TwoD.ImageViewComponent( 
                                                   new dummyIVirtualArray2D() )
             ) );
        fr.show();

    }
}//DataSetGRCTArrayMaker
