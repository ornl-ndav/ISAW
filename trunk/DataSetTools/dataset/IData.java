/** 
 *  Interface for Data objects.  3/14/2002  D.M.
 */

package  DataSetTools.dataset;

import DataSetTools.dataset.XScale;
import DataSetTools.dataset.IAttributeList;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Attribute;

public interface IData extends IAttributeList 
{
  public static final int KEEP    = 1;           // constants to control how
  public static final int AVERAGE = 2;           // stitching is done where two
  public static final int DISCARD = 3;           // Data blocks overlap

  public static final int SMOOTH_NONE   = 0; 
  public static final int SMOOTH_LINEAR = 1;

  // These "book keeping" methods will be implemented in the Data class, so that
  // the implementations don't need to be repeated in each derived class.  

  public void    setGroup_ID( int id );
  public int     getGroup_ID();

  public void    setSelected( boolean flag );
  public boolean isSelected();
  public void    toggleSelected();
  public boolean isMostRecentlySelected();
  public long    getSelectionTagValue();

  public void    setHide( boolean flag );
  public boolean isHidden();
  public void    toggleHide();

  public void    combineAttributeList( Data d );

  // Each Data object will have a current set of X values.  The base class
  // can implement the methods dealing with them for all derived classes.
  public float[]       getX_values();
  public XScale        getX_scale();

  // These are convenience methods that get a reference to the lower level 
  // array and then copy it and return a copy of it.  These are implemented
  // in the base class.
  public float[]       getCopyOfY_values();
  public float[]       getCopyOfErrors();

  // The remaining methods are abstract and will have to be implemented in 
  // each derived class.  Consequently, the semantics of the methods will vary
  // from class to class.
  // NOTE: There is not a setY_values() or a setX_values().  The x and y 
  //       values must remain coordinated, and are only directly set in the 
  //       constructor or indirectly adjusted using the resample method. 
 
  public boolean isHistogram();
  public float[] getY_values();
  public float[] getY_values( XScale x_scale, int smooth_flag ); 
  public float   getY_value( float x, int smooth_flag ); 

  public float[] getErrors();
  public void    setErrors( float errors[] );
  public void    setSqrtErrors();

  public Data    add( Data d );
  public Data    add( float y, float err );
  public Data    subtract( Data d );
  public Data    subtract( float y, float err );
  public Data    multiply( Data d );
  public Data    multiply( float y, float err );
  public Data    divide( Data d );
  public Data    divide( float y, float err );

  public void    resample( XScale x_scale, int smooth_flag );
  public Data    stitch( Data other_data, int overlap );

  public String  toString();
  public Object  clone();
}
