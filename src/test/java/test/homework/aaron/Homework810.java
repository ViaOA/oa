package test.homework.aaron;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;

public class Homework810 {

    
    public static void test() {
        Scanner sc = new Scanner(System.in);
        
        final int maxRows = 4;
        final int maxCols = 4;
        
        int[][] ints = new int[maxRows][maxCols];

        // generate random data
        for (int r=0; r<maxRows;r++) {
            for (int c=0; c < maxCols; c++) {
                ints[r][c] = (Math.random() < .5 ? 0 : 1);
            }
        }

        // printout the matrix
        for (int r=0; r<maxRows; r++) {
            String line = "";
            for (int c=0; c<maxCols; c++) {
                line += ints[r][c];
            }
            System.out.println(line);;
        }
        
        
        // find largest row(s)
        int max = 0;
        int pos = 0;
        for (int r=0; r<maxRows; r++) {
            int tot = 0;
            for (int c=0; c<maxCols; c++) {
                tot += ints[r][c];
            }            
            if (tot > max) {
                pos = r;
                max = tot;
            }
        }
        System.out.println("The largest row index: "+pos);
        
        
        
        // find largest column(s)
        max = 0;
        pos = 0;
        for (int c=0; c<maxCols; c++) {
            int tot = 0;
            for (int r=0; r<maxRows; r++) {
                tot += ints[r][c];
            }            
            if (tot > max) {
                pos = c;
                max = tot;
            }
        }
        System.out.println("The largest column index: "+pos);
    }
    
 
    public static void main(String[] args) {
        test();
    }
    
}



            