// By GuRui on 2014-11-28 上午4:58:53
package dlmu.mislab.validation;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import dlmu.mislab.common.ConfigBase;
import dlmu.mislab.common.DictBase;
import dlmu.mislab.common.KeyValuePair;
import dlmu.mislab.common.LogicError;
import dlmu.mislab.orm.annotation.IsKey;
import dlmu.mislab.orm.annotation.RefTo;
import dlmu.mislab.tool.Reflect;
import dlmu.mislab.web.interact.IParameter;
import dlmu.mislab.web.response.Err;

public abstract class Validator implements IValidation {

	/***
	 * 对Bean的值进行验证 (采用系统默认的验证类存放路径) By GuRui on 2016-9-19 上午9:02:38
	 * 
	 * @param bean
	 *            待验证的bean对象
	 * @return null if no error
	 */
	public Err validateBean(IParameter bean) {
		return Validator.validateBean(bean,	ConfigBase.FIELD_VALIDATION_PACKAGE_NAME);
	}
	
	public static Err validateBeanStatic(IParameter bean) {
		return Validator.validateBean(bean,	ConfigBase.FIELD_VALIDATION_PACKAGE_NAME);
	}

	/***
	 * 对Bean的值进行验证 By GuRui on 2016-9-19 上午9:02:56
	 * 
	 * @param bean
	 *            待验证的bean对象
	 * @param validationPackgeName
	 *            验证类所在包名
	 * @return
	 */
	public static Err validateBean(IParameter bean, String validationPackgeName) {
		List<KeyValuePair> errs = Validator.validateAll(bean,
				validationPackgeName);
		
		if (errs.size() > 0) {
			return Err.MAPPING_ERROR(errs);

		}
		return null;
	}

	/**
	 * <p>
	 * 验证通过返回null，否则抛出ValidationException异常（属于LogicError类型）
	 * </p>
	 * 使用方法：
	 * 
	 * <pre>
	 * --AParam implements IParameter;
	 * --Aparam bean;
	 * --String validation_package="com.test.validation.field";
	 * LogicError err=Validator.validate(bean,validation_package);
	 * if(err!=null){
	 * 	  return Err.getInstance(err);
	 * }
	 * </pre>
	 * 
	 * @param obj
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ValidationException
	 */
	public static boolean validate(IValidatable obj,
			String validationPackageName) {
		List<KeyValuePair> rtn = new LinkedList<KeyValuePair>();
		Validator.doValidation(rtn, null, obj, validationPackageName, false);
		return rtn.size() == 0;
	}

	/***
	 * Validate all fields without stopping at the first error. By GuRui on
	 * 2015-12-5 下午12:15:11
	 * 
	 * @param obj
	 * @param validationPackageName
	 * @param findAllErrors
	 *            default true. If false, only the first error will be returned.
	 * @return
	 */
	public static List<KeyValuePair> validateAll(IValidatable obj,
			String validationPackageName) throws ValidationException {
		List<KeyValuePair> rtn = new LinkedList<KeyValuePair>();
		try{
			doValidation(rtn, null, obj, validationPackageName, true);
		}catch(ValidationError e){
			//
			rtn.add(new KeyValuePair(e.getFieldName(),e.getMessage()));
		}catch(ValidationException e){
			rtn.add(new KeyValuePair("ValidationException",e.getMessage()));
		}
		return rtn;
	}

