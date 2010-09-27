package EventTools.ShowEventsApp.Command;

import java.util.Arrays;
import java.util.Vector;

public class Commands 
{
  public static final String DQLOG_SCALE  ="DQLOG_SCALE";//arg boolean  

  public static final String EXIT_APPLICATION = "EXIT_APPLICATION";

  public static final String UPDATE         = "UPDATE";
   
  public static final String LOAD_FILE      = "LOAD_FILE";
  
  public static final String LOAD_FAILED    = "LOAD_FAILED";
  
  
  public static final String LOAD_FILE_DATA = "LOAD_FILE_DATA";
  
  public static final String LOAD_FILE_DONE = "LOAD_FILE_DONE";
  
  public static final String LOAD_UDP_EVENTS = "LOAD_UDP_EVENTS";
  
  public static final String LOAD_CONFIG_INFO ="LOAD_CONFIG_INFO";
  
  public static final String PAUSE_UDP       = "PAUSE_UDP";
  
  public static final String CLEAR_UDP       = "CLEAR_UDP";
  
  public static final String CONTINUE_UDP    = "CONTINUE_UDP";
  
  public static final String SCALE_FACTOR    = "SCALE_FACTOR";
  
  public static final String NORMALIZE_QD_GRAPHS = "NORMALIZE_QD_GRAPHS";
  public static final String NORMALIZE_D_GRAPH = "NORMALIZE_D_GRAPH";
  public static final String NORMALIZE_Q_GRAPH = "NORMALIZE_Q_GRAPH";
  public static final String Q_GRAPH_NORMALIZER ="Q_GRAPH_NORMALIZER";
  public static final String D_GRAPH_NORMALIZER ="D_GRAPH_NORMALIZER";
  public static final String MAP_EVENTS_TO_Q = "MAP_EVENTS_TO_Q";

  public static final String ADD_EVENTS_TO_HISTOGRAMS
                                             = "ADD_EVENTS_TO_HISTOGRAMS";

  public static final String NUMBER_HISTOGRAMED = "NUMBER_HISTOGRAMED";
  
  public static final String ADD_EVENTS_TO_VIEW = "ADD_EVENTS_TO_VIEW";

  public static final String INIT_HISTOGRAM  = "INIT_HISTOGRAM";

  /**
   * CAUTION: The following "DONE" messages should only be sent back once
   * by the handlers for the DQ, 3D_Histogram and Q_Mapper, in response to
   * requests to initialize these structures.  If sent back multiple times,
   * there will be multiple requests to begin loading the actual file data.
   */
  public static final String INIT_DQ_DONE        = "INIT_DQ_DONE";
  public static final String INIT_HISTOGRAM_DONE = "INIT_HISTOGRAM_DONE";
  public static final String HISTOGRAM_RESIZED   = "HISTOGRAM_RESIZED";

  public static final String INIT_NEW_INSTRUMENT_DONE 
                                                = "SET_NEW_INSTRUMENT_DONE";


  public static final String INIT_EVENTS_VIEW = "INIT_EVENTS_VIEW";

  public static final String SET_WEIGHTS_FROM_HISTOGRAM = 
                                              "SET_WEIGHTS_FROM_HISTOGRAM";

 /**
  *  When a new data file is about to be loaded, this message should
  *  be sent with the instrument name as it's value, so other objects
  *  can start to re-configure themselves.  e.g. make a new default histogram
  */
  public static final String SET_NEW_INSTRUMENT = "SET_NEW_INSTRUMENT";
  public static final String INIT_NEW_INSTRUMENT = "INIT_NEW_INSTRUMENT";

  /**
   *  The data flow for gathering all of the required information to display
   *  regarding a selected point is as follows.  The initial message,
   *  SELECT_POINT comes from the EventViewHandler, when the user selects
   *  a point.  The class maintaining the SNS_TOF_to_Q_Map object 
   *  receives this message, and fills out most of the required information.
   *  It then sends an ADD_HISTOGRAM_INFO message to request that the 
   *  HistogramHandler add the histogram intensity at that point.
   *  The HistogramHandler should pass the select_info_cmd 
   *  on to the OrientationMatrixHandler by sending an ADD_ORIENTAION_INFO
   *  message.  The OrientationMatrixHandler then adds the hkl values
   *  and sends the info to be displayed in a SHOW_SELECTED_POINT_INFO
   *  command.
   */
  public static final String SELECT_POINT = "SELECT_POINT";
  public static final String ADD_HISTOGRAM_INFO = "ADD_HISTOGRAM_INFO";
  public static final String ADD_ORIENTATION_MATRIX_INFO =
                                                 "ADD_ORIENTATION_MATRIX_INFO";
  public static final String ADD_PEAK_LIST_INFO = "ADD_PEAK_LIST_INFO";
  public static final String SHOW_SELECTED_POINT_INFO =
                                                 "SHOW_SELECTED_POINT_INFO";

  public static final String GET_HISTOGRAM_MAX = "GET_HISTOGRAM_MAX";
  public static final String SET_HISTOGRAM_MAX = "SET_HISTOGRAM_MAX";

