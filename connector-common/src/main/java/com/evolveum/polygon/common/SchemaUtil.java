/**
 * Copyright (c) 2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.polygon.common;

import java.util.List;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.Attribute;

/**
 * @author Radovan Semancik
 *
 */
public class SchemaUtil {

	public static <T> T getSingleValue(Attribute attribute, Class<T> expectedClass) {
		List<Object> values = attribute.getValue();
		if (values == null) {
			return null;
		}
		if (values.size() > 1) {
			throw new IllegalArgumentException("Attribute "+attribute.getName()+" cannot have multiple values");
		}
		Object value = values.get(0);
		if (!expectedClass.isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException("Expected that value of attribute "+attribute.getName()+" will be of "+expectedClass+", but it was "+value.getClass());
		}
		return (T)value;
	}

	public static String getSingleStringNonBlankValue(Attribute attribute) {
		String value = getSingleValue(attribute, String.class);
		if (value == null) {
			throw new IllegalArgumentException("Attribute "+attribute.getName()+" cannot be null");
		}
		if (StringUtil.isBlank(value)) {
			throw new IllegalArgumentException("Attribute "+attribute.getName()+" cannot be blank");
		}
		return value;
	}

}
