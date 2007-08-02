package org.pentaho.di.trans;


public abstract class BaseTransSplitInfo implements TransSplitInfo
{
	protected TransMeta transMeta;
	protected boolean show;
	protected boolean post;
	protected boolean prepare;
	protected boolean start;
	
	public BaseTransSplitInfo(TransMeta transMeta)
	{
		this.transMeta = transMeta;
	}
	
	
	public TransMeta getTransMeta()
	{
		return transMeta;
	}

	public boolean post()
	{
		return post;
	}

	public boolean prepare()
	{
		return prepare;
	}

	public boolean show()
	{
		return show;
	}

	public boolean start()
	{
		return start;
	}


	public void setPost(boolean post)
	{
		this.post = post;
	}


	public void setPrepare(boolean prepare)
	{
		this.prepare = prepare;
	}


	public void setShow(boolean show)
	{
		this.show = show;
	}


	public void setStart(boolean start)
	{
		this.start = start;
	}


	public void setTransMeta(TransMeta transMeta)
	{
		this.transMeta = transMeta;
	}

}
