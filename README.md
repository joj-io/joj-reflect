# joj-annotation

# AnnotationBuilder: Type-safe annotation builder (synthesizer) for Java 8

```
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 XmlJavaTypeAdapter generated = AnnotationBuilder.builderFor(XmlJavaTypeAdapter.class)
   .with(XmlJavaTypeAdapter::value).returning(MyAdapter.class)
   .build();
```

## Installation

```
<dependency>
  <groupId>io.joj</groupId>
  <artifactId>joj-reflect</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
