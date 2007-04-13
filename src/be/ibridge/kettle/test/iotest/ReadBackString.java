package be.ibridge.kettle.test.iotest;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;

public class ReadBackString
{
    public static void main(String[] args) throws IOException
    {
        FileInputStream fileInputStream = new FileInputStream(GenerateData.FILE_NAME);
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect( GenerateData.ROW_SIZE );
        byte[] byteArray = new byte[GenerateData.ROW_SIZE];
        
        long start = new Date().getTime();
        
        StringBuffer stringBuffer = new StringBuffer();
        
        for (int i=0;i<GenerateData.NR_ROWS;i++)
        {
            if ((i%GenerateData.FEEDBACK_SIZE)==0) 
            {
                stringBuffer.setLength(0);
                System.out.println("Reading line #"+i+"-"+(i+GenerateData.FEEDBACK_SIZE-1));
            }
            
            byteBuffer.position(0);
            int n = fileChannel.read( byteBuffer );
            if( n == byteBuffer.capacity() ) {
                byteBuffer.position(0);
                byteBuffer.get(byteArray);
                    
                stringBuffer.append(new String(byteArray));
            } 
        }
        
        fileChannel.close();
        fileInputStream.close();
        
        long stop = new Date().getTime();
        System.out.println("Total time to read: "+(double)(stop-start)/(double)1000);
    }
}
