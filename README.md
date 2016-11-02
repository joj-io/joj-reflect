# joj-annotation

## AnnotationBuilder: Type-safe annotation builder (synthesizer) for Java 8

### Example

```java
import javax.validation.constraints.Pattern;

 Pattern generated = AnnotationBuilder.builderFor(Pattern.class)
   .with(Pattern::regexp).returning("^a+.*end$")
   .with(Pattern::message).returning("Value should look funny, begin with 'a' and end with 'end'.")
   .build();
```

### Installation

```xml
<dependency>
  <groupId>io.joj</groupId>
  <artifactId>joj-reflect</artifactId>
  <version>1.1.1</version>
</dependency>
```
