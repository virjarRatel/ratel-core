//
// Created by 邓维佳 on 2020-03-06.
//

#ifndef RATEL2_MACADDRESSFAKE_H
#define RATEL2_MACADDRESSFAKE_H



struct sockaddr_ll {
    unsigned short sll_family;
    unsigned short sll_protocol;
    int sll_ifindex;
    unsigned short sll_hatype;
    unsigned char sll_pkttype;
    unsigned char sll_halen;
    unsigned char sll_addr[8];
};

struct nlmsghdr
{
    __u32 nlmsg_len;
    __u16 nlmsg_type;
    __u16 nlmsg_flags;
    __u32 nlmsg_seq;
    __u32 nlmsg_pid;
};

#define NLM_F_REQUEST 1
#define NLM_F_MULTI 2
#define NLM_F_ACK 4
#define NLM_F_ECHO 8
#define NLM_F_DUMP_INTR 16
#define NLM_F_DUMP_FILTERED 32
#define NLM_F_ROOT 0x100
#define NLM_F_MATCH 0x200
#define NLM_F_ATOMIC 0x400
#define NLM_F_DUMP (NLM_F_ROOT | NLM_F_MATCH)
#define NLM_F_REPLACE 0x100
#define NLM_F_EXCL 0x200
#define NLM_F_CREATE 0x400
#define NLM_F_APPEND 0x800
#define NLMSG_ALIGNTO 4U
#define NLMSG_ALIGN(len) (((len) + NLMSG_ALIGNTO - 1) & ~(NLMSG_ALIGNTO - 1))
#define NLMSG_HDRLEN ((int) NLMSG_ALIGN(sizeof(struct nlmsghdr)))
#define NLMSG_LENGTH(len) ((len) + NLMSG_HDRLEN)
#define NLMSG_SPACE(len) NLMSG_ALIGN(NLMSG_LENGTH(len))
#define NLMSG_DATA(nlh) ((void *) (((char *) nlh) + NLMSG_LENGTH(0)))
#define NLMSG_NEXT(nlh,len) ((len) -= NLMSG_ALIGN((nlh)->nlmsg_len), (struct nlmsghdr *) (((char *) (nlh)) + NLMSG_ALIGN((nlh)->nlmsg_len)))
#define NLMSG_OK(nlh,len) ((len) >= (int) sizeof(struct nlmsghdr) && (nlh)->nlmsg_len >= sizeof(struct nlmsghdr) && (nlh)->nlmsg_len <= (len))
#define NLMSG_PAYLOAD(nlh,len) ((nlh)->nlmsg_len - NLMSG_SPACE((len)))
#define NLMSG_NOOP 0x1
#define NLMSG_ERROR 0x2
#define NLMSG_DONE 0x3
#define NLMSG_OVERRUN 0x4
#define NLMSG_MIN_TYPE 0x10


