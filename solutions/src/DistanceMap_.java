import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.LutLoader;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DistanceMap_ implements PlugInFilter{

    int[][] inDataArrInt;
    int width;
    int height;
    ArrayList<Integer> blockedPixelValues;

    static final int WALL_COLOR = 0;
    static final int BACKGROUND_COLOR = 255;
    static final int START_COLOR = 60;
    static final int OBSTACLE_COLOR = 180;
    static final int TARGET_COLOR = 120;
    static final int LUT_COLOR = 201;

    static final double EPSILON = 0.000001;

    public static class DistanceMapScaler {

        // scales the provided distance map to the specified [0;200] interval
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
                    if(!Double.isInfinite(distanceMap[j][i]) && !(distanceMap[j][i] == WALL_COLOR)) {
                        double normalizedValue = (distanceMap[j][i] - min) / (max - min);
                        scaledMap[j][i] = (int) (normalizedValue * 200);
                    }
                    else {
                        scaledMap[j][i] = 255;
                    }
                }
            }
            return scaledMap;
        }
    }

    // helper class for storing Positions to help with distance map calculations
    public class Position {

        public final int xPos;
        public final int yPos;

        public Position(int xPos, int yPos) {
            this.xPos = xPos;
            this.yPos = yPos;
        }

        @Override
        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = prime * result + this.xPos;
            result = prime * result + this.yPos;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Position))
                return false;
            if (this == obj) return true;
            Position other = (Position) obj;
            return this.xPos == other.xPos && this.yPos == other.yPos;
        }
    }


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

    // Calculates the moving costs of a move
    // dx, dy: Distance in x and y coordinate respectively
    // modes: Euclidean, Chebyshev(Chessboard), Manhattan
    double costsBetweenPositions(int dx, int dy, String mode) {
        double costs = 0;
        if(!(dx == 0 && dy == 0)) {
            switch(mode) {
                case "Euclidean":
                    if(Math.abs(dx) > 0 && Math.abs(dy) > 0)
                        costs = Math.sqrt(dx*dx + dy*dy); // move diagonally
                    else
                        costs = Math.max(Math.abs(dx), Math.abs(dy)); // go the one direction there was a value for
                    break;
                case "Chebyshev":
                    costs = Math.max(Math.abs(dx), Math.abs(dy));
                    break;
                case "Manhattan":
                    costs = Math.abs(dx) + Math.abs(dy);
                    break;
            }
        }

         return costs;
    }


    // Returns a Vector<Position> with all the Positions which correspond to the 'initialPositionValue'
    // can e.g. be used to get all targetPixels in the picture which one can be taken from as a starting point
    // for distance map calculation
    public Vector<Position> getInitialPositionsFromValue(int initialPositionValue) {
        Vector<Position> positions = new Vector<>();
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (this.inDataArrInt[j][i] == initialPositionValue) {
                    positions.add(new Position(j, i));
                }
            }
        }
        return positions;
    }


    // calculates the distance map based on an initialPosition with a user-defined mode
    // the printUpdates determines whether the number of updates will be printed
    public double[][] getDistanceMap(Position initialPosition, int initialPositionValue, String mode, boolean printUpdates) {

        ArrayDeque<Position> openSet = new ArrayDeque<>();
        HashSet<Position> visitedPixels = new HashSet<>();

        if(initialPosition == null)
            throw new RuntimeException("no target position exists");

        double[][] distanceMap = new double[this.width][this.height];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (this.inDataArrInt[j][i] == initialPositionValue) {
                    distanceMap[j][i] = 0.0;
                } else {
                    distanceMap[j][i] = Double.POSITIVE_INFINITY;
                }
            }
        }

        openSet.add(initialPosition);
        visitedPixels.add(initialPosition);

        int updates = 0;

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
                            double tentativeScore = distanceMap[current.xPos][current.yPos] + costsBetweenPositions(dx, dy, mode);
                            if (tentativeScore < distanceMap[neighbor.xPos][neighbor.yPos]) {
                                distanceMap[neighbor.xPos][neighbor.yPos] = tentativeScore;
                                updates++;
                            }
                        }
                    }
                }

            }
        }
        if(printUpdates) {
            System.out.println("Number of updates: " + updates);
        }

        return distanceMap;
    }

    // calculates the shortest path through the distance map by always choosing the smallest next neighbor
    Vector<Position> calculateShortestPath(double[][] distanceMap) {

        Vector<Position> resultPath = new Vector<>();
        Position startPosition = null;

        // find start point
        startPosition = getInitialPositionsFromValue(START_COLOR).get(0);
        if(startPosition == null) {
            throw new RuntimeException("No startposition found in image");
        }

        resultPath.add(startPosition);
        Position current = startPosition;
        Position bestNeighbor = null;

        double smallestNeighborDistanceToGoal = distanceMap[startPosition.xPos][startPosition.yPos];
        double neighborDistanceToGoal;

        while(smallestNeighborDistanceToGoal > EPSILON) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if ((dx != 0 || dy != 0) && isAllowedMove(current, dx, dy)) {    // exclude non - moves
                        neighborDistanceToGoal = distanceMap[current.xPos + dx][current.yPos + dy];
                        if (neighborDistanceToGoal < smallestNeighborDistanceToGoal) {
                            bestNeighbor = new Position(current.xPos + dx, current.yPos + dy);
                            smallestNeighborDistanceToGoal = neighborDistanceToGoal;
                        }
                    }
                }
            }
            resultPath.add(bestNeighbor);
            current = bestNeighbor;
        }

        return resultPath;
    }

    // draws the shortestPast on a given image-array
    int[][] plotShortestPath(int [][] originalMaze, Vector<Position> shortestPath) {
        int [][] resultMap = new int[originalMaze.length][];
        for(int i = 0; i < originalMaze.length; i++) {
            resultMap[i] = originalMaze[i].clone();
        }

        for(Position position : shortestPath) {
            resultMap[position.xPos][position.yPos] = LUT_COLOR;
        }

        return resultMap;
    }

    // calculates the path length of the shortestPath
    // note:
    //      this could have been simply a printout of the first 'smallestNeighborDistanceToGoal'
    //      from the calculateShortestPath() method, but to avoid side-effects the distance is calculated here again
    double calculateOverallPath(Vector<Position> shortestPath,  String mode) {
        double overallCost = 0;

        Iterator<Position> iterator = shortestPath.iterator();

        if(iterator.hasNext()) {
            Position previousPosition = iterator.next();

            while(iterator.hasNext()) {
                Position currentPosition = iterator.next();
                overallCost += costsBetweenPositions(currentPosition.xPos - previousPosition.xPos,
                        currentPosition.yPos -previousPosition.yPos, mode);
                previousPosition = currentPosition;
            }
        }
        return overallCost;
    }

    // calculates a cumulated distance map from all possible starting points of an obstacle
    // first, individual distance maps are calculated per starting point
    // then, one distance map aggregates all those distance maps by the following logic
    // if a distance in a new map is smaller than the currently-cumulated distance map, updated that distance
    // this effectively calculates how far from an object one can travel
    // subsequently this is used to establish the safetyDistance (all points that reach the safety distance limit
    // will be blocked pixels)
    double[][] getObstacleMap(int initialPositionValue, String mode) {
        Vector<double[][]> allObstacleMaps = new Vector<>();
        Vector<Position> allStartingPositions = getInitialPositionsFromValue(initialPositionValue);

        // used for progress bar calculations
        double displayIncrement = 2.0; // 2% per increment
        double completionPercentage = 0.0;
        int printedIncrements = 0;
        System.out.print("Calculating Distance Maps: [");

        for (Position startingPosition : allStartingPositions) {
            allObstacleMaps.add(getDistanceMap(startingPosition, initialPositionValue, mode, false));

            completionPercentage += 100.0 / allStartingPositions.size();
            while (completionPercentage >= printedIncrements * displayIncrement + displayIncrement) {
                System.out.print("#");
                printedIncrements++;
            }
        }
        System.out.println("]");

        double[][] intermediaryMap = new double[this.width][this.height];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (this.inDataArrInt[j][i] == initialPositionValue) {
                    intermediaryMap[j][i] = 0.0;
                } else {
                    intermediaryMap[j][i] = Double.POSITIVE_INFINITY;
                }
            }
        }

        for (double[][] map : allObstacleMaps) {
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    if(map[j][i] < intermediaryMap[j][i])
                        intermediaryMap[j][i] = map[j][i];
                }
            }
        }

        return intermediaryMap;
    }

    // returns all Positions than can be reached from an obstacle by moving a certain distance (=safetyDistance)
    Vector<Position> filterOutObstacles(double[][] obstacleMap, double safetyDistance) {
        Vector<Position> obstacles = new Vector<>();
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if(obstacleMap[j][i] < safetyDistance) {
                    obstacles.add(new Position(j, i));
                }
            }
        }
        return obstacles;
    }


    public void run(ImageProcessor ip) {

        this.blockedPixelValues = new ArrayList<>();
        this.blockedPixelValues.add((Integer)WALL_COLOR);

        // task (2,3)
        GenericDialog gd = new GenericDialog("Metrik");
        String[] choices = new String[] {"Euclidean", "Chebyshev", "Manhattan"};

        gd.addChoice("Metrik", choices, choices[0]);
        gd.showDialog();
        if(gd.wasCanceled()) {
            return;
        }
        String mode = gd.getNextChoice();

        byte[] pixels = (byte[])ip.getPixels();
        this.width = ip.getWidth();
        this.height = ip.getHeight();
        this.inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, this.width, this.height);


        // tasks (1,1), (1,2) - calculate distance map and print out the number of update operations
        Vector<Position> initialPositionsGoal = getInitialPositionsFromValue(TARGET_COLOR);
        double[][] distanceMap = getDistanceMap(initialPositionsGoal.elementAt(0), TARGET_COLOR, mode, true);

        // task (1,3)
        int[][] scaledMap = DistanceMapScaler.scaleToImageInterval(distanceMap, this.height, this.width);
        int[][] distanceMapWithoutOverflow = new int[this.width][this.height];
        for(int i = 0; i < this.height; i++) {
            for(int j = 0; j < this.width; j++) {
                distanceMapWithoutOverflow[j][i] = Math.min((int)distanceMap[j][i], 255);
            }
        }
        ImageJUtility.showNewImage(scaledMap, this.width, this.height, "Scaled Distance Map [0;200]");
        ImageJUtility.showNewImage(distanceMapWithoutOverflow, this.width, this.height, "Distance Map Without Overflow");


        // task (2,1)
        Vector<Position> shortestPath = calculateShortestPath(distanceMap);
        int[][] resultMap = plotShortestPath(scaledMap, shortestPath);

        // task (2,2)
        byte[] resultImageBytes = ImageJUtility.convertFrom2DIntArr(resultMap, this.width, this.height);
        ImageProcessor imageProcessor = new ByteProcessor(this.width, this.height, resultImageBytes, null);
        imageProcessor.setLut(LutLoader.openLut("./luts/mazeLUT.lut"));
        ImagePlus resultImagePlus = new ImagePlus("Distance map with shortest path", imageProcessor);
        resultImagePlus.show();

        // tasks (3,1), (3,2)
        Vector<Position> initialPositionsObstacles = getInitialPositionsFromValue(OBSTACLE_COLOR);
        double[][] obstacleDistanceMap = getObstacleMap(OBSTACLE_COLOR, mode);
        int[][] scaledObstacleMap = DistanceMapScaler.scaleToImageInterval(obstacleDistanceMap, this.height, this.width);
        ImageJUtility.showNewImage(scaledObstacleMap, this.width, this.height, "Scaled Obstacle Map");

        // task (3,3)
        this.blockedPixelValues.add((Integer)OBSTACLE_COLOR);
        Vector<Position> obstacles = filterOutObstacles(obstacleDistanceMap, 4.0);
        for(Position obstacle : obstacles) {
            this.inDataArrInt[obstacle.xPos][obstacle.yPos] = OBSTACLE_COLOR;
        }
        double[][] combinedDistanceMap = getDistanceMap(initialPositionsGoal.elementAt(0), TARGET_COLOR, mode, true);
        Vector<Position> shortestPathAroundObstacles = calculateShortestPath(combinedDistanceMap);
        int[][] resultMapObstacles = plotShortestPath(this.inDataArrInt, shortestPathAroundObstacles);

        byte[] resultImageBytesObstacles = ImageJUtility.convertFrom2DIntArr(resultMapObstacles, this.width, this.height);
        ImageProcessor imageProcessorObstacles = new ByteProcessor(this.width, this.height, resultImageBytesObstacles, null);
        imageProcessorObstacles.setLut(LutLoader.openLut("./luts/mazeLUT.lut"));
        ImagePlus resultImagePlusObstacles = new ImagePlus("Original image with shortest path (Obstacles considered)", imageProcessorObstacles);
        resultImagePlusObstacles.show();

        System.out.printf("%s%.3f\n", "Length of path (without Obstacles): ", calculateOverallPath(shortestPath, mode));
        System.out.printf("%s%.3f\n", "Length of path (with    Obstacles): ", calculateOverallPath(shortestPathAroundObstacles, mode));

    } //run

    void showAbout() {
        IJ.showMessage("About DistanceMap_...",
                "Creates a DistanceMap for Path Finding\n");
    } //showAbout


} //class DistanceMap_