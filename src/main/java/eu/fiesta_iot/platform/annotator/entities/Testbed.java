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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.fiesta_iot.utils.semantics.vocabulary.M3Lite;
import eu.fiesta_iot.utils.semantics.vocabulary.Ssn;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "testbed")
@XmlAccessorType(XmlAccessType.NONE)
public class Testbed extends Annotatable {

	@XmlElement(name = "id", required=true)
	@JsonInclude(Include.ALWAYS)
	@JsonProperty("id")
	private String iri;
	
	@XmlElement(name = "domain_of_interest")
	@JsonProperty("domain_of_interest")
	@JsonInclude(Include.NON_NULL)
	private String domainOfInterest;
	
	// @XmlList
	@XmlElementWrapper(name = "devices")
	@XmlElement(name = "device")
	@JsonProperty("devices")
	@JsonInclude(Include.NON_NULL)
	private List<Device> devices = new ArrayList<Device>(0);
	
	private Testbed() {
		
	}
	
	public Testbed(String iri) {
		setIri(iri);
	}

	public Testbed(String iri, String domainOfInterest) {
		this(iri);
		setDomainOfInterest(domainOfInterest);
	}

	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}

	public String getDomainOfInterest() {
		return domainOfInterest;
	}

	public void setDomainOfInterest(String domainOfInterest) {
		this.domainOfInterest = domainOfInterest;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	public Resource asResource() {
		if (iri == null) {
			throw new IllegalArgumentException("Testbed identifier cannot be null");
		}
		
		Model model = ModelFactory.createDefaultModel();
		
		Resource testbed = model.createResource(iri)
		        .addProperty(RDF.type, Ssn.Deployment);
		
		if (domainOfInterest != null) {
			Resource doi = model.createResource(iri + ".domain")
			        .addProperty(RDF.type, M3Lite.createClass(domainOfInterest));
			testbed.addProperty(M3Lite.hasDomainOfInterest, doi);
		}
		
		for (Device device : devices) {
			Resource resource = device.asResource();
			resource.addProperty(Ssn.hasDeployment, testbed);
			model.add(resource.getModel());
		}
		
		return testbed;
	}
}