struct rtnl_link_stats {
    __u32 rx_packets;
    __u32 tx_packets;
    __u32 rx_bytes;
    __u32 tx_bytes;
    __u32 rx_errors;
    __u32 tx_errors;
    __u32 rx_dropped;
    __u32 tx_dropped;
    __u32 multicast;
    __u32 collisions;
    __u32 rx_length_errors;
    __u32 rx_over_errors;
    __u32 rx_crc_errors;
    __u32 rx_frame_errors;
    __u32 rx_fifo_errors;
    __u32 rx_missed_errors;
    __u32 tx_aborted_errors;
    __u32 tx_carrier_errors;
    __u32 tx_fifo_errors;
    __u32 tx_heartbeat_errors;
    __u32 tx_window_errors;
    __u32 rx_compressed;
    __u32 tx_compressed;
    __u32 rx_nohandler;
};
struct rtnl_link_stats64 {
    __u64 rx_packets;
    __u64 tx_packets;
    __u64 rx_bytes;
    __u64 tx_bytes;
    __u64 rx_errors;
    __u64 tx_errors;
    __u64 rx_dropped;
    __u64 tx_dropped;
    __u64 multicast;
    __u64 collisions;
    __u64 rx_length_errors;
    __u64 rx_over_errors;
    __u64 rx_crc_errors;
    __u64 rx_frame_errors;
    __u64 rx_fifo_errors;
    __u64 rx_missed_errors;
    __u64 tx_aborted_errors;
    __u64 tx_carrier_errors;
    __u64 tx_fifo_errors;
    __u64 tx_heartbeat_errors;
    __u64 tx_window_errors;
    __u64 rx_compressed;
    __u64 tx_compressed;
    __u64 rx_nohandler;
};
struct rtnl_link_ifmap {
    __u64 mem_start;
    __u64 mem_end;
    __u64 base_addr;
    __u16 irq;
    __u8 dma;
    __u8 port;
};
enum {
    IFLA_UNSPEC,
    IFLA_ADDRESS,
    IFLA_BROADCAST,
    IFLA_IFNAME,
    IFLA_MTU,
    IFLA_LINK,
    IFLA_QDISC,
    IFLA_STATS,
    IFLA_COST,
#define IFLA_COST IFLA_COST
    IFLA_PRIORITY,
#define IFLA_PRIORITY IFLA_PRIORITY
    IFLA_MASTER,
#define IFLA_MASTER IFLA_MASTER
    IFLA_WIRELESS,
#define IFLA_WIRELESS IFLA_WIRELESS
    IFLA_PROTINFO,
#define IFLA_PROTINFO IFLA_PROTINFO
    IFLA_TXQLEN,
#define IFLA_TXQLEN IFLA_TXQLEN
    IFLA_MAP,
#define IFLA_MAP IFLA_MAP
    IFLA_WEIGHT,
#define IFLA_WEIGHT IFLA_WEIGHT
    IFLA_OPERSTATE,
    IFLA_LINKMODE,
    IFLA_LINKINFO,
#define IFLA_LINKINFO IFLA_LINKINFO
    IFLA_NET_NS_PID,
    IFLA_IFALIAS,
    IFLA_NUM_VF,
    IFLA_VFINFO_LIST,
    IFLA_STATS64,
    IFLA_VF_PORTS,
    IFLA_PORT_SELF,
    IFLA_AF_SPEC,
    IFLA_GROUP,
    IFLA_NET_NS_FD,
    IFLA_EXT_MASK,
    IFLA_PROMISCUITY,
#define IFLA_PROMISCUITY IFLA_PROMISCUITY
    IFLA_NUM_TX_QUEUES,
    IFLA_NUM_RX_QUEUES,
    IFLA_CARRIER,
    IFLA_PHYS_PORT_ID,
    IFLA_CARRIER_CHANGES,
    IFLA_PHYS_SWITCH_ID,
    IFLA_LINK_NETNSID,
    IFLA_PHYS_PORT_NAME,
    IFLA_PROTO_DOWN,
    IFLA_GSO_MAX_SEGS,
    IFLA_GSO_MAX_SIZE,
    IFLA_PAD,
    IFLA_XDP,
    IFLA_EVENT,
    IFLA_NEW_NETNSID,
    IFLA_IF_NETNSID,
    IFLA_CARRIER_UP_COUNT,
    IFLA_CARRIER_DOWN_COUNT,
    IFLA_NEW_IFINDEX,
    __IFLA_MAX
};
#define IFLA_MAX (__IFLA_MAX - 1)
#define IFLA_RTA(r) ((struct rtattr *) (((char *) (r)) + NLMSG_ALIGN(sizeof(struct ifinfomsg))))
#define IFLA_PAYLOAD(n) NLMSG_PAYLOAD(n, sizeof(struct ifinfomsg))


