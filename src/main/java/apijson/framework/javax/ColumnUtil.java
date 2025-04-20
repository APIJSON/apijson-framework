/*Copyright ©2016 APIJSON(https://github.com/APIJSON)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package apijson.framework.javax;

import apijson.RequestMethod;
import apijson.StringUtil;
import apijson.orm.AbstractSQLConfig;
import apijson.orm.AbstractSQLExecutor;

import java.util.*;
import java.util.Map.Entry;


/**表字段相关工具类
 * @author Lemon
 * @see 先提前配置 {@link #VERSIONED_TABLE_COLUMN_MAP}, {@link #VERSIONED_KEY_COLUMN_MAP} 等，然后调用相关方法。
 * 不支持直接关联 database, schema, datasource，可以把这些与 table 拼接为一个字符串传给参数 table，格式可以是 database-schema-datasource-table
 */
public class ColumnUtil extends apijson.framework.ColumnUtil {}
