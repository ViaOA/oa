package test.homework.aaron;
import java.util.Scanner;

public class Homework828 {

    public static boolean equals(int[][] m1, int[][] m2) {
        if (m1 == m2) return true;
        if (m1 == null || m2 == null) return false;
        if (m1.length != m2.length) return false;
        
        for (int i=0; i<m1.length; i++) {
            if (m1[i].length != m2[i].length) return false;
            if (m1[i] != m2[i]) return false;
        }
        return true;
    }
    
    public static void test() {
        // Input option
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter list1: ");
        int[][] list1 = new int[3][3];
        for (int r=0; r<3; r++) {
            for (int c=0; c<3; c++) {
                list1[r][c] = sc.nextInt();
            }
        }
        System.out.println("Enter list2: ");
        int[][] list2 = new int[3][3];
        for (int r=0; r<3; r++) {
            for (int c=0; c<3; c++) {
                list2[r][c] = sc.nextInt();
            }
        }
        
        boolean b = equals(list1, list2);
        System.out.println("The two arrays are " + (b ? "" : "not ") + "strictly identical");
    }
    
 
    public static void main(String[] args) {
        test();
    }
}



            