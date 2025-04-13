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

import apijson.*;
import apijson.JSONRequest;
import apijson.orm.*;

import jakarta.servlet.http.HttpSession;

import java.rmi.ServerException;
import java.util.*;

import static apijson.JSON.*;
import static apijson.RequestMethod.*;
import static apijson.framework.APIJSONConstant.*;


/**APIJSON base controller，建议在子项目被 @RestController 注解的类继承它或通过它的实例调用相关方法
 * <br > 全通过 HTTP POST 来请求:
 * <br > 1.减少代码 - 客户端无需写 HTTP GET, HTTP PUT 等各种方式的请求代码
 * <br > 2.提高性能 - 无需 URL encode 和 decode
 * <br > 3.调试方便 - 建议使用 APIAuto-机器学习自动化接口管理工具(https://github.com/TommyLemon/APIAuto)
 * @author Lemon
 */
public class APIJSONController<T, M extends Map<String, Object>, L extends List<Object>> {
	public static final String TAG = "APIJSONController";

	@NotNull
	public static APIJSONCreator<?, ? extends Map<String, Object>, ? extends List<Object>> APIJSON_CREATOR;
	static {
		APIJSON_CREATOR = new APIJSONCreator<Object, JSONObject, JSONArray>();
	}

	public String getRequestURL() {
		return null;
	}

	public APIJSONParser<T, M, L> newParser(HttpSession session, RequestMethod method) {
		@SuppressWarnings("unchecked")
		APIJSONParser<T, M, L> parser = (APIJSONParser<T, M, L>) APIJSON_CREATOR.createParser();
		parser.setMethod(method);
		parser.setSession(session);
		parser.setRequestURL(getRequestURL());
		return parser;
	}

	public String parse(RequestMethod method, String request, HttpSession session) {
		return newParser(session, method).parse(request);
	}

	public String parseByTag(RequestMethod method, String tag, Map<String, String> params, String request, HttpSession session) {
		APIJSONParser<T, M, L> parser = newParser(null, null);
		M req = parser.wrapRequest(method, tag, JSON.parseObject(request), false, (JSONCreator<M, L>) APIJSON_CREATOR);
		if (req == null) {
			req = JSON.createJSONObject();
		}
		if (params != null && params.isEmpty() == false) {
			req.putAll(params);
		}

		return newParser(session, method).parse(req);
	}

	//通用接口，非事务型操作 和 简单事务型操作 都可通过这些接口自动化实现<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**增删改查统一入口，这个一个方法可替代以下 7 个方法，牺牲一点路由解析性能来提升一些开发效率
	 * @param method
	 * @param request
	 * @param session
	 * @return
	 */
	public String crud(String method, String request, HttpSession session) {
		if (METHODS.contains(method)) {
			return parse(RequestMethod.valueOf(method.toUpperCase()), request, session);
		}

		Parser<T, M, L> parser = newParser(null, null);
		return toJSONString(parser.newErrorResult(
				new IllegalArgumentException("URL 路径 /{method} 中 method 值 "
						+ method + " 错误！只允许 " + METHODS + " 中的一个！")
		));
	}

	/**获取
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#GET}
	 */
	public String get(String request, HttpSession session) {
		return parse(GET, request, session);
	}

	/**计数
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#HEAD}
	 */
	public String head(String request, HttpSession session) {
		return parse(HEAD, request, session);
	}

	/**限制性GET，request和response都非明文，浏览器看不到，用于对安全性要求高的GET请求
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#GETS}
	 */
	public String gets(String request, HttpSession session) {
		return parse(GETS, request, session);
	}

	/**限制性HEAD，request和response都非明文，浏览器看不到，用于对安全性要求高的HEAD请求
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#HEADS}
	 */
	public String heads(String request, HttpSession session) {
		return parse(HEADS, request, session);
	}

	/**新增
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#POST}
	 */
	public String post(String request, HttpSession session) {
		return parse(POST, request, session);
	}

	/**修改
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#PUT}
	 */
	public String put(String request, HttpSession session) {
		return parse(PUT, request, session);
	}

	/**删除
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#DELETE}
	 */
	public String delete(String request, HttpSession session) {
		return parse(DELETE, request, session);
	}

	/**支持全局事物、批量执行多条语句
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#GET}
	 */
	public String crud(String request, HttpSession session) {
		return parse(CRUD, request, session);
	}

