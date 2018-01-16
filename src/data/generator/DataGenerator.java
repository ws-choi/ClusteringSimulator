package data.generator;

import index.rtree.dimitris.Data;


 abstract public class DataGenerator{

	protected int dim;
	protected float bounds[][];
	protected boolean isFloatEnable;

	public DataGenerator(int dim, float[][] bounds, boolean isFloatEnable) throws Exception {
		this.dim = dim;
		setBounds(bounds);
		this.isFloatEnable = isFloatEnable;
	}
	
	public DataGenerator(int dim, Data bounds, boolean isFloatEnable) throws Exception {

		System.out.println(bounds.dimension);
		
		this.dim = dim;
		setBounds(bounds);

		this.isFloatEnable = isFloatEnable;
	}
	
	private void setBounds (float[][] bounds) throws Exception {
		
		if (bounds.length != dim)	
			throw new Exception();
			
		for (int i = 0; i < bounds.length; i++)
			if(bounds[i][0] > bounds[i][1])
				throw new Exception();
		
		this.bounds = bounds.clone();
	}
	
	private void setBounds (Data data) throws Exception {
		
		if (data.dimension != dim)	
			throw new Exception();
		
		bounds= new float[dim][2];
		
		for (int i = 0; i < bounds.length; i++)
		{
			bounds[i][0] = data.data[2*i];
			bounds[i][1] = data.data[2*1+1];
		}
		
	}


	
	abstract float sample_RV(int dim); // Random Variable
	protected float auto_cast (float input) { return isFloatEnable ? input : (int)input;}	
	
	public Data getNext (){
		Data result = new Data(dim);
		
		for (int i = 0; i < dim; i++) 
			result.data[2*i] = result.data[2*i+1] = sample_RV(i);
		
		return result;
		
	}

}
