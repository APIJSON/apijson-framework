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
import static apijson.framework.APIJSONConstant.FUNCTION_;
import static apijson.framework.APIJSONConstant.SCRIPT_;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import apijson.JSON;
import apijson.JSONResponse;
import apijson.Log;
import apijson.NotNull;
import apijson.RequestMethod;
import apijson.StringUtil;
import apijson.orm.AbstractFunctionParser;
import apijson.orm.JSONRequest;
import apijson.orm.script.JavaScriptExecutor;
import apijson.orm.script.ScriptExecutor;
import jakarta.servlet.http.HttpSession;
import unitauto.MethodUtil;
import unitauto.MethodUtil.Argument;


/**可远程调用的函数类
 * @author Lemon
 */
public class APIJSONFunctionParser extends AbstractFunctionParser {
	public static final String TAG = "APIJSONFunctionParser";

	@NotNull
	public static APIJSONCreator<? extends Object> APIJSON_CREATOR;
	@NotNull
	public static final String[] ALL_METHODS;
	static {
		APIJSON_CREATOR = new APIJSONCreator<>();
		ALL_METHODS = new String[]{ GET.name(), HEAD.name(), GETS.name(), HEADS.name(), POST.name(), PUT.name(), DELETE.name() };
	}

	private HttpSession session;
	public APIJSONFunctionParser() {
		this(null);
	}
	public APIJSONFunctionParser(HttpSession session) {
		this(null, null, 0, null, session);
	}
	public APIJSONFunctionParser(RequestMethod method, String tag, int version, JSONObject curObj, HttpSession session) {
		super(method, tag, version, curObj);
		setSession(session);
	}
	public HttpSession getSession() {
		return session;
	}
	public APIJSONFunctionParser setSession(HttpSession session) {
		this.session = session;
		return this;
	}

	@Override
	public APIJSONFunctionParser setMethod(RequestMethod method) {
		super.setMethod(method);
		return this;
	}
	@Override
	public APIJSONFunctionParser setTag(String tag) {
		super.setTag(tag);
		return this;
	}
	@Override
	public APIJSONFunctionParser setVersion(int version) {
		super.setVersion(version);
		return this;
	}

