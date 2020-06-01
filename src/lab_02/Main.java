package lab_02;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


class MultiplierThread extends Thread
{

    private final int[][] firstMatrix;
    private final int[][] secondMatrix;
    private final int[][] resultMatrix;
    private final int firstIndex;
    private final int lastIndex;
    private final int sumLength;

    public MultiplierThread(final int[][] firstMatrix,
                            final int[][] secondMatrix,
                            final int[][] resultMatrix,
                            final int firstIndex,
                            final int lastIndex)
    {
        this.firstMatrix  = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.resultMatrix = resultMatrix;
        this.firstIndex   = firstIndex;
        this.lastIndex    = lastIndex;

        sumLength = secondMatrix.length;
    }

    private void calcValue(final int row, final int col)
    {
        int sum = 0;
        for (int i = 0; i < sumLength; ++i)
            sum += firstMatrix[row][i] * secondMatrix[i][col];
        resultMatrix[row][col] = sum;
    }

    @Override
    public void run()
    {
        System.out.println("Thread " + getName() + " started. Calculating cells from " + firstIndex + " to " + lastIndex + "...");

        final int colCount = secondMatrix[0].length;
        for (int index = firstIndex; index < lastIndex; ++index)
            calcValue(index / colCount, index % colCount);

        System.out.println("Thread " + getName() + " finished.");
    }
}

class Main
{
    private static void randomMatrix(final int[][] matrix)
    {
        final Random random = new Random();

        for (int row = 0; row < matrix.length; ++row)
            for (int col = 0; col < matrix[row].length; ++col)
                matrix[row][col] = random.nextInt(100);
    }



    private static void printMatrix(final FileWriter fileWriter,
                                    final int[][] matrix) throws IOException
    {
        boolean hasNegative = false;
        int     maxValue    = 0;      for (final int[] row : matrix) {
            for (final int element : row) {
                int temp = element;
                if (element < 0) {
                    hasNegative = true;
                    temp = -temp;
                }
                if (temp > maxValue)
                    maxValue = temp;
            }
        }


        int len = Integer.toString(maxValue).length() + 1;
        if (hasNegative)
            ++len;


        final String formatString = "%" + len + "d";


        for (final int[] row : matrix) {
            for (final int element : row)
                fileWriter.write(String.format(formatString, element));

            fileWriter.write("\n");
        }
    }

    private static void printAllMatrix(final String fileName,
                                       final int[][] firstMatrix,
                                       final int[][] secondMatrix,
                                       final int[][] resultMatrix)
    {
        try (final FileWriter fileWriter = new FileWriter(fileName, false)) {
            fileWriter.write("First matrix:\n");
            printMatrix(fileWriter, firstMatrix);

            fileWriter.write("\nSecond matrix:\n");
            printMatrix(fileWriter, secondMatrix);

            fileWriter.write("\nResult matrix:\n");
            printMatrix(fileWriter, resultMatrix);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int[][] multiplyMatrix(final int[][] firstMatrix,
                                          final int[][] secondMatrix)
    {
        final int rowCount = firstMatrix.length;
        final int colCount = secondMatrix[0].length;
        final int sumLength = secondMatrix.length;
        final int[][] result = new int[rowCount][colCount];

        for (int row = 0; row < rowCount; ++row) {
            for (int col = 0; col < colCount; ++col) {
                int sum = 0;
                for (int i = 0; i < sumLength; ++i)
                    sum += firstMatrix[row][i] * secondMatrix[i][col];
                result[row][col] = sum;
            }
        }

        return result;
    }


    private static int[][] multiplyMatrixMT(final int[][] firstMatrix,
                                            final int[][] secondMatrix,
                                            int threadCount)
    {
        assert threadCount > 0;

        final int rowCount = firstMatrix.length;
        final int colCount = secondMatrix[0].length;
        final int[][] result = new int[rowCount][colCount];

        final int cellsForThread = (rowCount * colCount) / threadCount;
        int firstIndex = 0;
        final MultiplierThread[] multiplierThreads = new MultiplierThread[threadCount];


        for (int threadIndex = threadCount - 1; threadIndex >= 0; --threadIndex) {
            int lastIndex = firstIndex + cellsForThread;
            if (threadIndex == 0) {

                lastIndex = rowCount * colCount;
            }
            multiplierThreads[threadIndex] = new MultiplierThread(firstMatrix, secondMatrix, result, firstIndex, lastIndex);
            multiplierThreads[threadIndex].start();
            firstIndex = lastIndex;
        }


        try {
            for (final MultiplierThread multiplierThread : multiplierThreads)
                multiplierThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }


    final static int FIRST_MATRIX_ROWS  = 1000;

    final static int FIRST_MATRIX_COLS  = 1000;

    final static int SECOND_MATRIX_ROWS = FIRST_MATRIX_COLS;

    final static int SECOND_MATRIX_COLS = 1000;

    public static void main(String[] args)
    {
        final int[][] matrix1  = new int[FIRST_MATRIX_ROWS][FIRST_MATRIX_COLS];
        final int[][] matrix2 = new int[SECOND_MATRIX_ROWS][SECOND_MATRIX_COLS];

        randomMatrix(matrix1);
        randomMatrix(matrix2);

        final int[][] resultMatrixMT = multiplyMatrixMT(matrix1, matrix2, Runtime.getRuntime().availableProcessors());


        final int[][] resultMatrix = multiplyMatrix(matrix1, matrix2);

        for (int row = 0; row < FIRST_MATRIX_ROWS; ++row) {
            for (int col = 0; col < SECOND_MATRIX_COLS; ++col) {
                if (resultMatrixMT[row][col] != resultMatrix[row][col]) {
                    System.out.println("Error in multithreaded calculation!");
                    return;
                }
            }
        }

        printAllMatrix("Matrix.txt", matrix1, matrix2, resultMatrixMT);
    }
}