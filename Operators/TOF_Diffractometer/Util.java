/* 
 * File: Util.java
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
package Operators.TOF_Diffractometer;

import java.io.File;
import java.util.Date;
import java.util.Vector;

import org.w3c.dom.*;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.xml.UtilSm;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;

import DataSetTools.util.SharedData;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;

import EventTools.EventList.*;
import EventTools.Histogram.*;


/**
 * This class contains utility methods and some static methods that are
 * converted to operators.
 * 
 * @author ruth
 *
 */
public class Util
{
  public static final String D_SPACING  = "d-Spacing";
  public static final String MAG_Q      = "Magnitude Q";
  public static final String WAVELENGTH = "Wavelength";
  public static final String RAW_TOF    = "RAW Time-of-flight";
  public static final String TOF        = "Time-of-flight";
  
                                        // A segment size of about 4 MB that
                                        // is a multiple fo the underlying
                                        // file buffer size works well.
  public static final int DEFAULT_SEG_SIZE = 64 * SNS_TofEventList.BUFFER_SIZE;

  private static int n_threads = 8;
  private static int max_time  = 1200000;   // Should not take more than 1200
                                            // seconds (20 minutes) 

   /**
    * Makes a DataSet containing spectra in Wavelength,|Q|,
    * d-Spacing or time-focused to the center of each bank,
    * with one spectrum for each detector bank.
    *
    * @param EventFileName    The name of the file with events
    * @param DetCalFileName   The name of the file with the detector
    *                         calibrations
    * @param bankInfoFileName The name of the file with bank and pixelID(nex)
    *                         info
    * @param MappingFileName  The name of the file that maps DAS pixel_id's
    *                         to NeXus pixel_id's
    * @param firstToLoad      The first Event to load
    * @param numToLoad        The number of events to load
    * @param min              The minimum x-axis value to use
    * @param max              The maximum x-axis value to use 
    * @param isLog            If true use log binning, otherwise use uniform
    *                         binnings
    * @param first_logStep    The length of first interval( isLog = true )
    * 
    * @param nUniformbins     The number of uniform bins( isLog = false )
    *
    * @param x_axis_type      One of the Strings "d-Spacing", "Magnitude Q", 
    *                         "Wavelength", "RAW Time-of-flight"
    *                         or "Time-of-flight"
    *
    * @return  A DataSet in whose spectra are the histograms
    *          for a detector bank.
    */
   public static DataSet Make_DataSetFromEvents( String  EventFileName,
                                                 String  DetCalFileName,
                                                 String  bankInfoFileName,
                                                 String  MappingFileName,
                                                 float   firstToLoad,
                                                 float   numToLoad,
                                                 float   min,
                                                 float   max,
                                                 boolean isLog,
                                                 float   first_logStep,
                                                 int     nUniformbins,
                                                 String  x_axis_type   )
                                throws Exception
   {
     if ( ! x_axis_type.equalsIgnoreCase( MAG_Q )      &&
          ! x_axis_type.equalsIgnoreCase( WAVELENGTH ) &&
          ! x_axis_type.equalsIgnoreCase( D_SPACING )  && 
          ! x_axis_type.equalsIgnoreCase( RAW_TOF )    && 
          ! x_axis_type.equalsIgnoreCase( TOF )        )

       throw new IllegalArgumentException( "x_axis_type MUST be one of " +
                      MAG_Q + ", " + WAVELENGTH  + " or " + D_SPACING );

     String Instrument = FileIO.getSNSInstrumentName( EventFileName );
     SNS_Tof_to_Q_map SMap = new SNS_Tof_to_Q_map( Instrument,
                                                   DetCalFileName,
                                                   bankInfoFileName,
                                                   MappingFileName,
                                                   null );

     SNS_TofEventList STOF = new  SNS_TofEventList( EventFileName );

     IEventBinner binner;
     if ( isLog )
       binner = new LogEventBinner( min, max, first_logStep);
     else
       binner = new UniformEventBinner( min,max,nUniformbins);

     long firstEvent = (long)firstToLoad;
     long NumEventsToLoad = (long)numToLoad;

     if ( firstEvent >= STOF.numEntries() )
       throw new IllegalArgumentException("first event " + firstEvent +
                    " exceeds number of events in file " + STOF.numEntries());

                                                       // keep events in range
     long last = firstEvent + NumEventsToLoad - 1;
     if ( last >= STOF.numEntries() )
       last =  STOF.numEntries() - 1;

     long num_to_load  = last - firstEvent + 1;
     long seg_size     = DEFAULT_SEG_SIZE;
     long num_segments = num_to_load / seg_size + 1;
     long num_loaded   = 0;

     Vector ops = new Vector();
     for ( int i = 0; i < num_segments; i ++ )
     {
       seg_size = Math.min( seg_size, num_to_load - num_loaded );

       Make_Histograms_Op op;
       op = new Make_Histograms_Op( SMap,
                                    x_axis_type,
                                    STOF, firstEvent, seg_size,
                                    binner );
       ops.add( op );
       num_loaded += seg_size;
       firstEvent += seg_size;
     }

     ParallelExecutor pe = new ParallelExecutor(ops, n_threads, max_time);
     Vector results = pe.runOperators();

     float[][] Histograms = CombinePartialHistograms( results );

     if ( Histograms == null)
       return null;

     int run_num = getRunNumber( EventFileName );

     String title = Instrument + "_"+ run_num;

     String log_message = "Mapped events to " + x_axis_type;

     DataSet DS = MakeDataSet( Histograms, 
                               binner, 
                               title, 
                               log_message, 
                               false );
     String x_units = "";
     if ( x_axis_type.equalsIgnoreCase(MAG_Q) )
       x_units = "Inverse Angstroms";
     else if ( x_axis_type.equalsIgnoreCase(WAVELENGTH) ||
               x_axis_type.equalsIgnoreCase(D_SPACING) )
       x_units = "Angstroms";
     else if ( x_axis_type.equalsIgnoreCase(TOF) )
       x_units = "Time(us)";

     DS.setX_units( x_units );
     DS.setX_label( x_axis_type );
     DS.setY_units( "Counts" );
     DS.setY_label( "Scattering Intensity" );

     AddBankDetectorPositions( DS, SMap );
     SetAttributes( DS, EventFileName, SMap );

     if ( x_axis_type.equalsIgnoreCase(MAG_Q) )
       DataSetFactory.add_q_Operators( DS );
     else if ( x_axis_type.equalsIgnoreCase(D_SPACING) )
       DataSetFactory.add_d_Operators( DS );
     else if ( x_axis_type.equalsIgnoreCase(TOF) )
       DataSetFactory.addOperators( DS, InstrumentType.TOF_DIFFRACTOMETER );

     return DS;
   }


