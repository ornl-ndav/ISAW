/*
 * File:  MNestedForLoops.java 
 *
 * Copyright (C) 2001, Ruth Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 * $Log$
 * Revision 1.2  2002/11/27 23:25:37  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/02/11 21:38:19  rmikk
 * Initial Checkin
 *
*/
package DataSetTools.viewer.Table;

/**  This implements n nested "while" loops whose loop variables are counters
*/
public class MNestedForLoops
{
  INextHandler Nexts[];
  INtupleOperation operation;
  int Permutation[];
  


  public MNestedForLoops( INextHandler Nxt[], INtupleOperation opn, int Perm[])
     {
       Nexts=Nxt;
       operation = opn;
       Permutation = Perm;
     }

   /** implements the operation of a sequence of nested for(while) loops
   */
  public void execute()
   { boolean done = false;
    if( Nexts== null) return;
    if( operation == null) return;
    if( Permutation != null)
      if( Permutation.length != Nexts.length)
         Permutation= null;
    int LoopVars[];
    LoopVars = new int[Nexts.length];
    for(int i=0;i< Nexts.length;i++)
        {LoopVars[i]= Nexts[i].start();
         if( LoopVars[i] < 0) done = true;
        }
    while(! done )
      {
         operation.execute( LoopVars);


         for ( int i= Nexts.length -1; i >= 0; i--)
          { int j=i;
            if(Permutation != null)
              j = Permutation[i];
            LoopVars[ j ] = Nexts[j].next();
            if( LoopVars[j] >= 0)
               i = -1;
            else if( LoopVars[j] < 0)
              {LoopVars[j] = Nexts[j].start();
               if( (i==0) || (LoopVars[j] < 0))
                  done = true;
               }
           
            
           }
         
      }
   }//execute
/** test program for this module
*/
public static void main( String args[])
  {MNextHandler NH[];
   mopn  opn;
   NH= new MNextHandler[4];
   for( int i=0; i<4;i++)
      NH[i] = new MNextHandler( 4*i,4*i+3);
    opn = new mopn();
   
    MNestedForLoops flps = new MNestedForLoops( NH, opn,null);
    flps.execute();



  }

}
class MNextHandler implements INextHandler
  { int first, last, current;
    
    public MNextHandler( int first, int last)
      {this.first = first;
       this.last = last;
       this.current = first;
       

      }
    public int start( ){ current = first; return current;}
    public int next() 
         { if( current+1 <= last)
               {
                current ++;
                return current;
              }
            else return -1;
          }

   }
class mopn  implements INtupleOperation
  {
    public void execute( int X[])
      {
         System.out.print("AAA ");
         for( int i=0;i< X.length; i++)
           System.out.print( X[i] +" ");
         System.out.println("");

      }

  }