  public static final String FIND_PEAKS        = "FIND_PEAKS";

  /**
   *  SET_PEAK_Q_LIST has a PeakQ_cmd object as it's  message value.
   */
  public static final String SET_PEAK_Q_LIST   = "SET_PEAK_Q_LIST";
  
  /**
   *  GET_PEAK_NEW_LIST has a Vector of PeakQ objects as it's  message value.
   */

  public static final String GET_PEAK_NEW_LIST = "GET_PEAK_NEW_LIST";
  /**
   *  SET_PEAK_NEW_LIST has a Vector of Peak_new objects as it's  message value.
   */
  public static final String SET_PEAK_NEW_LIST      = "SET_PEAK_NEW_LIST";
  public static final String GET_PEAK_IMAGE_REGIONS = "GET_PEAK_IMAGE_REGIONS";
  public static final String CLOSE_PEAK_IMAGES      = "CLOSE_PEAK_IMAGES";
  public static final String MAKE_PEAK_IMAGES       = "MAKE_PEAK_IMAGES";
  public static final String SHOW_PEAK_IMAGES       = "SHOW_PEAK_IMAGES";
  public static final String PEAK_LIST_CHANGED      = "PEAK_LIST_CHANGED";
  public static final String CLEAR_PEAK_LISTS       = "CLEAR_PEAK_LISTS";
  public static final String FILTER_DETECTOR        = "FILTER_DETECTOR";
  public static final String FILTER_QD              = "FILTER_QD";
  public static final String FILTER_PEAKS           = "FILTER_PEAKS";

  /**
   * MARK_PEAKS has value Boolean OR a vector of PeakQ objects
   */
  public static final String MARK_PEAKS = "MARK_PEAKS";

  /**
   * MARK_INDEXED_PEAKS has value Boolean OR a vector of Peak_new objects
   */
  public static final String MARK_INDEXED_PEAKS = "MARK_INDEXED_PEAKS";

  public static final String WRITE_PEAK_FILE = "WRITE_PEAK_FILE";

  public static final String SHOW_PEAK_FILE = "SHOW_PEAK_FILE";

  public static final String CHANGE_PANEL = "CHANGE_PANEL";
  public static final String SET_COLOR_SCALE = "SET_COLOR_SCALE";
  public static final String SET_DRAWING_OPTIONS = "SET_DRAWING_OPTIONS";

  public static final String CLEAR_ORIENTATION_MATRIX
                                                  = "CLEAR_ORIENTATION_MATRIX";
  public static final String SET_ORIENTATION_MATRIX = "SET_ORIENTATION_MATRIX";
  public static final String GET_ORIENTATION_MATRIX = "GET_ORIENTATION_MATRIX";

  public static final String PLANE_CHANGED = "PLANE_CHANGED";
  public static final String SLICE_MODE_CHANGED = "SLICE_MODE_CHANGED";
  public static final String SET_SLICE_1 = "SET_SLICE_1";
  public static final String DISPLAY_INFO = "DISPLAY_INFO";
  public static final String DISPLAY_ERROR = "DISPLAY_ERROR";
  public static final String DISPLAY_WARNING = "DISPLAY_WARNING";
  public static final String DISPLAY_CLEAR = "DISPLAY_CLEAR";

  public static final String WRITE_ORIENTATION_MATRIX = 
                                                  "WRITE_ORIENTATION_MATRIX";
  public static final String SHOW_ORIENTATION_MATRIX = 
                                                  "SHOW_ORIENTATION_MATRIX";
  /**
   * Will currently get the PeakList indexed with the read orientation matrix
   */
  public static final String READ_ORIENTATION_MATRIX = 
                                                  "READ_ORIENTATION_MATRIX";

  public static final String INDEX_PEAKS = "INDEX_PEAKS";

  public static final String INDEX_PEAKS_WITH_ORIENTATION_MATRIX =
                                         "INDEX_PEAKS_WITH_ORIENTATION_MATRIX";
  
  public static final String INDEX_PEAKS_ROSS = "INDEX_PEAKS_ROSS";

  public static final String INDEX_PEAKS_ARCS = "INDEX_PEAKS_ARCS";
  
  public static final String INIT_DQ = "INIT_DQ";
  
  public static final String SHOW_Q_GRAPH = "SHOW_Q_GRAPH";
  public static final String HIDE_Q_GRAPH = "HIDE_Q_GRAPH";
  public static final String GET_Q_VALUES = "GET_Q_VALUES";
  public static final String SET_Q_VALUES = "SET_Q_VALUES";
  public static final String SAVE_Q_VALUES ="SAVE_Q_VALUES";
  
  public static final String SHOW_D_GRAPH = "SHOW_D_GRAPH";
  public static final String HIDE_D_GRAPH = "HIDE_D_GRAPH";
  public static final String GET_D_VALUES = "GET_D_VALUES";
  public static final String SET_D_VALUES = "SET_D_VALUES";
  public static final String SAVE_D_VALUES ="SAVE_D_VALUES";
  
