/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.hop.vfs;

//import org.apache.commons.collections4.MultiValuedMap;
//import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AwsS3FileSystem extends AbstractFileSystem {

    private S3Client s3Client;
//    public Map<Region, S3Client> regionClientMap = new HashMap<>();
//    public Map<String, Region> bucketRegionMap = new HashMap<>();

    protected AwsS3FileSystem(FileName rootName, FileObject parentLayer, FileSystemOptions fileSystemOptions) {
        super(rootName, parentLayer, fileSystemOptions);
    }

    @Override
    protected FileObject createFile(AbstractFileName abstractFileName) throws Exception {
        return new AwsS3FileObject(abstractFileName, this);
    }

    @Override
    protected void addCapabilities(Collection<Capability> capabilities) {
        capabilities.addAll(AwsS3FileProvider.capabilities);
    }

    public S3Client getS3Client(){
        s3Client = S3Client.builder().build();
        return s3Client;
    }

    public S3Client getS3Client(String regionName){
        s3Client = S3Client.builder().region(Region.of(regionName)).build();
        return s3Client;
    }
}
