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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import apijson.JSON;

/**base model for reduce model codes
 * @author Lemon
 * @use extends BaseModel
 */
public abstract class BaseModel<T extends Object, D extends Object> implements Serializable {
	private static final long serialVersionUID = 1L;

	private T id;       //主键，唯一标识, Long 或 String
	private T userId;   //所属人 ID，对应User表中的id，外键, Long 或 String
	private T creatorId;   //创建人 ID，对应User表中的id，外键, Long 或 String
	private T updaterId;   //编辑人 ID，对应User表中的id，外键, Long 或 String
	private T deleterId;   //删除人 ID，对应User表中的id，外键, Long 或 String
	private T createdBy;   //创建人 ID，对应User表中的id，外键, Long 或 String
	private T updatedBy;   //编辑人 ID，对应User表中的id，外键, Long 或 String
	private T deletedBy;   //删除人 ID，对应User表中的id，外键, Long 或 String

	private D date;   //创建时间，JSON没有Date,TimeStamp类型，都会被转成Long，不能用！
	private D time;   //创建时间，JSON没有Date,TimeStamp类型，都会被转成Long，不能用！
	// 可以类型用 String，或重写 getCreateTime 加注解 @JSONField(format = "yyyy-MM-dd HH:mm:ss") 或全局配置 JSON.DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private D createTime;   //创建时间，JSON没有Date,TimeStamp类型，都会被转成Long，不能用！
	private D updateTime;   //更新时间，JSON没有Date,TimeStamp类型，都会被转成Long，不能用！
	private D deleteTime;   //删除时间，JSON没有Date,TimeStamp类型，都会被转成Long，不能用！
	private D createdAt;   //创建时间，JSON没有Date,TimeStamp类型，都会被转成Long，不能用！
	private D updatedAt;   //更新时间，JSON没有Date,TimeStamp类型，都会被转成Long，不能用！
	private D deletedAt;   //删除时间，JSON没有Date,TimeStamp类型，都会被转成Long，不能用！

	public T getId() {
		return id;
	}
	public BaseModel<T, D> setId(T id) {
		this.id = id;
		return this;
	}
	public T getUserId() {
		return userId;
	}
	public BaseModel<T, D> setUserId(T userId) {
		this.userId = userId;
		return this;
	}

	public T getCreatorId() {
		return creatorId;
	}
	public BaseModel<T, D> setCreatorId(T creatorId) {
		this.creatorId = creatorId;
		return this;
	}

	public T getUpdaterId() {
		return updaterId;
	}
	public BaseModel<T, D> setUpdaterId(T updaterId) {
		this.updaterId = updaterId;
		return this;
	}

	public T getDeleterId() {
		return deleterId;
	}
	public BaseModel<T, D> setDeleterId(T deleterId) {
		this.deleterId = deleterId;
		return this;
	}

