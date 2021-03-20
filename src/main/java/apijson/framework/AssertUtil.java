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


/**简单断言工具类，不用额外引入 JUnit 等库
 * @author Lemon
 */
public class AssertUtil {

	public static void assertEqual(Object a, Object b) {
		assertEqual(a, b, null);
	}
	
	public static void assertEqual(Object a, Object b, String errorMessage) {
		if (a == b) {
			return;
		}

		if (a == null || b == null || a.equals(b) == false) {
			throw new AssertionError(errorMessage == null ? "assert fail: a != b" : errorMessage);
		}
	}

}
