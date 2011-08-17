/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
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
package com.orientechnologies.orient.core.record.impl;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.orientechnologies.common.util.OPair;
import com.orientechnologies.orient.core.config.OStorageConfiguration;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement.STATUS;
import com.orientechnologies.orient.core.db.record.ORecordLazyList;
import com.orientechnologies.orient.core.db.record.ORecordLazyMap;
import com.orientechnologies.orient.core.db.record.ORecordLazySet;
import com.orientechnologies.orient.core.db.record.ORecordTrackedList;
import com.orientechnologies.orient.core.db.record.ORecordTrackedSet;
import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.db.record.OTrackedMap;
import com.orientechnologies.orient.core.db.record.OTrackedSet;
import com.orientechnologies.orient.core.exception.OQueryParsingException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.serialization.serializer.OStringSerializerHelper;

/**
 * Helper class to manage documents.
 * 
 * @author Luca Garulli
 * 
 */
public class ODocumentHelper {
	public static void sort(List<OIdentifiable> ioResultSet, List<OPair<String, String>> iOrderCriteria) {
		Collections.sort(ioResultSet, new ODocumentComparator(iOrderCriteria));
	}

	@SuppressWarnings("unchecked")
	public static <RET> RET convertField(final ODocument iDocument, final String iFieldName, final Class<?> iFieldType, Object iValue) {
		if (iFieldType == null)
			return (RET) iValue;

		if (ORID.class.isAssignableFrom(iFieldType)) {
			if (iValue instanceof ORID) {
				return (RET) iValue;
			} else if (iValue instanceof String) {
				return (RET) new ORecordId((String) iValue);
			} else if (iValue instanceof ORecord<?>) {
				return (RET) ((ORecord<?>) iValue).getIdentity();
			}
		} else if (ORecord.class.isAssignableFrom(iFieldType)) {
			if (iValue instanceof ORID || iValue instanceof ORecord<?>) {
				return (RET) iValue;
			} else if (iValue instanceof String) {
				return (RET) new ORecordId((String) iValue);
			}
		} else if (Set.class.isAssignableFrom(iFieldType)) {
			if (!(iValue instanceof Set)) {
				// CONVERT IT TO SET
				final Collection<?> newValue;

				if (iValue instanceof ORecordLazyList || iValue instanceof ORecordLazyMap)
					newValue = new ORecordLazySet(iDocument);
				else
					newValue = new OTrackedSet<Object>(iDocument);

				if (iValue instanceof Collection<?>) {
					((Collection<Object>) newValue).addAll((Collection<Object>) iValue);
					return (RET) newValue;
				} else if (iValue instanceof Map) {
					((Collection<Object>) newValue).addAll(((Map<String, Object>) iValue).values());
					return (RET) newValue;
				} else if (iValue instanceof String) {
					final String stringValue = (String) iValue;

					if (stringValue != null && stringValue.length() > 0) {
						final String[] items = stringValue.split(",");
						for (String s : items) {
							((Collection<Object>) newValue).add(s);
						}
					}
					return (RET) newValue;
				}
			} else {
				return (RET) iValue;
			}
		} else if (List.class.isAssignableFrom(iFieldType)) {
			if (!(iValue instanceof List)) {
				// CONVERT IT TO LIST
				final Collection<?> newValue;

				if (iValue instanceof ORecordLazySet || iValue instanceof ORecordLazyMap)
					newValue = new ORecordLazyList(iDocument);
				else
					newValue = new OTrackedList<Object>(iDocument);

				if (iValue instanceof Collection) {
					((Collection<Object>) newValue).addAll((Collection<Object>) iValue);
					return (RET) newValue;
				} else if (iValue instanceof Map) {
					((Collection<Object>) newValue).addAll(((Map<String, Object>) iValue).values());
					return (RET) newValue;
				} else if (iValue instanceof String) {
					final String stringValue = (String) iValue;

					if (stringValue != null && stringValue.length() > 0) {
						final String[] items = stringValue.split(",");
						for (String s : items) {
							((Collection<Object>) newValue).add(s);
						}
					}
					return (RET) newValue;
				}
			} else {
				return (RET) iValue;
			}
		} else if (iValue instanceof Enum) {
			// ENUM
			if (Number.class.isAssignableFrom(iFieldType))
				iValue = ((Enum<?>) iValue).ordinal();
			else
				iValue = iValue.toString();
			if (!(iValue instanceof String) && !iFieldType.isAssignableFrom(iValue.getClass()))
				throw new IllegalArgumentException("Property '" + iFieldName + "' of type '" + iFieldType
						+ "' can't accept value of type: " + iValue.getClass());
		} else if (Date.class.isAssignableFrom(iFieldType)) {
			if (iValue instanceof String && iDocument.getDatabase() != null) {
				final OStorageConfiguration config = iDocument.getDatabase().getStorage().getConfiguration();

				DateFormat formatter = config.getDateFormatInstance();

				if (((String) iValue).length() > config.dateFormat.length()) {
					// ASSUMES YOU'RE USING THE DATE-TIME FORMATTE
					formatter = config.getDateTimeFormatInstance();
				}

				try {
					Date newValue = formatter.parse((String) iValue);
					// _fieldValues.put(iFieldName, newValue);
					return (RET) newValue;
				} catch (ParseException pe) {
					final String dateFormat = ((String) iValue).length() > config.dateFormat.length() ? config.dateTimeFormat
							: config.dateFormat;
					throw new OQueryParsingException("Error on conversion of date '" + iValue + "' using the format: " + dateFormat);
				}
			}
		}

		iValue = OType.convert(iValue, iFieldType);

		return (RET) iValue;
	}