   /**
    * Makes a DataSet in d-spacing for each detector from Event Data
    * 
    * @param EventFileName    The name of the file with events
    * @param DetCalFileName   The name of the file with the detector 
    *                         calibrations
    * @param bankInfoFileName The name of the file with bank and pixelID(nex) 
    *                         info
    * @param MappingFileName  The name of the file that maps DAS pixel_id's
    *                         to NeXus pixel_id's
    * @param firstToLoad      The first Event to load
    * @param numToLoad        The number of events to load
    * @param min              The minimum d-spacing to consider
    * @param max              The maximum d-spacing to consider
    * @param isLog            If true use log binning, otherwise use uniform
    *                         binnings     
    * @param first_logStep    The length of first interval( isLog = true )
    *       
    * @param nUniformbins     The number of uniform bins( isLog=false )
    * @param useDspaceMap     Calculate the "d" value for each event by
    *                         multiplying by the diffractometer constant for
    *                         the corresponding pixel taken from a 
    *                         dspace map file, rather than using the
    *                         instrument geometry.
    * @param DspaceMapFile    The name of a binary file of doubles containing
    *                         the diffractometer constants, one for each 
    *                         detector pixel.
    * @param useGhosting      Get estimate of ghost spectrum, NOT the
    *                         actual spectrum.
    * @param GhostInfoFile    The name of the file with ghost information
    * @param nGhostIDs        The number of DAS pixel id's to use
    * @param nGhosts          The number of ghost corrections per is 
    * 
    * @return  A DataSet in d-spacing whose spectra are the summed d-spacing
    *           for a detector.
    */
   public static DataSet Make_d_DataSet( String  EventFileName,
                                         String  DetCalFileName,
                                         String  bankInfoFileName,
                                         String  MappingFileName,
                                         float   firstToLoad,
                                         float   numToLoad,
                                         float   min,
                                         float   max,
                                         boolean isLog,
                                         float   first_logStep,
                                         int     nUniformbins,
                                         boolean useDspaceMap,
                                         String  DspaceMapFile,
                                         boolean useGhosting,
                                         String  GhostInfoFile,
                                         int     nGhostIDs,
                                         int     nGhosts      )
                         throws Exception
   {
         String Instrument = FileIO.getSNSInstrumentName( EventFileName );
         SNS_Tof_to_Q_map SMap = new SNS_Tof_to_Q_map( Instrument, 
                                                       DetCalFileName,
                                                       bankInfoFileName,
                                                       MappingFileName,
                                                       null );
         
         int[][]    ghost_ids = null;
         double[][] ghost_weights = null;
         if ( useGhosting )
         {
           if ( nGhostIDs <= 0 )
             throw new IllegalArgumentException(
                  "Specify correct number of DAS IDs, not " + nGhostIDs );

           if ( nGhosts <= 0 )
             throw new IllegalArgumentException(
                  "Specify correct ghost levels(16?), not " + nGhosts );
           try
           {
             FileUtil.CheckFile( GhostInfoFile );
           }
           catch ( Exception ex )
           {
             String default_dir = SharedData.getProperty("ISAW_HOME","") +
                                  "/InstrumentInfo/SNS/" + Instrument + "/";
             GhostInfoFile = default_dir + Instrument + "_GhostPks.dat";
             FileUtil.CheckFile ( GhostInfoFile );
           }

           Vector V = FileUtil.LoadGhostMapFile( GhostInfoFile, 
                                                 nGhostIDs, 
                                                 nGhosts );
           System.out.println("Loaded Ghost map from " + GhostInfoFile );
           ghost_ids =(int[][]) V.firstElement( );
           ghost_weights =(double[][]) V.lastElement( );
         }

         double[] d_map = null;
         if ( useDspaceMap )
         {
           try
           {
             FileUtil.CheckFile( DspaceMapFile );
           }
           catch ( Exception ex )
           {
             throw new  IllegalArgumentException(
               "Requested d-space map file " + DspaceMapFile +
               " can't be read." );
/*
             String default_dir = SharedData.getProperty("ISAW_HOME","") +
                                  "/InstrumentInfo/SNS/" + Instrument + "/";
             DspaceMapFile = default_dir + Instrument + "_dspacemap.dat";
             FileUtil.CheckFile ( DspaceMapFile );
*/
           }

           d_map = FileUtil.LoadDspaceMapFile( DspaceMapFile );
           System.out.println("Loaded d-space map from " + DspaceMapFile );
         }
         
         IEventBinner binner;
         if( isLog)
            binner = new LogEventBinner( min, max, first_logStep);
         else
            binner = new UniformEventBinner( min,max,nUniformbins);

         SNS_TofEventList STOF = new SNS_TofEventList(EventFileName);

         long firstEvent = (long)firstToLoad;
         long NumEventsToLoad = (long)numToLoad;

         if ( firstEvent >= STOF.numEntries() )
           throw new IllegalArgumentException("first event " + firstEvent +
                    " exceeds number of events in file " + STOF.numEntries());

                                                       // keep events in range
         long last = firstEvent + NumEventsToLoad - 1;
         if ( last >= STOF.numEntries() )
           last =  STOF.numEntries() - 1;

         long num_to_load  = last - firstEvent + 1;
         long seg_size     = DEFAULT_SEG_SIZE;
         long num_segments = num_to_load / seg_size + 1;
         long num_loaded   = 0;

         Vector ops = new Vector();
         for ( int i = 0; i < num_segments; i ++ )
         {
           seg_size = Math.min( seg_size, num_to_load - num_loaded );

           Make_d_Histograms_Op op;
           op = new Make_d_Histograms_Op( SMap,
                                          STOF, firstEvent, seg_size,
                                          binner,
                                          d_map,
                                          ghost_ids, ghost_weights );
           ops.add( op );
           num_loaded += seg_size;
           firstEvent += seg_size;
         }

         ParallelExecutor pe = new ParallelExecutor(ops, n_threads, max_time);
         Vector results = pe.runOperators();

         float[][] Histograms = CombinePartialHistograms( results );

         if( Histograms == null)
            return null;

         int run_num = getRunNumber( EventFileName );

         String title = Instrument + "_"+ run_num + "_d-spacing";
         if ( useGhosting)
           title += "(Ghost)";

         String log_message = "Mapped events to d ";
         if ( useDspaceMap )
           log_message += "using d-space map\n";
         else
           log_message += "using instrument geometry\n";

         if ( useGhosting )
           log_message += "formed GHOST histogram one Data block per bank.";
         else
           log_message += "formed histogram one Data block per bank.";
         
         DataSet DS = MakeDataSet( Histograms, 
                                   binner, 
                                   title, 
                                   log_message,
                                   useGhosting );
         
         DS.setX_units( "Angstroms");
         DS.setX_label( "d-Spacing" );
         DS.setY_units( "Counts" );
         DS.setY_label( "Scattering Intensity" );
         
         AddBankDetectorPositions( DS, SMap );
         SetAttributes( DS, EventFileName, SMap );

         DataSetFactory.add_d_Operators( DS );

         return DS;
   }
   

