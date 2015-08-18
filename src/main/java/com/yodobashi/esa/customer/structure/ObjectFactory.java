
package com.yodobashi.esa.customer.structure;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.structure package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.structure
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link COMMONINPUT }
     * 
     */
    public COMMONINPUT createCOMMONINPUT() {
        return new COMMONINPUT();
    }

    /**
     * Create an instance of {@link COMMONRETURN.ReturnComHeader }
     * 
     */
    public COMMONRETURN.ReturnComHeader createCOMMONRETURNReturnComHeader() {
        return new COMMONRETURN.ReturnComHeader();
    }

    /**
     * Create an instance of {@link COMMONRETURN }
     * 
     */
    public COMMONRETURN createCOMMONRETURN() {
        return new COMMONRETURN();
    }

    /**
     * Create an instance of {@link COMMONRETURN.ReturnComDetail }
     * 
     */
    public COMMONRETURN.ReturnComDetail createCOMMONRETURNReturnComDetail() {
        return new COMMONRETURN.ReturnComDetail();
    }

}
