/*
 * @(#)DoubleDifferentialCrossection.java   0.1  2000/07/25   Dennis Mikkelson
 *             
 *  $Log$
 *  Revision 1.1  2000/07/24 16:05:17  dennis
 *  Operator to calculate the Double Differential Crossection for a Spectrometer.
 *
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  Compute the double differential crossection for a time-of-flight 
  *  spectrometer DataSet based on a sample run, background run and the
  *  number of atoms in the sample.
  *
  *  @see DataSetOperator
  *  @see Operator
  */

public class DoubleDifferentialCrossection extends    DataSetOperator 
                                           implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public DoubleDifferentialCrossection( )
  {
    super( "Double Differential Crossection" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to calculate the Double Differential Crossection
   *  for a spectrometer DataSet.  It is assumed that the background DataSet
   *  has already been normalized and subtracted from the sample DataSet.
   *
   *  @param  ds               The sample DataSet for which the double 
   *                           differential crossection is to be calculated 
   *  @parm   background_ds    The background DataSet.
   *  @param  atoms            The number of "scattering units" in the sample
   *                           exposed to the beam times 10 ** -24.
   *  @param  make_new_ds Flag that determines whether a new DataSet is
   *                           constructed, or the Data blocks of the original 
   *                           DataSet are just altered.
   */

  public DoubleDifferentialCrossection( DataSet    ds,
                                        float      atoms,
                                        boolean    make_new_ds )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    Parameter parameter = getParameter( 1 );
    parameter.setValue( new Float(atoms) );

    parameter = getParameter( 2 );
    parameter.setValue( new Boolean( make_new_ds ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "DSDODE";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Atoms in sample (times 10**-24)",
                                new Float( 1.0 ) ); 
    addParameter( parameter );

    parameter = new Parameter( "Create new DataSet?", new Boolean(false) );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {       
    return "NOT IMPLEMENTED YET";
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    DoubleDifferentialCrossection new_op = new DoubleDifferentialCrossection( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
