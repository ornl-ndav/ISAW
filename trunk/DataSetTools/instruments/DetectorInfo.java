/*
 * File:   DetectorInfo.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2001/07/10 18:46:46  dennis
 *  Initial version of Class to hold information on individual
 *  detector positions, sizes, efficiency, etc.
 *
 *
 */
package  DataSetTools.instruments;

import java.io.*;
import java.text.*;
import DataSetTools.math.*;

/**
 * DetectorInfo represents the position, size, ID, and efficiency of an
 * individual detector segment.  This may be only one pixel of an area
 * detector or LPSD, but may also be an entire detector if it is not
 * segmented.
 */
public class DetectorInfo implements Serializable
{
  private int               seg_num;   // ID for this particular segment
  private int               det_num;   // "raw" detector ID for detector
                                       // containing this detector segment
  private int               row,       // row and column number for this
                            column;    // segment on the detector

  private DetectorPosition  position; 

  private float             length;
  private float             width;
  private float             depth;

  private float             efficiency;

  /**
   *  Construct a new DetectorInfo object with the specified data values
   */
  public DetectorInfo( int               seg_num, 
                       int               det_num, 
                       int               row, 
                       int               col,
                       DetectorPosition  position,
                       float             length,
                       float             width,
                       float             depth,
                       float             efficiency )
  {
    this.seg_num    = seg_num;
    this.det_num    = det_num;
    this.row        = row;
    this.column     = col;
    this.position   = position;
    this.length     = length;
    this.width      = width;
    this.depth      = depth;
    this.efficiency = efficiency;
  }


  /**
   *  Construct a new DetectorInfo object with the same data values as the
   *  specified DetectorInfo object
   */
  public DetectorInfo( DetectorInfo  info )
  {
    this.seg_num    = info.seg_num;
    this.det_num    = info.det_num;
    this.row        = info.row;
    this.column     = info.column;
    this.position   = info.position;
    this.length     = info.length;
    this.width      = info.width;
    this.depth      = info.depth;
    this.efficiency = info.efficiency;
  }


  /**
   *  Get the segment number for this Detector segment. 
   *
   *  @return  The segment number for this object.
   */
  public int getSeg_num()
  {
    return seg_num;
  }

  /**
   *  Get the main detector number for the Detector segment.
   *
   *  @return  The detector number for this object.
   */
  public int getDet_num()
  {
    return det_num;
  }

  /**
   *  Get the row number for the Detector segment.
   *
   *  @return  The row number for this segment.
   */
  public int getRow()
  {
    return row;
  }


  /**
   *  Get the column number for the Detector segment.
   *
   *  @return  The column number for this segment.
   */
  public int getColumn()
  {
    return column;
  }


  /** 
   *  Get the nominal detector position for the Detector segment.
   *
   *  @return  The detector position for this object.
   */
  public DetectorPosition getPosition()
  {
    return position;
  }


  /** 
   *  Get the nominal detector length for the Detector segment.
   *
   *  @return  The detector length for this object.
   */
  public float getLength()
  {
    return length;
  }


  /** 
   *  Get the nominal detector width for the Detector segment.
   *
   *  @return  The detector width for this object.
   */
  public float getWidth()
  {
    return width;
  }


  /** 
   *  Get the nominal detector depth for the Detector segment.
   *
   *  @return  The detector depth for this object.
   */
  public float getDepth()
  {
    return depth;
  }


  /** 
   *  Get the nominal detector efficiency for the Detector sgement.
   *
   *  @return  The detector efficiency for this object.
   */
  public float getEfficiency()
  {
    return efficiency;
  }

 
  /**
   *  Form a string listing the detector info.
   *
   *  @return  String containing the detector ID, location and size information.
   */
  public String toString()
  {
     NumberFormat f = NumberFormat.getInstance();
     f.setMaximumFractionDigits( 2 );

     String s = "Seg: " + seg_num + " Det: " + det_num + "\n";
     s += "(row, col) = (" + row + ", " + column + ")\n";
     s += position.toString() + "\n";

     s += "Size: " + f.format( length ) + 
          "x" + f.format( width ) +
          "x" + f.format( depth ) + "\n";
 
     s += "Efficiency: " + f.format( efficiency ); 

     return s;
  }

  static public void main( String[] args )
  {
    DetectorPosition point = new DetectorPosition();

    point.setSphericalCoords( -10, (float)Math.PI/6, (float)Math.PI/4 );

    DetectorInfo det_info = new DetectorInfo( 1, 2, 3, 4, point, 5, 6, 7, 8 );
    System.out.println( ""+ det_info );
  }

}