	/**初始化，加载所有远程函数配置，并校验是否已在应用层代码实现
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init() throws ServerException {
		return init(false);
	}
	/**初始化，加载所有远程函数配置，并校验是否已在应用层代码实现
	 * @param shutdownWhenServerError 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init(boolean shutdownWhenServerError) throws ServerException {
		return init(shutdownWhenServerError, null);
	}
	/**初始化，加载所有远程函数配置，并校验是否已在应用层代码实现
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static <T extends Object> JSONObject init(APIJSONCreator<T> creator) throws ServerException {
		return init(false, creator);
	}
	/**初始化，加载所有远程函数配置，并校验是否已在应用层代码实现
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static <T extends Object> JSONObject init(boolean shutdownWhenServerError, APIJSONCreator<T> creator) throws ServerException {
		return init(shutdownWhenServerError, creator, null);
	}
	/**初始化，加载所有远程函数配置，并校验是否已在应用层代码实现
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @param table 表内自定义数据过滤条件
	 * @return 
	 * @throws ServerException
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> JSONObject init(boolean shutdownWhenServerError, APIJSONCreator<T> creator, JSONObject table) throws ServerException {
		if (creator == null) {
			creator = (APIJSONCreator<T>) APIJSON_CREATOR;
		}
		APIJSON_CREATOR = creator;


		boolean isAll = table == null || table.isEmpty();

		//JSONObject function = isAll ? new JSONRequest() : table;
		//if (Log.DEBUG == false) {
		//	function.put(APIJSONConstant.KEY_DEBUG, 0);
		//}
        //
		//JSONRequest functionItem = new JSONRequest();
		//functionItem.put(FUNCTION_, function);
        //
        //JSONObject script = new JSONRequest(); // isAll ? new JSONRequest() : table;
        //script.put("simple", 0);
        //if (Log.DEBUG == false) {
        //    script.put(APIJSONConstant.KEY_DEBUG, 0);
        //}
        // 不能用这个来优化，因为可能配置了不校验远程函数是否存在
        //{   // name{}@ <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            //JSONRequest nameInAt = new JSONRequest();
            //nameInAt.put("from", "Function");
            //{   // Function <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            //    JSONRequest fun = new JSONRequest();
            //    fun.setColumn("name");
            //    nameInAt.put("Function", fun);
            //}   // Function >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

            //script.put("name{}@", nameInAt);
        //}   // name{}@ >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		//JSONRequest scriptItem = new JSONRequest();
        //scriptItem.put(SCRIPT_, script);

		JSONObject request = new JSONObject();
		//request.putAll(functionItem.toArray(0, 0, FUNCTION_));
		//request.putAll(scriptItem.toArray(0, 0, SCRIPT_));

        // 可以用它，因为 Function 表必须存在，没有绕过校验的配置 // 不能用这个来优化，因为可能配置了不校验远程函数是否存在
        {   // [] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            JSONRequest item = new JSONRequest();

            {   // Function <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                JSONObject function = isAll ? new JSONRequest() : table;
                if (Log.DEBUG == false) {
                    function.put(APIJSONConstant.KEY_DEBUG, 0);
                }
                item.put(FUNCTION_, function);
            }   // Function >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

            if (ENABLE_SCRIPT_FUNCTION) { // Script <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                JSONRequest script = new JSONRequest();
                script.put("name@", "/Function/name");
                script.put("simple", 0);
                item.put(SCRIPT_, script);
            }   // Script >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

            request.putAll(item.toArray(0, 0));
        }   // [] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


		JSONObject response = creator.createParser().setMethod(GET).setNeedVerify(true).parseResponse(request);
		if (JSONResponse.isSuccess(response) == false) {
			onServerError("\n\n\n\n\n !!!! 查询远程函数异常 !!!\n" + response.getString(JSONResponse.KEY_MSG) + "\n\n\n\n\n", shutdownWhenServerError);
		}
		
		//初始化默认脚本引擎,避免增量
		if (isAll || SCRIPT_EXECUTOR_MAP.get("js") == null) {
			ScriptExecutor javaScriptExecutor = new JavaScriptExecutor();
			javaScriptExecutor.init();
			SCRIPT_EXECUTOR_MAP.put("js", javaScriptExecutor);
			SCRIPT_EXECUTOR_MAP.put("JavaScript", javaScriptExecutor);
			SCRIPT_EXECUTOR_MAP.put("javascript", javaScriptExecutor);
		}

		Map<String, JSONObject> scriptMap = new HashMap<>();
        JSONArray scriptList = response.getJSONArray("[]"); // response.getJSONArray(SCRIPT_ + "[]");
        if (scriptList != null && scriptList.isEmpty() == false) {
            //if (isAll) {
            //    SCRIPT_MAP = new LinkedHashMap<>();
            //}
            Map<String, JSONObject> newMap = new LinkedHashMap<>();

            for (int i = 0; i < scriptList.size(); i++) {
                JSONObject item = scriptList.getJSONObject(i);
                item = item == null ? null : item.getJSONObject(SCRIPT_);
                if (item == null) { // 关联查不到很正常
                    continue;
                }

                String n = item.getString("name");
                if (StringUtil.isName(n) == false) {
                    onServerError("Script 表字段 name 的值 " + n + " 不合法！必须为合法的方法名字符串！", shutdownWhenServerError);
                }

                String s = item.getString("script");
                if (StringUtil.isEmpty(s, true)) {
                    onServerError("Script 表字段 script 的值 " + s + " 不合法！不能为空！", shutdownWhenServerError);
                }
                newMap.put(n, item);
            }

            scriptMap = newMap;
        }

		JSONArray list = scriptList; // response.getJSONArray(FUNCTION_ + "[]");
		int size = list == null ? 0 : list.size();
		if (isAll && size <= 0) {
			Log.w(TAG, "init isAll && size <= 0，，没有可用的远程函数");
			return response;
		}


		if (isAll) {  // 必须在测试 invoke 前把配置 put 进 FUNCTION_MAP！ 如果要做成完全校验通过才更新 FUNCTION_MAP，但又不提供 忽略校验 参数，似乎无解
			FUNCTION_MAP = new LinkedHashMap<>();
		}
		Map<String, JSONObject> newMap = FUNCTION_MAP;  // 必须在测试 invoke 前把配置 put 进 FUNCTION_MAP！ new LinkedHashMap<>();

		for (int i = 0; i < size; i++) {
			JSONObject item = list.getJSONObject(i);
            item = item == null ? null : item.getJSONObject(FUNCTION_);
			if (item == null) {
				continue;
			}

			JSONObject demo = JSON.parseObject(item.getString("demo"));
			if (demo == null) {
				onServerError("字段 demo 的值必须为合法且非 null 的 JSONObejct 字符串！", shutdownWhenServerError);
			}
			String name = item.getString("name");
			if (demo.containsKey("result()") == false) {
				demo.put("result()", getFunctionCall(name, item.getString("arguments")));
			}
			//			demo.put(JSONRequest.KEY_TAG, item.getString(JSONRequest.KEY_TAG));
			//			demo.put(JSONRequest.KEY_VERSION, item.getInteger(JSONRequest.KEY_VERSION));
			//加载脚本
			if (item.get("language") != null) {
				String language = item.getString("language");
				if (SCRIPT_EXECUTOR_MAP.get(language) == null) {
					onServerError("找不到脚本语言 " + language + " 对应的执行引擎！请先依赖相关库并在后端 APIJSONFunctionParser 中注册！", shutdownWhenServerError);
				}
				ScriptExecutor scriptExecutor = SCRIPT_EXECUTOR_MAP.get(language);
				scriptExecutor.load(name, scriptMap.get(name).getString("script"));
			}
			newMap.put(name, item);  // 必须在测试 invoke 前把配置 put 进 FUNCTION_MAP！ 

			String[] methods = StringUtil.split(item.getString("methods"));

			if (methods == null || methods.length <= 0) {
				methods = ALL_METHODS;
			}

			for (String method : methods) {
				JSONObject r = APIJSON_CREATOR.createParser()
						.setMethod(RequestMethod.valueOf(method))
						.setNeedVerify(false)
						.setTag(item.getString(JSONRequest.KEY_TAG))
						.setVersion(item.getIntValue(JSONRequest.KEY_VERSION))
						.parseResponse(demo);

				if (JSONResponse.isSuccess(r) == false) {
					onServerError(JSONResponse.getMsg(r), shutdownWhenServerError);
				}
			}
		}

		// 必须在测试 invoke 前把配置 put 进 FUNCTION_MAP！ 
		//		if (isAll) {
		//			FUNCTION_MAP = newMap;
		//		}
		//		else {
		//			FUNCTION_MAP.putAll(newMap);
		//		}

		return response;
	}


	protected static void onServerError(String msg, boolean shutdown) throws ServerException {
		Log.e(TAG, "\n远程函数文档测试未通过！\n请新增 demo 里的函数，或修改 Function 表里的 demo 为已有的函数示例！\n保证前端看到的远程函数文档是正确的！！！\n\n原因：\n" + msg);

		if (shutdown) {
			System.exit(1);	
		} else {
			throw new ServerException(msg);
		}
	}


	public static void test() throws Exception {
		test(null);
	}
	public static void test(APIJSONFunctionParser function) throws Exception {
		int i0 = 1, i1 = -2;
		JSONObject request = new JSONObject(); 
		request.put("id", 10);
		request.put("i0", i0);
		request.put("i1", i1);
		JSONArray arr = new JSONArray();
		arr.add(new JSONObject());
		request.put("arr", arr);

		JSONArray array = new JSONArray();
		array.add(1);//new JSONObject());
		array.add(2);//new JSONObject());
		array.add(4);//new JSONObject());
		array.add(10);//new JSONObject());
		request.put("array", array);

		request.put("position", 1);
		request.put("@position", 0);

		request.put("key", "key");
		JSONObject object = new JSONObject();
		object.put("key", "success");
		request.put("object", object);

		if (function == null) {
			function = new APIJSONFunctionParser(null, null, 1, null, null);
		}

		// 等数据库 Function 表加上 plus 配置再过两个以上迭代(应该是到 5.0)后再取消注释
		//		Log.i(TAG, "plus(1,-2) = " + function.invoke("plus(i0,i1)", request));
		//		AssertUtil.assertEqual(-1, function.invoke("plus(i0,i1)", request));

		Log.i(TAG, "count([1,2,4,10]) = " + function.invoke("countArray(array)", request));
		AssertUtil.assertEqual(4, function.invoke("countArray(array)", request));

		Log.i(TAG, "isContain([1,2,4,10], 10) = " + function.invoke("isContain(array,id)", request));
		AssertUtil.assertEqual(true, function.invoke("isContain(array,id)", request));

		Log.i(TAG, "getFromArray([1,2,4,10], 0) = " + function.invoke("getFromArray(array,@position)", request));
		AssertUtil.assertEqual(1, function.invoke("getFromArray(array,@position)", request));

		Log.i(TAG, "getFromObject({key:\"success\"}, key) = " + function.invoke("getFromObject(object,key)", request));
		AssertUtil.assertEqual("success", function.invoke("getFromObject(object,key)", request));

	}







	/**获取远程函数的demo，如果没有就自动补全
	 * @param curObj
	 * @return
	 * @throws ServerException 
	 */
	public JSONObject getFunctionDemo(@NotNull JSONObject curObj) {
		JSONObject demo = JSON.parseObject(curObj.getString("demo"));
		if (demo == null) {
			demo = new JSONObject();
		}
		if (demo.containsKey("result()") == false) {
			demo.put("result()", getFunctionCall(curObj.getString("name"), curObj.getString("arguments")));
		}
		return demo;
	}

