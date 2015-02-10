package org.gdg.lille.dao;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Generic DAO for CRUD Operations
 *
 * @param <T> must be an Entity Objectify
 */
public class GenericDao<T> {

    private Class<T> typeParameterClass;

    private GenericDao() {
    }

    public GenericDao(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        ObjectifyService.register(typeParameterClass);
    }

    /**
     * return the entire list of Entity
     */
    public List<T> getEntities() {
        return getQuery().list();
    }

    /**
     *
     */
    public List<T> getEntities(Map<String, Object> keyValues) {

        return getQueryFiltered(getQuery(), keyValues)
                .list();
    }

    public List<T> getEntities(Map<String, Object> keyValues, String sortParam) {

        return getQueryFiltered(getQuery(), keyValues)
                .order(sortParam)
                .list();
    }

    public List<T> getEntities(Map<String, Object> keyValues, String sortParam, int limit) {

        return getQueryFiltered(getQuery(), keyValues)
                .order(sortParam)
                .limit(limit)
                .list();
    }

    /**
     * return the entity filtered by one criteria
     */
    public T getEntity(String column, String value) {
        Map<String, Object> criteria = new HashMap<>(2, 1);//initial capacity = 2, load factor 1
        criteria.put(column, value);

        return getEntity(criteria);
    }

    /**
     * return the entity filtered by a list of criteria
     */
    public final T getEntity(Map<String, Object> keyValues) {

        return getQueryFiltered(getQuery(), keyValues)
                .first()
                .now();
    }

    /**
     * return the entire list of Entity
     */
    public T getEntityById(long id) {
        return ofy().load().type(this.typeParameterClass).id(id).now();
    }

    /**
     * Insert one Entity
     * Synchronous insert to get the ID
     *
     * @param object entity to save
     */
    public void insertEntity(T object) {
        ofy().save().entity(object).now();
    }

    /**
     * Insert Entity
     * Synchronous insert to get the ID
     *
     * @param objects entities to save
     */
    public void insertEntities(List<T> objects) {
        ofy().save().entities(objects).now();
    }

    /**
     * Delete one entity
     *
     * @param id the id Entity to delete
     */
    public void deleteEntity(Long id) {
        ofy().delete().type(this.typeParameterClass).id(id);
    }

    /**
     * Delete one entity
     *
     * @param id the id Entity to delete
     */
    public void deleteEntity(String id) {
        ofy().delete().type(this.typeParameterClass).id(id);
    }

    /**
     * Delete one entity
     *
     * @param entities the entities to delete
     */
    public void deleteEntities(List<T> entities) {
        ofy().delete().entities(entities);
    }

    protected final Query<T> getQueryFiltered(Query<T> query, Map<String, Object> keyValues) {
        if (query == null) {
            query = getQuery();
        }

        for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
            query = query.filter(entry.getKey(), entry.getValue());
        }

        return query;
    }

    protected final Query<T> getQuery() {
        return ofy().load().type(this.typeParameterClass);
    }

}
