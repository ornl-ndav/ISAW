package DataSetTools.operator.Generic.Special;

import DataSetTools.dataset.*;
import DataSetTools.operator.Operator.*;
import DataSetTools.util.*;
import DataSetTools.parameter.*;
import java.util.*;

public class Zero extends GenericSpecial{


    public Zero(){
       super("Zero channels");
    }

    public Zero( DataSet ds, int GroupIndex, int firstChannel, int lastChannel){
       this();
       parameters = new Vector();
       parameters.add( new DataSetPG("Enter Data Set", ds));
       parameters.add( new IntegerPG("Group Index to zero", GroupIndex));
       parameters.add( new IntegerPG("first Channel to zero", firstChannel));
       parameters.add( new IntegerPG("last Channel to zero", lastChannel));

    }
   
   public void setDefaultParameters(){
       parameters = new Vector();
       parameters.add( new DataSetPG("Enter Data Set", null));
       parameters.add( new IntegerPG("Group Index to zero", new Integer(0)));
       parameters.add( new IntegerPG("first Channel to zero", new Integer(0)));
       parameters.add( new IntegerPG("last Channel to zero", new Integer(10)));

    }

   public Object getResult(){

     DataSet ds = (DataSet)(getParameter(0).getValue());
     int Group = ((Integer)(getParameter(1).getValue())).intValue();
     int firstIndex = ((Integer)(getParameter(2).getValue())).intValue();
     int lastIndex = ((Integer)(getParameter(3).getValue())).intValue();

     if( ds == null)
       return new ErrorString(" No DataSet Selected");
     if( Group < 0)
       return new ErrorString( "Improper Group Index");
     if( Group >= ds.getNum_entries())
       return new ErrorString("No such Group Index");

     Data D = ds.getData_entry( Group);
     if( firstIndex > lastIndex)
       return new ErrorString("first Index must be less than last index");
     if( firstIndex < 0)
       firstIndex = 0;
     if( lastIndex >= D.getY_values().length)
       lastIndex = D.getY_values().length-1;
     float[] yvalues = D.getY_values();
     for( int i = firstIndex; i <= lastIndex; i++)
        yvalues[i]=0.0f;

     return "Success";
   }

}