	public T getCreatedBy() {
		return createdBy;
	}
	public BaseModel<T, D> setCreatedBy(T createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	public T getUpdatedBy() {
		return updatedBy;
	}
	public BaseModel<T, D> setUpdatedBy(T updatedBy) {
		this.updatedBy = updatedBy;
		return this;
	}

	public T getDeletedBy() {
		return deletedBy;
	}
	public BaseModel<T, D> setDeletedBy(T deletedBy) {
		this.deletedBy = deletedBy;
		return this;
	}

	public D getDate() {
		return date;
	}
	public BaseModel<T, D> setDate(D date) {
		this.date = date;
		return this;
	}

	public D getTime() {
		return time;
	}
	public BaseModel<T, D> setTime(D time) {
		this.time = time;
		return this;
	}

	public D getCreateTime() {
		return createTime;
	}
	public BaseModel<T, D> setCreateTime(D createTime) {
		this.createTime = createTime;
		return this;
	}

	public D getUpdateTime() {
		return updateTime;
	}
	public BaseModel<T, D> setUpdateTime(D updateTime) {
		this.updateTime = updateTime;
		return this;
	}

	public D getDeleteTime() {
		return deleteTime;
	}
	public BaseModel<T, D> setDeleteTime(D deleteTime) {
		this.deleteTime = deleteTime;
		return this;
	}

	public D getCreatedAt() {
		return createdAt;
	}
	public BaseModel<T, D> setCreatedAt(D createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public D getUpdatedAt() {
		return updatedAt;
	}
	public BaseModel<T, D> setUpdatedAt(D updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

	public D getDeletedAt() {
		return deletedAt;
	}
	public BaseModel<T, D> setDeletedAt(D deletedAt) {
		this.deletedAt = deletedAt;
		return this;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}


	/**获取当前时间
	 * @return
	 */
	public static Date currentTime() {
		return new Date();
	}
	/**获取时间
	 * @param time
	 * @return
	 */
	public static Date toTime(String time) {
		return new Date(time);
	}
	/**获取当前时间戳
	 * @return
	 */
	public static Timestamp currentTimeStamp() {
	    return new Timestamp(new Date().getTime());
	}
	/**获取时间戳
	 * @param time
	 * @return
	 */
	public static Timestamp toTimeStamp(String time) {
		return Timestamp.valueOf(time);
	}
	public static Timestamp toTimeStamp(Date time) {
		return time instanceof Timestamp ? (Timestamp) time : new Timestamp(time.getTime());
	}
	/**获取时间毫秒值 TODO 判空？ 还是要报错？
	 * @param time
	 * @return
	 */
	public static long toTimeMillis(String time) {
		return toTime(time).getTime();
	}
	public static long toTimeMillis(Date time) {
		return time == null ? 0 : time.getTime();
	}

	//判断是否为空 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**判断array是否为空
	 * @param array
	 * @return
	 */
	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length <= 0;
	}
	/**判断collection是否为空
	 * @param collection
	 * @return
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}
	/**判断map是否为空
	 * @param map
	 * @return
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
	//判断是否为空 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//判断是否包含 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**判断array是否包含a
	 * @param array
	 * @param a
	 * @return
	 */
	public static <T> boolean isContain(T[] array, T a) {
		return array == null ? false : Arrays.asList(array).contains(a);
	}
	/**判断collection是否包含object
	 * @param collection
	 * @param object
	 * @return
	 */
	public static <T> boolean isContain(Collection<T> collection, T object) {
		return collection != null && collection.contains(object);
	}
	/**判断map是否包含key
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @param key
	 * @return
	 */
	public static <K, V> boolean isContainKey(Map<K, V> map, K key) {
		return map != null && map.containsKey(key);
	}
	/**判断map是否包含value
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @param value
	 * @return
	 */
	public static <K, V> boolean isContainValue(Map<K, V> map, V value) {
		return map != null && map.containsValue(value);
	}
	//判断是否为包含 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//获取集合长度 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取数量
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static <T> int count(T[] array) {
		return array == null ? 0 : array.length;
	}
	/**获取数量
	 * @param collection List, Vector, Set等都是Collection的子类
	 * @return
	 */
	public static int count(Collection<?> collection) {
		return collection == null ? 0 : collection.size();
	}
	/**获取数量
	 * @param map
	 * @return
	 */
	public static int count(Map<?, ?> map) {
		return map == null ? 0 : map.size();
	}
	//获取集合长度 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//获取集合长度 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static <T> T get(T[] array, int position) {
		return position < 0 || position >= count(array) ? null : array[position];
	}
	/**获取
	 * @param <T>
	 * @param collection List, Vector, Set等都是Collection的子类
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(Collection<T> collection, int position) {
		return collection == null ? null : (T) get(collection.toArray(), position);
	}
	/**获取
	 * @param <K>
	 * @param <V>
	 * @param map null ? null
	 * @param key null ? null : map.get(key);
	 * @return
	 */
	public static <K, V> V get(Map<K, V> map, K key) {
		return key == null || map == null ? null : map.get(key);
	}
	//获取集合长度 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>



	//获取非基本类型对应基本类型的非空值 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取非空值
	 * @param value
	 * @return
	 */
	public static boolean value(Boolean value) {
		return value == null ? false : value;
	}
	/**获取非空值
	 * @param value
	 * @return
	 */
	public static int value(Integer value) {
		return value == null ? 0 : value;
	}
	/**获取非空值
	 * @param value
	 * @return
	 */
	public static long value(Long value) {
		return value == null ? 0 : value;
	}
	/**获取非空值
	 * @param value
	 * @return
	 */
	public static float value(Float value) {
		return value == null ? 0 : value;
	}
	/**获取非空值
	 * @param value
	 * @return
	 */
	public static double value(Double value) {
		return value == null ? 0 : value;
	}
	//获取非基本类型对应基本类型的非空值 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	/**index是否在arr长度范围内
	 * @param index
	 * @param array
	 * @return
	 */
	public static boolean isIndexInRange(Integer index, Object[] array) {
		return index != null && index >= 0 && index < count(array);
	}

	/**获取在arr长度范围内的index
	 * defaultIndex = 0
	 * @param index
	 * @param array
	 * @return
	 */
	public static int getIndexInRange(Integer index, Object[] array) {
		return getIndexInRange(index, array, 0);
	}
	/**获取在arr长度范围内的index
	 * @param index
	 * @param array
	 * @param defaultIndex
	 * @return
	 */
	public static int getIndexInRange(Integer index, Object[] array, int defaultIndex) {
		return isIndexInRange(index, array) ? index : defaultIndex;
	}

	/**获取在arr长度范围内的index
	 * defaultIndex = 0
	 * @param <T>
	 * @param index
	 * @param array
	 * @return
	 */
	public static <T> T getInRange(Integer index, T[] array) {
		return getInRange(index, array, 0);
	}
	/**获取在arr长度范围内的index
	 * @param <T>
	 * @param index
	 * @param array
	 * @param defaultIndex
	 * @return
	 */
	public static <T> T getInRange(Integer index, T[] array, int defaultIndex) {
		return get(array, getIndexInRange(index, array, defaultIndex));
	}

}
