package data.generator;

public class NormalDG extends DataGenerator{

	float[] means, vars;
	
	public NormalDG(int dim, float[][] bounds, boolean isFloatEnable)
			throws Exception {		
		super(dim, bounds, isFloatEnable);
		
		UniformDG udg= new UniformDG(dim, bounds, false);

		means = new float[dim];
		vars = new float[dim];
		
		for (int i = 0; i < means.length; i++) {
			means[i] = udg.sample_RV(i);
			vars[i] =  50;
		}
	}

	@Override
	float sample_RV(int dim) {
		
		float nr01;
		float result;
		
		if(bounds[dim][0] == bounds[dim][1])
			return bounds[dim][0];
		
		do{
			nr01 = (float)normalRandom();
			result = nr01 * vars[dim] + means[dim];
			
		}while(!validate(dim, result));
		
		return result;
	}

	private boolean validate(int dim, float res) {
		
		if(res>= bounds[dim][0])
			if(res <= bounds[dim][1])
				return true;
		
		return false;
	}

	private double normalRandom () {
		return Math.sqrt( -2.0d * Math.log( Math.random() ) ) * 
				Math.sin( 2.0d * Math.PI * Math.random() );

	}
}
