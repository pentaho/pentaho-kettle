package org.pentaho.di.trans;


public abstract class ApplianceTransSplitInfo extends BaseTransSplitInfo
{
	
	public ApplianceTransSplitInfo(TransMeta transMeta)
	{
		super(transMeta);
		this.prepare = true;
		this.post = true;
		this.start = true;
	}
	

}
