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
    protected Map<String, String> s3ObjectMetadata;
    String bucketName, key;
    private Bucket bucket;
    private List<String> children = null;
    private S3Object s3Object;


    protected AwsS3FileObject(final AbstractFileName name, final AwsS3FileSystem fileSystem) {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        bucketName = getBucketName();
        key = getBucketRelativeS3Path();
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        LogChannel.GENERAL.logDebug("Accessing content {0} ", getQualifiedName());
        GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        ResponseInputStream inputStream = fileSystem.getS3Client().getObject(objectRequest);
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
            childrenList = processChildren(key, bucketName);
        }

        String[] childrenArr = new String[childrenList.size()];

        return childrenList.toArray(childrenArr);

    }

    private List<String> processChildren(String key, String bucketName){

        List<String> childrenList = new ArrayList<>();

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

        return childrenList;
    }

    private void getObjectsFromNonRootFolder(String key, String bucketName, List<String> childrenList, String realKey){
        // Getting files/folders in a folder/bucket
        String prefix = key.isEmpty() || key.endsWith(DELIMITER) ? key : key + DELIMITER;


        S3Client s3Client;
        GetBucketLocationRequest bucketLocationRequest = GetBucketLocationRequest.builder().bucket(bucketName).build();
        GetBucketLocationResponse bucketLocationResponse =  fileSystem.getS3Client().getBucketLocation(bucketLocationRequest);
        if(!StringUtils.isEmpty(bucketLocationResponse.locationConstraintAsString())){
            s3Client = fileSystem.getS3Client(bucketLocationResponse.locationConstraintAsString());
        }else{
            s3Client = fileSystem.getS3Client();
        }

        ListObjectsRequest objectsRequest;
        if(!prefix.isEmpty()){
            objectsRequest = ListObjectsRequest.builder().bucket(bucketName).prefix(prefix).delimiter(DELIMITER).build();
        }else{
            objectsRequest = ListObjectsRequest.builder().bucket(bucketName).build();
        }
        ListObjectsResponse objectsResponse = s3Client.listObjects(objectsRequest);
        List<S3Object> objectList = objectsResponse.contents();
        for(S3Object object : objectList){
            if(!objectsResponse.prefix().isEmpty()){
                childrenList.add(objectsResponse.prefix() + DELIMITER + object.key());
            }else{
                childrenList.add(object.key());
            }
        }
    }

    @Override
    protected void doAttach() throws Exception {
        if(isRootBucket()){
            injectType(FileType.FOLDER);
            return;
        }

        try{
            // is this an existing file?
            HeadObjectRequest objectRequest =HeadObjectRequest.builder().bucket(bucketName).key(key).build();
            s3ObjectMetadata = fileSystem.getS3Client().headObject(objectRequest).metadata();
            injectType(getName().getType());
        }catch(S3Exception e){
            // is this an existing folder?
            String keyWidthDelimiter = key + DELIMITER;
            try {
                HeadObjectRequest objectRequest =HeadObjectRequest.builder().bucket(bucketName).key(keyWidthDelimiter).build();
                s3ObjectMetadata = fileSystem.getS3Client().headObject(objectRequest).metadata();
                injectType(FileType.FOLDER);
                this.key = keyWidthDelimiter;
                try{
                    objectRequest =HeadObjectRequest.builder().bucket(bucketName).key(keyWidthDelimiter).build();
                    s3ObjectMetadata = fileSystem.getS3Client().headObject(objectRequest).metadata();
                    injectType(FileType.FOLDER);
                    this.key = keyWidthDelimiter;
                }catch(S3Exception e2){
                    System.out.println("key " + key + " in bucket " + bucketName + " doesn't exist.");
                }
            }catch(S3Exception e3){
                System.out.println("key " + key + " in bucket " + bucketName + " doesn't exist.");
            }
        }



        if(bucketName.length() > 0){
            if(this.bucket == null){
                bucket = getBucket();
            }
            // we're in a bucket's root
            if(!StringUtils.isEmpty(bucketName) && StringUtils.isEmpty(key)){
                injectType(FileType.FOLDER);
            }
            // we have a file object, now check if it is a folder.
            if(!StringUtils.isEmpty(key)){
                if(key.endsWith(DELIMITER)){
                    injectType(FileType.FOLDER);
                }else{
                    HeadObjectRequest objectRequest =HeadObjectRequest.builder().bucket(bucketName).key(key).build();
                    s3ObjectMetadata = fileSystem.getS3Client().headObject(objectRequest).metadata();
                }
            }
        }else{
            bucketName = DELIMITER;
            injectType(FileType.FOLDER);
        }
    }

    private S3Object getS3Object(String key, String bucketName){
        if(s3Object != null && s3Object.size() > 0){
            LogChannel.GENERAL.logDebug("Returning existing object {0} ", getQualifiedName());
            return s3Object;
        }else{
            ListObjectsRequest objectsRequest = ListObjectsRequest.builder().bucket(bucket.name()).build();
            ListObjectsResponse objectsResponse = fileSystem.getS3Client().listObjects(objectsRequest);
            List<S3Object> objectList = objectsResponse.contents();
            for(S3Object object : objectList){
                if(object.key().equals(key) || object.key().equals(key + DELIMITER)){
                    return object;
                }
            }
        }
        return s3Object;
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
            return getName().getPath().substring(getName().getPath().indexOf(DELIMITER, 1) + 1);
        } else {
            return "";
        }
    }

    private Bucket getBucket(){
        List<Bucket> bucketList = fileSystem.getS3Client().listBuckets().buckets();
        for(Bucket bucket : bucketList){
            if(bucket.name().equals(bucketName)){
                return bucket;
            }
        }
        return null;
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
        if(key.isEmpty()){
            return lastModified;
        }else{
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucketName).key(key).build();
            HeadObjectResponse objectResponse = fileSystem.getS3Client().headObject(headObjectRequest);
            lastModified = objectResponse.lastModified().toEpochMilli();
        }
        return lastModified;
    }

    @Override
    protected long doGetContentSize() throws Exception {
        Long contentSize = 0L;
        if(key.isEmpty()){
            return contentSize;
        }else{
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucketName).key(key).build();
            HeadObjectResponse objectResponse = fileSystem.getS3Client().headObject(headObjectRequest);
            contentSize=  objectResponse.contentLength();
        }
        return contentSize;
    }
}
