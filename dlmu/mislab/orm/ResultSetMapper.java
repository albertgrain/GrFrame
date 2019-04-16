// By GuRui on 2015-12-7 下午12:45:53
package dlmu.mislab.orm;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import org.apache.commons.beanutils.BeanUtils;
import dlmu.mislab.tool.Reflect;

class ResultSetMapper {
	/***
	 * 将ResultSet映射到指定的IOrmBean类型Bean
	 * @param rs
	 * @param outputBeanClass
	 * @return 如果ResultSet为空则返回空，如果ResultSet没有记录则发回长度为0的List。出错否则抛出OrmException。
	 */
	public static <T extends IOrmBean> List<T> resultSet2Bean(ResultSet rs, Class<T> outputBeanClass) {
		List<T> rtn = new LinkedList<T>();
		try {
			// make sure resultset is not null
			if (rs != null) {
				// get the resultset metadata
				ResultSetMetaData rsmd = rs.getMetaData();
				// get all the attributes of outputClass
				//Field[] fields = outputClass.getDeclaredFields();
				List<Field> fields = Reflect.getFieldsUpTo(outputBeanClass);
				
				while (rs.next()) {
					T bean = outputBeanClass.newInstance();
					for (int idx = 0; idx < rsmd.getColumnCount(); idx++) {
						// getting the SQL column name
						String columnName = rsmd.getColumnLabel(idx + 1);
						// reading the value of the SQL column
						Object columnValue = rs.getObject(idx + 1);
						for(int i=0;i<fields.size();i++){
							Field f = fields.get(i);
							if (f.getName().equalsIgnoreCase(columnName) && columnValue != null) {
								BeanUtils.setProperty(bean, f.getName(), columnValue);
								break;
							}
						}
					}
					rtn.add(bean);
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new OrmException(e);
		}
		return (List<T>) rtn;
	}
	
	/***
	 * 将查询结果转化为一个由key-object组成的对象列表的列表。
	 * 外层列表内的每一个列表代表一行，内层列表每个KeyObject对象代表一个元素
	 * By GuRui on 2016-6-18 上午10:41:40
	 * @param rs 查询结果集
	 * @return 没有结果返回一个长度为0的空List
	 * @throws SQLException
	 */
	public static List<List<SimpleEntry<String, Object>>> resultSet2KeyValuePair(ResultSet rs) throws SQLException {
		List<List<SimpleEntry<String, Object>>> rtn=new LinkedList<List<SimpleEntry<String, Object>>>();
		if (rs != null) {
			ResultSetMetaData rsmd = rs.getMetaData();
			int size=rsmd.getColumnCount();
			while (rs.next()) {
				List<SimpleEntry<String, Object>> row=new ArrayList<SimpleEntry<String, Object>>(size);
				for (int itr = 0; itr < size; itr++) {
					String columnName = rsmd.getColumnLabel(itr + 1);
					Object columnValue = rs.getObject(itr + 1);
					row.add(new SimpleEntry<String, Object>(columnName, columnValue));
				}
				rtn.add(row);
			}
		}
		return rtn;
	}
}
