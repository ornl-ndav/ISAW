/*
 * File:  BestFitTest.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/03/19 21:38:43  dennis
 * Basic functionality test and example of use of the
 * LinearAlgebra.BestFitMatrix(M,q,r) method.
 *
 */
import java.io.*;
import DataSetTools.math.*;

public class BestFitTest
{
  public static void main( String args[] )
  {
    double q[][] = { { 1.1, 0, 0 },               // list of "q" vectors
                     { 1.9, 0, 0 },
                     { 2.9, 0, 0 },
                     { 4.1, 0, 0 },
                     {  .75, 0,  .75 },
                     { 1.50, 0, 1.50 },
                     { 2.25, 0, 2.25 },
                     { 3.00, 0, 3.00 },
                     { 0, 1, 1.5 },
                     { 0, 2, 3.0 },
                     { 0, 3, 4.5 },
                     { 0, 4, 6.0 } };

    double r[][] = { { 1, 0, 0 },
                     { 2, 0, 0 },
                     { 3, 0, 0 },
                     { 4, 0, 0 },
                     { 0, 1, 0 },
                     { 0, 2, 0 },
                     { 0, 3, 0 },
                     { 0, 4, 0 },
                     { 0, 0, 1 },
                     { 0, 0, 2 },
                     { 0, 0, 3 },
                     { 0, 0, 4 } };
  
    double M[][] = new double[3][3];

    double errors = LinearAlgebra.BestFitMatrix( M, q, r ); 

    System.out.println("Residual errors are: " + errors );
    System.out.println("Matrix M is : " );
    for ( int row = 0; row < 3; row++ )
    {
      for ( int col = 0; col < 3; col++ )
        System.out.print( " " + M[row][col] + "    " );
      System.out.println();
    }
  }
}
