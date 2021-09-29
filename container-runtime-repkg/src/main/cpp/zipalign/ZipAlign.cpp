/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Zip alignment tool
 */
#include "ZipFile.h"

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

using namespace android;

/*
 * Show program usage.
 */
void usage(void)
{
    fprintf(stderr, "Zip alignment utility\n");
    fprintf(stderr, "Copyright (C) 2009 The Android Open Source Project\n\n");
    fprintf(stderr,
            "Usage: zipalign [-f] [-p] [-v] [-z] <align> infile.zip outfile.zip\n"
            "       zipalign -c [-p] [-v] <align> infile.zip\n\n" );
    fprintf(stderr,
            "  <align>: alignment in bytes, e.g. '4' provides 32-bit alignment\n");
    fprintf(stderr, "  -c: check alignment only (does not modify file)\n");
    fprintf(stderr, "  -f: overwrite existing outfile.zip\n");
    fprintf(stderr, "  -p: memory page alignment for stored shared object files\n");
    fprintf(stderr, "  -v: verbose output\n");
    fprintf(stderr, "  -z: recompress using Zopfli\n");
}

static int getAlignment(bool pageAlignSharedLibs, int defaultAlignment,
                        ZipEntry* pEntry) {

    static const int kPageAlignment = 4096;

    if (!pageAlignSharedLibs) {
        return defaultAlignment;
    }

    const char* ext = strrchr(pEntry->getFileName(), '.');
    if (ext && strcmp(ext, ".so") == 0) {
        return kPageAlignment;
    }

    return defaultAlignment;
}

/*
 * Copy all entries from "pZin" to "pZout", aligning as needed.
 */
static int copyAndAlign(ZipFile* pZin, ZipFile* pZout, int alignment, bool zopfli,
                        bool pageAlignSharedLibs)
{
    int numEntries = pZin->getNumEntries();
    ZipEntry* pEntry;
    int bias = 0;
    status_t status;

    for (int i = 0; i < numEntries; i++) {
        ZipEntry* pNewEntry;
        int padding = 0;

        pEntry = pZin->getEntryByIndex(i);
        if (pEntry == NULL) {
            fprintf(stderr, "ERROR: unable to retrieve entry %d\n", i);
            return 1;
        }

        if (pEntry->isCompressed()) {
            /* copy the entry without padding */
            //printf("--- %s: orig at %ld len=%ld (compressed)\n",
            //    pEntry->getFileName(), (long) pEntry->getFileOffset(),
            //    (long) pEntry->getUncompressedLen());

            if (zopfli) {
                status = pZout->addRecompress(pZin, pEntry, &pNewEntry);
                bias += pNewEntry->getCompressedLen() - pEntry->getCompressedLen();
            } else {
                status = pZout->add(pZin, pEntry, padding, &pNewEntry);
            }
        } else {
            const int alignTo = getAlignment(pageAlignSharedLibs, alignment, pEntry);

            /*
             * Copy the entry, adjusting as required.  We assume that the
             * file position in the new file will be equal to the file
             * position in the original.
             */
            long newOffset = pEntry->getFileOffset() + bias;
            padding = (alignTo - (newOffset % alignTo)) % alignTo;

            //printf("--- %s: orig at %ld(+%d) len=%ld, adding pad=%d\n",
            //    pEntry->getFileName(), (long) pEntry->getFileOffset(),
            //    bias, (long) pEntry->getUncompressedLen(), padding);
            status = pZout->add(pZin, pEntry, padding, &pNewEntry);
        }

        if (status != NO_ERROR)
            return 1;
        bias += padding;
        //printf(" added '%s' at %ld (pad=%d)\n",
        //    pNewEntry->getFileName(), (long) pNewEntry->getFileOffset(),
        //    padding);
    }

    return 0;
}

/*
 * Process a file.  We open the input and output files, failing if the
 * output file exists and "force" wasn't specified.
 */
