from __future__ import print_function
import os
import Image

colour1_coord = (10, 8)
colour1_base = 0xff/float(0xfb)

def tohex(c):
	return c[0] << 16 | c[1] << 8 | c[2]

def doone(typ):
	mylist = []
	for i in os.listdir("itemsout"):
		if not i.startswith(typ):
			continue
		img = Image.open("itemsout/" + i).convert("RGBA")
		colour1 = tuple(int(round(i * colour1_base)) for i in img.getpixel(colour1_coord)[0:3])
		n = int(i[i.rfind("_")+1:i.rfind(".")]) if i != typ + ".png" else 0
		mylist.append((n, colour1))
	print(typ)
	for i in sorted(mylist):
		print(hex(tohex(i[1])) + ",\t/* " + str(i[0]) + " */")

def main():
	for typ in ["potion_bottle_drinkable", "potion_bottle_splash"]:
		doone(typ)
	
if __name__ == "__main__":
	main()
