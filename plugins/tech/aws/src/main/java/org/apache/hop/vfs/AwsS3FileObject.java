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

import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.hop.core.logging.LogChannel;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.utils.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AwsS3FileObject extends AbstractFileObject<AwsS3FileSystem> {

    public static final String DELIMITER = "/";
    private AwsS3FileSystem fileSystem;
    String bucketName, key;
    private S3Client client;


    protected AwsS3FileObject(final AbstractFileName name, final AwsS3FileSystem fileSystem) {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        client = fileSystem.getS3Client();
        bucketName = getBucketName();
        key = getBucketRelativeS3Path();
        if(!StringUtils.isEmpty(bucketName)){
            client = getClient(bucketName);
        }
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        LogChannel.GENERAL.logDebug("Accessing content {0} ", getQualifiedName());
        GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        client = getClient(bucketName);
        ResponseInputStream inputStream = client.getObject(objectRequest);
        return inputStream;
    }

    @Override
    protected FileType doGetType() throws Exception {
        if(getName() instanceof AwsS3FileName ){
            return getName().getType();
        }
        if(StringUtils.isEmpty(key) || key.endsWith(DELIMITER)){
            return FileType.FOLDER;
        }else{
            return FileType.FILE;
        }
    }

    @Override
    protected String[] doListChildren() throws Exception {

        List<String> childrenList = new ArrayList<>();
        if(getType() == FileType.FOLDER || isRootBucket()){
//            childrenList = processChildren(key, bucketName);
            String realKey = key;
            if(!realKey.endsWith(DELIMITER)){
                realKey += DELIMITER;
            }

            if("".equals(key) && "".equals(bucketName)){
                // getting buckets in root folder
                ListBucketsResponse listBucketsResponse = fileSystem.getS3Client().listBuckets();
                List<Bucket> bucketList = listBucketsResponse.buckets();
                for(Bucket bucket : bucketList){
                    childrenList.add(bucket.name() + DELIMITER);
                }
            }else{
                getObjectsFromNonRootFolder(key, bucketName, childrenList, realKey);
            }
        }
        String[] childrenArr = new String[childrenList.size()];

        return childrenList.toArray(childrenArr);

    }

    private void getObjectsFromNonRootFolder(String key, String bucketName, List<String> childrenList, String realKey){

        // get the folders for this bucket
        if(key.isEmpty()){

            client = getClient(bucketName);
            ListObjectsResponse objectsResponse = getListObjectsResponse(client, bucketName);
            List<S3Object> objectList = objectsResponse.contents();

            for(S3Object object : objectList){
                if(object.key().endsWith(DELIMITER)) {
                    childrenList.add(object.key());
                }
            }
        }else{
            if(key.endsWith(DELIMITER)){
                ListObjectsResponse objectsResponse = getListObjectsResponseWithPrefix(client, bucketName, key);
                List<S3Object> objectList = objectsResponse.contents();
                for(S3Object object : objectList){
                    if(!object.key().equals(key) && !(object.key()+DELIMITER).equals(key)){
                        childrenList.add(object.key().replaceAll(objectsResponse.prefix(), ""));
                    }
                }
            }else{
                // safe to assume we can add this file if we can get a headObjectResponse
                getHeadObjectResponse(client, bucketName, key);
                childrenList.add(bucketName + DELIMITER + key);
            }
        }
    }

    @Override
    protected void doAttach() throws Exception {
        if(isRootBucket() || key.endsWith(DELIMITER)){
            injectType(FileType.FOLDER);
            return;
        }

        try{
            client = getClient(bucketName);

            // is this an existing file?
            HeadObjectResponse objectResponse = getHeadObjectResponse(client, bucketName, key);
            if(objectResponse.contentType().equals("application/x-directory")){
                injectType(FileType.FOLDER);
            }else{
                injectType(FileType.FILE);
            }
        }catch(S3Exception e){
            // did we lose the trailing delimiter at the end of the key? Let's try again.
            key = key + DELIMITER;

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucketName).key(key).build();
            HeadObjectResponse objectResponse = client.headObject(headObjectRequest);
            if(objectResponse.contentType().equals("application/x-directory")) {
                injectType(FileType.FOLDER);
            }else{
                injectType(FileType.FILE);
            }
        }
    }

    String getBucketName(){
        String bucket = getName().getPath();
        if (bucket.indexOf(DELIMITER, 1) > 1) {
            // this file is a file, to get the bucket, remove the name from the path
            bucket = bucket.substring(1, bucket.indexOf(DELIMITER, 1));
        } else {
            // this file is a bucket
            bucket = bucket.replace(DELIMITER, "");
        }
        if (bucket.startsWith("/")) {
            bucket = bucket.substring(1);
        }
        return bucket;
    }

    String getBucketRelativeS3Path(){
        if (getName().getPath().indexOf(DELIMITER, 1) >= 0) {
            String relativePath = getName().getPath().substring(getName().getPath().indexOf(DELIMITER, 1) + 1);
            return relativePath;
        } else {
            return "";
        }
    }

    protected String getQualifiedName() {
        return getQualifiedName(this);
    }

    protected String getQualifiedName(AwsS3FileObject fileObject) {
        return fileObject.bucketName + "/" + fileObject.key;
    }

    private boolean isRootBucket(){
        return "".equals(key);
    }

    @Override
    public long doGetLastModifiedTime() {
        Long lastModified = 0L;
        return getHeadObjectResponse(client, bucketName, key).lastModified().toEpochMilli();
    }

    @Override
    protected long doGetContentSize() throws Exception {
        return getHeadObjectResponse(client, bucketName, key).contentLength();
    }

    private HeadObjectResponse getHeadObjectResponse(S3Client client, String bucket, String key){
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucket).key(key).build();
        return client.headObject(headObjectRequest);
    }

    private ListObjectsResponse getListObjectsResponse(S3Client client, String bucketName){
        ListObjectsRequest objectsRequest = ListObjectsRequest.builder().bucket(bucketName).build();
        return client.listObjects(objectsRequest);
    }

    private ListObjectsResponse getListObjectsResponseWithPrefix(S3Client client, String bucketName, String prefix){
        ListObjectsRequest objectsRequest = ListObjectsRequest.builder().bucket(bucketName).prefix(prefix).build();
        return client.listObjects(objectsRequest);
    }

    private S3Client getClient(String bucket){
        GetBucketLocationRequest bucketLocationRequest = GetBucketLocationRequest.builder().bucket(bucket).build();
        GetBucketLocationResponse bucketLocationResponse =  fileSystem.getS3Client().getBucketLocation(bucketLocationRequest);
        Region region = Region.of(bucketLocationResponse.locationConstraintAsString());
        if(region != null){
            client = S3Client.builder().region(region).build();
        }else{
            client = fileSystem.getS3Client();
        }
        return client;
    }
}
