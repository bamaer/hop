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
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
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

        if(StringUtils.isEmpty(key) && StringUtils.isEmpty(bucketName)) {
            ListBucketsResponse listBucketsResponse = client.listBuckets();
            List<Bucket> bucketList = listBucketsResponse.buckets();
            for (Bucket bucket : bucketList) {
                childrenList.add(bucket.name());
            }
        }else{
            getObjectsFromNonRootFolder(bucketName, key, childrenList);
        }

        String[] childrenArr = new String[childrenList.size()];

        return childrenList.toArray(childrenArr);

    }

    private void getObjectsFromNonRootFolder(String bucketName, String key, List<String> childrenList) {

        client = getClient(bucketName);

        // we didn't receive a key.
        // check if we have any folders (prefixes) that need to be processed.
        // after the prefixes/folders, add all files in this (root) folder to the list.
        if(StringUtils.isEmpty(key)){
            // even though "/" is the default delimiter, it needs to be explicitly added to this ListObjectsRequest
            ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder().bucket(bucketName).delimiter(DELIMITER).build();
            ListObjectsResponse listObjectsResponse = client.listObjects(listObjectsRequest);
            if(!StringUtils.isEmpty(listObjectsResponse.prefix()) || listObjectsResponse.commonPrefixes().size() > 0){
                if(StringUtils.isEmpty(listObjectsResponse.prefix())){
                    ListObjectsRequest prefixListRequest = ListObjectsRequest.builder().bucket(bucketName).prefix(listObjectsRequest.prefix()).build();
                    ListObjectsResponse prefixResponse = client.listObjects(prefixListRequest);
                    System.out.println("bucket " + bucketName + " contains folder " + prefixResponse.prefix());
                    if(!StringUtils.isEmpty(prefixResponse.prefix())){
                        childrenList.add(prefixResponse.prefix());
                    }
                }
                if(listObjectsResponse.commonPrefixes().size() > 0){
                    for(CommonPrefix prefix : listObjectsResponse.commonPrefixes()){
                        System.out.println("bucket " + bucketName + " contains common prefix " + prefix.prefix());
                        if(!StringUtils.isEmpty(prefix.prefix())){
                            childrenList.add(prefix.prefix());
                        }
                    }
                }
            }
            List<S3Object> objectList = listObjectsResponse.contents();
            for(S3Object object : objectList){
                childrenList.add(object.key());
            }
        // keys ending in the delimiter "?" are easy, just add their children to the list.
        // TODO: run prefix check here as well?
        }else if(key.endsWith(DELIMITER)){
            ListObjectsResponse objectsResponse = getListObjectsResponseWithPrefix(client, bucketName, key);
            List<S3Object> objectList = objectsResponse.contents();
            for(S3Object object : objectList){
                if(!object.key().equals(key) && !(object.key()+DELIMITER).equals(key)){
                    childrenList.add(object.key().replaceAll(objectsResponse.prefix(), ""));
                }
            }
        // we have a key that could be either a file or a folder, let's find out.
        }else {
            try{
                // is it a folder? check if we can find a list of objects with the current key + DELIMITER
                ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder().bucket(bucketName).prefix(key + DELIMITER).delimiter(DELIMITER).build();
                ListObjectsResponse listObjectsResponse = client.listObjects(listObjectsRequest);
                List<S3Object> objectList = listObjectsResponse.contents();

                // check if we have any subfolders to process and add them to the list.
                for(CommonPrefix prefix : listObjectsResponse.commonPrefixes()){
                    childrenList.add(prefix.prefix().replaceAll(key + DELIMITER, ""));
                }
                // now add the files we found at this level
                for(S3Object object : objectList){
                    if(!object.key().replaceAll(DELIMITER, "").equals(key)){
                        childrenList.add(object.key().replaceAll(key + DELIMITER, ""));
                    }
                }
            }catch(NoSuchKeyException e){
                HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucketName).key(bucketName).build();
                HeadObjectResponse headObjectResponse = client.headObject(headObjectRequest);
            }
        }
    }

    @Override
    protected void doAttach() throws Exception {

        if(StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(key)){
            injectType(FileType.FOLDER);
        }else{
            // keys ending with delimiters are folders
            if(key.endsWith(DELIMITER)){
                injectType(FileType.FOLDER);
            }else{
                // if the key doesn't end with the delimiter, we need to check
                ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder().bucket(bucketName).prefix(key).build();
                ListObjectsResponse listObjectsResponse = client.listObjects(listObjectsRequest);

                try{
                    HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucketName).key(key).build();
                    HeadObjectResponse headObjectResponse = client.headObject(headObjectRequest);
                    injectType(FileType.FILE);
                }catch(NoSuchKeyException e){
                    injectType(FileType.FOLDER);
                }
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
        if(!StringUtils.isEmpty(key)){
            try{
                lastModified = getHeadObjectResponse(client, bucketName, key).lastModified().toEpochMilli();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return lastModified;
    }

    @Override
    protected long doGetContentSize() throws Exception {
        Long contentLength = 0L;
        if(!key.isEmpty()){
            try{
                contentLength = getHeadObjectResponse(client, bucketName, key).contentLength();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return contentLength;
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

        if(!StringUtils.isEmpty(bucketLocationResponse.locationConstraintAsString())){
            Region region = Region.of(bucketLocationResponse.locationConstraintAsString());
            client = fileSystem.getS3Client(region);
        }else{
            client = fileSystem.getS3Client();
        }
        return client;
    }
}
