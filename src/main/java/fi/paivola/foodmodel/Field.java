package fi.paivola.foodmodel;

import fi.paivola.mapserver.core.*;
import fi.paivola.mapserver.utils.*;
import fi.paivola.mapserver.core.setting.*;
/**
 * @author Jaakko Hannikainen
 */
public class Field extends PointModel {

    private Edible content;
    private String contentString;
    private double area;
    private double lastfood = 0;
    private int index = 1;

    /**
     * Main constructor for the food model
     */
    public Field(int id) {
        super(id);
    }
    
    @Override
    public void onTick(DataFrame last, DataFrame current) {
        double d = this.content.onTick(last, current) ;
        if(d != lastfood && d != 0) {
            this.saveDouble("avgFood", d/index++);
            lastfood = d;
            addEventToAll(current, new Event("cropReady", Event.Type.DOUBLE,
                        d));
        }
        this.saveDouble("foodAmount", d);
    }

    @Override
    public void onEvent(Event e, DataFrame current) {
        switch(e.name) {
            case "gather":
                addEventTo(e.sender, current, new Event("harvested", Event.Type.OBJECT,
                            content.harvest((double)e.value)));
                break;
            case "flood":
                if((boolean)e.value)
                    content.handleEvent(e, current);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRegisteration(GameManager gm, SettingMaster sm) {
        sm.settings.put("area", new SettingDouble("area", 1.0,
                new RangeDouble(0, Integer.MAX_VALUE)));
        sm.settings.put("content", new SettingString("content", "empty"));
        sm.settings.put("MaissiVesi", new SettingDouble("MaissiVesi", 1.0,
                new RangeDouble(0, Integer.MAX_VALUE)));
        sm.settings.put("MaissiLämpö", new SettingDouble("MaissiLämpö", 1.0,
                new RangeDouble(0, Integer.MAX_VALUE)));
        sm.settings.put("DurraVesi", new SettingDouble("DurraVesi", 1.0,
                new RangeDouble(0, Integer.MAX_VALUE)));
        sm.settings.put("DurraLämpö", new SettingDouble("DurraLämpö", 1.0,
                new RangeDouble(0, Integer.MAX_VALUE)));
    }

    @Override
    public void onGenerateDefaults(DataFrame df) {
//        System.out.println("asd");
        setContents(contentString);
        content.setArea(this.area);
    }

    @Override
    public void onUpdateSettings(SettingMaster sm) {
        setContents(sm.settings.get("content").getValue().toLowerCase());
        this.area = Double.parseDouble(sm.settings.get("area").getValue());
        this.content.onUpdateSettings(sm);
        this.content.setArea(this.area);
    }

    public void setContents(String newContent) {
        this.contentString = newContent;
        switch(newContent) {
            case "empty":
                content = new Empty();
                break;
            case "wheat":
                content = new Wheat();
                break;
            case "maize":
                content = new Maize();
                break;
            case "sorghum":
                content = new Sorghum();
                break;
            case "cow":
                content = new Cow();
                break;
            default:
                content = new Maize();
                break;
        }
    }
}


