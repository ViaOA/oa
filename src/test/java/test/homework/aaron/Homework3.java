package test.homework.aaron;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;

public class Homework3 {

    
    public static void sumColumnTest() {
        Scanner sc = new Scanner(System.in);
        
        final int maxRows = 8;
        final int maxCols = 7;
        
        int[][] ints = new int[maxRows][maxCols];

        // generate random data
        for (int r=0; r<maxRows;r++) {
            for (int c=0; c < maxCols; c++) {
                ints[r][c] = (int) (Math.random() * 12);
            }
        }
        
        /* Input option
        System.out.printf("\nEnter a %d-by%d matrix row by row:", maxRows, maxCols);
        for (int r=0; r < maxRows; r++) {
            System.out.printf("\n%d) Enter the next %d numbers and hit enter: ", r+1, maxCols);
            
            for (int c=0; c < maxCols; c++) {
                if (!sc.hasNext()) break;
                ints[r][c] = sc.nextInt();
            }
        }
        */
    
        int[] tots = new int[maxRows];
        for (int r=0; r < maxRows; r++) {
            
            int tot = 0;
            for (int c=0; c < maxCols; c++) {
                tot += ints[r][c];
            }
            
            System.out.printf("\nEmployee %d = %d", r, tot);
            tots[r] = tot;
        }
        
        // create a new array that will be sorted
        int[] sortedTots = new int[maxRows];
        // copy the total hours of all emps (tots) into the new array
        System.arraycopy(tots, 0, sortedTots,  0, maxRows);
        // sort
        Arrays.sort(sortedTots);
        
        // now, we need to output in descending order (using sortedTots) and match the employee with the hours
        //   by finding the employee hours in the tots array.
        
        // this is needed to "know" if the employee was already listed - in cases where more then one employee has the same tot hours for the week
        boolean[] bs = new boolean[maxRows];
        
        System.out.print("\n\nDescending ...");
        for (int r=maxRows-1; r>=0; r--) {
            for (int i=0; i<maxRows; i++) {
                if (tots[i] == sortedTots[r] && !bs[i]) {
                    bs[i] = true;
                    System.out.printf("\nEmployee %d = %d", i, sortedTots[r]);
                    break;
                }
            }
        }
    }
    
 

    
    
    
    public static void main(String[] args) {
        sumColumnTest();
    }
    
}



            