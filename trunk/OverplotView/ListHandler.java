package Command;
import java.lang.*;
import java.io.*;

public class ListHandler    // list of strings, numbers etc.
   {public String ItString;
    public int    position;
    
   public ListHandler( String Lst )
      { ItString = Lst.trim();
        if( ! check( Lst ))
           //throw java.lang.InstantiationException;
           
        position = -1;
      }

    public void start()
      {
        position=-1;
      }
    private int findd(String S, int start, String SrchChars)
      {int i, j, j1;
       if(S == null)
          return -1;
       if(SrchChars == null)  return -1;
       if( ( start < 0 ) || ( start >= S.length() ) )
          return S.length();
       
          
       j = S.length();
       j1 = S.length(); 
       for ( i = 0 ; i < SrchChars.length() ; i++ )
         { j = S.indexOf( SrchChars.charAt( i ),start );
          // System.out.println("            "+i+","+j+","+j1);
           if( j >= 0)
             if( j < j1 )
               {j1 = j;
               }
         }
        return j1;

      }
private int finddQuote(String S, int start, String SrchChars,String brcpairs)
      {int i, j, j1;
       int brclevel;
       boolean quote;

       if(S == null)
          return -1;
       if(SrchChars == null)  return -1;
       if( ( start < 0 ) || ( start >= S.length() ) )
          return S.length();
       brclevel=0;
       quote=false;          
      
       for ( i = start ; i < S.length() ; i++ )
         { char c = S.charAt(i);
            
            if( c == '\"' )
             {if( ( !quote ) && ( brclevel == 0 ) && (SrchChars.indexOf(c) >= 0 ) )
                 return i;
              quote = !quote;
              if( i >= 1)
                if( S.charAt( i - 1 )  =='\"' )
                   {quote = !quote;}
             }
            else if( quote )
               { }
           
            else if(SrchChars.indexOf(c)>=0)
               {if( brclevel == 0)
                   return i;
               }
            if( ( !quote ) && ( brcpairs != null ) )
              { j = brcpairs.indexOf(c);
                if(j<0) {}
                else if( j == 2* (int)(j/2))
                    brclevel++;
                else
                    brclevel--;
              }
            if(brclevel < 0) return i;

             
               
         }
        return S.length();

      }

    public String next()
      {position ++;
       if( position < 0)
          return null;     //throw java.lang.InstantiationException;
       int  i,
            j,
            count,
           lastcomma,
           parenlevel,
           bracelevel;
       boolean quote;
       i = 0 ;
       count = 0;
       parenlevel = 0;
       bracelevel = 1;
       quote = false;
       lastcomma = 1;
       //System.out.println( ItString +","+position+","+ItString.length() );
      

       for( i = 1 ; i < ItString.length() ; )
         {     j = findd( ItString , i , "[](),\"" );
               
	 //System.out.print("::"+i+","+j+","+bracelevel);

               if( j < 0 )
                 { //System.out.println("A");
                   return null;  
 
                  }
               else if( j >= ItString.length())
		   { //System.out.println("B");
                   return null;
                 }
               else if( ItString.charAt(j) == '\"')
		   { //System.out.println("C"+quote);

                    quote = !quote;
                    if( ItString.charAt( j - 1 ) == '\\')
                       if( !quote ) 
                          { quote = !quote;}                   
                     
                 }
               else if(quote)
		   {//System.out.println("D");

                 }
               else if( ItString.charAt(j) == '(' )
                 { parenlevel++;
		 //System.out.println("F");

                 }
               else if( ItString.charAt( j ) == ')')
                 { parenlevel--;
		 //System.out.println("G");

                 }
               else if( ItString.charAt( j ) == '[')
                 {bracelevel++;
		 //System.out.println("H");

                 }
               else if( ItString.charAt( j ) == ']')
                 {if( bracelevel == 1 )
                    if( parenlevel == 0 )
                       count ++;
                  if(count == position + 1 )
                     return ItString.substring( lastcomma , j ) ;

                  bracelevel--;
                  //System.out.println("I");

                 }
               else if( ItString.charAt( j ) == ',')
		   { //System.out.println("J");

                   if( parenlevel == 0 )
                      if( bracelevel == 1 )
                         count++;
                   if(count == position + 1 )
                     return ItString.substring( lastcomma , j ) ;
                   else if((bracelevel==1) && (parenlevel == 0))
                     lastcomma = j+1;
                 }
               //System.out.println("ZZ"+lastcomma+","+i+","+j);
               i=j+1;

               if((bracelevel<=0) ||(parenlevel<0))
		   {//System.out.println("E"+bracelevel+","+parenlevel);
                   return null;
                 }

          
         }
         return null;

      }
     public boolean more()
      { if(next() == null)
          return false;
        return true;
       }
       
   

    public boolean check( String S )
      {return true;
      }
    public String getString()
      { return ItString;
      }
      
    public boolean isRangeItem( String Result)
      {return false;
      }
    //Used to replace Strings in a this list by the 
    // actual value.  Needed for Range entries 
    public String nextVariableNameString()
      {return null;
      }

    public static void main( String args[] )
      {ListHandler  It;
           char  c;
           int   i;
           byte  B[];
       
       B = new byte[50];   
       It = null;
       c = 0;
       while( c != 'x' )
         {
           System.out.println("Enter option desired" );
           System.out.println("  a) Enter a String");
           System.out.println("  b) Set String to start");
           System.out.println("  c) Get next entry");
           System.out.println("  d) Test findd ");
           System.out.println("  e) xxxxx");
           System.out.println("  x) Exit");
           
           try
             {c = 0;
              while( c < ' ' )
                 c=(char)System.in.read() ;
             }
           catch(java.io.IOException s){c = 0;}

           if( c == 'a' )
             {try
               {i = System.in.read(B);
               }
              catch ( java.io.IOException s )
                { i = 0; }
              if( i > 0 )
                It = new ListHandler ( new String( B , 0 , i ) );
              else
                {It = null;
                 System.out.println( " Could not create ListHandler " ) ;
                }
              
             }
           else if( c =='b' )
             { It.start();
             }
           else if( c =='c' )
             {System.out.println( It.next() );
             }
           else if( c =='d' )
             { System.out.println(It.ItString + ","+It.position);
               System.out.println( "findd="+ It.finddQuote(It.getString(),0,"+-/*]","{}[]()"));
             }
           else if( c =='e' )
             {
             }

           
         }

       
      }
   }
