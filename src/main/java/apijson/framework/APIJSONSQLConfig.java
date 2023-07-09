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

import static apijson.framework.APIJSONConstant.ID;
import static apijson.framework.APIJSONConstant.PRIVACY_;
import static apijson.framework.APIJSONConstant.USER_;
import static apijson.framework.APIJSONConstant.USER_ID;

import java.util.List;
import java.util.Map;

import apijson.column.ColumnUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import apijson.RequestMethod;
import apijson.orm.AbstractSQLConfig;
import apijson.orm.Join;
import apijson.orm.SQLConfig;


/**SQL配置
 * TiDB 用法和 MySQL 一致
 * @author Lemon
 */
public class APIJSONSQLConfig<T extends Object> extends AbstractSQLConfig<T> {
	public static final String TAG = "APIJSONSQLConfig";

	public static boolean ENABLE_COLUMN_CONFIG = false;

	public static Callback<? extends Object> SIMPLE_CALLBACK;
	public static APIJSONCreator<? extends Object> APIJSON_CREATOR;
	
	static {
		DEFAULT_DATABASE = DATABASE_MYSQL;  //TODO 默认数据库类型，改成你自己的
		DEFAULT_SCHEMA = "sys";  //TODO 默认模式名，改成你自己的，默认情况是 MySQL: sys, PostgreSQL: public, SQL Server: dbo, Oracle: 
		//		TABLE_KEY_MAP.put(Access.class.getSimpleName(), "apijson_access");

		//  由 APIJSONVerifier.init 方法读取数据库 Access 表来替代手动输入配置
		//		//表名映射，隐藏真实表名，对安全要求很高的表可以这么做
		//		TABLE_KEY_MAP.put(User.class.getSimpleName(), "apijson_user");
		//		TABLE_KEY_MAP.put(Privacy.class.getSimpleName(), "apijson_privacy");

		APIJSON_CREATOR = new APIJSONCreator<>();

		SIMPLE_CALLBACK = new SimpleCallback<Object>() {

			@Override
			public SQLConfig getSQLConfig(RequestMethod method, String database, String schema,String datasource, String table) {
				SQLConfig config = APIJSON_CREATOR.createSQLConfig();
				config.setMethod(method);
				config.setDatabase(database);
				config.setDatasource(datasource);
				config.setSchema(schema);
				config.setTable(table);
				return config;
			}

			//取消注释来实现自定义各个表的主键名
			//			@Override
			//			public String getIdKey(String database, String schema, String datasource, String table) {
			//				return StringUtil.firstCase(table + "Id");  // userId, comemntId ...
			//				//		return StringUtil.toLowerCase(t) + "_id";  // user_id, comemnt_id ...
			//				//		return StringUtil.toUpperCase(t) + "_ID";  // USER_ID, COMMENT_ID ...
			//			}

			@Override
			public String getUserIdKey(String database, String schema, String datasource, String table) {
				return USER_.equals(table) || PRIVACY_.equals(table) ? ID : USER_ID; // id / userId
			}

			//取消注释来实现数据库自增 id
			//			@Override
			//			public Object newId(RequestMethod method, String database, String schema, String datasource, String table) {
			//				return null; // return null 则不生成 id，一般用于数据库自增 id
			//			}
		};

	}



	@Override
	public String getDBVersion() {
		if (isMySQL()) {
			return "5.7.22"; //"8.0.11"; //TODO 改成你自己的 MySQL 或 PostgreSQL 数据库版本号 //MYSQL 8 和 7 使用的 JDBC 配置不一样
		}
		if (isPostgreSQL()) {
			return "9.6.15"; //TODO 改成你自己的
		}
		if (isSQLServer()) {
			return "2016"; //TODO 改成你自己的
		}
		if (isOracle()) {
			return "18c"; //TODO 改成你自己的
		}
		return null;
	}

	@JSONField(serialize = false)  // 不在日志打印 账号/密码 等敏感信息，用了 UnitAuto 则一定要加
	@Override
	public String getDBUri() {
		if (isMySQL()) {
			return "jdbc:mysql://localhost:3306"; //TODO 改成你自己的，TiDB 可以当成 MySQL 使用，默认端口为 4000
		}
		if (isPostgreSQL()) {
			return "jdbc:postgresql://localhost:5432/postgres"; //TODO 改成你自己的
		}
		if (isSQLServer()) {
			return "jdbc:jtds:sqlserver://localhost:1433/pubs;instance=SQLEXPRESS"; //TODO 改成你自己的
		}
		if (isOracle()) {
			return "jdbc:oracle:thin:@localhost:1521:orcl"; //TODO 改成你自己的
		}
		return null;
	}

