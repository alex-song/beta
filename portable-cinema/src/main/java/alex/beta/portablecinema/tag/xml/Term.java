//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.3.0-b170531.0717 生成的
// 请访问 <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2022.03.04 时间 09:24:49 PM CST 
//


package alex.beta.portablecinema.tag.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>Term complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="Term"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="keyword" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="alias" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="tag" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Term", propOrder = {
        "keyword",
        "alias",
        "tag"
})
public class Term {

    @XmlElement(required = true)
    protected String keyword;
    @XmlElement(nillable = true)
    protected Set<String> alias;
    @XmlElement(nillable = true)
    protected Set<String> tag;

    public Term() {
        this(null);
    }

    public Term(String keyword) {
        this.keyword = keyword;
    }

    /**
     * 获取keyword属性的值。
     *
     * @return possible object is
     * {@link String }
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * 设置keyword属性的值。
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setKeyword(String value) {
        this.keyword = value;
    }

    /**
     * Gets the value of the alias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tag property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAlias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the set
     * {@link String }
     */
    public Set<String> getAlias() {
        if (alias == null) {
            alias = new HashSet<>();
        }
        return this.alias;
    }

    /**
     * Gets the value of the tag property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tag property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTag().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the set
     * {@link String }
     */
    public Set<String> getTag() {
        if (tag == null) {
            tag = new HashSet<>();
        }
        return this.tag;
    }

}