   /**
    * Makes a DataSet from Event Data where each detector is time focused
    * 
    * @param EventFileName    The name of the file with events
    * @param DetCalFileName   The name of the file with the detector 
    *                         calibrations
    * @param bankInfoFileName The name of the file with bank and pixelID(nex) 
    *                         info
    * @param MappingFileName  The name of the file that maps DAS pixel_id's
    *                         to NeXus pixel_id's
    * @param firstToLoad      The first Event to load
    * @param numToLoad        The number of events to load
    * @param angle_deg        The "virtual" scattering angle, two theta, 
    *                         (in degrees) to which the data should be focused
    *
    * @param final_L_m        The final flight path length (in meters) to which
    *                         the data should be focused
    * @param min              The minimum time to consider
    * @param max              The maximum time to consider
    * @param isLog            If true use log binning, otherwise use uniform
    *                            binnings    
    *                            
    * @param first_logStep    The length of first interval( isLog = true )
    *        
    * @param nUniformbins     The number of uniform bins( isLog=false )
    * @param useGhosting      Get estimate of ghost spectrum, NOT the
    *                         actual spectrum.
    * @param GhostInfoFile    The name of the file with ghost information
    * @param nGhostIDs        The number of DAS pixel id's to use
    * @param nGhosts          The number of ghost corrections per is 
    * 
    * @return a DataSet from Event Data where each detector is time focused
    */
   public static DataSet MakeTimeFocusedDataSet( 
                                         String  EventFileName,
                                         String  DetCalFileName,
                                         String  bankInfoFileName,
                                         String  MappingFileName,
                                         float   firstToLoad,
                                         float   numToLoad,
                                         float   angle_deg,
                                         float   final_L_m, 
                                         float   min,
                                         float   max,
                                         boolean isLog,
                                         float   first_logStep,
                                         int     nUniformbins,
                                         boolean useGhosting,
                                         String  GhostInfoFile,
                                         int     nGhostIDs,
                                         int     nGhosts
                                         
                                         )
                         throws Exception
   {
         String Instrument = FileIO.getSNSInstrumentName( EventFileName );
         SNS_Tof_to_Q_map SMap = new SNS_Tof_to_Q_map( Instrument, 
                                                       DetCalFileName, 
                                                       bankInfoFileName, 
                                                       MappingFileName, 
                                                       null );

         SNS_TofEventList STOF = new SNS_TofEventList(EventFileName);
         
         int[][]    ghost_ids = null;
         double[][] ghost_weights = null;
         
         if ( useGhosting )
         {
           if ( nGhostIDs <= 0 )
             throw new IllegalArgumentException(
                  "Specify correct number of DAS IDs, not " + nGhostIDs );

           if ( nGhosts <= 0 )
             throw new IllegalArgumentException(
                  "Specify correct ghost levels(16?), not " + nGhosts );
           try
           {
             FileUtil.CheckFile( GhostInfoFile );
           }
           catch ( Exception ex )
           {
             String default_dir = SharedData.getProperty("ISAW_HOME","") +
                                  "/InstrumentInfo/SNS/" + Instrument + "/";
             GhostInfoFile = default_dir + Instrument + "_GhostPks.dat";
             FileUtil.CheckFile ( GhostInfoFile );
           }

           Vector V = FileUtil.LoadGhostMapFile( GhostInfoFile, 
                                                 nGhostIDs, 
                                                 nGhosts );
           System.out.println("Loaded Ghost map from " + GhostInfoFile );
           ghost_ids =(int[][]) V.firstElement( );
           ghost_weights =(double[][]) V.lastElement( );
         }

         IEventBinner binner;
         if( isLog)
            binner = new LogEventBinner( min, max, first_logStep);
         else
            binner = new UniformEventBinner( min,max,nUniformbins);

         long firstEvent = (long)firstToLoad;
         long NumEventsToLoad = (long)numToLoad;

         if ( firstEvent >= STOF.numEntries() )
           throw new IllegalArgumentException("first event " + firstEvent +
                    " exceeds number of events in file " + STOF.numEntries());

                                                       // keep events in range
         long last = firstEvent + NumEventsToLoad - 1;
         if ( last >= STOF.numEntries() )
           last =  STOF.numEntries() - 1;
  
         long num_to_load  = last - firstEvent + 1;
         long seg_size     = DEFAULT_SEG_SIZE;
         long num_segments = num_to_load / seg_size + 1;
         long num_loaded   = 0;

         Vector ops = new Vector();
         for ( int i = 0; i < num_segments; i ++ )
         {
           seg_size = Math.min( seg_size, num_to_load - num_loaded );

           Make_Time_Focused_Histograms_Op op;
           op = new Make_Time_Focused_Histograms_Op( 
                                          SMap,
                                          STOF, firstEvent, seg_size,
                                          binner,
                                          angle_deg, final_L_m,
                                          ghost_ids, ghost_weights );
           ops.add( op );
           num_loaded += seg_size;
           firstEvent += seg_size;
         }

         ParallelExecutor pe = new ParallelExecutor(ops, n_threads, max_time);
         Vector results = pe.runOperators();

         float[][] Histograms = CombinePartialHistograms( results );

         if( Histograms == null)
            return null;
         
         int run_num = getRunNumber( EventFileName);
         
         String title = Instrument+"_" + run_num + "_TimeFocused";
         if ( useGhosting)
           title += "(Ghost)";

         String log_message = "Time-Focused Events and ";

         if ( useGhosting )
           log_message += "formed GHOST histogram one Data block per bank.";
         else
           log_message += "formed histogram one Data block per bank.";

         DataSet DS = MakeDataSet( Histograms, 
                                   binner, 
                                   title, 
                                   log_message,
                                   useGhosting );
      
         DS.setX_units( "Time(us)");
         DS.setX_label( "Time-of-flight" );
         DS.setY_units( "Counts" );
         DS.setY_label( "Scattering Intensity" );
         
         AddFocusedDetectorPositions( DS, angle_deg, final_L_m );

         SetAttributes( DS, EventFileName, SMap );

         DataSetFactory.addOperators( DS, InstrumentType.TOF_DIFFRACTOMETER );
         
         return DS;
   }


