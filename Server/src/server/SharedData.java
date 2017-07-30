
package server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 * @author Yogesh Dabas
 */
public class SharedData {
    
    //Feedback
    public static HashMap<String, String> programFeedbackToGUI = new HashMap<>();
    
    //Clients
    public static HashSet newClients=new HashSet();
    public static HashSet disconnectedClients=new HashSet();
    
    //Messages
    public static String serverClosedMsg="serverClosed";
    
    //Paths
    public static String metadataPath = "apps\\metadata.txt";
    public static String programs_FE_Path="apps\\FE\\";
    public static String programs_BE_Path="apps\\BE\\";
    public static String programs_DS_Path="apps\\DS\\";
}
