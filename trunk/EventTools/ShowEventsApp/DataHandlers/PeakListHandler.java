
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.JOptionPane;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D_d;

import DataSetTools.components.ui.Peaks.OrientMatrixControl;
import DataSetTools.instruments.SNS_SampleOrientation;
import DataSetTools.operator.Generic.Special.ViewASCII;
import DataSetTools.operator.Generic.TOF_SCD.*;

import Operators.TOF_SCD.IndexingUtils;
import Operators.TOF_SCD.IndexPeaks_Calc;
import Operators.TOF_SCD.LsqrsJ_base;
import Operators.TOF_SCD.ARCS_Index_Calc;
import Operators.TOF_SCD.SampleOffset_Calc;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.ConfigLoadCmd;
import EventTools.ShowEventsApp.Command.PeaksCmd;
import EventTools.ShowEventsApp.Command.IndexPeaksCmd;
import EventTools.ShowEventsApp.Command.IndexARCS_PeaksCmd;
import EventTools.ShowEventsApp.Command.IndexWithParamsCmd;
import EventTools.ShowEventsApp.Command.IndexPeaksAutoCmd;
import EventTools.ShowEventsApp.Command.IntegratePeaksCmd;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;
import EventTools.ShowEventsApp.Command.IndexAndRefineUBCmd;
import EventTools.ShowEventsApp.Command.UBwTolCmd;
import EventTools.ShowEventsApp.Command.Util;
import EventTools.ShowEventsApp.Controls.ScalarHandlePanel;


public class PeakListHandler implements IReceiveMessage
{
  private MessageCenter message_center;
  private Vector<Peak_new> peakNew_list = new Vector<Peak_new>();
  private float[][] UB = null;
  private float tolerance =.12f;
  int runNumber =0;
  int nbins =0;
  float phi=0;
  float chi=0;
  float omega =0;
  