  /**
   *  Sum up the partial histograms that were returned from the
   *  operators that loaded parts of the files.
   */
  private static float[][] CombinePartialHistograms( Vector results )
  {
    float[][] Histograms = null;

    boolean first_time = true;

    if ( results != null )
    {
      float[][]temp = null;
      for ( int i = 0; i < results.size(); i ++ )
      {
        if ( results.elementAt(i) instanceof float[][] )
          temp = (float[][])results.elementAt(i);
        else
        {
          temp = null;
          System.out.println("Error, returned element not float[][]:" +
                              results.elementAt(i) );
        }
        if ( first_time && temp != null )
        {
          Histograms = temp;
          first_time = false;
        }
        else if ( temp != null )        // add in the new histogram data
        {
          for ( int row = 0; row < Histograms.length; row++ )
            if ( temp[row] != null && Histograms[row] != null )
              for ( int col = 0; col < Histograms[row].length; col++ )
                Histograms[row][col] += temp[row][col];
        }
      }
    }
    return Histograms;
  }


  /**
   *  Convert the integer arrays of counts from the SNS_Tof_to_Q_map
   *  into arrays of floats.
   *
   *  @param intArray  The 2D array of ints from the Q-mapper
   *
   *  @return A 2D array of floats containing the counts from the int array.
   */
   public static float[][] ConvertTo2DfloatArray( int[][] intArray )
   {
      if( intArray == null)
         return null;
      float [][] Res = new float[intArray.length][];
      for( int i = 0 ; i < Res.length ; i++ )
      {
         if ( intArray[i] == null )
            Res[i] = null;
         else
         {
            Res[i] = new float[ intArray[i].length ];
            for( int j = 0 ; j < Res[i].length ; j++ )
               Res[i][j] = ( float ) intArray[i][j];
         }
      }
      return Res;
   }


