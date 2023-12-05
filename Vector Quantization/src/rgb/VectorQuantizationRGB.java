package rgb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class VectorQuantizationRGB {
    public static void compress(int[][][] pixels, int vectorSize, int numberOfVectorsInCodeBook) {
        List<int[][][]> vectors = new ArrayList<>();
        for (int i = 0; i < pixels.length; i += vectorSize) {
            for (int j = 0; j < pixels[0].length; j += vectorSize) {
                int[][][] vector = new int[vectorSize][vectorSize][3];
                for (int k = 0; k < vectorSize; k++) {
                    for (int l = 0; l < vectorSize; l++) {
                        vector[k][l] = pixels[i + k][j + l];
                    }
                }
                vectors.add(vector);
            }
        }
        Map<int[][][], List<int[][][]>> codeBook = new HashMap<>();
        float[][][] average = new float[vectorSize][vectorSize][3];
        for (int i = 0; i < vectors.size(); i++) {
            int[][][] vector = vectors.get(i);
            for (int j = 0; j < vectorSize; j++) {
                for (int k = 0; k < vectorSize; k++) {
                    for (int m = 0; m < 3; m++) {
                        average[j][k][m] += vector[j][k][m];
                    }
                }
            }
        }
        for (int i = 0; i < vectorSize; i++) {
            for (int j = 0; j < vectorSize; j++) {
                for (int m = 0; m < 3; m++) {
                    average[i][j][m] /= vectors.size();
                }
            }
        }
        int[][][] floor = new int[vectorSize][vectorSize][3];
        int[][][] ceil = new int[vectorSize][vectorSize][3];
        for (int i = 0; i < vectorSize; i++) {
            for (int j = 0; j < vectorSize; j++) {
                for (int m = 0; m < 3; m++) {
                    ceil[i][j][m] = (int) Math.floor(average[i][j][m] - 1);
                    floor[i][j][m] = (int) Math.floor(average[i][j][m] + 1);
                }
            }
        }
        codeBook.put(ceil, new ArrayList<>());
        codeBook.put(floor, new ArrayList<>());
        codeBook = suitableCentroids(codeBook, vectors);
        while (codeBook.size() < numberOfVectorsInCodeBook) {
            codeBook = split(codeBook, vectorSize);
            codeBook = suitableCentroids(codeBook, vectors);
        }
        codeBook = suitableCentroids(codeBook, vectors);
        ArrayList<Integer> compressedList = generateStream(vectors, codeBook);
        writeToFileAsCompressed(codeBook, compressedList, vectorSize, pixels.length, pixels[0].length);
    }
    public static ArrayList<Integer> generateStream(List<int[][][]> vectors, Map<int[][][], List<int[][][]>> codeBook) {
        List<int[][][]> centroids = new ArrayList<>(codeBook.keySet());
        ArrayList<Integer> compressedList = new ArrayList<>();
        for (int[][][] vector : vectors) {
            int[][][] centroid = findCentroid(vector, codeBook);
            compressedList.add(centroids.indexOf(centroid));
        }
        return compressedList;
    }

    private static int[][][] findCentroid(int[][][] vector, Map<int[][][], List<int[][][]>> codeBook) {
        for (Map.Entry<int[][][], List<int[][][]>> entry : codeBook.entrySet()) {
            if (entry.getValue().contains(vector)) {
                return entry.getKey();
            }
        }
        return null;
    }
    public static void writeToFileAsCompressed(Map<int[][][], List<int[][][]>> codeBook, ArrayList<Integer> compressedList, int sizeOfVector, int height, int width) {
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream("compressed.bin"))) {
            outputStream.writeByte(sizeOfVector);
            outputStream.writeByte(codeBook.size());
            for (Map.Entry<int[][][], List<int[][][]>> entry : codeBook.entrySet()) {
                int[][][] centroid = entry.getKey();
                for (int i = 0; i < centroid.length; i++) {
                    for (int j = 0; j < centroid[0].length; j++) {
                        for (int m = 0; m < 3; m++) {
                            outputStream.writeByte(centroid[i][j][m]);
                        }
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
    public static Map<int[][][], List<int[][][]>> suitableCentroids(Map<int[][][], List<int[][][]>> codeBook, List<int[][][]> vectors) {
        for (int i = 0; i < vectors.size(); i++) {
            int[][][] vector = vectors.get(i);
            int minDistance = Integer.MAX_VALUE;
            int[][][] minCentroid = null;
            for (int[][][] centroid : codeBook.keySet()) {
                int distance = 0;
                for (int j = 0; j < vector.length; j++) {
                    for (int k = 0; k < vector[0].length; k++) {
                        for (int m = 0; m < 3; m++) {
                            distance += Math.abs(vector[j][k][m] - centroid[j][k][m]);
                        }
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
    public static Map<int[][][], List<int[][][]>> split(Map<int[][][], List<int[][][]>> codeBook, int vectorSize) {
        Map<int[][][], List<int[][][]>> newCodeBook = new HashMap<>();
        for (int[][][] centroid : codeBook.keySet()) {
            float[][][] average = new float[vectorSize][vectorSize][3];
            for (int i = 0; i < codeBook.get(centroid).size(); i++) {
                int[][][] vector = codeBook.get(centroid).get(i);
                for (int j = 0; j < vectorSize; j++) {
                    for (int k = 0; k < vectorSize; k++) {
                        for (int m = 0; m < 3; m++) {
                            average[j][k][m] += vector[j][k][m];
                        }
                    }
                }
            }
            for (int i = 0; i < vectorSize; i++) {
                for (int j = 0; j < vectorSize; j++) {
                    for (int m = 0; m < 3; m++) {
                        average[i][j][m] /= codeBook.get(centroid).size();
                    }
                }
            }
            int[][][] ceil = new int[vectorSize][vectorSize][3];
            int[][][] floor = new int[vectorSize][vectorSize][3];
            for (int i = 0; i < vectorSize; i++) {
                for (int j = 0; j < vectorSize; j++) {
                    for (int m = 0; m < 3; m++) {
                        ceil[i][j][m] = (int) Math.floor(average[i][j][m] - 1);
                        floor[i][j][m] = (int) Math.floor(average[i][j][m] + 1);
                    }
                }
            }
            newCodeBook.put(ceil, new ArrayList<>());
            newCodeBook.put(floor, new ArrayList<>());
        }
        return newCodeBook;
    }
    public static void decompress(String filePathForCompressed, String filePathForDecompressed) {
        ArrayList<int[][][]> codeBook = new ArrayList<>();
        ArrayList<Integer> compressedList = new ArrayList<>();
        int height = 0;
        int width = 0;
        int sizeOfVector = 0;
        int sizeOfCodeBook = 0;
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(filePathForCompressed))) {
            sizeOfVector = inputStream.readUnsignedByte();
            sizeOfCodeBook = inputStream.readUnsignedByte();
            for (int i = 0; i < sizeOfCodeBook; i++) {
                int[][][] centroid = new int[sizeOfVector][sizeOfVector][3];
                for (int j = 0; j < sizeOfVector; j++) {
                    for (int k = 0; k < sizeOfVector; k++) {
                        for (int m = 0; m < 3; m++) {
                            centroid[j][k][m] = inputStream.readUnsignedByte();
                        }
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

        int[][][] pixels = new int[height][width][3];
        int index = 0;
        for (int i = 0; i < height; i += sizeOfVector) {
            for (int j = 0; j < width; j += sizeOfVector) {
                int[][][] vector = codeBook.get(compressedList.get(index));
                index++;
                for (int k = 0; k < sizeOfVector; k++) {
                    for (int l = 0; l < sizeOfVector; l++) {
                        for (int m = 0; m < 3; m++) {
                            pixels[i + k][j + l][m] = vector[k][l][m];
                        }
                    }
                }
            }
        }
        writeImageRGB(pixels, filePathForDecompressed);
    }
    public static void writeImageRGB(int[][][] pixels, String outputPath) {
        try {
            int height = pixels.length;
            int width = pixels[0].length;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixelValue = (pixels[y][x][0] << 16) | (pixels[y][x][1] << 8) | pixels[y][x][2];
                    image.setRGB(x, y, pixelValue);
                }
            }
            ImageIO.write(image, "jpg", new File(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
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
}

