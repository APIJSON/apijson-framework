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

import java.rmi.ServerException;

import com.alibaba.fastjson.JSONObject;


/**请求结构校验。已整合进 APIJSONVerifier，最快 4.5.0 移除
 * @author Lemon
 */
@Deprecated
public class StructureUtil {
	public static final String TAG = "StructureUtil";

	public static APIJSONCreator APIJSON_CREATOR;
	//根据 version 动态从数据库查的  version{}:">=$currentVersion"，所以静态缓存暂时没用   public static final Map<String, JSONObject> REQUEST_MAP;
	static {
		//		REQUEST_MAP = new HashMap<>();
		APIJSON_CREATOR = new APIJSONCreator();
	}

	/**初始化，加载所有请求校验配置
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init() throws ServerException {
		return init(false, null);
	}
	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init(boolean shutdownWhenServerError) throws ServerException {
		return init(shutdownWhenServerError, null);
	}
	/**初始化，加载所有请求校验配置
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init(APIJSONCreator creator) throws ServerException {
		return init(false, creator);
	}
	/**初始化，加载所有请求校验配置
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static JSONObject init(boolean shutdownWhenServerError, APIJSONCreator creator) throws ServerException {
		return APIJSONVerifier.initRequest(shutdownWhenServerError, creator);
	}

	static final String requestString = APIJSONVerifier.requestString;
	static final String responseString = APIJSONVerifier.responseString;
	/**测试
	 * @throws Exception
	 */
	public static void test() throws Exception {
		APIJSONVerifier.testStructure();
	}




}
