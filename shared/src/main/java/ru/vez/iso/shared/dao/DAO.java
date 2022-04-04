package ru.vez.iso.shared.dao;

import java.util.List;

public interface DAO<T> {

    List<T> findAll();
    T findById(Long id);
    T upsert(T t);
    boolean delete(T t);
}