struct ifinfomsg
{
    unsigned char ifi_family;
    unsigned char __ifi_pad;
    unsigned short ifi_type;
    int ifi_index;
    unsigned ifi_flags;
    unsigned ifi_change;
};
#define RTNL_FAMILY_IPMR 128
#define RTNL_FAMILY_IP6MR 129
#define RTNL_FAMILY_MAX 129
enum {
    RTM_BASE = 16,
#define RTM_BASE RTM_BASE
    RTM_NEWLINK = 16,
#define RTM_NEWLINK RTM_NEWLINK
    RTM_DELLINK,
#define RTM_DELLINK RTM_DELLINK
    RTM_GETLINK,
#define RTM_GETLINK RTM_GETLINK
    RTM_SETLINK,
#define RTM_SETLINK RTM_SETLINK
    RTM_NEWADDR = 20,
#define RTM_NEWADDR RTM_NEWADDR
    RTM_DELADDR,
#define RTM_DELADDR RTM_DELADDR
    RTM_GETADDR,
#define RTM_GETADDR RTM_GETADDR
    RTM_NEWROUTE = 24,
#define RTM_NEWROUTE RTM_NEWROUTE
    RTM_DELROUTE,
#define RTM_DELROUTE RTM_DELROUTE
    RTM_GETROUTE,
#define RTM_GETROUTE RTM_GETROUTE
    RTM_NEWNEIGH = 28,
#define RTM_NEWNEIGH RTM_NEWNEIGH
    RTM_DELNEIGH,
#define RTM_DELNEIGH RTM_DELNEIGH
    RTM_GETNEIGH,
#define RTM_GETNEIGH RTM_GETNEIGH
    RTM_NEWRULE = 32,
#define RTM_NEWRULE RTM_NEWRULE
    RTM_DELRULE,
#define RTM_DELRULE RTM_DELRULE
    RTM_GETRULE,
#define RTM_GETRULE RTM_GETRULE
    RTM_NEWQDISC = 36,
#define RTM_NEWQDISC RTM_NEWQDISC
    RTM_DELQDISC,
#define RTM_DELQDISC RTM_DELQDISC
    RTM_GETQDISC,
#define RTM_GETQDISC RTM_GETQDISC
    RTM_NEWTCLASS = 40,
#define RTM_NEWTCLASS RTM_NEWTCLASS
    RTM_DELTCLASS,
#define RTM_DELTCLASS RTM_DELTCLASS
    RTM_GETTCLASS,
#define RTM_GETTCLASS RTM_GETTCLASS
    RTM_NEWTFILTER = 44,
#define RTM_NEWTFILTER RTM_NEWTFILTER
    RTM_DELTFILTER,
#define RTM_DELTFILTER RTM_DELTFILTER
    RTM_GETTFILTER,
#define RTM_GETTFILTER RTM_GETTFILTER
    RTM_NEWACTION = 48,
#define RTM_NEWACTION RTM_NEWACTION
    RTM_DELACTION,
#define RTM_DELACTION RTM_DELACTION
    RTM_GETACTION,
#define RTM_GETACTION RTM_GETACTION
    RTM_NEWPREFIX = 52,
#define RTM_NEWPREFIX RTM_NEWPREFIX
    RTM_GETMULTICAST = 58,
#define RTM_GETMULTICAST RTM_GETMULTICAST
    RTM_GETANYCAST = 62,
#define RTM_GETANYCAST RTM_GETANYCAST
    RTM_NEWNEIGHTBL = 64,
#define RTM_NEWNEIGHTBL RTM_NEWNEIGHTBL
    RTM_GETNEIGHTBL = 66,
#define RTM_GETNEIGHTBL RTM_GETNEIGHTBL
    RTM_SETNEIGHTBL,
#define RTM_SETNEIGHTBL RTM_SETNEIGHTBL
    RTM_NEWNDUSEROPT = 68,
#define RTM_NEWNDUSEROPT RTM_NEWNDUSEROPT
    RTM_NEWADDRLABEL = 72,
#define RTM_NEWADDRLABEL RTM_NEWADDRLABEL
    RTM_DELADDRLABEL,
#define RTM_DELADDRLABEL RTM_DELADDRLABEL
    RTM_GETADDRLABEL,
#define RTM_GETADDRLABEL RTM_GETADDRLABEL
    RTM_GETDCB = 78,
#define RTM_GETDCB RTM_GETDCB
    RTM_SETDCB,
#define RTM_SETDCB RTM_SETDCB
    RTM_NEWNETCONF = 80,
#define RTM_NEWNETCONF RTM_NEWNETCONF
    RTM_DELNETCONF,
#define RTM_DELNETCONF RTM_DELNETCONF
    RTM_GETNETCONF = 82,
#define RTM_GETNETCONF RTM_GETNETCONF
    RTM_NEWMDB = 84,
#define RTM_NEWMDB RTM_NEWMDB
    RTM_DELMDB = 85,
#define RTM_DELMDB RTM_DELMDB
    RTM_GETMDB = 86,
#define RTM_GETMDB RTM_GETMDB
    RTM_NEWNSID = 88,
#define RTM_NEWNSID RTM_NEWNSID
    RTM_DELNSID = 89,
#define RTM_DELNSID RTM_DELNSID
    RTM_GETNSID = 90,
#define RTM_GETNSID RTM_GETNSID
    RTM_NEWSTATS = 92,
#define RTM_NEWSTATS RTM_NEWSTATS
    RTM_GETSTATS = 94,
#define RTM_GETSTATS RTM_GETSTATS
    RTM_NEWCACHEREPORT = 96,
#define RTM_NEWCACHEREPORT RTM_NEWCACHEREPORT
    __RTM_MAX,
#define RTM_MAX (((__RTM_MAX + 3) & ~3) - 1)
};


struct rtattr {
    unsigned short rta_len;
    unsigned short rta_type;
};

typedef int (*rtnl_filter_t)(const struct sockaddr_nl *,
                             struct nlmsghdr *n, void *);


struct rtnl_dump_filter_arg {
    rtnl_filter_t filter;
    void *arg1;
    __u16 nc_flags;
};


struct nlmsg_list {
    struct nlmsg_list *next;
    struct nlmsghdr h;
};

struct nlmsg_chain {
    struct nlmsg_list *head;
    struct nlmsg_list *tail;
};

