import struct
import ctypes
import sys

PATH = 'D:/csc445/BackGround-MultiCaster/'#this path must be changed to the location of the program
SPI_SETDESKWALLPAPER = 20

def is_64bit_windows():
    """Check if 64 bit Windows OS"""
    return struct.calcsize('P') * 8 == 64

def changeBG(path):
    """Change background depending on bit size"""
    if is_64bit_windows():
        ctypes.windll.user32.SystemParametersInfoW(SPI_SETDESKWALLPAPER, 0, PATH+path, 3)
    else:
        ctypes.windll.user32.SystemParametersInfoA(SPI_SETDESKWALLPAPER, 0, PATH+path, 3)

changeBG(sys.argv[1])