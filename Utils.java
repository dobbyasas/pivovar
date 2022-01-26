package cz.jahodyspeprem.pivovar;

import java.util.Random;

public class Utils {

    private static Random r = new Random();

    public static int randomNumber(int bound) {
        return r.nextInt(bound);
    }

    public static boolean oneIn(int i) {
        return r.nextInt(i) == 0;
    }

    public static String longToTime(long l) {
        long hours = l / 60;
        long minutes = l % 60;

        return hours + ":" + minutes;
    }
}
