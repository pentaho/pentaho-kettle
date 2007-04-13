package be.ibridge.kettle.test.iotest;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class GenerateData
{
    public static final int NR_ROWS = 1000000;
    public static final int ROW_SIZE = 1500;
    public static final String FILE_NAME = "D:/temp/testfile.txt";

    public static final int FEEDBACK_SIZE = 500;
    
    public static void main(String[] args) throws IOException
    {
        FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
        
        long start = new Date().getTime();
        
        for (int i=0;i<NR_ROWS;i++)
        {
            if ((i%FEEDBACK_SIZE)==0) System.out.println("Writing line #"+i+"-"+(i+FEEDBACK_SIZE-1));
            for (int j=0;j<ROW_SIZE;j++)
            {
                dataOutputStream.writeByte( (int)'A' + (j%26) );
            }
        }
        System.out.println("done.");
        
        dataOutputStream.close();
        fileOutputStream.close();
        
        long stop = new Date().getTime();
        System.out.println("Total time to read: "+(double)(stop-start)/(double)1000);
    }
}
