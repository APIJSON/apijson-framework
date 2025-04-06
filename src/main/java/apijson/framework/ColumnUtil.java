/*Copyright ©2021 TommyLemon(https://github.com/APIJSON/apijson-column)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package apijson.framework;

import java.util.*;
import java.util.Map.Entry;

import apijson.RequestMethod;
import apijson.StringUtil;
import apijson.orm.AbstractSQLConfig;
import apijson.orm.AbstractSQLExecutor;


/**表字段相关工具类
 * @author Lemon
 * @see 先提前配置 {@link #VERSIONED_TABLE_COLUMN_MAP}, {@link #VERSIONED_KEY_COLUMN_MAP} 等，然后调用相关方法。
 * 不支持直接关联 database, schema, datasource，可以把这些与 table 拼接为一个字符串传给参数 table，格式可以是 database-schema-datasource-table
 */
public class ColumnUtil {

	/**带版本的表和字段一对多对应关系，用来做 反选字段
	 * Map<version, Map<table, [column0, column1...]>>
	 */
	public static SortedMap<Integer, Map<String, List<String>>> VERSIONED_TABLE_COLUMN_MAP;

	/**带版本的 JSON key 和表字段一对一对应关系，用来做字段名映射
	 * Map<version, Map<table, Map<key, column>>>
	 */
	public static SortedMap<Integer, Map<String, Map<String, String>>> VERSIONED_KEY_COLUMN_MAP;

	/**带版本的 JSON key 和表字段一对一对应关系，用来做字段名映射，与 VERSIONED_KEY_COLUMN_MAP 相反
	 * Map<version, Map<table, Map<column, key>>>
	 */
	private static SortedMap<Integer, Map<String, Map<String, String>>> VERSIONED_COLUMN_KEY_MAP;

