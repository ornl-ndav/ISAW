package DataSetTools.retriever;

/**
  *  Classes that implement this information have a method to get added
  *  information about the datasets that are dealt with
  */
public interface hasInformation{

   /**
     *  Returns info in the form for selecting this data set
     * @param  data_set_num  the number of the data set
     * @return an array of Strings where the first is the data set name,
     *         the second is the data set type( monitor,sample) and the
     *         third is the string description of a range of default GroupID'x
     *         for this data set.
     */
   public String[] getDataSetInfo( int data_set_num);


}
