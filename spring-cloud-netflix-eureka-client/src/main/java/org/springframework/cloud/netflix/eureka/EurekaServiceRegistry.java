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

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

/**
 * @author Spencer Gibb
 */
public class EurekaServiceRegistry implements ServiceRegistry<EurekaRegistration>, ApplicationContextAware {

	private static final Log log = LogFactory.getLog(EurekaServiceRegistry.class);
	private ApplicationContext context;
	private CloudEurekaClient eurekaClient;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}


	CloudEurekaClient getEurekaClient() {
		if (this.eurekaClient == null) {
			EurekaClient client = this.context.getBean(EurekaClient.class);
			try {
				this.eurekaClient = getTargetObject(client, CloudEurekaClient.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.eurekaClient;
	}

	@SuppressWarnings({"unchecked"})
	protected <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			return (T) ((Advised) proxy).getTargetSource().getTarget();
		} else {
			return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
		}
	}

	@Override
	public void register(EurekaRegistration reg) {
		maybeInitializeClient(reg);

		if (log.isInfoEnabled()) {
			log.info("Registering application " + reg.getInstanceConfig().getAppname()
					+ " with eureka with status "
					+ reg.getInstanceConfig().getInitialStatus());
		}

		reg.getApplicationInfoManager()
				.setInstanceStatus(reg.getInstanceConfig().getInitialStatus());

		if (reg.getHealthCheckHandler() != null) {
			getEurekaClient().registerHealthCheck(reg.getHealthCheckHandler());
		}
	}
	
	private void maybeInitializeClient(EurekaRegistration reg) {
		// force initialization of possibly scoped proxies
		reg.getApplicationInfoManager().getInfo();
		getEurekaClient().getApplications();
	}
	
	@Override
	public void deregister(EurekaRegistration reg) {
		if (reg.getApplicationInfoManager().getInfo() != null) {

			if (log.isInfoEnabled()) {
				log.info("Unregistering application " + reg.getInstanceConfig().getAppname()
						+ " with eureka with status DOWN");
			}

			reg.getApplicationInfoManager().setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
		}
	}

	@Override
	public void setStatus(EurekaRegistration registration, String status) {
		InstanceInfo info = registration.getApplicationInfoManager().getInfo();

		//TODO: howto deal with delete properly?
		if ("RESET_OVERRIDE".equalsIgnoreCase(status)) {
			getEurekaClient().cancelOverrideStatus(info);
			return;
		}

		//TODO: howto deal with status types across discovery systems?
		InstanceInfo.InstanceStatus newStatus = InstanceInfo.InstanceStatus.toEnum(status);
		getEurekaClient().setStatus(newStatus, info);
	}

	@Override
	public Object getStatus(EurekaRegistration registration) {
		HashMap<String, Object> status = new HashMap<>();

		InstanceInfo info = registration.getApplicationInfoManager().getInfo();
		status.put("status", info.getStatus().toString());
		status.put("overriddenStatus", info.getOverriddenStatus().toString());

		return status;
	}

	public void close() {
		this.eurekaClient.shutdown();
	}
}
