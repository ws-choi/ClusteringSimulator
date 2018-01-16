package data.generator;

import com.clustering.dbscan.gui.DataTuple;


public class UniformDG extends DataGenerator{

	public UniformDG (int dim, float[][] bounds, boolean isFloatEnable) throws Exception {
		super(dim, bounds, isFloatEnable);
	}
	


	public UniformDG(int dim, DataTuple data_range, boolean isFloatEnable) throws Exception {
		super(dim, data_range, isFloatEnable);
	}



	@Override
	float sample_RV(int dim) {

		float min = bounds[dim][0];
		float max = bounds[dim][1];
		float rv01 = (float) (Math.random() * (max-min) + min) ;
		
		return auto_cast(rv01);
	}

}
