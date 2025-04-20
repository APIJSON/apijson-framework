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

import static apijson.JSON.*;
import static apijson.JSONMap.KEY_ORDER;
import static apijson.JSONMap.isTableKey;
import static apijson.JSONRequest.KEY_COUNT;
import static apijson.framework.javax.APIJSONConstant.*;
import static apijson.framework.javax.APIJSONConstant.METHODS;

import java.rmi.ServerException;
import java.util.*;

import apijson.*;
import apijson.orm.JSONRequest;
import javax.servlet.http.HttpSession;

import apijson.orm.*;


/**权限验证器
 * @author Lemon
 */
public class APIJSONVerifier<T, M extends Map<String, Object>, L extends List<Object>> extends AbstractVerifier<T, M, L> {
	public static final String TAG = "APIJSONVerifier";

	public static boolean ENABLE_VERIFY_COLUMN = true;
	public static boolean ENABLE_APIJSON_ROUTER = false;

	//	由 init 方法读取数据库 Access 表来替代手动输入配置
	//	// <TableName, <METHOD, allowRoles>>
	//	// <User, <GET, [OWNER, ADMIN]>>
	//	static { //注册权限
	//		ACCESS_MAP.put(User.class.getSimpleName(), getAccessMap(User.class.getAnnotation(MethodAccess.class)));
	//		ACCESS_MAP.put(Privacy.class.getSimpleName(), getAccessMap(Privacy.class.getAnnotation(MethodAccess.class)));
	//		ACCESS_MAP.put(Moment.class.getSimpleName(), getAccessMap(Moment.class.getAnnotation(MethodAccess.class)));
	//		ACCESS_MAP.put(Comment.class.getSimpleName(), getAccessMap(Comment.class.getAnnotation(MethodAccess.class)));
	//		ACCESS_MAP.put(Verify.class.getSimpleName(), getAccessMap(Verify.class.getAnnotation(MethodAccess.class)));
	//		ACCESS_MAP.put(Login.class.getSimpleName(), getAccessMap(Login.class.getAnnotation(MethodAccess.class)));
	//	}

	public static Map<String, SortedMap<Integer, Map<String, Object>>> DOCUMENT_MAP;

	static {
		DOCUMENT_MAP = new HashMap<>();
	}

