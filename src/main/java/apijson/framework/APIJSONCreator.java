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

import apijson.orm.ParserCreator;
import apijson.orm.SQLCreator;
import apijson.orm.VerifierCreator;

import java.util.List;
import java.util.Map;


/**APIJSON相关创建器
 * @author Lemon
 */
public class APIJSONCreator<T, M extends Map<String, Object>, L extends List<Object>>
		implements ParserCreator<T, M, L>, VerifierCreator<T, M, L>, SQLCreator<T, M, L> {

	@Override
	public APIJSONParser<T, M, L> createParser() {
		return new APIJSONParser<>();
	}

	@Override
	public APIJSONFunctionParser<T, M, L> createFunctionParser() {
		return new APIJSONFunctionParser<>();
	}

	@Override
	public APIJSONVerifier<T, M, L> createVerifier() {
		return new APIJSONVerifier<>();
	}
	
	@Override
	public APIJSONSQLConfig<T, M, L> createSQLConfig() {
		return new APIJSONSQLConfig<>();
	}

	@Override
	public APIJSONSQLExecutor<T, M, L> createSQLExecutor() {
		return new APIJSONSQLExecutor<>();
	}

}
