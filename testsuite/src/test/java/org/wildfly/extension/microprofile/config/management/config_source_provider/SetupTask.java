/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extension.microprofile.config.management.config_source_provider;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.test.module.util.TestModule;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.microprofile.config.management.config_source.CustomConfigSource;

/**
 * Add a config-source-provider with a custom class in the microprofile-config subsystem.
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class SetupTask implements ServerSetupTask {
    private static final String MODULE_NAME = "test.custom-config-source-provider";

    private static TestModule testModule;

    @Override
    public void setup(ManagementClient managementClient, String s) throws Exception {
        URL url = ConfigSourceProviderFromClassTestCase.class.getResource("module.xml");
        File moduleXmlFile = new File(url.toURI());
        testModule = new TestModule(MODULE_NAME, moduleXmlFile);
        testModule.addResource("config-source-provider.jar")
                .addClass(CustomConfigSourceProvider.class)
                .addClass(CustomConfigSource.class);
        testModule.create();

        addConfigSourceProvider(managementClient.getControllerClient());
    }

    @Override
    public void tearDown(ManagementClient managementClient, String s) throws Exception {
        removeConfigSourceProvider(managementClient.getControllerClient());

        testModule.remove();
    }

    private void addConfigSourceProvider(ModelControllerClient client) throws IOException {
        ModelNode op;
        op = new ModelNode();
        op.get(OP_ADDR).add(SUBSYSTEM, "microprofile-config");
        op.get(OP_ADDR).add("config-source-provider", "my-config-source-config_source_provider");
        op.get(OP).set(ADD);
        op.get("class").get("module").set(MODULE_NAME);
        op.get("class").get("name").set(CustomConfigSourceProvider.class.getName());
        client.execute(op);
    }

    private void removeConfigSourceProvider(ModelControllerClient client) throws IOException {
        ModelNode op;
        op = new ModelNode();
        op.get(OP_ADDR).add(SUBSYSTEM, "microprofile-config");
        op.get(OP_ADDR).add("config-source-provider", "my-config-source-config_source_provider");
        op.get(OP).set(REMOVE);
        client.execute(op);
    }
}
