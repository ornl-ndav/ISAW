package EventTools.ShowEventsApp.Command;

import java.util.Vector;

public class QDFilterCmd
{
// FLOATS
    private Vector q;
    private Vector d;
    
    public QDFilterCmd(Vector q, Vector d)
    {
        this.q = q;
        this.d = d;
    }    
    public Vector getQ()
    {
        return q;
    }

    public Vector getD()
    {
        return d;
    }
    
    public String toString()
    {
       return "\nDiscard Q : " + getQ().toString() +
              "\nDiscard D : " + getD().toString();
    }
}
