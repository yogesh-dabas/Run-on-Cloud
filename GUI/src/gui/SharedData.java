/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JButton;

/**
 *
 * @author Yogesh Dabas
 */
public class SharedData {
    public static ConcurrentHashMap<String,JButton> clientData=new ConcurrentHashMap();
    
    //Loading Queue
    public static HashSet<String> programsForLoading=new HashSet();
    public final static StringBuffer programLoadingMutex=new StringBuffer();
    
    //Execution Queue
    public static HashMap programsForExecution=new HashMap();
    public final static StringBuffer programExecutionMutex=new StringBuffer();
    
}
