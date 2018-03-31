Classes in this package support serializing/deserializing StepMetaInterface objects by leveraging 
MetadataInjection annotations, eliminating the need to write tedious metadata serialization code.

The simplest way for a step to use this strategy is to extend 
the BaseSerializingMeta class, which implements the 
  ````
  .loadXML() / .getXML() 
  .readRep() / .saveRep() 
````

The MetaXmlSerializer implementation uses JAXB.  The output XML will have a form like:
```
    <step-props>
      <group name="some_group">
        <property name="Prop1">
          <value xsi:type="xs:string">value</value>
        </property>
        <property name="isLarge">
          <value xsi:type="xs:boolean">false</value>
        </property>
      </group>
    </step-props>
```

To serialize XML within a StepMetaInterface implementation, the call
is:
  ` MetaXmlSerializer.serialize( StepMetaProps.from( myMeta ) )`

For deserialization:
  ` MetaXmlSerializer.deserialize( xml ).to( myMeta )`

Repo writes/reads use

  ```
RepoSerializer
         .builder()
         .repo( repo )
         .stepMeta( stepMeta )
         .stepId( stepId )
         .transId( transId )
         .serialize()   /  .deserialize()
```

If any properties need to be encrypted, mark them with the @Sensitive annotation.


TODO:
* Support for database lists / metastore
