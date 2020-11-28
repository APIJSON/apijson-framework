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

import apijson.NotNull;


/**SpringBootApplication
 * 右键这个类 > Run As > Java Application
 * @author Lemon
 */
public class APIJSONApplication {

	@NotNull
	public static APIJSONCreator DEFAULT_APIJSON_CREATOR;
	static {
		DEFAULT_APIJSON_CREATOR = new APIJSONCreator();
	}


	/**初始化，加载所有配置并校验
	 * @return 
	 * @throws ServerException
	 */
	public static void init() throws ServerException {
		init(true, DEFAULT_APIJSON_CREATOR);
	}
	/**初始化，加载所有配置并校验
	 * @param shutdownWhenServerError 
	 * @return 
	 * @throws ServerException
	 */
	public static void init(boolean shutdownWhenServerError) throws ServerException {
		init(shutdownWhenServerError, DEFAULT_APIJSON_CREATOR);
	}
	/**初始化，加载所有配置并校验
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static void init(@NotNull APIJSONCreator creator) throws ServerException {
		init(true, creator);
	}
	/**初始化，加载所有配置并校验
	 * @param shutdownWhenServerError 
	 * @param creator 
	 * @return 
	 * @throws ServerException
	 */
	public static void init(boolean shutdownWhenServerError, @NotNull APIJSONCreator creator) throws ServerException {
		System.out.println("\n\n\n\n\n<<<<<<<<<<<<<<<<<<<<<<<<< APIJSON 开始启动 >>>>>>>>>>>>>>>>>>>>>>>>\n");
		DEFAULT_APIJSON_CREATOR = creator;

		// 统一用同一个 creator
		APIJSONSQLConfig.APIJSON_CREATOR = creator;
		APIJSONParser.APIJSON_CREATOR = creator;
		APIJSONController.APIJSON_CREATOR = creator;


		System.out.println("\n\n\n开始初始化: 权限校验配置 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
		try {
			APIJSONVerifier.initAccess(shutdownWhenServerError, creator);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n完成初始化: 权限校验配置 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		
		
		System.out.println("\n\n\n开始初始化: 远程函数配置 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
		try {
			APIJSONFunctionParser.init(shutdownWhenServerError, creator);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n完成初始化: 远程函数配置 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		System.out.println("开始测试: 远程函数 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
		try {
			APIJSONFunctionParser.test();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n完成测试: 远程函数 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");



		System.out.println("\n\n\n开始初始化: 请求结构校验配置 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
		try {
			APIJSONVerifier.initRequest(shutdownWhenServerError, creator);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n完成初始化: 请求结构校验配置 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		System.out.println("\n\n\n开始测试: Request 和 Response 的数据结构校验 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
		try {
			APIJSONVerifier.testStructure();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n完成测试: Request 和 Response 的数据结构校验 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");




		System.out.println("\n\n<<<<<<<<<<<<<<<<<<<<<<<<< APIJSON 启动完成，试试调用自动化 API 吧 ^_^ >>>>>>>>>>>>>>>>>>>>>>>>\n");
	}

}
