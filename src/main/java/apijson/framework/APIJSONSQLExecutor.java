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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.postgresql.util.PGobject;

import com.alibaba.fastjson.JSONObject;

import apijson.JSON;
import apijson.Log;
import apijson.NotNull;
import apijson.orm.AbstractSQLExecutor;
import apijson.orm.SQLConfig;


/**executor for query(read) or update(write) MySQL database
 * @author Lemon
 */
public class APIJSONSQLExecutor extends AbstractSQLExecutor {
	public static final String TAG = "APIJSONSQLExecutor";

	static {
		try { //加载驱动程序
			Log.d(TAG, "尝试加载 MySQL 8 驱动 <<<<<<<<<<<<<<<<<<<<< ");
			Class.forName("com.mysql.cj.jdbc.Driver");
			Log.d(TAG, "成功加载 MySQL 8 驱动！>>>>>>>>>>>>>>>>>>>>>");
		}
		catch (ClassNotFoundException e) {
			Log.e(TAG, "加载 MySQL 8 驱动失败，请检查 pom.xml 中 mysql-connector-java 版本是否存在以及可用 ！！！");
			e.printStackTrace();

			try { //加载驱动程序
				Log.d(TAG, "尝试加载 MySQL 7 及以下版本的 驱动 <<<<<<<<<<<<<<<<<<<<< ");
				Class.forName("com.mysql.jdbc.Driver");
				Log.d(TAG, "成功加载 MySQL 7 及以下版本的 驱动！>>>>>>>>>>>>>>>>>>>>> ");
			}
			catch (ClassNotFoundException e2) {
				Log.e(TAG, "加载 MySQL 7 及以下版本的 驱动失败，请检查 pom.xml 中 mysql-connector-java 版本是否存在以及可用 ！！！");
				e2.printStackTrace();
			}
		}

		try { //加载驱动程序
			Log.d(TAG, "尝试加载 PostgresSQL 驱动 <<<<<<<<<<<<<<<<<<<<< ");
			Class.forName("org.postgresql.Driver");
			Log.d(TAG, "成功加载 PostgresSQL 驱动！>>>>>>>>>>>>>>>>>>>>> ");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "加载 PostgresSQL 驱动失败，请检查 libs 目录中 postgresql.jar 版本是否存在以及可用 ！！！");
		}
		
		
		try { //加载驱动程序
			Log.d(TAG, "尝试加载 SQLServer 驱动 <<<<<<<<<<<<<<<<<<<<< ");
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Log.d(TAG, "成功加载 SQLServer 驱动！>>>>>>>>>>>>>>>>>>>>> ");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "加载 SQLServer 驱动失败，请检查 pom.xml 中 net.sourceforge.jtds 版本是否存在以及可用 ！！！");
		}
		
		try { //加载驱动程序
			Log.d(TAG, "尝试加载 Oracle 驱动 <<<<<<<<<<<<<<<<<<<<< ");
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Log.d(TAG, "成功加载 Oracle 驱动！>>>>>>>>>>>>>>>>>>>>> ");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "加载 Oracle 驱动失败，请检查 pom.xml 中 com.oracle.jdbc 版本是否存在以及可用 ！！！");
		}
		
		try { //加载驱动程序
			Log.d(TAG, "尝试加载 DB2 驱动 <<<<<<<<<<<<<<<<<<<<< ");
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			Log.d(TAG, "成功加载 DB2 驱动！>>>>>>>>>>>>>>>>>>>>> ");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "加载 DB2 驱动失败，请检查 pom.xml 中 com.ibm.db2 版本是否存在以及可用 ！！！");
		}
		
	}


	@Override
	public PreparedStatement setArgument(@NotNull SQLConfig config, @NotNull PreparedStatement statement, int index, Object value) throws SQLException {
		if (config.isPostgreSQL() && JSON.isBooleanOrNumberOrString(value) == false) {
			PGobject o = new PGobject();
			o.setType("jsonb");
			o.setValue(value == null ? null : value.toString());
			statement.setObject(index + 1, o); //PostgreSQL 除了基本类型，其它的必须通过 PGobject 设置进去，否则 jsonb = varchar 等报错
			return statement;
		}
		
		return super.setArgument(config, statement, index, value);
	}


	@Override
	protected Object getValue(SQLConfig config, ResultSet rs, ResultSetMetaData rsmd, int tablePosition,
			JSONObject table, int columnIndex, String lable, Map<String, JSONObject> childMap) throws Exception {
		
		Object value = super.getValue(config, rs, rsmd, tablePosition, table, columnIndex, lable, childMap);

		return value instanceof PGobject ? JSON.parse(((PGobject) value).getValue()) : value;
	}


}
