/* 
 * File: MakeSearchData.java
 *  
 * Copyright (C) 2005  Dominic Kramer and Galina Pozharsky
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            Galina Pozharsky<pozharskyg@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 * $Log$
 * Revision 1.3  2006/10/22 18:26:59  rmikk
 * Changed directory where the presearched database info is stored
 *
 * Revision 1.2  2006/09/26 13:51:03  rmikk
 * Fixed the calculation for the position of a word so that the highlighting is closer
 *    in the search view for this word
 *
 * Revision 1.1  2006/09/15 14:43:27  rmikk
 * Fixed another off by one error so that the top row now displays proper info
 *   in the conversions table when selecteed.
 *
 * */


package devTools;

import java.io.*;
import Command.*;
import java.util.*;
//import java.lang.reflect.*;
import DataSetTools.operator.DataSet.*;
import IsawHelp.HelpSystem.*;

/**
 * Creates a Search Data Base for searching operators
 * 
 * @author Ruth
 * 
 */
public class MakeSearchData {
   FileOutputStream fout1 , fout2;

   String[]         Words;         // List of words

   int[]            buffptrs;     // Pointer to "last" word

   int[]            ranks;         // sort info on Words

   int              NWords;

   int              file2Offset;

   
   

   public MakeSearchData() throws Exception {
      
      super();
      new DataSetTools.util.SharedData();
      String HelpDir = System.getProperty( "user.home" );
      //f( HelpDir == null ) exitt( " No Help directory in IsawProps.dat" );
      HelpDir = HelpDir.replace( '\\' , '/' );
      if( ! HelpDir.endsWith( "/" ) ) HelpDir += "/";
      HelpDir += "ISAW/";
      
      
      fout1 = new FileOutputStream( HelpDir + "keys.txt" );
      fout2 = new FileOutputStream( HelpDir + "data.txt" );

      Words = new String[ 800 ];
      buffptrs = new int[ 800 ];
      ranks = new int[ 800 ];
      NWords = 0;
      file2Offset = 0;

      Script_Class_List_Handler SCLH = new Script_Class_List_Handler();
      HTMLizer mkHTM = new HTMLizer();
      for( int i = 0 ; i < SCLH.getNum_operators() ; i++ ) {

         DataSetTools.operator.Operator op = SCLH.getOperator( i );
         String doc = mkHTM.createHTML( op );

         String filename = SCLH.getOperator( i ).getSource();
         String Command = SCLH.getOperator( i ).getCommand();
         //System.out.println("-------------------"+Command+"----------------");
         update( "Generic" , Command , filename , doc );


      }


      for( int i = 0 ; i < SCLH.getNumDataSetOperators() ; i++ ) {
         
         DataSetOperator dsOp = SCLH.getDataSetOperator( i );
         String doc = mkHTM.createHTML( dsOp );
         String Command = dsOp.getCommand();
         String filename = dsOp.getSource();
         update( "DataSet" , Command , filename , doc );

      }


      fout2.close();
      
      //Now write the file with the words and pointers to info
      //   on the first word in this list
      
      fout1.write( ( System.currentTimeMillis() + "\n" ).getBytes() );
      fout1.write( ( "" + NWords + "\n" ).getBytes() );
      byte[] chArray = new byte[ 8 ];
      for( int i = 0 ; i < NWords ; i++ ) {

         Arrays.fill( chArray , (byte) 32 );
         int NN = Words[ ranks[ i ] ].length();
         NN = Math.min( NN , 8 );
         System.arraycopy( Words[ ranks[ i ] ].getBytes() , 0 , chArray , 0 ,
                  NN );
         fout1.write( chArray );
         
         String S = "" + buffptrs[ ranks[ i ] ];
         Arrays.fill( chArray , (byte) 32 );
         for( int k = 0 ; k < S.length() ; k++ )
            chArray[ k ] = (byte) S.charAt( k );
         
         fout1.write( chArray );
      }
   }


   public void exitt( String message ) {
      
      System.out.println( message );
      System.exit( 0 );
   }


   public void update( String tag , String Command , String filename ,
            String doc ) {
      
      String word;
      getWord( doc );
      
      for( word = getNextWord() ; word != null ; word = getNextWord() ) {
         
         //System.out.println("^^^^^^^^^^^^^^^^^^^^"+word+"^^^^^^^^^^^^^^^");
         int p = Viewpos - word.length();
         append( word , p , tag , Command , filename );
      }

   }

