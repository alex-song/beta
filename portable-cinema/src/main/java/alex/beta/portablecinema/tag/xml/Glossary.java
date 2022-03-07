//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.3.0-b170531.0717 生成的
// 请访问 <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2022.03.04 时间 09:24:49 PM CST 
//


package alex.beta.portablecinema.tag.xml;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>anonymous complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="actor" type="{}Term" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="category" type="{}Term" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="producer" type="{}Term" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="other" type="{}Term" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "actor",
        "category",
        "producer",
        "other"
})
@XmlRootElement(name = "Glossary")
public class Glossary {

    @XmlElement(nillable = true)
    protected List<Term> actor;
    @XmlElement(nillable = true)
    protected List<Term> category;
    @XmlElement(nillable = true)
    protected List<Term> producer;
    @XmlElement(nillable = true)
    protected List<Term> other;

    /**
     * Gets the value of the actor property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actor property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActor().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Term }
     */
    public List<Term> getActor() {
        if (actor == null) {
            actor = new ArrayList<>();
        }
        return this.actor;
    }

    /**
     * Gets the value of the category property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the category property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCategory().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Term }
     */
    public List<Term> getCategory() {
        if (category == null) {
            category = new ArrayList<>();
        }
        return this.category;
    }

    /**
     * Gets the value of the producer property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the producer property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProducer().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Term }
     */
    public List<Term> getProducer() {
        if (producer == null) {
            producer = new ArrayList<>();
        }
        return this.producer;
    }

    /**
     * Gets the value of the other property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the other property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOther().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Term }
     */
    public List<Term> getOther() {
        if (other == null) {
            other = new ArrayList<>();
        }
        return this.other;
    }

}
