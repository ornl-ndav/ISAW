package Command.JavaCC.Fortran;
import java.util.*;

public class ListHandler{

   LList List = null;
   int n=0;
   public ListHandler(){
      List = null;
   }

   public boolean add( Object value, String name){
      LList newItem = new LList( value,name);
      n++;
      if( List == null)
         List = newItem;
      else
         List.add(newItem);
      return true;

   }
   public Object get( String name){
     if( List == null)
       return null;
     LList L = List.get(name);
     if( L == null)
        return null;
     return L.value;
     

   }
  
  
  public int size(){
    return n;
  }

  public String getName( int i){
    if( (i < 0) || ( i >= n))
       return null;
    
    return List.get(i).name;

  } 

  public Object getValue( int i){
    if( (i < 0) || ( i >= n))
       return null;
    
    return List.get(i).value;

  } 
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
