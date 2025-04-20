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

package apijson.framework.javax;

import apijson.NotNull;
import apijson.RequestMethod;
import apijson.framework.javax.*;
import apijson.orm.AbstractObjectParser;
import apijson.orm.Join;
import apijson.orm.Parser;
import apijson.orm.SQLConfig;
import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;


/**简化Parser，getObject和getArray(getArrayConfig)都能用
 * @author Lemon
 */
public class APIJSONObjectParser<T, M extends Map<String, Object>, L extends List<Object>>
		extends AbstractObjectParser<T, M, L> {
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
	public APIJSONObjectParser(HttpSession session, @NotNull M request, String parentPath, SQLConfig<T, M, L> arrayConfig
			, boolean isSubquery, boolean isTable, boolean isArrayMainTable) throws Exception {
		super(request, parentPath, arrayConfig, isSubquery, isTable, isArrayMainTable);
	}

	@Override
	public APIJSONObjectParser<T, M, L> setMethod(RequestMethod method) {
		super.setMethod(method);
		return this;
	}

	@Override
	public APIJSONObjectParser<T, M, L> setParser(Parser<T, M, L> parser) {
		super.setParser(parser);
		return this;
	}


	@Override
	public SQLConfig<T, M, L> newSQLConfig(RequestMethod method, String table, String alias, M request
			, List<Join<T, M, L>> joinList, boolean isProcedure) throws Exception {
		return APIJSONSQLConfig.newSQLConfig(method, table, alias, request, joinList, isProcedure);
	}

}