	@JSONField(serialize = false)  // 不在日志打印 账号/密码 等敏感信息，用了 UnitAuto 则一定要加
	@Override
	public String getDBAccount() {
		if (isMySQL()) {
			return "root";  //TODO 改成你自己的
		}
		if (isPostgreSQL()) {
			return "postgres";  //TODO 改成你自己的
		}
		if (isSQLServer()) {
			return "sa";  //TODO 改成你自己的
		}
		if (isOracle()) {
			return "scott";  //TODO 改成你自己的
		}
		return null;
	}

	@JSONField(serialize = false)  // 不在日志打印 账号/密码 等敏感信息，用了 UnitAuto 则一定要加
	@Override
	public String getDBPassword() {
		if (isMySQL()) {
			return "apijson";  //TODO 改成你自己的，TiDB 可以当成 MySQL 使用， 默认密码为空字符串 ""
		}
		if (isPostgreSQL()) {
			return null;  //TODO 改成你自己的
		}
		if (isSQLServer()) {
			return "apijson@123";  //TODO 改成你自己的
		}
		if (isOracle()) {
			return "tiger";  //TODO 改成你自己的
		}
		return null;
	}

	/**获取 APIJSON 配置表所在数据库模式 database，默认与业务表一块
	 * @return
	 */
	public String getConfigDatabase() {
		return getDatabase();
	}
	/**获取 APIJSON 配置表所在数据库模式 schema，默认与业务表一块
	 * @return
	 */
	public String getConfigSchema() {
		return getSchema();
	}
	/**是否为 APIJSON 配置表，如果和业务表一块，可以重写这个方法，固定 return false 来提高性能
	 * @return
	 */
	public boolean isConfigTable() {
		return CONFIG_TABLE_LIST.contains(getTable());
	}
	@Override
	public String getSQLDatabase() {
		String db = isConfigTable() ? getConfigDatabase() : super.getSQLDatabase();
		return db == null ? DEFAULT_DATABASE : db;
	}
	@Override
	public String getSQLSchema() {
		String sch = isConfigTable() ? getConfigSchema() : super.getSQLSchema();
		return sch == null ? DEFAULT_SCHEMA : sch;
	}


	@Override
	public String getIdKey() {
		return SIMPLE_CALLBACK.getIdKey(getDatabase(), getSchema(), getDatasource(), getTable());
	}

	@Override
	public String getUserIdKey() {
		return SIMPLE_CALLBACK.getUserIdKey(getDatabase(), getSchema(), getDatasource(), getTable());
	}


	public APIJSONSQLConfig() {
		this(RequestMethod.GET);
	}
	public APIJSONSQLConfig(RequestMethod method) {
		super(method);
	}
	public APIJSONSQLConfig(RequestMethod method, String table) {
		super(method, table);
	}
	public APIJSONSQLConfig(RequestMethod method, int count, int page) {
		super(method, count, page);
	}



	/**获取SQL配置
	 * @param table
	 * @param alias 
	 * @param request
	 * @param isProcedure 
	 * @return
	 * @throws Exception 
	 */
	public static SQLConfig newSQLConfig(RequestMethod method, String table, String alias, JSONObject request, List<Join> joinList, boolean isProcedure) throws Exception {
		return newSQLConfig(method, table, alias, request, joinList, isProcedure, SIMPLE_CALLBACK);
	}


	// 支持 !key 反选字段 和 字段名映射，依赖插件 https://github.com/APIJSON/apijson-column
	@Override
	public AbstractSQLConfig setColumn(List<String> column) {
		if (ENABLE_COLUMN_CONFIG) {
			column = ColumnUtil.compatInputColumn(column, getTable(), getMethod(), getVersion(), ! isConfigTable());
		}
		return super.setColumn(column);
	}

	@Override
	public String getKey(String key) {
		if (ENABLE_COLUMN_CONFIG) {
			key = ColumnUtil.compatInputKey(key, getTable(), getMethod(), getVersion(), ! isConfigTable());
		}
		return super.getKey(key);
	}

}
