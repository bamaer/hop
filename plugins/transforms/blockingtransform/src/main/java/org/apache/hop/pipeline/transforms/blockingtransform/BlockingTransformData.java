/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.pipeline.transforms.blockingtransform;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

@SuppressWarnings("java:S1104")
public class BlockingTransformData extends BaseTransformData implements ITransformData {
  public List<FileObject> files;
  public List<Object[]> buffer;
  public List<InputStream> fis;
  public List<GZIPInputStream> gzis;
  public List<DataInputStream> dis;
  public List<Object[]> rowbuffer;

  public IRowMeta outputRowMeta;

  public int[] fieldnrs; // the corresponding field numbers
  public FileObject fil;

  public BlockingTransformData() {
    super();

    buffer = new ArrayList<>(BlockingTransformMeta.CACHE_SIZE);
    files = new ArrayList<>();
    fis = new ArrayList<>();
    dis = new ArrayList<>();
    gzis = new ArrayList<>();
    rowbuffer = new ArrayList<>();
  }
}
