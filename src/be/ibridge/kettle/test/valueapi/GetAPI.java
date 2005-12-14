package be.ibridge.kettle.test.valueapi;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import be.ibridge.kettle.core.value.Value;

public class GetAPI
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
       Value value = new Value();
       Method method[] = value.getClass().getMethods();
       for (int i=0;i<method.length;i++)
       {
           int modifiers = method[i].getModifiers();
           if (Modifier.isPublic(modifiers)) 
           {
               if (method[i].getReturnType().equals(value.getClass()))
               {
                   System.out.print(method[i].getName()+"(");
                   
                   Class par[] = method[i].getParameterTypes();
                   for (int p=0;p<par.length;p++)
                   {
                       if (p>0) System.out.print(", ");
                       System.out.print(par[p]. getName());
                   }
                   System.out.println(")");
               }
           }
       }
       
    }

}