	//通用接口，非事务型操作 和 简单事务型操作 都可通过这些接口自动化实现>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//通用接口，非事务型操作 和 简单事务型操作 都可通过这些接口自动化实现<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	/**增删改查统一入口，这个一个方法可替代以下 7 个方法，牺牲一些路由解析性能来提升一点开发效率
	 * @param method
	 * @param tag
	 * @param params
	 * @param request
	 * @param session
	 * @return
	 */
	public String crudByTag(String method, String tag, Map<String, String> params, String request, HttpSession session) {
		if (METHODS.contains(method)) {
			return parseByTag(RequestMethod.valueOf(method.toUpperCase()), tag, params, request, session);
		}

		Parser<T, M, L> parser = newParser(null, null);
		return toJSONString(parser.newErrorResult(
				new IllegalArgumentException("URL 路径 /{method}/{tag} 中 method 值 "
						+ method + " 错误！只允许 " + METHODS + " 中的一个！")
		));
	}


//	/**获取列表
//	 * @param request 只用String，避免encode后未decode
//	 * @param session
//	 * @return
//	 * @see {@link RequestMethod#GET}
//	 */
//	public String listByTag(String tag, String request, HttpSession session) {
//		return parseByTag(GET, tag + apijson.JSONObject.KEY_ARRAY, request, session);
//	}

	/**获取
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#GET}
	 */
	public String getByTag(String tag, Map<String, String> params, String request, HttpSession session) {
		return parseByTag(GET, tag, params, request, session);
	}


	/**计数
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#HEAD}
	 */
	public String headByTag(String tag, Map<String, String> params, String request, HttpSession session) {
		return parseByTag(HEAD, tag, params, request, session);
	}

	/**限制性GET，request和response都非明文，浏览器看不到，用于对安全性要求高的GET请求
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#GETS}
	 */
	public String getsByTag(String tag, Map<String, String> params, String request, HttpSession session) {
		return parseByTag(GETS, tag, params, request, session);
	}

	/**限制性HEAD，request和response都非明文，浏览器看不到，用于对安全性要求高的HEAD请求
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#HEADS}
	 */
	public String headsByTag(String tag, Map<String, String> params, String request, HttpSession session) {
		return parseByTag(HEADS, tag, params, request, session);
	}

	/**新增
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#POST}
	 */
	public String postByTag(String tag, Map<String, String> params, String request, HttpSession session) {
		return parseByTag(POST, tag, params, request, session);
	}

	/**修改
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#PUT}
	 */
	public String putByTag(String tag, Map<String, String> params, String request, HttpSession session) {
		return parseByTag(PUT, tag, params, request, session);
	}