	/**初始化，加载所有权限配置和请求校验配置
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M init() throws ServerException {
		return init(false);
	}

	/**初始化，加载所有权限配置和请求校验配置
	 * @param shutdownWhenServerError
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M init(boolean shutdownWhenServerError) throws ServerException {
		return init(shutdownWhenServerError, new APIJSONCreator<T, M, L>() {

		});
	}

	/**初始化，加载所有权限配置和请求校验配置
	 * @param creator
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M init(APIJSONCreator<T, M, L> creator) throws ServerException {
		return init(false, creator);
	}

	/**初始化，加载所有权限配置和请求校验配置
	 * @param shutdownWhenServerError
	 * @param creator
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M init(
			boolean shutdownWhenServerError, APIJSONCreator<T, M, L> creator) throws ServerException {
		M result = JSON.createJSONObject();
		if (ENABLE_VERIFY_ROLE) {
			result.put(ACCESS_, initAccess(shutdownWhenServerError, creator));
		}
		if (ENABLE_VERIFY_CONTENT) {
			result.put(REQUEST_, initRequest(shutdownWhenServerError, creator));
		}
		return result;
	}

	/**初始化，加载所有权限配置
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initAccess() throws ServerException {
		return initAccess(false);
	}

	/**初始化，加载所有权限配置
	 * @param shutdownWhenServerError
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initAccess(boolean shutdownWhenServerError) throws ServerException {
		return initAccess(shutdownWhenServerError, null);
	}

	/**初始化，加载所有权限配置
	 * @param creator
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initAccess(APIJSONCreator<T, M, L> creator) throws ServerException {
		return initAccess(false, creator);
	}

	/**初始化，加载所有权限配置
	 * @param shutdownWhenServerError
	 * @param creator
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initAccess(
			boolean shutdownWhenServerError, APIJSONCreator<T, M, L> creator) throws ServerException {
		return initAccess(shutdownWhenServerError, creator, null);
	}

	/**初始化，加载所有权限配置
	 * @param shutdownWhenServerError
	 * @param creator
	 * @param table 表内自定义数据过滤条件
	 * @return
	 * @throws ServerException
	 */
	@SuppressWarnings("unchecked")
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initAccess(
			boolean shutdownWhenServerError, APIJSONCreator<T, M, L> creator, M table) throws ServerException {
		if (creator == null) {
			creator = (APIJSONCreator<T, M, L>) APIJSONApplication.DEFAULT_APIJSON_CREATOR;
		}

		boolean isAll = table == null || table.isEmpty();

		M access = isAll ? JSON.createJSONObject() : table;
		if (Log.DEBUG == false) {
			access.put(APIJSONConstant.KEY_DEBUG, 0);
		}
		M accessItem = JSON.createJSONObject();
		accessItem.put(ACCESS_, access);
		accessItem.put(apijson.JSONRequest.KEY_COUNT, 0);

		M request = JSON.createJSONObject();
		request.put(ACCESS_ + "[]", accessItem);

		M response = creator.createParser().setMethod(RequestMethod.GET).setNeedVerify(false).parseResponse(request);
		if (JSONResponse.isSuccess(response) == false) {
			Log.e(TAG, "\n\n\n\n\n !!!! 查询权限配置异常 !!!\n" + getString(response, JSONResponse.KEY_MSG) + "\n\n\n\n\n");
			onServerError("查询权限配置异常 !", shutdownWhenServerError);
		}

		L list = getJSONArray(response, ACCESS_ + "[]");
		int size = list == null ? 0 : list.size();
		if (isAll && size <= 0) {
			Log.w(TAG, "initAccess isAll && size <= 0，，没有可用的权限配置");
			return response;
		}

		Log.d(TAG, "initAccess < for ACCESS_MAP.size() = " + ACCESS_MAP.size() + " <<<<<<<<<<<<<<<<<<<<<<<<");

		Map<String, Map<RequestMethod, String[]>> newMap = new LinkedHashMap<>();
		Map<String, Map<String, Object>> fakeDeleteMap = new LinkedHashMap<>();
		Map<String, String> newTKMap = new LinkedHashMap<>(); // JSON.createJSONObject();
		Map<String, String> tableSchemaMap = new LinkedHashMap<>(); // JSON.createJSONObject();

		SortedMap<Integer, Map<String, List<String>>> versionedTableColumnMap = new TreeMap<>(ColumnUtil.DESC_COMPARATOR);
		SortedMap<Integer, Map<String, Map<String, String>>> versionedKeyColumnMap = new TreeMap<>(ColumnUtil.DESC_COMPARATOR);
		for (int i = 0; i < size; i++) {
			M item = getJSONObject(list, i);
			if (item == null) {
				continue;
			}

			Map<RequestMethod, String[]> map = new HashMap<>();
			// fastjson2 不支持
			//map.put(RequestMethod.GET, JSON.parseObject(getString(item, "get"), String[].class));
			//map.put(RequestMethod.HEAD, JSON.parseObject(getString(item, "head"), String[].class));
			//map.put(RequestMethod.GETS, JSON.parseObject(getString(item, "gets"), String[].class));
			//map.put(RequestMethod.HEADS, JSON.parseObject(getString(item, "heads"), String[].class));
			//map.put(RequestMethod.POST, JSON.parseObject(getString(item, "post"), String[].class));
			//map.put(RequestMethod.PUT, JSON.parseObject(getString(item, "put"), String[].class));
			//map.put(RequestMethod.DELETE, JSON.parseObject(getString(item, "delete"), String[].class));

			List<String> getArr = parseArray(getString(item, "get"), String.class);
			map.put(RequestMethod.GET, getArr == null ? null : getArr.toArray(new String[]{}));

			List<String> headArr = parseArray(getString(item, "head"), String.class);
			map.put(RequestMethod.HEAD, headArr == null ? null : headArr.toArray(new String[]{}));

			List<String> getsArr = parseArray(getString(item, "gets"), String.class);
			map.put(RequestMethod.GETS, getsArr == null ? null : getsArr.toArray(new String[]{}));

			List<String> headsArr = parseArray(getString(item, "heads"), String.class);
			map.put(RequestMethod.HEADS, headsArr == null ? null : headsArr.toArray(new String[]{}));

			List<String> postArr = parseArray(getString(item, "post"), String.class);
			map.put(RequestMethod.POST, postArr == null ? null : postArr.toArray(new String[]{}));

			List<String> putArr = parseArray(getString(item, "put"), String.class);
			map.put(RequestMethod.PUT, putArr == null ? null : putArr.toArray(new String[]{}));

			List<String> deleteArr = parseArray(getString(item, "delete"), String.class);
			map.put(RequestMethod.DELETE, deleteArr == null ? null : deleteArr.toArray(new String[]{}));

			String name = getString(item, "name");
			String alias = getString(item, "alias");
			String schema = getString(item, "schema");

			Map<String, Object> fakeMap = new LinkedHashMap<>();
			String deletedKey = getString(item, AbstractSQLConfig.KEY_DELETED_KEY);
			if(StringUtil.isNotEmpty(deletedKey, true)) {
				boolean containNotDeletedValue = item.containsKey(AbstractSQLConfig.KEY_NOT_DELETED_VALUE);
				Object deletedValue = getString(item, AbstractSQLConfig.KEY_DELETED_VALUE);
				if (containNotDeletedValue == false && StringUtil.isEmpty(deletedValue, true)) {
					onServerError(
							"Access表 id = " + getString(item, "id") + " 对应的 "
									+ AbstractSQLConfig.KEY_DELETED_VALUE + " 的值不能为空！或者必须包含字段 "
									+ AbstractSQLConfig.KEY_NOT_DELETED_VALUE + " ！"
							, shutdownWhenServerError
					);
				}
				fakeMap.put(AbstractSQLConfig.KEY_DELETED_KEY, deletedKey);
				fakeMap.put(AbstractSQLConfig.KEY_DELETED_VALUE, deletedValue);
				if (containNotDeletedValue) {
					fakeMap.put(AbstractSQLConfig.KEY_NOT_DELETED_VALUE, item.get(AbstractSQLConfig.KEY_NOT_DELETED_VALUE));
				}
			}

			/**TODO
			 * 以下判断写得比较复杂，因为表设计不够好，但为了兼容旧版 APIJSON 服务 和 APIAuto 工具而保留了下来。
			 * 如果是 name 为接口传参的 表对象 的 key，对应一个可缺省的 tableName，判断就会简单不少。
			 */

			if (StringUtil.isEmpty(name, true)) {
				onServerError("字段 name 的值不能为空！", shutdownWhenServerError);
			}

			if (StringUtil.isEmpty(alias, true)) {
				if (isTableKey(name) == false) {
					onServerError("name: " + name + "不合法！字段 alias 的值为空时，name 必须为合法表名！", shutdownWhenServerError);
				}

				alias = name;
			} else if (isTableKey(alias) == false) {
				onServerError("alias: " + alias + "不合法！字段 alias 的值只能为 空 或者 合法表名！", shutdownWhenServerError);
			}

			newMap.put(alias, map);
			fakeDeleteMap.put(alias, fakeMap);
			newTKMap.put(alias, name);
			tableSchemaMap.put(alias, schema);

			if (ENABLE_VERIFY_COLUMN) {
				M columns = getJSONObject(item, "columns");
				Set<Map.Entry<String, Object>> set = columns == null ? null : columns.entrySet();
				if (set != null) {

					for (Map.Entry<String, Object> entry : set) {
						Integer version = entry == null ? null : Integer.valueOf(entry.getKey()); // null is not possible
						Object val = version == null ? null : entry.getValue();
						if (val == null) {
							continue;
						}

						Map<String, Map<String, String>> kcm = new LinkedHashMap<>(); // versionedKeyColumnMap.get(version);
						Map<String, String> cm = new LinkedHashMap<>(); // kcm.get(alias);

						String[] cs = StringUtil.split(String.valueOf(val));
						List<String> l = new ArrayList<>();
						for (int j = 0; j < cs.length; j++) {
							String s = cs[j];
							Entry<String, String> ety = Pair.parseEntry(s, true);
							String k = ety == null ? null : ety.getKey();
							String v = ety == null ? null : ety.getValue();
							if (StringUtil.isName(k) == false || (v != null && StringUtil.isName(v) == false)) {
								throw new IllegalArgumentException("后端 Access 表中 name: " + name + " 对应 columns 字段的值 "
										+ version + ":value 中第 " + j + " 个字段 column:alias 中字符 " + s + " 不合法！"
										+ "alias 可缺省，但 column, alias 都必须为合法的变量名！"
										+ " ！ ！ety == null || StringUtil.isName(ety.getKey()) == false "
										+ " || (ety.getValue() != null && StringUtil.isName(ety.getValue()) == false)");
							}

							l.add(k);

							cm.put(v == null ? k : v, k);

//							if (v != null) {
////								if (kcm == null) {
////									kcm = new LinkedHashMap<>();
////								}
////								if (m == null) {
////									m = new LinkedHashMap<>();
////									kcm.put(alias, m);
////								}
//								cm.put(v, k);
//							}
						}

						Map<String, List<String>> m = new LinkedHashMap<>();
						m.put(alias, l);
						versionedTableColumnMap.put(version, m);

						kcm.put(alias, cm);
						versionedKeyColumnMap.put(version, kcm);
					}
				}
			}
		}

		if (isAll) {  // 全量更新
			ACCESS_MAP = newMap;
			ACCESS_FAKE_DELETE_MAP = fakeDeleteMap;
			APIJSONSQLConfig.TABLE_KEY_MAP = newTKMap;
			APIJSONSQLConfig.TABLE_SCHEMA_MAP = tableSchemaMap;
		} else {
			ACCESS_MAP.putAll(newMap);
			ACCESS_FAKE_DELETE_MAP.putAll(fakeDeleteMap);
			APIJSONSQLConfig.TABLE_KEY_MAP.putAll(newTKMap);
			APIJSONSQLConfig.TABLE_SCHEMA_MAP = tableSchemaMap;
		}

//		if (ENABLE_VERIFY_COLUMN) {
//			if (isAll) { // 全量更新
//				ColumnUtil.VERSIONED_TABLE_COLUMN_MAP = versionedTableColumnMap;
//				ColumnUtil.VERSIONED_KEY_COLUMN_MAP = versionedKeyColumnMap;
//			} else {
//				ColumnUtil.VERSIONED_TABLE_COLUMN_MAP.putAll(versionedTableColumnMap);
//				ColumnUtil.VERSIONED_KEY_COLUMN_MAP.putAll(versionedKeyColumnMap);
//			}
//			ColumnUtil.init();
//		}

		Log.d(TAG, "initAccess  for /> ACCESS_MAP.size() = " + ACCESS_MAP.size() + " >>>>>>>>>>>>>>>>>>>>>>>");

		return response;
	}


