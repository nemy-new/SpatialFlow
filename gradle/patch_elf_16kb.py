#!/usr/bin/env python3
"""
patch_elf_16kb.py
Post-build ELF header patcher for Android 15 (API 35+) 16 KB Page Alignment compatibility.
Scans compiled .so native libraries in the build directory and updates any ELF program header
PT_LOAD segments with p_align < 16384 (0x4000) to 16384 (0x4000).
"""

import os
import sys
import struct

def patch_so_file(path):
    try:
        with open(path, "r+b") as f:
            data = bytearray(f.read())
            if len(data) < 64 or data[:4] != b"\x7fELF":
                return False
            
            elf_class = data[4]
            endian = "<" if data[5] == 1 else ">"
            patched = False
            
            if elf_class == 2:  # 64-bit ELF (arm64-v8a, x86_64)
                e_phoff, = struct.unpack_from(endian + "Q", data, 0x20)
                e_phentsize, e_phnum = struct.unpack_from(endian + "HH", data, 0x36)
                for i in range(e_phnum):
                    offset = e_phoff + i * e_phentsize
                    if offset + 0x38 > len(data):
                        continue
                    p_type, = struct.unpack_from(endian + "I", data, offset)
                    if p_type == 1:  # PT_LOAD
                        p_align, = struct.unpack_from(endian + "Q", data, offset + 0x30)
                        if 0 < p_align < 16384:
                            struct.pack_into(endian + "Q", data, offset + 0x30, 0x4000)
                            patched = True
            elif elf_class == 1:  # 32-bit ELF (armeabi-v7a, x86)
                e_phoff, = struct.unpack_from(endian + "I", data, 0x1c)
                e_phentsize, e_phnum = struct.unpack_from(endian + "HH", data, 0x2a)
                for i in range(e_phnum):
                    offset = e_phoff + i * e_phentsize
                    if offset + 0x20 > len(data):
                        continue
                    p_type, = struct.unpack_from(endian + "I", data, offset)
                    if p_type == 1:  # PT_LOAD
                        p_align, = struct.unpack_from(endian + "I", data, offset + 0x1c)
                        if 0 < p_align < 16384:
                            struct.pack_into(endian + "I", data, offset + 0x1c, 0x4000)
                            patched = True
            
            if patched:
                f.seek(0)
                f.write(data)
                print(f"[16KB Patch] Realigned ELF PT_LOAD segment alignment to 16KB (0x4000): {os.path.basename(path)}")
                return True
    except Exception as e:
        print(f"[16KB Patch] Warning: Could not patch {path}: {e}")
    return False

def main():
    search_dirs = []
    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            for p in arg.split(os.pathsep):
                if os.path.exists(p):
                    search_dirs.append(p)
    else:
        # Default search path if no args provided
        search_dirs.append("app/build/intermediates")

    paths_to_check = set()
    for target in search_dirs:
        if os.path.isfile(target) and target.endswith(".so"):
            paths_to_check.add(target)
        elif os.path.isdir(target):
            for root, _, files in os.walk(target):
                for file in files:
                    if file.endswith(".so"):
                        paths_to_check.add(os.path.join(root, file))

    count = 0
    for path in sorted(paths_to_check):
        if patch_so_file(path):
            count += 1

    if count > 0:
        print(f"[16KB Patch] Successfully realigned {count} native libraries for Android 15 compatibility.")
    else:
        print("[16KB Patch] All native libraries checked are already 16KB compatible or none found.")

if __name__ == "__main__":
    main()
