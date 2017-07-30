package gui;

import javax.swing.JLabel;

/**
 *
 * @author Yogesh Dabas
 */
public class Banner implements Runnable {

    JLabel l;

    Banner( JLabel l) {
        this.l = l;
    }
    
    public void setBanner(String msg){
        String t ="                                                                                                                                                             ";
        t+=msg;
        l.setText(t);
    }

    @Override
    public void run() {
        while (true) {
            String t=l.getText();
            t=t.substring(1,t.length())+t.charAt(0);
            l.setText(t);
            try{
                Thread.sleep(100);
            }catch(InterruptedException e){
            
            }
        }

    }

}