	/**初始化，加载所有请求校验配置
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initRequest() throws ServerException {
		return initRequest(false);
	}

	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initRequest(boolean shutdownWhenServerError) throws ServerException {
		return initRequest(shutdownWhenServerError, null);
	}

	/**初始化，加载所有请求校验配置
	 * @param creator
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initRequest(
			APIJSONCreator<T, M, L> creator) throws ServerException {
		return initRequest(false, creator);
	}

	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError
	 * @param creator
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initRequest(
			boolean shutdownWhenServerError, APIJSONCreator<T, M, L> creator) throws ServerException {
		return initRequest(shutdownWhenServerError, creator, null);
	}

	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError
	 * @param creator
	 * @param table 表内自定义数据过滤条件
	 * @return
	 * @throws ServerException
	 */
	@SuppressWarnings("unchecked")
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initRequest(
			boolean shutdownWhenServerError, APIJSONCreator<T, M, L> creator, M table) throws ServerException {
		if (creator == null) {
			creator = (APIJSONCreator<T, M, L>) APIJSONApplication.DEFAULT_APIJSON_CREATOR;
		}

		boolean isAll = table == null || table.isEmpty();
		M tblObj = createJSONObject();
		tblObj.put(KEY_ORDER, "version-,id+");
		M requestTable = isAll ? tblObj : table;
		if (Log.DEBUG == false) {
			requestTable.put(APIJSONConstant.KEY_DEBUG, 0);
		}

		M requestItem = JSON.createJSONObject();
		requestItem.put(REQUEST_, requestTable);  // 方便查找
		requestItem.put(KEY_COUNT, 0);

		M request = JSON.createJSONObject();
		request.put(REQUEST_ + "[]", requestItem);

		M response = creator.createParser().setMethod(RequestMethod.GET).setNeedVerify(false).parseResponse(request);
		if (JSONResponse.isSuccess(response) == false) {
			Log.e(TAG, "\n\n\n\n\n !!!! 查询请求校验规则配置异常 !!!\n" + getString(response, JSONResponse.KEY_MSG) + "\n\n\n\n\n");
			onServerError("查询请求校验规则配置异常 !", shutdownWhenServerError);
		}

		L list = getJSONArray(response, REQUEST_ + "[]");
		int size = list == null ? 0 : list.size();
		if (isAll && size <= 0) {
			Log.w(TAG, "initRequest isAll && size <= 0，没有可用的请求校验规则配置");
			return response;
		}

		Log.d(TAG, "initRequest < for REQUEST_MAP.size() = " + REQUEST_MAP.size() + " <<<<<<<<<<<<<<<<<<<<<<<<");

		Map<String, SortedMap<Integer, Map<String, Object>>> newMap = new LinkedHashMap<>();

		for (int i = 0; i < size; i++) {
			M item = getJSONObject(list, i);
			if (item == null) {
				continue;
			}

			String version = getString(item, "version");
			if (StringUtil.isEmpty(version, true)) {
				Log.e(TAG, "initRequest  for  StringUtil.isEmpty(version, true)，Request 表中的 version 不能为空！");
				onServerError("服务器内部错误，Request 表中的 version 不能为空！", shutdownWhenServerError);
			}

			String method = getString(item, "method");
			if (StringUtil.isEmpty(method, true)) {
				Log.e(TAG, "initRequest  for  StringUtil.isEmpty(method, true)，Request 表中的 method 不能为空！");
				onServerError("服务器内部错误，Request 表中的 method 不能为空！", shutdownWhenServerError);
			}

			String tag = getString(item, "tag");
			if (StringUtil.isEmpty(tag, true)) {
				Log.e(TAG, "initRequest  for  StringUtil.isEmpty(tag, true)，Request 表中的 tag 不能为空！");
				onServerError("服务器内部错误，Request 表中的 tag 不能为空！", shutdownWhenServerError);
			}

			M structure = JSON.parseObject(getString(item, "structure"));

			M target = null;

			if (structure != null) {
				target = structure;
				if (structure.containsKey(tag) == false) { //tag 是 Table 名或 Table[]

					boolean isArrayKey = tag.endsWith(":[]");  //  apijson.isArrayKey(tag);
					String key = isArrayKey ? tag.substring(0, tag.length() - 3) : tag;

					if (isTableKey(key)) {
						if (isArrayKey) { //自动为 tag = Comment:[] 的 { ... } 新增键值对 "Comment[]":[] 为 { "Comment[]":[], ... }
							target.put(key + "[]", JSON.createJSONArray());
						} else { //自动为 tag = Comment 的 { ... } 包一层为 { "Comment": { ... } }
							target = JSON.createJSONObject();
							target.put(tag, structure);
						}
					}
				}
			}

			if (target == null || target.isEmpty()) {
				Log.e(TAG, "initRequest  for  target == null || target.isEmpty()");
				onServerError("服务器内部错误，Request 表中的 version = " + version + ", method = " + method + ", tag = " + tag +  " 对应的 structure 不能为空！", shutdownWhenServerError);
			}

			String cacheKey = getCacheKeyForRequest(method, tag);
			SortedMap<Integer, Map<String, Object>> versionedMap = newMap.get(cacheKey);
			if (versionedMap == null) {
				versionedMap = new TreeMap<>(new Comparator<Integer>() {

					@Override
					public int compare(Integer o1, Integer o2) {
						return o2 == null ? -1 : o2.compareTo(o1);  // 降序
					}
				});
			}
			versionedMap.put(Integer.valueOf(version), item);
			newMap.put(cacheKey, versionedMap);
		}

		if (isAll) {  // 全量更新
			REQUEST_MAP = new LinkedHashMap<>();
			REQUEST_MAP.putAll(newMap);
		} else {
			REQUEST_MAP.putAll(newMap);
		}

		Log.d(TAG, "initRequest  for /> REQUEST_MAP.size() = " + REQUEST_MAP.size() + " >>>>>>>>>>>>>>>>>>>>>>>");

		return response;
	}



