package ru.vez.iso.desktop.shared;

/**
 * Generic Mapper
 * Used to map DTO to Entry
 * */
public interface DataMapper<T,R> {

    R map(T t);
}
