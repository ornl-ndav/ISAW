/* Generated By:JavaCC: Do not edit this line. FortranParser.java */
package Command.JavaCC.Fortran;

import java.io.StringReader;

import java.util.*;

public class FortranParser implements FortranParserConstants {
  //don't want to continually recreate this thing
  private static FortranParser myParser;
  private static boolean expandVectorIntoElements = true;
  private static boolean standalone = false;

  /**
   * Method used to run the FortranParser for testing purposes.
   *
   * @param args Unused.
   *
   * @throws ParseException If anything goes wrong during parsing
   */
  public static void main( String args[] ) throws ParseException {
    standalone = true;
    myParser = new FortranParser( System.in );
    myParser.parseCode(  );
  }

  /**
   *  Used to call the parser from an outside class.  Returns a String in Java
   *  for each line of Fortran code.
   *
   * @param text The text to parse.
   *
   * @return The result of the parsing.
   *
   * @throws ParseException If anything goes wrong during parsing
   */
  public static String parseText( String text ) throws ParseException {
    standalone = false;
    //need a semicolon on the end
    if( text.indexOf( ";" ) < 0 ) {
      text = text.trim(  ) + ";";
    }
    if( myParser == null ) {
      myParser = new FortranParser( new StringReader( text )  );
    } else {
      myParser.ReInit( new StringReader( text )  );
    }
    String line = myParser.parseCode(  );

    if( line == null || line.length(  ) <= 0 ) {
      return "";
    }  else  {
      return line;
    }
  }

  /**
   * Method to convert fromFortran style array and variable 
   * declaration to Java style array and variable declaration.
   * If no array declarations exist, this essentially just converts from
   * Fortran type declaration to Java type declaration.
   * 
   * @param s The String to convert Fortran arrays from.
   * @param type The type (e.g. int) of the array.
   * 
   * @return The String representing the passed in String converted
   *                 to Java style arrays.
   */
  private static String convertToJavaStyle(
    String s, String type ) {
    StringBuffer buffer = new StringBuffer( type );
    buffer.append( " " );

    //the parentheses should NOT be right at the beginning of the declaration
    if( s.indexOf( "(" ) > 0 ) {

      String tokenList[] = s.split( "\\s+|\\s*,\\s*" );
      //now go through the tokens, determining if an element is an array
      String[] temp;
      for( int i = 0; i < tokenList.length; i++ ) {
        if( tokenList[i].indexOf( "(" ) >= 0 ) {
          //change to Java style array delimiters
          tokenList[i] = tokenList[i].replace( '(', '[' );
          tokenList[i] = tokenList[i].replace( ')', ']' );
          //this will split the String just after the variable name.  We
          //will have to put the '[' back on however
          temp = tokenList[i].split( "\\[" );
          tokenList[i] = temp[0] + " = new " + type + "[" + temp[1];
        }
        buffer.append( tokenList[i] );

        if( i < tokenList.length - 1 ) {
          buffer.append( "," );
        }
      }
    } else {
      //just return what we had after initial replacements
      buffer.append( s );
    }

    return buffer.toString(  );
  }

/**
 * This method goes through a line of Fortran code, converting it to Java.
 * It also has the ability to test the parser by checking the standalone
 * quality and bringing up a command line prompt if it should run 
 * standalone.
 * 
 * @return The converted Fortran-to-Java code.
 */
  static final public String parseCode() throws ParseException {
  String codeLine = "";
    if( standalone ) {
      System.out.println( "Please type in a line of Fortran code " +
        "or ^D to quit:\n" );
    }
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case DIGIT:
      case STRING:
      case FLOATING_POINT:
      case FORTRAN_ABS:
      case FORTRAN_SQRT:
      case FORTRAN_FLOAT_FUN:
      case FORTRAN_TRUNC_FUN:
      case FORTRAN_MOD_FUN:
      case FORTRAN_INT:
      case FORTRAN_REAL:
      case FORTRAN_DOUBLE:
      case FORTRAN_LOGICAL:
      case FORTRAN_CHAR_1:
      case FORTRAN_CHAR_2:
      case 36:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      codeLine = convertFortranToJava();
      jj_consume_token(36);
      //a newline ends the expansion, and the parser expects one, so tack it on
      //print the parsed expression
      if( standalone ) {
        System.out.println( codeLine );
        System.out.println(  );
        System.out.println( "Please type in another line of Fortran code or ^D to quit:" );
        System.out.println(  );
      }
    }
    {if (true) return codeLine;}
    jj_consume_token(0);
    throw new Error("Missing return statement in function");
  }

