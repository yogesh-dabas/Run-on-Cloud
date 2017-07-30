package server;

/**
 *
 * @author Yogesh Dabas
 */
public class ClientUtilization {

    double cpu;
    double mem;
    double dsk;
    
    //HashSet<String> programExecRequests=new HashSet<String>();
    StringBuffer progExecRequests = new StringBuffer();

    ClientUtilization() {
    }

    ClientUtilization(double cpu, double mem, double dsk) {
        this.cpu = cpu;
        this.mem = mem;
        this.dsk = dsk;
    }

    public void addProgramExecRequest(String programName) {
        progExecRequests.append(programName + ":");

    }

    public String getProgramForExecIfAny() {
        String t = progExecRequests.toString();
        int i = t.indexOf(':', t.indexOf(':')+1);
        if (i != -1) {
            String r = t.substring(0, i).trim();
            t = t.substring(i + 1, t.length());
            progExecRequests = new StringBuffer(t);
            return r;
        } else {
            return null;
        }
    }

}
