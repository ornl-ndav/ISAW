package OverplotView;

/**
 * $Id$
 *
 * defines a way for arbitrary graphics packages to play nice with
 * OverplotView.
 *
 * $Log$
 * Revision 1.1  2001/06/27 18:32:35  neffk
 * changed the name of the interface to start w/ I.
 *
 */

import java.util.Vector;
import javax.swing.JComponent;
import OverplotView.GraphableData;

public interface IGraphableDataGraph
{

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
   * draws all current data on the graphics display
   */
  public JComponent redraw();
}  


