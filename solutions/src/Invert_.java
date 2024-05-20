import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Plugin that inverts a given greyscale image. (Lab 5)
 */
public class Invert_ implements PlugInFilter {

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

        int[] inversionTF = ImageTransformationFilter.getInversionTF(255);
		int[][] invertedImg = ImageTransformationFilter.getTransformedImage(
				inDataArrInt, width, height, inversionTF);

        ImageJUtility.showNewImage(invertedImg, width, height, "inverted image");
	} //run

	void showAbout() {
		IJ.showMessage("About Invert_...",
			"this is a PluginFilter to invert the colors\n");
	} //showAbout

} //class Invert_