  public static final String SHOW_MESSAGE_PANE ="SHOW_MESSAGE_PANE";
  public static final String SHOW_DISPLAY_PANE ="SHOW_DISPLAY_PANE";

  /**
   * no or null value
   */
  public static final String CLEAR_OMITTED_PIXELS ="CLEAR_OMITTED_PIXELS";

  /*
   * Value is a Vector of Vectors where the first vector is the filename(null)
   * then each of the next Vector elements consist of 3 entries of int[] or 
   * null.
   */
  public static final String APPLY_OMITTED_PIXELS ="APPLY_OMITTED_PIXELS";
  
  /**
   * Value is a Vector with 2 elements. The first is boolean indicating whether
   * to omit or keep the ranges and the 2nd is a float[] where each pair is 
   * start/end of interval to omit/keep
   */
  public static final String APPLY_OMITTED_DRANGE ="APPLY_OMITTED_DRANGE";

  public static final String CLEAR_OMITTED_DRANGE ="CLEAR_OMITTED_DRANGE";
     

  /**
   * Messages for quick event integration.
   */
  public static final String GET_PEAKS_TO_SPHERE_INTEGRATE = 
                                              "GET_PEAKS_TO_SPHERE_INTEGRATE";
  public static final String SPHERE_INTEGRATE_PEAKS = 
                                              "SPHERE_INTEGRATE_PEAKS";
  public static final String REVERSE_WEIGHT_INTEGRALS = 
                                              "REVERSE_WEIGHT_INTEGRALS"; 
  public static final String INIT_INTEGRATE_HISTOGRAM =
                                              "INIT_INTEGRATE_HISTOGRAM";
  public static final String FREE_INTEGRATE_HISTOGRAM =
                                              "FREE_INTEGRATE_HISTOGRAM";
  public static final String INTEGRATE_HISTOGRAM_READY =
                                              "INTEGRATE_HISTOGRAM_READY";
  public static final String ADDED_EVENTS_TO_INTEGRATE_HISTOGRAM =
                                         "ADDED_EVENTS_TO_INTEGRATE_HISTOGRAM";
  public static final String INTEGRATE_HISTOGRAM_FREED =
                                              "INTEGRATE_HISTOGRAM_FREED";
  public static final String SET_STEPS_PER_MILLER_INDEX =
                                              "SET_STEPS_PER_MILLER_INDEX";
  public static final String SET_HISTOGRAM_SPACE_MB =
                                              "SET_HISTOGRAM_SPACE_MB";
  public static final String SCAN_INTEGRATED_INTENSITIES =
                                              "SCAN_INTEGRATED_INTENSITIES";
  public static final String MAKE_INTEGRATED_PEAK_Q_LIST =
                                              "MAKE_INTEGRATED_PEAK_Q_LIST";
  public static final String SET_INTEGRATED_PEAKS_LIST =
                                              "SET_INTEGRATED_PEAKS_LIST";
  public static final String SET_INTEGRATED_INTENSITY_STATS =
                                              "SET_INTEGRATED_INTENSITY_STATS";

  /*
//  public static final String SET_POSITION_INFO = "SET_POSITION_INFO";
  public static final String SET_SLICE_2 = "Set Slice 2";
  public static final String SET_SLICE_3 = "Set Slice 3";  
  public static final String SET_COLOR_TABLE = "Set New Color Table";
  public static final String SET_VIEW = "Set View";
  public static final String SET_MARKER = "Set New Marker";
  public static final String SET_WAYPOINT = "Set New Waypoint";
  public static final String SET_HISTOGRAM = "Set New Histogram";
  public static final String ROTATE_CAM_ABOUT_COP = "Rotate cam about cop";
  public static final String ROTATE_CAM_ABOUT_VRP = "Rotate cam about vrp";
  public static final String SHOW_SLICE_CONTROL = "Show Slice Control";
  public static final String LOAD_MATRIX = "Load Orientation Matrix";
  public static final String EXIT = "Exit";
  public static final String SHOW_SLICE = "Show Slice";
  public static final String SHOW_LOAD_FILE = "Show Load File";
  public static final String LOAD_PEAK_FILE = "Load Peak File";
  public static final String WRITE_ORIENTATION_FILE = "Write Orientation File";
  public static final String WRITE_INDEX_FILE = "Write Index File";
  public static final String OMIT_PEAKS = "Omit Peaks";*/
  //*/
  
  public static Vector MakeSET_ORIENTATION_MATRIX_arg( float[][] UB, float[] sig)
  {
     if( UB == null)
     {
        UB = new float[3][3];
        Arrays.fill( UB[0],0f  );
        Arrays.fill( UB[1],0f  );
        Arrays.fill( UB[2],0f  );
     }
     if( sig == null)
     {
        sig = new float[7];
        Arrays.fill(  sig , 0f );
     }
     
     Vector  V = new Vector(2);
     V.add( UB );
     V.add( sig );
     return V;
  }
}
