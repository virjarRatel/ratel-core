//
// Created by 邓维佳 on 2019/5/9.
//

#include "SandboxProperties.h"
#include "Log.h"
#include <utility>
#include <map>

std::map<std::string, PropertiesMockItem *> propertiesFakeMap;


PropertiesMockItem *query_mock_properties(const char *properties_key) {

    if (properties_key == nullptr) {
        return nullptr;
    }
    std::string queryKey(properties_key);
    auto it = propertiesFakeMap.find(queryKey);
    if (it == propertiesFakeMap.end()) {
        return nullptr;
    }
    return it->second;
}


void add_mock_properties(const char *properties_key, const char *properties_value) {
    if (properties_key == nullptr) {
        return;
    }

    PropertiesMockItem *existed = query_mock_properties(properties_key);
    if (existed != nullptr) {
        if (existed->properties_key) {
            free(existed->properties_key);
            existed->properties_key = nullptr;
        }
        if (existed->properties_value) {
            free(existed->properties_value);
            existed->properties_value = nullptr;
        }
        propertiesFakeMap.erase(properties_key);
    }

    auto *insert = static_cast<PropertiesMockItem *>(malloc(
            sizeof(PropertiesMockItem)));
    insert->properties_key = strdup(properties_key);
    insert->properties_value = strdup(properties_value);
    if (properties_value != nullptr) {
        insert->value_length = strlen(properties_value);
    } else {
        insert->value_length = -1;
    }

    propertiesFakeMap.insert(make_pair(std::string(properties_key), insert));

}