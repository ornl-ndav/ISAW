/*
 * File:  ListHandler.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2004/06/04 22:01:54  rmikk
 * Added Documentation and GPL
 *
 */

package Command.JavaCC.Fortran;
import java.util.*;


/**
 *  This class forms a linked list of items that have a name and value
 * 
 * @author MikkelsonR
 * 

 */
public class ListHandler{

   LList List = null;
   int n=0;
   public ListHandler(){
      List = null;
   }

   /**
    *  Adds the given name, value pair to the list(ordered by name)
    * @param value  The value to be added
    * @param name    The name to be added
    * @return     true if successful otherwise false
    */
   public boolean add( Object value, String name){
      LList newItem = new LList( value,name);
      n++;
      if( List == null)
         List = newItem;
      else
         List.add(newItem);
      return true;

   }
   
   /**
    * Returns the object(value) associated with the given name
    * @param name   The name
    * @return   The value associated with the name
    */
   public Object get( String name){
     if( List == null)
       return null;
     LList L = List.get(name);
     if( L == null)
        return null;
     return L.value;
     

   }
  
  /**
   *   Returns the number of elements in this list
   * @return  The number of elements in this list
   */
  public int size(){
    return n;
  }

  /**
   *   Returns the name of the ith element( sorted by name) in this
   *   list
   * @param i   the position alphabetically in the list of the name   
   * @return   The name associated with that position
   */
  public String getName( int i){
    if( (i < 0) || ( i >= n))
       return null;
    
    return List.get(i).name;

  } 

  /**
   *  Returns the value of the ith member, when arranged alphabetically, in the list
   * @param i   The position of the name in the list when arranged alphabetically
   * @return    The value associated with that position
   */
  public Object getValue( int i){
    if( (i < 0) || ( i >= n))
       return null;
    
    return List.get(i).value;

  } 
  
 /**
  *  Utility for the main test program
  * @return  The next word entered by System.in
  */ 
 public static String readlin(){
     char c=0;
     String S="";
     try{
       while( c<=32)
          c= (char)System.in.read();
       while(c >32){
          S +=c;
          c= (char)System.in.read();
          
       }
     }catch(Exception ss){ c=0;}
     return S;
 } 
 
 /**
  *  Test program for this module. It is menu driven
  * @param args  Not used
  */
 public static void main( String args[]){
   ListHandler LH= new ListHandler();
   char c=0;
   int n=0;
   String S="";
   while( c!='x'){
     System.out.println("a. Add new item");
     System.out.println("S. Show List");
     System.out.println("n. Enter Num");
     System.out.println("s. Enter String");
     System.out.println("g. get nth item");
     System.out.println("G. Get named item");
     System.out.println("x. exit");
     c = readlin().charAt(0);
     if( c=='a')
        LH.add(new Integer(n), S);
     else if( c=='S')
       for( int i=0; i< LH.size();i++)
         System.out.println(LH.getName(i)+","+LH.getValue(i));
     else if( c=='n')
       try{
        n= (new Integer(readlin())).intValue();
       }catch(Exception ss){ n=0;}
     else if( c=='s')
        S=readlin();
     else if( c=='g')
        System.out.println( LH.getName(n)+","+LH.getValue(n));
     else if( c=='G')
        System.out.println(LH.get(S));




   }



 }

  class LList{
    public String name;
    public Object value;
    LList left,right;
    int nleft,nright;
    public LList( Object value, String name){
      this.value=value;
      this.name = name;
      left = right =  null;
       nleft = nright = 0;
    }
    public boolean add( LList neww){
       if( neww == null) 
         return false;
       int st = name.compareTo( neww.name);
       if( st == 0)
         return false;
       if( st > 0)
          if( left == null)
              left = neww;
          else
              left.add(neww);
       else
          if( right == null)
             right = neww;
          else
             right.add( neww);
       if( st > 0) 
         nleft ++;
       else
         nright++;
       return true;

    }
    public LList get( String name){
      int st = this.name.compareTo( name);
      if( st == 0)
        return this;
      if( st > 0)
        if( left == null)
            return null;
        else
            return left.get(name);
      else
        if( right == null)
           return null;
        else
           return right.get(name);


    }
   // ith elt in sublist. Starts at 0 for left most
   public LList get( int i){
      if( i < 0) 
        return null;
      if( i > nleft+nright)
         return null;
      if( nleft == i)
         return this;
      if( i < nleft)
         return left.get(i);
      return right.get( i - nleft-1);


   }

  }//LList


} 