	@SuppressWarnings("unchecked")
	public static <RET> RET getFieldValue(ODocument current, final String iFieldName) {
		int separatorPos = 0;

		final int fieldNameLength = iFieldName.length();
		Object value = null;

		do {
			char separator = ' ';
			for (; separatorPos < fieldNameLength; ++separatorPos) {
				separator = iFieldName.charAt(separatorPos);
				if (separator == '.' || separator == '[')
					break;
			}

			final String fieldName;
			if (separatorPos < fieldNameLength)
				fieldName = iFieldName.substring(0, separatorPos);
			else
				fieldName = iFieldName;

			if (separator == '.') {
				// GET THE LINKED OBJECT IF ANY
				value = current._fieldValues.get(fieldName);

				if (value == null || !(value instanceof ODocument))
					// IGNORE IT BY RETURNING NULL
					return null;

				current = (ODocument) value;
				if (current.getInternalStatus() == STATUS.NOT_LOADED)
					// LAZY LOAD IT
					current.reload();
			} else if (separator == '[') {
				if (value == null)
					value = current._fieldValues.get(fieldName);

				final int end = iFieldName.indexOf(']', separatorPos);
				if (end == -1)
					throw new IllegalArgumentException("Missed closed ']'");

				final String index = OStringSerializerHelper.getStringContent(iFieldName.substring(separatorPos + 1, end));
				separatorPos = end + 1;

				if (value == null)
					return null;
				else if (value instanceof ODocument) {
					final List<String> indexParts = OStringSerializerHelper.smartSplit(index, ',');
					if (indexParts.size() == 1)
						// SINGLE VALUE
						value = ((ODocument) value).field(index);
					else {
						// MULTI VALUE
						final Object[] values = new Object[indexParts.size()];
						for (int i = 0; i < indexParts.size(); ++i) {
							values[i] = ((ODocument) value).field(indexParts.get(i));
						}
						value = values;
					}
				} else if (value instanceof Map<?, ?>) {
					final List<String> indexParts = OStringSerializerHelper.smartSplit(index, ',');
					if (indexParts.size() == 1)
						// SINGLE VALUE
						value = ((Map<?, ?>) value).get(index);
					else {
						// MULTI VALUE
						final Object[] values = new Object[indexParts.size()];
						for (int i = 0; i < indexParts.size(); ++i) {
							values[i] = ((Map<?, ?>) value).get(indexParts.get(i));
						}
						value = values;
					}
				} else if (value instanceof List<?>) {
					final List<String> indexParts = OStringSerializerHelper.smartSplit(index, ',');
					final List<String> indexRanges = OStringSerializerHelper.smartSplit(index, '-');
					if (indexParts.size() == 1 && indexRanges.size() == 1)
						// SINGLE VALUE
						value = ((List<?>) value).get(Integer.parseInt(index));
					else {
						final Object[] values;
						if (indexParts.size() > 1) {
							// MULTI VALUES
							values = new Object[indexParts.size()];
							for (int i = 0; i < indexParts.size(); ++i) {
								values[i] = ((List<?>) value).get(Integer.parseInt(indexParts.get(i)));
							}
						} else {
							// MULTI VALUES RANGE
							final int rangeFrom = Integer.parseInt(indexRanges.get(0));
							final int rangeTo = Integer.parseInt(indexRanges.get(1));

							values = new Object[rangeTo - rangeFrom + 1];
							for (int i = rangeFrom; i <= rangeTo; ++i) {
								values[i - rangeFrom] = ((List<?>) value).get(i);
							}
						}
						value = values;
					}
				} else if (value.getClass().isArray()) {
					final List<String> indexParts = OStringSerializerHelper.smartSplit(index, ',');
					final List<String> indexRanges = OStringSerializerHelper.smartSplit(index, '-');
					if (indexParts.size() == 1 && indexRanges.size() == 1)
						// SINGLE VALUE
						value = Array.get(value, Integer.parseInt(index));
					else {
						final Object[] values;
						if (indexParts.size() > 1) {
							// MULTI VALUES
							values = new Object[indexParts.size()];
							for (int i = 0; i < indexParts.size(); ++i) {
								values[i] = Array.get(value, Integer.parseInt(indexParts.get(i)));
							}
						} else {
							// MULTI VALUES RANGE
							final int rangeFrom = Integer.parseInt(indexRanges.get(0));
							final int rangeTo = Integer.parseInt(indexRanges.get(1));

							values = new Object[rangeTo - rangeFrom + 1];
							for (int i = rangeFrom; i <= rangeTo; ++i) {
								values[i - rangeFrom] = Array.get(value, i);
							}
						}
						value = values;
					}
				}
			} else
				value = current._fieldValues.get(fieldName);

		} while (separatorPos < fieldNameLength);

		return (RET) value;
	}

