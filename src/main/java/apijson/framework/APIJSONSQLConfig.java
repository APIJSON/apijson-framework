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

import static apijson.framework.APIJSONConstant.ID;
import static apijson.framework.APIJSONConstant.PRIVACY_;
import static apijson.framework.APIJSONConstant.USER_;
import static apijson.framework.APIJSONConstant.USER_ID;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import apijson.JSONList;
import apijson.JSONMap;
//import apijson.column.ColumnUtil;

import apijson.RequestMethod;
import apijson.orm.AbstractSQLConfig;
import apijson.orm.Join;
import apijson.orm.SQLConfig;


/**SQL配置
 * TiDB 用法和 MySQL 一致
 * @author Lemon
 */
public class APIJSONSQLConfig<T, M extends Map<String, Object>, L extends List<Object>> extends AbstractSQLConfig<T, M, L> {
	public static final String TAG = "APIJSONSQLConfig";

	public static boolean ENABLE_COLUMN_CONFIG = false;

	public static Callback<?, ? extends Map<String, Object>, ? extends List<Object>> SIMPLE_CALLBACK;

	static {
		DEFAULT_DATABASE = DATABASE_MYSQL;  //TODO 默认数据库类型，改成你自己的
		DEFAULT_SCHEMA = "sys";  //TODO 默认模式名，改成你自己的，默认情况是 MySQL: sys, PostgreSQL: public, SQL Server: dbo, Oracle: 
		//		TABLE_KEY_MAP.put(Access.class.getSimpleName(), "apijson_access");

		//  由 APIJSONVerifier.init 方法读取数据库 Access 表来替代手动输入配置
		//		//表名映射，隐藏真实表名，对安全要求很高的表可以这么做
		//		TABLE_KEY_MAP.put(User.class.getSimpleName(), "apijson_user");
		//		TABLE_KEY_MAP.put(Privacy.class.getSimpleName(), "apijson_privacy");

		SIMPLE_CALLBACK = new SimpleCallback<Long, LinkedHashMap<String, Object>, List<Object>>() {

			@Override
			public SQLConfig<Long, LinkedHashMap<String, Object>, List<Object>> getSQLConfig(
					RequestMethod method, String database, String schema, String datasource, String table) {
				SQLConfig<Long, LinkedHashMap<String, Object>, List<Object>> config = APIJSONApplication.createSQLConfig();
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

	/**获取SQL配置
	 * @param table
	 * @param alias
	 * @param request
	 * @param isProcedure
	 * @return
	 * @throws Exception
	 */
	public static <T, M extends Map<String, Object>, L extends List<Object>> SQLConfig<T, M, L> newSQLConfig(
			RequestMethod method, String table, String alias, M request, List<Join<T, M, L>> joinList, boolean isProcedure) throws Exception {
		return newSQLConfig(method, table, alias, request, joinList, isProcedure, (SimpleCallback<T, M, L>) SIMPLE_CALLBACK);
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


	public String gainDBVersion() {
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

	public String gainDBUri() {
		if (isMySQL()) {
			return "jdbc:mysql://localhost:3306";
		}
		if (isTiDB()) {
			return "jdbc:mysql://localhost:4000";
		}
		if (isPostgreSQL()) { // PG JDBC 必须在 URI 传 catalog
			return "jdbc:postgresql://localhost:5432/postgres?stringtype=unspecified"; //TODO 改成你自己的
		}
		//if (isCockroachDB()) { // PG JDBC 必须在 URI 传 catalog
		//	return "jdbc:postgresql://localhost:26257/movr?sslmode=require"; //TODO 改成你自己的 brew install cockroachdb/tap/cockroach && cockroach demo
		//	// return "jdbc:postgresql://localhost:26258/postgres?sslmode=disable"; //TODO 改成你自己的 brew install cockroachdb/tap/cockroach # && start 3 nodes and init cluster
		//}
		if (isSQLServer()) {
			return "jdbc:jtds:sqlserver://localhost:1433/pubs;instance=SQLEXPRESS"; //TODO 改成你自己的
		}
		if (isOracle()) {
			return "jdbc:oracle:thin:@localhost:1521:orcl"; //TODO 改成你自己的
		}
		if (isDb2()) {
			return "jdbc:db2://localhost:50000/BLUDB"; //TODO 改成你自己的
		}
		if (isSQLite()) {
		  	return "jdbc:sqlite:sample.db"; //TODO 改成你自己的
		}
		if (isDameng()) {
			return "jdbc:dm://localhost:5236"; //TODO 改成你自己的
		}
		if (isTDengine()) {
			//      return "jdbc:TAOS://localhost:6030"; //TODO 改成你自己的
			return "jdbc:TAOS-RS://localhost:6041"; //TODO 改成你自己的
		}
		if (isTimescaleDB()) { // PG JDBC 必须在 URI 传 catalog
			return "jdbc:postgresql://localhost:5432/postgres?stringtype=unspecified"; //TODO 改成你自己的
		}
		if (isQuestDB()) { // PG JDBC 必须在 URI 传 catalog
			return "jdbc:postgresql://localhost:8812/qdb"; //TODO 改成你自己的
		}
		if (isInfluxDB()) {
			return "http://203.189.6.3:8086"; //TODO 改成你自己的
		}
		if (isMilvus()) {
			return "http://localhost:19530"; //TODO 改成你自己的
		}
		if (isManticore()) {
			return "jdbc:mysql://localhost:9306?characterEncoding=utf8&maxAllowedPacket=512000";
		}
		if (isIoTDB()) {
			return "jdbc:iotdb://localhost:6667"; // ?charset=GB18030 加参数会报错 URI 格式错误
		}
		if (isMongoDB()) {
			return "jdbc:mongodb://atlas-sql-6593c65c296c5865121e6ebe-xxskv.a.query.mongodb.net/myVirtualDatabase?ssl=true&authSource=admin";
		}
		if (isCassandra()) {
			return "http://localhost:7001";
		}
		if (isDuckDB()) {
			return "jdbc:duckdb:/Users/root/my_database.duckdb";
		}
		if (isSurrealDB()) {
			//	return "memory";
			//	return "surrealkv://localhost:8000";
			return "ws://localhost:8000";
		}
		if (isOpenGauss()) {
			return "jdbc:opengauss://127.0.0.1:5432/postgres?currentSchema=" + DEFAULT_SCHEMA;
		}
		if (isDoris()) {
			return "jdbc:mysql://localhost:9030";
		}
		return null;
	}

	public String gainDBAccount() {
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
		if (isMySQL()) {
			return "root"; // ""apijson";  //TODO 改成你自己的
		}
		if (isPostgreSQL()) {
			return "postgres";  //TODO 改成你自己的
		}
		//if (isCockroachDB()) { // PG JDBC 必须在 URI 传 catalog
		//	return "demo"; //TODO 改成你自己的
		//	//return "postgres"; //TODO 改成你自己的
		//}
		if (isSQLServer()) {
			return "sa";  //TODO 改成你自己的
		}
		if (isOracle()) {
			return "scott";  //TODO 改成你自己的
		}
		if (isDb2()) {
			return "db2admin"; //TODO 改成你自己的
		}
		//  if (isSQLite()) {
		//  	return "root"; //TODO 改成你自己的
		//  }
		if (isDameng()) {
			return "SYSDBA";
		}
		if (isTDengine()) {
			return "root"; //TODO 改成你自己的
		}
		//if (isTimescaleDB()) {
		//	return "postgres";  //TODO 改成你自己的
		//}
		if (isQuestDB()) {
			return "admin";  //TODO 改成你自己的
		}
		if (isInfluxDB()) {
			return "iotos";
		}
		if (isMilvus()) {
			return "root";
		}
		if (isManticore()) {
			return null; // "root";
		}
		if (isIoTDB()) {
			return "root";
		}
		if (isMongoDB()) {
			return "root"; //TODO 改成你自己的
		}
		if (isCassandra()) {
			return "root"; //TODO 改成你自己的
		}
		if (isDuckDB()) {
			return "root"; //TODO 改成你自己的
		}
		if (isSurrealDB()) {
			return "root"; //TODO 改成你自己的
		}
		if (isOpenGauss()) {
			return "postgres"; //TODO 改成你自己的
			// 不允许用初始账号，需要 CREATE USER 创建新账号并 GRANT 授权 return "opengauss"; //TODO 改成你自己的
		}
		if (isDoris()) {
			return "root";  //TODO 改成你自己的
		}

		return null;
	}

	public String gainDBPassword() {
		if (isMySQL()) {
			return "yourPassword@123";
		}
		if (isTiDB()) {
			return "";
		}
		if (isPostgreSQL()) {
			return null;
		}
		if (isSQLServer()) {
			return "yourPassword@123";
		}
		if (isOracle()) {
			return "tiger";
		}
		//if (isCockroachDB()) { // PG JDBC 必须在 URI 传 catalog
		//	return "demo39865";
		//	// return null
		//}
		if (isSQLServer()) {
			return "yourPassword@123";
		}
		if (isOracle()) {
			return "tiger";
		}
		if (isDb2()) {
			return "123";
		}
		if (isSQLite()) {
		  	return "yourPassword@123";
		}
		if (isDameng()) {
			return "SYSDBA";
		}
		if (isTDengine()) {
			return "taosdata";
		}
		if (isTimescaleDB()) {
			return "password";
		}
		if (isQuestDB()) {
			return "quest";
		}
		if (isInfluxDB()) {
			return "yourPassword@123";
		}
		if (isMilvus()) {
			return "yourPassword@123";
		}
		//if (isManticore()) {
		//	return null;
		//}
		//if (isIoTDB()) {
		//	return "root";
		//}
		if (isMongoDB()) {
			return "yourPassword@123";
		}
		if (isCassandra()) {
			return "yourPassword@123";
		}
		if (isDuckDB()) {
			return "";
		}
		if (isSurrealDB()) {
			return "root";
		}
		if (isOpenGauss()) {
			return "yourPassword@123";
		}
		if (isDoris()) {
			return "";
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
	public String gainSQLDatabase() {
		String db = isConfigTable() ? getConfigDatabase() : super.gainSQLDatabase();
		return db == null ? DEFAULT_DATABASE : db;
	}
	@Override
	public String gainSQLSchema() {
		String sch = isConfigTable() ? getConfigSchema() : super.gainSQLSchema();
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


	// 支持 !key 反选字段 和 字段名映射，依赖插件 https://github.com/APIJSON/apijson-column
	@Override
	public APIJSONSQLConfig<T, M, L> setColumn(List<String> column) {
		if (ENABLE_COLUMN_CONFIG) {
			column = ColumnUtil.compatInputColumn(column, getTable(), getMethod(), getVersion(), ! isConfigTable());
		}
		super.setColumn(column);
		return this;
	}

	@Override
	public String gainKey(String key) {
		if (ENABLE_COLUMN_CONFIG) {
			key = ColumnUtil.compatInputKey(key, getTable(), getMethod());
		}
		return super.gainKey(key);
	}

}