	private static void doValidation(List<KeyValuePair> rtn, String fieldName, IValidatable obj,
			String validationPackageName, boolean findAllErrors)
			throws ValidationException {
		if (obj == null) {
			throw new ValidationError(null, "待验证对象为空");
		}
//		try {
			List<Field> fs = Reflect.getFieldsUpTo(obj.getClass());

			for (Field f : fs) {
				if (f.isAnnotationPresent(NoValidation.class)
						|| (f.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT) {
					continue;
				}
				if (f.getType().isPrimitive()) {
					throw new ValidationException(
							"Primary type should not be used in bean.\n (You should use Integer than int)");
				}
				
				f.setAccessible(true);
				Object val=null;
				try {
					val = f.get(obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new ValidationException("System Error: Failed to get the value of property "+f.getName()+"of the validating object.\n" + e.getMessage(), e);
				}
				if (val == null){
					if(f.isAnnotationPresent(NotNull.class) || f.isAnnotationPresent(IsKey.class)){
						throw new ValidationError(f.getName(),"["+ DictBase.getFieldNameCn(f.getName())+"] 不可为空");
					}else{
						continue;
					}
				}
				if (f.isAnnotationPresent(RefTo.class)) { //Do not validate this field, but validation its' children
					if (f.getType().isArray()) { //is Array
						validateArray(rtn,f, val, validationPackageName, findAllErrors);
					}else if(Collection.class.isAssignableFrom(f.getType())){ //is List, but may include Collection
						validateCollection(rtn,f, val, validationPackageName, findAllErrors);
					} else { // is object, but contains validatable objects
						validateContainerObject(rtn,f, val, validationPackageName, findAllErrors);
					}
				} else {
					Class<?> cls=null;
					try {
						cls = Class.forName(validationPackageName + "." + f.getName());
					} catch (ClassNotFoundException e) {
						throw new ValidationException(
								"System Error: Failed to locate the validation class ["+f.getName()+"] of a Bean field.", e);
					}

					Constructor<?> cst=null;
					try {
						cst = cls.getConstructor(f.getType());
					} catch (NoSuchMethodException | SecurityException e) {
						throw new ValidationException(
								"System Error: Failed to locate the constructor of validation class ["+f.getName()+"] of a Bean field.", e);
					}
					Object validator=null;
					try {
						validator = cst.newInstance(val);
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException e) {
						throw new ValidationException(
								"System Error: Failed to initialize a new object from validation class ["+f.getName()+"] of a Bean field. Please check the parameter fro constructor.", e);
					}
					
					if (validator instanceof IValidation) {
						if (f.isAnnotationPresent(NotNull.class)) {
							((IProperty) validator).setNullable(false);
						}
						LogicError err = ((IValidation) validator).validate();
						if (err != null) {
							rtn.add(new KeyValuePair(f.getName(), err.getMsg()));
							if (!findAllErrors) {
								throw err;
							}
						}
					}
				}
			}
		/*} catch (NoSuchMethodException e) {
			//"验证字段时发生系统错误:没有找到应验证类的构造方法。请检查:\n(1)验证类的查构造方法签名是否正确，(2)被检验Bean的对应字段类型是否正确。"
			throw new ValidationException(
					"System Error: Failed to locate the validation class for a field, please check the signature of the constructor of the validation class.\n" + e.getMessage(),
					e);
		} catch (ClassNotFoundException e) {
			//"验证字段时发生系统错误:没有找到字段对应的验证类。请检查字段名是否完全一致"
			throw new ValidationException(
					"System Error: Failed to locate the validation class for a field.\n" + e.getMessage(), e);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			//"根据类创建对象失败。请检查类中是否存在无参数构造函数"
			throw new ValidationException("System Error: Failed to initialize Object by class. Please ensure the existance of no-parameter constructor.\n" + e.getMessage(), e);
		}*/
	}
	
	private static void throwNotValidatable(String fieldName){
		throw new ValidationException("Validation Error: The field ["+fieldName+"] or its children elements are not validatable.");
	}
	
	private static void validateArray(List<KeyValuePair> rtn, Field f, Object val, String validationPackageName, boolean findAllErrors){
		int len = Array.getLength(val);
		for (int i = 0; i < len; i++) {
			Object child = Array.get(val, i);
			if(child instanceof IValidatable){
				doValidation(rtn, f.getName(), (IValidatable) child,	validationPackageName, findAllErrors);
			}else{
				throwNotValidatable(f.getName());
			}
		}
	}
	
	private static void validateCollection(List<KeyValuePair> rtn, Field f, Object val, String validationPackageName, boolean findAllErrors){
		@SuppressWarnings("rawtypes")
		Collection cl=(Collection)val;
		for(Object o:cl){
			if(o instanceof IValidatable){
				doValidation(rtn, f.getName(), (IValidatable) o, validationPackageName, findAllErrors);
			}else{
				throwNotValidatable(f.getName());
			}
		}
	}
	
	private static void validateContainerObject(List<KeyValuePair> rtn, Field f, Object val, String validationPackageName, boolean findAllErrors){
		if(val instanceof IValidatable){
			doValidation(rtn, f.getName(), (IValidatable) val, validationPackageName, findAllErrors);
		}else{
			throwNotValidatable(f.getName());
		}
	}
}
