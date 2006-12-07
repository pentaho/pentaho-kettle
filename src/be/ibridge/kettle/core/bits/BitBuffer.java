package be.ibridge.kettle.core.bits;

import java.util.ArrayList;
import java.util.List;

public class BitBuffer
{
    private List buffer;
    private int position;
    
    public BitBuffer()
    {
        position=0;
        buffer = new ArrayList();
    }
    
    public void addBits(long value, int nrBits)
    {
        int needed = buffer.size()*8 - position + nrBits;
        while (needed>0)
        {
            buffer.add(new Byte((byte)0));
            needed-=8;
        }
        
        // now add the bits in the correct positions
        for (int i=0;i<nrBits;i++)
        {
            long mask = 1<<i;
            
            boolean bit = ( value & mask ) > 0;
            System.out.println("adding bit #"+i+" : "+(bit?"1":"0"));
            if (bit)
            {
                int byteNr = position/8;
                
                Byte byteObj = (Byte) buffer.get(byteNr);
                byte b = byteObj.byteValue();
                
                int bitNr = position%8;
                
                b|= (1<<bitNr);
                
                buffer.set(byteNr, new Byte(b));
            }
            position++;
        }
    }
    
    public long getBits(int nrBits, int idx)
    {
        return 0L; // TODO
    }
    
    public List getBuffer()
    {
        return buffer;
    }
    
    
    public static void main(String[] args)
    {
        BitBuffer bitBuffer = new BitBuffer();
        bitBuffer.addBits(0xDEADBEEF, 32);
        
        for (int i=0;i<bitBuffer.getBuffer().size();i++)
        {
            byte b = ((Byte)bitBuffer.getBuffer().get(i)).byteValue();
            
            String bits = Long.toBinaryString( b & 0xFF );
            System.out.println("byte #"+i+" : "+bits );
        }
    }
    
    
}
