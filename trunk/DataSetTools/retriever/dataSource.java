package DataSetTools.retriever;



public class dataSource
{ String Source;
    public dataSource( String data_source_name)
     {Source = data_source_name;
     }
 public boolean check()
   {if( getSemiColon( 4) < 0)
      return false;
    if( getSemiColon( 5 ) >= 0)
      return false;
    return true;
   }
  private int getSemiColon( int i )
    {if( i < 0) return -1;
     if( i >4) return -1;
     int c=0;
     for( int k = Source.indexOf(";");  k>=0; )
        {c++;
         if( c == i)
            return k;
         if( c > i)
           return -1;
         k = Source.indexOf( ";", k+1);
        }
     return -1;
    }
   public String getMachine()
    {int i = getSemiColon( 1);
     if( i< 0) return null;
     return Source.substring( 0, i);
    }

   public String getPort()
    {int i = getSemiColon( 1);
     int j = getSemiColon(2);
     if((i < 0) || (j < 0)) return null;
     if( (i >= j) ||( j >= Source.length()))
        return null;
     return Source.substring( i+1,j);
    }

   public String getUserName()
    {int i = getSemiColon( 2);
     int j = getSemiColon(3);
     if((i < 0) || (j < 0)) return null;
     if( (i >= j) ||( j >= Source.length()))
        return null;
     return Source.substring( i+1,j);
    }

   public String getPassWord()
    {int i = getSemiColon( 3);
     int j = getSemiColon(4);
     if((i < 0) || (j < 0)) return null;
     if( (i >= j) ||( j >= Source.length()))
        return null;
     return Source.substring( i+1,j);
    }

   public String getFileName()
    {int i = getSemiColon( 4);
    
     if((i < 0) ) return null;
     if(( i >= Source.length()))
        return null;
     return Source.substring( i+1);
    }
public static void main( String args[])
 {if( args != null)
  if (args.length > 0)
   {dataSource DT = new dataSource( args[0] );
    System.out.println( DT.getMachine());
    System.out.println( DT.getPort());
    System.out.println( DT.getUserName());
    System.out.println( DT.getPassWord());
    System.out.println( DT.getFileName());
    


   }
  System.exit( 0 );
  }
}
