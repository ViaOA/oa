package test.homework.aaron;
import java.util.Scanner;

public class Homework821 {

    
    public static void test() {

        // Input option
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of cities: ");
        final int amt = sc.nextInt();
        
        System.out.println("Enter the coordinates of the cities: ");
        double[] xs = new double[amt];
        double[] ys = new double[amt];
        
        for (int i=0; i<amt; i++) {
            xs[i] = sc.nextDouble();
            ys[i] = sc.nextDouble();
        }

        int pos = 0;
        double minTot = 0;
        for (int i=0; i<amt; i++) {
            double tot = .0d;
            for (int j=0; j<amt; j++) {
                if (i == j) continue;
                double x = Math.abs(xs[i] - xs[j]);
                double y = Math.abs(ys[i] - ys[j]);
                double c = Math.sqrt((x*x) + (y*y));
                tot += c;
            }            
            if (minTot == 0 || tot < minTot) {
                minTot = tot;
                pos = i;
            }
        }
        System.out.printf("The central city is at %.2f, %.2f\n", xs[pos], ys[pos]);
        System.out.printf("The total distance to all other cities is %.2f\n", minTot);
        
    }
    
 
    public static void main(String[] args) {
        test();
    }
    
}



            