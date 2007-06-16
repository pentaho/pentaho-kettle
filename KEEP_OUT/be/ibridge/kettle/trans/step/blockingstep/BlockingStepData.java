package be.ibridge.kettle.trans.step.blockingstep;

import java.util.ArrayList;
import org.apache.commons.vfs.FileObject;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;

public class BlockingStepData extends BaseStepData implements StepDataInterface
{
	public ArrayList files;
	public ArrayList buffer;
	public ArrayList fis, gzis, dis;
	public ArrayList rowbuffer;
    public ArrayList rowMeta;

	public int     fieldnrs[];      // the corresponding field numbers;
    public FileObject fil;

    public BlockingStepData()
    {
        super();
        		
		buffer=new ArrayList(BlockingStepMeta.CACHE_SIZE);
		files=new ArrayList();
		fis  =new ArrayList();
		dis  =new ArrayList();
		gzis = new ArrayList();
		rowbuffer=new ArrayList();
        rowMeta = new ArrayList();
    }
}