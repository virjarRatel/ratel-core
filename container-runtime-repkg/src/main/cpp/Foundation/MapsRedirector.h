#ifndef MAPS_REDIRECTOR_H
#define MAPS_REDIRECTOR_H

int redirect_proc_maps(const char *const pathname, const int flags, const int mode);


extern const char *package_name;

extern const char *odex_file_path;

#endif // MAPS_REDIRECTOR_H