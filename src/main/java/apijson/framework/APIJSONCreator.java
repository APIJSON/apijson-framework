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

import apijson.orm.FunctionParser;
import apijson.orm.Parser;
import apijson.orm.ParserCreator;
import apijson.orm.SQLConfig;
import apijson.orm.SQLCreator;
import apijson.orm.SQLExecutor;
import apijson.orm.Verifier;
import apijson.orm.VerifierCreator;


/**APIJSON相关创建器
 * @author Lemon
 */
public class APIJSONCreator<T> implements ParserCreator<T>, VerifierCreator<T>, SQLCreator {

	@Override
	public Parser<T> createParser() {
		return new APIJSONParser<>();
	}

	@Override
	public FunctionParser createFunctionParser() {
		return new APIJSONFunctionParser();
	}

	@Override
	public Verifier<T> createVerifier() {
		return new APIJSONVerifier<>();
	}
	
	@Override
	public SQLConfig createSQLConfig() {
		return new APIJSONSQLConfig();
	}

	@Override
	public SQLExecutor createSQLExecutor() {
		return new APIJSONSQLExecutor();
	}

}