   String               doc       = null;

   int                  pos       = 0;

   int                  Viewpos   = 0;

   public static String OmitWords = ";html;body;hr;li;br;font;size;the;a;is;at;algorith;param;error;overview;n;t;bgcolor;"
                                           + "table;td;tr;colspan;align;center;title;head;border;cellspacing;width;class;size;title;www;w3;org;frameset;dtd;"
                                           + "isaw;python;script;class;wrapped;b;u;ol;ul;command;i;helpsyst;assumptio;return;last;modified;a;am;an;are;as;all;"
                                           + "be;been;but;by;of;if;off;on;in;over;under;while;after;can;could;should;would;he;it;them;those;that;when;for;do;did;"
                                           + "this;operator;to;java;int;string;float;object;vector;paramete;and;its;no;not;or;vectors;then;into;either;any;was;"
                                           + "has;from;next;down;with;only;less;than;must;such;what;get;gets;take;takes;their;they;go;always;too;many;how;make;so;"
                                           + "same;per;just;had;were;copy;path;fill;small;large;later;good;above;take;here;like;well;really;being;where;about;";


   public void getWord( String doc ) {
      
      // doc = fixString( doc );
      this.doc = doc.toLowerCase();
      pos = doc.indexOf( "<body" );
      if( pos < 0 ) 
          pos = 0;
      
      Viewpos = 1;
   }


   // Eliminates 2 spaces and \n and \r
   private String fixString( String doc ) {
      
      if( doc == null ) 
         return null;
      
      String Res = "";
      char prevChar = 0;
      
      for( int i = 0 ; i < doc.length() ; i++ ) {
         
         char c = doc.charAt( i );
         if( c == '\n' ) {
         } else if( c == 'r' ) {
         } else if( c == ' ' ) {
            if( prevChar == ' ' ) {
            } else {
               prevChar = ' ';
               Res += ' ';
            }
         } else {
            Res += c;
            prevChar = c;
         }

      }
      return Res;

   }

   
  /**
   * Gets the next word in a string. The string is set in the getWord method
   * 
   * @return  the next word in the string.
   */
   public String getNextWord() {
      
      if( doc == null ) 
         return null; 
      
      if( pos >= doc.length() ) 
         return null;
      
      boolean inDirective = false;
    // System.out.println("              --------------------------------              ");
    
      String S = "";
      char c = doc.charAt( pos++ );
      S = null;
      boolean lastCharisSpace = false;
      boolean startOfLine = false;
      while( ( ( c <= ' ' )
               || ( ( "<>/\\.,=?[]{}+-)(*&^%$#@!:;\"\'0123456789" ).indexOf( c ) >= 0 ) || inDirective )
               && ( pos < doc.length() ) ) {
         
       
         if( inDirective && ( S != null ) ) {
            if( c != '>' ) S += c;
            S = S.trim();
            if( S.length() > 0 )
               if( ( c == ' ' ) || ( c == '>' ) ) {
                 
                  if( ";p;br;body;/h1;/h2;/h3;li;/li;hr;tr;/ol;/ul;/table;".indexOf( ";"
                           + S + ";" ) >= 0 ){ 
                        if( ! lastCharisSpace )
                            Viewpos++ ;
                     lastCharisSpace = false;
                     startOfLine = true;// leading spaces on a line are omitted
                  }
                  S = null;
               }
         }
          if( c == '<' ) {
            
            inDirective = true;
            S = "";
         }
          
         if( ! inDirective ) if( ( c ) >= ' ' ) {
             if( !lastCharisSpace &&!( startOfLine &&(c ==' ')))
                 Viewpos++ ;
             if( c == ' ')
                lastCharisSpace = true;
             else 
                lastCharisSpace = false;
             
             startOfLine = false;
         }
       
         
         if( c == '>' ) {
            
            inDirective = false;
            S = null;
            
         }
         
         c = doc.charAt( pos++ );
      }

      if( pos >= doc.length() ) 
         return null;


      S = "";
      S += c;
      //System.out.print("       Viewpos:"+Viewpos);
      Viewpos++;
     
      c = doc.charAt( pos++ );
      while( ( ( ( c >= 'a' ) && ( c <= 'z' ) )
               || ( ( c >= 'A' ) && ( c <= 'Z' ) ) || ( c == '_' ) || ( ( c >= '0' ) && ( c <= '9' ) ) )
               && ( pos < doc.length() ) ) {
         
         S += c;
         Viewpos++ ;
         c = doc.charAt( pos++ );

      }

      //System.out.println("-"+Viewpos+"="+S);
     
      if( c == '<' ) {
         
         inDirective = true;
         pos-- ; // reread it for next word
      } else if( c==' ')
         pos--;  //reread space in case two spaces
      else
         Viewpos++ ;

      S = S.toLowerCase();
      if( S.length() > 8 ) 
         S = S.substring( 0 , 8 );

      if( OmitWords.indexOf( ";" + S + ";" ) >= 0 ) 
         return getNextWord();

      return S;

   }

   
   
