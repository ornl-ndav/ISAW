<<<<<<< ScriptOperator.java
package Command;


import javax.swing.text.*;
//import Command.*;
import java.lang.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
import java.beans.*;
import java.io.*;
public class ScriptOperator extends GenericOperator
                            implements IObservable,
                                       Customizer  //for property change events
                                        
 {
   private String filename;
   private String command;
   
   private String categList[];
   private ScriptProcessor SP;
   private String errorMessage ="";
   public static String ER_FILE_ERROR             = "File error ";
   public ScriptOperator(  String filename )
     {  super("UNKNOWN");
        
        this.filename = filename;
        command = null;
        categList = null;
        SP = null;
        IsawGUI.Util  ut = new IsawGUI.Util();
        Document D =ut.openDoc( filename ) ;
        errorMessage ="";
        if((D == null) ||( filename == null ))
          { errorMessage = ER_FILE_ERROR;
            return;
          }
        
        SP = new ScriptProcessor( D );
        

        if( SP.getErrorMessage().length( ) > 0 )
           {  errorMessage = SP.getErrorMessage();
              return;
            }
        int i, j;
        

        j = filename.lastIndexOf( '.');
        if( j < 0 ) 
          j = filename.length();
        
        String F = filename.replace('\\','/');
        i = F.lastIndexOf( '/', j);
        if( i < 0 )
           i = -1;
        command = F.substring( i + 1, j );
        F = F.substring( 0, i );
        String F2 = F;
        String X = System.getProperty( "Script_Path");
          
	
        if( X != null )
          {X = X.replace( '\\' , '/' );
           X = X.replace(java.io.File.pathSeparatorChar,';');
           F2 = adjust( F, X );
           }
        

        if( F2.equals(F))
         {  X = System.getProperty( "java.class.path");
           X = X.replace( '\\', '/');
           X.replace(java.io.File.pathSeparatorChar, ';');
          
            F2 = adjust ( F , X );


         } 
         

        if( !(F2.equals(F)))
             F = F2;
        else
          { i =F.indexOf(':');
            if( i > 0 )
            F = F.substring( i + 1 );
            if( F.charAt( 0 ) == '/' )
                F = F.substring ( 1);
            }
        

       //  System.out.println("Final"+F);
        F.trim();
        if(F.length() >0)
          if(F.charAt(0) == '/')
             F =F.substring(1);
        if(F.length() > 0 )
          if( F.charAt(F.length()-1) =='/')
            F = F.substring( 0, F.length()-1);
        int c = 1;
                
        if( F.length()>1)
         for( i = F.indexOf( '/' ); i >= 0;  i = F.indexOf( '/' , i + 1 ) )
           c++;
         
        if( F.length()>1)
          categList = new String [ c + 1];
        else
          categList = new String [ c ];

        categList[ 0 ] = DataSetTools.operator.Operator.OPERATOR;
        j = 1 ;
        
        
        int i1 = 0;
        if( F.length()>1)
        for( i = F.indexOf( '/' ); i >= 0;  i = F.indexOf( '/' , i + 1 ) )
          {categList[j] = F.substring(i1, i );
           j++;
           i1 = i+1;
          }
         

        if( F.length()>1)
         categList[ c ] = F.substring( i1 ) ;               
        

      }
 public void show()
    {System.out.println( "Command ="+command );
     if(categList == null )
         System.out.println( "Cat list is null" );
     else
     {System.out.println("Cat leng="+categList.length);
     for(int i = 0; i < categList.length; i++ )
         {System.out.print( categList[i]+",");
         }
      System.out.println("");
       }
      }

 private String adjust( String F1, String X ) 
       {String F = F1;
         if( F == null ) 
           return null;
         F = F.trim();
         if( F.length()<1)
           return F;
         if( F.charAt(F.length()-1) !='/')
            F = F+'/';
          int i = 0;
        X = X.replace(java.io.File.pathSeparatorChar,';');
       // System.out.println("X ="+X);
        int j = X.indexOf( ';');
        boolean done = X.length()==0;
        if( j < 0) 
           j = X.length();
        
        while( !done)
          {//System.out.println(F+"-"+X.substring(i,j));
            if (F.toUpperCase().indexOf( X.substring(i,j).toUpperCase() ) >=0 )
             { done = true;
               F = F.substring( j - i ); 
               //System.out.println("adjusted");
               return F;
              }
           else if( j < X.length() )
             { i= j  + 1;
               j = X.indexOf( ';' , i );
               if( j < 0) 
                   j = X.length();
             }
           else 
              done = true;
          }
         return F;
        }
public String getTitle()
  { if( SP != null )
       return SP.getTitle();
    else 
       return null;
   }
public String[] getCategoryList()
  {return categList;
  }
public String getCategory()
  {if(categList == null )
      return "UNKNOWN";
   if( categList.length <= 0)
      return "UNKNOWN";
   return categList[categList.length-1];
  }
public String getErrorMessage()
  { return errorMessage;
  }
public void setDefaultParameters()
  { if(SP != null)
       SP.setDefaultParameters();
     }
public String getCommand()
  {if(command == null)
      return "unknown";
    return command;
  }
public String getFileName()
  {return filename;
   }
public int getErrorCharPos()
  { return SP.getErrorCharPos();
   }
public int getErrorLine()
   {return SP.getErrorLine();
   }
public int getNum_parameters()
  {return SP.getNum_parameters();
  }

public boolean setParameter(Parameter parameter, int index)
  { return SP.setParameter( parameter,index);
  }
public Parameter getParameter( int index)
  { return SP.getParameter( index );
  }
public void addParameter( Parameter P)
  { return;
  }
public void CopyParametersFrom( Operator op)
  { SP.CopyParametersFrom( op );
  }
public void addPropertyChangeListener( PropertyChangeListener pl )
  {SP.addPropertyChangeListener( pl );
  }
public void removePropertyChangeListener(PropertyChangeListener listener)
  { //SP.removePropertyChangeListener( listener);
   }
public void setObject(Object bean)
  {
   }
public void addIObserver( IObserver iobs )
 {SP.addIObserver( iobs );
 }
public void deleteIObserver( IObserver iobs )
    {SP.deleteIObserver( iobs ) ; 
       
    }

   /**
     *deletes all the Iobserver 
    
     */

  public void deleteIObservers()
    {SP.deleteIObservers() ; 
     

    }
public void setLogDoc( Document doc )
  {SP.setLogDoc(doc );
   }
 
public Object getResult()
  { if( SP != null)
      { Object Res = SP.getResult();
        errorMessage = SP.getErrorMessage();
        return Res;
      }
     else
         return null;
   }
public static void main( String args [] )
  {
   java.util.Properties isawProp;
   isawProp = new java.util.Properties(System.getProperties());
   String path = System.getProperty("user.home")+"\\";
       path = StringUtil.fixSeparator(path);
       try {
	    FileInputStream input = new FileInputStream(path + "props.dat" );
          isawProp.load( input );
	  
          System.setProperties(isawProp);  
    //    System.getProperties().list(System.out);
          input.close();
       }
       catch (IOException ex) {
          System.out.println("Properties file could not be loaded due to error :" +ex);
       }




     ScriptOperator SO;
    SO = new ScriptOperator("C:/Ruth/ISAW/Scripts/Test.iss");

  System.out.println("Error ="+SO.getErrorMessage() );
  System.out.println("filename="+SO.getFileName() );
  SO.show();
   }
 }
