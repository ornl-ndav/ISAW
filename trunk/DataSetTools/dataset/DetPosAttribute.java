/*
 * @(#)DetPosAttribute.java       1.0 99/06/10  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.3  2000/07/10 22:23:56  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.7  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 *
 */

package  DataSetTools.dataset;

import   java.text.*;
import   DataSetTools.math.*;

/**
 * The concrete class for an attribute whose value is a Position3D object.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 *
 * @version 1.0  
 */

public class DetPosAttribute extends Attribute
{
  private DetectorPosition value;

  /**
   * Constructs a DetPosAttribute object using the specified name and value.
   */
  public DetPosAttribute( String name, DetectorPosition value )
  {
    super( name );
    this.value = new DetectorPosition( value );
  }


  /**
   * Returns a copy the DetectorPosition object that is the value of this
   * attribute, as a generic object.
   */
  public Object getValue( )
  {
     return new DetectorPosition( value );
  } 

  /**
   * Set the value for the int attribute using a generic object.  The actual
   * class of the object must be a Position3D object.
   */
  public boolean setValue( Object obj )
  {
    if ( obj instanceof Position3D )
      value = new DetectorPosition( (Position3D)obj );
    else
      return false;

    return true;
  }   

  /**
   * Returns a copy the DetectorPosition object that is the value of this
   * attribute.
   */
   public DetectorPosition getDetectorPosition( )
   {
     return new DetectorPosition( value );
   }


  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is obtained by averaging the 3D Positions represented by
   * the two attributes.
   *
   *  @param   attr   A DetPosAttribute whose position is to be averaged 
   *                  with the value of the this attribute.
   */
  public void combine( Attribute attr )
  {
     if ( !(attr instanceof DetPosAttribute) )       // can't combine
       return;

     float xyz[] = new float[3]; 
     float this_xyz[],
           other_xyz[];

     this_xyz  = this.value.getCartesianCoords();
     other_xyz = ((DetectorPosition)attr.getValue()).getCartesianCoords();

     for ( int i = 0; i < 3; i++ )
       xyz[i] = ( this_xyz[i] + other_xyz[i] ) / 2.0f;
  
     this.value.setCartesianCoords( xyz[0], xyz[1], xyz[2] );
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {
     return value.getScatteringAngle();
   }


  /**
   * Returns a string representation of the float value of this attribute
   */
   public String getStringValue()
   {
     float cyl_coords[] = value.getCylindricalCoords();
     
     NumberFormat f = NumberFormat.getInstance();
     f.setMaximumFractionDigits( 2 );
     String scat_ang = f.format( value.getScatteringAngle() * 180.0/Math.PI );

     f.setMaximumFractionDigits( 3 );
     String r     = f.format( cyl_coords[0] );
     f.setMaximumFractionDigits( 2 );
     String cyl_angle = f.format( cyl_coords[1] * 180.0/Math.PI );
     f.setMaximumFractionDigits( 3 );
     String z     = f.format( cyl_coords[2] );
                                                    // upper case theta: \u0398
                                                    // lower case theta: \u03b8
                                                    // upper case phi:   \u03a6
                                                    // lower case phi:   \u03c6
     String string = "2\u03b8" +"=" + scat_ang +
                     ":r="  + r + 
                     ","+"\u03c6" +"=" + cyl_angle + 
                     ",z=" + z;
     return string;
   }

  /**
   * Returns a string representation of the (name,value) pair for this
   * attribute
   */
  public String toString()
  {
     return this.getName() + ": " + this.getStringValue();
  }

  /**
   * Returns a copy of the current attribute
   */
  public Object clone()
  {
    return new DetPosAttribute( this.getName(), value );
  }

}
