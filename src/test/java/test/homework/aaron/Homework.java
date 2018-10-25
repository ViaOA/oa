package test.homework.aaron;
import java.util.Scanner;

public class Homework {

    
    public static void sumColumnTest() {
        Scanner sc = new Scanner(System.in);
        
        final int maxRows = 3;
        final int maxCols = 4;
        
        double[][] ds = new double[maxRows][maxCols];
        
        System.out.printf("\nEnter a %d-by%d matrix row by row:", maxRows, maxCols);
        for (int r=0; r < maxRows; r++) {
            System.out.printf("\n%d) Enter the next %d numbers and hit enter: ", r+1, maxCols);
            
            for (int c=0; c < maxCols; c++) {
                if (!sc.hasNext()) break;
                ds[r][c] = sc.nextDouble();
            }
        }
    
        for (int c=0; c < maxCols; c++) {
            double d = sumColumn(ds, c);
            System.out.printf("\nSum of elements at column %d is %.2f", c, d);
        }
    }
    
 
    public static double sumColumn(double[][] m, int columnIndex) {
        if (m == null || m.length == 0) return .0d;
        double dx = 0.0;
        for (int r=0; r<m.length; r++) {
            double[] col = m[r];
            if (columnIndex >= col.length) continue;
            dx += col[columnIndex];
        }
        return dx;
    }

    
    
    public static void sumColumnTest2() {
        System.out.println("Simple test");
        double[][] ds = new double[][] {{1,2,3,4}, {1.1, 2.1, 3.1, 4.1}, {1.2, 2.2, 3.2, 4.2}};
        for (int c=0; c<4; c++) System.out.printf("column:%d = %.2f\n", c, sumColumn(ds, c));
        
        ds = new double[3][4];
        System.out.println("Empty array");
        for (int c=0; c<4; c++) System.out.printf("column:%d = %.2f\n", c, sumColumn(ds, c));
        
        for (int r=0; r<3; r++) {
            for (int c=0; c<4; c++) {
                ds[r][c] = Math.random();
            }
        }
        System.out.println("Random array");
        for (int c=0; c<4; c++) System.out.printf("column:%d = %.2f\n", c, sumColumn(ds, c));
            
        sumColumnTest();
    }
    
    public static void main(String[] args) {
        sumColumnTest();
    }
    
}



            