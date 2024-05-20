/**
 * Utility class that supports various convolution filters
 */
public class ConvolutionFilter {

    /**
     * convolution of input image with kernel, normalization to kernel sum 1.0 only use for low-pass filters
     *
     * @param inputImg The input image on which the kernel should be applied.
     * @param width    The width of the image.
     * @param height   The height of the image.
     * @param kernel   The kernel that should be applied on the image.
     * @param radius   The radius of the kernel.
     * @return The image with the applied kernel.
     */
    public static double[][] convolveDoubleNorm(double[][] inputImg, int width, int height, double[][] kernel, int radius) {
        double[][] returnImg = new double[width][height];

        //move hot-spot to all possible positions, e.g. (3,3) cf. lecture slide
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                //e.g. (3,3) for (x,y)
                //xOffset [-radius;radius], ... [-1;1] ==> nb positions (2,2) to (4,4)
                double sum = 0.0;
                double coeffSum = 0.0; //should be 1.0 for all low-pass filters!!!!
                for(int xOffset = -radius; xOffset <= radius; xOffset++) {
                    for(int yOffset = -radius; yOffset <= radius; yOffset++) {
                        int nbX = x + xOffset;
                        int nbY = y + yOffset;
                        if (nbX >= 0 && nbY >= 0 && nbX < width && nbY < height) {
                            sum += inputImg[nbX][nbY] * kernel[xOffset + radius][yOffset + radius];
                            coeffSum += kernel[xOffset + radius][yOffset + radius];
                        } //if inside the image
                    } //for yOffset
                } //for xOffset
                returnImg[x][y] = sum / coeffSum;
            } //for y
        } //for x

        return returnImg;
    }

    /**
     * convolution of input image with kernel
     *
     * @param inputImg The input image on which the kernel should be applied.
     * @param width    The width of the image.
     * @param height   The height of the image.
     * @param kernel   The kernel that should be applied on the image.
     * @param radius   The radius of the kernel.
     * @return The image with the applied kernel.
     */
    public static double[][] convolveDouble(double[][] inputImg, int width, int height, double[][] kernel, int radius) {
        double[][] returnImg = new double[width][height];

        //move hot-spot to all possible positions, e.g. (3,3) cf. lecture slide
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
              //e.g. (3,3) for (x,y)
              //xOffset [-radius;radius], ... [-1;1] ==> nb positions (2,2) to (4,4)
              double sum = 0.0;
              for(int xOffset = -radius; xOffset <= radius; xOffset++) {
                  for(int yOffset = -radius; yOffset <= radius; yOffset++) {
                     int nbX = x + xOffset;
                     int nbY = y + yOffset;
                     if (nbX >= 0 && nbY >= 0 && nbX < width && nbY < height) {
                         sum += inputImg[nbX][nbY] * kernel[xOffset + radius][yOffset + radius];
                     } //if inside the image
                  } //for yOffset
              } //for xOffset
              returnImg[x][y] = sum;
            } //for y
        } //for x

        return returnImg;
    }

    /**
     * returns kernel image according to specified radius for mean low-pass filtering
     *
     * @param tgtRadius the radius that the mean mask should have.
     * @return The resulting mean mask.
     */
    public static double[][] getMeanMask(int tgtRadius) {
        int size = 2 * tgtRadius + 1;
        double[][] kernelImg = new double[size][size];
        double coeff = 1.0 / (size * size);

        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                kernelImg[x][y] = coeff;
            }
        }

        return kernelImg;
    }

}
