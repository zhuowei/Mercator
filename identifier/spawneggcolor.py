from __future__ import print_function
import os
import Image

colour1_coord = (7, 6)
colour1_base = 0xff/float(0xf2)
colour2_coord = (5, 9)
colour2_base = 0xff/float(0xe2)

def tohex(c):
	return c[0] << 16 | c[1] << 8 | c[2]

def main():
	mylist = []
	for i in os.listdir("itemsout"):
		if not (i.startswith("spawn_egg_") or i == "spawn_egg.png"):
			continue
		img = Image.open("itemsout/" + i).convert("RGBA")
		colour1 = tuple(int(round(i * colour1_base)) for i in img.getpixel(colour1_coord)[0:3])
		colour2 = tuple(int(round(i * colour2_base)) for i in img.getpixel(colour2_coord)[0:3])
		n = int(i[len("spawn_egg_"):i.rfind(".")]) if i != "spawn_egg.png" else 0
		mylist.append((n, colour1, colour2))
	for i in sorted(mylist):
		print(hex(tohex(i[1])) + ",", hex(tohex(i[2])) + ",\t/* " + str(i[0]) + " */")
	
if __name__ == "__main__":
	main()