	/**获取远程函数的demo，如果没有就自动补全
	 * @param curObj
	 * @return
	 */
	public String getFunctionDetail(@NotNull JSONObject curObj) {
		return getFunctionCall(curObj.getString("name"), curObj.getString("arguments"))
				+ ": " + StringUtil.getTrimedString(curObj.getString("detail"));
	}
	/**获取函数调用代码
	 * @param name
	 * @param arguments
	 * @return
	 */
	private static String getFunctionCall(String name, String arguments) {
		return name + "(" + StringUtil.getTrimedString(arguments) + ")";
	}


	public double plus(@NotNull JSONObject curObj, String i0, String i1) {
		return curObj.getDoubleValue(i0) + curObj.getDoubleValue(i1);
	}
	public double minus(@NotNull JSONObject curObj, String i0, String i1) {
		return curObj.getDoubleValue(i0) - curObj.getDoubleValue(i1);
	}
	public double multiply(@NotNull JSONObject curObj, String i0, String i1) {
		return curObj.getDoubleValue(i0) * curObj.getDoubleValue(i1);
	}
	public double divide(@NotNull JSONObject curObj, String i0, String i1) {
		return curObj.getDoubleValue(i0) / curObj.getDoubleValue(i1);
	}

	public double plus(@NotNull JSONObject curObj, Number n0, Number n1) {
		return n0.doubleValue() + n1.doubleValue();
	}
	public double minus(@NotNull JSONObject curObj, Number n0, Number n1) {
		return n0.doubleValue() - n1.doubleValue();
	}
	public double multiply(@NotNull JSONObject curObj, Number n0, Number n1) {
		return n0.doubleValue() * n1.doubleValue();
	}
	public double divide(@NotNull JSONObject curObj, Number n0, Number n1) {
		return n0.doubleValue() / n1.doubleValue();
	}

