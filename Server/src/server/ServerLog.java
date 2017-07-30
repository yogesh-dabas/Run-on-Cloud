package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yogesh Dabas
 */
public class ServerLog {

    private FileWriter out;
    private String type;

    public ServerLog(String type) {
        this.type=type;
        File logFolder = new File("log");
        if (!logFolder.exists()) {
            logFolder.mkdir();
        }
    }

    public void print(String msg) {

        try {
            Calendar cal = Calendar.getInstance();
            File logFile = new File("log\\"+ cal.get(Calendar.DATE) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR) +"_"+type+".txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            out = new FileWriter(logFile,true);
            out.append(cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND)+" - ");
            out.append(msg+"\r\n");
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerLog.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
