/*
 * File:  FortranTokenManager.java 
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
 * Revision 1.3  2004/06/18 18:31:22  rmikk
 * Now produces tokens for correctly for .  GT, etc.
 *
 * Revision 1.2  2004/06/04 22:01:34  rmikk
 * Added documentation  and GPL
 *
 */

package Command.JavaCC.Fortran;
import java.io.*;

/**
 * This class implements the token manager tasks for Fortran files. It 
 * reads continuation lines, handles columns 1 through 6, and returns special
 * tokens for most "reserved" words in FORTRAN 
 * @author MikkelsonR
 *
 *
 */
public class FortranTokenManager implements TokenManager,
                                          FcvrtConstants{
                                            
  private FileInputStream fin;
  boolean quoteMode = false;
  boolean labelMode = false;
  boolean checkingNextline=false;//i.e. eoln appeared
  boolean nextCharEOF=false;
  public static boolean debug=false;
  int lineNum=1;  char c=0; //first lookahead character
  int column=0; //when parsing first couple columns
  
  /**
   *  Constructor
   * @param filename  The source Fortran filename
   */
  public FortranTokenManager( String filename){
   try{
    fin = new FileInputStream( filename);
   }catch(Exception s){fin=null;}
  }
  

  /** This gets the next token from the input stream.
   *  A token of kind 0 (<EOF>) should be returned on EOF.
   */
  public Token getNextToken(){
    
    if( fin == null)
      return  TToken( EOF,null);
   
    Token t= getNextWord();
    if( debug)
      System.out.println("Next token="+t.kind+","+t.image);
      
    return t;


  }
  
 Token TToken( int kind, String image){
   
    Token Res = new Token();
    Res.kind = kind;
    Res.image = image;
    Res.next = null;
    Res.beginLine= StartLineNum;
    Res.beginColumn=startColNum ;
    Res.endLine =lineNum ;
    Res.endColumn = column;
    return Res;
   }
  
 char SavChar =0;
 int StartLineNum=1,
     SavLineNum=1, 
     startColNum=0, 
     savColNum=0;
     
 private Token getNextWord(){
    String SS="";
    char c=0;
    StartLineNum=lineNum;
    startColNum=column;
    if( nextCharEOF) return TToken(EOF,null);
    if( SavChar<10)
        c=getNextChar();
    else{
        c=SavChar;
        SavChar = 0;
        StartLineNum=SavLineNum;
        startColNum=savColNum;
    }
    if( c==0)
        return TToken(EOF,null);
    else if( c==13)
      return TToken( EOLN,null);
    else if( c=='.'){
       SavLineNum=lineNum;
       savColNum= column;
       for(SavChar=getNextChar(); SavChar == ' '; SavChar=getNextChar())
         {}
       if( (SavChar<=32)||("+-*/.),".indexOf(SavChar)>=0)||
         Character.isDigit(SavChar))
           return TToken(DOT,".");
       else return TToken(LOGDOT,".");
    }
   //int colStart = column;
    if( c=='+')
       return TToken( PLUS,""+c);
    else if( c=='-')
       return TToken( MINUS,""+c);
    else if( c=='/')
       return TToken( DIVISION,""+c);
    else if( c=='+')
       return TToken( PLUS,""+c);
    else if( c=='=')
       return TToken( EQUALS,""+c);
    else if( c=='*'){
      SavLineNum=lineNum;
      savColNum= column;
      SavChar = getNextChar();
      if( SavChar =='*'){
         SavChar=0;
         return TToken(POWER,"**");
       }else return TToken(PROD,"*");

    }
    if( c=='(') return TToken(LPAREN,""+c);
    else if( c==')') return TToken(RPAREN,""+c);
    else if( c==',') return TToken(COMMA,""+c);
    if( (column==1) &&("Cc".indexOf(c)>=0)){// comment line
       for( c=getNextChar(); (c>=32);c=getNextChar())
          SS+=c;
 
      SavLineNum=lineNum;
      savColNum= column -1 ;
       SavChar = c;
       return TToken(COMMENT, SS);
    }else if( Character.isJavaIdentifierStart(c)){
      SS+=c;
      int startcolumn=column;  
      for( c=getNextChar(); (Character.isJavaIdentifierPart(c)||(c=='_'))&&(c >0);
             c=getNextChar()){
         SS+=c;
      }
     if(c==32) 
       SavChar=0;
     else if( c==13) 
       SavChar = 13;
     else if( c==0) 
        nextCharEOF=true;
     else 
       SavChar =c;
     SavLineNum=lineNum;
     savColNum=column-1;
     if( startcolumn >6)
        return IdentToken(SS);
     else 
       return TToken(ERROR, "Label must be integer");
    }else if( Character.isDigit(c) ||(c=='.')){
      SS+=c;
      //boolean flt = false;
      int startcolumn=column;
      for( c=getNextChar(); Character.isDigit(c);
             c=getNextChar()){
         SS+=c;
      }
     if(c==32) SavChar=0;
     else if( c==13) SavChar = 13;
     else if( c==0) nextCharEOF=true;
     else SavChar = c;
     
     SavLineNum=lineNum;
     savColNum=column-1;
     if( startcolumn < 6)
         if( startcolumn+SS.length() <6)return TToken( LABEL,SS);
         else return TToken( ERROR,"Label is too long");
    
     else
         return TToken (INTNUM,SS);
    }
   else if( c=='\''){
      for( c=getNextChar(); (c>=32)&&(c!='\'');c=getNextChar())
           SS+=c;
      if( c!='\'')return TToken(ERROR,"Unclosed quotes");
      else return TToken(STRING, SS);

   
   }else if( c==32){
       return getNextWord();
   }else 
     return TToken (ERROR,"Improper character"+(int)c+"::"+c);
 }//getNextWord
 
 
 private Token IdentToken(String SS){

    SS = SS.toUpperCase();
    
    if( SS.equals("IF")) return TToken(IF, SS);
    if( SS.equals("THEN"))return TToken(THEN,SS);
    if( SS.equals("ELSE"))return TToken(ELSE,SS);
    if( SS.equals("DO"))return TToken(DO,SS);
    if( SS.equals("WRITE"))return TToken(WRITE,SS);
    if( SS.equals("READ"))return TToken(READ,SS);
    if( SS.equals("REAL"))return TToken(REAL,SS);
    if( SS.equals("INTEGER"))return TToken(INTEGER,SS);
    if( SS.equals("CHARACTER"))return TToken(CHARACTER,SS);
    if( SS.equals("LOGICAL"))return TToken(LOGICAL,SS);
    if( SS.equals("EXTERNAL"))return TToken(EXTERNAL,SS);
    if( SS.equals("SUBROUTINE"))return TToken(SUBROUTINE,SS);
    if( SS.equals("FUNCTION"))return TToken(FUNCTION,SS);
    if( SS.equals("CONTINUE"))return TToken(CONTINUE,SS);
    if( SS.equals("AND"))return TToken(AND,SS);
    if( SS.equals("OR"))return TToken(OR,SS);
    if( SS.equals("NOT"))return TToken(NOT,SS);
    if( SS.equals("LE"))return TToken(LE,SS);
    if( SS.equals("LT"))return TToken(LT,SS);
    if( SS.equals("GE"))return TToken(GE,SS);
    if( SS.equals("GT"))return TToken(GT,SS);
    if( SS.equals("EQ"))return TToken(EQ,SS);
    if( SS.equals("NE"))return TToken(NE,SS);

    if( SS.equals("E"))return TToken(EXP,SS);
    if( SS.equals("ENDIF"))return TToken(ENDIF,SS);
    if( SS.equals("END")) return TToken(END, SS);
    if( SS.equals("WHILE")) return TToken( WHILE,SS);
    if( SS.equals("CALL")) return TToken( CALL,SS);
    if( SS.equals("TRUE")) return TToken( TRUE,SS);
    if( SS.equals("FALSE")) return TToken( FALSE,SS);
    if( SS.equals("DIMENSION")) return TToken( DIMENSION, SS);
    
    if( SS.equals("RETURN")) return TToken( RETURN, SS);
    return TToken( IDENTIFIER,SS);
   
 }
 
 
 String S="";
 char prevChar=0;
 /**
   *  Get's the next character in a Fortran program
   *  It continue's the line if it is a continuation
   *  quotes and comment line retain spaces. all others only one space is returned
   *  The quotes and leading c are NOT returned.
   *  NewLine returns new line character if not a continuation line
   */
 private char getNextChar(){
    int cc=0,i;
    try{
    if( S.length()>0){
      cc=S.charAt(0); 
      
      S=S.substring(1);
    }else
       cc=fin.read();
    column++;
    if( (column==1) &&("Cc".indexOf((char)cc)>=0)){
       quoteMode=true;

    }
    else if( column==1)
       quoteMode = false;
    if( cc<0) return 0;
    
    if( cc<32){
       quoteMode=false;
       lineNum++;
       int c_newLine =cc;
       while((cc<32)&&(cc>=0)){ 
           cc=fin.read();
           if(c_newLine == cc)
             lineNum++;
       }
       if( cc<0) 
           return 0;
       column=1;
       S+=(char)cc;
       for(  i=0;(i<5)&&(cc==32);i++)
          {cc=fin.read();
           column++;
           S+=(char)cc;
          }
       
       if((i>4)&&(cc!=32)){//continuation 
          S="";
          column=7;
          return getNextChar();//incase cont and return
       }else {
         column=0;
         prevChar=0;
         return 13;
       }
           
     }  
    else if( (!quoteMode)&&(cc==32)&&((prevChar==' ')||(prevChar==0)))
      return getNextChar();
    else if( cc==32){
       prevChar=' ';
       return ' ';
       
 
   }else if( ((char)cc)=='\''){
     quoteMode=!quoteMode;
     prevChar='\'';
     return (char)cc;
   }else{
      prevChar =(char)cc;
       return(char)cc;
    }
   }catch(Exception ss){return 0;}
 }//getNextChar
 
  /**
   *  Test program for this module
   * @param args[0]  A filename for source Fortran code
   */
  public static void main( String args[]){
   FortranTokenManager fm = new FortranTokenManager( args[0]);
   //int i=0;
  // for( Token c=fm.getNextWord(); c.kind!=EOF; c=fm.getNextWord()){
  //    System.out.println(i+"  "+c.kind+"::"+c.image+":"
  //       +c.beginLine+","+c.beginColumn+","+c.endLine+","+c.endColumn);
  //    i++;
  // }
  }
}
