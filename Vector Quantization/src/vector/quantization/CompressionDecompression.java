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
    public void decompress(String filePathForCompressed,String filePathForDecompressed) {
        ArrayList<int[][]> codeBook = new ArrayList<>();
        ArrayList<Integer> compressedList = new ArrayList<>();
        int height = 0;
        int width = 0;
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(filePathForCompressed))) {
            int sizeOfVector = inputStream.readUnsignedByte();
            int sizeOfCodeBook = inputStream.readUnsignedByte();
            for (int i = 0; i < sizeOfCodeBook; i++) {
                int[][] centroid = new int[sizeOfVector][sizeOfVector];
                for (int j = 0; j < sizeOfVector; j++) {
                    for (int k = 0; k < sizeOfVector; k++) {
                        centroid[j][k] = inputStream.readUnsignedByte();
                    }
                }
                codeBook.add(centroid);
            }
            height = inputStream.readUnsignedShort();
            width = inputStream.readUnsignedShort();
            for (int i = 0; i < height * width / (sizeOfVector * sizeOfVector); i++) {
                compressedList.add(inputStream.readUnsignedByte());
            }

        } catch (IOException e) {
            System.out.println("Error while reading from file");
        }
        int[][] pixels = new int[height][width];
        int vectorSize = codeBook.get(0).length;
        int index = 0;
        for (int i = 0; i < height; i += vectorSize) {
            for (int j = 0; j < width; j += vectorSize) {
                int[][] vector = codeBook.get(compressedList.get(index));
                index++;
                for (int k = 0; k < vectorSize; k++) {
                    for (int l = 0; l < vectorSize; l++) {
                        pixels[i + k][j + l] = vector[k][l];
                    }
                }
            }
        }
        writeImage(pixels, filePathForDecompressed);
    }
}
class Main {
    public static void main(String[] args) {
        int[][] image = CompressionDecompression.readImage("C:\\Users\\Ziad Ayman\\Desktop\\giraffe-Gray.bmp");
        CompressionDecompression.notColoredCompress(image, 2, 64);
        CompressionDecompression compressionDecompression = new CompressionDecompression();
        compressionDecompression.decompress("compressed.bin","decompressed");
    }
}
