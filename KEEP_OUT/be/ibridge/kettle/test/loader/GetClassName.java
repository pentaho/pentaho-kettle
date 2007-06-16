package be.ibridge.kettle.test.loader;

import be.ibridge.kettle.job.Job;

public class GetClassName
{
    public static void main(String[] args)
    {
        System.out.println("Job package: "+Job.class.getPackage().toString());
        System.out.println("Job name   : "+Job.class.getName());
    }
}
