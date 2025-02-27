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
public class HprofInstanceValue extends HprofObject implements FieldValue {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    HprofField field;
    long instanceOffset;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    HprofInstanceValue(InstanceDump i, HprofField f, long fieldOffset) {
        super(fieldOffset);
        instanceOffset = i.fileOffset;
        field = f;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Instance getDefiningInstance() {
        return field.classDump.getHprof().getInstanceByOffset(new long[] {instanceOffset});
    }

    public Field getField() {
        return field;
    }

    public String getValue() {
        return getTypeValue().toString();
    }

    Object getTypeValue() {
        byte type = field.getValueType();
        HprofByteBuffer dumpBuffer = field.classDump.getHprofBuffer();

        return getTypeValue(dumpBuffer, fileOffset, type);
    }

    static Object getTypeValue(final HprofByteBuffer dumpBuffer, final long position, final byte type) {
        switch (type) {
            case HprofHeap.OBJECT:

                long obj = dumpBuffer.getID(position);

                return new Long(obj);
            case HprofHeap.BOOLEAN:

                byte b = dumpBuffer.get(position);

                return Boolean.valueOf(b != 0);
            case HprofHeap.CHAR:

                char ch = dumpBuffer.getChar(position);

                return Character.valueOf(ch);
            case HprofHeap.FLOAT:

                float f = dumpBuffer.getFloat(position);

                return new Float(f);
            case HprofHeap.DOUBLE:

                double d = dumpBuffer.getDouble(position);

                return new Double(d);
            case HprofHeap.BYTE:

                byte bt = dumpBuffer.get(position);

                return new Byte(bt);
            case HprofHeap.SHORT:

                short sh = dumpBuffer.getShort(position);

                return new Short(sh);
            case HprofHeap.INT:

                int i = dumpBuffer.getInt(position);

                return Integer.valueOf(i);
            case HprofHeap.LONG:

                long lg = dumpBuffer.getLong(position);

                return new Long(lg);
            default:
                return "Invalid type " + type; // NOI18N
        }
    }
}
