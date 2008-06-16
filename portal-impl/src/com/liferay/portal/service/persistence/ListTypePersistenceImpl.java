/**
 * Copyright (c) 2000-2008 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.service.persistence;

import com.liferay.portal.NoSuchListTypeException;
import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.dao.DynamicQuery;
import com.liferay.portal.kernel.dao.DynamicQueryInitializer;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringMaker;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.ListType;
import com.liferay.portal.model.ModelListener;
import com.liferay.portal.model.impl.ListTypeImpl;
import com.liferay.portal.model.impl.ListTypeModelImpl;
import com.liferay.portal.spring.hibernate.FinderCache;
import com.liferay.portal.spring.hibernate.HibernateUtil;
import com.liferay.portal.util.PropsUtil;

import com.liferay.util.dao.hibernate.QueryPos;
import com.liferay.util.dao.hibernate.QueryUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Query;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <a href="ListTypePersistenceImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class ListTypePersistenceImpl extends BasePersistence
	implements ListTypePersistence {
	public ListType create(int listTypeId) {
		ListType listType = new ListTypeImpl();

		listType.setNew(true);
		listType.setPrimaryKey(listTypeId);

		return listType;
	}

	public ListType remove(int listTypeId)
		throws NoSuchListTypeException, SystemException {
		Session session = null;

		try {
			session = openSession();

			ListType listType = (ListType)session.get(ListTypeImpl.class,
					new Integer(listTypeId));

			if (listType == null) {
				if (_log.isWarnEnabled()) {
					_log.warn("No ListType exists with the primary key " +
						listTypeId);
				}

				throw new NoSuchListTypeException(
					"No ListType exists with the primary key " + listTypeId);
			}

			return remove(listType);
		}
		catch (NoSuchListTypeException nsee) {
			throw nsee;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public ListType remove(ListType listType) throws SystemException {
		if (_listeners.length > 0) {
			for (ModelListener listener : _listeners) {
				listener.onBeforeRemove(listType);
			}
		}

		listType = removeImpl(listType);

		if (_listeners.length > 0) {
			for (ModelListener listener : _listeners) {
				listener.onAfterRemove(listType);
			}
		}

		return listType;
	}

	protected ListType removeImpl(ListType listType) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			session.delete(listType);

			session.flush();

			return listType;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);

			FinderCache.clearCache(ListType.class.getName());
		}
	}

	/**
	 * @deprecated Use <code>update(ListType listType, boolean merge)</code>.
	 */
	public ListType update(ListType listType) throws SystemException {
		if (_log.isWarnEnabled()) {
			_log.warn(
				"Using the deprecated update(ListType listType) method. Use update(ListType listType, boolean merge) instead.");
		}

		return update(listType, false);
	}

	/**
	 * Add, update, or merge, the entity. This method also calls the model
	 * listeners to trigger the proper events associated with adding, deleting,
	 * or updating an entity.
	 *
	 * @param        listType the entity to add, update, or merge
	 * @param        merge boolean value for whether to merge the entity. The
	 *                default value is false. Setting merge to true is more
	 *                expensive and should only be true when listType is
	 *                transient. See LEP-5473 for a detailed discussion of this
	 *                method.
	 * @return        true if the portlet can be displayed via Ajax
	 */
	public ListType update(ListType listType, boolean merge)
		throws SystemException {
		boolean isNew = listType.isNew();

		if (_listeners.length > 0) {
			for (ModelListener listener : _listeners) {
				if (isNew) {
					listener.onBeforeCreate(listType);
				}
				else {
					listener.onBeforeUpdate(listType);
				}
			}
		}

		listType = updateImpl(listType, merge);

		if (_listeners.length > 0) {
			for (ModelListener listener : _listeners) {
				if (isNew) {
					listener.onAfterCreate(listType);
				}
				else {
					listener.onAfterUpdate(listType);
				}
			}
		}

		return listType;
	}

	public ListType updateImpl(com.liferay.portal.model.ListType listType,
		boolean merge) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			if (merge) {
				session.merge(listType);
			}
			else {
				if (listType.isNew()) {
					session.save(listType);
				}
			}

			session.flush();

			listType.setNew(false);

			return listType;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);

			FinderCache.clearCache(ListType.class.getName());
		}
	}

	public ListType findByPrimaryKey(int listTypeId)
		throws NoSuchListTypeException, SystemException {
		ListType listType = fetchByPrimaryKey(listTypeId);

		if (listType == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("No ListType exists with the primary key " +
					listTypeId);
			}

			throw new NoSuchListTypeException(
				"No ListType exists with the primary key " + listTypeId);
		}

		return listType;
	}

	public ListType fetchByPrimaryKey(int listTypeId) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			return (ListType)session.get(ListTypeImpl.class,
				new Integer(listTypeId));
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List<ListType> findByType(String type) throws SystemException {
		boolean finderClassNameCacheEnabled = ListTypeModelImpl.CACHE_ENABLED;
		String finderClassName = ListType.class.getName();
		String finderMethodName = "findByType";
		String[] finderParams = new String[] { String.class.getName() };
		Object[] finderArgs = new Object[] { type };

		Object result = null;

		if (finderClassNameCacheEnabled) {
			result = FinderCache.getResult(finderClassName, finderMethodName,
					finderParams, finderArgs, getSessionFactory());
		}

		if (result == null) {
			Session session = null;

			try {
				session = openSession();

				StringMaker query = new StringMaker();

				query.append("FROM com.liferay.portal.model.ListType WHERE ");

				if (type == null) {
					query.append("type_ IS NULL");
				}
				else {
					query.append("type_ = ?");
				}

				query.append(" ");

				query.append("ORDER BY ");

				query.append("name ASC");

				Query q = session.createQuery(query.toString());

				QueryPos qPos = QueryPos.getInstance(q);

				if (type != null) {
					qPos.add(type);
				}

				List<ListType> list = q.list();

				FinderCache.putResult(finderClassNameCacheEnabled,
					finderClassName, finderMethodName, finderParams,
					finderArgs, list);

				return list;
			}
			catch (Exception e) {
				throw HibernateUtil.processException(e);
			}
			finally {
				closeSession(session);
			}
		}
		else {
			return (List<ListType>)result;
		}
	}

	public List<ListType> findByType(String type, int start, int end)
		throws SystemException {
		return findByType(type, start, end, null);
	}

	public List<ListType> findByType(String type, int start, int end,
		OrderByComparator obc) throws SystemException {
		boolean finderClassNameCacheEnabled = ListTypeModelImpl.CACHE_ENABLED;
		String finderClassName = ListType.class.getName();
		String finderMethodName = "findByType";
		String[] finderParams = new String[] {
				String.class.getName(),
				
				"java.lang.Integer", "java.lang.Integer",
				"com.liferay.portal.kernel.util.OrderByComparator"
			};
		Object[] finderArgs = new Object[] {
				type,
				
				String.valueOf(start), String.valueOf(end), String.valueOf(obc)
			};

		Object result = null;

		if (finderClassNameCacheEnabled) {
			result = FinderCache.getResult(finderClassName, finderMethodName,
					finderParams, finderArgs, getSessionFactory());
		}

		if (result == null) {
			Session session = null;

			try {
				session = openSession();

				StringMaker query = new StringMaker();

				query.append("FROM com.liferay.portal.model.ListType WHERE ");

				if (type == null) {
					query.append("type_ IS NULL");
				}
				else {
					query.append("type_ = ?");
				}

				query.append(" ");

				if (obc != null) {
					query.append("ORDER BY ");
					query.append(obc.getOrderBy());
				}

				else {
					query.append("ORDER BY ");

					query.append("name ASC");
				}

				Query q = session.createQuery(query.toString());

				QueryPos qPos = QueryPos.getInstance(q);

				if (type != null) {
					qPos.add(type);
				}

				List<ListType> list = (List<ListType>)QueryUtil.list(q,
						getDialect(), start, end);

				FinderCache.putResult(finderClassNameCacheEnabled,
					finderClassName, finderMethodName, finderParams,
					finderArgs, list);

				return list;
			}
			catch (Exception e) {
				throw HibernateUtil.processException(e);
			}
			finally {
				closeSession(session);
			}
		}
		else {
			return (List<ListType>)result;
		}
	}

	public ListType findByType_First(String type, OrderByComparator obc)
		throws NoSuchListTypeException, SystemException {
		List<ListType> list = findByType(type, 0, 1, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();

			msg.append("No ListType exists with the key {");

			msg.append("type=" + type);

			msg.append(StringPool.CLOSE_CURLY_BRACE);

			throw new NoSuchListTypeException(msg.toString());
		}
		else {
			return list.get(0);
		}
	}

	public ListType findByType_Last(String type, OrderByComparator obc)
		throws NoSuchListTypeException, SystemException {
		int count = countByType(type);

		List<ListType> list = findByType(type, count - 1, count, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();

			msg.append("No ListType exists with the key {");

			msg.append("type=" + type);

			msg.append(StringPool.CLOSE_CURLY_BRACE);

			throw new NoSuchListTypeException(msg.toString());
		}
		else {
			return list.get(0);
		}
	}

	public ListType[] findByType_PrevAndNext(int listTypeId, String type,
		OrderByComparator obc) throws NoSuchListTypeException, SystemException {
		ListType listType = findByPrimaryKey(listTypeId);

		int count = countByType(type);

		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();

			query.append("FROM com.liferay.portal.model.ListType WHERE ");

			if (type == null) {
				query.append("type_ IS NULL");
			}
			else {
				query.append("type_ = ?");
			}

			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}

			else {
				query.append("ORDER BY ");

				query.append("name ASC");
			}

			Query q = session.createQuery(query.toString());

			QueryPos qPos = QueryPos.getInstance(q);

			if (type != null) {
				qPos.add(type);
			}

			Object[] objArray = QueryUtil.getPrevAndNext(q, count, obc, listType);

			ListType[] array = new ListTypeImpl[3];

			array[0] = (ListType)objArray[0];
			array[1] = (ListType)objArray[1];
			array[2] = (ListType)objArray[2];

			return array;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List<ListType> findWithDynamicQuery(
		DynamicQueryInitializer queryInitializer) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			DynamicQuery query = queryInitializer.initialize(session);

			return query.list();
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List<ListType> findWithDynamicQuery(
		DynamicQueryInitializer queryInitializer, int start, int end)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			DynamicQuery query = queryInitializer.initialize(session);

			query.setLimit(start, end);

			return query.list();
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List<ListType> findAll() throws SystemException {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	public List<ListType> findAll(int start, int end) throws SystemException {
		return findAll(start, end, null);
	}

	public List<ListType> findAll(int start, int end, OrderByComparator obc)
		throws SystemException {
		boolean finderClassNameCacheEnabled = ListTypeModelImpl.CACHE_ENABLED;
		String finderClassName = ListType.class.getName();
		String finderMethodName = "findAll";
		String[] finderParams = new String[] {
				"java.lang.Integer", "java.lang.Integer",
				"com.liferay.portal.kernel.util.OrderByComparator"
			};
		Object[] finderArgs = new Object[] {
				String.valueOf(start), String.valueOf(end), String.valueOf(obc)
			};

		Object result = null;

		if (finderClassNameCacheEnabled) {
			result = FinderCache.getResult(finderClassName, finderMethodName,
					finderParams, finderArgs, getSessionFactory());
		}

		if (result == null) {
			Session session = null;

			try {
				session = openSession();

				StringMaker query = new StringMaker();

				query.append("FROM com.liferay.portal.model.ListType ");

				if (obc != null) {
					query.append("ORDER BY ");
					query.append(obc.getOrderBy());
				}

				else {
					query.append("ORDER BY ");

					query.append("name ASC");
				}

				Query q = session.createQuery(query.toString());

				List<ListType> list = (List<ListType>)QueryUtil.list(q,
						getDialect(), start, end);

				if (obc == null) {
					Collections.sort(list);
				}

				FinderCache.putResult(finderClassNameCacheEnabled,
					finderClassName, finderMethodName, finderParams,
					finderArgs, list);

				return list;
			}
			catch (Exception e) {
				throw HibernateUtil.processException(e);
			}
			finally {
				closeSession(session);
			}
		}
		else {
			return (List<ListType>)result;
		}
	}

	public void removeByType(String type) throws SystemException {
		for (ListType listType : findByType(type)) {
			remove(listType);
		}
	}

	public void removeAll() throws SystemException {
		for (ListType listType : findAll()) {
			remove(listType);
		}
	}

	public int countByType(String type) throws SystemException {
		boolean finderClassNameCacheEnabled = ListTypeModelImpl.CACHE_ENABLED;
		String finderClassName = ListType.class.getName();
		String finderMethodName = "countByType";
		String[] finderParams = new String[] { String.class.getName() };
		Object[] finderArgs = new Object[] { type };

		Object result = null;

		if (finderClassNameCacheEnabled) {
			result = FinderCache.getResult(finderClassName, finderMethodName,
					finderParams, finderArgs, getSessionFactory());
		}

		if (result == null) {
			Session session = null;

			try {
				session = openSession();

				StringMaker query = new StringMaker();

				query.append("SELECT COUNT(*) ");
				query.append("FROM com.liferay.portal.model.ListType WHERE ");

				if (type == null) {
					query.append("type_ IS NULL");
				}
				else {
					query.append("type_ = ?");
				}

				query.append(" ");

				Query q = session.createQuery(query.toString());

				QueryPos qPos = QueryPos.getInstance(q);

				if (type != null) {
					qPos.add(type);
				}

				Long count = null;

				Iterator<Long> itr = q.list().iterator();

				if (itr.hasNext()) {
					count = itr.next();
				}

				if (count == null) {
					count = new Long(0);
				}

				FinderCache.putResult(finderClassNameCacheEnabled,
					finderClassName, finderMethodName, finderParams,
					finderArgs, count);

				return count.intValue();
			}
			catch (Exception e) {
				throw HibernateUtil.processException(e);
			}
			finally {
				closeSession(session);
			}
		}
		else {
			return ((Long)result).intValue();
		}
	}

	public int countAll() throws SystemException {
		boolean finderClassNameCacheEnabled = ListTypeModelImpl.CACHE_ENABLED;
		String finderClassName = ListType.class.getName();
		String finderMethodName = "countAll";
		String[] finderParams = new String[] {  };
		Object[] finderArgs = new Object[] {  };

		Object result = null;

		if (finderClassNameCacheEnabled) {
			result = FinderCache.getResult(finderClassName, finderMethodName,
					finderParams, finderArgs, getSessionFactory());
		}

		if (result == null) {
			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(
						"SELECT COUNT(*) FROM com.liferay.portal.model.ListType");

				Long count = null;

				Iterator<Long> itr = q.list().iterator();

				if (itr.hasNext()) {
					count = itr.next();
				}

				if (count == null) {
					count = new Long(0);
				}

				FinderCache.putResult(finderClassNameCacheEnabled,
					finderClassName, finderMethodName, finderParams,
					finderArgs, count);

				return count.intValue();
			}
			catch (Exception e) {
				throw HibernateUtil.processException(e);
			}
			finally {
				closeSession(session);
			}
		}
		else {
			return ((Long)result).intValue();
		}
	}

	public void registerListener(ModelListener listener) {
		List<ModelListener> listeners = ListUtil.fromArray(_listeners);

		listeners.add(listener);

		_listeners = listeners.toArray(new ModelListener[listeners.size()]);
	}

	public void unregisterListener(ModelListener listener) {
		List<ModelListener> listeners = ListUtil.fromArray(_listeners);

		listeners.remove(listener);

		_listeners = listeners.toArray(new ModelListener[listeners.size()]);
	}

	protected void initDao() {
		String[] listenerClassNames = StringUtil.split(GetterUtil.getString(
					PropsUtil.get(
						"value.object.listener.com.liferay.portal.model.ListType")));

		if (listenerClassNames.length > 0) {
			try {
				List<ModelListener> listeners = new ArrayList<ModelListener>();

				for (String listenerClassName : listenerClassNames) {
					listeners.add((ModelListener)Class.forName(
							listenerClassName).newInstance());
				}

				_listeners = listeners.toArray(new ModelListener[listeners.size()]);
			}
			catch (Exception e) {
				_log.error(e);
			}
		}
	}

	private static Log _log = LogFactory.getLog(ListTypePersistenceImpl.class);
	private ModelListener[] _listeners = new ModelListener[0];
}