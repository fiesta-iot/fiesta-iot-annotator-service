/*******************************************************************************
 * Copyright (c) 2018 Jorge Lanza, 
 *                    David Gomez, 
 *                    Luis Sanchez,
 *                    Juan Ramon Santana
 *
 * For the full copyright and license information, please view the LICENSE
 * file that is distributed with this source code.
 *******************************************************************************/
package eu.fiesta_iot.platform.annotator.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.fiesta_iot.utils.semantics.vocabulary.IotLite;
import eu.fiesta_iot.utils.semantics.vocabulary.M3Lite;
import eu.fiesta_iot.utils.semantics.vocabulary.Ssn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

//https://dzone.com/articles/custom-json-deserialization-with-jackson
//DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES
@XmlRootElement(name = "sensing_device")
@XmlAccessorType(XmlAccessType.NONE)
public class SensingDevice extends Annotatable {

	protected String id;

	protected String type;

	protected String quantityKind;

	protected String unitOfMeasurement;

	protected String endpoint;

	protected SensingDevice() {
	}

	public SensingDevice(String id, String quantityKind,
	        String unitOfMeasurement) {
		setId(id);
		this.quantityKind = quantityKind;
		this.unitOfMeasurement = unitOfMeasurement;
	}

	public String getId() {
		return this.id;
	}

	@XmlElement(name = "id", required = true, nillable = false)
	@JsonInclude(Include.ALWAYS)
	@JsonSetter("id")
	public void setId(String id) {
		this.id = id;
	}

	@XmlElement(name = "qk", required = true)
	@JsonInclude(Include.ALWAYS)
	@JsonProperty("qk")
	public String getQuantityKind() {
		return quantityKind;
	}

	public void setQuantityKind(String quantityKind) {
		this.quantityKind = quantityKind;
	}

	@XmlElement(name = "uom", required = true)
	@JsonProperty("uom")
	@JsonInclude(Include.ALWAYS)
	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}

	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}

	@XmlElement(name = "type")
	@JsonProperty("type")
	@JsonInclude(Include.NON_NULL)
	public String getType() {
		return this.type;
	}

	public void setType(String m3LiteClass) {
		this.type = m3LiteClass;
	}

	@XmlElement(name = "endpoint")
	@JsonProperty("endpoint")
	@JsonInclude(Include.NON_NULL)
	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public Resource asResource() {
		if (id == null) {
			throw new IllegalArgumentException("Device identifier cannot be null");
		}

		if (quantityKind == null || unitOfMeasurement == null) {
			throw new IllegalArgumentException("Quantity kind and unit of measurement must be defined in device definition");
		}

		Model model = ModelFactory.createDefaultModel();

		Resource qk = model.createResource(id + ".quantity." + quantityKind)
		        .addProperty(RDF.type, M3Lite.createClass(quantityKind));
		Resource uom = model.createResource(id + ".unit." + unitOfMeasurement)
		        .addProperty(RDF.type, M3Lite.createClass(unitOfMeasurement));

		Resource device = model.createResource(id);

		if (this.type != null) {
			device.addProperty(RDF.type, M3Lite.createClass(type));
		} else {
			device.addProperty(RDF.type, Ssn.SensingDevice);
		}

		device.addProperty(IotLite.hasQuantityKind, qk)
		        .addProperty(IotLite.hasUnit, uom);

		if (this.endpoint != null) {
			Resource service = model.createResource(id + ".service");
			Literal endpointUrl = model
			        .createTypedLiteral(this.endpoint, XSDDatatype.XSDanyURI);
			service.addProperty(RDF.type, IotLite.Service)
			        .addProperty(IotLite.exposes, device)
			        .addLiteral(IotLite.endpoint, endpointUrl)
			        .addLiteral(IotLite.interfaceType, "REST");

			device.addProperty(IotLite.exposedBy, service);

		}

		return device;
	}

}
