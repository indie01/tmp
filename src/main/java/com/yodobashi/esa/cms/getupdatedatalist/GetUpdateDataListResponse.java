
package com.yodobashi.esa.cms.getupdatedatalist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.domain.NotifyUpdateColumnIF;
import com.kickmogu.yodobashi.community.resource.domain.NotifyUpdateDataIF;
import com.kickmogu.yodobashi.community.resource.domain.NotifyUpdateRecordIF;
import com.yodobashi.esa.community.common.COMMONRETURN;


/**
 * <p>Java class for GetUpdateDataList_Response complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetUpdateDataList_Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataList" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="recordList" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="tableName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="columnList" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="column" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="columnName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                                 &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="COMMON_RETURN" type="{http://esa.yodobashi.com/CMS/common}COMMON_RETURN" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetUpdateDataList_Response", propOrder = {
    "dataList",
    "commonreturn"
})
public class GetUpdateDataListResponse implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5521047533098902987L;
	
	protected List<GetUpdateDataListResponse.DataList> dataList = Lists.newArrayList();
    @XmlElement(name = "COMMON_RETURN")
    protected COMMONRETURN commonreturn = COMMONRETURN.SUCCESS;

    /**
     * Gets the value of the dataList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetUpdateDataListResponse.DataList }
     * 
     * 
     */
    public List<GetUpdateDataListResponse.DataList> getDataList() {
        if (dataList == null) {
            dataList = new ArrayList<GetUpdateDataListResponse.DataList>();
        }
        return this.dataList;
    }

    /**
     * Gets the value of the commonreturn property.
     * 
     * @return
     *     possible object is
     *     {@link COMMONRETURN }
     *     
     */
    public COMMONRETURN getCOMMONRETURN() {
        return commonreturn;
    }

    /**
     * Sets the value of the commonreturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link COMMONRETURN }
     *     
     */
    public void setCOMMONRETURN(COMMONRETURN value) {
        this.commonreturn = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="recordList" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="tableName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="columnList" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="column" minOccurs="0">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="columnName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                                       &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "recordList"
    })
    public static class DataList implements Serializable,NotifyUpdateDataIF {

        /**
		 * 
		 */
		private static final long serialVersionUID = -3541611596008622533L;
		
		protected List<GetUpdateDataListResponse.DataList.RecordList> recordList;
		
		@XmlTransient
		protected String notifyUpdateEntryId;


		@Override
		public void setNotifyUpdateEntryId(String entryId) {
			this.notifyUpdateEntryId = entryId;
		}

		@Override
		public String getNotifyUpdateEntryId() {
			return notifyUpdateEntryId;
		}		

        /**
         * Gets the value of the recordList property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the recordList property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRecordList().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GetUpdateDataListResponse.DataList.RecordList }
         * 
         * 
         */
        public List<GetUpdateDataListResponse.DataList.RecordList> getRecordList() {
            if (recordList == null) {
                recordList = new ArrayList<GetUpdateDataListResponse.DataList.RecordList>();
            }
            return this.recordList;
        }
        
        @Override
        public List<? extends NotifyUpdateRecordIF> getNotifyUpdateRecordList() {
        	return getRecordList();
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="tableName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="columnList" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="column" minOccurs="0">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="columnName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                             &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "tableName",
            "columnList"
        })
        public static class RecordList implements Serializable, NotifyUpdateRecordIF {

            /**
			 * 
			 */
			private static final long serialVersionUID = -486259481074188198L;
			
			protected String tableName;
            protected List<GetUpdateDataListResponse.DataList.RecordList.ColumnList> columnList;

            /**
             * Gets the value of the tableName property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Override
            public String getTableName() {
                return tableName;
            }

            /**
             * Sets the value of the tableName property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTableName(String value) {
                this.tableName = value;
            }

            /**
             * Gets the value of the columnList property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the columnList property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getColumnList().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link GetUpdateDataListResponse.DataList.RecordList.ColumnList }
             * 
             * 
             */
            public List<GetUpdateDataListResponse.DataList.RecordList.ColumnList> getColumnList() {
                if (columnList == null) {
                    columnList = new ArrayList<GetUpdateDataListResponse.DataList.RecordList.ColumnList>();
                }
                return this.columnList;
            }
            
            @Override
            public List<? extends NotifyUpdateColumnIF> getNotifyUpdateColumnList() {
            	return getColumnList();
            }


            /**
             * <p>Java class for anonymous complex type.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="column" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="columnName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *                   &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "column"
            })
            public static class ColumnList implements Serializable, NotifyUpdateColumnIF {

                /**
				 * 
				 */
				private static final long serialVersionUID = -3858613111333000828L;
				
				protected GetUpdateDataListResponse.DataList.RecordList.ColumnList.Column column;
				
				@Override
				public String getColumnName() {
					return column.getColumnName();
				}
				
				@Override
				public String getValue() {
					return column.getValue();
				}

				@Override
				public boolean isEmpty() {
					return column == null;
				}				
                /**
                 * Gets the value of the column property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link GetUpdateDataListResponse.DataList.RecordList.ColumnList.Column }
                 *     
                 */
                public GetUpdateDataListResponse.DataList.RecordList.ColumnList.Column getColumn() {
                    return column;
                }

                /**
                 * Sets the value of the column property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link GetUpdateDataListResponse.DataList.RecordList.ColumnList.Column }
                 *     
                 */
                public void setColumn(GetUpdateDataListResponse.DataList.RecordList.ColumnList.Column value) {
                    this.column = value;
                }


                /**
                 * <p>Java class for anonymous complex type.
                 * 
                 * <p>The following schema fragment specifies the expected content contained within this class.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;sequence>
                 *         &lt;element name="columnName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
                 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                    "columnName",
                    "value"
                })
                public static class Column implements Serializable {

                    /**
					 * 
					 */
					private static final long serialVersionUID = 3310763179492375431L;
					
					protected String columnName;
                    @XmlElement(name = "Value")
                    protected String value;

                    /**
                     * Gets the value of the columnName property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getColumnName() {
                        return columnName;
                    }

                    /**
                     * Sets the value of the columnName property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setColumnName(String value) {
                        this.columnName = value;
                    }

                    /**
                     * Gets the value of the value property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getValue() {
                        return value;
                    }

                    /**
                     * Sets the value of the value property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setValue(String value) {
                        this.value = value;
                    }

                }


            }


        }


    }

}