	public static final Comparator<Integer> DESC_COMPARATOR = new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			return o2.compareTo(o1);
		}
	};

	static {
		VERSIONED_TABLE_COLUMN_MAP = new TreeMap<>(DESC_COMPARATOR);
		VERSIONED_KEY_COLUMN_MAP = new TreeMap<>(DESC_COMPARATOR);
		VERSIONED_COLUMN_KEY_MAP = new TreeMap<>(DESC_COMPARATOR);
	}

	/**初始化
	 */
	public static void init() {
		VERSIONED_COLUMN_KEY_MAP.clear();

		// 反过来补全 column -> key 的配置，以空间换时间
		Set<Entry<Integer, Map<String, Map<String, String>>>> set = VERSIONED_KEY_COLUMN_MAP.entrySet();
		if (set != null && set.isEmpty() == false) {

			SortedMap<Integer, Map<String, Map<String, String>>> map = new TreeMap<>(DESC_COMPARATOR);

			for (Entry<Integer, Map<String, Map<String, String>>> entry : set) {

				Map<String, Map<String, String>> tableKeyColumnMap = entry == null ? null : entry.getValue();
				Set<Entry<String, Map<String, String>>> tableKeyColumnSet = tableKeyColumnMap == null ? null : tableKeyColumnMap.entrySet();

				if (tableKeyColumnSet != null && tableKeyColumnSet.isEmpty() == false) {

					Map<String, Map<String, String>> tableColumnKeyMap = new HashMap<>();

					for (Entry<String, Map<String, String>> tableKeyColumnEntry : tableKeyColumnSet) {

						Map<String, String> keyColumnMap = tableKeyColumnEntry == null ? null : tableKeyColumnEntry.getValue();
						Set<Entry<String, String>> keyColumnSet = keyColumnMap == null ? null : keyColumnMap.entrySet();

						if (keyColumnSet != null && keyColumnSet.isEmpty() == false) {
							Map<String, String> columnKeyMap = new HashMap<>();
							for (Entry<String, String> keyColumnEntry : keyColumnSet) {
								if (keyColumnEntry == null) {
									continue;
								}

								columnKeyMap.put(keyColumnEntry.getValue(), keyColumnEntry.getKey());
							}

							tableColumnKeyMap.put(tableKeyColumnEntry.getKey(), columnKeyMap);
						}
					}

					map.put(entry.getKey(), tableColumnKeyMap);
				}
			}

			VERSIONED_COLUMN_KEY_MAP = map;
		}


		// 补全剩下未定义别名的 key，以空间换时间
		Set<Entry<Integer, Map<String, List<String>>>> allSet = VERSIONED_TABLE_COLUMN_MAP.entrySet();
		if (allSet != null && allSet.isEmpty() == false) {
			
			for (Entry<Integer, Map<String, List<String>>> entry : allSet) {
				Map<String, Map<String, String>> keyColumnMap = VERSIONED_KEY_COLUMN_MAP.get(entry.getKey());
// 没必要，没特殊配置的就原样返回，没有安全隐患，还能减少性能浪费				Map<String, Map<String, String>> columnKeyMap = VERSIONED_COLUMN_KEY_MAP.get(entry.getKey());
				if (keyColumnMap == null) {
					keyColumnMap = new LinkedHashMap<>();
					VERSIONED_KEY_COLUMN_MAP.put(entry.getKey(), keyColumnMap);
				}
//				if (columnKeyMap == null) {
//					columnKeyMap = new LinkedHashMap<>();
//					VERSIONED_COLUMN_KEY_MAP.put(entry.getKey(), columnKeyMap);
//				}
				
				Map<String, List<String>> tableKeyColumnMap = entry == null ? null : entry.getValue();
				Set<Entry<String, List<String>>> tableKeyColumnSet = tableKeyColumnMap == null ? null : tableKeyColumnMap.entrySet();

				if (tableKeyColumnSet != null && tableKeyColumnSet.isEmpty() == false) {

					for (Entry<String, List<String>> tableKeyColumnEntry : tableKeyColumnSet) {

						List<String> list = tableKeyColumnEntry == null ? null : tableKeyColumnEntry.getValue();

						if (list != null && list.isEmpty() == false) {

							Map<String, String> kcm = keyColumnMap.get(tableKeyColumnEntry.getKey());
//							Map<String, String> ckm = columnKeyMap.get(tableKeyColumnEntry.getKey());
							if (kcm == null) {
								kcm = new LinkedHashMap<>();
								keyColumnMap.put(tableKeyColumnEntry.getKey(), kcm);
							}
//							if (ckm == null) {
//								ckm = new LinkedHashMap<>();
//								columnKeyMap.put(tableKeyColumnEntry.getKey(), ckm);
//							}

							for (String column : list) {
								if (column == null) {
									continue;
								}

//								ckm.putIfAbsent(column, column);
								//FIXME 对 Comment.toId (多版本) 居然不起作用
//								if (kcm.containsValue(column) == false) {
									kcm.putIfAbsent(column, column);
//								}
							}

//							for (String column : list) {
//								if (column == null || ckm.get(column) != null) {
//									continue;
//								}
//
//								kcm.putIfAbsent(column, column);
//							}

						}
					}

				}
			}

		}

	}

	/**适配请求参数 JSON 中 @column:value 的 value 中的 key。支持 !key 反选字段 和 字段名映射
	 * @param columns
	 * @param table
	 * @param method
	 * @return
	 */
	public static List<String> compatInputColumn(List<String> columns, String table, RequestMethod method) {
		return compatInputColumn(columns, table, method, null, false);
	}

	/**适配请求参数 JSON 中 @column:value 的 value 中的 key。支持 !key 反选字段 和 字段名映射
	 * @param columns
	 * @param table
	 * @param method
	 * @param version
	 * @return
	 * @see 先提前配置 {@link #VERSIONED_TABLE_COLUMN_MAP}，然后在 {@link AbstractSQLConfig} 的子类重写 {@link AbstractSQLConfig#setColumn } 并调用这个方法，例如
	 * <pre >
	public AbstractSQLConfig<T, M, L> setColumn(List<String> column) { <br>
	return super.setColumn(ColumnUtil.compatInputColumn(column, getTable(), version)); <br>
	}
	 * </pre>
	 */
	public static List<String> compatInputColumn(List<String> columns, String table, RequestMethod method, Integer version, boolean throwWhenNoKey) {
		String[] keys = columns == null ? null : columns.toArray(new String[]{});  // StringUtil.split(c, ";");
		if (keys == null || keys.length <= 0) { // JOIN 副表可以设置 @column:"" 来指定不返回字段
			return columns != null ? columns : getClosestValue(VERSIONED_TABLE_COLUMN_MAP, version, table);
		}

		//		boolean isQueryMethod = RequestMethod.isQueryMethod(method);

		List<String> exceptColumns = new ArrayList<>(); // Map<String, String> exceptColumnMap = new HashMap<>();
		List<String> newColumns = new ArrayList<>();

		Map<String, String> keyColumnMap = getClosestValue(VERSIONED_KEY_COLUMN_MAP, version, table);
		boolean isEmpty = keyColumnMap == null || keyColumnMap.isEmpty();

		String q = "`";

		String expression;
		//...;fun0(arg0,arg1,...):fun0;fun1(arg0,arg1,...):fun1;...
		for (int i = 0; i < keys.length; i++) {

			//!column,column2,!column3,column4:alias4;fun(arg0,arg1,...)
			expression = keys[i];
			int start = expression.indexOf("(");
			int end = expression.lastIndexOf(")");
			if (start >= 0 && start < end) {
				String[] ks = StringUtil.split(expression.substring(start + 1, end));

				String expr = expression.substring(0, start + 1);
				for (int j = 0; j < ks.length; j++) {
					String ck = ks[j];
					boolean hasQuote = false;
					if (ck.endsWith("`")) {
						String nck = ck.substring(0, ck.length() - 1);
						if (nck.lastIndexOf("`") == 0) {
							ck = nck.substring(1);
							hasQuote = true;
						}
					}

					String rc = null;
					if (hasQuote || StringUtil.isName(ck)) {
						rc = isEmpty ? null : keyColumnMap.get(ck);
						if (rc == null && isEmpty == false && throwWhenNoKey) {
							throw new NullPointerException(table + ":{ @column: value } 的 value 中 " + ck + " 不合法！不允许传后端未授权访问的字段名！");
						}
					}

					expr += (j <= 0 ? "" : ",") + (hasQuote ? q : "") + (rc == null ? ck : rc) + (hasQuote ? q : "");
				}

				newColumns.add(expr + expression.substring(end));

//				newColumns.add(expression);
				continue;
			}

			String[] ckeys = StringUtil.split(expression);
			if (ckeys != null && ckeys.length > 0) {
				for (int j = 0; j < ckeys.length; j++) {
					String ck = ckeys[j];

					if (ck.startsWith("!")) {
						if (ck.length() <= 1) {
							throw new IllegalArgumentException("@column:value 的 value 中 " + ck
									+ " 不合法！ !column 不允许 column 为空字符串！column,!column2,!column3,column4:alias4 中所有 column 必须符合变量名格式！");
						}
						String c = ck.substring(1);
						if (StringUtil.isName(c) == false) {
							throw new IllegalArgumentException("@column:value 的 value 中 " + c
									+ " 不合法！ column,!column2,!column3,column4:alias4 中所有 column 必须符合变量名格式！");
						}

						String rc = isEmpty ? null : keyColumnMap.get(c);
						exceptColumns.add(rc == null ? c : rc);  // 不使用数据库别名，以免 JOIN 等复杂查询情况下报错字段不存在	exceptColumnMap.put(nc == null ? c : nc, c);  // column:alias
					} else {
						boolean hasQuote = false;
						if (ck.endsWith("`")) {
							String nck = ck.substring(0, ck.length() - 1);
							if (nck.lastIndexOf("`") == 0) {
								ck = nck.substring(1);
								hasQuote = true;
							}
						}

						String rc = null;
						if (hasQuote || StringUtil.isName(ck)) {
							rc = isEmpty ? null : keyColumnMap.get(ck);
							if (rc == null && isEmpty == false && throwWhenNoKey) {
								throw new NullPointerException(table + ":{ @column: value } 的 value 中 " + ck + " 不合法！不允许传后端未授权访问的字段名！");
							}
						}

						newColumns.add(rc == null ? ck : rc);  // 不使用数据库别名，以免 JOIN 等复杂查询情况下报错字段不存在 newColumns.add(rc == null ? ck : (isQueryMethod ? (rc + ":" + ck) : rc));
					}
				}
			}
		}

		List<String> allColumns = exceptColumns == null || exceptColumns.isEmpty() ? null : getClosestValue(VERSIONED_TABLE_COLUMN_MAP, version, table);

		if (allColumns != null && allColumns.isEmpty() == false) {

			// 不使用数据库别名，以免 JOIN 等复杂查询情况下报错字段不存在
			//			Map<String, Map<String, String>> tableColumnKeyMap = VERSIONED_COLUMN_KEY_MAP == null || VERSIONED_COLUMN_KEY_MAP.isEmpty() ? null : VERSIONED_COLUMN_KEY_MAP.get(version);
			//			Map<String, String> columnKeyMap = tableColumnKeyMap == null || tableColumnKeyMap.isEmpty() ? null : tableColumnKeyMap.get(table);

			for (String c : allColumns) {
				if (c != null && exceptColumns.contains(c) == false) {  // column:alias
					// 不使用数据库别名，以免 JOIN 等复杂查询情况下报错字段不存在		String alias = isQueryMethod == false || columnKeyMap == null || columnKeyMap.isEmpty() ? null : columnKeyMap.get(c);
					newColumns.add(c); // newColumns.add(alias == null ? c : (c + ":" + alias));
				}
			}
		}

		return newColumns;
	}


	/**适配请求参数 JSON 中 条件/赋值 键值对的 key
	 * @param key
	 * @param table
	 * @param method
	 * @return
	 */
	public static String compatInputKey(String key, String table, RequestMethod method) {
		return compatInputKey(key, table, method, null, false);
	}

	/**适配请求参数 JSON 中 条件/赋值 键值对的 key
	 * @param key
	 * @param table
	 * @param method
	 * @param version
	 * @return
	 * @see 先提前配置 {@link #VERSIONED_KEY_COLUMN_MAP}，然后在 {@link AbstractSQLConfig} 的子类重写 {@link AbstractSQLConfig#getKey } 并调用这个方法，例如
	 * <pre >
	public String getKey(String key) { <br>
	return super.getKey(ColumnUtil.compatInputKey(key, getTable(), version)); <br>
	}
	 * </pre>
	 */
	public static String compatInputKey(String key, String table, RequestMethod method, Integer version, boolean throwWhenNoKey) {
		Map<String, String> keyColumnMap = getClosestValue(VERSIONED_KEY_COLUMN_MAP, version, table);
		boolean isEmpty = keyColumnMap == null || keyColumnMap.isEmpty();
		String alias = isEmpty ? null : keyColumnMap.get(key);
		if (alias == null) {
			if (isEmpty == false && throwWhenNoKey) {
				throw new NullPointerException(table + ":{} 中不允许传 " + key + " ！");
			}
			return key;
		}

		return alias;
	}

	/**适配返回结果 JSON 中键值对的 key。可能通过不传 @column 等方式来返回原始字段名，这样就达不到隐藏真实字段名的需求了，所以只有最终这个兜底方式靠谱。
	 * @param key
	 * @param table
	 * @param method
	 * @return
	 */
	public static String compatOutputKey(String key, String table, RequestMethod method) {
		return compatOutputKey(key, table, method, null);
	}

	/**适配返回结果 JSON 中键值对的 key。可能通过不传 @column 等方式来返回原始字段名，这样就达不到隐藏真实字段名的需求了，所以只有最终这个兜底方式靠谱。
	 * @param key
	 * @param table
	 * @param method
	 * @param version
	 * @return
	 * @see 先提前配置 {@link #VERSIONED_COLUMN_KEY_MAP}，然后在 {@link AbstractSQLExecutor} 的子类重写 {@link AbstractSQLExecutor#getKey } 并调用这个方法，例如
	 * <pre >
	protected String getKey(SQLConfig<T, M, L> config, ResultSet rs, ResultSetMetaData rsmd, int tablePosition, JSONObject table,
	int columnIndex, Map<String, JSONObject> childMap) throws Exception { <br>
	return ColumnUtil.compatOutputKey(super.getKey(config, rs, rsmd, tablePosition, table, columnIndex, childMap), config.getTable(), config.getMethod(), version); <br>
	}
	 * </pre>
	 */
	public static String compatOutputKey(String key, String table, RequestMethod method, Integer version) {
		Map<String, String> columnKeyMap = getClosestValue(VERSIONED_COLUMN_KEY_MAP, version, table);
		String alias = columnKeyMap == null || columnKeyMap.isEmpty() ? null : columnKeyMap.get(key);
		return alias == null ? key : alias;
	}

	public static <T> T getClosestValue(SortedMap<Integer, Map<String, T>> versionedMap, Integer version, String table) {
		boolean isEmpty = versionedMap == null || versionedMap.isEmpty();

		Map<String, T> map = isEmpty || version == null ? null : versionedMap.get(version);
		T m = map == null ? null : map.get(table);
		if (isEmpty == false && m == null) {
			Set<Entry<Integer, Map<String, T>>> set = versionedMap.entrySet();

			T lm = null;
			for (Entry<Integer, Map<String, T>> entry : set) {
				Map<String, T> val = entry.getValue();
				m = val == null ? null : val.get(table);
				if (m == null) {
					continue;
				}

				if (version == null || version == 0) {
					//	versionedMap.put(null, val);
					return m;
				}

				Integer key = entry.getKey();
				if (key == null) {
					lm = m;
					map = val;
					continue;
				}

				if (version >= key) {
					versionedMap.put(version, val);
					return m;
				}

				break;
			}

			if (lm != null) {
				m = lm;
			}

			if (map != null) {
				versionedMap.put(version, map);
			}
		}

		return m;
	}


	/**把多个表名相关属性拼接成一个表名
	 * @param database
	 * @param schema
	 * @param datasource
	 * @param table
	 * @return
	 */
	public static String concat(String database, String schema, String datasource, String table) {
		return database + "-" + schema + "-" + datasource + "-" + table;
	}


}
