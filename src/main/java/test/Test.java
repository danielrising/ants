package test;

import java.security.SecureRandom;

public class Test {

    private static SecureRandom rand = new SecureRandom();


    public static void main(String[] args) {
        int random = rand.nextInt(5, 10);
        System.out.println(random);
    }
}