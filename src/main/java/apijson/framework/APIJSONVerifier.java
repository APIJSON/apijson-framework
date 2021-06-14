/*Copyright ©2016 TommyLemon(https://github.com/TommyLemon/APIJSON)

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

import static apijson.framework.APIJSONConstant.ACCESS_;
import static apijson.framework.APIJSONConstant.REQUEST_;
import static apijson.framework.APIJSONConstant.VISITOR_;
import static apijson.framework.APIJSONConstant.VISITOR_ID;

import java.rmi.ServerException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import apijson.JSON;
import apijson.JSONResponse;
import apijson.Log;
import apijson.NotNull;
import apijson.RequestMethod;
import apijson.RequestRole;
import apijson.StringUtil;
import apijson.orm.AbstractVerifier;
import apijson.orm.JSONRequest;
import apijson.orm.Visitor;


/**权限验证器
 * @author Lemon
 */
public class APIJSONVerifier extends AbstractVerifier<Long> {
	public static final String TAG = "APIJSONVerifier";

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

	public static APIJSONCreator APIJSON_CREATOR;
	static {
		APIJSON_CREATOR = new APIJSONCreator();
	}

	/**初始化，加载所有权限配置和请求校验配置
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init() throws ServerException {
		return init(false);
	}
	/**初始化，加载所有权限配置和请求校验配置
	 * @param shutdownWhenServerError 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init(boolean shutdownWhenServerError) throws ServerException {
		return init(shutdownWhenServerError, null);
	}
	/**初始化，加载所有权限配置和请求校验配置
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init(APIJSONCreator creator) throws ServerException {
		return init(false, creator);
	}
	/**初始化，加载所有权限配置和请求校验配置
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init(boolean shutdownWhenServerError, APIJSONCreator creator) throws ServerException {
		JSONObject result = new JSONObject(true);
		result.put(ACCESS_, initAccess(shutdownWhenServerError, creator));
		result.put(REQUEST_, initRequest(shutdownWhenServerError, creator));
		return result;
	}

	/**初始化，加载所有权限配置
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initAccess() throws ServerException {
		return initAccess(false);
	}
	/**初始化，加载所有权限配置
	 * @param shutdownWhenServerError 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initAccess(boolean shutdownWhenServerError) throws ServerException {
		return initAccess(shutdownWhenServerError, null);
	}
	/**初始化，加载所有权限配置
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initAccess(APIJSONCreator creator) throws ServerException {
		return initAccess(false, creator);
	}
	/**初始化，加载所有权限配置
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initAccess(boolean shutdownWhenServerError, APIJSONCreator creator) throws ServerException {
		return initAccess(shutdownWhenServerError, creator, null);
	}
	/**初始化，加载所有权限配置
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @param table 表内自定义数据过滤条件
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initAccess(boolean shutdownWhenServerError, APIJSONCreator creator, JSONObject table) throws ServerException {
		if (creator == null) {
			creator = APIJSON_CREATOR;
		}
		APIJSON_CREATOR = creator;


		boolean isAll = table == null || table.isEmpty();

		JSONObject access = isAll ? new JSONRequest() : table;
		if (Log.DEBUG == false) {
			access.put("debug", 0);
		}
		JSONRequest accessItem = new JSONRequest();
		accessItem.put(ACCESS_, access);

		JSONRequest request = new JSONRequest();
		request.putAll(accessItem.toArray(0, 0, ACCESS_));


		JSONObject response = creator.createParser().setMethod(RequestMethod.GET).setNeedVerify(false).parseResponse(request);
		if (JSONResponse.isSuccess(response) == false) {
			Log.e(TAG, "\n\n\n\n\n !!!! 查询权限配置异常 !!!\n" + response.getString(JSONResponse.KEY_MSG) + "\n\n\n\n\n");
			onServerError("查询权限配置异常 !", shutdownWhenServerError);
		}

		JSONArray list = response.getJSONArray(ACCESS_ + "[]");
		int size = list == null ? 0 : list.size();
		if (isAll && size <= 0) {
			Log.w(TAG, "initAccess isAll && size <= 0，，没有可用的权限配置");
			throw new NullPointerException("没有可用的权限配置");
		}

		Log.d(TAG, "initAccess < for ACCESS_MAP.size() = " + ACCESS_MAP.size() + " <<<<<<<<<<<<<<<<<<<<<<<<");

		if (isAll) {  // 全量更新
			ACCESS_MAP.clear();
		}

		JSONObject item;
		for (int i = 0; i < size; i++) {
			item = list.getJSONObject(i);
			if (item == null) {
				continue;
			}

			Map<RequestMethod, RequestRole[]> map = new HashMap<>();
			map.put(RequestMethod.GET, JSON.parseObject(item.getString("get"), RequestRole[].class));
			map.put(RequestMethod.HEAD, JSON.parseObject(item.getString("head"), RequestRole[].class));
			map.put(RequestMethod.GETS, JSON.parseObject(item.getString("gets"), RequestRole[].class));
			map.put(RequestMethod.HEADS, JSON.parseObject(item.getString("heads"), RequestRole[].class));
			map.put(RequestMethod.POST, JSON.parseObject(item.getString("post"), RequestRole[].class));
			map.put(RequestMethod.PUT, JSON.parseObject(item.getString("put"), RequestRole[].class));
			map.put(RequestMethod.DELETE, JSON.parseObject(item.getString("delete"), RequestRole[].class));

			String name = item.getString("name");
			String alias = item.getString("alias");

			/**TODO 
			 * 以下判断写得比较复杂，因为表设计不够好，但为了兼容旧版 APIJSON 服务 和 APIAuto 工具而保留了下来。
			 * 如果是 name 为接口传参的 表对象 的 key，对应一个可缺省的 tableName，判断就会简单不少。
			 */