static int process(const char* inFileName, const char* outFileName,
                   int alignment, bool force, bool zopfli, bool pageAlignSharedLibs)
{
    ZipFile zin, zout;

    //printf("PROCESS: align=%d in='%s' out='%s' force=%d\n",
    //    alignment, inFileName, outFileName, force);

    /* this mode isn't supported -- do a trivial check */
    if (strcmp(inFileName, outFileName) == 0) {
        fprintf(stderr, "Input and output can't be same file\n");
        return 1;
    }

    /* don't overwrite existing unless given permission */
    if (!force && access(outFileName, F_OK) == 0) {
        fprintf(stderr, "Output file '%s' exists\n", outFileName);
        return 1;
    }

    if (zin.open(inFileName, ZipFile::kOpenReadOnly) != NO_ERROR) {
        fprintf(stderr, "Unable to open '%s' as zip archive\n", inFileName);
        return 1;
    }
    if (zout.open(outFileName,
                  ZipFile::kOpenReadWrite|ZipFile::kOpenCreate|ZipFile::kOpenTruncate)
        != NO_ERROR)
    {
        fprintf(stderr, "Unable to open '%s' as zip archive\n", outFileName);
        return 1;
    }

    int result = copyAndAlign(&zin, &zout, alignment, zopfli, pageAlignSharedLibs);
    if (result != 0) {
        printf("zipalign: failed rewriting '%s' to '%s'\n",
               inFileName, outFileName);
    }
    return result;
}

/*
 * Verify the alignment of a zip archive.
 */
static int verify(const char* fileName, int alignment, bool verbose,
                  bool pageAlignSharedLibs)
{
    ZipFile zipFile;
    bool foundBad = false;

    if (verbose)
        printf("Verifying alignment of %s (%d)...\n", fileName, alignment);

    if (zipFile.open(fileName, ZipFile::kOpenReadOnly) != NO_ERROR) {
        fprintf(stderr, "Unable to open '%s' for verification\n", fileName);
        return 1;
    }

    int numEntries = zipFile.getNumEntries();
    ZipEntry* pEntry;

    for (int i = 0; i < numEntries; i++) {
        pEntry = zipFile.getEntryByIndex(i);
        if (pEntry->isCompressed()) {
            if (verbose) {
                printf("%8ld %s (OK - compressed)\n",
                       (long) pEntry->getFileOffset(), pEntry->getFileName());
            }
        } else {
            long offset = pEntry->getFileOffset();
            const int alignTo = getAlignment(pageAlignSharedLibs, alignment, pEntry);
            if ((offset % alignTo) != 0) {
                if (verbose) {
                    printf("%8ld %s (BAD - %ld)\n",
                           (long) offset, pEntry->getFileName(),
                           offset % alignTo);
                }
                foundBad = true;
            } else {
                if (verbose) {
                    printf("%8ld %s (OK)\n",
                           (long) offset, pEntry->getFileName());
                }
            }
        }
    }

    if (verbose)
        printf("Verification %s\n", foundBad ? "FAILED" : "succesful");

    return foundBad ? 1 : 0;
}

/*
 * Parse args.
 */
int main(int argc, char* const argv[])
{
    bool wantUsage = false;
    bool check = false;
    bool force = false;
    bool verbose = false;
    bool zopfli = false;
    bool pageAlignSharedLibs = false;
    int result = 1;
    int alignment;
    char* endp;

    if (argc < 4) {
        wantUsage = true;
        goto bail;
    }

    argc--;
    argv++;

    while (argc && argv[0][0] == '-') {
        const char* cp = argv[0] +1;

        while (*cp != '\0') {
            switch (*cp) {
                case 'c':
                    check = true;
                    break;
                case 'f':
                    force = true;
                    break;
                case 'v':
                    verbose = true;
                    break;
                case 'z':
                    zopfli = true;
                    break;
                case 'p':
                    pageAlignSharedLibs = true;
                    break;
                default:
                    fprintf(stderr, "ERROR: unknown flag -%c\n", *cp);
                    wantUsage = true;
                    goto bail;
            }

            cp++;
        }

        argc--;
        argv++;
    }

    if (!((check && argc == 2) || (!check && argc == 3))) {
        wantUsage = true;
        goto bail;
    }

    alignment = strtol(argv[0], &endp, 10);
    if (*endp != '\0' || alignment <= 0) {
        fprintf(stderr, "Invalid value for alignment: %s\n", argv[0]);
        wantUsage = true;
        goto bail;
    }

    if (check) {
        /* check existing archive for correct alignment */
        result = verify(argv[1], alignment, verbose, pageAlignSharedLibs);
    } else {
        /* create the new archive */
        result = process(argv[1], argv[2], alignment, force, zopfli, pageAlignSharedLibs);

        /* trust, but verify */
        if (result == 0) {
            result = verify(argv[2], alignment, verbose, pageAlignSharedLibs);
        }
    }

    bail:
    if (wantUsage) {
        usage();
        result = 2;
    }

    return result;
}
