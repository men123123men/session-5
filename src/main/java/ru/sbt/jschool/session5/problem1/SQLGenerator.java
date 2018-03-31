package ru.sbt.jschool.session5.problem1;

import java.lang.reflect.Field;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLGenerator {
    public <T> String insert(Class<T> clazz) {
        String tableNamme = clazz.getAnnotation(Table.class).name();
        StringJoiner parameterNames = new StringJoiner(", ");
        StringJoiner values         = new StringJoiner(", ");

        String columnName;
        String fildName;
        for(Field f: clazz.getDeclaredFields()){
            if (!(f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(PrimaryKey.class)))
                continue;
            fildName = f.getName();

            if (f.isAnnotationPresent(Column.class)) {
                columnName = f.getAnnotation(Column.class).name();
                if (!columnName.equals(""))
                    fildName = columnName;
            }
            parameterNames.add(fildName.toLowerCase());
            values.add("?");
        }
        return String.format("INSERT INTO %s(%s) VALUES (%s)",tableNamme,parameterNames,values);
    }

    public <T> String update(Class<T> clazz) {
        String tableName = clazz.getAnnotation(Table.class).name();

        StringJoiner primaryKeyFields = new StringJoiner(" AND ");
        StringJoiner simpleFields= new StringJoiner(", ");

        String columnName;
        for(Field f: clazz.getDeclaredFields()){
            if (f.isAnnotationPresent(PrimaryKey.class))
                primaryKeyFields.add(f.getName().toLowerCase()+" = ?");
            if (f.isAnnotationPresent(Column.class)) {
                columnName = f.getAnnotation(Column.class).name();
                if (columnName.equals(""))
                    columnName = f.getName();
                simpleFields.add(columnName.toLowerCase() + " = ?");
            }
        }
        return String.format("UPDATE %s SET %s WHERE %s",tableName,simpleFields,primaryKeyFields);
    }

    public <T> String delete(Class<T> clazz) {
        String tableName = clazz.getAnnotation(Table.class).name();
        String primeryKeys = Stream.of(clazz.getDeclaredFields())
                .filter(f->f.isAnnotationPresent(PrimaryKey.class))
                .map(Field::getName)
                .map(String::toLowerCase)
                .map(s->s.concat(" = ?"))
                .collect(Collectors.joining(" AND "));
        return String.format("DELETE FROM %s WHERE %s",tableName,primeryKeys);
    }

    public <T> String select(Class<T> clazz) {
        String tableName = clazz.getAnnotation(Table.class).name();

        StringJoiner columnFieldsJoiner = new StringJoiner(", ");
        StringJoiner primeryKeyFieldsJoiner = new StringJoiner(" AND ");

        String fieldName;
        String columnName;
        for(Field f:clazz.getDeclaredFields()){
            fieldName = f.getName().toLowerCase();
            if(f.isAnnotationPresent(Column.class)) {
                columnName = f.getAnnotation(Column.class).name();
                if (!columnName.equals(""))
                    fieldName = columnName.toLowerCase();
                columnFieldsJoiner.add(fieldName);
            }
            if(f.isAnnotationPresent(PrimaryKey.class))
                primeryKeyFieldsJoiner.add(fieldName+" = ?");
        }

        return String.format("SELECT %s FROM %s WHERE %s",columnFieldsJoiner,tableName,primeryKeyFieldsJoiner);

    }
}
