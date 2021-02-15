package org.larma;

public class Main {
    public static void main(
            String[] args
            ) throws Exception
    {
        try {
            while (true) {
                new Crawler().run();
                Thread.sleep(100);
            }
        } catch (Exception e) {}
    }
}