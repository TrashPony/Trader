package Trader;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by trash on 14.08.2017.
 */
class Log {

    static void log(String log, String verbouse) {
        System.out.println(log);
        if (verbouse.equals("debug") || verbouse.equals("info")) {
            try (FileWriter writer = new FileWriter("D:\\проги\\trader\\Trader\\src\\Trader\\debugLog.txt", true)) {
                writer.write(log);
                writer.append('\n');
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

        if (verbouse.equals("info")) {
            try (FileWriter writer = new FileWriter("D:\\проги\\trader\\Trader\\src\\Trader\\logFile.txt", true)) {
                writer.write(log);
                writer.append('\n');
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