  public PeakListHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX);
    message_center.addReceiver( this, Commands.SET_PEAK_NEW_LIST );
    message_center.addReceiver( this, Commands.WRITE_PEAK_FILE );
    message_center.addReceiver( this, Commands.SHOW_PEAK_FILE );
    message_center.addReceiver( this, Commands.MAKE_PEAK_IMAGES );
    message_center.addReceiver( this, Commands.INIT_HISTOGRAM );
    message_center.addReceiver( this, Commands.ADD_PEAK_LIST_INFO );
    message_center.addReceiver( this, Commands.LOAD_CONFIG_INFO ); 
    message_center.addReceiver( this, Commands.INDEX_PEAKS );
    message_center.addReceiver( this, Commands.INDEX_PEAKS_ARCS );
    message_center.addReceiver( this, Commands.INDEX_PEAKS_WITH_PARAMS );
    message_center.addReceiver( this, Commands.INDEX_PEAKS_AUTO );
    message_center.addReceiver( this, Commands.INDEX_PEAKS_FFT );
    message_center.addReceiver( this, Commands.INDEX_PEAKS_ROSS );
    message_center.addReceiver( this, Commands.GET_PEAKS_TO_SPHERE_INTEGRATE );
    message_center.addReceiver( this, 
                                Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX);
    message_center.addReceiver( this, Commands.LSQRS_REFINE_ORIENTATION_MATRIX);
  }


  public boolean receive( Message message )
  {
    if ( message.getName().equals(Commands.SET_PEAK_NEW_LIST) )
    {
      Object obj = message.getValue();
      if ( obj == null )
        return false;

      if ( obj instanceof PeaksCmd )
      {
        PeaksCmd cmd = (PeaksCmd)obj;
        Vector<Peak_new> new_peaks = cmd.getPeaks();
        Sort( new_peaks );
        peakNew_list = new Vector<Peak_new>();
        for ( int i = 0; i < new_peaks.size(); i++ )
          peakNew_list.add( new_peaks.elementAt(i) );

        if ( cmd.getShowImages() )
        {
          PeaksCmd new_cmd = new PeaksCmd( new_peaks,
                                           cmd.getShowImages(),
                                           cmd.getImageSize(),
                                           cmd.getMaxOffset() );

          message_center.send( new Message( Commands.GET_PEAK_IMAGE_REGIONS,
                                            new_cmd,
                                            true ) );
        }
      }

      else if ( obj instanceof Vector )  // from CRUDE INTEGRATE ALL HKL's
      {
         Vector<Peak_new> new_peaks = (Vector<Peak_new>)obj;
         Sort( new_peaks );
         peakNew_list = new Vector<Peak_new>();
         for ( int i = 0; i < new_peaks.size(); i++ )
           peakNew_list.add( new_peaks.elementAt(i) );
      }

      if ( obj instanceof PeaksCmd  || obj instanceof Vector )
        if ( UB != null )
        {
          indexAllPeaks( peakNew_list, UB, tolerance );
          Message mark_indexed  = new Message( Commands.MARK_INDEXED_PEAKS,
                                               peakNew_list,
                                               true,
                                               true );
          message_center.send( mark_indexed );
        }

    }
 
    else if ( message.getName().equals( Commands.MAKE_PEAK_IMAGES ) )
    {
      Object obj = message.getValue();
      if ( obj == null || peakNew_list == null || peakNew_list.size() <= 0 )
        return false;

      if ( obj instanceof PeaksCmd )
      {
        PeaksCmd cmd = (PeaksCmd)obj;
        PeaksCmd new_cmd = new PeaksCmd( peakNew_list,
                                         cmd.getShowImages(),
                                         cmd.getImageSize(),
                                         cmd.getMaxOffset() );
        message_center.send( new Message( Commands.GET_PEAK_IMAGE_REGIONS,
                                          new_cmd,
                                          true ) );
      }
    }
    else if ( message.getName().equals( Commands.INIT_HISTOGRAM ) )
    {
       peakNew_list = new Vector< Peak_new >( );
       UB = null;
    } 

    else if ( message.getName( ).equals( Commands.SET_ORIENTATION_MATRIX ) )
    {
       Object obj = message.getValue();

       if( obj instanceof float[][])
         UB = (float[][])obj;
        
        else if( obj instanceof Vector && 
                 ((Vector)obj).size() == 2 && 
                 ((Vector)obj).firstElement() instanceof float[][] )
         UB = (float[][]) ((Vector)obj).firstElement( );
           
        return false;
      } 

    else if( message.getName( ).equals( Commands.ADD_PEAK_LIST_INFO ))
    {
       Object val = message.getValue();
       if ( val instanceof SelectionInfoCmd )         // fill in counts field
       {
         SelectionInfoCmd select_info_cmd = (SelectionInfoCmd)val;
         select_info_cmd.setSeqNum( getNearestSeqNum( peakNew_list, 
                                    select_info_cmd.getQxyz( ),
                               (int)select_info_cmd.getDetNum( )) );

         message_center.send( new Message( Commands.SHOW_SELECTED_POINT_INFO,
                                           select_info_cmd,
                                           true ));
       }
    }
    else if( message.getName( ).equals( Commands.LOAD_CONFIG_INFO ))
    {
       Object val = message.getValue();
       if( val instanceof ConfigLoadCmd)
       {
          ConfigLoadCmd conf = (ConfigLoadCmd)val;
          runNumber = conf.getRunNumber( );
          nbins = conf.getNbins( );
          phi = conf.getPhi( );
          chi = conf.getChi( );
          omega = conf.getOmega();
       }
    }

    else if ( message.getName().equals(Commands.WRITE_PEAK_FILE ) )
    {
      if ( peakNew_list == null || peakNew_list.size() <= 0 )
      {
        Util.sendError( "ERROR: No Peaks to write" );
      }
      
      String file_name = (String)message.getValue();
      try 
      {
        Vector<Peak_new> VP = UpdateOrig( peakNew_list );
        
        boolean append = false;
        
        if( JOptionPane.showConfirmDialog( null , "Append to prev Peaks file")==
             JOptionPane.YES_OPTION)
           append = true;
        Vector<Peak_new> WPeaks = new Vector();
        if( append)
           try
        {
              WPeaks = Peak_new_IO.ReadPeaks_new( file_name );
        }catch(Exception ss)
        {
           WPeaks = new Vector();
        }
        WPeaks.addAll( VP );
        Peak_new_IO.WritePeaks_new( file_name, (Vector)WPeaks, append );
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: could not write peaks to file " +
                        file_name );
        return false;
      }
    }

    else if ( message.getName().equals(Commands.SHOW_PEAK_FILE ) )
    {
      if ( peakNew_list == null || peakNew_list.size() <= 0 )
      {
        Util.sendError( "ERROR: No Peaks to write" );
        return false;
      }
     
      String file_name = gov.anl.ipns.Util.File.FileIO.appendPath( 
            System.getProperty( "user.home" ), "ISAW/tmp/ppp.peaks" );
      try 
      { 
         Vector<Peak_new> VP = UpdateOrig( peakNew_list);
        Peak_new_IO.WritePeaks_new( file_name, (Vector)VP, false );
        (new ViewASCII(file_name)).getResult();        
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: could not write peaks to file " +
                        file_name );
        return false;
      }
      Wizard.TOF_SCD.Util.ClearFiles( "ppp" , "peaks" );
    }

    else if ( message.getName().equals(Commands.INDEX_PEAKS ) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof IndexPeaksCmd) )
      {
        Util.sendError("ERROR: wrong value object in INDEX_PEAKS command");
        return false;
      }

      if ( peakNew_list == null || peakNew_list.size() <= 0 )
      {
        Util.sendError( "ERROR: No peaks found... can't index yet");
        return false;
      }

      IndexPeaksCmd cmd = (IndexPeaksCmd)obj;

      float[][] UB = null;
      try
      {
        tolerance = cmd.getTolerance();
        Util.sendInfo("Starting to index peaks, PLEASE WAIT...");

        UB = IndexPeaks_Calc.IndexPeaksWithOptimizer( 
                                                  peakNew_list,
                                                  cmd.getA(),
                                                  cmd.getB(),
                                                  cmd.getC(),
                                                  cmd.getAlpha(),
                                                  cmd.getBeta(),
                                                  cmd.getGamma(),
                                                  tolerance,
                                                  cmd.getRequiredFraction(),
                                                  cmd.getFixedPeakIndex() );

        this.UB = getErrorsAndSendMatrix( UB, 
                                          Convert2IPeak(peakNew_list), 
                                          tolerance ); 

        Util.sendInfo( "Finished Indexing" );
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: Failed to index Peaks " + ex);
        ex.printStackTrace();
        return false;
      } 
      return false;
    }

    else if ( message.getName().equals(Commands.INDEX_PEAKS_WITH_PARAMS) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof IndexWithParamsCmd) )
      {
        Util.sendError("ERROR:wrong value object in " + 
                       "INDEX_PEAKS_WITH_PARAMS command");
        return false;
      }

      if ( peakNew_list == null || peakNew_list.size() <= 0 )
      {
        Util.sendError( "ERROR: No peaks found... can't index yet");
        return false;
      }

      IndexWithParamsCmd cmd = (IndexWithParamsCmd)obj;
      try
      {
        tolerance = cmd.getTolerance();
        Util.sendInfo("Starting Auto Indexing using Lattice Parameters," +
                      " PLEASE WAIT..");

        Tran3D UB_tran = new Tran3D();
        Vector q_vectors = new Vector();
        for ( int i = 0; i < peakNew_list.size(); i++ )
        {
          Vector3D q_vec = new Vector3D( peakNew_list.elementAt(i).getQ() );
          q_vectors.add( q_vec );
        }

        IndexingUtils.Find_UB( UB_tran,          // WITH Lattice Parameters
                               q_vectors,
                               cmd.getA(), cmd.getB(), cmd.getC(),
                               cmd.getAlpha(),cmd.getBeta(),cmd.getGamma(),
                               cmd.getTolerance(),
                               cmd.getBase_index(),
                               cmd.getNum_initial(),
                               cmd.getAngle_step() );

         float[][]UB = new float[3][3];
         float[][] UB_tran_arr = UB_tran.get();
         for ( int row = 0; row < 3; row++ )
           for ( int col = 0; col < 3; col++ )
             UB[row][col] = UB_tran_arr[row][col];

        this.UB = getErrorsAndSendMatrix( UB,
                                          Convert2IPeak(peakNew_list),
                                          tolerance );

        Util.sendInfo( "Finished Indexing" );
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: Failed to index Peaks " + ex);
        ex.printStackTrace();
        return false;
      }
      return false;
    }

    else if ( message.getName().equals(Commands.INDEX_PEAKS_AUTO ) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof IndexPeaksAutoCmd) )
      {
        Util.sendError("ERROR:wrong value object in INDEX_PEAKS_AUTO command");
        return false;
      }

      if ( peakNew_list == null || peakNew_list.size() <= 0 )
      {
        Util.sendError( "ERROR: No peaks found... can't index yet");
        return false;
      }

      IndexPeaksAutoCmd cmd = (IndexPeaksAutoCmd)obj;
      try
      {
        tolerance = cmd.getTolerance();
        Util.sendInfo("Starting Min,Max d Auto Indexing, PLEASE WAIT...");

        Tran3D UB_tran = new Tran3D();
        Vector q_vectors = new Vector();
        for ( int i = 0; i < peakNew_list.size(); i++ )
        {
          Vector3D q_vec = new Vector3D( peakNew_list.elementAt(i).getQ() );
          q_vectors.add( q_vec );
        }

        IndexingUtils.Find_UB( UB_tran,          // NO Lattice Parameters
                               q_vectors,
                               cmd.getD_min(),
                               cmd.getD_max(),
                               cmd.getTolerance(), 
                               cmd.getBase_index(),
                               cmd.getNum_initial(),
                               cmd.getAngle_step() );

         float[][] UB = new float[3][3];
         float[][] UB_tran_arr = UB_tran.get();
         for ( int row = 0; row < 3; row++ )
           for ( int col = 0; col < 3; col++ )
             UB[row][col] = UB_tran_arr[row][col];

        this.UB = getErrorsAndSendMatrix( UB,
                                          Convert2IPeak(peakNew_list),
                                          tolerance );

        Util.sendInfo( "Finished Indexing" );
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: Failed to index Peaks " + ex);
        ex.printStackTrace();
        return false;
      }
      return false;
    }


    else if ( message.getName().equals(Commands.INDEX_PEAKS_FFT ) )
    {
      Object obj = message.getValue();
      if ( obj == null || !(obj instanceof IndexPeaksAutoCmd) )
      {
        Util.sendError("ERROR:wrong value object in INDEX_PEAKS_AUTO command");
        return false;
      }

      if ( peakNew_list == null || peakNew_list.size() <= 0 )
      {
        Util.sendError( "ERROR: No peaks found... can't index yet");
        return false;
      }

      IndexPeaksAutoCmd cmd = (IndexPeaksAutoCmd)obj;
      try
      {
        tolerance = cmd.getTolerance();
        Util.sendInfo("Starting FFT Auto Indexing, PLEASE WAIT...");

        Tran3D UB_tran = new Tran3D();
        Vector q_vectors = new Vector();
        for ( int i = 0; i < peakNew_list.size(); i++ )
        {
          Vector3D q_vec = new Vector3D( peakNew_list.elementAt(i).getQ() );
          q_vectors.add( q_vec );
        }

         IndexingUtils.FindUB_UsingFFT( UB_tran,  
                                        q_vectors,
                                        cmd.getD_min(),
                                        cmd.getD_max(),
                                        cmd.getTolerance(),
                                        cmd.getAngle_step() );

         float[][] UB = new float[3][3];
         float[][] UB_tran_arr = UB_tran.get();
         for ( int row = 0; row < 3; row++ )
           for ( int col = 0; col < 3; col++ )
             UB[row][col] = UB_tran_arr[row][col];

        this.UB = getErrorsAndSendMatrix( UB,
                                          Convert2IPeak(peakNew_list),
                                          tolerance );

        Util.sendInfo( "Finished Indexing" );
      }
      catch ( Exception ex )
      {
        Util.sendError( "ERROR: Failed to index Peaks " + ex);
        ex.printStackTrace();
        return false;
      }
      return false;
    }

    else if( message.getName().equals( Commands.INDEX_PEAKS_ARCS) )
    {
      Object obj = message.getValue();
//      System.out.println("IndexPeaksCmd = \n " + obj );
      if ( obj == null || !(obj instanceof IndexARCS_PeaksCmd) )
      {
        Util.sendError("ERROR: wrong value object in INDEX_PEAKS command");
        return false;
      }

      IndexARCS_PeaksCmd cmd = (IndexARCS_PeaksCmd)obj;
      tolerance = cmd.getTolerance();
      Vector results   = null;
      try
      {
        results = ARCS_Index_Calc.index ( cmd.getLatticeParameters(),
                                          peakNew_list,
                                          cmd.getPSI(),
                                          cmd.getU_hkl(),
                                          cmd.getV_hkl(),
                                          tolerance,
                                          cmd.getInitialNum(),
                                          cmd.getRequiredFraction() );
       }
       catch ( Exception ex )
       {
         Util.sendError( "ERROR: Failed to index Peaks " + ex);
         ex.printStackTrace();
         return false;
       }                                        
  
       float[][] UB          = (float[][]) results.elementAt(0);
       double psi            = (Double)    results.elementAt(1);
       Vector3D_d u_proj_hkl = (Vector3D_d)results.elementAt(2);
       Vector3D_d v_proj_hkl = (Vector3D_d)results.elementAt(3);
       
       this.UB = null;
       if ( UB != null )
       {
//       System.out.println("UB = ");
//       LinearAlgebra.print( UB );
         String psiStr = String.format("PSI = %8.4f\n", psi);
         String uStr = String.format("U Projected HKL  = %7.4f  %7.4f  %7.4f\n",
                    u_proj_hkl.getX(), u_proj_hkl.getY(), u_proj_hkl.getZ() );
         String vStr = String.format("V Projected HKL  = %7.4f  %7.4f  %7.4f\n",
                    v_proj_hkl.getX(), v_proj_hkl.getY(), v_proj_hkl.getZ() );
      
         Util.sendInfo( "\n" + psiStr + uStr + vStr );

         UB = getErrorsAndSendMatrix( UB, 
                                      Convert2IPeak(peakNew_list), 
                                      tolerance );

         Util.sendInfo( "Finished Indexing" );
         this.UB = UB;
       }
       else
         Util.sendInfo( "ARCS Indexing FAILED, use different U,V,PSI" );
    } 

    else if( message.getName().equals( 
             Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX ))
    {
       UBwTolCmd UBB = (UBwTolCmd)message.getValue();
       
                                           // don't use the specified 
                                           // if it is not valid
       float temp_tol = UBB.getOffIntMax();
       if ( temp_tol > 0 )
         tolerance = UBB.getOffIntMax( );
 
       indexAllPeaks( peakNew_list, UBB.getUB(), tolerance );
       Message set_peaks = new Message( Commands.PEAK_LIST_CHANGED,
                                        peakNew_list,
                                        true );
       this.UB = UBB.getUB( );
       message_center.send( set_peaks );
       
       Message mark_indexed  = new Message( Commands.MARK_INDEXED_PEAKS,
             peakNew_list,
             true,
             true );
       message_center.send( mark_indexed ); 
       return false;
    } 

    else if(message.getName().equals(Commands.LSQRS_REFINE_ORIENTATION_MATRIX))
    {
      Object obj = message.getValue();
      if ( ! (obj instanceof IndexAndRefineUBCmd ) )
      {
        Util.sendError( "Wrong command type in " +
                         Commands.LSQRS_REFINE_ORIENTATION_MATRIX );
        return false;
      } 

      if ( peakNew_list == null ||  peakNew_list.size() == 0 )
      {
        Util.sendError( "NO PEAKS, can't refine Orientation matrix" );
        return false;
      }

      IndexAndRefineUBCmd cmd = (IndexAndRefineUBCmd)obj;
      tolerance = cmd.getTolerance();
//      System.out.println( "Tolerance = " + tolerance );

      double[][] newUB     = new double[3][3];
      double[][] newUB_inv = new double[3][3];
      double[][] oldUB     = cmd.getUB_double();

      oldUB = LinearAlgebra.getTranspose( oldUB );

      // Multiply oldUB by 2PI to conform to the convention used by OptimzeUB
      for ( int row = 0; row < 3; row++ )
        for ( int col = 0; col < 3; col++ )
          oldUB[row][col] *= (2*Math.PI);
      double[][] oldUB_inv = LinearAlgebra.copy( oldUB );
      LinearAlgebra.invert( oldUB_inv );
/*
      System.out.println("Before OptimizeUB, old UB = " );
      LinearAlgebra.print( oldUB );
*/
      Vector peaks = new Vector();
      for ( int i = 0; i < peakNew_list.size(); i++ )
        peaks.add( peakNew_list.elementAt(i) );

      boolean failed = false;
      try
      {
        double[][] hkls;
        hkls = IndexPeaks_Calc.OptimizeUB( peaks, 
                                           oldUB_inv, newUB_inv, tolerance );
        if ( hkls == null )
          failed = true;
      }
      catch ( Exception ex )
      {
//      System.out.println("Exception in OptimizeUB is: " + ex );
//      ex.printStackTrace();
        failed = true;
      }
      if ( failed )
      {
        Util.sendError("NOT Enough Data, failed to refine Orientation matrix");
        return false;
      }

      newUB = LinearAlgebra.copy( newUB_inv );
      LinearAlgebra.invert( newUB );
      float[][] UB_float = new float[3][3];
      
      // Divide newUB by 2PI to conform to the convention used in IsawEV
      for ( int row = 0; row < 3; row++ )
        for ( int col = 0; col < 3; col++ )
        {
          UB_float[row][col] = (float)(newUB[row][col] / (2*Math.PI));
          newUB[row][col]    = (float)(newUB[row][col] / (2*Math.PI));
        }

      indexAllPeaks( peakNew_list, 
                     LinearAlgebra.getTranspose(UB_float), 
                     tolerance);

      this.UB = getErrorsAndSendMatrix( UB_float, 
                                        Convert2IPeak(peakNew_list), 
                                        tolerance );

      Util.sendInfo( "Finished Refining UB" );

      newUB = LinearAlgebra.getTranspose( newUB );
/*
      System.out.println("BACK FROM OptimizeUB, new UB = " );
      LinearAlgebra.print( newUB );
      System.out.println("Orientation Matrix's UB = " );
      LinearAlgebra.print( this.UB );
*/
      return false;
    }

    else if( message.getName().equals( Commands.INDEX_PEAKS_ROSS ))
    {
       float[] value = (float[])message.getValue();
       if( value == null || value.length < 3)
          return false;
       
       tolerance = value[2];
       
       Util.sendInfo( "Starting long calculation. Please wait..." );
       GetUB.DMIN = value[0];
       GetUB.ELIM_EQ_CRYSTAL_PARAMS = true;
       
       Vector<float[][]> OrientationMatrices =
                GetUB.getAllOrientationMatrices( peakNew_list , null , 
                                                .02f , value[1] );
       GetUB.DMIN = 1f;
       GetUB.ELIM_EQ_CRYSTAL_PARAMS = false;
       if( OrientationMatrices == null || OrientationMatrices.size() < 1)
       {
           Util.sendError( 
           "No Orientation Matrices found in Auto with no Crystal Parameters" );
           return false;
       }
       Vector<IPeak> Peaks = Convert2IPeak(peakNew_list);
       float[][]UB = OrientMatrixControl.showCurrentOrientationMatrices(
                                           Peaks, OrientationMatrices );
        
       if( UB == null)
       {
           Util.sendError( "No Orientation Matrix was selected" );
           this.UB = null;
           return false;
       }
        
       this.UB = getErrorsAndSendMatrix( UB, Peaks, value[2]); 
    
       return false;
    }

    else if ( message.getName().equals(Commands.GET_PEAKS_TO_SPHERE_INTEGRATE))
    {
                                            // this ONLY handles the case of
                                            // integrating peaks in the current
                                            // peaks list.
       Object value = message.getValue();
       if ( value instanceof IntegratePeaksCmd )
       {
         IntegratePeaksCmd cmd = (IntegratePeaksCmd)value;
         if ( cmd.getCurrent_peaks_only() && 
              peakNew_list != null        &&
              peakNew_list.size() > 0     )  // PeakListHandler provides list
         {
           cmd = new IntegratePeaksCmd( peakNew_list,
                                        cmd.getSphere_radius(),
                                        cmd.getCurrent_peaks_only(),
                                        cmd.getCentering_code(),
                                        cmd.getRecord_as_peaks_list() );
           Message integrate = new Message( Commands.SPHERE_INTEGRATE_PEAKS,
                                            cmd, true, true );
           message_center.send( integrate );
         } 
       }
    }

    return false;
  }
  

  private static void Copy( Peak_new[] Sav, Vector<Peak_new> PeakList)
  {
     if( Sav == null || PeakList== null ||Sav.length != PeakList.size())
        throw new IllegalArgumentException(
                                 "null or improper array sizes. Cannot Sort");
     for( int i=0; i< Sav.length ; i++)
        Sav[i]= PeakList.elementAt( i );
  }


  /**
   *  Sort the specifed vector of Peak_new objects into the same order
   *  that will be written to a file, by default.  Assign the sequence
   *  numbers based on that order.
   */
  public static void Sort( Vector<Peak_new> peak_list )
  {
    Peak_new[] peak_array = new Peak_new[ peak_list.size()];

    Copy( peak_array, peak_list );

    Arrays.sort( peak_array, new Peak_newBasicComparator() );

    peak_list.clear();

    for ( int i = 0; i < peak_array.length; i++ )
    {
      peak_array[i].seqnum(i+1);
      peak_list.add( peak_array[i] );
    }
  }


  private int getNearestSeqNum( Vector<Peak_new> peak_list, 
                                Vector3D         Qxyz, 
                                int              detNum )
  {
     if( peak_list == null || Qxyz == null )
        return 0;

     if( peak_list.size() < 1 )
        return 0;

     float minQ = Float.MAX_VALUE;
     int seqNum = 0;
     for( int i = 0; i < peak_list.size(); i++ )
     {
        Peak_new peak = peak_list.elementAt(i);
        if ( peak.detnum( ) == detNum )
        {
           Vector3D Q = new Vector3D( peak.getUnrotQ( ) );

           float d = Qxyz.distance( Q );
           if ( d < minQ )
           {
              minQ = d;
              seqNum = peak.seqnum();
           }
        }
     }
    return seqNum; 
  }


  private float[][] getErrorsAndSendMatrix( float[][] UB, 
                                            Vector<IPeak>Peaks, 
                                            float tolerance )
  {
    float[][] UBT = null;

    try
    {
      indexAllPeaks( peakNew_list, LinearAlgebra.getTranspose(UB), tolerance );

      double[][] UB2 = new double[3][3];
      double[] sig_abc = new double[7];
/*
      System.out.println("UB passed in to getErrorsAnd...");
      LinearAlgebra.print( UB ); 
*/   
      if ( Peaks.size() > 2 )
      {
        UB2 = LinearAlgebra.float2double( ScalarHandlePanel.LSQRS( Peaks , 
                                                                   sig_abc ));

//        System.out.println("UB back from ScalarHandlePanel..");
//        LinearAlgebra.print( UB2 ); 
      }
      else
        UB2 = null;

      if(  UB2 == null || sig_abc[0] <= 0)
      {
         Util.sendError( "LeastSquares Error. No error estimates" );
         UB2 = LinearAlgebra.float2double( UB );
      }
     
      UBT = LinearAlgebra.double2float( LinearAlgebra.getTranspose( UB2 ) );
      /*Object messageValue = UBT;
      if( sig_abc != null)
      {
        messageValue = new Vector(2);
        ((Vector)messageValue).addElement( UBT);
        ((Vector)messageValue).add(LinearAlgebra.double2float( sig_abc ));
      }
      */
      Vector messageValue = Commands.MakeSET_ORIENTATION_MATRIX_arg( UBT , 
                                       LinearAlgebra.double2float( sig_abc ) );

      message_center.send( new Message( Commands.SET_ORIENTATION_MATRIX,
                                        messageValue, false));

      Message set_peaks = new Message( Commands.PEAK_LIST_CHANGED,
                                       peakNew_list,
                                       true );
      message_center.send( set_peaks );

      indexAllPeaks( peakNew_list, LinearAlgebra.getTranspose(UB), tolerance );
      Message mark_indexed  = new Message( Commands.MARK_INDEXED_PEAKS,
                                           peakNew_list,
                                           true,
                                           true );
      message_center.send( mark_indexed );

      Vector3D offset = new Vector3D();
      SampleOffset_Calc.EstimateOffset( peakNew_list, tolerance, offset );
      System.out.println("Estimated Sample Offsets(mm)");
      System.out.printf( "Along Beam Direction: %12.3f\n", 
                          offset.getX()*1000 );
      System.out.printf( "In Horizontal Plane:  %12.3f\n", 
                          offset.getY()*1000 );
      System.out.printf( "Vertical Direction:   %12.3f\n", 
                          offset.getZ()*1000 );
    }
    catch ( Exception ex )
    {
      Util.sendInfo("Indexing FAILED");
    } 
    return UBT;
  }


  private Vector<IPeak> Convert2IPeak( Vector<Peak_new> Peaks)
  {
     if( Peaks == null)
        return null;
     
     Vector<IPeak> Res = new Vector<IPeak>( Peaks.size());
     for( int i=0; i< Peaks.size(); i++)
        Res.add( (IPeak )Peaks.elementAt(i));
     
     return Res;
  }


  public static void indexAllPeaks(Vector Peaks, float[][]UBT, float tolerance)
  {
    float[][]UB = LinearAlgebra.getTranspose( UBT );
    int n=0;
    for( int i=0; i<Peaks.size(); i++)
    { 
       Object peak = Peaks.elementAt(i);
      
       if(peak != null && peak instanceof IPeak)
         {
            IPeak pk = (IPeak)peak;
            pk.sethkl( 0f,0f,0f);
            pk.UB( UB );
            pk.UB( null );
            if( distanceToInt(pk.h()) > tolerance ||
                distanceToInt(pk.k()) > tolerance ||
                distanceToInt(pk.l()) > tolerance )
            {
               pk.sethkl( 0f , 0f , 0f );
               n++; 
            }
            else if ( Math.round( pk.h() ) == 0 &&
                      Math.round( pk.k() ) == 0 &&
                      Math.round( pk.l() ) == 0 )
            {
               pk.sethkl( 0f , 0f , 0f );
               n++;
            }
         }
    }
    n = Peaks.size() - n;
    Util.sendInfo( "Indexed " + n + 
                   " out of " + Peaks.size() + 
                   " peaks to within "+tolerance );
  }


  private int numIndexed( Vector peaks, float tolerance )
  {
    if ( peaks == null )
      return 0;
    
    if ( tolerance <= 0 )
      return 0;

    int total_peaks = peaks.size();
    int count = 0;
    for ( int i = 0; i < total_peaks; i++ )
    {
      IPeakQ peak = peakNew_list.elementAt(i);
      if ( distanceToInt( peak.h() ) <= tolerance &&
           distanceToInt( peak.k() ) <= tolerance &&
           distanceToInt( peak.l() ) <= tolerance &&
           ( peak.h() != 0 || peak.k() != 0 || peak.l() != 0 ) )
        count++;
    }
    return count;
  }

 private Vector<Peak_new> UpdateOrig( Vector<Peak_new> peaks)
 {
    if( peaks == null )
       return  null;
    
    Vector<Peak_new> Result = new Vector<Peak_new>();
    
    SNS_SampleOrientation orientation = 
                              new SNS_SampleOrientation( phi,chi,omega);
    for( int i=0; i< peaks.size();i++)
    {
       Peak_new P = peaks.elementAt( i );
       Peak_new Pn= new Peak_new( runNumber,P.monct( ),P.x( ),P.y( ),P.z( ),
             P.getGrid(),orientation,P.time(),P.L1( ),P.T0( ));
       Pn.sethkl( P.h() , P.k() , P.l() );
       Pn.UB( P.UB());
       Pn.setFacility( P.getFacility( ) );
       Pn.setInstrument( P.getInstrument( ) );
       Pn.inti(P.inti());
       Pn.ipkobs( P.ipkobs());
       Pn.sigi( P.sigi());
       Pn.reflag( P.reflag());
       Pn.seqnum( i+1 );
    
       Result.add( Pn );
    }
    return Result;
 }

  private static float distanceToInt( float val )
  {
    float rounded = Math.round(val);
    return Math.abs( val - rounded );
  }

}