/**
 * This method actually converts the line of Fortran to Java.
 * 
 * @return A line of code converted from Fortran to Java. 
 */
  static final public String convertFortranToJava() throws ParseException {
  StringBuffer fCode = new StringBuffer(  );
  String fToken;
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case DIGIT:
      case STRING:
      case FLOATING_POINT:
      case FORTRAN_ABS:
      case FORTRAN_SQRT:
      case FORTRAN_FLOAT_FUN:
      case FORTRAN_TRUNC_FUN:
      case FORTRAN_MOD_FUN:
      case FORTRAN_INT:
      case FORTRAN_REAL:
      case FORTRAN_DOUBLE:
      case FORTRAN_LOGICAL:
      case FORTRAN_CHAR_1:
      case FORTRAN_CHAR_2:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      /*( ( <WHITESPACE> )* )*/ fToken = convertFortranTokens();
    fCode.append( fToken );
    fCode.append( " " );
    }
    //convert single spaces to one space, then trim it and append the
    //Java line closer semicolon.  This may fail for Strings-we will see...
    {if (true) return fCode.toString(  ).replaceAll( "\\s+", " " ).trim(  ) + ";";}
    throw new Error("Missing return statement in function");
  }

/**
 * This method goes through the tokens defined earlier in this file and 
 * changes each to a Java token.
 * 
 * @return The converted token.
 */
  static final public String convertFortranTokens() throws ParseException {
  Token t;
  String s;
  StringBuffer buffer;
  String[] tokenList;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case FORTRAN_ABS:
      t = jj_consume_token(FORTRAN_ABS);
    if( t.image.indexOf( "iabs" ) >= 0 ) {
      {if (true) return t.image.replaceAll( "iabs", "Math.abs" );}
    } else {
      {if (true) return t.image.replaceAll( "abs", "Math.abs" );}
    }
      break;
    case FORTRAN_SQRT:
      t = jj_consume_token(FORTRAN_SQRT);
    {if (true) return t.image.replaceAll( "sqrt", "Math.sqrt" );}
      break;
    case FORTRAN_FLOAT_FUN:
      t = jj_consume_token(FORTRAN_FLOAT_FUN);
    s = t.image;
    if( s.indexOf( "float" ) >= 0 ) {
      s = s.replaceAll( "float", "" );
    } else {
      s = s.replaceAll( "real", "" );
    }

    s = "Float.parseFloat" + s;

    {if (true) return s;}
      break;
    case FORTRAN_TRUNC_FUN:
      t = jj_consume_token(FORTRAN_TRUNC_FUN);
    s = t.image.replaceAll( "int", "" );

    s = "new Float" + s + ".intValue()";
      break;
    case FORTRAN_MOD_FUN:
      t = jj_consume_token(FORTRAN_MOD_FUN);
    s = t.image;
    s = s.replaceAll( "mod", "" ).replaceAll( "\\)", "" ).replaceAll( "\\(", "" );
    s = s.replaceAll( ",", " % " );
    {if (true) return s;}
      break;
    case FORTRAN_DOUBLE:
      //matched a double-precision floating point
        t = jj_consume_token(FORTRAN_DOUBLE);
    t.image = t.image.replaceAll( "double precision", "" );
    {if (true) return convertToJavaStyle( t.image, "double" );}
      break;
    case FORTRAN_INT:
      //matched an integer
        t = jj_consume_token(FORTRAN_INT);
    t.image = t.image.replaceAll( "integer", "" );
    {if (true) return convertToJavaStyle( t.image, "int" );}
      break;
    case FORTRAN_REAL:
      //matched a floating point
        t = jj_consume_token(FORTRAN_REAL);
    t.image = t.image.replaceAll( "real", "" );
    {if (true) return convertToJavaStyle( t.image, "float" );}
      break;
    case FORTRAN_LOGICAL:
      //matched a boolean
        t = jj_consume_token(FORTRAN_LOGICAL);
    t.image = t.image.replaceAll( "logical", "" );
    {if (true) return convertToJavaStyle( t.image, "boolean" );}
      break;
    case FORTRAN_CHAR_1:
      //matched a character or character array where each variable is
        //explicitly given a length
        t = jj_consume_token(FORTRAN_CHAR_1);
    s = t.image;
    buffer = new StringBuffer( "char " );

    //dump the "character" stuff
    s = ( s.replaceAll( "character", "" ) ).trim(  );
    //now we will create character arrays for each of the Fortran
    //character declarations.  The assumption is that we will have a 
    //comma delimited list of name, number pairs at this point
    tokenList = s.split( "\\s+|\\s*,\\s*" );

    for( int i = 0; i < tokenList.length; i++ ) {
      if( ( i % 2 )  == 0 ) {
        //we found a variable name, so append it and the array declaration
        buffer.append( tokenList[i] );
        buffer.append( " = new char[" );
      } else {
        //we found the array dimension, so append that
        buffer.append( tokenList[i].replaceAll( "\\*", "" ) );
        buffer.append( "]" );

        //if we haven't reached the end of our token list, append a 
        //comma
        if( i < tokenList.length - 2 ) {
                buffer.append( "," );
        }
      }
    }

    {if (true) return buffer.toString();}
      break;
    case FORTRAN_CHAR_2:
      //matched a character or character array where each variable is
        //given a length specified in the character *n declaration
        t = jj_consume_token(FORTRAN_CHAR_2);
    s = t.image;
    buffer = new StringBuffer( "char " );

    //dump the "character" stuff
    s = ( s.replaceAll( "character", "" ) ).trim(  );
    //break the declarations up by comma and space.  The first token is
    //the number to use for length

    tokenList = s.split( "\\s+|\\s*,\\s*" );
    String length = tokenList[0].replaceAll( "\\*", "" );

    for( int i = 1; i < tokenList.length; i++ ) {
      buffer.append( tokenList[i] );
      buffer.append( " = new char[" );
      //first element is array length
      buffer.append( length );
      buffer.append( "]" );

      if( i < tokenList.length - 1 ) {
        buffer.append( "," );
      }
    }

    {if (true) return buffer.toString();}
      break;
    case DIGIT:
      //matched a digit
        t = jj_consume_token(DIGIT);
    {if (true) return t.image;}
      break;
    case FLOATING_POINT:
      //matched a float
        t = jj_consume_token(FLOATING_POINT);
    {if (true) return t.image;}
      break;
    case STRING:
      //matched a String
        t = jj_consume_token(STRING);
    //replace Fortran's double single quotes with a backslash quote
    {if (true) return t.image.replaceAll( "''", "\\\\\"");}
      break;
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static private boolean jj_initialized_once = false;
  static public FortranParserTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  static public Token token, jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[3];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0xfe018040,0xfe018040,0xfe018040,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x1f,0xf,0xf,};
   }

  public FortranParser(java.io.InputStream stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new FortranParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  public FortranParser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new FortranParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  public FortranParser(FortranParserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  public void ReInit(FortranParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }

  static final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector jj_expentries = new java.util.Vector();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  static public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[37];
    for (int i = 0; i < 37; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 3; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 37; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

}
