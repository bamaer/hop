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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

public class AwsS3FileNameParser extends AbstractFileNameParser {

    private static final AwsS3FileNameParser INSTANCE = new AwsS3FileNameParser();

    public AwsS3FileNameParser() {
        super();
    }

    public static FileNameParser getInstance() {
        return INSTANCE;
    }

    @Override
    public FileName parseUri(VfsComponentContext vfsComponentContext, FileName fileName, String uri) throws FileSystemException {
        StringBuilder name = new StringBuilder();

        String scheme = UriParser.extractScheme(uri, name);
        UriParser.canonicalizePath(name, 0, name.length(), this);

        // Normalize separators in the path
        UriParser.fixSeparators(name);

        // Normalise the path
        FileType fileType = UriParser.normalisePath(name);

        String fullPath = name.toString();
        // Extract bucket name
        final String bucketName = UriParser.extractFirstElement(name);

        return new AwsS3FileName(scheme, bucketName, fullPath, fileType);    }
}