	/**删除
	 * @param request 只用String，避免encode后未decode
	 * @param session
	 * @return
	 * @see {@link RequestMethod#DELETE}
	 */
	public String deleteByTag(String tag, Map<String, String> params, String request, HttpSession session) {
		return parseByTag(DELETE, tag, params, request, session);
	}
	//通用接口，非事务型操作 和 简单事务型操作 都可通过这些接口自动化实现<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**增删改查统一的类 RESTful API 入口，牺牲一些路由解析性能来提升一点开发效率
	 * compatCommonAPI = Log.DEBUG
	 * @param method
	 * @param tag
	 * @param params
	 * @param request
	 * @param session
	 * @return
	 */
	public String router(String method, String tag, Map<String, String> params, String request, HttpSession session) {
		return router(method, tag, params, request, session, Log.DEBUG);
	}
	/**增删改查统一的类 RESTful API 入口，牺牲一些路由解析性能来提升一点开发效率
	 * @param method
	 * @param tag
	 * @param params
	 * @param request
	 * @param session
	 * @param compatCommonAPI 兼容万能通用 API，当没有映射 APIJSON 格式请求时，自动转到万能通用 API
	 * @return
	 */
	public String router(String method, String tag, Map<String, String> params, String request, HttpSession session, boolean compatCommonAPI) {
		RequestMethod requestMethod = null;
		try {
			requestMethod = RequestMethod.valueOf(method.toUpperCase());
		} catch (Throwable e) {
			// 下方 METHODS.contains(method) 会抛异常
		}
		Parser<T, M, L> parser = newParser(session, requestMethod);

		if (METHODS.contains(method) == false) {
			return JSON.toJSONString(
					parser.newErrorResult(
							new IllegalArgumentException("URL 路径 /{method}/{tag} 中 method 值 "
									+ method + " 错误！只允许 " + METHODS + " 中的一个！"
							)
					)
			);
		}

		String t = compatCommonAPI && tag != null && tag.endsWith("[]") ? tag.substring(0, tag.length() - 2) : tag;
		if (StringUtil.isName(t) == false) {
			return JSON.toJSONString(
					parser.newErrorResult(
							new IllegalArgumentException("URL 路径 /" + method + "/{tag} 的 tag 中 "
									+ t + " 错误！tag 不能为空，且只允许变量命名格式！"
							)
					)
			);
		}

		String versionStr = params == null ? null : params.remove(APIJSONConstant.VERSION);
		Integer version;
		try {
			version = StringUtil.isEmpty(versionStr, false) ? null : Integer.valueOf(versionStr);
		}
		catch (Exception e) {
			return JSON.toJSONString(
					parser.newErrorResult(new IllegalArgumentException("URL 路径 /" + method + "/"
							+ tag + "?version=value 中 value 值 " + versionStr + " 错误！必须符合整数格式！")
					)
			);
		}

		if (version == null) {
			version = 0;
		}

		try {
			// 从 Document 查这样的接口
			String cacheKey = AbstractVerifier.getCacheKeyForRequest(method, tag);
			SortedMap<Integer, Map<String, Object>> versionedMap = APIJSONVerifier.DOCUMENT_MAP.get(cacheKey);

			Map<String, Object> result = versionedMap == null ? null : versionedMap.get(version);
			if (result == null) {  // version <= 0 时使用最新，version > 0 时使用 > version 的最接近版本（最小版本）
				Set<Map.Entry<Integer, Map<String, Object>>> set = versionedMap == null ? null : versionedMap.entrySet();

				if (set != null && set.isEmpty() == false) {
					Map.Entry<Integer, Map<String, Object>> maxEntry = null;

					for (Map.Entry<Integer, Map<String, Object>> entry : set) {
						if (entry == null || entry.getKey() == null || entry.getValue() == null) {
							continue;
						}

						if (version == null || version <= 0 || version == entry.getKey()) {  // 这里应该不会出现相等，因为上面 versionedMap.get(Integer.valueOf(version))
							maxEntry = entry;
							break;
						}

						if (entry.getKey() < version) {
							break;
						}

						maxEntry = entry;
					}

					result = maxEntry == null ? null : maxEntry.getValue();
				}

				if (result != null) {  // 加快下次查询，查到值的话组合情况其实是有限的，不属于恶意请求
					if (versionedMap == null) {
						versionedMap = new TreeMap<>((o1, o2) -> {
							return o2 == null ? -1 : o2.compareTo(o1);  // 降序
						});
					}

					versionedMap.put(version, result);
					APIJSONVerifier.DOCUMENT_MAP.put(cacheKey, versionedMap);
				}
			}

			@SuppressWarnings("unchecked")
			APIJSONCreator<T, M, L> creator = (APIJSONCreator<T, M, L>) APIJSONParser.APIJSON_CREATOR;
			if (result == null && Log.DEBUG && APIJSONVerifier.DOCUMENT_MAP.isEmpty()) {

				//获取指定的JSON结构 <<<<<<<<<<<<<<
				SQLConfig<T, M, L> config = creator.createSQLConfig().setMethod(GET).setTable(APIJSONConstant.DOCUMENT_);
				config.setPrepared(false);
				config.setColumn(Arrays.asList("request,apijson"));

				Map<String, Object> where = new HashMap<String, Object>();
				where.put("url", "/" + method + "/" + tag);
				where.put("apijson{}", "length(apijson)>0");

				if (version > 0) {
					where.put(JSONRequest.KEY_VERSION + ">=", version);
				}
				config.setWhere(where);
				config.setOrder(JSONRequest.KEY_VERSION + (version > 0 ? "+" : "-"));
				config.setCount(1);

				//too many connections error: 不try-catch，可以让客户端看到是服务器内部异常
				result = creator.createSQLExecutor().execute(config, false);

				// version, method, tag 组合情况太多了，JDK 里又没有 LRUCache，所以要么启动时一次性缓存全部后面只用缓存，要么每次都查数据库
				//			versionedMap.put(Integer.valueOf(version), result);
				//			DOCUMENT_MAP.put(cacheKey, versionedMap);
			}

			String apijson = result == null ? null : getString(result, "apijson");
			if (StringUtil.isEmpty(apijson, true)) {  //
				if (compatCommonAPI) {
					return crudByTag(method, tag, params, request, session);
				}

				throw new IllegalArgumentException("URL 路径 /" + method
						+ "/" + tag + (versionStr == null ? "" : "?version=" + versionStr) + " 对应的接口不存在！");
			}

			M rawReq = JSON.parseObject(request);
			if (rawReq == null) {
				rawReq = JSON.createJSONObject();
			}
			if (params != null && params.isEmpty() == false) {
				rawReq.putAll(params);
			}

			if (parser.isNeedVerifyContent()) {
				Verifier<T, M, L> verifier = creator.createVerifier();

				//获取指定的JSON结构 <<<<<<<<<<<<
				Map<String, Object> target = parser.getStructure("Request", method.toUpperCase(), tag, version);
				if (target == null) { //empty表示随意操作  || object.isEmpty()) {
					throw new UnsupportedOperationException("找不到 version: " + version + ", method: " + method.toUpperCase() + ", tag: " + tag + " 对应的 structure ！"
							+ "非开放请求必须是后端 Request 表中校验规则允许的操作！如果需要则在 Request 表中新增配置！");
				}

				//M clone 浅拷贝没用，Structure.parse 会导致 structure 里面被清空，第二次从缓存里取到的就是 {}
				verifier.verifyRequest(requestMethod, "", JSON.createJSONObject(target), rawReq, 0, null, null, creator);
			}

			M apijsonReq = JSON.parseObject(apijson);
			if (apijsonReq == null) {
				apijsonReq = JSON.createJSONObject();
			}

			Set<Map.Entry<String, Object>> rawSet = rawReq.entrySet();
			if (rawSet != null && rawSet.isEmpty() == false) {
				for (Map.Entry<String, Object> entry : rawSet) {
					String key = entry == null ? null : entry.getKey();
					if (key == null) {  // value 为 null 有效
						continue;
					}

					String[] pathKeys = key.split("\\.");
					//逐层到达child的直接容器JSONObject parent
					int last = pathKeys.length - 1;
					M parent = apijsonReq;
					for (int i = 0; i < last; i++) {//一步一步到达指定位置
						M p = getJSONObject(parent, pathKeys[i]);
						if (p == null) {
							p = JSON.createJSONObject();
							parent.put(key, p);
						}
						parent = p;
					}

					parent.put(pathKeys[last], entry.getValue());
				}
			}

			// 没必要，已经是预设好的实际参数了，如果要 tag 就在 apijson 字段配置  apijsonReq.put(JSONRequest.KEY_TAG, tag);

			return parser.setNeedVerifyContent(false).parse(apijsonReq);
		}
		catch (Exception e) {
			return JSON.toJSONString(parser.newErrorResult(e));
		}
	}