	//判断是否为空 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**判断array是否为空
	 * @param curObj
	 * @param array
	 * @return
	 */
	public boolean isArrayEmpty(@NotNull JSONObject curObj, String array) {
		return BaseModel.isEmpty(curObj.getJSONArray(array));
	}
	/**判断object是否为空
	 * @param curObj
	 * @param object
	 * @return
	 */
	public boolean isObjectEmpty(@NotNull JSONObject curObj, String object) {
		return BaseModel.isEmpty(curObj.getJSONObject(object)); 
	}
	//判断是否为空 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//判断是否为包含 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**判断array是否包含value
	 * @param curObj
	 * @param array
	 * @param value
	 * @return
	 */
	public boolean isContain(@NotNull JSONObject curObj, String array, String value) {
		//解决isContain((List<Long>) [82001,...], (Integer) 82001) == false及类似问题, list元素可能是从数据库查到的bigint类型的值
		//		return BaseModel.isContain(curObj.getJSONArray(array), curObj.get(value));

		//不用准确的的 curObj.getString(value).getClass() ，因为Long值转Integer崩溃，而且转成一种类型本身就和字符串对比效果一样了。
		List<String> list = com.alibaba.fastjson.JSON.parseArray(curObj.getString(array), String.class);
		return list != null && list.contains(curObj.getString(value));
	}
	/**判断object是否包含key
	 * @param curObj
	 * @param object
	 * @param key
	 * @return
	 */
	public boolean isContainKey(@NotNull JSONObject curObj, String object, String key) { 
		return BaseModel.isContainKey(curObj.getJSONObject(object), curObj.getString(key)); 
	}
	/**判断object是否包含value
	 * @param curObj
	 * @param object
	 * @param value
	 * @return
	 */
	public boolean isContainValue(@NotNull JSONObject curObj, String object, String value) { 
		return BaseModel.isContainValue(curObj.getJSONObject(object), curObj.get(value));
	}
	//判断是否为包含 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//获取集合长度 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取数量
	 * @param curObj
	 * @param array
	 * @return
	 */
	public int countArray(@NotNull JSONObject curObj, String array) { 
		return BaseModel.count(curObj.getJSONArray(array)); 
	}
	/**获取数量
	 * @param curObj
	 * @param object
	 * @return
	 */
	public int countObject(@NotNull JSONObject curObj, String object) {
		return BaseModel.count(curObj.getJSONObject(object)); 
	}
	//获取集合长度 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//根据键获取值 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取
	 ** @param curObj
	 * @param array
	 * @param position 支持直接传数字，例如 getFromArray(array,0) ；或者引用当前对象的值，例如 "@position": 0, "result()": "getFromArray(array,@position)"
	 * @return
	 */
	public Object getFromArray(@NotNull JSONObject curObj, String array, String position) {
		int p;
		try {
			p = Integer.parseInt(position);
		} catch (Exception e) {
			p = curObj.getIntValue(position);
		}
		return BaseModel.get(curObj.getJSONArray(array), p); 
	}
	/**获取
	 * @param curObj
	 * @param object
	 * @param key
	 * @return
	 */
	public Object getFromObject(@NotNull JSONObject curObj, String object, String key) { 
		return BaseModel.get(curObj.getJSONObject(object), curObj.getString(key));
	}
	//根据键获取值 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//根据键移除值 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**移除
	 ** @param curObj
	 * @param position 支持直接传数字，例如 getFromArray(array,0) ；或者引用当前对象的值，例如 "@position": 0, "result()": "getFromArray(array,@position)"
	 * @return
	 */
	public Object removeIndex(@NotNull JSONObject curObj, String position) {
		int p;
		try {
			p = Integer.parseInt(position);
		} catch (Exception e) {
			p = curObj.getIntValue(position);
		}
		curObj.remove(p); 
		return null;
	}
	/**移除
	 * @param curObj
	 * @param key
	 * @return
	 */
	public Object removeKey(@NotNull JSONObject curObj, String key) { 
		curObj.remove(key);
		return null;
	}
	//根据键获取值 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>



