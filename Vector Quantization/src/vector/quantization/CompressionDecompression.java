package vector.quantization;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class CompressionDecompression {
    public static int[][] readImage(String imagePath) {
        try {
            File file = new File(imagePath);
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();
            int[][] pixels = new int[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int red = (rgb >> 16) & 0xFF;
                    pixels[y][x] = red;
                }
            }
            return pixels;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[][][] readImageColored(String imagePath) {
        try {
            File file = new File(imagePath);
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();
            int[][][] pixels = new int[height][width][3];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    pixels[y][x][0] = red;
                    pixels[y][x][1] = green;
                    pixels[y][x][2] = blue;
                }
            }
            return pixels;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeImage(int[][] pixels, String outputPath) {
        try {
            int height = pixels.length;
            int width = pixels[0].length;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixelValue = pixels[y][x] << 16 | pixels[y][x] << 8 | pixels[y][x];
                    image.setRGB(x, y, pixelValue);
                }
            }
            ImageIO.write(image, "jpg", new File(outputPath));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeColoredImage(int[][][] pixels, String outputPath) {
        try {
            int height = pixels.length;
            int width = pixels[0].length;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int red = pixels[y][x][0];
                    int green = pixels[y][x][1];
                    int blue = pixels[y][x][2];

                    int pixelValue = (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, pixelValue);
                }
            }

            ImageIO.write(image, "jpg", new File(outputPath));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void notColoredCompress(int[][] pixels, int vectorSize, int numberOfVectorsInCodeBook) {
        List<int[][]> vectors = new ArrayList<>();
        for (int i = 0; i < pixels.length; i += vectorSize) {
            for (int j = 0; j < pixels[0].length; j += vectorSize) {
                int[][] vector = new int[vectorSize][vectorSize];
                for (int k = 0; k < vectorSize; k++) {
                    for (int l = 0; l < vectorSize; l++) {
                        vector[k][l] = pixels[i + k][j + l];
                    }
                }
                vectors.add(vector);
            }
        }
        Map<int[][], List<int[][]>> codeBook = new HashMap<>();
        float[][] average = new float[vectorSize][vectorSize];
        for (int i = 0; i < vectors.size(); i++) {
            int[][] vector = vectors.get(i);
            for (int j = 0; j < vectorSize; j++) {
                for (int k = 0; k < vectorSize; k++) {
                    average[j][k] += vector[j][k];
                }
            }
        }
        for (int i = 0; i < vectorSize; i++) {
            for (int j = 0; j < vectorSize; j++) {
                average[i][j] /= vectors.size();
            }
        }
        int[][] floor = new int[vectorSize][vectorSize];
        int[][] ceil = new int[vectorSize][vectorSize];
        for (int i = 0; i < vectorSize; i++) {
            for (int j = 0; j < vectorSize; j++) {
                ceil[i][j] = (int) Math.floor(average[i][j] - 1);
                floor[i][j] = (int) Math.floor(average[i][j] + 1);
            }
        }
        codeBook.put(ceil, new ArrayList<>());
        codeBook.put(floor, new ArrayList<>());
        codeBook = suitableCentriods(codeBook, vectors);
        while (codeBook.size() < numberOfVectorsInCodeBook) {
            codeBook = split(codeBook, vectorSize);
            codeBook = suitableCentriods(codeBook, vectors);
        }
        codeBook = suitableCentriods(codeBook, vectors);
        ArrayList<Integer> compressedList = generateStream(vectors, codeBook);
        writeToFileAsCompressed(codeBook, compressedList, vectorSize, pixels.length, pixels[0].length);
    }


    public static Map<int[][], List<int[][]>> suitableCentriods(Map<int[][], List<int[][]>> codeBook, List<int[][]> vectors) {
        for (int i = 0; i < vectors.size(); i++) {
            int[][] vector = vectors.get(i);
            int minDistance = Integer.MAX_VALUE;
            int[][] minCentroid = null;
            for (int[][] centroid : codeBook.keySet()) {
                int distance = 0;
                for (int j = 0; j < vector.length; j++) {
                    for (int k = 0; k < vector[0].length; k++) {
                        distance += Math.abs(vector[j][k] - centroid[j][k]);
                    }
                }
                if (distance < minDistance) {
                    minDistance = distance;
                    minCentroid = centroid;
                }
            }
            codeBook.get(minCentroid).add(vector);
        }
        return codeBook;
    }

    public static Map<int[][], List<int[][]>> split(Map<int[][], List<int[][]>> codeBook, int vectorSize) {
        Map<int[][], List<int[][]>> newCodeBook = new HashMap<>();
        for (int[][] centroid : codeBook.keySet()) {
            float[][] average = new float[vectorSize][vectorSize];
            for (int i = 0; i < codeBook.get(centroid).size(); i++) {
                int[][] vector = codeBook.get(centroid).get(i);
                for (int j = 0; j < vectorSize; j++) {
                    for (int k = 0; k < vectorSize; k++) {
                        average[j][k] += vector[j][k];
                    }
                }
            }
            for (int i = 0; i < vectorSize; i++) {
                for (int j = 0; j < vectorSize; j++) {
                    average[i][j] /= codeBook.get(centroid).size();
                }
            }
            int[][] ceil = new int[vectorSize][vectorSize];
            int[][] floor = new int[vectorSize][vectorSize];
            for (int i = 0; i < vectorSize; i++) {
                for (int j = 0; j < vectorSize; j++) {
                    ceil[i][j] = (int) Math.floor(average[i][j] - 1);
                    floor[i][j] = (int) Math.floor(average[i][j] + 1);
                }
            }
            newCodeBook.put(ceil, new ArrayList<>());
            newCodeBook.put(floor, new ArrayList<>());
        }
        return newCodeBook;
    }

    public static ArrayList<Integer> generateStream(List<int[][]> vectors, Map<int[][], List<int[][]>> codeBook) {
        List<int[][]> centroids = codeBook.keySet().stream().toList();
        ArrayList<Integer> compressedList = new ArrayList<>();
        for (int[][] vector : vectors) {
            int[][] centroid = findCentroid(vector, codeBook);
            compressedList.add(centroids.indexOf(centroid));

        }
        return compressedList;
    }

    private static int[][] findCentroid(int[][] vector, Map<int[][], List<int[][]>> codeBook) {
        for (Map.Entry<int[][], List<int[][]>> entry : codeBook.entrySet()) {
            if (entry.getValue().contains(vector)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void writeToFileAsCompressed(Map<int[][], List<int[][]>> codeBook, ArrayList<Integer> compressedList, int sizeOfVector, int height, int width) {
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream("compressed.bin"))) {
            outputStream.writeByte(sizeOfVector);
            outputStream.writeByte(codeBook.size());
            for (Map.Entry<int[][], List<int[][]>> entry : codeBook.entrySet()) {
                int[][] centroid = entry.getKey();
                for (int i = 0; i < centroid.length; i++) {
                    for (int j = 0; j < centroid[0].length; j++) {
                        outputStream.writeByte(centroid[i][j]);
                    }
                }
            }
            outputStream.writeShort(height);
            outputStream.writeShort(width);
            for (int i = 0; i < compressedList.size(); i++) {
                outputStream.writeByte(compressedList.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFromCompressed(String filePath) {
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(filePath))) {
            int sizeOfVector = inputStream.readUnsignedByte();
            int sizeOfCodeBook = inputStream.readUnsignedByte();
            ArrayList<int[][]> codeBook = new ArrayList<>();
            for (int i = 0; i < sizeOfCodeBook; i++) {
                int[][] centroid = new int[sizeOfVector][sizeOfVector];
                for (int j = 0; j < sizeOfVector; j++) {
                    for (int k = 0; k < sizeOfVector; k++) {
                        centroid[j][k] = inputStream.readUnsignedByte();
                    }
                }
                codeBook.add(centroid);
            }
            int height = inputStream.readUnsignedShort();
            int width = inputStream.readUnsignedShort();
            ArrayList<Integer> compressedList = new ArrayList<>();
            for (int i = 0; i < height * width; i++) {
                compressedList.add((int) inputStream.readByte());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

class Main {
    public static void main(String[] args) {
        //int[][] pixels = new int[6][6];pixels[0][0] = 1;pixels[0][1] = 2;pixels[0][2] = 7;pixels[0][3] = 9;pixels[0][4] = 4;pixels[0][5] = 11;pixels[1][0] = 3;pixels[1][1] = 4;pixels[1][2] = 6;pixels[1][3] = 6;pixels[1][4] = 12;pixels[1][5] = 12;pixels[2][0] = 4;pixels[2][1] = 9;pixels[2][2] = 15;pixels[2][3] = 14;pixels[2][4] = 9;pixels[2][5] = 9;pixels[3][0] = 10;pixels[3][1] = 10;pixels[3][2] = 20;pixels[3][3] = 18;pixels[3][4] = 8;pixels[3][5] = 8;pixels[4][0] = 4;pixels[4][1] = 3;pixels[4][2] = 17;pixels[4][3] = 16;pixels[4][4] = 1;pixels[4][5] = 4;pixels[5][0] = 4;pixels[5][1] = 5;pixels[5][2] = 18;pixels[5][3] = 18;pixels[5][4] = 5;pixels[5][5] = 6;
        int[][] image = CompressionDecompression.readImage("C:\\Users\\Ziad Ayman\\Desktop\\giraffe-Gray.bmp");
        CompressionDecompression.notColoredCompress(image, 2, 4);

    }
}