	//通用接口，非事务型操作 和 简单事务型操作 都可通过这些接口自动化实现>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>




	/**重新加载配置
	 * @return
	 * @see
	 * <pre>
	{
	"type": "ALL",  //重载对象，ALL, FUNCTION, REQUEST, ACCESS，非必须
	"phone": "13000082001",
	"verify": "1234567" //验证码，对应类型为 Verify.TYPE_RELOAD
	}
	 * </pre>
	 */
	public M reload(String type) {
		Parser<T, M, L> parser = newParser(null, null);
		M result = parser.newSuccessResult();

		boolean reloadAll = StringUtil.isEmpty(type, true) || "ALL".equals(type);

		if (reloadAll || "ACCESS".equals(type)) {
			try {
				if (reloadAll == false && APIJSONVerifier.ENABLE_VERIFY_ROLE == false) {
					throw new UnsupportedOperationException("AbstractVerifier.ENABLE_VERIFY_ROLE == false 时不支持校验角色权限！" +
							"如需支持则设置 AbstractVerifier.ENABLE_VERIFY_ROLE = true ！");
				}

				if (APIJSONVerifier.ENABLE_VERIFY_ROLE) {
					result.put(ACCESS_, APIJSONVerifier.initAccess());
				}
			} catch (ServerException e) {
				e.printStackTrace();
				result.put(ACCESS_, parser.newErrorResult(e));
			}
		}

		if (reloadAll || "FUNCTION".equals(type)) {
			try {
				if (reloadAll == false && APIJSONFunctionParser.ENABLE_REMOTE_FUNCTION == false) {
					throw new UnsupportedOperationException("AbstractFunctionParser.ENABLE_REMOTE_FUNCTION" +
							" == false 时不支持远程函数！如需支持则设置 AbstractFunctionParser.ENABLE_REMOTE_FUNCTION = true ！");
				}

				if (APIJSONFunctionParser.ENABLE_REMOTE_FUNCTION) {
					result.put(FUNCTION_, APIJSONFunctionParser.init());
				}
			} catch (ServerException e) {
				e.printStackTrace();
				result.put(FUNCTION_, parser.newErrorResult(e));
			}
		}

		if (reloadAll || "REQUEST".equals(type)) {
			try {
				if (reloadAll == false && APIJSONVerifier.ENABLE_VERIFY_CONTENT == false) {
					throw new UnsupportedOperationException("AbstractVerifier.ENABLE_VERIFY_CONTENT == false 时不支持校验请求传参内容！" +
							"如需支持则设置 AbstractVerifier.ENABLE_VERIFY_CONTENT = true ！");
				}

				if (APIJSONVerifier.ENABLE_VERIFY_CONTENT) {
					result.put(REQUEST_, APIJSONVerifier.initRequest());
				}
			} catch (ServerException e) {
				e.printStackTrace();
				result.put(REQUEST_, parser.newErrorResult(e));
			}
		}

		return result;
	}