	//获取非基本类型对应基本类型的非空值 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取非空值
	 * @param curObj
	 * @param value
	 * @return
	 */
	public boolean booleanValue(@NotNull JSONObject curObj, String value) { 
		return curObj.getBooleanValue(value);
	}
	/**获取非空值
	 * @param curObj
	 * @param value
	 * @return
	 */
	public int intValue(@NotNull JSONObject curObj, String value) {  
		return curObj.getIntValue(value);
	}
	/**获取非空值
	 * @param curObj
	 * @param value
	 * @return
	 */
	public long longValue(@NotNull JSONObject curObj, String value) {   
		return curObj.getLongValue(value);
	}
	/**获取非空值
	 * @param curObj
	 * @param value
	 * @return
	 */
	public float floatValue(@NotNull JSONObject curObj, String value) {  
		return curObj.getFloatValue(value);
	}
	/**获取非空值
	 * @param curObj
	 * @param value
	 * @return
	 */
	public double doubleValue(@NotNull JSONObject curObj, String value) {    
		return curObj.getDoubleValue(value); 
	}
	//获取非基本类型对应基本类型的非空值 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	/**获取value，当value为null时获取defaultValue
	 * @param curObj
	 * @param value
	 * @param defaultValue
	 * @return v == null ? curObj.get(defaultValue) : v
	 */
	public Object getWithDefault(@NotNull JSONObject curObj, String value, String defaultValue) {    
		Object v = curObj.get(value); 
		return v == null ? curObj.get(defaultValue) : v; 
	}



