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

import java.util.Arrays;
import java.util.List;

import apijson.JSONResponse;
import apijson.orm.JSONRequest;
import apijson.orm.Visitor;
import apijson.orm.model.Access;
import apijson.orm.model.Column;
import apijson.orm.model.Document;
import apijson.orm.model.ExtendedProperty;
import apijson.orm.model.Function;
import apijson.orm.model.PgAttribute;
import apijson.orm.model.PgClass;
import apijson.orm.model.Request;
import apijson.orm.model.Script;
import apijson.orm.model.SysColumn;
import apijson.orm.model.SysTable;
import apijson.orm.model.Table;
import apijson.orm.model.TestRecord;


/**APIJSON 常量类
 * @author Lemon
 */
public class APIJSONConstant {
	public static String KEY_DEBUG = "debug";

	public static final String DEFAULTS = "defaults";
	public static final String USER_ = "User";
	public static final String PRIVACY_ = "Privacy";
	public static final String VISITOR_ID = "visitorId";
	
	public static final String ID = JSONRequest.KEY_ID;
	public static final String USER_ID = JSONRequest.KEY_USER_ID;
	public static final String TAG = JSONRequest.KEY_TAG;
	public static final String VERSION = JSONRequest.KEY_VERSION;
	public static final String FORMAT = JSONRequest.KEY_FORMAT;
	
	public static final String CODE = JSONResponse.KEY_CODE;
	public static final String MSG = JSONResponse.KEY_MSG;
	public static final String COUNT = JSONResponse.KEY_COUNT;
	public static final String TOTAL = JSONResponse.KEY_TOTAL;
	
	public static final String ACCESS_;
	public static final String COLUMN_;
	public static final String DOCUMENT_;
	public static final String EXTENDED_PROPERTY_;
	public static final String FUNCTION_;
	public static final String SCRIPT_;
	public static final String PG_ATTRIBUTE_;
	public static final String PG_CLASS_;
	public static final String REQUEST_;
	public static final String SYS_COLUMN_;
	public static final String SYS_TABLE_;
	public static final String TABLE_;
	public static final String TEST_RECORD_;
	
	public static final String VISITOR_;
	
	public static final List<String> METHODS;

	static {
		ACCESS_ = Access.class.getSimpleName();
		COLUMN_ = Column.class.getSimpleName();
		DOCUMENT_ = Document.class.getSimpleName();
		EXTENDED_PROPERTY_ = ExtendedProperty.class.getSimpleName();
		FUNCTION_ = Function.class.getSimpleName();
		SCRIPT_ = Script.class.getSimpleName();
		PG_ATTRIBUTE_ = PgAttribute.class.getSimpleName();
		PG_CLASS_ = PgClass.class.getSimpleName();
		REQUEST_ = Request.class.getSimpleName();
		SYS_COLUMN_ = SysColumn.class.getSimpleName();
		SYS_TABLE_ = SysTable.class.getSimpleName();
		TABLE_ = Table.class.getSimpleName();
		TEST_RECORD_ = TestRecord.class.getSimpleName();
		
		VISITOR_ = Visitor.class.getSimpleName();
		
		METHODS = Arrays.asList("get", "head", "gets", "heads", "post", "put", "delete");
	}

}
