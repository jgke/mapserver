package fi.paivola.weathermodel;

import java.util.Calendar;

/**
 * Rain class.
 * @author Jaakko Hannikainen
 */
public class Rain {
    public static final double[] rainAvrg = {
        1, 0, 9, 58,  56, 82, 58, 40,  23, 27, 36, 9};
    public static final double[] rainProb = {
        0, 0, 3, 17,  23, 43, 42, 32,  17, 13, 13, 6};
    public static double rain = 0;

    // XXX: quick dumb implementation
    private Rain() {}

    public static double getRain(Calendar date) {
        int month = date.get(Calendar.MONTH);
        double r = 0;
        for(int i = 0; i < 7; i++) {
            if(Math.random() * 100 < rainProb[month])
                r += rainAvrg[month] / date.getActualMaximum(Calendar.DAY_OF_MONTH)
                    + rain / 7;
        }
        if(r <= 0)
            return 0;
        return r + rain;
    }
}