	/**初始化，加载所有请求校验配置
	 * @return
	 * @throws ServerException
	 */
	public static <M extends Map<String, Object>> M initDocument() throws ServerException {
		return initDocument(false);
	}
	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError
	 * @return
	 * @throws ServerException
	 */
	public static <M extends Map<String, Object>> M initDocument(boolean shutdownWhenServerError) throws ServerException {
		return initDocument(shutdownWhenServerError, null);
	}
	/**初始化，加载所有请求校验配置
	 * @param creator
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initDocument(APIJSONCreator<T, M, L> creator) throws ServerException {
		return initDocument(false, creator);
	}
	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError
	 * @param creator
	 * @return
	 * @throws ServerException
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initDocument(
			boolean shutdownWhenServerError, APIJSONCreator<T, M, L> creator) throws ServerException {
		return initDocument(shutdownWhenServerError, creator, null);
	}
	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError
	 * @param creator
	 * @param table 表内自定义数据过滤条件
	 * @return
	 * @throws ServerException
	 */
	@SuppressWarnings("unchecked")
	public static <T, M extends Map<String, Object>, L extends List<Object>> M initDocument(
			boolean shutdownWhenServerError, APIJSONCreator<T, M, L> creator, M table) throws ServerException {
		if (creator == null) {
			creator = (APIJSONCreator<T, M, L>) APIJSONApplication.DEFAULT_APIJSON_CREATOR;
		}

		boolean isAll = table == null || table.isEmpty();
		M document = isAll ? JSON.createJSONObject(new JSONRequest("apijson{}", "length(apijson)>0").setOrder("version-,id+")) : table;
		if (Log.DEBUG == false) {
			document.put(APIJSONConstant.KEY_DEBUG, 0);
		}

		M requestItem = JSON.createJSONObject();
		requestItem.put(DOCUMENT_, document);  // 方便查找
		requestItem.put(KEY_COUNT, 0);

		M request = JSON.createJSONObject();
		request.put(DOCUMENT_ + "[]", requestItem);

		M response = creator.createParser().setMethod(RequestMethod.GET).setNeedVerify(false).parseResponse(request);
		if (JSONResponse.isSuccess(response) == false) {
			Log.e(TAG, "\n\n\n\n\n !!!! 查询请求映射配置异常 !!!\n" + getString(response, JSONResponse.KEY_MSG) + "\n\n\n\n\n");
			onServerError("查询请求映射配置异常 !", shutdownWhenServerError);
		}

		L list = getJSONArray(response, DOCUMENT_ + "[]");
		int size = list == null ? 0 : list.size();
		if (isAll && size <= 0) {
			Log.w(TAG, "initDocument isAll && size <= 0，没有可用的请求映射配置");
			return response;
		}

		Log.d(TAG, "initDocument < for DOCUMENT_MAP.size() = " + DOCUMENT_MAP.size() + " <<<<<<<<<<<<<<<<<<<<<<<<");


		Map<String, SortedMap<Integer, Map<String, Object>>> newMap = new LinkedHashMap<>();

		for (int i = 0; i < size; i++) {
			M item = getJSONObject(list, i);
			if (item == null) {
				continue;
			}

			String version = getString(item, "version");
			if (StringUtil.isEmpty(version, true)) {
				onServerError("服务器内部错误，Document 表中的 id=" + getString(item, "id") + ", name=" + getString(item, "name")
						+ " 对应 version 不能为空！", shutdownWhenServerError);
			}

			String url = getString(item, "url");
			int index = url == null ? -1 : url.indexOf("/");
			if (index != 0) {
				onServerError("服务器内部错误，Document 表中的 id=" + getString(item, "id") + ", name=" + getString(item, "name") + ", url=" + url
						+ " 对应 url 值错误，必须以 / 开头！", shutdownWhenServerError);
			}

			String requestStr = getString(item, "request");

			String apijson = getString(item, "apijson");
			if (StringUtil.isEmpty(apijson)) {
				if (StringUtil.isBranchUrl(url) == false) {
					onServerError("服务器内部错误，Document 表中的 id=" + getString(item, "id") + ", name=" + getString(item, "name") + ", url=" + url
							+ " 对应 url 值错误！只允许合法的 URL 格式！", shutdownWhenServerError);
				}
			}
			else {
				if (StringUtil.isNotEmpty(requestStr)) {
					try {
						JSON.parseObject(requestStr);
					}
					catch (Exception e) {
						onServerError("服务器内部错误，Document 表中的 id=" + getString(item, "id") + ", name=" + getString(item, "name") + ", url=" + url
								+ " 对应 request 值 " + requestStr + " 错误！只允许合法的 M 格式！" + e.getMessage(), shutdownWhenServerError);
					}
				}

				try {
					JSON.parseObject(apijson);
				}
				catch (Exception e) {
					onServerError("服务器内部错误，Document 表中的 id=" + getString(item, "id") + ", name=" + getString(item, "name") + ", url=" + url
							+ " 对应 apijson 值 " + apijson + " 错误！只允许合法的 M 格式！" + e.getMessage(), shutdownWhenServerError);
				}

				index = url.lastIndexOf("/");
				String method = index < 0 ? null : url.substring(0, index);
				String tag = index < 0 ? null : url.substring(index + 1);

				index = method == null ? -1 : method.lastIndexOf("/");
				method = index < 0 ? method : method.substring(index + 1);

				if (METHODS.contains(method) == false) {
					onServerError("服务器内部错误，Document 表中的 id=" + getString(item, "id") + ", name=" + getString(item, "name") + ", url=" + url
							+ " 对应路径 /{method}/{tag} 中 method 值 " + method + " 错误！apijson 字段不为空时只允许 " + METHODS + " 中的一个！", shutdownWhenServerError);
				}

				if (StringUtil.isName(tag) == false) {
					onServerError("服务器内部错误，Document 表中的 id=" + getString(item, "id") + ", name=" + getString(item, "name") + ", url=" + url
							+ " 对应路径 /{method}/{tag} 中 tag 值 " + tag + " 错误！apijson 字段不为空时只允许变量命名格式！", shutdownWhenServerError);
				}

				String cacheKey = getCacheKeyForRequest(method, tag);
				SortedMap<Integer, Map<String, Object>> versionedMap = newMap.get(cacheKey);
				if (versionedMap == null) {
					versionedMap = new TreeMap<>(new Comparator<Integer>() {

						@Override
						public int compare(Integer o1, Integer o2) {
							return o2 == null ? -1 : o2.compareTo(o1);  // 降序
						}
					});
				}
				versionedMap.put(Integer.valueOf(version), item);
				newMap.put(cacheKey, versionedMap);
			}

		}

		if (isAll) {  // 全量更新
			DOCUMENT_MAP = newMap;
		}
		else {
			DOCUMENT_MAP.putAll(newMap);
		}

		Log.d(TAG, "initDocument  for /> DOCUMENT_MAP.size() = " + DOCUMENT_MAP.size() + " >>>>>>>>>>>>>>>>>>>>>>>");

		return response;
	}


