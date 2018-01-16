package index.rtree.dimitris;
public class PPoint
{
    int dimension;
    public float data[];
    float distanz;

    public PPoint()
    {
        dimension = Constants.RTDataNode__dimension;
        data = new float[dimension];
        distanz = 0.0f;
    }
    
    public PPoint(int dimension)
    {
        this.dimension = dimension;
        data = new float[dimension];
        distanz = 0.0f;
    }
}