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

package apijson.framework;

import static apijson.framework.APIJSONConstant.DEFAULTS;
import static apijson.framework.APIJSONConstant.FORMAT;
import static apijson.framework.APIJSONConstant.VERSION;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import apijson.NotNull;
import apijson.RequestMethod;
import apijson.orm.AbstractParser;
import apijson.orm.FunctionParser;
import apijson.orm.SQLConfig;


/**请求解析器
 * @author Lemon
 */
public class APIJSONParser<T, M extends Map<String, Object>, L extends List<Object>> extends AbstractParser<T, M, L> {
	public static final String TAG = "APIJSONParser";

	
	public APIJSONParser() {
		super();
	}
	public APIJSONParser(RequestMethod method) {
		super(method);
	}
	public APIJSONParser(RequestMethod method, boolean needVerify) {
		super(method, needVerify);
	}

	private HttpSession session;
	public HttpSession getSession() {
		return session;
	}
	public APIJSONParser<T, M, L> setSession(HttpSession session) {
		this.session = session;
		setVisitor(APIJSONVerifier.getVisitor(session));
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public APIJSONParser<T, M, L> createParser() {
		return APIJSONApplication.createParser();
	}
	@Override
	public APIJSONFunctionParser<T, M, L> createFunctionParser() {
		return APIJSONApplication.createFunctionParser();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public APIJSONVerifier<T, M, L> createVerifier() {
		return APIJSONApplication.createVerifier();
	}
	
	@Override
	public APIJSONSQLConfig<T, M, L> createSQLConfig() {
		return APIJSONApplication.createSQLConfig();
	}
	@Override
	public APIJSONSQLExecutor<T, M, L> createSQLExecutor() {
		return APIJSONApplication.createSQLExecutor();
	}

	@Override
	public APIJSONParser<T, M, L> setNeedVerify(boolean needVerify) {
		super.setNeedVerify(needVerify);
		return this;
	}

	@Override
	public APIJSONParser<T, M, L> setNeedVerifyLogin(boolean needVerifyLogin) {
		super.setNeedVerifyLogin(needVerifyLogin);
		return this;
	}

	@Override
	public APIJSONParser<T, M, L> setNeedVerifyRole(boolean needVerifyRole) {
		super.setNeedVerifyRole(needVerifyRole);
		return this;
	}

	@Override
	public APIJSONParser<T, M, L> setNeedVerifyContent(boolean needVerifyContent) {
		super.setNeedVerifyContent(needVerifyContent);
		return this;
	}

	@Override
	public M parseResponse(M request) {
		//补充format
		if (session != null && request != null) {
			if (request.get(FORMAT) == null) {
				request.put(FORMAT, session.getAttribute(FORMAT));
			}
			if (request.get(VERSION) == null) {
				request.put(VERSION, session.getAttribute(VERSION));
			}

			if (request.get(DEFAULTS) == null) {
				M defaults = (M) session.getAttribute(DEFAULTS);
				Set<Map.Entry<String, Object>> set = defaults == null ? null : defaults.entrySet();

				if (set != null) {
					for (Map.Entry<String, Object> e : set) {
						if (e != null && request.get(e.getKey()) == null) {
							request.put(e.getKey(), e.getValue());
						}
					}
				}
			}
		}
		
		return super.parseResponse(request);
	}

	private FunctionParser<T, M, L> functionParser;
	public FunctionParser<T, M, L> getFunctionParser() {
		return functionParser;
	}
	@Override
	public Object onFunctionParse(String key, String function, String parentPath, String currentName, M currentObject, boolean containRaw) throws Exception {
		if (functionParser == null) {
			functionParser = createFunctionParser();
			functionParser.setParser(this);
			functionParser.setMethod(getMethod());
			functionParser.setTag(getTag());
			functionParser.setVersion(getVersion());
			functionParser.setRequest(requestObject);
			
			if (functionParser instanceof APIJSONFunctionParser<?, ?, ?>) {
				((APIJSONFunctionParser<?, ?, ?>) functionParser).setSession(getSession());
			}
		}
		functionParser.setKey(key);
		functionParser.setParentPath(parentPath);
		functionParser.setCurrentName(currentName);
		functionParser.setCurrentObject(currentObject);
		
		return functionParser.invoke(function, currentObject, containRaw);
	}


	@Override
	public APIJSONObjectParser<T, M, L> createObjectParser(@NotNull M request, String parentPath, SQLConfig<T, M, L> arrayConfig
			, boolean isSubquery, boolean isTable, boolean isArrayMainTable) throws Exception {

		return new APIJSONObjectParser<T, M, L>(getSession(), request, parentPath, arrayConfig, isSubquery, isTable, isArrayMainTable) {

			//			@Override
			//			protected APIJSONSQLConfig<T, M, L> newQueryConfig() {
			//				if (itemConfig != null) {
			//					return itemConfig;
			//				}
			//				return super.newQueryConfig();
			//			}

			// 导致最多评论的(Strong 30个)的那个动态详情界面Android(82001)无姓名和头像，即User=null
			//			@Override
			//			protected void onComplete() {
			//				if (response != null) {
			//					putQueryResult(path, response); // 解决获取关联数据时requestObject里不存在需要的关联数据
			//				}
			//			}

		}.setMethod(getMethod()).setParser(this);
	}

}
