package Command;

import Command.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import DataSetTools.components.ParametersGUI.*;

public class Script_Class_List_Handler  implements OperatorHandler
  {
     private static final Vector SortOnCommand = new Vector();  //Contains ordering for Command Names
     private static final Vector SortOnFileName = new Vector(); //Contains ordering for file names
     private static  final Vector opList  = new Vector();
     private String errorMessage= "";
     private static int Command_Compare =257;
     private static int File_Compare = 322;
     private static boolean first = true;
     public Script_Class_List_Handler()
       {inittt();
        }
     private void inittt()
      {  if( !first)
           return;
        
        first = false;     
        String ScrPaths = System.getProperty( "Script_Path" );
        if( ScrPaths == null )
          return ;
         
        ScrPaths.trim();
        
        ScrPaths=ScrPaths.replace(java.io.File.pathSeparatorChar,';'); 
        if( ScrPaths.lastIndexOf(';') != ';')
             ScrPaths = ScrPaths+";";
        

        for( String Path = getNextPath( ScrPaths , null );
                   Path != null;   
                   Path = getNextPath( ScrPaths , Path ) )
           { 
             File Dir = new File( Path) ;
              

             if( Dir.isDirectory() )
                 ProcessDirectory( Dir ,opList);

             
           }
        for( int i = 0 ; i < GenericOperatorList.getNum_operators(); i++)
          {Operator op = GenericOperatorList.getOperator( i );
            if( op instanceof GenericOperator)
                 add( op );
          }
        return;
        }//Constructor

 /**
* Extracts the Next Path in a semicolon delimited list of paths
* This method can be used to extract next Entry from any delimited list.<BR>
* Repeats will NOT be extracted twice. Empty Paths will be ignored
*@param  PathList    a semicolon delimited list of String entities
*@param  PrevPath    Must be null or one entity in the list (w.o. semicolons )
*@return  The next entity in the PathList or null if therre is none.
*/
 public  String getNextPath( String PathList , String PrevPath )
    {  if(PathList == null )
         return null;
       
       int i ;
       int j;
       if( PrevPath != null )
          if( PrevPath.length() <= 0)
            PrevPath = null;

       PathList.trim();
       if( PathList.lastIndexOf( ';') != PathList.length()-1)
         PathList = PathList+ ";";
       i = 0;
       if( PrevPath != null )
        if( PrevPath.length() > 0 )
          if( PathList.indexOf( PrevPath ) >= 0 )
          {  i = PathList.indexOf( PrevPath );
            
             if( i >= 0 )
             while( PathList.indexOf( PrevPath+";", i+PrevPath.length()) >= 0 )
               {  
                 i = PathList.indexOf( PrevPath, i + PrevPath.length() );
                 
               } 
             
             if( i < 0 ) 
               return null;
             i = i + PrevPath.length() +1;
             if( i >= PathList.length())
                return null;
          }
      
       
       j = PathList.indexOf(';', i );
       
       if( j < 0 )
         return null;
       String Res = PathList.substring( i , j );
       while( PathList.indexOf( Res, j ) > 0 )
         { i = j+1 ;
           j = PathList.indexOf( ';' , i  );
           if( j < 0 )
             return null;
           Res = PathList.substring( i , j );
         }
       return Res;
    }

private  boolean isFileExtension( File F)
  {String nm = F.getName();
   if(nm == null )
       return false;
   int i = nm.lastIndexOf( '.');
   if( i < 0 )
      return false;
   String ext = nm.substring( i + 1).toUpperCase();
   if( ext.equals("ISS") )
       return true;
    if( ext.equals("CLASS"))
       return true;
   return false;

  }
private  void ProcessDirectory( File Dir, Vector opList )
   { 
     
     File F[];
     F = new File[0];
     F = Dir.listFiles();
     for( int i = 0; i < F.length; i++)
       {
        if( F[i].isDirectory())
          ProcessDirectory( F[i], opList );
        else if( F[i].isFile() )
           if( isFileExtension(F[i]))
              ProcessFile( F[i], opList );
         
       }
    
   }
private  void ProcessFile( File file , Vector opList)
  {  
 add( file.toString() , opList);
   }
public  void show( int mode  )
  {if( SortOnFileName.size() <= 0)
    return;
   int i;
  
   for( i=0; i< SortOnFileName.size(); i++ )
     { int j;
        if( mode == Command_Compare)
            j= ((Integer)SortOnCommand.elementAt(i)).intValue();
        else
            j=((Integer)SortOnFileName.elementAt(i)).intValue();

       Operator X =(Operator) opList.elementAt(j);
       //System.out.print(i+":Command="+X.getCommand());
       if( X instanceof ScriptOperator)
         { ScriptOperator Y =(ScriptOperator) X;
           //System.out.println("  File="+Y.getFileName());
         }
        else 
          System.out.println("");
    }
  
   }
private  void add( String filename , Vector opList)
  {int i;
   
   if(filename == null )
      return;
   i = filename.lastIndexOf('.');
   if( i< 0 )
     return;
    String Extension = filename.substring( i + 1 );
    if( Extension.equalsIgnoreCase("iss"))
      {ScriptOperator X = new ScriptOperator( filename );
       add( X );
      }
    else
      { Operator X = getClassInst( filename );
        if( X != null )
           add( X );
       }
   }
public  Operator getClassInst( String filename )
   {String path;
   if( filename == null )
       return null;
    filename = filename.replace('\\' , '/' );
    String pathlist = System.getProperty("java.class.path");
    pathlist=pathlist.replace('\\','/');
    pathlist=pathlist.replace(java.io.File.pathSeparatorChar,';');
    int i = filename.lastIndexOf('/' );
    
    if( i < 0 )
       return null;
    String CPath = filename.substring( 0 , i ).trim();
    String classname = filename.substring( i + 1 , filename.length()-6); 
    String CPathFix=CPath;
    
    if( CPath == null )
      return null;
    if( CPath.length() <= 0 )
        return null;
    for( path = getNextPath( pathlist, null ) ; path != null;
         path = getNextPath( pathlist, path ))
      {String Path1=path.trim();
       if( Path1 != null )
         if( Path1.length() > 0 )
           {if( Path1.lastIndexOf('/') >= 0)
              Path1=Path1.substring( 0, Path1.lastIndexOf('/' )).trim();
            if( Path1 !=null )
             if( Path1.length() > 0 )
               {
               }
            }
          

          if( CPath.indexOf(Path1 ) == 0 )
            {CPathFix = CPath.substring( Path1.length()+1);
             CPathFix =CPathFix.replace('/','.');
             CPath=CPathFix+"."+classname;
             
             try{
                Class C = Class.forName( CPath );
                Object XX = C.newInstance();
                if( XX instanceof GenericOperator)
                  { return (Operator)XX;
                  }            
                
                }
             catch(Exception s)
                {
                }

            }
        
       }//Check each classpath for match
     return null;
   }
private  void add( Operator op)
  {
   opList.addElement( op );
    
    insert( opList , SortOnCommand );
    insert(opList , SortOnFileName );

  }
/**
* insert before
*/
private  void insert( int n_toInsert, int pos_insert , Vector rankList  )
  {

  if((pos_insert < 0) ||(pos_insert >=rankList.size()))
      pos_insert = rankList.size();
     rankList.add( pos_insert, new Integer( n_toInsert ) );
/*  System.out.print( pos_insert+" ");
  for ( int i = 0; i < rankList.size();i++)
    {int j= ((Integer)(rankList.elementAt(i))).intValue();
     Operator X = (Operator)(opList.elementAt( j ) );
     System.out.print( X.getCommand()+" " );
    }
  System.out.println("");
*/
  }
private  void insert( Vector opList, Vector rankList )
  {if( opList.size()<= 0)
      return;
    
     
   Operator op = (Operator)( opList.elementAt( opList.size() - 1));
   int n = opList.size() -1;
   int indx = -1;
   if( rankList.equals( SortOnCommand ) )
     indx = find( op.getCommand(), Command_Compare );
   else if(op instanceof ScriptOperator )
     indx = find( ((ScriptOperator)op).getFileName(), File_Compare );
   else 
      return;
   
   insert( n, indx, rankList);
  }
/**
* binary search for inserting only
*@returns  the first index in the appropriate rank list that is more than or = to op
*/
private  int find( String textt, int mode )
  {int mid, first, last;
   boolean done = false;
   boolean less;
   Vector V;
 
   if( textt == null )
      return -5;  
   if( mode == Command_Compare )
      V = SortOnCommand;
   else
      V = SortOnFileName;
   first = 0;
   last = V.size() -1;
   done = first > last; 
   less = true;
   if( done)
       { mid = 0;
         less = false;
       }
   else
   for( mid =(first + last ) / 2; !done ; )
      {int pos = ((Integer)(V.elementAt(mid))).intValue();
      Operator opp = (Operator)(opList.elementAt(pos));
       less = false;
  
       if( mode == Command_Compare)
           {if( ScompareLess(opp.getCommand() , textt))
               less = true;
            else if( opp.getCommand().equals( textt ))  
                return mid;  
           }
        else
          { 
            if( !(V.equals( SortOnFileName )  ) )
               return -12;
           String  sopp;
           
            sopp = ((ScriptOperator)opp).getFileName();
           if( ScompareLess(sopp , textt) )
              less = true;
           else if( sopp.equals( textt ) )
               return mid;

          }
        if( less)
           {first = mid +1;
           }
        else
           {last = mid -1;
           }
         if( first > last)
            done = true;
         else
           mid =(first + last )/2;

       }
     
     if( less )
         return mid +1;
     else
         return mid;
     
  }
private Operator getOp1( int index )
  {if( index < 0 )
        return null;
     if( index >= opList.size())
        return null;
     Operator X = (Operator)(opList.elementAt( index));
     return X;

   }
private int CommandListIndex(int index)
  { if( index < 0) return -1;
    if( index >= SortOnCommand.size()) return -1;
    Integer X = (Integer)SortOnCommand.elementAt(index);
    int j = X.intValue();
    return j;
  }
public String getOperatorCommand( int index )
   {   Operator X = getOp1(CommandListIndex(index));
      if(X != null)  
         return X.getCommand();
      else 
         return null;
   }
public int getNum_operators()
  { return opList.size();
   }
public int getNumParameters( int index )
  { Operator X = getOp1(CommandListIndex(index));
    if( X == null )
        return 0;
    else
       return X.getNum_parameters();

   }
public Object getOperatorParameter( int index, int par_index)
   { 
    Operator X = getOp1(CommandListIndex(index));
    if( X == null )
        return null;
    int n = X.getNum_parameters();
    if( par_index < 0 )
       return null;
    if( par_index >= n )
       return null;
    Parameter P = X.getParameter( par_index );
    if( P == null )
      return null;
    return P.getValue();
     
     
    }
public Operator getOperator( int index )
   {
   Operator X = getOp1( CommandListIndex(index) );
   if( X == null )
     return null;
   if( X instanceof ScriptOperator )
     {String filename = ((ScriptOperator)X).getFileName();
      return new ScriptOperator( filename );
     }
   else
     {Class C = X.getClass();
      try{
         return (Operator)(C.newInstance());
         }
      catch(Exception s)
         { return null;}
      }
   }
public int getOperatorPosition( String CommName)
  {int i = find(CommName , Command_Compare);
   if( i < 0 )
      return i;
   int j = i-1;
   while( j >= 0 )
     {if( getOperatorCommand( j ).equals(CommName))
          {i = j;
           j--;
          }
       else
           return i;
     }
   return i;
   }

public Operator getOperator( String CommName)
  {int i = getOperatorPosition( CommName);
   if( i < 0)
     return null;
   return getOperator( i );
  }
private String getString()
  {char c=0;
   String S ="";
   try{
   while( c<32)
      c = (char)System.in.read();
   
   while( c>=32)
    {S = S + c;
     c = (char)System.in.read();
    }
   }
  catch(Exception s){}
  //System.out.println("getString="+S);
   return S;

   }
public static void main( String args[] )
   { java.util.Properties isawProp;
     isawProp = new java.util.Properties(System.getProperties());
   String path = System.getProperty("user.home")+"\\";
       path = StringUtil.fixSeparator(path);
       try {
	    FileInputStream input = new FileInputStream(path + "props.dat" );
          isawProp.load( input );
	   
          System.setProperties(isawProp);  
    
          input.close();
       }
       catch (IOException ex) {
          System.out.println("Properties file could not be loaded due to error :" +ex);
       }
     System.out.println("Here");
     Script_Class_List_Handler BB = new Script_Class_List_Handler();
     BB.show(257);
     
   
     char c=0;
     String S="";
     int n=0, N=0;
     Operator O1=null, O2=null;
     
     while( c!='x')
       {
            System.out.println("Enter option desired");
            System.out.println("    s: Enter String");
            System.out.println("    n: Enter Integer");
            System.out.println("    N: Enter 2nd Integer");
            System.out.println("     c:Test getopCommand(index)");
            System.out.println("     i.Test index=getOp(Command)");
            System.out.println("     1: getNewOp(index) 1");
            System.out.println("     2: getNewOp(index) 2");
	    System.out.println("     m: getNumParameters");
            System.out.println("     p:getParameter");
            try
               {
                 c=0;
                while( c < 32)
                  { c=(char)System.in.read();
                   }
               }
             catch(IOException s){c=0;}
            try{
            if( c=='s')
              S = BB.getString();
            
            else if( c=='n')
              n = new Integer(BB.getString()).intValue();
            else if( c=='N')
              N = new Integer(BB.getString()).intValue();

            else if( c=='c')
              System.out.println(BB.getOperatorCommand( n ));

            else if( c=='i')
              System.out.println(BB.getOperatorPosition( S));
            else if( c=='1')
             { O1=BB.getOperator( n );
               JParametersDialog JP=new JParametersDialog( O1,null,null);
             }
            else if( c=='2')
              {O2=BB.getOperator( N);
               JParametersDialog JP=new JParametersDialog( O2,null,null);

              }

            else if( c=='m')
               System.out.println(BB.getNumParameters(n));
            else if( c=='p')
               System.out.println(BB.getOperatorParameter(n,N));
               }
           catch(Exception s){ System.out.println("Exception="+s);}

        }
     System.exit( 0 );
   }
private  boolean ScompareLess( String s1, String s2)
  { if( s1 == null )
       if( s2 != null )
         return true;
       else
         return false;
    for( int i=0 ; i< s1.length(); i++)
     {if( i >= s2.length())
        return false;
      if( s1.charAt(i) >s2.charAt( i ))
        return false;
      if( s1.charAt(i) < s2.charAt( i ))
         return true;

      }
      
    if( s1.length() == s2.length())
       return false;
    return true;

  }
}
