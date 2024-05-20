import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Applies a simple mean low-pass filter on a given image. (Lab 6)
 */
public class MeanUserRadius_ implements PlugInFilter {

	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
			{showAbout(); return DONE;}
		return DOES_8G;
	} //setup


	public void run(ImageProcessor ip) {
		byte[] pixels = (byte[])ip.getPixels();
		int width = ip.getWidth();
		int height = ip.getHeight();
		int tgtRadius = 4; //r=4 ==> 9x9 mask

		//let the user enter the radius
		GenericDialog gd = new GenericDialog("user input");
		gd.addNumericField("radius", tgtRadius, 0);
		gd.showDialog();
		if(gd.wasCanceled()) {
		  return;
		} //if -was canceled
        tgtRadius = (int)Math.round(gd.getNextNumber());
        System.out.println("mean kernel radius = " + tgtRadius);

		int[][] inImgInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
		double[][] inImgDbl = ImageJUtility.convertToDoubleArr2D(inImgInt, width, height);

		double[][] meanKernel = ConvolutionFilter.getMeanMask(tgtRadius);
		double[][] resultImg = ConvolutionFilter.convolveDoubleNorm(
				inImgDbl, width, height, meanKernel, tgtRadius);

		ImageJUtility.showNewImage(resultImg, width, height, "mean filtered, r= " + tgtRadius);
	} //run

	void showAbout() {
		IJ.showMessage("About MeanUserRadius_...",
			"this is a PluginFilter for low-pass filtering\n");
	} //showAbout

} //class MeanUserRadius_

