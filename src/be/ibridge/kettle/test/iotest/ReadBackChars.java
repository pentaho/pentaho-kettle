package be.ibridge.kettle.test.iotest;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class ReadBackChars
{
    public static void main(String[] args) throws IOException
    {
        FileInputStream fileInputStream = new FileInputStream(GenerateData.FILE_NAME);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        // char[] buffer = new char[GenerateData.ROW_SIZE];

        long start = new Date().getTime();
        
        StringBuffer stringBuffer = new StringBuffer();
        
        for (int i=0;i<GenerateData.NR_ROWS;i++)
        {
            if ((i%GenerateData.FEEDBACK_SIZE)==0) 
            {
                stringBuffer.setLength(0);
                System.out.println("Reading line #"+i+"-"+(i+GenerateData.FEEDBACK_SIZE-1));
            }
            
            for (int r=0;r<GenerateData.ROW_SIZE;r++)
            {
                stringBuffer.append('A' /* (char)bufferedInputStream.read()*/);
            }
        }
        
        bufferedInputStream.close();
        fileInputStream.close();
        
        long stop = new Date().getTime();
        System.out.println("Total time to read: "+(double)(stop-start)/(double)1000);
    }
}
