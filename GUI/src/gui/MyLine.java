package gui;

/**
 *
 * @author Yogesh Dabas
 */
public class MyLine {
    private int x1,x2;
    private int y1_cpu,y2_cpu;
    private int y1_mem,y2_mem;
    private int y1_dsk,y2_dsk;
    
    //GET SET methods
    
    public void shiftLeft(){
        x1-=MyJPanel.stepSizeOfX;
        x2-=MyJPanel.stepSizeOfX;
    }
    public void setX1(int x1){
        this.x1=x1;
    }
    public void setX2(int x2){
        this.x2=x2;
    }
    public int getX1(){
        return x1;
    }
    public int getX2(){
        return x2;
    }
    public void setY1_cpu(int y1_cpu){
        this.y1_cpu=y1_cpu;
    }
    public void setY2_cpu(int y2_cpu){
        this.y2_cpu=y2_cpu;
    }
    public int getY1_cpu(){
        return y1_cpu;
    }
    public int getY2_cpu(){
        return y2_cpu;
    }
    public int getY1_mem() {
        return y1_mem;
    }

    public void setY1_mem(int y1_mem) {
        this.y1_mem = y1_mem;
    }

    public int getY2_mem() {
        return y2_mem;
    }

    public void setY2_mem(int y2_mem) {
        this.y2_mem = y2_mem;
    }

    public int getY1_dsk() {
        return y1_dsk;
    }

    public void setY1_dsk(int y1_dsk) {
        this.y1_dsk = y1_dsk;
    }

    public int getY2_dsk() {
        return y2_dsk;
    }

    public void setY2_dsk(int y2_dsk) {
        this.y2_dsk = y2_dsk;
    }
    
    
}
