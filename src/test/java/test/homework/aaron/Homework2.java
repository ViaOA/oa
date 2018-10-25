package test.homework.aaron;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;

public class Homework2 {
    final static int maxRows = 3;
    final static int maxCols = 3;

    
    public static double[][] getDoubles() {
        double[][] doubles = new double[maxRows][maxCols];

        // generate random data
        for (int r=0; r<maxRows;r++) {
            for (int c=0; c < maxCols; c++) {
                doubles[r][c] = (Math.random() * 100);
            }
        }
        
        /*
        // Input option
        Scanner sc = new Scanner(System.in);
        System.out.printf("\nEnter a %d-by%d matrix row by row:", maxRows, maxCols);
        for (int r=0; r < maxRows; r++) {
            System.out.printf("\n%d) Enter the next %d numbers and hit enter: ", r+1, maxCols);
            
            for (int c=0; c < maxCols; c++) {
                if (!sc.hasNext()) break;
                doubles[r][c] = sc.nextDouble();
            }
        }
        */
        return doubles;
    }
    
    public static void test() {
        
        double[][] doublesA = getDoubles(); 
        double[][] doublesB = getDoubles(); 
        
        if (doublesA.length != doublesB.length) throw new RuntimeException("arrays are not same size");
        
        double[][] doublesC = new double[maxRows][maxCols];
        
        // add A and B into C
        for (int r=0; r<maxRows; r++) {
            for (int c=0; c<maxCols; c++) {
                doublesC[r][c] = doublesA[r][c] + doublesB[r][c]; 
            }
        }
        
        for (int r=0; r<maxRows; r++) {
            String line = "";
            for (int c=0; c<maxCols; c++) {
                line += String.format("%5.1f ", doublesA[r][c]);
            }
            line += " ";
            if (r == 1) line += "+ ";
            else line += "  ";
            
            for (int c=0; c<maxCols; c++) {
                line += String.format("%5.1f ", doublesB[r][c]);
            }
            line += "  ";
            if (r == 1) line += "= ";
            else line += "  ";
            for (int c=0; c<maxCols; c++) {
                line += String.format("%5.1f ", doublesC[r][c]);
            }
            System.out.println(line);
        }
    }
   
    
    public static void main(String[] args) {
        test();
    }
    
}



            