   int          filePos   = 0;

   StringBuffer fout2Buff = new StringBuffer( 1200 );


   public void append( String word , int pos , String tag , String Command ,
            String filename ) {

      int k = binarySearch( word );
      
      if( k < 0 ) {

         if( NWords + 1 > ranks.length ) {// add 100 more words
            ranks = grow( ranks );
            buffptrs = grow( buffptrs );
            Words = grow( Words );
         }
         Words[ NWords ] = word;
         buffptrs[ NWords ] = - 1;
         k = insertInRank( ranks , word , NWords );
         NWords++ ;
      }

      int Fpos1 = buffptrs[ ranks[ k ] ];
      buffptrs[ ranks[ k ] ] = filePos;
      
      
      // ----- Now set up fout2
      fout2Buff.setLength( 0 );
      fout2Buff.append( "" + Fpos1 + "\n" + pos + "\n" + tag + "\n" + Command
               + "\n" + filename + "\n" );
      filePos += fout2Buff.length();
      
      try {
         fout2.write( fout2Buff.toString().getBytes() );
      } catch( Exception s ) {
         
         s.printStackTrace();
         System.out.println( fout2Buff );
         System.exit( 0 );
      }

   }


   
   /**
    * Inserts the word into the search structure
    * 
    * @param ranks  An array that points to the ith word alphabetically
    * @param word  The word to be inserted
    * @param wordIndx  The number of words so far
    * @return   the position in the rank array where this word was inserted
    */
   public int insertInRank( int[] ranks , String word , int wordIndx ) {
      
      int k = NWords - 1;
      while( ( k >= 0 ) && ( Words[ ranks[ k ] ].compareTo( word ) > 0 ) ) {
         
         ranks[ k + 1 ] = ranks[ k ];
         k-- ;
      }

      ranks[ k + 1 ] = wordIndx;
      return k + 1;
      
   }


   
   /**
    * Increases the size of an int array by 100
    * @param intArray  the int array whose size is to be increased
    * @return  a pointer to the new array.  This new array must replace all 
    *             references to the old array
    */
   public int[] grow( int[] intArray ) {
      
      int L = intArray.length;
      int[] Res = new int[ L + 100 ];
      System.arraycopy( intArray , 0 , Res , 0 , L );
      return Res;
      
   }


   
   
   /**
    * Increases the size of an String array by 100
    * @param intArray  the String array whose size is to be increased
    * @return  a pointer to the new array.  This new array must replace all 
    *             references to the old array
    */
   public String[] grow( String[] intArray ) {
      
      int L = intArray.length;
      String[] Res = new String[ L + 100 ];
      System.arraycopy( intArray , 0 , Res , 0 , L );
      return Res;
      
   }

   

   /**
    * Test program for some of these methods
    * 
    * @param args  none are needed
    */
   public static void main( String[] args ) {
      try {
         new MakeSearchData();
      } catch( Exception s ) {
         s.printStackTrace();
      }

   }

   
   
   /**
    * Finds the position in the ranks array to insert the word "word". It
    * is appended to the end of the Words[] list
    * @param word  the word to be inserted
    * @return  the position where the word is to be inserted
    */
   public int binarySearch( String word ) {
      
      int first , 
          last , 
          mid;
      
      first = 0;
      last = NWords - 1;
      if( NWords <= 0 ) return - 1;
      while( first <= last ) {
         
         mid = ( first + last ) / 2;
         int comp = Words[ ranks[ mid ] ].compareTo( word );
         
         if( comp < 0 )
            first = mid + 1;
         else if( comp > 0 )
            last = mid - 1;
         else
            return mid;

      }

      return - 1;
   }

}