	@SuppressWarnings("unchecked")
	public static void copyFieldValue(final ODocument iCloned, final Entry<String, Object> iEntry) {
		final Object fieldValue = iEntry.getValue();

		if (fieldValue != null)
			// LISTS
			if (fieldValue instanceof ORecordLazyList) {
				iCloned._fieldValues.put(iEntry.getKey(), ((ORecordLazyList) fieldValue).copy(iCloned));

			} else if (fieldValue instanceof ORecordTrackedList) {
				final ORecordTrackedList newList = new ORecordTrackedList(iCloned);
				newList.addAll((ORecordTrackedList) fieldValue);
				iCloned._fieldValues.put(iEntry.getKey(), newList);

			} else if (fieldValue instanceof OTrackedList<?>) {
				final OTrackedList<Object> newList = new OTrackedList<Object>(iCloned);
				newList.addAll((OTrackedList<Object>) fieldValue);
				iCloned._fieldValues.put(iEntry.getKey(), newList);

			} else if (fieldValue instanceof List<?>) {
				iCloned._fieldValues.put(iEntry.getKey(), new ArrayList<Object>((List<Object>) fieldValue));

				// SETS
			} else if (fieldValue instanceof ORecordLazySet) {
				iCloned._fieldValues.put(iEntry.getKey(), ((ORecordLazySet) fieldValue).copy(iCloned));

			} else if (fieldValue instanceof ORecordTrackedSet) {
				final ORecordTrackedSet newList = new ORecordTrackedSet(iCloned);
				newList.addAll((ORecordTrackedSet) fieldValue);
				iCloned._fieldValues.put(iEntry.getKey(), newList);

			} else if (fieldValue instanceof OTrackedSet<?>) {
				final OTrackedSet<Object> newList = new OTrackedSet<Object>(iCloned);
				newList.addAll((OTrackedSet<Object>) fieldValue);
				iCloned._fieldValues.put(iEntry.getKey(), newList);

			} else if (fieldValue instanceof Set<?>) {
				iCloned._fieldValues.put(iEntry.getKey(), new HashSet<Object>((Set<Object>) fieldValue));

				// MAPS
			} else if (fieldValue instanceof ORecordLazyMap) {
				final ORecordLazyMap newMap = new ORecordLazyMap(iCloned, ((ORecordLazyMap) fieldValue).getRecordType());
				newMap.putAll((ORecordLazyMap) fieldValue);
				iCloned._fieldValues.put(iEntry.getKey(), newMap);

			} else if (fieldValue instanceof OTrackedMap) {
				final OTrackedMap<Object> newMap = new OTrackedMap<Object>(iCloned);
				newMap.putAll((OTrackedMap<Object>) fieldValue);
				iCloned._fieldValues.put(iEntry.getKey(), newMap);

			} else if (fieldValue instanceof Map<?, ?>) {
				iCloned._fieldValues.put(iEntry.getKey(), new LinkedHashMap<String, Object>((Map<String, Object>) fieldValue));
			} else
				iCloned._fieldValues.put(iEntry.getKey(), fieldValue);
	}

