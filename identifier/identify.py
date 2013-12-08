from __future__ import print_function

import Image
import sys
import os



def processImages(mylist, mytexfolder, comptexfolder):
	mytexcontents = os.listdir(mytexfolder)
	comptexcontents = os.listdir(comptexfolder)
	comp_images = []
	resultslist = []

	for comptex in comptexcontents:
		if comptex[len(comptex) - len(".png"):] != ".png":
			print(comptex)
			continue
		
		comp = Image.open(comptexfolder + "/" + comptex)
		comp_images.append((comptex, comp, list(comp.getdata())))

	for i in mylist:
		if not i in mytexcontents:
			print("Not found in my tex folder! " + i)
			continue
		if i in comptexcontents:
			print("this is already there")
			continue
		mytex = Image.open(mytexfolder + "/" + i)
		compresult = compare(mytex, comp_images)
		resultslist.append((i, compresult))
	return resultslist

def compare(mytex, comp_images):
	mydata = list(mytex.getdata())
	ranking = []
	for img in comp_images:
		data = img[2]
		myerr = 0
		for pixelindex in xrange(len(mydata)):
			mypix = mydata[pixelindex]
			comppix = data[pixelindex]
			for i in xrange(3):
				myerr += (mypix[i] - comppix[i]) ** 2
		ranking.append((myerr, img))
	ranking.sort()
	return ranking[0][1][0]


def loadImageList(myfilename):
	mylist = []
	with open(myfilename, "r") as myfile:
		for l in myfile:
			mytemplist = l.strip().replace("[","").replace("]","").replace(" ","").split(",")
			mylist += mytemplist
	print(mylist)
	return mylist

def write_results(resultslist):
	with open("results.txt", "w") as mywrite:
		for i in resultslist:
			print(i[0].replace(".png","") + "," + i[1].replace(".png", ""), file=mywrite)


def main():
	if len(sys.argv) < 4:
		print("usage: python identify.py missingfiles.txt mytexfolder desktoptexfolder")
		return
	mylist = loadImageList(sys.argv[1])
	resultslist = processImages(mylist, sys.argv[2], sys.argv[3])
	write_results(resultslist)

if __name__ == "__main__":
	main()
