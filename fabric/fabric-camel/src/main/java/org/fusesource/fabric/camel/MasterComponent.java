/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.fabric.camel;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.util.ObjectHelper;
import org.fusesource.fabric.groups.Group;
import org.fusesource.fabric.groups.ZooKeeperGroupFactory;

/**
 * The MASTER camel component ensures that only a single endpoint in a cluster is active at any
 * point in time with all other JVMs being hot standbys which wait until the master JVM dies before
 * taking over to provide high availability of a single consumer.
 */
public class MasterComponent extends ZKComponentSupport {
    private String zkRoot = "/fabric/registry/camel/master";

    public String getZkRoot() {
        return zkRoot;
    }

    public void setZkRoot(String zkRoot) {
        this.zkRoot = zkRoot;
    }

    //  Implementation methods
    //-------------------------------------------------------------------------


    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> params) throws Exception {
        int idx = remaining.indexOf(':');
        if (idx <= 0) {
            throw new IllegalArgumentException("Missing : in URI so can't split the group name from the actual URI for '" + remaining + "'");
        }
        // we are registering a regular endpoint
        String name = remaining.substring(0, idx);
        String fabricPath = getFabricPath(name);
        String childUri = remaining.substring(idx + 1);

        Group group = ZooKeeperGroupFactory.create(getZkClient(), fabricPath);
        return new MasterEndpoint(uri, this, name, group, childUri);
    }

    protected String getFabricPath(String name) {
        String path = name;
        if (ObjectHelper.isNotEmpty(zkRoot)) {
            path = zkRoot + "/" + name;
        }
        return path;
    }

}