	/**获取方法参数的定义
	 * @param curObj
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalArgumentException 
	 */
	public String getMethodArguments(@NotNull JSONObject curObj) throws IllegalArgumentException, ClassNotFoundException, IOException {
		return getMethodArguments(curObj, "methodArgs");
	}
	/**获取方法参数的定义
	 * @param curObj
	 * @param methodArgsKey
	 * @return
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public String getMethodArguments(@NotNull JSONObject curObj, String methodArgsKey) throws IllegalArgumentException, ClassNotFoundException, IOException {
		JSONObject obj = curObj.getJSONObject("request");
		String argsStr = obj == null ? null : obj.getString(methodArgsKey);
		if (StringUtil.isEmpty(argsStr, true)) {
			argsStr = curObj.getString(methodArgsKey);
		}
		List<Argument> methodArgs = JSON.parseArray(removeComment(argsStr), Argument.class);
		if (methodArgs == null || methodArgs.isEmpty()) {
			return "";
		}

		//		Class<?>[] types = new Class<?>[methodArgs.size()];
		//		Object[] args = new Object[methodArgs.size()];
		//		MethodUtil.initTypesAndValues(methodArgs, types, args, true);

		String s = "";
		//		if (types != null) {
		//			String sn;
		//			for (int i = 0; i < types.length; i++) {
		//				sn = types[i] == null ? null : types[i].getSimpleName();
		//				if (sn == null) {
		//					sn = Object.class.getSimpleName();
		//				}
		//
		//				if (i > 0) {
		//					s += ",";
		//				}
		//
		//				if (MethodUtil.CLASS_MAP.containsKey(sn)) {
		//					s += sn;
		//				}
		//				else {
		//					s += types[i].getName();
		//				}
		//			}
		//		}

		for (int i = 0; i < methodArgs.size(); i++) {
			Argument arg = methodArgs.get(i);

			String sn = arg == null ? null : arg.getType();
			if (sn == null) {
				sn = arg.getValue() == null ? Object.class.getSimpleName() : MethodUtil.trimType(arg.getValue().getClass());
			}

			if (i > 0) {
				s += ",";
			}
			s += sn;
		}

		return s;
	}


	/**改用 getMethodDefinition
	 */
    @Deprecated
	public String getMethodDefination(@NotNull JSONObject curObj)
			throws IllegalArgumentException, ClassNotFoundException, IOException {
		//		curObj.put("arguments", removeComment(curObj.getString("methodArgs")));
		return getMethodDefination(curObj, "method", "arguments", "genericType", "genericExceptions", "Java");
	}

