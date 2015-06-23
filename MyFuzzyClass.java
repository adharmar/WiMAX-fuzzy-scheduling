/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myfuzzy;

/**
 *
 * @author anand-pt63
 */

/*
 * Now the paths are going to be kept constant to estimate the fairness of the algorithm.
 * We have to give qos and data for a path to be suggested for a packet.
 * We have to update the paths and continue the same procedure for the remaining packets.
 */
import java.io.*;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;//rule.FuzzyRuleSet;
public class MyFuzzyClass {
    static int time=0;
    static int path_selected=0;
    public static void main(String args[]) throws IOException
    {

        int np=10; //no.of packets
        int n=10; // no.of paths
        // get no.of packets
        //System.out.println("Enter the no.of packets : ");
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
       // np=Integer.parseInt(br.readLine());

        
        //final goodness value parameter is fg
        //double fg;

        // array to get qos,data of 'np' packets
        int[] qos={4,5,7,7,3,6,2,1,9,8};//new int[np];
        double[] data={5,4,10,12,5,10,2,5,100,120};//new double[np];

        // get parameters for each path
        int[] x={3,5,7,6,2,1,3,8,9,7};//new int[n];
        double[] y={4,6,5,3,8,9,2,1,5,10};//new double[n];
        double[] z={3,8,6,7,7,10,1,2,4,5};//new double[n];
        
        //describe the paths

        /*for(int i=0;i<n;i++)
        {
            System.out.println("Enter the qlen,dr,bprob :");
            x[i]=Integer.parseInt(br.readLine());
            y[i]=Double.parseDouble(br.readLine());
            z[i]=Double.parseDouble(br.readLine());
        }*/

        
        //get the input data packets' size and qos

        /*for(int i=0;i<np;i++)
        {
            System.out.println("Enter the data,qos :");
            
            data[i]=Double.parseDouble(br.readLine());
            qos[i]=Integer.parseInt(br.readLine());
        }*/

        long startTime = System.currentTimeMillis();

        // to store the newly calculated dr parameter
        double[][] y_cal=new double[np][n];

        //process d and r
        y_cal=caldr(data,y);

        // display y and ycal
        System.out.println("Printing y..........");
        for(int i=0;i<y.length;i++)
        {
            System.out.println(y[i]);
        }
        System.out.println("Printing ycal..........");
        for(int i=0;i<data.length;i++)
        {
            for(int j=0;j<y.length;j++)
                System.out.println(y_cal[i][j]);
            System.out.println();
        }


        double fgmax=0;
        double fg_temp;
        int idata=0;
        for(int i=0;i<np;i++)
        {
            fg_temp = fuzzyfn(x,y_cal[i],z,n,np,qos[i]);
            if(fgmax<fg_temp)
            {
                fgmax = fg_temp;
                idata = i+1;
            }
        }
        
        //fg=fw2(gmax,qos);

        // print the max goodness with path number
        //System.out.println("The path is "+path_selected+" and its goodness value is "+gmax+" is the best path...!!!");
        System.out.println("The path is "+path_selected+" and its fgoodness value is "+fgmax+" is the best path for "+idata+ "th packet ...!!!");

        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time in execution is :"+ (endTime-startTime));
        System.out.println("Total processed time in execution is :"+ time);
        //double g=fw1(10,10,10);

        //System.out.println ("Printing from main.... G is "+g);
    }
    public static double fuzzyfn(int[] x,double[] y,double[] z,int n,int np,int qos)
    {
        
        //declare path no.of variables to get the goodness values
        double[] g=new double[n];
        // final goodness variable
        double fg;
        // caldr is a function that calculates the new dr values for given data rate
        

        long startTime = System.currentTimeMillis();
        // preprocess the parameters to fit the FIS
        int[] p,q,r=new int[n];
        p=preprocess_qlen(x);
        q=preprocess_dr(y);
        r=preprocess_dr_bprob(z);

        double gmax=0;
        int imax=0;


        //get the goodness values for each set and also get the highest among them in the same for loop

        for(int i=0;i<n;i++)
        {
            System.out.println("The final parameters into the fw1 function are  : "+ p[i]+ " and "+ q[i]+" and "+r[i]);
            g[i]=fw1(p[i],q[i],r[i]);
            //System.out.println("The path "+(i+1)+" goodness is "+g[i]);
            if(gmax<g[i])
            {
                gmax=g[i];
                imax=i+1;
            }
        }
        fg=fw2(gmax,qos);
        path_selected=imax;
        return fg;
    }
    public static double[][] caldr(double[] data,double[] y)
    {
        //algorithm to process d and r
        /*
         * we calculate the d/r ratio for all combinations and invert it to get a ascending tone (i.e. higher the value, better the parameter)
         * after inversion normalize all values and put it into y_cal
         */
        int a=data.length;
        int b=y.length;
        double maxval=0;
        double factor=0;
        double[][] temp=new double[a][b];
        for(int i=0;i<a;i++)
        {
            for(int j=0;j<b;j++)
            {
                temp[i][j]=(1/(data[i]/y[j]));
                System.out.println("The temp[i][j] is : " + temp[i][j]);
                if(maxval < temp[i][j])
                    maxval = temp[i][j];
            }
        }
        System.out.println("The maxval out of the given data is : " + maxval);
        factor=10/maxval;
        System.out.println("The factor is : " + factor);
        for(int i=0;i<a;i++)
        {
            for(int j=0;j<b;j++)
            {
                temp[i][j]=(temp[i][j]*factor);
            }
        }
        return temp;
    }
    public static double fw1(int param1,int param2,int param3)
    {

        // Load from 'FCL' file
        String fileName = "fw.fcl";
        FIS fis = FIS.load(fileName,true);
        // Error while loading?
        if( fis == null ) {
            System.err.println("Can't load file: '"
                                   + fileName + "'");
            return 0;
        }

        // Show ruleset
        //fis.chart();
        FunctionBlock functionBlock = fis.getFunctionBlock(null);

        // Set inputs
        //fis.setVariable("service", 3);
        //fis.setVariable("food", 7);
        functionBlock.setVariable("qlen", param1);
        functionBlock.setVariable("dr", param2);
        functionBlock.setVariable("bprob",param3);

        // Evaluate
        //fis.evaluate();
        long startTime = System.currentTimeMillis();
        functionBlock.evaluate();
        long endTime = System.currentTimeMillis();
        // Show output variable's chart
        //fis.getVariable("tip").chartDefuzzifier(true);
        functionBlock.getVariable("goodness").chartDefuzzifier(true);

        // Print ruleSet
        //System.out.println(fis);

        // get the goodness in a variable
        double gness = functionBlock.getVariable("goodness").getValue();
        System.out.println(functionBlock);
        System.out.println("GOODNESS:"+ functionBlock.getVariable("goodness").getValue());

        time+=(endTime-startTime);
        return gness;
    }
    public static double fw2(double param1,int param2)
    {

        // Load from 'FCL' file
        String fileName = "fw.fcl";
        FIS fis = FIS.load(fileName,true);
        // Error while loading?
        if( fis == null ) {
            System.err.println("Can't load file: '"
                                   + fileName + "'");
            return 0;
        }

        // Show ruleset
        //fis.chart();
        FunctionBlock functionBlock = fis.getFunctionBlock(null);

        // Set inputs
        //fis.setVariable("service", 3);
        //fis.setVariable("food", 7);
        functionBlock.setVariable("g", param1);
        functionBlock.setVariable("qos", param2);
        //functionBlock.setVariable("bprob",param3);

        // Evaluate
        //fis.evaluate();
        long startTime = System.currentTimeMillis();
        functionBlock.evaluate();
        long endTime = System.currentTimeMillis();

        // Show output variable's chart
        //fis.getVariable("tip").chartDefuzzifier(true);
        functionBlock.getVariable("fgoodness").chartDefuzzifier(true);

        // Print ruleSet
        //System.out.println(fis);

        // get the goodness in a variable
        double fgness = functionBlock.getVariable("fgoodness").getValue();
        System.out.println(functionBlock);
        System.out.println("FGOODNESS:"+ functionBlock.getVariable("fgoodness").getValue());

        time+=(endTime-startTime);
        return fgness;
    }
    public static int[] preprocess_qlen(int[] x)
    {
        int[] a=new int[x.length];
        //scale of qlen
        double qlenscale =10;
        double qlenmax=0;
        for(int i=0;i<x.length;i++)
        {
            if(qlenmax<x[i])
            {
                qlenmax=x[i];
            }
        }
        double qlenfactor=qlenscale/qlenmax;
        for(int j=0;j<x.length;j++)
        {
            a[j]=(int)(Math.round(x[j]*qlenfactor));
            //System.out.println("The adjusted value of qlen is "+a[j]);
        }

        return a;
    }
    public static int[] preprocess_dr_bprob(double[] x)
    {
        int[] a=new int[x.length];
        //scale of qlen
        double scale =10;
        double max=0;
        for(int i=0;i<x.length;i++)
        {
            if(max<x[i])
            {
                max=x[i];
            }
        }
        double factor=scale/max;
        for(int j=0;j<x.length;j++)
        {
            a[j]=(int)(Math.round(x[j]*factor));
            //System.out.println("The adjusted value of br_prob is "+a[j]);
        }

        return a;
    }
    public static int[] preprocess_dr(double[] x)
    {
        int[] a=new int[x.length];
        //scale of qlen

        for(int j=0;j<x.length;j++)
        {
            a[j]=(int)(Math.round(x[j]));
            //System.out.println("The adjusted value of br_prob is "+a[j]);
        }

        return a;
    }

}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

