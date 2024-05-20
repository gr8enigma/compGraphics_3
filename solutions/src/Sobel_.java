import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Applies a sobel edge detection (high-pass filter) onto a given image. (Lab 6)
 */
public class Sobel_ implements PlugInFilter {

   public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
			{showAbout(); return DONE;}
		return DOES_8G;
	} //setup



	public void run(ImageProcessor ip) {
		byte[] pixels = (byte[])ip.getPixels();
		int width = ip.getWidth();
		int height = ip.getHeight();
		int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
        double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(inDataArrInt, width, height);

		//row vs. column major order
		//double[][] verticalSobelMask = new double[][]{{1, 0, -1}, {2, 0, -2}, {1, 0, -1}};
		double[][] verticalSobelMask = new double[][]{{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};

		//values in range [-1020;1020] possible while 8bit ==> [0..255]
		double[][] edgeResultImg = ConvolutionFilter.convolveDouble(
				inDataArrDbl, width, height, verticalSobelMask, 1);
		ImageJUtility.showNewImage(edgeResultImg, width, height, "vertical sobel #1");

		double maxVal = Double.MIN_VALUE;
		//remove negative values
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
               double oldVal = edgeResultImg[x][y];
			   oldVal = Math.abs(oldVal);
			   edgeResultImg[x][y] = oldVal;
			   if(oldVal > maxVal) {
				  maxVal = oldVal;
				  System.out.println("new max value found = " + maxVal);
			   }
			}
		}

		IJ.log("final max Value = " + maxVal);
		ImageJUtility.showNewImage(edgeResultImg, width, height, "vertical sobel #2");

		double scaleFactorToUse = 255.0 / maxVal;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				double oldVal = edgeResultImg[x][y] * scaleFactorToUse;
				edgeResultImg[x][y] = oldVal;
			}
		}
		ImageJUtility.showNewImage(edgeResultImg, width, height, "vertical sobel #3");
	} //run

	void showAbout() {
		IJ.showMessage("About Sobel_...",
			"this is a SobelFilter to detect vertical edges\n");
	} //showAbout

} //class Sobel_