	public static boolean hasSameContentItem(final Object iCurrent, final Object iOther) {
		if (iCurrent instanceof ODocument) {
			final ODocument current = (ODocument) iCurrent;
			if (iOther instanceof ORID) {
				if (!((ODocument) current).isDirty()) {
					if (!((ODocument) current).getIdentity().equals(iOther))
						return false;
				} else {
					final ODocument otherDoc = (ODocument) current.getDatabase().load((ORID) iOther);
					if (!ODocumentHelper.hasSameContentOf(current, otherDoc))
						return false;
				}
			} else if (!ODocumentHelper.hasSameContentOf(current, (ODocument) iOther))
				return false;
		} else if (!iCurrent.equals(iOther))
			return false;
		return true;
	}

	/**
	 * Makes a deep comparison field by field to check if the passed ODocument instance is identical in the content to the current
	 * one. Instead equals() just checks if the RID are the same.
	 * 
	 * @param iOther
	 *          ODocument instance
	 * @return true if the two document are identical, otherwise false
	 * @see #equals(Object);
	 */
	public static boolean hasSameContentOf(ODocument iCurrent, ODocument iOther) {
		if (iOther == null)
			return false;

		if (!iCurrent.equals(iOther) && iCurrent.getIdentity().isValid())
			return false;

		if (iCurrent.getInternalStatus() == STATUS.NOT_LOADED)
			iCurrent.reload();
		if (iOther.getInternalStatus() == STATUS.NOT_LOADED)
			iOther = (ODocument) iOther.load();

		iCurrent.checkForFields();
		iOther.checkForFields();

		if (iCurrent._fieldValues.size() != iOther._fieldValues.size())
			return false;

		// CHECK FIELD-BY-FIELD
		Object myFieldValue;
		Object otherFieldValue;
		for (Entry<String, Object> f : iCurrent._fieldValues.entrySet()) {
			myFieldValue = f.getValue();
			otherFieldValue = iOther._fieldValues.get(f.getKey());

			// CHECK FOR NULLS
			if (myFieldValue == null) {
				if (otherFieldValue != null)
					return false;
			} else if (otherFieldValue == null)
				return false;

			if (myFieldValue != null && otherFieldValue != null)
				if (myFieldValue instanceof Collection && otherFieldValue instanceof Collection) {
					final Collection<?> myCollection = (Collection<?>) myFieldValue;
					final Collection<?> otherCollection = (Collection<?>) otherFieldValue;

					if (myCollection.size() != otherCollection.size())
						return false;

					Iterator<?> myIterator = myCollection.iterator();
					Iterator<?> otherIterator = otherCollection.iterator();

					while (myIterator.hasNext()) {
						hasSameContentItem(myIterator.next(), otherIterator.next());
					}
				} else if (myFieldValue instanceof Map && otherFieldValue instanceof Map) {
					// CHECK IF THE ORDER IS RESPECTED
					final Map<?, ?> myMap = (Map<?, ?>) myFieldValue;
					final Map<?, ?> otherMap = (Map<?, ?>) otherFieldValue;

					if (myMap.size() != otherMap.size())
						return false;

					for (Entry<?, ?> myEntry : myMap.entrySet()) {
						if (!otherMap.containsKey(myEntry.getKey()))
							return false;

						if (myEntry.getValue() instanceof ODocument) {
							if (!hasSameContentOf((ODocument) myEntry.getValue(), (ODocument) otherMap.get(myEntry.getKey())))
								return false;
						} else if (!myEntry.getValue().equals(otherMap.get(myEntry.getKey())))
							return false;
					}
				} else {
					if (!myFieldValue.equals(otherFieldValue))
						return false;
				}
		}

		return true;
	}
}