=======
package Command;


import javax.swing.text.*;
//import Command.*;
import java.lang.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
import java.beans.*;
import java.io.*;
public class ScriptOperator extends GenericOperator
                            implements IObservable,
                                       Customizer  //for property change events
                                        
 {
   private String filename;
   private String command;
   
   private String categList[];
   private ScriptProcessor SP;
   private String errorMessage ="";
   public static String ER_FILE_ERROR             = "File error ";
   public ScriptOperator(  String filename )
     {  super("UNKNOWN");
        
        this.filename = filename;
        command = null;
        categList = null;
        SP = null;
        IsawGUI.Util  ut = new IsawGUI.Util();
        Document D =ut.openDoc( filename ) ;
                errorMessage ="";
        if((D == null) ||( filename == null ))
          { errorMessage = ER_FILE_ERROR;
            return;
          }
        
        SP = new ScriptProcessor( D );
        

        if( SP.getErrorMessage().length( ) > 0 )
           {  errorMessage = SP.getErrorMessage();
              return;
            }
        int i, j;
        

        j = filename.lastIndexOf( '.');
        if( j < 0 ) 
          j = filename.length();
        
        String F = filename.replace('\\','/');
        i = F.lastIndexOf( '/', j);
        if( i < 0 )
           i = -1;
        command = F.substring( i + 1, j );
        F = F.substring( 0, i );
        String F2 = F;
        String X = System.getProperty( "Script_Path");
          

        if( X != null )
          {X = X.replace( '\\' , '/' );
           X = X.replace(java.io.File.pathSeparatorChar,';');
           F2 = adjust( F, X );
           }
        

        if( F2.equals(F))
         {  X = System.getProperty( "java.class.path");
           X = X.replace( '\\', '/');
           X.replace(java.io.File.pathSeparatorChar, ';');
          
            F2 = adjust ( F , X );


         } 
         

        if( !(F2.equals(F)))
             F = F2;
        else
          { i =F.indexOf(':');
            if( i > 0 )
            F = F.substring( i + 1 );
            if( F.charAt( 0 ) == '/' )
                F = F.substring ( 1);
            }
        

       //  System.out.println("Final"+F);
        F.trim();
        if(F.length() >0)
          if(F.charAt(0) == '/')
             F =F.substring(1);
        if(F.length() > 0 )
          if( F.charAt(F.length()-1) =='/')
            F = F.substring( 0, F.length()-1);
        int c = 1;
                
        if( F.length()>1)
         for( i = F.indexOf( '/' ); i >= 0;  i = F.indexOf( '/' , i + 1 ) )
           c++;
         
        if( F.length()>1)
          categList = new String [ c + 1];
        else
          categList = new String [ c ];

        categList[ 0 ] = DataSetTools.operator.Operator.OPERATOR;
        j = 1 ;
        
        
        int i1 = 0;
        if( F.length()>1)
        for( i = F.indexOf( '/' ); i >= 0;  i = F.indexOf( '/' , i + 1 ) )
          {categList[j] = F.substring(i1, i );
           j++;
           i1 = i+1;
          }
         

        if( F.length()>1)
         categList[ c ] = F.substring( i1 ) ;               
        

      }
 public void show()
    {System.out.println( "Command ="+command );
     if(categList == null )
         System.out.println( "Cat list is null" );
     else
     {System.out.println("Cat leng="+categList.length);
     for(int i = 0; i < categList.length; i++ )
         {System.out.print( categList[i]+",");
         }
      System.out.println("");
       }
      }

 private String adjust( String F1, String X ) 
       {String F = F1;
         if( F == null ) 
           return null;
         F = F.trim();
         if( F.length()<1)
           return F;
         if( F.charAt(F.length()-1) !='/')
            F = F+'/';
          int i = 0;
        X = X.replace(java.io.File.pathSeparatorChar,';');
       // System.out.println("X ="+X);
        int j = X.indexOf( ';');
        boolean done = X.length()==0;
        if( j < 0) 
           j = X.length();
        
        while( !done)
          {//System.out.println(F+"-"+X.substring(i,j));
            if (F.toUpperCase().indexOf( X.substring(i,j).toUpperCase() ) >=0 )
             { done = true;
               F = F.substring( j - i ); 
               //System.out.println("adjusted");
               return F;
              }
           else if( j < X.length() )
             { i= j  + 1;
               j = X.indexOf( ';' , i );
               if( j < 0) 
                   j = X.length();
             }
           else 
              done = true;
          }
         return F;
        }
public String getTitle()
  { if( SP != null )
       return SP.getTitle();
    else 
       return null;
   }
public String[] getCategoryList()
  {return categList;
  }
public String getCategory()
  {if(categList == null )
      return "UNKNOWN";
   if( categList.length <= 0)
      return "UNKNOWN";
   return categList[categList.length-1];
  }
public String getErrorMessage()
  { return errorMessage;
  }
public void setDefaultParameters()
  { if(SP != null)
       SP.setDefaultParameters();
     }
public String getCommand()
  {if(command == null)
      return "unknown";
    return command;
  }
public String getFileName()
  {return filename;
   }
public int getErrorCharPos()
  { return SP.getErrorCharPos();
   }
public int getErrorLine()
   {return SP.getErrorLine();
   }
public int getNum_parameters()
  {return SP.getNum_parameters();
  }

public boolean setParameter(Parameter parameter, int index)
  { return SP.setParameter( parameter,index);
  }
public Parameter getParameter( int index)
  { return SP.getParameter( index );
  }
public void addParameter( Parameter P)
  { return;
  }
public void CopyParametersFrom( Operator op)
  { SP.CopyParametersFrom( op );
  }
public void addPropertyChangeListener( PropertyChangeListener pl )
  {SP.addPropertyChangeListener( pl );
  }
public void removePropertyChangeListener(PropertyChangeListener listener)
  { //SP.removePropertyChangeListener( listener);
   }
public void setObject(Object bean)
  {
   }
public void addIObserver( IObserver iobs )
 {SP.addIObserver( iobs );
 }
public void deleteIObserver( IObserver iobs )
    {SP.deleteIObserver( iobs ) ; 
       
    }

   /**
     *deletes all the Iobserver 
    
     */

  public void deleteIObservers()
    {SP.deleteIObservers() ; 
     

    }
public void setLogDoc( Document doc )
  {SP.setLogDoc(doc );
   }
 
public Object getResult()
  { if( SP != null)
      { Object Res = SP.getResult();
        errorMessage = SP.getErrorMessage();
        return Res;
      }
     else
         return null;
   }
public static void main( String args [] )
  {
   java.util.Properties isawProp;
   isawProp = new java.util.Properties(System.getProperties());
   String path = System.getProperty("user.home")+"\\";
       path = StringUtil.fixSeparator(path);
       try {
	    FileInputStream input = new FileInputStream(path + "props.dat" );
          isawProp.load( input );
	  
          System.setProperties(isawProp);  
    //    System.getProperties().list(System.out);
          input.close();
       }
       catch (IOException ex) {
          System.out.println("Properties file could not be loaded due to error :" +ex);
       }




     ScriptOperator SO;
    SO = new ScriptOperator("E:/Isaw/Scripts/junk.iss");

  System.out.println("Error ="+SO.getErrorMessage() );
  System.out.println("filename="+SO.getFileName() );
  SO.show();
   }
 }
>>>>>>> 1.2
