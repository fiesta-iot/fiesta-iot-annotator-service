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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.fiesta_iot.utils.semantics.vocabulary.Ssn;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "device")
@XmlAccessorType(XmlAccessType.NONE)
public class DeviceStandalone extends Device {

	@XmlElement(name = "testbed")
	@JsonInclude(Include.NON_NULL)
	@JsonProperty("testbed")
	private String deployment;

	public DeviceStandalone() {
		super();
	}

	public DeviceStandalone(String id, String deployment, Location location,
	        List<SensingDevice> subDevices) {
		super(id, location, subDevices);
		this.deployment = deployment;
	}

	public DeviceStandalone(String id, String deployment, Location location,
	        String quantityKind, String unitOfMeasurement) {
		super(id, location, quantityKind, unitOfMeasurement);
		this.deployment = deployment;
	}

	public Resource asResource() {
		Resource device = super.asResource();

		if (deployment != null) {
			device.addProperty(Ssn.hasDeployment, ResourceFactory.createResource(deployment));
		}

		return device;
	}
}
