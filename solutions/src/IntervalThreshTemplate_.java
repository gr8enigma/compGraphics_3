import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class IntervalThreshTemplate_ implements PlugInFilter {

    public static final int FG_VAL = 255; //and max value
    public static final int BG_VAL = 0;

    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about"))
        {showAbout(); return DONE;}
        return DOES_8G+DOES_STACKS+SUPPORTS_MASKING;
    } //setup

    public void run(ImageProcessor ip) {
        byte[] pixels = (byte[])ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);

        // TODO

    } //run

    void showAbout() {
        IJ.showMessage("About Template_...",
                "inverts the scalar values\n");
    } //showAbout

} //class IntervalThresh_