  /**
   *  Construct a DataSet containing Data blocks for each of the rows
   *  of the given 2D array of floats.
   *
   *  @param  histograms  2D array of floats containing the counts 
   *  @param  binner      The binner that was used for forming the 
   *                      histogram of counts.  The binner defines the XScale.
   *  @param  title       The title to place on the DataSet
   *  @param  log_message The initial log message to use.
   *  @param  is_ghost    Flag indicating whether or not this is a 
   *                      "ghost" histogram.
   */
  private static DataSet MakeDataSet( float[][]    histograms,
                                      IEventBinner binner,     
                                      String       title,     
                                      String       log_message,
                                      boolean      is_ghost  )
  {
    DataSet DS = new DataSet( title, log_message );

    float[] xs = new float[ binner.numBins() + 1 ];
    for( int i = 0; i < xs.length; i++ )
      xs[i] = (float)binner.minVal( i );

    VariableXScale xscl = new VariableXScale( xs );

    for( int i = 0; i < histograms.length; i++)
    {
      if ( histograms[i] != null )
      {
        float[] yvals = histograms[i];
        HistogramTable D = new HistogramTable( xscl, yvals, i ) ;
        if ( !is_ghost )
          D.setSqrtErrors( true );
        else
        {
          float[] errors = new float[ yvals.length ];
          D.setErrors( errors );
        }
        DS.addData_entry( D );
      }
    }

    DataSetFactory.addOperators( DS );
    return DS;
  }