#define RTA_ALIGNTO 4U
#define RTA_ALIGN(len) (((len) + RTA_ALIGNTO - 1) & ~(RTA_ALIGNTO - 1))
#define RTA_OK(rta,len) ((len) >= (int) sizeof(struct rtattr) && (rta)->rta_len >= sizeof(struct rtattr) && (rta)->rta_len <= (len))
#define RTA_NEXT(rta,attrlen) ((attrlen) -= RTA_ALIGN((rta)->rta_len), (struct rtattr *) (((char *) (rta)) + RTA_ALIGN((rta)->rta_len)))
#define RTA_LENGTH(len) (RTA_ALIGN(sizeof(struct rtattr)) + (len))
#define RTA_SPACE(len) RTA_ALIGN(RTA_LENGTH(len))
#define RTA_DATA(rta) ((void *) (((char *) (rta)) + RTA_LENGTH(0)))
#define RTA_PAYLOAD(rta) ((int) ((rta)->rta_len) - RTA_LENGTH(0))


extern int (*origin_getifaddrs)(struct ifaddrs **__list_ptr);

int new_getifaddrs(struct ifaddrs **__list_ptr);

void hook_libipcommand();

typedef struct
{
    __u16 flags;
    __u16 bytelen;
    __s16 bitlen;
    /* These next two fields match rtvia */
    __u16 family;
    __u32 data[8];
} inet_prefix;

static struct
{
    int ifindex;
    int family;
    int oneline;
    int showqueue;
    inet_prefix pfx;
    int scope, scopemask;
    int flags, flagmask;
    int up;
    char *label;
    int flushed;
    char *flushb;
    int flushp;
    int flushe;
    int group;
    int master;
    char *kind;
} filter;


enum net_device_flags {
    IFF_UP				= 1<<0,  /* sysfs */
    IFF_BROADCAST			= 1<<1,  /* __volatile__ */
    IFF_DEBUG			= 1<<2,  /* sysfs */
    IFF_LOOPBACK			= 1<<3,  /* __volatile__ */
    IFF_POINTOPOINT			= 1<<4,  /* __volatile__ */
    IFF_NOTRAILERS			= 1<<5,  /* sysfs */
    IFF_RUNNING			= 1<<6,  /* __volatile__ */
    IFF_NOARP			= 1<<7,  /* sysfs */
    IFF_PROMISC			= 1<<8,  /* sysfs */
    IFF_ALLMULTI			= 1<<9,  /* sysfs */
    IFF_MASTER			= 1<<10, /* __volatile__ */
    IFF_SLAVE			= 1<<11, /* __volatile__ */
    IFF_MULTICAST			= 1<<12, /* sysfs */
    IFF_PORTSEL			= 1<<13, /* sysfs */
    IFF_AUTOMEDIA			= 1<<14, /* sysfs */
    IFF_DYNAMIC			= 1<<15, /* sysfs */
    IFF_LOWER_UP			= 1<<16, /* __volatile__ */
    IFF_DORMANT			= 1<<17, /* __volatile__ */
    IFF_ECHO			= 1<<18, /* __volatile__ */
};

#define IFF_UP				IFF_UP
#define IFF_BROADCAST			IFF_BROADCAST
#define IFF_DEBUG			IFF_DEBUG
#define IFF_LOOPBACK			IFF_LOOPBACK
#define IFF_POINTOPOINT			IFF_POINTOPOINT
#define IFF_NOTRAILERS			IFF_NOTRAILERS
#define IFF_RUNNING			IFF_RUNNING
#define IFF_NOARP			IFF_NOARP
#define IFF_PROMISC			IFF_PROMISC
#define IFF_ALLMULTI			IFF_ALLMULTI
#define IFF_MASTER			IFF_MASTER
#define IFF_SLAVE			IFF_SLAVE
#define IFF_MULTICAST			IFF_MULTICAST
#define IFF_PORTSEL			IFF_PORTSEL
#define IFF_AUTOMEDIA			IFF_AUTOMEDIA
#define IFF_DYNAMIC			IFF_DYNAMIC
#define IFF_LOWER_UP			IFF_LOWER_UP
#define IFF_DORMANT			IFF_DORMANT
#define IFF_ECHO			IFF_ECHO

#define IFF_VOLATILE	(IFF_LOOPBACK|IFF_POINTOPOINT|IFF_BROADCAST|IFF_ECHO|\
		IFF_MASTER|IFF_SLAVE|IFF_RUNNING|IFF_LOWER_UP|IFF_DORMANT)

#define IF_GET_IFACE	0x0001		/* for querying only */
#define IF_GET_PROTO	0x0002

#define SPRINT_BSIZE 64
#define SPRINT_BUF(x)	char x[SPRINT_BSIZE]

#endif //RATEL2_MACADDRESSFAKE_H
