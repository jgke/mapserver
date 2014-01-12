package fi.paivola.mapserver;

import fi.paivola.mapserver.core.GameManager;
import fi.paivola.mapserver.core.GameThread;
import fi.paivola.mapserver.core.Model;
import fi.paivola.mapserver.core.SettingsParser;
import fi.paivola.mapserver.core.WSServer;
import fi.paivola.mapserver.utils.LatLng;
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

        App.runTest();
        if (true) {
            return;
        }

        SettingsParser sp = new SettingsParser();

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
        GameThread one = new GameThread((int) Math.floor(52.177457*20));
        GameManager gm = one.game;

        Model m1 = gm.createModel("Power plant");
        gm.addModel(m1, "Power plant");
        Model m12 = gm.createModel("Power plant");
        gm.addModel(m12, "Power plant");

        m1.getLatLng().latitude = 4;
        m12.getLatLng().latitude = 10;
        
        Model m2 = gm.createModel("Power connection");
        gm.addModel(m2, "Power connection");
        Model m22 = gm.createModel("Power connection");
        gm.addModel(m22, "Power connection");
        
        Model m3 = gm.createModel("Power user");
        gm.addModel(m3, "Power user");
        
        
        
        // And link!
        gm.linkModels(m1, m2);
        //gm.linkModels(m12, m2);
        gm.linkModels(m2, m3);

        gm.linkModels(m22, m3);
        gm.linkModels(m22, m12);

        // Print final data in the end?
        // Start the gamethread
        one.start();
    }
}
