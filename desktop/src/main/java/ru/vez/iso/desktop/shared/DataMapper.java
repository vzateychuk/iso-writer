package ru.vez.iso.desktop.shared;

/**
 * Generic Mapper
 * */
public interface DataMapper<T,R> {

    R map(T t);
}
