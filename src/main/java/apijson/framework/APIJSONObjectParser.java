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

import java.util.List;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;

import apijson.NotNull;
import apijson.RequestMethod;
import apijson.orm.AbstractObjectParser;
import apijson.orm.AbstractParser;
import apijson.orm.Join;
import apijson.orm.SQLConfig;


/**简化Parser，getObject和getArray(getArrayConfig)都能用
 * @author Lemon
 */
public class APIJSONObjectParser extends AbstractObjectParser {
	public static final String TAG = "APIJSONObjectParser";

	/**for single object
	 * @param session
	 * @param request
	 * @param parentPath
	 * @param arrayConfig
	 * @param isSubquery
	 * @param isTable
	 * @param isArrayMainTable
	 * @throws Exception
	 */
	public APIJSONObjectParser(HttpSession session, @NotNull JSONObject request, String parentPath, SQLConfig arrayConfig
			, boolean isSubquery, boolean isTable, boolean isArrayMainTable) throws Exception {
		super(request, parentPath, arrayConfig, isSubquery, isTable, isArrayMainTable);
	}

	@Override
	public APIJSONObjectParser setMethod(RequestMethod method) {
		super.setMethod(method);
		return this;
	}

	@Override
	public APIJSONObjectParser setParser(AbstractParser<?> parser) {
		super.setParser(parser);
		return this;
	}


	@Override
	public SQLConfig newSQLConfig(RequestMethod method, String table, String alias, JSONObject request, List<Join> joinList, boolean isProcedure) throws Exception {
		return APIJSONSQLConfig.newSQLConfig(method, table, alias, request, joinList, isProcedure);
	}


}
