package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Yogesh Dabas
 */
public class MyJPanel extends JPanel implements Runnable {

    private String status = "";

    private double cpu;
    private double mem;
    private double dsk;

    private double maxUsage;
    
    //private double maxCpu=0;

    //private double maxMem=0;
    
    static int numberOfSeconds = 30;
    static int maxXSize = 420;
    static int maxYSize = 200;
    static int stepSizeOfX = maxXSize / numberOfSeconds;

    MyLine l[] = new MyLine[numberOfSeconds + 1];

    public void setStatus(String status) {
        this.status = status;
    }

    /*public void setMaxCpu(double maxCpu) {
     this.maxCpu = maxCpu;
     }

     public void setMaxMem(double maxMem) {
     this.maxMem = maxMem;
     }
     */
    public void setCpu(double cpu) {
        this.cpu = cpu;
        if (cpu > maxUsage) {
            maxUsage = cpu;
        }
    }

    public void setMem(double mem) {
        this.mem = mem;
        if (mem > maxUsage) {
            maxUsage = mem;
        }
    }

    public void setDsk(double dsk) {
        this.dsk = dsk;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        super.setBackground(Color.BLACK);
        Font f = new Font("Verdana", Font.PLAIN, 10);
        g.setFont(f);
        g.setColor(Color.WHITE);
        g.drawString(status, 0, 20);
        
        g.setColor(Color.red);
        g.drawLine(370, 15, 400, 15);
        g.drawString("CPU: ", 330, 20);
        
        g.setColor(Color.green);
        g.drawLine(370, 25, 400, 25);
        g.drawString("RAM: ", 330, 30);
        
        g.setColor(Color.BLUE);
        g.drawLine(370, 35, 400, 35);
        g.drawString("DISK:", 330, 40);
        
        for (int i = 0; i < numberOfSeconds + 1; i++) {
            g.setColor(Color.red);
            g.drawLine(l[i].getX1(), maxYSize - l[i].getY1_cpu(), l[i].getX2(), maxYSize - l[i].getY2_cpu());
            g.setColor(Color.green);
            g.drawLine(l[i].getX1(), maxYSize - l[i].getY1_mem(), l[i].getX2(), maxYSize - l[i].getY2_mem());
            g.setColor(Color.BLUE);
            g.drawLine(l[i].getX1(), maxYSize - l[i].getY1_dsk(), l[i].getX2(), maxYSize - l[i].getY2_dsk());
           
            //System.out.println(l[i].getY2_cpu()+ " " + l[i].getY2_mem() + " " + l[i].getY2_dsk() + " " + l[i].getY2_tym());
        }
    }

    public void run() {
        while (true) {
            for (int i = 1; i < numberOfSeconds + 1; i++) {
                l[i].shiftLeft();
                l[i - 1] = l[i];
            }
            l[numberOfSeconds] = new MyLine();
            if (status.startsWith("Client")) {
                
                l[numberOfSeconds].setX1(maxXSize - stepSizeOfX);
                l[numberOfSeconds].setX2(maxXSize);

                //cpu usage
                l[numberOfSeconds].setY1_cpu(l[numberOfSeconds - 1].getY2_cpu());
                l[numberOfSeconds].setY2_cpu((int) (cpu * 150 / maxUsage));

                //mem usage
                l[numberOfSeconds].setY1_mem(l[numberOfSeconds - 1].getY2_mem());
                l[numberOfSeconds].setY2_mem((int) (mem * 150 / maxUsage));

                //dsk usage
                l[numberOfSeconds].setY1_dsk(l[numberOfSeconds - 1].getY2_dsk());
                l[numberOfSeconds].setY2_dsk((int) dsk);
            }

            repaint();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error->" + e.getMessage());
            }
        }
    }

}
