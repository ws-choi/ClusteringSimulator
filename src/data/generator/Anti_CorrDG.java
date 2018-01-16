package data.generator;

import index.rtree.dimitris.Data;

public class Anti_CorrDG extends NormalDG{

	float ratio;
	int pos_dim, neg_dim;
	
	public Anti_CorrDG(int dim, float[][] bounds, boolean isFloatEnable, int pos_dim, int neg_dim)
			throws Exception {
		super(dim, bounds, isFloatEnable);
		
		this.pos_dim = pos_dim; 
		this.neg_dim = neg_dim;
		
		ratio = bounds[neg_dim][1] - bounds[neg_dim][0];
		ratio /= bounds[pos_dim][1] - bounds[pos_dim][0];
		ratio *= -1;
		
	}

	@Override
	public Data getNext() {
		
		Data buf = super.getNext();
		
		buf.data[2*neg_dim] = buf.data[2*neg_dim+1] = ratio * buf.data[2*pos_dim] + bounds[neg_dim][1];
		
		return buf;
		
	}
}
