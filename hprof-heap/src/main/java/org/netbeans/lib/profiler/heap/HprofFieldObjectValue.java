/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.heap;


/**
 *
 * @author Tomas Hurka
 */
public class HprofFieldObjectValue extends HprofFieldValue implements ObjectFieldValue {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public HprofFieldObjectValue(ClassDump cls, long offset) {
        super(cls, offset);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public long getInstanceId() {
        HprofByteBuffer dumpBuffer = classDump.getHprofBuffer();
        return dumpBuffer.getID(fileOffset + classDump.classDumpSegment.fieldValueOffset);
    }
    
    public Instance getInstance() {
        return classDump.getHprof().getInstanceByID(getInstanceId());
    }
}
