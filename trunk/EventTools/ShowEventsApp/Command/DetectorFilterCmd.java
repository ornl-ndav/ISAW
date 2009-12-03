package EventTools.ShowEventsApp.Command;

import gov.anl.ipns.Util.Numeric.IntList;

import java.util.Vector;

public class DetectorFilterCmd
{
    private Vector detectors;
    private Vector rows;
    private Vector cols;
    
    public DetectorFilterCmd(Vector detectors,
                             Vector rows,
                             Vector cols)
    {
        this.detectors = detectors;
        this.rows = rows;
        this.cols = cols;
    }

    public Vector getDetectors()
    {
        return detectors;
    }

    public Vector getRows()
    {
        return rows;
    }

    public Vector getCols()
    {
        return cols;
    }
    
    public String toString()
    {
       return "\nDetectors : " + getDetectors().toString() +
              "\nRows : " + getRows().toString() +
              "\nCols : " + getCols().toString();
    }
}