	public static void test() throws Exception {
		testStructure();
	}

	static final String requestConfig = "{\"Comment\":{\"REFUSE\": \"id\", \"MUST\": \"userId,momentId,content\"}, \"INSERT\":{\"@role\":\"OWNER\"}}";
	static final String responseConfig = "{\"User\":{\"REMOVE\": \"phone\", \"REPLACE\":{\"sex\":2}, \"INSERT\":{\"name\":\"api\"}, \"UPDATE\":{\"verifyURLList-()\":\"verifyURLList(pictureList)\"}}}";

	/**
	 * 测试 Request 和 Response 的数据结构校验
	 * @throws Exception
	 */
	public static <T extends Object, M extends Map<String, Object>, L extends List<Object>> void testStructure() throws Exception {
		Parser<T, M, L> parser = APIJSONApplication.createParser();

		M request;
		try {
			request = JSON.parseObject("{\"Comment\":{\"userId\":0}}");
			Log.d(TAG, "test  verifyRequest = " + AbstractVerifier.verifyRequest(RequestMethod.POST, "", JSON.parseObject(requestConfig), request, parser));
		} catch (Throwable e) {
			if (e instanceof IllegalArgumentException == false || "POST请求，Comment 里面不能缺少 momentId 等[userId,momentId,content]内的任何字段！".equals(e.getMessage()) == false) {
				throw e;
			}
			Log.d(TAG, "测试 Operation.MUST 校验缺少字段：成功");
		}
		try {
			request = JSON.parseObject("{\"Comment\":{\"id\":0, \"userId\":0, \"momentId\":0, \"content\":\"apijson\"}}");
			Log.d(TAG, "test  verifyRequest = " + AbstractVerifier.verifyRequest(RequestMethod.POST, "", JSON.parseObject(requestConfig), request, parser));
		} catch (Throwable e) {
			if (e instanceof IllegalArgumentException == false || "POST请求，/Comment 不能传 id ！".equals(e.getMessage()) == false) {
				throw e;
			}
			Log.d(TAG, "测试 Operation.REFUSE 校验不允许传字段：成功");
		}
		try {
			request = JSON.parseObject("{\"Comment\":{\"userId\":0, \"momentId\":0, \"content\":\"apijson\"}}");
			Log.d(TAG, "test  verifyRequest = " + AbstractVerifier.verifyRequest(RequestMethod.POST, "", JSON.parseObject(requestConfig), request, parser));
			AssertUtil.assertEqual("OWNER", getString(request, "@role"));
			Log.d(TAG, "测试 Operation.INSERT 不存在字段时插入：成功");
		} catch (Throwable e) {
			throw e;
		}


		M response;
		try {
			response = JSON.parseObject("{\"User\":{\"userId\":0}}");
			Log.d(TAG, "test  verifyResponse = " + AbstractVerifier.verifyResponse(RequestMethod.GET, "", JSON.parseObject(responseConfig), response, parser, null));
			AssertUtil.assertEqual("verifyURLList(pictureList)", getJSONObject(response, "User").get("verifyURLList-()"));
			Log.d(TAG, "测试 Operation.UPDATE 强制插入/替换：成功");
		} catch (Throwable e) {
			throw e;
		}
		try {
			response = JSON.parseObject("{\"User\":{\"userId\":0, \"phone\":\"12345678\"}}");
			Log.d(TAG, "test  verifyResponse = " + AbstractVerifier.verifyResponse(RequestMethod.GET, "", JSON.parseObject(responseConfig), response, parser, null));
			AssertUtil.assertEqual(null, getJSONObject(response, "User").get("phone"));
			Log.d(TAG, "测试 Operation.REMOVE 强制移除：成功");
		} catch (Throwable e) {
			throw e;
		}
		try {
			response = JSON.parseObject("{\"User\":{\"userId\":0, \"phone\":\"12345678\", \"sex\":1}}");
			Log.d(TAG, "test  verifyResponse = " + AbstractVerifier.verifyResponse(RequestMethod.GET, "", JSON.parseObject(responseConfig), response, parser, null));
			AssertUtil.assertEqual("api", getJSONObject(response, "User").get("name"));
			Log.d(TAG, "测试 Operation.INSERT 不存在字段时插入：成功");
		} catch (Throwable e) {
			throw e;
		}
		try {
			response = JSON.parseObject("{\"User\":{\"id\":0, \"name\":\"tommy\", \"phone\":\"12345678\", \"sex\":1}}");
			Log.d(TAG, "test  verifyResponse = " + AbstractVerifier.verifyResponse(RequestMethod.GET, "", JSON.parseObject(responseConfig), response, parser, null));
			AssertUtil.assertEqual(2, getJSONObject(response, "User").get("sex"));
			Log.d(TAG, "测试 Operation.REPLACE 存在字段时替换：成功");
		} catch (Throwable e) {
			throw e;
		}

	}


