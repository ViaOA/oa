package test.homework.aaron;
import java.util.Scanner;

public class Homework837 {

    
    public static void test() {
        String[][] data = new String[50][2];
        // have states and captitals put in data
        
        // Input option
        Scanner sc = new Scanner(System.in);
        int cntCorrect = 0;
        for (int i=0; i<50;i++) {
            System.out.printf("\nWhat is the captial of %s ? ", data[i][0]);
            String s = sc.next();
            System.out.printf("\nthe correct answer should be %s ? ", data[i][1]);
            if (s != null && s.equalsIgnoreCase(data[i][1])) cntCorrect++;
        }
        System.out.printf("\nThe correct count is %d\n", cntCorrect);
    }
 
    public static void main(String[] args) {
        test();
    }
}



            