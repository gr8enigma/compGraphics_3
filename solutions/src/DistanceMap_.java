import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DistanceMap_ implements PlugInFilter{

    int[][] inDataArrInt;
    int width;
    int height;
    ArrayList<Integer> blockedPixelValues;

    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about"))
        {showAbout(); return DONE;}
        return DOES_8G;
    } //setup

    boolean isAllowedMove(Position currentPosition, int dX, int dY) {
        int targetX = currentPosition.xPos + dX;
        int targetY = currentPosition.yPos + dY;

        // case: out of image
        if((targetX < 0 || targetX >= width) || (targetY < 0 || targetY >= height)) {
            return false;
        }
        // case blocked Area (barriers, obstacle safety zone)
        int targetValue = inDataArrInt[targetX][targetY];
        if(blockedPixelValues.contains((Integer)targetValue)) {
            return false;
        }

        // case: corners
        if(blockedPixelValues.contains((Integer) inDataArrInt[currentPosition.xPos][targetY]) &&
                blockedPixelValues.contains((Integer) inDataArrInt[targetX][currentPosition.yPos])) {
            return false;
        }

        return true;
    }

    double costsBetweenPositions(Position start, Position target) {
        // Chebychev
         return 1;
    }




    public double[][] getDistanceMap() {
        this.blockedPixelValues = new ArrayList<>();
        this.blockedPixelValues.add((Integer)0);
        this.blockedPixelValues.add((Integer)180);

        ArrayDeque<Position> openSet = new ArrayDeque<>();
        HashSet<Position> visitedPixels = new HashSet<>();

        Position initialPosition = null;
        boolean initialPositionFound = false;

        double[][] distanceMap = new double[this.width][this.height];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (this.inDataArrInt[j][i] == 120) {
                    distanceMap[j][i] = 0.0;
                    if(!initialPositionFound) {
                        initialPosition = new Position(j, i);
                        initialPositionFound = true;
                    }
                } else {
                    distanceMap[j][i] = Double.POSITIVE_INFINITY;
                }
            }
        }
        if(!initialPositionFound)
            throw new RuntimeException("no target position exists");

        openSet.add(initialPosition);
        visitedPixels.add(initialPosition);

        while (!openSet.isEmpty()) {
            Position current = openSet.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {    // exclude non - moves
                        if (isAllowedMove(current, dx, dy)) {
                            Position neighbor = new Position(current.xPos + dx, current.yPos + dy);
                            if (!visitedPixels.contains(neighbor)) {
                                openSet.add(neighbor);
                                visitedPixels.add(neighbor);
                            }
                            double tentativeScore = distanceMap[current.xPos][current.yPos] + costsBetweenPositions(current, neighbor);
                            if (tentativeScore < distanceMap[neighbor.xPos][neighbor.yPos]) {
                                distanceMap[neighbor.xPos][neighbor.yPos] = tentativeScore;
                            }
                        }
                    }
                }

            }
        }
        return distanceMap;
    }




    public void run(ImageProcessor ip) {
        byte[] pixels = (byte[])ip.getPixels();
        this.width = ip.getWidth();
        this.height = ip.getHeight();
        this.inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, this.width, this.height);



        double[][] distanceMap = getDistanceMap();
        int[][] finalMap = DistanceMapScaler.scaleToImageInterval(distanceMap, this.height, this.width);

//        int[][] finalMap = new int[this.width][this.height];
//        for(int i = 0; i < this.height; i++) {
//            for(int j = 0; j < this.width; j++) {
//                finalMap[j][i] = (int)distanceMap[j][i];
//            }
//        }

        ImageJUtility.showNewImage(finalMap, this.width, this.height, "same image");
    } //run

    void showAbout() {
        IJ.showMessage("About DistanceMap_...",
                "Creates a DistanceMap for Path Finding\n");
    } //showAbout


} //class DistanceMap_