	protected static void onServerError(String msg, boolean shutdown) throws ServerException {
		Log.e(TAG, "\n校验配置测试未通过！\n请修改 Access/Request 表里的记录！\n保证所有配置都是正确的！！！\n\n原因：\n" + msg);

		if (shutdown) {
			System.exit(1);
		} else {
			throw new ServerException(msg);
		}
	}

	protected Parser<T, M, L> parser;
	@Override
	public Parser<T, M, L> getParser() {
		if (parser == null) {
			parser = createParser();
		}
		return parser;
	}

	@Override
	public APIJSONVerifier<T, M, L> setParser(AbstractParser<T, M, L> parser) {
		this.parser = parser;
		return this;
	}

	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public APIJSONParser<T, M, L> createParser() {
		APIJSONParser<T, M, L> parser = APIJSONApplication.createParser();
		parser.setVisitor(visitor);
		return parser;
	}

	/**登录校验
	 * @author
	 * @modifier Lemon
	 * @param session
	 * @throws Exception
	 */
	public static void verifyLogin(HttpSession session) throws Exception {
		Log.d(TAG, "verifyLogin  session.getId() = " + (session == null ? null : session.getId()));
		APIJSONApplication.createVerifier().setVisitor(getVisitor(session)).verifyLogin();
	}


	/**获取来访用户的id
	 * @author Lemon
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getVisitorId(HttpSession session) {
		if (session == null) {
			return null;
		}

		T id = (T) session.getAttribute(VISITOR_ID);
		if (id == null) {
			id = (T) getVisitor(session);
			session.setAttribute(VISITOR_ID, id);
		}
		return id;
	}

	/**获取来访用户
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Visitor<T> getVisitor(HttpSession session) {
		return session == null ? null : (Visitor<T>) session.getAttribute(VISITOR_);
	}


	@Override
	public String getIdKey(String database, String schema, String datasource, String table) {
		return APIJSONSQLConfig.SIMPLE_CALLBACK.getIdKey(database, schema, datasource, table);
	}

	@Override
	public String getUserIdKey(String database, String schema, String datasource, String table) {
		return APIJSONSQLConfig.SIMPLE_CALLBACK.getUserIdKey(database, schema, datasource, table);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T newId(RequestMethod method, String database, String schema, String datasource, String table) {
		return (T) APIJSONSQLConfig.SIMPLE_CALLBACK.newId(method, database, schema, datasource, table);
	}

}
