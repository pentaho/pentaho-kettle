package be.ibridge.kettle.test.iotest;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;

public class ReadBack
{
    public static void main(String[] args) throws IOException
    {
        FileInputStream fileInputStream = new FileInputStream(GenerateData.FILE_NAME);
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect( GenerateData.ROW_SIZE );
        
        long start = new Date().getTime();
        
        StringBuffer stringBuffer = new StringBuffer();
        
        for (int i=0;i<GenerateData.NR_ROWS;i++)
        {
            if ((i%500)==0) 
            {
                System.out.println("Reading line #"+i+"-"+(i+999));
                stringBuffer.setLength(0);
            }
            
            
            byteBuffer.position(0);
            int n = fileChannel.read( byteBuffer );
            if( n == byteBuffer.capacity() ) {
                byteBuffer.position(0);
                for (int x=0;x<byteBuffer.capacity();x++)
                {
                  stringBuffer.append((char)byteBuffer.get());
                }
            } 
        }
        
        fileChannel.close();
        fileInputStream.close();
        
        long stop = new Date().getTime();
        System.out.println("Total time to read: "+(double)(stop-start)/(double)1000);
    }
}
