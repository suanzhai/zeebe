/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.operate.util;

import io.zeebe.exporter.record.Record;

public abstract class IdUtil {

  public static String getId(long key, Record record) {
    return String.valueOf(key);
  }

  public static String getId(Record record) {
    return String.valueOf(record.getKey());
  }

  public static long getKey(String id) {
    return Long.valueOf(id);
  }

  public static String getVariableId(long scopeKey, String name) {
    return String.format("%s-%s", scopeKey, name);
  }

}
