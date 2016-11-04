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

package org.springframework.cloud.netflix.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.discovery.EurekaClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Spencer Gibb
 */
public class EurekaRegistration implements Registration {
	private static final Log log = LogFactory.getLog(EurekaRegistration.class);

	private final EurekaClient eurekaClient;
	private final AtomicReference<CloudEurekaClient> cloudEurekaClient = new AtomicReference<>();
	private final CloudEurekaInstanceConfig instanceConfig;
	private final ApplicationInfoManager applicationInfoManager;
	private HealthCheckHandler healthCheckHandler;

	public EurekaRegistration(EurekaClient eurekaClient, CloudEurekaInstanceConfig instanceConfig, ApplicationInfoManager applicationInfoManager, HealthCheckHandler healthCheckHandler) {
		this.eurekaClient = eurekaClient;
		this.instanceConfig = instanceConfig;
		this.applicationInfoManager = applicationInfoManager;
		this.healthCheckHandler = healthCheckHandler;
	}

	public CloudEurekaClient getEurekaClient() {
		if (this.cloudEurekaClient.get() == null) {
			try {
				this.cloudEurekaClient.compareAndSet(null, getTargetObject(eurekaClient, CloudEurekaClient.class));
			} catch (Exception e) {
				log.error("error getting CloudEurekaClient", e);
			}
		}
		return this.cloudEurekaClient.get();
	}

	@SuppressWarnings({"unchecked"})
	protected <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			return (T) ((Advised) proxy).getTargetSource().getTarget();
		} else {
			return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
		}
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
