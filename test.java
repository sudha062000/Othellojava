import java.util.Scanner;

public class test {
    public static void main (String args[])
    {

        Scanner scan = new Scanner(System.in);

        int a,b,c,d;

        System.out.println("Enter a value for a ");
        a = scan.nextInt();
        System.out.println("Enter a value for b ");
        b = scan.nextInt();
        System.out.println("Enter a value for c ");
        c = scan.nextInt();
        System.out.println("Enter a value for d ");
        d = scan.nextInt();

        int sum = a+b+c+d;

        System.out.println("total sum : "+sum);



    }
    
}
