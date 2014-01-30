package fi.paivola.foodmodel;

import fi.paivola.mapserver.core.DataFrame;
import fi.paivola.mapserver.core.Event;
import fi.paivola.mapserver.core.GameManager;
import fi.paivola.mapserver.core.setting.*;
import fi.paivola.mapserver.utils.*;
import fi.paivola.weathermodel.Weather;
import java.util.Map;
import java.util.Calendar;
/**
 * @author Jaakko Hannikainen
 * Generic class for field products, eg. maize.
 */

public abstract class Crop extends Edible {

    private double waterMinimum;
    private double waterOptimal = 2;
    private double waterMaximum;
    private double temperatureMinimum;
    private double temperatureOptimal = 30;
    private double temperatureMaximum;
    private double sunlightMinimum;
    private double sunlightOptimal;
    private double sunlightMaximum;
    private double pHMinimum;
    private double pHOptimal;
    private double pHMaximum;
    private double maxYield;
    private int growTime;
    private int currentGrowTime;
    private double currentIndexMultiplier;
    private double currentStoredFood;
    private Distribution waterDistribution;
    private Distribution temperatureDistribution;
    private Distribution sunlightDistribution;
    private Distribution pHDistribution;
    private double storedWater;

    /* The constructor is horrible, I'm sorry. */
    public Crop(String name, double wmin, double wopt, double wmax,
            double tmin, double topt, double tmax, double smin, double sopt,
            double smax, double phmin, double phopt, double phmax, int gtime,
            double yield) {
        super(name);
        
        this.name = name;
        waterMinimum = wmin;
        waterOptimal = wopt;
        waterMaximum = wmax;
        temperatureMinimum = tmin;
        temperatureOptimal = topt;
        temperatureMaximum = tmax;
        sunlightMinimum = smin;
        sunlightOptimal = sopt;
        sunlightMaximum = smax;
        pHMinimum = phmin;
        pHOptimal = phopt;
        pHMaximum = phmax;
        growTime = gtime;
        maxYield = yield;
        currentStoredFood = 0;
        currentIndexMultiplier = 0;
        storedWater = 0;
        this.waterDistribution = new Distribution(waterMinimum,
                                                  waterOptimal,
                                                  waterMaximum);
        this.temperatureDistribution = new Distribution(temperatureMinimum,
                                                  temperatureOptimal,
                                                  temperatureMaximum);
        this.sunlightDistribution = new Distribution(sunlightMinimum,
                                                  sunlightOptimal,
                                                  sunlightMaximum);
        this.pHDistribution = new Distribution(pHMinimum,
                                                  pHOptimal,
                                                  pHMaximum);
        
    }

    public void resetCrop() {
        currentGrowTime = 0;
        currentIndexMultiplier = 0;
    }

    @Override
    public double onTick(DataFrame last, DataFrame current) {
        int month = current.getDate().get(Calendar.MONTH);
        if(month == 1 || month == 2 || month == 12)
            resetCrop();

        if(currentGrowTime == growTime) {
            double addfood = currentIndexMultiplier /
                    growTime * getArea() * maxYield * (Math.random() / 10 + 0.5) * 1000 * 15;
            currentStoredFood += addfood;
            resetCrop();
        }
        else {
            currentGrowTime++;
            currentIndexMultiplier += this.getWaterIndex(last) *
                this.getTemperatureIndex(last) * this.getSunshineIndex(last) * 
                this.getPHIndex(last);
        }
        return currentStoredFood;
    }

    @Override
    public void handleEvent(Event e, DataFrame current) {
        switch(e.name) {
            case "flood":
                resetCrop();
                break;
        }
    }
   
    public Supplies harvest(double max) {
        Supplies ret = new Supplies (1, currentStoredFood);
        if(max < 0 || max > ret.amount) {
            currentStoredFood = 0;
            return ret;
        }
        currentStoredFood -= max;
        ret.amount = max;
        return ret;
    }

    public double getCurrentStoredFood() {
        return currentStoredFood;
    }
    
    private double getWaterIndex(DataFrame last) {
        double d = waterDistribution.exact(last.getGlobalDouble("rain"));
        if(d < 1) {
            if(storedWater > d * getArea()) {
                storedWater -= d * getArea();
                return 1;
            }
            else {
                double m = storedWater;
                storedWater = 0;
                return d + storedWater/getArea();
            }
        }
        return d;
    }
    
    private double getTemperatureIndex(DataFrame last) {
        return temperatureDistribution.exact(last.getGlobalDouble("temperature"));
    }

    private double getSunshineIndex(DataFrame last) {
        return sunlightDistribution.exact(last.getGlobalDouble("sunlight"));
    }

    // Looks like this won't be coming soon, leaving it to optimal.
    private double getPHIndex(DataFrame last) {
        return 1;
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    void onUpdateSettings(SettingMaster sm) {
        String s;
        if(this.name.equals("maize"))
            s = "Maissi";
        else
            s = "Durra";
        if(sm.settings.get(s + "Water") == null)
            onRegisteration(null, sm);
        
        this.waterOptimal = Double.parseDouble(
                sm.settings.get(s + "Vesi").getValue());
        this.waterDistribution = new Distribution(waterOptimal * 0.5,
                waterOptimal, waterOptimal * 1.5);
        this.temperatureOptimal = Double.parseDouble(
                sm.settings.get(s + "Lämpö").getValue());
        this.temperatureDistribution = new Distribution(
                temperatureOptimal * 0.5, temperatureOptimal,
                Double.MAX_VALUE);
    }
}
