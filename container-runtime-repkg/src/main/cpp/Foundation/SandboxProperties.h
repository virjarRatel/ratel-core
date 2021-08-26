//
// Created by 邓维佳 on 2019/5/9.
//
#include <string>
#include <cerrno>

#ifndef MYAPPLICATION_SANDBOXPROPERTIES_H
#define MYAPPLICATION_SANDBOXPROPERTIES_H
#define KEY_MAX 256

typedef struct PropertiesMockItem {
    char *properties_key;
    char *properties_value;
    int value_length;
} PropertiesMockItem;

PropertiesMockItem *query_mock_properties(const char *properties_key);

void add_mock_properties(const char *properties_key, const char *properties_value);

#endif //MYAPPLICATION_SANDBOXPROPERTIES_H