	/**获取方法的定义
	 * @param curObj
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 */
	public String getMethodDefinition(@NotNull JSONObject curObj)
			throws IllegalArgumentException, ClassNotFoundException, IOException {
		//		curObj.put("arguments", removeComment(curObj.getString("methodArgs")));
		return getMethodDefinition(curObj, "method", "arguments", "genericType", "genericExceptions", "Java");
	}
    /**改用 getMethodDefinition
     */
    @Deprecated
	public String getMethodDefination(@NotNull JSONObject curObj, String method, String arguments
            , String type, String exceptions, String language) throws IllegalArgumentException {
        return getMethodDefinition(curObj, method, arguments, type, exceptions, language);
    }
	/**获取方法的定义
	 * @param curObj
	 * @param method
	 * @param arguments
	 * @param type
	 * @return method(argType0,argType1...): returnType
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 */
	public String getMethodDefinition(@NotNull JSONObject curObj, String method, String arguments
            , String type, String exceptions, String language) throws IllegalArgumentException {
		String n = curObj.getString(method);
		if (StringUtil.isEmpty(n, true)) {
			throw new NullPointerException("getMethodDefination  StringUtil.isEmpty(methodArgs, true) !");
		}
		String a = curObj.getString(arguments);
		String t = curObj.getString(type);
		String e = curObj.getString(exceptions);

		if (language == null) {
			language = "";
		}
		switch (language) {
		case "TypeScript":
			return n + "(" + (StringUtil.isEmpty(a, true) ? "" : a) + ")" + (StringUtil.isEmpty(t, true) ? "" : ": " + t) + (StringUtil.isEmpty(e, true) ? "" : " throws " + e);
		case "Go":
			return n + "(" + (StringUtil.isEmpty(a, true) ? "" : a ) + ")" + (StringUtil.isEmpty(t, true) ? "" : " " + t) + (StringUtil.isEmpty(e, true) ? "" : " throws " + e);
		default:
			//类型可能很长，Eclipse, Idea 代码提示都是类型放后面			return (StringUtil.isEmpty(t, true) ? "" : t + " ") + n + "(" + (StringUtil.isEmpty(a, true) ? "" : a) + ")";
			return n + "(" + (StringUtil.isEmpty(a, true) ? "" : a) + ")" + (StringUtil.isEmpty(t, true) ? "" : ": " + t) + (StringUtil.isEmpty(e, true) ? "" : " throws " + e);
		}
	}

	/**
	 * methodArgs 和 classArgs 都可以带注释
	 */
	public String getMethodRequest(@NotNull JSONObject curObj) {
		String req = curObj.getString("request");
		if (StringUtil.isEmpty(req, true) == false) {
			return req;
		}

		req = "{";
		Boolean isStatic = curObj.getBoolean("static");
		String methodArgs = curObj.getString("methodArgs");
		String classArgs = curObj.getString("classArgs");

		boolean comma = false;
		if (isStatic != null && isStatic) {
			req += "\n    \"static\": " + true;
			comma = true;
		}
		if (StringUtil.isEmpty(methodArgs, true) == false) {
			req += (comma ? "," : "") + "\n    \"methodArgs\": " + methodArgs;
			comma = true;
		} 
		if (StringUtil.isEmpty(classArgs, true) == false) {
			req += (comma ? "," : "") + "\n    \"classArgs\": " + classArgs;
		}
		req += "\n}";
		return req;
	}

	//	public static JSONObject removeComment(String json) {
	//		return JSON.parseObject(removeComment(json));
	//	}
	public static String removeComment(String json) {
		return json == null ? null: json.replaceAll("(//.*)|(/\\*[\\s\\S]*?\\*/)", "");
	}

}