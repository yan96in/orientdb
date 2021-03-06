/*
 *
 *  *  Copyright 2014 Orient Technologies LTD (info(at)orientechnologies.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://www.orientechnologies.com
 *
 */
package com.orientechnologies.orient.client.remote;

import com.orientechnologies.orient.core.config.OStorageClusterConfiguration;
import com.orientechnologies.orient.core.conflict.ORecordConflictStrategy;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.storage.*;

import java.io.IOException;

/**
 * Remote cluster implementation
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 */
public class OClusterRemote implements OCluster {
  private String name;
  private int    id;

  /*
   * (non-Javadoc)
   * 
   * @see com.orientechnologies.orient.core.storage.OCluster#configure(com.orientechnologies.orient.core.storage.OStorage, int,
   * java.lang.String, java.lang.String, int, java.lang.Object[])
   */
  public void configure(OStorage iStorage, int iId, String iClusterName, Object... iParameters) throws IOException {
    id = iId;
    name = iClusterName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.orientechnologies.orient.core.storage.OCluster#configure(com.orientechnologies.orient.core.storage.OStorage,
   * com.orientechnologies.orient.core.config.OStorageClusterConfiguration)
   */
  public void configure(OStorage iStorage, OStorageClusterConfiguration iConfig) throws IOException {
    id = iConfig.getId();
    name = iConfig.getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.orientechnologies.orient.core.storage.OCluster#create(int)
   */
  public void create(int iStartSize) throws IOException {

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.orientechnologies.orient.core.storage.OCluster#open()
   */
  public void open() throws IOException {
  }

  public void close() throws IOException {
  }

  @Override
  public void close(boolean flush) throws IOException {
  }

  @Override
  public OPhysicalPosition allocatePosition(byte recordType) throws IOException {
    throw new UnsupportedOperationException("allocatePosition");
  }

  @Override
  public OPhysicalPosition createRecord(byte[] content, int recordVersion, byte recordType, OPhysicalPosition allocatedPosition) throws IOException {
    throw new UnsupportedOperationException("createRecord");
  }

  @Override
  public boolean deleteRecord(long clusterPosition) throws IOException {
    throw new UnsupportedOperationException("deleteRecord");
  }

  @Override
  public void updateRecord(long clusterPosition, byte[] content, int recordVersion, byte recordType) throws IOException {
    throw new UnsupportedOperationException("updateRecord");
  }

  @Override
  public void recycleRecord(long clusterPosition, byte[] content, int recordVersion, byte recordType) throws IOException {
    throw new UnsupportedOperationException("recyclePosition");
  }

  @Override
  public ORawBuffer readRecord(long clusterPosition) throws IOException {
    throw new UnsupportedOperationException("readRecord");
  }

  @Override
  public ORawBuffer readRecordIfVersionIsNotLatest(long clusterPosition, int recordVersion) throws IOException,
      ORecordNotFoundException {
    throw new UnsupportedOperationException("readRecordIfVersionIsNotLatest");
  }

  @Override
  public boolean exists() {
    throw new UnsupportedOperationException("exists");
  }

  public void delete() throws IOException {
  }

  public Object set(ATTRIBUTES iAttribute, Object iValue) throws IOException {
    return null;
  }

  @Override
  public String encryption() {
    throw new UnsupportedOperationException("encryption");
  }

  public void truncate() throws IOException {
  }

  public OPhysicalPosition getPhysicalPosition(OPhysicalPosition iPPosition) throws IOException {
    return null;
  }

  public long getEntries() {
    return 0;
  }

  @Override
  public long getTombstonesCount() {
    throw new UnsupportedOperationException("getTombstonesCount()");
  }

  public long getFirstPosition() {
    return 0;
  }

  public long getLastPosition() {
    return 0;
  }

  @Override
  public String getFileName() {
    throw new UnsupportedOperationException("getFileName()");
  }

  public int getId() {
    return id;
  }

  public void synch() throws IOException {
  }

  public String getName() {
    return name;
  }

  public long getRecordsSize() {
    throw new UnsupportedOperationException("getRecordsSize()");
  }

  public boolean isHashBased() {
    return false;
  }

  @Override
  public boolean isSystemCluster() {
    return false;
  }

  public OClusterEntryIterator absoluteIterator() {
    throw new UnsupportedOperationException("getRecordsSize()");
  }

  @Override
  public OPhysicalPosition[] higherPositions(OPhysicalPosition position) {
    throw new UnsupportedOperationException("higherPositions()");
  }

  @Override
  public OPhysicalPosition[] lowerPositions(OPhysicalPosition position) {
    throw new UnsupportedOperationException("lowerPositions()");
  }

  @Override
  public OPhysicalPosition[] ceilingPositions(OPhysicalPosition position) throws IOException {
    throw new UnsupportedOperationException("ceilingPositions()");
  }

  @Override
  public OPhysicalPosition[] floorPositions(OPhysicalPosition position) throws IOException {
    throw new UnsupportedOperationException("floorPositions()");
  }

  @Override
  public float recordGrowFactor() {
    throw new UnsupportedOperationException("recordGrowFactor()");
  }

  @Override
  public float recordOverflowGrowFactor() {
    throw new UnsupportedOperationException("recordOverflowGrowFactor()");
  }

  @Override
  public String compression() {
    throw new UnsupportedOperationException("compression()");
  }

  @Override
  public boolean hideRecord(long position) {
    throw new UnsupportedOperationException("Operation is not supported for given cluster implementation");
  }

  @Override
  public ORecordConflictStrategy getRecordConflictStrategy() {
    return null;
  }

  @Override
  public void acquireAtomicExclusiveLock() {
    throw new UnsupportedOperationException("remote cluster doesn't support atomic locking");
  }
}
