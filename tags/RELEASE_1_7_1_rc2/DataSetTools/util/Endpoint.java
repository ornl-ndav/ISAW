/*
 * File: Endpoint.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:23:49  pfpeterson
 * standardized header
 *
 */
package DataSetTools.util;

import DataSetTools.dataset.Attribute;

public class Endpoint
{
  final int SEPERATOR = ':';
  private boolean closed;
  private Attribute attr;

  public Endpoint( Attribute a, boolean closed_ )
  {
    set( a, closed_ );
  }


  public void set( Attribute a, boolean closed_ )
  {
    attr = a;
    closed = closed_;
  }


  public Attribute getAttr()
  {
    return attr;
  }


  /**
   * test if this object comes before the given Endpoint.  this method
   * considers whether the interval is open or closed.
   */
  public boolean isBefore( Endpoint e )
  {
    if( closed )
      if( attr.compare(e.getAttr()) <= 0 )
        return true;
      else if( attr.compare(e.getAttr()) > 0 )
        return false;
    
    if( !closed )
      if( attr.compare(e.getAttr()) < 0 )
        return true;
      else if( attr.compare(e.getAttr()) >= 0 )
        return false;

    return false;  //javac is retarded
  }

  /**
   * test if this object comes after 'e' the given ednpoint.  this method
   * takes into account whether the interval is open or closed.  
   * if e comes after this...
   */
  public boolean isAfter( Endpoint e )
  {
    if( closed )
      if( attr.compare(e.getAttr()) >= 0 )
        return true;
      else if( attr.compare(e.getAttr()) < 0 )
        return false;
  
    if( !closed )
      if( attr.compare(e.getAttr()) > 0 )
        return true;
      else if( attr.compare(e.getAttr()) <= 0 )
        return false;

    return false;  //javac is retarded
  }

  
  public boolean isClosed()
  {
    if( closed )
     return true;
    else
      return false;
  }


  public void setClosed( boolean c )
  {
    closed = c;
  }


  public String toString()
  {
    return new String( attr.getName() + "=" + attr.getValue() );
  }

 
  public Object clone()
  {
    return new Endpoint( attr, closed );
  }

}
