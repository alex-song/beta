//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.3.0-b170531.0717 生成的
// 请访问 <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2022.03.04 时间 09:24:49 PM CST 
//


package alex.beta.portablecinema.tag.xml;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the alex.beta.portablecinema.tag.xml package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: alex.beta.portablecinema.tag.xml
     */
    public ObjectFactory() {
        //default constructor
    }

    /**
     * Create an instance of {@link Glossary }
     */
    public Glossary createGlossary() {
        return new Glossary();
    }

    /**
     * Create an instance of {@link Term }
     */
    public Term createTerm() {
        return new Term();
    }

}
