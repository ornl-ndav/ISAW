package DataSetTools.viewer.OverplotView;

/**
 * $Id$
 *
 * defines a way for arbitrary graphics packages to play nice with
 * OverplotView.
 *
 * $Log$
 * Revision 1.1  2002/07/18 22:06:19  dennis
 * Moved separate OverplotView hiearchy into DataSetTools/viewer
 * hierarchy.
 *
 * Revision 1.2  2001/06/28 22:06:24  neffk
 * added setAttributeList( AttributeList l ) to the interface so that the
 * graph can store things like units, labels, and the title--things that are
 * global for all data on the graph.
 *
 * Revision 1.1  2001/06/27 18:32:35  neffk
 * changed the name of the interface to start w/ I.
 *
 */


import DataSetTools.dataset.AttributeList;
import java.util.Vector;
import javax.swing.JComponent;


public interface IGraphableDataGraph
{
  public static final String TITLE      = "Graph Title";
  public final static String TITLE_SUB1 = "First Graph Subtitle";
  public final static String TITLE_SUB2 = "Second Graph Subtitle";

  public final static String X_UNITS    = "X-Axis Units";
  public final static String Y_UNITS    = "Y-Axis Units";

  public final static String X_LABEL    = "X-Axis Label";
  public final static String Y_LABEL    = "Y-Axis Label";


  /**
   * converts from GraphableData (the internal format for this viewer)
   * to another format.  this allows future developers/maintainers of 
   * ISAW to plug in a new graphic front end.  
   *
   * this method replaces all previous data.  it does NOT
   * graph the data--graphics are handled by the redraw() method.
   *
   * @param gd - a vector of GraphabldData objects
   */
  public void init( Vector gd );

 
  /**
   * draws all current data on the graphics object, whatever it
   * might be.  
   */
  public JComponent redraw();


  /**
   * s
   */
  public void setAttributeList( AttributeList l );
}  


