/* 
 * File: ReducedFormList.java
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
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */
package Operators.TOF_SCD;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.lattice_calc;

import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * This class Manages the list of possible transformations that could apply
 * to a reduced orientation matrix that map the matrix to a conventional cell.
 */
public class ReducedFormList
{

   double[]          scalarsExp;

   ReducedCellInfo[] DataBase;

   /**
    * Constructor. The algorithm used to determine transformation is described
    * in the class ReducedCellInfo
    * 
    * @param LatticeParamsExp  The array of lattice constants
    * 
    * @see ReducedCellInfo
    */
   public ReducedFormList(double[] LatticeParamsExp)
   {

      DataBase = new ReducedCellInfo[ 45 ];

      for( int i = 0 ; i < 45 ; i++ )
         DataBase[i] = new ReducedCellInfo( i , LatticeParamsExp[0] , LatticeParamsExp[1] ,
               LatticeParamsExp[2] , LatticeParamsExp[3] , LatticeParamsExp[4] , LatticeParamsExp[5] );

   }
   
   /**
    * Constructor
    * 
    * @param UB  The orientation matrix for a reduced cell.
    */
   public ReducedFormList( float[][] UB)
   {
      this( lattice_calc.LatticeParamsOfUB(LinearAlgebra.float2double( UB )));
      
   }

   /**
    * Returns information on all transformations that transform the reduced cell
    * to a conventional cell.
    * 
    * @param delta  Max error in any entry of the reduced cell scalar
    * @return     A list of information on all applicable transformations
    */
   public ReducedCellInfo[] getEntries(double delta)
   {

      ReducedCellInfo compareTo = DataBase[0];

      Vector< ReducedCellInfo > V = new Vector< ReducedCellInfo >( );

      for( int i = 1 ; i < 45 ; i++ )

         if ( DataBase[i].distance( compareTo ) < delta )
            V.addElement( DataBase[i] );

      return V.toArray( new ReducedCellInfo[ 0 ] );
   }
   
   /**
    * Not implemented yet.
    * Eliminate all possibilities in the list with any of the lattice types AND
    * any of the centering types. If lattice types is null, all types will be
    *  used.If the centering types is null, all centering types will be used.
    * 
    * @param possibilities   The list of transformation information
    * 
    * @param latticeTypes    The list of lattice types that could be 
    *                        eliminated. If null all lattice types will be used.
    *                        
    * @param CenteringTypes  The  list of centering types that could be 
    *                        eliminated. If null, all types will be used. 
    *                        Possibilites will be eliminated if its lattice type is
    *                        any one in the list of lattice types and centering 
    *                        type is any one of the centering types.
    *                        
    * @return a new list of possibilities with the indicated possibilities eliminated
    */
   public ReducedCellInfo[] EliminateEntries( ReducedCellInfo[] possibilities, 
                                                            String[] latticeTypes,
                                                            String[] CenteringTypes)
   {
      return possibilities;
   }

   /**
    * Test program
    * 
    * @param args  the 6 lattice parameters
    */
   public static void main(String[] args)
   {

      double[] LatticeParams = null;

      while( LatticeParams == null )

         if ( args != null && args.length >= 6 )

            try
            {
               LatticeParams = new double[ 6 ];
               for( int i = 0 ; i < 6 ; i++ )
                  LatticeParams[i] = Double.parseDouble( args[i] );

            } catch( Exception s )
            {
               LatticeParams = null;
               args = null;

            }
         else
         {
            String resp = JOptionPane.showInputDialog( null ,
                  "Enter Lattice Parameters separated by spaces" ,
                  "4.913    4.913   5.40   90   90   120" );

            if ( resp != null )
               args = resp.split( " +" );
         }

      ReducedFormList List = new ReducedFormList( LatticeParams );

      ReducedCellInfo[] Res = List.getEntries( 2 );

      ReducedCellInfo Orig = new ReducedCellInfo( 0 ,
                               LatticeParams[0] ,
                               LatticeParams[1] , 
                               LatticeParams[2] ,
                               LatticeParams[3] ,
                               LatticeParams[4] , 
                               LatticeParams[5] );

      for( int i = 0 ; i < Res.length ; i++ )
      {
         System.out.println( Res[i].toString( ) );
         System.out.println( "      err=" + Res[i].distance( Orig )
               + "\n      ------------------------------------------\n" );
      }

   }

}