			if (StringUtil.isEmpty(name, true)) {
				onServerError("字段 name 的值不能为空！", shutdownWhenServerError);
			}

			if (StringUtil.isEmpty(alias, true)) {
				if (JSONRequest.isTableKey(name) == false) {
					onServerError("name: " + name + "不合法！字段 alias 的值为空时，name 必须为合法表名！", shutdownWhenServerError);
				}

				ACCESS_MAP.put(name, map);
			}
			else {
				if (JSONRequest.isTableKey(alias) == false) {
					onServerError("alias: " + alias + "不合法！字段 alias 的值只能为 空 或者 合法表名！", shutdownWhenServerError);
				}

				ACCESS_MAP.put(alias, map);
			}

			APIJSONSQLConfig.TABLE_KEY_MAP.put(alias, name);
		}

		Log.d(TAG, "initAccess  for /> ACCESS_MAP.size() = " + ACCESS_MAP.size() + " >>>>>>>>>>>>>>>>>>>>>>>");

		return response;
	}


	/**初始化，加载所有请求校验配置
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initRequest() throws ServerException {
		return initRequest(false);
	}
	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initRequest(boolean shutdownWhenServerError) throws ServerException {
		return initRequest(shutdownWhenServerError, null);
	}
	/**初始化，加载所有请求校验配置
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initRequest(APIJSONCreator creator) throws ServerException {
		return initRequest(false, creator);
	}
	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initRequest(boolean shutdownWhenServerError, APIJSONCreator creator) throws ServerException {
		return initRequest(shutdownWhenServerError, creator, null);
	}
	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @param table 表内自定义数据过滤条件
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject initRequest(boolean shutdownWhenServerError, APIJSONCreator creator, JSONObject table) throws ServerException {
		if (creator == null) {
			creator = APIJSON_CREATOR;
		}
		APIJSON_CREATOR = creator;


		boolean isAll = table == null || table.isEmpty();

		JSONRequest requestItem = new JSONRequest();
		requestItem.put(REQUEST_, isAll ? new JSONRequest().setOrder("version-,id+") : table);  // 方便查找

		JSONRequest request = new JSONRequest();
		request.putAll(requestItem.toArray(0, 0, REQUEST_));


		JSONObject response = creator.createParser().setMethod(RequestMethod.GET).setNeedVerify(false).parseResponse(request);
		if (JSONResponse.isSuccess(response) == false) {
			Log.e(TAG, "\n\n\n\n\n !!!! 查询权限配置异常 !!!\n" + response.getString(JSONResponse.KEY_MSG) + "\n\n\n\n\n");
			onServerError("查询权限配置异常 !", shutdownWhenServerError);
		}

		JSONArray list = response.getJSONArray(REQUEST_ + "[]");
		int size = list == null ? 0 : list.size();
		if (isAll && size <= 0) {
			Log.w(TAG, "initRequest isAll && size <= 0，没有可用的权限配置");
			throw new NullPointerException("没有可用的权限配置");
		}

		Log.d(TAG, "initRequest < for REQUEST_MAP.size() = " + REQUEST_MAP.size() + " <<<<<<<<<<<<<<<<<<<<<<<<");

		if (isAll) {  // 全量更新
			REQUEST_MAP.clear();
		}

		JSONObject item;
		for (int i = 0; i < size; i++) {
			item = list.getJSONObject(i);
			if (item == null) {
				continue;
			}

			String version = item.getString("version");
			if (StringUtil.isEmpty(version, true)) {
				Log.e(TAG, "initRequest  for  StringUtil.isEmpty(version, true)，Request 表中的 version 不能为空！");
				onServerError("服务器内部错误，Request 表中的 version 不能为空！", shutdownWhenServerError);
			}

			String method = item.getString("method");
			if (StringUtil.isEmpty(method, true)) {
				Log.e(TAG, "initRequest  for  StringUtil.isEmpty(method, true)，Request 表中的 method 不能为空！");
				onServerError("服务器内部错误，Request 表中的 method 不能为空！", shutdownWhenServerError);
			}

			String tag = item.getString("tag");
			if (StringUtil.isEmpty(tag, true)) {
				Log.e(TAG, "initRequest  for  StringUtil.isEmpty(tag, true)，Request 表中的 tag 不能为空！");
				onServerError("服务器内部错误，Request 表中的 tag 不能为空！", shutdownWhenServerError);
			}

			JSONObject structure = JSON.parseObject(item.getString("structure"));

			JSONObject target = null;

			if (structure != null) {
				target = structure;
				if (structure.containsKey(tag) == false) { //tag 是 Table 名或 Table[]

					boolean isArrayKey = tag.endsWith(":[]");  //  JSONRequest.isArrayKey(tag);
					String key = isArrayKey ? tag.substring(0, tag.length() - 3) : tag;

					if (apijson.JSONObject.isTableKey(key)) {
						if (isArrayKey) { //自动为 tag = Comment:[] 的 { ... } 新增键值对 "Comment[]":[] 为 { "Comment[]":[], ... }
							target.put(key + "[]", new JSONArray()); 
						}
						else { //自动为 tag = Comment 的 { ... } 包一层为 { "Comment": { ... } }
							target = new JSONObject(true);
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
			SortedMap<Integer, JSONObject> versionedMap = REQUEST_MAP.get(cacheKey);
			if (versionedMap == null) {
				versionedMap = new TreeMap<>(new Comparator<Integer>() {

					@Override
					public int compare(Integer o1, Integer o2) {
						return o2 == null ? -1 : o2.compareTo(o1);  // 降序
					}
				});
			}
			versionedMap.put(Integer.valueOf(version), item);
			REQUEST_MAP.put(cacheKey, versionedMap);
		}

		Log.d(TAG, "initRequest  for /> REQUEST_MAP.size() = " + REQUEST_MAP.size() + " >>>>>>>>>>>>>>>>>>>>>>>");

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
	public static void testStructure() throws Exception {
		JSONObject request;
		try {
			request = JSON.parseObject("{\"Comment\":{\"userId\":0}}");
			Log.d(TAG, "test  verifyRequest = " + AbstractVerifier.verifyRequest(RequestMethod.POST, "", JSON.parseObject(requestConfig), request, APIJSON_CREATOR));
		} catch (Throwable e) {
			if (e instanceof IllegalArgumentException == false || "POST请求，Comment 里面不能缺少 momentId 等[userId,momentId,content]内的任何字段！".equals(e.getMessage()) == false) {
				throw e;
			}
			Log.d(TAG, "测试 Operation.MUST 校验缺少字段：成功");
		}
		try {			
			request = JSON.parseObject("{\"Comment\":{\"id\":0, \"userId\":0, \"momentId\":0, \"content\":\"apijson\"}}");
			Log.d(TAG, "test  verifyRequest = " + AbstractVerifier.verifyRequest(RequestMethod.POST, "", JSON.parseObject(requestConfig), request, APIJSON_CREATOR));
		} catch (Throwable e) {
			if (e instanceof IllegalArgumentException == false || "POST请求，/Comment 不能传 id ！".equals(e.getMessage()) == false) {
				throw e;
			}
			Log.d(TAG, "测试 Operation.REFUSE 校验不允许传字段：成功");
		}
		try {
			request = JSON.parseObject("{\"Comment\":{\"userId\":0, \"momentId\":0, \"content\":\"apijson\"}}");
			Log.d(TAG, "test  verifyRequest = " + AbstractVerifier.verifyRequest(RequestMethod.POST, "", JSON.parseObject(requestConfig), request, APIJSON_CREATOR));
			AssertUtil.assertEqual("OWNER", request.getString("@role"));
			Log.d(TAG, "测试 Operation.INSERT 不存在字段时插入：成功");
		} catch (Throwable e) {
			throw e;
		}



		JSONObject response;
		try {
			response = JSON.parseObject("{\"User\":{\"userId\":0}}");
			Log.d(TAG, "test  verifyResponse = " + AbstractVerifier.verifyResponse(RequestMethod.GET, "", JSON.parseObject(responseConfig), response, APIJSON_CREATOR, null));
			AssertUtil.assertEqual("verifyURLList(pictureList)", response.getJSONObject("User").getString("verifyURLList-()"));
			Log.d(TAG, "测试 Operation.UPDATE 强制插入/替换：成功");
		} catch (Throwable e) {
			throw e;
		}
		try {
			response = JSON.parseObject("{\"User\":{\"userId\":0, \"phone\":\"12345678\"}}");
			Log.d(TAG, "test  verifyResponse = " + AbstractVerifier.verifyResponse(RequestMethod.GET, "", JSON.parseObject(responseConfig), response, APIJSON_CREATOR, null));
			AssertUtil.assertEqual(null, response.getJSONObject("User").get("phone"));
			Log.d(TAG, "测试 Operation.REMOVE 强制移除：成功");
		} catch (Throwable e) {
			throw e;
		}
		try {
			response = JSON.parseObject("{\"User\":{\"userId\":0, \"phone\":\"12345678\", \"sex\":1}}");
			Log.d(TAG, "test  verifyResponse = " + AbstractVerifier.verifyResponse(RequestMethod.GET, "", JSON.parseObject(responseConfig), response, APIJSON_CREATOR, null));
			AssertUtil.assertEqual("api", response.getJSONObject("User").get("name"));
			Log.d(TAG, "测试 Operation.INSERT 不存在字段时插入：成功");
		} catch (Throwable e) {
			throw e;
		}
		try {
			response = JSON.parseObject("{\"User\":{\"id\":0, \"name\":\"tommy\", \"phone\":\"12345678\", \"sex\":1}}");
			Log.d(TAG, "test  verifyResponse = " + AbstractVerifier.verifyResponse(RequestMethod.GET, "", JSON.parseObject(responseConfig), response, APIJSON_CREATOR, null));
			AssertUtil.assertEqual(2, response.getJSONObject("User").get("sex"));
			Log.d(TAG, "测试 Operation.REPLACE 存在字段时替换：成功");
		} catch (Throwable e) {
			throw e;
		}

	}



	private static void onServerError(String msg, boolean shutdown) throws ServerException {
		Log.e(TAG, "\n权限配置文档测试未通过！\n请修改 Access 表里的记录！\n保证前端看到的权限配置文档是正确的！！！\n\n原因：\n" + msg);

		if (shutdown) {
			System.exit(1);	
		} else {
			throw new ServerException(msg);
		}
	}



	@NotNull
	@Override
	public APIJSONParser createParser() {
		APIJSONParser parser = (APIJSONParser) APIJSON_CREATOR.createParser();
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
		APIJSON_CREATOR.createVerifier().setVisitor(getVisitor(session)).verifyLogin();
	}


	/**获取来访用户的id
	 * @author Lemon
	 * @param session
	 * @return 
	 */
	public static long getVisitorId(HttpSession session) {
		if (session == null) {
			return 0;
		}
		Long id = (Long) session.getAttribute(VISITOR_ID);
		if (id == null) {
			Visitor<Long> v = getVisitor(session);
			id = v == null ? 0 : value(v.getId());
			session.setAttribute(VISITOR_ID, id);
		}
		return value(id);
	}
	/**获取来访用户
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Visitor<Long> getVisitor(HttpSession session) {
		return session == null ? null : (Visitor<Long>) session.getAttribute(VISITOR_);
	}

	public static long value(Long v) {
		return v == null ? 0 : v;
	}

	@Override
	public String getIdKey(String database, String schema, String table) {
		return APIJSONSQLConfig.SIMPLE_CALLBACK.getIdKey(database, schema, table);
	}
	@Override
	public String getIdKey(String database, String schema, String datasource, String table) {
		return APIJSONSQLConfig.SIMPLE_CALLBACK.getIdKey(database, schema, datasource, table);
	}
	@Override
	public String getUserIdKey(String database, String schema, String table) {
		return APIJSONSQLConfig.SIMPLE_CALLBACK.getUserIdKey(database, schema, table);
	}
	@Override
	public String getUserIdKey(String database, String schema, String datasource, String table) {
		return APIJSONSQLConfig.SIMPLE_CALLBACK.getUserIdKey(database, schema, datasource, table);
	}
	@Override
	public Object newId(RequestMethod method, String database, String schema, String table) {
		return APIJSONSQLConfig.SIMPLE_CALLBACK.newId(method, database, schema, table);
	}



}