  /**
   *  Add the effective position and pixel info list attributes corresponding
   *  to the focused position, to all of the Data blocks in the DataSet.
   *
   *  @param DS         The DataSet to which detector position information 
   *                    will be added.
   *  @param angle_deg  The 2-theta angle to the focused detector position.
   *  @param final_L_m  The final flight path in meters, to the focused
   *                    detector position.
   */
  private static void AddFocusedDetectorPositions( DataSet DS,
                                                   float   angle_deg,
                                                   float   final_L_m )
  {
    float[] position = new float[3];
    float   angleRad = (float)(angle_deg*Math.PI/180);

    position[0] = (float)( final_L_m*Math.cos( angleRad ));
    position[1] = (float)( final_L_m*Math.sin( angleRad ));
    position[2] = 0;

    Vector3D pos = new Vector3D(position[0], position[1], position[2]);

    int pixelNum = 1;
    for( int i = 0; i < DS.getNum_entries(); i++)
    {
      Data D  = DS.getData_entry(i);
      DetectorPosition dp  = new DetectorPosition( pos );
      D.setAttribute( new DetPosAttribute(Attribute.DETECTOR_POS, dp ));

      Vector3D up_vec     = new Vector3D(0,0,1);
      Vector3D base_vec   = new Vector3D();
      Vector3D radial_vec = new Vector3D( pos );
      radial_vec.normalize();
      base_vec.cross( up_vec, radial_vec );
      int  id = D.getGroup_ID();

      UniformGrid grid = new UniformGrid( id, "m", pos, base_vec, up_vec,
                                         .2f, .2f, .002f, 1, 1);
      DetectorPixelInfo pix =
                    new DetectorPixelInfo( pixelNum,(short)1, (short)1,grid );

      D.setAttribute( new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST,
                                                  new PixelInfoList(pix)) );
    }
  }


  /**
   *  Add the center position of each bank to the Data block for that bank.
   *  Also, add a pixel info list with one pixel and one grid, corresponding
   *  to the entire bank, for each of the Data blocks.
   *
   *  @param DS      The DataSet to which the bank position is added.
   *  @param mapper  The SNS_Tof_to_Q_map mapper used to form the histograms
   *                 from the event data.  This provides the bank position
   *                 and size information.
   */
  private static void AddBankDetectorPositions( DataSet DS,
                                                SNS_Tof_to_Q_map mapper )
  {
    int pixelNum = 1;
    for( int i = 0; i < DS.getNum_entries(); i++)
    {
      Data D  = DS.getData_entry(i);
      int  id = D.getGroup_ID();

      IDataGrid grid = mapper.getIDataGrid( id );
      if ( grid == null )
      {
        System.out.println("WARNING: Detector Grid NOT Found for Bank ID "+id);
        return;
      }

      Vector3D pos = grid.position();
      DetectorPosition dp  = new DetectorPosition( pos );
      D.setAttribute( new DetPosAttribute(Attribute.DETECTOR_POS, dp ));

      Vector3D up_vec   = grid.y_vec(); 
      Vector3D base_vec = grid.x_vec();
      String   units    = grid.units();
      float    width    = grid.width();
      float    height   = grid.height();
      float    depth    = grid.depth();
      UniformGrid new_grid = new UniformGrid( id, units, pos, base_vec, up_vec,
                                              width, height, depth, 1, 1 );
      DetectorPixelInfo pix =
               new DetectorPixelInfo( pixelNum,(short)1, (short)1, new_grid );

      D.setAttribute( new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST,
                                                  new PixelInfoList(pix)) );
    }
  }


  /**
   *  Set the basic attributes that are common to both the time-focused
   *  DataSet and the "d" DataSet.
   *
   *  @param ds         The DataSet for which the attributes will be set
   *  @param filename   The name of the event file that was loaded
   *  @param mapper     The mapper that mapped the events to histograms
   */
  private static void SetAttributes( DataSet ds,
                                     String           filename,
                                     SNS_Tof_to_Q_map mapper )
  {
    int[] RunNums = new int[1];
    RunNums[0] = getRunNumber( filename );

    float L1 = mapper.getL1();
    float T0 = mapper.getT0();
    Attribute L1Attr   = new FloatAttribute( Attribute.INITIAL_PATH, L1);
    Attribute T0Attr   = new FloatAttribute( Attribute.T0_SHIFT, T0);
    Attribute RunsAttr = new IntListAttribute( Attribute.RUN_NUM, RunNums );

    float TotTotCount = 0;

    for( int i = 0; i < ds.getNum_entries(); i++)
    {
      Data D = ds.getData_entry(i);
      D.setAttribute( RunsAttr );
      D.setAttribute( L1Attr );
      D.setAttribute( T0Attr);

      float[] yvals = D.getY_values();
      float TotCount =0;
      for( int j = 0; j < yvals.length; j++ )
        TotCount +=yvals[j];
     
      TotTotCount +=TotCount;
      D.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT, TotCount));
      // TO DO: Add effective position and pixel info list
      // TO DO: Add delta 2 theta
      // TO DO: Add Total Solid Angle
      // TO DO: Add list of Bank IDs
      // TO DO: Add list of Pixel IDs
    }

    ds.setAttribute( new StringAttribute( Attribute.FILE_NAME, filename));

    String instr = FileIO.getSNSInstrumentName( filename );
    if( instr != null && instr.length()>2)
            ds.setAttribute( new StringAttribute(Attribute.INST_NAME, instr));

    ds.setAttribute( new StringAttribute( Attribute.FACILITY_NAME, "SNS"));

    // TO DO: add Instrument Type

    ds.setAttribute(     
              new StringAttribute( Attribute.DS_TYPE, Attribute.SAMPLE_DATA));

    // TO DO: add run title

    ds.setAttribute( RunsAttr );

    AddDateTimeAttribute( ds, (new File( filename )).lastModified());

    ds.setAttribute( new FloatAttribute(Attribute.TOTAL_COUNT, TotTotCount ));
    // TO DO: add number of pulses
    // TO DO: add proton count 
    // TO DO: add user name

  }


  /**
   *  Add an end date and time attribute to the DataSet, corresponding
   *  to the specified Date object.
   *
   *  @param DS    The DataSet to which the date information is added
   *  @param date  The Date object with the information to add
   */
   public static void AddDateTimeAttribute( DataSet DS,  Date date )
   {
      if( date == null )
         date = new Date( System.currentTimeMillis( ));
      
      java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat();
      
      sdf.applyPattern( "dd-MMM-yy" );
      
      DS.setAttribute( new StringAttribute( Attribute.END_DATE , sdf
               .format( date ) ) );
      
      sdf.applyPattern( "HH:mm:ss" );
      
      DS.setAttribute( new StringAttribute( Attribute.END_TIME , sdf
               .format( date ) ) );
   }
   

  /**
   *  Add an end date and time attribute to the DataSet, corresponding
   *  to the specified time in milliseconds.  If the specified time is
   *  not positive, the current time will be used.
   *
   *  @param DS    The DataSet to which the date information is added
   *  @param date  long specifying the time in milliseconds of the date
   *               that should be added. 
   */
   public static void AddDateTimeAttribute( DataSet DS,  long date)
   {
     if( date <= 0 )
        date = System.currentTimeMillis( );
     
     Util.AddDateTimeAttribute( DS,  new Date( date ));
   }
   

   /**
    * Returns the number corresponding to the digits to the left of
    * the "." in the filename.
    * 
    * @param FileName  The filename
    * 
    * @return the number corresponding to the digits to the left of
    * the "." in the filename or -1 if it is not possible to find these digits
    */
   public static int getRunNumber( String FileName)
   {
      if( FileName == null)
         return -1;
      int k= FileName.lastIndexOf( '.' );
      if( k < 0)
         k = FileName.length( );
      String S ="";
      boolean done = false;
      int i;
      for(  i=k-1; i > 0  && !done; )
         if( Character.isDigit( FileName.charAt( i )))
              done = true;
         else
            i--;
      
      k=i;
      done = false;
      for(  i=k; i > 0  && !done; i--)
         if( Character.isDigit( FileName.charAt( i )))
              S = FileName.charAt( i )+S;
         else
             done = true;
      try
      {
         return Integer.parseInt(  S.trim() );
      }catch(Exception s)
      {
         return -1;
      }
   }

   /**
    * Retrieves the rotation angle information from an SNS cvinfo file for detector groups.
    * 
    * @param cvinfoFilename   The name of the cvinfo file to use
    * 
    * @return  Information on the rotation angles or an ErrorString
    *          if the result is not an ErrorString it is a Vector with one
    *          element per rotation.  Each element of the resultant vector is 
    *          also a Vector with 2 elements. The first is the rotation angle in
    *          degrees and the second is an int[] of detector ID's for which
    *          that rotation applies.
    *          
    * NOTE: So far this only applies to the SNAP instrument.
    */
   public static Object  getRotationAngles( String cvinfoFilename)
   {
      if( cvinfoFilename == null || cvinfoFilename.length() <3)
         return new ErrorString("Could not retrieve rotations: No cvinfo File");
      
      String instrument = FileIO.getSNSInstrumentName( cvinfoFilename );
      if( instrument == null ||! instrument.toUpperCase().equals( "SNAP" ))
         return new ErrorString("Could not retrieve rotations: Only works with SNAP so far");
      
      
      try
      {
         Document D = UtilSm.Open( cvinfoFilename );
         Vector<String[]>attributes= UtilSm.Add( 
                          UtilSm.Add( null , "name" , "det_arc1" ,null ),
                          "device","Detector",null);
         
         Node N = UtilSm.NextChildNodeRecursive(D , null , "cvlog" , attributes );
         if( N == null)
            return new Vector();
         
         String S = UtilSm.getNodeValue( N );
         if( S == null)
            return new Vector();
         
         S = S.trim( );
         int k= S.lastIndexOf( ' ' );
         if( k < 0)
            return new ErrorString("cvinfo file is in improper format");
         float ang1 = Float.parseFloat( S.substring( k+1 ).trim());
        
         Vector Res = new Vector();
         Vector Res1 = new Vector();
         Res1.add(  ang1 );
         Res1.add(  IntList.ToArray( "10:18" ) );
         Res.add( Res1 );
         
         attributes= UtilSm.Add( 
               UtilSm.Add( null , "name" , "det_arc2" ,null ),
               "device","Detector",null);

         N = UtilSm.NextChildNodeRecursive(D , null , "cvlog" , attributes );
          if( N == null)
            return Res;

         S = UtilSm.getNodeValue( N );
         if( S == null)
            return Res;

         S = S.trim( );
         k= S.lastIndexOf( ' ' );
         if( k < 0)
         return new ErrorString("cvinfo file is in improper format");
         float ang2 = Float.parseFloat( S.substring( k+1 ).trim());

         Res1 = new Vector();
         Res1.add(  ang2 );
         Res1.add(  IntList.ToArray( "1:9" ) );
         Res.add( Res1 ); 
         return Res;
         
      }catch( NumberFormatException sn)
      {

         return new ErrorString("cvinfo file is in improper format");
         
      }catch( Exception s)
      {
         return new ErrorString("Could not retrieve rotations: "+s);
      }
      
   }
   
   /**
    * @param args
    */
   public static void main(String[] args) throws Exception
   {
      String EventFileName=
                "C:/Users/ruth/SNS/EventData/Snap_240_neutron_event.dat";

      String DetCalFileName="C:/ISAW/InstrumentInfo/SNS/SNAP/SNAP.DetCal";
      String bankInfoFileName=null;
      String MappingFileName=null;
      int firstEvent=1;
      int NumEventsToLoad=8452339;
      boolean  isLog=true;
      float min=.2f;
      float max=10;
      int nUniformbins=10000;
      float first_logStep=.0002f;
      DataSet D = Util.Make_d_DataSet( EventFileName,  
                                       DetCalFileName,
                                       bankInfoFileName, 
                                       MappingFileName, 
                                       firstEvent, 
                                       NumEventsToLoad, 
                                       min, 
                                       max,
                                       isLog,  
                                       first_logStep, 
                                       nUniformbins,
                                       false,
                                       null,
                                       false, 
                                       null,
                                       0,
                                       0);
      Command.ScriptUtil.display( D );
   }
   
   public static void mainMakeTimeFocus(String[] args) throws Exception
   {
      String EventFileName=
                 "C:/Users/ruth/SNS/EventData/Snap_240_neutron_event.dat";

      String DetCalFileName="C:/ISAW/InstrumentInfo/SNS/SNAP/SNAP.DetCal";
      String bankInfoFileName=null;
      String MappingFileName=null;
      int firstEvent=1;
      int NumEventsToLoad=8452339;
      boolean  isLog=false;
      float min=1000f;
      float max=20000;
      int nUniformbins=1000;
      float first_logStep=.0002f;
      DataSet D = Util.MakeTimeFocusedDataSet( EventFileName, 
                                               DetCalFileName,
                                               bankInfoFileName, 
                                               MappingFileName,
                                               firstEvent, 
                                               NumEventsToLoad,
                                               90f,
                                               .5f, 
                                               min, 
                                               max,
                                               isLog, 
                                               first_logStep, 
                                               nUniformbins ,
                                               false,
                                               null,
                                               0,
                                               0);
      Command.ScriptUtil.display( D );
   }

}
