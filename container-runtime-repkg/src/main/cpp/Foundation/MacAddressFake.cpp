//
// Created by 邓维佳 on 2020-03-06.
//

#include <linux/socket.h>
#include <ifaddrs.h>
#include <cstdlib>
#include <VAJni.h>
#include <Substrate/SubstrateHook.h>
#include <fnmatch.h>
#include "MacAddressFake.h"
#include "dlfcn_nougat.h"
#include "Symbol.h"
#include "Log.h"
//#include <linux/netlink.h>
//#include <linux/if_link.h>
//#include <linux/rtnetlink.h>

int (*origin_getifaddrs)(struct ifaddrs **__list_ptr);

int new_getifaddrs(struct ifaddrs **__list_ptr) {
    //VLOGI("call getifaddrs");
    int ret = origin_getifaddrs(__list_ptr);
    if (ret == -1) {
        return ret;
    }
    if (!open_mac_address_fake) {
        return ret;
    }
    //reset hardware data
    int ifCount = 0;
    for (ifaddrs *ifa = *__list_ptr; ifa != nullptr; ifa = ifa->ifa_next) {
        ++ifCount;
    }

    int index = 0;
    for (ifaddrs *ifa = *__list_ptr; nullptr != ifa; ifa = ifa->ifa_next, ++index) {
        // jint flags = ifa->ifa_flags;
        auto *interfaceAddr =
                reinterpret_cast<sockaddr_storage *>(ifa->ifa_addr);

        if (interfaceAddr != nullptr && interfaceAddr->ss_family == AF_PACKET) {
            // Raw Interface.
            auto *sll = reinterpret_cast<sockaddr_ll *>(ifa->ifa_addr);

            bool allZero = true;
            for (int i = 0; i < sll->sll_halen; ++i) {
                if (sll->sll_addr[i] != 0) {
                    allZero = false;
                    break;
                }
            }

            srandom((unsigned int) user_seed);

            if (!allZero) {
                for (int i = sll->sll_halen - 1; i > 0 && i >= sll->sll_halen - 2; i--) {
                    sll->sll_addr[i] = sll->sll_addr[i] ^ (u_char) random();
                }
            }
        }
    }


    return ret;
}


int parse_rtattr_flags(struct rtattr *tb[], int max, struct rtattr *rta,
                       int len, unsigned short flags) {
    unsigned short type;

    memset(tb, 0, sizeof(struct rtattr *) * (max + 1));
    while (RTA_OK(rta, len)) {
        type = rta->rta_type & ~flags;
        if ((type <= max) && (!tb[type]))
            tb[type] = rta;
        rta = RTA_NEXT(rta, len);
    }
    if (len)
        fprintf(stderr, "!!!Deficit %d, rta_len=%d\n", len, rta->rta_len);
    return 0;
}

int parse_rtattr(struct rtattr *tb[], int max, struct rtattr *rta, int len) {
    return parse_rtattr_flags(tb, max, rta, len, 0);
}


int (*origin_print_linkinfo)(const struct sockaddr_nl *who,
                             struct nlmsghdr *n, void *arg);

void modify_sockdaddr_data(const struct sockaddr_nl *who,
                           struct nlmsghdr *n, void *arg) {
    // FILE *fp = (FILE *) arg;
    struct ifinfomsg *ifi = (struct ifinfomsg *) NLMSG_DATA(n);
    struct rtattr *tb[IFLA_MAX + 1];
    int len = n->nlmsg_len;

    if (n->nlmsg_type != RTM_NEWLINK && n->nlmsg_type != RTM_DELLINK)
        return;

    len -= NLMSG_LENGTH(sizeof(*ifi));
    if (len < 0)
        return;

    if (filter.ifindex && ifi->ifi_index != filter.ifindex)
        return;
    if (filter.up && !(ifi->ifi_flags & IFF_UP))
        return;

    parse_rtattr(tb, IFLA_MAX, IFLA_RTA(ifi), len);

    if (filter.label &&
        (!filter.family || filter.family == AF_PACKET) &&
        fnmatch(filter.label, (const char *) RTA_DATA(tb[IFLA_IFNAME]), 0))
        return;

    if (tb[IFLA_GROUP]) {
        int group = *(int *) RTA_DATA(tb[IFLA_GROUP]);
        if (filter.group != -1 && group != filter.group)
            return;
    }

    if (tb[IFLA_MASTER]) {
        int master = *(int *) RTA_DATA(tb[IFLA_MASTER]);
        if (filter.master > 0 && master != filter.master)
            return;
    } else if (filter.master > 0) {
        return;
    }


    if (!filter.family || filter.family == AF_PACKET) {

        if (!tb[IFLA_ADDRESS]) {
            return;
        }

        unsigned char *addr = (unsigned char *) RTA_DATA(tb[IFLA_ADDRESS]);
        int alen = RTA_PAYLOAD(tb[IFLA_ADDRESS]);
        int type = ifi->ifi_type;
        if (type != 1) {
            // 1是以太网
            return;
        }

        srandom((unsigned int) user_seed);
        for (int i = alen - 1; i > 0 && i >= alen - 2; i--) {
            //修改mac地址
            addr[i] = addr[i] ^ (u_char) random();
        }
    }

}

int new_print_linkinfo(const struct sockaddr_nl *who,
                       struct nlmsghdr *n, void *arg) {
    modify_sockdaddr_data(who, n, arg);
    int ret = origin_print_linkinfo(who, n, arg);
    return ret;
}

#define CMD_IP_PATH "/system/bin/ip"

// adb shell ip link
void hook_libipcommand() {
    if (!is_mapped(CMD_IP_PATH)) {
        //不是IP进程，不需要拦截
        return;
    }

    void *handle = fake_dlopen(CMD_IP_PATH, RTLD_NOW);
    if (handle == nullptr) {
        ALOGW("can not open ip bin file: %s", CMD_IP_PATH);
        return;
    }
    void *func_address = fake_dlsym(handle, "print_linkinfo");
    if (func_address != nullptr) {
        MSHookFunction(func_address, (void *) new_print_linkinfo,
                       (void **) &origin_print_linkinfo);
    }

    fake_dlclose(handle);

}