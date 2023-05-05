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

import static apijson.RequestMethod.DELETE;
import static apijson.RequestMethod.GET;
import static apijson.RequestMethod.GETS;
import static apijson.RequestMethod.HEAD;
import static apijson.RequestMethod.HEADS;
import static apijson.RequestMethod.POST;
import static apijson.RequestMethod.PUT;
import static apijson.RequestMethod.CRUD;
import static apijson.framework.APIJSONConstant.ACCESS_;
import static apijson.framework.APIJSONConstant.METHODS;
import static apijson.framework.APIJSONConstant.DEFAULTS;
import static apijson.framework.APIJSONConstant.FORMAT;
import static apijson.framework.APIJSONConstant.FUNCTION_;
import static apijson.framework.APIJSONConstant.REQUEST_;
import static apijson.framework.APIJSONConstant.VERSION;
import static apijson.framework.APIJSONConstant.VISITOR_;
import static apijson.framework.APIJSONConstant.VISITOR_ID;

import java.lang.reflect.Method;
import java.rmi.ServerException;
import java.util.Map;



import com.alibaba.fastjson.JSONObject;

import apijson.JSON;
import apijson.Log;
import apijson.NotNull;
import apijson.RequestMethod;
import apijson.StringUtil;
import apijson.orm.AbstractParser;
import apijson.orm.Parser;
import apijson.orm.Visitor;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import unitauto.MethodUtil;
import unitauto.MethodUtil.InterfaceProxy;


/**APIJSON base controller，建议在子项目被 @RestController 注解的类继承它或通过它的实例调用相关方法
 * <br > 全通过 HTTP POST 来请求:
 * <br > 1.减少代码 - 客户端无需写 HTTP GET, HTTP PUT 等各种方式的请求代码
 * <br > 2.提高性能 - 无需 URL encode 和 decode
 * <br > 3.调试方便 - 建议使用 APIAuto-机器学习自动化接口管理工具(https://github.com/TommyLemon/APIAuto)
 * @author Lemon
 */
public class APIJSONController<T extends Object> {
	public static final String TAG = "APIJSONController";
	
	@NotNull
	public static APIJSONCreator<? extends Object> APIJSON_CREATOR;
	static {
		APIJSON_CREATOR = new APIJSONCreator<>();
	}
	
	public String getRequestURL() {
		return null;
	}

	public Parser<T> newParser(HttpSession session, RequestMethod method) {
		@SuppressWarnings("unchecked")
		Parser<T> parser = (Parser<T>) APIJSON_CREATOR.createParser();
		parser.setMethod(method);
		if (parser instanceof APIJSONParser) {
			((APIJSONParser<T>) parser).setSession(session);
		}
		// 可以更方便地通过日志排查错误
		if (parser instanceof AbstractParser) {
			((AbstractParser<T>) parser).setRequestURL(getRequestURL());
		}
		return parser;
	}

	public String parse(RequestMethod method, String request, HttpSession session) {
		return newParser(session, method).parse(request);
	}
	
	public String parseByTag(RequestMethod method, String tag, Map<String, String> params, String request, HttpSession session) {
		
		JSONObject req = AbstractParser.wrapRequest(method, tag, JSON.parseObject(request), false);
		if (req == null) {
			req = new JSONObject(true);
		}
		if (params != null && params.isEmpty() == false) {
			req.putAll(params);
		}
		
		return newParser(session, method).parse(req);
	}

	//通用接口，非事务型操作 和 简单事务型操作 都可通过这些接口自动化实现<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**增删改查统一入口，这个一个方法可替代以下 7 个方法，牺牲一些路由解析性能来提升一点开发效率
	 * @param method
	 * @param tag
	 * @param params
	 * @param request
	 * @param session
	 * @return
	 */
	public String crud(String method, String request, HttpSession session) {
		if (METHODS.contains(method)) {
			return parse(RequestMethod.valueOf(method.toUpperCase()), request, session);
		}
		
		return APIJSONParser.newErrorResult(new IllegalArgumentException("URL 路径 /{method} 中 method 值 " + method
				+ " 错误！只允许 " + METHODS + " 中的一个！")).toJSONString();
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
		
		return APIJSONParser.newErrorResult(new IllegalArgumentException("URL 路径 /{method}/{tag} 中 method 值 " + method
				+ " 错误！只允许 " + METHODS + " 中的一个！")).toJSONString();
	}

	
//	/**获取列表
//	 * @param request 只用String，避免encode后未decode
//	 * @param session
//	 * @return
//	 * @see {@link RequestMethod#GET}
//	 */
//	public String listByTag(String tag, String request, HttpSession session) {
//		return parseByTag(GET, tag + JSONRequest.KEY_ARRAY, request, session);
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

	//通用接口，非事务型操作 和 简单事务型操作 都可通过这些接口自动化实现>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>




	/**重新加载配置
	 * @param request
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
	public JSONObject reload(String type) {
		JSONObject result = APIJSONParser.newSuccessResult();

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
				result.put(ACCESS_, APIJSONParser.newErrorResult(e));
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
				result.put(FUNCTION_, APIJSONParser.newErrorResult(e));
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
				result.put(REQUEST_, APIJSONParser.newErrorResult(e));
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
	public Object login(@NotNull HttpSession session, Visitor<Long> visitor, Integer version, Boolean format, JSONObject defaults) {
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



	public JSONObject listMethod(String request) {
		if (Log.DEBUG == false) {
			return APIJSONParser.newErrorResult(new IllegalAccessException("非 DEBUG 模式下不允许使用 UnitAuto 单元测试！"));
		}
		return MethodUtil.listMethod(request);
	}

	public void invokeMethod(String request, HttpServletRequest servletRequest) {
		AsyncContext asyncContext = servletRequest.startAsync();

		final boolean[] called = new boolean[] { false };
		MethodUtil.Listener<JSONObject> listener = new MethodUtil.Listener<JSONObject>() {

			@Override
			public void complete(JSONObject data, Method method, InterfaceProxy proxy, Object... extras) throws Exception {
				
				ServletResponse servletResponse = called[0] ? null : asyncContext.getResponse();
				if (servletResponse == null) {  //  || servletResponse.isCommitted()) {  // isCommitted 在高并发时可能不准，导致写入多次
                    			Log.w(TAG, "invokeMethod  listener.complete  servletResponse == null || servletResponse.isCommitted() >> return;");
                    			return;
				}
				called[0] = true;

				servletResponse.setCharacterEncoding(servletRequest.getCharacterEncoding());
				servletResponse.setContentType(servletRequest.getContentType());
				servletResponse.getWriter().println(data);
				asyncContext.complete();
			}
		};
		
		if (Log.DEBUG == false) {
			try {
				listener.complete(MethodUtil.JSON_CALLBACK.newErrorResult(new IllegalAccessException("非 DEBUG 模式下不允许使用 UnitAuto 单元测试！")));
			}
			catch (Exception e1) {
				e1.printStackTrace();
				asyncContext.complete();
			}
			
			return;
		}
		

		try {
			MethodUtil.invokeMethod(request, null, listener);
		}
		catch (Exception e) {
			Log.e(TAG, "invokeMethod  try { JSONObject req = JSON.parseObject(request); ... } catch (Exception e) { \n" + e.getMessage());
			try {
				listener.complete(MethodUtil.JSON_CALLBACK.newErrorResult(e));
			}
			catch (Exception e1) {
				e1.printStackTrace();
				asyncContext.complete();
			}
		}
	}

}
