/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.netflix;

import org.springframework.cloud.netflix.eureka.CloudEurekaInstanceConfig;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.HealthCheckHandler;

/**
 * @author Spencer Gibb
 */
public class EurekaRegistration {

	private final CloudEurekaInstanceConfig instanceConfig;

	private final ApplicationInfoManager applicationInfoManager;

	private HealthCheckHandler healthCheckHandler;

	public EurekaRegistration(CloudEurekaInstanceConfig instanceConfig, ApplicationInfoManager applicationInfoManager, HealthCheckHandler healthCheckHandler) {
		this.instanceConfig = instanceConfig;
		this.applicationInfoManager = applicationInfoManager;
		this.healthCheckHandler = healthCheckHandler;
	}

	public CloudEurekaInstanceConfig getInstanceConfig() {
		return instanceConfig;
	}

	public ApplicationInfoManager getApplicationInfoManager() {
		return applicationInfoManager;
	}

	public HealthCheckHandler getHealthCheckHandler() {
		return healthCheckHandler;
	}

	public void setHealthCheckHandler(HealthCheckHandler healthCheckHandler) {
		this.healthCheckHandler = healthCheckHandler;
	}

	public void setNonSecurePort(int port) {
		this.instanceConfig.setNonSecurePort(port);
	}

	public int getNonSecurePort() {
		return this.instanceConfig.getNonSecurePort();
	}
}
