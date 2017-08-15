package Trader;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by trash on 14.08.2017.
 */
class Log {

    static void log(String log) {
        try(FileWriter writer = new FileWriter("D:\\проги\\trader\\Trader\\src\\Trader\\logFile.txt", true))
        {
            writer.write(log);
            writer.append('\n');
            System.out.println(log);
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}