	/**用户登录
	 * @param session
	 * @param visitor
	 * @param version
	 * @param format
	 * @param defaults
	 * @return 返回类型设置为 Object 是为了子类重写时可以有返回值，避免因为冲突而另写一个有返回值的登录方法
	 */
	public Object login(@NotNull HttpSession session, Visitor<Long> visitor, Integer version, Boolean format, M defaults) {
		//登录状态保存至session
		session.setAttribute(VISITOR_ID, visitor.getId()); //用户id
		session.setAttribute(VISITOR_, visitor); //用户
		session.setAttribute(VERSION, version); //全局默认版本号
		session.setAttribute(FORMAT, format); //全局默认格式化配置
		session.setAttribute(DEFAULTS, defaults); //给每个请求JSON最外层加的字段
		return null;
	}

	/**退出登录，清空session
	 * @param session
	 * @return 返回类型设置为 Object 是为了子类重写时可以有返回值，避免因为冲突而另写一个有返回值的登录方法
	 */
	public Object logout(@NotNull HttpSession session) {
		Object userId = APIJSONVerifier.getVisitorId(session);//必须在session.invalidate();前！
		Log.d(TAG, "logout  userId = " + userId + "; session.getId() = " + (session == null ? null : session.getId()));
		session.invalidate();
		return null;
	}



//	public JSONObject listMethod(String request) {
//		if (Log.DEBUG == false) {
//			return APIJSONParser.newErrorResult(new IllegalAccessException("非 DEBUG 模式下不允许使用 UnitAuto 单元测试！"));
//		}
//		return MethodUtil.listMethod(request);
//	}
//
//	public void invokeMethod(String request, HttpServletRequest servletRequest) {
//		AsyncContext asyncContext = servletRequest.startAsync();
//
//		final boolean[] called = new boolean[] { false };
//		MethodUtil.Listener<JSONObject> listener = new MethodUtil.Listener<JSONObject>() {
//
//			@Override
//			public void complete(JSONObject data, Method method, InterfaceProxy proxy, Object... extras) throws Exception {
//
//				ServletResponse servletResponse = called[0] ? null : asyncContext.getResponse();
//				if (servletResponse == null) {  //  || servletResponse.isCommitted()) {  // isCommitted 在高并发时可能不准，导致写入多次
//                    			Log.w(TAG, "invokeMethod  listener.complete  servletResponse == null || servletResponse.isCommitted() >> return;");
//                    			return;
//				}
//				called[0] = true;
//
//				servletResponse.setCharacterEncoding(servletRequest.getCharacterEncoding());
//				servletResponse.setContentType(servletRequest.getContentType());
//				servletResponse.getWriter().println(data);
//				asyncContext.complete();
//			}
//		};
//
//		if (Log.DEBUG == false) {
//			try {
//				listener.complete(MethodUtil.JSON_CALLBACK.newErrorResult(new IllegalAccessException("非 DEBUG 模式下不允许使用 UnitAuto 单元测试！")));
//			}
//			catch (Exception e1) {
//				e1.printStackTrace();
//				asyncContext.complete();
//			}
//
//			return;
//		}
//
//
//		try {
//			MethodUtil.invokeMethod(request, null, listener);
//		}
//		catch (Exception e) {
//			Log.e(TAG, "invokeMethod  try { JSONObject req = JSON.parseObject(request); ... } catch (Exception e) { \n" + e.getMessage());
//			try {
//				listener.complete(MethodUtil.JSON_CALLBACK.newErrorResult(e));
//			}
//			catch (Exception e1) {
//				e1.printStackTrace();
//				asyncContext.complete();
//			}
//		}
//	}

}
