public class DistanceMapScaler {

    public static int[][] scaleToImageInterval(double[][] distanceMap, int height, int width) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        // Find the min and max values in the distance map
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double currentValue = distanceMap[j][i];
                if (!Double.isInfinite(currentValue)) {
                    if (currentValue < min) {
                        min = currentValue;
                    }
                    if (currentValue > max) {
                        max = currentValue;
                    }
                }
            }
        }

        int[][] scaledMap = new int[width][height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double normalizedValue = (distanceMap[j][i] - min) / (max - min);
                scaledMap[j][i] = (int) (normalizedValue * 255);
            }
        }
        return scaledMap;
    }

}