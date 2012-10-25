/*******************************************************************************
 * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.cloudifysource.dsl;

import java.io.Serializable;

import org.cloudifysource.dsl.internal.CloudifyDSLEntity;
import org.cloudifysource.dsl.internal.DSLValidationContext;
import org.cloudifysource.dsl.internal.DSLValidationException;

/**
 * 
 * @author elip
 *
 */
@CloudifyDSLEntity(name = "deployment", clazz = ServiceDeployment.class, 
			allowInternalNode = true, allowRootNode = true, parent = "service")
public class ServiceDeployment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
			
	private PublicServiceDeploymentDescriptor global;
	private DedicatedServiceDeploymentDescriptor dedicated;
	
	/**
	 * Default deployment will be dedicated
	 */
	public ServiceDeployment() {
		dedicated = new DedicatedServiceDeploymentDescriptor();
	}
	
	public PublicServiceDeploymentDescriptor getGlobal() {
		return global;
	}

	public void setGlobal(final PublicServiceDeploymentDescriptor global) {
		this.global = global;
	}

	public DedicatedServiceDeploymentDescriptor getDedicated() {
		return dedicated;
	}

	public void setDedicated(final DedicatedServiceDeploymentDescriptor dedicated) {
		this.dedicated = dedicated;
	}
	
	@DSLValidation
	void validateDefaultValues(final DSLValidationContext validationContext)
			throws DSLValidationException {
		
		if (global != null && dedicated != null) {
			throw new DSLValidationException("cannot define both global and dedicated deployment types. " +
					"please choose one or the other");
		}
	}



}
