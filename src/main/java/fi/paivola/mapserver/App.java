package fi.paivola.mapserver;

import fi.paivola.mapserver.core.Event;
import fi.paivola.mapserver.core.ExtensionModel;
import fi.paivola.mapserver.core.GameManager;
import fi.paivola.mapserver.core.GameThread;
import fi.paivola.mapserver.core.Model;
import fi.paivola.mapserver.core.SettingsParser;
import fi.paivola.mapserver.core.WSServer;
import fi.paivola.mapserver.core.setting.*;
import fi.paivola.mapserver.utils.LatLng;
import fi.paivola.mapserver.core.setting.SettingMaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Integer.parseInt;
import java.net.UnknownHostException;
import java.util.logging.LogManager;
import org.json.simple.parser.ParseException;

public class App {

    static final boolean profilingRun = false;

    public static void main(String[] args) throws UnknownHostException, IOException, ParseException, InterruptedException {

        SettingsParser.parse();

        if (profilingRun) { // For profiling

            LogManager.getLogManager().reset();

            for (int i = 0; i < 1000; i++) {
                runTest();
            }

        } else {

            WSServer ws = new WSServer(parseInt(SettingsParser.settings.get("websocket_port").toString()));
            ws.start();

            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
            printHelp();
            mainloop:
            while (true) {
                String in = sysin.readLine();
                switch (in) {
                    case "q":
                    case "quit":
                    case "e":
                    case "exit":
                        ws.stop();
                        break mainloop;
                    case "t":
                    case "test":
                        ws.stop();
                        runTest();
                        break mainloop;
                    case "h":
                    case "help":
                        printHelp();
                        break;
                    default:
                        System.out.println("Unknown command (" + in + ")");
                        printHelp();
                        break;
                }
            }
        }
    }

    static void printHelp() {
        System.out.println("q|e|quit|exit   - Quits the program\n"
                + "t|test          - Run the test function\n"
                + "h|help          - Display this help");
    }

    /**
     * This function can be used for testing your own models. Please modify
     * this!
     */ 
    static void runTest() {

        // How many ticks? Each one is a week.
        int simulationDurationTicks = (int) Math.floor(52.177457 * 20);
        // print debug-info on all parameters moving between models
        boolean printFrameData = false;
        GameThread one = new GameThread(simulationDurationTicks, printFrameData);
        GameManager gm = one.game;

        // globalit
        gm.createModel("Weather");

        // ruoka
        SettingMaster sm = one.game.getDefaultSM("Field");
        sm.settings.get("content").setValue("maize");
        Model Field1 = gm.createModel("Field");

        // kaupungit
        Model Town1 = gm.createModel("PopCenter");
        Model Town2 = gm.createModel("PopCenter");
        Model Road1 = gm.createModel("Road");
        gm.linkModelsWith(Town1, Town2, Road1);
        gm.linkModelsWith(Town1, Town2, gm.createModel("PhoneLine"));

        // ruoka x kaupungit
        Model Road2 = gm.createModel("Road");
        gm.linkModelsWith(Field1, Town1, Road2);

        // power
        Model PP1 = gm.createModel("Power plant");
        Model PC1 = gm.createModel("Power connection");
        Model PU1 = gm.createModel("Power user");
        PP1.setLatLng(1, 0);
        PU1.setLatLng(1, 3);
        gm.linkModelsWith(PP1, PU1, PC1);

        // water
        Model l1 = gm.createModel("Lake");
        sm = gm.getDefaultSM("Lake");
        sm.settings.get("order").setValue("1");
        l1.onActualUpdateSettings(sm);
        
        Model l2 = gm.createModel("Lake");
        sm = gm.getDefaultSM("Lake");
        sm.settings.get("order").setValue("1");
        l2.onActualUpdateSettings(sm);
        
        Model r1 = gm.createModel("River");
        sm = gm.getDefaultSM("River");
        sm.settings.get("order").setValue("2");
        r1.onActualUpdateSettings(sm);
        
        Model r2 = gm.createModel("River");
        sm = gm.getDefaultSM("River");
        sm.settings.get("order").setValue("2");
        r2.onActualUpdateSettings(sm);
        
        Model l3 = gm.createModel("Lake");
        sm = gm.getDefaultSM("Lake");
        sm.settings.get("order").setValue("3");
        l3.onActualUpdateSettings(sm);
        
        Model r3 = gm.createModel("River");
        sm = gm.getDefaultSM("River");
        sm.settings.get("order").setValue("4");
        r3.onActualUpdateSettings(sm);
        
        Model s1 = gm.createModel("Sea");
        sm = gm.getDefaultSM("Sea");
        sm.settings.get("order").setValue("5");
        s1.onActualUpdateSettings(sm);
        
        Model weather = gm.createModel("Weather");
        
        gm.linkModelsWith(l1, l3, r1);
        gm.linkModelsWith(l2, l3, r2);
        gm.linkModelsWith(l3, s1, r3);

        if (!profilingRun) {
            gm.printOnDone = 2;
        }

        // Start the gamethread
        one.start();
    }
}
